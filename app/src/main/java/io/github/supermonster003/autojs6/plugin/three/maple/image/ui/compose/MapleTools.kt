package io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

/** One entry of [MapleToolStrip]. */
internal data class MapleTool(
    val id: String,
    val icon: Painter,
    val label: String,
    val onClick: () -> Unit,
    val selected: Boolean = false,
    val enabled: Boolean = true,
    val contentDescription: String = label,
)

/** Size preset of [MapleToolStrip]: primary groups (72 x 64 dp) or secondary tools (64 x 56 dp). */
internal enum class MapleToolStripSize(val minWidth: Int, val height: Int) { PRIMARY(72, 64), SECONDARY(64, 56) }

/**
 * Horizontal strip of icon + 12 sp label tools on the strong dark scrim (design draft 7.5). The selected item
 * uses the overlay accent on a tinted pill; disabled items drop to 38% white.
 */
@Composable
internal fun MapleToolStrip(
    tools: List<MapleTool>,
    size: MapleToolStripSize,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val canvas = MapleTheme.canvas
    LazyRow(
        modifier = modifier.fillMaxWidth().background(canvas.scrimStrong),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        items(tools, key = { it.id }) { tool ->
            val active = enabled && tool.enabled
            val color = when {
                !active -> canvas.onScrimDisabled
                tool.selected -> canvas.accentOnScrim
                else -> canvas.onScrim
            }
            val background = if (tool.selected) canvas.accentOnScrim.copy(alpha = 0.16f) else Color.Transparent
            Column(
                modifier = Modifier
                    .widthIn(min = size.minWidth.dp)
                    .height(size.height.dp)
                    .background(background, RoundedCornerShape(12.dp))
                    .selectable(selected = tool.selected, enabled = active, role = Role.Button, onClick = tool.onClick)
                    .semantics { contentDescription = tool.contentDescription }
                    .testTag("maple.tool.${tool.id}")
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(painter = tool.icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
                Spacer(Modifier.height(4.dp))
                Text(
                    text = tool.label,
                    style = MaterialTheme.typography.labelMedium,
                    color = color,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

/**
 * Parameter panel shown instead of the tool strips while a tool is active (design draft 7.5): dark strong scrim,
 * caller content, then equal cancel / confirm buttons of at least 48 dp.
 */
@Composable
internal fun MapleParameterPanel(
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
    cancelText: String,
    confirmText: String,
    modifier: Modifier = Modifier,
    confirmEnabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    val canvas = MapleTheme.canvas
    Column(modifier = modifier.fillMaxWidth().background(canvas.scrimStrong).padding(horizontal = 16.dp, vertical = 8.dp)) {
        content()
        Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            TextButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f).defaultMinSize(minHeight = 48.dp).testTag("maple.panel.cancel"),
            ) { Text(cancelText, color = canvas.onScrim) }
            Button(
                onClick = onConfirm,
                enabled = confirmEnabled,
                modifier = Modifier.weight(1f).defaultMinSize(minHeight = 48.dp).testTag("maple.panel.confirm"),
            ) { Text(confirmText) }
        }
    }
}

/** Slider with a title and a monospaced value; [centered] draws the zero tick for signed ranges. */
@Composable
internal fun MapleSliderRow(
    title: String,
    value: Int,
    range: IntRange,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    valueText: String = value.toString(),
    onDark: Boolean = true,
    enabled: Boolean = true,
    centered: Boolean = false,
) {
    val canvas = MapleTheme.canvas
    val textColor = if (onDark) canvas.onScrim else MaterialTheme.colorScheme.onSurface
    val mutedColor = if (onDark) canvas.onScrimMuted else MaterialTheme.colorScheme.onSurfaceVariant
    Column(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(title, style = MaterialTheme.typography.bodyLarge, color = textColor.disabledIf(!enabled), modifier = Modifier.weight(1f))
            Text(
                valueText,
                style = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace),
                color = mutedColor.disabledIf(!enabled),
                modifier = Modifier.semantics { contentDescription = "$title $valueText" },
            )
        }
        val accent = if (onDark) canvas.accentOnScrim else MaterialTheme.colorScheme.primary
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            if (centered) {
                // Zero tick of a symmetric range: the track is centered, so the midpoint of the row is value 0.
                Box(modifier = Modifier.width(2.dp).height(20.dp).background(mutedColor.copy(alpha = 0.6f)))
            }
            Slider(
                value = value.toFloat(),
                onValueChange = { onValueChange(it.roundToInt().coerceIn(range)) },
                valueRange = range.first.toFloat()..range.last.toFloat(),
                enabled = enabled,
                colors = SliderDefaults.colors(
                    thumbColor = accent,
                    activeTrackColor = accent,
                    inactiveTrackColor = accent.copy(alpha = 0.3f),
                ),
                modifier = Modifier.fillMaxWidth().testTag("maple.slider.$title"),
            )
        }
    }
}

/** Single-choice segmented row for 2 to 5 short options. */
@Composable
internal fun MapleSegmented(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    tagPrefix: String = "maple.segment",
) {
    SingleChoiceSegmentedButtonRow(modifier = modifier.fillMaxWidth()) {
        options.forEachIndexed { index, label ->
            SegmentedButton(
                selected = index == selectedIndex,
                onClick = { onSelect(index) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                enabled = enabled,
                modifier = Modifier.testTag("$tagPrefix.$index"),
            ) { Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis) }
        }
    }
}

/** Scrollable chip row for longer option sets such as crop aspect ratios. */
@Composable
internal fun MapleChipRow(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    onDark: Boolean = true,
) {
    val canvas = MapleTheme.canvas
    LazyRow(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(options.size) { index ->
            FilterChip(
                selected = index == selectedIndex,
                onClick = { onSelect(index) },
                label = { Text(options[index]) },
                colors = if (onDark) {
                    FilterChipDefaults.filterChipColors(
                        labelColor = canvas.onScrim,
                        selectedLabelColor = canvas.accentOnScrim,
                        selectedContainerColor = canvas.accentOnScrim.copy(alpha = 0.16f),
                    )
                } else {
                    FilterChipDefaults.filterChipColors()
                },
                modifier = Modifier.testTag("maple.chip.$index"),
            )
        }
    }
}

/** Wrapping chip set for option labels that are too long for a segmented row (for example output formats). */
@Composable
internal fun MapleChipFlow(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    tagPrefix: String = "maple.chip",
) {
    FlowRow(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEachIndexed { index, label ->
            FilterChip(
                selected = index == selectedIndex,
                onClick = { onSelect(index) },
                label = { Text(label) },
                enabled = enabled,
                modifier = Modifier.testTag("$tagPrefix.$index"),
            )
        }
    }
}

/** 40 dp circular swatch with a label that opens the color picker. */
@Composable
internal fun MapleColorButton(
    color: Int,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onDark: Boolean = true,
) {
    val canvas = MapleTheme.canvas
    val textColor = if (onDark) canvas.onScrim else MaterialTheme.colorScheme.onSurface
    Row(
        modifier = modifier
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .semantics(mergeDescendants = true) {}
            .defaultMinSize(minHeight = 48.dp)
            .padding(horizontal = 4.dp)
            .testTag("maple.colorButton.$label"),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(Color(color or 0xFF000000.toInt()), CircleShape)
                .border(2.dp, textColor.copy(alpha = 0.7f), CircleShape),
        )
        Spacer(Modifier.width(12.dp))
        Text(label, style = MaterialTheme.typography.bodyLarge, color = textColor.disabledIf(!enabled))
    }
}
