package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.app.Activity
import android.content.ClipData
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.activity.result.contract.ActivityResultContracts
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.autojs.plugin.explorer.api.ExplorerActionIntentExtras
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.materialDialog

internal class HomeActions(private val activity: HomeActivity) {
    private var mode = "view"
    private var pending: String? = null
    private var exportName = "image.png"
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
    fun populate(content: LinearLayout) {
        activity.addHomeAction(content, R.string.action_view_image, null, R.drawable.ic_article_24) { choose("view") }
        activity.addHomeAction(content, R.string.action_edit_image, null, R.drawable.ic_edit_24) { choose("edit") }
        activity.addHomeAction(content, R.string.action_convert_image, null, R.drawable.ic_swap_horiz_24) { choose("convert") }
        activity.pendingRow = activity.addHomeAction(content, R.string.standalone_home_save_output, null, R.drawable.ic_download_24) {
            activity.runWork { prepareExport() }
        }
    }
    private fun choose(selected: String) {
        val previous = pending
        if (previous != null && StandaloneImageWork.hasOutput(activity, previous)) {
            val dialog = activity.materialDialog()
                .setMessage(R.string.standalone_home_unsaved)
                .setNegativeButton(android.R.string.cancel, null)
                .setNeutralButton(R.string.standalone_home_save_output) { _, _ -> activity.runWork { prepareExport() } }
                .setPositiveButton(R.string.standalone_home_discard) { _, _ ->
                    StandaloneImageWork.discard(activity, previous)
                    pending = null
                    refresh()
                    choose(selected)
                }.show()
            activity.tintDialogButtons(dialog)
            return
        }
        mode = selected
        selectImage.launch(arrayOf("image/*"))
    }
    private fun open(uri: Uri) = activity.runWork {
        val document = withContext(Dispatchers.IO) { StandaloneDocuments.inspect(activity, uri) }
        if (mode == "view") {
            activity.startActivity(Intent(activity, ExternalViewActivity::class.java).apply {
                action = Intent.ACTION_VIEW
                setDataAndType(uri, document.mimeType.takeIf { it.startsWith("image/") } ?: "image/*")
                clipData = ClipData.newRawUri(document.name, uri)
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            })
        } else {
            // Keep a previous finished edit available until it is explicitly saved or replaced.
            val newId = withContext(Dispatchers.IO) {
                StandaloneImageWork.create(activity, document, if (mode == "edit") ImageToolsPlugin.EDIT_ACTION_ID else ImageToolsPlugin.CONVERT_ACTION_ID)
            }
            pending?.let { StandaloneImageWork.discard(activity, it) }
            pending = newId
            exportName = document.name.substringBeforeLast('.', document.name)
            val target = if (mode == "edit") StandaloneImageEditorActivity::class.java else StandaloneImageConverterActivity::class.java
            processImage.launch(Intent(activity, target).putExtra(StandaloneImageWork.EXTRA_SESSION, newId))
        }
    }
    private suspend fun prepareExport() {
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
        activity.pendingRow.visibility = if (pending?.let { StandaloneImageWork.hasOutput(activity, it) } == true) View.VISIBLE else View.GONE
    }
    fun save(state: Bundle) {
        state.putString("maple.mode", mode)
        state.putString("maple.pending", pending)
        state.putString("maple.exportName", exportName)
    }
    fun restore(state: Bundle?) {
        mode = state?.getString("maple.mode") ?: "view"
        pending = state?.getString("maple.pending")
        exportName = state?.getString("maple.exportName") ?: "image"
    }
}
