package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.os.SystemClock
import android.view.InputDevice
import android.view.MotionEvent
import android.view.ViewConfiguration
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.core.content.FileProvider
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
 * ROADMAP 9.2: the optional viewer gestures on the page view (one-finger zoom, 1:1 double tap, the zoom limit,
 * edge taps, swipe down, brightness drag), and the viewer applying the stored choice to its pages and window.
 */
@RunWith(AndroidJUnit4::class)
class ViewerGesturesInstrumentationTest {

    @get:Rule
    val compose = createEmptyComposeRule()

    private val instrumentation get() = InstrumentationRegistry.getInstrumentation()
    private val context get() = instrumentation.targetContext
    private val store get() = ApplicationSettingsStore(context)
    private lateinit var original: ApplicationSettings
    private lateinit var directory: File
    private val bitmaps = mutableListOf<Bitmap>()

    @Before fun prepare() {
        original = store.load()
        directory = File(context.cacheDir, "standalone-images/gestures-${UUID.randomUUID()}").apply { mkdirs() }
    }

    @After fun restore() {
        store.save(original)
        directory.deleteRecursively()
        instrumentation.runOnMainSync { bitmaps.forEach(Bitmap::recycle) }
    }

    /** A 200 px square fitted into a 1000 px view: 1x shows it 1000 px wide. */
    private fun newView(configure: ZoomableImageView.() -> Unit = {}): ZoomableImageView {
        lateinit var view: ZoomableImageView
        instrumentation.runOnMainSync {
            val bitmap = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888).also { bitmaps += it }
            view = ZoomableImageView(context).apply {
                setImageDrawable(BitmapDrawable(resources, bitmap))
                layout(0, 0, VIEW_SIZE, VIEW_SIZE)
                resetZoom()
                configure()
            }
        }
        return view
    }

    private fun settle() {
        SystemClock.sleep(ViewConfiguration.getDoubleTapTimeout().toLong() + 150L)
        instrumentation.waitForIdleSync()
    }

    @Test fun doubleTapThenDragZoomsWithOneFingerInsteadOfJumping() {
        val view = newView()
        instrumentation.runOnMainSync { dispatchDoubleTapDrag(view, toY = 850f) }
        settle()
        instrumentation.runOnMainSync {
            val zoom = view.currentZoomForTesting
            assertTrue("zoom $zoom", zoom > 1.2f && zoom <= ImageZoomState.MAX_ZOOM)
            assertTrue("no 2.5x jump", kotlin.math.abs(zoom - ImageZoomState.DOUBLE_TAP_ZOOM) > 0.05f)
        }
    }

    @Test fun withoutOneFingerZoomTheSecondTapZoomsAtOnceAndTheDragPans() {
        val view = newView { oneFingerZoom = false }
        instrumentation.runOnMainSync { dispatchDoubleTapDrag(view, toY = 850f) }
        settle()
        instrumentation.runOnMainSync { assertEquals(ImageZoomState.DOUBLE_TAP_ZOOM, view.currentZoomForTesting, 0.001f) }
    }

    @Test fun aOneToOneDoubleTapUsesTheImagePixelsAndRaisesTheLimit() {
        val view = newView { doubleTapZoom = ViewerDoubleTapZoom.ACTUAL_PIXELS; sourcePixelSize = ImagePixelSize(4000, 4000) }
        instrumentation.runOnMainSync {
            assertEquals(4f, view.actualPixelsZoom()!!, 0.001f)
            assertTrue(view.toggleDoubleTapZoom(500f, 500f))
            assertEquals(4f, view.currentZoomForTesting, 0.001f)
            assertTrue(view.toggleDoubleTapZoom(500f, 500f))
            view.sourcePixelSize = ImagePixelSize(8000, 8000)
            assertEquals("1:1 beyond 5x is not cut short", 8f, view.maximumZoom, 0.001f)
            assertTrue(view.toggleDoubleTapZoom(500f, 500f))
            assertEquals(8f, view.currentZoomForTesting, 0.001f)
        }
    }

    @Test fun thePinchStopsAtTheChosenMaximum() {
        for ((setting, expected) in listOf(ViewerMaxZoom.FIVE to 5f, ViewerMaxZoom.TEN to 10f)) {
            val view = newView { maxZoom = setting }
            instrumentation.runOnMainSync {
                dispatchPinchOpen(view)
                assertEquals(setting.name, expected, view.currentZoomForTesting, 0.001f)
            }
        }
    }

    @Test fun edgeTapsTurnPagesWhileTheMiddleStillToggles() {
        val pages = mutableListOf<Int>()
        var clicks = 0
        val view = newView {
            edgeTapPaging = true
            onPageSwipe = { pages += it }
            setOnClickListener { clicks++ }
        }
        for (x in listOf(50f, 500f, 950f)) {
            instrumentation.runOnMainSync { dispatchTap(view, x, 500f) }
            settle()
        }
        assertEquals(listOf(ZoomableImageView.DIRECTION_PREVIOUS, ZoomableImageView.DIRECTION_NEXT), pages)
        assertEquals(1, clicks)

        instrumentation.runOnMainSync { view.edgeTapPaging = false; dispatchTap(view, 50f, 500f) }
        settle()
        assertEquals(2, clicks)
        assertEquals(2, pages.size)
    }

    @Test fun aQuickSwipeDownClosesOnlyWhenOnAndNotZoomedIn() {
        var closes = 0
        val view = newView { onSwipeDown = { closes++ } }
        instrumentation.runOnMainSync { dispatchSwipe(view, 500f, 200f, 500f, 800f) }
        assertEquals("off by default", 0, closes)
        instrumentation.runOnMainSync { view.swipeDownToClose = true; dispatchSwipe(view, 500f, 200f, 500f, 800f) }
        assertEquals(1, closes)
        instrumentation.runOnMainSync { dispatchSwipe(view, 500f, 800f, 500f, 200f) }
        assertEquals("upward", 1, closes)
        instrumentation.runOnMainSync { view.toggleDoubleTapZoom(500f, 500f); dispatchSwipe(view, 500f, 200f, 500f, 800f) }
        assertEquals("zoomed in, the drag pans", 1, closes)
    }

    @Test fun aVerticalDragOnTheLeftThirdReportsTheBrightnessAndNothingElse() {
        val drags = mutableListOf<Pair<Float, Boolean>>()
        var clicks = 0
        var closes = 0
        val view = newView {
            brightnessSwipe = true
            swipeDownToClose = true
            onBrightnessDrag = { distanceY, _, finished -> drags += distanceY to finished }
            onSwipeDown = { closes++ }
            setOnClickListener { clicks++ }
        }
        instrumentation.runOnMainSync { dispatchSwipe(view, 100f, 300f, 100f, 800f) }
        settle()
        assertTrue(drags.toString(), drags.size >= 2 && drags.last().second && drags.dropLast(1).none { it.second })
        assertEquals(500f, drags.last().first, 1f)
        assertEquals("the drag is not a swipe down", 0, closes)
        assertEquals(0, clicks)

        drags.clear()
        instrumentation.runOnMainSync { dispatchSwipe(view, 600f, 300f, 600f, 800f) }
        assertTrue("outside the left third", drags.isEmpty())
        assertEquals(1, closes)
    }

    @Test fun theViewerAppliesTheStoredGesturesToItsPagesAndWindow() {
        listOf("a.png" to Color.RED, "b.png" to Color.BLUE).forEach { (name, color) ->
            val bitmap = Bitmap.createBitmap(40, 30, Bitmap.Config.ARGB_8888).apply { eraseColor(color) }
            File(directory, name).outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
            bitmap.recycle()
        }
        store.save(
            original.copy(
                viewer = ViewerSettings(
                    rotationGesture = false, oneFingerZoom = false, edgeTapPaging = true, swipeDownToClose = true,
                    brightnessSwipe = true, doubleTapZoom = ViewerDoubleTapZoom.ACTUAL_PIXELS, maxZoom = ViewerMaxZoom.TEN,
                ),
            ),
        )
        val pages = listOf("a.png", "b.png").map { name ->
            val file = File(directory, name)
            ImageViewerRequest(FileProvider.getUriForFile(context, context.packageName + ".standalone-images", file), name, file.length(), "image/png")
        }
        ActivityScenario.launchActivityForResult<ImageViewerActivity>(
            ImageViewerContract.viewerIntent(context, ImageViewerLaunchRequest.explicitSelection(pages)),
        ).use { scenario ->
            compose.waitUntil(TIMEOUT) {
                var laidOut = false
                scenario.onActivity { laidOut = (it.currentImageViewForTesting()?.width ?: 0) > 0 }
                laidOut && scenario.state().let { !it.loading && it.pageCount == 2 }
            }
            scenario.onActivity { activity ->
                val view = requireNotNull(activity.currentImageViewForTesting())
                assertFalse(view.rotationGestureEnabled || view.oneFingerZoom)
                assertTrue(view.edgeTapPaging && view.swipeDownToClose && view.brightnessSwipe)
                assertEquals(ViewerDoubleTapZoom.ACTUAL_PIXELS, view.doubleTapZoom)
                assertEquals(ViewerMaxZoom.TEN, view.maxZoom)
                assertEquals("the image's own size", ImagePixelSize(40, 30), view.sourcePixelSize)
                dispatchSwipe(view, 20f, view.height * 0.7f, 20f, view.height * 0.2f)
                assertTrue("brightened: ${activity.windowBrightnessForTesting} ${activity.viewerStateForTesting.zoomIndicatorText} ${view.width}x${view.height} zoom ${view.currentZoomForTesting}", activity.windowBrightnessForTesting > 0f)
            }
            // Changed in the settings while away: back in the viewer the pages follow.
            store.saveViewer(store.load().viewer.copy(edgeTapPaging = false, maxZoom = ViewerMaxZoom.FIVE))
            scenario.moveToState(Lifecycle.State.STARTED)
            scenario.moveToState(Lifecycle.State.RESUMED)
            scenario.onActivity { activity ->
                val view = requireNotNull(activity.currentImageViewForTesting())
                assertFalse(view.edgeTapPaging)
                assertEquals(ViewerMaxZoom.FIVE, view.maxZoom)
                dispatchSwipe(view, view.width / 2f, view.height * 0.2f, view.width / 2f, view.height * 0.8f)
            }
            compose.waitUntil(TIMEOUT) { scenario.state == Lifecycle.State.DESTROYED }
        }
    }

    @Test fun theSettingsPageStoresTheGestureChoices() {
        store.save(original.copy(viewer = ViewerSettings()))
        ActivityScenario.launch(AppSettingsActivity::class.java).use {
            compose.onNodeWithTag("maple.settings.viewerOneFingerZoom").performScrollTo().assertIsOn().performClick()
            compose.onNodeWithTag("maple.settings.viewerEdgeTap").performScrollTo().assertIsOff().performClick()
            compose.onNodeWithTag("maple.settings.viewerSwipeDown").performScrollTo().performClick()
            compose.onNodeWithTag("maple.settings.viewerBrightnessSwipe").performScrollTo().performClick()
            compose.onNodeWithTag("maple.settings.viewerDoubleTapZoom").performScrollTo().performClick()
            compose.onNodeWithTag("maple.choice.${ViewerDoubleTapZoom.ACTUAL_PIXELS.ordinal}").performClick()
            compose.onNodeWithTag("maple.dialog.confirm").performClick()
            compose.onNodeWithTag("maple.settings.viewerMaxZoom").performScrollTo().performClick()
            compose.onNodeWithTag("maple.choice.${ViewerMaxZoom.TEN.ordinal}").performClick()
            compose.onNodeWithTag("maple.dialog.cancel").performClick()
            compose.waitForIdle()
            assertEquals(
                ViewerSettings(
                    oneFingerZoom = false, edgeTapPaging = true, swipeDownToClose = true, brightnessSwipe = true,
                    doubleTapZoom = ViewerDoubleTapZoom.ACTUAL_PIXELS,
                ),
                store.load().viewer,
            )
            compose.onNodeWithTag("maple.settings.viewerMaxZoom").performClick()
            compose.onNodeWithTag("maple.choice.${ViewerMaxZoom.TEN.ordinal}").performClick()
            compose.onNodeWithTag("maple.dialog.confirm").performClick()
            compose.waitForIdle()
            assertEquals(ViewerMaxZoom.TEN, store.load().viewer.maxZoom)
        }
    }

    private fun ActivityScenario<ImageViewerActivity>.state(): ViewerUiState {
        var state: ViewerUiState? = null
        onActivity { state = it.viewerStateForTesting }
        return requireNotNull(state)
    }

    // region Event helpers

    private fun dispatchTap(view: ZoomableImageView, x: Float, y: Float) {
        val down = SystemClock.uptimeMillis()
        dispatch(view, down, down, MotionEvent.ACTION_DOWN, x, y)
        dispatch(view, down, down + 40L, MotionEvent.ACTION_UP, x, y)
    }

    /** A double tap whose second finger stays down and drags from the middle to [toY]. */
    private fun dispatchDoubleTapDrag(view: ZoomableImageView, toY: Float) {
        val first = SystemClock.uptimeMillis()
        dispatch(view, first, first, MotionEvent.ACTION_DOWN, 500f, 500f)
        dispatch(view, first, first + 40L, MotionEvent.ACTION_UP, 500f, 500f)
        val second = first + 120L
        dispatch(view, second, second, MotionEvent.ACTION_DOWN, 500f, 500f)
        val steps = 8
        for (step in 1..steps) {
            dispatch(view, second, second + step * 30L, MotionEvent.ACTION_MOVE, 500f, 500f + (toY - 500f) * step / steps)
        }
        dispatch(view, second, second + (steps + 1) * 30L, MotionEvent.ACTION_UP, 500f, toY)
    }

    /** A quick single-finger drag in a few steps. */
    private fun dispatchSwipe(view: ZoomableImageView, fromX: Float, fromY: Float, toX: Float, toY: Float) {
        val down = SystemClock.uptimeMillis()
        dispatch(view, down, down, MotionEvent.ACTION_DOWN, fromX, fromY)
        val steps = 5
        for (step in 1..steps) {
            dispatch(view, down, down + step * 10L, MotionEvent.ACTION_MOVE, fromX + (toX - fromX) * step / steps, fromY + (toY - fromY) * step / steps)
        }
        dispatch(view, down, down + (steps + 1) * 10L, MotionEvent.ACTION_UP, toX, toY)
    }

    private fun dispatchPinchOpen(view: ZoomableImageView) {
        val down = SystemClock.uptimeMillis()
        val pointerDown = MotionEvent.ACTION_POINTER_DOWN or (1 shl MotionEvent.ACTION_POINTER_INDEX_SHIFT)
        val pointerUp = MotionEvent.ACTION_POINTER_UP or (1 shl MotionEvent.ACTION_POINTER_INDEX_SHIFT)
        dispatchPointers(view, down, down, MotionEvent.ACTION_DOWN, floatArrayOf(495f), floatArrayOf(495f))
        dispatchPointers(view, down, down + 10L, pointerDown, floatArrayOf(495f, 505f), floatArrayOf(495f, 505f))
        var spread = 5f
        for (step in 1..16) {
            spread *= 1.6f
            dispatchPointers(view, down, down + 10L + step * 10L, MotionEvent.ACTION_MOVE, floatArrayOf(500f - spread, 500f + spread), floatArrayOf(500f - spread, 500f + spread))
        }
        dispatchPointers(view, down, down + 200L, pointerUp, floatArrayOf(500f - spread, 500f + spread), floatArrayOf(500f - spread, 500f + spread))
        dispatchPointers(view, down, down + 210L, MotionEvent.ACTION_UP, floatArrayOf(500f - spread), floatArrayOf(500f - spread))
    }

    private fun dispatchPointers(view: ZoomableImageView, downTime: Long, eventTime: Long, action: Int, xs: FloatArray, ys: FloatArray) {
        val properties = Array(xs.size) { index -> MotionEvent.PointerProperties().apply { id = index; toolType = MotionEvent.TOOL_TYPE_FINGER } }
        val coordinates = Array(xs.size) { index -> MotionEvent.PointerCoords().apply { x = xs[index]; y = ys[index]; pressure = 1f; size = 1f } }
        val event = MotionEvent.obtain(downTime, eventTime, action, xs.size, properties, coordinates, 0, 0, 1f, 1f, 0, 0, InputDevice.SOURCE_TOUCHSCREEN, 0)
        try {
            view.onTouchEvent(event)
        } finally {
            event.recycle()
        }
    }

    private fun dispatch(view: ZoomableImageView, downTime: Long, eventTime: Long, action: Int, x: Float, y: Float) {
        val event = MotionEvent.obtain(downTime, eventTime, action, x, y, 0).apply { source = InputDevice.SOURCE_TOUCHSCREEN }
        try {
            view.onTouchEvent(event)
        } finally {
            event.recycle()
        }
    }

    // endregion

    private companion object {
        const val VIEW_SIZE = 1000
        const val TIMEOUT = 15_000L
    }
}
