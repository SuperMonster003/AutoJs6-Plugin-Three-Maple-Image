package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
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

/** Compose converter page over the unchanged conversion model, driven through a private standalone session. */
@RunWith(AndroidJUnit4::class)
class ImageConverterComposeInstrumentationTest {

    @get:Rule
    val compose = createEmptyComposeRule()

    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private lateinit var fixture: File
    private var session: String? = null

    @Before fun createSession() {
        fixture = File(context.cacheDir, "standalone-images/converter-${UUID.randomUUID()}.png")
        fixture.parentFile!!.mkdirs()
        val bitmap = Bitmap.createBitmap(400, 300, Bitmap.Config.ARGB_8888).apply { eraseColor(0xff336699.toInt()) }
        fixture.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()
        val uri = FileProvider.getUriForFile(context, context.packageName + ".standalone-images", fixture)
        session = StandaloneImageWork.create(context, StandaloneDocuments.inspect(context, uri), ImageToolsPlugin.CONVERT_ACTION_ID)
    }

    @After fun discardSession() {
        session?.let { StandaloneImageWork.discard(context, it) }
        fixture.delete()
    }

    private fun launch(): ActivityScenario<StandaloneImageConverterActivity> {
        val intent = Intent(context, StandaloneImageConverterActivity::class.java).putExtra(StandaloneImageWork.EXTRA_SESSION, session)
        val scenario = ActivityScenario.launch<StandaloneImageConverterActivity>(intent)
        compose.waitUntil(10_000) { compose.onAllNodes(hasTestTag("maple.converter.convert")).fetchSemanticsNodes().isNotEmpty() }
        compose.waitUntil(10_000) { runCatching { compose.onNodeWithTag("maple.converter.convert").assertIsEnabled() }.isSuccess }
        return scenario
    }

    @Test fun resizeModesShowOnlyTheirInputsAndValidateLive() {
        launch().use {
            compose.onNodeWithTag("maple.converter.resolution")
                .assertTextEquals(context.getString(R.string.text_image_conversion_output_resolution, 400, 300))
            compose.onNodeWithTag("maple.converter.percentage").assertDoesNotExist()
            compose.onNodeWithTag("maple.converter.resize.1").performClick()
            compose.onNodeWithTag("maple.converter.percentage").assertIsDisplayed()
            compose.onNodeWithTag("maple.converter.longEdge").assertDoesNotExist()
            compose.onNodeWithTag("maple.converter.percentage").performTextClearance()
            compose.onNodeWithTag("maple.converter.percentage").performTextInput("50")
            compose.onNodeWithTag("maple.converter.resolution")
                .assertTextEquals(context.getString(R.string.text_image_conversion_output_resolution, 200, 150))
            compose.onNodeWithTag("maple.converter.convert").assertIsEnabled()
            compose.onNodeWithTag("maple.converter.percentage").performTextClearance()
            compose.onNodeWithTag("maple.converter.percentage").performTextInput("0")
            compose.onNodeWithTag("maple.converter.convert").assertIsNotEnabled()
            compose.onNodeWithTag("maple.converter.resolution")
                .assertTextEquals(context.getString(R.string.text_image_conversion_output_resolution_unavailable))
            compose.onNodeWithTag("maple.converter.resize.3").performClick()
            compose.onNodeWithTag("maple.converter.width").assertIsDisplayed()
            compose.onNodeWithTag("maple.converter.height").assertIsDisplayed()
            compose.onNodeWithTag("maple.converter.lockAspect").performClick()
            compose.onNodeWithTag("maple.converter.width").performTextClearance()
            compose.onNodeWithTag("maple.converter.width").performTextInput("200")
            compose.onNodeWithTag("maple.converter.resolution")
                .assertTextEquals(context.getString(R.string.text_image_conversion_output_resolution, 200, 150))
            compose.onNodeWithTag("maple.converter.convert").assertIsEnabled()
        }
    }

    @Test fun formatDrivesQualityTargetSizeAndBackgroundControlsAndSurvivesRecreation() {
        launch().use { scenario ->
            compose.onNodeWithTag("maple.converter.format.1").assertIsSelected()
            compose.onNodeWithTag("maple.converter.targetEnabled").assertDoesNotExist()
            compose.onNodeWithTag("maple.converter.background.0").assertDoesNotExist()
            compose.onNodeWithTag("maple.converter.format.0").performClick()
            compose.onNodeWithTag("maple.converter.background.0").assertIsSelected()
            compose.onNodeWithTag("maple.converter.targetEnabled").performClick()
            compose.onNodeWithTag("maple.converter.targetFileSize").assertIsDisplayed()
            compose.onNodeWithTag("maple.converter.targetFileSize").performTextClearance()
            compose.onNodeWithTag("maple.converter.targetFileSize").performTextInput("0")
            compose.onNodeWithTag("maple.converter.convert").assertIsNotEnabled()
            compose.onNodeWithTag("maple.converter.targetFileSize").performTextClearance()
            compose.onNodeWithTag("maple.converter.targetFileSize").performTextInput("64")
            compose.onNodeWithTag("maple.converter.convert").assertIsEnabled()
            scenario.recreate()
            compose.waitUntil(10_000) { compose.onAllNodes(hasTestTag("maple.converter.targetFileSize")).fetchSemanticsNodes().isNotEmpty() }
            compose.onNodeWithTag("maple.converter.format.0").assertIsSelected()
            compose.onNodeWithTag("maple.converter.targetFileSize").assert(hasText("64"))
            scenario.onActivity { assertEquals(false, it.isFinishing) }
        }
    }
}
