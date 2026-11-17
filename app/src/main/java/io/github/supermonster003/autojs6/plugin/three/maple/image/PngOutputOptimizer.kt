package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.graphics.Bitmap
import java.io.BufferedInputStream
import java.io.DataInputStream
import java.io.EOFException
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.LinkedHashMap
import java.util.zip.CRC32
import java.util.zip.Deflater
import java.util.zip.DeflaterOutputStream

internal object PngPalettePolicy {

    const val MAX_COLOR_COUNT = 256

    fun bitDepthForColorCount(colorCount: Int): Int {
        require(colorCount in 1..MAX_COLOR_COUNT) { "PNG palette must contain between 1 and 256 colors" }
        return when {
            colorCount <= 2 -> 1
            colorCount <= 4 -> 2
            colorCount <= 16 -> 4
            else -> 8
        }
    }
}

/**
 * Writes an indexed PNG when every output pixel can be represented by a PNG palette.
 *
 * The feasibility pass retains at most 256 colors and one bitmap row, while the encoder
 * streams bounded IDAT chunks. Large output bitmaps therefore do not require a second
 * full-frame pixel buffer or a full encoded-image byte array.
 */
internal object PngOutputOptimizer {

    internal data class PalettePlan(
        val colors: IntArray,
        val colorIndices: Map<Int, Int>,
        val bitDepth: Int,
    )

    fun plan(bitmap: Bitmap): PalettePlan? {
        val colorIndices = LinkedHashMap<Int, Int>(PngPalettePolicy.MAX_COLOR_COUNT)
        val row = IntArray(bitmap.width)
        for (y in 0 until bitmap.height) {
            bitmap.getPixels(row, 0, bitmap.width, 0, y, bitmap.width, 1)
            for (color in row) {
                if (!colorIndices.containsKey(color)) {
                    if (colorIndices.size == PngPalettePolicy.MAX_COLOR_COUNT) return null
                    colorIndices[color] = colorIndices.size
                }
            }
        }
        val colors = IntArray(colorIndices.size)
        colorIndices.forEach { (color, index) -> colors[index] = color }
        return PalettePlan(
            colors = colors,
            colorIndices = colorIndices,
            bitDepth = PngPalettePolicy.bitDepthForColorCount(colors.size),
        )
    }

    @Throws(IOException::class)
    fun encode(bitmap: Bitmap, plan: PalettePlan, output: OutputStream) {
        val width = bitmap.width
        encode(width, bitmap.height, plan, output) { y, row -> bitmap.getPixels(row, 0, width, 0, y, width, 1) }
    }

    /** [encode] for pixels that [readRow] supplies one row at a time, from top to bottom. */
    @Throws(IOException::class)
    fun encode(width: Int, height: Int, plan: PalettePlan, output: OutputStream, readRow: (y: Int, row: IntArray) -> Unit) {
        require(plan.colors.isNotEmpty() && plan.colors.size <= PngPalettePolicy.MAX_COLOR_COUNT)
        require(plan.bitDepth == PngPalettePolicy.bitDepthForColorCount(plan.colors.size))

        output.write(PNG_SIGNATURE)
        writeChunk(output, CHUNK_IHDR, createHeader(width, height, plan.bitDepth, INDEXED_COLOR_TYPE))
        writeChunk(output, CHUNK_PLTE, createPalette(plan.colors))
        createTransparency(plan.colors)?.let { writeChunk(output, CHUNK_TRNS, it) }
        writeImageData(width, height, plan, output, readRow)
        writeChunk(output, CHUNK_IEND, EMPTY_BYTES)
    }

    private fun createPalette(colors: IntArray): ByteArray {
        val palette = ByteArray(colors.size * RGB_CHANNEL_COUNT)
        colors.forEachIndexed { index, color ->
            val offset = index * RGB_CHANNEL_COUNT
            palette[offset] = (color ushr 16).toByte()
            palette[offset + 1] = (color ushr 8).toByte()
            palette[offset + 2] = color.toByte()
        }
        return palette
    }

