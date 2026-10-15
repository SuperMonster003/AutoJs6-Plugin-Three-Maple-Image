package io.github.supermonster003.autojs6.plugin.three.maple.image

import io.github.supermonster003.autojs6.plugin.three.maple.image.ExtendedDetailField.CAMERA
import io.github.supermonster003.autojs6.plugin.three.maple.image.ExtendedDetailField.EXPOSURE
import io.github.supermonster003.autojs6.plugin.three.maple.image.ExtendedDetailField.FOCAL_LENGTH
import io.github.supermonster003.autojs6.plugin.three.maple.image.ExtendedDetailField.ISO
import io.github.supermonster003.autojs6.plugin.three.maple.image.ExtendedDetailField.LOCATION
import io.github.supermonster003.autojs6.plugin.three.maple.image.ExtendedDetailField.MODIFIED
import io.github.supermonster003.autojs6.plugin.three.maple.image.ExtendedDetailField.PATH
import io.github.supermonster003.autojs6.plugin.three.maple.image.ExtendedDetailField.RESOLUTION
import io.github.supermonster003.autojs6.plugin.three.maple.image.ExtendedDetailField.SIZE
import io.github.supermonster003.autojs6.plugin.three.maple.image.ExtendedDetailField.TAKEN
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

class ExtendedDetailsPolicyTest {

    private val values = ExtendedDetailValues(
        path = "DCIM/Camera/IMG_0042.jpg",
        resolution = "4000 x 3000 px",
        size = "3.2 MB",
        taken = "Taken 2026-10-15 10:22:30",
        modified = "Modified 2026-10-15 10:30:00",
        camera = "Sony XQ-DQ72",
        exposure = "1/125 s · f/1.8",
        focalLength = "4.25 mm",
        iso = "ISO 200",
        location = "31.230416, 121.473701",
    )

    @Test fun theChoiceIsStoredByKeyAndUnknownKeysAreIgnored() {
        val chosen = setOf(ISO, PATH, TAKEN)
        assertEquals("path,taken,iso", ExtendedDetailsPolicy.encode(chosen))
        assertEquals(chosen, ExtendedDetailsPolicy.decode("path,taken,iso"))
        assertEquals(setOf(SIZE, LOCATION), ExtendedDetailsPolicy.decode(" size ,histogram,,location"))
        assertEquals(emptySet<ExtendedDetailField>(), ExtendedDetailsPolicy.decode(""))
        assertEquals(emptySet<ExtendedDetailField>(), ExtendedDetailsPolicy.decode(null))
        ExtendedDetailField.entries.forEach { field ->
            assertEquals(setOf(field), ExtendedDetailsPolicy.decode(ExtendedDetailsPolicy.encode(setOf(field))))
        }
    }

    @Test fun everyFieldShownGroupsIntoThePathTheFileTheCameraAndThePlace() {
        assertEquals(
            listOf(
                "DCIM/Camera/IMG_0042.jpg",
                "4000 x 3000 px · 3.2 MB · Taken 2026-10-15 10:22:30 · Modified 2026-10-15 10:30:00",
                "Sony XQ-DQ72 · 1/125 s · f/1.8 · 4.25 mm · ISO 200",
                "31.230416, 121.473701",
            ),
            ExtendedDetailsPolicy.lines(ExtendedDetailField.entries.toSet(), values, showLocation = true),
        )
    }

    @Test fun onlyTheChosenFieldsAppearInTheirOwnOrder() {
        assertEquals(
            listOf("3.2 MB", "Sony XQ-DQ72 · ISO 200"),
            ExtendedDetailsPolicy.lines(setOf(ISO, SIZE, CAMERA), values, showLocation = true),
        )
    }

    @Test fun theLocationNeedsShowLocation() {
        assertFalse(ExtendedDetailsPolicy.available(LOCATION, showLocation = false))
        assertTrue(ExtendedDetailsPolicy.available(LOCATION, showLocation = true))
        assertTrue(ExtendedDetailField.entries.filter { it != LOCATION }.all { ExtendedDetailsPolicy.available(it, false) })
        assertEquals(listOf("3.2 MB"), ExtendedDetailsPolicy.lines(setOf(SIZE, LOCATION), values, showLocation = false))
    }

    @Test fun valuesThePageDoesNotHaveAreLeftOut() {
        val noExif = ExtendedDetailValues(path = "IMG.png", resolution = "40 x 30 px", size = "1 KB", camera = " ")
        assertEquals(
            listOf("IMG.png", "40 x 30 px · 1 KB"),
            ExtendedDetailsPolicy.lines(setOf(PATH, RESOLUTION, SIZE, MODIFIED, CAMERA, EXPOSURE, FOCAL_LENGTH, ISO), noExif, true),
        )
        assertEquals(emptyList<String>(), ExtendedDetailsPolicy.lines(setOf(TAKEN, CAMERA), noExif, true))
    }

    @Test fun theExposureFieldsAreFormattedOnTheirOwn() {
        val exif = ImageExifMetadata(
            capturedAt = null, make = null, model = null,
            exposureTimeSeconds = 1.0 / 125, apertureFNumber = 1.8, sensitivityIso = 200, focalLengthMm = 4.25,
            orientation = null, hasGpsMetadata = false,
        )
        assertEquals("1/125 s · f/1.8", ImageExifValueFormatter.shutterAndAperture(exif, Locale.ROOT))
        assertEquals("ISO 200", ImageExifValueFormatter.iso(exif))
        assertEquals("4.25 mm", ImageExifValueFormatter.focalLength(exif, Locale.ROOT))
        assertEquals("1/125 s · f/1.8 · ISO 200 · 4.25 mm", ImageExifValueFormatter.exposure(exif, Locale.ROOT))
        val apertureOnly = exif.copy(exposureTimeSeconds = null)
        assertEquals("f/1.8", ImageExifValueFormatter.shutterAndAperture(apertureOnly, Locale.ROOT))
    }
}
