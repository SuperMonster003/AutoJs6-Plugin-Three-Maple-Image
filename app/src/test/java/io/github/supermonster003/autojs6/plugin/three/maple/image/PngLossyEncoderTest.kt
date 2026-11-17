package io.github.supermonster003.autojs6.plugin.three.maple.image

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.Random
import javax.imageio.ImageIO
import kotlin.math.abs
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

/** The lossy PNG writer, read back with the JVM's own PNG decoder. */
class PngLossyEncoderTest {

    private class Encoded(val layout: PngLossyEncoder.Layout, val bytes: ByteArray, val pixels: IntArray)

    @Test
    fun aPhotographStaysTruecolorAndEveryChannelStaysWithinTheLevel() {
        val width = 192
        val height = 128
        val source = noisyGradient(width, height, opaque = true)
        for (quality in listOf(99, 59, 9)) {
            val level = requireNotNull(PngLossyPolicy.level(quality))
            val encoded = encode(source, width, height, mayHaveAlpha = false, level)
            assertEquals(PngLossyEncoder.Layout.TRUECOLOR, encoded.layout)
            assertEquals(TRUECOLOR, colorType(encoded.bytes))
            assertWithin(source, encoded.pixels, level.maxTolerance)
        }
    }

    @Test
    fun lowerQualityWritesASmallerPhotograph() {
        val width = 192
        val height = 128
        val source = noisyGradient(width, height, opaque = true)
        val sizes = listOf(99, 79, 49, 9).map { quality ->
            encode(source, width, height, mayHaveAlpha = false, requireNotNull(PngLossyPolicy.level(quality))).bytes.size
        }
        assertEquals("sizes $sizes", sizes.sortedDescending(), sizes)
        assertTrue("sizes $sizes", sizes.last() < sizes.first() * 3 / 4)
    }

    @Test
    fun transparencyIsWrittenExactlyAndOnlyWhenTheImageHasAny() {
        val width = 48
        val height = 40
        val level = PngLossyLevel(maxTolerance = 6, smoothTolerance = 3)
        val translucent = noisyGradient(width, height, opaque = false)
        val withAlpha = encode(translucent, width, height, mayHaveAlpha = true, level)
        assertEquals(PngLossyEncoder.Layout.TRUECOLOR_ALPHA, withAlpha.layout)
        assertEquals(TRUECOLOR_ALPHA, colorType(withAlpha.bytes))
        translucent.indices.forEach { assertEquals("alpha of pixel $it", translucent[it] ushr 24, withAlpha.pixels[it] ushr 24) }
        assertWithin(translucent, withAlpha.pixels, level.maxTolerance)

        // A bitmap that could hold transparency but does not is written without an alpha channel.
        val opaque = noisyGradient(width, height, opaque = true)
        val withoutAlpha = encode(opaque, width, height, mayHaveAlpha = true, level)
        assertEquals(PngLossyEncoder.Layout.TRUECOLOR, withoutAlpha.layout)
        assertEquals(TRUECOLOR, colorType(withoutAlpha.bytes))
        assertWithin(opaque, withoutAlpha.pixels, level.maxTolerance)
    }

    @Test
    fun anImageOfFewColorsBecomesAPaletteThatKeepsItsCommonColorsExactly() {
        val width = 200
        val height = 60
        val background = 0xFFFAFAFA.toInt()
        val text = 0xFF757575.toInt()
        // A flat background, flat "text" and 400 shades between them along anti-aliased edges: more than 256 colors.
        val source = IntArray(width * height) { index ->
            val x = index % width
            val y = index / width
            when {
                y < 20 -> background
                y < 40 -> text
                else -> {
                    val shade = 0x75 + (x * (0xFA - 0x75)) / (width - 1)
                    0xFF000000.toInt() or (shade shl 16) or ((shade + (y % 3)) and 0xFF shl 8) or shade
                }
            }
        }
        assertTrue(source.toSet().size > 256)
        val level = PngLossyLevel(maxTolerance = 4, smoothTolerance = 3)
        val encoded = encode(source, width, height, mayHaveAlpha = true, level)
        assertEquals(PngLossyEncoder.Layout.INDEXED, encoded.layout)
        assertEquals(INDEXED, colorType(encoded.bytes))
        assertWithin(source, encoded.pixels, level.smoothTolerance)
        for (index in 0 until width * 40) assertEquals("pixel $index", source[index], encoded.pixels[index])
        // One source color always becomes the same output color, so flat areas cannot turn patchy.
        val mapping = HashMap<Int, Int>()
        source.indices.forEach { assertEquals(mapping.getOrPut(source[it]) { encoded.pixels[it] }, encoded.pixels[it]) }
    }

