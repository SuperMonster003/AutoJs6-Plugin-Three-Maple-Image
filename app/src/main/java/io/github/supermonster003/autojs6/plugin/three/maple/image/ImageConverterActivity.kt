package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import io.github.supermonster003.autojs6.plugin.three.maple.image.databinding.DialogImageConverterBinding
import kotlinx.coroutines.launch
import org.autojs.plugin.explorer.api.ExplorerActionIntentExtras
import java.util.Locale

open class ImageConverterActivity : ConfiguredActivity() {

    private val model by viewModels<ImageConverterViewModel>()
    private var request: ImageToolsRequest? = null
    private var activeDialog: AlertDialog? = null
    private var activeDialogStage: ConverterStage? = null
    private var activeController: ConversionDialogController? = null
    private var terminalResultHandled = false

    internal open fun resolveRequest(): ImageToolsRequest? = ImageToolsRequestPolicy.resolve(this, intent, ImageToolsPlugin.CONVERT_ACTION_ID)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(RESULT_CANCELED)
        request = resolveRequest()
        if (request == null) {
            finish()
            return
        }
        setContentView(ProgressBar(this).apply {
            isIndeterminate = true
            contentDescription = getString(R.string.text_please_wait)
        })
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                when (model.state.value.stage) {
                    ConverterStage.CONVERTING -> Unit
                    ConverterStage.TARGET_SIZE_CONFIRMATION -> {
                        model.cancelClosestTargetOutput()
                        finish()
                    }
                    else -> finish()
                }
            }
        })
        observeModel()
        model.initialize(requireNotNull(request))
    }

    override fun onSaveInstanceState(outState: Bundle) {
        activeController?.snapshot()?.let { model.draft = it }
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        activeController?.snapshot()?.let { model.draft = it }
        activeController = null
        activeDialog?.setOnDismissListener(null)
        activeDialog?.dismiss()
        activeDialog = null
        activeDialogStage = null
        super.onDestroy()
    }

    private fun observeModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                model.state.collect(::renderState)
            }
        }
    }

    private fun renderState(state: ImageConverterScreenState) {
        if (isFinishing || isDestroyed) return
        when (state.stage) {
            ConverterStage.LOADING -> Unit
            ConverterStage.READY -> {
                val sourceInfo = state.sourceInfo ?: return
                if (activeDialog == null) showConversionDialog(sourceInfo)
            }
            ConverterStage.CONVERTING -> showProgressDialog()
            ConverterStage.TARGET_SIZE_CONFIRMATION -> {
                val result = state.targetFileSizeResult ?: return
                showTargetFileSizeConfirmation(result)
            }
            ConverterStage.SUCCEEDED -> handleTerminalResult(
                success = true,
                targetFileSizeResult = state.targetFileSizeResult,
            )
            ConverterStage.SOURCE_FAILED -> if (!terminalResultHandled) {
                terminalResultHandled = true
                toast(R.string.error_image_conversion_invalid_source)
                finish()
            }
            ConverterStage.OUTPUT_FAILED -> handleTerminalResult(success = false, state.outputError)
        }
    }

    private fun showProgressDialog() {
        if (activeDialog != null && activeDialogStage == ConverterStage.CONVERTING) return
        dismissActiveDialog()
        activeDialog = AlertDialog.Builder(this)
            .setTitle(R.string.dialog_button_convert)
            .setMessage(R.string.text_please_wait)
            .setCancelable(false)
            .create()
            .also(AlertDialog::show)
        activeDialogStage = ConverterStage.CONVERTING
    }

    private fun handleTerminalResult(
        success: Boolean,
        error: Throwable? = null,
        targetFileSizeResult: ImageTargetFileSizeResult? = null,
    ) {
        if (terminalResultHandled || isFinishing || isDestroyed) return
        terminalResultHandled = true
        activeController = null
        dismissActiveDialog()
        if (success) {
            val activeRequest = request ?: return finish()
            setResult(
                RESULT_OK,
                Intent().putExtra(
                    ExplorerActionIntentExtras.OUTPUT_TRANSACTION_ID,
                    activeRequest.transactionId,
                ),
            )
            if (targetFileSizeResult == null) {
                toast(R.string.text_done)
            } else {
                toast(
                    getString(
                        R.string.text_image_conversion_target_file_size_saved,
                        formatBytes(targetFileSizeResult.encodedBytes),
                        formatQuality(targetFileSizeResult.quality),
                        formatBytes(targetFileSizeResult.targetBytes),
                    ),
                )
            }
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

    private fun showTargetFileSizeConfirmation(result: ImageTargetFileSizeResult) {
        if (activeDialog != null && activeDialogStage == ConverterStage.TARGET_SIZE_CONFIRMATION) return
        dismissActiveDialog()
        val message = when (result.status) {
            ImageTargetFileSizeStatus.TARGET_BELOW_MINIMUM_QUALITY -> getString(
                R.string.text_image_conversion_target_file_size_below_minimum,
                formatQuality(result.quality),
                formatBytes(result.encodedBytes),
                formatBytes(result.targetBytes),
            )
            ImageTargetFileSizeStatus.TARGET_ABOVE_MAXIMUM_QUALITY -> getString(
                R.string.text_image_conversion_target_file_size_above_maximum,
                formatQuality(result.quality),
                formatBytes(result.encodedBytes),
                formatBytes(result.targetBytes),
            )
            ImageTargetFileSizeStatus.WITHIN_QUALITY_RANGE -> return
        }
        val dialog = AlertDialog.Builder(this)
            .setTitle(R.string.text_image_conversion_target_file_size_unavailable)
            .setMessage(message)
            .setNegativeButton(R.string.dialog_button_cancel, null)
            .setPositiveButton(R.string.dialog_button_save_closest, null)
            .create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener {
                model.cancelClosestTargetOutput()
                dialog.setOnDismissListener(null)
                dialog.dismiss()
                activeDialog = null
                activeDialogStage = null
                finish()
            }
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                dialog.setOnDismissListener(null)
                dialog.dismiss()
                activeDialog = null
                activeDialogStage = null
                model.confirmClosestTargetOutput()
            }
        }
        dialog.setOnCancelListener {
            model.cancelClosestTargetOutput()
            finish()
        }
        dialog.setOnDismissListener {
            if (!isFinishing && !isDestroyed && activeDialog === dialog) {
                model.cancelClosestTargetOutput()
                finish()
            }
        }
        activeDialog = dialog
        activeDialogStage = ConverterStage.TARGET_SIZE_CONFIRMATION
        dialog.show()
    }

    private fun dismissActiveDialog() {
        activeDialog?.setOnDismissListener(null)
        activeDialog?.dismiss()
        activeDialog = null
        activeDialogStage = null
    }

    private fun showConversionDialog(sourceInfo: ImageBitmapIO.SourceInfo) {
        if (isFinishing || isDestroyed) return
        val activeRequest = request ?: return
        val binding = DialogImageConverterBinding.inflate(layoutInflater)
        val permittedFormats = ImageOutputFormat.entries.filter {
            it.mimeType in activeRequest.allowedOutputMimeTypes
        }
        if (permittedFormats.isEmpty()) {
            finish()
            return
        }
        val dialog = AlertDialog.Builder(this)
            .setTitle(R.string.dialog_button_convert)
            .setView(binding.root)
            .setNegativeButton(R.string.dialog_button_cancel, null)
            .setPositiveButton(R.string.dialog_button_convert, null)
            .create()
        val controller = ConversionDialogController(
            activity = this,
            sourceInfo = sourceInfo,
            binding = binding,
            dialog = dialog,
            formats = permittedFormats,
            outputSuffix = activeRequest.outputNameSuffix,
            maxOutputBytes = activeRequest.maxOutputBytes,
            initialDraft = model.draft,
            onDraftChanged = { model.draft = it },
        )
        dialog.setOnShowListener {
            controller.refresh()
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener {
                dialog.dismiss()
                finish()
            }
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val options = controller.currentOptions() ?: return@setOnClickListener
                model.draft = controller.snapshot()
                dialog.setOnDismissListener(null)
                dialog.dismiss()
                activeDialog = null
                activeDialogStage = null
                activeController = null
                model.convert(options)
            }
        }
        dialog.setOnCancelListener { finish() }
        dialog.setOnDismissListener {
            if (!isFinishing && !isDestroyed && activeDialog === dialog) finish()
        }
        activeDialog = dialog
        activeDialogStage = ConverterStage.READY
        activeController = controller
        dialog.show()
    }

    private fun toast(messageRes: Int) =
        Toast.makeText(this, messageRes, Toast.LENGTH_LONG).show()

    private fun toast(message: String) =
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()

    private fun formatBytes(bytes: Long): String {
        val units = arrayOf("B", "KiB", "MiB", "GiB")
        var value = bytes.toDouble()
        var unit = 0
        while (value >= 1024.0 && unit < units.lastIndex) {
            value /= 1024.0
            unit += 1
        }
        return if (unit == 0) "$bytes ${units[unit]}" else "%.1f %s".format(value, units[unit])
    }

    private fun formatQuality(quality: Int): String =
        String.format(Locale.getDefault(), "%d", quality)

    private class ConversionDialogController(
        private val activity: ImageConverterActivity,
        private val sourceInfo: ImageBitmapIO.SourceInfo,
        private val binding: DialogImageConverterBinding,
        private val dialog: AlertDialog,
        private val formats: List<ImageOutputFormat>,
        private val outputSuffix: String,
        private val maxOutputBytes: Long,
        initialDraft: ConversionDialogDraft?,
        private val onDraftChanged: (ConversionDialogDraft) -> Unit,
    ) {

        private var updatingLockedDimension = false

        init {
            binding.format.adapter = ArrayAdapter(
                activity,
                android.R.layout.simple_spinner_item,
                formats.map { it.displayName },
            ).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }
            binding.format.setSelection(formats.indexOf(ImageOutputFormat.PNG).coerceAtLeast(0))
            binding.quality.progress =
                ImageConversionOptions.DEFAULT_QUALITY - ImageConversionOptions.MIN_QUALITY
            binding.width.setText(String.format(Locale.ROOT, "%d", sourceInfo.displaySize.width))
            binding.height.setText(String.format(Locale.ROOT, "%d", sourceInfo.displaySize.height))
            val defaultLongEdge = minOf(
                ImageConversionSizing.DEFAULT_LONG_EDGE,
                maxOf(sourceInfo.displaySize.width, sourceInfo.displaySize.height),
            )
            binding.longEdge.setText(String.format(Locale.ROOT, "%d", defaultLongEdge))
            val defaultTargetKibibytes = minOf(
                ImageTargetFileSizePolicy.DEFAULT_TARGET_KIBIBYTES,
                ImageTargetFileSizePolicy.maxTargetKibibytes(maxOutputBytes).coerceAtLeast(1L),
            )
            binding.targetFileSize.setText(String.format(Locale.ROOT, "%d", defaultTargetKibibytes))
            initialDraft?.let(::restore)

            binding.format.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) =
                    updateControlsAndSummary()

                override fun onNothingSelected(parent: AdapterView<*>?) = Unit
            }
            binding.quality.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) =
                    updateControlsAndSummary()

                override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit
                override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
            })
            binding.webpLossless.setOnCheckedChangeListener { _, checked ->
                if (checked) binding.targetFileSizeEnabled.isChecked = false
                updateControlsAndSummary()
            }
            binding.targetFileSizeEnabled.setOnCheckedChangeListener { _, checked ->
                if (checked) binding.webpLossless.isChecked = false
                updateControlsAndSummary()
            }
            binding.resizeMode.setOnCheckedChangeListener { _, _ -> updateControlsAndSummary() }
            binding.jpegBackground.setOnCheckedChangeListener { _, _ -> updateControlsAndSummary() }
            binding.preserveExif.setOnCheckedChangeListener { _, _ -> updateControlsAndSummary() }
            binding.lockAspectRatio.setOnCheckedChangeListener { _, checked ->
                if (checked) updateHeightFromWidth()
                updateControlsAndSummary()
            }
            binding.targetFileSize.afterTextChanged { updateControlsAndSummary() }
            binding.percentage.afterTextChanged { updateControlsAndSummary() }
            binding.longEdge.afterTextChanged { updateControlsAndSummary() }
            binding.width.afterTextChanged {
                if (!updatingLockedDimension && binding.lockAspectRatio.isChecked && binding.width.hasFocus()) {
                    updateHeightFromWidth()
                }
                updateControlsAndSummary()
            }
            binding.height.afterTextChanged {
                if (!updatingLockedDimension && binding.lockAspectRatio.isChecked && binding.height.hasFocus()) {
                    updateWidthFromHeight()
                }
                updateControlsAndSummary()
            }
        }

        fun refresh() = updateControlsAndSummary()

        fun snapshot() = ConversionDialogDraft(
            formatMimeType = selectedFormat().mimeType,
            quality = selectedQuality(),
            resizeMode = selectedResizeMode(),
            percentage = binding.percentage.text?.toString().orEmpty(),
            width = binding.width.text?.toString().orEmpty(),
            height = binding.height.text?.toString().orEmpty(),
            longEdge = binding.longEdge.text?.toString().orEmpty(),
            lockAspectRatio = binding.lockAspectRatio.isChecked,
            jpegBackgroundBlack = binding.jpegBackgroundBlack.isChecked,
            webpLossless = binding.webpLossless.isChecked &&
                ImageOutputEncodingPolicy.isWebpLosslessAvailable(Build.VERSION.SDK_INT),
            targetFileSizeEnabled = usesTargetFileSize(selectedFormat()),
            targetFileSizeKibibytes = binding.targetFileSize.text?.toString().orEmpty(),
            preserveExifMetadata = binding.preserveExif.isChecked,
        )

        fun currentOptions(): ImageConversionOptions? {
            val targetSize = currentResizeResult(showErrors = true).size ?: return null
            val format = selectedFormat()
            val targetFileSizeBytes = resolveTargetFileSizeBytes(format)
            if (usesTargetFileSize(format) && targetFileSizeBytes == null) return null
            return createOptions(format, targetSize, targetFileSizeBytes)
        }

        private fun createOptions(
            format: ImageOutputFormat,
            targetSize: ImagePixelSize,
            targetFileSizeBytes: Long? = null,
        ) = ImageConversionOptions(
            format = format,
            quality = selectedQuality(),
            targetSize = targetSize,
            jpegBackgroundColor = if (binding.jpegBackgroundBlack.isChecked) Color.BLACK else Color.WHITE,
            webpLossless = usesLosslessWebp(format),
            pngPaletteColorCountHint = ImageConversionSizing.pngPaletteColorCountHint(
                format = format,
                sourceSize = sourceInfo.displaySize,
                targetSize = targetSize,
                sourcePaletteColorCount = sourceInfo.pngPaletteColorCount,
            ),
            targetFileSizeBytes = targetFileSizeBytes,
            preservedExifMetadata = sourceInfo.preservableExifMetadata.takeIf {
                binding.preserveExif.isChecked
            },
        )

        private fun updateControlsAndSummary() {
            val format = selectedFormat()
            val losslessAvailable = format == ImageOutputFormat.WEBP &&
                ImageOutputEncodingPolicy.isWebpLosslessAvailable(Build.VERSION.SDK_INT)
            binding.webpLossless.visibility = if (losslessAvailable) View.VISIBLE else View.GONE
            val targetFileSizeAvailable = ImageTargetFileSizePolicy.isAvailable(
                format = format,
                webpLosslessRequested = binding.webpLossless.isChecked,
                sdkInt = Build.VERSION.SDK_INT,
                maxOutputBytes = maxOutputBytes,
            )
            binding.targetFileSizeEnabled.visibility =
                if (targetFileSizeAvailable) View.VISIBLE else View.GONE
            val targetFileSizeEnabled = targetFileSizeAvailable && binding.targetFileSizeEnabled.isChecked
            binding.targetFileSizeParent.visibility =
                if (targetFileSizeEnabled) View.VISIBLE else View.GONE
            val qualityEnabled = ImageOutputEncodingPolicy.qualityEnabled(
                format = format,
                webpLosslessRequested = binding.webpLossless.isChecked,
                sdkInt = Build.VERSION.SDK_INT,
                targetFileSizeRequested = targetFileSizeEnabled,
            )
            binding.quality.isEnabled = qualityEnabled
            binding.qualityTitle.isEnabled = qualityEnabled
            binding.qualityValue.isEnabled = qualityEnabled
            binding.quality.alpha = if (qualityEnabled) 1f else DISABLED_ALPHA
            binding.qualityTitle.alpha = if (qualityEnabled) 1f else DISABLED_ALPHA
            binding.qualityValue.alpha = if (qualityEnabled) 1f else DISABLED_ALPHA
            binding.qualityValue.text = String.format(Locale.getDefault(), "%d", selectedQuality())

            val resizeMode = selectedResizeMode()
            binding.percentageParent.visibility =
                if (resizeMode == ImageResizeMode.PERCENTAGE) View.VISIBLE else View.GONE
            binding.longEdgeParent.visibility =
                if (resizeMode == ImageResizeMode.LONG_EDGE) View.VISIBLE else View.GONE
            binding.customSizeParent.visibility =
                if (resizeMode == ImageResizeMode.CUSTOM) View.VISIBLE else View.GONE
            binding.jpegBackgroundParent.visibility =
                if (format == ImageOutputFormat.JPEG) View.VISIBLE else View.GONE

            clearFieldErrors()
            val result = currentResizeResult(showErrors = false)
            val targetSize = result.size
            val targetFileSizeBytes = resolveTargetFileSizeBytes(format)
            val targetFileSizeValid = !targetFileSizeEnabled || targetFileSizeBytes != null
            binding.validationError.visibility = if (result.error == null) View.GONE else View.VISIBLE
            binding.validationError.text = result.error?.let(::errorText).orEmpty()
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.isEnabled = targetSize != null && targetFileSizeValid

            if (targetSize == null) {
                binding.outputResolution.setText(R.string.text_image_conversion_output_resolution_unavailable)
                binding.outputSize.setText(R.string.text_image_conversion_estimated_size_unavailable)
            } else {
                binding.outputResolution.text = activity.getString(
                    R.string.text_image_conversion_output_resolution,
                    targetSize.width,
                    targetSize.height,
                )
                binding.outputSize.text = if (targetFileSizeEnabled) {
                    targetFileSizeBytes?.let { bytes ->
                        activity.getString(
                            R.string.text_image_conversion_target_file_size_summary,
                            activity.formatBytes(bytes),
                        )
                    } ?: activity.getString(R.string.text_image_conversion_estimated_size_unavailable)
                } else {
                    val options = createOptions(format, targetSize)
                    activity.getString(
                        R.string.text_image_conversion_estimated_size,
                        activity.formatBytes(ImageConversionSizing.estimateEncodedBytes(options)),
                    )
                }
            }
            binding.outputFilename.text = buildString {
                append(activity.getString(
                    R.string.text_image_conversion_output_filename,
                    "$outputSuffix.${format.extension}",
                ))
                append('\n')
                append(activity.getString(R.string.text_image_conversion_filename_strategy))
            }
            onDraftChanged(snapshot())
        }

        private fun restore(draft: ConversionDialogDraft) {
            formats.indexOfFirst { it.mimeType == draft.formatMimeType }
                .takeIf { it >= 0 }
                ?.let(binding.format::setSelection)
            binding.quality.progress =
                (draft.quality - ImageConversionOptions.MIN_QUALITY).coerceIn(0, binding.quality.max)
            binding.resizeMode.check(
                when (draft.resizeMode) {
                    ImageResizeMode.ORIGINAL -> R.id.resize_original
                    ImageResizeMode.PERCENTAGE -> R.id.resize_percentage
                    ImageResizeMode.LONG_EDGE -> R.id.resize_long_edge
                    ImageResizeMode.CUSTOM -> R.id.resize_custom
                },
            )
            binding.percentage.setText(draft.percentage)
            binding.width.setText(draft.width)
            binding.height.setText(draft.height)
            binding.longEdge.setText(draft.longEdge)
            binding.lockAspectRatio.isChecked = draft.lockAspectRatio
            binding.jpegBackgroundBlack.isChecked = draft.jpegBackgroundBlack
            binding.webpLossless.isChecked = draft.webpLossless &&
                ImageOutputEncodingPolicy.isWebpLosslessAvailable(Build.VERSION.SDK_INT)
            binding.targetFileSizeEnabled.isChecked = draft.targetFileSizeEnabled
            binding.targetFileSize.setText(draft.targetFileSizeKibibytes)
            binding.preserveExif.isChecked = draft.preserveExifMetadata
        }

        private fun currentResizeResult(showErrors: Boolean): ImageResizeResult {
            val request = when (selectedResizeMode()) {
                ImageResizeMode.ORIGINAL -> ImageResizeRequest(ImageResizeMode.ORIGINAL)
                ImageResizeMode.PERCENTAGE -> ImageResizeRequest(
                    mode = ImageResizeMode.PERCENTAGE,
                    percentage = binding.percentage.text?.toString()?.toIntOrNull(),
                )
                ImageResizeMode.LONG_EDGE -> ImageResizeRequest(
                    mode = ImageResizeMode.LONG_EDGE,
                    longEdge = binding.longEdge.text?.toString()?.toIntOrNull(),
                )
                ImageResizeMode.CUSTOM -> ImageResizeRequest(
                    mode = ImageResizeMode.CUSTOM,
                    width = binding.width.text?.toString()?.toIntOrNull(),
                    height = binding.height.text?.toString()?.toIntOrNull(),
                )
            }
            val resolved = ImageConversionSizing.resolve(sourceInfo.displaySize, request)
            val result = if (
                resolved.size != null &&
                !ImageBitmapIO.isWithinConversionMemoryBudget(sourceInfo, resolved.size)
            ) {
                ImageResizeResult(error = ImageResizeError.MEMORY_BUDGET_EXCEEDED)
            } else {
                resolved
            }
            if (showErrors && result.error != null) showFieldError(result.error)
            return result
        }

        private fun selectedFormat(): ImageOutputFormat =
            formats.getOrElse(binding.format.selectedItemPosition) { formats.first() }

        private fun selectedQuality(): Int =
            binding.quality.progress + ImageConversionOptions.MIN_QUALITY

        private fun usesLosslessWebp(format: ImageOutputFormat): Boolean =
            ImageOutputEncodingPolicy.usesWebpLossless(
                format = format,
                requested = binding.webpLossless.isChecked,
                sdkInt = Build.VERSION.SDK_INT,
            )

        private fun usesTargetFileSize(format: ImageOutputFormat): Boolean =
            binding.targetFileSizeEnabled.isChecked && ImageTargetFileSizePolicy.isAvailable(
                format = format,
                webpLosslessRequested = binding.webpLossless.isChecked,
                sdkInt = Build.VERSION.SDK_INT,
                maxOutputBytes = maxOutputBytes,
            )

        private fun resolveTargetFileSizeBytes(format: ImageOutputFormat): Long? {
            if (!usesTargetFileSize(format)) {
                binding.targetFileSizeParent.error = null
                return null
            }
            val targetKibibytes = binding.targetFileSize.text?.toString()?.toLongOrNull()
            val targetBytes = ImageTargetFileSizePolicy.resolveTargetBytes(targetKibibytes, maxOutputBytes)
            binding.targetFileSizeParent.error = if (targetBytes == null) {
                activity.getString(
                    R.string.error_image_conversion_invalid_target_file_size,
                    ImageTargetFileSizePolicy.maxTargetKibibytes(maxOutputBytes),
                )
            } else {
                null
            }
            return targetBytes
        }

        private fun selectedResizeMode(): ImageResizeMode = when (binding.resizeMode.checkedRadioButtonId) {
            R.id.resize_percentage -> ImageResizeMode.PERCENTAGE
            R.id.resize_long_edge -> ImageResizeMode.LONG_EDGE
            R.id.resize_custom -> ImageResizeMode.CUSTOM
            else -> ImageResizeMode.ORIGINAL
        }

        private fun updateHeightFromWidth() {
            val width = binding.width.text?.toString()?.toIntOrNull() ?: return
            val height = ImageConversionSizing.heightForWidth(sourceInfo.displaySize, width) ?: return
            updateDimension(binding.height, height)
        }

        private fun updateWidthFromHeight() {
            val height = binding.height.text?.toString()?.toIntOrNull() ?: return
            val width = ImageConversionSizing.widthForHeight(sourceInfo.displaySize, height) ?: return
            updateDimension(binding.width, width)
        }

        private fun updateDimension(field: EditText, value: Int) {
            val text = String.format(Locale.ROOT, "%d", value)
            if (field.text?.toString() == text) return
            updatingLockedDimension = true
            field.setText(text)
            field.setSelection(text.length)
            updatingLockedDimension = false
        }

        private fun clearFieldErrors() {
            binding.percentageParent.error = null
            binding.longEdgeParent.error = null
            binding.widthParent.error = null
            binding.heightParent.error = null
            binding.targetFileSizeParent.error = null
        }

        private fun showFieldError(error: ImageResizeError) {
            clearFieldErrors()
            when (error) {
                ImageResizeError.INVALID_PERCENTAGE -> binding.percentageParent.error = errorText(error)
                ImageResizeError.INVALID_LONG_EDGE -> binding.longEdgeParent.error = errorText(error)
                ImageResizeError.INVALID_WIDTH -> binding.widthParent.error = errorText(error)
                ImageResizeError.INVALID_HEIGHT -> binding.heightParent.error = errorText(error)
                else -> binding.validationError.apply {
                    visibility = View.VISIBLE
                    text = errorText(error)
                }
            }
        }

        private fun errorText(error: ImageResizeError): String = when (error) {
            ImageResizeError.INVALID_SOURCE -> activity.getString(R.string.error_image_conversion_invalid_source)
            ImageResizeError.INVALID_PERCENTAGE -> activity.getString(
                R.string.error_image_conversion_invalid_percentage,
                ImageConversionSizing.MIN_PERCENTAGE,
                ImageConversionSizing.MAX_PERCENTAGE,
            )
            ImageResizeError.INVALID_LONG_EDGE -> activity.getString(
                R.string.error_image_conversion_invalid_long_edge,
                ImageConversionSizing.MAX_DIMENSION,
            )
            ImageResizeError.INVALID_WIDTH -> activity.getString(R.string.error_image_conversion_invalid_width)
            ImageResizeError.INVALID_HEIGHT -> activity.getString(R.string.error_image_conversion_invalid_height)
            ImageResizeError.DIMENSION_TOO_LARGE -> activity.getString(
                R.string.error_image_conversion_dimension_too_large,
                ImageConversionSizing.MAX_DIMENSION,
            )
            ImageResizeError.PIXEL_COUNT_TOO_LARGE -> activity.getString(
                R.string.error_image_conversion_pixel_count_too_large,
                ImageConversionSizing.MAX_PIXEL_COUNT,
            )
            ImageResizeError.MEMORY_BUDGET_EXCEEDED ->
                activity.getString(R.string.error_image_conversion_memory_budget_exceeded)
        }

        private fun EditText.afterTextChanged(action: () -> Unit) {
            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(text: CharSequence?, start: Int, count: Int, after: Int) = Unit
                override fun onTextChanged(text: CharSequence?, start: Int, before: Int, count: Int) = Unit
                override fun afterTextChanged(text: Editable?) = action()
            })
        }

        companion object {
            private const val DISABLED_ALPHA = 0.45f
        }
    }
}
