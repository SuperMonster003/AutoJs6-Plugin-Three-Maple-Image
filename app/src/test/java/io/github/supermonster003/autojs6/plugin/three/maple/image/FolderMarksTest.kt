package io.github.supermonster003.autojs6.plugin.three.maple.image

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** ROADMAP 7.4: which folders the gallery leaves out or pins, independent of Room and MediaStore. */
class FolderMarksTest {

    private fun item(id: Long, volume: String, relativePath: String) = MediaItem(
        id = id,
        displayName = "i$id.jpg",
        mimeType = "image/jpeg",
        sizeBytes = 10,
        width = 1,
        height = 1,
        dateModifiedSeconds = id,
        dateTakenMillis = null,
        bucketId = relativePath.hashCode().toLong(),
        bucketName = relativePath,
        relativePath = relativePath,
        contentUri = "content://media/external/images/media/$id",
        volume = volume,
    )

    @Test
    fun marksGroupByKindAndNormalizeTheVolume() {
        val state = FolderMarkState.of(
            listOf(
                FolderMarkPolicy.entity(AlbumPath("external", "Pictures/A/"), FolderMark.EXCLUDED, 1),
                FolderMarkPolicy.entity(AlbumPath("1EFF-E702", "DCIM/Camera/"), FolderMark.PINNED, 2),
                FolderMarkPolicy.entity(AlbumPath(MediaCatalog.PRIMARY_VOLUME, "Pictures/B/"), FolderMark.HIDDEN, 3),
            ),
        )
        assertTrue(state.isExcluded(AlbumPath(MediaCatalog.PRIMARY_VOLUME, "Pictures/A/")))
        assertFalse(state.isExcluded(AlbumPath("1eff-e702", "Pictures/A/")))
        assertTrue(state.isPinned(AlbumPath("1eff-e702", "DCIM/Camera/")))
        assertEquals(setOf("${MediaCatalog.PRIMARY_VOLUME}|Pictures/B/"), state.hidden)
    }

    @Test
    fun exclusionIsPerFolderNotPerTree() {
        val state = FolderMarkState(excluded = setOf(MediaCatalog.folderKey(AlbumPath(MediaCatalog.PRIMARY_VOLUME, "Pictures/"))))
        val items = listOf(
            item(1, MediaCatalog.PRIMARY_VOLUME, "Pictures/"),
            item(2, MediaCatalog.PRIMARY_VOLUME, "Pictures/Trips/"),
            item(3, "1eff-e702", "Pictures/"),
        )
        assertEquals(listOf(2L, 3L), state.visible(items).map { it.id })
        assertEquals(items, FolderMarkState.NONE.visible(items))
    }

    @Test
    fun pinnedAlbumsComeFirstInTheirOwnOrder() {
        val albums = listOf("d", "a", "c", "b")
        assertEquals(listOf("a", "b", "d", "c"), FolderMarkPolicy.pinnedFirst(albums) { it == "a" || it == "b" })
        assertEquals(albums, FolderMarkPolicy.pinnedFirst(albums) { false })
    }
}
