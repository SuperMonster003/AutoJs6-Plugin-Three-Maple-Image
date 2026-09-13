package io.github.supermonster003.autojs6.plugin.imagetools

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.ColorUtils
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import io.github.supermonster003.autojs6.plugin.imagetools.databinding.ActivityImageEditorBinding
import io.github.supermonster003.autojs6.plugin.imagetools.databinding.DialogImageEditorSaveBinding
import io.github.supermonster003.autojs6.plugin.imagetools.databinding.DialogImageEditorTextBinding
import kotlin.math.min
import java.util.Locale

class ImageEditorActivity : AppCompatActivity() {

    private val model by viewModels<ImageEditorViewModel>()
    private lateinit var binding: ActivityImageEditorBinding
    private val backPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() = requestExit()
    }
    private var request: ImageToolsRequest? = null
    private var bitmap: Bitmap? = null
    private var toolState = EditorToolState.NORMAL
    private var activeAdjustment: ImageEditingEngine.Adjustment? = null
    private var rotationDegrees = 0
    private var brushColor = Color.RED
    private var selectedBrushTool = BrushTool.PEN
    private var brushWidths = BrushWidthMemory()
    private var mosaicStrengthProgress = DEFAULT_MOSAIC_STRENGTH_PROGRESS
    private var cropAspectRatioPreset = CropAspectRatioPreset.FREE
    private var textStyleSettings = EditorTextStyleSettings()
    private var saveOptions = EditorSaveOptions()
    private var busy = false
    private var currentStateId = INITIAL_STATE_ID
    private var savedStateId = INITIAL_STATE_ID
    private var uiStateRestored = false
    private var terminalResultHandled = false
    private val activeDialogs = mutableSetOf<AlertDialog>()
    private var activeTextDialog: ActiveTextDialog? = null
    private var activeSaveDialog: ActiveSaveDialog? = null
    private var activeColorDialog: ActiveColorDialog? = null
    private var exitConfirmationVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(RESULT_CANCELED)
        request = ImageToolsRequestPolicy.resolve(this, intent, ImageToolsPlugin.EDIT_ACTION_ID)
        if (request == null) {
            finish()
            return
        }
        binding = ActivityImageEditorBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = request!!.displayName
        binding.toolbar.setNavigationOnClickListener { requestExit() }
        binding.toolbar.subtitle = getString(R.string.text_edit)
        onBackPressedDispatcher.addCallback(this, backPressedCallback)
        bindControls()
        observeModel()
        model.initialize(requireNotNull(request))
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_image_editor, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val idle = bitmap != null && !busy && toolState == EditorToolState.NORMAL
        val state = model.state.value
        menu.findItem(R.id.action_save)?.isEnabled = idle && currentStateId != savedStateId
        menu.findItem(R.id.action_undo)?.isEnabled = idle && state.canUndo
        menu.findItem(R.id.action_redo)?.isEnabled = idle && state.canRedo
        menu.findItem(R.id.action_restore_original)?.isEnabled = idle && state.canRestoreOriginal
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_release_history -> { showReleaseHistory(); true }
        android.R.id.home -> true.also { requestExit() }
        R.id.action_save -> true.also { save() }
        R.id.action_undo -> true.also { undo() }
        R.id.action_redo -> true.also { redo() }
        R.id.action_restore_original -> true.also { restoreOriginal() }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        if (::binding.isInitialized) model.uiState = captureUiState()
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        if (isChangingConfigurations && ::binding.isInitialized) model.uiState = captureUiState()
        activeDialogs.toList().forEach { dialog ->
            dialog.setOnDismissListener(null)
            dialog.dismiss()
        }
        activeDialogs.clear()
        activeTextDialog = null
        activeSaveDialog = null
        activeColorDialog = null
        if (::binding.isInitialized) binding.editor.setBitmap(null)
        bitmap = null
        super.onDestroy()
    }

    private fun observeModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { model.state.collect(::renderState) }
                launch { model.operationErrors.collect { toast(R.string.text_failed) } }
                launch {
                    model.historyWarnings.collect { toast(R.string.image_editor_history_unavailable) }
                }
            }
        }
    }

    private fun renderState(state: ImageEditorScreenState) {
        currentStateId = state.currentStateId
        savedStateId = state.savedStateId
        setBusy(state.busy)
        binding.errorText.isVisible = state.loadFailed
        if (state.bitmap !== bitmap) {
            bitmap = state.bitmap
            binding.editor.setBitmap(state.bitmap)
            if (state.bitmap != null && !uiStateRestored) restoreUiState(model.uiState)
        }
        when (val output = state.output) {
            OutputCommitState.Idle, OutputCommitState.Running -> Unit
            OutputCommitState.Succeeded -> handleTerminalResult(success = true)
            is OutputCommitState.Failed -> handleTerminalResult(success = false, output.error)
        }
        invalidateOptionsMenu()
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

    private fun bindControls() = with(binding) {
        cropAspectRatio.adapter = ArrayAdapter(
            this@ImageEditorActivity,
            android.R.layout.simple_spinner_item,
            CropAspectRatioPreset.entries.map(::cropAspectRatioLabel),
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        cropAspectRatio.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                val selected = CropAspectRatioPreset.entries[position]
                this@ImageEditorActivity.cropAspectRatioPreset = selected
                if (toolState == EditorToolState.CROP) editor.updateCropAspectRatio(selected)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }
        brushTool.adapter = ArrayAdapter(
            this@ImageEditorActivity,
            android.R.layout.simple_spinner_item,
            BrushTool.entries.map(::brushToolLabel),
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        brushTool.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                selectBrushTool(BrushTool.entries[position])
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }
        toolCrop.setOnClickListener { beginCrop() }
        toolRotateLeft.setOnClickListener {
            commitOperation { ImageEditingEngine.transform(it, rotation = -90f) }
        }
        toolRotateRight.setOnClickListener {
            commitOperation { ImageEditingEngine.transform(it, rotation = 90f) }
        }
        toolRotateFine.setOnClickListener { beginFineRotation() }
        toolFlipHorizontal.setOnClickListener {
            commitOperation { ImageEditingEngine.transform(it, flipX = true) }
        }
        toolFlipVertical.setOnClickListener {
            commitOperation { ImageEditingEngine.transform(it, flipY = true) }
        }
        toolBrightness.setOnClickListener {
            beginAdjustment(ImageEditingEngine.Adjustment.BRIGHTNESS, R.string.image_editor_brightness)
        }
        toolContrast.setOnClickListener {
            beginAdjustment(ImageEditingEngine.Adjustment.CONTRAST, R.string.image_editor_contrast)
        }
        toolSaturation.setOnClickListener {
            beginAdjustment(ImageEditingEngine.Adjustment.SATURATION, R.string.image_editor_saturation)
        }
        toolTemperature.setOnClickListener {
            beginAdjustment(ImageEditingEngine.Adjustment.TEMPERATURE, R.string.image_editor_temperature)
        }
        toolBrush.setOnClickListener { beginBrush() }
        toolText.setOnClickListener { showTextDialog() }

        adjustmentSeekBar.setOnSeekBarChangeListener(object : SimpleSeekBarListener() {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (toolState != EditorToolState.ADJUSTMENT) return
                val value = progress - ADJUSTMENT_CENTER
                adjustmentValue.text = if (value > 0) "+$value" else value.toString()
                activeAdjustment?.let { editor.setPreviewColorMatrix(ImageEditingEngine.colorMatrix(it, value)) }
            }
        })
        adjustmentButtons.buttonCancel.setOnClickListener { cancelActiveTool() }
        adjustmentButtons.buttonConfirm.setOnClickListener { applyAdjustment() }
        rotationSeekBar.setOnSeekBarChangeListener(object : SimpleSeekBarListener() {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (toolState != EditorToolState.ROTATION) return
                rotationDegrees = progress - ROTATION_CENTER
                rotationValue.text = getString(R.string.image_editor_rotation_degrees, rotationDegrees)
                editor.updateRotationPreview(rotationDegrees.toFloat())
            }
        })
        rotationButtons.buttonCancel.setOnClickListener { cancelActiveTool() }
        rotationButtons.buttonConfirm.setOnClickListener { applyFineRotation() }
        interactionButtons.buttonCancel.setOnClickListener { cancelActiveTool() }
        interactionButtons.buttonConfirm.setOnClickListener { applyInteractiveTool() }
        brushColor.setOnClickListener {
            showColorPicker(this@ImageEditorActivity.brushColor, EditorColorTarget.BRUSH) { selected ->
                this@ImageEditorActivity.brushColor = selected
                updateColorButton(binding.brushColor, selected)
                binding.editor.updateBrush(color = selected)
            }
        }
        brushWidth.setOnSeekBarChangeListener(object : SimpleSeekBarListener() {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                brushWidths = brushWidths.withProgress(selectedBrushTool, progress)
                if (toolState == EditorToolState.BRUSH) updateBrushPreviewSettings()
            }
        })
        mosaicStrength.setOnSeekBarChangeListener(object : SimpleSeekBarListener() {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                mosaicStrengthProgress = progress
                mosaicStrengthValue.text = getString(
                    R.string.image_editor_mosaic_strength_value,
                    progress + MIN_MOSAIC_BLOCK_DP,
                )
                if (toolState == EditorToolState.BRUSH) updateBrushPreviewSettings()
            }
        })
        brushButtons.buttonCancel.setOnClickListener { cancelActiveTool() }
        brushButtons.buttonConfirm.setOnClickListener { applyBrush() }
        updateColorButton(brushColor, this@ImageEditorActivity.brushColor)
        renderBrushToolControls()
    }

    private fun beginAdjustment(adjustment: ImageEditingEngine.Adjustment, titleRes: Int) {
        if (!canStartTool()) return
        toolState = EditorToolState.ADJUSTMENT
        activeAdjustment = adjustment
        binding.adjustmentName.setText(titleRes)
        binding.adjustmentSeekBar.progress = ADJUSTMENT_CENTER
        binding.adjustmentValue.text = "0"
        showOnlyPanel(EditorToolState.ADJUSTMENT)
        invalidateOptionsMenu()
    }

    private fun applyAdjustment() {
        val adjustment = activeAdjustment ?: return
        val value = binding.adjustmentSeekBar.progress - ADJUSTMENT_CENTER
        if (value == 0) {
            cancelActiveTool()
            return
        }
        finishActiveToolUi()
        commitOperation { ImageEditingEngine.applyAdjustment(it, adjustment, value) }
    }

    private fun beginFineRotation() {
        if (!canStartTool()) return
        rotationDegrees = 0
        toolState = EditorToolState.ROTATION
        binding.editor.beginRotation()
        binding.rotationSeekBar.progress = ROTATION_CENTER
        binding.rotationValue.text = getString(R.string.image_editor_rotation_degrees, 0)
        showOnlyPanel(EditorToolState.ROTATION)
        invalidateOptionsMenu()
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
        binding.editor.beginCrop(cropAspectRatioPreset)
        binding.cropAspectRatio.setSelection(cropAspectRatioPreset.ordinal, false)
        binding.interactionHelp.setText(R.string.image_editor_crop_help)
        showOnlyPanel(EditorToolState.CROP)
        invalidateOptionsMenu()
    }

    private fun beginBrush() {
        if (!canStartTool()) return
        toolState = EditorToolState.BRUSH
        binding.brushTool.setSelection(selectedBrushTool.ordinal, false)
        binding.brushWidth.progress = brushWidths.progress(selectedBrushTool)
            .coerceIn(0, binding.brushWidth.max)
        binding.mosaicStrength.progress = mosaicStrengthProgress
            .coerceIn(0, binding.mosaicStrength.max)
        binding.editor.beginBrush(
            tool = selectedBrushTool,
            color = brushColor,
            widthOnScreen = brushWidthOnScreen(),
            mosaicBlockSizeOnScreen = mosaicBlockSizeOnScreen(),
        )
        renderBrushToolControls()
        showOnlyPanel(EditorToolState.BRUSH)
        invalidateOptionsMenu()
    }

    private fun applyBrush() {
        val strokes = binding.editor.brushStrokes()
        if (strokes.isEmpty() || strokes.all { it.tool == BrushTool.ERASER }) {
            cancelActiveTool()
            return
        }
        finishActiveToolUi()
        commitOperation { ImageEditingEngine.applyBrush(it, strokes) }
    }

    private fun selectBrushTool(tool: BrushTool) {
        if (tool != selectedBrushTool) {
            brushWidths = brushWidths.withProgress(
                selectedBrushTool,
                binding.brushWidth.progress,
            )
            selectedBrushTool = tool
        }
        val rememberedWidth = brushWidths.progress(tool).coerceIn(0, binding.brushWidth.max)
        if (binding.brushWidth.progress != rememberedWidth) {
            binding.brushWidth.progress = rememberedWidth
        }
        renderBrushToolControls()
        if (toolState == EditorToolState.BRUSH) updateBrushPreviewSettings()
    }

    private fun renderBrushToolControls() = with(binding) {
        brushColor.isVisible = selectedBrushTool == BrushTool.PEN ||
            selectedBrushTool == BrushTool.HIGHLIGHTER
        mosaicStrengthRow.isVisible = selectedBrushTool == BrushTool.MOSAIC
        mosaicStrengthValue.text = getString(
            R.string.image_editor_mosaic_strength_value,
            mosaicStrengthProgress + MIN_MOSAIC_BLOCK_DP,
        )
    }

    private fun updateBrushPreviewSettings() {
        binding.editor.updateBrush(
            tool = selectedBrushTool,
            color = brushColor,
            widthOnScreen = brushWidthOnScreen(),
            mosaicBlockSizeOnScreen = mosaicBlockSizeOnScreen(),
        )
    }

    private fun brushWidthOnScreen(): Float =
        dp(binding.brushWidth.progress + MIN_BRUSH_WIDTH_DP)

    private fun mosaicBlockSizeOnScreen(): Float =
        dp(mosaicStrengthProgress + MIN_MOSAIC_BLOCK_DP)

    private fun showTextDialog(draft: EditorTextDialogDraft? = null) {
        val current = bitmap ?: return
        if (!canStartTool()) return
        val dialogBinding = DialogImageEditorTextBinding.inflate(layoutInflater)
        val initialStyle = draft?.style ?: textStyleSettings
        var selectedColor = initialStyle.selectedColor
        fun updateSizeLabel(progress: Int) {
            dialogBinding.textSizeLabel.text = getString(
                R.string.image_editor_text_size_percent,
                progress + MIN_TEXT_PERCENT,
            )
        }
        fun updateRotationLabel(progress: Int) {
            dialogBinding.textRotationValue.text = getString(
                R.string.image_editor_rotation_degrees,
                progress - TEXT_ROTATION_CENTER,
            )
        }
        draft?.let {
            dialogBinding.textContent.setText(it.text)
            dialogBinding.textContent.setSelection(it.text.length)
        }
        dialogBinding.textSize.progress = initialStyle.sizeProgress.coerceIn(0, dialogBinding.textSize.max)
        dialogBinding.textOutline.isChecked = initialStyle.outlineEnabled
        dialogBinding.textShadow.isChecked = initialStyle.shadowEnabled
        dialogBinding.textRotation.progress =
            (initialStyle.rotationDegrees + TEXT_ROTATION_CENTER).coerceIn(0, dialogBinding.textRotation.max)
        updateSizeLabel(dialogBinding.textSize.progress)
        updateRotationLabel(dialogBinding.textRotation.progress)
        updateColorButton(dialogBinding.textColor, selectedColor)
        dialogBinding.textSize.setOnSeekBarChangeListener(object : SimpleSeekBarListener() {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                updateSizeLabel(progress)
            }
        })
        dialogBinding.textRotation.setOnSeekBarChangeListener(object : SimpleSeekBarListener() {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                updateRotationLabel(progress)
            }
        })
        dialogBinding.textColor.setOnClickListener {
            showColorPicker(selectedColor, EditorColorTarget.TEXT) { color ->
                selectedColor = color
                activeTextDialog?.selectedColor = color
                updateColorButton(dialogBinding.textColor, color)
            }
        }
        val prompt = AlertDialog.Builder(this)
            .setTitle(R.string.image_editor_text)
            .setView(dialogBinding.root)
            .setNegativeButton(R.string.dialog_button_cancel, null)
            .setPositiveButton(R.string.dialog_button_confirm, null)
            .create()
        prompt.setOnShowListener {
            prompt.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val text = dialogBinding.textContent.text?.toString().orEmpty().trim()
                if (text.isEmpty()) {
                    toast(R.string.text_should_not_be_empty)
                    return@setOnClickListener
                }
                val style = captureTextStyle(dialogBinding, selectedColor)
                textStyleSettings = style
                val percent = style.sizeProgress + MIN_TEXT_PERCENT
                activeTextDialog = null
                prompt.dismiss()
                toolState = EditorToolState.TEXT
                binding.editor.beginText(
                    ImageEditingEngine.TextSpec(
                        text = text,
                        color = selectedColor,
                        size = min(current.width, current.height) * percent / 100f,
                        centerX = current.width / 2f,
                        centerY = current.height / 2f,
                        outlineEnabled = style.outlineEnabled,
                        shadowEnabled = style.shadowEnabled,
                        rotationDegrees = style.rotationDegrees.toFloat(),
                    ),
                )
                binding.interactionHelp.setText(R.string.image_editor_text_position_help)
                showOnlyPanel(EditorToolState.TEXT)
                invalidateOptionsMenu()
            }
        }
        activeTextDialog = ActiveTextDialog(prompt, dialogBinding, selectedColor)
        trackDialog(prompt) {
            if (activeTextDialog?.dialog === prompt) activeTextDialog = null
        }.show()
    }

    private fun applyInteractiveTool() {
        when (toolState) {
            EditorToolState.CROP -> {
                val selection = binding.editor.cropSelection() ?: return
                finishActiveToolUi()
                commitOperation { ImageEditingEngine.crop(it, selection) }
            }
            EditorToolState.TEXT -> {
                val spec = binding.editor.textSpec() ?: return
                finishActiveToolUi()
                commitOperation { ImageEditingEngine.addText(it, spec) }
            }
            else -> Unit
        }
    }

    private fun cancelActiveTool() {
        if (busy || toolState == EditorToolState.NORMAL) return
        finishActiveToolUi()
        invalidateOptionsMenu()
    }

    private fun finishActiveToolUi() {
        binding.editor.clearToolState()
        activeAdjustment = null
        rotationDegrees = 0
        toolState = EditorToolState.NORMAL
        showOnlyPanel(EditorToolState.NORMAL)
    }

    private fun showOnlyPanel(state: EditorToolState) = with(binding) {
        toolBar.isVisible = state == EditorToolState.NORMAL
        adjustmentPanel.isVisible = state == EditorToolState.ADJUSTMENT
        rotationPanel.isVisible = state == EditorToolState.ROTATION
        interactionPanel.isVisible = state == EditorToolState.CROP || state == EditorToolState.TEXT
        cropAspectRatioRow.isVisible = state == EditorToolState.CROP
        brushPanel.isVisible = state == EditorToolState.BRUSH
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

    private fun save() {
        showSaveDialog()
    }

    private fun showSaveDialog(draft: EditorSaveOptions? = null) {
        if (!canStartTool() || activeSaveDialog != null) return
        val sourceInfo = model.state.value.sourceInfo ?: return
        val activeRequest = request ?: return
        val formats = EditorSavePolicy.availableFormats(activeRequest.allowedOutputMimeTypes)
        val dialogBinding = DialogImageEditorSaveBinding.inflate(layoutInflater)
        val initialOptions = draft ?: saveOptions

        fun selectedFormat(): EditorSaveFormat = formats.getOrElse(
            dialogBinding.saveFormat.selectedItemPosition,
        ) { formats.first() }

        fun currentOptions() = EditorSaveOptions(
            format = selectedFormat(),
            quality = dialogBinding.saveQuality.progress + ImageConversionOptions.MIN_QUALITY,
            webpLossless = dialogBinding.saveWebpLossless.isChecked,
        )

        fun updateControls() {
            val effectiveFormat = EditorSavePolicy.effectiveFormat(
                selection = selectedFormat(),
                detectedMimeType = sourceInfo.mimeType,
                allowedOutputMimeTypes = activeRequest.allowedOutputMimeTypes,
            )
            val losslessAvailable = effectiveFormat == ImageOutputFormat.WEBP &&
                ImageOutputEncodingPolicy.isWebpLosslessAvailable(Build.VERSION.SDK_INT)
            dialogBinding.saveWebpLossless.isVisible = losslessAvailable
            val qualityEnabled = ImageOutputEncodingPolicy.qualityEnabled(
                format = effectiveFormat,
                webpLosslessRequested = dialogBinding.saveWebpLossless.isChecked,
                sdkInt = Build.VERSION.SDK_INT,
            )
            dialogBinding.saveQuality.isEnabled = qualityEnabled
            dialogBinding.saveQualityTitle.isEnabled = qualityEnabled
            dialogBinding.saveQualityValue.isEnabled = qualityEnabled
            dialogBinding.saveQuality.alpha = if (qualityEnabled) 1f else DISABLED_CONTROL_ALPHA
            dialogBinding.saveQualityTitle.alpha = if (qualityEnabled) 1f else DISABLED_CONTROL_ALPHA
            dialogBinding.saveQualityValue.alpha = if (qualityEnabled) 1f else DISABLED_CONTROL_ALPHA
            dialogBinding.saveQualityValue.text = String.format(
                Locale.getDefault(),
                "%d",
                currentOptions().quality,
            )
        }

        dialogBinding.saveFormat.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            formats.map { format ->
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
            },
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        dialogBinding.saveFormat.setSelection(formats.indexOf(initialOptions.format).coerceAtLeast(0))
        dialogBinding.saveQuality.progress =
            (initialOptions.quality - ImageConversionOptions.MIN_QUALITY)
                .coerceIn(0, dialogBinding.saveQuality.max)
        dialogBinding.saveWebpLossless.isChecked = initialOptions.webpLossless &&
            ImageOutputEncodingPolicy.isWebpLosslessAvailable(Build.VERSION.SDK_INT)
        dialogBinding.saveFormat.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                updateControls()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }
        dialogBinding.saveQuality.setOnSeekBarChangeListener(object : SimpleSeekBarListener() {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                updateControls()
            }
        })
        dialogBinding.saveWebpLossless.setOnCheckedChangeListener { _, _ -> updateControls() }
        updateControls()

        val prompt = AlertDialog.Builder(this)
            .setTitle(R.string.text_save)
            .setView(dialogBinding.root)
            .setNegativeButton(R.string.dialog_button_cancel, null)
            .setPositiveButton(R.string.text_save, null)
            .create()
        prompt.setOnShowListener {
            prompt.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val selectedOptions = currentOptions()
                saveOptions = selectedOptions
                activeSaveDialog = null
                prompt.dismiss()
                model.save(selectedOptions)
            }
        }
        activeSaveDialog = ActiveSaveDialog(prompt, dialogBinding, formats)
        trackDialog(prompt) {
            if (activeSaveDialog?.dialog === prompt) activeSaveDialog = null
        }.show()
    }

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
        val dialog = AlertDialog.Builder(this)
            .setTitle(R.string.text_unsaved_changes)
            .setMessage(R.string.edit_exit_without_save_warn)
            .setNegativeButton(R.string.dialog_button_cancel, null)
            .setPositiveButton(R.string.dialog_button_discard_changes) { _, _ -> finishWithoutCallback() }
            .create()
        trackDialog(dialog) { exitConfirmationVisible = false }.show()
    }

    private fun finishWithoutCallback() {
        backPressedCallback.isEnabled = false
        onBackPressedDispatcher.onBackPressed()
    }

    private fun canStartTool(): Boolean = bitmap != null && !busy && toolState == EditorToolState.NORMAL

    private fun setBusy(value: Boolean) {
        busy = value
        if (::binding.isInitialized) {
            binding.loadingIndicator.isVisible = value
            binding.toolBar.isEnabled = !value
        }
        invalidateOptionsMenu()
    }

    private fun showColorPicker(
        initialColor: Int,
        target: EditorColorTarget,
        initialInput: String? = null,
        onSelected: (Int) -> Unit,
    ) {
        if (activeColorDialog != null) return
        val input = EditText(this).apply {
            setSingleLine(true)
            setText(initialInput ?: String.format("#%08X", initialColor))
            setSelection(text.length)
        }
        val dialog = AlertDialog.Builder(this)
            .setTitle(R.string.dialog_title_color_palette)
            .setView(input)
            .setNegativeButton(R.string.dialog_button_cancel, null)
            .setPositiveButton(R.string.dialog_button_confirm, null)
            .create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                parseColor(input.text?.toString().orEmpty()).onSuccess { color ->
                    onSelected(color)
                    activeColorDialog = null
                    dialog.dismiss()
                }.onFailure {
                    input.error = getString(R.string.text_failed)
                }
            }
        }
        activeColorDialog = ActiveColorDialog(dialog, input, target)
        trackDialog(dialog) {
            if (activeColorDialog?.dialog === dialog) activeColorDialog = null
        }.show()
    }

    private fun updateColorButton(button: android.widget.Button, color: Int) {
        ViewCompat.setBackgroundTintList(button, ColorStateList.valueOf(color))
        button.setTextColor(if (ColorUtils.calculateLuminance(color) > 0.5) Color.BLACK else Color.WHITE)
    }

    private fun captureTextStyle(
        dialogBinding: DialogImageEditorTextBinding,
        selectedColor: Int,
    ) = EditorTextStyleSettings(
        selectedColor = selectedColor,
        sizeProgress = dialogBinding.textSize.progress,
        outlineEnabled = dialogBinding.textOutline.isChecked,
        shadowEnabled = dialogBinding.textShadow.isChecked,
        rotationDegrees = dialogBinding.textRotation.progress - TEXT_ROTATION_CENTER,
    )

    private fun captureSaveOptions(dialog: ActiveSaveDialog) = EditorSaveOptions(
        format = dialog.formats.getOrElse(dialog.binding.saveFormat.selectedItemPosition) {
            dialog.formats.first()
        },
        quality = dialog.binding.saveQuality.progress + ImageConversionOptions.MIN_QUALITY,
        webpLossless = dialog.binding.saveWebpLossless.isChecked,
    )

    private fun captureUiState(): EditorUiState {
        brushWidths = brushWidths.withProgress(selectedBrushTool, binding.brushWidth.progress)
        return EditorUiState(
            toolState = toolState,
            activeAdjustment = activeAdjustment,
            adjustmentProgress = binding.adjustmentSeekBar.progress,
            rotationDegrees = rotationDegrees,
            brushColor = brushColor,
            brushTool = selectedBrushTool,
            brushWidths = brushWidths,
            mosaicStrengthProgress = mosaicStrengthProgress,
            cropAspectRatioPreset = cropAspectRatioPreset,
            canvasState = binding.editor.captureCanvasState(),
            textStyleSettings = textStyleSettings,
            textDialogDraft = activeTextDialog?.let {
                EditorTextDialogDraft(
                    text = it.binding.textContent.text?.toString().orEmpty(),
                    style = captureTextStyle(it.binding, it.selectedColor),
                )
            },
            saveOptions = saveOptions,
            saveDialogDraft = activeSaveDialog?.let(::captureSaveOptions),
            colorDialogDraft = activeColorDialog?.let {
                EditorColorDialogDraft(it.target, it.input.text?.toString().orEmpty())
            },
            exitConfirmationVisible = exitConfirmationVisible,
        )
    }

    private fun restoreUiState(state: EditorUiState) {
        uiStateRestored = true
        toolState = state.toolState
        activeAdjustment = state.activeAdjustment
        rotationDegrees = state.rotationDegrees.coerceIn(-ROTATION_CENTER, ROTATION_CENTER)
        brushColor = state.brushColor
        selectedBrushTool = state.brushTool
        brushWidths = state.brushWidths
        mosaicStrengthProgress = state.mosaicStrengthProgress.coerceIn(0, binding.mosaicStrength.max)
        textStyleSettings = state.textStyleSettings
        saveOptions = state.saveOptions
        binding.rotationSeekBar.progress = rotationDegrees + ROTATION_CENTER
        cropAspectRatioPreset = state.cropAspectRatioPreset
        binding.brushTool.setSelection(selectedBrushTool.ordinal, false)
        binding.brushWidth.progress = brushWidths.progress(selectedBrushTool)
            .coerceIn(0, binding.brushWidth.max)
        binding.mosaicStrength.progress = mosaicStrengthProgress
        binding.cropAspectRatio.setSelection(cropAspectRatioPreset.ordinal, false)
        updateColorButton(binding.brushColor, brushColor)
        renderBrushToolControls()
        binding.editor.restoreCanvasState(state.canvasState)
        when (state.toolState) {
            EditorToolState.NORMAL -> showOnlyPanel(EditorToolState.NORMAL)
            EditorToolState.ADJUSTMENT -> {
                binding.adjustmentName.setText(adjustmentTitle(state.activeAdjustment))
                binding.adjustmentSeekBar.progress = state.adjustmentProgress
                val value = state.adjustmentProgress - ADJUSTMENT_CENTER
                binding.adjustmentValue.text = if (value > 0) "+$value" else value.toString()
                state.activeAdjustment?.let {
                    binding.editor.setPreviewColorMatrix(ImageEditingEngine.colorMatrix(it, value))
                }
                showOnlyPanel(EditorToolState.ADJUSTMENT)
            }
            EditorToolState.ROTATION -> {
                binding.rotationValue.text = getString(
                    R.string.image_editor_rotation_degrees,
                    rotationDegrees,
                )
                showOnlyPanel(EditorToolState.ROTATION)
            }
            EditorToolState.CROP -> {
                binding.interactionHelp.setText(R.string.image_editor_crop_help)
                showOnlyPanel(EditorToolState.CROP)
            }
            EditorToolState.BRUSH -> {
                updateBrushPreviewSettings()
                showOnlyPanel(EditorToolState.BRUSH)
            }
            EditorToolState.TEXT -> {
                binding.interactionHelp.setText(R.string.image_editor_text_position_help)
                showOnlyPanel(EditorToolState.TEXT)
            }
        }
        state.textDialogDraft?.let(::showTextDialog)
        state.saveDialogDraft?.let(::showSaveDialog)
        state.colorDialogDraft?.let { draft ->
            when (draft.target) {
                EditorColorTarget.BRUSH -> showColorPicker(
                    initialColor = brushColor,
                    target = EditorColorTarget.BRUSH,
                    initialInput = draft.input,
                ) { selected ->
                    brushColor = selected
                    updateColorButton(binding.brushColor, selected)
                    binding.editor.updateBrush(color = selected)
                }
                EditorColorTarget.TEXT -> activeTextDialog?.let { textDialog ->
                    showColorPicker(
                        initialColor = textDialog.selectedColor,
                        target = EditorColorTarget.TEXT,
                        initialInput = draft.input,
                    ) { selected ->
                        textDialog.selectedColor = selected
                        updateColorButton(textDialog.binding.textColor, selected)
                    }
                }
            }
        }
        if (state.exitConfirmationVisible) showExitConfirmation()
        invalidateOptionsMenu()
    }

    private fun adjustmentTitle(adjustment: ImageEditingEngine.Adjustment?): Int = when (adjustment) {
        ImageEditingEngine.Adjustment.BRIGHTNESS -> R.string.image_editor_brightness
        ImageEditingEngine.Adjustment.CONTRAST -> R.string.image_editor_contrast
        ImageEditingEngine.Adjustment.SATURATION -> R.string.image_editor_saturation
        ImageEditingEngine.Adjustment.TEMPERATURE -> R.string.image_editor_temperature
        null -> R.string.text_edit
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

    private fun trackDialog(dialog: AlertDialog, onDismiss: () -> Unit = {}): AlertDialog {
        activeDialogs.add(dialog)
        dialog.setOnDismissListener {
            activeDialogs.remove(dialog)
            onDismiss()
        }
        return dialog
    }

    private data class ActiveTextDialog(
        val dialog: AlertDialog,
        val binding: DialogImageEditorTextBinding,
        var selectedColor: Int,
    )

    private data class ActiveSaveDialog(
        val dialog: AlertDialog,
        val binding: DialogImageEditorSaveBinding,
        val formats: List<EditorSaveFormat>,
    )

    private data class ActiveColorDialog(
        val dialog: AlertDialog,
        val input: EditText,
        val target: EditorColorTarget,
    )

    private fun dp(value: Int): Float = value * resources.displayMetrics.density

    private fun toast(messageRes: Int) =
        Toast.makeText(this, messageRes, Toast.LENGTH_LONG).show()

    private fun parseColor(value: String): Result<Int> = runCatching {
        val normalized = value.trim().removePrefix("#")
        require(normalized.length == 6 || normalized.length == 8)
        val parsed = normalized.toLong(16)
        if (normalized.length == 6) (0xFF000000L or parsed).toInt() else parsed.toInt()
    }

    private abstract class SimpleSeekBarListener : SeekBar.OnSeekBarChangeListener {
        override fun onStartTrackingTouch(seekBar: SeekBar) = Unit
        override fun onStopTrackingTouch(seekBar: SeekBar) = Unit
    }

    companion object {
        private const val INITIAL_STATE_ID = 0L
        private const val ADJUSTMENT_CENTER = 100
        private const val ROTATION_CENTER = 45
        private const val MIN_BRUSH_WIDTH_DP = 2
        private const val MIN_MOSAIC_BLOCK_DP = 4
        private const val DEFAULT_MOSAIC_STRENGTH_PROGRESS = 8
        private const val MIN_TEXT_PERCENT = 2
        private const val TEXT_ROTATION_CENTER = 180
        private const val DISABLED_CONTROL_ALPHA = 0.38f
    }
}
