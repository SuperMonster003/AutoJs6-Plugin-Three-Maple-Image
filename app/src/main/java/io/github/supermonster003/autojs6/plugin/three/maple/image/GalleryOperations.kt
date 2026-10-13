package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.content.ClipData
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.text.format.Formatter
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.MapleDialog
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.MapleSettingsDefaults
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.MediaSelectionActions
import java.text.DateFormat
import java.util.Date
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** A dialog of the selection operations. */
internal sealed interface GalleryDialog {
    data class Details(val items: List<MediaItem>) : GalleryDialog

    /** Permanent delete of media library images or authorized folder documents ([reason] picks the message). */
    data class ConfirmDelete(val items: List<MediaItem>, val reason: DeleteReason) : GalleryDialog

    /** Permanent delete from the recycle bin page; [all] empties it. */
    data class ConfirmBinDelete(val entries: List<RecycleBinEntry>, val all: Boolean) : GalleryDialog
}

internal enum class DeleteReason {
    /** The recycle bin switch is off. */
    RECYCLE_BIN_OFF,
    /** Android 7-10: the images alone exceed the private bin capacity. */
    TOO_LARGE,
    /** Authorized folders have no recycle bin (ROADMAP 7.0). */
    AUTHORIZED_FOLDER,
}

/**
 * The selection operations of the media grid and the recycle bin page (ROADMAP 7.1-7.4) for [HomeActivity]:
 * resolves the selected images, runs them through [MediaWriteCoordinator], owns the dialogs and the snackbar
 * (with "Undo" for deletes, moves and renames), and refreshes the gallery. Construct it in `onCreate`.
 */
