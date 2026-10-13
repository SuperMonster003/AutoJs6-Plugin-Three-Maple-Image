package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.net.Uri
import android.os.ParcelFileDescriptor
import android.os.SystemClock
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4
import java.io.File
import org.autojs.plugin.explorer.api.IExplorerActionHostSession
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ImageViewerExifInstrumentationTest {

    @Test
    fun detailsPanelShowsSafeExifFieldsAndRestoresExpandedState() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val file = File(context.cacheDir, "image-viewer-activity-exif.jpg")
        ExifJpegTestFixture.writeCompleteTo(file)
        val session = object : IExplorerActionHostSession.Default() {
            override fun openFile(targetId: String?, relativePath: String?): ParcelFileDescriptor =
                ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
        }
        val seed = ImageViewerLaunchRequest(
            pages = listOf(
                ImageViewerPage(
                    image = ImageViewerRequest(
                        Uri.parse("content://image-viewer.test/selected.png"),
                        "selected.png",
                        1L,
                        "image/png",
                    ),
                    hostRelativePath = "",
                ),
                ImageViewerPage(
                    image = ImageViewerRequest(
                        ImageViewerContract.hostImageUri(1),
                        file.name,
                        file.length(),
                        "image/jpeg",
                    ),
                    hostRelativePath = file.name,
                ),
            ),
            hostSession = session,
            hostTargetId = "exif-test-target",
        )
        val token = HostSessionImageRegistry.register(seed)
        try {
            val source = HostSessionImageRegistry.imageUri(token, 1)
            val request = ImageViewerLaunchRequest.single(
                ImageViewerRequest(source, file.name, file.length(), "image/jpeg"),
            )
            ActivityScenario.launchActivityForResult<ImageViewerActivity>(
                ImageViewerContract.viewerIntent(context, request),
            ).use { scenario ->
                awaitDetailsPanel(scenario, expanded = false)
                awaitImageReady(scenario)
                assertTransientZoomIndicator(scenario)
                scenario.onActivity { activity ->
                    assertFalse(activity.viewerStateForTesting.detailsExpanded)
                    activity.toggleDetailsForTesting()
                    assertDetailsAreSafe(activity)
                }

                scenario.recreate()

                awaitDetailsPanel(scenario, expanded = true)
                scenario.onActivity { activity ->
                    assertDetailsAreSafe(activity)
                    activity.toggleDetailsForTesting()
                    assertFalse(activity.viewerStateForTesting.detailsExpanded)
                }
            }
        } finally {
            HostSessionImageRegistry.unregister(token)
            file.delete()
        }
    }

    private fun assertDetailsAreSafe(activity: ImageViewerActivity) {
        val state = activity.viewerStateForTesting
        val text = state.exifText
        assertTrue(state.detailsAvailable)
        assertTrue(state.detailsExpanded)
        assertTrue(text.contains("2026-08-31 14:05:09"))
        assertTrue(text.contains("${ExifJpegTestFixture.MAKE} ${ExifJpegTestFixture.MODEL}"))
        assertTrue(text.contains("1/125 s"))
        assertTrue(text.contains("f/1.8"))
        assertTrue(text.contains("ISO 200"))
        assertTrue(text.contains("4.25 mm"))
        assertTrue(text.contains(activity.getString(R.string.exif_orientation_clockwise, 90)))
        assertTrue(text.contains(activity.getString(R.string.exif_gps_present)))
        assertFalse(text.contains(ExifJpegTestFixture.LATITUDE.toString()))
        assertFalse(text.contains(ExifJpegTestFixture.LONGITUDE.toString()))
        val metadata = state.metadataText
        assertTrue(metadata.contains("32 bpp"))
        assertTrue(metadata.contains("sRGB", ignoreCase = true))
        assertEquals(metadata, state.detailsSummary)
    }

    private fun assertTransientZoomIndicator(
        scenario: ActivityScenario<ImageViewerActivity>,
    ) {
        scenario.onActivity { activity ->
            val image = requireNotNull(activity.currentImageViewForTesting())
            assertTrue(image.toggleDoubleTapZoom(image.width / 2f, image.height / 2f))
            val indicator = requireNotNull(activity.viewerStateForTesting.zoomIndicatorText)
            val zoom = requireNotNull(
                ImageZoomIndicatorFormatter.format(
                    image.currentZoomForTesting,
                    activity.resources.configuration.locales[0],
                ),
            )
            assertEquals(activity.getString(R.string.zoom_indicator, zoom), indicator)
        }
        SystemClock.sleep(ZOOM_INDICATOR_SETTLE_MILLIS)
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        scenario.onActivity { activity ->
            assertNull(activity.viewerStateForTesting.zoomIndicatorText)
        }
    }

    private fun awaitImageReady(scenario: ActivityScenario<ImageViewerActivity>) {
        val deadline = SystemClock.uptimeMillis() + LOAD_TIMEOUT_MILLIS
        var ready = false
        while (!ready && SystemClock.uptimeMillis() < deadline) {
            scenario.onActivity { activity ->
                ready = activity.viewerStateForTesting.printEnabled
            }
            if (!ready) SystemClock.sleep(POLL_INTERVAL_MILLIS)
        }
        assertTrue("Timed out waiting for the image drawable", ready)
    }

    private fun awaitDetailsPanel(
        scenario: ActivityScenario<ImageViewerActivity>,
        expanded: Boolean,
    ) {
        val deadline = SystemClock.uptimeMillis() + LOAD_TIMEOUT_MILLIS
        var ready = false
        while (!ready && SystemClock.uptimeMillis() < deadline) {
            scenario.onActivity { activity ->
                val state = activity.viewerStateForTesting
                ready = state.detailsAvailable && state.detailsExpanded == expanded
            }
            if (!ready) SystemClock.sleep(POLL_INTERVAL_MILLIS)
        }
        assertTrue("Timed out waiting for the EXIF details panel", ready)
    }

    private companion object {
        const val LOAD_TIMEOUT_MILLIS = 10_000L
        const val POLL_INTERVAL_MILLIS = 50L
        const val ZOOM_INDICATOR_SETTLE_MILLIS = 1_100L
    }
}
