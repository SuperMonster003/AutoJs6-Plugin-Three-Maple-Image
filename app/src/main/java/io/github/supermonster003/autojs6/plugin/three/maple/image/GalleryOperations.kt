package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.content.ClipData
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.Settings
import android.text.format.Formatter
import android.widget.Toast
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.AlbumSelectionActions
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.ConflictDialog
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.MapleDialog
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.MapleSettingsDefaults
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.MediaSelectionActions
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.NewAlbumDialog
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.RenameDialog
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.TargetAlbum
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.TargetAlbumDialog
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.TransferProgressDialog
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

    /** Rename one image; [document] renames an authorized folder document instead of a library row. */
    data class Rename(val item: MediaItem, val document: Boolean) : GalleryDialog

    data class PickTarget(val items: List<MediaItem>, val copy: Boolean, val recent: List<TargetAlbum>, val albums: List<TargetAlbum>) : GalleryDialog

    data class NewAlbum(val items: List<MediaItem>, val copy: Boolean) : GalleryDialog

    data class Conflict(val items: List<MediaItem>, val copy: Boolean, val target: TargetAlbum, val count: Int) : GalleryDialog

    data class Progress(val copy: Boolean, val control: WriteControl) : GalleryDialog

    /** Delete whole albums (ROADMAP 7.4): their [items] go to the recycle bin, or for good when it is off. */
    data class ConfirmDeleteAlbums(val albumCount: Int, val items: List<MediaItem>, val forGood: Boolean) : GalleryDialog
}

internal enum class DeleteReason {
    /** The recycle bin switch is off. */
    RECYCLE_BIN_OFF,
    /** Android 7-10: the images alone exceed the private bin capacity. */
    TOO_LARGE,
    /** Authorized folders have no recycle bin (ROADMAP 7.0). */
    AUTHORIZED_FOLDER,
}

/** What the operations act on: the media grid selection on the home screen, or the viewer's current page. */
internal interface OperationTarget {
    /** The images to act on, in album order. */
    suspend fun items(): List<MediaItem>

    /** Kind of the open album; [AlbumKind.FOLDER] limits the operations to share, delete, rename and details. */
    val albumKind: AlbumKind?

    /** The open media library album (a move skips it as a target), or null. */
    val openAlbum: AlbumSummary?

    val favoriteIds: Set<Long>

    /** Media library albums for the move / copy picker. */
    suspend fun albums(): List<AlbumSummary>

    fun clearSelection()

    /** A write finished or was undone: reload what shows the images. */
    fun afterWrite()

    /** The albums selected on the album grid (ROADMAP 7.4); none outside the home screen. */
    fun selectedAlbums(): List<AlbumSummary> = emptyList()

    fun clearAlbumSelection() {}

    /** Every image of a media library album. */
    suspend fun albumItems(album: AlbumSummary): List<MediaItem> = emptyList()
}

/**
 * The management operations (ROADMAP 7.1-7.4) of the home screen (media grid selection, recycle bin page) and of
 * the viewer: resolves the [target] images, runs them through [MediaWriteCoordinator], owns the dialogs and the
 * snackbar (with "Undo" for deletes, moves and renames). [recycleBin] is the home screen's recycle bin page.
 * Construct it in `onCreate`.
 */
