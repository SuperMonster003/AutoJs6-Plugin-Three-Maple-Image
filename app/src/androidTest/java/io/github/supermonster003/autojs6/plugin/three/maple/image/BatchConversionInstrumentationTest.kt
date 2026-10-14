package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.app.Application
import android.content.ContentResolver
import android.content.ContentUris
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.SystemClock
import android.provider.MediaStore
import android.util.Log
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTouchInput
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.Locale
import java.util.UUID

/**
 * ROADMAP 7.6 (gallery entry): a batch writes converted copies next to the originals, reports the images it could
 * not convert while converting the rest, stops between images when cancelled without leaving partial files, and the
 * selection bar leads to the settings page and back to the grid.
 */
@RunWith(AndroidJUnit4::class)
class BatchConversionInstrumentationTest {

    @get:Rule
    val compose = createEmptyComposeRule()

    @get:Rule
    val permission: GrantPermissionRule = GrantPermissionRule.grant(*MediaStoreTestImages.permissions())

    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private val record by lazy { MediaPermissionRecordStore(context) }
    private var originalRequested = false
    private val folder = "MapleBatch-" + UUID.randomUUID().toString().take(8)
    private val path = AlbumPath(MediaCatalog.PRIMARY_VOLUME, "${Environment.DIRECTORY_PICTURES}/$folder/")
    private val inserted = ArrayList<Uri>()

    @Before
    fun setUp() {
        originalRequested = record.requestedBefore
        record.markRequested()
    }

    @After
    fun tearDown() {
        // The converted copies belong to the app; delete them with the inserted originals.
        val created = folderItems().map { Uri.parse(it.contentUri) }
        MediaStoreTestImages.cleanUp(context, inserted + created, folder)
        if (!originalRequested) record.reset()
    }

    @Test
    fun aBatchWritesTurnedAndResizedCopiesNextToTheOriginals() {
        insert("wide.png", Color.RED, 120, 80, Bitmap.CompressFormat.PNG)
        insert("photo.jpg", Color.GREEN, 90, 60, Bitmap.CompressFormat.JPEG)
        @Suppress("DEPRECATION")
        insert("sticker.webp", Color.BLUE, 60, 40, Bitmap.CompressFormat.WEBP)
        val sources = folderItems()
        assertEquals(3, sources.size)

        val model = BatchConversionViewModel(context.applicationContext as Application)
        model.update(BatchConversionSettings(format = ImageOutputFormat.JPEG, resizeMode = ImageResizeMode.PERCENTAGE, percentage = "50", rotationDegrees = 90))
        model.prepare(sources)
        assertTrue(model.start())
        val result = awaitFinished(model)

        assertEquals(3, result.succeeded)
        assertEquals(emptyList<BatchFailureEntry>(), result.failures)
        val outputs = folderItems().filter { "_converted" in it.displayName }.associateBy { it.displayName }
        assertEquals(setOf("wide_converted.jpg", "photo_converted.jpg", "sticker_converted.jpg"), outputs.keys)
        // Half size, then a quarter turn: 120 x 80 becomes 40 x 60.
        assertEquals(Triple(40, 60, "image/jpeg"), bounds(outputs.getValue("wide_converted.jpg")))
        assertEquals(Triple(30, 45, "image/jpeg"), bounds(outputs.getValue("photo_converted.jpg")))
        assertEquals(Triple(20, 30, "image/jpeg"), bounds(outputs.getValue("sticker_converted.jpg")))
        assertEquals(sources.map { it.id }.toSet(), folderItems().filter { "_converted" !in it.displayName }.map { it.id }.toSet())

        // The finished run is reported once.
        assertEquals(result.runId, model.takeFinished()?.runId)
        assertNull(model.takeFinished())
    }

    @Test
    fun anUnreadableImageIsReportedAndTheOthersAreStillConverted() {
        insert("a.png", Color.RED, 40, 40, Bitmap.CompressFormat.PNG)
        insert("b.png", Color.GREEN, 40, 40, Bitmap.CompressFormat.PNG)
        val (first, second) = folderItems()
        val gone = first.copy(contentUri = ContentUris.withAppendedId(MediaStoreRepository(context).collection, Long.MAX_VALUE / 2).toString())

        val model = BatchConversionViewModel(context.applicationContext as Application)
        model.prepare(listOf(gone, second))
        assertTrue(model.start())
        val result = awaitFinished(model)

        assertEquals(1, result.succeeded)
        assertEquals(listOf(BatchFailureEntry(gone, BatchFailure.UNREADABLE)), result.failures)
        assertEquals(listOf("${MediaFileNamePolicy.baseName(second.displayName)}_converted.jpg"), folderItems().map { it.displayName }.filter { "_converted" in it })
    }

