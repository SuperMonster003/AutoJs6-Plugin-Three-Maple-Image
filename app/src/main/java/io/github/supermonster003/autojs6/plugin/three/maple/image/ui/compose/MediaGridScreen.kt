package io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.supermonster003.autojs6.plugin.three.maple.image.DocumentTreeLimit
import io.github.supermonster003.autojs6.plugin.three.maple.image.DocumentTreeWalker
import io.github.supermonster003.autojs6.plugin.three.maple.image.GalleryPreferences
import io.github.supermonster003.autojs6.plugin.three.maple.image.MediaGridUiState
import io.github.supermonster003.autojs6.plugin.three.maple.image.MediaGroup
import io.github.supermonster003.autojs6.plugin.three.maple.image.MediaGroupLayout
import io.github.supermonster003.autojs6.plugin.three.maple.image.MediaGrouping
import io.github.supermonster003.autojs6.plugin.three.maple.image.MediaItem
import io.github.supermonster003.autojs6.plugin.three.maple.image.MediaSort
import io.github.supermonster003.autojs6.plugin.three.maple.image.R
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter

/** Grid callbacks of one album; the selection callbacks take MediaStore / folder item ids. */
internal class MediaGridCallbacks(
    val onBack: () -> Unit,
    val onOpen: (MediaItem) -> Unit,
    val onLoadMore: () -> Unit,
    /** The sort dialog's choice: order, date grouping and "this album only" (ROADMAP 7.5). */
    val onSort: (MediaSort, MediaGrouping, Boolean) -> Unit,
    val onRevealed: () -> Unit,
    val onColumns: (Int) -> Unit,
    val onRefresh: () -> Unit,
    val onToggle: (Long) -> Unit = {},
    val onDragStart: (Long) -> Unit = {},
    val onDragTo: (Long) -> Unit = {},
    val onDragEnd: () -> Unit = {},
    val onClearSelection: () -> Unit = {},
    val onSelectAll: () -> Unit = {},
    val onInvertSelection: () -> Unit = {},
    /** A hidden album (ROADMAP 7.4): removes its `.nomedia` file; null for every other album. */
    val onUnhide: (() -> Unit)? = null,
    /** Search inside the album by file name (ROADMAP 7.5); an empty query shows the whole album again. */
    val onQuery: (String) -> Unit = {},
)

/**
 * The operations the selection bar offers for the open album (ROADMAP 7.1 / 7.0); a null action is not shown
 * (authorized folders only delete and rename). Share, favorite, move and delete sit in the bar, the rest under
 * "More".
 */
internal class MediaSelectionActions(
    val onShare: (() -> Unit)? = null,
    val onFavorite: (() -> Unit)? = null,
    val onMove: (() -> Unit)? = null,
    val onDelete: (() -> Unit)? = null,
    val onCopy: (() -> Unit)? = null,
    val onRename: (() -> Unit)? = null,
    val onDetails: (() -> Unit)? = null,
    /** Every selected image is a favorite already: the favorite action removes them instead. */
    val favorited: Boolean = false,
)

