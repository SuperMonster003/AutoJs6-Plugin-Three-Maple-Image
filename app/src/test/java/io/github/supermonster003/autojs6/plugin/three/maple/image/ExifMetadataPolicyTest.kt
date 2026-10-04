package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.graphics.Bitmap
import android.graphics.Color
import androidx.exifinterface.media.ExifInterface
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ExifMetadataPolicyTest {

    private lateinit var temporaryDirectory: File

    @Before
    fun setUp() {
        temporaryDirectory = Files.createTempDirectory("image-exif-test").toFile()
    }

    @After
    fun tearDown() {
        temporaryDirectory.deleteRecursively()
    }

    @Test
    fun extractsOnlyBoundedSafeMetadataAndNeverCopiesLocationContainers() {
        val source = createSourceWithExif()
        val exif = ExifInterface(source.absolutePath)
        val snapshot = ExifMetadataPolicy.snapshot(exif)

        assertEquals(ExifInterface.ORIENTATION_ROTATE_90, snapshot.orientation)
        assertEquals(CAMERA_MAKE, snapshot.preservableMetadata.attributes[ExifInterface.TAG_MAKE])
        assertEquals(CAMERA_MODEL, snapshot.preservableMetadata.attributes[ExifInterface.TAG_MODEL])
        assertEquals(
            CAPTURE_TIME,
            snapshot.preservableMetadata.attributes[ExifInterface.TAG_DATETIME_ORIGINAL],
        )
        assertFalse(snapshot.preservableMetadata.attributes.containsKey(ExifInterface.TAG_ORIENTATION))
        assertFalse(snapshot.preservableMetadata.attributes.containsKey(ExifInterface.TAG_GPS_LATITUDE))
        assertFalse(snapshot.preservableMetadata.attributes.containsKey(ExifInterface.TAG_GPS_LONGITUDE))
        assertFalse(snapshot.preservableMetadata.attributes.containsKey(ExifInterface.TAG_XMP))
        assertFalse(snapshot.preservableMetadata.attributes.containsKey(ExifInterface.TAG_USER_COMMENT))
        assertFalse(snapshot.preservableMetadata.attributes.containsKey(ExifInterface.TAG_ARTIST))
        assertNotNull(exif.latLong)
        assertNotNull(exif.getAttribute(ExifInterface.TAG_XMP))
        assertNotNull(exif.getAttribute(ExifInterface.TAG_USER_COMMENT))
    }

    @Test
    fun safeMetadataRoundTripsAcrossJpegPngAndWebpWithoutGpsOrSourceOrientation() {
        val metadata = sourceMetadata()
        val bitmap = paletteBitmap(96, 64)
        try {
            ImageOutputFormat.entries.forEach { format ->
                val encoded = encode(bitmap, format, metadata = metadata)
                val output = File(temporaryDirectory, "round-trip.${format.extension}")
                FileOutputStream(output).use { it.write(encoded.bytes) }
                val exif = ExifInterface(output.absolutePath)

                assertEquals(format.toString(), CAMERA_MAKE, exif.getAttribute(ExifInterface.TAG_MAKE))
                assertEquals(format.toString(), CAMERA_MODEL, exif.getAttribute(ExifInterface.TAG_MODEL))
                assertEquals(format.toString(), CAPTURE_TIME, exif.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL))
                assertEquals(
                    format.toString(),
                    ExifInterface.ORIENTATION_NORMAL,
                    exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL),
                )
                assertNull(format.toString(), exif.latLong)
                assertNull(format.toString(), exif.getAttribute(ExifInterface.TAG_GPS_ALTITUDE))
                assertNull(format.toString(), exif.getAttribute(ExifInterface.TAG_GPS_DATESTAMP))
                assertNull(format.toString(), exif.getAttribute(ExifInterface.TAG_XMP))
                assertNull(format.toString(), exif.getAttribute(ExifInterface.TAG_USER_COMMENT))
                assertNull(format.toString(), exif.getAttribute(ExifInterface.TAG_ARTIST))
                assertFalse(format.toString(), exif.hasThumbnail())
                assertEquals(encoded.bytes.size.toLong(), encoded.result.encodedBytes)
            }
            assertNoStagedFilesRemain()
        } finally {
            bitmap.recycle()
        }
    }

    @Test
    fun metadataPreservationIsOptInAndDefaultsToNoExifOutput() {
        val bitmap = texturedBitmap(80, 60)
        try {
            val options = ImageConversionOptions(
                format = ImageOutputFormat.JPEG,
                targetSize = ImagePixelSize(bitmap.width, bitmap.height),
            )
            assertNull(options.preservedExifMetadata)
            val output = ByteArrayOutputStream()
            ImageBitmapIO.encodeOutput(
                bitmap = bitmap,
                options = options,
                output = output,
                maxOutputBytes = ImageToolsPlugin.MAX_OUTPUT_BYTES,
            )
            val outputFile = File(temporaryDirectory, "default-off.jpg")
            FileOutputStream(outputFile).use { it.write(output.toByteArray()) }
            val exif = ExifInterface(outputFile.absolutePath)

            assertNull(exif.getAttribute(ExifInterface.TAG_MAKE))
            assertNull(exif.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL))
            assertNull(exif.latLong)
        } finally {
            bitmap.recycle()
        }
    }

    @Test
    fun targetSearchAndHostLimitUseTheFinalFileSizeIncludingExif() {
        val metadata = sourceMetadata()
        val bitmap = texturedBitmap(256, 192)
        try {
            val minimum = encode(
                bitmap,
                ImageOutputFormat.JPEG,
                quality = ImageConversionOptions.MIN_QUALITY,
                metadata = metadata,
            ).bytes.size.toLong()
            val maximum = encode(
                bitmap,
                ImageOutputFormat.JPEG,
                quality = ImageConversionOptions.MAX_QUALITY,
                metadata = metadata,
            ).bytes.size.toLong()
            assertTrue("min=$minimum max=$maximum", maximum > minimum)
            val targetBytes = minimum + (maximum - minimum) / 2L
            val targeted = encode(
                bitmap,
                ImageOutputFormat.JPEG,
                metadata = metadata,
                targetBytes = targetBytes,
            )

            assertTrue("encoded=${targeted.bytes.size} target=$targetBytes", targeted.bytes.size <= targetBytes)
            assertEquals(targeted.bytes.size.toLong(), targeted.result.encodedBytes)
            assertEquals(
                targeted.bytes.size.toLong(),
                requireNotNull(targeted.result.targetFileSizeResult).encodedBytes,
            )

            val plain = encode(bitmap, ImageOutputFormat.JPEG, quality = 80)
            val withExif = encode(bitmap, ImageOutputFormat.JPEG, quality = 80, metadata = metadata)
            assertTrue(withExif.bytes.size > plain.bytes.size)
            val rejectedOutput = ByteArrayOutputStream()
            assertThrows(ImageBitmapIO.OutputLimitExceededException::class.java) {
                ImageBitmapIO.encodeOutput(
                    bitmap = bitmap,
                    options = options(
                        bitmap = bitmap,
                        format = ImageOutputFormat.JPEG,
                        quality = 80,
                        metadata = metadata,
                    ),
                    output = rejectedOutput,
                    maxOutputBytes = withExif.bytes.size.toLong() - 1L,
                    temporaryDirectory = temporaryDirectory,
                )
            }
            assertEquals(0, rejectedOutput.size())
            assertNoStagedFilesRemain()
        } finally {
            bitmap.recycle()
        }
    }

    private fun sourceMetadata(): PreservedExifMetadata = ExifMetadataPolicy.snapshot(
        ExifInterface(createSourceWithExif().absolutePath),
    ).preservableMetadata

    private fun createSourceWithExif(): File {
        val bitmap = texturedBitmap(48, 32)
        val source = File.createTempFile("source-with-exif-", ".jpg", temporaryDirectory)
        try {
            FileOutputStream(source).use { output ->
                assertTrue(bitmap.compress(Bitmap.CompressFormat.JPEG, 92, output))
            }
        } finally {
            bitmap.recycle()
        }
        ExifInterface(source.absolutePath).apply {
            setAttribute(ExifInterface.TAG_MAKE, CAMERA_MAKE)
            setAttribute(ExifInterface.TAG_MODEL, CAMERA_MODEL)
            setAttribute(ExifInterface.TAG_DATETIME_ORIGINAL, CAPTURE_TIME)
            setAttribute(ExifInterface.TAG_COPYRIGHT, "Roadmap fixture")
            setAttribute(ExifInterface.TAG_ARTIST, "A".repeat(9 * 1024))
            setAttribute(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_ROTATE_90.toString())
            setAttribute(ExifInterface.TAG_USER_COMMENT, "location=31.2304,121.4737")
            setAttribute(
                ExifInterface.TAG_XMP,
                "<xmp><exif:GPSLatitude>31.2304</exif:GPSLatitude></xmp>",
            )
            setLatLong(31.2304, 121.4737)
            setAltitude(12.5)
            saveAttributes()
        }
        return source
    }

    private fun encode(
        bitmap: Bitmap,
        format: ImageOutputFormat,
        quality: Int = ImageConversionOptions.DEFAULT_QUALITY,
        metadata: PreservedExifMetadata? = null,
        targetBytes: Long? = null,
    ): EncodedOutput {
        val output = ByteArrayOutputStream()
        val result = ImageBitmapIO.encodeOutput(
            bitmap = bitmap,
            options = options(bitmap, format, quality, metadata, targetBytes),
            output = output,
            maxOutputBytes = ImageToolsPlugin.MAX_OUTPUT_BYTES,
            temporaryDirectory = temporaryDirectory,
        )
        return EncodedOutput(output.toByteArray(), result)
    }

    private fun options(
        bitmap: Bitmap,
        format: ImageOutputFormat,
        quality: Int,
        metadata: PreservedExifMetadata?,
        targetBytes: Long? = null,
    ) = ImageConversionOptions(
        format = format,
        quality = quality,
        targetSize = ImagePixelSize(bitmap.width, bitmap.height),
        targetFileSizeBytes = targetBytes,
        preservedExifMetadata = metadata,
    )

    private fun texturedBitmap(width: Int, height: Int): Bitmap {
        val pixels = IntArray(width * height) { index ->
            val x = index % width
            val y = index / width
            Color.rgb(
                (x * 31 + y * 17 + index) and 0xFF,
                (x * 13 + y * 47 + index * 3) and 0xFF,
                (x * 43 + y * 7 + index * 5) and 0xFF,
            )
        }
        return Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).apply {
            setPixels(pixels, 0, width, 0, 0, width, height)
            setHasAlpha(false)
        }
    }

    private fun paletteBitmap(width: Int, height: Int): Bitmap =
        Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).apply {
            val colors = intArrayOf(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW)
            val pixels = IntArray(width * height) { index ->
                val x = index % width
                val y = index / width
                colors[(x / 8 + y / 8) % colors.size]
            }
            setPixels(pixels, 0, width, 0, 0, width, height)
            setHasAlpha(false)
        }

    private fun assertNoStagedFilesRemain() {
        assertTrue(
            temporaryDirectory.listFiles().orEmpty().none { file ->
                file.name.startsWith("image-tools-output-")
            },
        )
    }

    private data class EncodedOutput(
        val bytes: ByteArray,
        val result: ImageOutputWriteResult,
    )

    companion object {
        private const val CAMERA_MAKE = "AutoJs6 Test Camera"
        private const val CAMERA_MODEL = "Image Tools Fixture"
        private const val CAPTURE_TIME = "2026:08:31 12:34:56"
    }
}
