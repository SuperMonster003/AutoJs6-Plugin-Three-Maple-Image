package io.github.supermonster003.autojs6.plugin.imageviewer

import androidx.exifinterface.media.ExifInterface
import java.io.FileDescriptor
import java.io.InputStream
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

internal data class ImageExifOrientation(
    val tagValue: Int,
    val rotationDegrees: Int,
    val mirrored: Boolean,
) {
    val swapsDimensions: Boolean
        get() = rotationDegrees == 90 || rotationDegrees == 270
}

/** Privacy-conscious EXIF subset. GPS coordinates are deliberately never retained. */
internal data class ImageExifMetadata(
    val capturedAt: String?,
    val make: String?,
    val model: String?,
    val exposureTimeSeconds: Double?,
    val apertureFNumber: Double?,
    val sensitivityIso: Int?,
    val focalLengthMm: Double?,
    val orientation: ImageExifOrientation?,
    val hasGpsMetadata: Boolean,
)

/**
 * Presentation facts after Glide has normalized the source pixels for all eight EXIF orientations.
 * The viewer then applies only user-requested quarter turns to that normalized drawable.
 */
internal object ImageExifOrientationCorrection {

    fun displayDimensions(
        width: Int?,
        height: Int?,
        orientation: ImageExifOrientation?,
    ): Pair<Int?, Int?> = if (orientation?.swapsDimensions == true) {
        height to width
    } else {
        width to height
    }

    /** Maps encoded source coordinates into the same EXIF-normalized space produced by Glide. */
    fun sourceToDisplayMatrixValues(
        width: Int,
        height: Int,
        orientation: ImageExifOrientation?,
    ): FloatArray? {
        if (width <= 0 || height <= 0) return null
        val sourceWidth = width.toFloat()
        val sourceHeight = height.toFloat()
        return when (orientation?.tagValue ?: ExifInterface.ORIENTATION_NORMAL) {
            ExifInterface.ORIENTATION_NORMAL -> floatArrayOf(
                1f, 0f, 0f,
                0f, 1f, 0f,
                0f, 0f, 1f,
            )
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> floatArrayOf(
                -1f, 0f, sourceWidth,
                0f, 1f, 0f,
                0f, 0f, 1f,
            )
            ExifInterface.ORIENTATION_ROTATE_180 -> floatArrayOf(
                -1f, 0f, sourceWidth,
                0f, -1f, sourceHeight,
                0f, 0f, 1f,
            )
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> floatArrayOf(
                1f, 0f, 0f,
                0f, -1f, sourceHeight,
                0f, 0f, 1f,
            )
            ExifInterface.ORIENTATION_TRANSPOSE -> floatArrayOf(
                0f, 1f, 0f,
                1f, 0f, 0f,
                0f, 0f, 1f,
            )
            ExifInterface.ORIENTATION_ROTATE_90 -> floatArrayOf(
                0f, -1f, sourceHeight,
                1f, 0f, 0f,
                0f, 0f, 1f,
            )
            ExifInterface.ORIENTATION_TRANSVERSE -> floatArrayOf(
                0f, -1f, sourceHeight,
                -1f, 0f, sourceWidth,
                0f, 0f, 1f,
            )
            ExifInterface.ORIENTATION_ROTATE_270 -> floatArrayOf(
                0f, 1f, 0f,
                -1f, 0f, sourceWidth,
                0f, 0f, 1f,
            )
            else -> return null
        }
    }
}

internal object ImageExifMetadataReader {

    fun read(inputStream: InputStream): ImageExifMetadata? =
        runCatching { extract(ExifInterface(inputStream)) }.getOrNull()

    fun read(fileDescriptor: FileDescriptor): ImageExifMetadata? =
        runCatching { extract(ExifInterface(fileDescriptor)) }.getOrNull()

