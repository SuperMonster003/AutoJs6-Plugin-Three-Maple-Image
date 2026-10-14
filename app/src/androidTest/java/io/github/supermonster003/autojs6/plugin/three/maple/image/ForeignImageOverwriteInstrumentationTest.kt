package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.UUID
import java.util.regex.Pattern
import kotlin.coroutines.resume

/**
 * ROADMAP 8.2 on Android 11+: an image another app owns (here one the shell made) is overwritten only after the
 * system's `createWriteRequest` confirmation, which the test accepts.
 */
@RunWith(AndroidJUnit4::class)
class ForeignImageOverwriteInstrumentationTest {

    @get:Rule
    val permissions: GrantPermissionRule = GrantPermissionRule.grant(*MediaStoreTestImages.permissions())

    private val instrumentation get() = InstrumentationRegistry.getInstrumentation()
    private val context get() = instrumentation.targetContext
    private val device by lazy { UiDevice.getInstance(instrumentation) }

    @Suppress("DEPRECATION")
    private val directory = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "MapleForeign${System.currentTimeMillis()}")
    private lateinit var scenario: ActivityScenario<ComponentActivity>
    private lateinit var coordinator: MediaWriteCoordinator
    private val files = mutableListOf<File>()

    @Before fun setUp() {
        assumeTrue(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
        scenario = ActivityScenario.launch(ComponentActivity::class.java)
        scenario.onActivity { coordinator = MediaWriteCoordinator(it) }
    }

    @After fun tearDown() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return
        scenario.close()
        files.forEach { it.delete() }
        shell("rm -rf ${directory.path}")
        runBlocking { scan(File(directory, "shot.png").path) }
    }

    @Test fun anotherAppsImageIsOverwrittenAfterTheSystemConfirmation() = runBlocking {
        shell("mkdir -p ${directory.path}")
        val path = File(directory, "shot.png").path
        shell("screencap -p $path")
        val uri = requireNotNull(scan(path)) { "The shell's image was not scanned" }
        assertNotEquals(context.packageName, owner(uri))
        val edited = File(context.cacheDir, "foreign-${UUID.randomUUID()}.png").also { files += it }
        val bitmap = Bitmap.createBitmap(30, 30, Bitmap.Config.ARGB_8888).apply { eraseColor(Color.BLUE) }
        edited.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }

        val write = CoroutineScope(Dispatchers.Main).async { coordinator.overwrite(uri) { edited.inputStream() } }
        val allow = device.wait(Until.findObject(By.text(Pattern.compile("(?i)allow|允许"))), CONFIRMATION_TIMEOUT)
        requireNotNull(allow) { "No write confirmation appeared" }.click()
        assertEquals(1, withTimeout(CONFIRMATION_TIMEOUT) { write.await() }.succeeded)

        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }
        assertEquals(30 to 30, options.outWidth to options.outHeight)
    }

    private fun shell(command: String) {
        instrumentation.uiAutomation.executeShellCommand(command).close()
        Thread.sleep(SHELL_SETTLE_MILLIS)
    }

    private suspend fun scan(path: String): Uri? = suspendCancellableCoroutine { continuation ->
        MediaScannerConnection.scanFile(context, arrayOf(path), null) { _, uri -> if (continuation.isActive) continuation.resume(uri) }
    }

    private fun owner(uri: Uri): String? =
        context.contentResolver.query(uri, arrayOf(MediaStore.MediaColumns.OWNER_PACKAGE_NAME), null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) cursor.getString(0) else null
        }

    private companion object {
        const val CONFIRMATION_TIMEOUT = 15_000L
        const val SHELL_SETTLE_MILLIS = 800L
    }
}
