package io.github.supermonster003.autojs6.plugin.imagetools

import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ImageBitmapIOInspectTest {

    private lateinit var temporaryDirectory: File

    @Before
    fun setUp() {
        temporaryDirectory = Files.createTempDirectory("image-bitmap-io-inspect-test").toFile()
    }

    @After
    fun tearDown() {
        temporaryDirectory.deleteRecursively()
    }

    @Test
    fun boundsOnlyDecodeInspectsAndThenDecodesARealPng() {
        val source = File(temporaryDirectory, "source.png")
        val bitmap = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888)
        try {
            bitmap.eraseColor(Color.rgb(24, 96, 160))
            FileOutputStream(source).use { output ->
                check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, output))
            }
        } finally {
            bitmap.recycle()
        }

        val resolver = RuntimeEnvironment.getApplication().contentResolver
        val uri = Uri.fromFile(source)
        val info = ImageBitmapIO.inspect(resolver, uri)

        assertEquals(ImagePixelSize(WIDTH, HEIGHT), info.rawSize)
        assertEquals(ImagePixelSize(WIDTH, HEIGHT), info.displaySize)
        assertEquals(ImageOutputFormat.PNG.mimeType, info.mimeType)
        assertEquals(ExifInterface.ORIENTATION_NORMAL, info.orientation)

        val decoded = ImageBitmapIO.decodeForEditing(resolver, uri)
        try {
            assertEquals(WIDTH, decoded.width)
            assertEquals(HEIGHT, decoded.height)
        } finally {
            decoded.recycle()
        }
    }

    private companion object {
        const val WIDTH = 73
        const val HEIGHT = 41
    }
}
