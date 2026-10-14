package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.app.Application
import android.os.Binder
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.BatchConvertScreen
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.BatchProgressDialog
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.MapleDialog
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.MapleSettingsDefaults
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.MapleTheme
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.batchFailureResource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.autojs.plugin.explorer.api.ExplorerActionHostSessionKeys
import org.autojs.plugin.explorer.api.ExplorerActionHostSessionValues
import org.autojs.plugin.explorer.api.IExplorerActionHostSession
import java.io.IOException
import java.util.UUID

/**
 * The new files of one host batch conversion (ROADMAP 7.6): each image is a create-in-parent output transaction
 * (`prepareOutput` with automatic renaming, written once, read back through `openPendingOutput`), and [commit]
 * publishes them together: one through `commitOutput`, two or more as a v15 output batch, which the host publishes
 * completely or rolls back. [abort] takes back whatever was not published.
 */
internal class HostBatchOutputs(private val session: IExplorerActionHostSession) {

    private val clientToken = Binder()
    private val transactions = ArrayList<String>()
    private var batchId: String? = null
    private var committed = false

    init {
        // Ties the outputs to this process: the host rolls them back if the plugin dies before publishing.
        session.attachClient(clientToken)
    }

    val count: Int get() = transactions.size

    /** Reserves `displayName` (renamed by the host when taken), lets [write] fill it, and checks it reads back. */
    fun add(displayName: String, mimeType: String, write: (open: () -> ParcelFileDescriptor) -> ConvertedImage) {
        val prepared = session.prepareOutput(displayName, mimeType, ExplorerActionHostSessionValues.OUTPUT_CONFLICT_AUTO_RENAME)
            ?: throw IOException("The host reserved no output")
        val id = prepared.getString(ExplorerActionHostSessionKeys.OUTPUT_TRANSACTION_ID)?.takeIf(::isUuid)
            ?: throw IOException("The host output ID is invalid")
        transactions += id
        val converted = write { session.openOutput(id) ?: throw IOException("The host opened no output") }
        BatchImageConverter.verify(session.openPendingOutput(id) ?: throw IOException("The host output cannot be read back"), converted)
    }

    /** Publishes every output at once; returns how many. Throws when the host did not publish them all. */
    fun commit(): Int {
        check(!committed) { "The outputs were published already" }
        when (transactions.size) {
            0 -> Unit
            1 -> {
                val result = session.commitOutput(transactions.single()) ?: throw IOException("The host returned no commit result")
                val state = result.getInt(ExplorerActionHostSessionKeys.OUTPUT_STATE, ExplorerActionHostSessionValues.OUTPUT_STATE_COMMITTED)
                if (state != ExplorerActionHostSessionValues.OUTPUT_STATE_COMMITTED) throw IOException("The host did not publish the output")
            }
            else -> commitBatch()
        }
        committed = true
        return transactions.size
    }

    private fun commitBatch() {
        val prepared = session.prepareOutputBatch(ArrayList(transactions)) ?: throw IOException("The host prepared no output batch")
        val id = prepared.getString(ExplorerActionHostSessionKeys.OUTPUT_BATCH_ID)?.takeIf(::isUuid)
            ?: throw IOException("The host output batch ID is invalid")
        batchId = id
        if (prepared.getInt(ExplorerActionHostSessionKeys.OUTPUT_BATCH_STATE) != ExplorerActionHostSessionValues.OUTPUT_BATCH_STATE_PREPARED) {
            throw IOException("The host did not prepare the output batch")
        }
        val result = try {
            session.commitOutputBatch(id)
        } catch (error: Exception) {
            // The commit may have finished on the host although the reply was lost.
            val recovered = runCatching { session.queryOutputBatch(id) }.getOrNull()
            if (recovered != null && isCompleteBatch(recovered)) return
            throw error
        } ?: throw IOException("The host returned no output batch result")
        if (!isCompleteBatch(result)) throw IOException("The host did not publish the whole output batch")
    }