    private fun createTransparency(colors: IntArray): ByteArray? {
        val lastTransparentIndex = colors.indexOfLast { color -> color ushr 24 != OPAQUE_ALPHA }
        if (lastTransparentIndex < 0) return null
        return ByteArray(lastTransparentIndex + 1) { index -> (colors[index] ushr 24).toByte() }
    }

    private fun writeImageData(
        width: Int,
        height: Int,
        plan: PalettePlan,
        output: OutputStream,
        readRow: (y: Int, row: IntArray) -> Unit,
    ) {
        val pixelsPerByte = BITS_PER_BYTE / plan.bitDepth
        val packedRowBytes = (width.toLong() * plan.bitDepth + BITS_PER_BYTE - 1L) /
            BITS_PER_BYTE
        require(packedRowBytes <= Int.MAX_VALUE - 1L)
        val scanline = ByteArray(packedRowBytes.toInt() + 1)
        val row = IntArray(width)
        val idat = ChunkedIdatOutputStream(output)
        val deflater = Deflater(Deflater.BEST_COMPRESSION)
        try {
            val compressed = DeflaterOutputStream(idat, deflater, DEFLATE_BUFFER_SIZE)
            for (y in 0 until height) {
                scanline.fill(0)
                readRow(y, row)
                for (x in row.indices) {
                    val paletteIndex = plan.colorIndices[row[x]]
                        ?: throw IOException("Bitmap pixels changed during PNG encoding")
                    val byteOffset = 1 + x / pixelsPerByte
                    val shift = BITS_PER_BYTE - plan.bitDepth - (x % pixelsPerByte) * plan.bitDepth
                    scanline[byteOffset] = (
                        scanline[byteOffset].toInt() or (paletteIndex shl shift)
                        ).toByte()
                }
                compressed.write(scanline)
            }
            compressed.finish()
            idat.finish()
        } finally {
            deflater.end()
        }
    }
}

/**
 * Writes lossy PNG output without forcing a palette on the image:
 * - an image of few colors (a user interface screenshot, a drawing) becomes an indexed PNG once colors that
 *   differ by no more than the level's smooth tolerance share one palette entry; every source color always maps
 *   to the same entry, so flat areas stay flat and the most common colors stay exact;
 * - any other image stays truecolor and is written through a [PngNearLosslessFilter].
 *
 * Like [PngOutputOptimizer], it reads the image one row at a time (three source rows are in memory at once) and
 * streams bounded IDAT chunks; the image is read twice, once to learn its layout and once to write it.
 */
internal object PngLossyEncoder {

    fun interface RowReader {
        /** Fills [into] with the ARGB pixels of row [y]; rows are asked for from top to bottom. */
        fun read(y: Int, into: IntArray)
    }

    enum class Layout { INDEXED, TRUECOLOR, TRUECOLOR_ALPHA }

    @Throws(IOException::class)
    fun encode(bitmap: Bitmap, level: PngLossyLevel, output: OutputStream): Layout {
        val width = bitmap.width
        return encode(width, bitmap.height, bitmap.hasAlpha(), level, output) { y, into ->
            bitmap.getPixels(into, 0, width, 0, y, width, 1)
        }
    }

    /** [mayHaveAlpha] false promises that every pixel is opaque. */
    @Throws(IOException::class)
    fun encode(
        width: Int,
        height: Int,
        mayHaveAlpha: Boolean,
        level: PngLossyLevel,
        output: OutputStream,
        rows: RowReader,
    ): Layout {
        require(width > 0 && height > 0) { "Image size must be positive" }
        val analysis = analyze(width, height, mayHaveAlpha, level, rows)
        val plan = analysis.plan
        if (plan != null) {
            PngOutputOptimizer.encode(width, height, plan, output) { y, into -> rows.read(y, into) }
            return Layout.INDEXED
        }
        writeTruecolor(width, height, analysis.opaque, level, output, rows)
        return if (analysis.opaque) Layout.TRUECOLOR else Layout.TRUECOLOR_ALPHA
    }

