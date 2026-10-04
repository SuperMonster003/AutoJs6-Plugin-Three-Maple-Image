package io.github.supermonster003.autojs6.plugin.three.maple.image

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ImageExifOrientationTransformTest {

    @Test
    fun allEightExifTransformsMapEncodedCornersIntoGlideNormalizedSpace() {
        val cases = listOf(
            OrientationTransformCase(1, listOf(TL, TR, BL, BR)),
            OrientationTransformCase(2, listOf(TR, TL, BR, BL)),
            OrientationTransformCase(3, listOf(BR, BL, TR, TL)),
            OrientationTransformCase(4, listOf(BL, BR, TL, TR)),
            OrientationTransformCase(5, listOf(TL, BL, TR, BR)),
            OrientationTransformCase(6, listOf(BL, TL, BR, TR)),
            OrientationTransformCase(7, listOf(BR, TR, BL, TL)),
            OrientationTransformCase(8, listOf(TR, BR, TL, BL)),
        )

        cases.forEach { case ->
            val orientation = ImageExifOrientation(
                tagValue = case.tagValue,
                rotationDegrees = 0,
                mirrored = false,
            )
            val values = requireNotNull(
                ImageExifOrientationCorrection.sourceToDisplayMatrixValues(
                    SOURCE_WIDTH,
                    SOURCE_HEIGHT,
                    orientation,
                ),
            )
            val displayWidth = if (case.tagValue >= 5) SOURCE_HEIGHT else SOURCE_WIDTH
            val displayHeight = if (case.tagValue >= 5) SOURCE_WIDTH else SOURCE_HEIGHT
            val actualDisplayCorners = SOURCE_CORNERS.mapValues { (_, point) ->
                map(values, point.x, point.y)
            }
            val actualSourcesAtDisplayCorners = listOf(
                pointAt(actualDisplayCorners, 0f, 0f),
                pointAt(actualDisplayCorners, displayWidth.toFloat(), 0f),
                pointAt(actualDisplayCorners, 0f, displayHeight.toFloat()),
                pointAt(actualDisplayCorners, displayWidth.toFloat(), displayHeight.toFloat()),
            )
            assertArrayEquals(
                "EXIF orientation ${case.tagValue}",
                case.sourcesAtDisplayCorners.toTypedArray(),
                actualSourcesAtDisplayCorners.toTypedArray(),
            )
        }
    }

    @Test
    fun invalidGeometryOrOrientationHasNoTransform() {
        assertNull(ImageExifOrientationCorrection.sourceToDisplayMatrixValues(0, 10, null))
        assertNull(
            ImageExifOrientationCorrection.sourceToDisplayMatrixValues(
                10,
                10,
                ImageExifOrientation(tagValue = 9, rotationDegrees = 0, mirrored = false),
            ),
        )
    }

    private fun map(values: FloatArray, x: Float, y: Float): Point = Point(
        x = values[0] * x + values[1] * y + values[2],
        y = values[3] * x + values[4] * y + values[5],
    )

    private fun pointAt(
        mapped: Map<String, Point>,
        x: Float,
        y: Float,
    ): String = requireNotNull(mapped.entries.firstOrNull { (_, point) -> point.x == x && point.y == y }).key

    private data class Point(val x: Float, val y: Float)

    private data class OrientationTransformCase(
        val tagValue: Int,
        val sourcesAtDisplayCorners: List<String>,
    )

    private companion object {
        const val SOURCE_WIDTH = 80
        const val SOURCE_HEIGHT = 48
        const val TL = "top-left"
        const val TR = "top-right"
        const val BL = "bottom-left"
        const val BR = "bottom-right"
        val SOURCE_CORNERS = mapOf(
            TL to Point(0f, 0f),
            TR to Point(SOURCE_WIDTH.toFloat(), 0f),
            BL to Point(0f, SOURCE_HEIGHT.toFloat()),
            BR to Point(SOURCE_WIDTH.toFloat(), SOURCE_HEIGHT.toFloat()),
        )
    }
}