/**
 * Media grid of one album (design draft 8.2): square cells with type badges, provider paging, sort menu, a column
 * count (3..6) from the menu or a two-finger pinch, and pull to refresh. A long press starts selecting (ROADMAP
 * 7.1): taps toggle, dragging after the long press extends a range and scrolls at the edges, back leaves.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MediaGridScreen(
    state: MediaGridUiState,
    callbacks: MediaGridCallbacks,
    selectionActions: MediaSelectionActions = MediaSelectionActions(),
    snackbarHostState: SnackbarHostState? = null,
) {
    var sortOpen by remember { mutableStateOf(false) }
    var menuOpen by remember { mutableStateOf(false) }
    val album = state.album
    val selecting = state.selecting
    var searchOpen by rememberSaveable { mutableStateOf(state.query.isNotEmpty()) }
    val closeSearch = {
        searchOpen = false
        callbacks.onQuery("")
    }
    BackHandler(enabled = selecting, onBack = callbacks.onClearSelection)
    BackHandler(enabled = searchOpen && !selecting, onBack = closeSearch)
    val title = if (selecting) {
        pluralStringResource(R.plurals.gallery_selected_count, state.selection.size, state.selection.size)
    } else {
        album?.name.orEmpty()
    }
    val countText = if (selecting) {
        null
    } else {
        album?.let {
            val count = if (it.count >= 0) it.count else state.items.size
            pluralStringResource(R.plurals.gallery_image_count, count, count)
        }
    }
    MapleScreen(
        title = title,
        subtitle = countText,
        titleContent = if (searchOpen && !selecting) ({ MapleSearchField(state.query, callbacks.onQuery, stringResource(R.string.search_hint_album)) }) else null,
        onBack = when {
            selecting -> callbacks.onClearSelection
            searchOpen -> closeSearch
            else -> callbacks.onBack
        },
        navigationIcon = if (selecting) painterResource(R.drawable.ic_close_24) else null,
        navigationContentDescription = if (selecting) stringResource(R.string.selection_exit) else null,
        actions = {
            if (selecting) {
                IconButton(onClick = callbacks.onSelectAll, modifier = Modifier.testTag("maple.selection.selectAll")) {
                    Icon(painterResource(R.drawable.ic_select_all_24), contentDescription = stringResource(R.string.selection_select_all))
                }
                IconButton(onClick = { menuOpen = true }, modifier = Modifier.testTag("maple.selection.more")) {
                    Icon(painterResource(R.drawable.ic_more_vert_24), contentDescription = stringResource(R.string.action_more))
                }
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.selection_invert)) },
                        onClick = { menuOpen = false; callbacks.onInvertSelection() },
                        modifier = Modifier.testTag("maple.selection.invert"),
                    )
                }
            } else if (searchOpen) {
                if (state.query.isNotEmpty()) {
                    IconButton(onClick = { callbacks.onQuery("") }, modifier = Modifier.testTag("maple.search.clear")) {
                        Icon(painterResource(R.drawable.ic_close_24), contentDescription = stringResource(R.string.search_clear))
                    }
                }
            } else {
                IconButton(onClick = { searchOpen = true }, modifier = Modifier.testTag("maple.media.search")) {
                    Icon(painterResource(R.drawable.ic_search_24), contentDescription = stringResource(R.string.action_search))
                }
                IconButton(onClick = { sortOpen = true }, modifier = Modifier.testTag("maple.media.sort")) {
                    Icon(painterResource(R.drawable.ic_swap_vert_24), contentDescription = stringResource(R.string.gallery_sort))
                }
                if (sortOpen) {
                    SortDialog(
                        fields = mediaSortFields,
                        sort = state.sort,
                        grouping = state.grouping,
                        thisAlbumOnly = state.ownSort,
                        onDismiss = { sortOpen = false },
                        onConfirm = { sort, grouping, thisAlbumOnly ->
                            sortOpen = false
                            callbacks.onSort(sort, grouping, thisAlbumOnly)
                        },
                    )
                }
                IconButton(onClick = { menuOpen = true }, modifier = Modifier.testTag("maple.media.more")) {
                    Icon(painterResource(R.drawable.ic_more_vert_24), contentDescription = stringResource(R.string.action_more))
                }
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                    callbacks.onUnhide?.let { unhide ->
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.action_unhide)) },
                            onClick = { menuOpen = false; unhide() },
                            modifier = Modifier.testTag("maple.media.unhide"),
                        )
                    }
                    for (columns in GalleryPreferences.MIN_COLUMNS..GalleryPreferences.MAX_COLUMNS) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.gallery_columns_value, columns)) },
                            trailingIcon = if (state.columns == columns) ({ Icon(painterResource(R.drawable.ic_check_24), contentDescription = null) }) else null,
                            onClick = { menuOpen = false; callbacks.onColumns(columns) },
                            modifier = Modifier.testTag("maple.media.columns.$columns"),
                        )
                    }
                }
            }
        },
        bottomBar = { if (selecting) SelectionBar(selectionCount = state.selection.size, actions = selectionActions) },
        snackbarHostState = snackbarHostState,
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (state.loading && state.items.isEmpty()) {
                MapleLoading()
                return@Column
            }
            if (state.items.isEmpty()) {
                MapleEmptyState(
                    title = stringResource(if (state.query.isNotBlank()) R.string.search_no_results else R.string.gallery_empty_album_title),
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
            PullToRefreshBox(
                isRefreshing = state.refreshing,
                onRefresh = callbacks.onRefresh,
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(state.columns) {
                        detectColumnPinch { zoomIn -> callbacks.onColumns(if (zoomIn) state.columns - 1 else state.columns + 1) }
                    }
                    .testTag("maple.media.refresh"),
            ) {
                val gridState = rememberLazyGridState()
                val layout = remember(state.groups, state.items.size) { MediaGroupLayout(state.groups, state.items.size) }
                LaunchedEffect(gridState, state.items.size, state.endReached) {
                    snapshotFlow { gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1 }
                        .distinctUntilChanged()
                        .filter { last -> !state.endReached && last >= state.items.size - LOAD_MORE_THRESHOLD }
                        .collect { callbacks.onLoadMore() }
                }
                LaunchedEffect(state.reveal, state.items.size) {
                    val target = state.reveal ?: return@LaunchedEffect
                    val itemIndex = state.items.indexOfFirst { it.id == target }
                    if (itemIndex < 0) return@LaunchedEffect
                    val index = layout.gridIndex(itemIndex)
                    val visible = gridState.layoutInfo.visibleItemsInfo
                    val shown = visible.isNotEmpty() && index >= visible.first().index && index <= visible.last().index
                    if (!shown) gridState.scrollToItem(index)
                    callbacks.onRevealed()
                }
                val haptics = LocalHapticFeedback.current
                val density = LocalDensity.current
                val drag = remember { DragPointer() }
                var autoScrollSpeed by remember { mutableFloatStateOf(0f) }
                val autoScrolling by remember { derivedStateOf { autoScrollSpeed != 0f } }
                LaunchedEffect(autoScrolling) {
                    while (autoScrolling) {
                        gridState.scrollBy(autoScrollSpeed)
                        (gridState.keyAt(drag.position) as? Long)?.let(callbacks.onDragTo)
                        withFrameNanos { }
                    }
                }
                val startLabel = stringResource(R.string.selection_start)
                LazyVerticalGrid(
                    columns = GridCells.Fixed(state.columns),
                    state = gridState,
                    horizontalArrangement = Arrangement.spacedBy(MapleGridDefaults.mediaGap),
                    verticalArrangement = Arrangement.spacedBy(MapleGridDefaults.mediaGap),
                    modifier = Modifier
                        .fillMaxSize()
                        .longPressDragSelection(
                            gridState = gridState,
                            edgePx = with(density) { AUTO_SCROLL_EDGE.toPx() },
                            maxSpeedPx = with(density) { AUTO_SCROLL_MAX_SPEED.toPx() },
                            onStart = { key ->
                                (key as? Long)?.let {
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                    callbacks.onDragStart(it)
                                }
                            },
                            onDrag = { key -> (key as? Long)?.let(callbacks.onDragTo) },
                            onPointer = { drag.position = it },
                            onAutoScroll = { autoScrollSpeed = it },
                            onEnd = callbacks.onDragEnd,
                        )
                        .testTag("maple.media.grid"),
                ) {
                    val cell: @Composable (MediaItem) -> Unit = { item ->
                        // A remembered Uri keeps the cell skippable when a page is appended (a fresh Uri instance
                        // would recompose every visible cell).
                        val model = remember(item.contentUri) { Uri.parse(item.contentUri) }
                        MapleMediaCell(
                            model = model,
                            description = item.displayName,
                            badge = mediaTypeBadge(item.mimeType),
                            selectionMode = selecting,
                            selected = item.id in state.selection,
                            onLongClick = {
                                callbacks.onDragStart(item.id)
                                callbacks.onDragEnd()
                            },
                            onLongClickLabel = startLabel,
                            onClick = { if (selecting) callbacks.onToggle(item.id) else callbacks.onOpen(item) },
                            modifier = Modifier.testTag("maple.media.item.${item.id}"),
                        )
                    }
                    if (state.groups.isEmpty()) {
                        items(state.items, key = { it.id }, contentType = { MEDIA_CELL }) { item -> cell(item) }
                    } else {
                        // Date sections (ROADMAP 7.5): a full-width header before each group's items.
                        for (group in state.groups) {
                            item(key = groupKey(group), span = { GridItemSpan(maxLineSpan) }, contentType = GROUP_HEADER) {
                                GroupHeader(group, Modifier.testTag("maple.media.group.${groupKey(group)}"))
                            }
                            items(group.count, key = { state.items[group.start + it].id }, contentType = { MEDIA_CELL }) { offset ->
                                cell(state.items[group.start + offset])
                            }
                        }
                    }
                }
                if (state.groups.isNotEmpty()) {
                    // The section of the first visible row stays on top while scrolling (a sticky header).
                    val firstVisible by remember { derivedStateOf { gridState.firstVisibleItemIndex } }
                    layout.groupAt(firstVisible)?.let { group ->
                        GroupHeader(group, Modifier.align(Alignment.TopCenter).testTag("maple.media.stickyGroup"))
                    }
                }
                val locale = LocalConfiguration.current.locales[0]
                val dateFormat = remember(locale) { DateFormat.getDateInstance(DateFormat.MEDIUM, locale) }
                MapleFastScroller(
                    gridState = gridState,
                    itemCount = layout.gridCount,
                    label = { index ->
                        val item = layout.itemIndex(index)?.let(state.items::getOrNull) ?: layout.groupAt(index)?.let { state.items.getOrNull(it.start) }
                        item?.let { dateFormat.format(Date(it.effectiveTakenMillis)) }
                    },
                )
                if (state.loading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter).testTag("maple.media.progress"))
                }
            }
        }
    }
}

/** Last pointer position of a drag selection; read by the auto-scroll loop without recomposing the grid. */
private class DragPointer {
    var position: Offset = Offset.Unspecified
}

