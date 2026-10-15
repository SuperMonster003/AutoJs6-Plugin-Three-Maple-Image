package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import android.view.View
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
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
 * ROADMAP 9.2: the viewer's display settings (orientation, black canvas, maximum brightness, hidden system bars,
 * screen on, thumbnail strip, highest quality); with the defaults nothing changes from before.
 */
@RunWith(AndroidJUnit4::class)
class ViewerDisplayInstrumentationTest {

    @get:Rule
    val compose = createEmptyComposeRule()

    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private val store get() = ApplicationSettingsStore(context)
    private lateinit var original: ApplicationSettings
    private lateinit var directory: File

    @Before fun prepare() {
        original = store.load()
        directory = File(context.cacheDir, "standalone-images/display-${UUID.randomUUID()}").apply { mkdirs() }
        write("wide.png", 300, 100, Color.RED)
        write("tall.png", 100, 300, Color.BLUE)
    }

    @After fun restore() {
        store.save(original)
        directory.deleteRecursively()
    }

    private fun write(name: String, width: Int, height: Int, color: Int, format: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG) {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).apply { eraseColor(color) }
        @Suppress("DEPRECATION")
        File(directory, name).outputStream().use { bitmap.compress(format, 100, it) }
        bitmap.recycle()
    }

    private fun launch(vararg names: String): ActivityScenario<ImageViewerActivity> {
        val pages = names.map { name ->
            val file = File(directory, name)
            val mime = if (name.endsWith(".webp")) "image/webp" else "image/png"
            ImageViewerRequest(FileProvider.getUriForFile(context, context.packageName + ".standalone-images", file), name, file.length(), mime)
        }
        return ActivityScenario.launchActivityForResult(ImageViewerContract.viewerIntent(context, ImageViewerLaunchRequest.explicitSelection(pages)))
    }

    private fun ActivityScenario<ImageViewerActivity>.state(): ViewerUiState {
        var state: ViewerUiState? = null
        onActivity { state = it.viewerStateForTesting }
        return requireNotNull(state)
    }

    private fun <T> ActivityScenario<ImageViewerActivity>.read(block: (ImageViewerActivity) -> T): T {
        var value: Result<T>? = null
        onActivity { value = runCatching { block(it) } }
        return requireNotNull(value).getOrThrow()
    }

    private fun ActivityScenario<ImageViewerActivity>.awaitReady(pageIndex: Int = 0) =
        compose.waitUntil(TIMEOUT) { state().let { !it.loading && it.pageIndex == pageIndex && it.metadataText.isNotEmpty() } && read { it.currentImageViewForTesting()?.drawable != null } }

    /** Android 11+ reports it in the insets; before that the compat controller sets the fullscreen flag. */
    @Suppress("DEPRECATION")
    private fun ActivityScenario<ImageViewerActivity>.statusBarsVisible(): Boolean = read {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            ViewCompat.getRootWindowInsets(it.window.decorView)?.isVisible(WindowInsetsCompat.Type.statusBars()) ?: true
        } else {
            it.window.decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0
        }
    }

    private fun exists(tag: String): Boolean =
        runCatching { compose.onAllNodes(hasTestTag(tag)).fetchSemanticsNodes().isNotEmpty() }.getOrDefault(false)

    @Test fun theDefaultsChangeNothing() {
        store.save(original.copy(viewer = ViewerSettings()))
        launch("wide.png", "tall.png").use { scenario ->
            scenario.awaitReady()
            assertEquals(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED, scenario.read { it.requestedOrientation })
            assertFalse(scenario.read { it.keepsScreenOnForTesting })
            assertEquals(-1f, scenario.read { it.windowBrightnessForTesting }, 0f)
            assertTrue(scenario.state().blackBackground)
            compose.waitUntil(TIMEOUT) { exists("maple.viewer.strip") }
            scenario.onActivity { it.toggleControlsForTesting() }
            compose.waitForIdle()
            assertTrue("the system bars stay", scenario.statusBarsVisible())
        }
    }

    @Test fun theDisplaySettingsApplyToTheWindowAndTheOverlay() {
        store.save(
            original.copy(
                viewer = ViewerSettings(
                    orientation = ViewerOrientation.LOCKED, blackBackground = false, maxBrightness = true,
                    hideSystemBars = true, keepScreenOn = true, thumbnailStrip = false,
                ),
            ),
        )
        launch("wide.png", "tall.png").use { scenario ->
            scenario.awaitReady()
            assertEquals(ActivityInfo.SCREEN_ORIENTATION_LOCKED, scenario.read { it.requestedOrientation })
            assertTrue(scenario.read { it.keepsScreenOnForTesting })
            assertEquals(1f, scenario.read { it.windowBrightnessForTesting }, 0f)
            assertFalse(scenario.state().blackBackground)
            assertFalse(scenario.state().thumbnailStrip)
            assertFalse("no strip", exists("maple.viewer.strip"))

            scenario.onActivity { it.toggleControlsForTesting() }
            compose.waitUntil(TIMEOUT) { !scenario.statusBarsVisible() }
            scenario.onActivity { it.toggleControlsForTesting() }
            compose.waitUntil(TIMEOUT) { scenario.statusBarsVisible() }

            // Turned off while away: back in the viewer the window is the system's again.
            store.saveViewer(ViewerSettings())
            scenario.moveToState(Lifecycle.State.STARTED)
            scenario.moveToState(Lifecycle.State.RESUMED)
            assertFalse(scenario.read { it.keepsScreenOnForTesting })
            assertEquals(-1f, scenario.read { it.windowBrightnessForTesting }, 0f)
            assertEquals(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED, scenario.read { it.requestedOrientation })
            assertTrue(scenario.state().thumbnailStrip && scenario.state().blackBackground)
        }
    }

    @Test fun followTheImageTurnsTheScreenToEachPagesShape() {
        store.save(original.copy(viewer = ViewerSettings(orientation = ViewerOrientation.FOLLOW_IMAGE)))
        launch("wide.png", "tall.png").use { scenario ->
            compose.waitUntil(TIMEOUT) { runCatching { scenario.read { it.requestedOrientation } }.getOrNull() == ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE }
            scenario.awaitReady()
            scenario.onActivity { it.flipPageForTesting(ZoomableImageView.DIRECTION_NEXT) }
            compose.waitUntil(TIMEOUT) { runCatching { scenario.read { it.requestedOrientation } }.getOrNull() == ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT }
            scenario.awaitReady(pageIndex = 1)
        }
    }

    @Test fun highestQualityDecodesThePageAtItsOwnSize() {
        // WebP is never tiled, so the page always comes through the screen-sized decode.
        write("big.webp", 3000, 3000, Color.GREEN, Bitmap.CompressFormat.WEBP)
        val widths = listOf(false, true).map { highest ->
            store.save(original.copy(viewer = ViewerSettings(highestQuality = highest)))
            launch("big.webp").use { scenario ->
                scenario.awaitReady()
                scenario.read { requireNotNull(it.currentImageViewForTesting()?.drawable).intrinsicWidth to it.resources.displayMetrics }
            }
        }
        val (screenSized, metrics) = widths[0]
        val ownSized = widths[1].first
        val longEdge = maxOf(metrics.widthPixels, metrics.heightPixels)
        // Screen-sized by default; at its own size (the memory budget may still cap it a little) when asked.
        assertTrue("default $screenSized, screen $longEdge", screenSized <= longEdge)
        assertTrue("highest $ownSized, default $screenSized", ownSized > screenSized && ownSized > longEdge)
    }

    private companion object {
        const val TIMEOUT = 15_000L
    }
}
