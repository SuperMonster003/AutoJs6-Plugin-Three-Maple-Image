package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.Manifest
import android.app.Activity
import android.app.PendingIntent
import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.StatFs
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.resume

/**
 * Result of one write batch. [undo] reverses it when the batch can be reverted (trash, move, rename), and is
 * null otherwise (permanent delete, copy, nothing done).
 */
internal data class WriteOutcome(
    val succeeded: Int = 0,
    val failed: Int = 0,
    val cancelled: Boolean = false,
    /** Android 7-10: the files alone exceed the recycle bin capacity; the caller may delete them for good instead. */
    val tooLargeForRecycleBin: Boolean = false,
    /** A rename / move target already holds the name and the batch was not attempted. */
    val nameTaken: Boolean = false,
    /** Android 7-10: images on a storage card, which the app cannot change there; they were left as they are. */
    val readOnlyCard: Int = 0,
    val undo: (suspend () -> WriteOutcome)? = null,
)

/**
 * Writes to the media library for the gallery (ROADMAP 7.1): one system confirmation per batch on Android 11+
 * (`createTrashRequest` / `createDeleteRequest` / `createWriteRequest`, granted silently with media management
 * access), direct writes with all files access or for the app's own items, and `java.io.File` operations plus a
 * media scan on Android 7-10, where deletes go to the private recycle bin. The route is chosen by
 * [MediaWritePolicy]. Call it from the main thread; each confirmation registers its own one-shot result callback,
 * so the coordinator can be created at any point of the activity's life.
 */
internal class MediaWriteCoordinator(private val activity: ComponentActivity) {

    private val context: Context get() = activity
    private val resolver: ContentResolver get() = activity.contentResolver
    private val media by lazy { MediaStoreRepository(activity) }
    private val recycleBin by lazy { PrivateRecycleBin(activity) }

    private var requestCount = 0

    /** Set when Android 7-10 refused the storage write permission for the last batch; read once by the caller. */
    private var writePermissionRefused = false

    fun consumeWritePermissionRefused(): Boolean = writePermissionRefused.also { writePermissionRefused = false }

    private fun facts(owned: Boolean = false) = MediaWriteFacts(
        sdk = Build.VERSION.SDK_INT,
        allFilesAccess = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && Environment.isExternalStorageManager(),
        manageMedia = canManageMedia(context),
        legacyWriteGranted = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED,
        ownedByApp = owned,
    )

    /**
     * Android 7-10 writes through `java.io.File`, which can change the internal storage but not a storage card (apps
     * only get their own folder there): such albums are read-only for the gallery.
     */
    private fun readOnly(path: AlbumPath): Boolean = Build.VERSION.SDK_INT < Build.VERSION_CODES.R && !path.isPrimary

    /** [items] without the read-only ones, and how many those were (reported in [WriteOutcome.readOnlyCard]). */
    private fun splitReadOnly(items: List<MediaItem>): Pair<List<MediaItem>, Int> {
        val (writable, readOnly) = items.partition { !readOnly(it.albumPath) }
        return writable to readOnly.size
    }

    /** Moves [items] to the recycle bin (system trash on Android 11+, private bin on Android 7-10). */
    suspend fun trash(items: List<MediaItem>): WriteOutcome {
        val (writable, onCard) = splitReadOnly(items)
        if (writable.isEmpty()) return WriteOutcome(readOnlyCard = onCard)
        val outcome = when (val route = routeFor(MediaWriteOperation.TRASH, writable) ?: return WriteOutcome(cancelled = true)) {
            MediaWriteRoute.PRIVATE_BIN -> trashToPrivateBin(writable)
            else -> {
                val uris = writable.map(::uriOf)
                val outcome = setTrashed(uris, trashed = true, route)
                if (outcome.succeeded > 0) outcome.copy(undo = { setTrashed(uris, trashed = false, routeFor(MediaWriteOperation.RESTORE, writable) ?: MediaWriteRoute.SYSTEM_REQUEST) }) else outcome
            }
        }
        return outcome.copy(readOnlyCard = onCard)
    }

    /** Takes system-trashed images (Android 11+) out of the trash again. */
    @RequiresApi(Build.VERSION_CODES.R)
    suspend fun restoreTrashed(uris: List<Uri>): WriteOutcome =
        if (uris.isEmpty()) WriteOutcome() else setTrashed(uris, trashed = false, routeForUris(MediaWriteOperation.RESTORE, uris) ?: return WriteOutcome(cancelled = true))

    /** Deletes [items] for good: the recycle bin is switched off, or the files do not fit into it. */
    suspend fun delete(items: List<MediaItem>): WriteOutcome {
        val (writable, onCard) = splitReadOnly(items)
        if (writable.isEmpty()) return WriteOutcome(readOnlyCard = onCard)
        val route = routeFor(MediaWriteOperation.DELETE, writable) ?: return WriteOutcome(cancelled = true)
        return deleteUris(writable.map(::uriOf), route).copy(readOnlyCard = onCard)
    }

