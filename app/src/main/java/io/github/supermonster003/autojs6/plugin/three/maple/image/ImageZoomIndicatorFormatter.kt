package io.github.supermonster003.autojs6.plugin.three.maple.image

import java.text.NumberFormat
import java.util.Locale

internal object ImageZoomIndicatorFormatter {

    /** [maxZoom] is the viewer's current limit (ROADMAP 9.2 lets it go past 5x). */
    fun format(zoom: Float, locale: Locale, maxZoom: Float = ImageZoomState.MAX_ZOOM): String? {
        if (
            !zoom.isFinite() ||
            zoom < ImageZoomState.MIN_ZOOM ||
            zoom > maxZoom + ImageZoomState.ZOOM_EPSILON
        ) {
            return null
        }
        return NumberFormat.getNumberInstance(locale).apply {
            isGroupingUsed = false
            minimumFractionDigits = 0
            maximumFractionDigits = MAX_FRACTION_DIGITS
        }.format(zoom.toDouble())
    }

    private const val MAX_FRACTION_DIGITS = 2
}
