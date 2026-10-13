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
import io.github.supermonster003.autojs6.plugin.three.maple.image.AuthorizedFolder
import io.github.supermonster003.autojs6.plugin.three.maple.image.MediaAccessState
import io.github.supermonster003.autojs6.plugin.three.maple.image.MediaPermissionAction
import io.github.supermonster003.autojs6.plugin.three.maple.image.R

internal data class StoragePermissionUiModel(
    val state: MediaAccessState = MediaAccessState.DENIED,
    val nextAction: MediaPermissionAction = MediaPermissionAction.REQUEST,
    val allFilesAvailable: Boolean = false,
    val allFilesOn: Boolean = false,
    /** Android 12+ media management special access (ROADMAP 7.1): write requests are granted without a dialog. */
    val manageMediaAvailable: Boolean = false,
    val manageMediaOn: Boolean = false,
    val folders: List<AuthorizedFolder> = emptyList(),
)

internal class StoragePermissionCallbacks(
    val onBack: () -> Unit,
    val onGrant: () -> Unit,
    val onManageSelected: () -> Unit,
    val onAllFiles: () -> Unit,
    val onManageMedia: () -> Unit,
    val onAddFolder: () -> Unit,
    val onRemoveFolder: (AuthorizedFolder) -> Unit,
)

/** Settings > Storage permission (design draft 8.4): status, the one action that applies, all files access, authorized folders. */
@Composable
internal fun StoragePermissionScreen(model: StoragePermissionUiModel, callbacks: StoragePermissionCallbacks) {
    MapleScreen(title = stringResource(R.string.storage_permission_title), onBack = callbacks.onBack) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())) {
            MapleSectionHeader(stringResource(R.string.storage_section_status))
            MapleSettingsRow(
                title = stringResource(R.string.storage_photo_access),
                summary = stringResource(stateLabel(model.state)),
                iconResource = R.drawable.ic_image_24,
                showChevron = false,
                modifier = Modifier.testTag("maple.storage.state"),
            )
            when (model.nextAction) {
                MediaPermissionAction.REQUEST -> MapleSettingsRow(
                    title = stringResource(R.string.storage_grant_photo_access),
                    iconResource = R.drawable.ic_check_24,
                    onClick = callbacks.onGrant,
                    modifier = Modifier.testTag("maple.storage.grant"),
                )
                MediaPermissionAction.OPEN_APP_SETTINGS -> MapleSettingsRow(
                    title = stringResource(R.string.storage_open_app_settings),
                    iconResource = R.drawable.ic_open_in_new_24,
                    onClick = callbacks.onGrant,
                    modifier = Modifier.testTag("maple.storage.appSettings"),
                )
                MediaPermissionAction.MANAGE_SELECTED -> {
                    MapleSettingsRow(
                        title = stringResource(R.string.storage_manage_selected),
                        iconResource = R.drawable.ic_image_24,
                        onClick = callbacks.onManageSelected,
                        modifier = Modifier.testTag("maple.storage.manageSelected"),
                    )
                    MapleSettingsRow(
                        title = stringResource(R.string.storage_grant_photo_access),
                        iconResource = R.drawable.ic_check_24,
                        onClick = callbacks.onGrant,
                        modifier = Modifier.testTag("maple.storage.grant"),
                    )
                }
                MediaPermissionAction.NONE -> Unit
            }
            MapleSectionHeader(stringResource(R.string.storage_section_advanced))
            MapleSwitchRow(
                title = stringResource(R.string.storage_all_files_access),
                summary = if (model.allFilesAvailable) stringResource(R.string.storage_all_files_access_summary) else stringResource(R.string.storage_all_files_unavailable),
                iconResource = R.drawable.ic_folder_24,
                checked = model.allFilesOn,
                onToggle = { callbacks.onAllFiles() },
                enabled = model.allFilesAvailable,
                modifier = Modifier.testTag("maple.storage.allFiles"),
            )
            MapleSwitchRow(
                title = stringResource(R.string.storage_manage_media),
                summary = if (model.manageMediaAvailable) stringResource(R.string.storage_manage_media_summary) else stringResource(R.string.storage_manage_media_unavailable),
                iconResource = R.drawable.ic_perm_media_24,
                checked = model.manageMediaOn,
                onToggle = { callbacks.onManageMedia() },
                enabled = model.manageMediaAvailable,
                modifier = Modifier.testTag("maple.storage.manageMedia"),
            )
            MapleSectionHeader(stringResource(R.string.storage_section_folders))
            Text(
                text = stringResource(R.string.storage_folders_summary),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = MapleSettingsDefaults.horizontalPadding, vertical = 4.dp),
            )
            if (model.folders.isEmpty()) {
                MapleSettingsRow(
                    title = stringResource(R.string.storage_no_folders),
                    iconResource = R.drawable.ic_folder_24,
                    showChevron = false,
                    enabled = false,
                    modifier = Modifier.testTag("maple.storage.noFolders"),
                )
            }
            model.folders.forEachIndexed { index, folder ->
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    MapleSettingsRow(
                        title = folder.displayName,
                        summary = if (folder.available) folder.location.takeIf { it.isNotEmpty() } else stringResource(R.string.storage_folder_unavailable),
                        iconResource = R.drawable.ic_folder_24,
                        showChevron = false,
                        enabled = folder.available,
                        modifier = Modifier.weight(1f).testTag("maple.storage.folder.$index"),
                    )
                    IconButton(
                        onClick = { callbacks.onRemoveFolder(folder) },
                        modifier = Modifier.defaultMinSize(48.dp, 48.dp).padding(end = 8.dp).testTag("maple.storage.removeFolder.$index"),
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_close_24),
                            contentDescription = stringResource(R.string.storage_remove_folder),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }
            }
            MapleSettingsRow(
                title = stringResource(R.string.storage_add_folder),
                iconResource = R.drawable.ic_add_24,
                showChevron = false,
                onClick = callbacks.onAddFolder,
                modifier = Modifier.testTag("maple.storage.addFolder"),
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}

private fun stateLabel(state: MediaAccessState): Int = when (state) {
    MediaAccessState.FULL -> R.string.storage_state_full
    MediaAccessState.PARTIAL -> R.string.storage_state_partial
    MediaAccessState.DENIED -> R.string.storage_state_denied
    MediaAccessState.NEVER_ASK -> R.string.storage_state_never_ask
    MediaAccessState.ALL_FILES -> R.string.storage_state_all_files
}
