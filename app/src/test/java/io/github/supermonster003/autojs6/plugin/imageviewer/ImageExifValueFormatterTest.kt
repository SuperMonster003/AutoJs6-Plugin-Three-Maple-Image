package io.github.supermonster003.autojs6.plugin.imageviewer

import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ImageExifValueFormatterTest {

    @Test
    fun validatesAndNormalizesExifCaptureDateTime() {
        assertEquals(
            "2026-08-31 14:05:09",
            ImageExifValueFormatter.capturedAt("2026:08:31 14:05:09"),
        )
        assertEquals(
            "2024-02-29 23:59:59",
            ImageExifValueFormatter.capturedAt("2024-02-29 23:59:59"),
        )
        assertNull(ImageExifValueFormatter.capturedAt("2025:02:29 12:00:00"))
        assertNull(ImageExifValueFormatter.capturedAt("not-a-date"))
    }

    @Test
    fun combinesDeviceWithoutRepeatingTheMaker() {
        assertEquals("Google Pixel 9", ImageExifValueFormatter.device("Google", "Pixel 9"))
        assertEquals("Canon EOS R5", ImageExifValueFormatter.device("Canon", "Canon EOS R5"))
        assertEquals("Pixel 9", ImageExifValueFormatter.device(null, "Pixel 9"))
        assertNull(ImageExifValueFormatter.device(null, null))
    }

    @Test
    fun formatsCommonExposureValuesIntoACompactSummary() {
        val metadata = ImageExifMetadata(
            capturedAt = null,
            make = null,
            model = null,
            exposureTimeSeconds = 0.008,
            apertureFNumber = 1.8,
            sensitivityIso = 200,
            focalLengthMm = 4.25,
            orientation = null,
            hasGpsMetadata = false,
        )

        assertEquals(
            "1/125 s · f/1.8 · ISO 200 · 4.25 mm",
            ImageExifValueFormatter.exposure(metadata, Locale.US),
        )
        assertEquals(
            "0.3 s",
            ImageExifValueFormatter.formatExposureTime(0.3, Locale.US),
        )
    }
}
