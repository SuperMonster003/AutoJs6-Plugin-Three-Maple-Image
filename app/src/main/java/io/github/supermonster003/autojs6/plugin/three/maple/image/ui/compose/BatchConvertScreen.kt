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
    /** "Show location" is on: the form offers to keep the location (ROADMAP 9.1, T14). */
    showLocation: Boolean = false,
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
            BatchConvertForm(settings, error, onChange, enabled, showLocation)
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
    showLocation: Boolean,
) {
    val sdk = Build.VERSION.SDK_INT
    val formats = ImageOutputFormat.entries
    val tag = "maple.batch"
    ConversionFormatSection(
        labels = formats.map { it.displayName },
        selectedIndex = formats.indexOf(settings.format),
        onSelect = { onChange(settings.copy(format = formats[it])) },
        enabled = enabled,
        tagPrefix = tag,
    )
    Spacer(Modifier.height(8.dp))
    ConversionQualityRow(settings.quality, { onChange(settings.copy(quality = it)) }, enabled && BatchConversionPolicy.qualityEnabled(settings, sdk), tag)
    if (BatchConversionPolicy.losslessVisible(settings, sdk)) {
        ConversionLosslessRow(settings.webpLossless, { onChange(settings.copy(webpLossless = it)) }, enabled, tag)
    }
    ConversionResizeSection(
        modes = BatchConversionPolicy.RESIZE_MODES,
        mode = settings.resizeMode,
        onMode = { onChange(settings.copy(resizeMode = it)) },
        fields = ConversionResizeFields(
            percentage = settings.percentage,
            onPercentage = { onChange(settings.copy(percentage = it)) },
            percentageError = if (error == BatchSettingsError.INVALID_PERCENTAGE) {
                stringResource(R.string.error_image_conversion_invalid_percentage, ImageConversionSizing.MIN_PERCENTAGE, ImageConversionSizing.MAX_PERCENTAGE)
            } else {
                null
            },
            longEdge = settings.longEdge,
            onLongEdge = { onChange(settings.copy(longEdge = it)) },
            longEdgeError = if (error == BatchSettingsError.INVALID_LONG_EDGE) {
                stringResource(R.string.error_image_conversion_invalid_long_edge, ImageConversionSizing.MAX_DIMENSION)
            } else {
                null
            },
        ),
        enabled = enabled,
        tagPrefix = tag,
    )
    MapleSectionHeader(stringResource(R.string.batch_convert_rotation))
    val rotations = BatchConversionPolicy.ROTATIONS
    MapleSegmented(
        options = rotations.map { it.toString() },
        selectedIndex = rotations.indexOf(settings.rotationDegrees).coerceAtLeast(0),
        onSelect = { onChange(settings.copy(rotationDegrees = rotations[it])) },
        enabled = enabled,
        tagPrefix = "$tag.rotation",
        modifier = Modifier.padding(horizontal = MapleSettingsDefaults.horizontalPadding),
    )
    if (settings.format == ImageOutputFormat.JPEG) {
        ConversionJpegBackgroundSection(settings.jpegBackgroundBlack, { onChange(settings.copy(jpegBackgroundBlack = it)) }, enabled, tag)
    }
    Spacer(Modifier.height(8.dp))
    ConversionExifRow(settings.preserveExif, { onChange(settings.copy(preserveExif = it)) }, enabled, tag, locationShown = showLocation)
    if (showLocation) ConversionLocationRow(settings.preserveLocation, { onChange(settings.copy(preserveLocation = it)) }, enabled, tag)
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
