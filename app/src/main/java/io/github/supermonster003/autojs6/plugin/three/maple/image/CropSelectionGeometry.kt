package io.github.supermonster003.autojs6.plugin.three.maple.image

import kotlin.math.max
import kotlin.math.min

internal enum class CropAspectRatioPreset {
    FREE,
    SQUARE,
    LANDSCAPE_4_3,
    PORTRAIT_3_4,
    LANDSCAPE_16_9,
    PORTRAIT_9_16,
    ORIGINAL;

    fun aspectRatio(sourceWidth: Int, sourceHeight: Int): Float? = when (this) {
        FREE -> null
        SQUARE -> 1f
        LANDSCAPE_4_3 -> 4f / 3f
        PORTRAIT_3_4 -> 3f / 4f
        LANDSCAPE_16_9 -> 16f / 9f
        PORTRAIT_9_16 -> 9f / 16f
        ORIGINAL -> sourceWidth.toFloat() / sourceHeight
    }
}

internal enum class CropResizeHandle {
    LEFT,
    TOP,
    RIGHT,
    BOTTOM,
    TOP_LEFT,
    TOP_RIGHT,
    BOTTOM_LEFT,
    BOTTOM_RIGHT,
    MOVE,
}

internal data class CropSelection(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
) {
    val width: Float
        get() = right - left

    val height: Float
        get() = bottom - top

    val centerX: Float
        get() = (left + right) / 2f

    val centerY: Float
        get() = (top + bottom) / 2f
}

internal object CropSelectionGeometry {

    /** Changes only the ratio, retaining the center and never expanding outside the selection. */
    fun fitAspectRatio(selection: CropSelection, aspectRatio: Float): CropSelection {
        validateSelection(selection)
        validateAspectRatio(aspectRatio)
        var width = selection.width
        var height = width / aspectRatio
        if (height > selection.height) {
            height = selection.height
            width = height * aspectRatio
        }
        return fromCenter(selection.centerX, selection.centerY, width, height)
    }

    fun resizeLocked(
        selection: CropSelection,
        handle: CropResizeHandle,
        deltaX: Float,
        deltaY: Float,
        boundsWidth: Float,
        boundsHeight: Float,
        aspectRatio: Float,
        minimumSide: Float,
    ): CropSelection {
        validateSelection(selection)
        validateBounds(boundsWidth, boundsHeight)
        validateAspectRatio(aspectRatio)
        require(minimumSide.isFinite() && minimumSide > 0f) { "Minimum crop side must be positive" }
        return when (handle) {
            CropResizeHandle.MOVE -> move(selection, deltaX, deltaY, boundsWidth, boundsHeight)
            CropResizeHandle.LEFT, CropResizeHandle.RIGHT -> resizeHorizontalEdge(
                selection,
                handle,
                deltaX,
                boundsWidth,
                boundsHeight,
                aspectRatio,
                minimumSide,
            )
            CropResizeHandle.TOP, CropResizeHandle.BOTTOM -> resizeVerticalEdge(
                selection,
                handle,
                deltaY,
                boundsWidth,
                boundsHeight,
                aspectRatio,
                minimumSide,
            )
            else -> resizeCorner(
                selection,
                handle,
                deltaX,
                deltaY,
                boundsWidth,
                boundsHeight,
                aspectRatio,
                minimumSide,
            )
        }
    }

    private fun resizeCorner(
        selection: CropSelection,
        handle: CropResizeHandle,
        deltaX: Float,
        deltaY: Float,
        boundsWidth: Float,
        boundsHeight: Float,
        aspectRatio: Float,
        minimumSide: Float,
    ): CropSelection {
        val movesLeft = handle == CropResizeHandle.TOP_LEFT || handle == CropResizeHandle.BOTTOM_LEFT
        val movesTop = handle == CropResizeHandle.TOP_LEFT || handle == CropResizeHandle.TOP_RIGHT
        val anchorX = if (movesLeft) selection.right else selection.left
        val anchorY = if (movesTop) selection.bottom else selection.top
        val movingX = (if (movesLeft) selection.left else selection.right) + deltaX
        val movingY = (if (movesTop) selection.top else selection.bottom) + deltaY
        val rawWidth = (if (movesLeft) anchorX - movingX else movingX - anchorX).coerceAtLeast(0f)
        val rawHeight = (if (movesTop) anchorY - movingY else movingY - anchorY).coerceAtLeast(0f)
        val widthDrivenHeight = rawWidth / aspectRatio
        val heightDrivenWidth = rawHeight * aspectRatio
        val desiredWidth = if (
            square(widthDrivenHeight - rawHeight) <= square(heightDrivenWidth - rawWidth)
        ) {
            rawWidth
        } else {
            heightDrivenWidth
        }
        val horizontalCapacity = if (movesLeft) anchorX else boundsWidth - anchorX
        val verticalCapacity = if (movesTop) anchorY else boundsHeight - anchorY
        val maxWidth = min(horizontalCapacity, verticalCapacity * aspectRatio).coerceAtLeast(0f)
        val minWidth = max(minimumSide, minimumSide * aspectRatio)
        val width = clampSize(desiredWidth, minWidth, maxWidth)
        val height = width / aspectRatio
        return CropSelection(
            left = if (movesLeft) anchorX - width else anchorX,
            top = if (movesTop) anchorY - height else anchorY,
            right = if (movesLeft) anchorX else anchorX + width,
            bottom = if (movesTop) anchorY else anchorY + height,
        )
    }

