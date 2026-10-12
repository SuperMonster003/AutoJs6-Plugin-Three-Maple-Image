package io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import io.github.supermonster003.autojs6.plugin.three.maple.image.R
import io.github.supermonster003.autojs6.plugin.three.maple.image.ThemeColorValue

/** Draft of the text tool dialog; the Activity owns it so configuration changes keep the unsaved input. */
internal class EditorTextDialogState(
    text: String,
    color: Int,
    sizeProgress: Int,
    outline: Boolean,
    shadow: Boolean,
    rotationProgress: Int,
) {
    var text by mutableStateOf(text)
    var color by mutableIntStateOf(color)
    var sizeProgress by mutableIntStateOf(sizeProgress)
    var outline by mutableStateOf(outline)
    var shadow by mutableStateOf(shadow)
    var rotationProgress by mutableIntStateOf(rotationProgress)
    var showEmptyError by mutableStateOf(false)
}

/** Draft of the save dialog; derived flags are recomputed by the Activity through [EditorSaveDialogState.onChanged]. */
internal class EditorSaveDialogState(
    val formatLabels: List<String>,
    formatIndex: Int,
    quality: Int,
    webpLossless: Boolean,
) {
    var formatIndex by mutableIntStateOf(formatIndex)
    var quality by mutableIntStateOf(quality)
    var webpLossless by mutableStateOf(webpLossless)
    var losslessAvailable by mutableStateOf(false)
    var qualityEnabled by mutableStateOf(true)
    var onChanged: () -> Unit = {}
}

/** Draft of the palette dialog: a HEX input (`#RRGGBB` or `#AARRGGBB`) plus preset swatches. */
internal class EditorColorDialogState(input: String) {
    var input by mutableStateOf(input)
    var error by mutableStateOf(false)
}

internal object EditorDialogDefaults {
    const val TEXT_SIZE_MAX = 14
    const val TEXT_ROTATION_MAX = 360
    const val TEXT_ROTATION_CENTER = 180
    const val QUALITY_MIN = 1
    const val QUALITY_MAX = 100
    val colorPresets: List<Int> = listOf(0xFFFFFFFF.toInt(), 0xFF000000.toInt()) + ThemeColorValue.presets
}

@Composable
internal fun EditorTextDialog(
    state: EditorTextDialogState,
    sizeLabel: (Int) -> String,
    rotationLabel: (Int) -> String,
    onPickColor: () -> Unit,
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
) {
    MapleDialog(
        title = stringResource(R.string.image_editor_text),
        onDismissRequest = onDismissRequest,
        confirmText = stringResource(R.string.dialog_button_confirm),
        onConfirm = onConfirm,
        dismissText = stringResource(R.string.dialog_button_cancel),
        confirmEnabled = state.text.isNotBlank(),
    ) {
        Column(modifier = Modifier.padding(horizontal = MapleDialogDefaults.contentPadding)) {
            OutlinedTextField(
                value = state.text,
                onValueChange = { state.text = it; state.showEmptyError = false },
                label = { Text(stringResource(R.string.image_editor_text_hint)) },
                isError = state.showEmptyError,
                supportingText = if (state.showEmptyError) ({ Text(stringResource(R.string.text_should_not_be_empty)) }) else null,
                minLines = 2,
                maxLines = 5,
                modifier = Modifier.fillMaxWidth().testTag("maple.editor.text.input"),
            )
            Spacer(Modifier.height(12.dp))
            MapleSliderRow(
                title = stringResource(R.string.text_size),
                value = state.sizeProgress,
                range = 0..EditorDialogDefaults.TEXT_SIZE_MAX,
                onValueChange = { state.sizeProgress = it },
                valueText = sizeLabel(state.sizeProgress),
                onDark = false,
                modifier = Modifier.testTag("maple.editor.text.size"),
            )
            MapleSliderRow(
                title = stringResource(R.string.image_editor_rotation_angle),
                value = state.rotationProgress,
                range = 0..EditorDialogDefaults.TEXT_ROTATION_MAX,
                onValueChange = { state.rotationProgress = it },
                valueText = rotationLabel(state.rotationProgress),
                onDark = false,
                centered = true,
                modifier = Modifier.testTag("maple.editor.text.rotation"),
            )
            MapleColorButton(
                color = state.color,
                label = stringResource(R.string.text_color_palette),
                onClick = onPickColor,
                onDark = false,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        MapleSwitchRow(
            title = stringResource(R.string.image_editor_text_outline),
            checked = state.outline,
            onToggle = { state.outline = it },
            modifier = Modifier.testTag("maple.editor.text.outline"),
        )
        MapleSwitchRow(
            title = stringResource(R.string.image_editor_text_shadow),
            checked = state.shadow,
            onToggle = { state.shadow = it },
            modifier = Modifier.testTag("maple.editor.text.shadow"),
        )
    }
}

@Composable
internal fun EditorSaveDialog(
    state: EditorSaveDialogState,
    hint: String,
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
) {
    MapleDialog(
        title = stringResource(R.string.text_save),
        onDismissRequest = onDismissRequest,
        confirmText = stringResource(R.string.text_save),
        onConfirm = onConfirm,
        dismissText = stringResource(R.string.dialog_button_cancel),
    ) {
        Column(modifier = Modifier.padding(horizontal = MapleDialogDefaults.contentPadding)) {
            Text(
                text = stringResource(R.string.apk_info_format),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 4.dp),
            )
            MapleChipFlow(
                options = state.formatLabels,
                selectedIndex = state.formatIndex,
                onSelect = { state.formatIndex = it; state.onChanged() },
            )
            Spacer(Modifier.height(12.dp))
            MapleSliderRow(
                title = stringResource(R.string.text_image_conversion_quality),
                value = state.quality,
                range = EditorDialogDefaults.QUALITY_MIN..EditorDialogDefaults.QUALITY_MAX,
                onValueChange = { state.quality = it; state.onChanged() },
                onDark = false,
                enabled = state.qualityEnabled,
                modifier = Modifier.testTag("maple.editor.save.quality"),
            )
        }
        if (state.losslessAvailable) {
            MapleSwitchRow(
                title = stringResource(R.string.text_image_conversion_webp_lossless),
                checked = state.webpLossless,
                onToggle = { state.webpLossless = it; state.onChanged() },
                modifier = Modifier.testTag("maple.editor.save.lossless"),
            )
        }
        Text(
            text = hint,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = MapleDialogDefaults.contentPadding, vertical = 8.dp),
        )
    }
}

