package io.github.supermonster003.autojs6.plugin.three.maple.image

import kotlin.math.roundToInt
import kotlin.math.roundToLong

internal enum class ImageOutputFormat(
    val displayName: String,
    val extension: String,
    val mimeType: String,
    val supportsQuality: Boolean,
) {
    JPEG("JPEG", "jpg", "image/jpeg", true),
    PNG("PNG", "png", "image/png", false),
    WEBP("WebP", "webp", "image/webp", true),
}
internal enum class ImageResizeMode {
    ORIGINAL,
    PERCENTAGE,
    CUSTOM,
    LONG_EDGE,
}

internal data class ImagePixelSize(
    val width: Int,
    val height: Int,
) {
    val pixelCount: Long get() = width.toLong() * height.toLong()
}

internal data class ImageResizeRequest(
    val mode: ImageResizeMode,
    val percentage: Int? = null,
    val width: Int? = null,
    val height: Int? = null,
    val longEdge: Int? = null,
)

internal enum class ImageResizeError {
    INVALID_SOURCE,
    INVALID_PERCENTAGE,
    INVALID_WIDTH,
    INVALID_HEIGHT,
    INVALID_LONG_EDGE,
    DIMENSION_TOO_LARGE,
    PIXEL_COUNT_TOO_LARGE,
    MEMORY_BUDGET_EXCEEDED,
}

internal data class ImageResizeResult(
    val size: ImagePixelSize? = null,
    val error: ImageResizeError? = null,
) {
    val isValid: Boolean get() = size != null && error == null
}

internal data class ImageConversionOptions(
    val format: ImageOutputFormat,
    val quality: Int = DEFAULT_QUALITY,
    val targetSize: ImagePixelSize,
    val jpegBackgroundColor: Int = DEFAULT_JPEG_BACKGROUND_COLOR,
    val webpLossless: Boolean = false,
    val pngPaletteColorCountHint: Int? = null,
    val targetFileSizeBytes: Long? = null,
    val preservedExifMetadata: PreservedExifMetadata? = null,
) {
    init {
        require(quality in MIN_QUALITY..MAX_QUALITY) { "Quality must be between $MIN_QUALITY and $MAX_QUALITY" }
        require(!webpLossless || format == ImageOutputFormat.WEBP) {
            "Lossless WebP can only be enabled for WebP output"
        }
        require(pngPaletteColorCountHint == null || format == ImageOutputFormat.PNG) {
            "A PNG palette estimate can only be used for PNG output"
        }
        require(pngPaletteColorCountHint == null || pngPaletteColorCountHint in 1..PngPalettePolicy.MAX_COLOR_COUNT) {
            "PNG palette estimate must contain between 1 and ${PngPalettePolicy.MAX_COLOR_COUNT} colors"
        }
        require(targetFileSizeBytes == null || targetFileSizeBytes > 0L) {
            "Target file size must be positive"
        }
        require(
            targetFileSizeBytes == null ||
                format == ImageOutputFormat.JPEG ||
                format == ImageOutputFormat.WEBP && !webpLossless,
        ) {
            "Target file size is available only for JPEG and lossy WebP output"
        }
    }

    companion object {
        const val MIN_QUALITY = 1
        const val MAX_QUALITY = 100
        const val DEFAULT_QUALITY = 92
        const val DEFAULT_JPEG_BACKGROUND_COLOR = -0x1
    }
}

internal object ImageOutputEncodingPolicy {

    const val WEBP_LOSSLESS_MIN_SDK = 30

    fun isWebpLosslessAvailable(sdkInt: Int): Boolean = sdkInt >= WEBP_LOSSLESS_MIN_SDK

    fun usesWebpLossless(
        format: ImageOutputFormat,
        requested: Boolean,
        sdkInt: Int,
    ): Boolean = format == ImageOutputFormat.WEBP && requested && isWebpLosslessAvailable(sdkInt)

    fun qualityEnabled(
        format: ImageOutputFormat,
        webpLosslessRequested: Boolean,
        sdkInt: Int,
        targetFileSizeRequested: Boolean = false,
    ): Boolean = format.supportsQuality &&
        !usesWebpLossless(format, webpLosslessRequested, sdkInt) &&
        !targetFileSizeRequested

