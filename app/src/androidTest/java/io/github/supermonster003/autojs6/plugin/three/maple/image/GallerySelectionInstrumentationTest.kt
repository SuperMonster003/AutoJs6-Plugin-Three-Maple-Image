package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.content.ContentUris
import android.graphics.Color
import android.net.Uri
import android.os.SystemClock
import android.util.Log
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.longClick
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Locale
import java.util.UUID

/**
 * ROADMAP 7.1 acceptance: the selection survives an activity recreation (rotation), and select all over a large
 * album stays quick. The 1000 image case inserts its own images and only runs with `-e selectionLoad true`.
 */
@RunWith(AndroidJUnit4::class)
class GallerySelectionInstrumentationTest {

    @get:Rule
    val compose = createEmptyComposeRule()

    @get:Rule
    val permission: GrantPermissionRule = GrantPermissionRule.grant(*MediaStoreTestImages.permissions())

    private val instrumentation get() = InstrumentationRegistry.getInstrumentation()
    private val context get() = instrumentation.targetContext
    private val record by lazy { MediaPermissionRecordStore(context) }
    private var originalRequested = false
    private val folder = "MapleSelect-" + UUID.randomUUID().toString().take(8)
    private val inserted = ArrayList<Uri>()

    @Before
    fun silencePermissionRequest() {
        originalRequested = record.requestedBefore
        record.markRequested()
    }

    @After
    fun cleanUp() {
        MediaStoreTestImages.cleanUp(context, inserted, folder)
        if (!originalRequested) record.reset()
    }

    @Test
    fun theSelectionSurvivesARecreation() {
        insert(COUNT)
        openAlbum { scenario, items ->
            compose.onNodeWithTag("maple.media.item.${items[1].id}").performTouchInput { longClick() }
            compose.onNodeWithTag("maple.media.item.${items[2].id}").performClick()
            compose.waitForIdle()
            assertEquals(setOf(items[1].id, items[2].id), selection(scenario))
            scenario.recreate()
            compose.waitUntil(TIMEOUT_MILLIS) { compose.onAllNodes(hasTestTag("maple.selection.share")).fetchSemanticsNodes().isNotEmpty() }
            assertEquals(setOf(items[1].id, items[2].id), selection(scenario))
            compose.onNodeWithTag("maple.topBar.navigation").performClick()
            compose.waitForIdle()
            assertEquals(emptySet<Long>(), selection(scenario))
        }
    }

    @Test
    fun aFavoriteStaysMarkedWhenTheAlbumOpensAgain() {
        insert(COUNT)
        val favorites = FavoritesRepository(context)
        openAlbum { scenario, items ->
            val first = items[0].id
            try {
                compose.onNodeWithTag("maple.media.item.$first").performTouchInput { longClick() }
                compose.onNodeWithTag("maple.selection.favorite").performClick()
                compose.waitUntil(TIMEOUT_MILLIS) { first in favoriteIds(scenario) }
                // Coming back to the album (or refreshing it) opens it again; the marks must not wait for the next change.
                scenario.onActivity { activity -> activity.galleryForTesting.let { it.openAlbum(it.grid.value.album!!) } }
                compose.waitForIdle()
                assertTrue(first in favoriteIds(scenario))
                compose.onNodeWithTag("maple.media.item.$first").performTouchInput { longClick() }
                var unfavorite = ""
                scenario.onActivity { unfavorite = it.getString(R.string.action_unfavorite) }
                compose.onNodeWithTag("maple.selection.favorite").assert(hasText(unfavorite))
            } finally {
                runBlocking { favorites.remove(listOf(first)) }
            }
        }
    }

    @Test
    fun selectAllOverAThousandImagesIsQuick() {
        val enabled = InstrumentationRegistry.getArguments().getString("selectionLoad") == "true"
        assumeTrue("set -e selectionLoad true to insert 1000 images", enabled)
        insert(LOAD_COUNT)
        openAlbum { scenario, items ->
            compose.onNodeWithTag("maple.media.item.${items[0].id}").performTouchInput { longClick() }
            compose.waitForIdle()
            val started = SystemClock.elapsedRealtime()
            compose.onNodeWithTag("maple.selection.selectAll").performClick()
            compose.waitUntil(TIMEOUT_MILLIS) { selection(scenario).size == LOAD_COUNT }
            compose.waitForIdle()
            val elapsed = SystemClock.elapsedRealtime() - started
            Log.i(TAG, "select all over $LOAD_COUNT images: $elapsed ms")
            assertTrue("select all took $elapsed ms", elapsed < SELECT_ALL_BUDGET_MILLIS)
        }
    }

    private fun insert(count: Int) {
        for (n in 1..count) {
            inserted += MediaStoreTestImages.insert(context, folder, "select-%04d.png".format(Locale.ROOT, n), if (n % 2 == 0) Color.MAGENTA else Color.GREEN)
        }
    }

    private fun favoriteIds(scenario: ActivityScenario<HomeActivity>): Set<Long> {
        var ids: Set<Long> = emptySet()
        scenario.onActivity { ids = it.galleryForTesting.grid.value.favoriteIds }
        return ids
    }

    private fun selection(scenario: ActivityScenario<HomeActivity>): Set<Long> {
        var selection: Set<Long> = emptySet()
        scenario.onActivity { selection = it.galleryForTesting.grid.value.selection }
        return selection
    }

    private fun openAlbum(block: (ActivityScenario<HomeActivity>, List<MediaItem>) -> Unit) {
        val ids = inserted.map { ContentUris.parseId(it) }.toSet()
        val bucketId = runBlocking { MediaStoreRepository(context).also { it.invalidate() }.loadAll() }.first { it.id in ids }.bucketId
        ActivityScenario.launch(HomeActivity::class.java).use { scenario ->
            val albumTag = "maple.gallery.album.media.$bucketId"
            compose.waitUntil(TIMEOUT_MILLIS) { compose.onAllNodes(hasTestTag("maple.gallery.grid")).fetchSemanticsNodes().isNotEmpty() }
            compose.onNodeWithTag("maple.gallery.grid").performScrollToNode(hasTestTag(albumTag))
            compose.onNodeWithTag(albumTag).performClick()
            var items: List<MediaItem> = emptyList()
            compose.waitUntil(TIMEOUT_MILLIS) {
                scenario.onActivity { activity -> items = activity.galleryForTesting.grid.value.items }
                items.isNotEmpty() && compose.onAllNodes(hasTestTag("maple.media.item.${items.first().id}")).fetchSemanticsNodes().isNotEmpty()
            }
            block(scenario, items)
        }
    }

    private companion object {
        const val TAG = "MapleSelection"
        const val COUNT = 6
        const val LOAD_COUNT = 1000
        const val TIMEOUT_MILLIS = 30_000L
        const val SELECT_ALL_BUDGET_MILLIS = 2_000L
    }
}
