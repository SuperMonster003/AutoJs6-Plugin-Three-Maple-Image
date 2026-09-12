package io.github.supermonster003.autojs6.plugin.imagetools

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

internal data class RotationCropPlan(
    val degrees: Float,
    val outputWidth: Int,
    val outputHeight: Int,
    val coverScale: Float,
) {
    val outputPixelCount: Long
        get() = outputWidth.toLong() * outputHeight
}

internal object ImageRotationGeometry {

    fun plan(
        sourceWidth: Int,
        sourceHeight: Int,
        degrees: Float,
        maxPixelCount: Long,
    ): RotationCropPlan {
        validateDimensions(sourceWidth, sourceHeight)
        validateDegrees(degrees)
        require(maxPixelCount > 0L) { "Rotation pixel budget must be positive" }
        val outputPixelCount = sourceWidth.toLong() * sourceHeight
        require(outputPixelCount <= maxPixelCount) {
            "Rotated image exceeds the editing pixel budget"
        }
        return RotationCropPlan(
            degrees = degrees,
            outputWidth = sourceWidth,
            outputHeight = sourceHeight,
            coverScale = coverScale(sourceWidth, sourceHeight, degrees),
        )
    }

    /**
     * Returns the uniform scale that makes a centered, rotated source fully cover an output canvas
     * with the same dimensions. A small raster margin prevents sampling seams at the four edges.
     */
    fun coverScale(sourceWidth: Int, sourceHeight: Int, degrees: Float): Float {
        validateDimensions(sourceWidth, sourceHeight)
        validateDegrees(degrees)
        if (degrees == 0f) return 1f
        val radians = degrees.toDouble() * PI / 180.0
        val sine = abs(sin(radians))
        val cosine = abs(cos(radians))
        val aspect = max(
            sourceWidth.toDouble() / sourceHeight,
            sourceHeight.toDouble() / sourceWidth,
        )
        return (cosine + sine * aspect).times(RASTER_COVERAGE_MARGIN).toFloat()
    }

    private fun validateDimensions(width: Int, height: Int) {
        require(width > 0 && height > 0) { "Rotation dimensions must be positive" }
    }

    private fun validateDegrees(degrees: Float) {
        require(degrees.isFinite() && degrees in MIN_DEGREES..MAX_DEGREES) {
            "Fine rotation must be between $MIN_DEGREES and $MAX_DEGREES degrees"
        }
    }

    const val MIN_DEGREES = -45f
    const val MAX_DEGREES = 45f
    private const val RASTER_COVERAGE_MARGIN = 1.001
}
