package io.github.supermonster003.autojs6.plugin.three.maple.image

import java.util.Locale
import kotlin.math.ceil
import kotlin.math.floor

internal data class ImageBitmapLimits(
    val maximumBitmapWidth: Int,
    val maximumBitmapHeight: Int,
    val fullDecodeBudgetBytes: Long,
    val tileCacheBudgetBytes: Long,
)

internal data class ImageTileKey(
    val sampleSize: Int,
    val column: Int,
    val row: Int,
)

internal data class ImageTileSpec(
    val key: ImageTileKey,
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int,
)

/** Pure thresholds and level-of-detail planning for bounded region decoding. */
internal object ImageLargeImagePolicy {

    const val DECODED_TILE_EDGE = 512
    const val PREVIEW_MAX_EDGE = 2_048
    const val PREVIEW_MAX_PIXELS = 4_194_304L
    const val FALLBACK_MAX_BITMAP_EDGE = 4_096

    private const val BYTES_PER_ARGB_PIXEL = 4L
    private const val MEBIBYTE = 1_048_576L
    private const val MIN_FULL_DECODE_BUDGET = 32L * MEBIBYTE
    private const val MAX_FULL_DECODE_BUDGET = 64L * MEBIBYTE
    private const val MIN_TILE_CACHE_BUDGET = 12L * MEBIBYTE
    private const val MAX_TILE_CACHE_BUDGET = 32L * MEBIBYTE
    private const val MAX_SAMPLE_SIZE = 1_024
    private const val TARGET_DECODED_PIXEL_SCALE = 1.1f

    fun runtimeLimits(
        memoryClassMebibytes: Int,
        observedMaximumBitmapWidth: Int,
        observedMaximumBitmapHeight: Int,
    ): ImageBitmapLimits {
        val memoryBytes = memoryClassMebibytes.coerceAtLeast(1).toLong() * MEBIBYTE
        return ImageBitmapLimits(
            maximumBitmapWidth = observedMaximumBitmapWidth
                .takeIf { it > 0 }
                ?: FALLBACK_MAX_BITMAP_EDGE,
            maximumBitmapHeight = observedMaximumBitmapHeight
                .takeIf { it > 0 }
                ?: FALLBACK_MAX_BITMAP_EDGE,
            fullDecodeBudgetBytes = (memoryBytes / 8L).coerceIn(
                MIN_FULL_DECODE_BUDGET,
                MAX_FULL_DECODE_BUDGET,
            ),
            tileCacheBudgetBytes = (memoryBytes / 16L).coerceIn(
                MIN_TILE_CACHE_BUDGET,
                MAX_TILE_CACHE_BUDGET,
            ),
        )
    }

    fun shouldUseTiling(
        width: Int,
        height: Int,
        mimeType: String,
        sdkInt: Int,
        limits: ImageBitmapLimits,
    ): Boolean {
        if (width <= 0 || height <= 0 || sdkInt < 0) return false
        if (!supportsRegionDecoding(mimeType, sdkInt)) return false
        val exceedsTextureLimit = width > limits.maximumBitmapWidth ||
            height > limits.maximumBitmapHeight
        val exceedsMemoryBudget = estimatedArgbBytes(width, height) > limits.fullDecodeBudgetBytes
        return exceedsTextureLimit || exceedsMemoryBudget
    }

    fun supportsRegionDecoding(mimeType: String, sdkInt: Int): Boolean =
        when (mimeType.lowercase(Locale.ROOT)) {
            "image/jpeg", "image/jpg", "image/pjpeg", "image/png" -> true
            "image/heic", "image/heif" -> sdkInt >= 28
            // Animated WebP and HEIF sequences must retain the normal animated-drawable path.
            else -> false
        }

    fun estimatedArgbBytes(width: Int, height: Int): Long {
        if (width <= 0 || height <= 0) return 0L
        val pixels = width.toLong() * height.toLong()
        return if (pixels > Long.MAX_VALUE / BYTES_PER_ARGB_PIXEL) {
            Long.MAX_VALUE
        } else {
            pixels * BYTES_PER_ARGB_PIXEL
        }
    }

