package io.github.supermonster003.autojs6.plugin.three.maple.image

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GeoPositionTest {

    @Test fun decimalDegreesHaveSixDecimalsInEveryLocale() {
        assertEquals("31.230416, 121.473701", GeoPosition(31.230416, 121.473701).decimal())
        assertEquals("-33.868800, -151.209300", GeoPosition(-33.8688, -151.2093).decimal())
    }

    @Test fun degreesMinutesSecondsNameTheHemisphere() {
        assertEquals("31°13'49.5\"N 121°28'25.3\"E", GeoPosition(31.230416, 121.473701).degreesMinutesSeconds())
        assertEquals("33°52'7.7\"S 151°12'33.5\"W", GeoPosition(-33.8688, -151.2093).degreesMinutesSeconds())
        // Rounding carries into the minutes and degrees instead of showing 60 seconds.
        assertEquals("11°0'0.0\"N 0°0'0.0\"E", GeoPosition(10.99999999, 0.0).degreesMinutesSeconds())
    }

    @Test fun theMapUriDropsAPin() {
        assertEquals("geo:31.230416,121.473701?q=31.230416,121.473701", GeoPosition(31.230416, 121.473701).geoUri())
    }

    @Test fun onlyRealPositionsAreAccepted() {
        assertTrue(GeoPosition.isValid(90.0, -180.0))
        assertFalse(GeoPosition.isValid(90.5, 0.0))
        assertFalse(GeoPosition.isValid(0.0, 181.0))
        assertFalse(GeoPosition.isValid(Double.NaN, 0.0))
        assertNull(GeoPosition.of(0.0, Double.POSITIVE_INFINITY))
    }
}
