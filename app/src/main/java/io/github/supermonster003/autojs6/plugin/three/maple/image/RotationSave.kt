package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.content.ContentResolver
import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.io.IOException

/**
 * EXIF orientations as the 8 transforms of the image grid (ROADMAP 8.2, T15): each value maps stored pixels to what
 * a viewer shows, so "rotate what is shown by a quarter turn" is a product of two of them. Android-free.
 */
internal object ExifOrientationMath {

    /** `[a, b, c, d]` for the matrix `[[a, b], [c, d]]` acting on (x, y) with y pointing down. */
    private val transforms = mapOf(
        ExifInterface.ORIENTATION_NORMAL to intArrayOf(1, 0, 0, 1),
        ExifInterface.ORIENTATION_FLIP_HORIZONTAL to intArrayOf(-1, 0, 0, 1),
        ExifInterface.ORIENTATION_ROTATE_180 to intArrayOf(-1, 0, 0, -1),
        ExifInterface.ORIENTATION_FLIP_VERTICAL to intArrayOf(1, 0, 0, -1),
        ExifInterface.ORIENTATION_TRANSPOSE to intArrayOf(0, 1, 1, 0),
        ExifInterface.ORIENTATION_ROTATE_90 to intArrayOf(0, -1, 1, 0),
        ExifInterface.ORIENTATION_TRANSVERSE to intArrayOf(0, -1, -1, 0),
        ExifInterface.ORIENTATION_ROTATE_270 to intArrayOf(0, 1, -1, 0),
    )
    private val clockwise = intArrayOf(0, -1, 1, 0)

    /**
     * The orientation that shows the image as [orientation] does, then turned clockwise by [quarterTurns]; an
     * unknown or missing orientation counts as normal.
     */
    fun rotate(orientation: Int, quarterTurns: Int): Int {
        var transform = transforms[orientation] ?: transforms.getValue(ExifInterface.ORIENTATION_NORMAL)
        repeat(Math.floorMod(quarterTurns, 4)) { transform = multiply(clockwise, transform) }
        return transforms.entries.first { it.value.contentEquals(transform) }.key
    }

    private fun multiply(left: IntArray, right: IntArray) = intArrayOf(
        left[0] * right[0] + left[1] * right[2],
        left[0] * right[1] + left[1] * right[3],
        left[2] * right[0] + left[3] * right[2],
        left[2] * right[1] + left[3] * right[3],
    )
}

/**
 * Saving a viewer rotation into the image (ROADMAP 8.2, T15). JPEG keeps every byte of its image data and only gets
 * a new orientation tag; PNG is decoded and encoded again without loss, WebP with the converter's default quality
 * (the user is told first). Other formats are not offered.
 */
internal object RotationSave {

    enum class Kind { EXIF_ORIENTATION, LOSSLESS_REENCODE, LOSSY_REENCODE }

    /** The image is larger than this device can decode at full size, so re-encoding it would shrink it. */
    class TooLargeException : IOException("The image is too large to rotate at full size")

    fun kind(mimeType: String): Kind? = when (mimeType) {
        "image/jpeg" -> Kind.EXIF_ORIENTATION
        "image/png" -> Kind.LOSSLESS_REENCODE
        "image/webp" -> Kind.LOSSY_REENCODE
        else -> null
    }

    /** Writes [source] turned clockwise by [quarterTurns] into [output]; the caller writes [output] over the original. */
    fun render(resolver: ContentResolver, source: Uri, mimeType: String, quarterTurns: Int, output: File, temporaryDirectory: File) {
        when (requireNotNull(kind(mimeType)) { "Rotation cannot be saved for $mimeType" }) {
            Kind.EXIF_ORIENTATION -> {
                (resolver.openInputStream(source) ?: throw IOException("The image cannot be read")).use { input ->
                    output.outputStream().use { input.copyTo(it) }
                }
                val exif = ExifInterface(output.path)
                val current = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
                exif.setAttribute(ExifInterface.TAG_ORIENTATION, ExifOrientationMath.rotate(current, quarterTurns).toString())
                exif.saveAttributes()
            }
            Kind.LOSSLESS_REENCODE, Kind.LOSSY_REENCODE -> reencode(resolver, source, mimeType, quarterTurns, output, temporaryDirectory)
        }
    }

    private fun reencode(resolver: ContentResolver, source: Uri, mimeType: String, quarterTurns: Int, output: File, temporaryDirectory: File) {
        val info = ImageBitmapIO.inspect(resolver, source)
        if (info.displaySize.pixelCount > ImageBitmapIO.editingPixelBudget()) throw TooLargeException()
        val decoded = ImageBitmapIO.decodeForEditing(resolver, source)
        try {
            // Decoding never shrinks an image within the budget; if it did, saving would lose resolution.
            if (decoded.width != info.displaySize.width || decoded.height != info.displaySize.height) throw TooLargeException()
            val rotated = ImageEditingEngine.transform(decoded, rotation = 90f * Math.floorMod(quarterTurns, 4))
            try {
                val format = ImageOutputFormat.entries.first { it.mimeType == mimeType }
                val options = ImageConversionOptions(
                    format = format,
                    quality = ImageConversionOptions.DEFAULT_QUALITY,
                    targetSize = ImagePixelSize(rotated.width, rotated.height),
                    preservedExifMetadata = info.preservableExifMetadata.takeIf { !it.isEmpty },
                )
                ImageBitmapIO.writeOutput(rotated, options, ImageToolsPlugin.MAX_OUTPUT_BYTES, temporaryDirectory) {
                    ParcelFileDescriptor.open(output, ParcelFileDescriptor.MODE_CREATE or ParcelFileDescriptor.MODE_TRUNCATE or ParcelFileDescriptor.MODE_WRITE_ONLY)
                }
            } finally {
                if (rotated !== decoded) rotated.recycle()
            }
        } finally {
            decoded.recycle()
        }
    }
}