    /** Deletes system-trashed images for good (Android 11+ recycle bin view). */
    @RequiresApi(Build.VERSION_CODES.R)
    suspend fun deleteTrashed(uris: List<Uri>): WriteOutcome =
        if (uris.isEmpty()) WriteOutcome() else deleteUris(uris, routeForUris(MediaWriteOperation.DELETE, uris) ?: return WriteOutcome(cancelled = true))

    val privateRecycleBin: PrivateRecycleBin get() = recycleBin

    /** Android 7-10 recycle bin view: puts entries back (asking for the storage write permission if needed). */
    suspend fun restoreFromPrivateBin(ids: List<Long>): WriteOutcome {
        if (ids.isEmpty()) return WriteOutcome()
        routeForIds(MediaWriteOperation.RESTORE, emptyList()) ?: return WriteOutcome(cancelled = true)
        val (restored, paths) = recycleBin.restore(ids)
        scan(paths)
        return WriteOutcome(succeeded = restored, failed = ids.size - restored)
    }

    suspend fun deleteFromPrivateBin(ids: List<Long>): WriteOutcome =
        if (ids.isEmpty()) WriteOutcome() else WriteOutcome(succeeded = recycleBin.deletePermanently(ids))

    /** Renames one image inside its album. */
    suspend fun rename(item: MediaItem, newName: String): WriteOutcome {
        if (newName == item.displayName) return WriteOutcome()
        if (readOnly(item.albumPath)) return WriteOutcome(readOnlyCard = 1)
        val siblings = namesIn(item.albumPath, excludeId = item.id)
        if (siblings.any { it.equals(newName, ignoreCase = true) }) return WriteOutcome(nameTaken = true)
        val route = routeFor(MediaWriteOperation.RENAME, listOf(item)) ?: return WriteOutcome(cancelled = true)
        val (outcome, _) = relocate(listOf(Relocation(item, item.albumPath, newName)), route, WriteControl())
        return if (outcome.succeeded == 1) {
            outcome.copy(undo = { rename(item.copy(displayName = newName), item.displayName) })
        } else outcome
    }

