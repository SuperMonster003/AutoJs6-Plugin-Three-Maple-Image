package io.github.supermonster003.autojs6.plugin.imageviewer

/** Pure zoom-state decisions shared by the view and local unit tests. */
internal object ImageZoomState {

    const val MIN_ZOOM = 1f
    const val MAX_ZOOM = 5f
    const val DOUBLE_TAP_ZOOM = 2.5f
    const val ZOOM_EPSILON = 0.001f

    fun doubleTapTarget(currentZoom: Float): Float =
        if (currentZoom <= MIN_ZOOM + ZOOM_EPSILON) DOUBLE_TAP_ZOOM else MIN_ZOOM
}
