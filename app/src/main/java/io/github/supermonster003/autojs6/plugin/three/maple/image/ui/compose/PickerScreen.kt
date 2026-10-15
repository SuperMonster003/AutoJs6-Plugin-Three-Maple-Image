package io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose

import android.content.res.Configuration
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.supermonster003.autojs6.plugin.three.maple.image.AlbumKind
import io.github.supermonster003.autojs6.plugin.three.maple.image.AlbumSummary
import io.github.supermonster003.autojs6.plugin.three.maple.image.GalleryUiState
import io.github.supermonster003.autojs6.plugin.three.maple.image.GridPolicy
import io.github.supermonster003.autojs6.plugin.three.maple.image.MediaAccessState
import io.github.supermonster003.autojs6.plugin.three.maple.image.MediaPermissionAction
import io.github.supermonster003.autojs6.plugin.three.maple.image.R

/**
 * The album page of the script picker (ROADMAP 10.2): "All images", favorites and the media library albums, with
 * the photo access state of the gallery (the request, the partial access banner). Authorized folders and hidden
 * albums are left out: their images have no path a script could read.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PickerAlbumsScreen(
    title: String,
    state: GalleryUiState,
    allImages: AlbumSummary?,
    onCancel: () -> Unit,
    onOpenAlbum: (AlbumSummary) -> Unit,
    onGrant: () -> Unit,
    onManageSelected: () -> Unit,
    onRefresh: () -> Unit,
) {
    MapleScreen(
        title = title,
        onBack = onCancel,
        navigationIcon = painterResource(R.drawable.ic_close_24),
        navigationContentDescription = stringResource(R.string.dialog_button_cancel),
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            val access = state.access
            when {
                state.loading && access == null -> MapleLoading()
                access != null && !state.canReadMediaStore -> MapleEmptyState(
                    title = stringResource(R.string.gallery_empty_no_permission_title),
                    body = stringResource(R.string.pick_permission_body),
                    icon = painterResource(R.drawable.ic_image_24),
                    primaryAction = MapleEmptyAction(
                        stringResource(if (access.nextAction == MediaPermissionAction.OPEN_APP_SETTINGS) R.string.storage_open_app_settings else R.string.storage_grant_photo_access),
                        onGrant,
                        "maple.pick.grant",
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("maple.pick.noPermission"),
                )
                else -> {
                    if (access?.state == MediaAccessState.PARTIAL) {
                        MaplePermissionBanner(
                            text = stringResource(R.string.gallery_banner_partial),
                            actionLabel = stringResource(R.string.storage_manage_selected),
                            onAction = onManageSelected,
                            modifier = Modifier.testTag("maple.pick.banner"),
                        )
                    }
                    val albums = listOfNotNull(allImages, state.favorites) + state.albums.filter { it.kind == AlbumKind.MEDIA }
                    when {
                        state.loading -> MapleLoading()
                        albums.isEmpty() -> MapleEmptyState(
                            title = stringResource(R.string.gallery_empty_no_images_title),
                            icon = painterResource(R.drawable.ic_image_24),
                            modifier = Modifier.fillMaxWidth().testTag("maple.pick.empty"),
                        )
                        else -> PullToRefreshBox(
                            isRefreshing = state.refreshing,
                            onRefresh = onRefresh,
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            PickerAlbumGrid(albums, onOpenAlbum)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PickerAlbumGrid(albums: List<AlbumSummary>, onOpenAlbum: (AlbumSummary) -> Unit) {
    BoxWithConstraints {
        val configuration = LocalConfiguration.current
        val columns = GridPolicy.albumColumns(
            LocalGridSettings.current,
            landscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE,
            widthDp = maxWidth.value.toInt(),
        )
        val horizontal = MapleSettingsDefaults.horizontalPadding
        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            contentPadding = PaddingValues(start = horizontal, end = horizontal, top = 8.dp, bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(MapleGridDefaults.albumColumnSpacing),
            verticalArrangement = Arrangement.spacedBy(MapleGridDefaults.albumRowSpacing),
            modifier = Modifier.fillMaxSize().testTag("maple.pick.albums"),
        ) {
            itemsIndexed(albums, key = { _, album -> "${album.kind.name.lowercase()}.${album.id}" }) { _, album ->
                MapleAlbumCell(
                    name = album.name,
                    count = if (album.count >= 0) pluralStringResource(R.plurals.gallery_image_count, album.count, album.count) else "",
                    coverModel = album.coverUri?.let(Uri::parse),
                    badge = if (album.kind == AlbumKind.FAVORITES) painterResource(R.drawable.ic_favorite_24) else null,
                    onClick = { onOpenAlbum(album) },
                    modifier = Modifier.testTag("maple.pick.album.${album.kind.name.lowercase()}.${album.id}"),
                )
            }
        }
    }
}