    /**
     * Whether an album folder can be renamed as a whole (ROADMAP 7.4): Android 11+ with all files access, Android 7-10
     * on the internal storage. Otherwise a rename moves the album's images into a new folder.
     */
    fun renamesFolders(path: AlbumPath): Boolean = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> Environment.isExternalStorageManager()
        else -> path.isPrimary
    }

    /**
     * Renames the album folder at [path] to [name] (a sibling folder). With [renamesFolders] the folder itself is
     * renamed, everything in it included; otherwise its [items] move into the new folder (other files stay), which
     * Android 10+ only allows under DCIM/ and Pictures/. The undo renames back.
     */
    suspend fun renameAlbum(path: AlbumPath, items: List<MediaItem>, name: String): WriteOutcome {
        if (readOnly(path)) return WriteOutcome(readOnlyCard = items.size)
        val target = AlbumPath(path.volume, MediaFileNamePolicy.renamedFolder(path.relativePath, name))
        val directory = directoryOf(path)
        val targetDirectory = directoryOf(target)
        if (namesIn(target, excludeId = null).isNotEmpty() || targetDirectory?.exists() == true) return WriteOutcome(nameTaken = true)
        if (renamesFolders(path) && directory != null && targetDirectory != null) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R && routeFor(MediaWriteOperation.MOVE, items) == null) return WriteOutcome(cancelled = true)
            val renamed = withContext(Dispatchers.IO) { directory.isDirectory && directory.renameTo(targetDirectory) }
            if (!renamed) return WriteOutcome(failed = items.size)
            rescanRenamed(directory, targetDirectory)
            return WriteOutcome(succeeded = items.size, undo = {
                val back = withContext(Dispatchers.IO) { targetDirectory.renameTo(directory) }
                if (back) rescanRenamed(targetDirectory, directory)
                if (back) WriteOutcome(succeeded = items.size) else WriteOutcome(failed = items.size)
            })
        }
        if (!isWritableAlbumPath(target.relativePath)) return WriteOutcome(failed = items.size)
        return move(items, target, NameConflictChoice.SKIP)
    }

    /**
     * Hides album folders of the internal storage with a `.nomedia` file (ROADMAP 7.4): the media library then
     * stops listing their images, here and in other galleries; the files stay. The undo removes the file again.
     */
    suspend fun hideFolders(paths: List<AlbumPath>): WriteOutcome {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R && routeForIds(MediaWriteOperation.MOVE, emptyList()) == null) return WriteOutcome(cancelled = true)
        val writable = paths.filter { it.isPrimary }
        val changed = ArrayList<AlbumPath>()
        val files = ArrayList<String>()
        withContext(Dispatchers.IO) {
            for (path in writable) {
                val directory = HiddenFolders.directory(path)
                val marker = File(directory, HiddenFolders.NOMEDIA)
                if (marker.exists() || runCatching { marker.createNewFile() }.getOrDefault(false)) {
                    changed += path
                    // Android 9 and older re-classify the folder when the marker itself is scanned.
                    files += marker.absolutePath
                    files += folderFiles(directory)
                }
            }
        }
        scan(files)
        return WriteOutcome(
            succeeded = changed.size,
            failed = paths.size - changed.size,
            undo = if (changed.isEmpty()) null else ({ showFolders(changed) }),
        )
    }

    /** Removes the `.nomedia` file of hidden folders, so the media library lists their images again. */
    suspend fun showFolders(paths: List<AlbumPath>): WriteOutcome {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R && routeForIds(MediaWriteOperation.MOVE, emptyList()) == null) return WriteOutcome(cancelled = true)
        val changed = ArrayList<AlbumPath>()
        val files = ArrayList<String>()
        withContext(Dispatchers.IO) {
            for (path in paths.filter { it.isPrimary }) {
                val directory = HiddenFolders.directory(path)
                val marker = File(directory, HiddenFolders.NOMEDIA)
                if (!marker.exists() || marker.delete()) {
                    changed += path
                    files += marker.absolutePath
                    files += folderFiles(directory)
                }
            }
        }
        scan(files)
        return WriteOutcome(
            succeeded = changed.size,
            failed = paths.size - changed.size,
            undo = if (changed.isEmpty()) null else ({ hideFolders(changed) }),
        )
    }

    /** The files of one folder (not its subfolders), which a scan re-classifies after a `.nomedia` change. */
    private fun folderFiles(directory: File): List<String> =
        directory.listFiles().orEmpty().filter { it.isFile && it.name != HiddenFolders.NOMEDIA }.map { it.absolutePath }

    /** After a folder rename: the media library forgets the old paths and lists the files under the new ones. */
    private suspend fun rescanRenamed(from: File, to: File) {
        val moved = withContext(Dispatchers.IO) { to.walkTopDown().filter { it.isFile }.toList() }
        val old = moved.map { File(from, it.relativeTo(to).path).absolutePath }
        scan(old + moved.map { it.absolutePath })
    }

    /** The directory of an album folder, when the app can name it (internal storage, or a mounted card on Android 11+). */
    private fun directoryOf(path: AlbumPath): File? {
        val root = if (path.isPrimary) {
            externalRoot()
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val storage = context.getSystemService(android.os.storage.StorageManager::class.java)
            storage?.storageVolumes?.firstOrNull { it.mediaStoreVolumeName.equals(path.volume, ignoreCase = true) }?.directory
        } else {
            null
        } ?: return null
        return File(root, path.relativePath.trimEnd('/'))
    }

    /** How many of [items] would meet an existing name in [target] (ignoring case). */
    suspend fun conflicts(items: List<MediaItem>, target: AlbumPath): Int {
        val taken = namesIn(target, excludeId = null).mapTo(HashSet()) { it.lowercase() }
        return items.count { !it.albumPath.sameAs(target) && it.displayName.lowercase() in taken }
    }

    /**
     * Moves [items] into [target] (`Pictures/Trips/` on one volume); names already there follow [conflict] (replaced
     * images go to the recycle bin first). Within a volume the media library moves the file; across volumes
     * (Android 11+) the image is copied to the target volume and the original removed. [control] reports progress
     * and stops between two images when cancelled; the undo only reverses the images that were moved.
     */
    suspend fun move(items: List<MediaItem>, target: AlbumPath, conflict: NameConflictChoice, control: WriteControl = WriteControl()): WriteOutcome {
        if (readOnly(target)) return WriteOutcome(readOnlyCard = items.size)
        val (writable, onCard) = splitReadOnly(items)
        val candidates = writable.filter { !it.albumPath.sameAs(target) }
        if (candidates.isEmpty()) return WriteOutcome(readOnlyCard = onCard)
        if (conflict == NameConflictChoice.REPLACE && !replaceTargets(candidates, target)) return WriteOutcome(cancelled = true)
        val plan = planTargets(candidates, target, conflict)
        if (plan.isEmpty()) return WriteOutcome(readOnlyCard = onCard)
        val route = routeFor(MediaWriteOperation.MOVE, plan.map { it.item }) ?: return WriteOutcome(cancelled = true)
        val (outcome, moved) = relocate(plan, route, control)
        if (moved.isEmpty()) return outcome.copy(readOnlyCard = onCard)
        return outcome.copy(readOnlyCard = onCard, undo = {
            val back = moved.map { (entry, now) -> Relocation(now, entry.item.albumPath, entry.item.displayName) }
            relocate(back, routeFor(MediaWriteOperation.MOVE, back.map { it.item }) ?: MediaWriteRoute.SYSTEM_REQUEST, WriteControl()).first
        })
    }

    /** Copies [items] into [target]; never asks for confirmation (new files belong to the app). */
    suspend fun copy(items: List<MediaItem>, target: AlbumPath, conflict: NameConflictChoice, control: WriteControl = WriteControl()): WriteOutcome {
        if (items.isEmpty()) return WriteOutcome()
        if (readOnly(target)) return WriteOutcome(readOnlyCard = items.size)
        if (conflict == NameConflictChoice.REPLACE && !replaceTargets(items.filter { !it.albumPath.sameAs(target) }, target)) {
            return WriteOutcome(cancelled = true)
        }
        val plan = planTargets(items, target, conflict)
        if (plan.isEmpty()) return WriteOutcome()
        if (routeFor(MediaWriteOperation.COPY, items) == null) return WriteOutcome(cancelled = true)
        var done = 0
        var failed = 0
        val scanned = ArrayList<String>()
        withContext(Dispatchers.IO) {
            for ((index, entry) in plan.withIndex()) {
                if (control.cancelRequested) break
                if (copyOne(entry, scanned)) done++ else failed++
                control.report(index + 1, plan.size)
            }
        }
        scan(scanned)
        return WriteOutcome(succeeded = done, failed = failed)
    }

    /**
     * "Replace": the images in [targetRelativePath] whose names the batch needs go to the recycle bin first (an
     * item of the batch itself is never replaced). False when the user refused that step.
     */
    private suspend fun replaceTargets(items: List<MediaItem>, target: AlbumPath): Boolean {
        val names = items.mapTo(HashSet()) { it.displayName.lowercase() }
        val ids = items.mapTo(HashSet()) { it.id }
        val existing = media.loadFolder(target).filter { it.displayName.lowercase() in names && it.id !in ids }
        if (existing.isEmpty()) return true
        val outcome = trash(existing)
        return !outcome.cancelled && !outcome.tooLargeForRecycleBin
    }

    /**
     * "Sync system favorites" (Android 11+, ROADMAP 7.4): marks [items] in the media library as well, with one
     * system confirmation unless the images are the app's own or all files access writes directly.
     */
    @RequiresApi(Build.VERSION_CODES.R)
    suspend fun setSystemFavorite(items: List<MediaItem>, favorite: Boolean): WriteOutcome {
        if (items.isEmpty()) return WriteOutcome()
        val uris = items.map(::uriOf)
        val route = routeFor(MediaWriteOperation.FAVORITE, items) ?: return WriteOutcome(cancelled = true)
        if (route == MediaWriteRoute.DIRECT) {
            val refused = ArrayList<Uri>()
            withContext(Dispatchers.IO) {
                for (uri in uris) {
                    val values = ContentValues().apply { put(MediaStore.MediaColumns.IS_FAVORITE, if (favorite) 1 else 0) }
                    if (runCatching { resolver.update(uri, values, null, null) }.getOrDefault(0) == 0) refused += uri
                }
            }
            if (refused.isEmpty()) return WriteOutcome(succeeded = uris.size)
            val granted = systemRequest(MediaStore.createFavoriteRequest(resolver, refused, favorite))
            return if (granted) WriteOutcome(succeeded = uris.size) else WriteOutcome(succeeded = uris.size - refused.size, failed = refused.size)
        }
        val granted = systemRequest(MediaStore.createFavoriteRequest(resolver, uris, favorite))
        return if (granted) WriteOutcome(succeeded = uris.size) else WriteOutcome(cancelled = true)
    }

    // ---- routes ----

    /** The route for a batch, asking for the legacy write permission first when the policy needs it; null = refused. */
    private suspend fun routeFor(operation: MediaWriteOperation, items: List<MediaItem>): MediaWriteRoute? =
        routeForIds(operation, items.map { it.id })

    private suspend fun routeForIds(operation: MediaWriteOperation, ids: List<Long>): MediaWriteRoute? {
        val owned = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && withContext(Dispatchers.IO) { ownedByApp(ids) }
        var route = MediaWritePolicy.route(operation, facts(owned))
        if (route == MediaWriteRoute.NEEDS_LEGACY_PERMISSION) {
            if (!requestLegacyWrite()) {
                writePermissionRefused = true
                return null
            }
            route = MediaWritePolicy.route(operation, facts(owned))
            if (route == MediaWriteRoute.NEEDS_LEGACY_PERMISSION) {
                writePermissionRefused = true
                return null
            }
        }
        return route
    }

    private suspend fun routeForUris(operation: MediaWriteOperation, uris: List<Uri>): MediaWriteRoute? =
        routeForIds(operation, uris.mapNotNull { runCatching { ContentUris.parseId(it) }.getOrNull() })

    private suspend fun requestLegacyWrite(): Boolean =
        launchForResult(ActivityResultContracts.RequestPermission(), Manifest.permission.WRITE_EXTERNAL_STORAGE)

    @RequiresApi(Build.VERSION_CODES.R)
    private suspend fun systemRequest(pendingIntent: PendingIntent): Boolean =
        launchForResult(ActivityResultContracts.StartIntentSenderForResult(), IntentSenderRequest.Builder(pendingIntent.intentSender).build())
            .resultCode == Activity.RESULT_OK

    private suspend fun <I, O> launchForResult(contract: ActivityResultContract<I, O>, input: I): O {
        val result = CompletableDeferred<O>()
        val launcher = activity.activityResultRegistry.register("maple.media-write#${requestCount++}", contract) { result.complete(it) }
        try {
            launcher.launch(input)
            return result.await()
        } finally {
            launcher.unregister()
        }
    }

    // ---- trash / delete ----

    private suspend fun setTrashed(uris: List<Uri>, trashed: Boolean, route: MediaWriteRoute): WriteOutcome {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return WriteOutcome(failed = uris.size)
        if (route == MediaWriteRoute.DIRECT) {
            val refused = ArrayList<Uri>()
            var done = 0
            withContext(Dispatchers.IO) {
                for (uri in uris) {
                    val values = ContentValues().apply { put(MediaStore.MediaColumns.IS_TRASHED, if (trashed) 1 else 0) }
                    if (runCatching { resolver.update(uri, values, null, null) }.getOrDefault(0) > 0) done++ else refused += uri
                }
            }
            if (refused.isEmpty()) return WriteOutcome(succeeded = done)
            val rest = setTrashed(refused, trashed, MediaWriteRoute.SYSTEM_REQUEST)
            return rest.copy(succeeded = rest.succeeded + done)
        }
        val granted = systemRequest(MediaStore.createTrashRequest(resolver, uris, trashed))
        return if (granted) WriteOutcome(succeeded = uris.size) else WriteOutcome(cancelled = true)
    }

    private suspend fun deleteUris(uris: List<Uri>, route: MediaWriteRoute): WriteOutcome {
        if (route == MediaWriteRoute.SYSTEM_REQUEST) {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && systemRequest(MediaStore.createDeleteRequest(resolver, uris))) {
                WriteOutcome(succeeded = uris.size)
            } else WriteOutcome(cancelled = true)
        }
        val refused = ArrayList<Uri>()
        val removedPaths = ArrayList<String>()
        var done = 0
        withContext(Dispatchers.IO) {
            for (uri in uris) {
                val path = if (route == MediaWriteRoute.LEGACY_FILE) filePathOf(uri) else null
                path?.let { runCatching { File(it).delete() } }
                val rowDeleted = runCatching { resolver.delete(uri, null, null) }.getOrDefault(0) > 0
                when {
                    rowDeleted -> done++
                    path != null && !File(path).exists() -> {
                        // The file is gone but the row stayed: a scan of the missing path removes it.
                        removedPaths += path
                        done++
                    }
                    else -> refused += uri
                }
            }
        }
        scan(removedPaths)
        if (refused.isEmpty() || Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return WriteOutcome(succeeded = done, failed = refused.size)
        val rest = deleteUris(refused, MediaWriteRoute.SYSTEM_REQUEST)
        return rest.copy(succeeded = rest.succeeded + done, failed = rest.failed)
    }

    private suspend fun trashToPrivateBin(items: List<MediaItem>): WriteOutcome {
        val withPaths = withContext(Dispatchers.IO) { items.mapNotNull { item -> filePathOf(uriOf(item))?.let { item to it } } }
        val result = recycleBin.trash(withPaths) { item, _ -> runCatching { resolver.delete(uriOf(item), null, null) }.isSuccess }
        if (result.tooLarge) return WriteOutcome(tooLargeForRecycleBin = true)
        // Rows the provider kept although their files moved away disappear with a scan of the old paths.
        scan(withPaths.map { it.second }.filter { !File(it).exists() })
        val ids = result.entryIds
        return WriteOutcome(
            succeeded = ids.size,
            failed = items.size - ids.size,
            undo = if (ids.isEmpty()) null else ({ WriteOutcome(succeeded = recycleBin.restore(ids).also { scan(it.second) }.first) }),
        )
    }

    // ---- rename / move / copy ----

    private data class Relocation(val item: MediaItem, val target: AlbumPath, val targetName: String)

    private suspend fun planTargets(items: List<MediaItem>, target: AlbumPath, conflict: NameConflictChoice): List<Relocation> {
        val taken = namesIn(target, excludeId = null).toMutableList()
        val plan = ArrayList<Relocation>()
        for (item in items) {
            val clash = taken.any { it.equals(item.displayName, ignoreCase = true) }
            val name = when {
                !clash -> item.displayName
                conflict == NameConflictChoice.SKIP -> continue
                else -> NameConflictPolicy.uniqueName(item.displayName, taken)
            }
            taken += name
            plan += Relocation(item, target, name)
        }
        return plan
    }

    /** A cross-volume move whose copy exists while the original still waits for a delete confirmation. */
    private class PendingDelete(val entry: Relocation, val copy: Uri)

    /** Runs [plan] on [route]; returns the outcome and, for each image actually relocated, the image as it is now. */
    private suspend fun relocate(plan: List<Relocation>, route: MediaWriteRoute, control: WriteControl): Pair<WriteOutcome, List<Pair<Relocation, MediaItem>>> {
        if (route == MediaWriteRoute.SYSTEM_REQUEST) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R || !systemRequest(MediaStore.createWriteRequest(resolver, plan.map { uriOf(it.item) }))) {
                return WriteOutcome(cancelled = true) to emptyList()
            }
        }
        val done = ArrayList<Pair<Relocation, MediaItem>>()
        val refused = ArrayList<Relocation>()
        val pending = ArrayList<PendingDelete>()
        val scanned = ArrayList<String>()
        withContext(Dispatchers.IO) {
            for ((index, entry) in plan.withIndex()) {
                if (control.cancelRequested) break
                fun record(now: MediaItem?) {
                    if (now != null) done += entry to now else refused += entry
                }
                when {
                    route == MediaWriteRoute.LEGACY_FILE -> record(relocateFile(entry, scanned))
                    MediaCatalog.sameVolume(entry.item.volume, entry.target.volume) -> record(relocateRow(entry))
                    else -> {
                        val copy = insertCopy(entry)
                        when {
                            copy == null -> refused += entry
                            deleteRow(uriOf(entry.item)) -> done += entry to movedCopy(entry, copy)
                            else -> pending += PendingDelete(entry, copy)
                        }
                    }
                }
                control.report(index + 1, plan.size)
            }
        }
        scan(scanned)
        if (pending.isNotEmpty()) {
            // A write grant does not always cover deleting the original: one delete confirmation finishes the
            // cross-volume moves, and refusing it takes their copies back.
            val granted = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
                systemRequest(MediaStore.createDeleteRequest(resolver, pending.map { uriOf(it.entry.item) }))
            withContext(Dispatchers.IO) {
                for (move in pending) {
                    if (granted) {
                        done += move.entry to movedCopy(move.entry, move.copy)
                    } else {
                        deleteRow(move.copy)
                        refused += move.entry
                    }
                }
            }
        }
        // All files access may still be refused for some rows: one system request covers the rest.
        if (route != MediaWriteRoute.DIRECT || refused.isEmpty() || Build.VERSION.SDK_INT < Build.VERSION_CODES.R || control.cancelRequested) {
            return WriteOutcome(succeeded = done.size, failed = refused.size) to done
        }
        val (rest, restDone) = relocate(refused, MediaWriteRoute.SYSTEM_REQUEST, control)
        return rest.copy(succeeded = rest.succeeded + done.size) to (done + restDone)
    }

    /** Android 10+, within a volume: MediaStore moves / renames the file itself when `RELATIVE_PATH` / `DISPLAY_NAME` change. */
    private fun relocateRow(entry: Relocation): MediaItem? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return null
        val values = ContentValues().apply {
            if (entry.target.relativePath != entry.item.relativePath) put(MediaStore.MediaColumns.RELATIVE_PATH, entry.target.relativePath)
            if (entry.targetName != entry.item.displayName) put(MediaStore.MediaColumns.DISPLAY_NAME, entry.targetName)
        }
        val updated = runCatching { resolver.update(uriOf(entry.item), values, null, null) > 0 }.getOrDefault(false)
        return if (updated) entry.item.copy(relativePath = entry.target.relativePath, displayName = entry.targetName) else null
    }

    /** The moved image of a cross-volume move: the copy's row on the target volume. */
    private fun movedCopy(entry: Relocation, copy: Uri): MediaItem {
        val id = ContentUris.parseId(copy)
        return entry.item.copy(
            id = id,
            relativePath = entry.target.relativePath,
            displayName = entry.targetName,
            volume = entry.target.volume,
            contentUri = ContentUris.withAppendedId(media.collection, id).toString(),
        )
    }

    private fun deleteRow(uri: Uri): Boolean = runCatching { resolver.delete(uri, null, null) > 0 }.getOrDefault(false)

    /** Android 7-10: rename / move the file, then point the row at it (or let a scan re-create it). */
    private fun relocateFile(entry: Relocation, scanned: MutableList<String>): MediaItem? {
        val uri = uriOf(entry.item)
        val source = filePathOf(uri)?.let(::File) ?: return null
        val targetDir = File(externalRoot(), entry.target.relativePath)
        if (!targetDir.isDirectory && !targetDir.mkdirs()) return null
        val target = File(targetDir, entry.targetName)
        if (target.exists()) return null
        if (!source.renameTo(target) && !copyThenRemove(source, target)) return null
        val values = ContentValues().apply {
            @Suppress("DEPRECATION") put(MediaStore.MediaColumns.DATA, target.absolutePath)
            put(MediaStore.MediaColumns.DISPLAY_NAME, target.name)
        }
        val updated = runCatching { resolver.update(uri, values, null, null) > 0 }.getOrDefault(false)
        if (!updated) {
            runCatching { resolver.delete(uri, null, null) }
        }
        scanned += target.absolutePath
        return entry.item.copy(relativePath = entry.target.relativePath, displayName = entry.targetName)
    }

    /** A move across partitions: copy, then remove the original; if that is refused the copy goes again. */
    private fun copyThenRemove(source: File, target: File): Boolean {
        if (!copyFile(source, target)) return false
        if (source.delete()) return true
        target.delete()
        return false
    }

    /**
     * Android 10+: a new pending row on the target volume, filled from the source and then published; null when
     * any step failed (nothing is left behind).
     */
    private fun insertCopy(entry: Relocation): Uri? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return null
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, entry.targetName)
            put(MediaStore.MediaColumns.MIME_TYPE, entry.item.mimeType)
            put(MediaStore.MediaColumns.RELATIVE_PATH, entry.target.relativePath)
            entry.item.dateTakenMillis?.let { put(MediaStore.MediaColumns.DATE_TAKEN, it) }
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        }
        val volume = if (entry.target.isPrimary) MediaStore.VOLUME_EXTERNAL_PRIMARY else entry.target.volume
        val target = runCatching { resolver.insert(MediaStore.Images.Media.getContentUri(volume), values) }.getOrNull() ?: return null
        val copied = runCatching {
            resolver.openInputStream(uriOf(entry.item)).use { input ->
                resolver.openOutputStream(target, "w").use { output -> requireNotNull(input).copyTo(requireNotNull(output)) }
            }
        }.isSuccess
        val published = copied && runCatching {
            resolver.update(target, ContentValues().apply { put(MediaStore.MediaColumns.IS_PENDING, 0) }, null, null) > 0
        }.getOrDefault(false)
        if (!published) {
            deleteRow(target)
            return null
        }
        return target
    }

    private fun copyOne(entry: Relocation, scanned: MutableList<String>): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) return insertCopy(entry) != null
        val sourceFile = filePathOf(uriOf(entry.item))?.let(::File) ?: return false
        val targetDir = File(externalRoot(), entry.target.relativePath)
        if (!targetDir.isDirectory && !targetDir.mkdirs()) return false
        val target = File(targetDir, entry.targetName)
        if (!copyFile(sourceFile, target)) return false
        target.setLastModified(sourceFile.lastModified())
        scanned += target.absolutePath
        return true
    }

    // ---- queries and helpers ----

    private fun uriOf(item: MediaItem): Uri = Uri.parse(item.contentUri)

    /** Whether every row was created by this app; trashed rows count too (restoring them from the recycle bin). */
    private fun ownedByApp(ids: List<Long>): Boolean {
        if (ids.isEmpty() || Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return false
        val args = Bundle().apply {
            putString(ContentResolver.QUERY_ARG_SQL_SELECTION, "${MediaStore.MediaColumns._ID} IN (${ids.joinToString(",")})")
            putInt(MediaStore.QUERY_ARG_MATCH_TRASHED, MediaStore.MATCH_INCLUDE)
        }
        var rows = 0
        var owned = true
        runCatching {
            resolver.query(media.collection, arrayOf(MediaStore.MediaColumns.OWNER_PACKAGE_NAME), args, null)?.use { cursor ->
                while (cursor.moveToNext()) {
                    rows++
                    if (cursor.getString(0) != context.packageName) owned = false
                }
            }
        }.onFailure { return false }
        return owned && rows == ids.toSet().size
    }

    /** Display names already in [path] (case-insensitive comparisons are done by the callers). */
    private suspend fun namesIn(path: AlbumPath, excludeId: Long?): List<String> {
        val names = media.loadFolder(path).filter { it.id != excludeId }.mapTo(ArrayList()) { it.displayName }
        // Files the media library does not list still block a name (the internal storage is the one the app can list).
        if (path.isPrimary) withContext(Dispatchers.IO) { File(externalRoot(), path.relativePath).list() }?.let { names += it }
        return names
    }

    @Suppress("DEPRECATION")
    private fun filePathOf(uri: Uri): String? = runCatching {
        resolver.query(uri, arrayOf(MediaStore.MediaColumns.DATA), null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) cursor.getString(0) else null
        }
    }.getOrNull()

    @Suppress("DEPRECATION")
    private fun externalRoot(): File = Environment.getExternalStorageDirectory()

    private suspend fun scan(paths: List<String>) {
        if (paths.isEmpty()) return
        suspendCancellableCoroutine { continuation ->
            val remaining = AtomicInteger(paths.size)
            MediaScannerConnection.scanFile(context, paths.toTypedArray(), null) { _, _ ->
                if (remaining.decrementAndGet() == 0 && continuation.isActive) continuation.resume(Unit)
            }
        }
    }

    companion object {
        fun canManageMedia(context: Context): Boolean =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && MediaStore.canManageMedia(context)

        internal fun copyFile(source: File, target: File): Boolean = runCatching {
            source.inputStream().use { input -> target.outputStream().use { output -> input.copyTo(output) } }
            true
        }.getOrElse {
            target.delete()
            false
        }

        /** Album targets for move / copy: Android 10+ only accepts images under DCIM/ and Pictures/. */
        fun isWritableAlbumPath(relativePath: String, sdk: Int = Build.VERSION.SDK_INT): Boolean =
            sdk < Build.VERSION_CODES.Q || relativePath.startsWith("${Environment.DIRECTORY_DCIM}/") || relativePath.startsWith("${Environment.DIRECTORY_PICTURES}/")

        /** [isWritableAlbumPath], and on Android 7-10 only on the internal storage (a storage card is read-only there). */
        fun isWritableAlbum(path: AlbumPath, sdk: Int = Build.VERSION.SDK_INT): Boolean =
            isWritableAlbumPath(path.relativePath, sdk) && (sdk >= Build.VERSION_CODES.R || path.isPrimary)
    }
}

