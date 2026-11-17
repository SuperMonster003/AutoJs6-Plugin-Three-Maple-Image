package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import java.io.ByteArrayOutputStream
import kotlin.math.abs
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** "Lossy compression" of PNG output: the rules of the forms and the encoding pipeline behind them. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class PngLossyOutputTest {

    private val source = ImageBitmapIO.SourceInfo(
        rawSize = ImagePixelSize(4000, 3000),
        displaySize = ImagePixelSize(4000, 3000),
        mimeType = "image/png",
        orientation = 1,
    )

    @Test
    fun pngHasAQualityAndATargetSizeOnlyWhileLossyCompressionIsOn() {
        val png = ImageOutputFormat.PNG
        assertFalse(ImageOutputEncodingPolicy.qualityEnabled(png, webpLosslessRequested = false, sdkInt = 34))
        assertTrue(ImageOutputEncodingPolicy.qualityEnabled(png, webpLosslessRequested = false, sdkInt = 34, pngLossyRequested = true))
        // The target size picks the quality itself, as for JPEG and WebP.
        assertFalse(
            ImageOutputEncodingPolicy.qualityEnabled(png, webpLosslessRequested = false, sdkInt = 34, targetFileSizeRequested = true, pngLossyRequested = true),
        )
        assertFalse(ImageOutputEncodingPolicy.supportsTargetFileSize(png, webpLosslessRequested = false, sdkInt = 34))
        assertTrue(ImageOutputEncodingPolicy.supportsTargetFileSize(png, webpLosslessRequested = false, sdkInt = 34, pngLossyRequested = true))
        assertFalse(ImageTargetFileSizePolicy.isAvailable(png, webpLosslessRequested = false, sdkInt = 34, maxOutputBytes = 1L shl 20))
        assertTrue(
            ImageTargetFileSizePolicy.isAvailable(png, webpLosslessRequested = false, sdkInt = 34, maxOutputBytes = 1L shl 20, pngLossyRequested = true),
        )
        // The switch belongs to PNG alone and changes nothing for the other formats.
        assertFalse(ImageOutputEncodingPolicy.usesPngLossy(ImageOutputFormat.JPEG, requested = true))
        assertTrue(ImageOutputEncodingPolicy.qualityEnabled(ImageOutputFormat.JPEG, webpLosslessRequested = false, sdkInt = 34, pngLossyRequested = true))
        assertFalse(
            ImageOutputEncodingPolicy.qualityEnabled(ImageOutputFormat.WEBP, webpLosslessRequested = true, sdkInt = 34, pngLossyRequested = true),
        )
    }

    @Test
    fun optionsAcceptLossyPngOnlyForPngAndKnowWhenItIsStillLossless() {
        val size = ImagePixelSize(100, 80)
        assertThrows(IllegalArgumentException::class.java) { ImageConversionOptions(ImageOutputFormat.JPEG, targetSize = size, pngLossy = true) }
        assertThrows(IllegalArgumentException::class.java) {
            ImageConversionOptions(ImageOutputFormat.PNG, targetSize = size, targetFileSizeBytes = 4096L)
        }
        assertNotNull(ImageConversionOptions(ImageOutputFormat.PNG, targetSize = size, pngLossy = true, targetFileSizeBytes = 4096L))
        assertNull(ImageConversionOptions(ImageOutputFormat.PNG, quality = 50, targetSize = size).pngLossyLevel)
        assertNull(ImageConversionOptions(ImageOutputFormat.PNG, quality = 100, targetSize = size, pngLossy = true).pngLossyLevel)
        assertEquals(
            PngLossyPolicy.level(50),
            ImageConversionOptions(ImageOutputFormat.PNG, quality = 50, targetSize = size, pngLossy = true).pngLossyLevel,
        )
        assertNull(ImageConversionOptions(ImageOutputFormat.WEBP, quality = 50, targetSize = size).pngLossyLevel)

        val lossless = ImageConversionSizing.estimateEncodedBytes(ImageConversionOptions(ImageOutputFormat.PNG, quality = 50, targetSize = size))
        val lossy = ImageConversionSizing.estimateEncodedBytes(ImageConversionOptions(ImageOutputFormat.PNG, quality = 50, targetSize = size, pngLossy = true))
        val lossyAtFullQuality = ImageConversionSizing.estimateEncodedBytes(
            ImageConversionOptions(ImageOutputFormat.PNG, quality = 100, targetSize = size, pngLossy = true),
        )
        assertTrue("lossless $lossless, lossy $lossy", lossy < lossless)
        assertEquals(lossless, lossyAtFullQuality)
    }

    @Test
    fun theConverterOffersLossyCompressionForPngAndThenUnlocksQualityAndTargetSize() {
        val evaluator = ConverterFormEvaluator(source, ImageOutputFormat.entries.toList(), 50L * 1024 * 1024, 33, { true })
        val png = evaluator.defaultDraft()
        assertEquals(ImageOutputFormat.PNG.mimeType, png.formatMimeType)
        assertFalse(png.pngLossy)

        val off = evaluator.evaluate(png)
        assertTrue(off.pngLossyVisible)
        assertFalse(off.qualityEnabled)
        assertFalse(off.targetFileSizeVisible)
        assertFalse(requireNotNull(off.options).pngLossy)

        val on = evaluator.evaluate(png.copy(pngLossy = true, quality = 60))
        assertTrue(on.qualityEnabled)
        assertTrue(on.targetFileSizeVisible)
        assertTrue(requireNotNull(on.options).pngLossy)
        assertEquals(60, requireNotNull(on.options).quality)
        assertTrue(requireNotNull(on.estimatedBytes) < requireNotNull(off.estimatedBytes))

        val target = evaluator.evaluate(png.copy(pngLossy = true, targetFileSizeEnabled = true, targetFileSizeKibibytes = "300"))
        assertTrue(target.targetFileSizeActive)
        assertFalse(target.qualityEnabled)
        assertEquals(300L * 1024, requireNotNull(target.options).targetFileSizeBytes)
        assertTrue(requireNotNull(target.options).pngLossy)

        // A target size left on from another format does not apply to lossless PNG.
        val leftover = evaluator.evaluate(png.copy(targetFileSizeEnabled = true, targetFileSizeKibibytes = "300"))
        assertFalse(leftover.targetFileSizeActive)
        assertNull(requireNotNull(leftover.options).targetFileSizeBytes)

        val jpeg = evaluator.evaluate(png.copy(formatMimeType = ImageOutputFormat.JPEG.mimeType, pngLossy = true))
        assertFalse(jpeg.pngLossyVisible)
        assertTrue(jpeg.qualityEnabled)
        assertFalse(requireNotNull(jpeg.options).pngLossy)
    }

    @Test
    fun batchConversionAndTheEditorPassTheSwitchOnlyToPngOutput() {
        val pngBatch = BatchConversionSettings(format = ImageOutputFormat.PNG)
        assertTrue(BatchConversionPolicy.pngLossyVisible(pngBatch))
        assertFalse(BatchConversionPolicy.qualityEnabled(pngBatch, sdkInt = 33))
        assertTrue(BatchConversionPolicy.qualityEnabled(pngBatch.copy(pngLossy = true), sdkInt = 33))
        assertFalse(BatchConversionPolicy.pngLossyVisible(BatchConversionSettings(format = ImageOutputFormat.JPEG)))

        fun batchOptions(settings: BatchConversionSettings) =
            (BatchConversionPolicy.plan(settings, source, 33) { true } as BatchItemPlan.Ready).options
        assertFalse(batchOptions(pngBatch).pngLossy)
        assertTrue(batchOptions(pngBatch.copy(pngLossy = true, quality = 70)).pngLossy)
        assertFalse(batchOptions(BatchConversionSettings(format = ImageOutputFormat.JPEG, pngLossy = true)).pngLossy)

        val mimeTypes = ImageOutputFormat.entries.map { it.mimeType }.toSet()
        fun editorOptions(save: EditorSaveOptions, detected: String) =
            EditorSavePolicy.conversionOptions(save, detected, mimeTypes, ImagePixelSize(640, 480), sdkInt = 33)
        assertFalse(editorOptions(EditorSaveOptions(), "image/png").pngLossy)
        // "Follow source" on a PNG and an explicit PNG both honor the switch; a JPEG result ignores it.
        assertTrue(editorOptions(EditorSaveOptions(pngLossy = true), "image/png").pngLossy)
        assertTrue(editorOptions(EditorSaveOptions(format = EditorSaveFormat.PNG, pngLossy = true), "image/jpeg").pngLossy)
        assertFalse(editorOptions(EditorSaveOptions(pngLossy = true), "image/jpeg").pngLossy)
    }

    @Test
    fun lossyPngIsSmallerStaysWithinItsLevelAndFullQualityEqualsLossless() {
        val bitmap = photoLikeBitmap(256, 192)
        try {
            val lossless = encode(bitmap, quality = 50, lossy = false)
            assertArrayEquals(lossless, encode(bitmap, quality = 100, lossy = true))

            var previous = lossless.size
            for (quality in listOf(95, 65, 35, 5)) {
                val level = requireNotNull(PngLossyPolicy.level(quality))
                val encoded = encode(bitmap, quality, lossy = true)
                assertTrue("quality $quality: ${encoded.size} after $previous", encoded.size < previous)
                previous = encoded.size
                val decoded = requireNotNull(BitmapFactory.decodeByteArray(encoded, 0, encoded.size))
                try {
                    assertEquals("image/png", BitmapFactory.Options().apply { inJustDecodeBounds = true }.also {
                        BitmapFactory.decodeByteArray(encoded, 0, encoded.size, it)
                    }.outMimeType)
                    assertWithin(bitmap, decoded, level.maxTolerance)
                } finally {
                    decoded.recycle()
                }
            }
            assertTrue("lossless ${lossless.size}, lowest $previous", previous < lossless.size * 3 / 4)
        } finally {
            bitmap.recycle()
        }
    }

    @Test
    fun aTargetSizeSelectsTheBestPngQualityThatFits() {
        val bitmap = photoLikeBitmap(256, 192)
        try {
            val sizes = PngLossyPolicy.DISTINCT_QUALITIES.associateWith { quality -> encode(bitmap, quality, lossy = true).size.toLong() }
            val smallest = sizes.getValue(9)
            val largest = sizes.getValue(100)
            assertTrue("sizes $sizes", smallest < largest)

            val middle = (smallest + largest) / 2
            val expected = PngLossyPolicy.DISTINCT_QUALITIES.first { sizes.getValue(it) <= middle }
            val within = ImageBitmapIO.selectTargetFileSize(bitmap, targetOptions(bitmap, middle), ImageToolsPlugin.MAX_OUTPUT_BYTES)
            assertEquals(ImageTargetFileSizeStatus.WITHIN_QUALITY_RANGE, within.status)
            assertEquals(expected, within.quality)
            assertEquals(sizes.getValue(expected), within.encodedBytes)
            assertTrue("attempts ${within.attemptCount}", within.attemptCount <= 6)

            val written = ByteArrayOutputStream()
            val result = ImageBitmapIO.encodeOutput(bitmap, targetOptions(bitmap, middle), written, ImageToolsPlugin.MAX_OUTPUT_BYTES)
            assertEquals(expected, result.quality)
            assertEquals(written.size().toLong(), result.encodedBytes)
            assertTrue(written.size() <= middle)

            val tooSmall = ImageBitmapIO.selectTargetFileSize(bitmap, targetOptions(bitmap, smallest - 1), ImageToolsPlugin.MAX_OUTPUT_BYTES)
            assertEquals(ImageTargetFileSizeStatus.TARGET_BELOW_MINIMUM_QUALITY, tooSmall.status)
            assertEquals(9, tooSmall.quality)

            val roomy = ImageBitmapIO.selectTargetFileSize(bitmap, targetOptions(bitmap, largest + 1), ImageToolsPlugin.MAX_OUTPUT_BYTES)
            assertEquals(ImageTargetFileSizeStatus.TARGET_ABOVE_MAXIMUM_QUALITY, roomy.status)
            assertEquals(100, roomy.quality)
        } finally {
            bitmap.recycle()
        }
    }

    private fun targetOptions(bitmap: Bitmap, targetBytes: Long) = ImageConversionOptions(
        format = ImageOutputFormat.PNG,
        targetSize = ImagePixelSize(bitmap.width, bitmap.height),
        pngLossy = true,
        targetFileSizeBytes = targetBytes,
    )

    private fun encode(bitmap: Bitmap, quality: Int, lossy: Boolean): ByteArray {
        val output = ByteArrayOutputStream()
        ImageBitmapIO.encodeOutput(
            bitmap = bitmap,
            options = ImageConversionOptions(
                format = ImageOutputFormat.PNG,
                quality = quality,
                targetSize = ImagePixelSize(bitmap.width, bitmap.height),
                pngLossy = lossy,
            ),
            output = output,
            maxOutputBytes = ImageToolsPlugin.MAX_OUTPUT_BYTES,
        )
        return output.toByteArray()
    }

    private fun assertWithin(expected: Bitmap, actual: Bitmap, tolerance: Int) {
        assertEquals(expected.width, actual.width)
        assertEquals(expected.height, actual.height)
        val before = IntArray(expected.width * expected.height).also { expected.getPixels(it, 0, expected.width, 0, 0, expected.width, expected.height) }
        val after = IntArray(before.size).also { actual.getPixels(it, 0, actual.width, 0, 0, actual.width, actual.height) }
        for (index in before.indices) {
            for (shift in intArrayOf(24, 16, 8, 0)) {
                val allowed = if (shift == 24) 0 else tolerance
                val difference = abs(((before[index] ushr shift) and 0xFF) - ((after[index] ushr shift) and 0xFF))
                assertTrue("pixel $index shift $shift differs by $difference, allowed $allowed", difference <= allowed)
            }
        }
    }

    /** A color ramp with sensor-like noise; it has more colors than a palette is tried for. */
    private fun photoLikeBitmap(width: Int, height: Int): Bitmap {
        val random = java.util.Random(width * 1_000L + height)
        val pixels = IntArray(width * height) { index ->
            val x = index % width
            val y = index / width
            Color.rgb(
                (x * 255 / width + random.nextInt(9) - 4).coerceIn(0, 255),
                (y * 255 / height + random.nextInt(9) - 4).coerceIn(0, 255),
                ((x + y) * 255 / (width + height) + random.nextInt(9) - 4).coerceIn(0, 255),
            )
        }
        return Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).apply {
            setPixels(pixels, 0, width, 0, 0, width, height)
            setHasAlpha(false)
        }
    }
}
