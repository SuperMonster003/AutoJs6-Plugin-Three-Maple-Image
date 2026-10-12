package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StandaloneHomeInstrumentationTest {
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

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

    @Test fun homeShowsItsOwnIdentity() {
        ActivityScenario.launch<HomeActivity>(Intent(context, HomeActivity::class.java)).use { scenario ->
            scenario.onActivity { activity ->
                val texts = labels(activity.window.decorView)
                assertTrue(texts.contains(activity.getString(R.string.app_name)))
                assertTrue(texts.contains(activity.getString(R.string.standalone_settings_title)))
            }
        }
    }

    private fun labels(view: View): List<String> = buildList {
        if (view is TextView) add(view.text.toString())
        if (view is ViewGroup) for (i in 0 until view.childCount) addAll(labels(view.getChildAt(i)))
    }
}