/**
 * Progress and cancellation of a move or copy batch (ROADMAP 7.3): [report] is called from a background thread
 * after each image, and [cancel] stops the batch before the next one (finished images stay where they are).
 */
internal class WriteControl(private val onProgress: (done: Int, total: Int) -> Unit = { _, _ -> }) {
    @Volatile
    var cancelRequested: Boolean = false
        private set

    fun cancel() {
        cancelRequested = true
    }

    fun report(done: Int, total: Int) = onProgress(done, total)
}

/**
 * The private recycle bin of Android 7-10 (ROADMAP T10, Q8): files under `files/recycle-bin/`, indexed in Room,
 * expired after 30 days, capped at 10% of the free space (at most 2 GiB) with the oldest purged first.
 */
internal class PrivateRecycleBin(context: Context, private val now: () -> Long = System::currentTimeMillis) {
    private val appContext = context.applicationContext
    private val dao by lazy { MapleDatabase.get(appContext).recycleBin() }
    val directory: File get() = File(appContext.filesDir, "recycle-bin").apply { mkdirs() }

    data class TrashResult(val entryIds: List<Long>, val tooLarge: Boolean)

    private fun freeBytes(): Long = runCatching { StatFs(appContext.filesDir.absolutePath).availableBytes }.getOrDefault(0L)

