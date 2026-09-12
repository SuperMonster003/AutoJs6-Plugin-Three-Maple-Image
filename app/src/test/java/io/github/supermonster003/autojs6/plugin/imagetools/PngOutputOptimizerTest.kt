package io.github.supermonster003.autojs6.plugin.imagetools

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
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
class PngOutputOptimizerTest {

    @Test
    fun indexedPngUsesTheSmallestLegalBitDepthAndRoundTripsEveryPixel() {
        val cases = listOf(
            1 to 1,
            2 to 1,
            3 to 2,
            4 to 2,
            5 to 4,
            16 to 4,
            17 to 8,
            256 to 8,
        )

        cases.forEach { (colorCount, expectedBitDepth) ->
            val bitmap = paletteBitmap(colorCount)
            try {
                val encoded = encode(bitmap)
                assertEquals("color count $colorCount", expectedBitDepth, encoded.pngBitDepth())
                assertEquals("color count $colorCount", PNG_INDEXED_COLOR_TYPE, encoded.pngColorType())
                assertEquals(
                    "color count $colorCount",
                    colorCount,
                    PngSourceMetadata.paletteColorCount(ByteArrayInputStream(encoded)),
                )
                assertPngRoundTrip(bitmap, encoded)
            } finally {
                bitmap.recycle()
            }
        }
    }

    @Test
    fun indexedPngPreservesPaletteTransparencyWithoutOpaquePadding() {
        val requestedPixels = intArrayOf(
            Color.TRANSPARENT,
            Color.argb(128, 64, 128, 192),
            Color.rgb(12, 34, 56),
            Color.TRANSPARENT,
            Color.argb(128, 64, 128, 192),
        )
        val bitmap = Bitmap.createBitmap(requestedPixels.size, 1, Bitmap.Config.ARGB_8888).apply {
            setPixels(requestedPixels, 0, width, 0, 0, width, height)
        }
        try {
            val encoded = encode(bitmap)
            val transparency = requireNotNull(encoded.pngChunks()["tRNS"])

            assertArrayEquals(byteArrayOf(0, 128.toByte()), transparency)
            assertPngRoundTrip(bitmap, encoded)
        } finally {
            bitmap.recycle()
        }
    }

    @Test
    fun moreThanTwoHundredAndFiftySixColorsFallsBackToThePlatformPngEncoder() {
        val bitmap = paletteBitmap(257)
        try {
            assertNull(PngOutputOptimizer.plan(bitmap))
            val encoded = encode(bitmap)

            assertFalse(encoded.pngColorType() == PNG_INDEXED_COLOR_TYPE)
            assertNull(PngSourceMetadata.paletteColorCount(ByteArrayInputStream(encoded)))
            assertPngRoundTrip(bitmap, encoded)
        } finally {
            bitmap.recycle()
        }
    }

    @Test
    fun indexedEncodingMateriallyReducesAHighEntropyLimitedPaletteImage() {
        val bitmap = highEntropyPaletteBitmap(width = 512, height = 512, colorCount = 16)
        try {
            val optimized = encode(bitmap)
            val platformOutput = ByteArrayOutputStream()
            assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG, 100, platformOutput))
            val platform = platformOutput.toByteArray()

