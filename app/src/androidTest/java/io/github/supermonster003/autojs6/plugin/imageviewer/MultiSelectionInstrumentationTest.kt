package io.github.supermonster003.autojs6.plugin.imageviewer

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.os.SystemClock
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import java.util.concurrent.CopyOnWriteArrayList
import org.autojs.plugin.explorer.api.IExplorerActionHostSession
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MultiSelectionInstrumentationTest {

    @Test
    fun explicitSelectionDisplaysAndFlipsRealImagesInHostOrder() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val fixtureDirectory = File(
            context.cacheDir,
            "multi-selection-${UUID.randomUUID()}",
        )
        assertTrue(fixtureDirectory.mkdir())
        val redFile = File(fixtureDirectory, RED_NAME)
        val blueFile = File(fixtureDirectory, BLUE_NAME)
        writeSolidPng(redFile, RED)
        writeSolidPng(blueFile, BLUE)
        val session = SelectionFixtureSession(mapOf(RED_NAME to redFile, BLUE_NAME to blueFile))
        val registrySeed = ImageViewerLaunchRequest(
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
                hostPage(1, redFile),
                hostPage(2, blueFile),
            ),
            hostSession = session,
            hostTargetId = "selection-fixture",
        )
        val registryToken = HostSessionImageRegistry.register(registrySeed)
        try {
            val launchRequest = ImageViewerLaunchRequest.explicitSelection(
                listOf(
                    directPage(HostSessionImageRegistry.imageUri(registryToken, 1), redFile),
                    directPage(HostSessionImageRegistry.imageUri(registryToken, 2), blueFile),
                ),
            )
            ActivityScenario.launchActivityForResult<ImageViewerActivity>(
                ImageViewerContract.viewerIntent(context, launchRequest),
            ).use { scenario ->
                awaitPage(scenario, RED_NAME, RED)
                scenario.onActivity { activity ->
                    val image = activity.findViewById<ZoomableImageView>(R.id.image)
                    assertTrue(image.toggleDoubleTapZoom(image.width / 2f, image.height / 2f))
                    assertEquals(ImageZoomState.DOUBLE_TAP_ZOOM, image.currentZoomForTesting)
                    image.onPageSwipe?.invoke(ZoomableImageView.DIRECTION_NEXT)
                }

                awaitPage(scenario, BLUE_NAME, BLUE)
                scenario.onActivity { activity ->
                    val image = activity.findViewById<ZoomableImageView>(R.id.image)
                    assertEquals(ImageZoomState.MIN_ZOOM, image.currentZoomForTesting)
                    assertTrue(activity.findViewById<View>(R.id.share).isEnabled)
                    assertTrue(activity.findViewById<View>(R.id.open_external).isEnabled)
                    image.onPageSwipe?.invoke(ZoomableImageView.DIRECTION_PREVIOUS)
                }

                awaitPage(scenario, RED_NAME, RED)
            }
            assertTrue(session.openedPaths.contains(RED_NAME))
            assertTrue(session.openedPaths.contains(BLUE_NAME))
        } finally {
            HostSessionImageRegistry.unregister(registryToken)
            redFile.delete()
            blueFile.delete()
            fixtureDirectory.delete()
        }
    }

    private fun awaitPage(
        scenario: ActivityScenario<ImageViewerActivity>,
        expectedName: String,
        expectedColor: Int,
    ) {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val deadline = SystemClock.uptimeMillis() + PAGE_TIMEOUT_MILLIS
        while (SystemClock.uptimeMillis() < deadline) {
            var ready = false
            scenario.onActivity { activity ->
                val title = activity.findViewById<Toolbar>(R.id.toolbar).title?.toString()
                val drawable = activity.findViewById<ZoomableImageView>(R.id.image).drawable
                    as? BitmapDrawable
                val bitmap = drawable?.bitmap
                ready = title == expectedName && bitmap != null &&
                    bitmap.width > 0 && bitmap.height > 0 &&
                    bitmap.getPixel(bitmap.width / 2, bitmap.height / 2) == expectedColor
            }
            if (ready) return
            instrumentation.waitForIdleSync()
            SystemClock.sleep(POLL_INTERVAL_MILLIS)
        }
        fail("Timed out waiting for selected page $expectedName")
    }

    private fun writeSolidPng(file: File, color: Int) {
        val bitmap = Bitmap.createBitmap(FIXTURE_WIDTH, FIXTURE_HEIGHT, Bitmap.Config.ARGB_8888)
        try {
            bitmap.eraseColor(color)
            FileOutputStream(file).use { output ->
                assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG, 100, output))
            }
        } finally {
            bitmap.recycle()
        }
    }

    private fun hostPage(index: Int, file: File) = ImageViewerPage(
        image = ImageViewerRequest(
            ImageViewerContract.hostImageUri(index),
            file.name,
            file.length(),
            "image/png",
        ),
        hostRelativePath = file.name,
    )

    private fun directPage(uri: Uri, file: File) = ImageViewerRequest(
        targetUri = uri,
        displayName = file.name,
        declaredSize = file.length(),
        mimeType = "image/png",
    )

    private class SelectionFixtureSession(
        private val files: Map<String, File>,
    ) : IExplorerActionHostSession.Default() {

        val openedPaths = CopyOnWriteArrayList<String>()

        override fun openFile(targetId: String?, relativePath: String?): ParcelFileDescriptor {
            val path = requireNotNull(relativePath)
            openedPaths += path
            return ParcelFileDescriptor.open(
                requireNotNull(files[path]),
                ParcelFileDescriptor.MODE_READ_ONLY,
            )
        }
    }

    private companion object {
        const val RED_NAME = "selected-red.png"
        const val BLUE_NAME = "selected-blue.png"
        const val FIXTURE_WIDTH = 48
        const val FIXTURE_HEIGHT = 32
        const val PAGE_TIMEOUT_MILLIS = 10_000L
        const val POLL_INTERVAL_MILLIS = 50L
        val RED: Int = Color.rgb(220, 25, 35)
        val BLUE: Int = Color.rgb(30, 70, 220)
    }
}
