package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.MapleTheme
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.ShareCopyDialog
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.ShareCopyDialogState
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.testPalette
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.UUID

/**
 * ROADMAP 9.1: a shared copy carries no EXIF at all (with the orientation applied to its pixels), a smaller copy
 * follows the chosen long edge, and copies older than a day are removed.
 */
@RunWith(AndroidJUnit4::class)
class ShareCopiesInstrumentationTest {

    @get:Rule
    val compose = createComposeRule()

    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private val fixtures = mutableListOf<File>()

    @After fun cleanUp() {
        fixtures.forEach(File::delete)
        ShareCopies.root(context).deleteRecursively()
    }

    private fun fixtureUri(file: File) = FileProvider.getUriForFile(context, context.packageName + ".standalone-images", file)

    private fun newFixture(extension: String): File =
        File(context.cacheDir, "standalone-images/share-${UUID.randomUUID()}.$extension").also {
            it.parentFile!!.mkdirs()
            fixtures += it
        }

    private fun copyFile(uri: android.net.Uri): File {
        val target = File(context.cacheDir, "share-check-${UUID.randomUUID()}").also { fixtures += it }
        context.contentResolver.openInputStream(uri)!!.use { input -> target.outputStream().use { input.copyTo(it) } }
        return target
    }

    @Test fun aCopyWithoutMetadataHasNoExifAndAppliesTheOrientation() {
        val source = newFixture("jpg")
        ExifJpegTestFixture.writeCompleteTo(source)
        val original = ExifInterface(source.absolutePath)
        assertTrue(original.latLong != null && original.getAttribute(ExifInterface.TAG_MAKE) != null)
        val sourceBounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }.also { BitmapFactory.decodeFile(source.path, it) }

        val copy = ShareCopies.create(context, fixtureUri(source), "IMG_0042.jpg", ShareCopyRequest(ShareCopyMode.WITHOUT_METADATA))
        assertEquals("IMG_0042.jpg", copy.lastPathSegment)
        val file = copyFile(copy)
        val exif = ExifInterface(file.absolutePath)
        assertNull(exif.latLong)
        assertNull(exif.getAttribute(ExifInterface.TAG_MAKE))
        assertNull(exif.getAttribute(ExifInterface.TAG_MODEL))
        assertNull(exif.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL))
        assertEquals(ExifInterface.ORIENTATION_UNDEFINED, exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED))
        // The fixture is turned a quarter by its tag: the copy holds the turned pixels instead.
        val copyBounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }.also { BitmapFactory.decodeFile(file.path, it) }
        assertEquals(sourceBounds.outHeight to sourceBounds.outWidth, copyBounds.outWidth to copyBounds.outHeight)
    }

    @Test fun aSmallerCopyFollowsTheLongEdgeAndKeepsPng() {
        val source = newFixture("png")
        val bitmap = Bitmap.createBitmap(2400, 1200, Bitmap.Config.ARGB_8888).apply { eraseColor(Color.BLUE) }
        source.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()

        val copy = ShareCopies.create(context, fixtureUri(source), "wide.png", ShareCopyRequest(ShareCopyMode.SMALLER, 1080))
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(copy)!!.use { BitmapFactory.decodeStream(it, null, bounds) }
        assertEquals(1080 to 540, bounds.outWidth to bounds.outHeight)
        assertEquals("image/png", bounds.outMimeType)
    }

    @Test fun copiesOlderThanADayAreRemoved() {
        val root = ShareCopies.root(context)
        val old = File(root, "old").apply { mkdirs(); File(this, "a.jpg").writeText("x") }
        val fresh = File(root, "fresh").apply { mkdirs(); File(this, "b.jpg").writeText("y") }
        val now = System.currentTimeMillis()
        old.setLastModified(now - ShareCopyPolicy.MAX_AGE_MILLIS - 60_000L)
        ShareCopies.cleanup(context, now)
        assertFalse(old.exists())
        assertTrue(fresh.exists())
    }

    @Test fun aCustomLongEdgeMustBeANumberInRange() {
        val state = ShareCopyDialogState()
        compose.setContent {
            MapleTheme(palette = testPalette()) {
                ShareCopyDialog(state, onDismissRequest = {}, onShare = {})
            }
        }
        compose.onNodeWithTag("maple.dialog.confirm").assertIsEnabled()
        compose.onNodeWithTag("maple.shareCopy.smaller").performClick()
        compose.onNodeWithTag("maple.shareCopy.size.2").performClick()
        compose.onNodeWithTag("maple.dialog.confirm").assertIsNotEnabled()
        compose.onNodeWithTag("maple.shareCopy.custom").performTextInput("720")
        compose.onNodeWithTag("maple.dialog.confirm").assertIsEnabled()
        assertEquals(ShareCopyRequest(ShareCopyMode.SMALLER, 720), state.request())
    }
}
