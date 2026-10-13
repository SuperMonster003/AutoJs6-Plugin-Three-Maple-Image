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

    private val sdk: Int get() = Build.VERSION.SDK_INT

    private fun facts(owned: Boolean = false) = MediaWriteFacts(
        sdk = sdk,
        allFilesAccess = sdk >= Build.VERSION_CODES.R && Environment.isExternalStorageManager(),
        manageMedia = canManageMedia(context),
        legacyWriteGranted = sdk >= Build.VERSION_CODES.R ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED,
        ownedByApp = owned,
    )

    /** Moves [items] to the recycle bin (system trash on Android 11+, private bin on Android 7-10). */
    suspend fun trash(items: List<MediaItem>): WriteOutcome {
        if (items.isEmpty()) return WriteOutcome()
        return when (val route = routeFor(MediaWriteOperation.TRASH, items) ?: return WriteOutcome(cancelled = true)) {
            MediaWriteRoute.PRIVATE_BIN -> trashToPrivateBin(items)
            else -> {
                val uris = items.map(::uriOf)
                val outcome = setTrashed(uris, trashed = true, route)
                if (outcome.succeeded > 0) outcome.copy(undo = { setTrashed(uris, trashed = false, routeFor(MediaWriteOperation.RESTORE, items) ?: MediaWriteRoute.SYSTEM_REQUEST) }) else outcome
            }
        }
    }

    /** Takes system-trashed images (Android 11+) out of the trash again. */
    @RequiresApi(Build.VERSION_CODES.R)
    suspend fun restoreTrashed(uris: List<Uri>): WriteOutcome =
        if (uris.isEmpty()) WriteOutcome() else setTrashed(uris, trashed = false, routeForUris(MediaWriteOperation.RESTORE, uris) ?: return WriteOutcome(cancelled = true))

    /** Deletes [items] for good: the recycle bin is switched off, or the files do not fit into it. */
    suspend fun delete(items: List<MediaItem>): WriteOutcome {
        if (items.isEmpty()) return WriteOutcome()
        return deleteUris(items.map(::uriOf), routeFor(MediaWriteOperation.DELETE, items) ?: return WriteOutcome(cancelled = true))
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
        val siblings = withContext(Dispatchers.IO) { namesIn(item.relativePath, excludeId = item.id) }
        if (siblings.any { it.equals(newName, ignoreCase = true) }) return WriteOutcome(nameTaken = true)
        val route = routeFor(MediaWriteOperation.RENAME, listOf(item)) ?: return WriteOutcome(cancelled = true)
        val outcome = relocate(listOf(Relocation(item, item.relativePath, newName)), route)
        return if (outcome.succeeded == 1) {
            outcome.copy(undo = { rename(item.copy(displayName = newName), item.displayName) })
        } else outcome
    }

    /** Moves [items] into [targetRelativePath] (`Pictures/Trips/`); names already there follow [conflict]. */
    suspend fun move(items: List<MediaItem>, targetRelativePath: String, conflict: NameConflictChoice): WriteOutcome {
        val plan = withContext(Dispatchers.IO) { planTargets(items.filter { it.relativePath != targetRelativePath }, targetRelativePath, conflict) }
        if (plan.isEmpty()) return WriteOutcome()
        val route = routeFor(MediaWriteOperation.MOVE, plan.map { it.item }) ?: return WriteOutcome(cancelled = true)
        val outcome = relocate(plan, route)
        if (outcome.succeeded == 0) return outcome
        return outcome.copy(undo = {
            val back = plan.map { Relocation(it.item.copy(relativePath = it.targetRelativePath, displayName = it.targetName), it.item.relativePath, it.item.displayName) }
            relocate(back, routeFor(MediaWriteOperation.MOVE, back.map { it.item }) ?: MediaWriteRoute.SYSTEM_REQUEST)
        })
    }

    /** Copies [items] into [targetRelativePath]; never asks for confirmation (new files belong to the app). */
    suspend fun copy(items: List<MediaItem>, targetRelativePath: String, conflict: NameConflictChoice): WriteOutcome {
        val plan = withContext(Dispatchers.IO) { planTargets(items, targetRelativePath, conflict) }
        if (plan.isEmpty()) return WriteOutcome()
        if (routeFor(MediaWriteOperation.COPY, items) == null) return WriteOutcome(cancelled = true)
        var done = 0
        var failed = 0
        val scanned = ArrayList<String>()
        withContext(Dispatchers.IO) {
            for (entry in plan) if (copyOne(entry, scanned)) done++ else failed++
        }
        scan(scanned)
        return WriteOutcome(succeeded = done, failed = failed)
    }

    // ---- routes ----

    /** The route for a batch, asking for the legacy write permission first when the policy needs it; null = refused. */
    private suspend fun routeFor(operation: MediaWriteOperation, items: List<MediaItem>): MediaWriteRoute? =
        routeForIds(operation, items.map { it.id })

    private suspend fun routeForIds(operation: MediaWriteOperation, ids: List<Long>): MediaWriteRoute? {
        val owned = sdk >= Build.VERSION_CODES.R && withContext(Dispatchers.IO) { ownedByApp(ids) }
        var route = MediaWritePolicy.route(operation, facts(owned))
        if (route == MediaWriteRoute.NEEDS_LEGACY_PERMISSION) {
            if (!requestLegacyWrite()) return null
            route = MediaWritePolicy.route(operation, facts(owned))
            if (route == MediaWriteRoute.NEEDS_LEGACY_PERMISSION) return null
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
        if (sdk < Build.VERSION_CODES.R) return WriteOutcome(failed = uris.size)
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
            return if (sdk >= Build.VERSION_CODES.R && systemRequest(MediaStore.createDeleteRequest(resolver, uris))) {
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
        if (refused.isEmpty() || sdk < Build.VERSION_CODES.R) return WriteOutcome(succeeded = done, failed = refused.size)
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

    private data class Relocation(val item: MediaItem, val targetRelativePath: String, val targetName: String)

    private fun planTargets(items: List<MediaItem>, targetRelativePath: String, conflict: NameConflictChoice): List<Relocation> {
        val taken = namesIn(targetRelativePath, excludeId = null).toMutableList()
        val plan = ArrayList<Relocation>()
        for (item in items) {
            val clash = taken.any { it.equals(item.displayName, ignoreCase = true) }
            val name = when {
                !clash -> item.displayName
                conflict == NameConflictChoice.SKIP -> continue
                else -> NameConflictPolicy.uniqueName(item.displayName, taken)
            }
            taken += name
            plan += Relocation(item, targetRelativePath, name)
        }
        return plan
    }

    private suspend fun relocate(plan: List<Relocation>, route: MediaWriteRoute): WriteOutcome {
        if (route == MediaWriteRoute.SYSTEM_REQUEST) {
            if (sdk < Build.VERSION_CODES.R || !systemRequest(MediaStore.createWriteRequest(resolver, plan.map { uriOf(it.item) }))) {
                return WriteOutcome(cancelled = true)
            }
        }
        var done = 0
        val refused = ArrayList<Relocation>()
        val scanned = ArrayList<String>()
        withContext(Dispatchers.IO) {
            for (entry in plan) {
                val ok = if (route == MediaWriteRoute.LEGACY_FILE) relocateFile(entry, scanned) else relocateRow(entry)
                if (ok) done++ else refused += entry
            }
        }
        scan(scanned)
        // All files access may still be refused for some rows: one system request covers the rest.
        if (route != MediaWriteRoute.DIRECT || refused.isEmpty() || sdk < Build.VERSION_CODES.R) return WriteOutcome(succeeded = done, failed = refused.size)
        val rest = relocate(refused, MediaWriteRoute.SYSTEM_REQUEST)
        return rest.copy(succeeded = rest.succeeded + done)
    }

    /** Android 10+: MediaStore moves / renames the file itself when `RELATIVE_PATH` / `DISPLAY_NAME` change. */
    private fun relocateRow(entry: Relocation): Boolean {
        if (sdk < Build.VERSION_CODES.Q) return false
        val values = ContentValues().apply {
            if (entry.targetRelativePath != entry.item.relativePath) put(MediaStore.MediaColumns.RELATIVE_PATH, entry.targetRelativePath)
            if (entry.targetName != entry.item.displayName) put(MediaStore.MediaColumns.DISPLAY_NAME, entry.targetName)
        }
        return runCatching { resolver.update(uriOf(entry.item), values, null, null) > 0 }.getOrDefault(false)
    }

    /** Android 7-10: rename / move the file, then point the row at it (or let a scan re-create it). */
    private fun relocateFile(entry: Relocation, scanned: MutableList<String>): Boolean {
        val uri = uriOf(entry.item)
        val source = filePathOf(uri)?.let(::File) ?: return false
        val targetDir = File(externalRoot(), entry.targetRelativePath)
        if (!targetDir.isDirectory && !targetDir.mkdirs()) return false
        val target = File(targetDir, entry.targetName)
        if (target.exists()) return false
        val moved = source.renameTo(target) || (copyFile(source, target) && source.delete())
        if (!moved) return false
        val values = ContentValues().apply {
            @Suppress("DEPRECATION") put(MediaStore.MediaColumns.DATA, target.absolutePath)
            put(MediaStore.MediaColumns.DISPLAY_NAME, target.name)
        }
        val updated = runCatching { resolver.update(uri, values, null, null) > 0 }.getOrDefault(false)
        if (!updated) {
            runCatching { resolver.delete(uri, null, null) }
        }
        scanned += target.absolutePath
        return true
    }

    private fun copyOne(entry: Relocation, scanned: MutableList<String>): Boolean {
        val source = uriOf(entry.item)
        if (sdk >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, entry.targetName)
                put(MediaStore.MediaColumns.MIME_TYPE, entry.item.mimeType)
                put(MediaStore.MediaColumns.RELATIVE_PATH, entry.targetRelativePath)
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
            val target = runCatching { resolver.insert(media.collection, values) }.getOrNull() ?: return false
            val copied = runCatching {
                resolver.openInputStream(source).use { input ->
                    resolver.openOutputStream(target, "w").use { output -> requireNotNull(input).copyTo(requireNotNull(output)) }
                }
            }.isSuccess
            if (!copied) {
                runCatching { resolver.delete(target, null, null) }
                return false
            }
            val publish = ContentValues().apply { put(MediaStore.MediaColumns.IS_PENDING, 0) }
            return runCatching { resolver.update(target, publish, null, null) > 0 }.getOrDefault(false)
        }
        val sourceFile = filePathOf(source)?.let(::File) ?: return false
        val targetDir = File(externalRoot(), entry.targetRelativePath)
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
        if (ids.isEmpty() || sdk < Build.VERSION_CODES.R) return false
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

    /** Display names already in [relativePath] (case-insensitive comparisons are done by the callers). */
    private fun namesIn(relativePath: String, excludeId: Long?): List<String> {
        val names = ArrayList<String>()
        if (sdk >= Build.VERSION_CODES.Q) {
            runCatching {
                resolver.query(
                    media.collection,
                    arrayOf(MediaStore.MediaColumns._ID, MediaStore.MediaColumns.DISPLAY_NAME),
                    "${MediaStore.MediaColumns.RELATIVE_PATH} = ?",
                    arrayOf(relativePath),
                    null,
                )?.use { cursor -> while (cursor.moveToNext()) if (cursor.getLong(0) != excludeId) names += cursor.getString(1).orEmpty() }
            }
        }
        // Files the media library does not list (or Android 9 and older) still block a name.
        File(externalRoot(), relativePath).list()?.let { names += it }
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
    }
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
