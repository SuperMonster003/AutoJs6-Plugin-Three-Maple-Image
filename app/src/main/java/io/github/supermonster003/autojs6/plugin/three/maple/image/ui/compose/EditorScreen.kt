package io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.defaultMinSize
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
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import io.github.supermonster003.autojs6.plugin.three.maple.image.EditorToolState
import io.github.supermonster003.autojs6.plugin.three.maple.image.LookAdjustment
import io.github.supermonster003.autojs6.plugin.three.maple.image.ImageEditingView
import io.github.supermonster003.autojs6.plugin.three.maple.image.R
import io.github.supermonster003.autojs6.plugin.three.maple.image.ShapeKind

/** First-level tool groups of the editor (design draft 7.5; filters added in ROADMAP 8.1). */
internal enum class EditorGroup { TRANSFORM, ADJUST, FILTER, DRAW, ANNOTATE, TEXT }

internal object EditorScreenDefaults {
    val ADJUSTMENT_RANGE = -100..100
    val ROTATION_RANGE = -45..45
    const val BRUSH_WIDTH_MAX = 46
    const val MOSAIC_STRENGTH_MAX = 28
    val FILTER_THUMBNAIL = 56.dp
    val FILTER_ITEM_WIDTH = 64.dp
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
    val canCompare: Boolean = false,
    /** The original is shown while the compare button is held. */
    val comparing: Boolean = false,
    val toolState: EditorToolState = EditorToolState.NORMAL,
    val group: EditorGroup = EditorGroup.TRANSFORM,
    val adjustmentTitle: String = "",
    val adjustmentValue: Int = 0,
    val adjustmentRange: IntRange = EditorScreenDefaults.ADJUSTMENT_RANGE,
    val adjustmentLabels: List<String> = emptyList(),
    val lookAdjustmentIndex: Int = 0,
    /** 0 = filter tab, 1 = adjust tab of the look panel. */
    val lookTab: Int = 0,
    val filterLabels: List<String> = emptyList(),
    /** One thumbnail per filter (empty while they are being made). */
    val filterThumbnails: List<ImageBitmap> = emptyList(),
    val filterIndex: Int = 0,
    val filterIntensity: Int = 100,
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
    val shapeKindLabels: List<String> = emptyList(),
    val shapeKindIndex: Int = 0,
    val shapeFillVisible: Boolean = true,
    val shapeFilled: Boolean = false,
    val shapeWidthProgress: Int = 6,
    /** A shape is selected, so it can be deleted (and color, width and fill change it). */
    val shapeSelected: Boolean = false,
    /** The host overwrote the original; the bar offers its undo until the editor closes (ROADMAP 8.2). */
    val replacementUndo: Boolean = false,
    val replacementUndoBusy: Boolean = false,
)

