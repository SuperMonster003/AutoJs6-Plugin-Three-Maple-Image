package io.github.supermonster003.autojs6.plugin.three.maple.image

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class ImageRotationGeometryTest {

    @Test
    fun zeroDegreesKeepsOriginalDimensionsWithoutZoom() {
        val plan = ImageRotationGeometry.plan(
            sourceWidth = 400,
            sourceHeight = 300,
            degrees = 0f,
            maxPixelCount = 120_000L,
        )

        assertEquals(400, plan.outputWidth)
        assertEquals(300, plan.outputHeight)
        assertEquals(120_000L, plan.outputPixelCount)
        assertClose(1f, plan.coverScale)
    }

    @Test
    fun squareAtFortyFiveDegreesZoomsBySqrtTwoWithRasterMargin() {
        val scale = ImageRotationGeometry.coverScale(500, 500, 45f)

        assertTrue(scale >= sqrt(2.0).toFloat())
        assertTrue(scale < (sqrt(2.0) * 1.002).toFloat())
        assertClose(scale, ImageRotationGeometry.coverScale(500, 500, -45f))
    }

    @Test
    fun rectangularScaleUsesTheLongerAspectConstraint() {
        val degrees = 30f
        val radians = degrees * PI / 180.0
        val theoreticalMinimum = cos(radians) + sin(radians) * 2.0
        val wide = ImageRotationGeometry.coverScale(800, 400, degrees)
        val tall = ImageRotationGeometry.coverScale(400, 800, degrees)

        assertTrue(wide >= theoreticalMinimum.toFloat())
        assertTrue(wide < (theoreticalMinimum * 1.002).toFloat())
        assertClose(wide, tall)
    }

    @Test
    fun planNeverInflatesOutputPixelCount() {
        val plan = ImageRotationGeometry.plan(
            sourceWidth = 4_000,
            sourceHeight = 3_000,
            degrees = 37f,
            maxPixelCount = 12_000_000L,
        )

        assertEquals(12_000_000L, plan.outputPixelCount)
        assertEquals(4_000, plan.outputWidth)
        assertEquals(3_000, plan.outputHeight)
    }

    @Test
    fun invalidAngleAndExceededPixelBudgetAreRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            ImageRotationGeometry.plan(400, 300, 45.1f, 120_000L)
        }
        assertThrows(IllegalArgumentException::class.java) {
            ImageRotationGeometry.plan(400, 300, -45.1f, 120_000L)
        }
        assertThrows(IllegalArgumentException::class.java) {
            ImageRotationGeometry.plan(400, 300, Float.NaN, 120_000L)
        }
        assertThrows(IllegalArgumentException::class.java) {
            ImageRotationGeometry.plan(400, 300, 15f, 119_999L)
        }
    }

    private fun assertClose(expected: Float, actual: Float) {
        assertEquals(expected.toDouble(), actual.toDouble(), TOLERANCE.toDouble())
    }

    private companion object {
        const val TOLERANCE = 0.0001f
    }
}
