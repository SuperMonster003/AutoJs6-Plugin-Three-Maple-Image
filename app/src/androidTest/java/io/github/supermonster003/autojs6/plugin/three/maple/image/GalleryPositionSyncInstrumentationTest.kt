package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.content.ContentUris
import android.graphics.Color
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Locale
import java.util.UUID

/** ROADMAP 6.3: the viewer reports the item it was left on and the media grid scrolls to it. */
@RunWith(AndroidJUnit4::class)
class GalleryPositionSyncInstrumentationTest {

    @get:Rule
    val compose = createEmptyComposeRule()

    @get:Rule
    val permission: GrantPermissionRule = GrantPermissionRule.grant(*MediaStoreTestImages.permissions())

    private val instrumentation get() = InstrumentationRegistry.getInstrumentation()
    private val context get() = instrumentation.targetContext
    private val record by lazy { MediaPermissionRecordStore(context) }
    private var originalRequested = false

    @Before
    fun silencePermissionRequest() {
        originalRequested = record.requestedBefore
        record.markRequested()
    }

    @After
    fun restoreRecord() {
        if (!originalRequested) record.reset()
    }

    /** Open item 50, page to item 60 in the viewer, come back: the grid shows item 60. */
    @Test
    fun gridScrollsToTheItemTheViewerWasLeftOn() {
        val folder = "MapleSyncTest-" + UUID.randomUUID().toString().take(8)
        val inserted = (1..COUNT).map { n ->
            MediaStoreTestImages.insert(context, folder, "sync-%03d.png".format(Locale.ROOT, n), if (n % 2 == 0) Color.CYAN else Color.YELLOW)
        }
        try {
            val ids = inserted.map { ContentUris.parseId(it) }.toSet()
            val bucketId = runBlocking { MediaStoreRepository(context).loadAll() }.first { it.id in ids }.bucketId
            ActivityScenario.launch(HomeActivity::class.java).use { scenario ->
                val albumTag = "maple.gallery.album.media.$bucketId"
                compose.waitUntil(TIMEOUT_MILLIS) {
                    compose.onAllNodes(hasTestTag("maple.gallery.grid")).fetchSemanticsNodes().isNotEmpty()
                }
                compose.onNodeWithTag("maple.gallery.grid").performScrollToNode(hasTestTag(albumTag))
                compose.onNodeWithTag(albumTag).performClick()

                var items: List<MediaItem> = emptyList()
                compose.waitUntil(TIMEOUT_MILLIS) {
                    scenario.onActivity { activity -> items = activity.galleryForTesting.grid.value.items }
                    items.size == COUNT && compose.onAllNodes(hasTestTag("maple.media.grid")).fetchSemanticsNodes().isNotEmpty()
                }
                val opened = items[OPEN_INDEX]
                val target = items[TARGET_INDEX]
                compose.onNodeWithTag("maple.media.grid").performScrollToNode(hasTestTag("maple.media.item.${opened.id}"))
                compose.onNodeWithTag("maple.media.item.${opened.id}").performClick()

                val viewer = awaitViewer()
                compose.waitUntil(TIMEOUT_MILLIS) { onMain { viewer.sourceForTesting?.pages?.size == COUNT } }
                assertEquals(OPEN_INDEX, onMain { viewer.viewerStateForTesting.pageIndex })
                instrumentation.runOnMainSync {
                    repeat(TARGET_INDEX - OPEN_INDEX) { viewer.flipPageForTesting(ZoomableImageView.DIRECTION_NEXT) }
                }
                compose.waitUntil(TIMEOUT_MILLIS) { onMain { viewer.viewerStateForTesting.pageIndex == TARGET_INDEX } }
                assertEquals(target.id, onMain { viewer.sourceForTesting?.itemId(TARGET_INDEX) })
                instrumentation.runOnMainSync { viewer.finish() }

                compose.waitUntil(TIMEOUT_MILLIS) {
                    compose.onAllNodes(hasTestTag("maple.media.item.${target.id}")).fetchSemanticsNodes().isNotEmpty()
                }
                compose.onNodeWithTag("maple.media.item.${target.id}").assertIsDisplayed()
            }
        } finally {
            MediaStoreTestImages.cleanUp(context, inserted, folder)
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

    private companion object {
        const val COUNT = 64
        const val OPEN_INDEX = 49
        const val TARGET_INDEX = 59
        const val TIMEOUT_MILLIS = 20_000L
    }
}
