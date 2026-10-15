package io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose

import io.github.supermonster003.autojs6.plugin.three.maple.image.MediaItem
import org.junit.Assert.assertEquals
import org.junit.Test
import java.text.DateFormat
import java.util.Locale
import java.util.TimeZone

/** ROADMAP 9.4: TalkBack reads a media cell as its file name and the day it was taken. */
class MediaCellDescriptionTest {

    private val format = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") }

    private fun item(name: String, takenMillis: Long?, modifiedSeconds: Long) = MediaItem(
        id = 1L, displayName = name, mimeType = "image/jpeg", sizeBytes = 1L, width = 1, height = 1,
        dateModifiedSeconds = modifiedSeconds, dateTakenMillis = takenMillis, bucketId = 7L, bucketName = "Camera",
        relativePath = "DCIM/Camera/", contentUri = "content://media/external/images/media/1",
    )

    @Test fun theFileNameComesFirstThenTheDayItWasTaken() {
        // 2026-10-15 12:00 UTC taken, modified years earlier: the capture time wins.
        assertEquals("IMG_0001.jpg, Oct 15, 2026", mediaCellDescription(item("IMG_0001.jpg", 1_792_065_600_000L, 1_709_193_600L), format))
    }

    @Test fun withoutACaptureTimeTheModificationDayIsRead() {
        // Modified 2024-02-29 08:00 UTC, no capture time (a screenshot or a downloaded image).
        assertEquals("talk-one.png, Feb 29, 2024", mediaCellDescription(item("talk-one.png", null, 1_709_193_600L), format))
    }
}
