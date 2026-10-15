package io.github.supermonster003.autojs6.plugin.three.maple.image

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** ROADMAP 9.2: the album and media grid settings; the defaults are the 3.0.0 grids. */
class GridPolicyTest {

    private fun item(id: Long, takenMillis: Long?, modifiedSeconds: Long = 0L, bucket: Long = 7L, path: String = "DCIM/Camera/") = MediaItem(
        id = id, displayName = "$id.jpg", mimeType = "image/jpeg", sizeBytes = 1L, width = 1, height = 1,
        dateModifiedSeconds = modifiedSeconds, dateTakenMillis = takenMillis, bucketId = bucket, bucketName = "Camera",
        relativePath = path, contentUri = "content://media/external/images/media/$id",
    )

    @Test fun theDefaultsAreThe300Grids() {
        val defaults = GridSettings()
        assertEquals(4, GridPolicy.mediaColumns(4, defaults, landscape = true))
        assertEquals(2, GridPolicy.albumColumns(defaults, landscape = false, widthDp = 411))
        assertEquals(ThumbnailCorners.NONE, defaults.corners)
        assertEquals(2, defaults.spacing.dp)
        assertTrue(defaults.cropSquare && defaults.typeBadges && defaults.albumCounts && defaults.singleLineAlbumNames && defaults.animatedThumbnails)
        assertEquals(AlbumCoverOrder.NEWEST, defaults.albumCover)
    }

    @Test fun landscapeMayKeepItsOwnMediaColumns() {
        val settings = GridSettings(mediaColumnsLandscape = 6)
        assertEquals(3, GridPolicy.mediaColumns(3, settings, landscape = false))
        assertEquals(6, GridPolicy.mediaColumns(3, settings, landscape = true))
        assertEquals(GalleryPreferences.MAX_COLUMNS, GridPolicy.mediaColumns(3, GridSettings(mediaColumnsLandscape = 40), landscape = true))
    }

    @Test fun albumColumnsAreChosenPerOrientationOrFollowTheWidth() {
        assertEquals(2, GridPolicy.albumColumns(GridSettings(), false, 411))
        assertEquals(3, GridPolicy.albumColumns(GridSettings(), true, 700))
        assertEquals(4, GridPolicy.albumColumns(GridSettings(), true, 900))
        val chosen = GridSettings(albumColumnsPortrait = 3, albumColumnsLandscape = 5)
        assertEquals(3, GridPolicy.albumColumns(chosen, false, 411))
        assertEquals(5, GridPolicy.albumColumns(chosen, true, 411))
        assertEquals(GridPolicy.MAX_ALBUM_COLUMNS, GridPolicy.albumColumns(GridSettings(albumColumnsPortrait = 9), false, 411))
        assertEquals(GridPolicy.MIN_ALBUM_COLUMNS, GridPolicy.albumColumns(GridSettings(albumColumnsPortrait = 1), false, 411))
    }

    @Test fun theCoverIsTheAlbumsChoiceThenTheNewestOrTheOldest() {
        val rows = listOf(item(1, 3_000), item(2, 1_000), item(3, 2_000))
        assertEquals(1L, GridPolicy.cover(rows, AlbumCoverOrder.NEWEST, null)?.id)
        assertEquals(2L, GridPolicy.cover(rows, AlbumCoverOrder.OLDEST, null)?.id)
        assertEquals(3L, GridPolicy.cover(rows, AlbumCoverOrder.NEWEST, chosenId = 3))
        assertEquals("a chosen image that left the album", 1L, GridPolicy.cover(rows, AlbumCoverOrder.NEWEST, chosenId = 99)?.id)
        assertNull(GridPolicy.cover(emptyList(), AlbumCoverOrder.NEWEST, null))
    }

    @Test fun anAlbumsOrderStaysOnItsNewestImageWhateverTheCover() {
        val rows = listOf(item(1, 3_000), item(2, 1_000), item(3, 2_000), item(9, 500, bucket = 8, path = "Pictures/Other/"))
        val oldest = MediaCatalog.buckets(rows, AlbumCoverOrder.OLDEST)
        val camera = oldest.first { it.id == 7L }
        assertEquals(2L, camera.cover.id)
        assertEquals(3_000L, camera.latestTakenMillis)
        val chosen = MediaCatalog.buckets(rows, AlbumCoverOrder.NEWEST) { path -> if (path.relativePath == "DCIM/Camera/") 3L else null }
        assertEquals(3L, chosen.first { it.id == 7L }.cover.id)
        assertEquals(9L, chosen.first { it.id == 8L }.cover.id)
        assertEquals(1L, MediaCatalog.buckets(rows).first { it.id == 7L }.cover.id)
    }

    private fun assertEquals(expected: Long, actual: MediaItem?) = assertEquals(expected, actual?.id)
}
