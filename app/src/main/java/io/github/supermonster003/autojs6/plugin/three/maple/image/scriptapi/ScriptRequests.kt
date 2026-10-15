package io.github.supermonster003.autojs6.plugin.three.maple.image.scriptapi

import android.os.Bundle
import org.autojs.plugin.gallery.api.GalleryContract
import org.autojs.plugin.gallery.api.GalleryErrorCodes
import java.util.Locale

/** `openPicker` options (ROADMAP 10.2). [maxCount] is 1 for single selection. */
internal data class PickerRequest(
    val multiple: Boolean,
    val maxCount: Int,
    val mimeTypes: List<String>,
    val title: String?,
)

/** Output encoding shared by the editor and the converter; a null [format] keeps the source format when it can be written. */
internal data class EncodeRequest(
    val format: String? = null,
    val quality: Int? = null,
    val keepExif: Boolean = false,
    val keepLocation: Boolean = false,
)

/** `convert` options beyond [EncodeRequest]. */
internal data class ConvertRequest(
    val displayName: String?,
    val encode: EncodeRequest,
    val lossless: Boolean = false,
    val targetBytes: Long? = null,
    val sizeMode: String = GalleryContract.SIZE_ORIGINAL,
    val percent: Int? = null,
    val longEdge: Int? = null,
    val width: Int? = null,
    val height: Int? = null,
    val keepAspectRatio: Boolean = true,
    val background: Int? = null,
)

/**
 * The request Bundles of the gallery contract as this plugin reads them (ROADMAP 10.2). The host
 * already checked the script's arguments; these checks repeat the contract's ranges so a request
 * that skipped the host's checks is still refused with `INVALID_ARGUMENT` (or `LIMIT_EXCEEDED`)
 * instead of reaching the UI or the encoder.
 */
internal object ScriptRequests {

    private val IMAGE_MIME_TYPE = Regex("^image/(\\*|[a-z0-9][a-z0-9!#$&^_.+-]{0,126})$")

    fun picker(options: Bundle?): PickerRequest {
        val multiple = options?.getBoolean(GalleryContract.KEY_MULTIPLE, false) ?: false
        val maxCount = if (options?.containsKey(GalleryContract.KEY_MAX_COUNT) == true) options.getInt(GalleryContract.KEY_MAX_COUNT) else null
        if (maxCount != null && maxCount !in 1..GalleryContract.MAX_PICK_COUNT) invalid("maxCount must be between 1 and ${GalleryContract.MAX_PICK_COUNT}, got $maxCount")
        val mimeTypes = options?.getStringArray(GalleryContract.KEY_MIME_TYPES)?.map { it?.trim()?.lowercase(Locale.ROOT).orEmpty() }?.distinct().orEmpty()
        if (mimeTypes.size > GalleryContract.MAX_MIME_TYPES) limit("at most ${GalleryContract.MAX_MIME_TYPES} MIME types, got ${mimeTypes.size}")
        mimeTypes.firstOrNull { !IMAGE_MIME_TYPE.matches(it) }?.let { invalid("not an image MIME type: $it") }
        val title = options?.getString(GalleryContract.KEY_TITLE)?.trim()?.takeIf { it.isNotEmpty() }
        if (title != null && title.length > GalleryContract.MAX_TITLE_LENGTH) limit("title exceeds ${GalleryContract.MAX_TITLE_LENGTH} characters")
        return PickerRequest(multiple, if (multiple) maxCount ?: GalleryContract.MAX_PICK_COUNT else 1, mimeTypes, title)
    }

    /** The viewer's display names (one per source) and start index. */
    fun viewer(sourceCount: Int, options: Bundle?): Pair<List<String>, Int> {
        if (sourceCount !in 1..GalleryContract.MAX_VIEW_PAGES) {
            if (sourceCount < 1) invalid("at least one image is required") else limit("the viewer opens at most ${GalleryContract.MAX_VIEW_PAGES} images")
        }
        val given = options ?: invalid("viewer options are required")
        val names = given.getStringArray(GalleryContract.KEY_DISPLAY_NAMES)?.toList() ?: invalid("displayNames are required")
        if (names.size != sourceCount) invalid("displayNames has ${names.size} entries for $sourceCount sources")
        val start = given.getInt(GalleryContract.KEY_START_INDEX, 0)
        if (start !in 0 until sourceCount) invalid("startIndex $start is outside 0..${sourceCount - 1}")
        return names.map { displayName(it) ?: "" } to start
    }

    fun displayName(value: String?): String? =
        value?.trim()?.takeIf { it.isNotEmpty() }?.take(GalleryContract.MAX_DISPLAY_NAME_LENGTH)

