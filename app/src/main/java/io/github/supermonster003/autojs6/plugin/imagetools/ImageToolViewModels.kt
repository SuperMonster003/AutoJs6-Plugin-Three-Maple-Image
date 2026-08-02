package io.github.supermonster003.autojs6.plugin.imagetools

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal sealed interface OutputCommitState {
    data object Idle : OutputCommitState
    data object Running : OutputCommitState
    data object Succeeded : OutputCommitState
    data class Failed(val error: Throwable?, val alreadyClaimed: Boolean = false) : OutputCommitState
}

internal data class ImageEditorScreenState(
    val loading: Boolean = true,
    val loadFailed: Boolean = false,
    val bitmap: Bitmap? = null,
    val sourceInfo: ImageBitmapIO.SourceInfo? = null,
    val busy: Boolean = true,
    val canUndo: Boolean = false,
    val currentStateId: Long = 0L,
    val savedStateId: Long = 0L,
    val output: OutputCommitState = OutputCommitState.Idle,
)

internal class ImageEditorViewModel(application: Application) : AndroidViewModel(application) {

    private val history = EditorHistoryStore(application.cacheDir)
    private val outputLedger = OutputTransactionLedger(application)
    private val outputGuard = SingleUseOutputCommitGuard()
    private val _state = MutableStateFlow(ImageEditorScreenState())
    private val _operationErrors = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    private var request: ImageToolsRequest? = null
    private var bitmap: Bitmap? = null
    private var sourceInfo: ImageBitmapIO.SourceInfo? = null
    private var currentStateId = INITIAL_STATE_ID
    private var savedStateId = INITIAL_STATE_ID
    private var stateIdCounter = INITIAL_STATE_ID
    private var activeJob: Job? = null

    val state: StateFlow<ImageEditorScreenState> = _state.asStateFlow()
    val operationErrors: SharedFlow<Unit> = _operationErrors.asSharedFlow()
    var uiState: EditorUiState = EditorUiState()

    fun initialize(value: ImageToolsRequest) {
        if (request != null) return
        request = value
        if (outputLedger.isClaimed(value.transactionId)) {
            _state.value = _state.value.copy(
                loading = false,
                busy = false,
                output = OutputCommitState.Failed(error = null, alreadyClaimed = true),
            )
            return
        }
        activeJob = viewModelScope.launch {
            var decoded: Bitmap? = null
            try {
                val loaded = withContext(Dispatchers.IO) {
                    val info = ImageBitmapIO.inspect(getApplication<Application>().contentResolver, value.inputUri)
                    info to ImageBitmapIO.decodeForEditing(getApplication<Application>().contentResolver, value.inputUri)
                }
                sourceInfo = loaded.first
                decoded = loaded.second
                bitmap = decoded
                publish(loading = false, busy = false)
            } catch (cancelled: CancellationException) {
                decoded?.takeUnless(Bitmap::isRecycled)?.recycle()
                throw cancelled
            } catch (_: Throwable) {
                decoded?.takeUnless(Bitmap::isRecycled)?.recycle()
                publish(loading = false, loadFailed = true, busy = false)
            }
        }
    }

    fun commitOperation(operation: (Bitmap) -> Bitmap) {
        val source = bitmap ?: return
        if (_state.value.busy || _state.value.output !is OutputCommitState.Idle) return
        publish(busy = true)
        activeJob = viewModelScope.launch {
            var transformed: Bitmap? = null
            var snapshotPushed = false
            try {
                withContext(Dispatchers.IO) { history.push(source, currentStateId) }
                snapshotPushed = true
                transformed = withContext(Dispatchers.Default) { operation(source) }
                val edited = requireNotNull(transformed)
                if (edited !== source && !source.isRecycled) source.recycle()
                bitmap = edited
                currentStateId = ++stateIdCounter
                publish(busy = false)
            } catch (cancelled: CancellationException) {
                transformed?.takeIf { it !== source && !it.isRecycled }?.recycle()
                throw cancelled
            } catch (_: Throwable) {
                transformed?.takeIf { it !== source && !it.isRecycled }?.recycle()
                if (snapshotPushed) history.discardLast()
                publish(busy = false)
                _operationErrors.tryEmit(Unit)
            }
        }
    }