    private class Analysis(val plan: PngOutputOptimizer.PalettePlan?, val opaque: Boolean)

    /** One pass that stops as soon as it knows the image has too many colors for a palette and whether it is opaque. */
    private fun analyze(width: Int, height: Int, mayHaveAlpha: Boolean, level: PngLossyLevel, rows: RowReader): Analysis {
        val row = IntArray(width)
        var counter: ColorCounter? = ColorCounter(MAX_PALETTE_CANDIDATE_COLORS)
        var opaque = true
        for (y in 0 until height) {
            rows.read(y, row)
            if (opaque && mayHaveAlpha) opaque = row.all { it ushr 24 == OPAQUE_ALPHA }
            val counting = counter
            if (counting != null) {
                for (pixel in row) {
                    if (!counting.add(pixel)) {
                        counter = null
                        break
                    }
                }
            }
            if (counter == null && (!mayHaveAlpha || !opaque)) break
        }
        return Analysis(plan = counter?.let { palette(it, level.smoothTolerance) }, opaque = opaque)
    }

    /**
     * A palette for the counted colors in which every color is within [tolerance] of its entry on each channel
     * (alpha must match exactly), or null when that takes more than 256 entries. Colors claim entries from the
     * most to the least used, so the colors that fill large areas are kept exactly.
     */
    private fun palette(counter: ColorCounter, tolerance: Int): PngOutputOptimizer.PalettePlan? {
        val colors = counter.colorsByUse()
        val entries = IntArray(PngPalettePolicy.MAX_COLOR_COUNT)
        var entryCount = 0
        val indices = LinkedHashMap<Int, Int>(colors.size * 2)
        for (color in colors) {
            var nearest = -1
            var nearestDistance = Int.MAX_VALUE
            for (index in 0 until entryCount) {
                val entry = entries[index]
                if (entry ushr 24 != color ushr 24) continue
                val distance = PngNearLosslessFilter.difference(entry, color)
                if (distance <= tolerance && distance < nearestDistance) {
                    nearest = index
                    nearestDistance = distance
                    if (distance == 0) break
                }
            }
            if (nearest < 0) {
                if (entryCount == entries.size) return null
                entries[entryCount] = color
                nearest = entryCount++
            }
            indices[color] = nearest
        }
        return PngOutputOptimizer.PalettePlan(
            colors = entries.copyOf(entryCount),
            colorIndices = indices,
            bitDepth = PngPalettePolicy.bitDepthForColorCount(entryCount),
        )
    }

    private fun writeTruecolor(width: Int, height: Int, opaque: Boolean, level: PngLossyLevel, output: OutputStream, rows: RowReader) {
        val channels = if (opaque) OPAQUE_CHANNELS else ALPHA_CHANNELS
        val rowBytes = width.toLong() * channels
        require(rowBytes <= Int.MAX_VALUE - 1L)
        output.write(PNG_SIGNATURE)
        writeChunk(
            output,
            CHUNK_IHDR,
            createHeader(width, height, TRUECOLOR_BIT_DEPTH, if (opaque) TRUECOLOR_COLOR_TYPE else TRUECOLOR_ALPHA_COLOR_TYPE),
        )
        val scanline = ByteArray(rowBytes.toInt() + 1)
        val window = SourceWindow(width, height, rows)
        val filter = PngNearLosslessFilter(width, level)
        val idat = ChunkedIdatOutputStream(output)
        // Filtered rows are small differences with few repeats: the strategy made for them is about 1% smaller.
        val deflater = Deflater(TRUECOLOR_DEFLATE_LEVEL).apply { setStrategy(Deflater.FILTERED) }
        try {
            val compressed = DeflaterOutputStream(idat, deflater, DEFLATE_BUFFER_SIZE)
            for (y in 0 until height) {
                window.advance()
                val source = window.current
                filter.process(window.above, source, window.below)
                filterScanline(scanline, filter, source, window.above, width, opaque)
                compressed.write(scanline)
            }
            compressed.finish()
            idat.finish()
        } finally {
            deflater.end()
        }
        writeChunk(output, CHUNK_IEND, EMPTY_BYTES)
    }

