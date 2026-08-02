package io.github.supermonster003.autojs6.plugin.imagetools

internal enum class EditorToolState {
    NORMAL,
    ADJUSTMENT,
    CROP,
    BRUSH,
    TEXT,
}

internal enum class EditorColorTarget {
    BRUSH,
    TEXT,
}

internal data class EditorTextDialogDraft(
    val text: String,
    val selectedColor: Int,
    val sizeProgress: Int,
)

internal data class EditorColorDialogDraft(
    val target: EditorColorTarget,
    val input: String,
)

internal data class EditorUiState(
    val toolState: EditorToolState = EditorToolState.NORMAL,
    val activeAdjustment: ImageEditingEngine.Adjustment? = null,
    val adjustmentProgress: Int = 100,
    val brushColor: Int = android.graphics.Color.RED,
    val brushWidthProgress: Int = 0,
    val canvasState: ImageEditingView.CanvasState = ImageEditingView.CanvasState.Normal,
    val textDialogDraft: EditorTextDialogDraft? = null,
    val colorDialogDraft: EditorColorDialogDraft? = null,
    val exitConfirmationVisible: Boolean = false,
)