/** Bottom bar of selection mode: up to four direct actions plus "More" for the rest. */
@Composable
private fun SelectionBar(selectionCount: Int, actions: MediaSelectionActions) {
    var moreOpen by remember { mutableStateOf(false) }
    val share = painterResource(R.drawable.ic_share_24)
    val favorite = painterResource(if (actions.favorited) R.drawable.ic_favorite_24 else R.drawable.ic_favorite_border_24)
    val move = painterResource(R.drawable.ic_drive_file_move_24)
    val delete = painterResource(R.drawable.ic_delete_24)
    val more = painterResource(R.drawable.ic_more_vert_24)
    val shareLabel = stringResource(R.string.action_share)
    val favoriteLabel = stringResource(if (actions.favorited) R.string.action_unfavorite else R.string.action_favorite)
    val moveLabel = stringResource(R.string.action_move)
    val deleteLabel = stringResource(R.string.action_delete)
    val moreLabel = stringResource(R.string.action_more)
    val direct = buildList {
        actions.onShare?.let { add(MapleAction(share, shareLabel, it, testTag = "maple.selection.share")) }
        actions.onFavorite?.let { add(MapleAction(favorite, favoriteLabel, it, testTag = "maple.selection.favorite")) }
        actions.onMove?.let { add(MapleAction(move, moveLabel, it, testTag = "maple.selection.move")) }
        actions.onDelete?.let { add(MapleAction(delete, deleteLabel, it, testTag = "maple.selection.delete")) }
    }
    val hasMore = actions.onCopy != null || actions.onRename != null || actions.onDetails != null
    val all = if (hasMore) direct + MapleAction(more, moreLabel, { moreOpen = true }, testTag = "maple.selection.barMore") else direct
    if (all.isEmpty()) return
    Box(modifier = Modifier.fillMaxWidth()) {
        MapleBottomActionBar(actions = all, navigationBarInset = true)
        Box(modifier = Modifier.align(Alignment.TopEnd)) {
            DropdownMenu(expanded = moreOpen, onDismissRequest = { moreOpen = false }) {
                actions.onCopy?.let { copy ->
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.action_copy)) },
                        leadingIcon = { Icon(painterResource(R.drawable.ic_content_copy_24), contentDescription = null) },
                        onClick = { moreOpen = false; copy() },
                        modifier = Modifier.testTag("maple.selection.copy"),
                    )
                }
                actions.onRename?.let { rename ->
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.action_rename)) },
                        leadingIcon = { Icon(painterResource(R.drawable.ic_edit_24), contentDescription = null) },
                        enabled = selectionCount == 1,
                        onClick = { moreOpen = false; rename() },
                        modifier = Modifier.testTag("maple.selection.rename"),
                    )
                }
                actions.onDetails?.let { details ->
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.label_details)) },
                        leadingIcon = { Icon(painterResource(R.drawable.ic_info_outline_24), contentDescription = null) },
                        onClick = { moreOpen = false; details() },
                        modifier = Modifier.testTag("maple.selection.details"),
                    )
                }
            }
        }
    }
}

