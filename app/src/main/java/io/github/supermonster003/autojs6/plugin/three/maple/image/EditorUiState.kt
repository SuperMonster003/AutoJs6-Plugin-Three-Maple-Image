package io.github.supermonster003.autojs6.plugin.three.maple.image

import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.EditorGroup

internal enum class EditorToolState {
    NORMAL,
    /** The look panel (ROADMAP 8.1): filter and adjustments, previewed together. */
    LOOK,
    ROTATION,
    CROP,
    BRUSH,
    TEXT,
    /** Annotation shapes (ROADMAP 8.1): drawn, selected and adjusted on the canvas, applied together. */
    SHAPES,
}

internal enum class EditorColorTarget {
    BRUSH,
    TEXT,
}

internal data class EditorTextStyleSettings(
    val selectedColor: Int = android.graphics.Color.WHITE,
    val sizeProgress: Int = 4,
    val outlineEnabled: Boolean = false,
    val shadowEnabled: Boolean = false,
    val rotationDegrees: Int = 0,
) {
    init {
        require(sizeProgress >= 0) { "Text size progress must not be negative" }
        require(rotationDegrees in -180..180) { "Text rotation must be between -180 and 180 degrees" }
    }
}

internal data class EditorTextDialogDraft(
    val text: String,
    val style: EditorTextStyleSettings,
)

internal data class EditorColorDialogDraft(
    val target: EditorColorTarget,
    val input: String,
)

internal data class BrushWidthMemory(
    val pen: Int = 6,
    val highlighter: Int = 14,
    val mosaic: Int = 18,
    val eraser: Int = 14,
) {
    init {
        require(listOf(pen, highlighter, mosaic, eraser).all { it >= 0 }) {
            "Brush width progress must not be negative"
        }
    }

    fun progress(tool: BrushTool): Int = when (tool) {
        BrushTool.PEN -> pen
        BrushTool.HIGHLIGHTER -> highlighter
        BrushTool.MOSAIC -> mosaic
        BrushTool.ERASER -> eraser
    }

    fun withProgress(tool: BrushTool, progress: Int): BrushWidthMemory {
        require(progress >= 0) { "Brush width progress must not be negative" }
        return when (tool) {
            BrushTool.PEN -> copy(pen = progress)
            BrushTool.HIGHLIGHTER -> copy(highlighter = progress)
            BrushTool.MOSAIC -> copy(mosaic = progress)
            BrushTool.ERASER -> copy(eraser = progress)
        }
    }
}

internal data class EditorUiState(
    val toolState: EditorToolState = EditorToolState.NORMAL,
    val look: EditorLook = EditorLook(),
    val lookTab: Int = 0,
    val lookAdjustment: LookAdjustment = LookAdjustment.BRIGHTNESS,
    val rotationDegrees: Int = 0,
    val brushColor: Int = android.graphics.Color.RED,
    val brushTool: BrushTool = BrushTool.PEN,
    val brushWidths: BrushWidthMemory = BrushWidthMemory(),
    val mosaicStrengthProgress: Int = 8,
    val cropAspectRatioPreset: CropAspectRatioPreset = CropAspectRatioPreset.FREE,
    val canvasState: ImageEditingView.CanvasState = ImageEditingView.CanvasState.Normal,
    val group: EditorGroup = EditorGroup.TRANSFORM,
    val textStyleSettings: EditorTextStyleSettings = EditorTextStyleSettings(),
    val textDialogDraft: EditorTextDialogDraft? = null,
    val saveOptions: EditorSaveOptions = EditorSaveOptions(),
    val saveDialogDraft: EditorSaveOptions? = null,
    val colorDialogDraft: EditorColorDialogDraft? = null,
    val resizeDialogDraft: EditorResizeDraft? = null,
    val shapeKind: ShapeKind = ShapeKind.RECTANGLE,
    val shapeFilled: Boolean = false,
    val exitConfirmationVisible: Boolean = false,
)
