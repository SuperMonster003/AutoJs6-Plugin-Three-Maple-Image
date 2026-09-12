package io.github.supermonster003.autojs6.plugin.imageviewer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ImageExifOrientationCorrectionTest {

    @Test
    fun onlyQuarterTurnExifOrientationsSwapDisplayDimensions() {
        val orientations = listOf(
            orientation(tagValue = 1, rotationDegrees = 0) to false,
            orientation(tagValue = 2, rotationDegrees = 0, mirrored = true) to false,
            orientation(tagValue = 3, rotationDegrees = 180) to false,
            orientation(tagValue = 4, rotationDegrees = 180, mirrored = true) to false,
            orientation(tagValue = 5, rotationDegrees = 270, mirrored = true) to true,
            orientation(tagValue = 6, rotationDegrees = 90) to true,
            orientation(tagValue = 7, rotationDegrees = 90, mirrored = true) to true,
            orientation(tagValue = 8, rotationDegrees = 270) to true,
        )

        orientations.forEach { (orientation, swapsDimensions) ->
            if (swapsDimensions) {
                assertTrue(orientation.swapsDimensions)
                assertEquals(
                    ENCODED_HEIGHT to ENCODED_WIDTH,
                    ImageExifOrientationCorrection.displayDimensions(
                        ENCODED_WIDTH,
                        ENCODED_HEIGHT,
                        orientation,
                    ),
                )
            } else {
                assertFalse(orientation.swapsDimensions)
                assertEquals(
                    ENCODED_WIDTH to ENCODED_HEIGHT,
                    ImageExifOrientationCorrection.displayDimensions(
                        ENCODED_WIDTH,
                        ENCODED_HEIGHT,
                        orientation,
                    ),
                )
            }
        }
    }

    @Test
    fun missingExifKeepsEncodedDimensionsAndUnknownValues() {
        assertEquals(
            ENCODED_WIDTH to ENCODED_HEIGHT,
            ImageExifOrientationCorrection.displayDimensions(
                ENCODED_WIDTH,
                ENCODED_HEIGHT,
                orientation = null,
            ),
        )
        assertEquals(
            null to null,
            ImageExifOrientationCorrection.displayDimensions(null, null, orientation = null),
        )
    }

    private fun orientation(
        tagValue: Int,
        rotationDegrees: Int,
        mirrored: Boolean = false,
    ): ImageExifOrientation = ImageExifOrientation(
        tagValue = tagValue,
        rotationDegrees = rotationDegrees,
        mirrored = mirrored,
    )

    private companion object {
        const val ENCODED_WIDTH = 80
        const val ENCODED_HEIGHT = 48
    }
}