    fun undo() {
        val current = bitmap ?: return
        if (_state.value.busy || !history.canUndo || _state.value.output !is OutputCommitState.Idle) return
        publish(busy = true)
        activeJob = viewModelScope.launch {
            var restored: EditorHistoryStore.Restored? = null
            try {
                restored = withContext(Dispatchers.IO) { history.pop() }
                if (!current.isRecycled) current.recycle()
                bitmap = restored.bitmap
                currentStateId = restored.stateId
                publish(busy = false)
            } catch (cancelled: CancellationException) {
                restored?.bitmap?.takeUnless(Bitmap::isRecycled)?.recycle()
                throw cancelled
            } catch (_: Throwable) {
                restored?.bitmap?.takeUnless(Bitmap::isRecycled)?.recycle()
                publish(busy = false)
                _operationErrors.tryEmit(Unit)
            }
        }
    }

    fun save() {
        val activeRequest = request ?: return
        val currentBitmap = bitmap ?: return
        val detectedMimeType = sourceInfo?.mimeType ?: return
        if (_state.value.busy || _state.value.output !is OutputCommitState.Idle) return
        if (!outputGuard.tryStart()) return
        if (!outputLedger.tryClaim(activeRequest.transactionId)) {
            outputGuard.fail()
            publish(
                busy = false,
                output = OutputCommitState.Failed(error = null, alreadyClaimed = true),
            )
            return
        }
        publish(busy = true, output = OutputCommitState.Running)
        activeJob = viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    var format = ImageBitmapIO.outputFormatForDetectedMime(detectedMimeType)
                    if (format.mimeType !in activeRequest.allowedOutputMimeTypes) {
                        format = ImageOutputFormat.entries.firstOrNull {
                            it.mimeType in activeRequest.allowedOutputMimeTypes
                        } ?: throw IOException("No permitted image output format is available")
                    }
                    ImageBitmapIO.writeOutput(
                        resolver = getApplication<Application>().contentResolver,
                        outputUri = activeRequest.outputUri,
                        bitmap = currentBitmap,
                        options = ImageConversionOptions(
                            format = format,
                            targetSize = ImagePixelSize(currentBitmap.width, currentBitmap.height),
                        ),
                        maxOutputBytes = activeRequest.maxOutputBytes,
                    )
                }
                check(outputGuard.succeed())
                savedStateId = currentStateId
                publish(busy = false, output = OutputCommitState.Succeeded)
            } catch (cancelled: CancellationException) {
                outputGuard.fail()
                throw cancelled
            } catch (error: Throwable) {
                outputGuard.fail()
                publish(busy = false, output = OutputCommitState.Failed(error))
            }
        }
    }

    fun cancelNonOutputWork(): Boolean {
        if (_state.value.output is OutputCommitState.Running) return false
        activeJob?.cancel()
        return true
    }

    private fun publish(
        loading: Boolean = _state.value.loading,
        loadFailed: Boolean = _state.value.loadFailed,
        busy: Boolean = _state.value.busy,
        output: OutputCommitState = _state.value.output,
    ) {
        _state.value = ImageEditorScreenState(
            loading = loading,
            loadFailed = loadFailed,
            bitmap = bitmap,
            sourceInfo = sourceInfo,
            busy = busy,
            canUndo = history.canUndo,
            currentStateId = currentStateId,
            savedStateId = savedStateId,
            output = output,
        )
    }

    override fun onCleared() {
        val job = activeJob
        job?.cancel()
        if (job?.isActive == true) {
            job.invokeOnCompletion { clearOwnedResources() }
        } else {
            clearOwnedResources()
        }
        super.onCleared()
    }

    private fun clearOwnedResources() {
        bitmap?.takeUnless { it.isRecycled }?.recycle()
        bitmap = null
        history.clear()
    }

    private companion object {
        const val INITIAL_STATE_ID = 0L
    }
}

