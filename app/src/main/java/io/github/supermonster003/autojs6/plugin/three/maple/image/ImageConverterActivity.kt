package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.ConverterCallbacks
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.ConverterFormState
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.ConverterScreen
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.ConverterTexts
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.ConverterUiModel
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.MapleDialog
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.MapleTheme
import kotlinx.coroutines.launch
import org.autojs.plugin.explorer.api.ExplorerActionIntentExtras
import java.util.Locale

/**
 * Image converter. The conversion itself, the output transaction and the closest-target confirmation stay in
 * [ImageConverterViewModel]; the former dialog form is a full Compose page whose rules live in
 * [ConverterFormEvaluator] and whose draft is kept in the view model across recreation.
 */
open class ImageConverterActivity : ConfiguredActivity() {

    private val model by viewModels<ImageConverterViewModel>()
    private var request: ImageToolsRequest? = null
    private var terminalResultHandled = false
    private var evaluator: ConverterFormEvaluator? = null
    private var form by mutableStateOf<ConverterFormState?>(null)
    private var ui by mutableStateOf(ConverterUiModel())
    private var targetConfirmation by mutableStateOf<ImageTargetFileSizeResult?>(null)

    internal open fun resolveRequest(): ImageToolsRequest? = ImageToolsRequestPolicy.resolve(this, intent, ImageToolsPlugin.CONVERT_ACTION_ID)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(RESULT_CANCELED)
        val activeRequest = resolveRequest()
        request = activeRequest
        if (activeRequest == null) {
            finish()
            return
        }
        ui = ui.copy(
            title = activeRequest.displayName,
            previewModel = activeRequest.inputUri,
            outputSuffix = activeRequest.outputNameSuffix,
        )
        val texts = ConverterTexts(errorText = ::errorText, formatBytes = ::formatBytes)
        val callbacks = ConverterCallbacks(
            onBack = ::requestExit,
            onConvert = ::convert,
            onFormat = { index ->
                evaluator?.formatList?.getOrNull(index)?.let { format ->
                    form?.formatMimeType = format.mimeType
                    ui = ui.copy(formatIndex = index)
                }
            },
            onWebpLossless = { checked ->
                form?.let {
                    it.webpLossless = checked
                    if (checked) it.targetFileSizeEnabled = false
                }
            },
            onTargetFileSizeEnabled = { checked ->
                form?.let {
                    it.targetFileSizeEnabled = checked
                    if (checked) it.webpLossless = false
                }
            },
            onWidth = { text ->
                form?.let {
                    it.width = text
                    if (it.lockAspectRatio) evaluator?.heightForWidth(text.trim().toIntOrNull())?.let { height -> it.height = height.toString() }
                }
            },
            onHeight = { text ->
                form?.let {
                    it.height = text
                    if (it.lockAspectRatio) evaluator?.widthForHeight(text.trim().toIntOrNull())?.let { width -> it.width = width.toString() }
                }
            },
            onLockAspectRatio = { checked ->
                form?.let {
                    it.lockAspectRatio = checked
                    if (checked) evaluator?.heightForWidth(it.width.trim().toIntOrNull())?.let { height -> it.height = height.toString() }
                }
            },
        )
        setContent {
            MapleTheme(palette = appPalette) {
                val formState = form
                val evaluation = formState?.let { evaluator?.evaluate(it.snapshot()) }
                ConverterScreen(model = ui, form = formState, evaluation = evaluation, texts = texts, callbacks = callbacks) {
                    Dialogs()
                }
            }
        }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() = requestExit()
        })
        observeModel()
        model.initialize(activeRequest)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        form?.let { model.draft = it.snapshot() }
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        form?.let { model.draft = it.snapshot() }
        super.onDestroy()
    }

    @Composable
    private fun Dialogs() {
        val result = targetConfirmation ?: return
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
        MapleDialog(
            title = getString(R.string.text_image_conversion_target_file_size_unavailable),
            onDismissRequest = ::cancelClosestTargetOutput,
            confirmText = getString(R.string.dialog_button_save_closest),
            onConfirm = {
                targetConfirmation = null
                syncDialogFlag()
                model.confirmClosestTargetOutput()
            },
            dismissText = getString(R.string.dialog_button_cancel),
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
            )
        }
    }

    private fun cancelClosestTargetOutput() {
        targetConfirmation = null
        syncDialogFlag()
        model.cancelClosestTargetOutput()
        finish()
    }

    private fun syncDialogFlag() {
        composeDialogOpen = targetConfirmation != null || ui.converting
    }

    private fun requestExit() {
        when (model.state.value.stage) {
            ConverterStage.CONVERTING -> Unit
            ConverterStage.TARGET_SIZE_CONFIRMATION -> cancelClosestTargetOutput()
            else -> finish()
        }
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
            ConverterStage.LOADING -> ui = ui.copy(loading = true, converting = false)
            ConverterStage.READY -> {
                val sourceInfo = state.sourceInfo ?: return
                if (form == null && !setUpForm(sourceInfo)) return
                targetConfirmation = null
                ui = ui.copy(loading = false, converting = false)
            }
            ConverterStage.CONVERTING -> ui = ui.copy(loading = false, converting = true)
            ConverterStage.TARGET_SIZE_CONFIRMATION -> {
                ui = ui.copy(loading = false, converting = false)
                targetConfirmation = state.targetFileSizeResult
            }
            ConverterStage.SUCCEEDED -> handleTerminalResult(success = true, targetFileSizeResult = state.targetFileSizeResult)
            ConverterStage.SOURCE_FAILED -> if (!terminalResultHandled) {
                terminalResultHandled = true
                toast(R.string.error_image_conversion_invalid_source)
                finish()
            }
            ConverterStage.OUTPUT_FAILED -> handleTerminalResult(success = false, state.outputError)
        }
        syncDialogFlag()
    }

    private fun setUpForm(sourceInfo: ImageBitmapIO.SourceInfo): Boolean {
        val activeRequest = request ?: return false
        val formats = ImageOutputFormat.entries.filter { it.mimeType in activeRequest.allowedOutputMimeTypes }
        if (formats.isEmpty()) {
            finish()
            return false
        }
        val formEvaluator = ConverterFormEvaluator(
            sourceInfo = sourceInfo,
            formats = formats,
            maxOutputBytes = activeRequest.maxOutputBytes,
            sdkInt = Build.VERSION.SDK_INT,
            withinMemoryBudget = { ImageBitmapIO.isWithinConversionMemoryBudget(sourceInfo, it) },
        )
        evaluator = formEvaluator
        val draft = model.draft ?: formEvaluator.defaultDraft(getString(R.string.value_100))
        val state = ConverterFormState(draft)
        state.formatMimeType = formEvaluator.format(draft).mimeType
        form = state
        ui = ui.copy(
            formatLabels = formats.map { it.displayName },
            formatIndex = formats.indexOf(formEvaluator.format(draft)),
            maxTargetKibibytes = formEvaluator.maxTargetKibibytes(),
        )
        return true
    }

    private fun convert() {
        val state = form ?: return
        val options = evaluator?.evaluate(state.snapshot())?.options ?: return
        if (model.state.value.stage != ConverterStage.READY) return
        model.draft = state.snapshot()
        model.convert(options)
    }

    private fun handleTerminalResult(
        success: Boolean,
        error: Throwable? = null,
        targetFileSizeResult: ImageTargetFileSizeResult? = null,
    ) {
        if (terminalResultHandled || isFinishing || isDestroyed) return
        terminalResultHandled = true
        targetConfirmation = null
        if (success) {
            val activeRequest = request ?: return finish()
            setResult(
                RESULT_OK,
                Intent().putExtra(ExplorerActionIntentExtras.OUTPUT_TRANSACTION_ID, activeRequest.transactionId),
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

    private fun errorText(error: ImageResizeError): String = when (error) {
        ImageResizeError.INVALID_SOURCE -> getString(R.string.error_image_conversion_invalid_source)
        ImageResizeError.INVALID_PERCENTAGE -> getString(
            R.string.error_image_conversion_invalid_percentage,
            ImageConversionSizing.MIN_PERCENTAGE,
            ImageConversionSizing.MAX_PERCENTAGE,
        )
        ImageResizeError.INVALID_LONG_EDGE -> getString(R.string.error_image_conversion_invalid_long_edge, ImageConversionSizing.MAX_DIMENSION)
        ImageResizeError.INVALID_WIDTH -> getString(R.string.error_image_conversion_invalid_width)
        ImageResizeError.INVALID_HEIGHT -> getString(R.string.error_image_conversion_invalid_height)
        ImageResizeError.DIMENSION_TOO_LARGE -> getString(R.string.error_image_conversion_dimension_too_large, ImageConversionSizing.MAX_DIMENSION)
        ImageResizeError.PIXEL_COUNT_TOO_LARGE -> getString(R.string.error_image_conversion_pixel_count_too_large, ImageConversionSizing.MAX_PIXEL_COUNT)
        ImageResizeError.MEMORY_BUDGET_EXCEEDED -> getString(R.string.error_image_conversion_memory_budget_exceeded)
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
}
