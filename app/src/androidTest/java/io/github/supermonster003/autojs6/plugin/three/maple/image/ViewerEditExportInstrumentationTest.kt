package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.Manifest
import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/**
 * ROADMAP 5.2 viewer -> editor -> export: an edit saved without "Overwrite the original" goes through the system
 * document creator (stubbed here by an activity monitor) with the suggested `<name>_edited.<ext>` title, the edited
 * image lands in the chosen document, and the original stays as it was. A cancelled document creator writes nothing.
 */
@RunWith(AndroidJUnit4::class)
class ViewerEditExportInstrumentationTest {

    @get:Rule(order = 0)
    val permissions: GrantPermissionRule = GrantPermissionRule.grant(
        *(MediaStoreTestImages.permissions().toSet() + if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) setOf(Manifest.permission.WRITE_EXTERNAL_STORAGE) else emptySet()).toTypedArray(),
    )

    @get:Rule(order = 1)
    val compose = createEmptyComposeRule()

    private val instrumentation get() = InstrumentationRegistry.getInstrumentation()
    private val context get() = instrumentation.targetContext
    private val folder = "MapleViewerExport${System.currentTimeMillis()}"
    private var stub: DocumentCreatorStub? = null

    /** Answers a document creator request with [answer] instead of starting the system picker. */
    private class DocumentCreatorStub(answer: Instrumentation.ActivityResult) {

        /** Android 8.0+ hands the started Intent to the monitor, so the request itself can be checked. */
        private class Recorder(private val answer: Instrumentation.ActivityResult) : Instrumentation.ActivityMonitor() {
            @Volatile var request: Intent? = null

            override fun onStartActivity(intent: Intent): Instrumentation.ActivityResult? {
                if (intent.action != Intent.ACTION_CREATE_DOCUMENT) return null
                request = intent
                return answer
            }
        }

        // Android 7.x only matches a filter and counts the hits.
        val monitor: Instrumentation.ActivityMonitor = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Recorder(answer)
        } else {
            val filter = IntentFilter(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                addDataType("image/*")
            }
            Instrumentation.ActivityMonitor(filter, answer, true)
        }

        /** The request, where the platform shows it (Android 8.0+). */
        val request: Intent? get() = (monitor as? Recorder)?.request

        val requested: Boolean get() = monitor.hits > 0
    }

    @Before fun markPermissionAsked() {
        TestScreen.awake()
        MediaPermissionRecordStore(context).markRequested()
    }

    @After fun tearDown() {
        stub?.let { instrumentation.removeMonitor(it.monitor) }
        cleanUp()
    }

    // Between the viewer and the editor there is a moment without any Compose hierarchy: keep waiting through it.
    private fun exists(tag: String): Boolean =
        runCatching { compose.onAllNodes(hasTestTag(tag)).fetchSemanticsNodes().isNotEmpty() }.getOrDefault(false)

    private fun enabled(tag: String): Boolean = runCatching { compose.onNodeWithTag(tag).assertIsEnabled() }.isSuccess

    private fun size(uri: Uri): Pair<Int, Int> {
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }
        return options.outWidth to options.outHeight
    }

    private fun bytes(uri: Uri): ByteArray = context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: ByteArray(0)

    private fun await(timeout: Long, condition: () -> Boolean) {
        val deadline = System.currentTimeMillis() + timeout
        while (!condition()) {
            if (System.currentTimeMillis() > deadline) throw AssertionError("timed out; ${TestScreen.summary()}")
            Thread.sleep(200)
        }
    }

    /** Opens the gallery viewer on [uri], rotates it once in the editor and saves without overwriting. */
    private fun editAndSave(uri: Uri, block: () -> Unit) {
        val item = runBlocking { MediaStoreRepository(context).also { it.invalidate() }.loadAll() }
            .first { it.id == android.content.ContentUris.parseId(uri) }
        val request = ImageViewerLaunchRequest.gallery(ViewerSource.request(item), ViewerSourceSpec.MediaAlbum(item.bucketId, MediaSort.DEFAULT, item.id))
        // For result: the viewer accepts only the read grant flag, and a plain launch adds a new task flag.
        ActivityScenario.launchActivityForResult<ImageViewerActivity>(ImageViewerContract.viewerIntent(context, request)).use {
            compose.waitUntil(TIMEOUT) { exists("maple.viewer.edit") && enabled("maple.viewer.edit") }
            compose.onNodeWithTag("maple.viewer.edit").performClick()
            compose.waitUntil(TIMEOUT) { exists("maple.tool.rotateRight") && enabled("maple.tool.rotateRight") }
            compose.onNodeWithTag("maple.tool.rotateRight").performClick()
            compose.waitUntil(TIMEOUT) { enabled("maple.editor.save") }
            compose.onNodeWithTag("maple.editor.save").performClick()
            compose.waitUntil(TIMEOUT) { exists("maple.dialog.confirm") }
            compose.onNodeWithTag("maple.dialog.confirm").performClick()
            block()
        }
    }

    @Test fun anEditSavedAsANewFileIsExportedThroughTheDocumentCreator() {
        val original = MediaStoreTestImages.insert(context, folder, "wide.png", Color.RED, 40, 20, Bitmap.CompressFormat.PNG)
        // The document the user "creates": an image of this test, overwritten by the export.
        val target = MediaStoreTestImages.insert(context, folder, "target.png", Color.BLUE, 8, 8, Bitmap.CompressFormat.PNG)
        val originalBytes = bytes(original)
        val documents = DocumentCreatorStub(Instrumentation.ActivityResult(Activity.RESULT_OK, Intent().setData(target)))
        stub = documents.also { instrumentation.addMonitor(it.monitor) }

        editAndSave(original) {
            await(TIMEOUT) { size(target) == 20 to 40 }
        }

        assertEquals(true, documents.requested)
        documents.request?.let { request ->
            assertEquals("image/png", request.type)
            assertEquals(listOf(Intent.CATEGORY_OPENABLE), request.categories?.toList())
            assertEquals("wide_edited.png", request.getStringExtra(Intent.EXTRA_TITLE))
        }
        assertEquals(40 to 20, size(original))
        assertEquals(originalBytes.toList(), bytes(original).toList())
    }

    @Test fun aCancelledDocumentCreatorWritesNothing() {
        val original = MediaStoreTestImages.insert(context, folder, "wide.png", Color.RED, 40, 20, Bitmap.CompressFormat.PNG)
        val target = MediaStoreTestImages.insert(context, folder, "target.png", Color.BLUE, 8, 8, Bitmap.CompressFormat.PNG)
        val originalBytes = bytes(original)
        val targetBytes = bytes(target)
        val documents = DocumentCreatorStub(Instrumentation.ActivityResult(Activity.RESULT_CANCELED, null))
        stub = documents.also { instrumentation.addMonitor(it.monitor) }

        editAndSave(original) {
            await(TIMEOUT) { documents.requested }
            // Back in the viewer with nothing written.
            compose.waitUntil(TIMEOUT) { exists("maple.viewer.edit") }
        }

        assertEquals(originalBytes.toList(), bytes(original).toList())
        assertEquals(targetBytes.toList(), bytes(target).toList())
    }

    private fun relativePath() = "${Environment.DIRECTORY_PICTURES}/$folder/"

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
        const val TIMEOUT = 30_000L
    }
}