    private fun isCompleteBatch(result: Bundle): Boolean =
        result.getInt(ExplorerActionHostSessionKeys.OUTPUT_BATCH_STATE) == ExplorerActionHostSessionValues.OUTPUT_BATCH_STATE_COMMITTED &&
            result.getInt(ExplorerActionHostSessionKeys.OUTPUT_BATCH_PUBLISHED_COUNT, -1) == transactions.size

    /** Rolls back every output that was not published (best effort; the host also rolls back on session end). */
    fun abort() {
        if (committed) return
        batchId?.let { id -> runCatching { session.abortOutputBatch(id) } }
        transactions.forEach { id -> runCatching { session.abortOutput(id) } }
        transactions.clear()
        batchId = null
    }

    private fun isUuid(value: String): Boolean = runCatching { UUID.fromString(value).toString().equals(value, ignoreCase = true) }.getOrDefault(false)
}

/** Where the host entry's batch is. */
internal sealed interface HostBatchStage {
    data object Editing : HostBatchStage

    data class Running(val done: Int, val total: Int, val cancelling: Boolean = false) : HostBatchStage

    data class Saved(val count: Int) : HostBatchStage

    /** Nothing was saved: [name] is the image that failed, or null when the host refused to publish. */
    data class Failed(val name: String?, val failure: BatchFailure) : HostBatchStage

    data object Cancelled : HostBatchStage
}

/**
 * The host entry's batch (ROADMAP 7.6): owns the request and its host session for the whole screen (a rotation keeps
 * both), converts the targets one after the other, then publishes all outputs together; any failure or "Cancel"
 * rolls every output back. The session is closed when the screen goes away.
 */
internal class HostBatchConversionViewModel(application: Application) : AndroidViewModel(application) {

    private val _settings = MutableStateFlow(BatchConversionSettings())
    val settings: StateFlow<BatchConversionSettings> = _settings.asStateFlow()

    private val _stage = MutableStateFlow<HostBatchStage>(HostBatchStage.Editing)
    val stage: StateFlow<HostBatchStage> = _stage.asStateFlow()

    var request: ExplorerImageRequest? = null
        private set

    @Volatile
    private var cancelRequested = false
    private var job: Job? = null
    private var outputs: HostBatchOutputs? = null

    fun attach(value: ExplorerImageRequest) {
        if (request == null) request = value
    }

    fun update(value: BatchConversionSettings) {
        if (_stage.value == HostBatchStage.Editing) _settings.value = value
    }

    fun start() {
        val active = request ?: return
        val session = active.hostSession ?: return
        if (_stage.value != HostBatchStage.Editing || BatchConversionPolicy.settingsError(_settings.value) != null) return
        val settings = _settings.value
        val targets = active.targets
        cancelRequested = false
        _stage.value = HostBatchStage.Running(0, targets.size)
        job = viewModelScope.launch {
            _stage.value = withContext(Dispatchers.IO) { run(session, targets, settings) }
        }
    }

    fun cancel() {
        val running = _stage.value as? HostBatchStage.Running ?: return
        cancelRequested = true
        _stage.value = running.copy(cancelling = true)
    }

    private fun run(session: IExplorerActionHostSession, targets: List<ExplorerImageTarget>, settings: BatchConversionSettings): HostBatchStage {
        val app = getApplication<Application>()
        val batch = try {
            HostBatchOutputs(session).also { outputs = it }
        } catch (error: Exception) {
            return HostBatchStage.Failed(null, BatchFailure.WRITE_FAILED)
        }
        for ((index, target) in targets.withIndex()) {
            if (cancelRequested) {
                batch.abort()
                return HostBatchStage.Cancelled
            }
            val name = target.image.displayName
            try {
                batch.add(BatchConversionPolicy.outputName(name, settings.format), settings.format.mimeType) { open ->
                    BatchImageConverter.convert(app.contentResolver, target.image.targetUri, settings, app.cacheDir, open)
                }
            } catch (error: BatchItemException) {
                batch.abort()
                return HostBatchStage.Failed(name, error.failure)
            } catch (error: OutOfMemoryError) {
                batch.abort()
                return HostBatchStage.Failed(name, BatchFailure.OUT_OF_MEMORY)
            } catch (error: Exception) {
                batch.abort()
                return HostBatchStage.Failed(name, BatchFailure.WRITE_FAILED)
            }
            _stage.value = HostBatchStage.Running(index + 1, targets.size, cancelRequested)
        }
        if (cancelRequested) {
            batch.abort()
            return HostBatchStage.Cancelled
        }
        return try {
            HostBatchStage.Saved(batch.commit())
        } catch (error: Exception) {
            batch.abort()
            HostBatchStage.Failed(null, BatchFailure.WRITE_FAILED)
        }
    }

