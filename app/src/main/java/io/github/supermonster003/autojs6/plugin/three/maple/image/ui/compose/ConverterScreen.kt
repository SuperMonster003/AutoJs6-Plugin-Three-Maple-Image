package io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import io.github.supermonster003.autojs6.plugin.three.maple.image.ConversionDialogDraft
import io.github.supermonster003.autojs6.plugin.three.maple.image.ConverterFormEvaluation
import io.github.supermonster003.autojs6.plugin.three.maple.image.ImageResizeError
import io.github.supermonster003.autojs6.plugin.three.maple.image.ImageResizeMode
import io.github.supermonster003.autojs6.plugin.three.maple.image.EditorSavePolicy
import io.github.supermonster003.autojs6.plugin.three.maple.image.R

/** Observable converter form; the Activity owns it and snapshots it into [ConversionDialogDraft] for recreation. */
internal class ConverterFormState(draft: ConversionDialogDraft) {
    var formatMimeType by mutableStateOf(draft.formatMimeType)
    var quality by mutableIntStateOf(draft.quality)
    var resizeMode by mutableStateOf(draft.resizeMode)
    var percentage by mutableStateOf(draft.percentage)
    var width by mutableStateOf(draft.width)
    var height by mutableStateOf(draft.height)
    var longEdge by mutableStateOf(draft.longEdge)
    var lockAspectRatio by mutableStateOf(draft.lockAspectRatio)
    var jpegBackgroundBlack by mutableStateOf(draft.jpegBackgroundBlack)
    var webpLossless by mutableStateOf(draft.webpLossless)
    var targetFileSizeEnabled by mutableStateOf(draft.targetFileSizeEnabled)
    var targetFileSizeKibibytes by mutableStateOf(draft.targetFileSizeKibibytes)
    var preserveExifMetadata by mutableStateOf(draft.preserveExifMetadata)
    var overwrite by mutableStateOf(draft.overwrite)
    var preserveExifTouched by mutableStateOf(draft.preserveExifTouched)

    fun snapshot() = ConversionDialogDraft(
        formatMimeType = formatMimeType,
        quality = quality,
        resizeMode = resizeMode,
        percentage = percentage,
        width = width,
        height = height,
        lockAspectRatio = lockAspectRatio,
        jpegBackgroundBlack = jpegBackgroundBlack,
        webpLossless = webpLossless,
        targetFileSizeEnabled = targetFileSizeEnabled,
        targetFileSizeKibibytes = targetFileSizeKibibytes,
        longEdge = longEdge,
        preserveExifMetadata = preserveExifMetadata,
        overwrite = overwrite,
        preserveExifTouched = preserveExifTouched,
    )
}

internal data class ConverterUiModel(
    val title: String = "",
    val loading: Boolean = true,
    val converting: Boolean = false,
    val previewModel: Any? = null,
    val formatLabels: List<String> = emptyList(),
    val formatIndex: Int = 0,
    val outputSuffix: String = "",
    val maxTargetKibibytes: Long = 0L,
    /** "Overwrite the original" is offered, and the index of the original's format it locks to. */
    val overwriteAvailable: Boolean = false,
    val overwriteFormatIndex: Int = -1,
    /** The host replaced the original; the bar offers its undo until the converter closes (ROADMAP 8.3). */
    val replacementUndo: Boolean = false,
    val replacementUndoBusy: Boolean = false,
)

/** Strings the page cannot derive itself: error texts per resize error and the byte formatter of the estimate. */
internal class ConverterTexts(
    val errorText: (ImageResizeError) -> String,
    val formatBytes: (Long) -> String,
)

internal class ConverterCallbacks(
    val onBack: () -> Unit,
    val onConvert: () -> Unit,
    val onFormat: (Int) -> Unit,
    val onWebpLossless: (Boolean) -> Unit,
    val onTargetFileSizeEnabled: (Boolean) -> Unit,
    val onWidth: (String) -> Unit,
    val onHeight: (String) -> Unit,
    val onLockAspectRatio: (Boolean) -> Unit,
    val onPreserveExif: (Boolean) -> Unit,
    val onOverwrite: (Boolean) -> Unit,
    val onUndoReplacement: () -> Unit,
)

private val resizeModes = listOf(ImageResizeMode.ORIGINAL, ImageResizeMode.PERCENTAGE, ImageResizeMode.LONG_EDGE, ImageResizeMode.CUSTOM)

/**
 * Converter page (design draft 7.6): standard top bar with the filled "Convert" action, a static source preview,
 * the form (format, quality, lossless, target size, resize mode with only the current mode's inputs, JPEG
 * background, EXIF) and a fixed estimate bar at the bottom. While converting the form is disabled under a
 * non-dismissable progress dialog.
 */
