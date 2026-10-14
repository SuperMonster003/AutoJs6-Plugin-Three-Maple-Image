package io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import io.github.supermonster003.autojs6.plugin.three.maple.image.AlbumKind
import io.github.supermonster003.autojs6.plugin.three.maple.image.AlbumSummary
import io.github.supermonster003.autojs6.plugin.three.maple.image.GalleryPreferences
import io.github.supermonster003.autojs6.plugin.three.maple.image.GalleryUiState
import io.github.supermonster003.autojs6.plugin.three.maple.image.MediaItem
import io.github.supermonster003.autojs6.plugin.three.maple.image.SearchUiState
import io.github.supermonster003.autojs6.plugin.three.maple.image.MediaAccessState
import io.github.supermonster003.autojs6.plugin.three.maple.image.MediaPermissionAction
import io.github.supermonster003.autojs6.plugin.three.maple.image.MediaSort
import io.github.supermonster003.autojs6.plugin.three.maple.image.R

internal class AlbumGridCallbacks(
    val onOpenAlbum: (AlbumSummary) -> Unit,
    val onAlbumSort: (MediaSort) -> Unit,
    val onGrant: () -> Unit,
    val onManageSelected: () -> Unit,
    val onChooseFolder: () -> Unit,
    val onChooseImage: () -> Unit,
    val onEditImage: () -> Unit,
    val onConvertImage: () -> Unit,
    val onSaveOutput: () -> Unit,
    val onOpenHost: () -> Unit,
    val onSettings: () -> Unit,
    val onRetry: () -> Unit,
    val onRefresh: () -> Unit,
    val onShowAll: () -> Unit,
    val onRecycleBin: () -> Unit = {},
    /** Long press on a media library album: starts (or extends) the album selection (ROADMAP 7.4). */
    val onAlbumLongPress: (AlbumSummary) -> Unit = {},
    val onClearAlbumSelection: () -> Unit = {},
    val onSelectAllAlbums: () -> Unit = {},
    /** Home search (ROADMAP 7.5). */
    val onSearch: () -> Unit = {},
    val onSearchQuery: (String) -> Unit = {},
    val onEndSearch: () -> Unit = {},
    val onOpenSearchResult: (MediaItem) -> Unit = {},
)

/** The album selection bar (ROADMAP 7.4); null hides an action. [allPinned] turns "pin" into "unpin". */
internal class AlbumSelectionActions(
    val onPin: (() -> Unit)? = null,
    val onExclude: (() -> Unit)? = null,
    val onHide: (() -> Unit)? = null,
    val onDelete: (() -> Unit)? = null,
    val onRename: (() -> Unit)? = null,
    val allPinned: Boolean = false,
)

