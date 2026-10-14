package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import org.autojs.plugin.explorer.api.ExplorerActionHostSessionKeys
import org.autojs.plugin.explorer.api.ExplorerActionHostSessionValues
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.UUID

/**
 * ROADMAP 7.2 (protocol v24): in a viewer opened by the file manager, "Delete" moves the shown request target to the
 * host Trash, drops its page, and the snackbar's undo brings both back. Pages that are not request targets get no
 * delete.
 */
@RunWith(AndroidJUnit4::class)
class HostTrashInstrumentationTest {

    @get:Rule
    val compose = createEmptyComposeRule()

    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private lateinit var directory: File

    @Before fun createImages() {
        directory = File(context.cacheDir, "standalone-images/trash-${UUID.randomUUID()}").apply { mkdirs() }
        listOf("a.png" to Color.RED, "b.png" to Color.BLUE, "c.png" to Color.GREEN).forEach { (name, color) ->
            val bitmap = Bitmap.createBitmap(30, 20, Bitmap.Config.ARGB_8888).apply { eraseColor(color) }
            File(directory, name).outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
            bitmap.recycle()
        }
    }

    @After fun removeImages() {
        directory.deleteRecursively()
    }

    // The trampoline has no Compose content: until the viewer is up there is nothing to search.
    private fun exists(tag: String): Boolean = runCatching { compose.onAllNodes(hasTestTag(tag)).fetchSemanticsNodes().isNotEmpty() }.getOrDefault(false)

    private fun enabled(tag: String): Boolean = runCatching { compose.onNodeWithTag(tag).assertIsEnabled() }.isSuccess

    private fun shows(text: String): Boolean =
        runCatching { compose.onAllNodes(hasText(text, substring = true)).fetchSemanticsNodes().isNotEmpty() }.getOrDefault(false)

    @Test fun deletingASelectedImageMovesItToTheHostTrashAndUndoBringsItBack() {
        val session = TrashSession()
        val files = listOf(File(directory, "a.png"), File(directory, "b.png"))
        ActivityScenario.launchActivityForResult<ExplorerActionActivity>(HostRequestFixtures.selectionIntent(context, files, session)).use {
            compose.waitUntil(TIMEOUT) { exists("maple.viewer.delete") && enabled("maple.viewer.delete") }
            compose.waitUntil(TIMEOUT) { shows("1 / 2") }
            compose.onNodeWithTag("maple.viewer.delete").performClick()

            val moved = context.getString(R.string.host_trash_moved)
            compose.waitUntil(TIMEOUT) { shows(moved) }
            assertEquals(listOf(listOf("target-0")), session.moved)
            // One page left: no page counter any more.
            compose.waitUntil(TIMEOUT) { !shows("/ 2") }

            compose.onNode(hasText(context.getString(R.string.action_undo)) and hasAnyAncestor(hasTestTag("maple.viewer.snackbar"))).performClick()
            compose.waitUntil(TIMEOUT) { session.undone == listOf("batch-1") }
            compose.waitUntil(TIMEOUT) { shows("/ 2") }
        }
    }

    @Test fun deletingTheOnlyImageClosesTheViewer() {
        val session = TrashSession()
        ActivityScenario.launchActivityForResult<ExplorerActionActivity>(
            HostRequestFixtures.selectionIntent(context, listOf(File(directory, "a.png")), session),
        ).use {
            compose.waitUntil(TIMEOUT) { exists("maple.viewer.delete") && enabled("maple.viewer.delete") }
            compose.onNodeWithTag("maple.viewer.delete").performClick()
            compose.waitUntil(TIMEOUT) { session.moved == listOf(listOf("target-0")) && session.closed }
        }
    }

    @Test fun aRefusedMoveKeepsThePage() {
        val session = TrashSession(refuse = true)
        val files = listOf(File(directory, "a.png"), File(directory, "b.png"))
        ActivityScenario.launchActivityForResult<ExplorerActionActivity>(HostRequestFixtures.selectionIntent(context, files, session)).use {
            compose.waitUntil(TIMEOUT) { exists("maple.viewer.delete") && enabled("maple.viewer.delete") }
            compose.onNodeWithTag("maple.viewer.delete").performClick()
            val failed = context.getString(R.string.host_trash_failed)
            compose.waitUntil(TIMEOUT) { shows(failed) }
            assertEquals(true, shows("/ 2"))
        }
    }

    @Test fun aMovedPageStaysAwayWhenTheViewerIsRecreated() {
        val session = TrashSession()
        val files = listOf(File(directory, "a.png"), File(directory, "b.png"))
        ActivityScenario.launchActivityForResult<ExplorerActionActivity>(HostRequestFixtures.selectionIntent(context, files, session)).use {
            compose.waitUntil(TIMEOUT) { shows("1 / 2") && exists("maple.viewer.delete") && enabled("maple.viewer.delete") }
            compose.onNodeWithTag("maple.viewer.delete").performClick()
            compose.waitUntil(TIMEOUT) { session.moved.isNotEmpty() && !shows("/ 2") }

            val before = resumedViewer()
            InstrumentationRegistry.getInstrumentation().runOnMainSync { requireNotNull(before).recreate() }
            compose.waitUntil(TIMEOUT) { resumedViewer().let { it != null && it !== before } && exists("maple.viewer.delete") }
            compose.waitUntil(TIMEOUT) { shows("b.png") }
            assertEquals(false, shows("/ 2"))
            assertEquals(false, session.closed)
        }
    }

