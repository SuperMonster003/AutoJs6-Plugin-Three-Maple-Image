package io.github.supermonster003.autojs6.plugin.three.maple.image

/** One MediaStore image row as the gallery keeps it in memory (ROADMAP T6). */
internal data class MediaItem(
    val id: Long,
    val displayName: String,
    val mimeType: String,
    val sizeBytes: Long,
    val width: Int,
    val height: Int,
    /** Seconds since the epoch, as `DATE_MODIFIED` reports it. */
    val dateModifiedSeconds: Long,
    /** Milliseconds since the epoch from `DATE_TAKEN`, or null when the row has none. */
    val dateTakenMillis: Long?,
    val bucketId: Long,
    val bucketName: String,
    /** `RELATIVE_PATH` such as `DCIM/Camera/` (Android 10+), or the parent folder derived from the file path before that. */
    val relativePath: String,
    /** `content://media/...` as a string so the model stays free of Android types. */
    val contentUri: String,
    /** MediaStore volume (`external_primary`, or a storage card such as `1eff-e702`); see [MediaCatalog.PRIMARY_VOLUME]. */
    val volume: String = MediaCatalog.PRIMARY_VOLUME,
) {
    /** Capture time with the modification time as the fallback, in milliseconds. */
    val effectiveTakenMillis: Long get() = dateTakenMillis ?: (dateModifiedSeconds * 1000L)

    val albumPath: AlbumPath get() = AlbumPath(volume, relativePath)
}

/**
 * Where an album lives: a volume and a `RELATIVE_PATH` (`Pictures/Trips/`). Two albums with the same relative path
 * on the internal storage and on a storage card are different albums (ROADMAP 7.3 cross-volume moves).
 */
internal data class AlbumPath(val volume: String, val relativePath: String) {
    val isPrimary: Boolean get() = MediaCatalog.isPrimaryVolume(volume)

    fun sameAs(other: AlbumPath): Boolean = relativePath == other.relativePath && MediaCatalog.sameVolume(volume, other.volume)
}

/** An album: every cataloged image that shares a `BUCKET_ID`. */
internal data class MediaBucket(
    val id: Long,
    val name: String,
    val relativePath: String,
    val count: Int,
    /** The most recent image of the album (capture time, then modification time, then id). */
    val cover: MediaItem,
    val latestModifiedSeconds: Long,
    val volume: String = MediaCatalog.PRIMARY_VOLUME,
)

internal enum class MediaSortField { NAME, DATE_MODIFIED, DATE_TAKEN, SIZE }

internal data class MediaSort(val field: MediaSortField = MediaSortField.DATE_MODIFIED, val ascending: Boolean = false) {
    companion object {
        val DEFAULT = MediaSort()
    }
}

/**
 * Android-free part of the media data layer: which rows the gallery catalogs, how they sort and how albums are
 * aggregated. [MediaStoreRepository] feeds it rows; the grid and the album list consume the results.
 */
internal object MediaCatalog {

    /** Bucket id meaning "every bucket": the flat "all images" view and its viewer source (ROADMAP 6.2). */
    const val ALL_BUCKETS = Long.MIN_VALUE

    /** Id of the "Favorites" virtual album (ROADMAP 7.4); never a MediaStore bucket id in practice. */
    const val FAVORITES = Long.MIN_VALUE + 1

    /** `MediaStore.VOLUME_EXTERNAL_PRIMARY`: the internal shared storage. */
    const val PRIMARY_VOLUME = "external_primary"

    fun isPrimaryVolume(volume: String): Boolean = volume.equals(PRIMARY_VOLUME, ignoreCase = true) || volume.equals("external", ignoreCase = true)

    /** MediaStore names storage cards by their lower-case file system id; compare without case. */
    fun sameVolume(a: String, b: String): Boolean = (isPrimaryVolume(a) && isPrimaryVolume(b)) || a.equals(b, ignoreCase = true)

    /** Supported formats only (ROADMAP D2); zero-byte rows are placeholders MediaStore has not finished writing. */
    fun isCataloged(item: MediaItem, supports: (String, String) -> Boolean): Boolean =
        item.sizeBytes > 0L && supports(item.displayName, item.mimeType)