private const val LOAD_MORE_THRESHOLD = 72
private const val MEDIA_CELL = "media"
private const val GROUP_HEADER = "group"

private fun groupKey(group: MediaGroup): String = "group.${group.year}.${group.month}.${group.day}"

/** "Wednesday, October 14, 2026" / "October 2026" in the user's locale, with the section's image count. */
@Composable
private fun GroupHeader(group: MediaGroup, modifier: Modifier = Modifier) {
    val locale = LocalConfiguration.current.locales[0]
    val label = remember(group.year, group.month, group.day, locale) {
        val calendar = Calendar.getInstance().apply {
            clear()
            set(group.year, group.month - 1, if (group.day == 0) 1 else group.day)
        }
        val skeleton = if (group.day == 0) "yMMMM" else "yMMMdEEE"
        SimpleDateFormat(android.text.format.DateFormat.getBestDateTimePattern(locale, skeleton), locale).format(calendar.time)
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = MapleSettingsDefaults.horizontalPadding, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
        Text(
            pluralStringResource(R.plurals.gallery_image_count, group.count, group.count),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
private const val PINCH_IN_STEP = 1.3f
private const val PINCH_OUT_STEP = 0.77f
private val AUTO_SCROLL_EDGE = 64.dp
private val AUTO_SCROLL_MAX_SPEED = 16.dp

/**
 * Two-finger pinch over the grid, seen in the initial pass so the grid never scrolls with two fingers: every
 * 30% of spread (or 23% of squeeze) steps the column count by one; [onStep] receives true to show fewer, larger
 * cells. A single finger passes through untouched.
 */
private suspend fun PointerInputScope.detectColumnPinch(onStep: (zoomIn: Boolean) -> Unit) {
    awaitEachGesture {
        awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
        var zoom = 1f
        do {
            val event = awaitPointerEvent(PointerEventPass.Initial)
            val pressed = event.changes.count { it.pressed }
            if (pressed >= 2) {
                zoom *= event.calculateZoom()
                event.changes.forEach { change -> change.consume() }
                if (zoom >= PINCH_IN_STEP) {
                    zoom = 1f
                    onStep(true)
                } else if (zoom <= PINCH_OUT_STEP) {
                    zoom = 1f
                    onStep(false)
                }
            }
        } while (event.changes.any { it.pressed })
    }
}
