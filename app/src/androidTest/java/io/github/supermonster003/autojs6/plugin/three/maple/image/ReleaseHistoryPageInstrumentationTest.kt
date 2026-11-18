package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.core.content.FileProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.UUID

/**
 * P19.1 of the plugin center roadmap: the release history page is the model of the M3 release history pages of the
 * official plugins. Its title is "Release history" with the plugin name as the subtitle, and the menus of the viewer
 * and the editor open this page (they used to show the raw markdown in a dialog).
 */
@RunWith(AndroidJUnit4::class)
class ReleaseHistoryPageInstrumentationTest {

    @get:Rule
    val compose = createEmptyComposeRule()

    private val instrumentation get() = InstrumentationRegistry.getInstrumentation()
    private val context get() = instrumentation.targetContext
    private lateinit var directory: File
    private var session: String? = null

    @Before fun createImage() {
        TestScreen.awake()
        directory = File(context.cacheDir, "standalone-images/history-${UUID.randomUUID()}").apply { mkdirs() }
        val bitmap = Bitmap.createBitmap(96, 64, Bitmap.Config.ARGB_8888).apply { eraseColor(Color.rgb(0x33, 0x66, 0x99)) }
        File(directory, "a.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()
    }

    @After fun removeImage() {
        session?.let { StandaloneImageWork.discard(context, it) }
        finishReleaseHistory()
        directory.deleteRecursively()
    }

    /** Unmerged: the count chips sit inside the clickable header, which merges its children. */
    private fun exists(tag: String): Boolean =
        runCatching { compose.onAllNodes(hasTestTag(tag), useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty() }.getOrDefault(false)

    private fun resumed(): Activity? {
        var activity: Activity? = null
        instrumentation.runOnMainSync {
            activity = ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED).firstOrNull()
        }
        return activity
    }

    private fun finishReleaseHistory() {
        instrumentation.runOnMainSync {
            ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED)
                .filterIsInstance<ReleaseHistoryActivity>()
                .forEach(Activity::finish)
        }
        instrumentation.waitForIdleSync()
    }

    private fun uri(): android.net.Uri =
        FileProvider.getUriForFile(context, context.packageName + ".standalone-images", File(directory, "a.png"))

    /** The page that a menu item opened: the release history activity, with its list (not a dialog over the caller). */
    private fun assertThePageOpened() {
        compose.waitUntil(TIMEOUT) { resumed() is ReleaseHistoryActivity && exists("maple.releaseHistory.list") }
        assertTitleAndSubtitle()
    }

    private fun assertTitleAndSubtitle() {
        val inTopBar = hasAnyAncestor(hasTestTag("maple.topBar"))
        compose.onNode(hasText(context.getString(R.string.release_history)) and inTopBar).assertIsDisplayed()
        compose.onNode(hasText(context.getString(R.string.app_name)) and inTopBar).assertIsDisplayed()
    }

    @Test fun theTitleIsReleaseHistoryAndTheSubtitleThePluginName() {
        ActivityScenario.launch(ReleaseHistoryActivity::class.java).use {
            compose.waitUntil(TIMEOUT) { exists("maple.releaseHistory.list") }
            assertTitleAndSubtitle()
            assertTrue(compose.onAllNodes(hasTestTag("maple.emptyState")).fetchSemanticsNodes().isEmpty())
        }
    }

    private fun versions(scenario: ActivityScenario<ReleaseHistoryActivity>): List<String> {
        var versions = emptyList<String>()
        scenario.onActivity { activity ->
            val candidates = ReleaseHistoryAssetPolicy.candidatesFor(activity.resources.configuration.locales[0])
            versions = loadReleaseHistory(candidates) { path ->
                activity.assets.open(path).use { it.readBytes().toString(Charsets.UTF_8) }
            }.map { it.version }
        }
        return versions
    }

    /**
     * The newest release starts expanded, the others show their category counts; a header expands or collapses its
     * release, the top bar expands or collapses all of them, and the choice survives recreation.
     */
    @Test fun releasesExpandAndCollapse() {
        ActivityScenario.launch(ReleaseHistoryActivity::class.java).use { scenario ->
            compose.waitUntil(TIMEOUT) { exists("maple.releaseHistory.list") }
            val versions = versions(scenario)
            assertTrue("releases: ${versions.size}", versions.size > 2)
            val (newest, second) = versions
            assertTrue(exists("maple.releaseHistory.body.$newest"))
            assertFalse(exists("maple.releaseHistory.counts.$newest"))
            assertFalse(exists("maple.releaseHistory.body.$second"))
            assertTrue(exists("maple.releaseHistory.counts.$second"))

            compose.onNodeWithTag("maple.releaseHistory.entry.$second").performClick()
            compose.waitUntil(TIMEOUT) { exists("maple.releaseHistory.body.$second") }
            compose.onNodeWithTag("maple.releaseHistory.entry.$newest").performClick()
            compose.waitUntil(TIMEOUT) { !exists("maple.releaseHistory.body.$newest") && exists("maple.releaseHistory.counts.$newest") }

            scenario.recreate()
            compose.waitUntil(TIMEOUT) { exists("maple.releaseHistory.list") }
            assertTrue(exists("maple.releaseHistory.body.$second"))
            assertFalse(exists("maple.releaseHistory.body.$newest"))

            compose.onNodeWithTag("maple.releaseHistory.toggleAll")
                .assertContentDescriptionEquals(context.getString(R.string.release_history_expand_all))
                .performClick()
            compose.waitUntil(TIMEOUT) { exists("maple.releaseHistory.body.$newest") }
            compose.onNodeWithTag("maple.releaseHistory.toggleAll")
                .assertContentDescriptionEquals(context.getString(R.string.release_history_collapse_all))
                .performClick()
            compose.waitUntil(TIMEOUT) { !exists("maple.releaseHistory.body.$newest") && !exists("maple.releaseHistory.body.$second") }
        }
    }

    @Test fun theViewerMenuOpensThePage() {
        val page = ImageViewerRequest(uri(), "a.png", File(directory, "a.png").length(), "image/png")
        val intent = ImageViewerContract.viewerIntent(context, ImageViewerLaunchRequest.explicitSelection(listOf(page)))
        ActivityScenario.launchActivityForResult<ImageViewerActivity>(intent).use {
            compose.waitUntil(TIMEOUT) { exists("maple.viewer.more") }
            compose.onNodeWithTag("maple.viewer.more").performClick()
            compose.waitUntil(TIMEOUT) { exists("maple.viewer.menu.releaseHistory") }
            compose.onNodeWithTag("maple.viewer.menu.releaseHistory").performClick()
            assertThePageOpened()
        }
    }

    @Test fun theEditorMenuOpensThePage() {
        session = StandaloneImageWork.create(context, StandaloneDocuments.inspect(context, uri()), ImageToolsPlugin.EDIT_ACTION_ID)
        val intent = Intent(context, StandaloneImageEditorActivity::class.java).putExtra(StandaloneImageWork.EXTRA_SESSION, session)
        ActivityScenario.launch<StandaloneImageEditorActivity>(intent).use {
            compose.waitUntil(TIMEOUT) { exists("maple.editor.more") }
            compose.onNodeWithTag("maple.editor.more").performClick()
            compose.waitUntil(TIMEOUT) { exists("maple.editor.menu.releaseHistory") }
            compose.onNodeWithTag("maple.editor.menu.releaseHistory").performClick()
            assertThePageOpened()
        }
    }

    private companion object {
        const val TIMEOUT = 15_000L
    }
}