internal class EditorCallbacks(
    val onBack: () -> Unit,
    val onSave: () -> Unit,
    val onUndo: () -> Unit,
    val onRedo: () -> Unit,
    /** True while the compare button is held (or switched on through accessibility), false when it is let go. */
    val onCompare: (Boolean) -> Unit,
    val onUndoReplacement: () -> Unit,
    val onRestoreOriginal: () -> Unit,
    val onReleaseHistory: () -> Unit,
    val onGroup: (EditorGroup) -> Unit,
    val onCrop: () -> Unit,
    val onResize: () -> Unit,
    val onRotateLeft: () -> Unit,
    val onRotateRight: () -> Unit,
    val onRotateFine: () -> Unit,
    val onFlipHorizontal: () -> Unit,
    val onFlipVertical: () -> Unit,
    val onAdjustment: (LookAdjustment) -> Unit,
    val onFilterTool: (Int) -> Unit,
    val onLookTab: (Int) -> Unit,
    val onFilter: (Int) -> Unit,
    val onFilterIntensity: (Int) -> Unit,
    val onLookAdjustment: (Int) -> Unit,
    val onBrush: () -> Unit,
    val onText: () -> Unit,
    val onAdjustmentValue: (Int) -> Unit,
    val onRotationValue: (Int) -> Unit,
    val onCropPreset: (Int) -> Unit,
    val onBrushTool: (Int) -> Unit,
    val onBrushWidth: (Int) -> Unit,
    val onMosaicStrength: (Int) -> Unit,
    val onBrushColor: () -> Unit,
    val onShapeTool: (Int) -> Unit,
    val onShapeKind: (Int) -> Unit,
    val onShapeFilled: (Boolean) -> Unit,
    val onShapeWidth: (Int) -> Unit,
    val onShapeColor: () -> Unit,
    val onDeleteShape: () -> Unit,
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
            AndroidView(factory = { editingView }, modifier = Modifier.fillMaxSize().testTag("maple.editor.canvas"))
            if (model.busy) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter).testTag("maple.editor.progress"),
                    color = canvas.accentOnScrim,
                    trackColor = canvas.accentOnScrim.copy(alpha = 0.3f),
                )
            }
            if (model.comparing) {
                Text(
                    text = stringResource(R.string.image_editor_original),
                    color = canvas.onScrim,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 12.dp)
                        .background(canvas.scrimStrong, RoundedCornerShape(16.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .testTag("maple.editor.originalLabel"),
                )
            }
            if (model.replacementUndo) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                        .fillMaxWidth()
                        .background(canvas.scrimStrong, RoundedCornerShape(12.dp))
                        .padding(start = 16.dp, end = 8.dp)
                        .testTag("maple.editor.replacementUndo"),
                ) {
                    Text(
                        text = stringResource(R.string.overwrite_saved),
                        color = canvas.onScrim,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f),
                    )
                    androidx.compose.material3.TextButton(
                        onClick = callbacks.onUndoReplacement,
                        enabled = !model.replacementUndoBusy,
                        modifier = Modifier.defaultMinSize(minHeight = 48.dp).testTag("maple.editor.replacementUndo.action"),
                    ) { Text(stringResource(R.string.action_undo), color = canvas.accentOnScrim) }
                }
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
                EditorToolState.LOOK -> MapleParameterPanel(
                    onCancel = callbacks.onCancelTool,
                    onConfirm = callbacks.onConfirmTool,
                    cancelText = stringResource(R.string.dialog_button_cancel),
                    confirmText = stringResource(R.string.dialog_button_confirm),
                ) {
                    LookPanel(model, callbacks)
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
                    MapleSegmented(options = model.brushToolLabels, selectedIndex = model.brushToolIndex, onSelect = callbacks.onBrushTool, onDark = true)
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
                EditorToolState.SHAPES -> MapleParameterPanel(
                    onCancel = callbacks.onCancelTool,
                    onConfirm = callbacks.onConfirmTool,
                    cancelText = stringResource(R.string.dialog_button_cancel),
                    confirmText = stringResource(R.string.dialog_button_confirm),
                ) {
                    ShapesPanel(model, callbacks)
                }
            }
        }
    }
    dialogs()
}

/**
 * Look panel (ROADMAP 8.1): the filter tab (thumbnail strip and intensity) and the adjust tab (one chip per
 * adjustment and its slider) share one pending look, so a filter and adjustments are previewed and applied together.
 */
