package io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
    MapleSectionHeader(stringResource(R.string.apk_info_format))
    MapleSegmented(
        options = model.formatLabels,
        selectedIndex = model.formatIndex,
        onSelect = callbacks.onFormat,
        enabled = enabled,
        tagPrefix = "maple.converter.format",
        modifier = Modifier.padding(horizontal = horizontal),
    )
    Spacer(Modifier.height(8.dp))
    MapleSliderRow(
        title = stringResource(R.string.text_image_conversion_quality),
        value = form.quality,
        range = 1..100,
        onValueChange = { form.quality = it },
        onDark = false,
        enabled = enabled && evaluation.qualityEnabled,
        modifier = Modifier.padding(horizontal = horizontal).testTag("maple.converter.quality"),
    )
    if (evaluation.losslessVisible) {
        MapleSwitchRow(
            title = stringResource(R.string.text_image_conversion_webp_lossless),
            checked = form.webpLossless,
            onToggle = callbacks.onWebpLossless,
            enabled = enabled,
            modifier = Modifier.testTag("maple.converter.lossless"),
        )
    }
    if (evaluation.targetFileSizeVisible) {
        MapleSwitchRow(
            title = stringResource(R.string.text_image_conversion_target_file_size),
            summary = if (form.targetFileSizeEnabled) stringResource(R.string.text_image_conversion_target_file_size_helper) else null,
            checked = form.targetFileSizeEnabled,
            onToggle = callbacks.onTargetFileSizeEnabled,
            enabled = enabled,
            modifier = Modifier.testTag("maple.converter.targetEnabled"),
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
                modifier = Modifier.padding(horizontal = horizontal).testTag("maple.converter.targetFileSize"),
            )
        }
    }
    MapleSectionHeader(stringResource(R.string.text_image_conversion_resize))
    MapleChipFlow(
        options = listOf(
            stringResource(R.string.text_image_conversion_original_size),
            stringResource(R.string.text_image_conversion_percentage),
            stringResource(R.string.text_image_conversion_long_edge),
            stringResource(R.string.text_image_conversion_custom_size),
        ),
        selectedIndex = resizeModes.indexOf(form.resizeMode),
        onSelect = { form.resizeMode = resizeModes[it] },
        enabled = enabled,
        tagPrefix = "maple.converter.resize",
        modifier = Modifier.padding(horizontal = horizontal),
    )
    val error = evaluation.resizeError
    when (form.resizeMode) {
        ImageResizeMode.ORIGINAL -> Unit
        ImageResizeMode.PERCENTAGE -> MapleNumberField(
            value = form.percentage,
            onValueChange = { form.percentage = it },
            label = stringResource(R.string.text_image_conversion_percentage_hint),
            suffix = "%",
            error = error?.takeIf { it == ImageResizeError.INVALID_PERCENTAGE }?.let(texts.errorText),
            enabled = enabled,
            modifier = Modifier.padding(horizontal = horizontal, vertical = 8.dp).testTag("maple.converter.percentage"),
        )
        ImageResizeMode.LONG_EDGE -> MapleNumberField(
            value = form.longEdge,
            onValueChange = { form.longEdge = it },
            label = stringResource(R.string.text_image_conversion_long_edge_hint),
            suffix = "px",
            error = error?.takeIf { it == ImageResizeError.INVALID_LONG_EDGE }?.let(texts.errorText),
            enabled = enabled,
            modifier = Modifier.padding(horizontal = horizontal, vertical = 8.dp).testTag("maple.converter.longEdge"),
        )
        ImageResizeMode.CUSTOM -> {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = horizontal, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                MapleNumberField(
                    value = form.width,
                    onValueChange = callbacks.onWidth,
                    label = stringResource(R.string.text_image_conversion_width),
                    suffix = "px",
                    error = error?.takeIf { it == ImageResizeError.INVALID_WIDTH }?.let(texts.errorText),
                    enabled = enabled,
                    modifier = Modifier.weight(1f).testTag("maple.converter.width"),
                )
                MapleNumberField(
                    value = form.height,
                    onValueChange = callbacks.onHeight,
                    label = stringResource(R.string.text_image_conversion_height),
                    suffix = "px",
                    error = error?.takeIf { it == ImageResizeError.INVALID_HEIGHT }?.let(texts.errorText),
                    enabled = enabled,
                    modifier = Modifier.weight(1f).testTag("maple.converter.height"),
                )
            }
            MapleSwitchRow(
                title = stringResource(R.string.text_image_conversion_lock_aspect_ratio),
                checked = form.lockAspectRatio,
                onToggle = callbacks.onLockAspectRatio,
                enabled = enabled,
                modifier = Modifier.testTag("maple.converter.lockAspect"),
            )
        }
    }
    val generalError = error?.takeIf {
        it != ImageResizeError.INVALID_PERCENTAGE && it != ImageResizeError.INVALID_LONG_EDGE &&
            it != ImageResizeError.INVALID_WIDTH && it != ImageResizeError.INVALID_HEIGHT
    }
    if (generalError != null) {
        Text(
            text = texts.errorText(generalError),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(horizontal = horizontal, vertical = 4.dp).testTag("maple.converter.error"),
        )
    }
    if (evaluation.jpegBackgroundVisible) {
        MapleSectionHeader(stringResource(R.string.text_image_conversion_jpeg_background))
        MapleSegmented(
            options = listOf(
                stringResource(R.string.text_image_conversion_background_white),
                stringResource(R.string.text_image_conversion_background_black),
            ),
            selectedIndex = if (form.jpegBackgroundBlack) 1 else 0,
            onSelect = { form.jpegBackgroundBlack = it == 1 },
            enabled = enabled,
            tagPrefix = "maple.converter.background",
            modifier = Modifier.padding(horizontal = horizontal),
        )
    }
    Spacer(Modifier.height(8.dp))
    MapleSwitchRow(
        title = stringResource(R.string.text_image_conversion_preserve_exif),
        summary = stringResource(R.string.text_image_conversion_preserve_exif_helper),
        checked = form.preserveExifMetadata,
        onToggle = { form.preserveExifMetadata = it },
        enabled = enabled,
        modifier = Modifier.testTag("maple.converter.exif"),
    )
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
                    text = stringResource(R.string.text_image_conversion_output_filename, model.outputSuffix + "." + evaluation.format.extension) +
                        "\n" + stringResource(R.string.text_image_conversion_filename_strategy),
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
