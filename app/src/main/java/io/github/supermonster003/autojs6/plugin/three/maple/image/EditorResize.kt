package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.content.Context

/**
 * Draft of the editor's resize dialog (ROADMAP 8.1, D11): the numbers stay text while they are typed. Only the
 * modes that make sense inside the editor are offered (see [EditorResizePolicy.MODES]).
 */
internal data class EditorResizeDraft(
    val mode: ImageResizeMode,
    val percentage: String,
    val longEdge: String,
    val width: String,
    val height: String,
    val lockAspectRatio: Boolean = true,
)

/**
 * Android-free rules of the editor resize: the converter's sizing ([ImageConversionSizing]) with the editor's pixel
 * budget on top, so the editor refuses what it could not keep in memory with the same message the converter shows.
 */
internal object EditorResizePolicy {

    val MODES = listOf(ImageResizeMode.PERCENTAGE, ImageResizeMode.LONG_EDGE, ImageResizeMode.CUSTOM)

    const val DEFAULT_PERCENTAGE = "50"

    fun defaultDraft(source: ImagePixelSize): EditorResizeDraft = EditorResizeDraft(
        mode = ImageResizeMode.PERCENTAGE,
        percentage = DEFAULT_PERCENTAGE,
        longEdge = minOf(ImageConversionSizing.DEFAULT_LONG_EDGE, maxOf(source.width, source.height)).toString(),
        width = source.width.toString(),
        height = source.height.toString(),
    )

    /** The new size, or why there is none; larger than [maxPixels] is [ImageResizeError.MEMORY_BUDGET_EXCEEDED]. */
    fun resolve(source: ImagePixelSize, draft: EditorResizeDraft, maxPixels: Long): ImageResizeResult {
        val request = when (draft.mode) {
            ImageResizeMode.LONG_EDGE -> ImageResizeRequest(ImageResizeMode.LONG_EDGE, longEdge = draft.longEdge.trim().toIntOrNull())
            ImageResizeMode.CUSTOM -> ImageResizeRequest(
                ImageResizeMode.CUSTOM,
                width = draft.width.trim().toIntOrNull(),
                height = draft.height.trim().toIntOrNull(),
            )
            else -> ImageResizeRequest(ImageResizeMode.PERCENTAGE, percentage = draft.percentage.trim().toIntOrNull())
        }
        val resolved = ImageConversionSizing.resolve(source, request)
        val size = resolved.size ?: return resolved
        return if (size.pixelCount > maxPixels) ImageResizeResult(error = ImageResizeError.MEMORY_BUDGET_EXCEEDED) else resolved
    }

    /** [draft] with a new width; a locked aspect ratio follows with the height. */
    fun withWidth(source: ImagePixelSize, draft: EditorResizeDraft, text: String): EditorResizeDraft {
        val height = if (draft.lockAspectRatio) text.trim().toIntOrNull()?.let { ImageConversionSizing.heightForWidth(source, it) } else null
        return draft.copy(width = text, height = height?.toString() ?: draft.height)
    }

    /** [draft] with a new height; a locked aspect ratio follows with the width. */
    fun withHeight(source: ImagePixelSize, draft: EditorResizeDraft, text: String): EditorResizeDraft {
        val width = if (draft.lockAspectRatio) text.trim().toIntOrNull()?.let { ImageConversionSizing.widthForHeight(source, it) } else null
        return draft.copy(height = text, width = width?.toString() ?: draft.width)
    }

    /** Locking the ratio again recomputes the height from the width. */
    fun withLock(source: ImagePixelSize, draft: EditorResizeDraft, locked: Boolean): EditorResizeDraft {
        val locking = draft.copy(lockAspectRatio = locked)
        return if (locked) withWidth(source, locking, locking.width) else locking
    }
}

/** The message for a resize error, the same wording in the converter, the batch conversion and the editor. */
internal fun Context.resizeErrorText(error: ImageResizeError): String = when (error) {
    ImageResizeError.INVALID_SOURCE -> getString(R.string.error_image_conversion_invalid_source)
    ImageResizeError.INVALID_PERCENTAGE -> getString(
        R.string.error_image_conversion_invalid_percentage,
        ImageConversionSizing.MIN_PERCENTAGE,
        ImageConversionSizing.MAX_PERCENTAGE,
    )
    ImageResizeError.INVALID_LONG_EDGE -> getString(R.string.error_image_conversion_invalid_long_edge, ImageConversionSizing.MAX_DIMENSION)
    ImageResizeError.INVALID_WIDTH -> getString(R.string.error_image_conversion_invalid_width)
    ImageResizeError.INVALID_HEIGHT -> getString(R.string.error_image_conversion_invalid_height)
    ImageResizeError.DIMENSION_TOO_LARGE -> getString(R.string.error_image_conversion_dimension_too_large, ImageConversionSizing.MAX_DIMENSION)
    ImageResizeError.PIXEL_COUNT_TOO_LARGE -> getString(R.string.error_image_conversion_pixel_count_too_large, ImageConversionSizing.MAX_PIXEL_COUNT)
    ImageResizeError.MEMORY_BUDGET_EXCEEDED -> getString(R.string.error_image_conversion_memory_budget_exceeded)
}
