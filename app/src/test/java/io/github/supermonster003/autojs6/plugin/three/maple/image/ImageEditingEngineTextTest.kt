package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.graphics.Bitmap
import android.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ImageEditingEngineTextTest {

    @Test
    fun multilineOutlineAndShadowProduceTheirExpectedPixelFamilies() {
        val source = transparentBitmap(240, 180)
        try {
            val result = ImageEditingEngine.addText(
                source,
                ImageEditingEngine.TextSpec(
                    text = "Top\nBottom",
                    color = Color.RED,
                    size = 40f,
                    centerX = 120f,
                    centerY = 90f,
                    outlineEnabled = true,
                    shadowEnabled = true,
                ),
            )
            try {
                val pixels = result.pixels()
                assertTrue("fill pixels missing", pixels.any(::isRedFill))
                assertTrue("outline pixels missing", pixels.any(::isWhiteOutline))
                assertTrue("shadow pixels missing", pixels.any(::isDarkShadow))
                assertTrue(requireNotNull(result.alphaBounds()).height > 50)
                assertTrue(source.pixels().all { Color.alpha(it) == 0 })
            } finally {
                result.recycle()
            }
        } finally {
            source.recycle()
        }
    }

    @Test
    fun ninetyDegreeRotationTurnsAHorizontalLabelVerticalAroundItsCenter() {
        val source = transparentBitmap(260, 260)
        try {
            val horizontal = ImageEditingEngine.addText(source, labelSpec(rotationDegrees = 0f))
            val vertical = ImageEditingEngine.addText(source, labelSpec(rotationDegrees = 90f))
            try {
                val horizontalBounds = requireNotNull(horizontal.alphaBounds())
                val verticalBounds = requireNotNull(vertical.alphaBounds())
                assertTrue(horizontalBounds.width > horizontalBounds.height * 2)
                assertTrue(verticalBounds.height > verticalBounds.width * 2)
                assertEquals(horizontalBounds.width.toFloat(), verticalBounds.height.toFloat(), 2f)
                assertEquals(horizontalBounds.height.toFloat(), verticalBounds.width.toFloat(), 2f)
                assertEquals(130f, verticalBounds.centerX, 2f)
                assertEquals(130f, verticalBounds.centerY, 2f)
            } finally {
                horizontal.recycle()
                vertical.recycle()
            }
        } finally {
            source.recycle()
        }
    }

    private fun labelSpec(rotationDegrees: Float) = ImageEditingEngine.TextSpec(
        text = "ROTATION",
        color = Color.WHITE,
        size = 30f,
        centerX = 130f,
        centerY = 130f,
        rotationDegrees = rotationDegrees,
    )

    private fun transparentBitmap(width: Int, height: Int) =
        Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).apply {
            eraseColor(Color.TRANSPARENT)
        }

    private fun Bitmap.pixels() = IntArray(width * height).also { pixels ->
        getPixels(pixels, 0, width, 0, 0, width, height)
    }

    private fun Bitmap.alphaBounds(): PixelBounds? {
        var left = width
        var top = height
        var right = -1
        var bottom = -1
        pixels().forEachIndexed { index, color ->
            if (Color.alpha(color) < VISIBLE_ALPHA) return@forEachIndexed
            val x = index % width
            val y = index / width
            left = minOf(left, x)
            top = minOf(top, y)
            right = maxOf(right, x)
            bottom = maxOf(bottom, y)
        }
        return if (right < left || bottom < top) null else {
            PixelBounds(left, top, right + 1, bottom + 1)
        }
    }

    private fun isRedFill(color: Int) =
        Color.alpha(color) > 180 && Color.red(color) > 180 &&
            Color.green(color) < 70 && Color.blue(color) < 70

    private fun isWhiteOutline(color: Int) =
        Color.alpha(color) > 150 && Color.red(color) > 180 &&
            Color.green(color) > 180 && Color.blue(color) > 180

    private fun isDarkShadow(color: Int) =
        Color.alpha(color) in 80..180 && Color.red(color) < 30 &&
            Color.green(color) < 30 && Color.blue(color) < 30

    private data class PixelBounds(
        val left: Int,
        val top: Int,
        val right: Int,
        val bottom: Int,
    ) {
        val width: Int get() = right - left
        val height: Int get() = bottom - top
        val centerX: Float get() = (left + right) / 2f
        val centerY: Float get() = (top + bottom) / 2f
    }

    private companion object {
        const val VISIBLE_ALPHA = 16
    }
}