    /** The PNG scanline of the row the filter just reconstructed, with the filter type the filter chose for it. */
    private fun filterScanline(
        scanline: ByteArray,
        filter: PngNearLosslessFilter,
        source: IntArray,
        sourceAbove: IntArray?,
        width: Int,
        opaque: Boolean,
    ) {
        val type = filter.filterType
        val current = filter.currentRow
        val upper = filter.upperRow
        scanline[0] = type.toByte()
        var out = 1
        var i = 0
        for (x in 0 until width) {
            val first = x == 0
            repeat(PngNearLosslessFilter.CHANNELS) {
                val left = if (first) 0 else current[i - PngNearLosslessFilter.CHANNELS]
                val upLeft = if (first) 0 else upper[i - PngNearLosslessFilter.CHANNELS]
                scanline[out++] = (current[i] - predict(type, left, upper[i], upLeft)).toByte()
                i++
            }
            if (!opaque) {
                val left = if (first) 0 else source[x - 1] ushr 24
                val up = if (sourceAbove == null) 0 else sourceAbove[x] ushr 24
                val upLeft = if (first || sourceAbove == null) 0 else sourceAbove[x - 1] ushr 24
                scanline[out++] = ((source[x] ushr 24) - predict(type, left, up, upLeft)).toByte()
            }
        }
    }

    private fun predict(type: Int, left: Int, up: Int, upLeft: Int): Int = when (type) {
        PngNearLosslessFilter.FILTER_SUB -> left
        PngNearLosslessFilter.FILTER_UP -> up
        PngNearLosslessFilter.FILTER_AVERAGE -> (left + up) ushr 1
        else -> PngNearLosslessFilter.paeth(left, up, upLeft)
    }

    /** The source rows above, at and below the row being written; rows are read once each, in order. */
    private class SourceWindow(width: Int, private val height: Int, private val rows: RowReader) {
        private var upper = IntArray(width)
        private var middle = IntArray(width)
        private var lower = IntArray(width)
        private var position = -1

        val current: IntArray get() = middle
        val above: IntArray? get() = if (position > 0) upper else null
        val below: IntArray? get() = if (position < height - 1) lower else null

        fun advance() {
            position++
            if (position == 0) {
                rows.read(0, middle)
            } else {
                val recycled = upper
                upper = middle
                middle = lower
                lower = recycled
            }
            if (position + 1 < height) rows.read(position + 1, lower)
        }
    }

    /** Counts how often each color occurs, for at most [capacity] different colors, without boxing a pixel. */
    private class ColorCounter(private val capacity: Int) {
        private val mask = Integer.highestOneBit(capacity) * 4 - 1
        private val keys = IntArray(mask + 1)
        private val counts = IntArray(mask + 1)
        private var size = 0

        /** False once the color would be one more than the counter holds; the counter is useless from then on. */
        fun add(color: Int): Boolean {
            var slot = (color * HASH_MULTIPLIER ushr 12) and mask
            while (true) {
                val count = counts[slot]
                if (count == 0) {
                    if (size == capacity) return false
                    keys[slot] = color
                    counts[slot] = 1
                    size++
                    return true
                }
                if (keys[slot] == color) {
                    if (count < Int.MAX_VALUE) counts[slot] = count + 1
                    return true
                }
                slot = (slot + 1) and mask
            }
        }

        /** The counted colors, the most used first; equally used colors in the order of their values. */
        fun colorsByUse(): IntArray {
            val packed = LongArray(size)
            var next = 0
            for (slot in keys.indices) {
                val count = counts[slot]
                if (count != 0) packed[next++] = ((Int.MAX_VALUE - count).toLong() shl 32) or (keys[slot].toLong() and 0xFFFF_FFFFL)
            }
            packed.sort()
            return IntArray(size) { packed[it].toInt() }
        }

