package io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose

import android.net.Uri
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
import io.github.supermonster003.autojs6.plugin.three.maple.image.GalleryUiState
import io.github.supermonster003.autojs6.plugin.three.maple.image.MediaAccessState
import io.github.supermonster003.autojs6.plugin.three.maple.image.MediaPermissionAction
import io.github.supermonster003.autojs6.plugin.three.maple.image.MediaSort
import io.github.supermonster003.autojs6.plugin.three.maple.image.MediaSortField
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
)

/**
 * Standalone home for 3.0 (design draft 8.1): album grid with the authorized folders section, a permission
 * banner for partial access, and the no-permission state with the file picker tools. The former home actions
 * (view / edit / convert / save / host / settings) live in the "more" menu.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AlbumGridScreen(state: GalleryUiState, home: HomeUiState, callbacks: AlbumGridCallbacks) {
    var menuOpen by remember { mutableStateOf(false) }
    var sortOpen by remember { mutableStateOf(false) }
    MapleScreen(
        title = stringResource(R.string.app_name),
        actions = {
            IconButton(onClick = { sortOpen = true }, modifier = Modifier.testTag("maple.gallery.sort")) {
                Icon(painterResource(R.drawable.ic_swap_vert_24), contentDescription = stringResource(R.string.gallery_sort))
            }
            SortMenu(expanded = sortOpen, sort = state.albumSort, dateFields = false, onDismiss = { sortOpen = false }, onSelect = callbacks.onAlbumSort)
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
        if (state.albums.isNotEmpty()) {
            item(key = "header.albums", span = { GridItemSpan(maxLineSpan) }) {
                MapleSectionHeader(stringResource(R.string.gallery_section_albums))
            }
            itemsIndexed(state.albums, key = { _, album -> "media.${album.id}" }) { index, album ->
                AlbumCell(album, callbacks, Modifier.padding(cellPadding(index)))
            }
        }
        if (state.folders.isNotEmpty()) {
            item(key = "header.folders", span = { GridItemSpan(maxLineSpan) }) {
                MapleSectionHeader(stringResource(R.string.storage_section_folders))
            }
            itemsIndexed(state.folders, key = { _, album -> "folder.${album.id}" }) { index, album ->
                AlbumCell(album, callbacks, Modifier.padding(cellPadding(index)))
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
private fun AlbumCell(album: AlbumSummary, callbacks: AlbumGridCallbacks, modifier: Modifier = Modifier) {
    val count = if (album.count >= 0) pluralStringResource(R.plurals.gallery_image_count, album.count, album.count) else stringResource(R.string.gallery_loading_folder)
    MapleAlbumCell(
        name = album.name,
        count = if (album.available) count else stringResource(R.string.storage_folder_unavailable),
        coverModel = album.coverUri?.let(Uri::parse),
        badge = if (album.kind == AlbumKind.FOLDER) painterResource(R.drawable.ic_folder_24) else null,
        enabled = album.available,
        onClick = { callbacks.onOpenAlbum(album) },
        modifier = modifier.testTag("maple.gallery.album.${album.kind.name.lowercase()}.${album.id}"),
    )
}

/** Sort menu shared by the album list and the media grid: fields with a check mark, then the direction. */
@Composable
internal fun SortMenu(expanded: Boolean, sort: MediaSort, dateFields: Boolean, onDismiss: () -> Unit, onSelect: (MediaSort) -> Unit) {
    DropdownMenu(expanded = expanded, onDismissRequest = onDismiss) {
        val fields = buildList {
            add(MediaSortField.NAME to R.string.gallery_sort_name)
            if (dateFields) {
                add(MediaSortField.DATE_MODIFIED to R.string.gallery_sort_date_modified)
                add(MediaSortField.DATE_TAKEN to R.string.gallery_sort_date_taken)
            }
            add(MediaSortField.SIZE to R.string.gallery_sort_size)
        }
        for ((field, label) in fields) {
            DropdownMenuItem(
                text = { Text(stringResource(label)) },
                trailingIcon = if (sort.field == field) ({ Icon(painterResource(R.drawable.ic_check_24), contentDescription = null) }) else null,
                onClick = { onDismiss(); onSelect(sort.copy(field = field)) },
                modifier = Modifier.testTag("maple.sort.${field.name.lowercase()}"),
            )
        }
        DropdownMenuItem(
            text = { Text(stringResource(if (sort.ascending) R.string.gallery_sort_descending else R.string.gallery_sort_ascending)) },
            onClick = { onDismiss(); onSelect(sort.copy(ascending = !sort.ascending)) },
            modifier = Modifier.testTag("maple.sort.direction"),
        )
    }
}