    fun supportsTargetFileSize(
        format: ImageOutputFormat,
        webpLosslessRequested: Boolean,
        sdkInt: Int,
    ): Boolean = when (format) {
        ImageOutputFormat.JPEG -> true
        ImageOutputFormat.PNG -> false
        ImageOutputFormat.WEBP -> !usesWebpLossless(format, webpLosslessRequested, sdkInt)
    }

    fun maximumLossyQuality(format: ImageOutputFormat, sdkInt: Int): Int =
        if (format == ImageOutputFormat.WEBP && sdkInt < WEBP_LOSSLESS_MIN_SDK) {
            ImageConversionOptions.MAX_QUALITY - 1
        } else {
            ImageConversionOptions.MAX_QUALITY
        }
}

internal object ImageConversionSizing {

    const val MIN_PERCENTAGE = 1
    const val MAX_PERCENTAGE = 1000
    const val MAX_DIMENSION = 16_384
    const val MAX_PIXEL_COUNT = 40_000_000L
    const val DEFAULT_LONG_EDGE = 1920

    fun resolve(
        source: ImagePixelSize,
        request: ImageResizeRequest,
        maxPixelCount: Long = MAX_PIXEL_COUNT,
    ): ImageResizeResult {
        if (source.width <= 0 || source.height <= 0) {
            return ImageResizeResult(error = ImageResizeError.INVALID_SOURCE)
        }
        val target = when (request.mode) {
            ImageResizeMode.ORIGINAL -> source
            ImageResizeMode.PERCENTAGE -> {
                val percentage = request.percentage
                    ?: return ImageResizeResult(error = ImageResizeError.INVALID_PERCENTAGE)
                if (percentage !in MIN_PERCENTAGE..MAX_PERCENTAGE) {
                    return ImageResizeResult(error = ImageResizeError.INVALID_PERCENTAGE)
                }
                ImagePixelSize(
                    width = scaledDimension(source.width, percentage),
                    height = scaledDimension(source.height, percentage),
                )
            }
            ImageResizeMode.CUSTOM -> {
                val width = request.width
                    ?: return ImageResizeResult(error = ImageResizeError.INVALID_WIDTH)
                val height = request.height
                    ?: return ImageResizeResult(error = ImageResizeError.INVALID_HEIGHT)
                if (width <= 0) return ImageResizeResult(error = ImageResizeError.INVALID_WIDTH)
                if (height <= 0) return ImageResizeResult(error = ImageResizeError.INVALID_HEIGHT)
                ImagePixelSize(width, height)
            }
            ImageResizeMode.LONG_EDGE -> {
                val longEdge = request.longEdge
                    ?: return ImageResizeResult(error = ImageResizeError.INVALID_LONG_EDGE)
                if (longEdge !in 1..MAX_DIMENSION) {
                    return ImageResizeResult(error = ImageResizeError.INVALID_LONG_EDGE)
                }
                val sourceLongEdge = maxOf(source.width, source.height)
                when {
                    sourceLongEdge <= longEdge -> source
                    source.width >= source.height -> ImagePixelSize(
                        width = longEdge,
                        height = requireNotNull(proportionalDimension(longEdge, source.height, source.width)),
                    )
                    else -> ImagePixelSize(
                        width = requireNotNull(proportionalDimension(longEdge, source.width, source.height)),
                        height = longEdge,
                    )
                }
            }
        }
        if (target.width > MAX_DIMENSION || target.height > MAX_DIMENSION) {
            return ImageResizeResult(error = ImageResizeError.DIMENSION_TOO_LARGE)
        }
        if (target.pixelCount > maxPixelCount) {
            return ImageResizeResult(error = ImageResizeError.PIXEL_COUNT_TOO_LARGE)
        }
        return ImageResizeResult(size = target)
    }

    fun heightForWidth(source: ImagePixelSize, width: Int): Int? {
        if (source.width <= 0 || source.height <= 0 || width <= 0) return null
        return proportionalDimension(width, source.height, source.width)
    }

    fun widthForHeight(source: ImagePixelSize, height: Int): Int? {
        if (source.width <= 0 || source.height <= 0 || height <= 0) return null
        return proportionalDimension(height, source.width, source.height)
    }

