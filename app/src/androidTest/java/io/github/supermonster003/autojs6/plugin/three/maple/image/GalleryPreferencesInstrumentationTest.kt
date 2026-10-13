package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.content.ContentUris
import android.graphics.Color
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
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

/** ROADMAP 6.2: column count and sort are remembered, and "show all folder contents" opens the flat view. */
@RunWith(AndroidJUnit4::class)
class GalleryPreferencesInstrumentationTest {

    @get:Rule
    val compose = createEmptyComposeRule()

    @get:Rule
    val permission: GrantPermissionRule = GrantPermissionRule.grant(*MediaStoreTestImages.permissions())

    private val instrumentation get() = InstrumentationRegistry.getInstrumentation()
    private val context get() = instrumentation.targetContext
    private val record by lazy { MediaPermissionRecordStore(context) }
    private val preferences by lazy { GalleryPreferences(context) }
    private var originalRequested = false
    private var originalColumns = GalleryPreferences.DEFAULT_COLUMNS
    private var originalDefaultSort = MediaSort.DEFAULT

    @Before
    fun rememberState() {
        originalRequested = record.requestedBefore
        record.markRequested()
        originalColumns = preferences.mediaColumns
        originalDefaultSort = preferences.defaultSort
    }

    @After
    fun restoreState() {
        if (!originalRequested) record.reset()
        preferences.mediaColumns = originalColumns
        preferences.defaultSort = originalDefaultSort
    }

    @Test
    fun columnsAndSortAreRememberedAndTheFlatViewUsesTheDefaults() {
        val folder = "MaplePrefsTest-" + UUID.randomUUID().toString().take(8)
        val inserted = listOf(Color.RED, Color.GREEN, Color.BLUE).mapIndexed { index, color ->
            MediaStoreTestImages.insert(context, folder, "prefs-${index + 1}.png", color)
        }
        try {
            val ids = inserted.map { ContentUris.parseId(it) }.toSet()
            val bucketId = runBlocking { MediaStoreRepository(context).loadAll() }.first { it.id in ids }.bucketId
            preferences.mediaColumns = GalleryPreferences.DEFAULT_COLUMNS
            ActivityScenario.launch(HomeActivity::class.java).use { scenario ->
                val albumTag = "maple.gallery.album.media.$bucketId"
                compose.waitUntil(TIMEOUT_MILLIS) {
                    compose.onAllNodes(hasTestTag("maple.gallery.grid")).fetchSemanticsNodes().isNotEmpty()
                }
                compose.onNodeWithTag("maple.gallery.grid").performScrollToNode(hasTestTag(albumTag))
                compose.onNodeWithTag(albumTag).performClick()
                compose.waitUntil(TIMEOUT_MILLIS) {
                    compose.onAllNodes(hasTestTag("maple.media.grid")).fetchSemanticsNodes().isNotEmpty()
                }
                scenario.onActivity { activity -> assertEquals(GalleryPreferences.DEFAULT_COLUMNS, activity.galleryForTesting.grid.value.columns) }

                // Columns from the menu are applied at once and persisted.
                compose.onNodeWithTag("maple.media.more").performClick()
                compose.onNodeWithTag("maple.media.columns.3").performClick()
                compose.waitUntil(TIMEOUT_MILLIS) {
                    var columns = 0
                    scenario.onActivity { activity -> columns = activity.galleryForTesting.grid.value.columns }
                    columns == 3
                }
                assertEquals(3, preferences.mediaColumns)

                // Name ascending is remembered for this album and becomes the default.
                compose.onNodeWithTag("maple.media.sort").performClick()
                compose.onNodeWithTag("maple.sort.name").performClick()
                compose.onNodeWithTag("maple.media.sort").performClick()
                compose.onNodeWithTag("maple.sort.direction").performClick()
                val byName = MediaSort(MediaSortField.NAME, ascending = true)
                compose.waitUntil(TIMEOUT_MILLIS) {
                    var sort = MediaSort.DEFAULT
                    scenario.onActivity { activity -> sort = activity.galleryForTesting.grid.value.sort }
                    sort == byName
                }
                assertEquals(byName, preferences.sortFor(AlbumKind.MEDIA, bucketId))
                assertEquals(byName, preferences.defaultSort)

                // Recreation (rotation) keeps the media grid route, its sort and its column count.
                scenario.recreate()
                compose.waitUntil(TIMEOUT_MILLIS) {
                    compose.onAllNodes(hasTestTag("maple.media.grid")).fetchSemanticsNodes().isNotEmpty()
                }
                scenario.onActivity { activity ->
                    val grid = activity.galleryForTesting.grid.value
                    assertEquals(bucketId, grid.album?.id)
                    assertEquals(byName, grid.sort)
                    assertEquals(3, grid.columns)
                }

                // "Show all folder contents" opens the flat view with the default sort and the same columns.
                scenario.onActivity { activity -> activity.onBackPressedDispatcher.onBackPressed() }
                compose.waitUntil(TIMEOUT_MILLIS) {
                    compose.onAllNodes(hasTestTag("maple.gallery.more")).fetchSemanticsNodes().isNotEmpty()
                }
                compose.onNodeWithTag("maple.gallery.more").performClick()
                compose.onNodeWithTag("maple.gallery.menu.showAll").performClick()
                compose.waitUntil(TIMEOUT_MILLIS) {
                    var ready = false
                    scenario.onActivity { activity ->
                        val grid = activity.galleryForTesting.grid.value
                        ready = grid.album?.kind == AlbumKind.ALL && !grid.loading && grid.items.size >= 3
                    }
                    ready
                }
                scenario.onActivity { activity ->
                    val grid = activity.galleryForTesting.grid.value
                    assertEquals(byName, grid.sort)
                    assertEquals(3, grid.columns)
                    // The flat view pages 120 items at a time; the album count covers the whole library.
                    assertTrue(requireNotNull(grid.album).count >= ids.size)
                    assertTrue(grid.items.size >= ids.size)
                }
            }
        } finally {
            MediaStoreTestImages.cleanUp(context, inserted, folder)
        }
    }

    private companion object {
        const val TIMEOUT_MILLIS = 20_000L
    }
}
