package io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.supermonster003.autojs6.plugin.three.maple.image.AlbumPath
import io.github.supermonster003.autojs6.plugin.three.maple.image.R

/** One hidden folder on the settings page. */
internal data class HiddenFolderRow(val path: AlbumPath, val name: String, val location: String, val count: Int)

/**
 * Settings > Hidden folders (ROADMAP 7.4): what `.nomedia` means, the "show in albums" switch and one row per hidden
 * folder with "show again". Without file system access only the explanation and the way to grant it are shown.
 */
@Composable
internal fun HiddenFoldersScreen(
    available: Boolean,
    showInGallery: Boolean,
    folders: List<HiddenFolderRow>?,
    busy: Boolean,
    onBack: () -> Unit,
    onShowInGallery: (Boolean) -> Unit,
    onOpenStorage: () -> Unit,
    onUnhide: (HiddenFolderRow) -> Unit,
) {
    MapleScreen(title = stringResource(R.string.hidden_folders_title), onBack = onBack) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())) {
            Text(
                text = stringResource(R.string.hidden_folders_summary),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = MapleSettingsDefaults.horizontalPadding, vertical = 12.dp),
            )
            if (!available) {
                MapleSettingsRow(
                    title = stringResource(R.string.hidden_folders_open_settings),
                    summary = stringResource(R.string.hidden_folders_unavailable),
                    iconResource = R.drawable.ic_folder_24,
                    onClick = onOpenStorage,
                    modifier = Modifier.testTag("maple.hidden.access"),
                )
                return@Column
            }
            MapleSwitchRow(
                title = stringResource(R.string.hidden_folders_show),
                summary = stringResource(R.string.hidden_folders_show_summary),
                iconResource = R.drawable.ic_visibility_24,
                checked = showInGallery,
                onToggle = onShowInGallery,
                modifier = Modifier.testTag("maple.hidden.show"),
            )
            MapleSectionHeader(stringResource(R.string.hidden_folders_title))
            if (folders != null && folders.isEmpty()) {
                MapleSettingsRow(
                    title = stringResource(R.string.hidden_folders_empty),
                    iconResource = R.drawable.ic_visibility_off_24,
                    showChevron = false,
                    enabled = false,
                    modifier = Modifier.testTag("maple.hidden.empty"),
                )
            }
            folders.orEmpty().forEachIndexed { index, folder ->
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    MapleSettingsRow(
                        title = folder.name,
                        summary = folder.location + " · " + pluralStringResource(R.plurals.gallery_image_count, folder.count, folder.count),
                        iconResource = R.drawable.ic_folder_24,
                        showChevron = false,
                        modifier = Modifier.weight(1f).testTag("maple.hidden.folder.$index"),
                    )
                    IconButton(
                        onClick = { onUnhide(folder) },
                        enabled = !busy,
                        modifier = Modifier.defaultMinSize(48.dp, 48.dp).padding(end = 8.dp).testTag("maple.hidden.unhide.$index"),
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_visibility_24),
                            contentDescription = stringResource(R.string.action_unhide),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
