@file:Suppress("DEPRECATION")

package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.os.SystemClock
import android.widget.TextView
import androidx.exifinterface.media.ExifInterface
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import kotlin.math.roundToInt
import org.autojs.plugin.explorer.api.IExplorerActionHostSession
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ImageExifOrientationInstrumentationTest {

    @Test
    fun glideNormalizesPixelsAndMetadataForAllEightExifOrientations() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext

        ORIENTATION_CASES.forEach { orientationCase ->
            val file = File(context.cacheDir, "exif-orientation-${orientationCase.tagValue}.jpg")
            try {
                ExifJpegTestFixture.writeOrientedPatternTo(file, orientationCase.tagValue)
                val metadata = readHostMetadata(file)
                val exifOrientation = requireNotNull(metadata?.exif?.orientation)

                assertEquals(orientationCase.tagValue, exifOrientation.tagValue)
                assertEquals(orientationCase.rotationDegrees, exifOrientation.rotationDegrees)
                assertEquals(orientationCase.mirrored, exifOrientation.mirrored)
                assertEquals(orientationCase.displayWidth, metadata.width)
                assertEquals(orientationCase.displayHeight, metadata.height)

                val target = Glide.with(context)
                    .asBitmap()
                    .load(file)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .disallowHardwareConfig()
                    .submit()
                try {
                    val decoded = target.get(LOAD_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    assertEquals(orientationCase.displayWidth, decoded.width)
                    assertEquals(orientationCase.displayHeight, decoded.height)
                    assertCornerColors(orientationCase.decodedCorners, bitmapCornerColors(decoded))
                } finally {
                    instrumentation.runOnMainSync { Glide.with(context).clear(target) }
                }
            } finally {
                file.delete()
            }
        }
    }

    @Test
    fun viewerComposesExifMirrorWithManualRotationAndRestoresOnlyTheManualTurn() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val orientationCase = ORIENTATION_CASES.single {
            it.tagValue == ExifInterface.ORIENTATION_TRANSPOSE
        }
        val file = File(context.cacheDir, "image-viewer-exif-transpose.jpg")
        ExifJpegTestFixture.writeOrientedPatternTo(file, orientationCase.tagValue)
        val session = object : IExplorerActionHostSession.Default() {
            override fun openFile(targetId: String?, relativePath: String?): ParcelFileDescriptor =
                ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
        }
        val seed = ImageViewerLaunchRequest(
            pages = listOf(
                ImageViewerPage(
                    image = ImageViewerRequest(
                        Uri.parse("content://image-viewer.test/selected.png"),
                        "selected.png",
                        1L,
                        "image/png",
                    ),
                    hostRelativePath = "",
                ),
                ImageViewerPage(
                    image = ImageViewerRequest(
                        ImageViewerContract.hostImageUri(1),
                        file.name,
                        file.length(),
                        "image/jpeg",
                    ),
                    hostRelativePath = file.name,
                ),
            ),
            hostSession = session,
            hostTargetId = "exif-orientation-target",
        )
        val token = HostSessionImageRegistry.register(seed)
        try {
            val source = HostSessionImageRegistry.imageUri(token, 1)
            val request = ImageViewerLaunchRequest.single(
                ImageViewerRequest(source, file.name, file.length(), "image/jpeg"),
            )
            ActivityScenario.launchActivityForResult<ImageViewerActivity>(
                ImageViewerContract.viewerIntent(context, request),
            ).use { scenario ->
                awaitViewerOrientation(scenario, expectedManualDegrees = 0)
                scenario.onActivity { activity ->
                    val image = activity.findViewById<ZoomableImageView>(R.id.image)
                    val decoded = requireBitmap(image)
                    assertCornerColors(orientationCase.decodedCorners, bitmapCornerColors(decoded))
                    assertCornerColors(
                        orientationCase.decodedCorners,
                        displayedCornerColors(image, decoded),
                    )

                    activity.findViewById<MaterialButton>(R.id.rotate_clockwise).performClick()

                    assertEquals(90, image.currentRotationDegreesForTesting)
                    assertCornerColors(
                        rotateCornersClockwise(orientationCase.decodedCorners),
                        displayedCornerColors(image, decoded),
                    )
                }

                scenario.recreate()

                awaitViewerOrientation(scenario, expectedManualDegrees = 90)
                scenario.onActivity { activity ->
                    val image = activity.findViewById<ZoomableImageView>(R.id.image)
                    val decoded = requireBitmap(image)
                    assertCornerColors(orientationCase.decodedCorners, bitmapCornerColors(decoded))
                    assertCornerColors(
                        rotateCornersClockwise(orientationCase.decodedCorners),
                        displayedCornerColors(image, decoded),
                    )

                    activity.findViewById<MaterialToolbar>(R.id.toolbar)
                        .menu.performIdentifierAction(R.id.reset_zoom, 0)

                    assertEquals(0, image.currentRotationDegreesForTesting)
                    assertCornerColors(
                        orientationCase.decodedCorners,
                        displayedCornerColors(image, decoded),
                    )
                }
            }
        } finally {
            HostSessionImageRegistry.unregister(token)
            file.delete()
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

    private fun awaitViewerOrientation(
        scenario: ActivityScenario<ImageViewerActivity>,
        expectedManualDegrees: Int,
    ) {
        val deadline = SystemClock.uptimeMillis() + LOAD_TIMEOUT_MILLIS
        var ready = false
        var observedState = "not observed"
        while (!ready && SystemClock.uptimeMillis() < deadline) {
            scenario.onActivity { activity ->
                val image = activity.findViewById<ZoomableImageView>(R.id.image)
                val bitmap = (image.drawable as? BitmapDrawable)?.bitmap
                val metadataText = activity.findViewById<TextView>(R.id.metadata).text
                val printEnabled = activity.findViewById<MaterialToolbar>(R.id.toolbar)
                    .menu.findItem(R.id.print_image).isEnabled
                val expectedResolution = activity.getString(
                    R.string.metadata_resolution,
                    ExifJpegTestFixture.PATTERN_HEIGHT,
                    ExifJpegTestFixture.PATTERN_WIDTH,
                )
                observedState = "bitmap=${bitmap?.width}x${bitmap?.height}, " +
                    "view=${image.width}x${image.height}, " +
                    "manualRotation=${image.currentRotationDegreesForTesting}, " +
                    "printEnabled=$printEnabled, " +
                    "metadata=$metadataText"
                ready = bitmap != null &&
                    bitmap.height > bitmap.width &&
                    image.width > 0 &&
                    image.height > 0 &&
                    image.currentRotationDegreesForTesting == expectedManualDegrees &&
                    printEnabled &&
                    metadataText.contains(expectedResolution)
            }
            if (!ready) SystemClock.sleep(POLL_INTERVAL_MILLIS)
        }
        assertTrue("Timed out waiting for the EXIF-normalized image: $observedState", ready)
    }

    private fun requireBitmap(image: ZoomableImageView): Bitmap {
        val drawable = image.drawable
        assertTrue("Expected a BitmapDrawable but was ${drawable?.javaClass?.name}", drawable is BitmapDrawable)
        return (drawable as BitmapDrawable).bitmap.also { assertNotNull(it) }
    }

    private fun bitmapCornerColors(bitmap: Bitmap): List<Int> = listOf(
        bitmap.getPixel(bitmap.width / 4, bitmap.height / 4),
        bitmap.getPixel(bitmap.width * 3 / 4, bitmap.height / 4),
        bitmap.getPixel(bitmap.width / 4, bitmap.height * 3 / 4),
        bitmap.getPixel(bitmap.width * 3 / 4, bitmap.height * 3 / 4),
    )

    private fun displayedCornerColors(view: ZoomableImageView, bitmap: Bitmap): List<Int> {
        val displayedBounds = RectF(0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat())
            .also(view.imageMatrix::mapRect)
        val displayedPoints = floatArrayOf(
            displayedBounds.left + displayedBounds.width() / 4f,
            displayedBounds.top + displayedBounds.height() / 4f,
            displayedBounds.left + displayedBounds.width() * 3f / 4f,
            displayedBounds.top + displayedBounds.height() / 4f,
            displayedBounds.left + displayedBounds.width() / 4f,
            displayedBounds.top + displayedBounds.height() * 3f / 4f,
            displayedBounds.left + displayedBounds.width() * 3f / 4f,
            displayedBounds.top + displayedBounds.height() * 3f / 4f,
        )
        val inverse = Matrix()
        assertTrue(view.imageMatrix.invert(inverse))
        inverse.mapPoints(displayedPoints)
        return displayedPoints.asList().chunked(2).map { (x, y) ->
            bitmap.getPixel(
                x.roundToInt().coerceIn(0, bitmap.width - 1),
                y.roundToInt().coerceIn(0, bitmap.height - 1),
            )
        }
    }

    private fun rotateCornersClockwise(corners: List<Int>): List<Int> = listOf(
        corners[2],
        corners[0],
        corners[3],
        corners[1],
    )

    private fun assertCornerColors(expected: List<Int>, actual: List<Int>) {
        assertEquals(expected.size, actual.size)
        expected.zip(actual).forEachIndexed { index, (expectedColor, actualColor) ->
            assertTrue(
                "Corner $index expected ${colorDescription(expectedColor)} but was " +
                    colorDescription(actualColor),
                abs(Color.red(expectedColor) - Color.red(actualColor)) <= COLOR_TOLERANCE &&
                    abs(Color.green(expectedColor) - Color.green(actualColor)) <= COLOR_TOLERANCE &&
                    abs(Color.blue(expectedColor) - Color.blue(actualColor)) <= COLOR_TOLERANCE,
            )
        }
    }

    private fun colorDescription(color: Int): String =
        "rgb(${Color.red(color)},${Color.green(color)},${Color.blue(color)})"

    private data class OrientationCase(
        val tagValue: Int,
        val rotationDegrees: Int,
        val mirrored: Boolean,
        val displayWidth: Int,
        val displayHeight: Int,
        val decodedCorners: List<Int>,
    )

    private companion object {
        private val TL = ExifJpegTestFixture.TOP_LEFT_COLOR
        private val TR = ExifJpegTestFixture.TOP_RIGHT_COLOR
        private val BL = ExifJpegTestFixture.BOTTOM_LEFT_COLOR
        private val BR = ExifJpegTestFixture.BOTTOM_RIGHT_COLOR

        private val ORIENTATION_CASES = listOf(
            OrientationCase(ExifInterface.ORIENTATION_NORMAL, 0, false, 80, 48, listOf(TL, TR, BL, BR)),
            OrientationCase(ExifInterface.ORIENTATION_FLIP_HORIZONTAL, 0, true, 80, 48, listOf(TR, TL, BR, BL)),
            OrientationCase(ExifInterface.ORIENTATION_ROTATE_180, 180, false, 80, 48, listOf(BR, BL, TR, TL)),
            OrientationCase(ExifInterface.ORIENTATION_FLIP_VERTICAL, 180, true, 80, 48, listOf(BL, BR, TL, TR)),
            OrientationCase(ExifInterface.ORIENTATION_TRANSPOSE, 270, true, 48, 80, listOf(TL, BL, TR, BR)),
            OrientationCase(ExifInterface.ORIENTATION_ROTATE_90, 90, false, 48, 80, listOf(BL, TL, BR, TR)),
            OrientationCase(ExifInterface.ORIENTATION_TRANSVERSE, 90, true, 48, 80, listOf(BR, TR, BL, TL)),
            OrientationCase(ExifInterface.ORIENTATION_ROTATE_270, 270, false, 48, 80, listOf(TR, BR, TL, BL)),
        )

        private const val LOAD_TIMEOUT_SECONDS = 10L
        private const val LOAD_TIMEOUT_MILLIS = 10_000L
        private const val POLL_INTERVAL_MILLIS = 50L
        private const val COLOR_TOLERANCE = 48
    }
}