    fun previewSampleSize(width: Int, height: Int): Int {
        if (width <= 0 || height <= 0) return 1
        var sampleSize = 1
        while (sampleSize < MAX_SAMPLE_SIZE) {
            val decodedWidth = ceil(width.toDouble() / sampleSize.toDouble()).toLong()
            val decodedHeight = ceil(height.toDouble() / sampleSize.toDouble()).toLong()
            if (
                decodedWidth <= PREVIEW_MAX_EDGE &&
                decodedHeight <= PREVIEW_MAX_EDGE &&
                decodedWidth * decodedHeight <= PREVIEW_MAX_PIXELS
            ) {
                break
            }
            sampleSize *= 2
        }
        return sampleSize
    }

    fun tileSampleSize(effectiveScale: Float): Int {
        if (!effectiveScale.isFinite() || effectiveScale <= 0f) return 1
        var sampleSize = 1
        while (
            sampleSize < MAX_SAMPLE_SIZE &&
            effectiveScale * (sampleSize * 2) <= TARGET_DECODED_PIXEL_SCALE
        ) {
            sampleSize *= 2
        }
        return sampleSize
    }

    fun visibleTiles(
        rawWidth: Int,
        rawHeight: Int,
        visibleLeft: Float,
        visibleTop: Float,
        visibleRight: Float,
        visibleBottom: Float,
        sampleSize: Int,
    ): List<ImageTileSpec> {
        if (
            rawWidth <= 0 ||
            rawHeight <= 0 ||
            sampleSize <= 0 ||
            sampleSize and (sampleSize - 1) != 0 ||
            !visibleLeft.isFinite() ||
            !visibleTop.isFinite() ||
            !visibleRight.isFinite() ||
            !visibleBottom.isFinite()
        ) {
            return emptyList()
        }
        val left = floor(visibleLeft.coerceIn(0f, rawWidth.toFloat())).toInt()
        val top = floor(visibleTop.coerceIn(0f, rawHeight.toFloat())).toInt()
        val right = ceil(visibleRight.coerceIn(0f, rawWidth.toFloat())).toInt()
        val bottom = ceil(visibleBottom.coerceIn(0f, rawHeight.toFloat())).toInt()
        if (right <= left || bottom <= top) return emptyList()

        val sourceTileEdge = DECODED_TILE_EDGE * sampleSize
        val firstColumn = left / sourceTileEdge
        val lastColumn = (right - 1) / sourceTileEdge
        val firstRow = top / sourceTileEdge
        val lastRow = (bottom - 1) / sourceTileEdge
        val centerX = (left.toLong() + right.toLong()) / 2L
        val centerY = (top.toLong() + bottom.toLong()) / 2L

        return buildList {
            for (row in firstRow..lastRow) {
                for (column in firstColumn..lastColumn) {
                    val tileLeft = column * sourceTileEdge
                    val tileTop = row * sourceTileEdge
                    add(
                        ImageTileSpec(
                            key = ImageTileKey(sampleSize, column, row),
                            left = tileLeft,
                            top = tileTop,
                            right = (tileLeft.toLong() + sourceTileEdge.toLong())
                                .coerceAtMost(rawWidth.toLong())
                                .toInt(),
                            bottom = (tileTop.toLong() + sourceTileEdge.toLong())
                                .coerceAtMost(rawHeight.toLong())
                                .toInt(),
                        ),
                    )
                }
            }
        }.sortedBy { tile ->
            val tileCenterX = (tile.left.toLong() + tile.right.toLong()) / 2L
            val tileCenterY = (tile.top.toLong() + tile.bottom.toLong()) / 2L
            val deltaX = tileCenterX - centerX
            val deltaY = tileCenterY - centerY
            deltaX * deltaX + deltaY * deltaY
        }
    }
}
