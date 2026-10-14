package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import androidx.core.content.FileProvider
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.UUID
import kotlin.random.Random

/**
 * ROADMAP 8.3: a configuration change keeps the converter's draft, and a conversion that is running when the page is
 * recreated finishes and reports its result to the new page.
 */
@RunWith(AndroidJUnit4::class)
class ConverterRecreationInstrumentationTest {

    @get:Rule
    val compose = createEmptyComposeRule()

    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private val fixtures = mutableListOf<File>()
    private val sessions = mutableListOf<String>()

    @After fun cleanUp() {
        sessions.forEach { StandaloneImageWork.discard(context, it) }
        fixtures.forEach(File::delete)
    }

    private fun session(width: Int, height: Int, noisy: Boolean): String {
        val file = File(context.cacheDir, "standalone-images/recreate-${UUID.randomUUID()}.png").also { fixtures += it }
        file.parentFile!!.mkdirs()
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        if (noisy) {
            val random = Random(7)
            val row = IntArray(width)
            for (y in 0 until height) {
                for (x in 0 until width) row[x] = (0xFF000000.toInt()) or random.nextInt(0x1000000)
                bitmap.setPixels(row, 0, width, 0, y, width, 1)
            }
        } else {
            bitmap.eraseColor(0xff336699.toInt())
        }
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()
        val uri = FileProvider.getUriForFile(context, context.packageName + ".standalone-images", file)
        return StandaloneImageWork.create(context, StandaloneDocuments.inspect(context, uri), ImageToolsPlugin.CONVERT_ACTION_ID).also { sessions += it }
    }

    private fun launch(session: String): ActivityScenario<StandaloneImageConverterActivity> {
        val intent = Intent(context, StandaloneImageConverterActivity::class.java).putExtra(StandaloneImageWork.EXTRA_SESSION, session)
        val scenario = ActivityScenario.launchActivityForResult<StandaloneImageConverterActivity>(intent)
        awaitForm()
        return scenario
    }

    private fun awaitForm() {
        compose.waitUntil(TIMEOUT) { compose.onAllNodes(hasTestTag("maple.converter.convert")).fetchSemanticsNodes().isNotEmpty() }
        compose.waitUntil(TIMEOUT) { runCatching { compose.onNodeWithTag("maple.converter.convert").assertIsEnabled() }.isSuccess }
    }

    @Test fun theDraftSurvivesARecreation() {
        launch(session(400, 300, noisy = false)).use { scenario ->
            compose.onNodeWithTag("maple.converter.format.0").performClick()
            compose.onNodeWithTag("maple.converter.resize.1").performScrollTo().performClick()
            compose.onNodeWithTag("maple.converter.percentage").performScrollTo().performTextReplacement("37")
            compose.waitForIdle()
            scenario.recreate()
            awaitForm()
            compose.onNodeWithTag("maple.converter.format.0").assertIsSelected()
            compose.onNodeWithTag("maple.converter.percentage").performScrollTo().assertTextContains("37")
            compose.onNodeWithTag("maple.converter.resolution").assertTextContains("148", substring = true)
        }
    }

    @Test fun aRunningConversionFinishesAcrossARecreation() {
        val id = session(2000, 2000, noisy = true)
        launch(id).use { scenario ->
            // WebP with a target size searches the quality over several encodes: long enough to recreate meanwhile.
            compose.onNodeWithTag("maple.converter.format.2").performClick()
            compose.onNodeWithTag("maple.converter.targetEnabled").performScrollTo().performClick()
            compose.onNodeWithTag("maple.converter.targetFileSize").performScrollTo().performTextReplacement("300")
            compose.waitForIdle()
            compose.onNodeWithTag("maple.converter.convert").performClick()
            compose.waitUntil(TIMEOUT) { compose.onAllNodes(hasTestTag("maple.converter.progress")).fetchSemanticsNodes().isNotEmpty() }
            scenario.recreate()
            // Noise rarely fits the target: the new page then asks to save the closest result, which also proves the
            // search kept running across the recreation.
            compose.waitUntil(LONG_TIMEOUT) {
                scenario.state == Lifecycle.State.DESTROYED || compose.onAllNodes(hasTestTag("maple.dialog.confirm")).fetchSemanticsNodes().isNotEmpty()
            }
            if (scenario.state != Lifecycle.State.DESTROYED) compose.onNodeWithTag("maple.dialog.confirm").performClick()
            compose.waitUntil(LONG_TIMEOUT) { scenario.state == Lifecycle.State.DESTROYED }
            assertEquals(Activity.RESULT_OK, scenario.result.resultCode)
        }
        assertTrue(StandaloneImageWork.hasOutput(context, id))
        assertEquals("image/webp", StandaloneImageWork.outputMime(context, id))
    }

    private companion object {
        const val TIMEOUT = 15_000L
        const val LONG_TIMEOUT = 60_000L
    }
}
