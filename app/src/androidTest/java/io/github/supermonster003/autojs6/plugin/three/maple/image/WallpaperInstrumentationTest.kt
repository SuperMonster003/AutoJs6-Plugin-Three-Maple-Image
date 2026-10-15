package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.app.Activity
import android.app.WallpaperManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.core.content.FileProvider
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.UUID

/**
 * ROADMAP 9.1: "Set as wallpaper" locks the crop to the screen's shape and sets the home screen or the lock screen
 * wallpaper on its own (the emulator's wallpaper ids change one at a time).
 */
@RunWith(AndroidJUnit4::class)
class WallpaperInstrumentationTest {

    @get:Rule
    val compose = createEmptyComposeRule()

    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private lateinit var image: File

    @Before fun createImage() {
        image = File(context.cacheDir, "standalone-images/wallpaper-${UUID.randomUUID()}.png").apply { parentFile!!.mkdirs() }
        val bitmap = Bitmap.createBitmap(3000, 1000, Bitmap.Config.ARGB_8888)
        Canvas(bitmap).apply {
            drawColor(Color.BLUE)
            drawRect(1000f, 0f, 2000f, 1000f, Paint().apply { color = Color.YELLOW })
        }
        image.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()
    }

    @After fun removeImage() {
        image.delete()
    }

    private fun launch(): ActivityScenario<WallpaperActivity> {
        val uri = FileProvider.getUriForFile(context, context.packageName + ".standalone-images", image)
        return ActivityScenario.launchActivityForResult(
            WallpaperActivity.intent(context, ImageViewerRequest(uri, "wide.png", image.length(), "image/png")),
        )
    }

    private fun ActivityScenario<WallpaperActivity>.crop(): Rect? {
        var crop: Rect? = null
        onActivity { crop = it.editingViewForTesting.cropSelection() }
        return crop
    }

    private fun ActivityScenario<WallpaperActivity>.frame(): ImagePixelSize {
        var frame: ImagePixelSize? = null
        onActivity { frame = Wallpapers.frame(it) }
        return requireNotNull(frame)
    }

    @Test fun theCropHasTheScreensShapeAndSpansAWideImagesHeight() {
        launch().use { scenario ->
            compose.waitUntil(TIMEOUT) { scenario.crop() != null }
            val crop = requireNotNull(scenario.crop())
            val frame = scenario.frame()
            assertEquals(frame.width.toDouble() / frame.height, crop.width().toDouble() / crop.height(), 0.01)
            // Twice the screen height is more than the image has: it is decoded whole and the crop starts centered.
            assertEquals(1000, crop.height())
            assertEquals(1500.0, crop.exactCenterX().toDouble(), 2.0)
        }
    }

    @Test fun theHomeScreenAndTheLockScreenAreSetOnTheirOwn() {
        val manager = WallpaperManager.getInstance(context)
        val homeBefore = manager.getWallpaperId(WallpaperManager.FLAG_SYSTEM)
        apply(targetIndex = 0) { manager.getWallpaperId(WallpaperManager.FLAG_SYSTEM) != homeBefore }
        val home = manager.getWallpaperId(WallpaperManager.FLAG_SYSTEM)

        val lockBefore = manager.getWallpaperId(WallpaperManager.FLAG_LOCK)
        apply(targetIndex = 1) { manager.getWallpaperId(WallpaperManager.FLAG_LOCK).let { it > 0 && it != lockBefore } }
        assertEquals("the lock screen leaves the home screen alone", home, manager.getWallpaperId(WallpaperManager.FLAG_SYSTEM))
        assertNotEquals(home, manager.getWallpaperId(WallpaperManager.FLAG_LOCK))
    }

    private fun apply(targetIndex: Int, done: () -> Boolean) {
        launch().use { scenario ->
            compose.waitUntil(TIMEOUT) { scenario.crop() != null }
            compose.onNodeWithTag("maple.wallpaper.target.$targetIndex").performClick()
            compose.onNodeWithTag("maple.panel.confirm").performClick()
            compose.waitUntil(TIMEOUT, done)
            // The screen finishes on its next recomposition, which only runs while the test polls.
            compose.waitUntil(TIMEOUT) { scenario.state == Lifecycle.State.DESTROYED }
            assertEquals(Activity.RESULT_OK, scenario.result.resultCode)
        }
    }

    private companion object {
        const val TIMEOUT = 15_000L
    }
}
