package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.content.ClipData
import android.content.Intent
import android.net.Uri
import android.text.format.Formatter
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.MapleDialog
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.MapleSettingsDefaults
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.MediaSelectionActions
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.Date

/** A dialog of the selection operations. */
internal sealed interface GalleryDialog {
    data class Details(val items: List<MediaItem>) : GalleryDialog
}

/**
 * The selection operations of the media grid (ROADMAP 7.1-7.4) for [HomeActivity]: resolves the selected images,
 * runs them through [MediaWriteCoordinator], owns the dialogs and the snackbar, and refreshes the gallery.
 * Construct it in `onCreate`.
 */
internal class GalleryOperations(private val activity: HomeActivity, private val gallery: GalleryViewModel) {

    val snackbar = SnackbarHostState()
    private val coordinator = MediaWriteCoordinator(activity)
    private var dialog by mutableStateOf<GalleryDialog?>(null)
    private var job: Job? = null

    /** What the bar offers for [state]'s album: authorized folders only share, delete, rename and show details. */
    fun actionsFor(state: MediaGridUiState): MediaSelectionActions {
        if (state.album == null) return MediaSelectionActions()
        return MediaSelectionActions(
            onShare = ::share,
            onDetails = ::details,
        )
    }

    private fun launch(block: suspend () -> Unit) {
        if (job?.isActive == true) return
        job = activity.lifecycleScope.launch { block() }
    }

    private fun show(value: GalleryDialog?) {
        dialog = value
        activity.composeDialogOpen = value != null
    }

    fun share() = launch {
        val items = gallery.selectedItems()
        if (items.isEmpty()) return@launch
        if (items.size > SHARE_LIMIT) {
            snackbar.showSnackbar(activity.getString(R.string.selection_share_limit, SHARE_LIMIT))
            return@launch
        }
        val uris = items.map { Uri.parse(it.contentUri) }
        val intent = if (uris.size == 1) {
            Intent(Intent.ACTION_SEND).putExtra(Intent.EXTRA_STREAM, uris.single())
        } else {
            Intent(Intent.ACTION_SEND_MULTIPLE).putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(uris))
        }
        intent.type = sharedType(items.map { it.mimeType })
        intent.clipData = ClipData.newRawUri(null, uris.first()).apply { uris.drop(1).forEach { addItem(ClipData.Item(it)) } }
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        runCatching { activity.startActivity(Intent.createChooser(intent, activity.getString(R.string.action_share))) }
            .onFailure { activity.showMessage(R.string.standalone_home_open_failed) }
    }

    fun details() = launch {
        val items = gallery.selectedItems()
        if (items.isNotEmpty()) show(GalleryDialog.Details(items))
    }

    @Composable
    fun Dialogs() {
        when (val current = dialog) {
            is GalleryDialog.Details -> DetailsDialog(current.items, onDismiss = { show(null) })
            null -> Unit
        }
    }

    companion object {
        /** Keeps the share intent well inside the binder transaction limit. */
        const val SHARE_LIMIT = 500

        /** The MIME type of a share: the common one, `image/<any>` across image types, otherwise any type. */
        fun sharedType(types: List<String>): String {
            val distinct = types.map { it.lowercase() }.distinct()
            return when {
                distinct.size == 1 -> distinct.single()
                distinct.all { it.startsWith("image/") } -> "image/*"
                else -> "*/*"
            }
        }
    }
}

/** Name, folder, type, size, resolution and dates of one image; count and total size of several. */
@Composable
private fun DetailsDialog(items: List<MediaItem>, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val dateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
    val rows = buildList {
        val single = items.singleOrNull()
        if (single != null) {
            add(stringResource(R.string.details_name) to single.displayName)
            add(stringResource(R.string.details_folder) to single.relativePath.ifEmpty { single.bucketName })
            add(stringResource(R.string.details_type) to single.mimeType)
            add(stringResource(R.string.details_size) to Formatter.formatFileSize(context, single.sizeBytes))
            if (single.width > 0 && single.height > 0) {
                add(stringResource(R.string.details_resolution) to stringResource(R.string.metadata_resolution, single.width, single.height))
            }
            single.dateTakenMillis?.let { add(stringResource(R.string.details_date_taken) to dateFormat.format(Date(it))) }
            add(stringResource(R.string.details_date_modified) to dateFormat.format(Date(single.dateModifiedSeconds * 1000L)))
        } else {
            add(stringResource(R.string.details_count) to items.size.toString())
            add(stringResource(R.string.details_total_size) to Formatter.formatFileSize(context, items.sumOf { it.sizeBytes }))
        }
    }
    MapleDialog(
        title = stringResource(R.string.label_details),
        onDismissRequest = onDismiss,
        confirmText = stringResource(R.string.dialog_button_confirm),
        onConfirm = onDismiss,
        dismissText = null,
        modifier = Modifier.testTag("maple.details"),
    ) {
        rows.forEach { (label, value) ->
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = MapleSettingsDefaults.horizontalPadding, vertical = 6.dp)) {
                Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(value, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}