@Composable
private fun LookPanel(model: EditorUiModel, callbacks: EditorCallbacks) {
    MapleSegmented(
        options = listOf(stringResource(R.string.image_editor_group_filter), stringResource(R.string.image_editor_group_adjust)),
        selectedIndex = model.lookTab,
        onSelect = callbacks.onLookTab,
        tagPrefix = "maple.editor.lookTab",
        onDark = true,
    )
    Spacer(Modifier.height(4.dp))
    if (model.lookTab == 0) {
        FilterStrip(model, selectedIndex = model.filterIndex, onSelect = callbacks.onFilter)
        if (model.filterIndex > 0) {
            MapleSliderRow(
                title = stringResource(R.string.image_editor_filter_intensity),
                value = model.filterIntensity,
                range = 0..100,
                onValueChange = callbacks.onFilterIntensity,
                modifier = Modifier.testTag("maple.editor.filterIntensity"),
            )
        }
    } else {
        MapleChipRow(
            options = model.adjustmentLabels,
            selectedIndex = model.lookAdjustmentIndex,
            onSelect = callbacks.onLookAdjustment,
            modifier = Modifier.testTag("maple.editor.adjustments"),
        )
        val value = model.adjustmentValue
        val centered = model.adjustmentRange.first < 0
        MapleSliderRow(
            title = model.adjustmentTitle,
            value = value,
            range = model.adjustmentRange,
            onValueChange = callbacks.onAdjustmentValue,
            valueText = if (centered && value > 0) "+$value" else value.toString(),
            centered = centered,
            modifier = Modifier.testTag("maple.editor.adjustment"),
        )
    }
}

/** Filter thumbnails with their names; [selectedIndex] -1 marks none (the filter group's tool strip). */
@Composable
private fun FilterStrip(model: EditorUiModel, selectedIndex: Int, onSelect: (Int) -> Unit, enabled: Boolean = true) {
    val canvas = MapleTheme.canvas
    LazyRow(
        modifier = Modifier.fillMaxWidth().testTag("maple.editor.filters"),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(6.dp),
    ) {
        itemsIndexed(model.filterLabels) { index, label ->
            val selected = index == selectedIndex
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .width(EditorScreenDefaults.FILTER_ITEM_WIDTH)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(enabled = enabled) { onSelect(index) }
                    .padding(vertical = 4.dp)
                    .testTag("maple.editor.filter.$index"),
            ) {
                val thumbnail = model.filterThumbnails.getOrNull(index)
                val frame = Modifier
                    .size(EditorScreenDefaults.FILTER_THUMBNAIL)
                    .clip(RoundedCornerShape(6.dp))
                    .border(2.dp, if (selected) canvas.accentOnScrim else canvas.onScrim.copy(alpha = 0f), RoundedCornerShape(6.dp))
                if (thumbnail != null) {
                    Image(bitmap = thumbnail, contentDescription = null, contentScale = ContentScale.Crop, modifier = frame)
                } else {
                    Box(modifier = frame.background(canvas.onScrim.copy(alpha = 0.12f)))
                }
                Text(
                    text = label,
                    color = if (selected) canvas.accentOnScrim else canvas.onScrim,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
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
            CompareButton(model, callbacks)
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

/**
 * Annotation shapes (ROADMAP 8.1): the kind of the next shape, color and line width (shared with the brush), fill
 * for rectangles and ellipses, and deleting the selected shape.
 */
@Composable
private fun ShapesPanel(model: EditorUiModel, callbacks: EditorCallbacks) {
    val canvas = MapleTheme.canvas
    MapleSegmented(
        options = model.shapeKindLabels,
        selectedIndex = model.shapeKindIndex,
        onSelect = callbacks.onShapeKind,
        onDark = true,
        tagPrefix = "maple.editor.shapeKind",
    )
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
        MapleColorButton(
            color = model.brushColor,
            label = stringResource(R.string.text_color_palette),
            onClick = callbacks.onShapeColor,
            modifier = Modifier.weight(1f, fill = false),
        )
        Spacer(Modifier.weight(1f))
        IconButton(onClick = callbacks.onDeleteShape, enabled = model.shapeSelected, modifier = Modifier.testTag("maple.editor.deleteShape")) {
            Icon(
                painterResource(R.drawable.ic_delete_24),
                contentDescription = stringResource(R.string.image_editor_shape_delete),
                tint = if (model.shapeSelected) canvas.onScrim else canvas.onScrimDisabled,
            )
        }
    }
    if (model.shapeFillVisible) {
        MapleSegmented(
            options = listOf(stringResource(R.string.image_editor_shape_outline), stringResource(R.string.image_editor_shape_filled)),
            selectedIndex = if (model.shapeFilled) 1 else 0,
            onSelect = { callbacks.onShapeFilled(it == 1) },
            onDark = true,
            tagPrefix = "maple.editor.shapeFill",
        )
    }
    MapleSliderRow(
        title = stringResource(R.string.image_editor_line_width),
        value = model.shapeWidthProgress,
        range = 0..EditorScreenDefaults.BRUSH_WIDTH_MAX,
        onValueChange = callbacks.onShapeWidth,
        modifier = Modifier.testTag("maple.editor.shapeWidth"),
    )
    HelpText(model.interactionHelp)
}

/**
 * Shows the original while pressed (ROADMAP 8.1). It is not a click: holding is the gesture. For accessibility
 * services the click action switches the comparison on and off instead.
 */
@Composable
private fun CompareButton(model: EditorUiModel, callbacks: EditorCallbacks) {
    val canvas = MapleTheme.canvas
    val enabled = model.canCompare
    val description = stringResource(R.string.image_editor_compare)
    val currentModel by rememberUpdatedState(model)
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(if (model.comparing) canvas.accentOnScrim.copy(alpha = 0.2f) else Color.Transparent)
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput
                detectTapGestures(onPress = {
                    callbacks.onCompare(true)
                    try {
                        tryAwaitRelease()
                    } finally {
                        callbacks.onCompare(false)
                    }
                })
            }
            .semantics {
                contentDescription = description
                role = Role.Button
                if (enabled) {
                    onClick { callbacks.onCompare(!currentModel.comparing); true }
                } else {
                    disabled()
                }
            }
            .testTag("maple.editor.compare"),
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_compare_24),
            contentDescription = null,
            tint = when {
                !enabled -> canvas.onScrimDisabled
                model.comparing -> canvas.accentOnScrim
                else -> canvas.onScrim
            },
        )
    }
}

