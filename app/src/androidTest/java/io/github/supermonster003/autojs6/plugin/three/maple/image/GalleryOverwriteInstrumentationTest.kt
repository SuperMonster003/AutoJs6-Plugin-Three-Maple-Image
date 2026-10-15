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
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/**
 * ROADMAP 8.2 end to end: a gallery image opened in the viewer is edited, saved with "Overwrite the original", and
 * the snackbar's undo brings the original back; the media library row stays the same throughout.
 */
@RunWith(AndroidJUnit4::class)
class GalleryOverwriteInstrumentationTest {

    @get:Rule(order = 0)
    val permissions: GrantPermissionRule = GrantPermissionRule.grant(
        *(MediaStoreTestImages.permissions().toSet() + if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) setOf(Manifest.permission.WRITE_EXTERNAL_STORAGE) else emptySet()).toTypedArray(),
    )

    @get:Rule(order = 1)
    val compose = createEmptyComposeRule()

    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private val folder = "MapleGalleryOverwrite${System.currentTimeMillis()}"

    @Before fun markPermissionAsked() {
        TestScreen.awake()
        MediaPermissionRecordStore(context).markRequested()
    }

    @After fun tearDown() {
        OverwriteBackup(context).clear()
        cleanUp()
    }

    // Between the viewer and the editor there is a moment without any Compose hierarchy: keep waiting through it.
    private fun exists(tag: String): Boolean =
        runCatching { compose.onAllNodes(hasTestTag(tag)).fetchSemanticsNodes().isNotEmpty() }.getOrDefault(false)

    private fun shows(text: String): Boolean =
        runCatching { compose.onAllNodes(hasText(text)).fetchSemanticsNodes().isNotEmpty() }.getOrDefault(false)

    private fun enabled(tag: String): Boolean = runCatching { compose.onNodeWithTag(tag).assertIsEnabled() }.isSuccess

    private fun size(uri: Uri): Pair<Int, Int> {
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }
        return options.outWidth to options.outHeight
    }

    @Test fun anEditedGalleryImageOverwritesTheOriginalAndTheSnackbarUndoesIt() {
        val uri = MediaStoreTestImages.insert(context, folder, "wide.png", Color.RED, 40, 20, Bitmap.CompressFormat.PNG)
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
            compose.waitUntil(TIMEOUT) { exists("maple.editor.save.overwrite") }
            compose.onNodeWithTag("maple.editor.save.overwrite").performScrollTo().performClick()
            compose.onNodeWithTag("maple.editor.save.fileNote").assertTextEquals(context.getString(R.string.image_editor_save_replaces_name, "wide.png"))
            compose.onNodeWithTag("maple.dialog.confirm").performClick()

            // Back in the viewer: the original now holds the rotated image, in the same row.
            val saved = context.getString(R.string.overwrite_saved)
            compose.waitUntil(TIMEOUT) { shows(saved) }
            assertEquals(20 to 40, size(uri))
            val rows = runBlocking { MediaStoreRepository(context).also { it.invalidate() }.loadAll() }.filter { it.relativePath == relativePath() }
            assertEquals(listOf(item.id), rows.map { it.id })
            assertEquals(listOf("wide.png"), rows.map { it.displayName })

            compose.onNode(hasText(context.getString(R.string.action_undo))).performClick()
            compose.waitUntil(TIMEOUT) { size(uri) == 40 to 20 }
        }
    }

    @Test fun aViewerRotationIsSavedIntoTheJpegAndTheSnackbarUndoesIt() {
        val uri = MediaStoreTestImages.insert(context, folder, "turn.jpg", Color.RED, 40, 20, Bitmap.CompressFormat.JPEG)
        val item = runBlocking { MediaStoreRepository(context).also { it.invalidate() }.loadAll() }
            .first { it.id == android.content.ContentUris.parseId(uri) }
        val sizeBefore = item.sizeBytes
        val request = ImageViewerLaunchRequest.gallery(ViewerSource.request(item), ViewerSourceSpec.MediaAlbum(item.bucketId, MediaSort.DEFAULT, item.id))
        ActivityScenario.launchActivityForResult<ImageViewerActivity>(ImageViewerContract.viewerIntent(context, request)).use { scenario ->
            compose.waitUntil(TIMEOUT) { exists("maple.viewer.rotate") && enabled("maple.viewer.rotate") }
            // The rotate button does nothing until the page shows its image.
            compose.waitUntil(TIMEOUT) {
                var loaded = false
                scenario.onActivity { loaded = it.currentImageViewForTesting()?.drawable != null }
                loaded
            }
            val orientationBefore = orientation(uri)
            var viewBefore: Any? = null
            scenario.onActivity { viewBefore = it.currentImageViewForTesting() }
            compose.onNodeWithTag("maple.viewer.rotate").performClick()
            // The bar offers "Save" once the write check finished.
            compose.waitUntil(TIMEOUT) { exists("maple.viewer.rotationHint.save") }
            compose.onNodeWithTag("maple.viewer.rotationHint.save").performClick()
            val saved = context.getString(R.string.viewer_rotation_saved)
            compose.waitUntil(TIMEOUT) { shows(saved) }
            // JPEG: only the orientation tag changed, so the image now shows turned without being encoded again.
            assertEquals(androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_90, orientation(uri))
            assertEquals(20 to 40, shownSize(uri))
            assertTrue(kotlin.math.abs(fileSize(uri) - sizeBefore) < 128)
            // The saved turn is now in the image: a fresh page without a viewing rotation, or it would show turned twice.
            compose.waitUntil(TIMEOUT) { !exists("maple.viewer.rotationHint") }
            scenario.onActivity { activity ->
                val view = activity.currentImageViewForTesting()
                assertTrue(view !== viewBefore && view?.currentRotationQuarterTurns == ImageRotationState.ORIGINAL_QUARTER_TURNS)
            }

            compose.onNode(hasText(context.getString(R.string.action_undo)) and hasAnyAncestor(hasTestTag("maple.viewer.snackbar"))).performClick()
            // The original bytes are back, whatever orientation tag the encoder wrote.
            compose.waitUntil(TIMEOUT) { orientation(uri) == orientationBefore }
            assertEquals(sizeBefore, fileSize(uri))
        }
    }

    private fun orientation(uri: Uri): Int = context.contentResolver.openInputStream(uri)?.use {
        androidx.exifinterface.media.ExifInterface(it).getAttributeInt(
            androidx.exifinterface.media.ExifInterface.TAG_ORIENTATION,
            androidx.exifinterface.media.ExifInterface.ORIENTATION_NORMAL,
        )
    } ?: -1

    private fun shownSize(uri: Uri): Pair<Int, Int> =
        ImageBitmapIO.decodeForEditing(context.contentResolver, uri).let { (it.width to it.height).also { _ -> it.recycle() } }

    private fun fileSize(uri: Uri): Long = context.contentResolver.openFileDescriptor(uri, "r")?.use { it.statSize } ?: -1L

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
