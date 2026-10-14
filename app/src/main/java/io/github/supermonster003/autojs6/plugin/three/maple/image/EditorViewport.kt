package io.github.supermonster003.autojs6.plugin.three.maple.image

import kotlin.math.max
import kotlin.math.min

/**
 * Where the editor canvas draws the image (ROADMAP 8.1): fitted and centered at zoom 1, the user's zoom and pan on
 * top. The image never leaves a gap at an edge it can cover, and a side smaller than the view stays centered.
 * Android-free, so the mapping that tools and the color picker rely on is tested on the JVM.
 */
internal class EditorViewport {

    var viewWidth = 0f
        private set
    var viewHeight = 0f
        private set
    var imageWidth = 0
        private set
    var imageHeight = 0
        private set

    /** 1 is the fitted size; at most [maxZoom]. */
    var zoom = 1f
        private set

    /** Offset of the image center from the view center, in view pixels. */
    private var panX = 0f
    private var panY = 0f

    val fitScale: Float
        get() = if (viewWidth <= 0f || viewHeight <= 0f || imageWidth <= 0 || imageHeight <= 0) 0f
        else min(viewWidth / imageWidth, viewHeight / imageHeight)

    /** View pixels per image pixel. */
    val scale: Float get() = fitScale * zoom

    /** Up to [MAX_VIEW_PIXELS_PER_IMAGE_PIXEL] view pixels per image pixel, and never less than [MIN_MAX_ZOOM]. */
    val maxZoom: Float get() = fitScale.takeIf { it > 0f }?.let { max(MIN_MAX_ZOOM, MAX_VIEW_PIXELS_PER_IMAGE_PIXEL / it) } ?: 1f

    val isZoomed: Boolean get() = zoom > 1f + ZOOM_EPSILON

    val left: Float get() = (viewWidth - imageWidth * scale) / 2f + panX
    val top: Float get() = (viewHeight - imageHeight * scale) / 2f + panY
    val right: Float get() = left + imageWidth * scale
    val bottom: Float get() = top + imageHeight * scale

    fun setView(width: Float, height: Float) {
        viewWidth = width
        viewHeight = height
        clamp()
    }

    /** A new image; the zoom and pan stay only when its size did not change (an adjustment, not a crop). */
    fun setImage(width: Int, height: Int) {
        val sameSize = width == imageWidth && height == imageHeight
        imageWidth = width
        imageHeight = height
        if (!sameSize) reset() else clamp()
    }

    fun reset() {
        zoom = 1f
        panX = 0f
        panY = 0f
    }

    /** Zooms by [factor] keeping the image point under ([focusX], [focusY]) where it is. */
    fun zoomBy(factor: Float, focusX: Float, focusY: Float) {
        if (fitScale <= 0f || factor <= 0f || factor.isNaN()) return
        val imageX = toImageX(focusX)
        val imageY = toImageY(focusY)
        zoom = (zoom * factor).coerceIn(1f, maxZoom)
        val scale = scale
        panX = focusX - imageX * scale - (viewWidth - imageWidth * scale) / 2f
        panY = focusY - imageY * scale - (viewHeight - imageHeight * scale) / 2f
        clamp()
    }

    fun panBy(deltaX: Float, deltaY: Float) {
        panX += deltaX
        panY += deltaY
        clamp()
    }

    /** Double tap: back to the fitted size when zoomed, otherwise [DOUBLE_TAP_ZOOM] around the tapped point. */
    fun toggleZoom(focusX: Float, focusY: Float) {
        if (isZoomed) reset() else zoomBy(min(DOUBLE_TAP_ZOOM, maxZoom), focusX, focusY)
    }

    fun toImageX(viewX: Float): Float = (viewX - left) / scale

    fun toImageY(viewY: Float): Float = (viewY - top) / scale

    fun toViewX(imageX: Float): Float = left + imageX * scale

    fun toViewY(imageY: Float): Float = top + imageY * scale

    private fun clamp() {
        if (fitScale <= 0f) {
            panX = 0f
            panY = 0f
            return
        }
        zoom = zoom.coerceIn(1f, maxZoom)
        val limitX = max(0f, (imageWidth * scale - viewWidth) / 2f)
        val limitY = max(0f, (imageHeight * scale - viewHeight) / 2f)
        panX = panX.coerceIn(-limitX, limitX)
        panY = panY.coerceIn(-limitY, limitY)
    }

    companion object {
        const val MIN_MAX_ZOOM = 4f
        const val MAX_VIEW_PIXELS_PER_IMAGE_PIXEL = 8f
        const val DOUBLE_TAP_ZOOM = 2.5f
        private const val ZOOM_EPSILON = 0.001f
    }
}
