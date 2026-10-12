package io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import io.github.supermonster003.autojs6.plugin.three.maple.image.EditorToolState
import io.github.supermonster003.autojs6.plugin.three.maple.image.ImageEditingEngine
import io.github.supermonster003.autojs6.plugin.three.maple.image.ImageEditingView
import io.github.supermonster003.autojs6.plugin.three.maple.image.R

/** First-level tool groups of the editor (design draft 7.5). */
internal enum class EditorGroup { TRANSFORM, ADJUST, DRAW, TEXT }

internal object EditorScreenDefaults {
    val ADJUSTMENT_RANGE = -100..100
    val ROTATION_RANGE = -45..45
    const val BRUSH_WIDTH_MAX = 46
    const val MOSAIC_STRENGTH_MAX = 28
}

/** Everything the editor chrome renders; the Activity keeps the tool state machine and mutates this holder. */
internal data class EditorUiModel(
    val title: String = "",
    val busy: Boolean = false,
    val loadFailed: Boolean = false,
    val ready: Boolean = false,
    val canSave: Boolean = false,
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val canRestoreOriginal: Boolean = false,
    val toolState: EditorToolState = EditorToolState.NORMAL,
    val group: EditorGroup = EditorGroup.TRANSFORM,
    val adjustmentTitle: String = "",
    val adjustmentValue: Int = 0,
    val rotationDegrees: Int = 0,
    val cropPresetIndex: Int = 0,
    val cropPresetLabels: List<String> = emptyList(),
    val interactionHelp: String = "",
    val brushToolIndex: Int = 0,
    val brushToolLabels: List<String> = emptyList(),
    val brushColor: Int = 0xFFFF0000.toInt(),
    val brushColorVisible: Boolean = true,
    val brushWidthProgress: Int = 6,
    val mosaicStrengthVisible: Boolean = false,
    val mosaicStrengthProgress: Int = 8,
    val mosaicStrengthLabel: String = "",
)

internal class EditorCallbacks(
    val onBack: () -> Unit,
    val onSave: () -> Unit,
    val onUndo: () -> Unit,
    val onRedo: () -> Unit,
    val onRestoreOriginal: () -> Unit,
    val onReleaseHistory: () -> Unit,
    val onGroup: (EditorGroup) -> Unit,
    val onCrop: () -> Unit,
    val onRotateLeft: () -> Unit,
    val onRotateRight: () -> Unit,
    val onRotateFine: () -> Unit,
    val onFlipHorizontal: () -> Unit,
    val onFlipVertical: () -> Unit,
    val onAdjustment: (ImageEditingEngine.Adjustment) -> Unit,
    val onBrush: () -> Unit,
    val onText: () -> Unit,
    val onAdjustmentValue: (Int) -> Unit,
    val onRotationValue: (Int) -> Unit,
    val onCropPreset: (Int) -> Unit,
    val onBrushTool: (Int) -> Unit,
    val onBrushWidth: (Int) -> Unit,
    val onMosaicStrength: (Int) -> Unit,
    val onBrushColor: () -> Unit,
    val onConfirmTool: () -> Unit,
    val onCancelTool: () -> Unit,
)

/**
 * Editor page (design draft 7.5): overlay top bar with undo / redo / save, the editing canvas hosted through
 * AndroidView, and a bottom area that shows either the two tool strips (secondary tools of the selected group
 * over the group row) or the parameter panel of the active tool.
 */
