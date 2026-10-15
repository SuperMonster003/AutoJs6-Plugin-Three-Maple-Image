package io.github.supermonster003.autojs6.plugin.three.maple.image

/** Everything the converter page derives from the current [ConversionDialogDraft]; `options` is non-null only when the form is valid. */
internal data class ConverterFormEvaluation(
    val format: ImageOutputFormat,
    val resizeMode: ImageResizeMode,
    val losslessVisible: Boolean,
    val losslessActive: Boolean,
    val targetFileSizeVisible: Boolean,
    val targetFileSizeActive: Boolean,
    val targetFileSizeBytes: Long?,
    val qualityEnabled: Boolean,
    val jpegBackgroundVisible: Boolean,
    val outputSize: ImagePixelSize?,
    val resizeError: ImageResizeError?,
    val estimatedBytes: Long?,
    val options: ImageConversionOptions?,
    /** The output replaces the original, in its own format (ROADMAP 8.3). */
    val overwrite: Boolean = false,
) {
    val convertEnabled: Boolean get() = options != null
    val targetFileSizeInvalid: Boolean get() = targetFileSizeActive && targetFileSizeBytes == null
}

/**
 * Android-free form logic of the converter: default draft, derived control states, validation, estimate and the
 * final [ImageConversionOptions]. The platform level and the memory budget check are injected so the rules can be
 * tested on the JVM; the Activity only renders the result and forwards edits.
 */
