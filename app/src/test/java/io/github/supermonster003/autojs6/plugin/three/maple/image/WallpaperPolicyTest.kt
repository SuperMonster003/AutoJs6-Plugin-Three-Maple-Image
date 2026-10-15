package io.github.supermonster003.autojs6.plugin.three.maple.image

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class WallpaperPolicyTest {

    private val phone = ImagePixelSize(1080, 2400)
    private val budget = 16_000_000L
    private val roomy = 100_000_000L

    @Test fun aPhoneFramesUprightAndATabletFramesAsHeld() {
        assertEquals(phone, WallpaperPolicy.frame(2400, 1080, smallestWidthDp = 411))
        assertEquals(phone, WallpaperPolicy.frame(1080, 2400, smallestWidthDp = 411))
        assertEquals(ImagePixelSize(2560, 1600), WallpaperPolicy.frame(2560, 1600, smallestWidthDp = 800))
        assertEquals(ImagePixelSize(1600, 2560), WallpaperPolicy.frame(1600, 2560, smallestWidthDp = 800))
    }

    @Test fun anOrdinaryPhotoIsDecodedWholeAndNothingIsEnlarged() {
        // The widest crop spans the 3000 px height, less than twice the screen height: the whole image is kept.
        assertEquals(ImagePixelSize(4000, 3000), WallpaperPolicy.decodeSize(ImagePixelSize(4000, 3000), phone, budget))
        assertEquals(ImagePixelSize(800, 600), WallpaperPolicy.decodeSize(ImagePixelSize(800, 600), phone, budget))
        assertEquals(ImagePixelSize(6000, 1000), WallpaperPolicy.decodeSize(ImagePixelSize(6000, 1000), phone, budget))
    }

    @Test fun twiceTheScreenIsKeptForTheWidestCrop() {
        // Wider than the screen: the crop spans the height, which needs 2 x 2400 = 4800 px.
        assertEquals(ImagePixelSize(6400, 4800), WallpaperPolicy.decodeSize(ImagePixelSize(8000, 6000), phone, roomy))
        // Narrower than the screen: the crop spans the width, which needs 2 x 1080 = 2160 px.
        assertEquals(ImagePixelSize(2160, 8640), WallpaperPolicy.decodeSize(ImagePixelSize(3000, 12_000), phone, roomy))
    }

    @Test fun aHugePhotoIsDecodedWithinTheBudgetKeepingItsShape() {
        val size = WallpaperPolicy.decodeSize(ImagePixelSize(8160, 6120), phone, budget)
        assertTrue(size.toString(), size.pixelCount in 15_900_000L..budget)
        assertEquals(8160.0 / 6120, size.width.toDouble() / size.height, 0.001)
        assertTrue("the widest crop still has the screen's height", size.height > phone.height)
    }

    @Test fun theCropIsBroughtDownToCoverTheScreenButNeverUp() {
        assertEquals(phone, WallpaperPolicy.outputSize(ImagePixelSize(2160, 4800), phone))
        assertEquals(ImagePixelSize(540, 1200), WallpaperPolicy.outputSize(ImagePixelSize(540, 1200), phone))
        val nearly = WallpaperPolicy.outputSize(ImagePixelSize(1081, 2402), phone)
        assertTrue(nearly.toString(), abs(nearly.width - 1080) <= 1 && nearly.height == 2400)
    }
}
