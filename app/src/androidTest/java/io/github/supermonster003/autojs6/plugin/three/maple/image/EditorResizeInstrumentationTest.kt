package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextReplacement
import androidx.core.content.FileProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.UUID

/** ROADMAP 8.1: the editor resizes the image by percentage or width and height, as one history step each. */
@RunWith(AndroidJUnit4::class)
class EditorResizeInstrumentationTest {

    @get:Rule
    val compose = createEmptyComposeRule()

    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private lateinit var fixture: File
    private var session: String? = null

    @Before fun createSession() {
        fixture = File(context.cacheDir, "standalone-images/resize-${UUID.randomUUID()}.png")
        fixture.parentFile!!.mkdirs()
        val bitmap = Bitmap.createBitmap(120, 80, Bitmap.Config.ARGB_8888).apply { eraseColor(Color.rgb(30, 140, 220)) }
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

    private fun size(scenario: ActivityScenario<StandaloneImageEditorActivity>): Pair<Int, Int> {
        var value = 0 to 0
        scenario.onActivity { activity -> requireNotNull(activity.editedBitmapForTesting).let { value = it.width to it.height } }
        return value
    }

    private fun pixel(scenario: ActivityScenario<StandaloneImageEditorActivity>, x: Int, y: Int): Int {
        var value = 0
        scenario.onActivity { value = requireNotNull(it.editedBitmapForTesting).getPixel(x, y) }
        return value
    }

    private fun openResize() {
        compose.onNodeWithTag("maple.editor.tools").performScrollToNode(hasTestTag("maple.tool.resize"))
        compose.onNodeWithTag("maple.tool.resize").performClick()
        compose.waitUntil(TIMEOUT) { exists("maple.editor.resize") }
    }

    @Test fun percentageAndThenCustomSizeEachAddOneHistoryStep() {
        launch().use { scenario ->
            openResize()
            compose.onNodeWithTag("maple.editor.resize.result").assertTextContains("60 x 40", substring = true)
            compose.onNodeWithTag("maple.dialog.confirm").performClick()
            compose.waitUntil(TIMEOUT) { enabled("maple.editor.undo") }
            assertEquals(60 to 40, size(scenario))
            assertEquals(Color.rgb(30, 140, 220), pixel(scenario, 30, 20))

            openResize()
            compose.onNodeWithTag("maple.editor.resize.mode.2").performClick()
            compose.onNodeWithTag("maple.editor.resize.width").performTextReplacement("30")
            // The locked ratio follows with the height.
            compose.onNodeWithTag("maple.editor.resize.height").assertTextContains("20")
            compose.onNodeWithTag("maple.dialog.confirm").performClick()
            compose.waitUntil(TIMEOUT) { size(scenario) == 30 to 20 }

            compose.onNodeWithTag("maple.editor.undo").performClick()
            compose.waitUntil(TIMEOUT) { size(scenario) == 60 to 40 }
            compose.onNodeWithTag("maple.editor.undo").performClick()
            compose.waitUntil(TIMEOUT) { size(scenario) == 120 to 80 }
        }
    }

    @Test fun anInvalidPercentageCannotBeConfirmed() {
        launch().use { scenario ->
            openResize()
            compose.onNodeWithTag("maple.editor.resize.percentage").performTextReplacement("0")
            compose.onNodeWithTag("maple.dialog.confirm").assertIsNotEnabled()
            compose.onNodeWithTag("maple.editor.resize.percentage").performTextReplacement("150")
            compose.onNodeWithTag("maple.editor.resize.result").assertTextContains("180 x 120", substring = true)
            compose.onNodeWithTag("maple.dialog.cancel").performClick()
            compose.waitUntil(TIMEOUT) { !exists("maple.editor.resize") }
            assertEquals(120 to 80, size(scenario))
        }
    }

    private companion object {
        const val TIMEOUT = 15_000L
    }
}
