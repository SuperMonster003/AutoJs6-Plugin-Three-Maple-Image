package io.github.supermonster003.autojs6.plugin.imagetools

import android.graphics.Bitmap
import android.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ImageEditingEngineRotationTest {

    @Test
    fun fineRotationPreservesDimensionsAndCoversEveryCorner() {
        val source = Bitmap.createBitmap(96, 48, Bitmap.Config.ARGB_8888).apply {
            eraseColor(Color.rgb(245, 31, 17))
        }
        try {
            listOf(-45f, -17f, 17f, 45f).forEach { degrees ->
                val rotated = ImageEditingEngine.rotateAndCrop(
                    source = source,
                    rotation = degrees,
                    maxPixelCount = source.width.toLong() * source.height,
                )
                try {
                    assertEquals(source.width, rotated.width)
                    assertEquals(source.height, rotated.height)
                    listOf(
                        0 to 0,
                        rotated.width - 1 to 0,
                        0 to rotated.height - 1,
                        rotated.width - 1 to rotated.height - 1,
                    ).forEach { (x, y) -> assertSourceColor(rotated.getPixel(x, y)) }
                } finally {
                    rotated.recycle()
                }
            }
        } finally {
            source.recycle()
        }
    }

    @Test
    fun fineRotationChecksPixelBudgetBeforeCreatingOutput() {
        val source = Bitmap.createBitmap(12, 8, Bitmap.Config.ARGB_8888)
        try {
            assertThrows(IllegalArgumentException::class.java) {
                ImageEditingEngine.rotateAndCrop(source, 12f, maxPixelCount = 95L)
            }
            assertTrue(!source.isRecycled)
        } finally {
            source.recycle()
        }
    }

    private fun assertSourceColor(color: Int) {
        assertTrue("alpha=$color", Color.alpha(color) >= 250)
        assertTrue("red=$color", Color.red(color) >= 240)
        assertTrue("green=$color", Color.green(color) <= 36)
        assertTrue("blue=$color", Color.blue(color) <= 22)
    }
}