    @Test
    fun cancelStopsBetweenImagesAndLeavesNoPartialFiles() {
        repeat(CANCEL_COUNT) { n -> insert("big-$n.png", if (n % 2 == 0) Color.CYAN else Color.MAGENTA, 1600, 1200, Bitmap.CompressFormat.PNG) }
        val sources = folderItems()

        val model = BatchConversionViewModel(context.applicationContext as Application)
        model.update(BatchConversionSettings(format = ImageOutputFormat.PNG))
        model.prepare(sources)
        assertTrue(model.start())
        model.cancel()
        val result = awaitFinished(model)

        assertTrue(result.cancelled)
        assertTrue("cancel stopped nothing: ${result.succeeded}", result.succeeded < CANCEL_COUNT)
        assertEquals(result.succeeded, folderItems().count { "_converted" in it.displayName })
        assertEquals(0, pendingRows())
        @Suppress("DEPRECATION")
        val directory = File(Environment.getExternalStorageDirectory(), path.relativePath)
        assertEquals(emptyList<String>(), directory.list().orEmpty().filter { it.endsWith(".pending") })
    }

    @Test
    fun theSelectionBarOpensTheSettingsPageAndStartConverts() {
        insert("one.png", Color.RED, 40, 30, Bitmap.CompressFormat.PNG)
        insert("two.png", Color.GREEN, 40, 30, Bitmap.CompressFormat.PNG)
        val ids = inserted.map { ContentUris.parseId(it) }.toSet()
        val bucketId = runBlocking { MediaStoreRepository(context).also { it.invalidate() }.loadAll() }.first { it.id in ids }.bucketId
        ActivityScenario.launch(HomeActivity::class.java).use { scenario ->
            val albumTag = "maple.gallery.album.media.$bucketId"
            compose.waitUntil(TIMEOUT_MILLIS) { compose.onAllNodes(hasTestTag("maple.gallery.grid")).fetchSemanticsNodes().isNotEmpty() }
            compose.onNodeWithTag("maple.gallery.grid").performScrollToNode(hasTestTag(albumTag))
            compose.onNodeWithTag(albumTag).performClick()
            var items: List<MediaItem> = emptyList()
            compose.waitUntil(TIMEOUT_MILLIS) {
                scenario.onActivity { activity -> items = activity.galleryForTesting.grid.value.items }
                items.size == 2 && compose.onAllNodes(hasTestTag("maple.media.item.${items.first().id}")).fetchSemanticsNodes().isNotEmpty()
            }
            compose.onNodeWithTag("maple.media.item.${items[0].id}").performTouchInput { longClick() }
            compose.onNodeWithTag("maple.media.item.${items[1].id}").performClick()
            compose.onNodeWithTag("maple.selection.barMore").performClick()
            compose.onNodeWithTag("maple.selection.convert").performClick()
            compose.waitUntil(TIMEOUT_MILLIS) { compose.onAllNodes(hasTestTag("maple.batch.start")).fetchSemanticsNodes().isNotEmpty() }
            compose.onNodeWithTag("maple.batch.format.2").performScrollTo().performClick()
            compose.onNodeWithTag("maple.batch.start").performClick()
            compose.waitUntil(TIMEOUT_MILLIS) { folderItems().count { it.displayName.endsWith("_converted.webp") } == 2 }
            compose.waitUntil(TIMEOUT_MILLIS) { compose.onAllNodes(hasTestTag("maple.media.grid")).fetchSemanticsNodes().isNotEmpty() }
            var selection: Set<Long> = setOf(-1L)
            scenario.onActivity { selection = it.galleryForTesting.grid.value.selection }
            assertEquals(emptySet<Long>(), selection)
        }
    }

