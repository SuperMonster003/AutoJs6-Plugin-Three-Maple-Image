package io.github.supermonster003.autojs6.plugin.imageviewer

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.ParcelFileDescriptor
import android.view.View
import android.widget.TextView
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import java.io.File
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import org.autojs.plugin.explorer.api.IExplorerActionHostSession
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
                val error = activity.findViewById<TextView>(R.id.error_text)
                assertEquals(View.VISIBLE, error.visibility)
                assertEquals(
                    activity.getString(
                        R.string.error_image_format_requires_android,
                        ModernImageFormat.AVIF.displayLabel,
                        ModernImageFormat.AVIF.minimumAndroidVersion,
                    ),
                    error.text.toString(),
                )
                assertFalse(error.text.toString() == activity.getString(R.string.error_cannot_read_image))
                assertEquals(View.GONE, activity.findViewById<View>(R.id.loading_indicator).visibility)
                assertEquals(
                    activity.getString(R.string.metadata_unavailable),
                    activity.findViewById<TextView>(R.id.metadata).text.toString(),
                )
                assertFalse(
                    activity.findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
                        .menu.findItem(R.id.print_image).isEnabled,
                )
                assertTrue(activity.findViewById<MaterialButton>(R.id.share).isEnabled)
                assertTrue(activity.findViewById<MaterialButton>(R.id.open_external).isEnabled)
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
                activity.findViewById<ZoomableImageView>(R.id.image).onPageSwipe?.invoke(1)
                assertEquals("frame.avif", activity.titleText())
                assertEquals(
                    activity.getString(
                        R.string.error_image_format_requires_android,
                        ModernImageFormat.AVIF.displayLabel,
                        ModernImageFormat.AVIF.minimumAndroidVersion,
                    ),
                    activity.findViewById<TextView>(R.id.error_text).text.toString(),
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

    private fun ImageViewerActivity.titleText(): String =
        findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar).title.toString()

    private class RecordingHostSession(
        private val openCalls: AtomicInteger,
    ) : IExplorerActionHostSession.Stub() {

        override fun listChildren(
            targetId: String?,
            relativePath: String?,
            offset: Int,
            limit: Int,
        ): Bundle = throw UnsupportedOperationException()

        override fun openFile(targetId: String?, relativePath: String?): ParcelFileDescriptor {
            openCalls.incrementAndGet()
            throw AssertionError("Unsupported modern sibling must not be opened")
        }

        override fun prepareOutput(
            displayName: String?,
            mimeType: String?,
            conflictPolicy: Int,
        ): Bundle = throw UnsupportedOperationException()

        override fun openOutput(transactionId: String?): ParcelFileDescriptor =
            throw UnsupportedOperationException()

        override fun commitOutput(transactionId: String?): Bundle = throw UnsupportedOperationException()

        override fun abortOutput(transactionId: String?) = Unit

        override fun close() = Unit

        override fun openPendingOutput(transactionId: String?): ParcelFileDescriptor =
            throw UnsupportedOperationException()

        override fun prepareTargetReplacement(targetId: String?): Bundle =
            throw UnsupportedOperationException()

        override fun prepareOutputTree(displayName: String?, conflictPolicy: Int): Bundle =
            throw UnsupportedOperationException()

        override fun createOutputDirectory(transactionId: String?, relativePath: String?) = Unit

        override fun openOutputFile(
            transactionId: String?,
            relativePath: String?,
        ): ParcelFileDescriptor = throw UnsupportedOperationException()

        override fun queryOutput(transactionId: String?): Bundle = throw UnsupportedOperationException()

        override fun listOutputs(): Bundle = throw UnsupportedOperationException()

        override fun attachClient(clientToken: IBinder?) = Unit

        override fun getPlaybackProgress(targetId: String?, relativePath: String?): Bundle =
            throw UnsupportedOperationException()

        override fun reportPlaybackProgress(
            targetId: String?,
            relativePath: String?,
            positionMillis: Long,
            durationMillis: Long,
            reportState: Int,
        ) = Unit
    }

    private companion object {
        const val LOAD_TIMEOUT_SECONDS = 10L
    }
}
