package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.content.ContentResolver
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.io.IOException
import java.util.Collections

internal data class SourceExifSnapshot(
    val orientation: Int = ExifInterface.ORIENTATION_NORMAL,
    val preservableMetadata: PreservedExifMetadata = PreservedExifMetadata.EMPTY,
)

internal class PreservedExifMetadata private constructor(
    attributes: Map<String, String>,
    private val encodedValueBytes: Long,
) {

    val attributes: Map<String, String> = Collections.unmodifiableMap(LinkedHashMap(attributes))
    val isEmpty: Boolean
        get() = attributes.isEmpty()

    fun estimatedEncodedOverhead(format: ImageOutputFormat): Long {
        if (isEmpty) return 0L
        val containerOverhead = when (format) {
            ImageOutputFormat.JPEG -> JPEG_CONTAINER_OVERHEAD_BYTES
            ImageOutputFormat.PNG -> PNG_CONTAINER_OVERHEAD_BYTES
            ImageOutputFormat.WEBP -> WEBP_CONTAINER_OVERHEAD_BYTES
        }
        return encodedValueBytes + attributes.size.toLong() * ESTIMATED_IFD_ENTRY_BYTES + containerOverhead
    }

    companion object {
        val EMPTY = PreservedExifMetadata(emptyMap(), 0L)

        internal fun from(attributes: Map<String, String>): PreservedExifMetadata {
            if (attributes.isEmpty()) return EMPTY
            val encodedValueBytes = attributes.values.sumOf { value ->
                value.toByteArray(Charsets.UTF_8).size.toLong()
            }
            return PreservedExifMetadata(attributes, encodedValueBytes)
        }

        private const val ESTIMATED_IFD_ENTRY_BYTES = 20L
        private const val JPEG_CONTAINER_OVERHEAD_BYTES = 768L
        private const val PNG_CONTAINER_OVERHEAD_BYTES = 780L
        private const val WEBP_CONTAINER_OVERHEAD_BYTES = 800L
    }
}

/** Copies a bounded allowlist of capture metadata and excludes every location-bearing container. */
internal object ExifMetadataPolicy {

    fun read(resolver: ContentResolver, uri: Uri): SourceExifSnapshot = try {
        resolver.openFileDescriptor(uri, ImageToolsPlugin.INPUT_OPEN_MODE)?.use { descriptor ->
            snapshot(ExifInterface(descriptor.fileDescriptor))
        } ?: SourceExifSnapshot()
    } catch (_: IOException) {
        SourceExifSnapshot()
    } catch (_: RuntimeException) {
        SourceExifSnapshot()
    }

