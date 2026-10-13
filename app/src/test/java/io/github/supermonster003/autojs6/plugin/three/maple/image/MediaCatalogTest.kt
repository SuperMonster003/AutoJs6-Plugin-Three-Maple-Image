package io.github.supermonster003.autojs6.plugin.three.maple.image

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/** ROADMAP 6.1 `MediaStoreRepository` acceptance, Android-free half: 1000 synthetic rows through filter, sort and album aggregation. */
class MediaCatalogTest {

    private val supports: (String, String) -> Boolean = { name, mime ->
        mime in setOf("image/jpeg", "image/png", "image/webp", "image/gif") && !name.endsWith(".tiff")
    }

    private val buckets = listOf(
        1L to "Camera", 2L to "Screenshots", 3L to "Download", 4L to "WhatsApp Images", 5L to "img", 6L to "Pictures", 7L to "Twitter",
    )

    private fun library(): List<MediaItem> {
        val random = Random(20261013)
        return (1..1000).map { index ->
            val (bucketId, bucketName) = buckets[index % buckets.size]
            val unsupported = index % 50 == 0
            val empty = index % 97 == 0
            val modified = 1_700_000_000L + random.nextInt(0, 30_000_000)
            MediaItem(
                id = index.toLong(),
                displayName = if (unsupported) "scan$index.tiff" else "img${index % 37}-$index.jpg",
                mimeType = if (unsupported) "image/tiff" else listOf("image/jpeg", "image/png", "image/webp", "image/gif")[index % 4],
                sizeBytes = if (empty) 0L else random.nextLong(1_000L, 20_000_000L),
                width = 4000,
                height = 3000,
                dateModifiedSeconds = modified,
                dateTakenMillis = if (index % 3 == 0) null else (modified - random.nextInt(0, 86_400)) * 1000L,
                bucketId = bucketId,
                bucketName = bucketName,
                relativePath = "DCIM/$bucketName/",
                contentUri = "content://media/external/images/media/$index",
            )
        }
    }

    @Test
    fun filterDropsUnsupportedAndEmptyRows() {
        val all = library()
        val cataloged = MediaCatalog.filter(all, supports)
        assertEquals(1000 - 20 - 10, cataloged.size)
        assertTrue(cataloged.none { it.mimeType == "image/tiff" || it.sizeBytes == 0L })
    }

    @Test
    fun albumsAggregateCountsAndPickTheNewestCover() {
        val cataloged = MediaCatalog.filter(library(), supports)
        val albums = MediaCatalog.buckets(cataloged)
        assertEquals(7, albums.size)
        assertEquals(cataloged.size, albums.sumOf { it.count })
        for (album in albums) {
            val rows = cataloged.filter { it.bucketId == album.id }
            assertEquals(album.name, rows.size, album.count)
            assertEquals(album.name, rows.maxOf { it.effectiveTakenMillis }, album.cover.effectiveTakenMillis)
            assertEquals(album.name, rows.maxOf { it.dateModifiedSeconds }, album.latestModifiedSeconds)
            assertEquals(album.name, "DCIM/${album.name}/", album.relativePath)
        }
        assertTrue("newest album first", albums.zipWithNext().all { (a, b) -> a.latestModifiedSeconds >= b.latestModifiedSeconds })
        val byName = MediaCatalog.sortBuckets(albums, MediaSort(MediaSortField.NAME, ascending = true)).map { it.name }
        assertEquals(listOf("Camera", "Download", "img", "Pictures", "Screenshots", "Twitter", "WhatsApp Images"), byName)
        val byCount = MediaCatalog.sortBuckets(albums, MediaSort(MediaSortField.SIZE, ascending = false))
        assertTrue(byCount.zipWithNext().all { (a, b) -> a.count >= b.count })
    }

    @Test
    fun everySortFieldOrdersBothWaysWithStableTieBreakers() {
        val cataloged = MediaCatalog.filter(library(), supports)
        for (field in MediaSortField.entries) {
            for (ascending in listOf(true, false)) {
                val sorted = MediaCatalog.sort(cataloged, MediaSort(field, ascending))
                assertEquals(cataloged.size, sorted.size)
                val keys: (MediaItem) -> Long = when (field) {
                    MediaSortField.NAME -> { _ -> 0L }
                    MediaSortField.DATE_MODIFIED -> { it -> it.dateModifiedSeconds }
                    MediaSortField.DATE_TAKEN -> { it -> it.effectiveTakenMillis }
                    MediaSortField.SIZE -> { it -> it.sizeBytes }
                }
                if (field != MediaSortField.NAME) {
                    assertTrue("$field asc=$ascending", sorted.zipWithNext().all { (a, b) -> if (ascending) keys(a) <= keys(b) else keys(a) >= keys(b) })
                } else {
                    assertTrue("$field asc=$ascending", sorted.zipWithNext().all { (a, b) ->
                        val c = ImageSiblingDiscoveryPolicy.compareNaturally(a.displayName, b.displayName)
                        if (ascending) c <= 0 else c >= 0
                    })
                }
                // The same input sorted twice gives the same order (deterministic paging boundaries).
                assertEquals(sorted.map { it.id }, MediaCatalog.sort(cataloged.shuffled(Random(7)), MediaSort(field, ascending)).map { it.id })
            }
        }
    }

    @Test
    fun naturalNameOrderTreatsNumbersNumerically() {
        val names = listOf("img10-1.jpg", "img2-1.jpg", "IMG1-9.jpg", "img2-10.jpg")
        val items = names.mapIndexed { index, name ->
            MediaItem(index.toLong(), name, "image/jpeg", 10L, 1, 1, 1L, null, 1L, "b", "", "content://x/$index")
        }
        val sorted = MediaCatalog.sort(items, MediaSort(MediaSortField.NAME, ascending = true)).map { it.displayName }
        assertEquals(listOf("IMG1-9.jpg", "img2-1.jpg", "img2-10.jpg", "img10-1.jpg"), sorted)
    }

    @Test
    fun legacyPathsBecomeRelativeFolders() {
        val roots = listOf("/storage/emulated/0", "/sdcard")
        assertEquals("DCIM/Camera/", MediaCatalog.relativePathFromFilePath("/storage/emulated/0/DCIM/Camera/a.jpg", roots))
        assertEquals("", MediaCatalog.relativePathFromFilePath("/storage/emulated/0/a.jpg", roots))
        assertEquals("Pictures/", MediaCatalog.relativePathFromFilePath("/sdcard/Pictures/b.png", roots))
        assertEquals("storage/1234-5678/DCIM/", MediaCatalog.relativePathFromFilePath("/storage/1234-5678/DCIM/c.png", roots))
    }
}