            assertFalse(platform.pngColorType() == PNG_INDEXED_COLOR_TYPE)
            assertTrue(
                "optimized=${optimized.size}, platform=${platform.size}",
                optimized.size * 2 < platform.size,
            )
            assertPngRoundTrip(bitmap, optimized)
        } finally {
            bitmap.recycle()
        }
    }

    @Test
    fun indexedEncodingStillHonorsTheSharedOutputLimit() {
        val bitmap = paletteBitmap(2)
        try {
            assertThrows(ImageBitmapIO.OutputLimitExceededException::class.java) {
                encode(bitmap, maxOutputBytes = 32L)
            }
            assertFalse(bitmap.isRecycled)
        } finally {
            bitmap.recycle()
        }
    }

    @Test
    fun malformedOrNonPngMetadataDoesNotProduceAPaletteHint() {
        assertNull(PngSourceMetadata.paletteColorCount(ByteArrayInputStream(byteArrayOf(1, 2, 3))))
        assertNull(PngSourceMetadata.paletteColorCount(ByteArrayInputStream(ByteArray(128))))
    }

    private fun encode(
        bitmap: Bitmap,
        maxOutputBytes: Long = ImageToolsPlugin.MAX_OUTPUT_BYTES,
    ): ByteArray {
        val output = ByteArrayOutputStream()
        ImageBitmapIO.encodeOutput(
            bitmap = bitmap,
            options = ImageConversionOptions(
                format = ImageOutputFormat.PNG,
                quality = 100,
                targetSize = ImagePixelSize(bitmap.width, bitmap.height),
            ),
            output = output,
            maxOutputBytes = maxOutputBytes,
        )
        return output.toByteArray()
    }

    private fun assertPngRoundTrip(source: Bitmap, encoded: ByteArray) {
        val decoded = requireNotNull(BitmapFactory.decodeByteArray(encoded, 0, encoded.size))
        try {
            assertEquals(source.width, decoded.width)
            assertEquals(source.height, decoded.height)
            assertArrayEquals(source.pixels(), decoded.pixels())
        } finally {
            decoded.recycle()
        }
    }

    private fun paletteBitmap(colorCount: Int): Bitmap {
        val colors = IntArray(colorCount) { index ->
            Color.rgb(index and 0xFF, index ushr 8 and 0xFF, index * 73 and 0xFF)
        }
        return Bitmap.createBitmap(colorCount, 3, Bitmap.Config.ARGB_8888).apply {
            val pixels = IntArray(width * height) { index -> colors[index % colors.size] }
            setPixels(pixels, 0, width, 0, 0, width, height)
            setHasAlpha(false)
        }
    }

    private fun highEntropyPaletteBitmap(width: Int, height: Int, colorCount: Int): Bitmap {
        val colors = IntArray(colorCount) { index ->
            Color.rgb(index * 47 and 0xFF, index * 83 + 19 and 0xFF, index * 131 + 7 and 0xFF)
        }
        var randomState = 0x13579BDF
        val pixels = IntArray(width * height) {
            randomState = randomState * 1_103_515_245 + 12_345
            colors[randomState ushr 16 and (colorCount - 1)]
        }
        return Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).apply {
            setPixels(pixels, 0, width, 0, 0, width, height)
            setHasAlpha(false)
        }
    }

    private fun Bitmap.pixels() = IntArray(width * height).also { pixels ->
        getPixels(pixels, 0, width, 0, 0, width, height)
    }

    private fun ByteArray.pngBitDepth(): Int {
        require(size > PNG_COLOR_TYPE_OFFSET)
        return this[PNG_BIT_DEPTH_OFFSET].toInt() and 0xFF
    }

    private fun ByteArray.pngColorType(): Int {
        require(size > PNG_COLOR_TYPE_OFFSET)
        return this[PNG_COLOR_TYPE_OFFSET].toInt() and 0xFF
    }

    private fun ByteArray.pngChunks(): Map<String, ByteArray> {
        val chunks = linkedMapOf<String, ByteArray>()
        var offset = PNG_SIGNATURE_SIZE
        while (offset <= size - PNG_CHUNK_OVERHEAD) {
            val length = readPngInt(offset)
            require(length >= 0 && offset <= size - PNG_CHUNK_OVERHEAD - length)
            val type = String(this, offset + 4, 4, Charsets.US_ASCII)
            chunks[type] = copyOfRange(offset + 8, offset + 8 + length)
            offset += PNG_CHUNK_OVERHEAD + length
            if (type == "IEND") break
        }
        return chunks
    }

    private fun ByteArray.readPngInt(offset: Int): Int =
        ((this[offset].toInt() and 0xFF) shl 24) or
            ((this[offset + 1].toInt() and 0xFF) shl 16) or
            ((this[offset + 2].toInt() and 0xFF) shl 8) or
            (this[offset + 3].toInt() and 0xFF)

    companion object {
        private const val PNG_INDEXED_COLOR_TYPE = 3
        private const val PNG_SIGNATURE_SIZE = 8
        private const val PNG_CHUNK_OVERHEAD = 12
        private const val PNG_BIT_DEPTH_OFFSET = 24
        private const val PNG_COLOR_TYPE_OFFSET = 25
    }
}
