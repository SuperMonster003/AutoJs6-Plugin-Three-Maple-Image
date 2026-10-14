package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.os.ParcelFileDescriptor
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.autojs.plugin.explorer.api.ExplorerActionHostSessionKeys
import org.autojs.plugin.explorer.api.ExplorerActionHostSessionValues
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.UUID

/**
 * ROADMAP 8.2 (T11) for the host entry: "Edit image" as a managed target writes a new sibling or replaces the target
 * through the host session (staged, read back, then committed), the replacement can be undone once, and a target
 * that changed in the meantime is never overwritten (the staged file is aborted).
 */
@RunWith(AndroidJUnit4::class)
class HostToolOverwriteInstrumentationTest {

    @get:Rule
    val compose = createEmptyComposeRule()

    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private lateinit var directory: File
    private lateinit var target: File

    @Before fun createTarget() {
        directory = File(context.cacheDir, "standalone-images/host-${UUID.randomUUID()}").apply { mkdirs() }
        target = File(directory, "wide.png")
        writePng(target, Color.RED, 40, 20)
    }

    @After fun removeTarget() {
        directory.deleteRecursively()
    }

    private fun writePng(file: File, color: Int, width: Int, height: Int) {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).apply { eraseColor(color) }
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()
    }

    private fun size(file: File): Pair<Int, Int> {
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.path, options)
        return options.outWidth to options.outHeight
    }

    private fun exists(tag: String): Boolean = compose.onAllNodes(hasTestTag(tag)).fetchSemanticsNodes().isNotEmpty()

    private fun enabled(tag: String): Boolean = runCatching { compose.onNodeWithTag(tag).assertIsEnabled() }.isSuccess

    private fun launchEditor(session: FakeManagedSession): ActivityScenario<ImageEditorActivity> {
        val scenario = ActivityScenario.launchActivityForResult<ImageEditorActivity>(
            HostRequestFixtures.toolIntent(context, target, session, ImageToolsPlugin.EDIT_ACTION_ID),
        )
        compose.waitUntil(TIMEOUT) { exists("maple.tool.rotateRight") && enabled("maple.tool.rotateRight") }
        compose.onNodeWithTag("maple.tool.rotateRight").performClick()
        compose.waitUntil(TIMEOUT) { enabled("maple.editor.save") }
        compose.onNodeWithTag("maple.editor.save").performClick()
        compose.waitUntil(TIMEOUT) { exists("maple.editor.save.overwrite") }
        return scenario
    }

    @Test fun aNewSiblingIsWrittenThroughTheSession() {
        val session = FakeManagedSession(directory, target)
        launchEditor(session).use { scenario ->
            compose.onNodeWithTag("maple.dialog.confirm").performClick()
            compose.waitUntil(TIMEOUT) { scenario.state == androidx.lifecycle.Lifecycle.State.DESTROYED }
            assertEquals(Activity.RESULT_OK, scenario.result.resultCode)
        }
        assertEquals(20 to 40, size(File(directory, "wide_edited.png")))
        assertEquals(40 to 20, size(target))
        assertEquals(listOf("prepareOutput", "openOutput", "openPendingOutput", "commitOutput", "close"), session.calls.distinct())
    }

    @Test fun anOverwriteReplacesTheTargetAndItsUndoRestoresIt() {
        val session = FakeManagedSession(directory, target)
        launchEditor(session).use { scenario ->
            compose.onNodeWithTag("maple.editor.save.overwrite").performScrollTo().performClick()
            compose.onNodeWithTag("maple.dialog.confirm").performClick()
            compose.waitUntil(TIMEOUT) { exists("maple.editor.replacementUndo") }
            assertEquals(20 to 40, size(target))
            assertEquals(listOf("wide.png"), directory.list().orEmpty().sorted())

            compose.onNodeWithTag("maple.editor.replacementUndo.action").performClick()
            compose.waitUntil(TIMEOUT) { scenario.state == androidx.lifecycle.Lifecycle.State.DESTROYED }
        }
        assertEquals(40 to 20, size(target))
        assertTrue(session.calls.contains("prepareTargetReplacement"))
        assertTrue(session.calls.contains("undoTargetReplacement"))
    }

    @Test fun aTargetThatChangedMeanwhileIsNotOverwritten() {
        val session = FakeManagedSession(directory, target)
        launchEditor(session).use { scenario ->
            // Another app rewrites the image while the editor is open.
            writePng(target, Color.GREEN, 30, 30)
            target.setLastModified(System.currentTimeMillis() + 5_000)
            compose.onNodeWithTag("maple.editor.save.overwrite").performScrollTo().performClick()
            compose.onNodeWithTag("maple.dialog.confirm").performClick()
            compose.waitUntil(TIMEOUT) { scenario.state == androidx.lifecycle.Lifecycle.State.DESTROYED }
        }
        assertEquals(30 to 30, size(target))
        assertTrue(session.calls.contains("abortOutput"))
        assertEquals(listOf("wide.png"), directory.list().orEmpty().filterNot { it.startsWith(".") }.sorted())
    }

    @Test fun theConverterWritesItsNewFileThroughTheSessionToo() {
        val session = FakeManagedSession(directory, target)
        ActivityScenario.launchActivityForResult<ImageConverterActivity>(
            HostRequestFixtures.toolIntent(context, target, session, ImageToolsPlugin.CONVERT_ACTION_ID),
        ).use { scenario ->
            compose.waitUntil(TIMEOUT) { exists("maple.converter.convert") && enabled("maple.converter.convert") }
            compose.onNodeWithTag("maple.converter.convert").performClick()
            compose.waitUntil(TIMEOUT) { scenario.state == androidx.lifecycle.Lifecycle.State.DESTROYED }
        }
        // PNG stays PNG by default; the new file sits next to the untouched original.
        val converted = directory.listFiles().orEmpty().map { it.name }.filter { it.startsWith("wide_converted.") }
        assertEquals(1, converted.size)
        assertEquals(40 to 20, size(File(directory, converted.single())))
        assertEquals(40 to 20, size(target))
        assertTrue(session.calls.containsAll(listOf("prepareOutput", "openPendingOutput", "commitOutput")))
    }

    /**
     * The host side of a managed target, on a plain directory: outputs are staged, a replacement checks that the
     * target is still the one the request named, keeps the previous bytes for one undo, and renames over it.
     */
    private class FakeManagedSession(private val directory: File, private val target: File) : UnsupportedHostSessionStub() {
        val calls = java.util.Collections.synchronizedList(mutableListOf<String>())
        private val staged = mutableMapOf<String, File>()
        private val names = mutableMapOf<String, String>()
        private var replacing: String? = null
        private val identity = target.lastModified() to target.length()
        private var previous: ByteArray? = null
        private var replaced: Pair<Long, Long>? = null

        private fun stage(): String = UUID.randomUUID().toString().also { staged[it] = File(directory, ".staged-$it") }

        override fun prepareOutput(displayName: String?, mimeType: String?, conflictPolicy: Int): Bundle {
            calls += "prepareOutput"
            val id = stage()
            names[id] = requireNotNull(displayName)
            return Bundle().apply { putString(ExplorerActionHostSessionKeys.OUTPUT_TRANSACTION_ID, id) }
        }

        override fun prepareTargetReplacement(targetId: String?): Bundle {
            calls += "prepareTargetReplacement"
            check(targetId == "tool-target")
            val id = stage()
            replacing = id
            return Bundle().apply { putString(ExplorerActionHostSessionKeys.OUTPUT_TRANSACTION_ID, id) }
        }

        override fun openOutput(transactionId: String?): ParcelFileDescriptor {
            calls += "openOutput"
            return ParcelFileDescriptor.open(staged.getValue(requireNotNull(transactionId)), ParcelFileDescriptor.MODE_CREATE or ParcelFileDescriptor.MODE_TRUNCATE or ParcelFileDescriptor.MODE_WRITE_ONLY)
        }

        override fun openPendingOutput(transactionId: String?): ParcelFileDescriptor {
            calls += "openPendingOutput"
            return ParcelFileDescriptor.open(staged.getValue(requireNotNull(transactionId)), ParcelFileDescriptor.MODE_READ_ONLY)
        }

        override fun commitOutput(transactionId: String?): Bundle {
            calls += "commitOutput"
            val id = requireNotNull(transactionId)
            val file = staged.getValue(id)
            val result = Bundle().apply { putInt(ExplorerActionHostSessionKeys.OUTPUT_STATE, ExplorerActionHostSessionValues.OUTPUT_STATE_COMMITTED) }
            if (id == replacing) {
                check(target.lastModified() to target.length() == identity) { "Explorer Action replacement target changed before commit" }
                previous = target.readBytes()
                check(file.renameTo(target))
                replaced = target.lastModified() to target.length()
                result.putString(ExplorerActionHostSessionKeys.TARGET_REPLACEMENT_HISTORY_ID, "history-1")
                result.putInt(ExplorerActionHostSessionKeys.TARGET_REPLACEMENT_UNDO_STATE, ExplorerActionHostSessionValues.TARGET_REPLACEMENT_UNDO_STATE_AVAILABLE)
            } else {
                check(file.renameTo(File(directory, names.getValue(id))))
            }
            staged.remove(id)
            return result
        }

        override fun abortOutput(transactionId: String?) {
            calls += "abortOutput"
            staged.remove(transactionId)?.delete()
        }

        override fun undoTargetReplacement(targetId: String?, replacementHistoryId: String?): Bundle {
            calls += "undoTargetReplacement"
            val bytes = previous
            val state = if (replacementHistoryId == "history-1" && bytes != null && target.lastModified() to target.length() == replaced) {
                target.writeBytes(bytes)
                previous = null
                ExplorerActionHostSessionValues.TARGET_REPLACEMENT_UNDO_STATE_RESTORED
            } else {
                ExplorerActionHostSessionValues.TARGET_REPLACEMENT_UNDO_STATE_STALE
            }
            return Bundle().apply { putInt(ExplorerActionHostSessionKeys.TARGET_REPLACEMENT_UNDO_STATE, state) }
        }

        override fun close() {
            calls += "close"
        }
    }

    private companion object {
        const val TIMEOUT = 20_000L
    }
}
