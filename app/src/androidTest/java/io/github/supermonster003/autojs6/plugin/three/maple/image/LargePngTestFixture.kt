package io.github.supermonster003.autojs6.plugin.three.maple.image

import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.zip.CRC32
import java.util.zip.Deflater
import java.util.zip.DeflaterOutputStream

/** A highly compressed 20 MP PNG generated row-by-row without allocating a full source bitmap. */
internal object LargePngTestFixture {

    const val WIDTH = 5_000
    const val HEIGHT = 4_000

    fun writeTo(file: File) {
        val topRow = row(
            leftRed = 230,
            leftGreen = 30,
            leftBlue = 40,
            rightRed = 30,
            rightGreen = 70,
            rightBlue = 230,
        )
        val bottomRow = row(
            leftRed = 30,
            leftGreen = 210,
            leftBlue = 70,
            rightRed = 235,
            rightGreen = 205,
            rightBlue = 35,
        )
        val compressed = ByteArrayOutputStream()
        DeflaterOutputStream(
            compressed,
            Deflater(Deflater.BEST_SPEED),
        ).use { output ->
            repeat(HEIGHT / 2) { output.write(topRow) }
            repeat(HEIGHT - HEIGHT / 2) { output.write(bottomRow) }
        }

        file.parentFile?.mkdirs()
        DataOutputStream(FileOutputStream(file)).use { output ->
            output.write(PNG_SIGNATURE)
            val header = ByteArrayOutputStream().also { bytes ->
                DataOutputStream(bytes).use { headerOutput ->
                    headerOutput.writeInt(WIDTH)
                    headerOutput.writeInt(HEIGHT)
                    headerOutput.writeByte(8)
                    headerOutput.writeByte(2)
                    headerOutput.writeByte(0)
                    headerOutput.writeByte(0)
                    headerOutput.writeByte(0)
                }
            }.toByteArray()
            writeChunk(output, "IHDR", header)
            writeChunk(output, "IDAT", compressed.toByteArray())
            writeChunk(output, "IEND", ByteArray(0))
        }
    }

    private fun row(
        leftRed: Int,
        leftGreen: Int,
        leftBlue: Int,
        rightRed: Int,
        rightGreen: Int,
        rightBlue: Int,
    ): ByteArray = ByteArray(1 + WIDTH * 3).also { row ->
        row[0] = 0
        for (x in 0 until WIDTH) {
            val offset = 1 + x * 3
            val leftHalf = x < WIDTH / 2
            row[offset] = (if (leftHalf) leftRed else rightRed).toByte()
            row[offset + 1] = (if (leftHalf) leftGreen else rightGreen).toByte()
            row[offset + 2] = (if (leftHalf) leftBlue else rightBlue).toByte()
        }
    }

    private fun writeChunk(output: DataOutputStream, name: String, data: ByteArray) {
        val type = name.toByteArray(Charsets.US_ASCII)
        val checksum = CRC32().apply {
            update(type)
            update(data)
        }
        output.writeInt(data.size)
        output.write(type)
        output.write(data)
        output.writeInt(checksum.value.toInt())
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
}
