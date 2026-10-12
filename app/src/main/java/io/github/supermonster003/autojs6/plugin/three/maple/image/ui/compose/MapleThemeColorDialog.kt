package io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import io.github.supermonster003.autojs6.plugin.three.maple.image.R
import io.github.supermonster003.autojs6.plugin.three.maple.image.ThemeAccentRoles
import io.github.supermonster003.autojs6.plugin.three.maple.image.ThemeColorValue

/** Labels of [MapleThemeColorDialog]; all come from string resources of the calling screen. */
internal data class MapleThemeColorLabels(
    val title: String,
    val followHost: String,
    val presets: String,
    val custom: String,
    val input: String,
    val invalid: String,
    val preview: String,
    val confirm: String,
    val dismiss: String,
)

/**
 * Theme color picker (design draft section 6, Three settings specification section 9): follow AutoJs6 row, the
 * shared 16-color grid, a HEX / RGB input and a local preview. Nothing outside the dialog changes until the
 * confirm button is pressed; `null` in [onConfirm] means "follow AutoJs6". The draft survives recreation.
 */
@Composable
internal fun MapleThemeColorDialog(
    currentColor: Int?,
    hostColor: Int,
    labels: MapleThemeColorLabels,
    onDismissRequest: () -> Unit,
    onConfirm: (Int?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var followsHost by rememberSaveable { mutableStateOf(currentColor == null) }
    var fromPreset by rememberSaveable { mutableStateOf(currentColor != null && currentColor in ThemeColorValue.presets) }
    var selected by rememberSaveable { mutableIntStateOf(currentColor ?: hostColor) }
    var text by rememberSaveable { mutableStateOf(ThemeColorValue.hex(currentColor ?: hostColor)) }
    val parsed = ThemeColorValue.parse(text)
    val valid = parsed != null
    val effective = if (followsHost) hostColor else selected
    val dark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val roles = ThemeAccentRoles.fromSeed(effective, dark)
    MapleDialog(
        title = labels.title,
        onDismissRequest = onDismissRequest,
        confirmText = labels.confirm,
        onConfirm = { if (followsHost || valid) onConfirm(if (followsHost) null else selected) },
        dismissText = labels.dismiss,
        confirmEnabled = followsHost || valid,
        modifier = modifier,
    ) {
        Column(modifier = Modifier.padding(horizontal = MapleDialogDefaults.contentPadding)) {
            ThemeRadioRow(
                selected = followsHost,
                label = labels.followHost,
                description = ThemeColorValue.hex(hostColor),
                onSelect = { followsHost = true },
                modifier = Modifier.testTag("maple.theme.follow"),
            )
            Text(
                text = labels.presets,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 12.dp, bottom = 8.dp),
            )
            PresetGrid(
                selectedSeed = if (!followsHost && fromPreset && valid) selected else null,
                labels = labels,
                onSelect = { seed ->
                    followsHost = false
                    fromPreset = true
                    selected = seed
                    text = ThemeColorValue.hex(seed)
                },
            )
            ThemeRadioRow(
                selected = !followsHost && !fromPreset,
                label = labels.custom,
                description = null,
                onSelect = { followsHost = false; fromPreset = false },
                modifier = Modifier.testTag("maple.theme.custom"),
            )
            OutlinedTextField(
                value = text,
                onValueChange = { input ->
                    text = input.take(64)
                    followsHost = false
                    fromPreset = false
                    ThemeColorValue.parse(text)?.let { selected = it }
                },
                label = { Text(labels.input) },
                placeholder = { Text("#RRGGBB / rgb(r, g, b)") },
                isError = !followsHost && !valid,
                supportingText = if (!followsHost && !valid) ({ Text(labels.invalid) }) else null,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(textDirection = TextDirection.Ltr),
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp).testTag("maple.theme.input"),
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .defaultMinSize(minHeight = 48.dp)
                    .background(Color(roles.primary), RoundedCornerShape(12.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                    .semantics { contentDescription = "${labels.preview} ${ThemeColorValue.hex(effective)}" }
                    .testTag("maple.theme.preview"),
                contentAlignment = Alignment.Center,
            ) {
                Text(labels.preview, color = Color(roles.onPrimary), style = MaterialTheme.typography.labelLarge)
            }
            Text(
                text = "${ThemeColorValue.hex(effective)}  ${ThemeColorValue.rgb(effective)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 8.dp).testTag("maple.theme.value"),
            )
        }
    }
}

@Composable
private fun ThemeRadioRow(selected: Boolean, label: String, description: String?, onSelect: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .selectable(selected = selected, role = Role.RadioButton, onClick = onSelect)
            .defaultMinSize(minHeight = 48.dp)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = null)
        Spacer(Modifier.width(16.dp))
        Column {
            Text(label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
            if (description != null) {
                Text(description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun PresetGrid(selectedSeed: Int?, labels: MapleThemeColorLabels, onSelect: (Int) -> Unit) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val columns = (maxWidth / 56.dp).toInt().coerceIn(1, 8)
        val rows = ThemeColorValue.presets.chunked(columns)
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            rows.forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
                    row.forEach { seed ->
                        Swatch(
                            seed = seed,
                            selected = seed == selectedSeed,
                            description = "${labels.presets} ${ThemeColorValue.hex(seed)}",
                            onSelect = { onSelect(seed) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
}

@Composable
private fun Swatch(seed: Int, selected: Boolean, description: String, onSelect: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier = modifier.height(52.dp), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .selectable(selected = selected, role = Role.RadioButton, onClick = onSelect)
                .background(Color(seed), CircleShape)
                .border(1.dp, MaterialTheme.colorScheme.onSurfaceVariant, CircleShape)
                .semantics { contentDescription = description }
                .testTag("maple.theme.preset.${ThemeColorValue.hex(seed)}"),
            contentAlignment = Alignment.Center,
        ) {
            if (selected) {
                Icon(
                    painter = painterResource(R.drawable.ic_check_24),
                    contentDescription = null,
                    tint = Color(ThemeColorValue.onColor(seed)),
                    modifier = Modifier.size(24.dp),
                )
            }
        }
    }
}

private fun Color.luminance(): Float = 0.2126f * red + 0.7152f * green + 0.0722f * blue