@Composable
internal fun EditorScreen(
    model: EditorUiModel,
    editingView: ImageEditingView,
    callbacks: EditorCallbacks,
    dialogs: @Composable () -> Unit,
) {
    val canvas = MapleTheme.canvas
    Column(modifier = Modifier.fillMaxSize().background(canvas.editorCanvas)) {
        EditorTopBar(model, callbacks)
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            AndroidView(factory = { editingView }, modifier = Modifier.fillMaxSize())
            if (model.busy) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter).testTag("maple.editor.progress"),
                    color = canvas.accentOnScrim,
                    trackColor = canvas.accentOnScrim.copy(alpha = 0.3f),
                )
            }
            if (model.loadFailed) {
                Text(
                    text = stringResource(R.string.text_cannot_read_file),
                    color = canvas.onScrim,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.align(Alignment.Center).padding(24.dp).testTag("maple.editor.error"),
                )
            }
        }
        Column(modifier = Modifier.fillMaxWidth().background(canvas.scrimStrong).windowInsetsPadding(WindowInsets.navigationBars)) {
            when (model.toolState) {
                EditorToolState.NORMAL -> {
                    SecondaryTools(model, callbacks)
                    PrimaryGroups(model, callbacks)
                }
                EditorToolState.ADJUSTMENT -> MapleParameterPanel(
                    onCancel = callbacks.onCancelTool,
                    onConfirm = callbacks.onConfirmTool,
                    cancelText = stringResource(R.string.dialog_button_cancel),
                    confirmText = stringResource(R.string.dialog_button_confirm),
                ) {
                    val value = model.adjustmentValue
                    MapleSliderRow(
                        title = model.adjustmentTitle,
                        value = value,
                        range = EditorScreenDefaults.ADJUSTMENT_RANGE,
                        onValueChange = callbacks.onAdjustmentValue,
                        valueText = if (value > 0) "+$value" else value.toString(),
                        centered = true,
                        modifier = Modifier.testTag("maple.editor.adjustment"),
                    )
                }
                EditorToolState.ROTATION -> MapleParameterPanel(
                    onCancel = callbacks.onCancelTool,
                    onConfirm = callbacks.onConfirmTool,
                    cancelText = stringResource(R.string.dialog_button_cancel),
                    confirmText = stringResource(R.string.dialog_button_confirm),
                ) {
                    MapleSliderRow(
                        title = stringResource(R.string.image_editor_rotation_angle),
                        value = model.rotationDegrees,
                        range = EditorScreenDefaults.ROTATION_RANGE,
                        onValueChange = callbacks.onRotationValue,
                        valueText = stringResource(R.string.image_editor_rotation_degrees, model.rotationDegrees),
                        centered = true,
                        modifier = Modifier.testTag("maple.editor.rotation"),
                    )
                }
                EditorToolState.CROP -> MapleParameterPanel(
                    onCancel = callbacks.onCancelTool,
                    onConfirm = callbacks.onConfirmTool,
                    cancelText = stringResource(R.string.dialog_button_cancel),
                    confirmText = stringResource(R.string.dialog_button_confirm),
                ) {
                    Text(
                        text = stringResource(R.string.image_editor_crop_aspect_ratio),
                        style = MaterialTheme.typography.bodyMedium,
                        color = canvas.onScrimMuted,
                        modifier = Modifier.padding(bottom = 4.dp),
                    )
                    MapleChipRow(options = model.cropPresetLabels, selectedIndex = model.cropPresetIndex, onSelect = callbacks.onCropPreset)
                    HelpText(model.interactionHelp)
                }
                EditorToolState.TEXT -> MapleParameterPanel(
                    onCancel = callbacks.onCancelTool,
                    onConfirm = callbacks.onConfirmTool,
                    cancelText = stringResource(R.string.dialog_button_cancel),
                    confirmText = stringResource(R.string.dialog_button_confirm),
                ) {
                    HelpText(model.interactionHelp)
                }
                EditorToolState.BRUSH -> MapleParameterPanel(
                    onCancel = callbacks.onCancelTool,
                    onConfirm = callbacks.onConfirmTool,
                    cancelText = stringResource(R.string.dialog_button_cancel),
                    confirmText = stringResource(R.string.dialog_button_confirm),
                ) {
                    MapleSegmented(options = model.brushToolLabels, selectedIndex = model.brushToolIndex, onSelect = callbacks.onBrushTool)
                    if (model.brushColorVisible) {
                        MapleColorButton(
                            color = model.brushColor,
                            label = stringResource(R.string.text_color_palette),
                            onClick = callbacks.onBrushColor,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                    MapleSliderRow(
                        title = stringResource(R.string.image_editor_brush_width),
                        value = model.brushWidthProgress,
                        range = 0..EditorScreenDefaults.BRUSH_WIDTH_MAX,
                        onValueChange = callbacks.onBrushWidth,
                        modifier = Modifier.testTag("maple.editor.brushWidth"),
                    )
                    if (model.mosaicStrengthVisible) {
                        MapleSliderRow(
                            title = stringResource(R.string.image_editor_mosaic_strength),
                            value = model.mosaicStrengthProgress,
                            range = 0..EditorScreenDefaults.MOSAIC_STRENGTH_MAX,
                            onValueChange = callbacks.onMosaicStrength,
                            valueText = model.mosaicStrengthLabel,
                            modifier = Modifier.testTag("maple.editor.mosaicStrength"),
                        )
                    }
                }
            }
        }
    }
    dialogs()
}

