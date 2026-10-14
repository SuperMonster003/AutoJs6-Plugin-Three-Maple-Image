package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.net.Uri
import android.os.Build
import android.os.ParcelFileDescriptor
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4
import com.bumptech.glide.Glide
import java.io.File
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ModernImageFormatInstrumentationTest {

    @Test
    fun platformAvailabilityUsesTheDocumentedAndroidBaselines() {
        val heif = request("camera.heic", "image/heic")
        val avif = request("frame.avif", "image/avif")

        if (Build.VERSION.SDK_INT < ModernImageFormat.HEIF.minimumSdk) {
            assertVersionFailure(heif, ModernImageFormat.HEIF)
        }
        if (Build.VERSION.SDK_INT < ModernImageFormat.AVIF.minimumSdk) {
            assertVersionFailure(avif, ModernImageFormat.AVIF)
        }
    }

    @Test
    fun decodesRealHeifOnAndroidNineAndLater() {
        assumeTrue(Build.VERSION.SDK_INT >= ModernImageFormat.HEIF.minimumSdk)
        withFixture("modern-format.heic", ModernImageTestFixture::writeHeifTo) { file ->
            assertPlatformDecode(file, "image/heic")
        }
    }

    @Test
    fun decodesRealAvifOnAndroidTwelveAndLater() {
        assumeTrue(Build.VERSION.SDK_INT >= ModernImageFormat.AVIF.minimumSdk)
        withFixture("modern-format.avif", ModernImageTestFixture::writeAvifTo) { file ->
            assertPlatformDecode(file, "image/avif")
        }
    }

    @Test
    fun oldAndroidShowsTheMinimumVersionInsteadOfAGenericLoadFailure() {
        assumeTrue(Build.VERSION.SDK_INT < ModernImageFormat.AVIF.minimumSdk)
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val request = ImageViewerLaunchRequest.single(request("frame.avif", "image/avif"))

        ActivityScenario.launchActivityForResult<ImageViewerActivity>(
            ImageViewerContract.viewerIntent(context, request),
        ).use { scenario ->
            scenario.onActivity { activity ->
                val state = activity.viewerStateForTesting
                val error = requireNotNull(state.errorText)
                assertEquals(
                    activity.getString(
                        R.string.error_image_format_requires_android,
                        ModernImageFormat.AVIF.displayLabel,
                        ModernImageFormat.AVIF.minimumAndroidVersion,
                    ),
                    error,
                )
                assertFalse(error == activity.getString(R.string.error_cannot_read_image))
                assertFalse(state.loading)
                assertEquals(activity.getString(R.string.metadata_unavailable), state.metadataText)
                assertFalse(state.printEnabled)
                assertTrue(state.canForward)
            }
        }
    }

    @Test
    fun unsupportedSiblingDoesNotOpenTheHostDescriptor() {
        assumeTrue(Build.VERSION.SDK_INT < ModernImageFormat.AVIF.minimumSdk)
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val openCalls = AtomicInteger()
        val session = RecordingHostSession(openCalls)
        val launch = ImageViewerLaunchRequest(
            pages = listOf(
                ImageViewerPage(
                    image = request("selected.png", "image/png"),
                    hostRelativePath = "",
                ),
                ImageViewerPage(
                    image = ImageViewerRequest(
                        ImageViewerContract.hostImageUri(1),
                        "frame.avif",
                        1L,
                        "image/avif",
                    ),
                    hostRelativePath = "frame.avif",
                ),
            ),
            hostSession = session,
            hostTargetId = "modern-format-test",
        )

        ActivityScenario.launchActivityForResult<ImageViewerActivity>(
            ImageViewerContract.viewerIntent(context, launch),
        ).use { scenario ->
            scenario.onActivity { activity ->
                requireNotNull(activity.currentImageViewForTesting()).onPageSwipe?.invoke(1)
                assertEquals("frame.avif", activity.titleText())
                assertEquals(
                    activity.getString(
                        R.string.error_image_format_requires_android,
                        ModernImageFormat.AVIF.displayLabel,
                        ModernImageFormat.AVIF.minimumAndroidVersion,
                    ),
                    activity.viewerStateForTesting.errorText,
                )
            }
        }
        assertEquals(0, openCalls.get())
    }

    private fun assertPlatformDecode(file: File, mimeType: String) {
        val request = ImageViewerRequest(
            Uri.parse("content://image-viewer.test/${file.name}"),
            file.name,
            file.length(),
            mimeType,
        )
        val metadata = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY).use {
            descriptor -> ImageContentValidator.readHostMetadata(descriptor, request)
        }
        assertNotNull(metadata)
        requireNotNull(metadata)
        assertTrue(requireNotNull(metadata.width) > 0)
        assertTrue(requireNotNull(metadata.height) > 0)
        assertNull(ImagePlatformDecodeSupport.unsupported(request))

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val future = Glide.with(context).asDrawable().load(file).submit()
        try {
            assertNotNull(future.get(LOAD_TIMEOUT_SECONDS, TimeUnit.SECONDS))
        } finally {
            Glide.with(context).clear(future)
        }
    }

    private fun assertVersionFailure(
        request: ImageViewerRequest,
        expectedFormat: ModernImageFormat,
    ) {
        val failure = requireNotNull(ImagePlatformDecodeSupport.unsupported(request))
        assertEquals(expectedFormat, failure.format)
        assertEquals(ModernImageUnavailableReason.ANDROID_VERSION, failure.reason)
    }

    private fun request(displayName: String, mimeType: String) = ImageViewerRequest(
        targetUri = Uri.parse("content://image-viewer.test/$displayName"),
        displayName = displayName,
        declaredSize = 1L,
        mimeType = mimeType,
    )

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

    private fun ImageViewerActivity.titleText(): String = viewerStateForTesting.title

    private class RecordingHostSession(
        private val openCalls: AtomicInteger,
    ) : UnsupportedHostSessionStub() {

        override fun openFile(targetId: String?, relativePath: String?): ParcelFileDescriptor {
            openCalls.incrementAndGet()
            throw AssertionError("Unsupported modern sibling must not be opened")
        }
    }

    private companion object {
        const val LOAD_TIMEOUT_SECONDS = 10L
    }
}
