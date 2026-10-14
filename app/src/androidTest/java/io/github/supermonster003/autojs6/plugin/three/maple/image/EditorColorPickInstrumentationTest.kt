package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.pinch
import androidx.compose.ui.test.swipe
import androidx.core.content.FileProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.UUID

/**
 * ROADMAP 8.1 (T16): a long press on the canvas shows the magnifier and letting go picks the bitmap pixel under the
 * finger, also after zooming about 3 times and panning; the color becomes the drawing color of brush and shapes.
 */
@RunWith(AndroidJUnit4::class)
class EditorColorPickInstrumentationTest {

    @get:Rule
    val compose = createEmptyComposeRule()

    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private lateinit var fixture: File
    private var session: String? = null

    /** Every pixel of the fixture has its own color. */
    private fun fixtureColor(x: Int, y: Int) = Color.rgb(x * 2, y * 3, (x * 7 + y * 5) % 256)

    @Before fun createSession() {
        fixture = File(context.cacheDir, "standalone-images/pick-${UUID.randomUUID()}.png")
        fixture.parentFile!!.mkdirs()
        val bitmap = Bitmap.createBitmap(120, 80, Bitmap.Config.ARGB_8888)
        for (y in 0 until 80) for (x in 0 until 120) bitmap.setPixel(x, y, fixtureColor(x, y))
        fixture.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()
        val uri = FileProvider.getUriForFile(context, context.packageName + ".standalone-images", fixture)
        session = StandaloneImageWork.create(context, StandaloneDocuments.inspect(context, uri), ImageToolsPlugin.EDIT_ACTION_ID)
    }

    @After fun discardSession() {
        session?.let { StandaloneImageWork.discard(context, it) }
        fixture.delete()
    }

    private fun launch(): ActivityScenario<StandaloneImageEditorActivity> {
        val intent = Intent(context, StandaloneImageEditorActivity::class.java).putExtra(StandaloneImageWork.EXTRA_SESSION, session)
        val scenario = ActivityScenario.launch<StandaloneImageEditorActivity>(intent)
        compose.waitUntil(TIMEOUT) { exists("maple.tool.crop") && enabled("maple.tool.crop") }
        return scenario
    }

    private fun exists(tag: String): Boolean = compose.onAllNodes(hasTestTag(tag)).fetchSemanticsNodes().isNotEmpty()

    private fun enabled(tag: String): Boolean = runCatching { compose.onNodeWithTag(tag).assertIsEnabled() }.isSuccess

    private fun <T> onActivity(scenario: ActivityScenario<StandaloneImageEditorActivity>, block: (StandaloneImageEditorActivity) -> T): T {
        var value: T? = null
        scenario.onActivity { value = block(it) }
        @Suppress("UNCHECKED_CAST")
        return value as T
    }

    private fun at(scenario: ActivityScenario<StandaloneImageEditorActivity>, x: Float, y: Float): Offset =
        onActivity(scenario) { it.editingViewForTesting.bitmapToView(x, y).let { point -> Offset(point.first, point.second) } }

    private fun pickingColor(scenario: ActivityScenario<StandaloneImageEditorActivity>): Int? =
        onActivity(scenario) { it.editingViewForTesting.pickingColor() }

    /** Holds a finger on bitmap pixel ([x], [y]) until the long press starts the magnifier. */
    private fun press(scenario: ActivityScenario<StandaloneImageEditorActivity>, x: Int, y: Int) {
        val point = at(scenario, x + 0.5f, y + 0.5f)
        compose.onNodeWithTag("maple.editor.canvas").performTouchInput { down(point) }
        compose.waitUntil(TIMEOUT) { pickingColor(scenario) != null }
    }

    @Test fun pickingAfterZoomAndPanReturnsTheBitmapPixel() {
        launch().use { scenario ->
            val canvas = compose.onNodeWithTag("maple.editor.canvas")
            canvas.performTouchInput {
                pinch(center - Offset(60f, 0f), center - Offset(180f, 0f), center + Offset(60f, 0f), center + Offset(180f, 0f))
            }
            canvas.performTouchInput { swipe(center, center + Offset(300f, 0f), durationMillis = 300) }
            compose.waitForIdle()
            val zoom = onActivity(scenario) { it.editingViewForTesting.zoom }
            assertTrue("zoom $zoom", zoom in 2.7f..3.3f)

            press(scenario, 37, 52)
            assertEquals(fixtureColor(37, 52), pickingColor(scenario))
            // The magnifier follows the finger; letting go picks where it ends.
            val next = at(scenario, 45.5f, 30.5f)
            canvas.performTouchInput { moveTo(next) }
            compose.waitForIdle()
            assertEquals(fixtureColor(45, 30), pickingColor(scenario))
            canvas.performTouchInput { up() }
            compose.waitForIdle()
            assertNull(pickingColor(scenario))
            assertEquals(fixtureColor(45, 30), onActivity(scenario) { it.drawingColorForTesting })
            // Picking changed nothing in the image and left no history step.
            assertTrue(!enabled("maple.editor.undo"))
        }
    }

    @Test fun aLongPressWithTheBrushPicksInsteadOfDrawing() {
        launch().use { scenario ->
            compose.onNodeWithTag("maple.editor.groups").performScrollToNode(hasTestTag("maple.tool.group.draw"))
            compose.onNodeWithTag("maple.tool.group.draw").performClick()
            compose.onNodeWithTag("maple.tool.brush").performClick()
            // The panel makes the canvas shorter: the image moves before positions are computed.
            compose.waitUntil(TIMEOUT) { exists("maple.editor.brushWidth") }
            compose.waitForIdle()
            press(scenario, 100, 10)
            compose.onNodeWithTag("maple.editor.canvas").performTouchInput { up() }
            compose.waitForIdle()
            assertEquals(fixtureColor(100, 10), onActivity(scenario) { it.drawingColorForTesting })
            // The dot the finger started is gone: confirming draws nothing and adds no history step.
            compose.onNodeWithTag("maple.panel.confirm").performClick()
            compose.waitUntil(TIMEOUT) { enabled("maple.tool.crop") || exists("maple.tool.brush") }
            assertTrue(!enabled("maple.editor.undo"))
        }
    }

    private companion object {
        const val TIMEOUT = 15_000L
    }
}
