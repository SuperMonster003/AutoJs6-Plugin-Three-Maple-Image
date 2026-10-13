package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/**
 * ROADMAP 7.1 `MediaWriteCoordinator` on images this app inserted itself, so Android 11+ writes them directly
 * (no system confirmation) and Android 7-10 goes through the file routes and the private recycle bin.
 */
@RunWith(AndroidJUnit4::class)
class MediaWriteCoordinatorInstrumentationTest {

    @get:Rule
    val permissions: GrantPermissionRule = GrantPermissionRule.grant(*MediaStoreTestImages.permissions())

    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private val folder = "MapleWrite${System.currentTimeMillis()}"
    private val target = "${folder}Target"
    private lateinit var scenario: ActivityScenario<ComponentActivity>
    private lateinit var coordinator: MediaWriteCoordinator

    @Before
    fun setUp() {
        scenario = ActivityScenario.launch(ComponentActivity::class.java)
        scenario.onActivity { coordinator = MediaWriteCoordinator(it) }
    }

    @After
    fun tearDown() {
        scenario.close()
        cleanUp()
    }

    @Test
    fun renameMoveAndTheirUndoKeepTheRows() = runBlocking {
        insert("a.png", Color.RED)
        insert("b.png", Color.GREEN)
        val (a, b) = items(folder).sortedBy { it.displayName }

        val renamed = main { coordinator.rename(a, "c.png") }
        assertEquals(1, renamed.succeeded)
        assertEquals(listOf("b.png", "c.png"), names(folder))
        assertTrue("a taken name is refused", main { coordinator.rename(b, "C.PNG") }.nameTaken)
        main { requireNotNull(renamed.undo).invoke() }
        assertEquals(listOf("a.png", "b.png"), names(folder))

        val moved = main { coordinator.move(listOf(a, b), relativePath(target), NameConflictChoice.AUTO_RENAME) }
        assertEquals(2, moved.succeeded)
        assertEquals(emptyList<String>(), names(folder))
        assertEquals(listOf("a.png", "b.png"), names(target))
        main { requireNotNull(moved.undo).invoke() }
        assertEquals(listOf("a.png", "b.png"), names(folder))
    }

    @Test
    fun copiesNeverOverwriteAndSkipOnRequest() = runBlocking {
        insert("a.png", Color.RED)
        val a = items(folder).single()
        assertEquals(1, main { coordinator.copy(listOf(a), relativePath(folder), NameConflictChoice.AUTO_RENAME) }.succeeded)
        assertEquals(listOf("a (1).png", "a.png"), names(folder))
        assertEquals(0, main { coordinator.copy(listOf(a), relativePath(folder), NameConflictChoice.SKIP) }.succeeded)
        val copied = main { coordinator.copy(listOf(a), relativePath(target), NameConflictChoice.AUTO_RENAME) }
        assertEquals(1, copied.succeeded)
        assertEquals(null, copied.undo)
        assertEquals(listOf("a.png"), names(target))
        assertEquals(items(folder).first { it.displayName == "a.png" }.sizeBytes, items(target).single().sizeBytes)
    }

    @Test
    fun conflictsAreCountedAndReplacedTargetsGoToTheRecycleBin() = runBlocking {
        insert("a.png", Color.RED)
        insert("b.png", Color.GREEN)
        MediaStoreTestImages.insert(context, target, "A.PNG", Color.BLUE)
        val (a, b) = items(folder).sortedBy { it.displayName }
        val old = items(target).single()
        assertEquals(1, main { coordinator.conflicts(listOf(a, b), relativePath(target)) })

        val moved = main { coordinator.move(listOf(a, b), relativePath(target), NameConflictChoice.REPLACE) }
        assertEquals(2, moved.succeeded)
        assertEquals(listOf("a.png", "b.png"), names(target).map { it.lowercase() }.sorted())
        assertTrue("the moved image replaced the old one", items(target).none { it.id == old.id })
        assertTrue("the replaced image waits in the recycle bin", RecycleBinRepository(context).load().any { it.displayName.equals("A.PNG", ignoreCase = true) })
    }

    @Test
    fun aCancelledCopyKeepsTheFinishedImages() = runBlocking {
        insert("a.png", Color.RED)
        insert("b.png", Color.GREEN)
        insert("c.png", Color.BLUE)
        lateinit var control: WriteControl
        control = WriteControl { done, _ -> if (done == 1) control.cancel() }
        val copied = main { coordinator.copy(items(folder), relativePath(target), NameConflictChoice.AUTO_RENAME, control) }
        assertEquals(1, copied.succeeded)
        assertEquals(1, names(target).size)
    }

