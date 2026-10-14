package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.Manifest
import android.graphics.Color
import android.os.Build
import android.os.Environment
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
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/**
 * ROADMAP 7.4 album rename: a folder rename where the app may (Android 7-10 internal storage, Android 11+ with all
 * files access), otherwise the album's images move into the new folder; the undo goes back, a taken name stops it.
 * Changing all files access restarts the app, so the Android 11+ cases follow the state the run starts in (grant it
 * with `appops set --uid <package> MANAGE_EXTERNAL_STORAGE allow` before a second run).
 */
@RunWith(AndroidJUnit4::class)
class AlbumRenameInstrumentationTest {

    @get:Rule
    val permissions: GrantPermissionRule = GrantPermissionRule.grant(
        *(MediaStoreTestImages.permissions().toSet() + if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) setOf(Manifest.permission.WRITE_EXTERNAL_STORAGE) else emptySet()).toTypedArray(),
    )

    private val instrumentation get() = InstrumentationRegistry.getInstrumentation()
    private val context get() = instrumentation.targetContext
    private val folder = "MapleRename${System.currentTimeMillis()}"
    private val renamed = "${folder}New"
    private lateinit var scenario: ActivityScenario<ComponentActivity>
    private lateinit var coordinator: MediaWriteCoordinator

    private fun path(name: String) = AlbumPath(MediaCatalog.PRIMARY_VOLUME, "${Environment.DIRECTORY_PICTURES}/$name/")

    @Before
    fun setUp() {
        scenario = ActivityScenario.launch(ComponentActivity::class.java)
        scenario.onActivity { coordinator = MediaWriteCoordinator(it) }
        MediaStoreTestImages.insert(context, folder, "a.png", Color.RED)
        MediaStoreTestImages.insert(context, folder, "b.png", Color.GREEN)
    }

    @After
    fun tearDown() {
        scenario.close()
        runBlocking {
            val rows = MediaStoreRepository(context).also { it.invalidate() }.loadAll()
                .filter { it.relativePath == path(folder).relativePath || it.relativePath == path(renamed).relativePath }
            rows.forEach { runCatching { context.contentResolver.delete(android.net.Uri.parse(it.contentUri), null, null) } }
        }
        for (name in listOf(folder, renamed)) {
            @Suppress("DEPRECATION")
            File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), name).deleteRecursively()
        }
    }

    @Test
    fun withoutAFolderRenameTheImagesMove() = runBlocking {
        assumeTrue(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager())
        assertFalse(coordinator.renamesFolders(path(folder)))
        renameAndUndo()
    }

    @Test
    fun theFolderItselfIsRenamedWhenAllowed() = runBlocking {
        assumeTrue(Build.VERSION.SDK_INT < Build.VERSION_CODES.R || Environment.isExternalStorageManager())
        assertTrue(coordinator.renamesFolders(path(folder)))
        @Suppress("DEPRECATION")
        val pictures = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        File(pictures, "$folder/notes.txt").writeText("kept with the folder")
        renameAndUndo()
        assertTrue("other files travel with the folder", File(pictures, "$folder/notes.txt").isFile)
    }

    @Test
    fun aTakenNameStopsTheRename() = runBlocking {
        MediaStoreTestImages.insert(context, renamed, "c.png", Color.BLUE)
        val outcome = main { coordinator.renameAlbum(path(folder), items(folder), renamed) }
        assertTrue(outcome.nameTaken)
        assertEquals(listOf("a.png", "b.png"), names(folder))
    }

    private suspend fun renameAndUndo() {
        val outcome = main { coordinator.renameAlbum(path(folder), items(folder), renamed) }
        assertEquals(2, outcome.succeeded)
        waitFor { names(renamed) == listOf("a.png", "b.png") && names(folder).isEmpty() }
        main { requireNotNull(outcome.undo).invoke() }
        waitFor { names(folder) == listOf("a.png", "b.png") && names(renamed).isEmpty() }
    }

    /** A folder rename reaches the media library through a scan; give it a moment. */
    private suspend fun waitFor(condition: suspend () -> Boolean) {
        withTimeout(TIMEOUT_MILLIS) {
            while (!condition()) kotlinx.coroutines.delay(200)
        }
    }

    private suspend fun <T> main(block: suspend () -> T): T = withTimeout(TIMEOUT_MILLIS) { withContext(Dispatchers.Main) { block() } }

    private suspend fun items(name: String): List<MediaItem> =
        MediaStoreRepository(context).also { it.invalidate() }.loadAll().filter { it.relativePath == path(name).relativePath }

    private suspend fun names(name: String): List<String> = items(name).map { it.displayName }.sorted()

    private companion object {
        const val TIMEOUT_MILLIS = 30_000L
    }
}
