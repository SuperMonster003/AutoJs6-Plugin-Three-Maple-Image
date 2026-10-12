package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextInput
import androidx.core.content.FileProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.UUID

/** Compose editor chrome on top of the unchanged tool state machine, driven through a private standalone session. */
@RunWith(AndroidJUnit4::class)
class ImageEditorComposeInstrumentationTest {

    @get:Rule
    val compose = createEmptyComposeRule()

    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private lateinit var fixture: File
    private var session: String? = null

    @Before fun createSession() {
        fixture = File(context.cacheDir, "standalone-images/editor-${UUID.randomUUID()}.png")
        fixture.parentFile!!.mkdirs()
        val bitmap = Bitmap.createBitmap(96, 64, Bitmap.Config.ARGB_8888).apply { eraseColor(0xff336699.toInt()) }
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
        compose.waitUntil(10_000) { exists("maple.tool.crop") && enabled("maple.tool.crop") }
        return scenario
    }

    private fun exists(tag: String): Boolean = compose.onAllNodes(hasTestTag(tag)).fetchSemanticsNodes().isNotEmpty()

    private fun enabled(tag: String): Boolean = runCatching { compose.onNodeWithTag(tag).assertIsEnabled() }.isSuccess

    @Test fun adjustmentPanelCommitsOnlyNonZeroValuesAndEnablesUndo() {
        launch().use {
            compose.onNodeWithTag("maple.editor.undo").assertIsNotEnabled()
            compose.onNodeWithTag("maple.editor.save").assertIsNotEnabled()
            compose.onNodeWithTag("maple.tool.group.adjust").performClick()
            compose.onNodeWithTag("maple.tool.brightness").assertIsDisplayed().performClick()
            compose.onNodeWithTag("maple.editor.adjustment").assertIsDisplayed()
            compose.onNodeWithTag("maple.panel.confirm").performClick()
            compose.onNodeWithTag("maple.tool.brightness").assertIsDisplayed()
            compose.onNodeWithTag("maple.editor.undo").assertIsNotEnabled()

            compose.onNodeWithTag("maple.tool.brightness").performClick()
            val sliderTag = "maple.slider." + context.getString(R.string.image_editor_brightness)
            compose.onNodeWithTag(sliderTag).performSemanticsAction(SemanticsActions.SetProgress) { it(40f) }
            compose.waitForIdle()
            compose.onNodeWithTag("maple.panel.confirm").performClick()
            compose.waitUntil(10_000) { enabled("maple.editor.undo") }
            compose.onNodeWithTag("maple.editor.save").assertIsEnabled()
            compose.onNodeWithTag("maple.editor.undo").performClick()
            compose.waitUntil(10_000) { enabled("maple.editor.redo") }
            compose.onNodeWithTag("maple.editor.save").assertIsNotEnabled()
        }
    }

    @Test fun textDialogLeadsToPlacementAndCancelReturnsToTheTools() {
        launch().use {
            compose.onNodeWithTag("maple.tool.group.text").performClick()
            compose.onNodeWithTag("maple.tool.text").performClick()
            compose.onNodeWithTag("maple.dialog.confirm").assertIsNotEnabled()
            compose.onNodeWithTag("maple.editor.text.input").performTextInput("Maple")
            compose.onNodeWithTag("maple.dialog.confirm").assertIsEnabled().performClick()
            compose.onNodeWithTag("maple.editor.help").assertIsDisplayed()
                .assertTextEquals(context.getString(R.string.image_editor_text_position_help))
            compose.onNodeWithTag("maple.panel.cancel").performClick()
            compose.onNodeWithTag("maple.tool.text").assertIsDisplayed()
            compose.onNodeWithTag("maple.editor.undo").assertIsNotEnabled()
        }
    }

    @Test fun saveDialogListsFormatsAndBackAsksBeforeDiscardingChanges() {
        launch().use { scenario ->
            compose.onNodeWithTag("maple.tool.rotateRight").performClick()
            compose.waitUntil(10_000) { enabled("maple.editor.save") }
            compose.onNodeWithTag("maple.editor.save").performClick()
            compose.onNodeWithText("PNG").assertIsDisplayed()
            compose.onNodeWithText("JPEG").assertIsDisplayed()
            compose.onNodeWithTag("maple.dialog.cancel").performClick()
            compose.onNodeWithTag("maple.tool.rotateRight").assertIsDisplayed()
            scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
            compose.onNodeWithText(context.getString(R.string.text_unsaved_changes)).assertIsDisplayed()
            compose.onNodeWithTag("maple.dialog.cancel").performClick()
            compose.onNodeWithTag("maple.tool.rotateRight").assertIsDisplayed()
        }
    }
}