    private fun resizeHorizontalEdge(
        selection: CropSelection,
        handle: CropResizeHandle,
        deltaX: Float,
        boundsWidth: Float,
        boundsHeight: Float,
        aspectRatio: Float,
        minimumSide: Float,
    ): CropSelection {
        val movesLeft = handle == CropResizeHandle.LEFT
        val anchorX = if (movesLeft) selection.right else selection.left
        val movingX = (if (movesLeft) selection.left else selection.right) + deltaX
        val desiredWidth = (if (movesLeft) anchorX - movingX else movingX - anchorX).coerceAtLeast(0f)
        val horizontalCapacity = if (movesLeft) anchorX else boundsWidth - anchorX
        val verticalHalfCapacity = min(selection.centerY, boundsHeight - selection.centerY)
        val maxWidth = min(horizontalCapacity, verticalHalfCapacity * 2f * aspectRatio).coerceAtLeast(0f)
        val minWidth = max(minimumSide, minimumSide * aspectRatio)
        val width = clampSize(desiredWidth, minWidth, maxWidth)
        val height = width / aspectRatio
        return CropSelection(
            left = if (movesLeft) anchorX - width else anchorX,
            top = selection.centerY - height / 2f,
            right = if (movesLeft) anchorX else anchorX + width,
            bottom = selection.centerY + height / 2f,
        )
    }

    private fun resizeVerticalEdge(
        selection: CropSelection,
        handle: CropResizeHandle,
        deltaY: Float,
        boundsWidth: Float,
        boundsHeight: Float,
        aspectRatio: Float,
        minimumSide: Float,
    ): CropSelection {
        val movesTop = handle == CropResizeHandle.TOP
        val anchorY = if (movesTop) selection.bottom else selection.top
        val movingY = (if (movesTop) selection.top else selection.bottom) + deltaY
        val desiredHeight = (if (movesTop) anchorY - movingY else movingY - anchorY).coerceAtLeast(0f)
        val verticalCapacity = if (movesTop) anchorY else boundsHeight - anchorY
        val horizontalHalfCapacity = min(selection.centerX, boundsWidth - selection.centerX)
        val maxHeight = min(verticalCapacity, horizontalHalfCapacity * 2f / aspectRatio).coerceAtLeast(0f)
        val minHeight = max(minimumSide, minimumSide / aspectRatio)
        val height = clampSize(desiredHeight, minHeight, maxHeight)
        val width = height * aspectRatio
        return CropSelection(
            left = selection.centerX - width / 2f,
            top = if (movesTop) anchorY - height else anchorY,
            right = selection.centerX + width / 2f,
            bottom = if (movesTop) anchorY else anchorY + height,
        )
    }

    private fun move(
        selection: CropSelection,
        deltaX: Float,
        deltaY: Float,
        boundsWidth: Float,
        boundsHeight: Float,
    ): CropSelection {
        val left = (selection.left + deltaX).coerceIn(0f, boundsWidth - selection.width)
        val top = (selection.top + deltaY).coerceIn(0f, boundsHeight - selection.height)
        return CropSelection(left, top, left + selection.width, top + selection.height)
    }

    private fun fromCenter(centerX: Float, centerY: Float, width: Float, height: Float) = CropSelection(
        left = centerX - width / 2f,
        top = centerY - height / 2f,
        right = centerX + width / 2f,
        bottom = centerY + height / 2f,
    )

    private fun clampSize(desired: Float, minimum: Float, maximum: Float): Float {
        val safeMaximum = maximum.coerceAtLeast(0f)
        return desired.coerceIn(min(minimum, safeMaximum), safeMaximum)
    }

    private fun square(value: Float): Float = value * value

    private fun validateSelection(selection: CropSelection) {
        require(
            selection.left.isFinite() && selection.top.isFinite() &&
                selection.right.isFinite() && selection.bottom.isFinite() &&
                selection.width > 0f && selection.height > 0f,
        ) { "Crop selection must be finite and non-empty" }
    }

    private fun validateBounds(width: Float, height: Float) {
        require(width.isFinite() && height.isFinite() && width > 0f && height > 0f) {
            "Crop bounds must be finite and non-empty"
        }
    }

    private fun validateAspectRatio(aspectRatio: Float) {
        require(aspectRatio.isFinite() && aspectRatio > 0f) { "Crop aspect ratio must be positive" }
    }
}
