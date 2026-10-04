package io.github.supermonster003.autojs6.plugin.three.maple.image

import java.text.NumberFormat
import java.util.Locale

internal object ImageZoomIndicatorFormatter {

    fun format(zoom: Float, locale: Locale): String? {
        if (
            !zoom.isFinite() ||
            zoom < ImageZoomState.MIN_ZOOM ||
            zoom > ImageZoomState.MAX_ZOOM
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
