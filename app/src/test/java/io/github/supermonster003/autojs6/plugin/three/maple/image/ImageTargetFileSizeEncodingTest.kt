package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import java.io.ByteArrayOutputStream
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
class ImageTargetFileSizeEncodingTest {

    @Test
    fun realJpegAndWebpEncodersConvergeWithoutExceedingTheRequestedSize() {
        val bitmap = texturedBitmap(320, 240)
        try {
            listOf(ImageOutputFormat.JPEG, ImageOutputFormat.WEBP).forEach { format ->
                val minimumBytes = fixedSize(bitmap, format, ImageConversionOptions.MIN_QUALITY)
                val maximumBytes = fixedSize(bitmap, format, ImageConversionOptions.MAX_QUALITY)
                assertTrue("$format min=$minimumBytes max=$maximumBytes", maximumBytes > minimumBytes)
                val targetBytes = minimumBytes + (maximumBytes - minimumBytes) / 2L
                val output = ByteArrayOutputStream()

                val writeResult = ImageBitmapIO.encodeOutput(
                    bitmap = bitmap,
                    options = options(bitmap, format, targetBytes),
                    output = output,
                    maxOutputBytes = ImageToolsPlugin.MAX_OUTPUT_BYTES,
                )
                val targetResult = requireNotNull(writeResult.targetFileSizeResult)
                val encoded = output.toByteArray()
                val bounds = BitmapFactory.Options().apply {
                    inJustDecodeBounds = true
                    BitmapFactory.decodeByteArray(encoded, 0, encoded.size, this)
                }

                assertEquals(format.mimeType, bounds.outMimeType)
                assertEquals(ImageTargetFileSizeStatus.WITHIN_QUALITY_RANGE, targetResult.status)
                assertTrue(targetResult.quality in 1..ImageConversionOptions.MAX_QUALITY)
                assertTrue("$format encoded=${encoded.size} target=$targetBytes", encoded.size <= targetBytes)
                assertEquals(encoded.size.toLong(), writeResult.encodedBytes)
                assertEquals(encoded.size.toLong(), targetResult.encodedBytes)
                assertTrue(targetResult.attemptCount <= 9)
            }
        } finally {
            bitmap.recycle()
        }
    }

    @Test
    fun realEncoderReportsBothUnreachableTargetBoundariesBeforeWriting() {
        val bitmap = texturedBitmap(256, 192)
        try {
            val minimumBytes = fixedSize(bitmap, ImageOutputFormat.JPEG, ImageConversionOptions.MIN_QUALITY)
            val maximumBytes = fixedSize(bitmap, ImageOutputFormat.JPEG, ImageConversionOptions.MAX_QUALITY)
            val belowMinimum = ImageBitmapIO.selectTargetFileSize(
                bitmap = bitmap,
                options = options(bitmap, ImageOutputFormat.JPEG, minimumBytes - 1L),
                maxOutputBytes = ImageToolsPlugin.MAX_OUTPUT_BYTES,
            )
            val aboveMaximum = ImageBitmapIO.selectTargetFileSize(
                bitmap = bitmap,
                options = options(bitmap, ImageOutputFormat.JPEG, maximumBytes + 1L),
                maxOutputBytes = ImageToolsPlugin.MAX_OUTPUT_BYTES,
            )

            assertEquals(ImageTargetFileSizeStatus.TARGET_BELOW_MINIMUM_QUALITY, belowMinimum.status)
            assertEquals(ImageConversionOptions.MIN_QUALITY, belowMinimum.quality)
            assertEquals(minimumBytes, belowMinimum.encodedBytes)
            assertTrue(belowMinimum.requiresConfirmation)
            assertEquals(ImageTargetFileSizeStatus.TARGET_ABOVE_MAXIMUM_QUALITY, aboveMaximum.status)
            assertEquals(ImageConversionOptions.MAX_QUALITY, aboveMaximum.quality)
            assertEquals(maximumBytes, aboveMaximum.encodedBytes)
            assertTrue(aboveMaximum.requiresConfirmation)
        } finally {
            bitmap.recycle()
        }
    }

    @Test
    fun candidatesAboveTheHostLimitAreStoppedWithoutBreakingConvergence() {
        val bitmap = texturedBitmap(320, 240)
        try {
            val targetBytes = fixedSize(bitmap, ImageOutputFormat.JPEG, quality = 50)
            val result = ImageBitmapIO.selectTargetFileSize(
                bitmap = bitmap,
                options = options(bitmap, ImageOutputFormat.JPEG, targetBytes),
                maxOutputBytes = targetBytes,
            )

            assertEquals(ImageTargetFileSizeStatus.WITHIN_QUALITY_RANGE, result.status)
            assertTrue(result.encodedBytes <= targetBytes)
            assertTrue(result.quality >= 50)
            assertTrue(result.attemptCount <= 9)
        } finally {
            bitmap.recycle()
        }
    }

    @Test
    fun targetFileSearchEncodesTheResolutionResolvedByLongEdgeMode() {
        val source = texturedBitmap(640, 360)
        val targetSize = requireNotNull(
            ImageConversionSizing.resolve(
                source = ImagePixelSize(source.width, source.height),
                request = ImageResizeRequest(ImageResizeMode.LONG_EDGE, longEdge = 320),
            ).size,
        )
        val scaled = Bitmap.createScaledBitmap(source, targetSize.width, targetSize.height, true)
        try {
            assertEquals(ImagePixelSize(320, 180), targetSize)
            val minimumBytes = fixedSize(scaled, ImageOutputFormat.JPEG, ImageConversionOptions.MIN_QUALITY)
            val maximumBytes = fixedSize(scaled, ImageOutputFormat.JPEG, ImageConversionOptions.MAX_QUALITY)
            val targetBytes = minimumBytes + (maximumBytes - minimumBytes) / 2L
            val output = ByteArrayOutputStream()

            val result = ImageBitmapIO.encodeOutput(
                bitmap = scaled,
                options = ImageConversionOptions(
                    format = ImageOutputFormat.JPEG,
                    targetSize = targetSize,
                    targetFileSizeBytes = targetBytes,
                ),
                output = output,
                maxOutputBytes = ImageToolsPlugin.MAX_OUTPUT_BYTES,
            )
            val bounds = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
                val encoded = output.toByteArray()
                BitmapFactory.decodeByteArray(encoded, 0, encoded.size, this)
            }

            assertEquals(targetSize.width, bounds.outWidth)
            assertEquals(targetSize.height, bounds.outHeight)
            assertTrue(result.encodedBytes <= targetBytes)
            assertEquals(ImageTargetFileSizeStatus.WITHIN_QUALITY_RANGE, result.targetFileSizeResult?.status)
        } finally {
            if (scaled !== source) scaled.recycle()
            source.recycle()
        }
    }

    private fun fixedSize(bitmap: Bitmap, format: ImageOutputFormat, quality: Int): Long {
        val output = ByteArrayOutputStream()
        return ImageBitmapIO.encodeOutput(
            bitmap = bitmap,
            options = ImageConversionOptions(
                format = format,
                quality = quality,
                targetSize = ImagePixelSize(bitmap.width, bitmap.height),
            ),
            output = output,
            maxOutputBytes = ImageToolsPlugin.MAX_OUTPUT_BYTES,
        ).encodedBytes
    }

    private fun options(
        bitmap: Bitmap,
        format: ImageOutputFormat,
        targetBytes: Long,
    ) = ImageConversionOptions(
        format = format,
        targetSize = ImagePixelSize(bitmap.width, bitmap.height),
        targetFileSizeBytes = targetBytes,
    )

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
