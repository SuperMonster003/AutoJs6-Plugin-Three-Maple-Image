package io.github.supermonster003.autojs6.plugin.imagetools

import android.app.Application
import android.graphics.Bitmap
import android.os.Build
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
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
    val canRedo: Boolean = false,
    val canRestoreOriginal: Boolean = false,
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
    private val _historyWarnings = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    private var request: ImageToolsRequest? = null
    private var bitmap: Bitmap? = null
    private var sourceInfo: ImageBitmapIO.SourceInfo? = null
    private var currentStateId = INITIAL_STATE_ID
    private var savedStateId = INITIAL_STATE_ID
    private var stateIdCounter = INITIAL_STATE_ID
    private var activeJob: Job? = null

    val state: StateFlow<ImageEditorScreenState> = _state.asStateFlow()
    val operationErrors: SharedFlow<Unit> = _operationErrors.asSharedFlow()
    val historyWarnings: SharedFlow<Unit> = _historyWarnings.asSharedFlow()
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
                    val image = ImageBitmapIO.decodeForEditing(
                        getApplication<Application>().contentResolver,
                        value.inputUri,
                    )
                    Triple(info, image, history.initializeOriginal(image, INITIAL_STATE_ID))
                }
                sourceInfo = loaded.first
                decoded = loaded.second
                bitmap = decoded
                publish(loading = false, busy = false)
                if (!loaded.third) _historyWarnings.tryEmit(Unit)
            } catch (cancelled: CancellationException) {
                decoded?.takeUnless(Bitmap::isRecycled)?.recycle()
                throw cancelled
            } catch (error: Throwable) {
                decoded?.takeUnless(Bitmap::isRecycled)?.recycle()
                Log.e(LOG_TAG, "Unable to initialize image editor", error)
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
            var prepared: EditorHistoryStore.Prepared? = null
            try {
                prepared = withContext(Dispatchers.IO) { history.prepare(source, currentStateId) }
                transformed = withContext(Dispatchers.Default) { operation(source) }
                val edited = requireNotNull(transformed)
                val historyRetained = history.commit(prepared)
                prepared = null
                if (edited !== source && !source.isRecycled) source.recycle()
                bitmap = edited
                currentStateId = ++stateIdCounter
                publish(busy = false)
                if (!historyRetained) _historyWarnings.tryEmit(Unit)
            } catch (cancelled: CancellationException) {
                history.discard(prepared)
                transformed?.takeIf { it !== source && !it.isRecycled }?.recycle()
                throw cancelled
            } catch (_: Throwable) {
                history.discard(prepared)
                transformed?.takeIf { it !== source && !it.isRecycled }?.recycle()
                publish(busy = false)
                _operationErrors.tryEmit(Unit)
            }
        }
    }

    fun undo() {
        navigateHistory(history.canUndo, history::undo)
    }

    fun redo() {
        navigateHistory(history.canRedo, history::redo)
    }

    fun restoreOriginal() {
        navigateHistory(
            history.canRestoreOriginal && !history.isOriginalState(currentStateId),
            history::restoreOriginal,
        )
    }

    private fun navigateHistory(
        available: Boolean,
        operation: (Bitmap, Long) -> EditorHistoryStore.Restored,
    ) {
        val current = bitmap ?: return
        if (_state.value.busy || !available || _state.value.output !is OutputCommitState.Idle) return
        publish(busy = true)
        activeJob = viewModelScope.launch {
            var restored: EditorHistoryStore.Restored? = null
            try {
                restored = withContext(Dispatchers.IO) { operation(current, currentStateId) }
                if (!current.isRecycled) current.recycle()
                bitmap = restored.bitmap
                currentStateId = restored.stateId
                publish(busy = false)
                if (!restored.returnStateRetained) _historyWarnings.tryEmit(Unit)
            } catch (cancelled: CancellationException) {
                restored?.bitmap?.takeUnless(Bitmap::isRecycled)?.recycle()
                throw cancelled
            } catch (_: Throwable) {
                restored?.bitmap?.takeUnless(Bitmap::isRecycled)?.recycle()
                publish(busy = false)
                _historyWarnings.tryEmit(Unit)
            }
        }
    }

    fun save(saveOptions: EditorSaveOptions) {
        val activeRequest = request ?: return
        val currentBitmap = bitmap ?: return
        val detectedMimeType = sourceInfo?.mimeType ?: return
        if (_state.value.busy || _state.value.output !is OutputCommitState.Idle) return
        val conversionOptions = EditorSavePolicy.conversionOptions(
            saveOptions = saveOptions,
            detectedMimeType = detectedMimeType,
            allowedOutputMimeTypes = activeRequest.allowedOutputMimeTypes,
            targetSize = ImagePixelSize(currentBitmap.width, currentBitmap.height),
            sdkInt = Build.VERSION.SDK_INT,
        )
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
                    ImageBitmapIO.writeOutput(
                        resolver = getApplication<Application>().contentResolver,
                        outputUri = activeRequest.outputUri,
                        bitmap = currentBitmap,
                        options = conversionOptions,
                        maxOutputBytes = activeRequest.maxOutputBytes,
                        temporaryDirectory = getApplication<Application>().cacheDir,
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
            canRedo = history.canRedo,
            canRestoreOriginal = history.canRestoreOriginal && !history.isOriginalState(currentStateId),
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
    TARGET_SIZE_CONFIRMATION,
    SUCCEEDED,
    SOURCE_FAILED,
    OUTPUT_FAILED,
}

internal data class ImageConverterScreenState(
    val stage: ConverterStage = ConverterStage.LOADING,
    val sourceInfo: ImageBitmapIO.SourceInfo? = null,
    val outputError: Throwable? = null,
    val outputAlreadyClaimed: Boolean = false,
    val targetFileSizeResult: ImageTargetFileSizeResult? = null,
)

internal class ImageConverterViewModel(application: Application) : AndroidViewModel(application) {

    private val outputLedger = OutputTransactionLedger(application)
    private val outputGuard = SingleUseOutputCommitGuard()
    private val _state = MutableStateFlow(ImageConverterScreenState())
    private var request: ImageToolsRequest? = null
    private var conversionJob: Job? = null
    private var pendingTargetOutput: PendingTargetOutput? = null

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
            } catch (error: Throwable) {
                Log.e(LOG_TAG, "Unable to inspect converter source", error)
                _state.value = ImageConverterScreenState(ConverterStage.SOURCE_FAILED)
            }
        }
    }

    fun convert(options: ImageConversionOptions) {
        val activeRequest = request ?: return
        if (_state.value.stage != ConverterStage.READY) return
        _state.value = _state.value.copy(
            stage = ConverterStage.CONVERTING,
            outputError = null,
            targetFileSizeResult = null,
        )
        conversionJob = viewModelScope.launch {
            var bitmap: Bitmap? = null
            try {
                val targetFileSizeResult = withContext(Dispatchers.IO) {
                    bitmap = ImageBitmapIO.decodeForConversion(
                        getApplication<Application>().contentResolver,
                        activeRequest.inputUri,
                        options.targetSize,
                    )
                    options.targetFileSizeBytes?.let {
                        ImageBitmapIO.selectTargetFileSize(
                            bitmap = requireNotNull(bitmap),
                            options = options,
                            maxOutputBytes = activeRequest.maxOutputBytes,
                            temporaryDirectory = getApplication<Application>().cacheDir,
                        )
                    }
                }
                val resolvedOptions = targetFileSizeResult?.let { result ->
                    options.copy(quality = result.quality, targetFileSizeBytes = null)
                } ?: options
                if (targetFileSizeResult?.requiresConfirmation == true) {
                    pendingTargetOutput = PendingTargetOutput(
                        bitmap = requireNotNull(bitmap),
                        options = resolvedOptions,
                        targetFileSizeResult = targetFileSizeResult,
                    )
                    bitmap = null
                    _state.value = _state.value.copy(
                        stage = ConverterStage.TARGET_SIZE_CONFIRMATION,
                        targetFileSizeResult = targetFileSizeResult,
                    )
                    return@launch
                }

                if (!claimOutput(activeRequest)) return@launch
                val writeResult = writeOutput(activeRequest, requireNotNull(bitmap), resolvedOptions)
                check(outputGuard.succeed())
                _state.value = _state.value.copy(
                    stage = ConverterStage.SUCCEEDED,
                    targetFileSizeResult = targetFileSizeResult?.copy(
                        encodedBytes = writeResult.encodedBytes,
                    ),
                )
            } catch (cancelled: CancellationException) {
                if (outputGuard.currentState == SingleUseOutputCommitGuard.State.RUNNING) outputGuard.fail()
                throw cancelled
            } catch (error: Throwable) {
                if (outputGuard.currentState == SingleUseOutputCommitGuard.State.RUNNING) outputGuard.fail()
                _state.value = _state.value.copy(
                    stage = ConverterStage.OUTPUT_FAILED,
                    outputError = error,
                )
            } finally {
                bitmap?.takeUnless(Bitmap::isRecycled)?.recycle()
                conversionJob = null
            }
        }
    }

    fun confirmClosestTargetOutput() {
        val activeRequest = request ?: return
        if (_state.value.stage != ConverterStage.TARGET_SIZE_CONFIRMATION) return
        val pending = pendingTargetOutput ?: return
        pendingTargetOutput = null
        _state.value = _state.value.copy(stage = ConverterStage.CONVERTING)
        conversionJob = viewModelScope.launch {
            try {
                if (!claimOutput(activeRequest)) return@launch
                val writeResult = writeOutput(activeRequest, pending.bitmap, pending.options)
                check(outputGuard.succeed())
                _state.value = _state.value.copy(
                    stage = ConverterStage.SUCCEEDED,
                    targetFileSizeResult = pending.targetFileSizeResult.copy(
                        encodedBytes = writeResult.encodedBytes,
                    ),
                )
            } catch (cancelled: CancellationException) {
                if (outputGuard.currentState == SingleUseOutputCommitGuard.State.RUNNING) outputGuard.fail()
                throw cancelled
            } catch (error: Throwable) {
                if (outputGuard.currentState == SingleUseOutputCommitGuard.State.RUNNING) outputGuard.fail()
                _state.value = _state.value.copy(
                    stage = ConverterStage.OUTPUT_FAILED,
                    outputError = error,
                )
            } finally {
                pending.bitmap.takeUnless(Bitmap::isRecycled)?.recycle()
                conversionJob = null
            }
        }
    }

    fun cancelClosestTargetOutput() {
        if (_state.value.stage != ConverterStage.TARGET_SIZE_CONFIRMATION) return
        pendingTargetOutput?.bitmap?.takeUnless(Bitmap::isRecycled)?.recycle()
        pendingTargetOutput = null
    }

    private fun claimOutput(activeRequest: ImageToolsRequest): Boolean {
        if (!outputGuard.tryStart()) {
            _state.value = _state.value.copy(
                stage = ConverterStage.OUTPUT_FAILED,
                outputError = IllegalStateException("Output transaction is no longer available"),
            )
            return false
        }
        if (!outputLedger.tryClaim(activeRequest.transactionId)) {
            outputGuard.fail()
            _state.value = _state.value.copy(
                stage = ConverterStage.OUTPUT_FAILED,
                outputAlreadyClaimed = true,
            )
            return false
        }
        return true
    }

    private suspend fun writeOutput(
        activeRequest: ImageToolsRequest,
        bitmap: Bitmap,
        options: ImageConversionOptions,
    ): ImageOutputWriteResult = withContext(Dispatchers.IO) {
        ImageBitmapIO.writeOutput(
            resolver = getApplication<Application>().contentResolver,
            outputUri = activeRequest.outputUri,
            bitmap = bitmap,
            options = options,
            maxOutputBytes = activeRequest.maxOutputBytes,
            temporaryDirectory = getApplication<Application>().cacheDir,
        )
    }

    override fun onCleared() {
        conversionJob?.cancel()
        pendingTargetOutput?.bitmap?.takeUnless(Bitmap::isRecycled)?.recycle()
        pendingTargetOutput = null
        super.onCleared()
    }

    private data class PendingTargetOutput(
        val bitmap: Bitmap,
        val options: ImageConversionOptions,
        val targetFileSizeResult: ImageTargetFileSizeResult,
    )
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
    val webpLossless: Boolean = false,
    val targetFileSizeEnabled: Boolean = false,
    val targetFileSizeKibibytes: String = ImageTargetFileSizePolicy.DEFAULT_TARGET_KIBIBYTES.toString(),
    val longEdge: String = ImageConversionSizing.DEFAULT_LONG_EDGE.toString(),
    val preserveExifMetadata: Boolean = false,
)

private const val LOG_TAG = "ImageToolsViewModel"
