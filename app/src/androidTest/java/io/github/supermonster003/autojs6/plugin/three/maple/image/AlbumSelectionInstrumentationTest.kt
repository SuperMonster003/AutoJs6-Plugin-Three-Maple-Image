package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.content.ContentUris
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTouchInput
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

/** ROADMAP 7.4: long press selects albums; pin / unpin, exclude with undo and delete act on the selection. */
@RunWith(AndroidJUnit4::class)
class AlbumSelectionInstrumentationTest {

    @get:Rule
    val compose = createEmptyComposeRule()

    @get:Rule
    val permission: GrantPermissionRule = GrantPermissionRule.grant(*MediaStoreTestImages.permissions())

    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private val record by lazy { MediaPermissionRecordStore(context) }
    private val marks by lazy { FolderMarksRepository(context) }
    private var originalRequested = false
    private val suffix = UUID.randomUUID().toString().take(8)
    private val folderA = "MapleAlbumA-$suffix"
    private val folderB = "MapleAlbumB-$suffix"
    private val inserted = HashMap<String, MutableList<Uri>>()

    private fun path(folder: String) = AlbumPath(MediaCatalog.PRIMARY_VOLUME, "${Environment.DIRECTORY_PICTURES}/$folder/")

    @Before
    fun setUp() {
        originalRequested = record.requestedBefore
        record.markRequested()
        for (folder in listOf(folderA, folderB)) {
            inserted[folder] = mutableListOf(
                MediaStoreTestImages.insert(context, folder, "1.png", Color.RED),
                MediaStoreTestImages.insert(context, folder, "2.png", Color.GREEN),
            )
        }
    }

    @After
    fun tearDown() {
        runBlocking {
            for (folder in listOf(folderA, folderB)) {
                marks.unmark(listOf(path(folder)), FolderMark.PINNED)
                marks.unmark(listOf(path(folder)), FolderMark.EXCLUDED)
            }
            val bin = PrivateRecycleBin(context)
            bin.deletePermanently(bin.entries().filter { it.originalPath.contains(suffix) }.map { it.id })
        }
        for ((folder, uris) in inserted) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                // Trashed rows are hidden from a plain delete.
                val args = Bundle().apply {
                    putString(android.content.ContentResolver.QUERY_ARG_SQL_SELECTION, "${MediaStore.MediaColumns.RELATIVE_PATH} = ?")
                    putStringArray(android.content.ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS, arrayOf(path(folder).relativePath))
                    putInt(MediaStore.QUERY_ARG_MATCH_TRASHED, MediaStore.MATCH_INCLUDE)
                }
                runCatching { context.contentResolver.delete(MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL), args) }
            }
            MediaStoreTestImages.cleanUp(context, uris, folder)
        }
        if (!originalRequested) record.reset()
    }

    @Test
    fun albumSelectionPinsExcludesAndDeletes() {
        val bucketA = bucketOf(folderA)
        val bucketB = bucketOf(folderB)
        ActivityScenario.launch(HomeActivity::class.java).use { scenario ->
            compose.waitUntil(TIMEOUT_MILLIS) { compose.onAllNodes(hasTestTag("maple.gallery.grid")).fetchSemanticsNodes().isNotEmpty() }
            compose.waitUntil(TIMEOUT_MILLIS) { albums(scenario).any { it.id == bucketA } && albums(scenario).any { it.id == bucketB } }

            tile(bucketA).performTouchInput { longClick() }
            tile(bucketB).performClick()
            compose.waitForIdle()
            assertEquals(setOf(bucketA, bucketB), selection(scenario))
            scenario.recreate()
            compose.waitUntil(TIMEOUT_MILLIS) { compose.onAllNodes(hasTestTag("maple.albums.pin")).fetchSemanticsNodes().isNotEmpty() }
            assertEquals("the selection survives a recreation", setOf(bucketA, bucketB), selection(scenario))

            compose.onNodeWithTag("maple.albums.pin").performClick()
            compose.waitUntil(TIMEOUT_MILLIS) { albums(scenario).filter { it.id == bucketA || it.id == bucketB }.all { it.pinned } }
            assertEquals(emptySet<Long>(), selection(scenario))
            assertEquals(setOf(bucketA, bucketB), albums(scenario).take(2).map { it.id }.toSet())

            tile(bucketA).performTouchInput { longClick() }
            compose.onNodeWithTag("maple.albums.pin").assert(hasText(context.getString(R.string.action_unpin)))
            compose.onNodeWithTag("maple.albums.pin").performClick()
            compose.waitUntil(TIMEOUT_MILLIS) { albums(scenario).firstOrNull { it.id == bucketA }?.pinned == false }

            tile(bucketA).performTouchInput { longClick() }
            compose.onNodeWithTag("maple.albums.exclude").performClick()
            compose.waitUntil(TIMEOUT_MILLIS) { albums(scenario).none { it.id == bucketA } }
            compose.onNodeWithText(context.getString(R.string.action_undo)).performClick()
            compose.waitUntil(TIMEOUT_MILLIS) { albums(scenario).any { it.id == bucketA } }

            tile(bucketB).performTouchInput { longClick() }
            compose.onNodeWithTag("maple.albums.delete").performClick()
            compose.onNodeWithTag("maple.dialog.confirm").performClick()
            compose.waitUntil(TIMEOUT_MILLIS) { albums(scenario).none { it.id == bucketB } }
            assertTrue(albums(scenario).any { it.id == bucketA })
            assertFalse(runBlocking { MediaStoreRepository(context).also { it.invalidate() }.loadAll() }.any { it.bucketId == bucketB })
        }
    }

    private fun tile(bucketId: Long) = compose.onNodeWithTag("maple.gallery.album.media.$bucketId").also {
        compose.onNodeWithTag("maple.gallery.grid").performScrollToNode(hasTestTag("maple.gallery.album.media.$bucketId"))
    }

    private fun bucketOf(folder: String): Long {
        val ids = inserted.getValue(folder).map { ContentUris.parseId(it) }.toSet()
        return runBlocking { MediaStoreRepository(context).also { it.invalidate() }.loadAll() }.first { it.id in ids }.bucketId
    }

    private fun albums(scenario: ActivityScenario<HomeActivity>): List<AlbumSummary> {
        var albums: List<AlbumSummary> = emptyList()
        scenario.onActivity { albums = it.galleryForTesting.albums.value.albums }
        return albums
    }

    private fun selection(scenario: ActivityScenario<HomeActivity>): Set<Long> {
        var selection: Set<Long> = emptySet()
        scenario.onActivity { selection = it.galleryForTesting.albums.value.albumSelection }
        return selection
    }

    private companion object {
        const val TIMEOUT_MILLIS = 30_000L
    }
}
