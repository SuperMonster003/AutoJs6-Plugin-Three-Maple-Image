package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.content.ContentUris
import android.graphics.Color
import android.net.Uri
import android.os.Environment
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

/**
 * ROADMAP 7.4: an excluded folder leaves the album grid, "all images" and the favorites, a pinned one leads the
 * album grid, and both follow the marks as they change.
 */
@RunWith(AndroidJUnit4::class)
class FolderMarksInstrumentationTest {

    @get:Rule
    val compose = createEmptyComposeRule()

    @get:Rule
    val permission: GrantPermissionRule = GrantPermissionRule.grant(*MediaStoreTestImages.permissions())

    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private val record by lazy { MediaPermissionRecordStore(context) }
    private val marks by lazy { FolderMarksRepository(context) }
    private var originalRequested = false
    private val suffix = UUID.randomUUID().toString().take(8)
    private val excludedFolder = "MapleExcluded-$suffix"
    private val pinnedFolder = "MaplePinned-$suffix"
    private val excluded = ArrayList<Uri>()
    private val pinned = ArrayList<Uri>()

    private fun path(folder: String) = AlbumPath(MediaCatalog.PRIMARY_VOLUME, "${Environment.DIRECTORY_PICTURES}/$folder/")

    @Before
    fun setUp() {
        originalRequested = record.requestedBefore
        record.markRequested()
        excluded += MediaStoreTestImages.insert(context, excludedFolder, "e.png", Color.RED)
        pinned += MediaStoreTestImages.insert(context, pinnedFolder, "p.png", Color.BLUE)
    }

    @After
    fun tearDown() {
        runBlocking {
            marks.unmark(listOf(path(excludedFolder)), FolderMark.EXCLUDED)
            marks.unmark(listOf(path(pinnedFolder)), FolderMark.PINNED)
        }
        MediaStoreTestImages.cleanUp(context, excluded, excludedFolder)
        MediaStoreTestImages.cleanUp(context, pinned, pinnedFolder)
        if (!originalRequested) record.reset()
    }

    @Test
    fun marksShapeTheGallery() {
        val excludedId = ContentUris.parseId(excluded.single())
        val pinnedId = ContentUris.parseId(pinned.single())
        runBlocking {
            marks.mark(listOf(path(excludedFolder)), FolderMark.EXCLUDED)
            marks.mark(listOf(path(pinnedFolder)), FolderMark.PINNED)
            FavoritesRepository(context).add(MediaStoreRepository(context).also { it.invalidate() }.loadAll().filter { it.id == excludedId })
        }
        ActivityScenario.launch(HomeActivity::class.java).use { scenario ->
            compose.waitUntil(TIMEOUT_MILLIS) { compose.onAllNodes(hasTestTag("maple.gallery.grid")).fetchSemanticsNodes().isNotEmpty() }
            compose.waitUntil(TIMEOUT_MILLIS) { albums(scenario).any { it.path == path(pinnedFolder) } }
            val first = albums(scenario).first()
            assertEquals("the pinned album leads", path(pinnedFolder), first.path)
            assertTrue(first.pinned)
            assertFalse("the excluded album is gone", albums(scenario).any { it.path == path(excludedFolder) })
            assertFalse("an excluded favorite stays out of the favorites", excludedId in favorites(scenario))

            val all = allImages(scenario)
            assertTrue(pinnedId in all)
            assertFalse(excludedId in all)

            runBlocking { marks.unmark(listOf(path(excludedFolder)), FolderMark.EXCLUDED) }
            compose.waitUntil(TIMEOUT_MILLIS) { albums(scenario).any { it.path == path(excludedFolder) } }
            assertTrue(excludedId in allImages(scenario))
            runBlocking { FavoritesRepository(context).remove(listOf(excludedId)) }
        }
    }

    private fun albums(scenario: ActivityScenario<HomeActivity>): List<AlbumSummary> {
        var albums: List<AlbumSummary> = emptyList()
        scenario.onActivity { albums = it.galleryForTesting.albums.value.albums }
        return albums
    }

    /** Ids in the favorites album (empty when the album is not shown). */
    private fun favorites(scenario: ActivityScenario<HomeActivity>): Set<Long> {
        var album: AlbumSummary? = null
        scenario.onActivity { album = it.galleryForTesting.albums.value.favorites }
        return album?.let { ids(scenario, it) }.orEmpty()
    }

    private fun allImages(scenario: ActivityScenario<HomeActivity>): Set<Long> {
        var album: AlbumSummary? = null
        scenario.onActivity { album = it.galleryForTesting.allImagesAlbum() }
        return ids(scenario, requireNotNull(album))
    }

    /** Ids of an album, read the way select all reads them (every page). */
    private fun ids(scenario: ActivityScenario<HomeActivity>, album: AlbumSummary): Set<Long> {
        scenario.onActivity { it.galleryForTesting.openAlbum(album) }
        compose.waitUntil(TIMEOUT_MILLIS) {
            var loaded = false
            scenario.onActivity { loaded = !it.galleryForTesting.grid.value.loading }
            loaded
        }
        scenario.onActivity { it.galleryForTesting.selectAll() }
        var selected: Set<Long> = emptySet()
        compose.waitUntil(TIMEOUT_MILLIS) {
            scenario.onActivity { selected = it.galleryForTesting.grid.value.selection }
            selected.isNotEmpty()
        }
        scenario.onActivity { it.galleryForTesting.clearSelection() }
        return selected
    }

    private companion object {
        const val TIMEOUT_MILLIS = 30_000L
    }
}