    @Test
    fun colorsFartherApartThanTheToleranceKeepTheirOwnPaletteEntries() {
        val width = 40
        val height = 30
        // 100 colors on a grid of 25 steps: no two of them are within the largest smooth tolerance of each other.
        val colors = IntArray(100) { 0xFF000000.toInt() or ((it % 10) * 25 shl 16) or ((it / 10) * 25 shl 8) or 0x80 }
        val source = IntArray(width * height) { colors[(it * 31) % colors.size] }
        val encoded = encode(source, width, height, mayHaveAlpha = false, PngLossyLevel(maxTolerance = 16, smoothTolerance = 8))
        assertEquals(PngLossyEncoder.Layout.INDEXED, encoded.layout)
        assertEquals(colors.size, paletteSize(encoded.bytes))
        assertArrayEquals(source, encoded.pixels)
    }

    @Test
    fun aPaletteImageOfCloseColorsNeedsFewerEntriesAtLowerQuality() {
        val width = 40
        val height = 30
        val colors = IntArray(100) { 0xFF000000.toInt() or (it * 2 shl 16) or (it shl 8) or (255 - it) }
        val source = IntArray(width * height) { colors[(it * 31) % colors.size] }
        var previous = colors.size + 1
        for (level in listOf(PngLossyLevel(1, 1), PngLossyLevel(4, 3), PngLossyLevel(16, 8))) {
            val encoded = encode(source, width, height, mayHaveAlpha = false, level)
            assertEquals(PngLossyEncoder.Layout.INDEXED, encoded.layout)
            assertWithin(source, encoded.pixels, level.smoothTolerance)
            val entries = paletteSize(encoded.bytes)
            assertTrue("$level: $entries entries after $previous", entries < previous)
            previous = entries
        }
    }

    @Test
    fun colorsThatDifferOnlyInAlphaNeverShareAPaletteEntry() {
        val width = 32
        val height = 8
        val source = IntArray(width * height) { index -> ((index % width) * 8 shl 24) or 0x336699 }
        val encoded = encode(source, width, height, mayHaveAlpha = true, PngLossyLevel(maxTolerance = 16, smoothTolerance = 8))
        assertEquals(PngLossyEncoder.Layout.INDEXED, encoded.layout)
        assertEquals(width, paletteSize(encoded.bytes))
        source.indices.forEach { assertEquals("alpha of pixel $it", source[it] ushr 24, encoded.pixels[it] ushr 24) }
    }

    @Test
    fun aFlatAreaInsideAPhotographKeepsItsExactColor() {
        val width = 192
        val height = 128
        val source = noisyGradient(width, height, opaque = true)
        val flat = 0xFF2E7D32.toInt()
        for (y in 20 until 40) for (x in 20 until 60) source[y * width + x] = flat
        val encoded = encode(source, width, height, mayHaveAlpha = false, PngLossyLevel(maxTolerance = 16, smoothTolerance = 8))
        assertEquals(PngLossyEncoder.Layout.TRUECOLOR, encoded.layout)
        // Pixels whose four neighbors are the same color are inside the flat area.
        for (y in 21 until 39) for (x in 21 until 59) assertEquals("pixel $x,$y", flat, encoded.pixels[y * width + x])
    }

    @Test
    fun singleRowsColumnsAndPixelsAreEncoded() {
        val level = PngLossyLevel(maxTolerance = 4, smoothTolerance = 2)
        for ((width, height) in listOf(1 to 1, 300 to 1, 1 to 300)) {
            val random = Random(width * 31L + height)
            val source = IntArray(width * height) { 0xFF000000.toInt() or random.nextInt(0x1000000) }
            val encoded = encode(source, width, height, mayHaveAlpha = false, level)
            assertWithin(source, encoded.pixels, level.maxTolerance)
        }
        assertThrows(IllegalArgumentException::class.java) {
            PngLossyEncoder.encode(0, 1, false, level, ByteArrayOutputStream()) { _, _ -> }
        }
    }