/**
 * Standalone home for 3.0 (design draft 8.1): album grid with the authorized folders section, a permission
 * banner for partial access, and the no-permission state with the file picker tools. The former home actions
 * (view / edit / convert / save / host / settings) live in the "more" menu.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AlbumGridScreen(
    state: GalleryUiState,
    home: HomeUiState,
    callbacks: AlbumGridCallbacks,
    albumActions: AlbumSelectionActions = AlbumSelectionActions(),
    snackbarHostState: SnackbarHostState? = null,
    search: SearchUiState = SearchUiState(),
    mediaColumns: Int = GalleryPreferences.DEFAULT_COLUMNS,
) {
    if (search.active) {
        BackHandler(onBack = callbacks.onEndSearch)
        MapleScreen(
            title = "",
            onBack = callbacks.onEndSearch,
            titleContent = { MapleSearchField(search.query, callbacks.onSearchQuery, stringResource(R.string.search_hint_global)) },
            actions = {
                if (search.query.isNotEmpty()) {
                    IconButton(onClick = { callbacks.onSearchQuery("") }, modifier = Modifier.testTag("maple.search.clear")) {
                        Icon(painterResource(R.drawable.ic_close_24), contentDescription = stringResource(R.string.search_clear))
                    }
                }
            },
            snackbarHostState = snackbarHostState,
        ) { padding ->
            SearchResults(
                search = search,
                columns = mediaColumns,
                onOpenAlbum = callbacks.onOpenAlbum,
                onOpenImage = callbacks.onOpenSearchResult,
                modifier = Modifier.padding(padding),
            )
        }
        return
    }
    var menuOpen by remember { mutableStateOf(false) }
    var sortOpen by remember { mutableStateOf(false) }
    val selecting = state.selectingAlbums
    BackHandler(enabled = selecting, onBack = callbacks.onClearAlbumSelection)
    MapleScreen(
        title = if (selecting) pluralStringResource(R.plurals.gallery_selected_count, state.albumSelection.size, state.albumSelection.size) else stringResource(R.string.app_name),
        onBack = if (selecting) callbacks.onClearAlbumSelection else null,
        navigationIcon = if (selecting) painterResource(R.drawable.ic_close_24) else null,
        navigationContentDescription = if (selecting) stringResource(R.string.selection_exit) else null,
        bottomBar = { if (selecting) AlbumSelectionBar(state.albumSelection.size, albumActions) },
        snackbarHostState = snackbarHostState,
        actions = {
            if (selecting) {
                IconButton(onClick = callbacks.onSelectAllAlbums, modifier = Modifier.testTag("maple.albums.selectAll")) {
                    Icon(painterResource(R.drawable.ic_select_all_24), contentDescription = stringResource(R.string.selection_select_all))
                }
                return@MapleScreen
            }
            if (state.canReadMediaStore) {
                IconButton(onClick = callbacks.onSearch, modifier = Modifier.testTag("maple.gallery.search")) {
                    Icon(painterResource(R.drawable.ic_search_24), contentDescription = stringResource(R.string.action_search))
                }
            }
            IconButton(onClick = { sortOpen = true }, modifier = Modifier.testTag("maple.gallery.sort")) {
                Icon(painterResource(R.drawable.ic_swap_vert_24), contentDescription = stringResource(R.string.gallery_sort))
            }
            if (sortOpen) {
                SortDialog(
                    fields = albumSortFields,
                    sort = state.albumSort,
                    grouping = null,
                    thisAlbumOnly = null,
                    onDismiss = { sortOpen = false },
                    onConfirm = { sort, _, _ ->
                        sortOpen = false
                        callbacks.onAlbumSort(sort)
                    },
                )
            }
            IconButton(onClick = { menuOpen = true }, modifier = Modifier.testTag("maple.gallery.more")) {
                Icon(painterResource(R.drawable.ic_more_vert_24), contentDescription = stringResource(R.string.action_more))
            }
            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                if (state.canReadMediaStore) {
                    DropdownMenuItem(text = { Text(stringResource(R.string.gallery_show_all)) }, onClick = { menuOpen = false; callbacks.onShowAll() }, modifier = Modifier.testTag("maple.gallery.menu.showAll"))
                    DropdownMenuItem(text = { Text(stringResource(R.string.recycle_bin_title)) }, onClick = { menuOpen = false; callbacks.onRecycleBin() }, modifier = Modifier.testTag("maple.gallery.menu.recycleBin"))
                }
                DropdownMenuItem(text = { Text(stringResource(R.string.action_view_image)) }, enabled = !home.working, onClick = { menuOpen = false; callbacks.onChooseImage() }, modifier = Modifier.testTag("maple.gallery.menu.view"))
                DropdownMenuItem(text = { Text(stringResource(R.string.action_edit_image)) }, enabled = !home.working, onClick = { menuOpen = false; callbacks.onEditImage() }, modifier = Modifier.testTag("maple.gallery.menu.edit"))
                DropdownMenuItem(text = { Text(stringResource(R.string.action_convert_image)) }, enabled = !home.working, onClick = { menuOpen = false; callbacks.onConvertImage() }, modifier = Modifier.testTag("maple.gallery.menu.convert"))
                if (home.pendingOutputName != null) {
                    DropdownMenuItem(text = { Text(stringResource(R.string.standalone_home_save_output)) }, enabled = !home.working, onClick = { menuOpen = false; callbacks.onSaveOutput() }, modifier = Modifier.testTag("maple.gallery.menu.saveOutput"))
                }
                DropdownMenuItem(text = { Text(stringResource(R.string.standalone_home_open_host)) }, onClick = { menuOpen = false; callbacks.onOpenHost() }, modifier = Modifier.testTag("maple.gallery.menu.openHost"))
                DropdownMenuItem(text = { Text(stringResource(R.string.standalone_settings_title)) }, onClick = { menuOpen = false; callbacks.onSettings() }, modifier = Modifier.testTag("maple.gallery.menu.settings"))
            }
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (home.statusText.isNotEmpty()) {
                Text(
                    text = home.statusText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .padding(horizontal = MapleSettingsDefaults.horizontalPadding, vertical = 8.dp)
                        .semantics { liveRegion = LiveRegionMode.Polite }
                        .testTag("maple.home.status"),
                )
            }
            val access = state.access
            when {
                state.loading && access == null -> MapleLoading()
                access != null && !state.canReadMediaStore -> NoPermissionContent(state, home, callbacks)
                else -> {
                    if (access?.state == MediaAccessState.PARTIAL) {
                        MaplePermissionBanner(
                            text = stringResource(R.string.gallery_banner_partial),
                            actionLabel = stringResource(R.string.storage_manage_selected),
                            onAction = callbacks.onManageSelected,
                            modifier = Modifier.testTag("maple.gallery.banner"),
                        )
                    }
                    if (state.loading) {
                        MapleLoading()
                    } else if (state.albums.isEmpty() && state.folders.isEmpty()) {
                        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                            MapleEmptyState(
                                title = stringResource(R.string.gallery_empty_no_images_title),
                                body = stringResource(R.string.gallery_empty_no_images_body),
                                icon = painterResource(R.drawable.ic_image_24),
                                primaryAction = MapleEmptyAction(stringResource(R.string.gallery_retry), callbacks.onRetry, "maple.gallery.retry"),
                                secondaryActions = listOf(
                                    MapleEmptyAction(stringResource(R.string.gallery_choose_folder), callbacks.onChooseFolder, "maple.gallery.chooseFolder"),
                                    MapleEmptyAction(stringResource(R.string.gallery_choose_image), callbacks.onChooseImage, "maple.gallery.chooseImage"),
                                ),
                                modifier = Modifier.fillMaxWidth().testTag("maple.gallery.empty"),
                            )
                        }
                    } else {
                        PullToRefreshBox(
                            isRefreshing = state.refreshing,
                            onRefresh = callbacks.onRefresh,
                            modifier = Modifier.fillMaxSize().testTag("maple.gallery.refresh"),
                        ) {
                            AlbumGrid(state, callbacks, showTools = false, home = home)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NoPermissionContent(state: GalleryUiState, home: HomeUiState, callbacks: AlbumGridCallbacks) {
    val access = state.access ?: return
    val primaryLabel = if (access.nextAction == MediaPermissionAction.OPEN_APP_SETTINGS) R.string.storage_open_app_settings else R.string.storage_grant_photo_access
    // The explanation scrolls with the folders and tool rows: on a small phone in landscape it is taller than the
    // window, and the tools below it must stay reachable.
    AlbumGrid(state, callbacks, showTools = true, home = home) {
        MapleEmptyState(
            title = stringResource(R.string.gallery_empty_no_permission_title),
            body = stringResource(R.string.gallery_empty_no_permission_body),
            icon = painterResource(R.drawable.ic_image_24),
            primaryAction = MapleEmptyAction(stringResource(primaryLabel), callbacks.onGrant, "maple.gallery.grant"),
            secondaryActions = listOf(
                MapleEmptyAction(stringResource(R.string.gallery_choose_folder), callbacks.onChooseFolder, "maple.gallery.chooseFolder"),
                MapleEmptyAction(stringResource(R.string.gallery_choose_image), callbacks.onChooseImage, "maple.gallery.chooseImage"),
            ),
            modifier = Modifier.fillMaxWidth().testTag("maple.gallery.noPermission"),
        )
    }
}

/**
 * Albums, authorized folders and (without photo access) the tool rows, in one lazy grid with full-span headers;
 * [header] (the no-permission explanation) is the first full-span row so everything scrolls together.
 */
