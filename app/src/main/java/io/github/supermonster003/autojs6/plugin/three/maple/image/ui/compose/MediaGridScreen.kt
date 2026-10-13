package io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.supermonster003.autojs6.plugin.three.maple.image.DocumentTreeLimit
import io.github.supermonster003.autojs6.plugin.three.maple.image.DocumentTreeWalker
import io.github.supermonster003.autojs6.plugin.three.maple.image.MediaGridUiState
import io.github.supermonster003.autojs6.plugin.three.maple.image.MediaItem
import io.github.supermonster003.autojs6.plugin.three.maple.image.MediaSort
import io.github.supermonster003.autojs6.plugin.three.maple.image.R
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter

/** Media grid of one album (design draft 8.2): square cells with type badges, provider paging, sort menu. */
@Composable
internal fun MediaGridScreen(
    state: MediaGridUiState,
    onBack: () -> Unit,
    onOpen: (MediaItem) -> Unit,
    onLoadMore: () -> Unit,
    onSort: (MediaSort) -> Unit,
    onRevealed: () -> Unit,
) {
    var sortOpen by remember { mutableStateOf(false) }
    val album = state.album
    val countText = album?.let { stringResource(R.string.gallery_image_count, if (it.count >= 0) it.count else state.items.size) }
    MapleScreen(
        title = album?.name.orEmpty(),
        subtitle = countText,
        onBack = onBack,
        actions = {
            IconButton(onClick = { sortOpen = true }, modifier = Modifier.testTag("maple.media.sort")) {
                Icon(painterResource(R.drawable.ic_swap_vert_24), contentDescription = stringResource(R.string.gallery_sort))
            }
            SortMenu(expanded = sortOpen, sort = state.sort, dateFields = true, onDismiss = { sortOpen = false }, onSelect = onSort)
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (state.loading && state.items.isEmpty()) {
                MapleLoading()
                return@Column
            }
            if (state.items.isEmpty()) {
                MapleEmptyState(
                    title = stringResource(R.string.gallery_empty_album_title),
                    icon = painterResource(R.drawable.ic_image_24),
                    modifier = Modifier.testTag("maple.media.empty"),
                )
                return@Column
            }
            val limitText = when (state.limit) {
                DocumentTreeLimit.DEPTH -> stringResource(R.string.gallery_folder_limit_depth, DocumentTreeWalker.DEFAULT_MAX_DEPTH)
                DocumentTreeLimit.ITEMS -> stringResource(R.string.gallery_folder_limit_items, DocumentTreeWalker.DEFAULT_MAX_IMAGES)
                DocumentTreeLimit.NONE -> null
            }
            if (limitText != null) {
                Text(
                    text = limitText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = MapleSettingsDefaults.horizontalPadding, vertical = 8.dp).testTag("maple.media.limit"),
                )
            }
            Box(modifier = Modifier.fillMaxSize()) {
                val gridState = rememberLazyGridState()
                LaunchedEffect(gridState, state.items.size, state.endReached) {
                    snapshotFlow { gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1 }
                        .distinctUntilChanged()
                        .filter { last -> !state.endReached && last >= state.items.size - LOAD_MORE_THRESHOLD }
                        .collect { onLoadMore() }
                }
                LaunchedEffect(state.reveal, state.items.size) {
                    val target = state.reveal ?: return@LaunchedEffect
                    val index = state.items.indexOfFirst { it.id == target }
                    if (index < 0) return@LaunchedEffect
                    val visible = gridState.layoutInfo.visibleItemsInfo
                    val shown = visible.isNotEmpty() && index >= visible.first().index && index <= visible.last().index
                    if (!shown) gridState.scrollToItem(index)
                    onRevealed()
                }
                LazyVerticalGrid(
                    columns = GridCells.Fixed(MapleGridDefaults.MEDIA_COLUMNS),
                    state = gridState,
                    horizontalArrangement = Arrangement.spacedBy(MapleGridDefaults.mediaGap),
                    verticalArrangement = Arrangement.spacedBy(MapleGridDefaults.mediaGap),
                    modifier = Modifier.fillMaxSize().testTag("maple.media.grid"),
                ) {
                    items(state.items, key = { it.id }) { item ->
                        MapleMediaCell(
                            model = Uri.parse(item.contentUri),
                            description = item.displayName,
                            badge = mediaTypeBadge(item.mimeType),
                            onClick = { onOpen(item) },
                            modifier = Modifier.testTag("maple.media.item.${item.id}"),
                        )
                    }
                }
                if (state.loading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter).testTag("maple.media.progress"))
                }
            }
        }
    }
}

private const val LOAD_MORE_THRESHOLD = 24
