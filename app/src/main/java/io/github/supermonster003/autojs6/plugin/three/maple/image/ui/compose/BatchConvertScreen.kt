package io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose

import android.os.Build
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.supermonster003.autojs6.plugin.three.maple.image.BatchConversionPolicy
import io.github.supermonster003.autojs6.plugin.three.maple.image.BatchConversionSettings
import io.github.supermonster003.autojs6.plugin.three.maple.image.BatchFailure
import io.github.supermonster003.autojs6.plugin.three.maple.image.BatchSettingsError
import io.github.supermonster003.autojs6.plugin.three.maple.image.ImageConversionSizing
import io.github.supermonster003.autojs6.plugin.three.maple.image.ImageOutputFormat
import io.github.supermonster003.autojs6.plugin.three.maple.image.ImageResizeMode
import io.github.supermonster003.autojs6.plugin.three.maple.image.R

/**
 * Batch conversion page (ROADMAP 7.6, T12): one set of settings for every selected image, a note on where the new
 * images go, and "Start" in the top bar. The gallery shows it as a route above the media grid; the host entry
 * ([io.github.supermonster003.autojs6.plugin.three.maple.image.HostBatchConverterActivity]) as its whole screen.
 */
@Composable
internal fun BatchConvertScreen(
    count: Int,
    settings: BatchConversionSettings,
    note: String,
    onChange: (BatchConversionSettings) -> Unit,
    onStart: () -> Unit,
    onBack: () -> Unit,
    enabled: Boolean = true,
    dialogs: @Composable () -> Unit = {},
) {
    val error = BatchConversionPolicy.settingsError(settings)
    MapleScreen(
        title = stringResource(R.string.batch_convert_title),
        subtitle = pluralStringResource(R.plurals.gallery_image_count, count, count),
        onBack = onBack,
        actions = {
            Button(
                onClick = onStart,
                enabled = enabled && error == null && count > 0,
                modifier = Modifier.padding(end = 8.dp).testTag("maple.batch.start"),
            ) { Text(stringResource(R.string.batch_convert_start)) }
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())) {
            BatchConvertForm(settings, error, onChange, enabled)
            Text(
                text = note,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = MapleSettingsDefaults.horizontalPadding, vertical = 12.dp).testTag("maple.batch.note"),
            )
            Spacer(Modifier.height(16.dp))
        }
    }
    dialogs()
}

@Composable
private fun BatchConvertForm(
    settings: BatchConversionSettings,
    error: BatchSettingsError?,
    onChange: (BatchConversionSettings) -> Unit,
    enabled: Boolean,
) {
    val horizontal = MapleSettingsDefaults.horizontalPadding
    val sdk = Build.VERSION.SDK_INT
    val formats = ImageOutputFormat.entries
    MapleSectionHeader(stringResource(R.string.apk_info_format))
    MapleSegmented(
        options = formats.map { it.displayName },
        selectedIndex = formats.indexOf(settings.format),
        onSelect = { onChange(settings.copy(format = formats[it])) },
        enabled = enabled,
        tagPrefix = "maple.batch.format",
        modifier = Modifier.padding(horizontal = horizontal),
    )
    Spacer(Modifier.height(8.dp))
    MapleSliderRow(
        title = stringResource(R.string.text_image_conversion_quality),
        value = settings.quality,
        range = 1..100,
        onValueChange = { onChange(settings.copy(quality = it)) },
        onDark = false,
        enabled = enabled && BatchConversionPolicy.qualityEnabled(settings, sdk),
        modifier = Modifier.padding(horizontal = horizontal).testTag("maple.batch.quality"),
    )
    if (BatchConversionPolicy.losslessVisible(settings, sdk)) {
        MapleSwitchRow(
            title = stringResource(R.string.text_image_conversion_webp_lossless),
            checked = settings.webpLossless,
            onToggle = { onChange(settings.copy(webpLossless = it)) },
            enabled = enabled,
            modifier = Modifier.testTag("maple.batch.lossless"),
        )
    }
    MapleSectionHeader(stringResource(R.string.text_image_conversion_resize))
    val modes = BatchConversionPolicy.RESIZE_MODES
    MapleChipFlow(
        options = listOf(
            stringResource(R.string.text_image_conversion_original_size),
            stringResource(R.string.text_image_conversion_percentage),
            stringResource(R.string.text_image_conversion_long_edge),
        ),
        selectedIndex = modes.indexOf(settings.resizeMode).coerceAtLeast(0),
        onSelect = { onChange(settings.copy(resizeMode = modes[it])) },
        enabled = enabled,
        tagPrefix = "maple.batch.resize",
        modifier = Modifier.padding(horizontal = horizontal),
    )
    when (settings.resizeMode) {
        ImageResizeMode.PERCENTAGE -> MapleNumberField(
            value = settings.percentage,
            onValueChange = { onChange(settings.copy(percentage = it)) },
            label = stringResource(R.string.text_image_conversion_percentage_hint),
            suffix = "%",
            error = if (error == BatchSettingsError.INVALID_PERCENTAGE) {
                stringResource(R.string.error_image_conversion_invalid_percentage, ImageConversionSizing.MIN_PERCENTAGE, ImageConversionSizing.MAX_PERCENTAGE)
            } else {
                null
            },
            enabled = enabled,
            modifier = Modifier.padding(horizontal = horizontal, vertical = 8.dp).testTag("maple.batch.percentage"),
        )
        ImageResizeMode.LONG_EDGE -> MapleNumberField(
            value = settings.longEdge,
            onValueChange = { onChange(settings.copy(longEdge = it)) },
            label = stringResource(R.string.text_image_conversion_long_edge_hint),
            suffix = "px",
            error = if (error == BatchSettingsError.INVALID_LONG_EDGE) {
                stringResource(R.string.error_image_conversion_invalid_long_edge, ImageConversionSizing.MAX_DIMENSION)
            } else {
                null
            },
            enabled = enabled,
            modifier = Modifier.padding(horizontal = horizontal, vertical = 8.dp).testTag("maple.batch.longEdge"),
        )
        else -> Unit
    }
    MapleSectionHeader(stringResource(R.string.batch_convert_rotation))
    val rotations = BatchConversionPolicy.ROTATIONS
    MapleSegmented(
        options = rotations.map { it.toString() },
        selectedIndex = rotations.indexOf(settings.rotationDegrees).coerceAtLeast(0),
        onSelect = { onChange(settings.copy(rotationDegrees = rotations[it])) },
        enabled = enabled,
        tagPrefix = "maple.batch.rotation",
        modifier = Modifier.padding(horizontal = horizontal),
    )
    if (settings.format == ImageOutputFormat.JPEG) {
        MapleSectionHeader(stringResource(R.string.text_image_conversion_jpeg_background))
        MapleSegmented(
            options = listOf(
                stringResource(R.string.text_image_conversion_background_white),
                stringResource(R.string.text_image_conversion_background_black),
            ),
            selectedIndex = if (settings.jpegBackgroundBlack) 1 else 0,
            onSelect = { onChange(settings.copy(jpegBackgroundBlack = it == 1)) },
            enabled = enabled,
            tagPrefix = "maple.batch.background",
            modifier = Modifier.padding(horizontal = horizontal),
        )
    }
    Spacer(Modifier.height(8.dp))
    MapleSwitchRow(
        title = stringResource(R.string.text_image_conversion_preserve_exif),
        summary = stringResource(R.string.text_image_conversion_preserve_exif_helper),
        checked = settings.preserveExif,
        onToggle = { onChange(settings.copy(preserveExif = it)) },
        enabled = enabled,
        modifier = Modifier.testTag("maple.batch.exif"),
    )
}


