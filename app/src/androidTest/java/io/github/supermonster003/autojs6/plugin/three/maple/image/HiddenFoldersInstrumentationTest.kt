package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.Manifest
import android.graphics.Color
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.activity.ComponentActivity
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/**
 * ROADMAP 7.4 hiding: a `.nomedia` file takes an album out of the media library, the gallery still reads it from
 * disk (through its private provider), and the undo brings it back. Android 11+ needs all files access for this;
 * grant it with `appops set --uid <package> MANAGE_EXTERNAL_STORAGE allow` before the run.
 */
@RunWith(AndroidJUnit4::class)
class HiddenFoldersInstrumentationTest {

    @get:Rule
    val permissions: GrantPermissionRule = GrantPermissionRule.grant(
        *(MediaStoreTestImages.permissions().toSet() + if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) setOf(Manifest.permission.WRITE_EXTERNAL_STORAGE) else emptySet()).toTypedArray(),
    )

    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private val folder = "MapleHidden${System.currentTimeMillis()}"
    private val path = AlbumPath(MediaCatalog.PRIMARY_VOLUME, "${Environment.DIRECTORY_PICTURES}/$folder/")
    private lateinit var scenario: ActivityScenario<ComponentActivity>
    private lateinit var coordinator: MediaWriteCoordinator

    @Before
    fun setUp() {
        assumeTrue("hiding needs file system access", HiddenFolders(context).available())
        scenario = ActivityScenario.launch(ComponentActivity::class.java)
        scenario.onActivity { coordinator = MediaWriteCoordinator(it) }
        MediaStoreTestImages.insert(context, folder, "a.png", Color.RED)
        MediaStoreTestImages.insert(context, folder, "b.png", Color.GREEN)
    }

    @After
    fun tearDown() {
        if (!::scenario.isInitialized) return
        scenario.close()
        val directory = HiddenFolders.directory(path)
        val files = directory.listFiles().orEmpty().map { it.absolutePath }
        directory.deleteRecursively()
        runBlocking { scan(files) }
        runBlocking { FolderMarksRepository(context).unmark(listOf(path), FolderMark.HIDDEN) }
    }

    @Test
    fun hidingTakesTheAlbumOutOfTheLibraryAndTheUndoBringsItBack() = runBlocking {
        waitFor { names() == listOf("a.png", "b.png") }
        val hidden = main { coordinator.hideFolders(listOf(path)) }
        assertEquals(1, hidden.succeeded)
        assertTrue(File(HiddenFolders.directory(path), HiddenFolders.NOMEDIA).isFile)
        waitFor { names().isEmpty() }

        val folders = HiddenFolders(context)
        val found = requireNotNull(folders.folder(path.relativePath))
        assertEquals(listOf("a.png", "b.png"), found.items.map { it.displayName }.sorted())
        assertTrue("discovered under Pictures/", folders.list().any { it.path == path })
        val bytes = context.contentResolver.openInputStream(Uri.parse(found.items.first().contentUri))!!.use { it.readBytes() }
        assertTrue(bytes.isNotEmpty())

        main { requireNotNull(hidden.undo).invoke() }
        waitFor { names() == listOf("a.png", "b.png") }
        assertEquals(null, folders.folder(path.relativePath))
    }

    @Test
    fun theHomeListsHiddenFoldersWhenAskedTo() = runBlocking {
        waitFor { names() == listOf("a.png", "b.png") }
        assertEquals(1, main { coordinator.hideFolders(listOf(path)) }.succeeded)
        val preferences = GalleryPreferences(context)
        val record = MediaPermissionRecordStore(context)
        val requestedBefore = record.requestedBefore
        val shownBefore = preferences.showHiddenFolders
        record.markRequested()
        preferences.showHiddenFolders = true
        try {
            ActivityScenario.launch(HomeActivity::class.java).use { home ->
                waitFor {
                    var hidden: List<AlbumSummary> = emptyList()
                    home.onActivity { hidden = it.galleryForTesting.albums.value.hidden }
                    hidden.any { it.subtitle == path.relativePath && it.count == 2 && it.kind == AlbumKind.HIDDEN }
                }
                assertTrue("a hidden album is not a media library album", home.let { scenarioHome ->
                    var albums: List<AlbumSummary> = emptyList()
                    scenarioHome.onActivity { albums = it.galleryForTesting.albums.value.albums }
                    albums.none { it.subtitle == path.relativePath }
                })
            }
        } finally {
            preferences.showHiddenFolders = shownBefore
            if (!requestedBefore) record.reset()
        }
    }

    private suspend fun names(): List<String> =
        MediaStoreRepository(context).also { it.invalidate() }.loadAll().filter { it.relativePath == path.relativePath }.map { it.displayName }.sorted()

    private suspend fun waitFor(condition: suspend () -> Boolean) {
        withTimeout(TIMEOUT_MILLIS) { while (!condition()) delay(200) }
    }

    private suspend fun scan(paths: List<String>) {
        if (paths.isEmpty()) return
        val done = CompletableDeferred<Unit>()
        var left = paths.size
        MediaScannerConnection.scanFile(context, paths.toTypedArray(), null) { _, _ -> if (--left == 0) done.complete(Unit) }
        withTimeout(TIMEOUT_MILLIS) { done.await() }
    }

    private suspend fun <T> main(block: suspend () -> T): T = withTimeout(TIMEOUT_MILLIS) { withContext(Dispatchers.Main) { block() } }

    private companion object {
        const val TIMEOUT_MILLIS = 30_000L
    }
}