    @Test
    fun trashIsUndoableAndDeleteIsFinal() = runBlocking {
        insert("a.png", Color.RED)
        insert("b.png", Color.BLUE)
        val (a, b) = items(folder).sortedBy { it.displayName }

        val trashed = main { coordinator.trash(listOf(a)) }
        assertEquals(1, trashed.succeeded)
        assertEquals(listOf("b.png"), names(folder))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            assertTrue("kept in the system trash", isTrashed(a.id))
        } else {
            val entry = coordinator.privateRecycleBin.entries().single { it.displayName == "a.png" }
            assertTrue(coordinator.privateRecycleBin.storedFile(entry).isFile)
        }
        val listed = RecycleBinRepository(context).load().filter { it.displayName == "a.png" }
        assertEquals("the recycle bin view lists it with the original name", 1, listed.size)
        assertTrue(listed.single().remainingDays in 1..RecycleBinPolicy.RETENTION_DAYS)
        main { requireNotNull(trashed.undo).invoke() }
        assertEquals(listOf("a.png", "b.png"), names(folder))
        assertTrue(RecycleBinRepository(context).load().none { it.displayName == "a.png" && it.id == listed.single().id })

        val deleted = main { coordinator.delete(listOf(b)) }
        assertEquals(1, deleted.succeeded)
        assertEquals(null, deleted.undo)
        assertEquals(listOf("a.png"), names(folder))
        assertFalse(isTrashed(b.id))
    }

    @Test
    fun albumTargetsOnAndroid10AndLaterStayInPicturesOrDcim() {
        assertTrue(MediaWriteCoordinator.isWritableAlbumPath("Pictures/Trips/", sdk = 29))
        assertTrue(MediaWriteCoordinator.isWritableAlbumPath("DCIM/Camera/", sdk = 33))
        assertFalse(MediaWriteCoordinator.isWritableAlbumPath("Download/", sdk = 30))
        assertTrue(MediaWriteCoordinator.isWritableAlbumPath("Download/", sdk = 28))
    }

    private suspend fun <T> main(block: suspend () -> T): T = withTimeout(TIMEOUT_MILLIS) { withContext(Dispatchers.Main) { block() } }

    private fun insert(name: String, color: Int) {
        MediaStoreTestImages.insert(context, folder, name, color)
    }

    private fun relativePath(name: String) = "${Environment.DIRECTORY_PICTURES}/$name/"

    private suspend fun items(name: String): List<MediaItem> {
        val path = relativePath(name)
        return MediaStoreRepository(context).loadAll().filter { it.relativePath == path }
    }

    private suspend fun names(name: String): List<String> = items(name).map { it.displayName }.sorted()

    /** Whether the row still exists in the system trash (Android 11+); false once it is gone for good. */
    private fun isTrashed(id: Long): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return false
        val args = Bundle().apply {
            putString(android.content.ContentResolver.QUERY_ARG_SQL_SELECTION, "${MediaStore.MediaColumns._ID} = ?")
            putStringArray(android.content.ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS, arrayOf(id.toString()))
            putInt(MediaStore.QUERY_ARG_MATCH_TRASHED, MediaStore.MATCH_INCLUDE)
        }
        val collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        return context.contentResolver.query(collection, arrayOf(MediaStore.MediaColumns.IS_TRASHED), args, null)?.use { cursor ->
            cursor.moveToFirst() && cursor.getInt(0) == 1
        } ?: false
    }

    private fun cleanUp() {
        val resolver = context.contentResolver
        for (name in listOf(folder, target)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val args = Bundle().apply {
                    putString(android.content.ContentResolver.QUERY_ARG_SQL_SELECTION, "${MediaStore.MediaColumns.RELATIVE_PATH} = ?")
                    putStringArray(android.content.ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS, arrayOf(relativePath(name)))
                    putInt(MediaStore.QUERY_ARG_MATCH_TRASHED, MediaStore.MATCH_INCLUDE)
                }
                val collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
                runCatching { resolver.delete(collection, args) }
            } else {
                @Suppress("DEPRECATION")
                runCatching { resolver.delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "${MediaStore.MediaColumns.DATA} LIKE ?", arrayOf("%/${relativePath(name)}%")) }
            }
            @Suppress("DEPRECATION")
            File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), name).deleteRecursively()
        }
        runBlocking {
            val bin = PrivateRecycleBin(context)
            bin.deletePermanently(bin.entries().filter { it.originalPath.contains(folder) }.map { it.id })
        }
    }

    private companion object {
        const val TIMEOUT_MILLIS = 30_000L
    }
}
