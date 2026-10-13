package io.github.supermonster003.autojs6.plugin.three.maple.image

import org.junit.Assert.assertEquals
import org.junit.Test

/** ROADMAP 7.4 / T9: favorites follow their media id, and a re-created row by its content fingerprint. */
class FavoritePolicyTest {

    private fun item(id: Long, name: String = "img$id.jpg", size: Long = 1_000 + id, path: String = "DCIM/Camera/") = MediaItem(
        id = id, displayName = name, mimeType = "image/jpeg", sizeBytes = size, width = 40, height = 30,
        dateModifiedSeconds = 99, dateTakenMillis = null, bucketId = 1, bucketName = "Camera",
        relativePath = path, contentUri = "content://media/external/images/media/$id",
    )

    private fun favorite(of: MediaItem, addedAt: Long) = FavoritePolicy.entity(of, addedAt)

    @Test
    fun favoritesKeepTheirIdsAndOrder() {
        val a = item(1)
        val b = item(2)
        val resolution = FavoritePolicy.resolve(listOf(favorite(b, 20), favorite(a, 10)), listOf(a, b, item(3)))
        assertEquals(listOf(2L, 1L), resolution.items.map { it.id })
        assertEquals(emptyList<Pair<Long, MediaItem>>(), resolution.rebinds)
    }

    @Test
    fun aRecreatedRowIsFoundByItsFingerprint() {
        val before = item(1, path = "DCIM/Camera/")
        val after = before.copy(id = 7, relativePath = "Pictures/Trips/", contentUri = "content://media/external/images/media/7")
        val resolution = FavoritePolicy.resolve(listOf(favorite(before, 10)), listOf(after, item(2)))
        assertEquals(listOf(7L), resolution.items.map { it.id })
        assertEquals(listOf(1L to after), resolution.rebinds)
    }

    @Test
    fun theSameNameWinsAmongTwinsAndAmbiguousTwinsStayUnbound() {
        val before = item(1, name = "sunset.jpg", size = 500)
        val renamedTwin = item(8, name = "copy.jpg", size = 500)
        val sameName = item(9, name = "sunset.jpg", size = 500)
        assertEquals(listOf(9L), FavoritePolicy.resolve(listOf(favorite(before, 1)), listOf(renamedTwin, sameName)).items.map { it.id })
        val otherTwin = item(10, name = "other.jpg", size = 500)
        assertEquals(emptyList<Long>(), FavoritePolicy.resolve(listOf(favorite(before, 1)), listOf(renamedTwin, otherTwin)).items.map { it.id })
    }

    @Test
    fun anImageAlreadyClaimedIsNotTakenTwice() {
        val kept = item(1, size = 500)
        val gone = item(2, name = "img1.jpg", size = 500)
        // "gone" vanished; its only fingerprint twin is a favorite in its own right.
        val resolution = FavoritePolicy.resolve(listOf(favorite(kept, 2), favorite(gone, 1)), listOf(kept))
        assertEquals(listOf(1L), resolution.items.map { it.id })
        assertEquals(emptyList<Pair<Long, MediaItem>>(), resolution.rebinds)
    }
}
