package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.Manifest
import android.content.ContentUris
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTouchInput
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

/**
 * ROADMAP 9.2: the grid settings change the album and media grids as soon as the home screen is back, an album can
 * have a cover of its own, and landscape keeps its own media columns.
 */
@RunWith(AndroidJUnit4::class)
class GridSettingsInstrumentationTest {

    @get:Rule(order = 0)
    val permissions: GrantPermissionRule = GrantPermissionRule.grant(
        *(MediaStoreTestImages.permissions().toSet() + if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) setOf(Manifest.permission.WRITE_EXTERNAL_STORAGE) else emptySet()).toTypedArray(),
    )

    @get:Rule(order = 1)
    val compose = createEmptyComposeRule()

    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private val preferences get() = GalleryPreferences(context)
    private val folder = "MapleGrid-" + UUID.randomUUID().toString().take(8)
    private val inserted = mutableListOf<Uri>()
    private lateinit var originalGrid: GridSettings
    private var originalColumns = GalleryPreferences.DEFAULT_COLUMNS
    private val record get() = MediaPermissionRecordStore(context)
    private var originalRequested = false

    @Before fun prepare() {
        TestScreen.awake()
        originalGrid = preferences.grid
        originalColumns = preferences.mediaColumns
        originalRequested = record.requestedBefore
        record.markRequested()
        inserted += MediaStoreTestImages.insert(context, folder, "a.png", Color.RED)
        inserted += MediaStoreTestImages.insert(context, folder, "b.png", Color.GREEN)
        inserted += MediaStoreTestImages.insert(context, folder, "c.webp", Color.BLUE, 64, 64, @Suppress("DEPRECATION") Bitmap.CompressFormat.WEBP)
    }

    @After fun restore() {
        preferences.setCustomCover(path(), null)
        preferences.grid = originalGrid
        preferences.mediaColumns = originalColumns
        if (!originalRequested) record.reset()
        MediaStoreTestImages.cleanUp(context, inserted, folder)
    }

    private fun path() = AlbumPath(MediaCatalog.PRIMARY_VOLUME, "${Environment.DIRECTORY_PICTURES}/$folder/")

    private fun ids() = inserted.map { ContentUris.parseId(it) }

    private fun bucketId(): Long = runBlocking { MediaStoreRepository(context).also { it.invalidate() }.loadAll() }.first { it.id in ids() }.bucketId

    private fun exists(tag: String, unmerged: Boolean = false): Boolean =
        runCatching { compose.onAllNodes(hasTestTag(tag), useUnmergedTree = unmerged).fetchSemanticsNodes().isNotEmpty() }.getOrDefault(false)

    private fun ActivityScenario<HomeActivity>.album(bucket: Long): AlbumSummary? {
        var album: AlbumSummary? = null
        onActivity { activity -> album = activity.galleryForTesting.albums.value.albums.firstOrNull { it.kind == AlbumKind.MEDIA && it.id == bucket } }
        return album
    }

    /** The cover's media id (the album's content URI names the volume differently from the inserted one). */
    private fun ActivityScenario<HomeActivity>.coverId(bucket: Long): Long? =
        album(bucket)?.coverUri?.let { runCatching { ContentUris.parseId(Uri.parse(it)) }.getOrNull() }

    private fun ActivityScenario<HomeActivity>.openAlbum(bucket: Long) {
        val tag = "maple.gallery.album.media.$bucket"
        compose.waitUntil(TIMEOUT) { exists("maple.gallery.grid") && album(bucket) != null }
        compose.onNodeWithTag("maple.gallery.grid").performScrollToNode(hasTestTag(tag))
        compose.onNodeWithTag(tag).performClick()
        compose.waitUntil(TIMEOUT) {
            var count = 0
            onActivity { count = it.galleryForTesting.grid.value.items.size }
            count == 3 && exists("maple.media.grid")
        }
    }

    private fun ActivityScenario<HomeActivity>.comeBack() {
        moveToState(Lifecycle.State.STARTED)
        moveToState(Lifecycle.State.RESUMED)
    }

    @Test fun theGridsFollowTheSettingsWhenTheHomeScreenIsBack() {
        val bucket = bucketId()
        preferences.grid = GridSettings(
            albumColumnsPortrait = 3, albumColumnsLandscape = 3, spacing = ThumbnailSpacing.WIDE, corners = ThumbnailCorners.LARGE,
            cropSquare = false, typeBadges = false, albumCounts = false, singleLineAlbumNames = false, albumCover = AlbumCoverOrder.OLDEST,
        )
        ActivityScenario.launch(HomeActivity::class.java).use { scenario ->
            compose.waitUntil(TIMEOUT) { exists("maple.gallery.grid") && scenario.album(bucket) != null }
            assertEquals("the oldest image covers the album", ContentUris.parseId(inserted[0]), scenario.coverId(bucket))
            assertFalse("no counts", exists("maple.album.count", unmerged = true))
            val tag = "maple.gallery.album.media.$bucket"
            compose.onNodeWithTag("maple.gallery.grid").performScrollToNode(hasTestTag(tag))
            val grid = compose.onNodeWithTag("maple.gallery.grid").fetchSemanticsNode().boundsInRoot
            val cell = compose.onNodeWithTag(tag).fetchSemanticsNode().boundsInRoot
            assertTrue("three album columns: cell ${cell.width}, grid ${grid.width}", cell.width * 3 <= grid.width)

            scenario.openAlbum(bucket)
            assertFalse("no WEBP badge", runCatching { compose.onAllNodes(hasText("WEBP")).fetchSemanticsNodes().isNotEmpty() }.getOrDefault(true))
            val items = ids().sorted()
            val first = compose.onNodeWithTag("maple.media.item.${items[0]}").fetchSemanticsNode().boundsInRoot
            val second = compose.onNodeWithTag("maple.media.item.${items[1]}").fetchSemanticsNode().boundsInRoot
            val density = context.resources.displayMetrics.density
            val gap = kotlin.math.abs(second.left - first.right).coerceAtMost(kotlin.math.abs(first.left - second.right))
            assertEquals("wide spacing", ThumbnailSpacing.WIDE.dp * density, gap, density)

            // Back to the defaults in the settings: the grid shows the badge again, the albums their counts and newest cover.
            preferences.grid = GridSettings()
            scenario.comeBack()
            compose.waitUntil(TIMEOUT) { runCatching { compose.onAllNodes(hasText("WEBP")).fetchSemanticsNodes().isNotEmpty() }.getOrDefault(false) }
            compose.waitUntil(TIMEOUT) { scenario.coverId(bucket) == ContentUris.parseId(inserted[2]) }
        }
    }

    @Test fun anAlbumCanHaveACoverOfItsOwn() {
        val bucket = bucketId()
        preferences.grid = GridSettings()
        ActivityScenario.launch(HomeActivity::class.java).use { scenario ->
            compose.waitUntil(TIMEOUT) { scenario.album(bucket) != null }
            assertEquals("the newest image covers the album", ContentUris.parseId(inserted[2]), scenario.coverId(bucket))
            scenario.openAlbum(bucket)
            val chosen = ContentUris.parseId(inserted[1])
            compose.onNodeWithTag("maple.media.item.$chosen").performTouchInput { longClick() }
            compose.waitUntil(TIMEOUT) { exists("maple.selection.barMore") }
            compose.onNodeWithTag("maple.selection.barMore").performClick()
            compose.onNodeWithTag("maple.selection.setCover").performClick()
            compose.waitUntil(TIMEOUT) { scenario.coverId(bucket) == ContentUris.parseId(inserted[1]) }
            assertEquals(chosen, preferences.customCover(path()))

            compose.onNodeWithTag("maple.media.more").performClick()
            compose.onNodeWithTag("maple.media.resetCover").performClick()
            compose.waitUntil(TIMEOUT) { scenario.coverId(bucket) == ContentUris.parseId(inserted[2]) }
            assertNull(preferences.customCover(path()))
            compose.onNodeWithTag("maple.media.more").performClick()
            assertFalse("nothing to reset any more", exists("maple.media.resetCover"))
        }
    }

    @Test fun landscapeKeepsItsOwnMediaColumns() {
        preferences.mediaColumns = 4
        preferences.grid = GridSettings()
        ActivityScenario.launch(HomeActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val gallery = activity.galleryForTesting
                gallery.setLandscape(true)
                assertEquals("the same as portrait until chosen", 4, gallery.grid.value.columns)
                gallery.setColumns(6)
                assertEquals(6, gallery.grid.value.columns)
                gallery.setLandscape(false)
                assertEquals(4, gallery.grid.value.columns)
            }
        }
        assertEquals(4, preferences.mediaColumns)
        assertEquals(6, preferences.grid.mediaColumnsLandscape)
    }

    private companion object {
        const val TIMEOUT = 30_000L
    }
}
