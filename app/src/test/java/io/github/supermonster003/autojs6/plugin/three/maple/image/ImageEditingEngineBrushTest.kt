package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.graphics.Bitmap
import android.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ImageEditingEngineBrushTest {

    @Test
    fun highlighterUsesTranslucentSourceOverBlend() {
        val source = solidBitmap(64, 64, Color.WHITE)
        try {
            val result = ImageEditingEngine.applyBrush(
                source,
                listOf(stroke(BrushTool.HIGHLIGHTER, Color.BLACK, 20f, point(32f, 32f))),
            )
            try {
                val center = result.getPixel(32, 32)
                assertEquals(255, Color.alpha(center))
                assertTrue(Color.red(center) in 150..170)
                assertEquals(Color.red(center), Color.green(center))
                assertEquals(Color.red(center), Color.blue(center))
            } finally {
                result.recycle()
            }
        } finally {
            source.recycle()
        }
    }

    @Test
    fun mosaicDestroysFineCheckerboardDetailAboveErrorThreshold() {
        val source = checkerboardBitmap(64, 64)
        try {
            val result = ImageEditingEngine.applyBrush(
                source,
                listOf(
                    stroke(
                        BrushTool.MOSAIC,
                        Color.TRANSPARENT,
                        32f,
                        point(4f, 32f),
                        point(60f, 32f),
                        mosaicBlockSize = 8,
                    ),
                ),
            )
            try {
                var totalError = 0L
                var samples = 0
                val colors = mutableSetOf<Int>()
                for (y in 22 until 42) {
                    for (x in 8 until 56) {
                        val original = source.getPixel(x, y)
                        val obscured = result.getPixel(x, y)
                        totalError += kotlin.math.abs(Color.red(original) - Color.red(obscured))
                        colors += obscured
                        samples += 1
                    }
                }
                val meanAbsoluteError = totalError.toDouble() / samples
                assertTrue("meanAbsoluteError=$meanAbsoluteError", meanAbsoluteError >= 80.0)
                assertTrue("uniqueColors=${colors.size}", colors.size <= 8)
            } finally {
                result.recycle()
            }
        } finally {
            source.recycle()
        }
    }

    @Test
    fun eraserRestoresOriginalPixelsInsteadOfClearingSource() {
        val sourceColor = Color.argb(128, 20, 40, 60)
        val source = solidBitmap(64, 64, sourceColor)
        try {
            val result = ImageEditingEngine.applyBrush(
                source,
                listOf(
                    stroke(
                        BrushTool.PEN,
                        Color.RED,
                        20f,
                        point(8f, 32f),
                        point(56f, 32f),
                    ),
                    stroke(
                        BrushTool.ERASER,
                        Color.TRANSPARENT,
                        8f,
                        point(24f, 32f),
                        point(40f, 32f),
                    ),
                ),
            )
            try {
                assertEquals(sourceColor, source.getPixel(32, 32))
                assertEquals(sourceColor, result.getPixel(32, 32))
                assertNotEquals(sourceColor, result.getPixel(12, 32))
                assertTrue(Color.red(result.getPixel(12, 32)) >= 240)
            } finally {
                result.recycle()
            }
        } finally {
            source.recycle()
        }
    }

    @Test
    fun mosaicStrengthChangesDownsampleResolutionAndCacheRemainsBounded() {
        val source = checkerboardBitmap(64, 64)
        val cache = MosaicBitmapCache(source, maxEntries = 1)
        try {
            val weaker = cache.bitmap(blockSize = 4)
            assertEquals(16, weaker.width)
            assertEquals(16, weaker.height)

            val stronger = cache.bitmap(blockSize = 16)
            assertEquals(4, stronger.width)
            assertEquals(4, stronger.height)
            assertTrue(weaker.isRecycled)
            assertTrue(!stronger.isRecycled)
        } finally {
            cache.close()
            source.recycle()
        }
    }

    private fun stroke(
        tool: BrushTool,
        color: Int,
        width: Float,
        vararg points: ImageEditingEngine.Point,
        mosaicBlockSize: Int = MosaicBitmapCache.MIN_MOSAIC_BLOCK_SIZE,
    ) = ImageEditingEngine.BrushStroke(
        tool = tool,
        color = color,
        width = width,
        mosaicBlockSize = mosaicBlockSize,
        points = points.toList(),
    )

    private fun point(x: Float, y: Float) = ImageEditingEngine.Point(x, y)

    private fun solidBitmap(width: Int, height: Int, color: Int) =
        Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).apply { eraseColor(color) }

    private fun checkerboardBitmap(width: Int, height: Int): Bitmap {
        val pixels = IntArray(width * height) { index ->
            val x = index % width
            val y = index / width
            if ((x + y) % 2 == 0) Color.BLACK else Color.WHITE
        }
        return Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).apply {
            setPixels(pixels, 0, width, 0, 0, width, height)
        }
    }
}
