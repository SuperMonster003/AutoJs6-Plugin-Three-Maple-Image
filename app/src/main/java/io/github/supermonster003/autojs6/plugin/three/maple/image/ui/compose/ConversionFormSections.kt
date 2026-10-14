package io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.supermonster003.autojs6.plugin.three.maple.image.ImageResizeMode
import io.github.supermonster003.autojs6.plugin.three.maple.image.R

/*
 * The sections of the conversion form (ROADMAP 8.3), shared by the converter and the batch conversion so both pages
 * look and behave the same. Every section tags its controls under one prefix ("maple.converter", "maple.batch").
 */

/** "Format" with one segment per output format; [locked] shows the choice without letting it change (overwrite). */
@Composable
internal fun ConversionFormatSection(
    labels: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    enabled: Boolean,
    tagPrefix: String,
    locked: Boolean = false,
) {
    MapleSectionHeader(stringResource(R.string.apk_info_format))
    MapleSegmented(
        options = labels,
        selectedIndex = selectedIndex,
        onSelect = onSelect,
        enabled = enabled && !locked,
        tagPrefix = "$tagPrefix.format",
        modifier = Modifier.padding(horizontal = MapleSettingsDefaults.horizontalPadding),
    )
}

@Composable
internal fun ConversionQualityRow(quality: Int, onChange: (Int) -> Unit, enabled: Boolean, tagPrefix: String) {
    MapleSliderRow(
        title = stringResource(R.string.text_image_conversion_quality),
        value = quality,
        range = 1..100,
        onValueChange = onChange,
        onDark = false,
        enabled = enabled,
        modifier = Modifier.padding(horizontal = MapleSettingsDefaults.horizontalPadding).testTag("$tagPrefix.quality"),
    )
}

@Composable
internal fun ConversionLosslessRow(checked: Boolean, onToggle: (Boolean) -> Unit, enabled: Boolean, tagPrefix: String) {
    MapleSwitchRow(
        title = stringResource(R.string.text_image_conversion_webp_lossless),
        checked = checked,
        onToggle = onToggle,
        enabled = enabled,
        modifier = Modifier.testTag("$tagPrefix.lossless"),
    )
}

/** The inputs of a resize mode, with the error of each field (null when it is fine). */
internal class ConversionResizeFields(
    val percentage: String,
    val onPercentage: (String) -> Unit,
    val percentageError: String?,
    val longEdge: String,
    val onLongEdge: (String) -> Unit,
    val longEdgeError: String?,
    val width: String = "",
    val onWidth: (String) -> Unit = {},
    val widthError: String? = null,
    val height: String = "",
    val onHeight: (String) -> Unit = {},
    val heightError: String? = null,
    val lockAspectRatio: Boolean = false,
    val onLockAspectRatio: (Boolean) -> Unit = {},
)

