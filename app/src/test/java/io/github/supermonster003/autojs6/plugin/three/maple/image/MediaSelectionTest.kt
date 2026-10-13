package io.github.supermonster003.autojs6.plugin.three.maple.image

import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.autoScrollSpeed
import org.junit.Assert.assertEquals
import org.junit.Test

/** ROADMAP 7.1: toggles, drag ranges, inversion, edge auto-scroll and the MIME type of a multi-share. */
class MediaSelectionTest {

    private val ordered = listOf(10L, 11L, 12L, 13L, 14L, 15L)

    @Test
    fun togglingAddsAndRemoves() {
        assertEquals(setOf(1L), MediaSelection.toggle(emptySet(), 1))
        assertEquals(setOf(2L), MediaSelection.toggle(setOf(1L, 2L), 1))
    }

    @Test
    fun aDragRangeRunsEitherWayAndShrinksBackKeepingTheBase() {
        assertEquals(setOf(11L, 12L, 13L, 14L), MediaSelection.range(emptySet(), ordered, anchor = 11, target = 14))
        assertEquals(setOf(11L, 12L, 13L), MediaSelection.range(emptySet(), ordered, anchor = 13, target = 11))
        // Moving back toward the anchor shrinks the range, the base selection stays.
        assertEquals(setOf(15L, 11L, 12L), MediaSelection.range(setOf(15L), ordered, anchor = 11, target = 12))
        assertEquals(setOf(15L, 11L), MediaSelection.range(setOf(15L), ordered, anchor = 11, target = 11))
        // An id outside the loaded order only adds the two ends.
        assertEquals(setOf(11L, 99L), MediaSelection.range(emptySet(), ordered, anchor = 11, target = 99))
    }

    @Test
    fun inversionKeepsAlbumOrder() {
        assertEquals(listOf(10L, 12L, 14L, 15L), MediaSelection.invert(ordered, setOf(11L, 13L)).toList())
        assertEquals(emptySet<Long>(), MediaSelection.invert(ordered, ordered.toSet()))
    }

    @Test
    fun autoScrollRampsInsideTheEdgeBands() {
        assertEquals(0f, autoScrollSpeed(y = 500f, height = 1000f, edgePx = 100f, maxSpeedPx = 20f))
        assertEquals(-20f, autoScrollSpeed(y = 0f, height = 1000f, edgePx = 100f, maxSpeedPx = 20f))
        assertEquals(-10f, autoScrollSpeed(y = 50f, height = 1000f, edgePx = 100f, maxSpeedPx = 20f))
        assertEquals(10f, autoScrollSpeed(y = 950f, height = 1000f, edgePx = 100f, maxSpeedPx = 20f))
        // Dragging past the grid edge keeps the maximum speed.
        assertEquals(20f, autoScrollSpeed(y = 1200f, height = 1000f, edgePx = 100f, maxSpeedPx = 20f))
        assertEquals(-20f, autoScrollSpeed(y = -40f, height = 1000f, edgePx = 100f, maxSpeedPx = 20f))
    }

    @Test
    fun trashedFilesShowTheirOriginalName() {
        assertEquals("a.png", RecycleBinNames.original(".trashed-1794496770-a.png"))
        assertEquals(".trashed-x-a.png", RecycleBinNames.original(".trashed-x-a.png"))
        assertEquals("b.jpg", RecycleBinNames.original("b.jpg"))
    }

    @Test
    fun aMultiShareUsesTheCommonType() {
        assertEquals("image/png", GalleryOperations.sharedType(listOf("image/png", "IMAGE/PNG")))
        assertEquals("image/*", GalleryOperations.sharedType(listOf("image/png", "image/jpeg")))
        assertEquals("*/*", GalleryOperations.sharedType(listOf("image/png", "application/octet-stream")))
    }
}
