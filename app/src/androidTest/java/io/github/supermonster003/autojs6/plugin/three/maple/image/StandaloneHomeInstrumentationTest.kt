package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.content.ComponentName
import android.content.Intent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
        for (name in listOf("HomeActivity", "AppSettingsActivity", "AboutActivity", "ReleaseHistoryActivity")) {
            assertFalse(name, manager.getActivityInfo(ComponentName(context.packageName, context.packageName + "." + name), 0).exported)
        }
    }

    @Test fun homeShowsItsIdentityAndEveryEntryRow() {
        ActivityScenario.launch(HomeActivity::class.java).use {
            compose.onNodeWithText(context.getString(R.string.app_name)).assertIsDisplayed()
            compose.onNodeWithText(context.getString(R.string.plugin_description)).assertIsDisplayed()
            for (tag in listOf("maple.home.view", "maple.home.edit", "maple.home.convert", "maple.home.settings", "maple.home.openHost")) {
                compose.onNodeWithTag(tag).performScrollTo().assertIsDisplayed().assertIsEnabled()
            }
            compose.onNodeWithText(context.getString(R.string.standalone_settings_title)).performScrollTo().assertIsDisplayed()
        }
    }
}