    override fun onCleared() {
        job?.cancel()
        outputs?.abort()
        request?.hostSession?.let { session -> runCatching { session.close() } }
        request = null
        super.onCleared()
    }
}

/**
 * "Convert images" from the AutoJs6 file manager's selection toolbar (ROADMAP 7.6): the batch settings page for the
 * selected images, a progress dialog, and the new files next to the originals, published only when every image
 * converted. Needs no storage permission: the host session reserves, receives and publishes the files.
 */
class HostBatchConverterActivity : ConfiguredActivity() {

    private val model by viewModels<HostBatchConversionViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (model.request == null) {
            val resolved = ImageRequestPolicy.resolveExplorer(intent)
                ?.takeIf { it.mode == ExplorerImageRequestMode.BATCH_CONVERT && it.hostSession != null }
            if (resolved == null) {
                ImageRequestPolicy.closeExplorerSession(intent)
                finish()
                return
            }
            model.attach(resolved)
        }
        val count = model.request?.targets?.size ?: 0
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() = leave()
        })
        setContent {
            MapleTheme(palette = appPalette) {
                val settings by model.settings.collectAsState()
                val stage by model.stage.collectAsState()
                BatchConvertScreen(
                    count = count,
                    settings = settings,
                    note = stringResource(R.string.batch_convert_note_host),
                    onChange = model::update,
                    onStart = model::start,
                    onBack = ::leave,
                    enabled = stage == HostBatchStage.Editing,
                ) {
                    when (val current = stage) {
                        is HostBatchStage.Running -> BatchProgressDialog(
                            done = current.done,
                            total = current.total,
                            failed = 0,
                            cancelling = current.cancelling,
                            onCancel = model::cancel,
                        )
                        is HostBatchStage.Failed -> MapleDialog(
                            title = stringResource(R.string.batch_host_failed_title),
                            onDismissRequest = ::finish,
                            confirmText = stringResource(R.string.dialog_button_confirm),
                            onConfirm = ::finish,
                            dismissText = null,
                            modifier = Modifier.testTag("maple.batch.hostFailed"),
                        ) {
                            val reason = stringResource(batchFailureResource(current.failure))
                            Text(
                                text = current.name?.let { stringResource(R.string.batch_host_failed_message, it, reason) } ?: reason,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = MapleSettingsDefaults.horizontalPadding),
                            )
                        }
                        else -> Unit
                    }
                }
                LaunchedEffect(stage) {
                    when (val current = stage) {
                        is HostBatchStage.Saved -> {
                            Toast.makeText(this@HostBatchConverterActivity, resources.getQuantityString(R.plurals.batch_converted_count, current.count, current.count), Toast.LENGTH_LONG).show()
                            setResult(RESULT_OK)
                            finish()
                        }
                        HostBatchStage.Cancelled -> {
                            Toast.makeText(this@HostBatchConverterActivity, R.string.batch_host_cancelled, Toast.LENGTH_LONG).show()
                            finish()
                        }
                        else -> Unit
                    }
                }
            }
        }
    }

    /** Back on the settings page leaves; while converting it asks the batch to stop (nothing is saved then). */
    private fun leave() {
        if (model.stage.value is HostBatchStage.Running) model.cancel() else finish()
    }
}
