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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

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

    val collection: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
    } else {
        @Suppress("DEPRECATION")
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    }

    /** Every cataloged image on the device, newest change first; cached until [invalidate] or a change event. */
    suspend fun loadAll(): List<MediaItem> {
        cachedAll?.let { return it }
        val rows = withContext(Dispatchers.IO) { query(selection = null, selectionArgs = null, sort = MediaSort.DEFAULT, limit = null, offset = 0) }
        val cataloged = MediaCatalog.filter(rows, supports)
        cachedAll = cataloged
        return cataloged
    }

    suspend fun loadBuckets(): List<MediaBucket> = MediaCatalog.buckets(loadAll())

    /**
     * One page of an album in the requested order. Date and size pages come from the provider so large albums stay
     * cheap; name order is natural (numbers compare numerically, like the viewer's sibling order), which SQLite
     * cannot express, so name pages are sliced from the cached full scan instead.
     */
    suspend fun loadBucket(bucketId: Long, sort: MediaSort = MediaSort.DEFAULT, limit: Int, offset: Int = 0): List<MediaItem> {
        if (sort.field == MediaSortField.NAME) {
            val album = loadAll().filter { it.bucketId == bucketId }
            return MediaCatalog.sort(album, sort).drop(offset).take(limit)
        }
        return withContext(Dispatchers.IO) {
            val rows = query(
                selection = "${MediaStore.Images.Media.BUCKET_ID} = ?",
                selectionArgs = arrayOf(bucketId.toString()),
                sort = sort,
                limit = limit,
                offset = offset,
            )
            MediaCatalog.filter(rows, supports)
        }
    }

    fun invalidate() {
        cachedAll = null
    }

    /**
     * Emits after MediaStore reports a change, at most once per [throttleMillis]; bursts (a camera burst, a bulk
     * delete) collapse into one emission. The cache is dropped before each emission.
     */
    fun changes(throttleMillis: Long = DEFAULT_THROTTLE_MILLIS): Flow<Unit> = flow {
        var lastEmission = 0L
        rawChanges().collect {
            val now = System.currentTimeMillis()
            val wait = throttleMillis - (now - lastEmission)
            if (wait > 0) delay(wait)
            invalidate()
            lastEmission = System.currentTimeMillis()
            emit(Unit)
        }
    }

    private fun rawChanges(): Flow<Unit> = callbackFlow {
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                trySend(Unit)
            }
        }
        resolver.registerContentObserver(collection, true, observer)
        awaitClose { resolver.unregisterContentObserver(observer) }
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) add(MediaStore.Images.Media.RELATIVE_PATH) else @Suppress("DEPRECATION") add(MediaStore.Images.Media.DATA)
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
        val items = ArrayList<MediaItem>(cursor.count)
        while (cursor.moveToNext()) {
            val id = cursor.getLong(idColumn)
            val relativePath = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                cursor.getString(pathColumn).orEmpty()
            } else {
                MediaCatalog.relativePathFromFilePath(cursor.getString(pathColumn).orEmpty(), externalRoots)
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
                    relativePath = relativePath,
                    contentUri = ContentUris.withAppendedId(collection, id).toString(),
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
        }
        val direction = if (sort.ascending) "ASC" else "DESC"
        return "$column $direction, ${MediaStore.Images.Media._ID} $direction"
    }

    companion object {
        const val DEFAULT_THROTTLE_MILLIS = 400L
    }
}
