package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.SystemClock
import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import kotlin.math.abs
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * "Lossy compression" of PNG output through the device's own bitmaps and PNG decoder (ROADMAP 11). The measurements
 * only run with `-e pngLossyBenchmark true` (results in logcat, tag MaplePng).
 */
@RunWith(AndroidJUnit4::class)
class PngLossyInstrumentationTest {

    @Test
    fun aPhotographStaysWithinItsLevelGetsSmallerAndFullQualityIsLossless() {
        val bitmap = photoLike(1024, 768)
        try {
            val lossless = encode(bitmap, quality = 100, lossy = false)
            assertArrayEquals(lossless, encode(bitmap, quality = 100, lossy = true))
            var previous = lossless.size
            for (quality in listOf(95, 55, 5)) {
                val level = requireNotNull(PngLossyPolicy.level(quality))
                val encoded = encode(bitmap, quality, lossy = true)
                assertEquals(TRUECOLOR, colorType(encoded))
                assertTrue("quality $quality: ${encoded.size} after $previous", encoded.size < previous)
                previous = encoded.size
                assertWithin(bitmap, encoded, level.maxTolerance)
            }
            assertTrue("lossless ${lossless.size}, lowest $previous", previous < lossless.size * 3 / 4)
        } finally {
            bitmap.recycle()
        }
    }

    @Test
    fun aScreenshotOfFewColorsBecomesAnIndexedPng() {
        val width = 720
        val height = 480
        val row = IntArray(width)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        try {
            for (y in 0 until height) {
                for (x in 0 until width) {
                    row[x] = when {
                        y < 160 -> 0xFFFAFAFA.toInt()
                        y < 320 -> if ((x / 24 + y / 16) % 2 == 0) 0xFF212121.toInt() else 0xFFFAFAFA.toInt()
                        // 600 shades, as along anti-aliased edges: too many for a lossless palette.
                        else -> (0x21 + (x * (0xFA - 0x21)) / (width - 1)).let { shade -> Color.rgb(shade, (shade + y % 3).coerceAtMost(255), shade) }
                    }
                }
                bitmap.setPixels(row, 0, width, 0, y, width, 1)
            }
            bitmap.setHasAlpha(false)
            val lossless = encode(bitmap, quality = 100, lossy = false)
            assertEquals(TRUECOLOR, colorType(lossless))
            val level = requireNotNull(PngLossyPolicy.level(55))
            val encoded = encode(bitmap, quality = 55, lossy = true)
            assertEquals(INDEXED, colorType(encoded))
            assertTrue("lossless ${lossless.size}, lossy ${encoded.size}", encoded.size < lossless.size)
            assertWithin(bitmap, encoded, level.smoothTolerance)
        } finally {
            bitmap.recycle()
        }
    }

    @Test
    fun transparencyIsKeptExactly() {
        val width = 320
        val height = 240
        val source = photoLike(width, height)
        val bitmap = source.copy(Bitmap.Config.ARGB_8888, true)
        source.recycle()
        try {
            bitmap.setHasAlpha(true)
            val row = IntArray(width)
            for (y in 0 until height) {
                bitmap.getPixels(row, 0, width, 0, y, width, 1)
                for (x in 0 until width) if ((x / 16 + y / 16) % 3 == 0) row[x] = (row[x] and 0x00FFFFFF) or (((x + y) % 200 + 20) shl 24)
                bitmap.setPixels(row, 0, width, 0, y, width, 1)
            }
            val level = requireNotNull(PngLossyPolicy.level(35))
            val encoded = encode(bitmap, quality = 35, lossy = true)
            assertEquals(TRUECOLOR_ALPHA, colorType(encoded))
            val decoded = requireNotNull(BitmapFactory.decodeByteArray(encoded, 0, encoded.size))
            try {
                val before = IntArray(width)
                val after = IntArray(width)
                for (y in 0 until height) {
                    bitmap.getPixels(before, 0, width, 0, y, width, 1)
                    decoded.getPixels(after, 0, width, 0, y, width, 1)
                    for (x in 0 until width) {
                        assertEquals("alpha at $x,$y", before[x] ushr 24, after[x] ushr 24)
                        // Translucent pixels are stored premultiplied by the device, which rounds their colors again.
                        if (before[x] ushr 24 == 0xFF) assertChannelsWithin(before[x], after[x], level.maxTolerance, "$x,$y")
                    }
                }
            } finally {
                decoded.recycle()
            }
        } finally {
            bitmap.recycle()
        }
    }

