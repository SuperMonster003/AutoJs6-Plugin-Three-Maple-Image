package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.database.ContentObserver
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicInteger

/**
 * Reads the device image library straight from MediaStore (ROADMAP T6): a lightweight full scan for the album list,
 * paged album queries for the grid and a throttled change stream backed by a [ContentObserver]. Pending rows
 * (Android 10+) and trashed rows (Android 11+) are excluded by the platform defaults of these queries, so the
 * gallery never shows half-written or recycled images; the recycle bin view of M3 will ask for trashed rows itself.
 * No persistent cache: the last full scan is kept in memory until the next change notification.
 */
internal class MediaStoreRepository(
    context: Context,
    private val supports: (String, String) -> Boolean = ImageFormatSupport::isSupportedExplorerTarget,
) {
    private val resolver: ContentResolver = context.applicationContext.contentResolver
    private val externalRoots: List<String> = listOf(Environment.getExternalStorageDirectory().absolutePath, "/sdcard")

    @Volatile
    private var cachedAll: List<MediaItem>? = null

    /** The last name-ordered album, so name pages are slices instead of a fresh sort of the album per page. */
    @Volatile
    private var cachedNameOrder: NameOrder? = null

    private class NameOrder(val source: List<MediaItem>, val bucketId: Long, val sort: MediaSort, val excluded: Set<String>, val items: List<MediaItem>)

    val collection: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
    } else {
        @Suppress("DEPRECATION")
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    }

    /**
     * Every cataloged image on the device, newest change first; cached until [invalidate] or a change event. While
     * a gallery change observer is registered the scan is also shared with other repositories of the process (the
     * viewer opened from the grid), so a large library is not queried twice.
     */
    suspend fun loadAll(): List<MediaItem> {
        cachedAll?.let { return it }
        sharedAll?.let { shared ->
            cachedAll = shared
            return shared
        }
        val cataloged = withContext(Dispatchers.IO) {
            MediaCatalog.filter(query(selection = null, selectionArgs = null, sort = MediaSort.DEFAULT, limit = null, offset = 0), supports)
        }
        cachedAll = cataloged
        if (observers.get() > 0) sharedAll = cataloged
        return cataloged
    }

    /** Aggregation runs off the main thread: a library of tens of thousands of images takes a noticeable while. */
    suspend fun loadBuckets(): List<MediaBucket> {
        val all = loadAll()
        return withContext(Dispatchers.Default) { MediaCatalog.buckets(all) }
    }

    /**
     * One page of an album in the requested order. Date and size pages come from the provider so large albums stay
     * cheap; name order is natural (numbers compare numerically, like the viewer's sibling order), which SQLite
     * cannot express, so name pages are sliced from the cached full scan instead.
     */
    suspend fun loadBucket(
        bucketId: Long,
        sort: MediaSort = MediaSort.DEFAULT,
        limit: Int,
        offset: Int = 0,
        marks: FolderMarkState = FolderMarkState.NONE,
    ): List<MediaItem> {
        // Excluded folders leave "all images" (ROADMAP 7.4); filtering provider pages would shift the offsets, so
        // that view pages through a filtered copy of the full scan instead.
        if (sort.field == MediaSortField.NAME || sort.field == MediaSortField.RANDOM || excludes(bucketId, marks)) {
            val album = ordered(bucketId, sort, marks)
            return album.subList(offset.coerceAtMost(album.size), (offset + limit).coerceAtMost(album.size))
        }
        return withContext(Dispatchers.IO) {
            val rows = query(
                selection = bucketSelection(bucketId),
                selectionArgs = bucketSelectionArgs(bucketId),
                sort = sort,
                limit = limit,
                offset = offset,
            )
            MediaCatalog.filter(rows, supports)
        }
    }

    private fun excludes(bucketId: Long, marks: FolderMarkState): Boolean = bucketId == MediaCatalog.ALL_BUCKETS && marks.excluded.isNotEmpty()

    /** One album (or every bucket without the excluded folders) sorted in memory; the last result is kept for paging. */
    private suspend fun ordered(bucketId: Long, sort: MediaSort, marks: FolderMarkState): List<MediaItem> {
        val all = loadAll()
        val excluded = if (bucketId == MediaCatalog.ALL_BUCKETS) marks.excluded else emptySet()
        cachedNameOrder?.let { cached ->
            if (cached.source === all && cached.bucketId == bucketId && cached.sort == sort && cached.excluded == excluded) return cached.items
        }
        val items = withContext(Dispatchers.Default) {
            val album = if (bucketId == MediaCatalog.ALL_BUCKETS) marks.visible(all) else all.filter { it.bucketId == bucketId }
            // The full scan is already in the default order.
            if (sort == MediaSort.DEFAULT && bucketId == MediaCatalog.ALL_BUCKETS) album else MediaCatalog.sort(album, sort)
        }
        cachedNameOrder = NameOrder(all, bucketId, sort, excluded, items)
        return items
    }

    private fun bucketSelection(bucketId: Long): String? =
        if (bucketId == MediaCatalog.ALL_BUCKETS) null else "${MediaStore.Images.Media.BUCKET_ID} = ?"

    private fun bucketSelectionArgs(bucketId: Long): Array<String>? =
        if (bucketId == MediaCatalog.ALL_BUCKETS) null else arrayOf(bucketId.toString())

    /** Every image of one album in the requested order; the viewer pages through this list (ROADMAP 6.3). */
    suspend fun loadBucketAll(bucketId: Long, sort: MediaSort = MediaSort.DEFAULT, marks: FolderMarkState = FolderMarkState.NONE): List<MediaItem> {
        if (sort.field == MediaSortField.NAME || sort.field == MediaSortField.RANDOM || excludes(bucketId, marks)) return ordered(bucketId, sort, marks)
        // The shared full scan is already in the default order (the provider query below would return the same rows).
        if (sort == MediaSort.DEFAULT) sharedAll?.let { all ->
            return if (bucketId == MediaCatalog.ALL_BUCKETS) all else withContext(Dispatchers.Default) { all.filter { it.bucketId == bucketId } }
        }
        return withContext(Dispatchers.IO) {
            val rows = query(
                selection = bucketSelection(bucketId),
                selectionArgs = bucketSelectionArgs(bucketId),
                sort = sort,
                limit = null,
                offset = 0,
            )
            MediaCatalog.filter(rows, supports)
        }
    }

    fun invalidate() {
        cachedAll = null
        cachedNameOrder = null
        sharedAll = null
    }

    /**
     * Emits after MediaStore reports changes, once they settle for [quietMillis] and at least every [maxWaitMillis]
     * while they keep coming (a camera burst, a bulk import or delete, a media rescan); see [settle]. The cache is
     * dropped before each emission.
     */
    fun changes(quietMillis: Long = DEFAULT_QUIET_MILLIS, maxWaitMillis: Long = DEFAULT_MAX_WAIT_MILLIS): Flow<Unit> =
        rawChanges().settle(quietMillis, maxWaitMillis).onEach { invalidate() }

    /** Every row of one folder (`Pictures/Trips/` on one volume), unfiltered: the name conflicts of a move or copy (ROADMAP 7.3). */
    suspend fun loadFolder(path: AlbumPath): List<MediaItem> = withContext(Dispatchers.IO) {
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                query("${MediaStore.Images.Media.RELATIVE_PATH} = ?", arrayOf(path.relativePath), MediaSort.DEFAULT, limit = null, offset = 0)
            } else {
                @Suppress("DEPRECATION")
                query("${MediaStore.Images.Media.DATA} LIKE ?", arrayOf("%/${path.relativePath}%"), MediaSort.DEFAULT, limit = null, offset = 0)
            }.filter { it.albumPath.sameAs(path) }
        }.getOrDefault(emptyList())
    }

    private fun rawChanges(): Flow<Unit> = callbackFlow {
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                trySend(Unit)
            }
        }
        resolver.registerContentObserver(collection, true, observer)
        observers.incrementAndGet()
        awaitClose {
            resolver.unregisterContentObserver(observer)
            // Without an observer nothing would drop a stale shared scan.
            if (observers.decrementAndGet() == 0) sharedAll = null
        }
    }.conflate()

    private fun query(selection: String?, selectionArgs: Array<String>?, sort: MediaSort, limit: Int?, offset: Int): List<MediaItem> {
        val cursor = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val args = Bundle().apply {
                if (selection != null) {
                    putString(ContentResolver.QUERY_ARG_SQL_SELECTION, selection)
                    putStringArray(ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS, selectionArgs)
                }
                putString(ContentResolver.QUERY_ARG_SQL_SORT_ORDER, sortOrder(sort))
                if (limit != null) {
                    putInt(ContentResolver.QUERY_ARG_LIMIT, limit)
                    putInt(ContentResolver.QUERY_ARG_OFFSET, offset)
                }
            }
            resolver.query(collection, projection(), args, null)
        } else {
            val order = buildString {
                append(sortOrder(sort))
                if (limit != null) append(" LIMIT ").append(offset).append(", ").append(limit)
            }
            resolver.query(collection, projection(), selection, selectionArgs, order)
        } ?: return emptyList()
        cursor.use { return readRows(it) }
    }

    private fun projection(): Array<String> = buildList {
        add(MediaStore.Images.Media._ID)
        add(MediaStore.Images.Media.DISPLAY_NAME)
        add(MediaStore.Images.Media.MIME_TYPE)
        add(MediaStore.Images.Media.SIZE)
        add(MediaStore.Images.Media.WIDTH)
        add(MediaStore.Images.Media.HEIGHT)
        add(MediaStore.Images.Media.DATE_MODIFIED)
        add(MediaStore.Images.Media.DATE_TAKEN)
        add(MediaStore.Images.Media.BUCKET_ID)
        add(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            add(MediaStore.Images.Media.RELATIVE_PATH)
            add(MediaStore.Images.Media.VOLUME_NAME)
        } else {
            @Suppress("DEPRECATION") add(MediaStore.Images.Media.DATA)
        }
    }.toTypedArray()

    private fun readRows(cursor: Cursor): List<MediaItem> {
        val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
        val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
        val mimeColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE)
        val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
        val widthColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH)
        val heightColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT)
        val modifiedColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED)
        val takenColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN)
        val bucketIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_ID)
        val bucketNameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
        val pathColumn = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            cursor.getColumnIndexOrThrow(MediaStore.Images.Media.RELATIVE_PATH)
        } else {
            @Suppress("DEPRECATION")
            cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        }
        val volumeColumn = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) cursor.getColumnIndexOrThrow(MediaStore.Images.Media.VOLUME_NAME) else -1
        val items = ArrayList<MediaItem>(cursor.count)
        while (cursor.moveToNext()) {
            val id = cursor.getLong(idColumn)
            val location = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                AlbumPath(cursor.getString(volumeColumn) ?: MediaCatalog.PRIMARY_VOLUME, cursor.getString(pathColumn).orEmpty())
            } else {
                MediaCatalog.albumPathFromFilePath(cursor.getString(pathColumn).orEmpty(), externalRoots)
            }
            items.add(
                MediaItem(
                    id = id,
                    displayName = cursor.getString(nameColumn).orEmpty(),
                    mimeType = cursor.getString(mimeColumn).orEmpty(),
                    sizeBytes = cursor.getLong(sizeColumn),
                    width = cursor.getInt(widthColumn),
                    height = cursor.getInt(heightColumn),
                    dateModifiedSeconds = cursor.getLong(modifiedColumn),
                    dateTakenMillis = if (cursor.isNull(takenColumn)) null else cursor.getLong(takenColumn).takeIf { it > 0L },
                    bucketId = cursor.getLong(bucketIdColumn),
                    bucketName = cursor.getString(bucketNameColumn).orEmpty(),
                    relativePath = location.relativePath,
                    contentUri = ContentUris.withAppendedId(collection, id).toString(),
                    volume = location.volume,
                ),
            )
        }
        return items
    }

    private fun sortOrder(sort: MediaSort): String {
        val column = when (sort.field) {
            MediaSortField.NAME -> MediaStore.Images.Media.DISPLAY_NAME + " COLLATE NOCASE"
            MediaSortField.DATE_MODIFIED -> MediaStore.Images.Media.DATE_MODIFIED
            MediaSortField.DATE_TAKEN -> MediaStore.Images.Media.DATE_TAKEN
            MediaSortField.SIZE -> MediaStore.Images.Media.SIZE
            // Random orders are shuffled in memory; a provider query never asks for one.
            MediaSortField.RANDOM -> MediaStore.Images.Media.DATE_MODIFIED
        }
        val direction = if (sort.ascending) "ASC" else "DESC"
        return "$column $direction, ${MediaStore.Images.Media._ID} $direction"
    }

    companion object {
        const val DEFAULT_QUIET_MILLIS = 500L
        const val DEFAULT_MAX_WAIT_MILLIS = 5_000L

        /** Change observers registered right now; the shared scan is only published while one keeps it fresh. */
        private val observers = AtomicInteger()

        @Volatile
        private var sharedAll: List<MediaItem>? = null
    }
}