@Composable
private fun AlbumGrid(
    state: GalleryUiState,
    callbacks: AlbumGridCallbacks,
    showTools: Boolean,
    home: HomeUiState,
    header: (@Composable () -> Unit)? = null,
) {
    val columns = MapleGridDefaults.albumColumns(LocalConfiguration.current.screenWidthDp)
    val horizontal = MapleSettingsDefaults.horizontalPadding
    // Full-span rows (headers, tool rows) carry their own 24 dp inset, so the grid itself has no horizontal
    // content padding and the album cells pad their outer edges by column instead.
    fun cellPadding(index: Int): PaddingValues = PaddingValues(
        start = if (index % columns == 0) horizontal else 0.dp,
        end = if (index % columns == columns - 1) horizontal else 0.dp,
    )
    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        contentPadding = PaddingValues(bottom = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(MapleGridDefaults.albumColumnSpacing),
        verticalArrangement = Arrangement.spacedBy(MapleGridDefaults.albumRowSpacing),
        modifier = Modifier.fillMaxSize().testTag("maple.gallery.grid"),
    ) {
        if (header != null) {
            item(key = "header.explanation", span = { GridItemSpan(maxLineSpan) }) { header() }
        }
        // The favorites album leads the albums while it has images (ROADMAP 7.0).
        val albums = listOfNotNull(state.favorites) + state.albums
        if (albums.isNotEmpty()) {
            item(key = "header.albums", span = { GridItemSpan(maxLineSpan) }) {
                MapleSectionHeader(stringResource(R.string.gallery_section_albums))
            }
            itemsIndexed(albums, key = { _, album -> "${album.kind.name.lowercase()}.${album.id}" }) { index, album ->
                AlbumCell(album, state, callbacks, Modifier.padding(cellPadding(index)))
            }
        }
        if (state.folders.isNotEmpty()) {
            item(key = "header.folders", span = { GridItemSpan(maxLineSpan) }) {
                MapleSectionHeader(stringResource(R.string.storage_section_folders))
            }
            itemsIndexed(state.folders, key = { _, album -> "folder.${album.id}" }) { index, album ->
                AlbumCell(album, state, callbacks, Modifier.padding(cellPadding(index)))
            }
        }
        if (state.hidden.isNotEmpty()) {
            item(key = "header.hidden", span = { GridItemSpan(maxLineSpan) }) {
                MapleSectionHeader(stringResource(R.string.hidden_folders_title))
            }
            itemsIndexed(state.hidden, key = { _, album -> "hidden.${album.id}" }) { index, album ->
                AlbumCell(album, state, callbacks, Modifier.padding(cellPadding(index)))
            }
        }
        if (showTools) {
            item(key = "header.tools", span = { GridItemSpan(maxLineSpan) }) {
                MapleSectionHeader(stringResource(R.string.gallery_section_tools))
            }
            item(key = "tool.edit", span = { GridItemSpan(maxLineSpan) }) {
                MapleSettingsRow(
                    title = stringResource(R.string.action_edit_image),
                    summary = stringResource(R.string.standalone_home_edit_summary),
                    iconResource = R.drawable.ic_edit_24,
                    enabled = !home.working,
                    onClick = callbacks.onEditImage,
                    modifier = Modifier.testTag("maple.gallery.tool.edit"),
                )
            }
            item(key = "tool.convert", span = { GridItemSpan(maxLineSpan) }) {
                MapleSettingsRow(
                    title = stringResource(R.string.action_convert_image),
                    summary = stringResource(R.string.standalone_home_convert_summary),
                    iconResource = R.drawable.ic_swap_horiz_24,
                    enabled = !home.working,
                    onClick = callbacks.onConvertImage,
                    modifier = Modifier.testTag("maple.gallery.tool.convert"),
                )
            }
            if (home.pendingOutputName != null) {
                item(key = "tool.save", span = { GridItemSpan(maxLineSpan) }) {
                    MapleSettingsRow(
                        title = stringResource(R.string.standalone_home_save_output),
                        summary = stringResource(R.string.standalone_home_pending_summary, home.pendingOutputName),
                        iconResource = R.drawable.ic_download_24,
                        enabled = !home.working,
                        onClick = callbacks.onSaveOutput,
                        modifier = Modifier.testTag("maple.gallery.tool.saveOutput"),
                    )
                }
            }
        }
        item(key = "footer", span = { GridItemSpan(maxLineSpan) }) { Spacer(Modifier.height(8.dp)) }
    }
}