/** Progress of a running batch: images done of the total, failures so far, and "Cancel" (the current image finishes). */
@Composable
internal fun BatchProgressDialog(done: Int, total: Int, failed: Int, cancelling: Boolean, onCancel: () -> Unit) {
    MapleDialog(
        title = stringResource(R.string.batch_progress_title),
        onDismissRequest = {},
        confirmText = stringResource(R.string.dialog_button_cancel),
        onConfirm = onCancel,
        dismissText = null,
        confirmEnabled = !cancelling,
        scrollable = false,
        modifier = Modifier.testTag("maple.batch.progress"),
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = MapleSettingsDefaults.horizontalPadding)) {
            LinearProgressIndicator(
                progress = { if (total > 0) done.toFloat() / total else 0f },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            )
            Text(stringResource(R.string.progress_count, done, total), style = MaterialTheme.typography.bodyMedium)
            if (failed > 0) {
                Text(
                    text = pluralStringResource(R.plurals.batch_not_converted_count, failed, failed),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.testTag("maple.batch.progressFailed"),
                )
            }
        }
    }
}

/** The images a batch could not convert, each with its reason; "Retry" runs them again with the same settings. */
@Composable
internal fun BatchFailuresDialog(entries: List<Pair<String, BatchFailure>>, onRetry: () -> Unit, onDismiss: () -> Unit) {
    MapleDialog(
        title = stringResource(R.string.batch_failures_title),
        onDismissRequest = onDismiss,
        confirmText = stringResource(R.string.gallery_retry),
        onConfirm = onRetry,
        dismissText = stringResource(R.string.dialog_button_close),
        modifier = Modifier.testTag("maple.batch.failures"),
    ) {
        entries.forEach { (name, failure) ->
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = MapleSettingsDefaults.horizontalPadding, vertical = 6.dp)) {
                Text(name, style = MaterialTheme.typography.bodyLarge)
                Text(batchFailureText(failure), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
internal fun batchFailureText(failure: BatchFailure): String = stringResource(batchFailureResource(failure))

internal fun batchFailureResource(failure: BatchFailure): Int = when (failure) {
    BatchFailure.UNREADABLE -> R.string.batch_failure_unreadable
    BatchFailure.TOO_LARGE -> R.string.batch_failure_too_large
    BatchFailure.OUT_OF_MEMORY -> R.string.batch_failure_memory
    BatchFailure.WRITE_FAILED -> R.string.batch_failure_write
}
