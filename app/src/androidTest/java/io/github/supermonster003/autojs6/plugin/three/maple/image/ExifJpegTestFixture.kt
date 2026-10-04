package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.graphics.Bitmap
import android.graphics.Color
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.io.FileOutputStream

internal object ExifJpegTestFixture {

    const val WIDTH = 8
    const val HEIGHT = 6
    const val PATTERN_WIDTH = 80
    const val PATTERN_HEIGHT = 48
    const val MAKE = "OpenAI Camera Lab"
    const val MODEL = "Fixture One"
    const val CAPTURED_AT = "2026:08:31 14:05:09"
    const val LATITUDE = 37.4219999
    const val LONGITUDE = -122.0840575

    val TOP_LEFT_COLOR: Int = Color.rgb(220, 32, 32)
    val TOP_RIGHT_COLOR: Int = Color.rgb(32, 190, 48)
    val BOTTOM_LEFT_COLOR: Int = Color.rgb(32, 80, 220)
    val BOTTOM_RIGHT_COLOR: Int = Color.rgb(224, 200, 32)

    fun writePlainTo(file: File) {
        val bitmap = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888)
        try {
            bitmap.eraseColor(Color.rgb(42, 96, 160))
            FileOutputStream(file).use { output ->
                check(bitmap.compress(Bitmap.CompressFormat.JPEG, 95, output))
            }
        } finally {
            bitmap.recycle()
        }
    }

    fun writeCompleteTo(file: File) {
        writePlainTo(file)
        ExifInterface(file).apply {
            setAttribute(ExifInterface.TAG_DATETIME_ORIGINAL, CAPTURED_AT)
            setAttribute(ExifInterface.TAG_MAKE, MAKE)
            setAttribute(ExifInterface.TAG_MODEL, MODEL)
            setAttribute(ExifInterface.TAG_EXPOSURE_TIME, "1/125")
            setAttribute(ExifInterface.TAG_F_NUMBER, "9/5")
            setAttribute(ExifInterface.TAG_PHOTOGRAPHIC_SENSITIVITY, "200")
            setAttribute(ExifInterface.TAG_FOCAL_LENGTH, "17/4")
            setAttribute(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_ROTATE_90.toString(),
            )
            setLatLong(LATITUDE, LONGITUDE)
            saveAttributes()
        }
    }

    fun writeOrientedPatternTo(file: File, orientationTag: Int) {
        require(orientationTag in ExifInterface.ORIENTATION_NORMAL..ExifInterface.ORIENTATION_ROTATE_270)
        val bitmap = Bitmap.createBitmap(PATTERN_WIDTH, PATTERN_HEIGHT, Bitmap.Config.ARGB_8888)
        try {
            val pixels = IntArray(PATTERN_WIDTH * PATTERN_HEIGHT) { index ->
                val x = index % PATTERN_WIDTH
                val y = index / PATTERN_WIDTH
                when {
                    x < PATTERN_WIDTH / 2 && y < PATTERN_HEIGHT / 2 -> TOP_LEFT_COLOR
                    x >= PATTERN_WIDTH / 2 && y < PATTERN_HEIGHT / 2 -> TOP_RIGHT_COLOR
                    x < PATTERN_WIDTH / 2 -> BOTTOM_LEFT_COLOR
                    else -> BOTTOM_RIGHT_COLOR
                }
            }
            bitmap.setPixels(pixels, 0, PATTERN_WIDTH, 0, 0, PATTERN_WIDTH, PATTERN_HEIGHT)
            FileOutputStream(file).use { output ->
                check(bitmap.compress(Bitmap.CompressFormat.JPEG, 100, output))
            }
        } finally {
            bitmap.recycle()
        }
        ExifInterface(file).apply {
            setAttribute(ExifInterface.TAG_ORIENTATION, orientationTag.toString())
            saveAttributes()
        }
    }

    fun writeMalformedExifTo(file: File) {
        writePlainTo(file)
        val jpeg = file.readBytes()
        check(jpeg.size > 2 && jpeg[0] == 0xFF.toByte() && jpeg[1] == 0xD8.toByte())
        file.writeBytes(
            jpeg.copyOfRange(0, 2) + MALFORMED_EXIF_SEGMENT + jpeg.copyOfRange(2, jpeg.size),
        )
    }

    // Valid APP1 framing with an Exif TIFF header whose first-IFD offset is outside the segment.
    private val MALFORMED_EXIF_SEGMENT = byteArrayOf(
        0xFF.toByte(), 0xE1.toByte(), 0x00, 0x10,
        0x45, 0x78, 0x69, 0x66, 0x00, 0x00,
        0x49, 0x49, 0x2A, 0x00, 0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0x7F,
    )
}
