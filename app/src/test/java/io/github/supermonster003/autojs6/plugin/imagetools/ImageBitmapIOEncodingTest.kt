package io.github.supermonster003.autojs6.plugin.imagetools

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import java.io.ByteArrayOutputStream
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
class ImageBitmapIOEncodingTest {

    @Test
    fun everyEditorFormatAndQualityCombinationProducesTheRequestedImageType() {
        val bitmap = texturedBitmap(160, 120)
        try {
            ImageOutputFormat.entries.forEach { format ->
                val qualities = if (format.supportsQuality) listOf(1, 50, 100) else listOf(1, 100)
                qualities.forEach { quality ->
                    val encoded = encode(bitmap, format, quality)
                    val bounds = inspect(encoded)
                    assertEquals("$format quality=$quality", format.mimeType, bounds.outMimeType)
                    assertEquals(bitmap.width, bounds.outWidth)
                    assertEquals(bitmap.height, bounds.outHeight)
                }
            }
        } finally {
            bitmap.recycle()
        }
    }

    @Test
    fun qualityChangesLossyOutputsWhilePngRemainsQualityIndependent() {
        val bitmap = texturedBitmap(192, 144)
        try {
            val jpegLow = encode(bitmap, ImageOutputFormat.JPEG, 20)
            val jpegHigh = encode(bitmap, ImageOutputFormat.JPEG, 95)
            val webpLow = encode(bitmap, ImageOutputFormat.WEBP, 20)
            val webpHigh = encode(bitmap, ImageOutputFormat.WEBP, 95)
            val pngLow = encode(bitmap, ImageOutputFormat.PNG, 1)
            val pngHigh = encode(bitmap, ImageOutputFormat.PNG, 100)

            assertTrue("JPEG low=${jpegLow.size}, high=${jpegHigh.size}", jpegHigh.size > jpegLow.size)
            assertTrue("WebP low=${webpLow.size}, high=${webpHigh.size}", webpHigh.size > webpLow.size)
            assertTrue(pngLow.contentEquals(pngHigh))
        } finally {
            bitmap.recycle()
        }
    }

    @Test
    fun jpegUsesTheConvertersDefaultWhiteBackgroundWithoutMutatingTransparentInput() {
        val bitmap = Bitmap.createBitmap(48, 48, Bitmap.Config.ARGB_8888).apply {
            eraseColor(Color.TRANSPARENT)
            setPixel(24, 24, Color.RED)
        }
        try {
            val encoded = encode(bitmap, ImageOutputFormat.JPEG, 100)
            val decoded = requireNotNull(BitmapFactory.decodeByteArray(encoded, 0, encoded.size))
            try {
                val corner = decoded.getPixel(2, 2)
                assertTrue(Color.red(corner) >= 245)
                assertTrue(Color.green(corner) >= 245)
                assertTrue(Color.blue(corner) >= 245)
                assertEquals(Color.TRANSPARENT, bitmap.getPixel(2, 2))
                assertEquals(Color.RED, bitmap.getPixel(24, 24))
            } finally {
                decoded.recycle()
            }
        } finally {
            bitmap.recycle()
        }
    }

    @Test
    fun sharedEncoderEnforcesTheHostOutputByteLimitForEditorAndConverter() {
        val bitmap = texturedBitmap(64, 64)
        try {
            assertThrows(ImageBitmapIO.OutputLimitExceededException::class.java) {
                encode(bitmap, ImageOutputFormat.PNG, 100, maxOutputBytes = 32L)
            }
            assertTrue(!bitmap.isRecycled)
        } finally {
            bitmap.recycle()
        }
    }

    @Test
    fun losslessWebpRoundTripsEveryPixelAndIgnoresTheDisabledQualityValue() {
        val bitmap = texturedBitmap(128, 96)
        try {
            val losslessAtLowQuality = encode(
                bitmap,
                ImageOutputFormat.WEBP,
                quality = 1,
                webpLossless = true,
            )
            val losslessAtHighQuality = encode(
                bitmap,
                ImageOutputFormat.WEBP,
                quality = 100,
                webpLossless = true,
            )
            val decodedLossless = requireNotNull(
                BitmapFactory.decodeByteArray(losslessAtLowQuality, 0, losslessAtLowQuality.size),
            )
            val lossy = encode(bitmap, ImageOutputFormat.WEBP, quality = 50)
            val decodedLossy = requireNotNull(BitmapFactory.decodeByteArray(lossy, 0, lossy.size))
            try {
                assertEquals("image/webp", inspect(losslessAtLowQuality).outMimeType)
                assertArrayEquals(bitmap.pixels(), decodedLossless.pixels())
                assertTrue(losslessAtLowQuality.contentEquals(losslessAtHighQuality))
                assertFalse(bitmap.pixels().contentEquals(decodedLossy.pixels()))
            } finally {
                decodedLossless.recycle()
                decodedLossy.recycle()
            }
        } finally {
            bitmap.recycle()
        }
    }

    @Test
    @Config(sdk = [29])
    fun losslessRequestFallsBackToLegacyLossyWebpBelowAndroidEleven() {
        val bitmap = texturedBitmap(96, 72)
        try {
            val encoded = encode(
                bitmap,
                ImageOutputFormat.WEBP,
                quality = 40,
                webpLossless = true,
            )
            val decoded = requireNotNull(BitmapFactory.decodeByteArray(encoded, 0, encoded.size))
            try {
                assertEquals("image/webp", inspect(encoded).outMimeType)
                assertFalse(bitmap.pixels().contentEquals(decoded.pixels()))
            } finally {
                decoded.recycle()
            }
        } finally {
            bitmap.recycle()
        }
    }

    private fun encode(
        bitmap: Bitmap,
        format: ImageOutputFormat,
        quality: Int,
        maxOutputBytes: Long = ImageToolsPlugin.MAX_OUTPUT_BYTES,
        webpLossless: Boolean = false,
    ): ByteArray {
        val output = ByteArrayOutputStream()
        ImageBitmapIO.encodeOutput(
            bitmap = bitmap,
            options = ImageConversionOptions(
                format = format,
                quality = quality,
                targetSize = ImagePixelSize(bitmap.width, bitmap.height),
                webpLossless = webpLossless,
            ),
            output = output,
            maxOutputBytes = maxOutputBytes,
        )
        return output.toByteArray()
    }

    private fun inspect(encoded: ByteArray) = BitmapFactory.Options().apply {
        inJustDecodeBounds = true
        BitmapFactory.decodeByteArray(encoded, 0, encoded.size, this)
    }

    private fun Bitmap.pixels() = IntArray(width * height).also { pixels ->
        getPixels(pixels, 0, width, 0, 0, width, height)
    }

    private fun texturedBitmap(width: Int, height: Int): Bitmap {
        val pixels = IntArray(width * height) { index ->
            val x = index % width
            val y = index / width
            Color.rgb(
                (x * 37 + y * 17 + x * y) and 0xFF,
                (x * 11 + y * 43 + index * 3) and 0xFF,
                (x * 29 + y * 7 + index * 5) and 0xFF,
            )
        }
        return Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).apply {
            setPixels(pixels, 0, width, 0, 0, width, height)
            setHasAlpha(false)
        }
    }
}
