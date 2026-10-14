package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.UUID
import kotlin.math.abs

/**
 * ROADMAP 8.2 (T15): a saved rotation shows the image exactly as the viewer showed it, turned by the saved quarter
 * turns. An asymmetric four-color JPEG goes through all 8 EXIF orientations and 1 to 3 turns (only the orientation
 * tag changes); PNG and WebP are encoded again.
 */
@RunWith(AndroidJUnit4::class)
class RotationSaveInstrumentationTest {

    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private val directory by lazy { File(context.cacheDir, "rotation-save-test-${UUID.randomUUID()}").apply { mkdirs() } }

    @After fun cleanUp() {
        directory.deleteRecursively()
    }

    /** Top left red, top right green, bottom left blue, bottom right yellow; 60 x 30 so the turns differ. */
    private fun quadrants(format: Bitmap.CompressFormat, name: String): File {
        val bitmap = Bitmap.createBitmap(60, 30, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint()
        listOf(Color.RED to (0 to 0), Color.GREEN to (30 to 0), Color.BLUE to (0 to 15), Color.YELLOW to (30 to 15)).forEach { (color, origin) ->
            paint.color = color
            canvas.drawRect(origin.first.toFloat(), origin.second.toFloat(), origin.first + 30f, origin.second + 15f, paint)
        }
        val file = File(directory, name)
        file.outputStream().use { bitmap.compress(format, 100, it) }
        bitmap.recycle()
        return file
    }

    private fun shown(file: File): Bitmap = ImageBitmapIO.decodeForEditing(context.contentResolver, Uri.fromFile(file))

    /** The colors at the centers of the four quadrants of what a viewer shows. */
    private fun quadrantColors(bitmap: Bitmap): List<Int> {
        val (w, h) = bitmap.width to bitmap.height
        return listOf(w / 4 to h / 4, 3 * w / 4 to h / 4, w / 4 to 3 * h / 4, 3 * w / 4 to 3 * h / 4).map { (x, y) -> bitmap.getPixel(x, y) }
    }

    private fun assertSameColors(expected: List<Int>, actual: List<Int>, message: String) {
        expected.zip(actual).forEach { (e, a) ->
            val close = abs(Color.red(e) - Color.red(a)) <= TOLERANCE && abs(Color.green(e) - Color.green(a)) <= TOLERANCE &&
                abs(Color.blue(e) - Color.blue(a)) <= TOLERANCE
            assertTrue("$message: expected ${Integer.toHexString(e)} got ${Integer.toHexString(a)}", close)
        }
    }

    @Test fun everyJpegOrientationKeepsWhatWasShownTurnedByTheSavedTurns() {
        val original = quadrants(Bitmap.CompressFormat.JPEG, "base.jpg")
        for (orientation in 1..8) {
            val source = File(directory, "o$orientation.jpg")
            original.copyTo(source, overwrite = true)
            ExifInterface(source.path).apply {
                setAttribute(ExifInterface.TAG_ORIENTATION, orientation.toString())
                saveAttributes()
            }
            val before = shown(source)
            for (turns in 1..3) {
                val output = File(directory, "o$orientation-t$turns.jpg")
                RotationSave.render(context.contentResolver, Uri.fromFile(source), "image/jpeg", turns, output, directory)
                val expected = ImageEditingEngine.transform(before, rotation = 90f * turns)
                val after = shown(output)
                val label = "orientation $orientation, $turns turns"
                assertEquals(label, expected.width to expected.height, after.width to after.height)
                assertSameColors(quadrantColors(expected), quadrantColors(after), label)
                // Only the tag changed: the compressed image data is the same length give or take the tag.
                assertTrue(label, abs(output.length() - source.length()) < 64)
                expected.recycle()
                after.recycle()
            }
            before.recycle()
        }
    }

    @Test fun pngAndWebpAreEncodedAgainWithTheTurnApplied() {
        for ((format, mime, name) in listOf(
            Triple(Bitmap.CompressFormat.PNG, "image/png", "q.png"),
            @Suppress("DEPRECATION") Triple(Bitmap.CompressFormat.WEBP, "image/webp", "q.webp"),
        )) {
            val source = quadrants(format, name)
            val before = shown(source)
            val output = File(directory, "turned-$name")
            RotationSave.render(context.contentResolver, Uri.fromFile(source), mime, 1, output, directory)
            val after = shown(output)
            val expected = ImageEditingEngine.transform(before, rotation = 90f)
            assertEquals(mime, 30 to 60, after.width to after.height)
            assertSameColors(quadrantColors(expected), quadrantColors(after), mime)
            // A saved rotation is plain pixels: no orientation tag is left to turn it again.
            assertEquals(ExifInterface.ORIENTATION_NORMAL, ExifInterface(output.path).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL))
            listOf(before, after, expected).forEach(Bitmap::recycle)
        }
    }

    @Test fun otherFormatsAreNotOffered() {
        assertEquals(null, RotationSave.kind("image/gif"))
        assertEquals(null, RotationSave.kind("image/heic"))
        assertEquals(RotationSave.Kind.EXIF_ORIENTATION, RotationSave.kind("image/jpeg"))
    }

    private companion object {
        const val TOLERANCE = 24
    }
}