    fun estimateEncodedBytes(options: ImageConversionOptions): Long {
        val imageBytes = if (
            options.format == ImageOutputFormat.PNG &&
            options.pngPaletteColorCountHint != null
        ) {
            estimateIndexedPngBytes(options.targetSize, options.pngPaletteColorCountHint)
        } else {
            val bytesPerPixel = when (options.format) {
                ImageOutputFormat.JPEG -> 0.10 + options.quality * 0.006
                ImageOutputFormat.PNG -> 1.75
                ImageOutputFormat.WEBP -> if (options.webpLossless) {
                    WEBP_LOSSLESS_ESTIMATED_BYTES_PER_PIXEL
                } else {
                    0.08 + options.quality * 0.0045
                }
            }
            (options.targetSize.pixelCount * bytesPerPixel)
                .roundToLongSafely()
                .coerceAtLeast(1L)
        }
        val metadataBytes = options.preservedExifMetadata
            ?.estimatedEncodedOverhead(options.format)
            ?: 0L
        return imageBytes.saturatedAdd(metadataBytes)
    }

    fun pngPaletteColorCountHint(
        format: ImageOutputFormat,
        sourceSize: ImagePixelSize,
        targetSize: ImagePixelSize,
        sourcePaletteColorCount: Int?,
    ): Int? = sourcePaletteColorCount?.takeIf { colorCount ->
        format == ImageOutputFormat.PNG &&
            sourceSize == targetSize &&
            colorCount in 1..PngPalettePolicy.MAX_COLOR_COUNT
    }

    private fun estimateIndexedPngBytes(size: ImagePixelSize, colorCount: Int): Long {
        val bitDepth = PngPalettePolicy.bitDepthForColorCount(colorCount)
        val packedRowBytes = (size.width.toLong() * bitDepth + BITS_PER_BYTE - 1L) / BITS_PER_BYTE
        val filteredBytes = (packedRowBytes + PNG_FILTER_BYTE_COUNT) * size.height.toLong()
        val estimatedDeflateOverhead = filteredBytes / DEFLATE_OVERHEAD_DIVISOR + ZLIB_OVERHEAD_BYTES
        val idatChunks = (filteredBytes + IDAT_CHUNK_DATA_SIZE - 1L) / IDAT_CHUNK_DATA_SIZE
        val paletteAndTransparencyBytes = colorCount.toLong() * PALETTE_ESTIMATED_BYTES_PER_COLOR
        return (
            filteredBytes +
                estimatedDeflateOverhead +
                idatChunks * PNG_CHUNK_OVERHEAD_BYTES +
                paletteAndTransparencyBytes +
                PNG_FIXED_OVERHEAD_BYTES
            ).coerceAtLeast(1L)
    }

    private fun scaledDimension(source: Int, percentage: Int): Int {
        val scaled = (source.toLong() * percentage + 50L) / 100L
        return scaled.coerceIn(1L, Int.MAX_VALUE.toLong()).toInt()
    }

    private fun proportionalDimension(value: Int, numerator: Int, denominator: Int): Int? {
        val result = value.toDouble() * numerator.toDouble() / denominator.toDouble()
        if (!result.isFinite() || result > Int.MAX_VALUE) return null
        return result.roundToInt().coerceAtLeast(1)
    }

    private fun Double.roundToLongSafely(): Long = when {
        !isFinite() || this >= Long.MAX_VALUE -> Long.MAX_VALUE
        this <= 0.0 -> 0L
        else -> this.roundToLong()
    }

    private fun Long.saturatedAdd(value: Long): Long =
        if (this > Long.MAX_VALUE - value) Long.MAX_VALUE else this + value

    private const val WEBP_LOSSLESS_ESTIMATED_BYTES_PER_PIXEL = 1.35
    private const val BITS_PER_BYTE = 8L
    private const val PNG_FILTER_BYTE_COUNT = 1L
    private const val DEFLATE_OVERHEAD_DIVISOR = 1000L
    private const val ZLIB_OVERHEAD_BYTES = 16L
    private const val IDAT_CHUNK_DATA_SIZE = 64L * 1024L
    private const val PNG_CHUNK_OVERHEAD_BYTES = 12L
    private const val PALETTE_ESTIMATED_BYTES_PER_COLOR = 4L
    private const val PNG_FIXED_OVERHEAD_BYTES = 69L
}
