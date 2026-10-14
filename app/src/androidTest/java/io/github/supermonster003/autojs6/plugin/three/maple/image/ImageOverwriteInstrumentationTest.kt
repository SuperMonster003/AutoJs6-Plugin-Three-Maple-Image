package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.Manifest
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.core.content.FileProvider
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
import java.util.UUID

/**
 * ROADMAP 8.2 (T11): a media library image is overwritten in place (its row stays), the one backup brings the
 * original back and is replaced by the next overwrite, and which originals the standalone tools may overwrite.
 */
@RunWith(AndroidJUnit4::class)
class ImageOverwriteInstrumentationTest {

    @get:Rule
    val permissions: GrantPermissionRule = GrantPermissionRule.grant(
        *(MediaStoreTestImages.permissions().toSet() + if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) setOf(Manifest.permission.WRITE_EXTERNAL_STORAGE) else emptySet()).toTypedArray(),
    )

    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private val folder = "MapleOverwrite${System.currentTimeMillis()}"
    private lateinit var scenario: ActivityScenario<ComponentActivity>
    private lateinit var coordinator: MediaWriteCoordinator
    private val backup by lazy { OverwriteBackup(context) }
    private val files = mutableListOf<File>()

    @Before
    fun setUp() {
        scenario = ActivityScenario.launch(ComponentActivity::class.java)
        scenario.onActivity { coordinator = MediaWriteCoordinator(it) }
    }

    @After
    fun tearDown() {
        scenario.close()
        backup.clear()
        files.forEach { it.delete() }
        cleanUp()
    }

    @Test
    fun aLibraryImageIsOverwrittenInPlaceAndTheBackupRestoresIt() = runBlocking {
        val uri = MediaStoreTestImages.insert(context, folder, "a.png", Color.RED)
        val id = idOf(uri)
        val edited = pngFile(Color.BLUE, 20, 10)

        assertTrue(backup.store(context.contentResolver, uri))
        assertTrue(backup.holds(uri))
        assertEquals(1, main { coordinator.overwrite(uri) { edited.inputStream() } }.succeeded)
        assertEquals(Color.BLUE, firstPixel(uri))
        assertEquals(20 to 10, size(uri))
        // Same row, same name: the image was replaced, not added.
        assertEquals(listOf(id), ids())
        assertEquals(listOf("a.png"), names())

        assertEquals(1, main { coordinator.overwrite(uri) { backup.open() } }.succeeded)
        assertEquals(Color.RED, firstPixel(uri))
        assertEquals(listOf(id), ids())
    }

    @Test
    fun theNextBackupReplacesThePreviousOne() = runBlocking {
        val first = MediaStoreTestImages.insert(context, folder, "a.png", Color.RED)
        val second = MediaStoreTestImages.insert(context, folder, "b.png", Color.GREEN)
        assertTrue(backup.store(context.contentResolver, first))
        assertTrue(backup.store(context.contentResolver, second))
        assertFalse(backup.holds(first))
        assertTrue(backup.holds(second))
        val kept = File(context.filesDir, "overwrite-backup").listFiles().orEmpty().filter { it.name.endsWith(".bin") }
        assertEquals(1, kept.size)
        assertEquals(Color.GREEN, BitmapFactory.decodeStream(backup.open()).getPixel(0, 0))
    }

    @Test
    fun aDocumentIsWrittenThroughItsOwnGrant() = runBlocking {
        val document = File(context.cacheDir, "standalone-images/overwrite-${UUID.randomUUID()}.png").also { files += it }
        document.parentFile!!.mkdirs()
        pngFile(Color.RED, 4, 4).copyTo(document, overwrite = true)
        val uri = FileProvider.getUriForFile(context, context.packageName + ".standalone-images", document)
        assertEquals(1, main { coordinator.overwrite(uri) { pngFile(Color.YELLOW, 6, 6).inputStream() } }.succeeded)
        assertEquals(Color.YELLOW, BitmapFactory.decodeFile(document.path).getPixel(0, 0))
    }

    @Test
    fun onlyWritableOriginalsInAFormatTheToolsWriteMayBeOverwritten() {
        val library = MediaStoreTestImages.insert(context, folder, "a.png", Color.RED)
        assertTrue(StandaloneOverwrite.isOverwritable(context, StandaloneDocument(library, "a.png", "image/png", 10)))
        // A GIF would come back as PNG under the same name.
        assertFalse(StandaloneOverwrite.isOverwritable(context, StandaloneDocument(library, "a.gif", "image/gif", 10)))
        // Another app's document without a write grant (granted documents: StandaloneOverwriteTest on the JVM).
        val foreign = Uri.parse("content://com.android.externalstorage.documents/document/primary%3APictures%2Fx.png")
        assertFalse(StandaloneOverwrite.isOverwritable(context, StandaloneDocument(foreign, "x.png", "image/png", 10)))
    }

    private suspend fun <T> main(block: suspend () -> T): T = withTimeout(TIMEOUT_MILLIS) { withContext(Dispatchers.Main) { block() } }

    private fun pngFile(color: Int, width: Int, height: Int): File {
        val file = File(context.cacheDir, "overwrite-${UUID.randomUUID()}.png").also { files += it }
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).apply { eraseColor(color) }
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()
        return file
    }

    private fun firstPixel(uri: Uri): Int = requireNotNull(context.contentResolver.openInputStream(uri)).use { BitmapFactory.decodeStream(it) }.getPixel(0, 0)

    private fun size(uri: Uri): Pair<Int, Int> {
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        requireNotNull(context.contentResolver.openInputStream(uri)).use { BitmapFactory.decodeStream(it, null, options) }
        return options.outWidth to options.outHeight
    }

    private fun idOf(uri: Uri): Long = android.content.ContentUris.parseId(uri)

    private fun relativePath() = "${Environment.DIRECTORY_PICTURES}/$folder/"

    private suspend fun items(): List<MediaItem> =
        MediaStoreRepository(context).also { it.invalidate() }.loadAll().filter { it.relativePath == relativePath() }

    private suspend fun ids(): List<Long> = items().map { it.id }

    private suspend fun names(): List<String> = items().map { it.displayName }.sorted()

    private fun cleanUp() {
        val resolver = context.contentResolver
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val args = Bundle().apply {
                putString(android.content.ContentResolver.QUERY_ARG_SQL_SELECTION, "${MediaStore.MediaColumns.RELATIVE_PATH} = ?")
                putStringArray(android.content.ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS, arrayOf(relativePath()))
                putInt(MediaStore.QUERY_ARG_MATCH_TRASHED, MediaStore.MATCH_INCLUDE)
            }
            runCatching { resolver.delete(MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL), args) }
        } else {
            @Suppress("DEPRECATION")
            runCatching { resolver.delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "${MediaStore.MediaColumns.DATA} LIKE ?", arrayOf("%/${relativePath()}%")) }
        }
        @Suppress("DEPRECATION")
        File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), folder).deleteRecursively()
    }

    private companion object {
        const val TIMEOUT_MILLIS = 30_000L
    }
}
