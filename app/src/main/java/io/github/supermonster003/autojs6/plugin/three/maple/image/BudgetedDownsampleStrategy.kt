package io.github.supermonster003.autojs6.plugin.three.maple.image

import com.bumptech.glide.load.resource.bitmap.DownsampleStrategy
import kotlin.math.sqrt

/**
 * Glide downsampling for the viewer's full-image path: [DownsampleStrategy.CENTER_OUTSIDE] (keep every detail the
 * window can show) until the decoded bitmap would exceed [maxPixels], then scale further so the result fits the
 * full-decode budget. Extreme aspect ratios (a long WebP screenshot, a GIF strip) otherwise decode at their native
 * size on the axis that is already smaller than the window and can exceed the canvas bitmap limit.
 */
internal class BudgetedDownsampleStrategy(private val maxPixels: Long) : DownsampleStrategy() {

    override fun getScaleFactor(sourceWidth: Int, sourceHeight: Int, requestedWidth: Int, requestedHeight: Int): Float {
        val base = CENTER_OUTSIDE.getScaleFactor(sourceWidth, sourceHeight, requestedWidth, requestedHeight)
        return budgetedScale(sourceWidth, sourceHeight, base, maxPixels)
    }

    override fun getSampleSizeRounding(
        sourceWidth: Int,
        sourceHeight: Int,
        requestedWidth: Int,
        requestedHeight: Int,
    ): SampleSizeRounding {
        val base = CENTER_OUTSIDE.getScaleFactor(sourceWidth, sourceHeight, requestedWidth, requestedHeight)
        val capped = budgetedScale(sourceWidth, sourceHeight, base, maxPixels)
        // Rounding the sample size up ("memory") is the only way to guarantee the budget once it bites.
        return if (capped < base) SampleSizeRounding.MEMORY else SampleSizeRounding.QUALITY
    }

    companion object {
        /** The scale that keeps `source * scale^2` within [maxPixels]; never larger than [base]. */
        fun budgetedScale(sourceWidth: Int, sourceHeight: Int, base: Float, maxPixels: Long): Float {
            if (sourceWidth <= 0 || sourceHeight <= 0 || maxPixels <= 0L || !base.isFinite() || base <= 0f) return base
            val sourcePixels = sourceWidth.toDouble() * sourceHeight.toDouble()
            val decodedPixels = sourcePixels * base * base
            if (decodedPixels <= maxPixels) return base
            return sqrt(maxPixels / sourcePixels).toFloat().coerceAtMost(base)
        }
    }
}
