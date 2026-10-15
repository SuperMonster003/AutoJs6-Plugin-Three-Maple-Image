package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.hasTestTag
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
 * ROADMAP 9.1: the extended details replace the metadata line with the chosen fields, wrap a long path below the
 * thumbnail strip without covering it, hide with the bars and follow the settings, whose choice is kept.
 */
@RunWith(AndroidJUnit4::class)
class ExtendedDetailsInstrumentationTest {

    @get:Rule
    val compose = createEmptyComposeRule()

    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private val store get() = ApplicationSettingsStore(context)
    private lateinit var original: ApplicationSettings
    private lateinit var directory: File

    @Before fun prepare() {
        original = store.load()
        directory = File(context.cacheDir, "standalone-images/details-${UUID.randomUUID()}").apply { mkdirs() }
        ExifJpegTestFixture.writeCompleteTo(File(directory, "photo.jpg"))
        val bitmap = Bitmap.createBitmap(40, 30, Bitmap.Config.ARGB_8888).apply { eraseColor(Color.GREEN) }
        File(directory, "plain.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()
    }

    @After fun restore() {
        store.save(original)
        directory.deleteRecursively()
    }

    private fun launchViewer(): ActivityScenario<ImageViewerActivity> {
        val pages = listOf("photo.jpg" to LONG_NAME, "plain.png" to "plain.png").map { (file, name) ->
            val target = File(directory, file)
            val uri = FileProvider.getUriForFile(context, context.packageName + ".standalone-images", target)
            ImageViewerRequest(uri, name, target.length(), if (file.endsWith(".jpg")) "image/jpeg" else "image/png")
        }
        return ActivityScenario.launchActivityForResult(ImageViewerContract.viewerIntent(context, ImageViewerLaunchRequest.explicitSelection(pages)))
    }

    private fun ActivityScenario<ImageViewerActivity>.state(): ViewerUiState {
        var state: ViewerUiState? = null
        onActivity { state = it.viewerStateForTesting }
        return requireNotNull(state)
    }

    private fun exists(tag: String): Boolean =
        runCatching { compose.onAllNodes(hasTestTag(tag)).fetchSemanticsNodes().isNotEmpty() }.getOrDefault(false)

    @Test fun theChosenFieldsReplaceTheMetadataLineAndFollowTheSettings() {
        store.save(original.copy(extendedDetails = true, extendedDetailFields = ExtendedDetailField.entries.toSet(), showLocation = false))
        launchViewer().use { scenario ->
            compose.waitUntil(TIMEOUT) { scenario.state().extendedDetails.isNotEmpty() && exists("maple.viewer.extendedDetails") }
            val lines = scenario.state().extendedDetails
            assertEquals(LONG_NAME, lines[0])
            val captured = requireNotNull(ImageExifValueFormatter.capturedAt(ExifJpegTestFixture.CAPTURED_AT))
            assertTrue(lines.toString(), lines[1].contains(captured))
            assertTrue(lines.toString(), lines[2].startsWith("${ExifJpegTestFixture.MAKE} ${ExifJpegTestFixture.MODEL}"))
            assertTrue(lines.toString(), listOf("1/125 s", "f/1.8", "4.25 mm", "ISO 200").all { lines[2].contains(it) })
            assertEquals("no location while Show location is off", 3, lines.size)
            assertFalse(exists("maple.viewer.metadata"))

            // A path that wraps lifts the strip: the details start below it.
            val strip = compose.onNodeWithTag("maple.viewer.strip").fetchSemanticsNode().boundsInRoot
            val details = compose.onNodeWithTag("maple.viewer.extendedDetails").fetchSemanticsNode().boundsInRoot
            assertTrue("strip $strip, details $details", strip.bottom <= details.top + 0.5f)
            assertTrue("the path wraps", details.height > strip.height)

            scenario.onActivity { it.toggleControlsForTesting() }
            compose.waitUntil(TIMEOUT) { !exists("maple.viewer.extendedDetails") }
            scenario.onActivity { it.toggleControlsForTesting() }
            compose.waitUntil(TIMEOUT) { exists("maple.viewer.extendedDetails") }

            // Settings changed elsewhere apply when the viewer comes back.
            store.saveShowLocation(true)
            scenario.moveToState(Lifecycle.State.STARTED)
            scenario.moveToState(Lifecycle.State.RESUMED)
            compose.waitUntil(TIMEOUT) { scenario.state().extendedDetails.size == 4 }
            // The EXIF rationals round the fixture's position a little; the decimal form starts the same.
            val location = scenario.state().extendedDetails.last()
            assertTrue(location, location.startsWith("37.42") && location.contains(", -122.08"))
            store.saveExtendedDetails(false)
            scenario.moveToState(Lifecycle.State.STARTED)
            scenario.moveToState(Lifecycle.State.RESUMED)
            compose.waitUntil(TIMEOUT) { scenario.state().extendedDetails.isEmpty() && exists("maple.viewer.metadata") }
        }
    }

    @Test fun theFieldChoiceIsKeptAndTheLocationWaitsForShowLocation() {
        store.save(original.copy(extendedDetails = false, extendedDetailFields = ExtendedDetailsPolicy.SUGGESTED, showLocation = false))
        ActivityScenario.launch(AppSettingsActivity::class.java).use { scenario ->
            compose.onNodeWithTag("maple.settings.extendedDetailFields").performScrollTo().assertIsNotEnabled()
            compose.onNodeWithTag("maple.settings.extendedDetails").performScrollTo().performClick()
            compose.onNodeWithTag("maple.settings.extendedDetailFields").assertIsEnabled().performClick()
            ExtendedDetailField.entries.forEachIndexed { index, field ->
                val row = compose.onNodeWithTag("maple.multiChoice.$index").performScrollTo()
                if (field in ExtendedDetailsPolicy.SUGGESTED) row.assertIsOn() else row.assertIsOff()
            }
            compose.onNodeWithTag("maple.multiChoice.${ExtendedDetailField.LOCATION.ordinal}").assertIsNotEnabled()
            compose.onNodeWithTag("maple.multiChoice.${ExtendedDetailField.PATH.ordinal}").performScrollTo().performClick()
            compose.onNodeWithTag("maple.multiChoice.${ExtendedDetailField.CAMERA.ordinal}").performScrollTo().performClick()
            compose.onNodeWithTag("maple.dialog.confirm").performClick()
            compose.waitForIdle()
            val expected = ExtendedDetailsPolicy.SUGGESTED - ExtendedDetailField.CAMERA + ExtendedDetailField.PATH
            assertEquals(true, store.load().extendedDetails)
            assertEquals(expected, store.load().extendedDetailFields)

            // Unticking everything leaves nothing to confirm.
            compose.onNodeWithTag("maple.settings.extendedDetailFields").performClick()
            expected.forEach { compose.onNodeWithTag("maple.multiChoice.${it.ordinal}").performScrollTo().performClick() }
            compose.onNodeWithTag("maple.dialog.confirm").assertIsNotEnabled()
            compose.onNodeWithTag("maple.dialog.cancel").performClick()
            // Closed before the recreation, or the dialog would save its pending choice for the next one.
            compose.waitForIdle()
            scenario.recreate()
            compose.onNodeWithTag("maple.settings.extendedDetails").performScrollTo().assertIsOn()
            compose.onNodeWithTag("maple.settings.extendedDetailFields").performClick()
            ExtendedDetailField.entries.forEachIndexed { index, field ->
                val row = compose.onNodeWithTag("maple.multiChoice.$index").performScrollTo()
                if (field in expected) row.assertIsOn() else row.assertIsOff()
            }
            compose.onNodeWithTag("maple.dialog.cancel").performClick()
        }
        // A location chosen while Show location was on stays chosen when it goes off.
        store.save(store.load().copy(extendedDetailFields = setOf(ExtendedDetailField.SIZE, ExtendedDetailField.LOCATION), showLocation = false))
        ActivityScenario.launch(AppSettingsActivity::class.java).use {
            compose.onNodeWithTag("maple.settings.extendedDetailFields").performScrollTo().performClick()
            compose.onNodeWithTag("maple.multiChoice.${ExtendedDetailField.ISO.ordinal}").performScrollTo().performClick()
            compose.onNodeWithTag("maple.dialog.confirm").performClick()
            compose.waitForIdle()
            assertEquals(setOf(ExtendedDetailField.SIZE, ExtendedDetailField.ISO, ExtendedDetailField.LOCATION), store.load().extendedDetailFields)
        }
    }

    private companion object {
        const val TIMEOUT = 15_000L
        val LONG_NAME = "IMG_" + "a_rather_long_folder_and_file_name_".repeat(5) + "0042.jpg"
    }
}