@Composable
private fun AlbumCell(album: AlbumSummary, state: GalleryUiState, callbacks: AlbumGridCallbacks, modifier: Modifier = Modifier) {
    val count = if (album.count >= 0) pluralStringResource(R.plurals.gallery_image_count, album.count, album.count) else stringResource(R.string.gallery_loading_folder)
    // Only media library albums take part in the album selection; the others ignore it (ROADMAP 7.4).
    val selectable = album.kind == AlbumKind.MEDIA
    val selecting = state.selectingAlbums
    MapleAlbumCell(
        name = album.name,
        count = if (album.available) count else stringResource(R.string.storage_folder_unavailable),
        coverModel = album.coverUri?.let(Uri::parse),
        badge = when (album.kind) {
            AlbumKind.FOLDER -> painterResource(R.drawable.ic_folder_24)
            AlbumKind.FAVORITES -> painterResource(R.drawable.ic_favorite_24)
            AlbumKind.HIDDEN -> painterResource(R.drawable.ic_visibility_off_24)
            else -> if (album.pinned) painterResource(R.drawable.ic_push_pin_24) else null
        },
        enabled = album.available && (!selecting || selectable),
        selectionMode = selecting && selectable,
        selected = album.id in state.albumSelection && selectable,
        onLongClick = if (selectable) ({ callbacks.onAlbumLongPress(album) }) else null,
        onLongClickLabel = if (selectable) stringResource(R.string.selection_start) else null,
        onClick = { if (selecting) callbacks.onAlbumLongPress(album) else callbacks.onOpenAlbum(album) },
        modifier = modifier.testTag("maple.gallery.album.${album.kind.name.lowercase()}.${album.id}"),
    )
}

