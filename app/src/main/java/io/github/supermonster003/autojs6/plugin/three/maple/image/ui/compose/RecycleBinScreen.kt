package io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.supermonster003.autojs6.plugin.three.maple.image.R
import io.github.supermonster003.autojs6.plugin.three.maple.image.RecycleBinEntry
import io.github.supermonster003.autojs6.plugin.three.maple.image.RecycleBinUiState

internal class RecycleBinCallbacks(
    val onBack: () -> Unit,
    val onToggle: (Long) -> Unit,
    val onSelectAll: () -> Unit,
    val onClearSelection: () -> Unit,
    val onRestore: () -> Unit,
    val onDeleteForever: () -> Unit,
    val onEmpty: () -> Unit,
)

/**
 * Recycle bin (ROADMAP 7.2): trashed images with the days left before they are removed for good. There is no
 * viewer here, so a tap selects; the bar restores or deletes the selection and the menu empties the bin.
 */
@Composable
internal fun RecycleBinScreen(state: RecycleBinUiState, callbacks: RecycleBinCallbacks, columns: Int, snackbarHostState: SnackbarHostState? = null) {
    var menuOpen by remember { mutableStateOf(false) }
    val selecting = state.selection.isNotEmpty()
    BackHandler(enabled = selecting, onBack = callbacks.onClearSelection)
    MapleScreen(
        title = if (selecting) {
            pluralStringResource(R.plurals.gallery_selected_count, state.selection.size, state.selection.size)
        } else {
            stringResource(R.string.recycle_bin_title)
        },
        subtitle = if (selecting || state.entries.isEmpty()) null else pluralStringResource(R.plurals.gallery_image_count, state.entries.size, state.entries.size),
        onBack = if (selecting) callbacks.onClearSelection else callbacks.onBack,
        navigationIcon = if (selecting) painterResource(R.drawable.ic_close_24) else null,
        navigationContentDescription = if (selecting) stringResource(R.string.selection_exit) else null,
        actions = {
            if (state.entries.isNotEmpty()) {
                IconButton(onClick = callbacks.onSelectAll, modifier = Modifier.testTag("maple.bin.selectAll")) {
                    Icon(painterResource(R.drawable.ic_select_all_24), contentDescription = stringResource(R.string.selection_select_all))
                }
                IconButton(onClick = { menuOpen = true }, modifier = Modifier.testTag("maple.bin.more")) {
                    Icon(painterResource(R.drawable.ic_more_vert_24), contentDescription = stringResource(R.string.action_more))
                }
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.recycle_bin_empty_action)) },
                        leadingIcon = { Icon(painterResource(R.drawable.ic_delete_forever_24), contentDescription = null) },
                        onClick = { menuOpen = false; callbacks.onEmpty() },
                        modifier = Modifier.testTag("maple.bin.empty"),
                    )
                }
            }
        },
        bottomBar = {
            if (selecting) {
                MapleBottomActionBar(
                    actions = listOf(
                        MapleAction(painterResource(R.drawable.ic_restore_from_trash_24), stringResource(R.string.action_restore), callbacks.onRestore, testTag = "maple.bin.restore"),
                        MapleAction(painterResource(R.drawable.ic_delete_forever_24), stringResource(R.string.action_delete_forever), callbacks.onDeleteForever, testTag = "maple.bin.deleteForever"),
                    ),
                    navigationBarInset = true,
                )
            }
        },
        snackbarHostState = snackbarHostState,
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                state.loading -> MapleLoading()
                state.entries.isEmpty() -> MapleEmptyState(
                    title = stringResource(R.string.recycle_bin_empty_title),
                    body = stringResource(R.string.recycle_bin_empty_body),
                    icon = painterResource(R.drawable.ic_delete_24),
                    modifier = Modifier.fillMaxWidth().testTag("maple.bin.emptyState"),
                )
                else -> LazyVerticalGrid(
                    columns = GridCells.Fixed(columns),
                    horizontalArrangement = Arrangement.spacedBy(MapleGridDefaults.mediaGap),
                    verticalArrangement = Arrangement.spacedBy(MapleGridDefaults.mediaGap),
                    modifier = Modifier.fillMaxSize().testTag("maple.bin.grid"),
                ) {
                    items(state.entries, key = { it.id }) { entry -> BinCell(entry, selecting, entry.id in state.selection, callbacks) }
                }
            }
        }
    }
}

@Composable
private fun BinCell(entry: RecycleBinEntry, selecting: Boolean, selected: Boolean, callbacks: RecycleBinCallbacks) {
    val days = pluralStringResource(R.plurals.recycle_bin_days_left, entry.remainingDays, entry.remainingDays)
    Box {
        MapleMediaCell(
            model = entry.model,
            description = "${entry.displayName}, $days",
            badge = mediaTypeBadge(entry.mimeType),
            selectionMode = selecting,
            selected = selected,
            onLongClick = { callbacks.onToggle(entry.id) },
            onLongClickLabel = stringResource(R.string.selection_start),
            onClick = { callbacks.onToggle(entry.id) },
            modifier = Modifier.testTag("maple.bin.item.${entry.id}"),
        )
        Text(
            text = days,
            style = MaterialTheme.typography.labelMedium,
            color = MapleTheme.canvas.onScrim,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(4.dp)
                .background(MapleTheme.canvas.scrimStrong, RoundedCornerShape(4.dp))
                .padding(horizontal = 4.dp, vertical = 1.dp),
        )
    }
}
