package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.app.Activity
import android.content.ClipData
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.HomeUiState
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.MapleDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.autojs.plugin.explorer.api.ExplorerActionIntentExtras

/**
 * File flows of the standalone home: pick a document, open it in the viewer or a private edit / convert session,
 * and export the finished result through the system document creator. UI facing state lives in [uiState].
 */
internal class HomeActions(private val activity: HomeActivity) {
    private var mode = MODE_VIEW
    private var pending: String? = null
    private var exportName = "image"
    private var pendingDisplayName: String? = null
    internal var uiState by mutableStateOf(HomeUiState())
    private var unsavedPromptFor by mutableStateOf<String?>(null)

    private val selectImage = activity.registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) open(uri)
    }
    private val processImage = activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val id = pending
        if (id != null && result.resultCode == Activity.RESULT_OK && result.data?.getStringExtra(ExplorerActionIntentExtras.OUTPUT_TRANSACTION_ID) == id) {
            activity.runWork { prepareExport() }
        } else if (id != null) {
            StandaloneImageWork.discard(activity, id)
            pending = null
            refresh()
        }
    }
    private val saveImage = activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val uri = result.data?.data
        val id = pending
        if (result.resultCode == Activity.RESULT_OK && uri != null && id != null) {
            activity.runWork {
                withContext(Dispatchers.IO) { StandaloneImageWork.export(activity, id, uri) }
                pending = null
                activity.showMessage(R.string.standalone_home_saved)
            }
        }
    }

    fun choose(selected: String) {
        val previous = pending
        if (previous != null && StandaloneImageWork.hasOutput(activity, previous)) {
            unsavedPromptFor = selected
            activity.composeDialogOpen = true
            return
        }
        mode = selected
        selectImage.launch(arrayOf("image/*"))
    }

    /** "Save or discard the current result first" prompt; the neutral button exports, confirm discards. */
    @Composable
    fun Dialogs() {
        val requested = unsavedPromptFor ?: return
        val close = { unsavedPromptFor = null; activity.composeDialogOpen = false }
        MapleDialog(
            title = activity.getString(R.string.text_unsaved_changes),
            onDismissRequest = close,
            confirmText = activity.getString(R.string.standalone_home_discard),
            onConfirm = {
                close()
                pending?.let { StandaloneImageWork.discard(activity, it) }
                pending = null
                refresh()
                choose(requested)
            },
            dismissText = activity.getString(R.string.dialog_button_cancel),
            neutralText = activity.getString(R.string.standalone_home_save_output),
            onNeutral = { close(); activity.runWork { prepareExport() } },
        ) {
            Text(
                text = activity.getString(R.string.standalone_home_unsaved),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
            )
        }
    }

    private fun open(uri: Uri) = activity.runWork {
        val document = withContext(Dispatchers.IO) { StandaloneDocuments.inspect(activity, uri) }
        if (mode == MODE_VIEW) {
            activity.startActivity(Intent(activity, ExternalViewActivity::class.java).apply {
                action = Intent.ACTION_VIEW
                setDataAndType(uri, document.mimeType.takeIf { it.startsWith("image/") } ?: "image/*")
                clipData = ClipData.newRawUri(document.name, uri)
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            })
        } else {
            // Keep a previous finished edit available until it is explicitly saved or replaced.
            val newId = withContext(Dispatchers.IO) {
                StandaloneImageWork.create(activity, document, if (mode == MODE_EDIT) ImageToolsPlugin.EDIT_ACTION_ID else ImageToolsPlugin.CONVERT_ACTION_ID)
            }
            pending?.let { StandaloneImageWork.discard(activity, it) }
            pending = newId
            exportName = document.name.substringBeforeLast('.', document.name)
            pendingDisplayName = document.name
            val target = if (mode == MODE_EDIT) StandaloneImageEditorActivity::class.java else StandaloneImageConverterActivity::class.java
            processImage.launch(Intent(activity, target).putExtra(StandaloneImageWork.EXTRA_SESSION, newId))
        }
    }

    suspend fun prepareExport() {
        val id = pending ?: return
        val mime = withContext(Dispatchers.IO) { StandaloneImageWork.outputMime(activity, id) }
        val extension = when (mime) { "image/jpeg" -> "jpg"; "image/webp" -> "webp"; else -> "png" }
        saveImage.launch(Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = mime
            putExtra(Intent.EXTRA_TITLE, "$exportName.$extension")
        })
    }

    fun refresh() {
        val hasOutput = pending?.let { StandaloneImageWork.hasOutput(activity, it) } == true
        uiState = uiState.copy(pendingOutputName = if (hasOutput) pendingDisplayName ?: exportName else null)
    }

    fun save(state: Bundle) {
        state.putString("maple.mode", mode)
        state.putString("maple.pending", pending)
        state.putString("maple.exportName", exportName)
        state.putString("maple.pendingDisplayName", pendingDisplayName)
        state.putString("maple.unsavedPromptFor", unsavedPromptFor)
    }

    fun restore(state: Bundle?) {
        mode = state?.getString("maple.mode") ?: MODE_VIEW
        pending = state?.getString("maple.pending")
        exportName = state?.getString("maple.exportName") ?: "image"
        pendingDisplayName = state?.getString("maple.pendingDisplayName")
        unsavedPromptFor = state?.getString("maple.unsavedPromptFor")
        activity.composeDialogOpen = unsavedPromptFor != null
    }

    companion object {
        const val MODE_VIEW = "view"
        const val MODE_EDIT = "edit"
        const val MODE_CONVERT = "convert"
    }
}