    /** Cleanup at start-up and before every delete: expired entries, then the oldest until [incomingBytes] fit. */
    suspend fun purge(incomingBytes: Long = 0): Boolean = withContext(Dispatchers.IO) {
        val entries = dao.all()
        val plan = RecycleBinPolicy.plan(entries.map { RecycleBinItem(it.id, it.sizeBytes, it.deletedAtMillis) }, freeBytes(), incomingBytes, now())
        removeEntries(entries.filter { it.id in plan.purgeIds })
        plan.fits
    }

    suspend fun trash(items: List<Pair<MediaItem, String>>, removeFromLibrary: (MediaItem, String) -> Boolean): TrashResult = withContext(Dispatchers.IO) {
        val incoming = items.sumOf { File(it.second).length() }
        if (!purge(incoming)) return@withContext TrashResult(emptyList(), tooLarge = true)
        val ids = ArrayList<Long>()
        val stored = directory.list().orEmpty().toMutableList()
        for ((item, path) in items) {
            val source = File(path)
            val deletedAt = now()
            val name = RecycleBinPolicy.storedName(item.displayName, deletedAt, stored)
            val target = File(directory, name)
            if (!MediaWriteCoordinator.copyFile(source, target)) continue
            if (!source.delete()) {
                target.delete()
                continue
            }
            removeFromLibrary(item, path)
            stored += name
            ids += dao.insert(
                RecycleBinEntity(
                    storedName = name,
                    originalPath = path,
                    displayName = item.displayName,
                    mimeType = item.mimeType,
                    sizeBytes = target.length(),
                    dateModifiedSeconds = item.dateModifiedSeconds,
                    dateTakenMillis = item.dateTakenMillis,
                    deletedAtMillis = deletedAt,
                ),
            )
        }
        TrashResult(ids, tooLarge = false)
    }

