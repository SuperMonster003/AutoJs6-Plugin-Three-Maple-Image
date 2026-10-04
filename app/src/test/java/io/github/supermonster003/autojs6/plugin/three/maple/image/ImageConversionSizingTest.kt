package io.github.supermonster003.autojs6.plugin.three.maple.image

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class ImageConversionSizingTest {

    private val source = ImagePixelSize(4000, 3000)

    @Test
    fun resolvesOriginalAndPercentageSizes() {
        assertEquals(
            source,
            ImageConversionSizing.resolve(source, ImageResizeRequest(ImageResizeMode.ORIGINAL)).size,
        )
        assertEquals(
            ImagePixelSize(1000, 750),
            ImageConversionSizing.resolve(
                source,
                ImageResizeRequest(ImageResizeMode.PERCENTAGE, percentage = 25),
            ).size,
        )
    }

    @Test
    fun rejectsInvalidAndExcessiveSizes() {
        assertEquals(
            ImageResizeError.INVALID_PERCENTAGE,
            ImageConversionSizing.resolve(
                source,
                ImageResizeRequest(ImageResizeMode.PERCENTAGE, percentage = 0),
            ).error,
        )
        assertEquals(
            ImageResizeError.INVALID_WIDTH,
            ImageConversionSizing.resolve(
                source,
                ImageResizeRequest(ImageResizeMode.CUSTOM, width = 0, height = 100),
            ).error,
        )
        assertEquals(
            ImageResizeError.DIMENSION_TOO_LARGE,
            ImageConversionSizing.resolve(
                source,
                ImageResizeRequest(ImageResizeMode.CUSTOM, width = 20_000, height = 100),
            ).error,
        )
        assertEquals(
            ImageResizeError.PIXEL_COUNT_TOO_LARGE,
            ImageConversionSizing.resolve(
                source,
                ImageResizeRequest(ImageResizeMode.CUSTOM, width = 10_000, height = 5_000),
            ).error,
        )
    }

    @Test
    fun computesLockedAspectRatioDimensions() {
        assertEquals(1500, ImageConversionSizing.heightForWidth(source, 2000))
        assertEquals(2000, ImageConversionSizing.widthForHeight(source, 1500))
    }

    @Test
    fun longEdgeModePreservesLandscapePortraitAndSquareAspectRatios() {
        assertEquals(
            ImagePixelSize(1000, 750),
            ImageConversionSizing.resolve(
                source,
                ImageResizeRequest(ImageResizeMode.LONG_EDGE, longEdge = 1000),
            ).size,
        )
        assertEquals(
            ImagePixelSize(750, 1000),
            ImageConversionSizing.resolve(
                ImagePixelSize(3000, 4000),
                ImageResizeRequest(ImageResizeMode.LONG_EDGE, longEdge = 1000),
            ).size,
        )
        assertEquals(
            ImagePixelSize(1000, 1000),
            ImageConversionSizing.resolve(
                ImagePixelSize(4000, 4000),
                ImageResizeRequest(ImageResizeMode.LONG_EDGE, longEdge = 1000),
            ).size,
        )
    }

    @Test
    fun longEdgeModeNeverUpscalesAndUsesStableRounding() {
        assertEquals(
            ImagePixelSize(800, 600),
            ImageConversionSizing.resolve(
                ImagePixelSize(800, 600),
                ImageResizeRequest(ImageResizeMode.LONG_EDGE, longEdge = 1920),
            ).size,
        )
        assertEquals(
            ImagePixelSize(1001, 751),
            ImageConversionSizing.resolve(
                ImagePixelSize(4032, 3024),
                ImageResizeRequest(ImageResizeMode.LONG_EDGE, longEdge = 1001),
            ).size,
        )
    }

    @Test
    fun longEdgeModeEnforcesDimensionAndPixelCountLimits() {
        assertEquals(
            ImageResizeError.INVALID_LONG_EDGE,
            ImageConversionSizing.resolve(
                source,
                ImageResizeRequest(ImageResizeMode.LONG_EDGE),
            ).error,
        )
        assertEquals(
            ImageResizeError.INVALID_LONG_EDGE,
            ImageConversionSizing.resolve(
                source,
                ImageResizeRequest(ImageResizeMode.LONG_EDGE, longEdge = 0),
            ).error,
        )
        assertEquals(
            ImageResizeError.INVALID_LONG_EDGE,
            ImageConversionSizing.resolve(
                source,
                ImageResizeRequest(ImageResizeMode.LONG_EDGE, longEdge = 16_385),
            ).error,
        )
        assertEquals(
            ImagePixelSize(16_384, 819),
            ImageConversionSizing.resolve(
                ImagePixelSize(20_000, 1000),
                ImageResizeRequest(ImageResizeMode.LONG_EDGE, longEdge = 16_384),
            ).size,
        )
        assertEquals(
            ImageResizeError.PIXEL_COUNT_TOO_LARGE,
            ImageConversionSizing.resolve(
                ImagePixelSize(10_000, 10_000),
                ImageResizeRequest(ImageResizeMode.LONG_EDGE, longEdge = 7000),
            ).error,
        )
        assertEquals(
            ImagePixelSize(6000, 6000),
            ImageConversionSizing.resolve(
                ImagePixelSize(10_000, 10_000),
                ImageResizeRequest(ImageResizeMode.LONG_EDGE, longEdge = 6000),
            ).size,
        )
    }

    @Test
    fun qualityAppliesOnlyToLossyFormats() {
        assertTrue(ImageOutputFormat.JPEG.supportsQuality)
        assertFalse(ImageOutputFormat.PNG.supportsQuality)
        assertTrue(ImageOutputFormat.WEBP.supportsQuality)
    }

    @Test
    fun encodedSizeEstimateRespondsToQualityAndResolution() {
        val small = ImageConversionOptions(
            format = ImageOutputFormat.JPEG,
            quality = 50,
            targetSize = ImagePixelSize(1000, 750),
        )
        val highQuality = small.copy(quality = 95)
        val large = small.copy(targetSize = ImagePixelSize(2000, 1500))

        assertTrue(ImageConversionSizing.estimateEncodedBytes(highQuality) > ImageConversionSizing.estimateEncodedBytes(small))
        assertTrue(ImageConversionSizing.estimateEncodedBytes(large) > ImageConversionSizing.estimateEncodedBytes(small))
    }

    @Test
    fun losslessWebpEstimateDoesNotDependOnTheDisabledQualityValue() {
        val lossless = ImageConversionOptions(
            format = ImageOutputFormat.WEBP,
            quality = 1,
            targetSize = ImagePixelSize(1000, 750),
            webpLossless = true,
        )

        assertEquals(
            ImageConversionSizing.estimateEncodedBytes(lossless),
            ImageConversionSizing.estimateEncodedBytes(lossless.copy(quality = 100)),
        )
        assertTrue(
            ImageConversionSizing.estimateEncodedBytes(lossless) >
                ImageConversionSizing.estimateEncodedBytes(lossless.copy(webpLossless = false)),
        )
    }

    @Test
    fun indexedPngEstimateReflectsPaletteBitDepthSavings() {
        val regularPng = ImageConversionOptions(
            format = ImageOutputFormat.PNG,
            targetSize = ImagePixelSize(1000, 750),
        )
        val twoColors = regularPng.copy(pngPaletteColorCountHint = 2)
        val sixteenColors = regularPng.copy(pngPaletteColorCountHint = 16)
        val twoHundredAndFiftySixColors = regularPng.copy(pngPaletteColorCountHint = 256)

        assertTrue(
            ImageConversionSizing.estimateEncodedBytes(twoColors) <
                ImageConversionSizing.estimateEncodedBytes(sixteenColors),
        )
        assertTrue(
            ImageConversionSizing.estimateEncodedBytes(sixteenColors) <
                ImageConversionSizing.estimateEncodedBytes(twoHundredAndFiftySixColors),
        )
        assertTrue(
            ImageConversionSizing.estimateEncodedBytes(twoHundredAndFiftySixColors) <
                ImageConversionSizing.estimateEncodedBytes(regularPng),
        )
    }

    @Test
    fun paletteEstimateHintIsUsedOnlyForUnscaledPngOutput() {
        val originalSize = ImagePixelSize(320, 240)

        assertEquals(
            16,
            ImageConversionSizing.pngPaletteColorCountHint(
                format = ImageOutputFormat.PNG,
                sourceSize = originalSize,
                targetSize = originalSize,
                sourcePaletteColorCount = 16,
            ),
        )
        assertNull(
            ImageConversionSizing.pngPaletteColorCountHint(
                format = ImageOutputFormat.PNG,
                sourceSize = originalSize,
                targetSize = ImagePixelSize(160, 120),
                sourcePaletteColorCount = 16,
            ),
        )
        assertNull(
            ImageConversionSizing.pngPaletteColorCountHint(
                format = ImageOutputFormat.JPEG,
                sourceSize = originalSize,
                targetSize = originalSize,
                sourcePaletteColorCount = 16,
            ),
        )
    }

    @Test
    fun paletteEstimateRejectsInvalidOrNonPngHints() {
        assertThrows(IllegalArgumentException::class.java) {
            ImageConversionOptions(
                format = ImageOutputFormat.JPEG,
                targetSize = ImagePixelSize(10, 10),
                pngPaletteColorCountHint = 2,
            )
        }
        assertThrows(IllegalArgumentException::class.java) {
            ImageConversionOptions(
                format = ImageOutputFormat.PNG,
                targetSize = ImagePixelSize(10, 10),
                pngPaletteColorCountHint = 257,
            )
        }
    }

    @Test
    fun exifEstimateAddsMetadataOverheadToRegularAndIndexedOutputs() {
        val metadata = PreservedExifMetadata.from(
            mapOf("Make" to "AutoJs6 Test Camera", "DateTimeOriginal" to "2026:08:31 12:34:56"),
        )
        val regular = ImageConversionOptions(
            format = ImageOutputFormat.PNG,
            targetSize = ImagePixelSize(320, 240),
        )
        val indexed = regular.copy(pngPaletteColorCountHint = 16)
        val expectedOverhead = metadata.estimatedEncodedOverhead(ImageOutputFormat.PNG)

        assertEquals(
            expectedOverhead,
            ImageConversionSizing.estimateEncodedBytes(regular.copy(preservedExifMetadata = metadata)) -
                ImageConversionSizing.estimateEncodedBytes(regular),
        )
        assertEquals(
            expectedOverhead,
            ImageConversionSizing.estimateEncodedBytes(indexed.copy(preservedExifMetadata = metadata)) -
                ImageConversionSizing.estimateEncodedBytes(indexed),
        )
    }
}
