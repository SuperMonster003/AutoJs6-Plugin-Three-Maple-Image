package io.github.supermonster003.autojs6.plugin.three.maple.image

/** Pure zoom-state decisions shared by the view and local unit tests; a zoom of 1 is the fitted image. */
internal object ImageZoomState {

    const val MIN_ZOOM = 1f
    const val MAX_ZOOM = 5f
    const val TEN_TIMES_ZOOM = 10f
    const val DOUBLE_TAP_ZOOM = 2.5f
    const val ZOOM_EPSILON = 0.001f

    /** Keeps a huge panorama's "actual pixels" within what a matrix and the tiles handle well. */
    const val CEILING_ZOOM = 64f

    fun doubleTapTarget(currentZoom: Float): Float =
        doubleTapTarget(currentZoom, ViewerDoubleTapZoom.TWO_AND_HALF, actualPixels = null, maximum = MAX_ZOOM)

    /** Zooming in from the fit goes to the chosen level (1:1 falls back to 2.5x for images that fit at 1:1 or less). */
    fun doubleTapTarget(currentZoom: Float, doubleTap: ViewerDoubleTapZoom, actualPixels: Float?, maximum: Float): Float {
        if (currentZoom > MIN_ZOOM + ZOOM_EPSILON) return MIN_ZOOM
        val target = when (doubleTap) {
            ViewerDoubleTapZoom.TWO_AND_HALF -> DOUBLE_TAP_ZOOM
            ViewerDoubleTapZoom.ACTUAL_PIXELS -> actualPixels?.takeIf { it > MIN_ZOOM + ZOOM_EPSILON } ?: DOUBLE_TAP_ZOOM
        }
        return target.coerceAtMost(maximum)
    }

    /** The zoom at which one image pixel covers one screen pixel; null while the fit is unknown. */
    fun actualPixelsZoom(imageWidth: Int, fittedWidth: Float): Float? =
        if (imageWidth > 0 && fittedWidth.isFinite() && fittedWidth > 0f) imageWidth / fittedWidth else null

    /**
     * The largest zoom: the chosen limit ("actual pixels" at least the 2.5x double tap, so small images still zoom),
     * and never less than a 1:1 double tap so that one is not cut short.
     */
    fun maximumZoom(max: ViewerMaxZoom, doubleTap: ViewerDoubleTapZoom, actualPixels: Float?): Float {
        val limit = when (max) {
            ViewerMaxZoom.FIVE -> MAX_ZOOM
            ViewerMaxZoom.TEN -> TEN_TIMES_ZOOM
            ViewerMaxZoom.ACTUAL_PIXELS -> maxOf(actualPixels ?: MAX_ZOOM, DOUBLE_TAP_ZOOM)
        }
        val doubleTapTarget = if (doubleTap == ViewerDoubleTapZoom.ACTUAL_PIXELS) actualPixels ?: MIN_ZOOM else MIN_ZOOM
        return maxOf(limit, doubleTapTarget).coerceAtMost(CEILING_ZOOM)
    }
}

/** The Android-free decisions of the viewer's optional gestures (ROADMAP 9.2). */
internal object ViewerGesturePolicy {

    /** The strip at each side where a tap turns the page while edge taps are on. */
    const val EDGE_FRACTION = 0.2f

    /** The left part of the screen where a vertical drag sets the brightness while that is on. */
    const val BRIGHTNESS_ZONE_FRACTION = 1f / 3f

    /** Dragging over this share of the view's height goes from darkest to brightest. */
    const val BRIGHTNESS_DRAG_FRACTION = 0.6f

    /** The window brightness never drops to 0, which some devices take as "screen off". */
    const val MIN_BRIGHTNESS = 0.01f

    /** -1 for the left edge, 1 for the right edge (the same sides as a swipe), null elsewhere. */
    fun edgeTapDirection(x: Float, width: Int): Int? {
        if (width <= 0) return null
        val edge = width * EDGE_FRACTION
        return when {
            x < edge -> ZoomableImageView.DIRECTION_PREVIOUS
            x > width - edge -> ZoomableImageView.DIRECTION_NEXT
            else -> null
        }
    }

    fun inBrightnessZone(x: Float, width: Int): Boolean = width > 0 && x < width * BRIGHTNESS_ZONE_FRACTION

    /** A drag mostly up or down past the slop; sideways drags stay with the pager. */
    fun isVerticalDrag(distanceX: Float, distanceY: Float, touchSlop: Float): Boolean =
        kotlin.math.abs(distanceY) > touchSlop && kotlin.math.abs(distanceY) > kotlin.math.abs(distanceX) * 1.5f

    /** Dragging up brightens, down dims, from the brightness at the start of the drag. */
    fun brightness(start: Float, distanceY: Float, height: Int): Float {
        if (height <= 0) return start.coerceIn(MIN_BRIGHTNESS, 1f)
        return (start - distanceY / (height * BRIGHTNESS_DRAG_FRACTION)).coerceIn(MIN_BRIGHTNESS, 1f)
    }

    /** A quick drag mostly downward closes the viewer while that is on. */
    fun isSwipeDown(distanceX: Float, distanceY: Float, velocityY: Float, minDistance: Float, minVelocity: Float): Boolean =
        distanceY >= minDistance && velocityY >= minVelocity && distanceY > kotlin.math.abs(distanceX) * 1.5f
}
