package io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.supermonster003.autojs6.plugin.three.maple.image.HostDirectoryEntry
import io.github.supermonster003.autojs6.plugin.three.maple.image.HostDirectoryPolicy
import io.github.supermonster003.autojs6.plugin.three.maple.image.R

/** State of the host directory browser (ROADMAP 6.4): one folder of the directory the user chose in AutoJs6. */
internal data class HostDirectoryUiState(
    val title: String = "",
    /** Path inside the chosen directory, empty at its root; never an absolute path. */
    val path: String = "",
    val folders: List<HostDirectoryEntry> = emptyList(),
    val images: List<HostDirectoryEntry> = emptyList(),
    val truncated: Boolean = false,
    val depthLimited: Boolean = false,
    val loading: Boolean = true,
    val failed: Boolean = false,
    val columns: Int = 4,
)

internal class HostDirectoryCallbacks(
    val onBack: () -> Unit,
    val onOpenFolder: (HostDirectoryEntry) -> Unit,
    val onOpenImage: (Int) -> Unit,
    val onRetry: () -> Unit,
    /** Thumbnail model of image [index] of the current listing (a private provider URI) or null. */
    val thumbnail: (Int) -> Any?,
)

/**
 * Folder cells first, then the images of the current folder in the media grid layout (design draft 8.2); the
 * title is the folder name and the subtitle the relative path inside the chosen directory.
 */
@Composable
internal fun HostDirectoryScreen(state: HostDirectoryUiState, callbacks: HostDirectoryCallbacks) {
    MapleScreen(
        title = state.title,
        subtitle = state.path.takeIf { it.isNotEmpty() },
        onBack = callbacks.onBack,
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                state.loading && state.folders.isEmpty() && state.images.isEmpty() -> MapleLoading()
                state.failed -> MapleEmptyState(
                    title = stringResource(R.string.error_cannot_read_image),
                    icon = painterResource(R.drawable.ic_folder_24),
                    primaryAction = MapleEmptyAction(stringResource(R.string.gallery_retry), callbacks.onRetry, "maple.hostdir.retry"),
                    modifier = Modifier.testTag("maple.hostdir.failed"),
                )
                state.folders.isEmpty() && state.images.isEmpty() -> MapleEmptyState(
                    title = stringResource(R.string.host_directory_empty),
                    icon = painterResource(R.drawable.ic_image_24),
                    modifier = Modifier.testTag("maple.hostdir.empty"),
                )
                else -> {
                    val notice = when {
                        state.truncated -> stringResource(R.string.host_directory_limit, HostDirectoryPolicy.MAX_ENTRIES)
                        state.depthLimited && state.folders.isNotEmpty() ->
                            stringResource(R.string.gallery_folder_limit_depth, HostDirectoryPolicy.MAX_DEPTH)
                        else -> null
                    }
                    if (notice != null) {
                        Text(
                            text = notice,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = MapleSettingsDefaults.horizontalPadding, vertical = 8.dp).testTag("maple.hostdir.notice"),
                        )
                    }
                    val folderLabel = stringResource(R.string.host_directory_folder)
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(state.columns),
                        horizontalArrangement = Arrangement.spacedBy(MapleGridDefaults.mediaGap),
                        verticalArrangement = Arrangement.spacedBy(MapleGridDefaults.mediaGap),
                        modifier = Modifier.fillMaxSize().testTag("maple.hostdir.grid"),
                    ) {
                        itemsIndexed(state.folders, key = { _, entry -> "d/" + entry.relativePath }) { _, entry ->
                            MapleAlbumCell(
                                name = entry.displayName,
                                count = folderLabel,
                                coverModel = null,
                                badge = painterResource(R.drawable.ic_folder_24),
                                enabled = !state.depthLimited,
                                onClick = { callbacks.onOpenFolder(entry) },
                                modifier = Modifier.padding(4.dp).testTag("maple.hostdir.folder." + entry.displayName),
                            )
                        }
                        itemsIndexed(state.images, key = { _, entry -> "f/" + entry.relativePath }) { index, entry ->
                            MapleMediaCell(
                                model = callbacks.thumbnail(index),
                                description = entry.displayName,
                                badge = mediaTypeBadge(entry.mimeType),
                                onClick = { callbacks.onOpenImage(index) },
                                modifier = Modifier.testTag("maple.hostdir.image." + entry.displayName),
                            )
                        }
                    }
                }
            }
        }
    }
}
