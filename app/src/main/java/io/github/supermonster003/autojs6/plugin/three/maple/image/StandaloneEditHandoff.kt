package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.app.Activity
import android.content.Intent
import android.net.Uri
import java.io.File
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.autojs.plugin.explorer.api.ExplorerActionIntentExtras

/**
 * Viewer -> editor hand-off for standalone and external entries (design draft 7.3, decision R5): the current
 * image is copied into a private edit session, the standalone editor runs, and a successful result is exported
 * through the system document creator, exactly like the home page does, or written over the original when the
 * user chose to overwrite it (ROADMAP 8.2) through [overwrite]. Register it before the Activity starts.
 */
internal class StandaloneEditHandoff(
    private val activity: ComponentActivity,
    private val onBusy: (Boolean) -> Unit,
    private val onMessage: (Int) -> Unit,
    /** Writes the output over the original; false keeps the edit, which is then saved as a new file. */
    private val overwrite: (suspend (original: Uri, output: File) -> Boolean)? = null,
) {
    private var pending: String? = null
    private var pendingOriginal: Uri? = null
    private var exportName = "image"
    private var exportSuffix = "edited"

    private val editImage: ActivityResultLauncher<Intent> =
        activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val id = pending
            if (id != null && result.resultCode == Activity.RESULT_OK &&
                result.data?.getStringExtra(ExplorerActionIntentExtras.OUTPUT_TRANSACTION_ID) == id
            ) {
                val original = pendingOriginal
                val write = overwrite
                if (result.data?.getBooleanExtra(StandaloneImageWork.EXTRA_OVERWRITE, false) == true && original != null && write != null) {
                    activity.lifecycleScope.launch { overwriteOrExport(id, original, write) }
                } else {
                    launchExport(id)
                }
            } else if (id != null) {
                StandaloneImageWork.discard(activity, id)
                pending = null
            }
        }

    private val saveImage: ActivityResultLauncher<Intent> =
        activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val uri = result.data?.data
            val id = pending
            if (result.resultCode == Activity.RESULT_OK && uri != null && id != null) {
                runCatching { StandaloneImageWork.export(activity, id, uri) }
                    .onSuccess { onMessage(R.string.standalone_home_saved) }
                    .onFailure { onMessage(R.string.standalone_home_open_failed) }
                pending = null
            }
            // Keeping the session when the save dialog is cancelled lets the user try another location later.
        }

    private suspend fun overwriteOrExport(id: String, original: Uri, write: suspend (Uri, File) -> Boolean) {
        val output = runCatching { withContext(Dispatchers.IO) { StandaloneImageWork.outputFile(activity, id) } }.getOrNull()
        if (output != null && write(original, output)) {
            StandaloneImageWork.discard(activity, id)
            if (pending == id) pending = null
        } else if (output != null) {
            launchExport(id)
        } else {
            StandaloneImageWork.discard(activity, id)
            if (pending == id) pending = null
            onMessage(R.string.standalone_home_open_failed)
        }
    }

    /** Copies the image behind [uri] into a private session and opens the standalone editor. */
    suspend fun edit(uri: Uri) {
        onBusy(true)
        try {
            val document = withContext(Dispatchers.IO) { StandaloneDocuments.inspect(activity, uri) }
            val overwritable = overwrite != null && withContext(Dispatchers.IO) { StandaloneOverwrite.isOverwritable(activity, document) }
            val id = withContext(Dispatchers.IO) { StandaloneImageWork.create(activity, document, ImageToolsPlugin.EDIT_ACTION_ID, overwritable) }
            pending?.let { StandaloneImageWork.discard(activity, it) }
            pending = id
            pendingOriginal = uri
            exportName = document.name.substringBeforeLast('.', document.name)
            editImage.launch(Intent(activity, StandaloneImageEditorActivity::class.java).putExtra(StandaloneImageWork.EXTRA_SESSION, id))
        } catch (error: Exception) {
            onMessage(R.string.standalone_home_open_failed)
        } finally {
            onBusy(false)
        }
    }

    private fun launchExport(id: String) {
        val mime = runCatching { StandaloneImageWork.outputMime(activity, id) }.getOrNull()
        if (mime == null) {
            StandaloneImageWork.discard(activity, id)
            pending = null
            onMessage(R.string.standalone_home_open_failed)
            return
        }
        val extension = when (mime) { "image/jpeg" -> "jpg"; "image/webp" -> "webp"; else -> "png" }
        saveImage.launch(Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = mime
            putExtra(Intent.EXTRA_TITLE, "${exportName}_$exportSuffix.$extension")
        })
    }

    fun discard() {
        pending?.let { StandaloneImageWork.discard(activity, it) }
        pending = null
    }
}
