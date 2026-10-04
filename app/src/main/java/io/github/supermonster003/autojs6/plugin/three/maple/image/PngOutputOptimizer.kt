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
        require(plan.colors.isNotEmpty() && plan.colors.size <= PngPalettePolicy.MAX_COLOR_COUNT)
        require(plan.bitDepth == PngPalettePolicy.bitDepthForColorCount(plan.colors.size))

        output.write(PNG_SIGNATURE)
        writeChunk(output, CHUNK_IHDR, createHeader(bitmap.width, bitmap.height, plan.bitDepth))
        writeChunk(output, CHUNK_PLTE, createPalette(plan.colors))
        createTransparency(plan.colors)?.let { writeChunk(output, CHUNK_TRNS, it) }
        writeImageData(bitmap, plan, output)
        writeChunk(output, CHUNK_IEND, EMPTY_BYTES)
    }

    private fun createHeader(width: Int, height: Int, bitDepth: Int) = ByteArray(IHDR_DATA_SIZE).also {
        writeInt(it, 0, width)
        writeInt(it, 4, height)
        it[8] = bitDepth.toByte()
        it[9] = INDEXED_COLOR_TYPE.toByte()
        it[10] = 0
        it[11] = 0
        it[12] = 0
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

    private fun writeImageData(bitmap: Bitmap, plan: PalettePlan, output: OutputStream) {
        val pixelsPerByte = BITS_PER_BYTE / plan.bitDepth
        val packedRowBytes = (bitmap.width.toLong() * plan.bitDepth + BITS_PER_BYTE - 1L) /
            BITS_PER_BYTE
        require(packedRowBytes <= Int.MAX_VALUE - 1L)
        val scanline = ByteArray(packedRowBytes.toInt() + 1)
        val row = IntArray(bitmap.width)
        val idat = ChunkedIdatOutputStream(output)
        val deflater = Deflater(Deflater.BEST_COMPRESSION)
        try {
            val compressed = DeflaterOutputStream(idat, deflater, DEFLATE_BUFFER_SIZE)
            for (y in 0 until bitmap.height) {
                scanline.fill(0)
                bitmap.getPixels(row, 0, bitmap.width, 0, y, bitmap.width, 1)
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
private const val IHDR_DATA_SIZE = 13
private const val CHUNK_TYPE_SIZE = 4
private const val CRC_SIZE = 4
private const val CHUNK_OVERHEAD_BYTES = 12L
private const val UINT_MASK = 0xFFFF_FFFFL
private const val DEFLATE_BUFFER_SIZE = 8 * 1024
private const val IDAT_CHUNK_DATA_SIZE = 64 * 1024
private const val MAX_METADATA_SCAN_BYTES = 1024L * 1024L
