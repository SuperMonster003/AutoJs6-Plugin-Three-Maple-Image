package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.content.ContentUris
import android.graphics.Color
import android.net.Uri
import android.os.SystemClock
import android.util.Log
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

/** ROADMAP 7.5: the home search finds albums and file names; the album search narrows the grid. */
@RunWith(AndroidJUnit4::class)
class SearchInstrumentationTest {

    @get:Rule
    val compose = createEmptyComposeRule()

    @get:Rule
    val permission: GrantPermissionRule = GrantPermissionRule.grant(*MediaStoreTestImages.permissions())

    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private val record by lazy { MediaPermissionRecordStore(context) }
    private var originalRequested = false
    private val suffix = UUID.randomUUID().toString().take(8)
    private val folder = "MapleSearch$suffix"
    private val inserted = ArrayList<Uri>()

    @Before
    fun setUp() {
        originalRequested = record.requestedBefore
        record.markRequested()
        inserted += MediaStoreTestImages.insert(context, folder, "sunset-beach-$suffix.png", Color.RED)
        inserted += MediaStoreTestImages.insert(context, folder, "sunset-city-$suffix.png", Color.GREEN)
        inserted += MediaStoreTestImages.insert(context, folder, "mountain-$suffix.png", Color.BLUE)
    }

    @After
    fun tearDown() {
        MediaStoreTestImages.cleanUp(context, inserted, folder)
        if (!originalRequested) record.reset()
    }

    @Test
    fun homeAndAlbumSearch() {
        val ids = inserted.map { ContentUris.parseId(it) }
        val bucketId = runBlocking { MediaStoreRepository(context).also { it.invalidate() }.loadAll() }.first { it.id == ids[0] }.bucketId
        ActivityScenario.launch(HomeActivity::class.java).use { scenario ->
            compose.waitUntil(TIMEOUT_MILLIS) { compose.onAllNodes(hasTestTag("maple.gallery.search")).fetchSemanticsNodes().isNotEmpty() }
            compose.onNodeWithTag("maple.gallery.search").performClick()

            val started = SystemClock.elapsedRealtime()
            compose.onNodeWithTag("maple.search.input").performTextInput("sunset $suffix")
            compose.waitUntil(TIMEOUT_MILLIS) { search(scenario).items.map { it.id }.toSet() == setOf(ids[0], ids[1]) }
            compose.waitUntil(TIMEOUT_MILLIS) { compose.onAllNodes(hasTestTag("maple.search.item.${ids[0]}")).fetchSemanticsNodes().isNotEmpty() }
            Log.i(TAG, "first results on screen after ${SystemClock.elapsedRealtime() - started} ms (250 ms of it is the typing pause)")

            compose.onNodeWithTag("maple.search.input").performTextClearance()
            compose.onNodeWithTag("maple.search.input").performTextInput(folder)
            compose.waitUntil(TIMEOUT_MILLIS) { search(scenario).albums.any { it.id == bucketId } }
            assertEquals("the album matches by name, not its images", emptyList<MediaItem>(), search(scenario).items)
            compose.onNodeWithTag("maple.search.results").performScrollToNode(hasTestTag("maple.search.album.$bucketId"))
            compose.onNodeWithTag("maple.search.album.$bucketId").performClick()
            compose.waitUntil(TIMEOUT_MILLIS) { compose.onAllNodes(hasTestTag("maple.media.search")).fetchSemanticsNodes().isNotEmpty() }

            compose.onNodeWithTag("maple.media.search").performClick()
            compose.onNodeWithTag("maple.search.input").performTextInput("beach")
            compose.waitUntil(TIMEOUT_MILLIS) { grid(scenario).let { it.query == "beach" && !it.loading && it.items.map { item -> item.id } == listOf(ids[0]) } }
            compose.onNodeWithTag("maple.search.clear").performClick()
            compose.waitUntil(TIMEOUT_MILLIS) { grid(scenario).let { it.query.isEmpty() && !it.loading && it.items.size == 3 } }
            assertTrue(search(scenario).active)
        }
    }

    private fun search(scenario: ActivityScenario<HomeActivity>): SearchUiState {
        var state = SearchUiState()
        scenario.onActivity { state = it.galleryForTesting.search.value }
        return state
    }

    private fun grid(scenario: ActivityScenario<HomeActivity>): MediaGridUiState {
        var state = MediaGridUiState()
        scenario.onActivity { state = it.galleryForTesting.grid.value }
        return state
    }

    private companion object {
        const val TAG = "MapleSearch"
        const val TIMEOUT_MILLIS = 30_000L
    }
}
