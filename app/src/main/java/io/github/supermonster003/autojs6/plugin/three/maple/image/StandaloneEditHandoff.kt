package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.autojs.plugin.explorer.api.ExplorerActionIntentExtras

/**
 * Viewer -> editor hand-off for standalone and external entries (design draft 7.3, decision R5): the current
 * image is copied into a private edit session, the standalone editor runs, and a successful result is exported
 * through the system document creator, exactly like the home page does. Register it before the Activity starts.
 */
internal class StandaloneEditHandoff(
    private val activity: ComponentActivity,
    private val onBusy: (Boolean) -> Unit,
    private val onMessage: (Int) -> Unit,
) {
    private var pending: String? = null
    private var exportName = "image"

    private val editImage: ActivityResultLauncher<Intent> =
        activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val id = pending
            if (id != null && result.resultCode == Activity.RESULT_OK &&
                result.data?.getStringExtra(ExplorerActionIntentExtras.OUTPUT_TRANSACTION_ID) == id
            ) {
                launchExport(id)
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

    /** Copies the image behind [uri] into a private session and opens the standalone editor. */
    suspend fun edit(uri: Uri) {
        onBusy(true)
        try {
            val document = withContext(Dispatchers.IO) { StandaloneDocuments.inspect(activity, uri) }
            val id = withContext(Dispatchers.IO) { StandaloneImageWork.create(activity, document, ImageToolsPlugin.EDIT_ACTION_ID) }
            pending?.let { StandaloneImageWork.discard(activity, it) }
            pending = id
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
            putExtra(Intent.EXTRA_TITLE, "$exportName.$extension")
        })
    }

    fun discard() {
        pending?.let { StandaloneImageWork.discard(activity, it) }
        pending = null
    }
}
