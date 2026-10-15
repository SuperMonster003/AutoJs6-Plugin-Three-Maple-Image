package io.github.supermonster003.autojs6.plugin.three.maple.image

internal enum class EditorSaveFormat(
    val explicitOutputFormat: ImageOutputFormat?,
) {
    FOLLOW_SOURCE(null),
    JPEG(ImageOutputFormat.JPEG),
    PNG(ImageOutputFormat.PNG),
    WEBP(ImageOutputFormat.WEBP),
}

internal data class EditorSaveOptions(
    val format: EditorSaveFormat = EditorSaveFormat.FOLLOW_SOURCE,
    val quality: Int = ImageConversionOptions.DEFAULT_QUALITY,
    val webpLossless: Boolean = false,
    /** Keep the camera and capture details of the original, as the converter offers. */
    val keepExif: Boolean = false,
    /**
     * Keep where the original was taken (ROADMAP 9.1, T14); offered only with "Show location" on, and like
     * [overwrite] never remembered for the next save.
     */
    val keepLocation: Boolean = false,
    /** Replace the original instead of creating a new file (D3); never remembered, each save dialog starts off. */
    val overwrite: Boolean = false,
) {
    init {
        require(quality in ImageConversionOptions.MIN_QUALITY..ImageConversionOptions.MAX_QUALITY) {
            "Quality must be between ${ImageConversionOptions.MIN_QUALITY} and " +
                ImageConversionOptions.MAX_QUALITY
        }
    }
}

internal object EditorSavePolicy {

    fun availableFormats(allowedOutputMimeTypes: Set<String>): List<EditorSaveFormat> =
        buildList {
            add(EditorSaveFormat.FOLLOW_SOURCE)
            EditorSaveFormat.entries.forEach { format ->
                val outputFormat = format.explicitOutputFormat ?: return@forEach
                if (outputFormat.mimeType in allowedOutputMimeTypes) add(format)
            }
        }.also { formats ->
            require(formats.size > 1) { "No permitted image output format is available" }
        }

    /**
     * The format an overwrite keeps (ROADMAP 8.2): the original's own format, or null when the original is in a
     * format the editor cannot write (GIF, HEIF, BMP...), where overwriting is not offered.
     */
    fun overwriteFormat(detectedMimeType: String, allowedOutputMimeTypes: Set<String>): ImageOutputFormat? =
        ImageOutputFormat.entries.firstOrNull { it.mimeType == detectedMimeType && it.mimeType in allowedOutputMimeTypes }

    /** The name the new file gets (`photo_edited.jpg`), or the original's name when it is overwritten. */
    fun outputFileName(displayName: String, format: ImageOutputFormat, suffix: String, overwrite: Boolean): String {
        if (overwrite) return displayName
        val base = displayName.substringBeforeLast('.', displayName).ifBlank { displayName }
        return "${base}_$suffix.${format.extension}"
    }

    fun effectiveFormat(
        selection: EditorSaveFormat,
        detectedMimeType: String,
        allowedOutputMimeTypes: Set<String>,
        overwrite: Boolean = false,
    ): ImageOutputFormat {
        if (overwrite) {
            return requireNotNull(overwriteFormat(detectedMimeType, allowedOutputMimeTypes)) { "The original's format cannot be written" }
        }
        val permitted = ImageOutputFormat.entries.filter {
            it.mimeType in allowedOutputMimeTypes
        }
        require(permitted.isNotEmpty()) { "No permitted image output format is available" }
        selection.explicitOutputFormat?.let { explicit ->
            require(explicit in permitted) { "Selected image output format is not permitted" }
            return explicit
        }
        val detected = ImageBitmapIO.outputFormatForDetectedMime(detectedMimeType)
        return detected.takeIf { it in permitted } ?: permitted.first()
    }

    fun conversionOptions(
        saveOptions: EditorSaveOptions,
        detectedMimeType: String,
        allowedOutputMimeTypes: Set<String>,
        targetSize: ImagePixelSize,
        sdkInt: Int,
        preservableExif: PreservedExifMetadata = PreservedExifMetadata.EMPTY,
    ): ImageConversionOptions {
        val outputFormat = effectiveFormat(
            selection = saveOptions.format,
            detectedMimeType = detectedMimeType,
            allowedOutputMimeTypes = allowedOutputMimeTypes,
            overwrite = saveOptions.overwrite,
        )
        return ImageConversionOptions(
            format = outputFormat,
            quality = saveOptions.quality,
            targetSize = targetSize,
            webpLossless = ImageOutputEncodingPolicy.usesWebpLossless(
                format = outputFormat,
                requested = saveOptions.webpLossless,
                sdkInt = sdkInt,
            ),
            preservedExifMetadata = preservableExif.select(capture = saveOptions.keepExif, location = saveOptions.keepLocation)
                .takeIf { !it.isEmpty },
        )
    }
}
