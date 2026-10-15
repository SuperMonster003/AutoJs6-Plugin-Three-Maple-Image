package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import io.github.supermonster003.autojs6.plugin.three.maple.image.scriptapi.GalleryPickerActivity
import io.github.supermonster003.autojs6.plugin.three.maple.image.scriptapi.GalleryPluginService
import io.github.supermonster003.autojs6.plugin.three.maple.image.scriptapi.GallerySessionActivity
import io.github.supermonster003.autojs6.plugin.three.maple.image.scriptapi.ScriptSessionRegistry
import org.autojs.plugin.gallery.api.GalleryActions
import org.autojs.plugin.gallery.api.GalleryContract
import org.autojs.plugin.gallery.api.GalleryErrorCodes
import org.autojs.plugin.gallery.api.IGalleryCallback
import org.autojs.plugin.gallery.api.IGalleryPlugin
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeFalse
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * The script picker without photo access (ROADMAP 10.2, M6 decision 6): it asks first, and a refusal (or leaving a
 * picker that can no longer ask) ends the session with `error(PERMISSION_REQUIRED)` and `close(error)`. Runs only
 * while the plugin has no photo access (a fresh install); other classes grant it, after which this test skips.
 */
@RunWith(AndroidJUnit4::class)
class GalleryPickerPermissionInstrumentationTest {

    private val instrumentation get() = InstrumentationRegistry.getInstrumentation()
    private val context get() = instrumentation.targetContext
    private val device by lazy { UiDevice.getInstance(instrumentation) }
    private var connection: ServiceConnection? = null
    private lateinit var plugin: IGalleryPlugin

    private class Recorder : IGalleryCallback.Stub() {
        val events = CopyOnWriteArrayList<Bundle>()
        override fun onEvent(generation: Long, seq: Long, event: Bundle) {
            events += event
        }

        fun await(type: String, millis: Long = 20_000): Bundle {
            val deadline = System.currentTimeMillis() + millis
            while (System.currentTimeMillis() < deadline) {
                events.lastOrNull { it.getString(GalleryContract.KEY_EVENT) == type }?.let { return it }
                Thread.sleep(100)
            }
            throw AssertionError("no $type event, got ${events.map { it.getString(GalleryContract.KEY_EVENT) }}")
        }
    }

    @Before fun bind() {
        device.findObject(By.res("android", "aerr_wait"))?.click()
        val latch = CountDownLatch(1)
        var binder: IBinder? = null
        connection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                binder = service
                latch.countDown()
            }

            override fun onServiceDisconnected(name: ComponentName?) = Unit
        }
        assertTrue(context.bindService(Intent().setComponent(ComponentName(context, GalleryPluginService::class.java)), connection!!, Context.BIND_AUTO_CREATE))
        assertTrue(latch.await(10, TimeUnit.SECONDS))
        plugin = IGalleryPlugin.Stub.asInterface(binder)
    }

    @After fun unbind() {
        repeat(2) {
            if (permissionDialogOnTop()) device.pressBack()
        }
        ScriptSessionRegistry.closeAll(GalleryContract.REASON_HOST)
        connection?.let { runCatching { context.unbindService(it) } }
    }

    private fun photoAccess(): Boolean {
        val granted = { permission: String -> context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED }
        return granted(MediaPermissionPolicy.readPermission(Build.VERSION.SDK_INT)) ||
            (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE && granted("android.permission.READ_MEDIA_VISUAL_USER_SELECTED"))
    }

    private fun permissionDialogOnTop(): Boolean =
        device.currentPackageName?.let { name -> listOf("permissioncontroller", "packageinstaller", "lbe.security").any { name.contains(it) } } == true

    private fun pickerResumed(): Boolean {
        var found = false
        instrumentation.runOnMainSync {
            found = ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED).any { it is GalleryPickerActivity }
        }
        return found
    }

    @Test fun aRefusedPickerAnswersPermissionRequired() {
        assumeFalse("the plugin already has photo access", photoAccess())
        val recorder = Recorder()
        val session = plugin.openPicker(Bundle().apply { putInt(GalleryContract.KEY_CONTRACT_VERSION, GalleryContract.CONTRACT_VERSION) }, recorder)
        context.startActivity(
            Intent(GalleryActions.SESSION_ACTIVITY_ACTION)
                .setComponent(ComponentName(context, GallerySessionActivity::class.java))
                .putExtra(GalleryActions.EXTRA_SESSION_TOKEN, session.state.getString(GalleryContract.KEY_SESSION_TOKEN))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
        recorder.await(GalleryContract.EVENT_OPEN)
        // Either the system asks (refused with back) or it can no longer ask and the picker shows its settings button.
        val deadline = System.currentTimeMillis() + 10_000
        while (System.currentTimeMillis() < deadline && !permissionDialogOnTop()) Thread.sleep(200)
        if (permissionDialogOnTop()) {
            device.pressBack()
        } else {
            while (System.currentTimeMillis() < deadline + 5_000 && !pickerResumed()) Thread.sleep(200)
            device.pressBack()
        }
        val error = recorder.await(GalleryContract.EVENT_ERROR)
        assertEquals(GalleryErrorCodes.PERMISSION_REQUIRED, error.getString(GalleryContract.KEY_ERROR_CODE))
        assertEquals(GalleryContract.REASON_ERROR, recorder.await(GalleryContract.EVENT_CLOSE).getString(GalleryContract.KEY_REASON))
    }
}
