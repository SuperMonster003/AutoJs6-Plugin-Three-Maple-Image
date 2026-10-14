package io.github.supermonster003.autojs6.plugin.three.maple.image

import java.util.Calendar
import java.util.TimeZone
import kotlin.random.Random

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

internal enum class MediaSortField { NAME, DATE_MODIFIED, DATE_TAKEN, SIZE, RANDOM }

/** [seed] shuffles a [MediaSortField.RANDOM] order; it is picked when an album opens and never stored. */
internal data class MediaSort(val field: MediaSortField = MediaSortField.DATE_MODIFIED, val ascending: Boolean = false, val seed: Long = 0L) {
    companion object {
        val DEFAULT = MediaSort()
    }
}

/** Date sections of a media grid (ROADMAP 7.5); they follow the date the album is sorted by. */
internal enum class MediaGrouping { NONE, DAY, MONTH }

/** One section of a sorted album: [start] and [count] index its items; [day] is 0 for a month section. */
internal data class MediaGroup(val year: Int, val month: Int, val day: Int, val start: Int, val count: Int)

/**
 * Positions of a media grid with section headers (ROADMAP 7.5): each group adds one full-width header before its
 * items. Without groups grid positions and item indices are the same.
 */
internal class MediaGroupLayout(private val groups: List<MediaGroup>, private val itemCount: Int) {
    /** Grid position of each group's header. */
    private val headers: IntArray = IntArray(groups.size).also { positions ->
        groups.forEachIndexed { index, group -> positions[index] = group.start + index }
    }

    val gridCount: Int get() = itemCount + groups.size

    /** The group that holds item [itemIndex] (binary search over the group starts). */
    private fun groupOfItem(itemIndex: Int): Int {
        var low = 0
        var high = groups.size - 1
        while (low < high) {
            val mid = (low + high + 1) / 2
            if (groups[mid].start <= itemIndex) low = mid else high = mid - 1
        }
        return low
    }

    fun gridIndex(itemIndex: Int): Int = if (groups.isEmpty()) itemIndex else itemIndex + groupOfItem(itemIndex) + 1

    private fun groupIndexAt(gridIndex: Int): Int {
        var low = 0
        var high = groups.size - 1
        while (low < high) {
            val mid = (low + high + 1) / 2
            if (headers[mid] <= gridIndex) low = mid else high = mid - 1
        }
        return low
    }

    /** The group shown at grid position [gridIndex] (its header or one of its items); null without groups. */
    fun groupAt(gridIndex: Int): MediaGroup? = if (groups.isEmpty()) null else groups[groupIndexAt(gridIndex)]

    /** The item at grid position [gridIndex], or null for a header. */
    fun itemIndex(gridIndex: Int): Int? {
        if (groups.isEmpty()) return gridIndex
        val index = groupIndexAt(gridIndex)
        val header = headers[index]
        return if (gridIndex == header) null else groups[index].start + (gridIndex - header - 1)
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

    fun normalizedVolume(volume: String): String = if (isPrimaryVolume(volume)) PRIMARY_VOLUME else volume.lowercase()

    /** One string per album folder (volume and relative path), for sets of marked folders (ROADMAP 7.4). */
    fun folderKey(path: AlbumPath): String = normalizedVolume(path.volume) + "|" + path.relativePath

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
            // Random orders come from [sort]; the id keeps a comparator total for callers that ask anyway.
            MediaSortField.RANDOM -> compareBy { it.id }
        }
        // The id breaks ties in the same direction as the field, exactly like the provider's "<column> DESC, _id DESC",
        // so in-memory and provider-side pages never disagree on boundaries.
        val combined = primary.thenBy { it.id }
        return if (sort.ascending) combined else combined.reversed()
    }

    fun sort(items: List<MediaItem>, sort: MediaSort): List<MediaItem> =
        if (sort.field == MediaSortField.RANDOM) items.sortedBy { it.id }.shuffled(Random(sort.seed)) else items.sortedWith(comparator(sort))

    /** Grouping only follows a date order (by date taken or date modified). */
    fun groupable(sort: MediaSort): Boolean = sort.field == MediaSortField.DATE_TAKEN || sort.field == MediaSortField.DATE_MODIFIED

    /**
     * The day or month sections of [items], which are in [sort] order; consecutive items of one calendar day /
     * month in [zone] form a section. Empty for [MediaGrouping.NONE] or an order that is not by date.
     */
    fun groups(items: List<MediaItem>, sort: MediaSort, grouping: MediaGrouping, zone: TimeZone): List<MediaGroup> {
        if (grouping == MediaGrouping.NONE || !groupable(sort) || items.isEmpty()) return emptyList()
        val calendar = Calendar.getInstance(zone)
        val groups = ArrayList<MediaGroup>()
        var year = -1
        var month = -1
        var day = -1
        var start = 0
        items.forEachIndexed { index, item ->
            calendar.timeInMillis = if (sort.field == MediaSortField.DATE_TAKEN) item.effectiveTakenMillis else item.dateModifiedSeconds * 1000L
            val y = calendar.get(Calendar.YEAR)
            val m = calendar.get(Calendar.MONTH) + 1
            val d = if (grouping == MediaGrouping.DAY) calendar.get(Calendar.DAY_OF_MONTH) else 0
            if (y != year || m != month || d != day) {
                if (index > 0) groups += MediaGroup(year, month, day, start, index - start)
                year = y
                month = m
                day = d
                start = index
            }
        }
        groups += MediaGroup(year, month, day, start, items.size - start)
        return groups
    }

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
            MediaSortField.RANDOM -> Comparator { a, b -> ImageSiblingDiscoveryPolicy.compareNaturally(a.name, b.name) }
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
