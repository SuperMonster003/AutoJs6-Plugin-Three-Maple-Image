package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.graphics.Bitmap
import android.graphics.Color
import android.os.ParcelFileDescriptor
import android.view.WindowManager
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.core.content.FileProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.ViewerUiState
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.UUID

/**
 * ROADMAP 9.1: a slideshow keeps the screen on with the bars hidden and moves on by itself; a tap holds it (screen
 * may sleep again, the bar offers Resume and Stop), and a show that does not loop ends on the last page.
 */
@RunWith(AndroidJUnit4::class)
class SlideshowInstrumentationTest {

    @get:Rule
    val compose = createEmptyComposeRule()

    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private lateinit var directory: File

    @Before fun createImages() {
        directory = File(context.cacheDir, "standalone-images/slides-${UUID.randomUUID()}").apply { mkdirs() }
        listOf("a.png" to Color.RED, "b.png" to Color.GREEN, "c.png" to Color.BLUE).forEach { (name, color) ->
            val bitmap = Bitmap.createBitmap(40, 30, Bitmap.Config.ARGB_8888).apply { eraseColor(color) }
            File(directory, name).outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
            bitmap.recycle()
        }
        SlideshowSettingsStore(context).save(SlideshowSettings(intervalSeconds = 1, transition = SlideshowTransition.NONE))
    }

    @After fun removeImages() {
        directory.deleteRecursively()
        SlideshowSettingsStore(context).save(SlideshowSettings())
    }

    private fun launch(): ActivityScenario<ImageViewerActivity> {
        val pages = listOf("a.png", "b.png", "c.png").map { name ->
            val file = File(directory, name)
            val uri = FileProvider.getUriForFile(context, context.packageName + ".standalone-images", file)
            ImageViewerRequest(uri, name, file.length(), "image/png")
        }
        return ActivityScenario.launchActivityForResult(ImageViewerContract.viewerIntent(context, ImageViewerLaunchRequest.explicitSelection(pages)))
    }

    private fun ActivityScenario<ImageViewerActivity>.state(): ViewerUiState {
        var state: ViewerUiState? = null
        onActivity { state = it.viewerStateForTesting }
        return requireNotNull(state)
    }

    private fun ActivityScenario<ImageViewerActivity>.screenKeptOn(): Boolean {
        var on = false
        onActivity { on = it.window.attributes.flags and WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON != 0 }
        return on
    }

    private fun exists(tag: String): Boolean =
        runCatching { compose.onAllNodes(hasTestTag(tag)).fetchSemanticsNodes().isNotEmpty() }.getOrDefault(false)

    private fun shell(command: String): String {
        val descriptor = InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand(command)
        return ParcelFileDescriptor.AutoCloseInputStream(descriptor).bufferedReader().use { it.readText() }.trim()
    }

    /**
     * ROADMAP 9.4: the page turns and fades the viewer runs itself (keys, the slideshow) follow "Remove animations"
     * like the Compose animations of its screens.
     */
    @Test fun theViewersOwnAnimationsFollowTheSystemAnimationScale() {
        val original = shell("settings get global animator_duration_scale")
        try {
            launch().use { scenario ->
                compose.waitUntil(TIMEOUT) { scenario.state().pageCount == 3 }
                for (scale in listOf("0.0", "0.5")) {
                    shell("settings put global animator_duration_scale $scale")
                    compose.waitUntil(TIMEOUT) {
                        var current = -1f
                        scenario.onActivity { current = it.motionScaleForTesting }
                        current == scale.toFloat()
                    }
                }
            }
        } finally {
            shell(if (original == "null" || original.isEmpty()) "settings delete global animator_duration_scale" else "settings put global animator_duration_scale $original")
        }
    }

    @Test fun aShowMovesOnPausesOnATapAndEndsOnTheLastPage() {
        launch().use { scenario ->
            compose.waitUntil(TIMEOUT) { scenario.state().slideshowAvailable && exists("maple.viewer.more") }
            compose.onNodeWithTag("maple.viewer.more").performClick()
            compose.onNodeWithTag("maple.viewer.menu.slideshow").performClick()
            compose.waitUntil(TIMEOUT) { exists("maple.slideshow.dialog") }
            compose.onNodeWithTag("maple.dialog.confirm").performClick()

            compose.waitUntil(TIMEOUT) { scenario.state().slideshowPlaying }
            assertTrue(scenario.screenKeptOn())
            assertFalse(scenario.state().controlsVisible)
            compose.waitUntil(TIMEOUT) { scenario.state().pageIndex == 1 }

            // A tap holds the show: the bars and the paused bar come back, the screen may sleep, no page turns.
            scenario.onActivity { requireNotNull(it.currentImageViewForTesting()).performClick() }
            compose.waitUntil(TIMEOUT) { exists("maple.viewer.slideshow.paused") }
            assertTrue(scenario.state().controlsVisible)
            assertFalse(scenario.screenKeptOn())
            Thread.sleep(2_500)
            assertEquals(1, scenario.state().pageIndex)

            compose.onNodeWithTag("maple.viewer.slideshow.resume").performClick()
            compose.waitUntil(TIMEOUT) { scenario.state().pageIndex == 2 }
            // The last page: the show ends by itself and says so.
            val ended = context.getString(R.string.slideshow_ended)
            compose.waitUntil(TIMEOUT) {
                runCatching { compose.onAllNodes(hasText(ended)).fetchSemanticsNodes().isNotEmpty() }.getOrDefault(false)
            }
            assertFalse(scenario.state().slideshowPlaying)
            assertTrue(scenario.state().controlsVisible)
            assertFalse(scenario.screenKeptOn())
        }
    }

    @Test fun stopEndsTheShowAndTheChoicesAreRemembered() {
        SlideshowSettingsStore(context).save(SlideshowSettings(intervalSeconds = 1, loop = true, transition = SlideshowTransition.FADE))
        launch().use { scenario ->
            compose.waitUntil(TIMEOUT) { scenario.state().slideshowAvailable && exists("maple.viewer.more") }
            compose.onNodeWithTag("maple.viewer.more").performClick()
            compose.onNodeWithTag("maple.viewer.menu.slideshow").performClick()
            compose.waitUntil(TIMEOUT) { exists("maple.slideshow.dialog") }
            // The dialog opens with the remembered choices.
            compose.onNodeWithTag("maple.slideshow.transition.1").assertExists()
            compose.onNodeWithTag("maple.dialog.confirm").performClick()
            compose.waitUntil(TIMEOUT) { scenario.state().pageIndex == 1 }
            // A fading loop wraps around past the last page.
            compose.waitUntil(LOOP_TIMEOUT) { scenario.state().pageIndex == 0 }
            scenario.onActivity { requireNotNull(it.currentImageViewForTesting()).performClick() }
            compose.waitUntil(TIMEOUT) { exists("maple.viewer.slideshow.stop") }
            compose.onNodeWithTag("maple.viewer.slideshow.stop").performClick()
            compose.waitUntil(TIMEOUT) { !scenario.state().slideshowPlaying }
            assertFalse(exists("maple.viewer.slideshow.paused"))
            assertFalse(scenario.screenKeptOn())
        }
        assertEquals(SlideshowTransition.FADE, SlideshowSettingsStore(context).load().transition)
        assertTrue(SlideshowSettingsStore(context).load().loop)
    }

    private companion object {
        const val TIMEOUT = 15_000L
        const val LOOP_TIMEOUT = 20_000L
    }
}