@Composable
private fun HelpText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MapleTheme.canvas.onScrimMuted,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).testTag("maple.editor.help"),
    )
}

@Composable
private fun EditorTopBar(model: EditorUiModel, callbacks: EditorCallbacks) {
    var menuOpen by remember { mutableStateOf(false) }
    val canvas = MapleTheme.canvas
    MapleTopBar(
        title = model.title,
        subtitle = stringResource(R.string.text_edit),
        navigationIcon = painterResource(R.drawable.ic_arrow_back_24),
        navigationContentDescription = stringResource(R.string.action_back),
        onNavigationClick = callbacks.onBack,
        overlay = true,
        actions = {
            IconButton(onClick = callbacks.onUndo, enabled = model.canUndo, modifier = Modifier.testTag("maple.editor.undo")) {
                Icon(painterResource(R.drawable.ic_undo_24), contentDescription = stringResource(R.string.text_undo),
                    tint = if (model.canUndo) canvas.onScrim else canvas.onScrimDisabled)
            }
            IconButton(onClick = callbacks.onRedo, enabled = model.canRedo, modifier = Modifier.testTag("maple.editor.redo")) {
                Icon(painterResource(R.drawable.ic_redo_24), contentDescription = stringResource(R.string.text_redo),
                    tint = if (model.canRedo) canvas.onScrim else canvas.onScrimDisabled)
            }
            IconButton(onClick = callbacks.onSave, enabled = model.canSave, modifier = Modifier.testTag("maple.editor.save")) {
                Icon(painterResource(R.drawable.ic_save_24), contentDescription = stringResource(R.string.text_save),
                    tint = if (model.canSave) canvas.accentOnScrim else canvas.onScrimDisabled)
            }
            IconButton(onClick = { menuOpen = true }, modifier = Modifier.testTag("maple.editor.more")) {
                Icon(painterResource(R.drawable.ic_more_vert_24), contentDescription = stringResource(R.string.action_more))
            }
            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.image_editor_restore_original)) },
                    enabled = model.canRestoreOriginal,
                    onClick = { menuOpen = false; callbacks.onRestoreOriginal() },
                    modifier = Modifier.testTag("maple.editor.menu.restore"),
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.release_history)) },
                    onClick = { menuOpen = false; callbacks.onReleaseHistory() },
                    modifier = Modifier.testTag("maple.editor.menu.releaseHistory"),
                )
            }
        },
    )
}