    private fun resumedViewer(): ImageViewerActivity? {
        var viewer: ImageViewerActivity? = null
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            viewer = ActivityLifecycleMonitorRegistry.getInstance()
                .getActivitiesInStage(Stage.RESUMED)
                .filterIsInstance<ImageViewerActivity>()
                .firstOrNull()
        }
        return viewer
    }

    @Test fun inTheSiblingViewerOnlyTheOpenedImageCanBeDeleted() {
        val session = TrashSession(siblings = TreeHostSession(directory))
        ActivityScenario.launchActivityForResult<ExplorerActionActivity>(
            HostRequestFixtures.imageIntent(context, File(directory, "b.png"), session),
        ).use {
            compose.waitUntil(TIMEOUT) { shows("2 / 3") && exists("maple.viewer.delete") && enabled("maple.viewer.delete") }
            // A sibling is no request target: "open with" keeps its place and nothing offers a delete.
            compose.onNodeWithTag("maple.viewer.strip.2").performClick()
            compose.waitUntil(TIMEOUT) { shows("3 / 3") && exists("maple.viewer.openWith") && !exists("maple.viewer.delete") }
            compose.waitUntil(TIMEOUT) { session.siblingOpens.get() > 0 }
            compose.onNodeWithTag("maple.viewer.strip.1").performClick()
            compose.waitUntil(TIMEOUT) { shows("2 / 3") && exists("maple.viewer.delete") && enabled("maple.viewer.delete") }
            val opensBefore = session.siblingOpens.get()
            compose.onNodeWithTag("maple.viewer.delete").performClick()

            compose.waitUntil(TIMEOUT) { shows(context.getString(R.string.host_trash_moved)) }
            assertEquals(listOf(listOf("image-target")), session.moved)
            // c.png moves up into the freed place and is read through the session again under a new page index.
            compose.waitUntil(TIMEOUT) { shows("2 / 2") && session.siblingOpens.get() > opensBefore }
            compose.waitUntil(TIMEOUT) { exists("maple.viewer.openWith") }
            assertEquals(false, exists("maple.viewer.error"))
        }
    }

    /**
     * The host side of a `targetTrash` session: every move is its own batch, undo restores a known batch. With
     * [siblings] it also lists and opens the files next to the target.
     */
    private class TrashSession(
        private val refuse: Boolean = false,
        private val siblings: TreeHostSession? = null,
    ) : UnsupportedHostSessionStub() {
        val moved = java.util.Collections.synchronizedList(mutableListOf<List<String>>())
        val undone = java.util.Collections.synchronizedList(mutableListOf<String>())
        val siblingOpens = java.util.concurrent.atomic.AtomicInteger()

        override fun listChildren(targetId: String?, relativePath: String?, offset: Int, limit: Int): Bundle =
            siblings?.listChildren(targetId, relativePath, offset, limit) ?: super.listChildren(targetId, relativePath, offset, limit)

        override fun openFile(targetId: String?, relativePath: String?): android.os.ParcelFileDescriptor {
            val tree = siblings ?: return super.openFile(targetId, relativePath)
            if (relativePath == "c.png") siblingOpens.incrementAndGet()
            return tree.openFile(targetId, relativePath)
        }

        @Volatile
        var closed = false

        override fun moveTargetsToTrash(targetIds: MutableList<String>?, outputTransactionIds: MutableList<String>?): Bundle {
            check(outputTransactionIds.isNullOrEmpty()) { "v24 viewers never pass output proofs" }
            val ids = requireNotNull(targetIds).toList()
            if (refuse) {
                return Bundle().apply { putInt(ExplorerActionHostSessionKeys.TARGET_TRASH_STATE, ExplorerActionHostSessionValues.TARGET_TRASH_STATE_FAILED) }
            }
            moved += ids
            return Bundle().apply {
                putInt(ExplorerActionHostSessionKeys.TARGET_TRASH_STATE, ExplorerActionHostSessionValues.TARGET_TRASH_STATE_COMMITTED)
                putInt(ExplorerActionHostSessionKeys.TARGET_TRASH_MOVED_COUNT, ids.size)
                putString(ExplorerActionHostSessionKeys.TARGET_TRASH_BATCH_ID, "batch-${moved.size}")
            }
        }

        override fun undoTargetTrashBatch(targetTrashBatchId: String?): Bundle {
            undone += requireNotNull(targetTrashBatchId)
            return Bundle().apply {
                putInt(ExplorerActionHostSessionKeys.TARGET_TRASH_BATCH_STATE, ExplorerActionHostSessionValues.TARGET_TRASH_BATCH_STATE_RESTORED)
            }
        }

        override fun close() {
            closed = true
        }
    }

    private companion object {
        const val TIMEOUT = 20_000L
    }
}