    @Test
    fun rowsAreReadFromTopToBottomInEachPass() {
        val width = 16
        val height = 12
        val source = noisyGradient(width, height, opaque = true)
        val asked = ArrayList<Int>()
        PngLossyEncoder.encode(width, height, false, PngLossyLevel(2, 1), ByteArrayOutputStream()) { y, into ->
            asked += y
            System.arraycopy(source, y * width, into, 0, width)
        }
        // Two passes: one that learns the layout (it may stop early) and one that writes every row.
        val writing = asked.subList(asked.lastIndexOf(0), asked.size)
        assertEquals((0 until height).toList(), writing)
        assertEquals(asked.subList(0, asked.lastIndexOf(0)).sorted(), asked.subList(0, asked.lastIndexOf(0)))
    }

    private fun encode(source: IntArray, width: Int, height: Int, mayHaveAlpha: Boolean, level: PngLossyLevel): Encoded {
        val output = ByteArrayOutputStream()
        val layout = PngLossyEncoder.encode(width, height, mayHaveAlpha, level, output) { y, into ->
            System.arraycopy(source, y * width, into, 0, width)
        }
        val bytes = output.toByteArray()
        val image = requireNotNull(ImageIO.read(ByteArrayInputStream(bytes))) { "The PNG does not decode" }
        assertEquals(width, image.width)
        assertEquals(height, image.height)
        val pixels = IntArray(width * height)
        image.getRGB(0, 0, width, height, pixels, 0, width)
        return Encoded(layout, bytes, pixels)
    }

    private fun assertWithin(source: IntArray, result: IntArray, tolerance: Int) {
        for (index in source.indices) {
            for (shift in intArrayOf(16, 8, 0)) {
                val before = (source[index] ushr shift) and 0xFF
                val after = (result[index] ushr shift) and 0xFF
                assertTrue("pixel $index: $before -> $after allows $tolerance", abs(before - after) <= tolerance)
            }
        }
    }

    /** A smooth color ramp with sensor-like noise; at 192 x 128 it has more colors than a palette is tried for. */
    private fun noisyGradient(width: Int, height: Int, opaque: Boolean): IntArray {
        val random = Random(width * 1_000L + height)
        return IntArray(width * height) { index ->
            val x = index % width
            val y = index / width
            val red = (x * 255 / width + random.nextInt(9) - 4).coerceIn(0, 255)
            val green = (y * 255 / height + random.nextInt(9) - 4).coerceIn(0, 255)
            val blue = ((x + y) * 255 / (width + height) + random.nextInt(9) - 4).coerceIn(0, 255)
            val alpha = if (opaque) 255 else if ((x + y) % 5 == 0) 64 + (x * 3) % 128 else 255
            (alpha shl 24) or (red shl 16) or (green shl 8) or blue
        }
    }

    /** The color type byte of the IHDR chunk. */
    private fun colorType(png: ByteArray): Int = png[25].toInt() and 0xFF

    /** The number of entries of the PLTE chunk, which follows the IHDR chunk in an indexed PNG. */
    private fun paletteSize(png: ByteArray): Int {
        val chunk = FIRST_CHUNK_AFTER_HEADER
        assertEquals("PLTE", String(png, chunk + 4, 4, Charsets.US_ASCII))
        val length = (0 until 4).fold(0) { value, index -> (value shl 8) or (png[chunk + index].toInt() and 0xFF) }
        return length / 3
    }

    private companion object {
        const val TRUECOLOR = 2
        const val INDEXED = 3
        const val TRUECOLOR_ALPHA = 6

        /** The signature (8 bytes) and the IHDR chunk (length, type, 13 bytes of data, CRC). */
        const val FIRST_CHUNK_AFTER_HEADER = 8 + 4 + 4 + 13 + 4
    }
}