internal enum class ConverterStage {
    LOADING,
    READY,
    CONVERTING,
    SUCCEEDED,
    SOURCE_FAILED,
    OUTPUT_FAILED,
}

internal data class ImageConverterScreenState(
    val stage: ConverterStage = ConverterStage.LOADING,
    val sourceInfo: ImageBitmapIO.SourceInfo? = null,
    val outputError: Throwable? = null,
    val outputAlreadyClaimed: Boolean = false,
)

internal class ImageConverterViewModel(application: Application) : AndroidViewModel(application) {

    private val outputLedger = OutputTransactionLedger(application)
    private val outputGuard = SingleUseOutputCommitGuard()
    private val _state = MutableStateFlow(ImageConverterScreenState())
    private var request: ImageToolsRequest? = null

    val state: StateFlow<ImageConverterScreenState> = _state.asStateFlow()
    var draft: ConversionDialogDraft? = null

    fun initialize(value: ImageToolsRequest) {
        if (request != null) return
        request = value
        if (outputLedger.isClaimed(value.transactionId)) {
            _state.value = ImageConverterScreenState(
                stage = ConverterStage.OUTPUT_FAILED,
                outputAlreadyClaimed = true,
            )
            return
        }
        viewModelScope.launch {
            try {
                val sourceInfo = withContext(Dispatchers.IO) {
                    ImageBitmapIO.inspect(getApplication<Application>().contentResolver, value.inputUri)
                }
                _state.value = ImageConverterScreenState(ConverterStage.READY, sourceInfo)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Throwable) {
                _state.value = ImageConverterScreenState(ConverterStage.SOURCE_FAILED)
            }
        }
    }

    fun convert(options: ImageConversionOptions) {
        val activeRequest = request ?: return
        if (_state.value.stage != ConverterStage.READY) return
        if (!outputGuard.tryStart()) return
        if (!outputLedger.tryClaim(activeRequest.transactionId)) {
            outputGuard.fail()
            _state.value = ImageConverterScreenState(
                stage = ConverterStage.OUTPUT_FAILED,
                sourceInfo = _state.value.sourceInfo,
                outputAlreadyClaimed = true,
            )
            return
        }
        _state.value = _state.value.copy(stage = ConverterStage.CONVERTING)
        viewModelScope.launch {
            var bitmap: Bitmap? = null
            try {
                withContext(Dispatchers.IO) {
                    bitmap = ImageBitmapIO.decodeForConversion(
                        getApplication<Application>().contentResolver,
                        activeRequest.inputUri,
                        options.targetSize,
                    )
                    ImageBitmapIO.writeOutput(
                        resolver = getApplication<Application>().contentResolver,
                        outputUri = activeRequest.outputUri,
                        bitmap = requireNotNull(bitmap),
                        options = options,
                        maxOutputBytes = activeRequest.maxOutputBytes,
                    )
                }
                check(outputGuard.succeed())
                _state.value = _state.value.copy(stage = ConverterStage.SUCCEEDED)
            } catch (cancelled: CancellationException) {
                outputGuard.fail()
                throw cancelled
            } catch (error: Throwable) {
                outputGuard.fail()
                _state.value = _state.value.copy(
                    stage = ConverterStage.OUTPUT_FAILED,
                    outputError = error,
                )
            } finally {
                bitmap?.takeUnless(Bitmap::isRecycled)?.recycle()
            }
        }
    }
}

internal data class ConversionDialogDraft(
    val formatMimeType: String,
    val quality: Int,
    val resizeMode: ImageResizeMode,
    val percentage: String,
    val width: String,
    val height: String,
    val lockAspectRatio: Boolean,
    val jpegBackgroundBlack: Boolean,
)