    @Suppress("DEPRECATION")
    private fun extract(exif: ExifInterface): ImageExifMetadata? {
        val capturedAt = sequenceOf(
            ExifInterface.TAG_DATETIME_ORIGINAL,
            ExifInterface.TAG_DATETIME_DIGITIZED,
        ).mapNotNull { tag -> cleanText(exif.getAttribute(tag), MAX_DATE_TIME_LENGTH) }
            .firstOrNull()
        val make = cleanText(exif.getAttribute(ExifInterface.TAG_MAKE), MAX_DEVICE_PART_LENGTH)
        val model = cleanText(exif.getAttribute(ExifInterface.TAG_MODEL), MAX_DEVICE_PART_LENGTH)
        val exposureTime = positiveFiniteAttribute(
            exif,
            ExifInterface.TAG_EXPOSURE_TIME,
            MAX_EXPOSURE_SECONDS,
        )
        val aperture = positiveFiniteAttribute(
            exif,
            ExifInterface.TAG_F_NUMBER,
            MAX_APERTURE_F_NUMBER,
        )
        val sensitivity = positiveIntAttribute(
            exif,
            ExifInterface.TAG_PHOTOGRAPHIC_SENSITIVITY,
            MAX_ISO,
        ) ?: positiveIntAttribute(
            exif,
            ExifInterface.TAG_ISO_SPEED_RATINGS,
            MAX_ISO,
        )
        val focalLength = positiveFiniteAttribute(
            exif,
            ExifInterface.TAG_FOCAL_LENGTH,
            MAX_FOCAL_LENGTH_MM,
        )
        val orientation = exif.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_UNDEFINED,
        ).takeIf { it in ExifInterface.ORIENTATION_NORMAL..ExifInterface.ORIENTATION_ROTATE_270 }
            ?.let { tagValue ->
                ImageExifOrientation(
                    tagValue = tagValue,
                    rotationDegrees = exif.rotationDegrees,
                    mirrored = exif.isFlipped,
                )
            }
        val hasGpsMetadata = GPS_TAGS.any(exif::hasAttribute)

        return ImageExifMetadata(
            capturedAt = capturedAt,
            make = make,
            model = model,
            exposureTimeSeconds = exposureTime,
            apertureFNumber = aperture,
            sensitivityIso = sensitivity,
            focalLengthMm = focalLength,
            orientation = orientation,
            hasGpsMetadata = hasGpsMetadata,
        ).takeIf { metadata ->
            metadata.capturedAt != null ||
                metadata.make != null ||
                metadata.model != null ||
                metadata.exposureTimeSeconds != null ||
                metadata.apertureFNumber != null ||
                metadata.sensitivityIso != null ||
                metadata.focalLengthMm != null ||
                metadata.orientation != null ||
                metadata.hasGpsMetadata
        }
    }

    private fun cleanText(value: String?, maxLength: Int): String? {
        val collapsed = value?.trim()?.replace(WHITESPACE, " ")?.takeIf { it.isNotEmpty() }
            ?: return null
        if (collapsed.length > maxLength || collapsed.any(::isUnsafeCharacter)) return null
        return collapsed
    }

    private fun positiveFiniteAttribute(
        exif: ExifInterface,
        tag: String,
        maximum: Double,
    ): Double? = exif.getAttributeDouble(tag, Double.NaN)
        .takeIf { value -> value.isFinite() && value > 0.0 && value <= maximum }

    private fun positiveIntAttribute(
        exif: ExifInterface,
        tag: String,
        maximum: Int,
    ): Int? = exif.getAttributeInt(tag, INVALID_INT)
        .takeIf { value -> value in 1..maximum }

    private fun isUnsafeCharacter(character: Char): Boolean =
        character.isISOControl() || character in BIDI_CONTROL_CHARACTERS

    private val GPS_TAGS = arrayOf(
        ExifInterface.TAG_GPS_LATITUDE,
        ExifInterface.TAG_GPS_LONGITUDE,
        ExifInterface.TAG_GPS_ALTITUDE,
        ExifInterface.TAG_GPS_TIMESTAMP,
        ExifInterface.TAG_GPS_DATESTAMP,
        ExifInterface.TAG_GPS_PROCESSING_METHOD,
    )

    private val WHITESPACE = Regex("\\s+")
    private val BIDI_CONTROL_CHARACTERS = setOf(
        '\u061C',
        '\u200E',
        '\u200F',
        '\u202A',
        '\u202B',
        '\u202C',
        '\u202D',
        '\u202E',
        '\u2066',
        '\u2067',
        '\u2068',
        '\u2069',
    )

    private const val INVALID_INT = -1
    private const val MAX_DATE_TIME_LENGTH = 32
    private const val MAX_DEVICE_PART_LENGTH = 96
    private const val MAX_EXPOSURE_SECONDS = 86_400.0
    private const val MAX_APERTURE_F_NUMBER = 256.0
    private const val MAX_ISO = 100_000_000
    private const val MAX_FOCAL_LENGTH_MM = 100_000.0
}

