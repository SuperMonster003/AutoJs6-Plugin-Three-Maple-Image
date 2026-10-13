package io.github.supermonster003.autojs6.plugin.three.maple.image

import com.bumptech.glide.load.resource.bitmap.DownsampleStrategy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class BudgetedDownsampleStrategyTest {

    private val budget = 16L * 1024 * 1024 // 64 MiB of ARGB
    private val strategy = BudgetedDownsampleStrategy(budget)

    @Test
    fun ordinaryPhotosKeepTheCenterOutsideScale() {
        val base = DownsampleStrategy.CENTER_OUTSIDE.getScaleFactor(4000, 3000, 1096, 2560)
        assertEquals(base, strategy.getScaleFactor(4000, 3000, 1096, 2560), 0f)
        assertEquals(DownsampleStrategy.SampleSizeRounding.QUALITY, strategy.getSampleSizeRounding(4000, 3000, 1096, 2560))
    }

    @Test
    fun extremeAspectRatiosAreScaledIntoTheBudget() {
        // A 1000 x 120000 strip: CENTER_OUTSIDE keeps full resolution on the narrow axis (120 M pixels, 480 MB).
        val scale = strategy.getScaleFactor(1000, 120_000, 1096, 2560)
        val base = DownsampleStrategy.CENTER_OUTSIDE.getScaleFactor(1000, 120_000, 1096, 2560)
        assertTrue(scale < base)
        val decodedPixels = 1000.0 * 120_000.0 * scale * scale
        // Float scale factors round by a few pixels; Glide's MEMORY rounding then keeps the real decode under budget.
        assertTrue("decoded $decodedPixels px must fit the budget", decodedPixels <= budget.toDouble() * 1.0001)
        assertEquals(DownsampleStrategy.SampleSizeRounding.MEMORY, strategy.getSampleSizeRounding(1000, 120_000, 1096, 2560))
        // The same strip against a square fallback target (a premeasured, not yet laid out page) is capped as well.
        val squareScale = strategy.getScaleFactor(2600, 11000, 2560, 2560)
        assertTrue(2600.0 * 11000.0 * squareScale * squareScale <= budget.toDouble() * 1.0001)
    }

    @Test
    fun degenerateInputsFallBackToTheBaseScale() {
        assertEquals(0.5f, BudgetedDownsampleStrategy.budgetedScale(0, 10, 0.5f, budget), 0f)
        assertEquals(0.5f, BudgetedDownsampleStrategy.budgetedScale(10, 10, 0.5f, 0L), 0f)
        assertEquals(Float.NaN, BudgetedDownsampleStrategy.budgetedScale(10, 10, Float.NaN, budget), 0f)
    }
}
