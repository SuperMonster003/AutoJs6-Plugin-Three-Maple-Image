package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.os.ParcelFileDescriptor
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import androidx.test.runner.AndroidJUnit4
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import io.github.supermonster003.autojs6.plugin.three.maple.image.scriptapi.GalleryPickerActivity
import io.github.supermonster003.autojs6.plugin.three.maple.image.scriptapi.GalleryPluginService
import io.github.supermonster003.autojs6.plugin.three.maple.image.scriptapi.GallerySessionActivity
import io.github.supermonster003.autojs6.plugin.three.maple.image.scriptapi.ScriptSessionRegistry
import org.autojs.plugin.gallery.api.GalleryActions
import org.autojs.plugin.gallery.api.GalleryCapabilityKeys
import org.autojs.plugin.gallery.api.GalleryContract
import org.autojs.plugin.gallery.api.GalleryErrorCodes
import org.autojs.plugin.gallery.api.GalleryIds
import org.autojs.plugin.gallery.api.IGalleryCallback
import org.autojs.plugin.gallery.api.IGalleryPlugin
import org.autojs.plugin.gallery.api.IGallerySession
import org.json.JSONArray
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.UUID
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * The gallery service of the script object `gallery` on a device (ROADMAP 10.2): identity, the headless converter
 * between descriptors, session tokens, replacement and claim timeout, and the session Activity driving the viewer,
 * the editor (a real save into the host's target) and the picker (a real MediaStore image).
 */
@RunWith(AndroidJUnit4::class)
class GalleryServiceContractTest {

    @get:Rule(order = 0)
    val permissions: GrantPermissionRule = GrantPermissionRule.grant(*MediaStoreTestImages.permissions())

    @get:Rule(order = 1)
    val compose = createEmptyComposeRule()

    private val instrumentation get() = InstrumentationRegistry.getInstrumentation()
    private val context get() = instrumentation.targetContext
    private lateinit var directory: File
    private lateinit var plugin: IGalleryPlugin
    private val inserted = ArrayList<Uri>()
    private var connection: ServiceConnection? = null
    private var binder: IBinder? = null
    private val folder = "MapleGallery" + UUID.randomUUID().toString().take(8)

    private class Recorder : IGalleryCallback.Stub() {
        val events = CopyOnWriteArrayList<Triple<Long, Long, Bundle>>()
        override fun onEvent(generation: Long, seq: Long, event: Bundle) {
            events += Triple(generation, seq, event)
        }

        fun types(): List<String?> = events.map { it.third.getString(GalleryContract.KEY_EVENT) }
        fun last(type: String): Bundle? = events.lastOrNull { it.third.getString(GalleryContract.KEY_EVENT) == type }?.third
        fun await(type: String, millis: Long = 15_000): Bundle {
            val deadline = System.currentTimeMillis() + millis
            while (System.currentTimeMillis() < deadline) {
                last(type)?.let { return it }
                Thread.sleep(50)
            }
            throw AssertionError("no $type event, got ${types()}")
        }
    }

    @Before fun setUp() {
        directory = File(context.cacheDir, "gallery-test-" + UUID.randomUUID()).apply { mkdirs() }
        val latch = CountDownLatch(1)
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

    @After fun tearDown() {
        ScriptSessionRegistry.closeAll(GalleryContract.REASON_HOST)
        ScriptSessionRegistry.claimTimeoutMs = GalleryContract.SESSION_CLAIM_TIMEOUT_MS
        connection?.let { runCatching { context.unbindService(it) } }
        directory.deleteRecursively()
        MediaStoreTestImages.cleanUp(context, inserted, folder)
    }

    private fun png(name: String, width: Int, height: Int, color: Int = 0x80336699.toInt()): File =
        File(directory, name).also { file ->
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).apply { eraseColor(color) }
            file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
            bitmap.recycle()
        }

    private fun readOnly(file: File) = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)

    private fun writable(file: File) = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_WRITE or ParcelFileDescriptor.MODE_CREATE)

    private fun request(build: Bundle.() -> Unit = {}) = Bundle().apply {
        putInt(GalleryContract.KEY_CONTRACT_VERSION, GalleryContract.CONTRACT_VERSION)
        build()
    }

    private fun launch(session: IGallerySession) {
        val token = requireNotNull(session.state.getString(GalleryContract.KEY_SESSION_TOKEN))
        assertTrue(Regex("[0-9a-f]{32}").matches(token))
        context.startActivity(
            Intent(GalleryActions.SESSION_ACTIVITY_ACTION)
                .setComponent(ComponentName(context, GallerySessionActivity::class.java))
                .putExtra(GalleryActions.EXTRA_SESSION_TOKEN, token)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
    }

    private fun resumed(type: Class<*>): Boolean {
        var found = false
        instrumentation.runOnMainSync {
            found = ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED).any { type.isInstance(it) }
        }
        return found
    }

    private fun exists(tag: String): Boolean =
        runCatching { compose.onAllNodes(hasTestTag(tag)).fetchSemanticsNodes().isNotEmpty() }.getOrDefault(false)

    @Test fun theIdentityDeclaresTheGalleryContract() {
        val info = plugin.info
        assertEquals(GalleryIds.ENGINE, info.engine)
        assertEquals(GalleryIds.PLUGIN_ID, info.id)
        val capabilities = plugin.capabilities
        assertEquals(1, capabilities.getInt(GalleryCapabilityKeys.CONTRACT_VERSION))
        assertEquals(GalleryContract.FEATURES, capabilities.getStringArray(GalleryCapabilityKeys.FEATURES)!!.toSet())
    }

    @Test fun convertWritesTheTargetAndAnswersWhatItWrote() {
        val source = png("source.png", 400, 300)
        val target = File(directory, "target.bin").apply { createNewFile() }
        val answer = plugin.convert(readOnly(source), writable(target), request {
            putString(GalleryContract.KEY_FORMAT, GalleryContract.FORMAT_JPEG)
            putInt(GalleryContract.KEY_QUALITY, 80)
            putString(GalleryContract.KEY_SIZE_MODE, GalleryContract.SIZE_PERCENT)
            putInt(GalleryContract.KEY_PERCENT, 50)
            putInt(GalleryContract.KEY_BACKGROUND, 0xFF00FF00.toInt())
        })
        assertNull(answer.getString(GalleryContract.KEY_ERROR_CODE), answer.getString(GalleryContract.KEY_ERROR_MESSAGE))
        assertEquals("image/jpeg", answer.getString(GalleryContract.KEY_MIME_TYPE))
        assertEquals(200, answer.getInt(GalleryContract.KEY_WIDTH))
        assertEquals(150, answer.getInt(GalleryContract.KEY_HEIGHT))
        assertEquals(target.length(), answer.getLong(GalleryContract.KEY_SIZE))
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }.also { BitmapFactory.decodeFile(target.path, it) }
        assertEquals("image/jpeg", bounds.outMimeType)
        assertEquals(200, bounds.outWidth)

        val broken = File(directory, "broken.png").apply { writeText("not an image") }
        val refused = plugin.convert(readOnly(broken), writable(File(directory, "t2.bin")), request())
        assertEquals(GalleryErrorCodes.DECODE_FAILED, refused.getString(GalleryContract.KEY_ERROR_CODE))
        val invalid = plugin.convert(readOnly(source), writable(File(directory, "t3.bin")), request {
            putString(GalleryContract.KEY_FORMAT, GalleryContract.FORMAT_PNG)
            putLong(GalleryContract.KEY_TARGET_BYTES, 1000L)
        })
        assertEquals(GalleryErrorCodes.INVALID_ARGUMENT, invalid.getString(GalleryContract.KEY_ERROR_CODE))
    }

    @Test fun aNewSessionReplacesTheOldOneAndUnclaimedSessionsTimeOut() {
        val first = Recorder()
        val picker = plugin.openPicker(request { putBoolean(GalleryContract.KEY_MULTIPLE, true) }, first)
        assertEquals(GalleryContract.KIND_PICKER, picker.state.getString(GalleryContract.KEY_KIND))
        ScriptSessionRegistry.claimTimeoutMs = 500
        val second = Recorder()
        val viewer = plugin.openViewer(arrayOf(readOnly(png("a.png", 8, 8))), request { putStringArray(GalleryContract.KEY_DISPLAY_NAMES, arrayOf("a.png")) }, second)
        assertEquals(GalleryContract.REASON_REPLACED, first.await(GalleryContract.EVENT_CLOSE).getString(GalleryContract.KEY_REASON))
        assertEquals(GalleryContract.REASON_TIMEOUT, second.await(GalleryContract.EVENT_CLOSE).getString(GalleryContract.KEY_REASON))
        assertEquals(GalleryErrorCodes.SESSION_CLOSED, viewer.state.getString(GalleryContract.KEY_ERROR_CODE))

        val third = Recorder()
        ScriptSessionRegistry.claimTimeoutMs = GalleryContract.SESSION_CLAIM_TIMEOUT_MS
        val closing = plugin.openPicker(request(), third)
        closing.close(request())
        assertEquals(GalleryContract.REASON_HOST, third.await(GalleryContract.EVENT_CLOSE).getString(GalleryContract.KEY_REASON))
        assertEquals(listOf(1L), third.events.map { it.second })
    }

    @Test fun aTokenClaimsItsSessionOnceAndAWrongOneNothing() {
        val recorder = Recorder()
        val session = plugin.openPicker(request(), recorder)
        val token = requireNotNull(session.state.getString(GalleryContract.KEY_SESSION_TOKEN))
        assertNull(ScriptSessionRegistry.claim("0".repeat(32)))
        assertNull(ScriptSessionRegistry.claim(token.uppercase()))
        assertNotNull(ScriptSessionRegistry.claim(token))
        assertNull("a token is single-use", ScriptSessionRegistry.claim(token))
        // A launch with a wrong token finishes at once and leaves the session alone.
        context.startActivity(
            Intent(GalleryActions.SESSION_ACTIVITY_ACTION)
                .setComponent(ComponentName(context, GallerySessionActivity::class.java))
                .putExtra(GalleryActions.EXTRA_SESSION_TOKEN, "f".repeat(32))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
        Thread.sleep(1_000)
        assertTrue(recorder.events.isEmpty())
        assertTrue(!resumed(GallerySessionActivity::class.java) && !resumed(GalleryPickerActivity::class.java))
    }

    @Test fun theViewerReportsOpenAndPagesAndTheHostClosesIt() {
        val recorder = Recorder()
        val names = arrayOf("one.png", "two.png", "three.png")
        val sources = names.mapIndexed { index, name -> readOnly(png(name, 40 + index, 30)) }.toTypedArray()
        val session = plugin.openViewer(sources, request {
            putStringArray(GalleryContract.KEY_DISPLAY_NAMES, names)
            putInt(GalleryContract.KEY_START_INDEX, 1)
        }, recorder)
        launch(session)
        assertEquals(GalleryContract.KIND_VIEWER, recorder.await(GalleryContract.EVENT_OPEN).getString(GalleryContract.KEY_KIND))
        assertEquals(1, recorder.await(GalleryContract.EVENT_PAGE).getInt(GalleryContract.KEY_INDEX))
        assertTrue(session.state.getBoolean(GalleryContract.KEY_VISIBLE))
        assertEquals(1, session.state.getInt(GalleryContract.KEY_INDEX))
        session.close(request { putBoolean(GalleryContract.KEY_FINISH, true) })
        assertEquals(GalleryContract.REASON_HOST, recorder.await(GalleryContract.EVENT_CLOSE).getString(GalleryContract.KEY_REASON))
        compose.waitUntil(10_000) { !resumed(ImageViewerActivity::class.java) && !resumed(GallerySessionActivity::class.java) }
        val seqs = recorder.events.map { it.second }
        assertEquals((1L..seqs.size).toList(), seqs)
        assertEquals(1, recorder.events.map { it.first }.distinct().size)
    }

    @Test fun theEditorWritesTheSavedImageIntoTheHostTarget() {
        val recorder = Recorder()
        val source = png("photo.png", 96, 64, 0xff336699.toInt())
        val target = File(directory, ".gallery-staging.tmp").apply { createNewFile() }
        val session = plugin.openEditor(readOnly(source), writable(target), request {
            putString(GalleryContract.KEY_DISPLAY_NAME, "photo.png")
            putString(GalleryContract.KEY_FORMAT, GalleryContract.FORMAT_PNG)
        }, recorder)
        launch(session)
        recorder.await(GalleryContract.EVENT_OPEN)
        compose.waitUntil(15_000) { exists("maple.tool.rotateRight") }
        compose.onNodeWithTag("maple.tool.rotateRight").performClick()
        compose.waitUntil(10_000) { runCatching { compose.onNodeWithTag("maple.editor.save").assertIsEnabled() }.isSuccess }
        compose.onNodeWithTag("maple.editor.save").performClick()
        compose.waitUntil(10_000) { exists("maple.dialog.confirm") }
        compose.onNodeWithTag("maple.dialog.confirm").performClick()
        val result = recorder.await(GalleryContract.EVENT_RESULT, 30_000)
        assertEquals("image/png", result.getString(GalleryContract.KEY_MIME_TYPE))
        assertEquals(64, result.getInt(GalleryContract.KEY_WIDTH))
        assertEquals(96, result.getInt(GalleryContract.KEY_HEIGHT))
        assertEquals(target.length(), result.getLong(GalleryContract.KEY_SIZE))
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }.also { BitmapFactory.decodeFile(target.path, it) }
        assertEquals(64, bounds.outWidth)
        assertEquals(GalleryContract.REASON_DONE, recorder.await(GalleryContract.EVENT_CLOSE).getString(GalleryContract.KEY_REASON))
        compose.waitUntil(10_000) { !resumed(StandaloneImageEditorActivity::class.java) }
    }

    @Test fun thePickerAnswersTheChosenImageWithItsPath() {
        val uri = MediaStoreTestImages.insert(context, folder, "picked.png", 0xffcc3300.toInt())
        inserted += uri
        val id = uri.lastPathSegment!!.toLong()
        val recorder = Recorder()
        val session = plugin.openPicker(request { putStringArray(GalleryContract.KEY_MIME_TYPES, arrayOf("image/png")) }, recorder)
        launch(session)
        recorder.await(GalleryContract.EVENT_OPEN)
        val all = "maple.pick.album.all.${MediaCatalog.ALL_BUCKETS}"
        compose.waitUntil(15_000) { exists(all) }
        compose.onNodeWithTag(all).performClick()
        compose.waitUntil(15_000) { exists("maple.media.item.$id") }
        compose.onNodeWithTag("maple.media.item.$id").performClick()
        val result = recorder.await(GalleryContract.EVENT_RESULT)
        val items = JSONArray(result.getString(GalleryContract.KEY_ITEMS))
        assertEquals(1, items.length())
        val item = items.getJSONObject(0)
        assertEquals("picked.png", item.getString(GalleryContract.FIELD_NAME))
        assertEquals("image/png", item.getString(GalleryContract.FIELD_MIME_TYPE))
        assertEquals(MediaStoreTestImages.PIXELS, item.getInt(GalleryContract.FIELD_WIDTH))
        val path = File(item.getString(GalleryContract.FIELD_PATH))
        assertTrue("$path must be the image's file", path.isAbsolute && path.name == "picked.png")
        assertNotNull(item.getLong(GalleryContract.FIELD_DATE_TAKEN))
        assertEquals(GalleryContract.REASON_DONE, recorder.await(GalleryContract.EVENT_CLOSE).getString(GalleryContract.KEY_REASON))
        compose.waitUntil(10_000) { !resumed(GalleryPickerActivity::class.java) }
    }
}
