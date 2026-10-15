package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.content.ComponentName
import android.content.Intent
import android.view.KeyEvent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StandaloneHomeInstrumentationTest {

    @get:Rule
    val compose = createEmptyComposeRule()

    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private val record by lazy { MediaPermissionRecordStore(context) }
    private var originalRequested = false

    /** The one-time photo access request belongs to MediaPermissionLaunchInstrumentationTest; keep it out of here. */
    @Before fun silencePermissionRequest() {
        originalRequested = record.requestedBefore
        record.markRequested()
    }

    @After fun restoreRecord() {
        if (!originalRequested) record.reset()
    }

    @Test fun launcherAndPrivateSettingsHaveExpectedComponents() {
        val manager = context.packageManager
        val launcher = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER).setPackage(context.packageName)
        val matches = manager.queryIntentActivities(launcher, 0)
        assertEquals(1, matches.size)
        assertEquals(context.packageName + ".HomeActivity", matches.single().activityInfo.targetActivity)
        for (name in listOf("HomeActivity", "AppSettingsActivity", "AboutActivity", "ReleaseHistoryActivity", "StoragePermissionActivity")) {
            assertFalse(name, manager.getActivityInfo(ComponentName(context.packageName, context.packageName + "." + name), 0).exported)
        }
    }

    /** ROADMAP 6.2: the home is the album grid; the 2.1.0 file-picker actions live in the menu and the no-permission state. */
    @Test fun homeShowsTheGalleryAndKeepsEveryFileActionReachable() {
        ActivityScenario.launch(HomeActivity::class.java).use { scenario ->
            compose.onNodeWithText(context.getString(R.string.app_name)).assertIsDisplayed()
            compose.waitUntil(10_000) {
                compose.onAllNodes(hasTestTag("maple.gallery.grid")).fetchSemanticsNodes().isNotEmpty() ||
                    compose.onAllNodes(hasTestTag("maple.gallery.noPermission")).fetchSemanticsNodes().isNotEmpty() ||
                    compose.onAllNodes(hasTestTag("maple.gallery.empty")).fetchSemanticsNodes().isNotEmpty()
            }
            var canRead = false
            scenario.onActivity { canRead = it.photoAccess?.canReadMediaStore == true }
            if (!canRead) {
                compose.onNodeWithTag("maple.gallery.noPermission").assertIsDisplayed()
                compose.onNodeWithTag("maple.gallery.grant").assertIsDisplayed().assertIsEnabled()
                compose.onNodeWithTag("maple.gallery.chooseFolder").assertIsDisplayed()
                compose.onNodeWithTag("maple.gallery.chooseImage").assertIsDisplayed()
            }
            compose.onNodeWithTag("maple.gallery.more").assertIsDisplayed().performClick()
            for (tag in listOf("maple.gallery.menu.view", "maple.gallery.menu.edit", "maple.gallery.menu.convert", "maple.gallery.menu.openHost", "maple.gallery.menu.settings")) {
                compose.onNodeWithTag(tag).assertIsDisplayed().assertIsEnabled()
            }
            assertTrue(compose.onAllNodes(hasTestTag("maple.gallery.sort")).fetchSemanticsNodes().isNotEmpty())
            // An open menu outlives the activity as a stray Compose root in this process; a later test that resizes
            // the display would then wait for it forever.
            InstrumentationRegistry.getInstrumentation().sendKeyDownUpSync(KeyEvent.KEYCODE_BACK)
            compose.waitUntil(10_000) { compose.onAllNodes(hasTestTag("maple.gallery.menu.settings")).fetchSemanticsNodes().isEmpty() }
        }
    }
}