internal class ConverterFormEvaluator(
    private val sourceInfo: ImageBitmapIO.SourceInfo,
    private val formats: List<ImageOutputFormat>,
    private val maxOutputBytes: Long,
    private val sdkInt: Int,
    private val withinMemoryBudget: (ImagePixelSize) -> Boolean,
    /** The request lets the original be replaced (ROADMAP 8.3). */
    canOverwrite: Boolean = false,
) {
    init {
        require(formats.isNotEmpty()) { "At least one output format is required" }
    }

    val formatList: List<ImageOutputFormat> get() = formats

    /**
     * The format an overwrite keeps: the original's own, or null when it cannot be written (GIF, HEIF...) or the
     * request does not allow replacing the original; then "Overwrite the original" is not offered.
     */
    val overwriteFormat: ImageOutputFormat? = if (canOverwrite) formats.firstOrNull { it.mimeType == sourceInfo.mimeType } else null

    fun defaultDraft(defaultPercentage: String = "100"): ConversionDialogDraft {
        val size = sourceInfo.displaySize
        return ConversionDialogDraft(
            formatMimeType = (formats.firstOrNull { it == ImageOutputFormat.PNG } ?: formats.first()).mimeType,
            quality = ImageConversionOptions.DEFAULT_QUALITY,
            resizeMode = ImageResizeMode.ORIGINAL,
            percentage = defaultPercentage,
            width = size.width.toString(),
            height = size.height.toString(),
            lockAspectRatio = false,
            jpegBackgroundBlack = false,
            webpLossless = false,
            targetFileSizeEnabled = false,
            targetFileSizeKibibytes = minOf(
                ImageTargetFileSizePolicy.DEFAULT_TARGET_KIBIBYTES,
                ImageTargetFileSizePolicy.maxTargetKibibytes(maxOutputBytes).coerceAtLeast(1L),
            ).toString(),
            longEdge = minOf(ImageConversionSizing.DEFAULT_LONG_EDGE, maxOf(size.width, size.height)).toString(),
            preserveExifMetadata = false,
        )
    }

    fun format(draft: ConversionDialogDraft): ImageOutputFormat =
        overwriteFormat?.takeIf { draft.overwrite } ?: formats.firstOrNull { it.mimeType == draft.formatMimeType } ?: formats.first()

    fun heightForWidth(width: Int?): Int? = width?.let { ImageConversionSizing.heightForWidth(sourceInfo.displaySize, it) }

    fun widthForHeight(height: Int?): Int? = height?.let { ImageConversionSizing.widthForHeight(sourceInfo.displaySize, it) }

    fun maxTargetKibibytes(): Long = ImageTargetFileSizePolicy.maxTargetKibibytes(maxOutputBytes)

    fun evaluate(draft: ConversionDialogDraft): ConverterFormEvaluation {
        val format = format(draft)
        val losslessVisible = format == ImageOutputFormat.WEBP &&
            ImageOutputEncodingPolicy.isWebpLosslessAvailable(sdkInt)
        val losslessActive = ImageOutputEncodingPolicy.usesWebpLossless(format, draft.webpLossless, sdkInt)
        val targetFileSizeVisible = ImageTargetFileSizePolicy.isAvailable(
            format = format,
            webpLosslessRequested = draft.webpLossless,
            sdkInt = sdkInt,
            maxOutputBytes = maxOutputBytes,
        )
        val targetFileSizeActive = targetFileSizeVisible && draft.targetFileSizeEnabled
        val qualityEnabled = ImageOutputEncodingPolicy.qualityEnabled(
            format = format,
            webpLosslessRequested = draft.webpLossless,
            sdkInt = sdkInt,
            targetFileSizeRequested = targetFileSizeActive,
        )
        val resize = resolveResize(draft)
        val targetFileSizeBytes = if (targetFileSizeActive) {
            ImageTargetFileSizePolicy.resolveTargetBytes(draft.targetFileSizeKibibytes.trim().toLongOrNull(), maxOutputBytes)
        } else {
            null
        }
        val size = resize.size
        val valid = size != null && resize.error == null && !(targetFileSizeActive && targetFileSizeBytes == null)
        val options = if (valid && size != null) createOptions(draft, format, size, targetFileSizeBytes) else null
        val estimatedBytes = if (options != null && !targetFileSizeActive) {
            ImageConversionSizing.estimateEncodedBytes(options)
        } else {
            null
        }
        return ConverterFormEvaluation(
            format = format,
            resizeMode = draft.resizeMode,
            losslessVisible = losslessVisible,
            losslessActive = losslessActive,
            targetFileSizeVisible = targetFileSizeVisible,
            targetFileSizeActive = targetFileSizeActive,
            targetFileSizeBytes = targetFileSizeBytes,
            qualityEnabled = qualityEnabled,
            jpegBackgroundVisible = format == ImageOutputFormat.JPEG,
            outputSize = size,
            resizeError = resize.error,
            estimatedBytes = estimatedBytes,
            options = options,
            overwrite = draft.overwrite && overwriteFormat != null,
        )
    }

    private fun resolveResize(draft: ConversionDialogDraft): ImageResizeResult {
        val request = when (draft.resizeMode) {
            ImageResizeMode.ORIGINAL -> ImageResizeRequest(ImageResizeMode.ORIGINAL)
            ImageResizeMode.PERCENTAGE -> ImageResizeRequest(
                mode = ImageResizeMode.PERCENTAGE,
                percentage = draft.percentage.trim().toIntOrNull(),
            )
            ImageResizeMode.LONG_EDGE -> ImageResizeRequest(
                mode = ImageResizeMode.LONG_EDGE,
                longEdge = draft.longEdge.trim().toIntOrNull(),
            )
            ImageResizeMode.CUSTOM -> ImageResizeRequest(
                mode = ImageResizeMode.CUSTOM,
                width = draft.width.trim().toIntOrNull(),
                height = draft.height.trim().toIntOrNull(),
            )
        }
        val resolved = ImageConversionSizing.resolve(sourceInfo.displaySize, request)
        val size = resolved.size
        return if (size != null && !withinMemoryBudget(size)) {
            ImageResizeResult(error = ImageResizeError.MEMORY_BUDGET_EXCEEDED)
        } else {
            resolved
        }
    }

    private fun createOptions(
        draft: ConversionDialogDraft,
        format: ImageOutputFormat,
        targetSize: ImagePixelSize,
        targetFileSizeBytes: Long?,
    ) = ImageConversionOptions(
        format = format,
        quality = draft.quality.coerceIn(ImageConversionOptions.MIN_QUALITY, ImageConversionOptions.MAX_QUALITY),
        targetSize = targetSize,
        jpegBackgroundColor = if (draft.jpegBackgroundBlack) JPEG_BACKGROUND_BLACK else ImageConversionOptions.DEFAULT_JPEG_BACKGROUND_COLOR,
        webpLossless = ImageOutputEncodingPolicy.usesWebpLossless(format, draft.webpLossless, sdkInt),
        pngPaletteColorCountHint = ImageConversionSizing.pngPaletteColorCountHint(
            format = format,
            sourceSize = sourceInfo.displaySize,
            targetSize = targetSize,
            sourcePaletteColorCount = sourceInfo.pngPaletteColorCount,
        ),
        targetFileSizeBytes = targetFileSizeBytes,
        preservedExifMetadata = sourceInfo.preservableExifMetadata
            .select(capture = draft.preserveExifMetadata, location = draft.preserveLocation)
            .takeIf { !it.isEmpty },
    )

    private companion object {
        /** `android.graphics.Color.BLACK` without touching the platform class. */
        const val JPEG_BACKGROUND_BLACK = -0x1000000
    }
}