/** Bottom bar of the album selection: pin (or unpin), exclude, hide, delete and rename (one album). */
@Composable
private fun AlbumSelectionBar(selectionCount: Int, actions: AlbumSelectionActions) {
    val pinLabel = stringResource(if (actions.allPinned) R.string.action_unpin else R.string.action_pin)
    val all = buildList {
        actions.onPin?.let { add(MapleAction(painterResource(R.drawable.ic_push_pin_24), pinLabel, it, testTag = "maple.albums.pin")) }
        actions.onExclude?.let { add(MapleAction(painterResource(R.drawable.ic_block_24), stringResource(R.string.action_exclude), it, testTag = "maple.albums.exclude")) }
        actions.onHide?.let { add(MapleAction(painterResource(R.drawable.ic_visibility_off_24), stringResource(R.string.action_hide), it, testTag = "maple.albums.hide")) }
        actions.onDelete?.let { add(MapleAction(painterResource(R.drawable.ic_delete_24), stringResource(R.string.action_delete), it, testTag = "maple.albums.delete")) }
        actions.onRename?.let {
            add(MapleAction(painterResource(R.drawable.ic_edit_24), stringResource(R.string.action_rename), it, enabled = selectionCount == 1, testTag = "maple.albums.rename"))
        }
    }
    if (all.isNotEmpty()) MapleBottomActionBar(actions = all, navigationBarInset = true)
}