    internal fun snapshot(exif: ExifInterface): SourceExifSnapshot = SourceExifSnapshot(
        orientation = exif.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL,
        ),
        preservableMetadata = extractPreservableMetadata(exif),
    )

    internal fun applyTo(file: File, metadata: PreservedExifMetadata) {
        if (metadata.isEmpty) return
        val exif = ExifInterface(file.absolutePath)
        metadata.attributes.forEach(exif::setAttribute)
        REMOVED_OPAQUE_TAGS.forEach { tag -> exif.setAttribute(tag, null) }
        GPS_TAGS.forEach { tag -> exif.setAttribute(tag, null) }
        exif.setAttribute(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL.toString(),
        )
        exif.saveAttributes()
        verifySanitized(file)
    }

    private fun extractPreservableMetadata(exif: ExifInterface): PreservedExifMetadata {
        val attributes = linkedMapOf<String, String>()
        var totalBytes = 0L
        for (tag in PRESERVED_TAGS) {
            val value = exif.getAttribute(tag) ?: continue
            val valueBytes = value.toByteArray(Charsets.UTF_8).size.toLong()
            if (valueBytes > MAX_ATTRIBUTE_BYTES || totalBytes > MAX_TOTAL_ATTRIBUTE_BYTES - valueBytes) {
                continue
            }
            attributes[tag] = value
            totalBytes += valueBytes
        }
        return PreservedExifMetadata.from(attributes)
    }

    private fun verifySanitized(file: File) {
        val exif = ExifInterface(file.absolutePath)
        val orientation = exif.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL,
        )
        if (orientation != ExifInterface.ORIENTATION_NORMAL) {
            throw IOException("Output EXIF orientation was not normalized")
        }
        if (exif.latLong != null || GPS_TAGS.any { exif.getAttribute(it) != null }) {
            throw IOException("Output EXIF still contains GPS metadata")
        }
        if (REMOVED_OPAQUE_TAGS.any { exif.getAttribute(it) != null }) {
            throw IOException("Output EXIF still contains opaque metadata")
        }
        if (exif.hasThumbnail()) {
            throw IOException("Output EXIF unexpectedly contains an embedded preview")
        }
    }

    private val PRESERVED_TAGS = listOf(
        ExifInterface.TAG_X_RESOLUTION,
        ExifInterface.TAG_Y_RESOLUTION,
        ExifInterface.TAG_RESOLUTION_UNIT,
        ExifInterface.TAG_DATETIME,
        ExifInterface.TAG_MAKE,
        ExifInterface.TAG_MODEL,
        ExifInterface.TAG_SOFTWARE,
        ExifInterface.TAG_ARTIST,
        ExifInterface.TAG_COPYRIGHT,
        ExifInterface.TAG_EXIF_VERSION,
        ExifInterface.TAG_FLASHPIX_VERSION,
        ExifInterface.TAG_COLOR_SPACE,
        ExifInterface.TAG_GAMMA,
        ExifInterface.TAG_DATETIME_ORIGINAL,
        ExifInterface.TAG_DATETIME_DIGITIZED,
        ExifInterface.TAG_OFFSET_TIME,
        ExifInterface.TAG_OFFSET_TIME_ORIGINAL,
        ExifInterface.TAG_OFFSET_TIME_DIGITIZED,
        ExifInterface.TAG_SUBSEC_TIME,
        ExifInterface.TAG_SUBSEC_TIME_ORIGINAL,
        ExifInterface.TAG_SUBSEC_TIME_DIGITIZED,
        ExifInterface.TAG_EXPOSURE_TIME,
        ExifInterface.TAG_F_NUMBER,
        ExifInterface.TAG_EXPOSURE_PROGRAM,
        ExifInterface.TAG_SPECTRAL_SENSITIVITY,
        ExifInterface.TAG_PHOTOGRAPHIC_SENSITIVITY,
        ExifInterface.TAG_SENSITIVITY_TYPE,
        ExifInterface.TAG_STANDARD_OUTPUT_SENSITIVITY,
        ExifInterface.TAG_RECOMMENDED_EXPOSURE_INDEX,
        ExifInterface.TAG_ISO_SPEED,
        ExifInterface.TAG_ISO_SPEED_LATITUDE_YYY,
        ExifInterface.TAG_ISO_SPEED_LATITUDE_ZZZ,
        ExifInterface.TAG_SHUTTER_SPEED_VALUE,
        ExifInterface.TAG_APERTURE_VALUE,
        ExifInterface.TAG_BRIGHTNESS_VALUE,
        ExifInterface.TAG_EXPOSURE_BIAS_VALUE,
        ExifInterface.TAG_MAX_APERTURE_VALUE,
        ExifInterface.TAG_SUBJECT_DISTANCE,
        ExifInterface.TAG_METERING_MODE,
        ExifInterface.TAG_LIGHT_SOURCE,
        ExifInterface.TAG_FLASH,
        ExifInterface.TAG_FOCAL_LENGTH,
        ExifInterface.TAG_FLASH_ENERGY,
        ExifInterface.TAG_FOCAL_PLANE_X_RESOLUTION,
        ExifInterface.TAG_FOCAL_PLANE_Y_RESOLUTION,
        ExifInterface.TAG_FOCAL_PLANE_RESOLUTION_UNIT,
        ExifInterface.TAG_EXPOSURE_INDEX,
        ExifInterface.TAG_SENSING_METHOD,
        ExifInterface.TAG_FILE_SOURCE,
        ExifInterface.TAG_SCENE_TYPE,
        ExifInterface.TAG_CUSTOM_RENDERED,
        ExifInterface.TAG_EXPOSURE_MODE,
        ExifInterface.TAG_WHITE_BALANCE,
        ExifInterface.TAG_DIGITAL_ZOOM_RATIO,
        ExifInterface.TAG_FOCAL_LENGTH_IN_35MM_FILM,
        ExifInterface.TAG_SCENE_CAPTURE_TYPE,
        ExifInterface.TAG_GAIN_CONTROL,
        ExifInterface.TAG_CONTRAST,
        ExifInterface.TAG_SATURATION,
        ExifInterface.TAG_SHARPNESS,
        ExifInterface.TAG_SUBJECT_DISTANCE_RANGE,
        ExifInterface.TAG_IMAGE_UNIQUE_ID,
        ExifInterface.TAG_CAMERA_OWNER_NAME,
        ExifInterface.TAG_BODY_SERIAL_NUMBER,
        ExifInterface.TAG_LENS_SPECIFICATION,
        ExifInterface.TAG_LENS_MAKE,
        ExifInterface.TAG_LENS_MODEL,
        ExifInterface.TAG_LENS_SERIAL_NUMBER,
        ExifInterface.TAG_INTEROPERABILITY_INDEX,
    )

    private val REMOVED_OPAQUE_TAGS = listOf(
        ExifInterface.TAG_XMP,
        ExifInterface.TAG_MAKER_NOTE,
        ExifInterface.TAG_USER_COMMENT,
    )

    private val GPS_TAGS = listOf(
        ExifInterface.TAG_GPS_VERSION_ID,
        ExifInterface.TAG_GPS_LATITUDE_REF,
        ExifInterface.TAG_GPS_LATITUDE,
        ExifInterface.TAG_GPS_LONGITUDE_REF,
        ExifInterface.TAG_GPS_LONGITUDE,
        ExifInterface.TAG_GPS_ALTITUDE_REF,
        ExifInterface.TAG_GPS_ALTITUDE,
        ExifInterface.TAG_GPS_TIMESTAMP,
        ExifInterface.TAG_GPS_SATELLITES,
        ExifInterface.TAG_GPS_STATUS,
        ExifInterface.TAG_GPS_MEASURE_MODE,
        ExifInterface.TAG_GPS_DOP,
        ExifInterface.TAG_GPS_SPEED_REF,
        ExifInterface.TAG_GPS_SPEED,
        ExifInterface.TAG_GPS_TRACK_REF,
        ExifInterface.TAG_GPS_TRACK,
        ExifInterface.TAG_GPS_IMG_DIRECTION_REF,
        ExifInterface.TAG_GPS_IMG_DIRECTION,
        ExifInterface.TAG_GPS_MAP_DATUM,
        ExifInterface.TAG_GPS_DEST_LATITUDE_REF,
        ExifInterface.TAG_GPS_DEST_LATITUDE,
        ExifInterface.TAG_GPS_DEST_LONGITUDE_REF,
        ExifInterface.TAG_GPS_DEST_LONGITUDE,
        ExifInterface.TAG_GPS_DEST_BEARING_REF,
        ExifInterface.TAG_GPS_DEST_BEARING,
        ExifInterface.TAG_GPS_DEST_DISTANCE_REF,
        ExifInterface.TAG_GPS_DEST_DISTANCE,
        ExifInterface.TAG_GPS_PROCESSING_METHOD,
        ExifInterface.TAG_GPS_AREA_INFORMATION,
        ExifInterface.TAG_GPS_DATESTAMP,
        ExifInterface.TAG_GPS_DIFFERENTIAL,
        ExifInterface.TAG_GPS_H_POSITIONING_ERROR,
    )

    private const val MAX_ATTRIBUTE_BYTES = 8L * 1024L
    private const val MAX_TOTAL_ATTRIBUTE_BYTES = 48L * 1024L
}
