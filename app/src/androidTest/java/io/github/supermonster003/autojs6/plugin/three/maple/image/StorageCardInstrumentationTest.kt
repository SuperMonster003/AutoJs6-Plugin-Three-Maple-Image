package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.Manifest
import android.content.ContentValues
import android.graphics.Color
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.os.storage.StorageManager
import android.provider.MediaStore
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/**
 * ROADMAP 7.3 across volumes, on a device with a storage card (skipped without one). Android 11+ moves an image
 * into the album that was chosen, on that album's volume, and copies land there as well; Android 7-10 cannot write
 * to a storage card, so the gallery leaves card images as they are and says so, without stray copies. A card without
 * room for the test images (an emulator card filled by other work) skips the class as well instead of failing on its
 * fixtures.
 */
@RunWith(AndroidJUnit4::class)
class StorageCardInstrumentationTest {

    @get:Rule
    val permissions: GrantPermissionRule = GrantPermissionRule.grant(
        *(MediaStoreTestImages.permissions().toSet() + if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) setOf(Manifest.permission.WRITE_EXTERNAL_STORAGE) else emptySet()).toTypedArray(),
    )

    private val instrumentation get() = InstrumentationRegistry.getInstrumentation()
    private val context get() = instrumentation.targetContext
    private val folder = "MapleCard${System.currentTimeMillis()}"
    private val relativePath = "${Environment.DIRECTORY_PICTURES}/$folder/"
    private lateinit var card: Card
    private lateinit var scenario: ActivityScenario<ComponentActivity>
    private lateinit var coordinator: MediaWriteCoordinator

    private class Card(val volume: String, val directory: File)

    private val cardAlbum get() = AlbumPath(card.volume, relativePath)
    private val internalAlbum get() = AlbumPath(MediaCatalog.PRIMARY_VOLUME, relativePath)

    @Before
    fun setUp() {
        val found = findCard()
        assumeTrue("no storage card mounted", found != null)
        val free = requireNotNull(found).directory.usableSpace
        assumeTrue("the storage card is full ($free bytes free)", free >= MIN_FREE_BYTES)
        card = found
        scenario = ActivityScenario.launch(ComponentActivity::class.java)
        scenario.onActivity { coordinator = MediaWriteCoordinator(it) }
    }

    @After
    fun tearDown() {
        if (!::card.isInitialized) return
        scenario.close()
        cleanUp()
    }

    @Test
    fun aMoveAcrossVolumesLandsInTheChosenAlbum() = runBlocking {
        assumeTrue(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
        insertOnCard("a.png", Color.RED)
        MediaStoreTestImages.insert(context, folder, "b.png", Color.GREEN)
        val a = items(cardAlbum).single()
        val b = items(internalAlbum).single()

        val toInternal = main { coordinator.move(listOf(a), internalAlbum, NameConflictChoice.AUTO_RENAME) }
        assertEquals(1, toInternal.succeeded)
        assertEquals(emptyList<String>(), names(cardAlbum))
        assertEquals(listOf("a.png", "b.png"), names(internalAlbum))
        assertFalse(File(card.directory, relativePath + "a.png").exists())
        main { requireNotNull(toInternal.undo).invoke() }
        assertEquals(listOf("a.png"), names(cardAlbum))
        assertEquals(listOf("b.png"), names(internalAlbum))

        val toCard = main { coordinator.move(listOf(b), cardAlbum, NameConflictChoice.AUTO_RENAME) }
        assertEquals(1, toCard.succeeded)
        assertEquals(listOf("a.png", "b.png"), names(cardAlbum))
        assertEquals(emptyList<String>(), names(internalAlbum))
        assertTrue(File(card.directory, relativePath + "b.png").isFile)
        main { requireNotNull(toCard.undo).invoke() }
        assertEquals(listOf("b.png"), names(internalAlbum))
    }

    @Test
    fun copiesAndNameConflictsStayOnTheirVolume() = runBlocking {
        assumeTrue(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
        insertOnCard("a.png", Color.RED)
        MediaStoreTestImages.insert(context, folder, "a.png", Color.GREEN)
        val onCard = items(cardAlbum).single()
        val internal = items(internalAlbum).single()
        // The same name in the same relative path on the other volume is no conflict.
        assertEquals(0, main { coordinator.conflicts(listOf(internal), AlbumPath(card.volume, "${Environment.DIRECTORY_PICTURES}/${folder}x/")) })
        assertEquals(1, main { coordinator.conflicts(listOf(onCard), internalAlbum) })

        val copied = main { coordinator.copy(listOf(internal), AlbumPath(card.volume, "${Environment.DIRECTORY_PICTURES}/${folder}x/"), NameConflictChoice.AUTO_RENAME) }
        assertEquals(1, copied.succeeded)
        assertEquals(listOf("a.png"), names(AlbumPath(card.volume, "${Environment.DIRECTORY_PICTURES}/${folder}x/")))
        assertTrue(File(card.directory, "${Environment.DIRECTORY_PICTURES}/${folder}x/a.png").isFile)
        assertEquals(listOf("a.png"), names(internalAlbum))
        assertEquals(listOf("a.png"), names(cardAlbum))
    }

    @Test
    fun trashAndRenameWorkOnTheCard() = runBlocking {
        assumeTrue(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
        insertOnCard("a.png", Color.RED)
        val a = items(cardAlbum).single()
        val renamed = main { coordinator.rename(a, "c.png") }
        assertEquals(1, renamed.succeeded)
        assertEquals(listOf("c.png"), names(cardAlbum))
        main { requireNotNull(renamed.undo).invoke() }
        assertEquals(listOf("a.png"), names(cardAlbum))

        val trashed = main { coordinator.trash(items(cardAlbum)) }
        assertEquals(1, trashed.succeeded)
        assertEquals(emptyList<String>(), names(cardAlbum))
        main { requireNotNull(trashed.undo).invoke() }
        assertEquals(listOf("a.png"), names(cardAlbum))
    }

    @Test
    fun theCardIsReadOnlyBeforeAndroid11() = runBlocking {
        assumeTrue(Build.VERSION.SDK_INT < Build.VERSION_CODES.R)
        copyToCardWithShell("a.png", Color.RED)
        MediaStoreTestImages.insert(context, folder, "b.png", Color.GREEN)
        val a = items(cardAlbum).single()
        val b = items(internalAlbum).single()
        val file = File(card.directory, relativePath + "a.png")
        assertFalse(MediaWriteCoordinator.isWritableAlbum(cardAlbum))
        assertTrue(MediaWriteCoordinator.isWritableAlbum(internalAlbum))

        assertEquals(WriteOutcome(readOnlyCard = 1), main { coordinator.trash(listOf(a)) })
        assertEquals(WriteOutcome(readOnlyCard = 1), main { coordinator.delete(listOf(a)) })
        assertEquals(WriteOutcome(readOnlyCard = 1), main { coordinator.rename(a, "c.png") })
        val moved = main { coordinator.move(listOf(a), AlbumPath(MediaCatalog.PRIMARY_VOLUME, "${Environment.DIRECTORY_PICTURES}/${folder}x/"), NameConflictChoice.AUTO_RENAME) }
        assertEquals(0, moved.succeeded)
        assertEquals(1, moved.readOnlyCard)
        assertEquals(1, main { coordinator.move(listOf(b), cardAlbum, NameConflictChoice.AUTO_RENAME) }.readOnlyCard)
        assertTrue("the card image stays", file.isFile)
        assertEquals(listOf("a.png"), names(cardAlbum))
        @Suppress("DEPRECATION")
        val strayFolder = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "${folder}x")
        assertTrue("no stray copy on the internal storage", strayFolder.list().isNullOrEmpty())

        // Reading the card is fine: a copy onto the internal storage works.
        val copied = main { coordinator.copy(listOf(a), AlbumPath(MediaCatalog.PRIMARY_VOLUME, "${Environment.DIRECTORY_PICTURES}/${folder}x/"), NameConflictChoice.AUTO_RENAME) }
        assertEquals(1, copied.succeeded)
        assertEquals(listOf("a.png"), names(AlbumPath(MediaCatalog.PRIMARY_VOLUME, "${Environment.DIRECTORY_PICTURES}/${folder}x/")))
        assertTrue(file.isFile)
    }

    // ---- fixtures ----

    private fun findCard(): Card? {
        val storage = context.getSystemService(StorageManager::class.java) ?: return null
        val volume = storage.storageVolumes.firstOrNull { it.isRemovable && it.state == Environment.MEDIA_MOUNTED } ?: return null
        val uuid = volume.uuid ?: return null
        val directory = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) volume.directory ?: File("/storage/$uuid") else File("/storage/$uuid")
        val name = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) volume.mediaStoreVolumeName ?: uuid.lowercase() else uuid.lowercase()
        return Card(name, directory)
    }

    /** Android 11+: the app inserts the image on the card itself (its own row, so writes need no confirmation). */
    private fun insertOnCard(name: String, color: Int) {
        val resolver = context.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, name)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.RELATIVE_PATH, relativePath)
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
        val uri = requireNotNull(resolver.insert(MediaStore.Images.Media.getContentUri(card.volume), values))
        requireNotNull(resolver.openOutputStream(uri)).use { it.write(MediaStoreTestImages.solidPng(color)) }
        resolver.update(uri, ContentValues().apply { put(MediaStore.Images.Media.IS_PENDING, 0) }, null, null)
    }

    /** Android 7-10: apps cannot write to the card, the shell can; the media scanner then lists the file. */
    private suspend fun copyToCardWithShell(name: String, color: Int) {
        @Suppress("DEPRECATION")
        val staging = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "$folder-$name")
        staging.writeBytes(MediaStoreTestImages.solidPng(color))
        val target = File(card.directory, relativePath + name)
        shell("mkdir -p ${target.parent}")
        shell("cp ${staging.absolutePath} ${target.absolutePath}")
        staging.delete()
        assertTrue("the shell could not write to the card", target.isFile)
        scan(target.absolutePath)
        withTimeout(TIMEOUT_MILLIS) { while (names(cardAlbum) != listOf(name)) delay(200) }
    }

    private fun shell(command: String) {
        ParcelFileDescriptor.AutoCloseInputStream(instrumentation.uiAutomation.executeShellCommand(command)).use { it.readBytes() }
    }

    private suspend fun scan(path: String) {
        val done = CompletableDeferred<Unit>()
        MediaScannerConnection.scanFile(context, arrayOf(path), null) { _, _ -> done.complete(Unit) }
        withTimeout(TIMEOUT_MILLIS) { done.await() }
    }

    private suspend fun <T> main(block: suspend () -> T): T = withTimeout(TIMEOUT_MILLIS) { withContext(Dispatchers.Main) { block() } }

    private suspend fun items(path: AlbumPath): List<MediaItem> =
        MediaStoreRepository(context).also { it.invalidate() }.loadAll().filter { it.albumPath.sameAs(path) }

    private suspend fun names(path: AlbumPath): List<String> = items(path).map { it.displayName }.sorted()

    private fun cleanUp() {
        val resolver = context.contentResolver
        val folders = listOf(folder, "${folder}x")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            for (name in folders) {
                val args = Bundle().apply {
                    putString(android.content.ContentResolver.QUERY_ARG_SQL_SELECTION, "${MediaStore.MediaColumns.RELATIVE_PATH} = ?")
                    putStringArray(android.content.ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS, arrayOf("${Environment.DIRECTORY_PICTURES}/$name/"))
                    putInt(MediaStore.QUERY_ARG_MATCH_TRASHED, MediaStore.MATCH_INCLUDE)
                }
                runCatching { resolver.delete(MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL), args) }
                runCatching { File(card.directory, "${Environment.DIRECTORY_PICTURES}/$name").delete() }
            }
        } else {
            for (name in folders) {
                shell("rm -rf ${File(card.directory, "${Environment.DIRECTORY_PICTURES}/$name").absolutePath}")
                @Suppress("DEPRECATION")
                runCatching { resolver.delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "${MediaStore.MediaColumns.DATA} LIKE ?", arrayOf("%/${Environment.DIRECTORY_PICTURES}/$name/%")) }
            }
        }
        for (name in folders) {
            @Suppress("DEPRECATION")
            File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), name).deleteRecursively()
        }
    }

    private companion object {
        const val TIMEOUT_MILLIS = 30_000L
        const val MIN_FREE_BYTES = 1L shl 20
    }
}