internal class GalleryOperations(
    private val activity: ConfiguredActivity,
    private val target: OperationTarget,
    private val recycleBin: RecycleBinViewModel? = null,
) {

    val snackbar = SnackbarHostState()
    private val coordinator = MediaWriteCoordinator(activity)
    private var dialog by mutableStateOf<GalleryDialog?>(null)
    private var job: Job? = null
    private var snackbarJob: Job? = null
    private val preferences = GalleryPreferences(activity)
    private val favorites = FavoritesRepository(activity)
    private val folderMarks = FolderMarksRepository(activity)
    private var renameError by mutableStateOf<String?>(null)
    private var renameBusy by mutableStateOf(false)
    private var progressDone by mutableIntStateOf(0)
    private var progressTotal by mutableIntStateOf(0)

    /** What the bar offers for [state]'s album: authorized folders only share, delete, rename and show details. */
    fun actionsFor(state: MediaGridUiState): MediaSelectionActions {
        val album = state.album ?: return MediaSelectionActions()
        val library = album.kind != AlbumKind.FOLDER
        return MediaSelectionActions(
            onShare = ::share,
            onFavorite = if (library) ::favorite else null,
            onMove = if (library) ::move else null,
            onDelete = ::delete,
            onCopy = if (library) ::copy else null,
            onRename = ::rename,
            onDetails = ::details,
            favorited = library && state.selection.isNotEmpty() && state.favoriteIds.containsAll(state.selection),
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

    /**
     * Android 7-10 with the storage write permission refused ("don't ask again"): say so and offer the app's
     * system settings, instead of a batch that silently did nothing. True when that was the reason.
     */
    private fun explainRefusedPermission(): Boolean {
        if (!coordinator.consumeWritePermissionRefused()) return false
        snackbarJob?.cancel()
        snackbarJob = activity.lifecycleScope.launch {
            val result = snackbar.showSnackbar(
                message = activity.getString(R.string.storage_write_refused),
                actionLabel = activity.getString(R.string.standalone_settings_title),
                duration = SnackbarDuration.Long,
            )
            if (result == SnackbarResult.ActionPerformed) {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", activity.packageName, null))
                runCatching { activity.startActivity(intent) }
            }
        }
        return true
    }

    private fun quantity(resource: Int, count: Int): String = activity.resources.getQuantityString(resource, count, count)

    private fun failedMessage(count: Int): String = quantity(R.plurals.gallery_failed_count, count)

    /** Snackbar text for a finished batch: the success line, plus the failures when some items were refused. */
    private fun summary(resource: Int, outcome: WriteOutcome): String = buildString {
        if (outcome.succeeded > 0) append(quantity(resource, outcome.succeeded))
        appendProblems(outcome)
    }

    /** Refused images, and storage card images Android 7-10 does not let the app change. */
    private fun StringBuilder.appendProblems(outcome: WriteOutcome) {
        if (outcome.failed > 0) {
            if (isNotEmpty()) append(". ")
            append(failedMessage(outcome.failed))
        }
        if (outcome.readOnlyCard > 0) {
            if (isNotEmpty()) append(". ")
            append(quantity(R.plurals.gallery_card_read_only_count, outcome.readOnlyCard))
        }
    }

    fun share() = launch {
        val items = target.items()
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
            .onFailure { Toast.makeText(activity, R.string.standalone_home_open_failed, Toast.LENGTH_LONG).show() }
    }

    fun details() = launch {
        val items = target.items()
        if (items.isNotEmpty()) show(GalleryDialog.Details(items))
    }

    // ---- favorites (ROADMAP 7.4) ----

    /** Adds the selection to the favorites, or removes it when every selected image is one already. */
    fun favorite() = launch {
        val items = target.items()
        if (items.isEmpty()) return@launch
        val known = target.favoriteIds
        val remove = items.all { it.id in known }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && ApplicationSettingsStore(activity).load().syncSystemFavorites) {
            if (coordinator.setSystemFavorite(items, favorite = !remove).cancelled) return@launch
        }
        if (remove) favorites.remove(items.map { it.id }) else favorites.add(items.filter { it.id !in known })
        target.clearSelection()
        notify(quantity(if (remove) R.plurals.favorites_removed_count else R.plurals.favorites_added_count, items.size))
    }

    // ---- delete (ROADMAP 7.2) ----

    /** Media library images go to the recycle bin unless it is switched off; folder documents ask first. */
    fun delete() = launch {
        val items = target.items()
        if (items.isEmpty()) return@launch
        when {
            target.albumKind == AlbumKind.FOLDER -> show(GalleryDialog.ConfirmDelete(items, DeleteReason.AUTHORIZED_FOLDER))
            !ApplicationSettingsStore(activity).load().recycleBinEnabled -> show(GalleryDialog.ConfirmDelete(items, DeleteReason.RECYCLE_BIN_OFF))
            else -> trash(items)
        }
    }

    private suspend fun trash(items: List<MediaItem>) {
        val outcome = coordinator.trash(items)
        when {
            outcome.cancelled -> explainRefusedPermission()
            outcome.tooLargeForRecycleBin -> show(GalleryDialog.ConfirmDelete(items, DeleteReason.TOO_LARGE))
            else -> {
                finishWrite()
                notify(summary(R.plurals.gallery_trashed_count, outcome), outcome.undo, afterUndo = ::refreshAll)
            }
        }
    }

    private fun deleteForGood(items: List<MediaItem>, reason: DeleteReason) = launch {
        val outcome = if (reason == DeleteReason.AUTHORIZED_FOLDER) deleteDocuments(items) else coordinator.delete(items)
        if (outcome.cancelled) {
            explainRefusedPermission()
            return@launch
        }
        if (reason != DeleteReason.AUTHORIZED_FOLDER) favorites.remove(items.map { it.id })
        finishWrite()
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

    private fun finishWrite() {
        target.clearSelection()
        refreshAll()
    }

    private fun refreshAll() {
        target.afterWrite()
        recycleBin?.load()
    }

    // ---- album actions (ROADMAP 7.4) ----

    /** The album selection bar for [state]; empty outside the album selection. */
    fun albumActionsFor(state: GalleryUiState): AlbumSelectionActions {
        if (!state.selectingAlbums) return AlbumSelectionActions()
        val selected = state.selectedAlbums
        return AlbumSelectionActions(
            onPin = ::pinAlbums,
            onExclude = ::excludeAlbums,
            onDelete = ::deleteAlbums,
            allPinned = selected.isNotEmpty() && selected.all { it.pinned },
        )
    }

    /** Pins the selected albums, or unpins them when every one is pinned already. */
    fun pinAlbums() = launch {
        val albums = target.selectedAlbums()
        if (albums.isEmpty()) return@launch
        val unpin = albums.all { it.pinned }
        val paths = albums.map { it.path }
        if (unpin) folderMarks.unmark(paths, FolderMark.PINNED) else folderMarks.mark(paths, FolderMark.PINNED)
        target.clearAlbumSelection()
        notify(quantity(if (unpin) R.plurals.albums_unpinned_count else R.plurals.albums_pinned_count, albums.size))
    }

    /** Leaves the selected albums out of the gallery; the undo (and Settings > Excluded folders) brings them back. */
    fun excludeAlbums() = launch {
        val albums = target.selectedAlbums()
        if (albums.isEmpty()) return@launch
        val paths = albums.map { it.path }
        folderMarks.mark(paths, FolderMark.EXCLUDED)
        target.clearAlbumSelection()
        notify(quantity(R.plurals.albums_excluded_count, albums.size), undo = {
            folderMarks.unmark(paths, FolderMark.EXCLUDED)
            WriteOutcome(succeeded = paths.size)
        })
    }

    /** Asks before deleting every image of the selected albums. */
    fun deleteAlbums() = launch {
        val albums = target.selectedAlbums()
        if (albums.isEmpty()) return@launch
        val items = albums.flatMap { target.albumItems(it) }
        if (items.isEmpty()) return@launch
        show(GalleryDialog.ConfirmDeleteAlbums(albums.size, items, forGood = !ApplicationSettingsStore(activity).load().recycleBinEnabled))
    }

    private fun deleteAlbumItems(items: List<MediaItem>, forGood: Boolean) = launch {
        target.clearAlbumSelection()
        if (forGood) {
            val outcome = coordinator.delete(items)
            if (outcome.cancelled) {
                explainRefusedPermission()
                return@launch
            }
            favorites.remove(items.map { it.id })
            finishWrite()
            notify(summary(R.plurals.gallery_deleted_count, outcome))
        } else {
            trash(items)
        }
    }

    // ---- rename / move / copy (ROADMAP 7.3) ----

    fun rename() = launch {
        val item = target.items().singleOrNull() ?: return@launch
        renameError = null
        show(GalleryDialog.Rename(item, document = target.albumKind == AlbumKind.FOLDER))
    }

    private fun runRename(item: MediaItem, document: Boolean, name: String) = launch {
        renameBusy = true
        val outcome = try {
            if (document) renameDocument(item, name) else coordinator.rename(item, name)
        } finally {
            renameBusy = false
        }
        when {
            outcome.nameTaken -> renameError = activity.getString(R.string.rename_error_taken)
            outcome.cancelled -> {
                show(null)
                explainRefusedPermission()
            }
            outcome.readOnlyCard > 0 -> renameError = quantity(R.plurals.gallery_card_read_only_count, 1)
            outcome.succeeded == 0 -> renameError = activity.getString(R.string.rename_error_failed)
            else -> {
                show(null)
                finishWrite()
                notify(activity.getString(R.string.gallery_renamed, name), outcome.undo, afterUndo = ::refreshAll)
            }
        }
    }

    /** Authorized folders rename through their documents provider; the undo renames the new document back. */
    private suspend fun renameDocument(item: MediaItem, name: String): WriteOutcome = withContext(Dispatchers.IO) {
        val resolver = activity.contentResolver
        val renamed = runCatching { DocumentsContract.renameDocument(resolver, Uri.parse(item.contentUri), name) }.getOrNull()
            ?: return@withContext WriteOutcome(failed = 1)
        WriteOutcome(succeeded = 1, undo = {
            withContext(Dispatchers.IO) {
                val back = runCatching { DocumentsContract.renameDocument(resolver, renamed, item.displayName) }.getOrNull()
                if (back != null) WriteOutcome(succeeded = 1) else WriteOutcome(failed = 1)
            }
        })
    }

    fun move() = pickTarget(copy = false)

    fun copy() = pickTarget(copy = true)

    private fun pickTarget(copy: Boolean) = launch {
        val items = target.items()
        if (items.isEmpty()) return@launch
        val (recent, albums) = targetAlbums(copy)
        show(GalleryDialog.PickTarget(items, copy, recent, albums))
    }

    /**
     * Writable albums of the media library (Android 10+ only accepts DCIM/ and Pictures/, Android 7-10 not a storage
     * card), moves skip the open one; an album on a storage card is labelled with the card's name.
     */
    private suspend fun targetAlbums(copy: Boolean): Pair<List<TargetAlbum>, List<TargetAlbum>> {
        val open = target.openAlbum
        val openPath = open?.takeIf { it.kind == AlbumKind.MEDIA }?.let { AlbumPath(it.volume, it.subtitle) }
        val albums = target.albums()
            .filter { it.kind == AlbumKind.MEDIA && it.subtitle.isNotEmpty() && MediaWriteCoordinator.isWritableAlbum(AlbumPath(it.volume, it.subtitle)) }
            .filter { copy || openPath == null || it.id != open.id }
            .map { TargetAlbum(it.name, AlbumPath(it.volume, it.subtitle), StorageVolumes.label(activity, it.volume)) }
            .distinctBy { GalleryPreferences.encodeTarget(it.path) }
        val byPath = albums.associateBy { GalleryPreferences.encodeTarget(it.path) }
        val recent = preferences.recentTargets
            .filter { MediaWriteCoordinator.isWritableAlbum(it) && (copy || openPath == null || !it.sameAs(openPath)) }
            .map { path -> byPath[GalleryPreferences.encodeTarget(path)] ?: TargetAlbum(path.relativePath.trimEnd('/').substringAfterLast('/'), path, StorageVolumes.label(activity, path.volume)) }
        return recent to albums
    }

    private fun chooseTarget(items: List<MediaItem>, copy: Boolean, target: TargetAlbum) = launch {
        show(null)
        val count = coordinator.conflicts(items, target.path)
        if (count > 0) show(GalleryDialog.Conflict(items, copy, target, count)) else transfer(items, copy, target, NameConflictChoice.AUTO_RENAME)
    }

    private fun transferLater(items: List<MediaItem>, copy: Boolean, target: TargetAlbum, conflict: NameConflictChoice) = launch {
        transfer(items, copy, target, conflict)
    }

    private suspend fun transfer(items: List<MediaItem>, copy: Boolean, target: TargetAlbum, conflict: NameConflictChoice) {
        val control = WriteControl { done, total ->
            progressDone = done
            progressTotal = total
        }
        progressDone = 0
        progressTotal = items.size
        if (items.size > 1) show(GalleryDialog.Progress(copy, control))
        val outcome = try {
            if (copy) coordinator.copy(items, target.path, conflict, control) else coordinator.move(items, target.path, conflict, control)
        } finally {
            if (dialog is GalleryDialog.Progress) show(null)
        }
        if (outcome.cancelled) {
            explainRefusedPermission()
            return
        }
        if (outcome.succeeded > 0) preferences.rememberTarget(target.path)
        finishWrite()
        val resource = if (copy) R.plurals.gallery_copied_count else R.plurals.gallery_moved_count
        val message = buildString {
            if (outcome.succeeded > 0) append(activity.resources.getQuantityString(resource, outcome.succeeded, outcome.succeeded, target.name))
            appendProblems(outcome)
        }
        if (message.isNotEmpty()) notify(message, if (copy) null else outcome.undo, afterUndo = ::refreshAll)
    }

    // ---- recycle bin page (ROADMAP 7.2) ----

    fun restoreFromBin() = launch {
        val recycleBin = recycleBin ?: return@launch
        val entries = recycleBin.selected()
        if (entries.isEmpty()) return@launch
        val outcome = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            coordinator.restoreTrashed(entries.map { it.model as Uri })
        } else {
            coordinator.restoreFromPrivateBin(entries.map { it.id })
        }
        if (outcome.cancelled) {
            explainRefusedPermission()
            return@launch
        }
        recycleBin.clearSelection()
        refreshAll()
        notify(summary(R.plurals.recycle_bin_restored_count, outcome))
    }

    fun deleteFromBin() {
        val entries = recycleBin?.selected().orEmpty()
        if (entries.isNotEmpty()) show(GalleryDialog.ConfirmBinDelete(entries, all = false))
    }

    fun emptyBin() {
        val entries = recycleBin?.state?.value?.entries.orEmpty()
        if (entries.isNotEmpty()) show(GalleryDialog.ConfirmBinDelete(entries, all = true))
    }

    private fun deleteBinEntries(entries: List<RecycleBinEntry>) = launch {
        val outcome = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            coordinator.deleteTrashed(entries.map { it.model as Uri })
        } else {
            coordinator.deleteFromPrivateBin(entries.map { it.id })
        }
        if (outcome.cancelled) {
            explainRefusedPermission()
            return@launch
        }
        // Android 11+ bin entries carry media ids; Android 7-10 favorites of binned images were rebound or dropped already.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) favorites.remove(entries.map { it.id })
        recycleBin?.clearSelection()
        recycleBin?.load()
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
            is GalleryDialog.ConfirmDeleteAlbums -> ConfirmDialog(
                title = pluralStringResource(R.plurals.delete_albums_title, current.albumCount, current.albumCount),
                message = pluralStringResource(
                    if (current.forGood) R.plurals.delete_albums_forever_message else R.plurals.delete_albums_bin_message,
                    current.items.size,
                    current.items.size,
                ),
                confirmText = stringResource(if (current.forGood) R.string.action_delete_forever else R.string.action_delete),
                onDismiss = { show(null) },
                onConfirm = {
                    show(null)
                    deleteAlbumItems(current.items, current.forGood)
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
            is GalleryDialog.Rename -> RenameDialog(
                name = current.item.displayName,
                externalError = renameError,
                busy = renameBusy,
                onDismiss = { show(null) },
                onConfirm = { name -> runRename(current.item, current.document, name) },
            )
            is GalleryDialog.PickTarget -> {
                TargetAlbumDialog(
                    copy = current.copy,
                    recent = current.recent,
                    albums = current.albums,
                    onNewAlbum = { show(GalleryDialog.NewAlbum(current.items, current.copy)) },
                    onPick = { target -> chooseTarget(current.items, current.copy, target) },
                    onDismiss = { show(null) },
                )
            }
            is GalleryDialog.NewAlbum -> NewAlbumDialog(
                onDismiss = { show(null) },
                onConfirm = { name -> chooseTarget(current.items, current.copy, TargetAlbum(name, AlbumPath(MediaCatalog.PRIMARY_VOLUME, "${Environment.DIRECTORY_PICTURES}/$name/"))) },
            )
            is GalleryDialog.Conflict -> ConflictDialog(
                count = current.count,
                onDismiss = { show(null) },
                onConfirm = { index ->
                    show(null)
                    val choice = when (index) {
                        1 -> NameConflictChoice.SKIP
                        2 -> NameConflictChoice.REPLACE
                        else -> NameConflictChoice.AUTO_RENAME
                    }
                    transferLater(current.items, current.copy, current.target, choice)
                },
            )
            is GalleryDialog.Progress -> TransferProgressDialog(
                copy = current.copy,
                done = progressDone,
                total = progressTotal,
                onCancel = current.control::cancel,
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
