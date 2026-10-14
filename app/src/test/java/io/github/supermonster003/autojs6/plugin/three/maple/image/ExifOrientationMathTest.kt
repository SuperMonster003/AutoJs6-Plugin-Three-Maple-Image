package io.github.supermonster003.autojs6.plugin.three.maple.image

import androidx.exifinterface.media.ExifInterface
import org.junit.Assert.assertEquals
import org.junit.Test

class ExifOrientationMathTest {

    private val all = (1..8).toList()

    @Test fun plainRotationsStepThroughTheQuarterTurns() {
        assertEquals(ExifInterface.ORIENTATION_ROTATE_90, ExifOrientationMath.rotate(ExifInterface.ORIENTATION_NORMAL, 1))
        assertEquals(ExifInterface.ORIENTATION_ROTATE_180, ExifOrientationMath.rotate(ExifInterface.ORIENTATION_ROTATE_90, 1))
        assertEquals(ExifInterface.ORIENTATION_ROTATE_270, ExifOrientationMath.rotate(ExifInterface.ORIENTATION_ROTATE_180, 1))
        assertEquals(ExifInterface.ORIENTATION_NORMAL, ExifOrientationMath.rotate(ExifInterface.ORIENTATION_ROTATE_270, 1))
        assertEquals(ExifInterface.ORIENTATION_ROTATE_270, ExifOrientationMath.rotate(ExifInterface.ORIENTATION_NORMAL, -1))
    }

    @Test fun mirroredOrientationsStayMirrored() {
        // EXIF 7 is "mirror horizontally, then turn 90 degrees clockwise", 5 the same with 270 degrees.
        assertEquals(ExifInterface.ORIENTATION_TRANSVERSE, ExifOrientationMath.rotate(ExifInterface.ORIENTATION_FLIP_HORIZONTAL, 1))
        assertEquals(ExifInterface.ORIENTATION_TRANSPOSE, ExifOrientationMath.rotate(ExifInterface.ORIENTATION_FLIP_HORIZONTAL, 3))
        assertEquals(ExifInterface.ORIENTATION_FLIP_VERTICAL, ExifOrientationMath.rotate(ExifInterface.ORIENTATION_FLIP_HORIZONTAL, 2))
        val mirrored = setOf(2, 4, 5, 7)
        all.forEach { orientation ->
            (0..3).forEach { turns -> assertEquals(orientation in mirrored, ExifOrientationMath.rotate(orientation, turns) in mirrored) }
        }
    }

    @Test fun fourTurnsAreTheIdentityAndTurnsAdd() {
        all.forEach { orientation ->
            assertEquals(orientation, ExifOrientationMath.rotate(orientation, 4))
            assertEquals(orientation, ExifOrientationMath.rotate(orientation, 0))
            (0..3).forEach { a ->
                (0..3).forEach { b ->
                    assertEquals(
                        ExifOrientationMath.rotate(orientation, a + b),
                        ExifOrientationMath.rotate(ExifOrientationMath.rotate(orientation, a), b),
                    )
                }
            }
        }
    }

    @Test fun eachTurnReachesDifferentOrientations() {
        all.forEach { orientation -> assertEquals(4, (0..3).map { ExifOrientationMath.rotate(orientation, it) }.toSet().size) }
    }

    @Test fun anUnknownOrientationCountsAsNormal() {
        assertEquals(ExifInterface.ORIENTATION_ROTATE_90, ExifOrientationMath.rotate(ExifInterface.ORIENTATION_UNDEFINED, 1))
        assertEquals(ExifInterface.ORIENTATION_ROTATE_180, ExifOrientationMath.rotate(42, 2))
    }
}
