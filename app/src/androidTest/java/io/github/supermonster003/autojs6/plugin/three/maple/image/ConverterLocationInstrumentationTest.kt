package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.app.Activity
import android.content.Intent
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.UUID

/**
 * ROADMAP 9.1 (T14): with "Show location" on, the converter offers "Keep location", and the converted image then
 * carries the position of the original; without the setting there is no such switch and the position is removed.
 */
@RunWith(AndroidJUnit4::class)
class ConverterLocationInstrumentationTest {

    @get:Rule
    val compose = createEmptyComposeRule()

    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private val fixtures = mutableListOf<File>()
    private val sessions = mutableListOf<String>()

    @After fun cleanUp() {
        ApplicationSettingsStore(context).saveShowLocation(false)
        sessions.forEach { StandaloneImageWork.discard(context, it) }
        fixtures.forEach(File::delete)
    }

    private fun session(): String {
        val file = File(context.cacheDir, "standalone-images/located-${UUID.randomUUID()}.jpg").also { fixtures += it }
        file.parentFile!!.mkdirs()
        ExifJpegTestFixture.writeCompleteTo(file)
        val uri = FileProvider.getUriForFile(context, context.packageName + ".standalone-images", file)
        return StandaloneImageWork.create(context, StandaloneDocuments.inspect(context, uri), ImageToolsPlugin.CONVERT_ACTION_ID).also { sessions += it }
    }

    private fun convert(id: String, keepLocation: Boolean?) {
        val intent = Intent(context, StandaloneImageConverterActivity::class.java).putExtra(StandaloneImageWork.EXTRA_SESSION, id)
        ActivityScenario.launchActivityForResult<StandaloneImageConverterActivity>(intent).use { scenario ->
            compose.waitUntil(TIMEOUT) { exists("maple.converter.convert") }
            if (keepLocation == null) {
                assertEquals(false, exists("maple.converter.location"))
            } else {
                compose.onNodeWithTag("maple.converter.location").performScrollTo().assertIsOff()
                if (keepLocation) compose.onNodeWithTag("maple.converter.location").performClick()
            }
            compose.onNodeWithTag("maple.converter.format.0").performClick()
            compose.waitForIdle()
            compose.onNodeWithTag("maple.converter.convert").performClick()
            compose.waitUntil(TIMEOUT) { scenario.state == Lifecycle.State.DESTROYED }
            assertEquals(Activity.RESULT_OK, scenario.result.resultCode)
        }
    }

    private fun exists(tag: String): Boolean =
        runCatching { compose.onAllNodes(hasTestTag(tag)).fetchSemanticsNodes().isNotEmpty() }.getOrDefault(false)

    private fun outputLatLong(id: String): DoubleArray? =
        ExifInterface(File(File(context.cacheDir, "standalone-images/$id"), "output.bin").absolutePath).latLong

    @Test fun keepLocationCarriesThePositionIntoTheOutput() {
        ApplicationSettingsStore(context).saveShowLocation(true)
        val id = session()
        convert(id, keepLocation = true)
        val latLong = requireNotNull(outputLatLong(id)) { "the output lost the position" }
        assertEquals(ExifJpegTestFixture.LATITUDE, latLong[0], 1e-4)
        assertEquals(ExifJpegTestFixture.LONGITUDE, latLong[1], 1e-4)
    }

    @Test fun leftOffTheSwitchRemovesThePosition() {
        ApplicationSettingsStore(context).saveShowLocation(true)
        val id = session()
        convert(id, keepLocation = false)
        assertNull(outputLatLong(id))
    }

    @Test fun withoutShowLocationThereIsNoSwitchAndNoPosition() {
        ApplicationSettingsStore(context).saveShowLocation(false)
        val id = session()
        convert(id, keepLocation = null)
        assertNull(outputLatLong(id))
    }

    private companion object {
        const val TIMEOUT = 20_000L
    }
}
