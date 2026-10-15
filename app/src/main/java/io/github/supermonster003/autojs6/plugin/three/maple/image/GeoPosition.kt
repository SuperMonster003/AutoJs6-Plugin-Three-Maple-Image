package io.github.supermonster003.autojs6.plugin.three.maple.image

import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToLong

/**
 * Where a photo was taken (ROADMAP 9.1, T14): shown only when "Show location" is on, as decimal degrees and as
 * degrees, minutes and seconds, and opened in a map app through a `geo:` URI.
 */
internal data class GeoPosition(val latitude: Double, val longitude: Double) {

    init {
        require(isValid(latitude, longitude)) { "Not a position: $latitude, $longitude" }
    }

    /** "31.230416, 121.473701" (six decimals, about 0.1 m). */
    fun decimal(): String = String.format(Locale.ROOT, "%.6f, %.6f", latitude, longitude)

    /** 31°13'49.5"N 121°28'25.3"E (seconds to a tenth). */
    fun degreesMinutesSeconds(): String =
        "${dms(latitude, if (latitude >= 0) 'N' else 'S')} ${dms(longitude, if (longitude >= 0) 'E' else 'W')}"

    /** `geo:lat,lng?q=lat,lng`, so map apps drop a pin on the spot instead of only centering it. */
    fun geoUri(): String {
        val point = String.format(Locale.ROOT, "%.6f,%.6f", latitude, longitude)
        return "geo:$point?q=$point"
    }

    private fun dms(value: Double, hemisphere: Char): String {
        val tenths = (abs(value) * 36_000).roundToLong()
        val degrees = tenths / 36_000
        val minutes = tenths % 36_000 / 600
        val seconds = tenths % 600
        return String.format(Locale.ROOT, "%d°%d'%d.%d\"%c", degrees, minutes, seconds / 10, seconds % 10, hemisphere)
    }

    companion object {
        fun isValid(latitude: Double, longitude: Double): Boolean =
            latitude.isFinite() && longitude.isFinite() && latitude in -90.0..90.0 && longitude in -180.0..180.0

        fun of(latitude: Double, longitude: Double): GeoPosition? =
            if (isValid(latitude, longitude)) GeoPosition(latitude, longitude) else null
    }
}