internal class GalleryOperations(
    private val activity: HomeActivity,
    private val gallery: GalleryViewModel,
    private val recycleBin: RecycleBinViewModel,
) {

    val snackbar = SnackbarHostState()
    private val coordinator = MediaWriteCoordinator(activity)
    private var dialog by mutableStateOf<GalleryDialog?>(null)
    private var job: Job? = null
    private var snackbarJob: Job? = null

    /** What the bar offers for [state]'s album: authorized folders only share, delete, rename and show details. */
    fun actionsFor(state: MediaGridUiState): MediaSelectionActions {
        if (state.album == null) return MediaSelectionActions()
        return MediaSelectionActions(
            onShare = ::share,
            onDelete = ::delete,
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

    /** Shows [message] without blocking the next operation; [undo] adds the action and runs it when pressed. */
    private fun notify(message: String, undo: (suspend () -> WriteOutcome)? = null, afterUndo: () -> Unit = {}) {
        snackbarJob?.cancel()
        snackbarJob = activity.lifecycleScope.launch {
            val result = snackbar.showSnackbar(
                message = message,
                actionLabel = if (undo != null) activity.getString(R.string.action_undo) else null,
                withDismissAction = undo == null,
                duration = if (undo != null) SnackbarDuration.Long else SnackbarDuration.Short,
            )
            if (result == SnackbarResult.ActionPerformed && undo != null) {
                val outcome = undo()
                afterUndo()
                if (outcome.failed > 0) notify(failedMessage(outcome.failed))
            }
        }
    }

    private fun quantity(resource: Int, count: Int): String = activity.resources.getQuantityString(resource, count, count)

    private fun failedMessage(count: Int): String = quantity(R.plurals.gallery_failed_count, count)

    /** Snackbar text for a finished batch: the success line, plus the failures when some items were refused. */
    private fun summary(resource: Int, outcome: WriteOutcome): String = buildString {
        if (outcome.succeeded > 0) append(quantity(resource, outcome.succeeded))
        if (outcome.failed > 0) {
            if (isNotEmpty()) append(". ")
            append(failedMessage(outcome.failed))
        }
    }

    fun share() = launch {
        val items = gallery.selectedItems()
        if (items.isEmpty()) return@launch
        if (items.size > SHARE_LIMIT) {
            notify(activity.getString(R.string.selection_share_limit, SHARE_LIMIT))
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

    // ---- delete (ROADMAP 7.2) ----

    /** Media library images go to the recycle bin unless it is switched off; folder documents ask first. */
    fun delete() = launch {
        val items = gallery.selectedItems()
        if (items.isEmpty()) return@launch
        when {
            gallery.grid.value.album?.kind == AlbumKind.FOLDER -> show(GalleryDialog.ConfirmDelete(items, DeleteReason.AUTHORIZED_FOLDER))
            !ApplicationSettingsStore(activity).load().recycleBinEnabled -> show(GalleryDialog.ConfirmDelete(items, DeleteReason.RECYCLE_BIN_OFF))
            else -> trash(items)
        }
    }

    private suspend fun trash(items: List<MediaItem>) {
        val outcome = coordinator.trash(items)
        when {
            outcome.cancelled -> Unit
            outcome.tooLargeForRecycleBin -> show(GalleryDialog.ConfirmDelete(items, DeleteReason.TOO_LARGE))
            else -> {
                afterGridWrite(items)
                notify(summary(R.plurals.gallery_trashed_count, outcome), outcome.undo, afterUndo = ::refreshAll)
            }
        }
    }

    private fun deleteForGood(items: List<MediaItem>, reason: DeleteReason) = launch {
        val outcome = if (reason == DeleteReason.AUTHORIZED_FOLDER) deleteDocuments(items) else coordinator.delete(items)
        if (outcome.cancelled) return@launch
        afterGridWrite(items)
        notify(summary(R.plurals.gallery_deleted_count, outcome))
    }

    private suspend fun deleteDocuments(items: List<MediaItem>): WriteOutcome = withContext(Dispatchers.IO) {
        var done = 0
        for (item in items) {
            val deleted = runCatching { DocumentsContract.deleteDocument(activity.contentResolver, Uri.parse(item.contentUri)) }.getOrDefault(false)
            if (deleted) done++
        }
        WriteOutcome(succeeded = done, failed = items.size - done)
    }

    private fun afterGridWrite(items: List<MediaItem>) {
        gallery.deselect(items.map { it.id })
        gallery.clearSelection()
        refreshAll()
    }

    private fun refreshAll() {
        gallery.refreshAfterWrite()
        recycleBin.load()
    }

    // ---- recycle bin page (ROADMAP 7.2) ----

    fun restoreFromBin() = launch {
        val entries = recycleBin.selected()
        if (entries.isEmpty()) return@launch
        val outcome = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            coordinator.restoreTrashed(entries.map { it.model as Uri })
        } else {
            coordinator.restoreFromPrivateBin(entries.map { it.id })
        }
        if (outcome.cancelled) return@launch
        recycleBin.clearSelection()
        refreshAll()
        notify(summary(R.plurals.recycle_bin_restored_count, outcome))
    }

    fun deleteFromBin() {
        val entries = recycleBin.selected()
        if (entries.isNotEmpty()) show(GalleryDialog.ConfirmBinDelete(entries, all = false))
    }

    fun emptyBin() {
        val entries = recycleBin.state.value.entries
        if (entries.isNotEmpty()) show(GalleryDialog.ConfirmBinDelete(entries, all = true))
    }

    private fun deleteBinEntries(entries: List<RecycleBinEntry>) = launch {
        val outcome = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            coordinator.deleteTrashed(entries.map { it.model as Uri })
        } else {
            coordinator.deleteFromPrivateBin(entries.map { it.id })
        }
        if (outcome.cancelled) return@launch
        recycleBin.clearSelection()
        recycleBin.load()
        notify(summary(R.plurals.gallery_deleted_count, outcome))
    }

    @Composable
    fun Dialogs() {
        when (val current = dialog) {
            is GalleryDialog.Details -> DetailsDialog(current.items, onDismiss = { show(null) })
            is GalleryDialog.ConfirmDelete -> ConfirmDialog(
                title = stringResource(R.string.gallery_delete_confirm_title),
                message = when (current.reason) {
                    DeleteReason.RECYCLE_BIN_OFF -> pluralStringResource(R.plurals.gallery_delete_confirm_message, current.items.size, current.items.size)
                    DeleteReason.TOO_LARGE -> stringResource(R.string.gallery_delete_too_large)
                    DeleteReason.AUTHORIZED_FOLDER -> pluralStringResource(R.plurals.gallery_delete_confirm_message, current.items.size, current.items.size) +
                        "\n\n" + stringResource(R.string.gallery_delete_folder_note)
                },
                confirmText = stringResource(R.string.action_delete_forever),
                onDismiss = { show(null) },
                onConfirm = {
                    show(null)
                    deleteForGood(current.items, current.reason)
                },
            )
            is GalleryDialog.ConfirmBinDelete -> ConfirmDialog(
                title = stringResource(R.string.gallery_delete_confirm_title),
                message = if (current.all) {
                    stringResource(R.string.recycle_bin_empty_confirm)
                } else {
                    pluralStringResource(R.plurals.gallery_delete_confirm_message, current.entries.size, current.entries.size)
                },
                confirmText = stringResource(if (current.all) R.string.recycle_bin_empty_action else R.string.action_delete_forever),
                onDismiss = { show(null) },
                onConfirm = {
                    show(null)
                    deleteBinEntries(current.entries)
                },
            )
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

/** A destructive confirmation: the message and a confirm button named after the action. */
@Composable
private fun ConfirmDialog(title: String, message: String, confirmText: String, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    MapleDialog(
        title = title,
        onDismissRequest = onDismiss,
        confirmText = confirmText,
        onConfirm = onConfirm,
        dismissText = stringResource(R.string.dialog_button_cancel),
        modifier = Modifier.testTag("maple.confirm"),
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = MapleSettingsDefaults.horizontalPadding),
        )
    }
}