/** "Resize": one chip per offered mode, then only the current mode's inputs (design draft 7.6, U5). */
@Composable
internal fun ConversionResizeSection(
    modes: List<ImageResizeMode>,
    mode: ImageResizeMode,
    onMode: (ImageResizeMode) -> Unit,
    fields: ConversionResizeFields,
    enabled: Boolean,
    tagPrefix: String,
) {
    val horizontal = MapleSettingsDefaults.horizontalPadding
    MapleSectionHeader(stringResource(R.string.text_image_conversion_resize))
    MapleChipFlow(
        options = modes.map { stringResource(resizeModeLabel(it)) },
        selectedIndex = modes.indexOf(mode).coerceAtLeast(0),
        onSelect = { onMode(modes[it]) },
        enabled = enabled,
        tagPrefix = "$tagPrefix.resize",
        modifier = Modifier.padding(horizontal = horizontal),
    )
    when (mode) {
        ImageResizeMode.ORIGINAL -> Unit
        ImageResizeMode.PERCENTAGE -> MapleNumberField(
            value = fields.percentage,
            onValueChange = fields.onPercentage,
            label = stringResource(R.string.text_image_conversion_percentage_hint),
            suffix = "%",
            error = fields.percentageError,
            enabled = enabled,
            modifier = Modifier.padding(horizontal = horizontal, vertical = 8.dp).testTag("$tagPrefix.percentage"),
        )
        ImageResizeMode.LONG_EDGE -> MapleNumberField(
            value = fields.longEdge,
            onValueChange = fields.onLongEdge,
            label = stringResource(R.string.text_image_conversion_long_edge_hint),
            suffix = "px",
            error = fields.longEdgeError,
            enabled = enabled,
            modifier = Modifier.padding(horizontal = horizontal, vertical = 8.dp).testTag("$tagPrefix.longEdge"),
        )
        ImageResizeMode.CUSTOM -> {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = horizontal, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                MapleNumberField(
                    value = fields.width,
                    onValueChange = fields.onWidth,
                    label = stringResource(R.string.text_image_conversion_width),
                    suffix = "px",
                    error = fields.widthError,
                    enabled = enabled,
                    modifier = Modifier.weight(1f).testTag("$tagPrefix.width"),
                )
                MapleNumberField(
                    value = fields.height,
                    onValueChange = fields.onHeight,
                    label = stringResource(R.string.text_image_conversion_height),
                    suffix = "px",
                    error = fields.heightError,
                    enabled = enabled,
                    modifier = Modifier.weight(1f).testTag("$tagPrefix.height"),
                )
            }
            MapleSwitchRow(
                title = stringResource(R.string.text_image_conversion_lock_aspect_ratio),
                checked = fields.lockAspectRatio,
                onToggle = fields.onLockAspectRatio,
                enabled = enabled,
                modifier = Modifier.testTag("$tagPrefix.lockAspect"),
            )
        }
    }
}

/** A form-wide error under the resize inputs (dimension or memory limits). */
@Composable
internal fun ConversionErrorText(text: String, tagPrefix: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.error,
        modifier = Modifier.padding(horizontal = MapleSettingsDefaults.horizontalPadding, vertical = 4.dp).testTag("$tagPrefix.error"),
    )
}

/** "JPEG transparent areas": white or black (JPEG has no transparency). */
@Composable
internal fun ConversionJpegBackgroundSection(black: Boolean, onChange: (Boolean) -> Unit, enabled: Boolean, tagPrefix: String) {
    MapleSectionHeader(stringResource(R.string.text_image_conversion_jpeg_background))
    MapleSegmented(
        options = listOf(
            stringResource(R.string.text_image_conversion_background_white),
            stringResource(R.string.text_image_conversion_background_black),
        ),
        selectedIndex = if (black) 1 else 0,
        onSelect = { onChange(it == 1) },
        enabled = enabled,
        tagPrefix = "$tagPrefix.background",
        modifier = Modifier.padding(horizontal = MapleSettingsDefaults.horizontalPadding),
    )
}

@Composable
internal fun ConversionExifRow(checked: Boolean, onToggle: (Boolean) -> Unit, enabled: Boolean, tagPrefix: String) {
    MapleSwitchRow(
        title = stringResource(R.string.text_image_conversion_preserve_exif),
        summary = stringResource(R.string.text_image_conversion_preserve_exif_helper),
        checked = checked,
        onToggle = onToggle,
        enabled = enabled,
        modifier = Modifier.testTag("$tagPrefix.exif"),
    )
}

/** "Overwrite the original" (ROADMAP 8.2 / 8.3): only shown where the original can be written back. */
@Composable
internal fun ConversionOverwriteRow(checked: Boolean, onToggle: (Boolean) -> Unit, enabled: Boolean, tagPrefix: String) {
    MapleSwitchRow(
        title = stringResource(R.string.image_editor_save_overwrite),
        summary = stringResource(R.string.image_editor_save_overwrite_summary),
        checked = checked,
        onToggle = onToggle,
        enabled = enabled,
        modifier = Modifier.testTag("$tagPrefix.overwrite"),
    )
}

private fun resizeModeLabel(mode: ImageResizeMode): Int = when (mode) {
    ImageResizeMode.ORIGINAL -> R.string.text_image_conversion_original_size
    ImageResizeMode.PERCENTAGE -> R.string.text_image_conversion_percentage
    ImageResizeMode.LONG_EDGE -> R.string.text_image_conversion_long_edge
    ImageResizeMode.CUSTOM -> R.string.text_image_conversion_custom_size
}
