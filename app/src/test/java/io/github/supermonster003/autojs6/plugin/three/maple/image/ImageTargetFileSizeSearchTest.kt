package io.github.supermonster003.autojs6.plugin.three.maple.image

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class ImageTargetFileSizeSearchTest {

    @Test
    fun binarySearchSelectsTheHighestQualityThatDoesNotExceedTheTarget() {
        val measuredQualities = mutableListOf<Int>()

        val result = ImageTargetFileSizeSearch.search(targetBytes = 73_500L) { quality ->
            measuredQualities += quality
            quality * 1_000L
        }

        assertEquals(73, result.quality)
        assertEquals(73_000L, result.encodedBytes)
        assertEquals(ImageTargetFileSizeStatus.WITHIN_QUALITY_RANGE, result.status)
        assertFalse(result.requiresConfirmation)
        assertEquals(measuredQualities.distinct().size, measuredQualities.size)
        assertEquals(measuredQualities.size, result.attemptCount)
        assertTrue(result.attemptCount <= 9)
    }

    @Test
    fun targetBelowMinimumQualityReturnsAnExplicitClosestResult() {
        val result = ImageTargetFileSizeSearch.search(targetBytes = 500L) { quality ->
            quality * 1_000L
        }

        assertEquals(ImageConversionOptions.MIN_QUALITY, result.quality)
        assertEquals(1_000L, result.encodedBytes)
        assertEquals(ImageTargetFileSizeStatus.TARGET_BELOW_MINIMUM_QUALITY, result.status)
        assertTrue(result.requiresConfirmation)
        assertEquals(1, result.attemptCount)
    }

    @Test
    fun targetAboveMaximumQualityReturnsAnExplicitClosestResult() {
        val result = ImageTargetFileSizeSearch.search(targetBytes = 101_000L) { quality ->
            quality * 1_000L
        }

        assertEquals(ImageConversionOptions.MAX_QUALITY, result.quality)
        assertEquals(100_000L, result.encodedBytes)
        assertEquals(ImageTargetFileSizeStatus.TARGET_ABOVE_MAXIMUM_QUALITY, result.status)
        assertTrue(result.requiresConfirmation)
        assertEquals(2, result.attemptCount)
    }

    @Test
    fun exactBoundaryAndSingleQualityRangeDoNotMisreportReachability() {
        val exact = ImageTargetFileSizeSearch.search(targetBytes = 64_000L) { quality ->
            quality * 1_000L
        }
        val singleQualityBelowTarget = ImageTargetFileSizeSearch.search(
            targetBytes = 2_000L,
            minQuality = 1,
            maxQuality = 1,
        ) { 1_000L }

        assertEquals(64, exact.quality)
        assertEquals(ImageTargetFileSizeStatus.WITHIN_QUALITY_RANGE, exact.status)
        assertEquals(ImageTargetFileSizeStatus.TARGET_ABOVE_MAXIMUM_QUALITY, singleQualityBelowTarget.status)
    }

    @Test
    fun targetInputPolicyHonorsKibibytesAndTheHostOutputLimit() {
        val hostLimit = 10L * ImageTargetFileSizePolicy.BYTES_PER_KIBIBYTE + 511L

        assertEquals(10L, ImageTargetFileSizePolicy.maxTargetKibibytes(hostLimit))
        assertEquals(1_024L, ImageTargetFileSizePolicy.resolveTargetBytes(1L, hostLimit))
        assertEquals(10_240L, ImageTargetFileSizePolicy.resolveTargetBytes(10L, hostLimit))
        assertNull(ImageTargetFileSizePolicy.resolveTargetBytes(0L, hostLimit))
        assertNull(ImageTargetFileSizePolicy.resolveTargetBytes(11L, hostLimit))
        assertNull(ImageTargetFileSizePolicy.resolveTargetBytes(null, hostLimit))
    }

    @Test
    fun targetModeIsIsolatedToJpegAndActuallyLossyWebp() {
        assertTrue(
            ImageOutputEncodingPolicy.supportsTargetFileSize(
                ImageOutputFormat.JPEG,
                webpLosslessRequested = true,
                sdkInt = 34,
            ),
        )
        assertTrue(
            ImageOutputEncodingPolicy.supportsTargetFileSize(
                ImageOutputFormat.WEBP,
                webpLosslessRequested = false,
                sdkInt = 34,
            ),
        )
        assertFalse(
            ImageOutputEncodingPolicy.supportsTargetFileSize(
                ImageOutputFormat.WEBP,
                webpLosslessRequested = true,
                sdkInt = 34,
            ),
        )
        assertFalse(
            ImageOutputEncodingPolicy.supportsTargetFileSize(
                ImageOutputFormat.PNG,
                webpLosslessRequested = false,
                sdkInt = 34,
            ),
        )
        assertFalse(
            ImageOutputEncodingPolicy.qualityEnabled(
                ImageOutputFormat.JPEG,
                webpLosslessRequested = false,
                sdkInt = 34,
                targetFileSizeRequested = true,
            ),
        )
        assertEquals(99, ImageOutputEncodingPolicy.maximumLossyQuality(ImageOutputFormat.WEBP, sdkInt = 29))
        assertEquals(100, ImageOutputEncodingPolicy.maximumLossyQuality(ImageOutputFormat.WEBP, sdkInt = 30))
        assertEquals(100, ImageOutputEncodingPolicy.maximumLossyQuality(ImageOutputFormat.JPEG, sdkInt = 29))
    }

    @Test
    fun conversionOptionsRejectUnsupportedTargetSizeCombinations() {
        assertThrows(IllegalArgumentException::class.java) {
            ImageConversionOptions(
                format = ImageOutputFormat.PNG,
                targetSize = ImagePixelSize(10, 10),
                targetFileSizeBytes = 1_024L,
            )
        }
        assertThrows(IllegalArgumentException::class.java) {
            ImageConversionOptions(
                format = ImageOutputFormat.WEBP,
                targetSize = ImagePixelSize(10, 10),
                webpLossless = true,
                targetFileSizeBytes = 1_024L,
            )
        }
        assertThrows(IllegalArgumentException::class.java) {
            ImageConversionOptions(
                format = ImageOutputFormat.JPEG,
                targetSize = ImagePixelSize(10, 10),
                targetFileSizeBytes = 0L,
            )
        }
    }
}
