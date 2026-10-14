package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.ColorMatrix
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.EditorCallbacks
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.EditorColorDialog
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.EditorColorDialogState
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.EditorResizeDialog
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.EditorResizeDialogState
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.EditorDialogDefaults
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.EditorGroup
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.EditorSaveDialog
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.EditorSaveDialogState
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.EditorScreen
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.EditorTextDialog
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.EditorTextDialogState
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.EditorUiModel
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.MapleDialog
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.MapleTheme
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.parseEditorColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.min

/**
 * Image editor. The tool state machine, undo history, output transaction and configuration-change recovery are
 * unchanged; the chrome is a Compose [EditorScreen] driven by [EditorUiModel], the canvas stays the View based
 * [ImageEditingView], and the text / save / palette dialogs are Compose dialogs whose drafts live in this Activity
 * so they survive recreation exactly like the former AlertDialog drafts did.
 */
open class ImageEditorActivity : ConfiguredActivity() {

    private val model by viewModels<ImageEditorViewModel>()
    private lateinit var editor: ImageEditingView
    private val backPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() = requestExit()
    }
    private var request: ImageToolsRequest? = null
    private var bitmap: Bitmap? = null
    private var toolState = EditorToolState.NORMAL
    private var look = EditorLook()
    private var lookTab = LOOK_TAB_FILTER
    private var lookAdjustment = LookAdjustment.BRIGHTNESS

    /** Downscaled copy of the image the look preview renders from, made when the look panel opens. */
    private var lookPreviewSource: Bitmap? = null
    private var lookPreviewJob: Job? = null

    /** The image the filter thumbnails were made from (they are rebuilt when it changes). */
    private var thumbnailOwner: Bitmap? = null
    private var thumbnailJob: Job? = null
    private var rotationDegrees = 0
    private var brushColor = Color.RED
    private var selectedBrushTool = BrushTool.PEN
    private var brushWidths = BrushWidthMemory()
    private var mosaicStrengthProgress = DEFAULT_MOSAIC_STRENGTH_PROGRESS
    private var cropAspectRatioPreset = CropAspectRatioPreset.FREE
    private var shapeKind = ShapeKind.RECTANGLE
    private var shapeFilled = false
    private var textStyleSettings = EditorTextStyleSettings()
    private var saveOptions = EditorSaveOptions()
    private var busy = false
    private var currentStateId = INITIAL_STATE_ID
    private var savedStateId = INITIAL_STATE_ID
    private var uiStateRestored = false
    private var terminalResultHandled = false
    private var ui by mutableStateOf(EditorUiModel())
    private var textDialog by mutableStateOf<EditorTextDialogState?>(null)
    private var saveDialog by mutableStateOf<EditorSaveDialogState?>(null)
    private var colorDialog by mutableStateOf<ColorDialogHolder?>(null)
    private var resizeDialog by mutableStateOf<EditorResizeDialogState?>(null)
    private var exitConfirmationVisible by mutableStateOf(false)

    private class ColorDialogHolder(
        val state: EditorColorDialogState,
        val target: EditorColorTarget,
        val onSelected: (Int) -> Unit,
    )

    internal open fun resolveRequest(): ImageToolsRequest? = ImageToolsRequestPolicy.resolve(this, intent, ImageToolsPlugin.EDIT_ACTION_ID)

    /** The image as edited so far (test hook). */
    internal val editedBitmapForTesting: Bitmap? get() = bitmap

    /** The color of brush and shapes (test hook). */
    internal val drawingColorForTesting: Int get() = brushColor

    /** The canvas (test hook). */
    internal val editingViewForTesting: ImageEditingView get() = editor

    override val usesDarkSystemBars: Boolean get() = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(RESULT_CANCELED)
        request = resolveRequest()
        if (request == null) {
            finish()
            return
        }
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        editor = ImageEditingView(this).apply { contentDescription = getString(R.string.text_edit) }
        ui = ui.copy(
            title = request!!.displayName,
            cropPresetLabels = CropAspectRatioPreset.entries.map(::cropAspectRatioLabel),
            brushToolLabels = BrushTool.entries.map(::brushToolLabel),
            filterLabels = FilterPreset.entries.map { getString(filterLabel(it)) },
            adjustmentLabels = LookAdjustment.entries.map { getString(adjustmentTitle(it)) },
            shapeKindLabels = ShapeKind.entries.map { getString(shapeLabel(it)) },
        )
        editor.onShapesChanged = { ui = ui.copy(shapeSelected = editor.hasSelectedShape()) }
        editor.onColorPicked = ::onColorPicked
        renderBrushToolControls()
        val callbacks = EditorCallbacks(
            onBack = ::requestExit,
            onSave = ::save,
            onUndo = ::undo,
            onRedo = ::redo,
            onCompare = ::onCompare,
            onRestoreOriginal = ::restoreOriginal,
            onReleaseHistory = ::showReleaseHistory,
            onGroup = { group ->
                ui = ui.copy(group = group)
                if (group == EditorGroup.FILTER) ensureFilterThumbnails()
            },
            onCrop = ::beginCrop,
            onResize = { showResizeDialog() },
            onRotateLeft = { commitOperation { ImageEditingEngine.transform(it, rotation = -90f) } },
            onRotateRight = { commitOperation { ImageEditingEngine.transform(it, rotation = 90f) } },
            onRotateFine = ::beginFineRotation,
            onFlipHorizontal = { commitOperation { ImageEditingEngine.transform(it, flipX = true) } },
            onFlipVertical = { commitOperation { ImageEditingEngine.transform(it, flipY = true) } },
            onAdjustment = { adjustment -> beginLook(LOOK_TAB_ADJUST, adjustment = adjustment) },
            onFilterTool = { index -> beginLook(LOOK_TAB_FILTER, filter = FilterPreset.entries[index]) },
            onLookTab = ::onLookTab,
            onFilter = { index -> onFilter(FilterPreset.entries[index]) },
            onFilterIntensity = ::onFilterIntensity,
            onLookAdjustment = { index -> onLookAdjustment(LookAdjustment.entries[index]) },
            onBrush = ::beginBrush,
            onText = { showTextDialog() },
            onAdjustmentValue = ::onAdjustmentValue,
            onRotationValue = ::onRotationValue,
            onCropPreset = { index -> onCropPreset(CropAspectRatioPreset.entries[index]) },
            onBrushTool = { index -> selectBrushTool(BrushTool.entries[index]) },
            onBrushWidth = ::onBrushWidth,
            onMosaicStrength = ::onMosaicStrength,
            onBrushColor = { showColorPicker(brushColor, EditorColorTarget.BRUSH, onSelected = ::applyBrushColor) },
            onShapeTool = { index -> beginShapes(ShapeKind.entries[index]) },
            onShapeKind = { index -> onShapeStyle(kind = ShapeKind.entries[index]) },
            onShapeFilled = { filled -> onShapeStyle(filled = filled) },
            onShapeWidth = { progress -> onShapeStyle(widthProgress = progress) },
            onShapeColor = { showColorPicker(brushColor, EditorColorTarget.BRUSH, onSelected = ::applyBrushColor) },
            onDeleteShape = { editor.deleteSelectedShape() },
            onConfirmTool = ::confirmActiveTool,
            onCancelTool = ::cancelActiveTool,
        )
        setContent {
            MapleTheme(palette = appPalette) {
                EditorScreen(model = ui, editingView = editor, callbacks = callbacks) { Dialogs() }
            }
        }
        onBackPressedDispatcher.addCallback(this, backPressedCallback)
        observeModel()
        model.initialize(requireNotNull(request))
    }

    override fun onSaveInstanceState(outState: Bundle) {
        if (::editor.isInitialized) model.uiState = captureUiState()
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        if (isChangingConfigurations && ::editor.isInitialized) model.uiState = captureUiState()
        textDialog = null
        saveDialog = null
        colorDialog = null
        resizeDialog = null
        lookPreviewJob?.cancel()
        thumbnailJob?.cancel()
        lookPreviewSource = null
        if (::editor.isInitialized) editor.setBitmap(null)
        bitmap = null
        super.onDestroy()
    }

    // region Dialogs

    @Composable
    private fun Dialogs() {
        // The palette replaces the text dialog instead of stacking on it; the draft lives in textDialog and survives.
        textDialog?.takeIf { colorDialog == null }?.let { state ->
            EditorTextDialog(
                state = state,
                sizeLabel = { progress -> getString(R.string.image_editor_text_size_percent, progress + MIN_TEXT_PERCENT) },
                rotationLabel = { progress -> getString(R.string.image_editor_rotation_degrees, progress - TEXT_ROTATION_CENTER) },
                onPickColor = {
                    showColorPicker(state.color, EditorColorTarget.TEXT) { selected -> state.color = selected }
                },
                onDismissRequest = { textDialog = null; syncDialogFlag() },
                onConfirm = { confirmTextDialog(state) },
            )
        }
        saveDialog?.let { state ->
            EditorSaveDialog(
                state = state,
                hint = getString(R.string.text_image_conversion_filename_strategy),
                onDismissRequest = { saveDialog = null; syncDialogFlag() },
                onConfirm = { confirmSaveDialog(state) },
            )
        }
        colorDialog?.let { holder ->
            EditorColorDialog(
                state = holder.state,
                onDismissRequest = { colorDialog = null; syncDialogFlag() },
                onConfirm = {
                    val color = parseEditorColor(holder.state.input)
                    if (color == null) {
                        holder.state.error = true
                    } else {
                        colorDialog = null
                        syncDialogFlag()
                        holder.onSelected(color)
                    }
                },
            )
        }
        resizeDialog?.let { state ->
            EditorResizeDialog(
                state = state,
                errorText = ::resizeErrorText,
                onDismissRequest = { resizeDialog = null; syncDialogFlag() },
                onConfirm = { confirmResizeDialog(state) },
            )
        }
        if (exitConfirmationVisible) {
            MapleDialog(
                title = getString(R.string.text_unsaved_changes),
                onDismissRequest = { exitConfirmationVisible = false; syncDialogFlag() },
                confirmText = getString(R.string.dialog_button_discard_changes),
                onConfirm = { exitConfirmationVisible = false; syncDialogFlag(); finishWithoutCallback() },
                dismissText = getString(R.string.dialog_button_cancel),
            ) {
                Text(
                    text = getString(R.string.edit_exit_without_save_warn),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
                )
            }
        }
    }

    private fun syncDialogFlag() {
        composeDialogOpen = textDialog != null || saveDialog != null || colorDialog != null || resizeDialog != null || exitConfirmationVisible
    }

    // endregion

    private fun observeModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { model.state.collect(::renderState) }
                launch { model.operationErrors.collect { toast(R.string.text_failed) } }
                launch { model.historyWarnings.collect { toast(R.string.image_editor_history_unavailable) } }
            }
        }
    }

    private fun renderState(state: ImageEditorScreenState) {
        currentStateId = state.currentStateId
        savedStateId = state.savedStateId
        setBusy(state.busy)
        ui = ui.copy(loadFailed = state.loadFailed)
        if (state.bitmap !== bitmap) {
            bitmap = state.bitmap
            editor.setBitmap(state.bitmap)
            ui = ui.copy(ready = state.bitmap != null)
            if (ui.group == EditorGroup.FILTER) ensureFilterThumbnails()
            if (state.bitmap != null && !uiStateRestored) restoreUiState(model.uiState)
        }
        when (val output = state.output) {
            OutputCommitState.Idle, OutputCommitState.Running -> Unit
            OutputCommitState.Succeeded -> handleTerminalResult(success = true)
            is OutputCommitState.Failed -> handleTerminalResult(success = false, output.error)
        }
        refreshActions(state)
    }

    private fun refreshActions(state: ImageEditorScreenState = model.state.value) {
        val idle = bitmap != null && !busy && toolState == EditorToolState.NORMAL
        ui = ui.copy(
            canSave = idle && currentStateId != savedStateId,
            canUndo = idle && state.canUndo,
            canRedo = idle && state.canRedo,
            canRestoreOriginal = idle && state.canRestoreOriginal,
            canCompare = bitmap != null && !busy && model.compareImage != null,
            toolState = toolState,
        )
        if (ui.comparing && !ui.canCompare) onCompare(false)
    }

    /** "Compare" (ROADMAP 8.1): the original replaces the edit on the canvas while [showing]; tools keep their state. */
    private fun onCompare(showing: Boolean) {
        val compare = model.compareImage?.takeIf { showing && bitmap != null && !busy }
        editor.setCompareImage(compare)
        ui = ui.copy(comparing = compare != null)
    }

    private fun handleTerminalResult(success: Boolean, error: Throwable? = null) {
        if (terminalResultHandled || isFinishing || isDestroyed) return
        terminalResultHandled = true
        if (success) {
            val activeRequest = request ?: return finish()
            setResult(
                RESULT_OK,
                Intent().putExtra(
                    org.autojs.plugin.explorer.api.ExplorerActionIntentExtras.OUTPUT_TRANSACTION_ID,
                    activeRequest.transactionId,
                ),
            )
            toast(R.string.text_done)
        } else {
            toast(
                if (error is ImageBitmapIO.OutputLimitExceededException) {
                    R.string.text_output_too_large
                } else {
                    R.string.error_failed_to_save
                },
            )
        }
        finish()
    }

    // region Tools

    /**
     * Opens the look panel (ROADMAP 8.1) on [tab]; a [filter] or an [adjustment] picked from the tool strips starts
     * selected. Filter and adjustments are previewed together and committed as one history step.
     */
    private fun beginLook(tab: Int, filter: FilterPreset? = null, adjustment: LookAdjustment? = null) {
        val current = bitmap ?: return
        if (!canStartTool()) return
        toolState = EditorToolState.LOOK
        look = EditorLook(filter = filter ?: FilterPreset.NONE)
        lookTab = tab
        adjustment?.let { lookAdjustment = it }
        startLookPreview(current)
        ensureFilterThumbnails()
        renderLook()
        refreshActions()
    }

    private fun onLookTab(tab: Int) {
        if (toolState != EditorToolState.LOOK) return
        lookTab = tab.coerceIn(LOOK_TAB_FILTER, LOOK_TAB_ADJUST)
        renderLook()
    }

    private fun onFilter(filter: FilterPreset) {
        if (toolState != EditorToolState.LOOK) return
        look = look.copy(filter = filter, intensity = if (filter == look.filter) look.intensity else FilterPreset.DEFAULT_INTENSITY)
        renderLook()
    }

    private fun onFilterIntensity(value: Int) {
        if (toolState != EditorToolState.LOOK) return
        look = look.copy(intensity = value.coerceIn(0, 100))
        renderLook()
    }

    private fun onLookAdjustment(adjustment: LookAdjustment) {
        if (toolState != EditorToolState.LOOK) return
        lookAdjustment = adjustment
        renderLook()
    }

    private fun onAdjustmentValue(value: Int) {
        if (toolState != EditorToolState.LOOK) return
        look = look.with(lookAdjustment, value)
        renderLook()
    }

    private fun renderLook() {
        ui = ui.copy(
            lookTab = lookTab,
            filterIndex = look.filter.ordinal,
            filterIntensity = look.intensity,
            lookAdjustmentIndex = lookAdjustment.ordinal,
            adjustmentTitle = getString(adjustmentTitle(lookAdjustment)),
            adjustmentValue = look.value(lookAdjustment),
            adjustmentRange = lookAdjustment.range,
        )
        updateLookPreview()
    }

    private fun startLookPreview(source: Bitmap) {
        lookPreviewJob?.cancel()
        lookPreviewSource = null
        val pixels = maxOf(editor.width.toLong() * editor.height, MIN_PREVIEW_PIXELS).coerceAtMost(MAX_PREVIEW_PIXELS)
        lookPreviewJob = lifecycleScope.launch {
            val scaled = withContext(Dispatchers.Default) { ImageEditingEngine.downscale(source, pixels) }
            if (toolState == EditorToolState.LOOK && bitmap === source) {
                lookPreviewSource = scaled
                updateLookPreview()
            }
        }
    }

    /**
     * The color matrix shows at once on the full image; a look that needs a pixel pass (tone curve, sharpening,
     * vignette) is rendered on the downscaled copy in the background, the newest settings winning.
     */
    private fun updateLookPreview() {
        if (toolState != EditorToolState.LOOK) return
        val current = look
        editor.setPreviewColorMatrix(ColorMatrix(current.colorMatrix()))
        if (!current.needsPixelPass) {
            lookPreviewJob?.takeIf { lookPreviewSource != null }?.cancel()
            editor.setPreviewBitmap(null)
            return
        }
        val source = lookPreviewSource ?: return
        lookPreviewJob?.cancel()
        lookPreviewJob = lifecycleScope.launch {
            delay(LOOK_PREVIEW_DEBOUNCE_MILLIS)
            val rendered = withContext(Dispatchers.Default) { ImageEditingEngine.applyLook(source, current) }
            if (toolState == EditorToolState.LOOK && look == current) editor.setPreviewBitmap(rendered)
        }
    }

    private fun applyLook() {
        val pending = look
        if (pending.isIdentity) {
            cancelActiveTool()
            return
        }
        finishActiveToolUi()
        commitOperation { ImageEditingEngine.applyLook(it, pending) }
    }

    /** Filter thumbnails of the current image for the filter strip, made in the background once per image. */
    private fun ensureFilterThumbnails() {
        val current = bitmap ?: return
        if (thumbnailOwner === current && ui.filterThumbnails.isNotEmpty()) return
        thumbnailOwner = current
        thumbnailJob?.cancel()
        val size = dp(FILTER_THUMBNAIL_DP).toInt().coerceAtLeast(1)
        thumbnailJob = lifecycleScope.launch {
            val thumbnails = withContext(Dispatchers.Default) { ImageEditingEngine.filterThumbnails(current, size).map { it.asImageBitmap() } }
            if (bitmap === current) ui = ui.copy(filterThumbnails = thumbnails)
        }
    }

    private fun beginFineRotation() {
        if (!canStartTool()) return
        rotationDegrees = 0
        toolState = EditorToolState.ROTATION
        editor.beginRotation()
        ui = ui.copy(rotationDegrees = 0)
        refreshActions()
    }

    private fun onRotationValue(degrees: Int) {
        if (toolState != EditorToolState.ROTATION) return
        rotationDegrees = degrees.coerceIn(-ROTATION_CENTER, ROTATION_CENTER)
        ui = ui.copy(rotationDegrees = rotationDegrees)
        editor.updateRotationPreview(rotationDegrees.toFloat())
    }

    private fun applyFineRotation() {
        val degrees = rotationDegrees
        if (degrees == 0) {
            cancelActiveTool()
            return
        }
        finishActiveToolUi()
        commitOperation { ImageEditingEngine.rotateAndCrop(it, degrees.toFloat()) }
    }

    private fun beginCrop() {
        if (!canStartTool()) return
        toolState = EditorToolState.CROP
        editor.beginCrop(cropAspectRatioPreset)
        ui = ui.copy(
            cropPresetIndex = cropAspectRatioPreset.ordinal,
            interactionHelp = getString(R.string.image_editor_crop_help),
        )
        refreshActions()
    }

    private fun onCropPreset(preset: CropAspectRatioPreset) {
        cropAspectRatioPreset = preset
        ui = ui.copy(cropPresetIndex = preset.ordinal)
        if (toolState == EditorToolState.CROP) editor.updateCropAspectRatio(preset)
    }

    private fun beginBrush() {
        if (!canStartTool()) return
        toolState = EditorToolState.BRUSH
        editor.beginBrush(
            tool = selectedBrushTool,
            color = brushColor,
            widthOnScreen = brushWidthOnScreen(),
            mosaicBlockSizeOnScreen = mosaicBlockSizeOnScreen(),
        )
        renderBrushToolControls()
        refreshActions()
    }

    private fun applyBrush() {
        val strokes = editor.brushStrokes()
        if (strokes.isEmpty() || strokes.all { it.tool == BrushTool.ERASER }) {
            cancelActiveTool()
            return
        }
        finishActiveToolUi()
        commitOperation { ImageEditingEngine.applyBrush(it, strokes) }
    }

    /** A long press on the canvas picked [color] (ROADMAP 8.1, T16): it becomes the drawing color of brush and shapes. */
    private fun onColorPicked(color: Int) {
        applyBrushColor(color)
        val hex = String.format(java.util.Locale.ROOT, "#%06X", color and 0xFFFFFF)
        Toast.makeText(this, getString(R.string.image_editor_color_picked, hex), Toast.LENGTH_SHORT).show()
    }

    /** Brush and shapes share one color (ROADMAP 8.1). */
    private fun applyBrushColor(selected: Int) {
        brushColor = selected
        ui = ui.copy(brushColor = selected)
        editor.updateBrush(color = selected)
        if (toolState == EditorToolState.SHAPES) editor.updateShapeStyle(shapeStyle())
    }

    private fun beginShapes(kind: ShapeKind) {
        if (!canStartTool()) return
        shapeKind = kind
        toolState = EditorToolState.SHAPES
        editor.beginShapes(shapeStyle())
        renderShapeControls()
        ui = ui.copy(interactionHelp = getString(R.string.image_editor_shape_help))
        refreshActions()
    }

    /** The shapes use the pen's line width, so that the brush and the shapes remember one width. */
    private fun shapeStyle() = ImageEditingView.ShapeStyle(
        kind = shapeKind,
        color = brushColor,
        widthOnScreen = dp(brushWidths.progress(BrushTool.PEN) + MIN_BRUSH_WIDTH_DP),
        filled = shapeFilled,
    )

    private fun onShapeStyle(kind: ShapeKind = shapeKind, filled: Boolean = shapeFilled, widthProgress: Int? = null) {
        shapeKind = kind
        shapeFilled = filled
        widthProgress?.let { brushWidths = brushWidths.withProgress(BrushTool.PEN, it.coerceIn(0, BRUSH_WIDTH_MAX)) }
        renderShapeControls()
        if (toolState == EditorToolState.SHAPES) editor.updateShapeStyle(shapeStyle())
    }

    private fun renderShapeControls() {
        ui = ui.copy(
            shapeKindIndex = shapeKind.ordinal,
            shapeFillVisible = shapeKind.fillable,
            shapeFilled = shapeFilled,
            shapeWidthProgress = brushWidths.progress(BrushTool.PEN).coerceIn(0, BRUSH_WIDTH_MAX),
            brushColor = brushColor,
            shapeSelected = editor.hasSelectedShape(),
        )
    }

    private fun applyShapes() {
        val shapes = editor.shapes()
        if (shapes.isEmpty()) {
            cancelActiveTool()
            return
        }
        finishActiveToolUi()
        commitOperation { ImageEditingEngine.drawShapes(it, shapes) }
    }

    private fun shapeLabel(kind: ShapeKind): Int = when (kind) {
        ShapeKind.RECTANGLE -> R.string.image_editor_shape_rectangle
        ShapeKind.ELLIPSE -> R.string.image_editor_shape_ellipse
        ShapeKind.ARROW -> R.string.image_editor_shape_arrow
        ShapeKind.LINE -> R.string.image_editor_shape_line
    }

    private fun selectBrushTool(tool: BrushTool) {
        selectedBrushTool = tool
        renderBrushToolControls()
        if (toolState == EditorToolState.BRUSH) updateBrushPreviewSettings()
    }

    private fun onBrushWidth(progress: Int) {
        brushWidths = brushWidths.withProgress(selectedBrushTool, progress.coerceIn(0, BRUSH_WIDTH_MAX))
        ui = ui.copy(brushWidthProgress = brushWidths.progress(selectedBrushTool))
        if (toolState == EditorToolState.BRUSH) updateBrushPreviewSettings()
    }

    private fun onMosaicStrength(progress: Int) {
        mosaicStrengthProgress = progress.coerceIn(0, MOSAIC_STRENGTH_MAX)
        ui = ui.copy(mosaicStrengthProgress = mosaicStrengthProgress, mosaicStrengthLabel = mosaicStrengthLabel(mosaicStrengthProgress))
        if (toolState == EditorToolState.BRUSH) updateBrushPreviewSettings()
    }

    private fun renderBrushToolControls() {
        ui = ui.copy(
            brushToolIndex = selectedBrushTool.ordinal,
            brushColor = brushColor,
            brushColorVisible = selectedBrushTool == BrushTool.PEN || selectedBrushTool == BrushTool.HIGHLIGHTER,
            brushWidthProgress = brushWidths.progress(selectedBrushTool).coerceIn(0, BRUSH_WIDTH_MAX),
            mosaicStrengthVisible = selectedBrushTool == BrushTool.MOSAIC,
            mosaicStrengthProgress = mosaicStrengthProgress,
            mosaicStrengthLabel = mosaicStrengthLabel(mosaicStrengthProgress),
        )
    }

    private fun mosaicStrengthLabel(progress: Int): String =
        getString(R.string.image_editor_mosaic_strength_value, progress + MIN_MOSAIC_BLOCK_DP)

    private fun updateBrushPreviewSettings() {
        editor.updateBrush(
            tool = selectedBrushTool,
            color = brushColor,
            widthOnScreen = brushWidthOnScreen(),
            mosaicBlockSizeOnScreen = mosaicBlockSizeOnScreen(),
        )
    }

    private fun brushWidthOnScreen(): Float =
        dp(brushWidths.progress(selectedBrushTool) + MIN_BRUSH_WIDTH_DP)

    private fun mosaicBlockSizeOnScreen(): Float =
        dp(mosaicStrengthProgress + MIN_MOSAIC_BLOCK_DP)

    private fun showTextDialog(draft: EditorTextDialogDraft? = null) {
        if (bitmap == null || !canStartTool() || textDialog != null) return
        val style = draft?.style ?: textStyleSettings
        textDialog = EditorTextDialogState(
            text = draft?.text.orEmpty(),
            color = style.selectedColor,
            sizeProgress = style.sizeProgress.coerceIn(0, EditorDialogDefaults.TEXT_SIZE_MAX),
            outline = style.outlineEnabled,
            shadow = style.shadowEnabled,
            rotationProgress = (style.rotationDegrees + TEXT_ROTATION_CENTER).coerceIn(0, EditorDialogDefaults.TEXT_ROTATION_MAX),
        )
        syncDialogFlag()
    }

    private fun confirmTextDialog(state: EditorTextDialogState) {
        val current = bitmap ?: return
        val text = state.text.trim()
        if (text.isEmpty()) {
            state.showEmptyError = true
            return
        }
        val style = captureTextStyle(state)
        textStyleSettings = style
        val percent = style.sizeProgress + MIN_TEXT_PERCENT
        textDialog = null
        syncDialogFlag()
        toolState = EditorToolState.TEXT
        editor.beginText(
            ImageEditingEngine.TextSpec(
                text = text,
                color = style.selectedColor,
                size = min(current.width, current.height) * percent / 100f,
                centerX = current.width / 2f,
                centerY = current.height / 2f,
                outlineEnabled = style.outlineEnabled,
                shadowEnabled = style.shadowEnabled,
                rotationDegrees = style.rotationDegrees.toFloat(),
            ),
        )
        ui = ui.copy(interactionHelp = getString(R.string.image_editor_text_position_help))
        refreshActions()
    }

    private fun confirmActiveTool() {
        when (toolState) {
            EditorToolState.LOOK -> applyLook()
            EditorToolState.ROTATION -> applyFineRotation()
            EditorToolState.BRUSH -> applyBrush()
            EditorToolState.SHAPES -> applyShapes()
            EditorToolState.CROP, EditorToolState.TEXT -> applyInteractiveTool()
            EditorToolState.NORMAL -> Unit
        }
    }

    private fun applyInteractiveTool() {
        when (toolState) {
            EditorToolState.CROP -> {
                val selection = editor.cropSelection() ?: return
                finishActiveToolUi()
                commitOperation { ImageEditingEngine.crop(it, selection) }
            }
            EditorToolState.TEXT -> {
                val spec = editor.textSpec() ?: return
                finishActiveToolUi()
                commitOperation { ImageEditingEngine.addText(it, spec) }
            }
            else -> Unit
        }
    }

    private fun cancelActiveTool() {
        if (busy || toolState == EditorToolState.NORMAL) return
        finishActiveToolUi()
        refreshActions()
    }

    private fun finishActiveToolUi() {
        editor.clearToolState()
        lookPreviewJob?.cancel()
        lookPreviewSource = null
        look = EditorLook()
        rotationDegrees = 0
        toolState = EditorToolState.NORMAL
        ui = ui.copy(toolState = EditorToolState.NORMAL)
    }

    private fun showResizeDialog(draft: EditorResizeDraft? = null) {
        val current = bitmap ?: return
        if (!canStartTool() || resizeDialog != null) return
        val source = ImagePixelSize(current.width, current.height)
        resizeDialog = EditorResizeDialogState(source, ImageBitmapIO.editingPixelBudget(), draft ?: EditorResizePolicy.defaultDraft(source))
        syncDialogFlag()
    }

    private fun confirmResizeDialog(state: EditorResizeDialogState) {
        val size = EditorResizePolicy.resolve(state.source, state.draft, state.maxPixels).takeIf { it.isValid }?.size ?: return
        resizeDialog = null
        syncDialogFlag()
        // The same size would only add an empty history step.
        if (size == state.source) return
        commitOperation { ImageEditingEngine.resize(it, size.width, size.height) }
    }

    private fun commitOperation(operation: (Bitmap) -> Bitmap) {
        if (bitmap == null || busy || toolState != EditorToolState.NORMAL) return
        model.commitOperation(operation)
    }

    private fun undo() {
        if (bitmap == null || busy || toolState != EditorToolState.NORMAL) return
        model.undo()
    }

    private fun redo() {
        if (bitmap == null || busy || toolState != EditorToolState.NORMAL) return
        model.redo()
    }

    private fun restoreOriginal() {
        if (bitmap == null || busy || toolState != EditorToolState.NORMAL) return
        model.restoreOriginal()
    }

    // endregion

    // region Save

    private fun save() {
        showSaveDialog()
    }

    private fun showSaveDialog(draft: EditorSaveOptions? = null) {
        if (!canStartTool() || saveDialog != null) return
        val sourceInfo = model.state.value.sourceInfo ?: return
        val activeRequest = request ?: return
        val formats = EditorSavePolicy.availableFormats(activeRequest.allowedOutputMimeTypes)
        val initial = draft ?: saveOptions
        val labels = formats.map { format ->
            if (format == EditorSaveFormat.FOLLOW_SOURCE) {
                val effectiveFormat = EditorSavePolicy.effectiveFormat(
                    selection = format,
                    detectedMimeType = sourceInfo.mimeType,
                    allowedOutputMimeTypes = activeRequest.allowedOutputMimeTypes,
                )
                getString(R.string.image_editor_save_follow_source, effectiveFormat.displayName)
            } else {
                requireNotNull(format.explicitOutputFormat).displayName
            }
        }
        val state = EditorSaveDialogState(
            formatLabels = labels,
            formatIndex = formats.indexOf(initial.format).coerceAtLeast(0),
            quality = initial.quality.coerceIn(ImageConversionOptions.MIN_QUALITY, EditorDialogDefaults.QUALITY_MAX),
            webpLossless = initial.webpLossless && ImageOutputEncodingPolicy.isWebpLosslessAvailable(Build.VERSION.SDK_INT),
        )
        state.onChanged = { refreshSaveDialog(state, formats, sourceInfo.mimeType, activeRequest.allowedOutputMimeTypes) }
        refreshSaveDialog(state, formats, sourceInfo.mimeType, activeRequest.allowedOutputMimeTypes)
        saveDialog = state
        syncDialogFlag()
    }

    private fun refreshSaveDialog(
        state: EditorSaveDialogState,
        formats: List<EditorSaveFormat>,
        detectedMimeType: String,
        allowedOutputMimeTypes: Set<String>,
    ) {
        val selected = formats.getOrElse(state.formatIndex) { formats.first() }
        val effectiveFormat = EditorSavePolicy.effectiveFormat(
            selection = selected,
            detectedMimeType = detectedMimeType,
            allowedOutputMimeTypes = allowedOutputMimeTypes,
        )
        state.losslessAvailable = effectiveFormat == ImageOutputFormat.WEBP &&
            ImageOutputEncodingPolicy.isWebpLosslessAvailable(Build.VERSION.SDK_INT)
        state.qualityEnabled = ImageOutputEncodingPolicy.qualityEnabled(
            format = effectiveFormat,
            webpLosslessRequested = state.webpLossless && state.losslessAvailable,
            sdkInt = Build.VERSION.SDK_INT,
        )
    }

    private fun confirmSaveDialog(state: EditorSaveDialogState) {
        val activeRequest = request ?: return
        val formats = EditorSavePolicy.availableFormats(activeRequest.allowedOutputMimeTypes)
        val options = EditorSaveOptions(
            format = formats.getOrElse(state.formatIndex) { formats.first() },
            quality = state.quality,
            webpLossless = state.webpLossless && state.losslessAvailable,
        )
        saveOptions = options
        saveDialog = null
        syncDialogFlag()
        model.save(options)
    }

    // endregion

    // region Exit

    private fun requestExit() {
        when {
            model.state.value.output is OutputCommitState.Running -> Unit
            busy -> if (model.cancelNonOutputWork()) finishWithoutCallback()
            toolState != EditorToolState.NORMAL -> cancelActiveTool()
            currentStateId != savedStateId -> showExitConfirmation()
            else -> finishWithoutCallback()
        }
    }

    private fun showExitConfirmation() {
        if (exitConfirmationVisible) return
        exitConfirmationVisible = true
        syncDialogFlag()
    }

    private fun finishWithoutCallback() {
        backPressedCallback.isEnabled = false
        onBackPressedDispatcher.onBackPressed()
    }

    // endregion

    private fun canStartTool(): Boolean = bitmap != null && !busy && toolState == EditorToolState.NORMAL

    private fun setBusy(value: Boolean) {
        busy = value
        ui = ui.copy(busy = value)
    }

    private fun showColorPicker(
        initialColor: Int,
        target: EditorColorTarget,
        initialInput: String? = null,
        onSelected: (Int) -> Unit,
    ) {
        if (colorDialog != null) return
        colorDialog = ColorDialogHolder(
            state = EditorColorDialogState(initialInput ?: String.format(java.util.Locale.ROOT, "#%08X", initialColor)),
            target = target,
            onSelected = onSelected,
        )
        syncDialogFlag()
    }

    private fun captureTextStyle(state: EditorTextDialogState) = EditorTextStyleSettings(
        selectedColor = state.color,
        sizeProgress = state.sizeProgress,
        outlineEnabled = state.outline,
        shadowEnabled = state.shadow,
        rotationDegrees = state.rotationProgress - TEXT_ROTATION_CENTER,
    )

    private fun captureUiState(): EditorUiState = EditorUiState(
        toolState = toolState,
        look = look,
        lookTab = lookTab,
        lookAdjustment = lookAdjustment,
        rotationDegrees = rotationDegrees,
        brushColor = brushColor,
        brushTool = selectedBrushTool,
        brushWidths = brushWidths,
        mosaicStrengthProgress = mosaicStrengthProgress,
        cropAspectRatioPreset = cropAspectRatioPreset,
        canvasState = editor.captureCanvasState(),
        group = ui.group,
        textStyleSettings = textStyleSettings,
        textDialogDraft = textDialog?.let { EditorTextDialogDraft(text = it.text, style = captureTextStyle(it)) },
        saveOptions = saveOptions,
        saveDialogDraft = saveDialog?.let { state ->
            val formats = EditorSavePolicy.availableFormats(request?.allowedOutputMimeTypes.orEmpty())
            EditorSaveOptions(
                format = formats.getOrElse(state.formatIndex) { EditorSaveFormat.FOLLOW_SOURCE },
                quality = state.quality,
                webpLossless = state.webpLossless,
            )
        },
        colorDialogDraft = colorDialog?.let { EditorColorDialogDraft(it.target, it.state.input) },
        resizeDialogDraft = resizeDialog?.draft,
        shapeKind = shapeKind,
        shapeFilled = shapeFilled,
        exitConfirmationVisible = exitConfirmationVisible,
    )

    private fun restoreUiState(state: EditorUiState) {
        uiStateRestored = true
        toolState = state.toolState
        look = state.look
        lookTab = state.lookTab
        lookAdjustment = state.lookAdjustment
        rotationDegrees = state.rotationDegrees.coerceIn(-ROTATION_CENTER, ROTATION_CENTER)
        brushColor = state.brushColor
        selectedBrushTool = state.brushTool
        brushWidths = state.brushWidths
        mosaicStrengthProgress = state.mosaicStrengthProgress.coerceIn(0, MOSAIC_STRENGTH_MAX)
        textStyleSettings = state.textStyleSettings
        saveOptions = state.saveOptions
        cropAspectRatioPreset = state.cropAspectRatioPreset
        shapeKind = state.shapeKind
        shapeFilled = state.shapeFilled
        ui = ui.copy(
            group = state.group,
            rotationDegrees = rotationDegrees,
            cropPresetIndex = cropAspectRatioPreset.ordinal,
        )
        renderBrushToolControls()
        editor.restoreCanvasState(state.canvasState)
        when (state.toolState) {
            EditorToolState.NORMAL -> Unit
            EditorToolState.LOOK -> bitmap?.let { current ->
                startLookPreview(current)
                ensureFilterThumbnails()
                renderLook()
            }
            EditorToolState.ROTATION -> Unit
            EditorToolState.CROP -> ui = ui.copy(interactionHelp = getString(R.string.image_editor_crop_help))
            EditorToolState.BRUSH -> updateBrushPreviewSettings()
            EditorToolState.TEXT -> ui = ui.copy(interactionHelp = getString(R.string.image_editor_text_position_help))
            EditorToolState.SHAPES -> {
                renderShapeControls()
                ui = ui.copy(interactionHelp = getString(R.string.image_editor_shape_help))
            }
        }
        refreshActions()
        state.textDialogDraft?.let(::showTextDialog)
        state.saveDialogDraft?.let(::showSaveDialog)
        state.resizeDialogDraft?.let(::showResizeDialog)
        state.colorDialogDraft?.let { draft ->
            when (draft.target) {
                EditorColorTarget.BRUSH -> showColorPicker(
                    initialColor = brushColor,
                    target = EditorColorTarget.BRUSH,
                    initialInput = draft.input,
                    onSelected = ::applyBrushColor,
                )
                EditorColorTarget.TEXT -> textDialog?.let { dialog ->
                    showColorPicker(
                        initialColor = dialog.color,
                        target = EditorColorTarget.TEXT,
                        initialInput = draft.input,
                    ) { selected -> dialog.color = selected }
                }
            }
        }
        if (state.exitConfirmationVisible) showExitConfirmation()
    }

    private fun adjustmentTitle(adjustment: LookAdjustment): Int = when (adjustment) {
        LookAdjustment.BRIGHTNESS -> R.string.image_editor_brightness
        LookAdjustment.CONTRAST -> R.string.image_editor_contrast
        LookAdjustment.SATURATION -> R.string.image_editor_saturation
        LookAdjustment.TEMPERATURE -> R.string.image_editor_temperature
        LookAdjustment.EXPOSURE -> R.string.image_editor_exposure
        LookAdjustment.HUE -> R.string.image_editor_hue
        LookAdjustment.HIGHLIGHTS -> R.string.image_editor_highlights
        LookAdjustment.SHADOWS -> R.string.image_editor_shadows
        LookAdjustment.SHARPEN -> R.string.image_editor_sharpen
        LookAdjustment.VIGNETTE -> R.string.image_editor_vignette
    }

    private fun filterLabel(filter: FilterPreset): Int = when (filter) {
        FilterPreset.NONE -> R.string.image_editor_filter_none
        FilterPreset.MONO -> R.string.image_editor_filter_mono
        FilterPreset.NOIR -> R.string.image_editor_filter_noir
        FilterPreset.SEPIA -> R.string.image_editor_filter_sepia
        FilterPreset.VINTAGE -> R.string.image_editor_filter_vintage
        FilterPreset.COOL -> R.string.image_editor_filter_cool
        FilterPreset.WARM -> R.string.image_editor_filter_warm
        FilterPreset.FILM -> R.string.image_editor_filter_film
        FilterPreset.FADE -> R.string.image_editor_filter_fade
        FilterPreset.HIGH_CONTRAST -> R.string.image_editor_filter_high_contrast
        FilterPreset.SOFT -> R.string.image_editor_filter_soft
        FilterPreset.VIVID -> R.string.image_editor_filter_vivid
        FilterPreset.MATTE -> R.string.image_editor_filter_matte
        FilterPreset.CHROME -> R.string.image_editor_filter_chrome
        FilterPreset.INVERT -> R.string.image_editor_filter_invert
    }

    private fun cropAspectRatioLabel(preset: CropAspectRatioPreset): String = when (preset) {
        CropAspectRatioPreset.FREE -> getString(R.string.image_editor_crop_ratio_free)
        CropAspectRatioPreset.SQUARE -> "1:1"
        CropAspectRatioPreset.LANDSCAPE_4_3 -> "4:3"
        CropAspectRatioPreset.PORTRAIT_3_4 -> "3:4"
        CropAspectRatioPreset.LANDSCAPE_16_9 -> "16:9"
        CropAspectRatioPreset.PORTRAIT_9_16 -> "9:16"
        CropAspectRatioPreset.ORIGINAL -> getString(R.string.image_editor_crop_ratio_original)
    }

    private fun brushToolLabel(tool: BrushTool): String = getString(
        when (tool) {
            BrushTool.PEN -> R.string.image_editor_brush_pen
            BrushTool.HIGHLIGHTER -> R.string.image_editor_brush_highlighter
            BrushTool.MOSAIC -> R.string.image_editor_brush_mosaic
            BrushTool.ERASER -> R.string.image_editor_brush_eraser
        },
    )

    private fun dp(value: Int): Float = value * resources.displayMetrics.density

    private fun toast(messageRes: Int) =
        Toast.makeText(this, messageRes, Toast.LENGTH_LONG).show()

    companion object {
        private const val INITIAL_STATE_ID = 0L
        private const val LOOK_TAB_FILTER = 0
        private const val LOOK_TAB_ADJUST = 1
        private const val LOOK_PREVIEW_DEBOUNCE_MILLIS = 40L
        private const val MIN_PREVIEW_PIXELS = 360L * 480L
        private const val MAX_PREVIEW_PIXELS = 700_000L
        private const val FILTER_THUMBNAIL_DP = 64
        private const val ROTATION_CENTER = 45
        private const val BRUSH_WIDTH_MAX = 46
        private const val MOSAIC_STRENGTH_MAX = 28
        private const val MIN_BRUSH_WIDTH_DP = 2
        private const val MIN_MOSAIC_BLOCK_DP = 4
        private const val DEFAULT_MOSAIC_STRENGTH_PROGRESS = 8
        private const val MIN_TEXT_PERCENT = 2
        private const val TEXT_ROTATION_CENTER = 180
    }
}
