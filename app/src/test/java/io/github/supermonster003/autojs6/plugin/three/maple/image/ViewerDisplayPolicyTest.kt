package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.content.pm.ActivityInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** ROADMAP 9.2: the viewer's display settings; the defaults keep the 3.0.0 behavior. */
class ViewerDisplayPolicyTest {

    @Test fun theDefaultsAreTheBehaviorBeforeTheSettings() {
        val defaults = ViewerSettings()
        assertEquals(ViewerOrientation.FOLLOW_SYSTEM, defaults.orientation)
        assertTrue(defaults.blackBackground && defaults.thumbnailStrip)
        assertFalse(defaults.maxBrightness || defaults.hideSystemBars || defaults.keepScreenOn || defaults.highestQuality)
        assertEquals(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED, ViewerDisplayPolicy.requestedOrientation(defaults.orientation, 300, 100))
        assertFalse(ViewerDisplayPolicy.keepScreenOn(defaults.keepScreenOn, slideshowRunning = false))
        assertTrue(ViewerDisplayPolicy.systemBarsVisible(defaults.hideSystemBars, controlsVisible = false))
        assertEquals(-1f, ViewerDisplayPolicy.windowBrightness(defaults.maxBrightness, dragged = null), 0f)
    }

    @Test fun theOrientationFollowsTheSettingAndTheImageShape() {
        assertEquals(ActivityInfo.SCREEN_ORIENTATION_LOCKED, ViewerDisplayPolicy.requestedOrientation(ViewerOrientation.LOCKED, null, null))
        val image = ViewerOrientation.FOLLOW_IMAGE
        assertEquals(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE, ViewerDisplayPolicy.requestedOrientation(image, 300, 100))
        assertEquals(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT, ViewerDisplayPolicy.requestedOrientation(image, 100, 300))
        assertNull("a square keeps the current one", ViewerDisplayPolicy.requestedOrientation(image, 200, 200))
        assertNull("not measured yet", ViewerDisplayPolicy.requestedOrientation(image, null, 300))
        assertNull(ViewerDisplayPolicy.requestedOrientation(image, 0, 300))
    }

    @Test fun theScreenStaysOnBySettingOrForARunningSlideshow() {
        assertTrue(ViewerDisplayPolicy.keepScreenOn(setting = true, slideshowRunning = false))
        assertTrue(ViewerDisplayPolicy.keepScreenOn(setting = false, slideshowRunning = true))
        assertFalse(ViewerDisplayPolicy.keepScreenOn(setting = false, slideshowRunning = false))
    }

    @Test fun hiddenSystemBarsFollowTheViewersBars() {
        assertTrue(ViewerDisplayPolicy.systemBarsVisible(hideSystemBars = true, controlsVisible = true))
        assertFalse(ViewerDisplayPolicy.systemBarsVisible(hideSystemBars = true, controlsVisible = false))
        assertTrue(ViewerDisplayPolicy.systemBarsVisible(hideSystemBars = false, controlsVisible = true))
    }

    @Test fun aDraggedBrightnessWinsOverMaximumBrightness() {
        assertEquals(1f, ViewerDisplayPolicy.windowBrightness(maxBrightness = true, dragged = null), 0f)
        assertEquals(0.3f, ViewerDisplayPolicy.windowBrightness(maxBrightness = true, dragged = 0.3f), 0f)
        assertEquals(0.3f, ViewerDisplayPolicy.windowBrightness(maxBrightness = false, dragged = 0.3f), 0f)
    }
}
