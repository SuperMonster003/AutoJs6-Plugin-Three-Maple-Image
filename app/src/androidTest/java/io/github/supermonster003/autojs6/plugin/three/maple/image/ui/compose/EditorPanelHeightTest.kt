package io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import io.github.supermonster003.autojs6.plugin.three.maple.image.EditorToolState
import io.github.supermonster003.autojs6.plugin.three.maple.image.ImageEditingView
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * A phone in landscape is about 360 dp high: the open tool's panel must leave the canvas visible and keep its
 * confirm button on screen (the shapes panel used to fill the whole page below the top bar, G8441 2026-10-15).
 */
class EditorPanelHeightTest {

    @get:Rule
    val compose = createComposeRule()

    @Test fun theShapesPanelLeavesTheCanvasAndItsButtonsOnALandscapePhone() {
        compose.setContent {
            // The view makes its gesture handlers on the main thread.
            val context = LocalContext.current
            val view = remember { ImageEditingView(context) }
            MapleTheme(palette = testPalette()) {
                Box(Modifier.size(width = 640.dp, height = 360.dp)) {
                    EditorScreen(
                        model = EditorUiModel(
                            title = "photo.png",
                            ready = true,
                            toolState = EditorToolState.SHAPES,
                            group = EditorGroup.ANNOTATE,
                            shapeKindLabels = listOf("Rectangle", "Ellipse", "Arrow", "Line"),
                            interactionHelp = "Drag to draw a shape, tap one to select it, then drag it or its handles",
                        ),
                        editingView = view,
                        callbacks = noCallbacks(),
                        dialogs = {},
                    )
                }
            }
        }
        compose.waitForIdle()

        val confirm = compose.onNodeWithTag("maple.panel.confirm").getBoundsInRoot()
        val canvas = compose.onNodeWithTag("maple.editor.canvas").getBoundsInRoot()
        assertTrue("confirm ends at ${confirm.bottom}", confirm.bottom <= 360.dp)
        assertTrue("canvas is ${canvas.bottom - canvas.top} high", canvas.bottom - canvas.top >= 80.dp)
    }

    private fun noCallbacks() = EditorCallbacks(
        onBack = {}, onSave = {}, onUndo = {}, onRedo = {}, onCompare = {}, onUndoReplacement = {},
        onRestoreOriginal = {}, onReleaseHistory = {}, onGroup = {}, onCrop = {}, onResize = {}, onRotateLeft = {},
        onRotateRight = {}, onRotateFine = {}, onFlipHorizontal = {}, onFlipVertical = {}, onAdjustment = {},
        onFilterTool = {}, onLookTab = {}, onFilter = {}, onFilterIntensity = {}, onLookAdjustment = {}, onBrush = {},
        onText = {}, onAdjustmentValue = {}, onRotationValue = {}, onCropPreset = {}, onBrushTool = {}, onBrushWidth = {},
        onMosaicStrength = {}, onBrushColor = {}, onShapeTool = {}, onShapeKind = {}, onShapeFilled = {},
        onShapeWidth = {}, onShapeColor = {}, onDeleteShape = {}, onConfirmTool = {}, onCancelTool = {},
    )
}
