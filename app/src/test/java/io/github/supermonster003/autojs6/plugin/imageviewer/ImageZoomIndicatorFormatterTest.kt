package io.github.supermonster003.autojs6.plugin.imageviewer

import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ImageZoomIndicatorFormatterTest {

    @Test
    fun formatsAtMostTwoFractionDigitsWithoutGrouping() {
        assertEquals("1", ImageZoomIndicatorFormatter.format(1f, Locale.US))
        assertEquals("2.5", ImageZoomIndicatorFormatter.format(2.5f, Locale.US))
        assertEquals("4.25", ImageZoomIndicatorFormatter.format(4.25f, Locale.US))
    }

    @Test
    fun usesTheCurrentLocalesDecimalSeparator() {
        assertEquals("2,5", ImageZoomIndicatorFormatter.format(2.5f, Locale.FRANCE))
    }

    @Test
    fun rejectsNonFiniteAndOutOfRangeValues() {
        assertNull(ImageZoomIndicatorFormatter.format(Float.NaN, Locale.US))
        assertNull(ImageZoomIndicatorFormatter.format(0.99f, Locale.US))
        assertNull(ImageZoomIndicatorFormatter.format(5.01f, Locale.US))
    }
}
