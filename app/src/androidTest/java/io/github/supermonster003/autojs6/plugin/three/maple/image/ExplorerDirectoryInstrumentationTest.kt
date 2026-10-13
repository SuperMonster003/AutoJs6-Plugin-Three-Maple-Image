package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.content.ClipData
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import org.autojs.plugin.explorer.api.ExplorerActionIntentExtras
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

    private fun directoryIntent(session: IExplorerActionHostSession): Intent = HostRequestFixtures.directoryIntent(context, session)

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

    private companion object {
        const val TIMEOUT_MILLIS = 20_000L
        const val TARGET_NAME = HostRequestFixtures.DIRECTORY_NAME
    }
}