    @Test
    fun measure() {
        assumeTrue(InstrumentationRegistry.getArguments().getString("pngLossyBenchmark") == "true")
        for ((width, height) in listOf(2048 to 1536, 4000 to 3000)) {
            val bitmap = try {
                photoLike(width, height)
            } catch (_: OutOfMemoryError) {
                Log.i(TAG, "$width x $height: skipped, the bitmap does not fit in memory")
                continue
            }
            try {
                val megapixels = width * height / 1_000_000.0
                val lossless = time { count(bitmap, ImageConversionOptions(ImageOutputFormat.PNG, targetSize = ImagePixelSize(width, height))) }
                Log.i(TAG, "%d x %d (%.1f MP) lossless: %d ms, %d bytes".format(width, height, megapixels, lossless.first, lossless.second))
                var smallest = lossless.second
                for (quality in listOf(95, 55, 5)) {
                    val lossy = time {
                        count(bitmap, ImageConversionOptions(ImageOutputFormat.PNG, quality = quality, targetSize = ImagePixelSize(width, height), pngLossy = true))
                    }
                    smallest = lossy.second
                    Log.i(
                        TAG,
                        "%d x %d quality %d: %d ms, %d bytes (%d%% of lossless)".format(width, height, quality, lossy.first, lossy.second, lossy.second * 100 / lossless.second),
                    )
                }
                val target = (smallest + lossless.second) / 2
                val started = SystemClock.elapsedRealtime()
                val result = ImageBitmapIO.encodeOutput(
                    bitmap,
                    ImageConversionOptions(ImageOutputFormat.PNG, targetSize = ImagePixelSize(width, height), pngLossy = true, targetFileSizeBytes = target),
                    CountingOutputStream(),
                    ImageToolsPlugin.MAX_OUTPUT_BYTES,
                )
                Log.i(
                    TAG,
                    "%d x %d target %d bytes: %d ms, quality %d, %d bytes".format(
                        width, height, target, SystemClock.elapsedRealtime() - started, result.quality, result.encodedBytes,
                    ),
                )
            } finally {
                bitmap.recycle()
            }
        }
    }

    private inline fun time(block: () -> Long): Pair<Long, Long> {
        val started = SystemClock.elapsedRealtime()
        val bytes = block()
        return (SystemClock.elapsedRealtime() - started) to bytes
    }

    private fun count(bitmap: Bitmap, options: ImageConversionOptions): Long =
        ImageBitmapIO.encodeOutput(bitmap, options, CountingOutputStream(), ImageToolsPlugin.MAX_OUTPUT_BYTES).encodedBytes

    private class CountingOutputStream : OutputStream() {
        override fun write(value: Int) = Unit
        override fun write(source: ByteArray, offset: Int, length: Int) = Unit
    }

    private fun encode(bitmap: Bitmap, quality: Int, lossy: Boolean): ByteArray {
        val output = ByteArrayOutputStream()
        ImageBitmapIO.encodeOutput(
            bitmap,
            ImageConversionOptions(ImageOutputFormat.PNG, quality = quality, targetSize = ImagePixelSize(bitmap.width, bitmap.height), pngLossy = lossy),
            output,
            ImageToolsPlugin.MAX_OUTPUT_BYTES,
        )
        return output.toByteArray()
    }

    private fun assertWithin(expected: Bitmap, png: ByteArray, tolerance: Int) {
        val decoded = requireNotNull(BitmapFactory.decodeByteArray(png, 0, png.size)) { "The PNG does not decode" }
        try {
            assertEquals(expected.width, decoded.width)
            assertEquals(expected.height, decoded.height)
            val before = IntArray(expected.width)
            val after = IntArray(expected.width)
            for (y in 0 until expected.height) {
                expected.getPixels(before, 0, expected.width, 0, y, expected.width, 1)
                decoded.getPixels(after, 0, expected.width, 0, y, expected.width, 1)
                for (x in before.indices) assertChannelsWithin(before[x], after[x], tolerance, "$x,$y")
            }
        } finally {
            decoded.recycle()
        }
    }

    private fun assertChannelsWithin(before: Int, after: Int, tolerance: Int, where: String) {
        for (shift in intArrayOf(16, 8, 0)) {
            val difference = abs(((before ushr shift) and 0xFF) - ((after ushr shift) and 0xFF))
            if (difference > tolerance) throw AssertionError("pixel $where differs by $difference, allowed $tolerance")
        }
    }

    /** Smooth color ramps with sensor-like noise, written row by row so that large sizes need no second buffer. */
    private fun photoLike(width: Int, height: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val row = IntArray(width)
        var seed = width * 1_000L + height
        for (y in 0 until height) {
            for (x in 0 until width) {
                seed = seed * 6364136223846793005L + 1442695040888963407L
                val noise = (seed ushr 33).toInt()
                row[x] = Color.rgb(
                    (x * 255 / width + (noise and 7) - 4).coerceIn(0, 255),
                    (y * 255 / height + ((noise ushr 3) and 7) - 4).coerceIn(0, 255),
                    ((x + y) * 255 / (width + height) + ((noise ushr 6) and 7) - 4).coerceIn(0, 255),
                )
            }
            bitmap.setPixels(row, 0, width, 0, y, width, 1)
        }
        bitmap.setHasAlpha(false)
        return bitmap
    }

    /** The color type byte of the IHDR chunk. */
    private fun colorType(png: ByteArray): Int = png[25].toInt() and 0xFF

    private companion object {
        const val TAG = "MaplePng"
        const val TRUECOLOR = 2
        const val INDEXED = 3
        const val TRUECOLOR_ALPHA = 6
    }
}
