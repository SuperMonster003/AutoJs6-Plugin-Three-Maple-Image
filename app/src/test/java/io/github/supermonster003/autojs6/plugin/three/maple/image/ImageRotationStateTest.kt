package io.github.supermonster003.autojs6.plugin.three.maple.image

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
    fun freeRotationSnapsToTheNearestQuarterTurn() {
        assertEquals(0, ImageRotationState.snapQuarterTurns(0f))
        assertEquals(0, ImageRotationState.snapQuarterTurns(44.9f))
        assertEquals(1, ImageRotationState.snapQuarterTurns(45f))
        assertEquals(1, ImageRotationState.snapQuarterTurns(80f))
        assertEquals(1, ImageRotationState.snapQuarterTurns(134.9f))
        assertEquals(2, ImageRotationState.snapQuarterTurns(180f))
        assertEquals(3, ImageRotationState.snapQuarterTurns(269f))
        assertEquals(4, ImageRotationState.snapQuarterTurns(359f))
        assertEquals(-1, ImageRotationState.snapQuarterTurns(-80f))
        assertEquals(0, ImageRotationState.snapQuarterTurns(-44f))
        assertEquals(-2, ImageRotationState.snapQuarterTurns(-200f))
        assertEquals(0, ImageRotationState.snapQuarterTurns(Float.NaN))
        assertEquals(3, ImageRotationState.normalize(ImageRotationState.ORIGINAL_QUARTER_TURNS + ImageRotationState.snapQuarterTurns(-80f)))
    }

    @Test
    fun onlyOddQuarterTurnsSwapDimensions() {
        assertFalse(ImageRotationState.swapsDimensions(0))
        assertTrue(ImageRotationState.swapsDimensions(1))
        assertFalse(ImageRotationState.swapsDimensions(2))
        assertTrue(ImageRotationState.swapsDimensions(3))
    }
}
