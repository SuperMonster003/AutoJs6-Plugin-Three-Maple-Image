package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.content.ClipData
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.ParcelFileDescriptor
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import org.autojs.plugin.explorer.api.ExplorerActionHostSessionKeys
import org.autojs.plugin.explorer.api.ExplorerActionIntentExtras
import org.autojs.plugin.explorer.api.ExplorerActionIntentValues
import org.autojs.plugin.explorer.api.ExplorerActionPluginActions
import org.autojs.plugin.explorer.api.ExplorerActionTargetKeys
import org.autojs.plugin.explorer.api.ExplorerActionValues
import org.autojs.plugin.explorer.api.IExplorerActionHostSession
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.Locale
import java.util.UUID

/** ROADMAP 6.4: the "View as album" directory action browses a host directory through its session. */
@RunWith(AndroidJUnit4::class)
class ExplorerDirectoryInstrumentationTest {

    @get:Rule
    val compose = createEmptyComposeRule()

    private val instrumentation get() = InstrumentationRegistry.getInstrumentation()
    private val context get() = instrumentation.targetContext

    @Test
    fun browsesFoldersAndImagesThroughTheHostSessionAndOpensTheViewer() {
        val root = File(context.cacheDir, "hostdir-" + UUID.randomUUID().toString().take(8)).apply { mkdirs() }
        try {
            File(root, "a.png").writeBytes(MediaStoreTestImages.solidPng(Color.RED))
            File(root, "b.png").writeBytes(MediaStoreTestImages.solidPng(Color.GREEN))
            File(root, "notes.txt").writeText("not an image")
            File(root, "sub").mkdirs()
            File(root, "sub/c.png").writeBytes(MediaStoreTestImages.solidPng(Color.BLUE))
            val session = TreeHostSession(root)

            // ActivityScenario adds FLAG_ACTIVITY_CLEAR_TASK, which the strict flag check rejects; launch like the host does.
            val activity = instrumentation.startActivitySync(
                directoryIntent(session).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            ) as ExplorerDirectoryActivity
            fun onActivity(block: (ExplorerDirectoryActivity) -> Unit) = instrumentation.runOnMainSync { block(activity) }
            run {
                compose.waitUntil(TIMEOUT_MILLIS) {
                    compose.onAllNodes(hasTestTag("maple.hostdir.grid")).fetchSemanticsNodes().isNotEmpty()
                }
                compose.onNodeWithTag("maple.hostdir.folder.sub").assertIsDisplayed()
                compose.onNodeWithTag("maple.hostdir.image.a.png").assertIsDisplayed()
                compose.onNodeWithTag("maple.hostdir.image.b.png").assertIsDisplayed()
                compose.onNodeWithTag("maple.hostdir.image.notes.txt").assertDoesNotExist()
                onActivity { activity ->
                    assertEquals("", activity.stateForTesting.path)
                    assertEquals(TARGET_NAME, activity.stateForTesting.title)
                }

                compose.onNodeWithTag("maple.hostdir.folder.sub").performClick()
                compose.waitUntil(TIMEOUT_MILLIS) {
                    compose.onAllNodes(hasTestTag("maple.hostdir.image.c.png")).fetchSemanticsNodes().isNotEmpty()
                }
                onActivity { activity ->
                    assertEquals("sub", activity.stateForTesting.path)
                    assertEquals("sub", activity.stateForTesting.title)
                }
                assertTrue(session.listedPaths.contains("sub"))

                compose.onNodeWithTag("maple.hostdir.image.c.png").performClick()
                val viewer = awaitViewer()
                compose.waitUntil(TIMEOUT_MILLIS) {
                    onMain {
                        val source = viewer.sourceForTesting
                        source?.kind == ViewerSourceKind.HOST_DIRECTORY && source.pages.size == 1 && viewer.viewerStateForTesting.printEnabled
                    }
                }
                assertEquals("c.png", onMain { viewer.viewerStateForTesting.title })
                assertTrue(session.openedPaths.contains("sub/c.png"))
                instrumentation.runOnMainSync { viewer.finish() }
                compose.waitUntil(TIMEOUT_MILLIS) {
                    onMain {
                        ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED).any { it is ExplorerDirectoryActivity }
                    }
                }
                assertEquals("the viewer borrows the session and never closes it", 0, session.closeCount)

                onActivity { activity -> activity.onBackPressedDispatcher.onBackPressed() }
                compose.waitUntil(TIMEOUT_MILLIS) {
                    compose.onAllNodes(hasTestTag("maple.hostdir.image.a.png")).fetchSemanticsNodes().isNotEmpty()
                }
                onActivity { activity -> activity.onBackPressedDispatcher.onBackPressed() }
                compose.waitUntil(TIMEOUT_MILLIS) { onMain { activity.isDestroyed } }
            }
            assertEquals(1, session.closeCount)
        } finally {
            root.deleteRecursively()
        }
    }

    @Test
    fun resolvesOnlyWellFormedDirectoryRequests() {
        val session = TreeHostSession(context.cacheDir)
        assertNotNull(ImageRequestPolicy.resolveExplorerDirectory(directoryIntent(session)))
        assertNull(
            "file targets are not directories",
            ImageRequestPolicy.resolveExplorerDirectory(
                directoryIntent(session).apply {
                    val target = getParcelableArrayListExtra<Bundle>(ExplorerActionIntentExtras.TARGETS)!!.single()
                    target.putInt(ExplorerActionTargetKeys.KIND, ExplorerActionValues.TARGET_FILE)
                    putParcelableArrayListExtra(ExplorerActionIntentExtras.TARGETS, arrayListOf(target))
                },
            ),
        )
        assertNull(
            "the view-image action is not a directory request",
            ImageRequestPolicy.resolveExplorerDirectory(
                directoryIntent(session).apply { putExtra(ExplorerActionIntentExtras.ACTION_ID, ThreeMapleImagePlugin.ACTION_ID) },
            ),
        )
        assertNull(
            "a host session is mandatory",
            ImageRequestPolicy.resolveExplorerDirectory(directoryIntent(session).apply { removeExtra(ExplorerActionIntentExtras.HOST_SESSION) }),
        )
        assertNull(
            "the clip must carry the directory URI",
            ImageRequestPolicy.resolveExplorerDirectory(
                directoryIntent(session).apply { clipData = ClipData.newRawUri("other", Uri.parse("content://images.test/root/other")) },
            ),
        )
    }

    private fun directoryIntent(session: IExplorerActionHostSession): Intent {
        val target = Bundle().apply {
            putString(ExplorerActionTargetKeys.ID, TARGET_ID)
            putParcelable(ExplorerActionTargetKeys.URI, TARGET_URI)
            putString(ExplorerActionTargetKeys.DISPLAY_NAME, TARGET_NAME)
            putInt(ExplorerActionTargetKeys.KIND, ExplorerActionValues.TARGET_DIRECTORY)
            putString(ExplorerActionTargetKeys.MIME_TYPE, DIRECTORY_MIME_TYPE)
            putLong(ExplorerActionTargetKeys.SIZE, -1L)
            putLong(ExplorerActionTargetKeys.LAST_MODIFIED, 1_700_000_000_000L)
        }
        return Intent(ExplorerActionPluginActions.EXECUTE).apply {
            setClassName(context, ExplorerDirectoryActivity::class.java.name)
            setDataAndType(TARGET_URI, DIRECTORY_MIME_TYPE)
            clipData = ClipData.newRawUri(TARGET_NAME, TARGET_URI)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PREFIX_URI_PERMISSION)
            putExtra(ExplorerActionIntentExtras.ACTION_ID, ThreeMapleImagePlugin.BROWSE_DIRECTORY_ACTION_ID)
            putExtra(ExplorerActionIntentExtras.PROTOCOL_VERSION, ThreeMapleImagePlugin.PROTOCOL_VERSION)
            putExtra(ExplorerActionIntentExtras.REQUEST_ID, UUID.randomUUID().toString())
            putExtra(ExplorerActionIntentExtras.DISPLAY_NAME, TARGET_NAME)
            putExtra(ExplorerActionIntentExtras.SIZE, -1L)
            putExtra(ExplorerActionIntentExtras.PARENT_URI, PARENT_URI)
            putExtra(ExplorerActionIntentExtras.PARENT_DISPLAY_PATH, "/images")
            putExtra(ExplorerActionIntentExtras.HOST_VERSION_CODE, ThreeMapleImagePlugin.REQUIRED_HOST_VERSION)
            putExtra(ExplorerActionIntentExtras.SOURCE_SURFACE, ExplorerActionIntentValues.SOURCE_SURFACE_MAIN)
            putParcelableArrayListExtra(ExplorerActionIntentExtras.TARGETS, arrayListOf(target))
            putExtra(
                ExplorerActionIntentExtras.HOST_SESSION,
                Bundle().apply { putBinder(ExplorerActionHostSessionKeys.BINDER, session.asBinder()) },
            )
        }
    }

    private fun awaitViewer(): ImageViewerActivity {
        var viewer: ImageViewerActivity? = null
        compose.waitUntil(TIMEOUT_MILLIS) {
            viewer = onMain {
                ActivityLifecycleMonitorRegistry.getInstance()
                    .getActivitiesInStage(Stage.RESUMED)
                    .filterIsInstance<ImageViewerActivity>()
                    .firstOrNull()
            }
            viewer != null
        }
        return requireNotNull(viewer)
    }

    private fun <T> onMain(block: () -> T): T {
        var value: T? = null
        instrumentation.runOnMainSync { value = block() }
        @Suppress("UNCHECKED_CAST")
        return value as T
    }

    /** A host session over a real directory tree, listing and opening files the way the host does. */
    private class TreeHostSession(private val root: File) : UnsupportedHostSessionStub() {
        val listedPaths = mutableListOf<String>()
        val openedPaths = mutableListOf<String>()
        var closeCount = 0

        override fun listChildren(targetId: String?, relativePath: String?, offset: Int, limit: Int): Bundle {
            val path = relativePath.orEmpty()
            listedPaths += path
            val directory = if (path.isEmpty()) root else File(root, path)
            val children = directory.listFiles().orEmpty().sortedBy { it.name.lowercase(Locale.ROOT) }
            val page = children.drop(offset).take(limit)
            val items = page.map { child ->
                Bundle().apply {
                    putString(ExplorerActionHostSessionKeys.RELATIVE_PATH, if (path.isEmpty()) child.name else path + "/" + child.name)
                    putString(ExplorerActionHostSessionKeys.DISPLAY_NAME, child.name)
                    putInt(ExplorerActionHostSessionKeys.KIND, if (child.isDirectory) ExplorerActionValues.TARGET_DIRECTORY else ExplorerActionValues.TARGET_FILE)
                    putString(
                        ExplorerActionHostSessionKeys.MIME_TYPE,
                        when {
                            child.isDirectory -> DIRECTORY_MIME_TYPE
                            child.extension == "png" -> "image/png"
                            else -> "text/plain"
                        },
                    )
                    putLong(ExplorerActionHostSessionKeys.SIZE, if (child.isFile) child.length() else -1L)
                    putLong(ExplorerActionHostSessionKeys.LAST_MODIFIED, child.lastModified())
                    putBoolean(ExplorerActionHostSessionKeys.READABLE, true)
                    putBoolean(ExplorerActionHostSessionKeys.SYMBOLIC_LINK, false)
                }
            }
            val nextOffset = offset + page.size
            return Bundle().apply {
                putParcelableArrayList(ExplorerActionHostSessionKeys.ITEMS, ArrayList(items))
                putInt(ExplorerActionHostSessionKeys.NEXT_OFFSET, nextOffset)
                putBoolean(ExplorerActionHostSessionKeys.COMPLETE, nextOffset >= children.size)
            }
        }

        override fun openFile(targetId: String?, relativePath: String?): ParcelFileDescriptor {
            val path = requireNotNull(relativePath)
            openedPaths += path
            return ParcelFileDescriptor.open(File(root, path), ParcelFileDescriptor.MODE_READ_ONLY)
        }

        override fun close() {
            closeCount += 1
        }

        override fun attachClient(clientToken: android.os.IBinder?) = Unit
    }

    private companion object {
        const val TIMEOUT_MILLIS = 20_000L
        const val TARGET_ID = "directory-target"
        const val TARGET_NAME = "Camera"
        const val DIRECTORY_MIME_TYPE = "inode/directory"
        val PARENT_URI: Uri = Uri.parse("content://images.test/root")
        val TARGET_URI: Uri = Uri.parse("content://images.test/root/Camera")
    }
}
