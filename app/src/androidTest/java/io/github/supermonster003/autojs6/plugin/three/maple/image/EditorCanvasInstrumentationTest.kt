package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.doubleClick
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.pinch
import androidx.compose.ui.test.swipe
import androidx.core.content.FileProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.UUID

/**
 * ROADMAP 8.1: holding "compare" shows the original, the canvas zooms and pans, tools hit the right pixels while
 * zoomed, a second finger turns a brush stroke into a zoom, and every touch target of the editor (all six groups) is at least 48 dp.
 */
@RunWith(AndroidJUnit4::class)
class EditorCanvasInstrumentationTest {

    @get:Rule
    val compose = createEmptyComposeRule()

    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private lateinit var fixture: File
    private var session: String? = null

    @Before fun createSession() {
        fixture = File(context.cacheDir, "standalone-images/canvas-${UUID.randomUUID()}.png")
        fixture.parentFile!!.mkdirs()
        // Left half red, right half blue.
        val bitmap = Bitmap.createBitmap(120, 80, Bitmap.Config.ARGB_8888).apply {
            eraseColor(Color.BLUE)
            Canvas(this).drawRect(0f, 0f, 60f, 80f, android.graphics.Paint().apply { color = Color.RED })
        }
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

    private fun exists(tag: String): Boolean =
        runCatching { compose.onAllNodes(hasTestTag(tag)).fetchSemanticsNodes().isNotEmpty() }.getOrDefault(false)

    private fun enabled(tag: String): Boolean = runCatching { compose.onNodeWithTag(tag).assertIsEnabled() }.isSuccess

    private fun <T> onView(scenario: ActivityScenario<StandaloneImageEditorActivity>, block: (ImageEditingView) -> T): T {
        var value: T? = null
        scenario.onActivity { value = block(it.editingViewForTesting) }
        @Suppress("UNCHECKED_CAST")
        return value as T
    }

    /** Where bitmap pixel ([x], [y]) is on the canvas node, in pixels. */
    private fun at(scenario: ActivityScenario<StandaloneImageEditorActivity>, x: Float, y: Float): Offset =
        onView(scenario) { view -> view.bitmapToView(x, y).let { Offset(it.first, it.second) } }

    /** The color the canvas shows at bitmap pixel ([x], [y]), read from a software render of the view. */
    private fun shown(scenario: ActivityScenario<StandaloneImageEditorActivity>, x: Float, y: Float): Int = onView(scenario) { view ->
        val render = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        view.draw(Canvas(render))
        val (viewX, viewY) = view.bitmapToView(x, y)
        render.getPixel(viewX.toInt(), viewY.toInt()).also { render.recycle() }
    }

    private fun pixel(scenario: ActivityScenario<StandaloneImageEditorActivity>, x: Int, y: Int): Int {
        var value = 0
        scenario.onActivity { value = requireNotNull(it.editedBitmapForTesting).getPixel(x, y) }
        return value
    }

    @Test fun holdingCompareShowsTheOriginal() {
        launch().use { scenario ->
            compose.onNodeWithTag("maple.tool.flipHorizontal").performClick()
            compose.waitUntil(TIMEOUT) { enabled("maple.editor.undo") }
            assertEquals(Color.BLUE, shown(scenario, 10f, 40f))

            compose.onNodeWithTag("maple.editor.compare").performTouchInput { down(center) }
            compose.waitUntil(TIMEOUT) { exists("maple.editor.originalLabel") }
            assertEquals(Color.RED, shown(scenario, 10f, 40f))
            compose.onNodeWithTag("maple.editor.compare").performTouchInput { up() }
            compose.waitUntil(TIMEOUT) { !exists("maple.editor.originalLabel") }
            assertEquals(Color.BLUE, shown(scenario, 10f, 40f))

            // Accessibility services switch it on and off.
            compose.onNodeWithTag("maple.editor.compare").performSemanticsAction(SemanticsActions.OnClick)
            compose.waitUntil(TIMEOUT) { exists("maple.editor.originalLabel") }
            compose.onNodeWithTag("maple.editor.compare").performSemanticsAction(SemanticsActions.OnClick)
            compose.waitUntil(TIMEOUT) { !exists("maple.editor.originalLabel") }
        }
    }

    @Test fun pinchZoomsPanMovesAndDoubleTapFitsAgain() {
        launch().use { scenario ->
            val canvas = compose.onNodeWithTag("maple.editor.canvas")
            canvas.performTouchInput {
                pinch(center - Offset(60f, 0f), center - Offset(240f, 0f), center + Offset(60f, 0f), center + Offset(240f, 0f))
            }
            compose.waitForIdle()
            val zoom = onView(scenario) { it.zoom }
            assertTrue("zoom $zoom", zoom > 2f)

            val before = at(scenario, 60f, 40f)
            canvas.performTouchInput { swipe(center, center + Offset(150f, 0f), durationMillis = 300) }
            compose.waitForIdle()
            val after = at(scenario, 60f, 40f)
            assertTrue("moved from $before to $after", after.x - before.x > 100f)

            canvas.performTouchInput { doubleClick(center) }
            compose.waitUntil(TIMEOUT) { onView(scenario) { it.zoom } == 1f }
        }
    }

    @Test fun aBrushStrokeLandsOnTheTouchedPixelWhileZoomed() {
        launch().use { scenario ->
            compose.onNodeWithTag("maple.editor.groups").performScrollToNode(hasTestTag("maple.tool.group.draw"))
            compose.onNodeWithTag("maple.tool.group.draw").performClick()
            compose.onNodeWithTag("maple.tool.brush").performClick()
            val canvas = compose.onNodeWithTag("maple.editor.canvas")
            // Two fingers zoom while the brush is active (about 1.5 times: bitmap x 20 to 100 stays on screen).
            canvas.performTouchInput {
                pinch(center - Offset(100f, 0f), center - Offset(150f, 0f), center + Offset(100f, 0f), center + Offset(150f, 0f))
            }
            compose.waitForIdle()
            assertTrue(onView(scenario) { it.zoom } > 1.3f)

            // A second finger during a stroke drops the stroke.
            val start = at(scenario, 70f, 20f)
            canvas.performTouchInput {
                down(0, start)
                moveTo(0, start + Offset(20f, 0f))
                down(1, start + Offset(0f, 200f))
                moveTo(1, start + Offset(0f, 210f))
                up(1)
                up(0)
            }
            // One finger draws where the bitmap pixels are shown: a short line through the centers of row 60.
            val from = at(scenario, 65.5f, 60.5f)
            val to = at(scenario, 75.5f, 60.5f)
            canvas.performTouchInput {
                down(from)
                moveTo(to)
                up()
            }
            compose.waitForIdle()
            compose.onNodeWithTag("maple.panel.confirm").performClick()
            compose.waitUntil(TIMEOUT) { enabled("maple.editor.undo") }
            val drawn = pixel(scenario, 70, 60)
            assertTrue("drawn ${Integer.toHexString(drawn)}", Color.red(drawn) > 230 && Color.blue(drawn) < 30)
            assertEquals(Color.BLUE, pixel(scenario, 70, 20))
            assertEquals(Color.BLUE, pixel(scenario, 90, 60))
        }
    }

    @Test fun everyTouchTargetIsAtLeast48Dp() {
        launch().use {
            val minimum = 48f * context.resources.displayMetrics.density - 1f
            val small = mutableListOf<String>()
            listOf("transform", "adjust", "filter", "draw", "annotate", "text").forEach { group ->
                compose.onNodeWithTag("maple.editor.groups").performScrollToNode(hasTestTag("maple.tool.group.$group"))
                compose.onNodeWithTag("maple.tool.group.$group").performClick()
                compose.waitForIdle()
                compose.onAllNodes(hasClickAction()).fetchSemanticsNodes().forEach { node ->
                    val bounds = node.touchBoundsInRoot
                    if (bounds.width < minimum || bounds.height < minimum) small += "$group ${node.config} ${bounds.size}"
                }
            }
            assertTrue(small.joinToString("\n"), small.isEmpty())
        }
    }

    private companion object {
        const val TIMEOUT = 15_000L
    }
}
