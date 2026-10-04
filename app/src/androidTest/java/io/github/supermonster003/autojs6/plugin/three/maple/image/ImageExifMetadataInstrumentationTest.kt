package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.autojs.plugin.explorer.api.IExplorerActionHostSession

@RunWith(AndroidJUnit4::class)
class ImageExifMetadataInstrumentationTest {

    @Test
    fun plainJpegKeepsBaseMetadataWithoutInventingExif() {
        withFixture("plain.jpg", ExifJpegTestFixture::writePlainTo) { file ->
            val metadata = readHostMetadata(file)

            assertNotNull(metadata)
            requireNotNull(metadata)
            assertEquals(ExifJpegTestFixture.WIDTH, metadata.width)
            assertEquals(ExifJpegTestFixture.HEIGHT, metadata.height)
            assertEquals(file.length(), metadata.byteSize)
            assertNull(metadata.exif)
            assertEquals(32, metadata.decodedColor?.bitsPerPixel)
            assertTrue(
                metadata.decodedColor?.colorSpaceName?.contains("sRGB", ignoreCase = true) == true,
            )
        }
    }

    @Test
    fun malformedExifFailsSoftWhileTheJpegRemainsReadable() {
        withFixture("malformed-exif.jpg", ExifJpegTestFixture::writeMalformedExifTo) { file ->
            val metadata = readHostMetadata(file)

            assertNotNull(metadata)
            requireNotNull(metadata)
            assertEquals(ExifJpegTestFixture.WIDTH, metadata.width)
            assertEquals(ExifJpegTestFixture.HEIGHT, metadata.height)
            assertNull(metadata.exif)
        }
    }

    @Test
    fun extractsDisplayFieldsAndOnlyRetainsGpsPresence() {
        withFixture("complete-exif.jpg", ExifJpegTestFixture::writeCompleteTo) { file ->
            val exif = requireNotNull(readHostMetadata(file)?.exif)

            assertEquals(ExifJpegTestFixture.CAPTURED_AT, exif.capturedAt)
            assertEquals(ExifJpegTestFixture.MAKE, exif.make)
            assertEquals(ExifJpegTestFixture.MODEL, exif.model)
            assertEquals(0.008, requireNotNull(exif.exposureTimeSeconds), DOUBLE_TOLERANCE)
            assertEquals(1.8, requireNotNull(exif.apertureFNumber), DOUBLE_TOLERANCE)
            assertEquals(200, exif.sensitivityIso)
            assertEquals(4.25, requireNotNull(exif.focalLengthMm), DOUBLE_TOLERANCE)
            assertEquals(6, exif.orientation?.tagValue)
            assertEquals(90, exif.orientation?.rotationDegrees)
            assertFalse(requireNotNull(exif.orientation).mirrored)
            assertTrue(exif.hasGpsMetadata)
        }
    }

    @Test
    fun readsExifThroughThePrivateContentProviderDisplayPath() {
        withFixture("content-provider-exif.jpg", ExifJpegTestFixture::writeCompleteTo) { file ->
            val session = object : IExplorerActionHostSession.Default() {
                override fun openFile(
                    targetId: String?,
                    relativePath: String?,
                ): ParcelFileDescriptor =
                    ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            }
            val seed = ImageViewerLaunchRequest(
                pages = listOf(
                    ImageViewerPage(
                        ImageViewerRequest(
                            Uri.parse("content://image-viewer.test/selected.png"),
                            "selected.png",
                            1L,
                            "image/png",
                        ),
                        hostRelativePath = "",
                    ),
                    ImageViewerPage(
                        ImageViewerRequest(
                            ImageViewerContract.hostImageUri(1),
                            file.name,
                            file.length(),
                            "image/jpeg",
                        ),
                        hostRelativePath = file.name,
                    ),
                ),
                hostSession = session,
                hostTargetId = "content-exif-target",
            )
            val token = HostSessionImageRegistry.register(seed)
            try {
                val request = ImageViewerRequest(
                    HostSessionImageRegistry.imageUri(token, 1),
                    file.name,
                    file.length(),
                    "image/jpeg",
                )
                val context = InstrumentationRegistry.getInstrumentation().targetContext
                val directExif = context.contentResolver.openFileDescriptor(request.targetUri, "r")
                    ?.use { descriptor -> ImageExifMetadataReader.read(descriptor.fileDescriptor) }

                assertEquals(ExifJpegTestFixture.MAKE, directExif?.make)

                val metadata = ImageContentValidator.readDisplayMetadata(
                    context.contentResolver,
                    request,
                )

                assertEquals(ExifJpegTestFixture.MAKE, metadata?.exif?.make)
                assertTrue(requireNotNull(metadata?.exif).hasGpsMetadata)
            } finally {
                HostSessionImageRegistry.unregister(token)
            }
        }
    }

    private fun readHostMetadata(file: File): ImageMetadata? {
        val request = ImageViewerRequest(
            targetUri = Uri.parse("content://image-viewer.test/${file.name}"),
            displayName = file.name,
            declaredSize = file.length(),
            mimeType = "image/jpeg",
        )
        return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY).use { descriptor ->
            ImageContentValidator.readHostMetadata(descriptor, request)
        }
    }

    private fun withFixture(
        name: String,
        write: (File) -> Unit,
        test: (File) -> Unit,
    ) {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val file = File(context.cacheDir, name)
        try {
            write(file)
            test(file)
        } finally {
            file.delete()
        }
    }

    private companion object {
        const val DOUBLE_TOLERANCE = 0.0001
    }
}