@Composable
internal fun ConverterScreen(
    model: ConverterUiModel,
    form: ConverterFormState?,
    evaluation: ConverterFormEvaluation?,
    texts: ConverterTexts,
    callbacks: ConverterCallbacks,
    dialogs: @Composable () -> Unit,
) {
    MapleScreen(
        title = model.title,
        subtitle = stringResource(R.string.dialog_button_convert),
        onBack = callbacks.onBack,
        actions = {
            Button(
                onClick = callbacks.onConvert,
                enabled = !model.loading && !model.converting && evaluation?.convertEnabled == true,
                modifier = Modifier.padding(end = 8.dp).testTag("maple.converter.convert"),
            ) { Text(stringResource(R.string.dialog_button_convert)) }
        },
    ) { padding ->
        if (model.loading || form == null || evaluation == null) {
            MapleLoading(modifier = Modifier.padding(padding))
        } else {
            BoxWithConstraints(modifier = Modifier.fillMaxSize().padding(padding)) {
                val previewHeight = (maxHeight * 0.35f).coerceIn(160.dp, 360.dp)
                Column(modifier = Modifier.fillMaxSize()) {
                    Column(modifier = Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState())) {
                        Preview(model, previewHeight)
                        Form(model, form, evaluation, texts, callbacks, enabled = !model.converting)
                        Spacer(Modifier.height(16.dp))
                    }
                    if (model.replacementUndo) ReplacementUndoBar(model, callbacks)
                    EstimateBar(model, evaluation, texts)
                }
            }
        }
    }
    if (model.converting) ConvertingDialog()
    dialogs()
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun Preview(model: ConverterUiModel, height: androidx.compose.ui.unit.Dp) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .background(MapleTheme.canvas.editorCanvas)
            .testTag("maple.converter.preview"),
        contentAlignment = Alignment.Center,
    ) {
        model.previewModel?.let { preview ->
            GlideImage(
                model = preview,
                contentDescription = model.title,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize().padding(8.dp),
            )
        }
    }
}

@Composable
private fun Form(
    model: ConverterUiModel,
    form: ConverterFormState,
    evaluation: ConverterFormEvaluation,
    texts: ConverterTexts,
    callbacks: ConverterCallbacks,
    enabled: Boolean,
) {
    val horizontal = MapleSettingsDefaults.horizontalPadding
    val tag = "maple.converter"
    ConversionFormatSection(
        labels = model.formatLabels,
        selectedIndex = if (evaluation.overwrite) model.overwriteFormatIndex else model.formatIndex,
        onSelect = callbacks.onFormat,
        enabled = enabled,
        tagPrefix = tag,
        locked = evaluation.overwrite,
    )
    Spacer(Modifier.height(8.dp))
    ConversionQualityRow(form.quality, { form.quality = it }, enabled && evaluation.qualityEnabled, tag)
    if (evaluation.losslessVisible) ConversionLosslessRow(form.webpLossless, callbacks.onWebpLossless, enabled, tag)
    if (evaluation.targetFileSizeVisible) {
        MapleSwitchRow(
            title = stringResource(R.string.text_image_conversion_target_file_size),
            summary = if (form.targetFileSizeEnabled) stringResource(R.string.text_image_conversion_target_file_size_helper) else null,
            checked = form.targetFileSizeEnabled,
            onToggle = callbacks.onTargetFileSizeEnabled,
            enabled = enabled,
            modifier = Modifier.testTag("$tag.targetEnabled"),
        )
        if (evaluation.targetFileSizeActive) {
            MapleNumberField(
                value = form.targetFileSizeKibibytes,
                onValueChange = { form.targetFileSizeKibibytes = it },
                label = stringResource(R.string.text_image_conversion_target_file_size_hint),
                suffix = "KiB",
                error = if (evaluation.targetFileSizeInvalid) {
                    stringResource(R.string.error_image_conversion_invalid_target_file_size, model.maxTargetKibibytes)
                } else {
                    null
                },
                enabled = enabled,
                modifier = Modifier.padding(horizontal = horizontal).testTag("$tag.targetFileSize"),
            )
        }
    }
    val error = evaluation.resizeError
    fun fieldError(expected: ImageResizeError) = error?.takeIf { it == expected }?.let(texts.errorText)
    ConversionResizeSection(
        modes = resizeModes,
        mode = form.resizeMode,
        onMode = { form.resizeMode = it },
        fields = ConversionResizeFields(
            percentage = form.percentage,
            onPercentage = { form.percentage = it },
            percentageError = fieldError(ImageResizeError.INVALID_PERCENTAGE),
            longEdge = form.longEdge,
            onLongEdge = { form.longEdge = it },
            longEdgeError = fieldError(ImageResizeError.INVALID_LONG_EDGE),
            width = form.width,
            onWidth = callbacks.onWidth,
            widthError = fieldError(ImageResizeError.INVALID_WIDTH),
            height = form.height,
            onHeight = callbacks.onHeight,
            heightError = fieldError(ImageResizeError.INVALID_HEIGHT),
            lockAspectRatio = form.lockAspectRatio,
            onLockAspectRatio = callbacks.onLockAspectRatio,
        ),
        enabled = enabled,
        tagPrefix = tag,
    )
    val generalError = error?.takeIf {
        it != ImageResizeError.INVALID_PERCENTAGE && it != ImageResizeError.INVALID_LONG_EDGE &&
            it != ImageResizeError.INVALID_WIDTH && it != ImageResizeError.INVALID_HEIGHT
    }
    if (generalError != null) ConversionErrorText(texts.errorText(generalError), tag)
    if (evaluation.jpegBackgroundVisible) {
        ConversionJpegBackgroundSection(form.jpegBackgroundBlack, { form.jpegBackgroundBlack = it }, enabled, tag)
    }
    Spacer(Modifier.height(8.dp))
    ConversionExifRow(form.preserveExifMetadata, callbacks.onPreserveExif, enabled, tag)
    if (model.overwriteAvailable) ConversionOverwriteRow(form.overwrite, callbacks.onOverwrite, enabled, tag)
}