@Composable
private fun SecondaryTools(model: EditorUiModel, callbacks: EditorCallbacks) {
    val enabled = model.ready && !model.busy
    val tools = when (model.group) {
        EditorGroup.TRANSFORM -> listOf(
            MapleTool("crop", painterResource(R.drawable.ic_crop_24), stringResource(R.string.image_editor_crop), callbacks.onCrop),
            MapleTool("resize", painterResource(R.drawable.ic_resize_24), stringResource(R.string.text_image_conversion_resize), callbacks.onResize),
            MapleTool("rotateLeft", painterResource(R.drawable.ic_rotate_left_24), stringResource(R.string.image_editor_rotate_left), callbacks.onRotateLeft),
            MapleTool("rotateRight", painterResource(R.drawable.ic_rotate_right_24), stringResource(R.string.image_editor_rotate_right), callbacks.onRotateRight),
            MapleTool("rotateFine", painterResource(R.drawable.ic_rotate_fine_24), stringResource(R.string.image_editor_rotate_fine), callbacks.onRotateFine),
            MapleTool("flipHorizontal", painterResource(R.drawable.ic_flip_horizontal_24), stringResource(R.string.image_editor_flip_horizontal), callbacks.onFlipHorizontal),
            MapleTool("flipVertical", painterResource(R.drawable.ic_flip_vertical_24), stringResource(R.string.image_editor_flip_vertical), callbacks.onFlipVertical),
        )
        EditorGroup.ADJUST -> listOf(
            MapleTool("brightness", painterResource(R.drawable.ic_brightness_24), stringResource(R.string.image_editor_brightness), onClick = { callbacks.onAdjustment(LookAdjustment.BRIGHTNESS) }),
            MapleTool("contrast", painterResource(R.drawable.ic_contrast_24), stringResource(R.string.image_editor_contrast), onClick = { callbacks.onAdjustment(LookAdjustment.CONTRAST) }),
            MapleTool("saturation", painterResource(R.drawable.ic_saturation_24), stringResource(R.string.image_editor_saturation), onClick = { callbacks.onAdjustment(LookAdjustment.SATURATION) }),
            MapleTool("temperature", painterResource(R.drawable.ic_temperature_24), stringResource(R.string.image_editor_temperature), onClick = { callbacks.onAdjustment(LookAdjustment.TEMPERATURE) }),
            MapleTool("exposure", painterResource(R.drawable.ic_exposure_24), stringResource(R.string.image_editor_exposure), onClick = { callbacks.onAdjustment(LookAdjustment.EXPOSURE) }),
            MapleTool("hue", painterResource(R.drawable.ic_palette_24), stringResource(R.string.image_editor_hue), onClick = { callbacks.onAdjustment(LookAdjustment.HUE) }),
            MapleTool("highlights", painterResource(R.drawable.ic_highlights_24), stringResource(R.string.image_editor_highlights), onClick = { callbacks.onAdjustment(LookAdjustment.HIGHLIGHTS) }),
            MapleTool("shadows", painterResource(R.drawable.ic_shadows_24), stringResource(R.string.image_editor_shadows), onClick = { callbacks.onAdjustment(LookAdjustment.SHADOWS) }),
            MapleTool("sharpen", painterResource(R.drawable.ic_sharpen_24), stringResource(R.string.image_editor_sharpen), onClick = { callbacks.onAdjustment(LookAdjustment.SHARPEN) }),
            MapleTool("vignette", painterResource(R.drawable.ic_vignette_24), stringResource(R.string.image_editor_vignette), onClick = { callbacks.onAdjustment(LookAdjustment.VIGNETTE) }),
        )
        EditorGroup.FILTER -> {
            FilterStrip(model, selectedIndex = -1, onSelect = callbacks.onFilterTool, enabled = enabled)
            return
        }
        EditorGroup.DRAW -> listOf(
            MapleTool("brush", painterResource(R.drawable.ic_brush_24), stringResource(R.string.image_editor_brush), callbacks.onBrush),
        )
        EditorGroup.ANNOTATE -> listOf(
            MapleTool("shape.rectangle", painterResource(R.drawable.ic_shape_rectangle_24), stringResource(R.string.image_editor_shape_rectangle), onClick = { callbacks.onShapeTool(ShapeKind.RECTANGLE.ordinal) }),
            MapleTool("shape.ellipse", painterResource(R.drawable.ic_shape_ellipse_24), stringResource(R.string.image_editor_shape_ellipse), onClick = { callbacks.onShapeTool(ShapeKind.ELLIPSE.ordinal) }),
            MapleTool("shape.arrow", painterResource(R.drawable.ic_shape_arrow_24), stringResource(R.string.image_editor_shape_arrow), onClick = { callbacks.onShapeTool(ShapeKind.ARROW.ordinal) }),
            MapleTool("shape.line", painterResource(R.drawable.ic_shape_line_24), stringResource(R.string.image_editor_shape_line), onClick = { callbacks.onShapeTool(ShapeKind.LINE.ordinal) }),
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
        MapleTool("group.filter", painterResource(R.drawable.ic_filter_24), stringResource(R.string.image_editor_group_filter), { callbacks.onGroup(EditorGroup.FILTER) }, selected = model.group == EditorGroup.FILTER),
        MapleTool("group.draw", painterResource(R.drawable.ic_brush_24), stringResource(R.string.image_editor_group_draw), { callbacks.onGroup(EditorGroup.DRAW) }, selected = model.group == EditorGroup.DRAW),
        MapleTool("group.annotate", painterResource(R.drawable.ic_annotate_24), stringResource(R.string.image_editor_group_annotate), { callbacks.onGroup(EditorGroup.ANNOTATE) }, selected = model.group == EditorGroup.ANNOTATE),
        MapleTool("group.text", painterResource(R.drawable.ic_text_24), stringResource(R.string.image_editor_text), { callbacks.onGroup(EditorGroup.TEXT) }, selected = model.group == EditorGroup.TEXT),
    )
    Row(modifier = Modifier.fillMaxWidth()) {
        MapleToolStrip(tools = groups, size = MapleToolStripSize.PRIMARY, modifier = Modifier.testTag("maple.editor.groups"))
    }
}