@Composable
internal fun EditorColorDialog(
    state: EditorColorDialogState,
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
) {
    val parsed = parseEditorColor(state.input)
    MapleDialog(
        title = stringResource(R.string.dialog_title_color_palette),
        onDismissRequest = onDismissRequest,
        confirmText = stringResource(R.string.dialog_button_confirm),
        onConfirm = onConfirm,
        dismissText = stringResource(R.string.dialog_button_cancel),
        confirmEnabled = parsed != null,
    ) {
        Column(modifier = Modifier.padding(horizontal = MapleDialogDefaults.contentPadding)) {
            val presets = EditorDialogDefaults.colorPresets
            val columns = 6
            presets.chunked(columns).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    row.forEach { color ->
                        val selected = parsed == color
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .size(44.dp)
                                .selectable(selected = selected, role = Role.RadioButton, onClick = {
                                    state.input = String.format(java.util.Locale.ROOT, "#%08X", color)
                                    state.error = false
                                })
                                .background(Color(color), CircleShape)
                                .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                                .semantics { contentDescription = ThemeColorValue.hex(color) }
                                .testTag("maple.editor.color.${ThemeColorValue.hex(color)}"),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (selected) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_check_24),
                                    contentDescription = null,
                                    tint = Color(ThemeColorValue.onColor(color)),
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                        }
                    }
                    repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
                }
            }
            OutlinedTextField(
                value = state.input,
                onValueChange = { state.input = it.take(16); state.error = false },
                label = { Text(stringResource(R.string.theme_picker_input)) },
                placeholder = { Text("#RRGGBB / #AARRGGBB") },
                isError = state.error || (state.input.isNotBlank() && parsed == null),
                supportingText = if (state.error || (state.input.isNotBlank() && parsed == null)) ({ Text(stringResource(R.string.text_failed)) }) else null,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(textDirection = TextDirection.Ltr),
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp).testTag("maple.editor.color.input"),
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 8.dp)
                    .height(40.dp)
                    .background(Color(parsed ?: 0x00000000), RoundedCornerShape(12.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                    .testTag("maple.editor.color.preview"),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.theme_picker_preview),
                    color = if (parsed != null) Color(ThemeColorValue.onColor(parsed)) else MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.labelLarge,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

/** Parses `#RRGGBB` / `#AARRGGBB` (hash optional); the editor accepts translucent brush and text colors. */
internal fun parseEditorColor(value: String): Int? {
    val normalized = value.trim().removePrefix("#")
    if (normalized.length != 6 && normalized.length != 8) return null
    val parsed = normalized.toLongOrNull(16) ?: return null
    return if (normalized.length == 6) (0xFF000000L or parsed).toInt() else parsed.toInt()
}
