package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performSemanticsAction
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
 * ROADMAP 8.1: the look panel previews a filter and adjustments together and commits them as one history step; a
 * look with a pixel pass (vignette) is applied to the full image as well.
 */
@RunWith(AndroidJUnit4::class)
class EditorLookInstrumentationTest {

    @get:Rule
    val compose = createEmptyComposeRule()

    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private lateinit var fixture: File
    private var session: String? = null

    @Before fun createSession() {
        fixture = File(context.cacheDir, "standalone-images/look-${UUID.randomUUID()}.png")
        fixture.parentFile!!.mkdirs()
        val bitmap = Bitmap.createBitmap(120, 80, Bitmap.Config.ARGB_8888).apply { eraseColor(Color.rgb(200, 90, 40)) }
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

    private fun pixel(scenario: ActivityScenario<StandaloneImageEditorActivity>, x: Int, y: Int): Int {
        var value = 0
        scenario.onActivity { value = requireNotNull(it.editedBitmapForTesting).getPixel(x, y) }
        return value
    }

    @Test fun aFilterAndAnAdjustmentApplyAsOneHistoryStep() {
        launch().use { scenario ->
            compose.onNodeWithTag("maple.editor.groups").performScrollToNode(hasTestTag("maple.tool.group.filter"))
            compose.onNodeWithTag("maple.tool.group.filter").performClick()
            compose.waitUntil(TIMEOUT) { exists("maple.editor.filter.${FilterPreset.MONO.ordinal}") }
            compose.onNodeWithTag("maple.editor.filters").performScrollToNode(hasTestTag("maple.editor.filter.${FilterPreset.MONO.ordinal}"))
            compose.onNodeWithTag("maple.editor.filter.${FilterPreset.MONO.ordinal}").performClick()
            compose.onNodeWithTag("maple.editor.filterIntensity").assertIsDisplayed()
            compose.onNodeWithTag("maple.editor.lookTab.1").performClick()
            compose.onNodeWithTag("maple.editor.adjustments").performScrollToNode(hasTestTag("maple.chip.${LookAdjustment.EXPOSURE.ordinal}"))
            compose.onNodeWithTag("maple.chip.${LookAdjustment.EXPOSURE.ordinal}").performClick()
            val sliderTag = "maple.slider." + context.getString(R.string.image_editor_exposure)
            compose.onNodeWithTag(sliderTag).performSemanticsAction(SemanticsActions.SetProgress) { it(-40f) }
            compose.waitForIdle()
            compose.onNodeWithTag("maple.panel.confirm").performClick()
            compose.waitUntil(TIMEOUT) { enabled("maple.editor.undo") }

            val after = pixel(scenario, 60, 40)
            assertEquals("gray after the mono filter", Color.red(after), Color.blue(after))
            val expected = LookMath.apply(EditorLook(FilterPreset.MONO).with(LookAdjustment.EXPOSURE, -40).colorMatrix(), 200, 90, 40, 255)
            assertEquals(expected[0].toFloat(), Color.red(after).toFloat(), 2f)

            // One undo brings back the original colors: the filter and the exposure were a single step.
            compose.onNodeWithTag("maple.editor.undo").performClick()
            compose.waitUntil(TIMEOUT) { enabled("maple.editor.redo") && !enabled("maple.editor.undo") }
            assertEquals(Color.rgb(200, 90, 40), pixel(scenario, 60, 40))
        }
    }

    @Test fun aVignetteGoesThroughThePixelPassAndDarkensTheCorners() {
        launch().use { scenario ->
            compose.onNodeWithTag("maple.tool.group.adjust").performClick()
            compose.onNodeWithTag("maple.editor.tools").performScrollToNode(hasTestTag("maple.tool.vignette"))
            compose.onNodeWithTag("maple.tool.vignette").performClick()
            val sliderTag = "maple.slider." + context.getString(R.string.image_editor_vignette)
            compose.onNodeWithTag(sliderTag).performSemanticsAction(SemanticsActions.SetProgress) { it(100f) }
            compose.waitForIdle()
            compose.onNodeWithTag("maple.panel.confirm").performClick()
            compose.waitUntil(TIMEOUT) { enabled("maple.editor.undo") }
            val center = pixel(scenario, 60, 40)
            val corner = pixel(scenario, 0, 0)
            assertEquals(Color.rgb(200, 90, 40), center)
            assertTrue("corner ${Integer.toHexString(corner)}", Color.red(corner) < 200 * 0.5)
        }
    }

    private companion object {
        const val TIMEOUT = 15_000L
    }
}
