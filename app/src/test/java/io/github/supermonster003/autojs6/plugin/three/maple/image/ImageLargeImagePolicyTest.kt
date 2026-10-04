package io.github.supermonster003.autojs6.plugin.three.maple.image

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ImageLargeImagePolicyTest {

    @Test
    fun runtimeLimitsAreBoundedByMemoryClassAndObservedCanvasLimits() {
        val lowMemory = ImageLargeImagePolicy.runtimeLimits(
            memoryClassMebibytes = 128,
            observedMaximumBitmapWidth = 8_192,
            observedMaximumBitmapHeight = 4_096,
        )
        assertEquals(8_192, lowMemory.maximumBitmapWidth)
        assertEquals(4_096, lowMemory.maximumBitmapHeight)
        assertEquals(32L * MEBIBYTE, lowMemory.fullDecodeBudgetBytes)
        assertEquals(12L * MEBIBYTE, lowMemory.tileCacheBudgetBytes)

        val highMemoryWithUnknownCanvas = ImageLargeImagePolicy.runtimeLimits(
            memoryClassMebibytes = 1_024,
            observedMaximumBitmapWidth = 0,
            observedMaximumBitmapHeight = 0,
        )
        assertEquals(
            ImageLargeImagePolicy.FALLBACK_MAX_BITMAP_EDGE,
            highMemoryWithUnknownCanvas.maximumBitmapWidth,
        )
        assertEquals(64L * MEBIBYTE, highMemoryWithUnknownCanvas.fullDecodeBudgetBytes)
        assertEquals(32L * MEBIBYTE, highMemoryWithUnknownCanvas.tileCacheBudgetBytes)
    }

    @Test
    fun tilingRequiresARegionDecodableStaticFormatAndAConcreteLimitBreach() {
        val limits = ImageBitmapLimits(
            maximumBitmapWidth = 4_096,
            maximumBitmapHeight = 4_096,
            fullDecodeBudgetBytes = 64L * MEBIBYTE,
            tileCacheBudgetBytes = 16L * MEBIBYTE,
        )

        assertFalse(shouldTile(4_096, 4_096, "image/jpeg", 35, limits))
        assertTrue(shouldTile(4_097, 1_000, "image/jpeg", 35, limits))
        assertTrue(shouldTile(4_000, 4_300, "image/png", 35, limits))
        assertTrue(shouldTile(5_000, 4_000, "image/heic", 28, limits))
        assertFalse(shouldTile(5_000, 4_000, "image/heic", 27, limits))

        assertFalse(shouldTile(20_000, 20_000, "image/gif", 35, limits))
        assertFalse(shouldTile(20_000, 20_000, "image/webp", 35, limits))
        assertFalse(shouldTile(20_000, 20_000, "image/heif-sequence", 35, limits))
        assertFalse(shouldTile(20_000, 20_000, "image/avif", 35, limits))
    }

    @Test
    fun byteEstimateSaturatesAndPreviewSamplingStaysPowerOfTwo() {
        assertEquals(0L, ImageLargeImagePolicy.estimatedArgbBytes(0, 1))
        assertEquals(80_000_000L, ImageLargeImagePolicy.estimatedArgbBytes(5_000, 4_000))
        assertEquals(
            Long.MAX_VALUE,
            ImageLargeImagePolicy.estimatedArgbBytes(Int.MAX_VALUE, Int.MAX_VALUE),
        )

        assertEquals(1, ImageLargeImagePolicy.previewSampleSize(2_048, 2_048))
        assertEquals(4, ImageLargeImagePolicy.previewSampleSize(5_000, 4_000))
        assertEquals(8, ImageLargeImagePolicy.previewSampleSize(10_000, 10_000))
    }

    @Test
    fun viewportScaleSelectsTheSmallestUsefulPowerOfTwoLevel() {
        assertEquals(1, ImageLargeImagePolicy.tileSampleSize(Float.NaN))
        assertEquals(1, ImageLargeImagePolicy.tileSampleSize(1f))
        assertEquals(2, ImageLargeImagePolicy.tileSampleSize(0.5f))
        assertEquals(4, ImageLargeImagePolicy.tileSampleSize(0.2f))
        assertEquals(8, ImageLargeImagePolicy.tileSampleSize(0.1f))
    }

    @Test
    fun visibleTilePlanIsClampedBoundedAndCenterFirst() {
        val tiles = ImageLargeImagePolicy.visibleTiles(
            rawWidth = 5_000,
            rawHeight = 4_000,
            visibleLeft = 1_000f,
            visibleTop = 500f,
            visibleRight = 2_500f,
            visibleBottom = 1_800f,
            sampleSize = 2,
        )

        assertEquals(6, tiles.size)
        assertEquals(ImageTileKey(sampleSize = 2, column = 1, row = 1), tiles.first().key)
        assertEquals(
            setOf(
                ImageTileKey(2, 0, 0),
                ImageTileKey(2, 1, 0),
                ImageTileKey(2, 2, 0),
                ImageTileKey(2, 0, 1),
                ImageTileKey(2, 1, 1),
                ImageTileKey(2, 2, 1),
            ),
            tiles.map(ImageTileSpec::key).toSet(),
        )
        assertTrue(tiles.all { it.left >= 0 && it.top >= 0 })
        assertTrue(tiles.all { it.right <= 5_000 && it.bottom <= 4_000 })
        assertTrue(
            ImageLargeImagePolicy.visibleTiles(
                5_000,
                4_000,
                0f,
                0f,
                1_000f,
                1_000f,
                sampleSize = 3,
            ).isEmpty(),
        )
    }

    private fun shouldTile(
        width: Int,
        height: Int,
        mimeType: String,
        sdkInt: Int,
        limits: ImageBitmapLimits,
    ): Boolean = ImageLargeImagePolicy.shouldUseTiling(
        width,
        height,
        mimeType,
        sdkInt,
        limits,
    )

    private companion object {
        const val MEBIBYTE = 1_048_576L
    }
}
