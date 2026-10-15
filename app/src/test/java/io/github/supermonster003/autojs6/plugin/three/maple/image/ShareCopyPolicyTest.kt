package io.github.supermonster003.autojs6.plugin.three.maple.image

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ShareCopyPolicyTest {

    @Test fun jpegPngAndWebpKeepTheirFormatEverythingElseBecomesJpeg() {
        assertEquals(ImageOutputFormat.PNG, ShareCopyPolicy.outputFormat("image/png"))
        assertEquals(ImageOutputFormat.WEBP, ShareCopyPolicy.outputFormat("IMAGE/WEBP"))
        assertEquals(ImageOutputFormat.JPEG, ShareCopyPolicy.outputFormat("image/jpeg"))
        listOf("image/gif", "image/heic", "image/avif", "image/bmp").forEach {
            assertEquals(it, ImageOutputFormat.JPEG, ShareCopyPolicy.outputFormat(it))
        }
    }

    @Test fun aSmallerCopyFollowsTheLongEdgeAndIsNeverEnlarged() {
        val landscape = ImagePixelSize(4000, 3000)
        assertEquals(ImagePixelSize(1080, 810), ShareCopyPolicy.targetSize(landscape, ShareCopyRequest(ShareCopyMode.SMALLER, 1080)).size)
        assertEquals(ImagePixelSize(1440, 1920), ShareCopyPolicy.targetSize(ImagePixelSize(3000, 4000), ShareCopyRequest(ShareCopyMode.SMALLER, 1920)).size)
        assertEquals(ImagePixelSize(800, 600), ShareCopyPolicy.targetSize(ImagePixelSize(800, 600), ShareCopyRequest(ShareCopyMode.SMALLER, 1920)).size)
        assertEquals(landscape, ShareCopyPolicy.targetSize(landscape, ShareCopyRequest(ShareCopyMode.WITHOUT_METADATA)).size)
    }

    @Test fun aFullSizeCopyOfAHugeImageIsRefused() {
        val result = ShareCopyPolicy.targetSize(ImagePixelSize(10_000, 10_000), ShareCopyRequest(ShareCopyMode.WITHOUT_METADATA))
        assertEquals(ImageResizeError.PIXEL_COUNT_TOO_LARGE, result.error)
    }

    @Test fun theCopyKeepsTheNameWithTheExtensionOfItsFormat() {
        assertEquals("IMG_0001.jpg", ShareCopyPolicy.fileName("IMG_0001.HEIC", ImageOutputFormat.JPEG))
        assertEquals("shot.png", ShareCopyPolicy.fileName("shot.png", ImageOutputFormat.PNG))
        assertEquals("a_b_c.webp", ShareCopyPolicy.fileName("a/b:c.webp", ImageOutputFormat.WEBP))
        assertEquals(".hidden.jpg", ShareCopyPolicy.fileName(".hidden", ImageOutputFormat.JPEG))
        assertEquals("image.jpg", ShareCopyPolicy.fileName("   ", ImageOutputFormat.JPEG))
    }

    @Test fun copiesExpireAfterADay() {
        val now = 10L * ShareCopyPolicy.MAX_AGE_MILLIS
        assertFalse(ShareCopyPolicy.expired(now - ShareCopyPolicy.MAX_AGE_MILLIS, now))
        assertTrue(ShareCopyPolicy.expired(now - ShareCopyPolicy.MAX_AGE_MILLIS - 1, now))
    }
}
