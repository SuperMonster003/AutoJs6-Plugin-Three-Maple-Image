package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.content.Intent
import android.graphics.Bitmap
import androidx.core.content.FileProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.UUID

/**
 * ROADMAP 6.1 / D1 / R2: the standalone home asks for photo access once on its first launch and never again by
 * itself; the external `ACTION_VIEW` entry never asks. The system dialog is detected through UiAutomator as a
 * foreign package on top of the app.
 */
@RunWith(AndroidJUnit4::class)
class MediaPermissionLaunchInstrumentationTest {

    private val instrumentation get() = InstrumentationRegistry.getInstrumentation()
    private val context get() = instrumentation.targetContext
    private val device by lazy { UiDevice.getInstance(instrumentation) }
    private val record by lazy { MediaPermissionRecordStore(context) }
    private var originalRequested = false

    @Before fun remember() {
        originalRequested = record.requestedBefore
        record.reset()
    }

    @After fun restore() {
        dismissForeignWindow()
        if (originalRequested) record.markRequested() else record.reset()
    }

    private fun foreignPackageOnTop(timeoutMillis: Long): Boolean {
        val deadline = System.currentTimeMillis() + timeoutMillis
        while (System.currentTimeMillis() < deadline) {
            val top = device.currentPackageName
            if (top != null && top != context.packageName) return true
            Thread.sleep(200)
        }
        return false
    }

    private fun dismissForeignWindow() {
        repeat(2) {
            if (device.currentPackageName != context.packageName) {
                device.pressBack()
                Thread.sleep(500)
            }
        }
    }

    @Test fun standaloneHomeAsksOnceOnFirstLaunchAndNotOnTheSecond() {
        var requests = 0
        var state: MediaAccessState? = null
        ActivityScenario.launch(HomeActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                requests = activity.permissionRequestsForTesting
                state = activity.photoAccess?.state
            }
            if (state == MediaAccessState.DENIED) {
                assertEquals("first launch asks once", 1, requests)
                assertTrue("system permission dialog expected on top", foreignPackageOnTop(5_000))
                dismissForeignWindow()
                scenario.onActivity { activity -> assertEquals("dismissing the dialog does not close the door", MediaAccessState.DENIED, activity.photoAccess?.state) }
            } else {
                assertEquals("already granted or restricted: no request", 0, requests)
            }
            assertTrue("the record remembers the first launch", record.requestedBefore || state != MediaAccessState.DENIED)
        }
        if (state != MediaAccessState.DENIED) record.markRequested()
        ActivityScenario.launch(HomeActivity::class.java).use { scenario ->
            scenario.onActivity { activity -> assertEquals("second launch stays silent", 0, activity.permissionRequestsForTesting) }
            assertFalse("no system dialog on the second launch", foreignPackageOnTop(2_000))
        }
    }

    @Test fun externalViewEntryNeverAsks() {
        val fixture = File(context.cacheDir, "standalone-images/permission-${UUID.randomUUID()}.png")
        fixture.parentFile!!.mkdirs()
        val bitmap = Bitmap.createBitmap(64, 48, Bitmap.Config.ARGB_8888).apply { eraseColor(0xff336699.toInt()) }
        fixture.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()
        try {
            val uri = FileProvider.getUriForFile(context, context.packageName + ".standalone-images", fixture)
            val intent = Intent(Intent.ACTION_VIEW, uri)
                .setClass(context, ExternalViewActivity::class.java)
                .setDataAndType(uri, "image/png")
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            ActivityScenario.launch<ExternalViewActivity>(intent).use {
                assertFalse("the external view entry must not show a permission dialog", foreignPackageOnTop(2_500))
                assertFalse("the external view entry leaves the record untouched", record.requestedBefore)
            }
        } finally {
            fixture.delete()
        }
    }
}