@Composable
private fun SecondaryTools(model: EditorUiModel, callbacks: EditorCallbacks) {
    val enabled = model.ready && !model.busy
    val tools = when (model.group) {
        EditorGroup.TRANSFORM -> listOf(
            MapleTool("crop", painterResource(R.drawable.ic_crop_24), stringResource(R.string.image_editor_crop), callbacks.onCrop),
            MapleTool("rotateLeft", painterResource(R.drawable.ic_rotate_left_24), stringResource(R.string.image_editor_rotate_left), callbacks.onRotateLeft),
            MapleTool("rotateRight", painterResource(R.drawable.ic_rotate_right_24), stringResource(R.string.image_editor_rotate_right), callbacks.onRotateRight),
            MapleTool("rotateFine", painterResource(R.drawable.ic_rotate_fine_24), stringResource(R.string.image_editor_rotate_fine), callbacks.onRotateFine),
            MapleTool("flipHorizontal", painterResource(R.drawable.ic_flip_horizontal_24), stringResource(R.string.image_editor_flip_horizontal), callbacks.onFlipHorizontal),
            MapleTool("flipVertical", painterResource(R.drawable.ic_flip_vertical_24), stringResource(R.string.image_editor_flip_vertical), callbacks.onFlipVertical),
        )
        EditorGroup.ADJUST -> listOf(
            MapleTool("brightness", painterResource(R.drawable.ic_brightness_24), stringResource(R.string.image_editor_brightness), onClick = { callbacks.onAdjustment(ImageEditingEngine.Adjustment.BRIGHTNESS) }),
            MapleTool("contrast", painterResource(R.drawable.ic_contrast_24), stringResource(R.string.image_editor_contrast), onClick = { callbacks.onAdjustment(ImageEditingEngine.Adjustment.CONTRAST) }),
            MapleTool("saturation", painterResource(R.drawable.ic_saturation_24), stringResource(R.string.image_editor_saturation), onClick = { callbacks.onAdjustment(ImageEditingEngine.Adjustment.SATURATION) }),
            MapleTool("temperature", painterResource(R.drawable.ic_temperature_24), stringResource(R.string.image_editor_temperature), onClick = { callbacks.onAdjustment(ImageEditingEngine.Adjustment.TEMPERATURE) }),
        )
        EditorGroup.DRAW -> listOf(
            MapleTool("brush", painterResource(R.drawable.ic_brush_24), stringResource(R.string.image_editor_brush), callbacks.onBrush),
        )
        EditorGroup.TEXT -> listOf(
            MapleTool("text", painterResource(R.drawable.ic_text_24), stringResource(R.string.image_editor_text), callbacks.onText),
        )
    }
    MapleToolStrip(tools = tools, size = MapleToolStripSize.SECONDARY, enabled = enabled, modifier = Modifier.testTag("maple.editor.tools"))
}

@Composable
private fun PrimaryGroups(model: EditorUiModel, callbacks: EditorCallbacks) {
    val groups = listOf(
        MapleTool("group.transform", painterResource(R.drawable.ic_transform_24), stringResource(R.string.image_editor_group_transform), { callbacks.onGroup(EditorGroup.TRANSFORM) }, selected = model.group == EditorGroup.TRANSFORM),
        MapleTool("group.adjust", painterResource(R.drawable.ic_tune_24), stringResource(R.string.image_editor_group_adjust), { callbacks.onGroup(EditorGroup.ADJUST) }, selected = model.group == EditorGroup.ADJUST),
        MapleTool("group.draw", painterResource(R.drawable.ic_brush_24), stringResource(R.string.image_editor_group_draw), { callbacks.onGroup(EditorGroup.DRAW) }, selected = model.group == EditorGroup.DRAW),
        MapleTool("group.text", painterResource(R.drawable.ic_text_24), stringResource(R.string.image_editor_text), { callbacks.onGroup(EditorGroup.TEXT) }, selected = model.group == EditorGroup.TEXT),
    )
    Row(modifier = Modifier.fillMaxWidth()) {
        MapleToolStrip(tools = groups, size = MapleToolStripSize.PRIMARY, modifier = Modifier.testTag("maple.editor.groups"))
    }
}