/** Pure presentation helpers kept independent of Android resources for local unit tests. */
internal object ImageExifValueFormatter {

    fun capturedAt(value: String?): String? {
        val match = value?.let(EXIF_DATE_TIME::matchEntire) ?: return null
        val year = match.groupValues[1].toInt()
        val month = match.groupValues[2].toInt()
        val day = match.groupValues[3].toInt()
        val hour = match.groupValues[4].toInt()
        val minute = match.groupValues[5].toInt()
        val second = match.groupValues[6].toInt()
        if (
            year !in 1..9999 ||
            month !in 1..12 ||
            day !in 1..daysInMonth(year, month) ||
            hour !in 0..23 ||
            minute !in 0..59 ||
            second !in 0..59
        ) {
            return null
        }
        return "%04d-%02d-%02d %02d:%02d:%02d".format(
            Locale.ROOT,
            year,
            month,
            day,
            hour,
            minute,
            second,
        )
    }

    fun device(make: String?, model: String?): String? = when {
        make == null -> model
        model == null -> make
        model.startsWith(make, ignoreCase = true) -> model
        else -> "$make $model"
    }

    fun exposure(metadata: ImageExifMetadata, locale: Locale): String? {
        val parts = buildList {
            metadata.exposureTimeSeconds?.let { add(formatExposureTime(it, locale)) }
            metadata.apertureFNumber?.let { add("f/${formatNumber(it, locale)}") }
            metadata.sensitivityIso?.let { add("ISO $it") }
            metadata.focalLengthMm?.let { add("${formatNumber(it, locale)} mm") }
        }
        return parts.takeIf { it.isNotEmpty() }?.joinToString(" · ")
    }

    internal fun formatExposureTime(seconds: Double, locale: Locale): String {
        if (seconds < 1.0) {
            val reciprocal = 1.0 / seconds
            val denominator = reciprocal.roundToInt()
            if (
                denominator > 1 &&
                abs(reciprocal - denominator) / reciprocal <= RECIPROCAL_TOLERANCE
            ) {
                return "1/$denominator s"
            }
        }
        return "${formatNumber(seconds, locale)} s"
    }

    private fun formatNumber(value: Double, locale: Locale): String =
        DecimalFormat("0.###", DecimalFormatSymbols.getInstance(locale)).format(value)

    private fun daysInMonth(year: Int, month: Int): Int = when (month) {
        2 -> if (isLeapYear(year)) 29 else 28
        4, 6, 9, 11 -> 30
        else -> 31
    }

    private fun isLeapYear(year: Int): Boolean =
        year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)

    private val EXIF_DATE_TIME = Regex(
        "^(\\d{4})[:-](\\d{2})[:-](\\d{2}) (\\d{2}):(\\d{2}):(\\d{2})$",
    )

    private const val RECIPROCAL_TOLERANCE = 0.02
}
