package io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import io.github.supermonster003.autojs6.plugin.three.maple.image.R

/** Design tokens of the settings list (design draft 5.4, Three settings specification section 4). */
internal object MapleSettingsDefaults {
    val horizontalPadding = 24.dp
    val verticalPadding = 12.dp
    val rowMinHeight = 72.dp
    val singleLineRowMinHeight = 56.dp
    val iconSize = 24.dp
    val iconSlot = 40.dp
    val textStart = 64.dp
    val headerTopPadding = 24.dp
    val headerBottomPadding = 8.dp
    const val CHEVRON_ALPHA = 0.6f
}

/** Group title: 14 sp medium, secondary text color, 24 / 8 dp vertical padding; a heading for screen readers. */
@Composable
internal fun MapleSectionHeader(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
            .semantics { heading() }
            .fillMaxWidth()
            .padding(
                start = MapleSettingsDefaults.horizontalPadding,
                end = MapleSettingsDefaults.horizontalPadding,
                top = MapleSettingsDefaults.headerTopPadding,
                bottom = MapleSettingsDefaults.headerBottomPadding,
            ),
    )
}

/** In-group hairline starting at the text start (64 dp with icons). */
@Composable
internal fun MapleHairline(modifier: Modifier = Modifier, insetStart: androidx.compose.ui.unit.Dp = MapleSettingsDefaults.textStart) {
    HorizontalDivider(
        modifier = modifier.padding(start = insetStart, end = MapleSettingsDefaults.horizontalPadding),
        thickness = 1.dp,
        color = MaterialTheme.colorScheme.outlineVariant,
    )
}

/**
 * Standard setting row: optional leading icon, title / summary column, optional trailing value and chevron.
 * The whole row is the single click target; decorative icons carry no accessibility focus.
 */
@Composable
internal fun MapleSettingsRow(
    title: String,
    modifier: Modifier = Modifier,
    summary: String? = null,
    @DrawableRes iconResource: Int? = null,
    value: String? = null,
    showChevron: Boolean = true,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
) {
    val minHeight = if (summary.isNullOrEmpty()) MapleSettingsDefaults.singleLineRowMinHeight else MapleSettingsDefaults.rowMinHeight
    val clickModifier = if (onClick != null) {
        Modifier.clickable(enabled = enabled, role = Role.Button, onClick = onClick)
    } else {
        Modifier
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(clickModifier)
            .semantics(mergeDescendants = true) {}
            .defaultMinSize(minHeight = minHeight)
            .padding(horizontal = MapleSettingsDefaults.horizontalPadding, vertical = MapleSettingsDefaults.verticalPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (iconResource != null) {
            LeadingIcon(painterResource(iconResource), enabled)
        }
        RowTexts(title, summary, enabled, Modifier.weight(1f))
        if (value != null) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.disabledIf(!enabled),
                modifier = Modifier.padding(start = 12.dp),
            )
        }
        if (showChevron && onClick != null) {
            Spacer(Modifier.width(16.dp))
            Icon(
                painter = painterResource(R.drawable.ic_chevron_right_24),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = MapleSettingsDefaults.CHEVRON_ALPHA).disabledIf(!enabled),
                modifier = Modifier.size(MapleSettingsDefaults.iconSize),
            )
        }
    }
}

/**
 * Setting row whose trailing switch mirrors row taps. The row owns the toggle semantics and fires
 * [onToggle] exactly once per tap; the switch itself only displays state.
 */
@Composable
internal fun MapleSwitchRow(
    title: String,
    checked: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    summary: String? = null,
    @DrawableRes iconResource: Int? = null,
    enabled: Boolean = true,
) {
    val minHeight = if (summary.isNullOrEmpty()) MapleSettingsDefaults.singleLineRowMinHeight else MapleSettingsDefaults.rowMinHeight
    Row(
        modifier = modifier
            .fillMaxWidth()
            .toggleable(value = checked, enabled = enabled, role = Role.Switch, onValueChange = onToggle)
            .defaultMinSize(minHeight = minHeight)
            .padding(horizontal = MapleSettingsDefaults.horizontalPadding, vertical = MapleSettingsDefaults.verticalPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (iconResource != null) {
            LeadingIcon(painterResource(iconResource), enabled)
        }
        RowTexts(title, summary, enabled, Modifier.weight(1f))
        Spacer(Modifier.width(12.dp))
        Switch(checked = checked, onCheckedChange = null, enabled = enabled)
    }
}

@Composable
private fun LeadingIcon(painter: Painter, enabled: Boolean) {
    Icon(
        painter = painter,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurfaceVariant.disabledIf(!enabled),
        modifier = Modifier.size(MapleSettingsDefaults.iconSize),
    )
    Spacer(Modifier.width(MapleSettingsDefaults.iconSlot - MapleSettingsDefaults.iconSize))
}

@Composable
private fun RowTexts(title: String, summary: String?, enabled: Boolean, modifier: Modifier) {
    Column(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.disabledIf(!enabled),
        )
        if (!summary.isNullOrEmpty()) {
            Text(
                text = summary,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.disabledIf(!enabled),
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

/** 38% alpha for disabled text and icons (design draft 5.1). */
internal fun androidx.compose.ui.graphics.Color.disabledIf(disabled: Boolean): androidx.compose.ui.graphics.Color =
    if (disabled) copy(alpha = alpha * 0.38f) else this
