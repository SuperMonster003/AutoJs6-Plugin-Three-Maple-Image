package io.github.supermonster003.autojs6.plugin.three.maple.image

import kotlin.math.min

/** Pure fit-center geometry shared by the on-screen viewer and printed PDF renderer. */
internal object ImageFitCenterTransform {

    fun calculate(
        sourceWidth: Float,
        sourceHeight: Float,
        contentLeft: Float,
        contentTop: Float,
        contentWidth: Float,
        contentHeight: Float,
        quarterTurns: Int,
    ): ImageFitCenterLayout? {
        if (
            !sourceWidth.isFinite() ||
            !sourceHeight.isFinite() ||
            !contentLeft.isFinite() ||
            !contentTop.isFinite() ||
            !contentWidth.isFinite() ||
            !contentHeight.isFinite() ||
            sourceWidth <= 0f ||
            sourceHeight <= 0f ||
            contentWidth <= 0f ||
            contentHeight <= 0f
        ) {
            return null
        }

        val swapsDimensions = ImageRotationState.swapsDimensions(quarterTurns)
        val rotatedWidth = if (swapsDimensions) sourceHeight else sourceWidth
        val rotatedHeight = if (swapsDimensions) sourceWidth else sourceHeight
        val scale = min(contentWidth / rotatedWidth, contentHeight / rotatedHeight)
        val fittedWidth = rotatedWidth * scale
        val fittedHeight = rotatedHeight * scale
        val offsetX = contentLeft + (contentWidth - fittedWidth) / 2f
        val offsetY = contentTop + (contentHeight - fittedHeight) / 2f

        return ImageFitCenterLayout(
            matrixValues = quarterTurnValues(
                sourceWidth = sourceWidth,
                sourceHeight = sourceHeight,
                scale = scale,
                offsetX = offsetX,
                offsetY = offsetY,
                quarterTurns = quarterTurns,
            ),
            scale = scale,
            fittedWidth = fittedWidth,
            fittedHeight = fittedHeight,
            offsetX = offsetX,
            offsetY = offsetY,
        )
    }

    private fun quarterTurnValues(
        sourceWidth: Float,
        sourceHeight: Float,
        scale: Float,
        offsetX: Float,
        offsetY: Float,
        quarterTurns: Int,
    ): FloatArray = when (ImageRotationState.normalize(quarterTurns)) {
        0 -> floatArrayOf(
            scale, 0f, offsetX,
            0f, scale, offsetY,
            0f, 0f, 1f,
        )

        1 -> floatArrayOf(
            0f, -scale, offsetX + sourceHeight * scale,
            scale, 0f, offsetY,
            0f, 0f, 1f,
        )

        2 -> floatArrayOf(
            -scale, 0f, offsetX + sourceWidth * scale,
            0f, -scale, offsetY + sourceHeight * scale,
            0f, 0f, 1f,
        )

        else -> floatArrayOf(
            0f, scale, offsetX,
            -scale, 0f, offsetY + sourceWidth * scale,
            0f, 0f, 1f,
        )
    }
}

internal data class ImageFitCenterLayout(
    val matrixValues: FloatArray,
    val scale: Float,
    val fittedWidth: Float,
    val fittedHeight: Float,
    val offsetX: Float,
    val offsetY: Float,
)
