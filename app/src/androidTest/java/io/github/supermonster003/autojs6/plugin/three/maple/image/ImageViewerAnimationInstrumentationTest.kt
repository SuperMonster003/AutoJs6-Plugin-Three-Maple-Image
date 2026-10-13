package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.content.Intent
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.os.SystemClock
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4
import com.bumptech.glide.load.resource.gif.GifDrawable
import java.io.File
import org.autojs.plugin.explorer.api.IExplorerActionHostSession
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ImageViewerAnimationInstrumentationTest {

    @Test
    fun rejectedInternalIntentFinishesWithoutDestroyCrash() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        ActivityScenario.launch<ImageViewerActivity>(
            Intent(context, ImageViewerActivity::class.java),
        ).use { scenario ->
            assertEquals(Lifecycle.State.DESTROYED, scenario.state)
        }
    }

    @Test
    fun viewerControlsGifAndRestoresUserPauseAfterRecreation() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val file = File(context.cacheDir, "image-viewer-activity-animation.gif")
        AnimatedGifTestFixture.writeTo(file)
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
                        "image/gif",
                    ),
                    hostRelativePath = file.name,
                ),
            ),
            hostSession = session,
            hostTargetId = "animation-test-target",
        )
        val token = HostSessionImageRegistry.register(seed)
        try {
            val source = HostSessionImageRegistry.imageUri(token, 1)
            val request = ImageViewerLaunchRequest.single(
                ImageViewerRequest(source, file.name, file.length(), "image/gif"),
            )
            ActivityScenario.launchActivityForResult<ImageViewerActivity>(
                ImageViewerContract.viewerIntent(context, request),
            ).use { scenario ->
                awaitAnimationControl(scenario, R.string.action_pause_animation)
                scenario.onActivity { activity ->
                    val drawable = requireNotNull(activity.currentImageViewForTesting()).drawable
                    assertTrue(drawable is GifDrawable && drawable.frameCount == 2)
                    assertTrue((drawable as GifDrawable).isRunning)
                    activity.toggleAnimationForTesting()
                    assertEquals(ImageAnimationPlayback.PAUSED, activity.viewerStateForTesting.animation)
                    assertFalse(drawable.isRunning)
                    activity.rotateForTesting()
                    assertEquals(90, requireNotNull(activity.currentImageViewForTesting())
                        .currentRotationDegreesForTesting)
                    assertFalse(drawable.isRunning)
                }

                scenario.recreate()

                awaitAnimationControl(scenario, R.string.action_resume_animation)
                scenario.onActivity { activity ->
                    val drawable = requireNotNull(activity.currentImageViewForTesting()).drawable
                    assertTrue(drawable is GifDrawable)
                    assertFalse((drawable as GifDrawable).isRunning)
                    val image = requireNotNull(activity.currentImageViewForTesting())
                    assertEquals(90, image.currentRotationDegreesForTesting)
                    activity.resetZoomForTesting()
                    assertEquals(0, image.currentRotationDegreesForTesting)
                    assertFalse(drawable.isRunning)
                }
            }
        } finally {
            HostSessionImageRegistry.unregister(token)
            file.delete()
        }
    }

    private fun awaitAnimationControl(
        scenario: ActivityScenario<ImageViewerActivity>,
        expectedText: Int,
    ) {
        val deadline = SystemClock.uptimeMillis() + LOAD_TIMEOUT_MILLIS
        var ready = false
        while (!ready && SystemClock.uptimeMillis() < deadline) {
            scenario.onActivity { activity ->
                val state = activity.viewerStateForTesting
                val expected = if (expectedText == R.string.action_resume_animation) {
                    ImageAnimationPlayback.PAUSED
                } else {
                    ImageAnimationPlayback.PLAYING
                }
                ready = state.controlsVisible && state.animation == expected
            }
            if (!ready) SystemClock.sleep(POLL_INTERVAL_MILLIS)
        }
        assertTrue("Timed out waiting for the GIF playback control", ready)
    }

    private companion object {
        const val LOAD_TIMEOUT_MILLIS = 10_000L
        const val POLL_INTERVAL_MILLIS = 50L
    }
}
