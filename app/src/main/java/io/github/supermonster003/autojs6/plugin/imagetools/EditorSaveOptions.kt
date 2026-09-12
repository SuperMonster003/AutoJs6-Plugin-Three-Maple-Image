package io.github.supermonster003.autojs6.plugin.imagetools

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

    fun effectiveFormat(
        selection: EditorSaveFormat,
        detectedMimeType: String,
        allowedOutputMimeTypes: Set<String>,
    ): ImageOutputFormat {
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
    ): ImageConversionOptions {
        val outputFormat = effectiveFormat(
            selection = saveOptions.format,
            detectedMimeType = detectedMimeType,
            allowedOutputMimeTypes = allowedOutputMimeTypes,
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
        )
    }
}
