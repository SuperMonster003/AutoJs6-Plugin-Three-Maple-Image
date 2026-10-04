package io.github.supermonster003.autojs6.plugin.three.maple.image

internal enum class EditorToolState {
    NORMAL,
    ADJUSTMENT,
    ROTATION,
    CROP,
    BRUSH,
    TEXT,
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
    val activeAdjustment: ImageEditingEngine.Adjustment? = null,
    val adjustmentProgress: Int = 100,
    val rotationDegrees: Int = 0,
    val brushColor: Int = android.graphics.Color.RED,
    val brushTool: BrushTool = BrushTool.PEN,
    val brushWidths: BrushWidthMemory = BrushWidthMemory(),
    val mosaicStrengthProgress: Int = 8,
    val cropAspectRatioPreset: CropAspectRatioPreset = CropAspectRatioPreset.FREE,
    val canvasState: ImageEditingView.CanvasState = ImageEditingView.CanvasState.Normal,
    val textStyleSettings: EditorTextStyleSettings = EditorTextStyleSettings(),
    val textDialogDraft: EditorTextDialogDraft? = null,
    val saveOptions: EditorSaveOptions = EditorSaveOptions(),
    val saveDialogDraft: EditorSaveOptions? = null,
    val colorDialogDraft: EditorColorDialogDraft? = null,
    val exitConfirmationVisible: Boolean = false,
)