    fun filter(items: List<MediaItem>, supports: (String, String) -> Boolean): List<MediaItem> =
        items.filter { isCataloged(it, supports) }

    fun comparator(sort: MediaSort): Comparator<MediaItem> {
        val primary: Comparator<MediaItem> = when (sort.field) {
            MediaSortField.NAME -> Comparator { a, b -> ImageSiblingDiscoveryPolicy.compareNaturally(a.displayName, b.displayName) }
            MediaSortField.DATE_MODIFIED -> compareBy { it.dateModifiedSeconds }
            MediaSortField.DATE_TAKEN -> compareBy { it.effectiveTakenMillis }
            MediaSortField.SIZE -> compareBy { it.sizeBytes }
        }
        // The id breaks ties in the same direction as the field, exactly like the provider's "<column> DESC, _id DESC",
        // so in-memory and provider-side pages never disagree on boundaries.
        val combined = primary.thenBy { it.id }
        return if (sort.ascending) combined else combined.reversed()
    }

    fun sort(items: List<MediaItem>, sort: MediaSort): List<MediaItem> = items.sortedWith(comparator(sort))

    private val coverOrder: Comparator<MediaItem> =
        compareBy<MediaItem> { it.effectiveTakenMillis }.thenBy { it.dateModifiedSeconds }.thenBy { it.id }

    /** Groups cataloged rows into albums; the newest album comes first. */
    fun buckets(items: List<MediaItem>): List<MediaBucket> = items
        .groupBy { it.bucketId }
        .map { (id, rows) ->
            val cover = rows.maxWith(coverOrder)
            MediaBucket(
                id = id,
                name = cover.bucketName,
                relativePath = cover.relativePath,
                count = rows.size,
                cover = cover,
                latestModifiedSeconds = rows.maxOf { it.dateModifiedSeconds },
                volume = cover.volume,
            )
        }
        .let { sortBuckets(it, MediaSort.DEFAULT) }

    /** Album order: by name (natural), by latest change, by latest capture or by size (image count). */
    fun sortBuckets(buckets: List<MediaBucket>, sort: MediaSort): List<MediaBucket> {
        val primary: Comparator<MediaBucket> = when (sort.field) {
            MediaSortField.NAME -> Comparator { a, b -> ImageSiblingDiscoveryPolicy.compareNaturally(a.name, b.name) }
            MediaSortField.DATE_MODIFIED -> compareBy { it.latestModifiedSeconds }
            MediaSortField.DATE_TAKEN -> compareBy { it.cover.effectiveTakenMillis }
            MediaSortField.SIZE -> compareBy { it.count }
        }
        val directed = if (sort.ascending) primary else primary.reversed()
        return buckets.sortedWith(
            directed.thenComparator { a, b -> ImageSiblingDiscoveryPolicy.compareNaturally(a.name, b.name) }.thenBy { it.id },
        )
    }

    /** The parent folder of a legacy `DATA` path relative to the external storage root, with a trailing slash. */
    fun relativePathFromFilePath(path: String, externalRoots: List<String>): String = albumPathFromFilePath(path, externalRoots).relativePath

    /**
     * The volume and parent folder of a legacy `DATA` path: under one of [externalRoots] it is the internal storage;
     * under `/storage/<id>/` a storage card named like MediaStore names it on Android 10+ (`b832-11f4`).
     */
    fun albumPathFromFilePath(path: String, externalRoots: List<String>): AlbumPath {
        val parent = path.substringBeforeLast('/', missingDelimiterValue = "")
        val root = externalRoots.firstOrNull { parent == it || parent.startsWith("$it/") }
        val card = if (root == null) CARD_ROOT.matchEntire(parent) else null
        val (volume, relative) = when {
            root != null -> PRIMARY_VOLUME to parent.removePrefix(root).trimStart('/')
            card != null -> card.groupValues[1].lowercase() to card.groupValues[2].trimStart('/')
            else -> PRIMARY_VOLUME to parent.trimStart('/')
        }
        return AlbumPath(volume, if (relative.isEmpty()) "" else "$relative/")
    }

    private val CARD_ROOT = Regex("/storage/([0-9A-Fa-f]{4}-[0-9A-Fa-f]{4}|[0-9A-Fa-f]{8,16})(/.*)?")
}
