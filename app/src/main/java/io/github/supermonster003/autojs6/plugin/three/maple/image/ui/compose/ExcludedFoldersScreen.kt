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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.supermonster003.autojs6.plugin.three.maple.image.AlbumPath
import io.github.supermonster003.autojs6.plugin.three.maple.image.R

/** One excluded album folder: its name, and the location shown under it. */
internal data class ExcludedFolder(val path: AlbumPath, val name: String, val location: String)

/** Settings > Excluded folders (ROADMAP 7.4): what the exclusion covers, then one row per folder with "show again". */
@Composable
internal fun ExcludedFoldersScreen(folders: List<ExcludedFolder>?, onBack: () -> Unit, onRestore: (ExcludedFolder) -> Unit) {
    MapleScreen(title = stringResource(R.string.excluded_folders_title), onBack = onBack) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())) {
            Text(
                text = stringResource(R.string.excluded_folders_summary),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = MapleSettingsDefaults.horizontalPadding, vertical = 12.dp),
            )
            if (folders != null && folders.isEmpty()) {
                MapleEmptyState(
                    title = stringResource(R.string.excluded_folders_empty),
                    body = stringResource(R.string.excluded_folders_empty_body),
                    icon = painterResource(R.drawable.ic_block_24),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            folders.orEmpty().forEachIndexed { index, folder ->
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    MapleSettingsRow(
                        title = folder.name,
                        summary = folder.location,
                        iconResource = R.drawable.ic_folder_24,
                        showChevron = false,
                        modifier = Modifier.weight(1f).testTag("maple.excluded.folder.$index"),
                    )
                    IconButton(
                        onClick = { onRestore(folder) },
                        modifier = Modifier.defaultMinSize(48.dp, 48.dp).padding(end = 8.dp).testTag("maple.excluded.restore.$index"),
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_visibility_24),
                            contentDescription = stringResource(R.string.excluded_folders_restore),
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
