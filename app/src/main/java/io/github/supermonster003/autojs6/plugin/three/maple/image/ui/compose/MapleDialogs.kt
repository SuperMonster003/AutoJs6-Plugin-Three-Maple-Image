package io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/** Geometry of the centered dialogs (design draft 5.4, Three settings specification section 8). */
internal object MapleDialogDefaults {
    val cornerRadius = 24.dp
    val maxWidth = 560.dp
    val horizontalMargin = 24.dp
    val contentPadding = 24.dp
    val buttonMinHeight = 48.dp
    const val MAX_HEIGHT_FRACTION = 0.85f
}

/**
 * Centered dialog container: 24 dp corners, at most 560 dp wide, at most 85% of the window high. The title and
 * the button row stay fixed while only [content] scrolls. Dismissing (back, outside tap, cancel) never confirms.
 */
@Composable
internal fun MapleDialog(
    title: String,
    onDismissRequest: () -> Unit,
    confirmText: String,
    onConfirm: () -> Unit,
    dismissText: String,
    modifier: Modifier = Modifier,
    confirmEnabled: Boolean = true,
    scrollable: Boolean = true,
    content: @Composable () -> Unit,
) {
    Dialog(onDismissRequest = onDismissRequest, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        BoxWithConstraints(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            val width = (maxWidth - MapleDialogDefaults.horizontalMargin * 2).coerceAtMost(MapleDialogDefaults.maxWidth)
            val height = maxHeight * MapleDialogDefaults.MAX_HEIGHT_FRACTION
            Surface(
                modifier = Modifier.widthIn(max = width).width(width).heightIn(max = height).testTag("maple.dialog"),
                shape = RoundedCornerShape(MapleDialogDefaults.cornerRadius),
                color = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                tonalElevation = 0.dp,
            ) {
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(
                            start = MapleDialogDefaults.contentPadding,
                            end = MapleDialogDefaults.contentPadding,
                            top = MapleDialogDefaults.contentPadding,
                            bottom = 16.dp,
                        ),
                    )
                    val scrollModifier = if (scrollable) Modifier.verticalScroll(rememberScrollState()) else Modifier
                    Column(modifier = Modifier.weight(1f, fill = false).then(scrollModifier)) {
                        content()
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        TextButton(
                            onClick = onDismissRequest,
                            modifier = Modifier.defaultMinSize(minHeight = MapleDialogDefaults.buttonMinHeight).testTag("maple.dialog.cancel"),
                        ) { Text(dismissText) }
                        Spacer(Modifier.width(8.dp))
                        TextButton(
                            onClick = onConfirm,
                            enabled = confirmEnabled,
                            modifier = Modifier.defaultMinSize(minHeight = MapleDialogDefaults.buttonMinHeight).testTag("maple.dialog.confirm"),
                        ) { Text(confirmText) }
                    }
                }
            }
        }
    }
}

/** One option of [MapleChoiceDialog]: 16 sp label, optional 14 sp description, optional disabled state. */
internal data class MapleChoice(val label: String, val description: String? = null, val enabled: Boolean = true)

/**
 * Single-choice dialog with the agreed "select, then confirm" semantics: tapping a row only changes the pending
 * selection, [onConfirm] fires once when the confirm button is pressed with a valid enabled index, and cancel,
 * back or an outside tap leave the saved value untouched. The pending index survives configuration changes.
 */
@Composable
internal fun MapleChoiceDialog(
    title: String,
    options: List<MapleChoice>,
    selectedIndex: Int,
    onDismissRequest: () -> Unit,
    onConfirm: (Int) -> Unit,
    confirmText: String,
    dismissText: String,
    modifier: Modifier = Modifier,
) {
    var pending by rememberSaveable(selectedIndex) { mutableIntStateOf(selectedIndex) }
    val valid = pending in options.indices && options[pending].enabled
    MapleDialog(
        title = title,
        onDismissRequest = onDismissRequest,
        confirmText = confirmText,
        onConfirm = { if (valid) onConfirm(pending) },
        dismissText = dismissText,
        confirmEnabled = valid,
        modifier = modifier,
    ) {
        Column(modifier = Modifier.testTag("maple.choice.list")) {
            options.forEachIndexed { index, option ->
                ChoiceRow(
                    option = option,
                    selected = index == pending,
                    onSelect = { pending = index },
                    modifier = Modifier.testTag("maple.choice.$index"),
                )
            }
        }
    }
}

@Composable
private fun ChoiceRow(option: MapleChoice, selected: Boolean, onSelect: () -> Unit, modifier: Modifier = Modifier) {
    val minHeight = if (option.description.isNullOrEmpty()) 56.dp else 72.dp
    Row(
        modifier = modifier
            .fillMaxWidth()
            .selectable(selected = selected, enabled = option.enabled, role = Role.RadioButton, onClick = onSelect)
            .defaultMinSize(minHeight = minHeight)
            .padding(horizontal = MapleDialogDefaults.contentPadding, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = null, enabled = option.enabled)
        Spacer(Modifier.width(16.dp))
        Column {
            Text(
                text = option.label,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.disabledIf(!option.enabled),
            )
            if (!option.description.isNullOrEmpty()) {
                Text(
                    text = option.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.disabledIf(!option.enabled),
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}
