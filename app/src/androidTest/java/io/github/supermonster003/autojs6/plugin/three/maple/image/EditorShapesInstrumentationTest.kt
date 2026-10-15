package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTouchInput
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

/** ROADMAP 8.1: annotation shapes are drawn by dragging, can be selected, moved, filled and deleted, and undo. */
@RunWith(AndroidJUnit4::class)
class EditorShapesInstrumentationTest {

    @get:Rule
    val compose = createEmptyComposeRule()

    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private lateinit var fixture: File
    private var session: String? = null

    @Before fun createSession() {
        fixture = File(context.cacheDir, "standalone-images/shapes-${UUID.randomUUID()}.png")
        fixture.parentFile!!.mkdirs()
        val bitmap = Bitmap.createBitmap(120, 80, Bitmap.Config.ARGB_8888).apply { eraseColor(Color.WHITE) }
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

    private fun at(scenario: ActivityScenario<StandaloneImageEditorActivity>, x: Float, y: Float): Offset {
        var value = Offset.Zero
        scenario.onActivity { activity -> activity.editingViewForTesting.bitmapToView(x, y).let { value = Offset(it.first, it.second) } }
        return value
    }

    private fun pixel(scenario: ActivityScenario<StandaloneImageEditorActivity>, x: Int, y: Int): Int {
        var value = 0
        scenario.onActivity { value = requireNotNull(it.editedBitmapForTesting).getPixel(x, y) }
        return value
    }

    private fun isRed(color: Int) = Color.red(color) > 230 && Color.green(color) < 30 && Color.blue(color) < 30

    private fun startShape(kind: String) {
        compose.onNodeWithTag("maple.editor.groups").performScrollToNode(hasTestTag("maple.tool.group.annotate"))
        compose.onNodeWithTag("maple.tool.group.annotate").performClick()
        compose.onNodeWithTag("maple.editor.tools").performScrollToNode(hasTestTag("maple.tool.shape.$kind"))
        compose.onNodeWithTag("maple.tool.shape.$kind").performClick()
        compose.waitUntil(TIMEOUT) { exists("maple.editor.shapeWidth") }
    }

    private fun drag(scenario: ActivityScenario<StandaloneImageEditorActivity>, fromX: Float, fromY: Float, toX: Float, toY: Float) {
        val from = at(scenario, fromX, fromY)
        val to = at(scenario, toX, toY)
        compose.onNodeWithTag("maple.editor.canvas").performTouchInput {
            down(from)
            moveTo(from + (to - from) / 2f)
            moveTo(to)
            up()
        }
        compose.waitForIdle()
    }

    private fun tap(scenario: ActivityScenario<StandaloneImageEditorActivity>, x: Float, y: Float) {
        val point = at(scenario, x, y)
        compose.onNodeWithTag("maple.editor.canvas").performTouchInput {
            down(point)
            up()
        }
        compose.waitForIdle()
    }

    private fun confirm() {
        compose.onNodeWithTag("maple.panel.confirm").performClick()
        compose.waitUntil(TIMEOUT) { enabled("maple.editor.undo") }
    }

    @Test fun aHollowRectangleIsDrawnAndUndone() {
        launch().use { scenario ->
            startShape("rectangle")
            drag(scenario, 20.5f, 20.5f, 100.5f, 60.5f)
            compose.onNodeWithTag("maple.editor.deleteShape").assertIsEnabled()
            confirm()
            assertTrue(isRed(pixel(scenario, 20, 40)))
            assertTrue(isRed(pixel(scenario, 60, 60)))
            assertEquals(Color.WHITE, pixel(scenario, 60, 40))

            compose.onNodeWithTag("maple.editor.undo").performClick()
            compose.waitUntil(TIMEOUT) { enabled("maple.editor.redo") }
            assertEquals(Color.WHITE, pixel(scenario, 20, 40))
        }
    }

    @Test fun aFilledEllipseStaysAndADeletedLineGoes() {
        launch().use { scenario ->
            startShape("ellipse")
            compose.onNodeWithTag("maple.editor.shapeFill.1").performClick()
            drag(scenario, 20f, 20f, 60f, 60f)
            compose.onNodeWithTag("maple.editor.shapeKind.${ShapeKind.LINE.ordinal}").performClick()
            // Lines have no fill.
            compose.waitUntil(TIMEOUT) { !exists("maple.editor.shapeFill.1") }
            drag(scenario, 80.5f, 10.5f, 80.5f, 70.5f)
            compose.onNodeWithTag("maple.editor.deleteShape").performClick()
            compose.onNodeWithTag("maple.editor.deleteShape").assertIsNotEnabled()
            confirm()
            assertTrue(isRed(pixel(scenario, 40, 40)))
            assertEquals(Color.WHITE, pixel(scenario, 80, 40))
        }
    }

    @Test fun aTapSelectsAShapeAndDraggingItMovesIt() {
        launch().use { scenario ->
            startShape("rectangle")
            drag(scenario, 20.5f, 20.5f, 60.5f, 60.5f)
            // A tap on an empty spot clears the selection and draws nothing.
            tap(scenario, 100f, 10f)
            compose.onNodeWithTag("maple.editor.deleteShape").assertIsNotEnabled()
            tap(scenario, 40f, 40f)
            compose.onNodeWithTag("maple.editor.deleteShape").assertIsEnabled()
            drag(scenario, 40f, 40f, 60f, 40f)
            confirm()
            assertTrue(isRed(pixel(scenario, 40, 30)))
            assertEquals(Color.WHITE, pixel(scenario, 20, 30))
            assertEquals(Color.WHITE, pixel(scenario, 100, 10))
        }
    }

    @Test fun anArrowHasAHeadAtItsEnd() {
        launch().use { scenario ->
            startShape("arrow")
            drag(scenario, 10.5f, 40.5f, 100.5f, 40.5f)
            confirm()
            assertTrue(isRed(pixel(scenario, 50, 40)))
            // The barbs spread around the tip, above and below the shaft.
            val headLength = ShapeGeometry.arrowHeadLength(
                EditorShape(ShapeKind.ARROW, 10.5f, 40.5f, 100.5f, 40.5f, Color.RED, strokeWidth = currentLineWidth(scenario)),
            )
            val barbY = (headLength * kotlin.math.sin(Math.toRadians(ShapeGeometry.ARROW_HEAD_ANGLE_DEGREES))).toInt()
            assertTrue("head length $headLength", barbY >= 2)
            assertTrue(isRed(pixel(scenario, 100 - headLength.toInt() / 2, 39)))
            assertEquals(Color.WHITE, pixel(scenario, 100 - headLength.toInt() / 2, 40 - barbY - 2))
        }
    }

    private fun currentLineWidth(scenario: ActivityScenario<StandaloneImageEditorActivity>): Float {
        var width = 0f
        scenario.onActivity { activity ->
            val density = activity.resources.displayMetrics.density
            val view = activity.editingViewForTesting
            val (x0, _) = view.bitmapToView(0f, 0f)
            val (x1, _) = view.bitmapToView(1f, 0f)
            // The default pen progress 6 plus the 2 dp minimum, converted to bitmap pixels.
            width = 8f * density / (x1 - x0)
        }
        return width
    }

    private companion object {
        const val TIMEOUT = 15_000L
    }
}
