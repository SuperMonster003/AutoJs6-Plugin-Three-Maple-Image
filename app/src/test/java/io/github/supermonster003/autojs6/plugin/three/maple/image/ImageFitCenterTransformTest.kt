package io.github.supermonster003.autojs6.plugin.three.maple.image

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ImageFitCenterTransformTest {

    @Test
    fun landscapeSourceFitsInsideOffsetSquareWithoutRotation() {
        val layout = requireNotNull(
            ImageFitCenterTransform.calculate(
                sourceWidth = 400f,
                sourceHeight = 200f,
                contentLeft = 10f,
                contentTop = 20f,
                contentWidth = 600f,
                contentHeight = 600f,
                quarterTurns = 0,
            ),
        )

        assertEquals(1.5f, layout.scale, TOLERANCE)
        assertEquals(600f, layout.fittedWidth, TOLERANCE)
        assertEquals(300f, layout.fittedHeight, TOLERANCE)
        assertEquals(10f, layout.offsetX, TOLERANCE)
        assertEquals(170f, layout.offsetY, TOLERANCE)
        assertArrayEquals(
            floatArrayOf(
                1.5f, 0f, 10f,
                0f, 1.5f, 170f,
                0f, 0f, 1f,
            ),
            layout.matrixValues,
            TOLERANCE,
        )
    }

    @Test
    fun clockwiseQuarterTurnSwapsDimensionsAndMapsEveryCornerInsideContent() {
        val layout = requireNotNull(
            ImageFitCenterTransform.calculate(
                sourceWidth = 400f,
                sourceHeight = 200f,
                contentLeft = 10f,
                contentTop = 20f,
                contentWidth = 600f,
                contentHeight = 600f,
                quarterTurns = 1,
            ),
        )

        assertEquals(1.5f, layout.scale, TOLERANCE)
        assertEquals(300f, layout.fittedWidth, TOLERANCE)
        assertEquals(600f, layout.fittedHeight, TOLERANCE)
        assertEquals(160f, layout.offsetX, TOLERANCE)
        assertEquals(20f, layout.offsetY, TOLERANCE)
        assertArrayEquals(
            floatArrayOf(
                0f, -1.5f, 460f,
                1.5f, 0f, 20f,
                0f, 0f, 1f,
            ),
            layout.matrixValues,
            TOLERANCE,
        )
        assertArrayEquals(
            floatArrayOf(160f, 20f, 460f, 620f),
            mappedBounds(layout.matrixValues, 400f, 200f),
            TOLERANCE,
        )
    }

    @Test
    fun allNormalizedQuarterTurnsMapToTheExpectedFitBounds() {
        val expected = arrayOf(
            floatArrayOf(10f, 170f, 610f, 470f),
            floatArrayOf(160f, 20f, 460f, 620f),
            floatArrayOf(10f, 170f, 610f, 470f),
            floatArrayOf(160f, 20f, 460f, 620f),
        )

        for (quarterTurns in -4..7) {
            val layout = requireNotNull(
                ImageFitCenterTransform.calculate(
                    sourceWidth = 400f,
                    sourceHeight = 200f,
                    contentLeft = 10f,
                    contentTop = 20f,
                    contentWidth = 600f,
                    contentHeight = 600f,
                    quarterTurns = quarterTurns,
                ),
            )
            assertArrayEquals(
                "quarterTurns=$quarterTurns",
                expected[ImageRotationState.normalize(quarterTurns)],
                mappedBounds(layout.matrixValues, 400f, 200f),
                TOLERANCE,
            )
        }
    }

    @Test
    fun invalidGeometryIsRejectedWithoutProducingNaNMatrixValues() {
        assertNull(ImageFitCenterTransform.calculate(0f, 10f, 0f, 0f, 10f, 10f, 0))
        assertNull(ImageFitCenterTransform.calculate(10f, -1f, 0f, 0f, 10f, 10f, 0))
        assertNull(ImageFitCenterTransform.calculate(10f, 10f, 0f, 0f, 0f, 10f, 0))
        assertNull(
            ImageFitCenterTransform.calculate(
                10f,
                10f,
                Float.NaN,
                0f,
                10f,
                10f,
                0,
            ),
        )
        assertTrue(
            requireNotNull(
                ImageFitCenterTransform.calculate(10f, 10f, 0f, 0f, 10f, 10f, 0),
            ).matrixValues.all(Float::isFinite),
        )
    }

    private fun mappedBounds(
        values: FloatArray,
        sourceWidth: Float,
        sourceHeight: Float,
    ): FloatArray {
        val corners = arrayOf(
            0f to 0f,
            sourceWidth to 0f,
            0f to sourceHeight,
            sourceWidth to sourceHeight,
        ).map { (x, y) ->
            val mappedX = values[0] * x + values[1] * y + values[2]
            val mappedY = values[3] * x + values[4] * y + values[5]
            mappedX to mappedY
        }
        return floatArrayOf(
            corners.minOf { it.first },
            corners.minOf { it.second },
            corners.maxOf { it.first },
            corners.maxOf { it.second },
        )
    }

    private companion object {
        const val TOLERANCE = 0.0001f
    }
}
