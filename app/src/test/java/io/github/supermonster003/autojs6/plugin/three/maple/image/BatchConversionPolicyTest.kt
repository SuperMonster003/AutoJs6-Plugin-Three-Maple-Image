package io.github.supermonster003.autojs6.plugin.three.maple.image

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class BatchConversionPolicyTest {

    private val landscape = ImageBitmapIO.SourceInfo(
        rawSize = ImagePixelSize(4000, 3000),
        displaySize = ImagePixelSize(4000, 3000),
        mimeType = "image/jpeg",
        orientation = 1,
    )

    private fun plan(
        settings: BatchConversionSettings,
        source: ImageBitmapIO.SourceInfo = landscape,
        sdkInt: Int = 33,
        withinMemoryBudget: (ImagePixelSize) -> Boolean = { true },
    ) = BatchConversionPolicy.plan(settings, source, sdkInt, withinMemoryBudget)

    private fun ready(settings: BatchConversionSettings, source: ImageBitmapIO.SourceInfo = landscape, sdkInt: Int = 33) =
        plan(settings, source, sdkInt) as BatchItemPlan.Ready

    @Test
    fun originalSizeKeepsEachImageAsItIs() {
        val plan = ready(BatchConversionSettings(format = ImageOutputFormat.WEBP, quality = 80))
        assertEquals(ImagePixelSize(4000, 3000), plan.decodeSize)
        assertEquals(ImagePixelSize(4000, 3000), plan.options.targetSize)
        assertEquals(ImageOutputFormat.WEBP, plan.options.format)
        assertEquals(80, plan.options.quality)
        assertEquals(0, plan.rotationDegrees)
    }

    @Test
    fun percentageAndLongEdgeScaleImagesOfAnyShape() {
        val half = ready(BatchConversionSettings(resizeMode = ImageResizeMode.PERCENTAGE, percentage = " 50 "))
        assertEquals(ImagePixelSize(2000, 1500), half.options.targetSize)

        val portrait = landscape.copy(rawSize = ImagePixelSize(3000, 4000), displaySize = ImagePixelSize(3000, 4000))
        val longEdge = BatchConversionSettings(resizeMode = ImageResizeMode.LONG_EDGE, longEdge = "1000")
        assertEquals(ImagePixelSize(1000, 750), ready(longEdge).options.targetSize)
        assertEquals(ImagePixelSize(750, 1000), ready(longEdge, portrait).options.targetSize)

        // A long edge above the image never enlarges it.
        val small = landscape.copy(rawSize = ImagePixelSize(800, 600), displaySize = ImagePixelSize(800, 600))
        assertEquals(ImagePixelSize(800, 600), ready(longEdge, small).options.targetSize)
    }

    @Test
    fun quarterTurnsSwapTheOutputButNotTheDecodedSize() {
        for (degrees in listOf(90, 270)) {
            val plan = ready(BatchConversionSettings(rotationDegrees = degrees, resizeMode = ImageResizeMode.PERCENTAGE, percentage = "10"))
            assertEquals(ImagePixelSize(400, 300), plan.decodeSize)
            assertEquals(ImagePixelSize(300, 400), plan.options.targetSize)
            assertEquals(degrees, plan.rotationDegrees)
        }
        val halfTurn = ready(BatchConversionSettings(rotationDegrees = 180))
        assertEquals(ImagePixelSize(4000, 3000), halfTurn.options.targetSize)
    }

    @Test(expected = IllegalArgumentException::class)
    fun rotationMustBeAQuarterTurn() {
        plan(BatchConversionSettings(rotationDegrees = 45))
    }

    @Test
    fun imagesTooLargeOrOverTheMemoryBudgetAreRefusedOneByOne() {
        val huge = BatchConversionSettings(resizeMode = ImageResizeMode.PERCENTAGE, percentage = "500")
        val refused = plan(huge) as BatchItemPlan.Refused
        assertEquals(ImageResizeError.DIMENSION_TOO_LARGE, refused.error)
        assertEquals(BatchFailure.TOO_LARGE, BatchConversionPolicy.failureOf(refused.error))

        val noMemory = plan(BatchConversionSettings(), withinMemoryBudget = { false }) as BatchItemPlan.Refused
        assertEquals(ImageResizeError.MEMORY_BUDGET_EXCEEDED, noMemory.error)
        assertEquals(BatchFailure.OUT_OF_MEMORY, BatchConversionPolicy.failureOf(noMemory.error))

        val broken = landscape.copy(displaySize = ImagePixelSize(0, 0))
        val unreadable = plan(BatchConversionSettings(), broken) as BatchItemPlan.Refused
        assertEquals(BatchFailure.UNREADABLE, BatchConversionPolicy.failureOf(unreadable.error))
    }

    @Test
    fun invalidNumbersAreSettingsErrorsOnlyForTheModeInUse() {
        assertNull(BatchConversionPolicy.settingsError(BatchConversionSettings(percentage = "x", longEdge = "")))
        assertEquals(
            BatchSettingsError.INVALID_PERCENTAGE,
            BatchConversionPolicy.settingsError(BatchConversionSettings(resizeMode = ImageResizeMode.PERCENTAGE, percentage = "0")),
        )
        assertEquals(
            BatchSettingsError.INVALID_PERCENTAGE,
            BatchConversionPolicy.settingsError(BatchConversionSettings(resizeMode = ImageResizeMode.PERCENTAGE, percentage = "1001")),
        )
        assertEquals(
            BatchSettingsError.INVALID_LONG_EDGE,
            BatchConversionPolicy.settingsError(BatchConversionSettings(resizeMode = ImageResizeMode.LONG_EDGE, longEdge = "16385")),
        )
        assertNull(BatchConversionPolicy.settingsError(BatchConversionSettings(resizeMode = ImageResizeMode.LONG_EDGE, longEdge = "16384")))
    }

    @Test
    fun exifIsKeptOnlyWhenAskedAndJpegBackgroundFollowsTheChoice() {
        val metadata = PreservedExifMetadata.from(mapOf("Make" to "Maple", "Model" to "Test"))
        val source = landscape.copy(preservableExifMetadata = metadata)
        assertNull(ready(BatchConversionSettings(), source).options.preservedExifMetadata)
        assertSame(metadata, ready(BatchConversionSettings(preserveExif = true), source).options.preservedExifMetadata)

        assertEquals(ImageConversionOptions.DEFAULT_JPEG_BACKGROUND_COLOR, ready(BatchConversionSettings()).options.jpegBackgroundColor)
        assertEquals(-0x1000000, ready(BatchConversionSettings(jpegBackgroundBlack = true)).options.jpegBackgroundColor)
    }

    @Test
    fun losslessWebpNeedsAndroid11AndDisablesQuality() {
        val lossless = BatchConversionSettings(format = ImageOutputFormat.WEBP, webpLossless = true)
        assertTrue(ready(lossless, sdkInt = 30).options.webpLossless)
        assertFalse(ready(lossless, sdkInt = 29).options.webpLossless)
        assertTrue(BatchConversionPolicy.losslessVisible(lossless, 30))
        assertFalse(BatchConversionPolicy.losslessVisible(lossless, 29))
        assertFalse(BatchConversionPolicy.qualityEnabled(lossless, 30))
        assertTrue(BatchConversionPolicy.qualityEnabled(lossless, 29))
        assertFalse(BatchConversionPolicy.qualityEnabled(BatchConversionSettings(format = ImageOutputFormat.PNG), 33))
        // Lossless on a JPEG request is ignored rather than rejected.
        assertFalse(ready(BatchConversionSettings(webpLossless = true)).options.webpLossless)
    }

    @Test
    fun outputNamesCarryTheSuffixAndNeverReuseATakenName() {
        assertEquals("photo_converted.jpg", BatchConversionPolicy.outputName("photo.png", ImageOutputFormat.JPEG))
        assertEquals("archive.tar_converted.webp", BatchConversionPolicy.outputName("archive.tar.gif", ImageOutputFormat.WEBP))
        assertEquals("noext_converted.png", BatchConversionPolicy.outputName("noext", ImageOutputFormat.PNG))
        assertEquals("image_converted.png", BatchConversionPolicy.outputName(".png", ImageOutputFormat.PNG))
        assertEquals(
            "photo_converted (2).jpg",
            BatchConversionPolicy.outputName("photo.heic", ImageOutputFormat.JPEG, listOf("PHOTO_CONVERTED.JPG", "photo_converted (1).jpg")),
        )
    }
}