        private companion object {
            const val HASH_MULTIPLIER = -0x61c88647
        }
    }

    /** More colors than this never fit a palette within a tolerance worth offering; photographs pass it in a few rows. */
    private const val MAX_PALETTE_CANDIDATE_COLORS = 16_384
    private const val OPAQUE_CHANNELS = 3
    private const val ALPHA_CHANNELS = 4

    /**
     * The level libpng uses as well, so lossy output takes about as long as lossless output. On photographs and
     * screenshots level 4 is three to five times quicker in this step but 5% to 8% larger, and level 3 9% to 50% larger.
     */
    private const val TRUECOLOR_DEFLATE_LEVEL = 6
}

private class ChunkedIdatOutputStream(private val output: OutputStream) : OutputStream() {
    private val buffer = ByteArray(IDAT_CHUNK_DATA_SIZE)
    private var size = 0

    override fun write(value: Int) {
        if (size == buffer.size) emitChunk()
        buffer[size++] = value.toByte()
    }

    override fun write(source: ByteArray, offset: Int, length: Int) {
        require(offset >= 0 && length >= 0 && offset <= source.size - length)
        var sourceOffset = offset
        var remaining = length
        while (remaining > 0) {
            if (size == buffer.size) emitChunk()
            val copied = minOf(remaining, buffer.size - size)
            source.copyInto(buffer, size, sourceOffset, sourceOffset + copied)
            size += copied
            sourceOffset += copied
            remaining -= copied
        }
    }

    override fun flush() {
        emitChunk()
        output.flush()
    }

    fun finish() = emitChunk()

    private fun emitChunk() {
        if (size == 0) return
        writeChunk(output, CHUNK_IDAT, buffer, size)
        size = 0
    }
}

/** Reads only enough of a PNG stream to identify an existing indexed-color palette. */
internal object PngSourceMetadata {

    fun paletteColorCount(input: InputStream): Int? = try {
        readPaletteColorCount(DataInputStream(BufferedInputStream(input)))
    } catch (_: IOException) {
        null
    } catch (_: RuntimeException) {
        null
    }

    private fun readPaletteColorCount(input: DataInputStream): Int? {
        val signature = ByteArray(PNG_SIGNATURE.size)
        input.readFully(signature)
        if (!signature.contentEquals(PNG_SIGNATURE)) return null

        var scannedBytes = PNG_SIGNATURE.size.toLong()
        var sawHeader = false
        while (scannedBytes < MAX_METADATA_SCAN_BYTES) {
            val chunkLength = input.readInt().toLong() and UINT_MASK
            val chunkType = ByteArray(CHUNK_TYPE_SIZE).also(input::readFully)
            val totalChunkBytes = chunkLength + CHUNK_OVERHEAD_BYTES
            if (
                chunkLength > MAX_METADATA_SCAN_BYTES ||
                totalChunkBytes > MAX_METADATA_SCAN_BYTES - scannedBytes
            ) {
                return null
            }
            scannedBytes += totalChunkBytes

            if (!sawHeader) {
                if (!chunkType.contentEquals(CHUNK_IHDR) || chunkLength != IHDR_DATA_SIZE.toLong()) return null
                val header = ByteArray(IHDR_DATA_SIZE).also(input::readFully)
                skipFully(input, CRC_SIZE.toLong())
                if (header[9].toInt() and 0xFF != INDEXED_COLOR_TYPE) return null
                if (header[8].toInt() and 0xFF !in INDEXED_BIT_DEPTHS) return null
                sawHeader = true
                continue
            }

            when {
                chunkType.contentEquals(CHUNK_PLTE) -> {
                    if (
                        chunkLength !in RGB_CHANNEL_COUNT.toLong()..
                        (PngPalettePolicy.MAX_COLOR_COUNT * RGB_CHANNEL_COUNT).toLong() ||
                        chunkLength % RGB_CHANNEL_COUNT != 0L
                    ) {
                        return null
                    }
                    skipFully(input, chunkLength + CRC_SIZE)
                    return (chunkLength / RGB_CHANNEL_COUNT).toInt()
                }
                chunkType.contentEquals(CHUNK_IDAT) || chunkType.contentEquals(CHUNK_IEND) -> return null
                else -> skipFully(input, chunkLength + CRC_SIZE)
            }
        }
        return null
    }

