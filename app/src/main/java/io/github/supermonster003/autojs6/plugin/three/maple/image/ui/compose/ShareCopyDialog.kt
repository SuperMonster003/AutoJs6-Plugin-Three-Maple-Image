package io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import io.github.supermonster003.autojs6.plugin.three.maple.image.ImageConversionSizing
import io.github.supermonster003.autojs6.plugin.three.maple.image.R
import io.github.supermonster003.autojs6.plugin.three.maple.image.ShareCopyMode
import io.github.supermonster003.autojs6.plugin.three.maple.image.ShareCopyRequest

/** Draft of "Share a copy" (ROADMAP 9.1): without metadata, or smaller at 1080 / 1920 / a custom long edge. */
internal class ShareCopyDialogState(
    /** The image is animated: its copy is a still picture. */
    val stillCopy: Boolean = false,
) {
    var mode by mutableStateOf(ShareCopyMode.WITHOUT_METADATA)
    var sizeIndex by mutableIntStateOf(ShareCopyRequest.LONG_EDGE_PRESETS.indexOf(ShareCopyRequest.DEFAULT_LONG_EDGE))
    var custom by mutableStateOf("")

    /** What to make; null while a custom long edge is missing or out of range. */
    fun request(): ShareCopyRequest? = when (mode) {
        ShareCopyMode.WITHOUT_METADATA -> ShareCopyRequest(ShareCopyMode.WITHOUT_METADATA)
        ShareCopyMode.SMALLER -> {
            val edge = ShareCopyRequest.LONG_EDGE_PRESETS.getOrNull(sizeIndex)
                ?: custom.trim().toIntOrNull()?.takeIf { it in 1..ImageConversionSizing.MAX_DIMENSION }
            edge?.let { ShareCopyRequest(ShareCopyMode.SMALLER, it) }
        }
    }
}

@Composable
internal fun ShareCopyDialog(state: ShareCopyDialogState, onDismissRequest: () -> Unit, onShare: () -> Unit) {
    MapleDialog(
        title = stringResource(R.string.action_share_copy),
        onDismissRequest = onDismissRequest,
        confirmText = stringResource(R.string.action_share),
        onConfirm = onShare,
        dismissText = stringResource(R.string.dialog_button_cancel),
        confirmEnabled = state.request() != null,
        modifier = Modifier.testTag("maple.shareCopy.dialog"),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            MapleChoiceRow(
                option = MapleChoice(
                    stringResource(R.string.share_copy_without_metadata),
                    stringResource(R.string.share_copy_without_metadata_summary),
                ),
                selected = state.mode == ShareCopyMode.WITHOUT_METADATA,
                onSelect = { state.mode = ShareCopyMode.WITHOUT_METADATA },
                modifier = Modifier.testTag("maple.shareCopy.withoutMetadata"),
            )
            MapleChoiceRow(
                option = MapleChoice(stringResource(R.string.share_copy_smaller), stringResource(R.string.share_copy_smaller_summary)),
                selected = state.mode == ShareCopyMode.SMALLER,
                onSelect = { state.mode = ShareCopyMode.SMALLER },
                modifier = Modifier.testTag("maple.shareCopy.smaller"),
            )
            if (state.mode == ShareCopyMode.SMALLER) {
                MapleSegmented(
                    options = ShareCopyRequest.LONG_EDGE_PRESETS.map { it.toString() } + stringResource(R.string.share_copy_custom),
                    selectedIndex = state.sizeIndex,
                    onSelect = { state.sizeIndex = it },
                    tagPrefix = "maple.shareCopy.size",
                    modifier = Modifier.padding(horizontal = MapleDialogDefaults.contentPadding, vertical = 8.dp),
                )
                if (state.sizeIndex == ShareCopyRequest.LONG_EDGE_PRESETS.size) {
                    OutlinedTextField(
                        value = state.custom,
                        onValueChange = { state.custom = it.filter(Char::isDigit).take(5) },
                        label = { Text(stringResource(R.string.share_copy_long_edge)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = MapleDialogDefaults.contentPadding)
                            .testTag("maple.shareCopy.custom"),
                    )
                }
            }
            if (state.stillCopy) {
                Text(
                    text = stringResource(R.string.share_copy_still_note),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = MapleDialogDefaults.contentPadding, vertical = 8.dp),
                )
            }
        }
    }
}
