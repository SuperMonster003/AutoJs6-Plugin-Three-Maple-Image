package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.RectF
import android.net.Uri
import android.os.Build
import android.os.ParcelFileDescriptor
import android.os.SystemClock
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.abs
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TiledImageDrawableInstrumentationTest {

    @Test
    fun platformRegionDecoderAcceptsARealStaticHeicOnAndroidNineAndLater() {
        assumeTrue("HEIC decoding starts with Android 9", Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val file = File(context.cacheDir, "image-viewer-tiled-static.heic")
        ModernImageTestFixture.writeHeifTo(file)
        try {
            val drawable = requireNotNull(
                TiledImageDrawable.create(
                    descriptor = ParcelFileDescriptor.open(
                        file,
                        ParcelFileDescriptor.MODE_READ_ONLY,
                    ),
                    orientation = null,
                    cacheBudgetBytes = TILE_CACHE_BYTES,
                ),
            )
            try {
                assertTrue(drawable.intrinsicWidth > 0)
                assertTrue(drawable.intrinsicHeight > 0)
                assertTrue(drawable.previewWidthForTesting > 0)
                assertTrue(drawable.previewHeightForTesting > 0)
            } finally {
                instrumentation.runOnMainSync { drawable.close() }
            }
        } finally {
            file.delete()
        }
    }

    @Test
    fun tiledDrawableNormalizesExifOrientationBeforeManualViewRotation() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val file = File(context.cacheDir, "image-viewer-tiled-exif-transpose.jpg")
        ExifJpegTestFixture.writeOrientedPatternTo(file, ExifInterface.ORIENTATION_TRANSPOSE)
        try {
            val request = request(
                Uri.parse("content://image-viewer.test/${file.name}"),
                file.name,
                file.length(),
            ).copy(mimeType = "image/jpeg")
            val metadata = ParcelFileDescriptor.open(
                file,
                ParcelFileDescriptor.MODE_READ_ONLY,
            ).use { descriptor -> ImageContentValidator.readHostMetadata(descriptor, request) }
            val drawable = requireNotNull(
                TiledImageDrawable.create(
                    descriptor = ParcelFileDescriptor.open(
                        file,
                        ParcelFileDescriptor.MODE_READ_ONLY,
                    ),
                    orientation = requireNotNull(metadata?.exif?.orientation),
                    cacheBudgetBytes = TILE_CACHE_BYTES,
                ),
            )
            try {
                assertEquals(ExifJpegTestFixture.PATTERN_HEIGHT, drawable.intrinsicWidth)
                assertEquals(ExifJpegTestFixture.PATTERN_WIDTH, drawable.intrinsicHeight)
                val rendered = Bitmap.createBitmap(
                    drawable.intrinsicWidth,
                    drawable.intrinsicHeight,
                    Bitmap.Config.ARGB_8888,
                )
                try {
                    instrumentation.runOnMainSync {
                        drawable.bounds = android.graphics.Rect(0, 0, rendered.width, rendered.height)
                        drawable.draw(Canvas(rendered))
                    }
                    val actualCorners = listOf(
                        rendered.getPixel(rendered.width / 4, rendered.height / 4),
                        rendered.getPixel(rendered.width * 3 / 4, rendered.height / 4),
                        rendered.getPixel(rendered.width / 4, rendered.height * 3 / 4),
                        rendered.getPixel(rendered.width * 3 / 4, rendered.height * 3 / 4),
                    )
                    val expectedCorners = listOf(
                        ExifJpegTestFixture.TOP_LEFT_COLOR,
                        ExifJpegTestFixture.BOTTOM_LEFT_COLOR,
                        ExifJpegTestFixture.TOP_RIGHT_COLOR,
                        ExifJpegTestFixture.BOTTOM_RIGHT_COLOR,
                    )
                    expectedCorners.zip(actualCorners).forEach { (expected, actual) ->
                        assertColorNear(expected, actual, JPEG_COLOR_TOLERANCE)
                    }
                } finally {
                    rendered.recycle()
                }
            } finally {
                instrumentation.runOnMainSync { drawable.close() }
            }
        } finally {
            file.delete()
        }
    }

    @Test
    fun realLargePngUsesBoundedTilesAcrossZoomRotationAndHostPaging() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val file = File(context.cacheDir, "image-viewer-large-tiled.png")
        LargePngTestFixture.writeTo(file)
        try {
            assertTrue(file.length() in 1L until MAX_FIXTURE_BYTES)
            verifyDrawablePipeline(file)
            verifyActivityAndHostPaging(file)
        } finally {
            file.delete()
        }
    }

    private fun verifyDrawablePipeline(file: File) {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val descriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
        val drawable = requireNotNull(
            TiledImageDrawable.create(
                descriptor = descriptor,
                orientation = null,
                cacheBudgetBytes = TILE_CACHE_BYTES,
            ),
        )
        try {
            assertEquals(LargePngTestFixture.WIDTH, drawable.intrinsicWidth)
            assertEquals(LargePngTestFixture.HEIGHT, drawable.intrinsicHeight)
            assertEquals(4, drawable.previewSampleSizeForTesting)
            assertTrue(drawable.previewWidthForTesting <= ImageLargeImagePolicy.PREVIEW_MAX_EDGE)
            assertTrue(drawable.previewHeightForTesting <= ImageLargeImagePolicy.PREVIEW_MAX_EDGE)

            instrumentation.runOnMainSync {
                drawable.updateViewport(
                    displayMatrix = Matrix().apply { setScale(0.5f, 0.5f) },
                    contentBounds = RectF(0f, 0f, 1_000f, 800f),
                )
            }
            awaitCondition("sharper sample-2 tiles") {
                var ready = false
                instrumentation.runOnMainSync {
                    ready = drawable.lastRequestedSampleSizeForTesting == 2 &&
                        drawable.loadedTileCountForTesting > 0
                }
                ready
            }

            val rendered = Bitmap.createBitmap(1_000, 800, Bitmap.Config.ARGB_8888)
            instrumentation.runOnMainSync {
                drawable.bounds = android.graphics.Rect(0, 0, rendered.width, rendered.height)
                drawable.draw(Canvas(rendered))
            }
            try {
                assertColorNear(Color.rgb(230, 30, 40), rendered.getPixel(250, 200), PNG_COLOR_TOLERANCE)
                assertColorNear(Color.rgb(30, 70, 230), rendered.getPixel(750, 200), PNG_COLOR_TOLERANCE)
                assertColorNear(Color.rgb(30, 210, 70), rendered.getPixel(250, 600), PNG_COLOR_TOLERANCE)
                assertColorNear(Color.rgb(235, 205, 35), rendered.getPixel(750, 600), PNG_COLOR_TOLERANCE)
            } finally {
                rendered.recycle()
            }

            // A decode still in flight would put its tile back after the trim (slow emulators under load).
            awaitCondition("tile decodes settle") {
                var settled = false
                instrumentation.runOnMainSync { settled = drawable.pendingTileCountForTesting == 0 }
                settled
            }
            instrumentation.runOnMainSync { drawable.trimTileCache() }
            assertEquals(0, drawable.loadedTileCountForTesting)
        } finally {
            instrumentation.runOnMainSync { drawable.close() }
        }
        assertTrue(drawable.isClosedForTesting)
    }

    private fun verifyActivityAndHostPaging(file: File) {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val session = RecordingHostSession(file)
        val registrySeed = ImageViewerLaunchRequest(
            pages = listOf(
                ImageViewerPage(
                    image = request(
                        Uri.parse("content://image-viewer.test/selected.png"),
                        "selected.png",
                        1L,
                    ),
                    hostRelativePath = "",
                ),
                ImageViewerPage(
                    image = request(
                        ImageViewerContract.hostImageUri(1),
                        file.name,
                        file.length(),
                    ),
                    hostRelativePath = file.name,
                ),
            ),
            hostSession = session,
            hostTargetId = "large-registry-seed",
        )
        val registryToken = HostSessionImageRegistry.register(registrySeed)
        var firstDrawable: TiledImageDrawable? = null
        var secondDrawable: TiledImageDrawable? = null
        try {
            val selectedSource = HostSessionImageRegistry.imageUri(registryToken, 1)
            val launchRequest = ImageViewerLaunchRequest(
                pages = listOf(
                    ImageViewerPage(
                        image = request(
                            selectedSource,
                            SELECTED_NAME,
                            file.length(),
                        ),
                        hostRelativePath = "",
                    ),
                    ImageViewerPage(
                        image = request(
                            ImageViewerContract.hostImageUri(1),
                            SIBLING_NAME,
                            file.length(),
                        ),
                        hostRelativePath = SIBLING_NAME,
                    ),
                ),
                hostSession = session,
                hostTargetId = "large-viewer-target",
            )
            ActivityScenario.launchActivityForResult<ImageViewerActivity>(
                ImageViewerContract.viewerIntent(context, launchRequest),
            ).use { scenario ->
                awaitActivityTiledPage(scenario, SELECTED_NAME)
                scenario.onActivity { activity ->
                    val image = requireNotNull(activity.currentImageViewForTesting())
                    firstDrawable = image.drawable as TiledImageDrawable
                    assertTrue(image.toggleDoubleTapZoom(image.width / 2f, image.height / 2f))
                    assertEquals(ImageZoomState.DOUBLE_TAP_ZOOM, image.currentZoomForTesting)
                }
                awaitCondition("activity high-resolution tiles") {
                    var ready = false
                    scenario.onActivity { activity ->
                        val drawable = activity.currentImageViewForTesting()
                            ?.drawable as? TiledImageDrawable
                        ready = drawable != null &&
                            drawable.lastRequestedSampleSizeForTesting <
                            drawable.previewSampleSizeForTesting &&
                            drawable.loadedTileCountForTesting > 0
                    }
                    ready
                }
                scenario.onActivity { activity ->
                    val image = requireNotNull(activity.currentImageViewForTesting())
                    activity.rotateForTesting()
                    assertEquals(90, image.currentRotationDegreesForTesting)
                    assertEquals(ImageZoomState.DOUBLE_TAP_ZOOM, image.currentZoomForTesting)
                    image.onPageSwipe?.invoke(ZoomableImageView.DIRECTION_NEXT)
                }

                awaitActivityTiledPage(scenario, SIBLING_NAME)
                scenario.onActivity { activity ->
                    secondDrawable = requireNotNull(activity.currentImageViewForTesting())
                        .drawable as TiledImageDrawable
                    assertNotSame(firstDrawable, secondDrawable)
                    assertTrue(requireNotNull(firstDrawable).isClosedForTesting)
                    assertEquals(0, requireNotNull(activity.currentImageViewForTesting()).currentRotationQuarterTurns)
                }
                assertTrue(session.openedPaths.count { it == SIBLING_NAME } >= 2)
            }
            assertTrue(requireNotNull(secondDrawable).isClosedForTesting)
            assertTrue(session.closeCount > 0)
        } finally {
            HostSessionImageRegistry.unregister(registryToken)
        }
    }

    private fun awaitActivityTiledPage(
        scenario: ActivityScenario<ImageViewerActivity>,
        expectedName: String,
    ) {
        awaitCondition("tiled Activity page $expectedName") {
            var ready = false
            scenario.onActivity { activity ->
                val image = activity.currentImageViewForTesting()
                val metadata = activity.viewerStateForTesting.metadataText
                ready = activity.viewerStateForTesting.printEnabled &&
                    image?.drawable is TiledImageDrawable &&
                    activity.titleText() == expectedName &&
                    metadata.contains("${LargePngTestFixture.WIDTH}") &&
                    metadata.contains("${LargePngTestFixture.HEIGHT}")
            }
            ready
        }
    }

    private fun awaitCondition(label: String, condition: () -> Boolean) {
        val deadline = SystemClock.uptimeMillis() + LOAD_TIMEOUT_MILLIS
        var complete = condition()
        while (!complete && SystemClock.uptimeMillis() < deadline) {
            SystemClock.sleep(POLL_INTERVAL_MILLIS)
            complete = condition()
        }
        assertTrue("Timed out waiting for $label", complete)
    }

    private fun request(
        uri: Uri,
        displayName: String,
        declaredSize: Long,
    ): ImageViewerRequest = ImageViewerRequest(
        targetUri = uri,
        displayName = displayName,
        declaredSize = declaredSize,
        mimeType = "image/png",
    )

    private fun assertColorNear(expected: Int, actual: Int, tolerance: Int) {
        assertTrue(
            "Expected ${describeColor(expected)} but was ${describeColor(actual)}",
            abs(Color.red(expected) - Color.red(actual)) <= tolerance &&
                abs(Color.green(expected) - Color.green(actual)) <= tolerance &&
                abs(Color.blue(expected) - Color.blue(actual)) <= tolerance,
        )
    }

    private fun describeColor(color: Int): String =
        "rgb(${Color.red(color)},${Color.green(color)},${Color.blue(color)})"

    private fun ImageViewerActivity.titleText(): String = viewerStateForTesting.title

    private class RecordingHostSession(
        private val file: File,
    ) : UnsupportedHostSessionStub() {

        val openedPaths = CopyOnWriteArrayList<String>()

        @Volatile
        var closeCount = 0
            private set

        override fun openFile(targetId: String?, relativePath: String?): ParcelFileDescriptor {
            openedPaths += relativePath.orEmpty()
            return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
        }

        override fun close() {
            closeCount += 1
        }
    }

    private companion object {
        const val SELECTED_NAME = "selected-large.png"
        const val SIBLING_NAME = "sibling-large.png"
        // Generous for slow CI emulators: decoding the 20 MP fixture through the host session can take long there.
        const val LOAD_TIMEOUT_MILLIS = 60_000L
        const val POLL_INTERVAL_MILLIS = 50L
        const val TILE_CACHE_BYTES = 16L * 1_048_576L
        const val MAX_FIXTURE_BYTES = 2L * 1_048_576L
        const val PNG_COLOR_TOLERANCE = 8
        const val JPEG_COLOR_TOLERANCE = 48
    }
}