    fun encode(options: Bundle?): EncodeRequest {
        val format = options?.getString(GalleryContract.KEY_FORMAT)?.lowercase(Locale.ROOT)
        if (format != null && format !in GalleryContract.FORMATS) invalid("format must be one of ${GalleryContract.FORMATS.joinToString()}, got $format")
        val quality = if (options?.containsKey(GalleryContract.KEY_QUALITY) == true) options.getInt(GalleryContract.KEY_QUALITY) else null
        if (quality != null && quality !in GalleryContract.MIN_QUALITY..GalleryContract.MAX_QUALITY) invalid("quality must be between ${GalleryContract.MIN_QUALITY} and ${GalleryContract.MAX_QUALITY}, got $quality")
        return EncodeRequest(
            format = format,
            quality = quality,
            keepExif = options?.getBoolean(GalleryContract.KEY_KEEP_EXIF, false) ?: false,
            keepLocation = options?.getBoolean(GalleryContract.KEY_KEEP_LOCATION, false) ?: false,
        )
    }

    fun convert(options: Bundle?): ConvertRequest {
        val encode = encode(options)
        val lossless = options?.getBoolean(GalleryContract.KEY_LOSSLESS, false) ?: false
        if (lossless && encode.format != null && encode.format != GalleryContract.FORMAT_WEBP) invalid("lossless applies to WebP output")
        val targetBytes = if (options?.containsKey(GalleryContract.KEY_TARGET_BYTES) == true) options.getLong(GalleryContract.KEY_TARGET_BYTES) else null
        if (targetBytes != null) {
            if (targetBytes < 1) invalid("targetBytes must be positive, got $targetBytes")
            if (encode.format == GalleryContract.FORMAT_PNG || lossless) invalid("targetBytes applies to JPEG and lossy WebP output")
        }
        val sizeMode = options?.getString(GalleryContract.KEY_SIZE_MODE) ?: GalleryContract.SIZE_ORIGINAL
        if (sizeMode !in GalleryContract.SIZE_MODES) invalid("sizeMode must be one of ${GalleryContract.SIZE_MODES.joinToString()}, got $sizeMode")
        fun intOrNull(key: String): Int? = if (options?.containsKey(key) == true) options.getInt(key) else null
        val percent = intOrNull(GalleryContract.KEY_PERCENT)
        val longEdge = intOrNull(GalleryContract.KEY_LONG_EDGE)
        val width = intOrNull(GalleryContract.KEY_WIDTH)
        val height = intOrNull(GalleryContract.KEY_HEIGHT)
        val keepAspectRatio = options?.getBoolean(GalleryContract.KEY_KEEP_ASPECT_RATIO, true) ?: true
        when (sizeMode) {
            GalleryContract.SIZE_PERCENT -> if (percent == null || percent !in GalleryContract.MIN_PERCENT..GalleryContract.MAX_PERCENT) {
                invalid("percent must be between ${GalleryContract.MIN_PERCENT} and ${GalleryContract.MAX_PERCENT}")
            }
            GalleryContract.SIZE_LONG_EDGE -> if (longEdge == null || longEdge !in 1..GalleryContract.MAX_DIMENSION) {
                invalid("longEdge must be between 1 and ${GalleryContract.MAX_DIMENSION}")
            }
            GalleryContract.SIZE_CUSTOM -> {
                if (width == null && height == null) invalid("the custom size needs width and/or height")
                if (width != null && width !in 1..GalleryContract.MAX_DIMENSION) invalid("width must be between 1 and ${GalleryContract.MAX_DIMENSION}")
                if (height != null && height !in 1..GalleryContract.MAX_DIMENSION) invalid("height must be between 1 and ${GalleryContract.MAX_DIMENSION}")
                if (!keepAspectRatio && (width == null || height == null)) invalid("a stretched size needs width and height")
            }
        }
        val background = if (options?.containsKey(GalleryContract.KEY_BACKGROUND) == true) options.getInt(GalleryContract.KEY_BACKGROUND) else null
        return ConvertRequest(
            displayName = displayName(options?.getString(GalleryContract.KEY_DISPLAY_NAME)),
            encode = encode,
            lossless = lossless,
            targetBytes = targetBytes,
            sizeMode = sizeMode,
            percent = percent,
            longEdge = longEdge,
            width = width,
            height = height,
            keepAspectRatio = keepAspectRatio,
            background = background,
        )
    }

    private fun invalid(detail: String): Nothing = throw ContractViolation(GalleryErrorCodes.INVALID_ARGUMENT, detail)

    private fun limit(detail: String): Nothing = throw ContractViolation(GalleryErrorCodes.LIMIT_EXCEEDED, detail)
}
