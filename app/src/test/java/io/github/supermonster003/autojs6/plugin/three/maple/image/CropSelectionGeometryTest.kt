package io.github.supermonster003.autojs6.plugin.three.maple.image

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CropSelectionGeometryTest {

    @Test
    fun presetsResolveExpectedRatiosIncludingSourceRatio() {
        assertNull(CropAspectRatioPreset.FREE.aspectRatio(400, 300))
        assertClose(1f, requireNotNull(CropAspectRatioPreset.SQUARE.aspectRatio(400, 300)))
        assertClose(4f / 3f, requireNotNull(CropAspectRatioPreset.LANDSCAPE_4_3.aspectRatio(400, 300)))
        assertClose(3f / 4f, requireNotNull(CropAspectRatioPreset.PORTRAIT_3_4.aspectRatio(400, 300)))
        assertClose(16f / 9f, requireNotNull(CropAspectRatioPreset.LANDSCAPE_16_9.aspectRatio(400, 300)))
        assertClose(9f / 16f, requireNotNull(CropAspectRatioPreset.PORTRAIT_9_16.aspectRatio(400, 300)))
        assertClose(4f / 3f, requireNotNull(CropAspectRatioPreset.ORIGINAL.aspectRatio(400, 300)))
    }

    @Test
    fun changingPresetRetainsCenterAndNeverExpandsSelection() {
        val selection = CropSelection(20f, 10f, 100f, 70f)

        val square = CropSelectionGeometry.fitAspectRatio(selection, 1f)
        assertClose(selection.centerX, square.centerX)
        assertClose(selection.centerY, square.centerY)
        assertClose(1f, square.width / square.height)
        assertContainedBy(square, selection)

        val landscape = CropSelectionGeometry.fitAspectRatio(selection, 16f / 9f)
        assertClose(selection.centerX, landscape.centerX)
        assertClose(selection.centerY, landscape.centerY)
        assertClose(16f / 9f, landscape.width / landscape.height)
        assertContainedBy(landscape, selection)

        val portrait = CropSelectionGeometry.fitAspectRatio(selection, 9f / 16f)
        assertClose(selection.centerX, portrait.centerX)
        assertClose(selection.centerY, portrait.centerY)
        assertClose(9f / 16f, portrait.width / portrait.height)
        assertContainedBy(portrait, selection)
    }

    @Test
    fun lockedCornerResizeKeepsOppositeCornerAnchored() {
        val selection = CropSelection(20f, 20f, 180f, 110f)
        val resized = CropSelectionGeometry.resizeLocked(
            selection = selection,
            handle = CropResizeHandle.TOP_LEFT,
            deltaX = 40f,
            deltaY = 10f,
            boundsWidth = 200f,
            boundsHeight = 150f,
            aspectRatio = 16f / 9f,
            minimumSide = 10f,
        )

        assertClose(selection.right, resized.right)
        assertClose(selection.bottom, resized.bottom)
        assertClose(16f / 9f, resized.width / resized.height)
        assertContainedByBounds(resized, 200f, 150f)
    }

    @Test
    fun lockedEdgeResizeKeepsPerpendicularCenter() {
        val selection = CropSelection(40f, 20f, 120f, 100f)
        val horizontal = CropSelectionGeometry.resizeLocked(
            selection = selection,
            handle = CropResizeHandle.LEFT,
            deltaX = 20f,
            deltaY = 0f,
            boundsWidth = 160f,
            boundsHeight = 120f,
            aspectRatio = 1f,
            minimumSide = 10f,
        )
        assertClose(selection.right, horizontal.right)
        assertClose(selection.centerY, horizontal.centerY)
        assertClose(1f, horizontal.width / horizontal.height)

        val vertical = CropSelectionGeometry.resizeLocked(
            selection = selection,
            handle = CropResizeHandle.TOP,
            deltaX = 0f,
            deltaY = 20f,
            boundsWidth = 160f,
            boundsHeight = 120f,
            aspectRatio = 1f,
            minimumSide = 10f,
        )
        assertClose(selection.bottom, vertical.bottom)
        assertClose(selection.centerX, vertical.centerX)
        assertClose(1f, vertical.width / vertical.height)
    }

    @Test
    fun lockedResizeClampsToBoundsAndMinimumSide() {
        val selection = CropSelection(10f, 20f, 170f, 110f)
        val expanded = CropSelectionGeometry.resizeLocked(
            selection = selection,
            handle = CropResizeHandle.BOTTOM_RIGHT,
            deltaX = 100f,
            deltaY = 100f,
            boundsWidth = 200f,
            boundsHeight = 120f,
            aspectRatio = 16f / 9f,
            minimumSide = 20f,
        )
        assertClose(16f / 9f, expanded.width / expanded.height)
        assertContainedByBounds(expanded, 200f, 120f)
        assertClose(120f, expanded.bottom)

        val collapsed = CropSelectionGeometry.resizeLocked(
            selection = CropSelection(20f, 20f, 100f, 100f),
            handle = CropResizeHandle.RIGHT,
            deltaX = -200f,
            deltaY = 0f,
            boundsWidth = 120f,
            boundsHeight = 120f,
            aspectRatio = 1f,
            minimumSide = 20f,
        )
        assertClose(20f, collapsed.width)
        assertClose(20f, collapsed.height)
        assertContainedByBounds(collapsed, 120f, 120f)
    }

    @Test
    fun movingLockedSelectionClampsWithoutChangingSize() {
        val selection = CropSelection(20f, 10f, 100f, 70f)
        val moved = CropSelectionGeometry.resizeLocked(
            selection = selection,
            handle = CropResizeHandle.MOVE,
            deltaX = 100f,
            deltaY = 100f,
            boundsWidth = 120f,
            boundsHeight = 80f,
            aspectRatio = 4f / 3f,
            minimumSide = 10f,
        )

        assertClose(selection.width, moved.width)
        assertClose(selection.height, moved.height)
        assertClose(120f, moved.right)
        assertClose(80f, moved.bottom)
        assertContainedByBounds(moved, 120f, 80f)
    }

    private fun assertContainedBy(actual: CropSelection, expectedBounds: CropSelection) {
        assertTrue(actual.left >= expectedBounds.left - TOLERANCE)
        assertTrue(actual.top >= expectedBounds.top - TOLERANCE)
        assertTrue(actual.right <= expectedBounds.right + TOLERANCE)
        assertTrue(actual.bottom <= expectedBounds.bottom + TOLERANCE)
    }

    private fun assertContainedByBounds(selection: CropSelection, width: Float, height: Float) {
        assertTrue(selection.left >= -TOLERANCE)
        assertTrue(selection.top >= -TOLERANCE)
        assertTrue(selection.right <= width + TOLERANCE)
        assertTrue(selection.bottom <= height + TOLERANCE)
    }

    private fun assertClose(expected: Float, actual: Float) {
        assertEquals(expected.toDouble(), actual.toDouble(), TOLERANCE.toDouble())
    }

    private companion object {
        const val TOLERANCE = 0.001f
    }
}
