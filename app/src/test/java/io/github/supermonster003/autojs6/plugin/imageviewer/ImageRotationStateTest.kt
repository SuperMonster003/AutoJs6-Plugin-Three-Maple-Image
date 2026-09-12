package io.github.supermonster003.autojs6.plugin.imageviewer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ImageRotationStateTest {

    @Test
    fun clockwiseAdvancesInQuarterTurnsAndWrapsAtFullCircle() {
        var quarterTurns = ImageRotationState.ORIGINAL_QUARTER_TURNS
        val degrees = buildList {
            repeat(ImageRotationState.QUARTER_TURNS_PER_CIRCLE) {
                quarterTurns = ImageRotationState.clockwise(quarterTurns)
                add(ImageRotationState.degrees(quarterTurns))
            }
        }

        assertEquals(listOf(90, 180, 270, 0), degrees)
    }

    @Test
    fun normalizationHandlesArbitraryPositiveAndNegativeTurns() {
        assertEquals(1, ImageRotationState.normalize(5))
        assertEquals(3, ImageRotationState.normalize(-1))
        assertEquals(0, ImageRotationState.normalize(-8))
    }

    @Test
    fun onlyOddQuarterTurnsSwapDimensions() {
        assertFalse(ImageRotationState.swapsDimensions(0))
        assertTrue(ImageRotationState.swapsDimensions(1))
        assertFalse(ImageRotationState.swapsDimensions(2))
        assertTrue(ImageRotationState.swapsDimensions(3))
    }
}