/** After the host replaced the original: "Original overwritten" with Undo until the converter closes. */
@Composable
private fun ReplacementUndoBar(model: ConverterUiModel, callbacks: ConverterCallbacks) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(start = MapleSettingsDefaults.horizontalPadding, end = 8.dp)
            .testTag("maple.converter.replacementUndo"),
    ) {
        Text(
            text = stringResource(R.string.overwrite_saved),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.weight(1f),
        )
        androidx.compose.material3.TextButton(
            onClick = callbacks.onUndoReplacement,
            enabled = !model.replacementUndoBusy,
            modifier = Modifier.defaultMinSize(minHeight = 48.dp).testTag("maple.converter.replacementUndo.action"),
        ) { Text(stringResource(R.string.action_undo)) }
    }
}

@Composable
private fun EstimateBar(model: ConverterUiModel, evaluation: ConverterFormEvaluation, texts: ConverterTexts) {
    val size = evaluation.outputSize
    Surface(color = MaterialTheme.colorScheme.surface, tonalElevation = 0.dp, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth()) {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Column(modifier = Modifier.padding(horizontal = MapleSettingsDefaults.horizontalPadding, vertical = 12.dp)) {
                Text(
                    text = if (size == null) {
                        stringResource(R.string.text_image_conversion_output_resolution_unavailable)
                    } else {
                        stringResource(R.string.text_image_conversion_output_resolution, size.width, size.height)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.testTag("maple.converter.resolution"),
                )
                Text(
                    text = when {
                        size == null -> stringResource(R.string.text_image_conversion_estimated_size_unavailable)
                        evaluation.targetFileSizeActive -> evaluation.targetFileSizeBytes?.let { bytes ->
                            stringResource(R.string.text_image_conversion_target_file_size_summary, texts.formatBytes(bytes))
                        } ?: stringResource(R.string.text_image_conversion_estimated_size_unavailable)
                        evaluation.estimatedBytes != null -> stringResource(R.string.text_image_conversion_estimated_size, texts.formatBytes(evaluation.estimatedBytes))
                        else -> stringResource(R.string.text_image_conversion_estimated_size_unavailable)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.testTag("maple.converter.estimate"),
                )
                Text(
                    text = if (evaluation.overwrite) {
                        stringResource(R.string.image_editor_save_replaces_name, model.title)
                    } else {
                        stringResource(
                            R.string.image_editor_save_new_name,
                            EditorSavePolicy.outputFileName(model.title, evaluation.format, model.outputSuffix, overwrite = false),
                        )
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp).testTag("maple.converter.suffix"),
                )
            }
        }
    }
}

/** Blocking progress while the conversion runs; back and outside taps are ignored like the former AlertDialog. */
@Composable
private fun ConvertingDialog() {
    Dialog(onDismissRequest = {}, properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.testTag("maple.converter.progress"),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(20.dp))
                Text(stringResource(R.string.text_please_wait), style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}