    /** Puts entries back where they were (a taken name gets a suffix); returns the count and the paths to scan. */
    suspend fun restore(ids: List<Long>): Pair<Int, List<String>> = withContext(Dispatchers.IO) {
        val entries = dao.all().filter { it.id in ids }
        val restoredPaths = ArrayList<String>()
        val done = ArrayList<Long>()
        for (entry in entries) {
            val original = File(entry.originalPath)
            val parent = original.parentFile ?: continue
            if (!parent.isDirectory && !parent.mkdirs()) continue
            val target = File(parent, NameConflictPolicy.uniqueName(original.name, parent.list().orEmpty().toList()))
            val stored = File(directory, entry.storedName)
            if (!MediaWriteCoordinator.copyFile(stored, target)) continue
            target.setLastModified(entry.dateModifiedSeconds * 1000)
            stored.delete()
            restoredPaths += target.absolutePath
            done += entry.id
        }
        dao.delete(done)
        done.size to restoredPaths
    }

    suspend fun deletePermanently(ids: List<Long>): Int = withContext(Dispatchers.IO) {
        val entries = dao.all().filter { it.id in ids }
        removeEntries(entries)
        entries.size
    }

    suspend fun entries(): List<RecycleBinEntity> = withContext(Dispatchers.IO) { dao.all() }

    fun storedFile(entry: RecycleBinEntity): File = File(directory, entry.storedName)

    private suspend fun removeEntries(entries: List<RecycleBinEntity>) {
        if (entries.isEmpty()) return
        entries.forEach { File(directory, it.storedName).delete() }
        dao.delete(entries.map { it.id })
    }
}
