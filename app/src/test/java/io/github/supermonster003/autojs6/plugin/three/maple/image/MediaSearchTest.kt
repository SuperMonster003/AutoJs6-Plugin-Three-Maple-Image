package io.github.supermonster003.autojs6.plugin.three.maple.image

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** ROADMAP 7.5 search: terms, case and the cost over a 50000-image library. */
class MediaSearchTest {

    private fun item(id: Long, name: String) = MediaItem(
        id = id,
        displayName = name,
        mimeType = "image/jpeg",
        sizeBytes = 10,
        width = 1,
        height = 1,
        dateModifiedSeconds = id,
        dateTakenMillis = null,
        bucketId = 1,
        bucketName = "Camera",
        relativePath = "DCIM/Camera/",
        contentUri = "content://media/external/images/media/$id",
    )

    @Test
    fun everyTermMustAppearIgnoringCase() {
        assertEquals(listOf("sunset", "beach"), MediaSearch.terms("  Sunset   BEACH "))
        assertTrue(MediaSearch.matches("IMG_Sunset-Beach.jpg", MediaSearch.terms("beach sunset")))
        assertFalse(MediaSearch.matches("IMG_Sunset-City.jpg", MediaSearch.terms("beach sunset")))
        assertFalse("a blank query matches nothing", MediaSearch.matches("anything.jpg", MediaSearch.terms("   ")))
        val items = listOf(item(1, "Sunset-Beach.png"), item(2, "sunset-city.png"), item(3, "mountain.png"))
        assertEquals(listOf(1L, 2L), MediaSearch.items(items, "SUNSET").map { it.id })
        assertEquals(listOf(2L), MediaSearch.items(items, "sunset city").map { it.id })
        assertEquals(emptyList<MediaItem>(), MediaSearch.items(items, ""))
        assertEquals("non-Latin names", listOf(4L), MediaSearch.items(items + item(4, "海边日落.jpg"), "日落").map { it.id })
    }

    @Test
    fun aFiftyThousandImageLibraryIsSearchedQuickly() {
        val library = (1L..50_000L).map { item(it, "IMG_2026${it % 12}_${it}_${if (it % 97 == 0L) "Beach" else "Street"}.jpg") }
        MediaSearch.items(library, "beach") // warm up
        val started = System.nanoTime()
        val found = MediaSearch.items(library, "beach")
        val millis = (System.nanoTime() - started) / 1_000_000
        println("search over ${library.size} names: $millis ms, ${found.size} results")
        assertEquals(50_000 / 97, found.size)
        // Generous on a shared CI machine; on a phone the whole first screen budget is 500 ms (ROADMAP 7.5).
        assertTrue("took $millis ms", millis < 250)
    }
}