    /**
     * ROADMAP 7.6 acceptance, only with `-e batchLoad true`: 100 images of three formats and sizes convert in one batch,
     * then a second batch cancelled half way leaves exactly the images it finished and no pending file.
     */
    @Test
    fun aHundredMixedImagesConvertAndACancelledBatchLeavesNoPartialFile() {
        assumeTrue("set -e batchLoad true to insert 100 images", InstrumentationRegistry.getArguments().getString("batchLoad") == "true")
        @Suppress("DEPRECATION")
        val kinds = listOf(
            Triple(Bitmap.CompressFormat.JPEG, "jpg", 4000 to 3000),
            Triple(Bitmap.CompressFormat.PNG, "png", 1920 to 1080),
            Triple(Bitmap.CompressFormat.WEBP, "webp", 1280 to 960),
        )
        repeat(LOAD_COUNT) { n ->
            val (format, extension, size) = kinds[n % kinds.size]
            insert("load-%03d.%s".format(Locale.ROOT, n, extension), Color.rgb(n * 2, 255 - n * 2, 128), size.first, size.second, format)
        }
        val sources = folderItems()
        assertEquals(LOAD_COUNT, sources.size)
        val model = BatchConversionViewModel(context.applicationContext as Application)
        model.update(BatchConversionSettings(format = ImageOutputFormat.JPEG, quality = 85, resizeMode = ImageResizeMode.LONG_EDGE, longEdge = "1920"))

        model.prepare(sources)
        val started = SystemClock.elapsedRealtime()
        assertTrue(model.start())
        val full = awaitFinished(model, LOAD_TIMEOUT_MILLIS)
        Log.i(TAG, "converted ${full.succeeded} of $LOAD_COUNT mixed images in ${SystemClock.elapsedRealtime() - started} ms")
        assertEquals(emptyList<BatchFailureEntry>(), full.failures)
        assertEquals(LOAD_COUNT, full.succeeded)
        assertEquals(LOAD_COUNT, folderItems().count { it.displayName.endsWith("_converted.jpg") })

        model.update(BatchConversionSettings(format = ImageOutputFormat.WEBP, quality = 80))
        model.prepare(sources)
        assertTrue(model.start())
        val deadline = SystemClock.elapsedRealtime() + LOAD_TIMEOUT_MILLIS
        while ((model.progress.value?.done ?: 0) < LOAD_COUNT / 2 && SystemClock.elapsedRealtime() < deadline) SystemClock.sleep(20)
        model.cancel()
        val cancelled = awaitFinished(model, LOAD_TIMEOUT_MILLIS)
        Log.i(TAG, "cancelled after ${cancelled.succeeded} of $LOAD_COUNT")
        assertTrue(cancelled.cancelled)
        assertTrue(cancelled.succeeded in 1 until LOAD_COUNT)
        assertEquals(cancelled.succeeded, folderItems().count { it.displayName.endsWith("_converted.webp") })
        assertEquals(0, pendingRows())
        @Suppress("DEPRECATION")
        val directory = File(Environment.getExternalStorageDirectory(), path.relativePath)
        assertEquals(emptyList<String>(), directory.list().orEmpty().filter { it.endsWith(".pending") })
    }

    private fun insert(name: String, color: Int, width: Int, height: Int, format: Bitmap.CompressFormat) {
        inserted += MediaStoreTestImages.insert(context, folder, name, color, width, height, format)
    }

    private fun folderItems(): List<MediaItem> = runBlocking { MediaStoreRepository(context).loadFolder(path) }.sortedBy { it.displayName }

    private fun bounds(item: MediaItem): Triple<Int, Int, String?> {
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(Uri.parse(item.contentUri)).use { BitmapFactory.decodeStream(it, null, options) }
        return Triple(options.outWidth, options.outHeight, options.outMimeType)
    }

    /** Rows of the test folder still pending (Android 10+), which a cancelled image must not leave. */
    private fun pendingRows(): Int {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return 0
        val args = Bundle().apply {
            putString(ContentResolver.QUERY_ARG_SQL_SELECTION, "${MediaStore.MediaColumns.RELATIVE_PATH} = ?")
            putStringArray(ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS, arrayOf(path.relativePath))
            putInt(MediaStore.QUERY_ARG_MATCH_PENDING, MediaStore.MATCH_ONLY)
        }
        return context.contentResolver.query(MediaStoreRepository(context).collection, arrayOf(MediaStore.MediaColumns._ID), args, null)?.use { it.count } ?: 0
    }

    private fun awaitFinished(model: BatchConversionViewModel, timeoutMillis: Long = TIMEOUT_MILLIS): BatchProgress {
        val deadline = SystemClock.elapsedRealtime() + timeoutMillis
        while (SystemClock.elapsedRealtime() < deadline) {
            model.progress.value?.takeIf { !it.running }?.let { return it }
            SystemClock.sleep(50)
        }
        throw AssertionError("The batch did not finish: ${model.progress.value}")
    }

    private companion object {
        const val TIMEOUT_MILLIS = 60_000L
        const val CANCEL_COUNT = 8
        const val TAG = "MapleBatch"
        const val LOAD_COUNT = 100
        const val LOAD_TIMEOUT_MILLIS = 15L * 60L * 1000L
    }
}
