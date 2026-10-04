package io.github.supermonster003.autojs6.plugin.three.maple.image

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ImageOutputEncodingPolicyTest {

    @Test
    fun losslessWebpAppearsStartingWithAndroidEleven() {
        assertFalse(ImageOutputEncodingPolicy.isWebpLosslessAvailable(29))
        assertTrue(ImageOutputEncodingPolicy.isWebpLosslessAvailable(30))
        assertTrue(ImageOutputEncodingPolicy.isWebpLosslessAvailable(36))
    }

    @Test
    fun requestedLosslessWebpFallsBackToLossyAndKeepsQualityOnOlderAndroid() {
        assertFalse(
            ImageOutputEncodingPolicy.usesWebpLossless(
                ImageOutputFormat.WEBP,
                requested = true,
                sdkInt = 29,
            ),
        )
        assertTrue(
            ImageOutputEncodingPolicy.qualityEnabled(
                ImageOutputFormat.WEBP,
                webpLosslessRequested = true,
                sdkInt = 29,
            ),
        )
    }

    @Test
    fun activeLosslessWebpDisablesQualityWithoutAffectingOtherFormats() {
        assertTrue(
            ImageOutputEncodingPolicy.usesWebpLossless(
                ImageOutputFormat.WEBP,
                requested = true,
                sdkInt = 30,
            ),
        )
        assertFalse(
            ImageOutputEncodingPolicy.qualityEnabled(
                ImageOutputFormat.WEBP,
                webpLosslessRequested = true,
                sdkInt = 30,
            ),
        )
        assertTrue(
            ImageOutputEncodingPolicy.qualityEnabled(
                ImageOutputFormat.JPEG,
                webpLosslessRequested = true,
                sdkInt = 30,
            ),
        )
        assertFalse(
            ImageOutputEncodingPolicy.qualityEnabled(
                ImageOutputFormat.PNG,
                webpLosslessRequested = false,
                sdkInt = 30,
            ),
        )
    }
}