    private fun skipFully(input: DataInputStream, byteCount: Long) {
        var remaining = byteCount
        while (remaining > 0L) {
            val skipped = input.skip(remaining)
            if (skipped > 0L) {
                remaining -= skipped
            } else {
                if (input.read() < 0) throw EOFException("Unexpected end of PNG metadata")
                remaining--
            }
        }
    }
}

private fun createHeader(width: Int, height: Int, bitDepth: Int, colorType: Int) = ByteArray(IHDR_DATA_SIZE).also {
    writeInt(it, 0, width)
    writeInt(it, 4, height)
    it[8] = bitDepth.toByte()
    it[9] = colorType.toByte()
    it[10] = 0
    it[11] = 0
    it[12] = 0
}

private fun writeChunk(
    output: OutputStream,
    type: ByteArray,
    data: ByteArray,
    length: Int = data.size,
) {
    require(type.size == CHUNK_TYPE_SIZE)
    require(length in 0..data.size)
    writeInt(output, length)
    output.write(type)
    output.write(data, 0, length)
    val checksum = CRC32().apply {
        update(type, 0, type.size)
        update(data, 0, length)
    }
    writeInt(output, checksum.value.toInt())
}

private fun writeInt(output: OutputStream, value: Int) {
    output.write(value ushr 24)
    output.write(value ushr 16)
    output.write(value ushr 8)
    output.write(value)
}

private fun writeInt(destination: ByteArray, offset: Int, value: Int) {
    destination[offset] = (value ushr 24).toByte()
    destination[offset + 1] = (value ushr 16).toByte()
    destination[offset + 2] = (value ushr 8).toByte()
    destination[offset + 3] = value.toByte()
}

private val PNG_SIGNATURE = byteArrayOf(
    0x89.toByte(),
    0x50,
    0x4E,
    0x47,
    0x0D,
    0x0A,
    0x1A,
    0x0A,
)
private val CHUNK_IHDR = byteArrayOf(0x49, 0x48, 0x44, 0x52)
private val CHUNK_PLTE = byteArrayOf(0x50, 0x4C, 0x54, 0x45)
private val CHUNK_TRNS = byteArrayOf(0x74, 0x52, 0x4E, 0x53)
private val CHUNK_IDAT = byteArrayOf(0x49, 0x44, 0x41, 0x54)
private val CHUNK_IEND = byteArrayOf(0x49, 0x45, 0x4E, 0x44)
private val EMPTY_BYTES = ByteArray(0)
private val INDEXED_BIT_DEPTHS = setOf(1, 2, 4, 8)

private const val BITS_PER_BYTE = 8
private const val RGB_CHANNEL_COUNT = 3
private const val OPAQUE_ALPHA = 0xFF
private const val INDEXED_COLOR_TYPE = 3
private const val TRUECOLOR_COLOR_TYPE = 2
private const val TRUECOLOR_ALPHA_COLOR_TYPE = 6
private const val TRUECOLOR_BIT_DEPTH = 8
private const val IHDR_DATA_SIZE = 13
private const val CHUNK_TYPE_SIZE = 4
private const val CRC_SIZE = 4
private const val CHUNK_OVERHEAD_BYTES = 12L
private const val UINT_MASK = 0xFFFF_FFFFL
private const val DEFLATE_BUFFER_SIZE = 8 * 1024
private const val IDAT_CHUNK_DATA_SIZE = 64 * 1024
private const val MAX_METADATA_SCAN_BYTES = 1024L * 1024L
