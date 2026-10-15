package io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.supermonster003.autojs6.plugin.three.maple.image.R
import io.github.supermonster003.autojs6.plugin.three.maple.image.ReleaseHistoryEntry

/** Current-value summaries of the four appearance rows (Three settings specification section 2). */
internal data class SettingsSummaries(
    val language: String,
    val darkMode: String,
    val themeColor: String,
    val launcherIcon: String,
)

/** Settings page: the appearance group first, then the information group (design draft 7.2). */
@Composable
internal fun SettingsScreen(
    summaries: SettingsSummaries,
    onBack: () -> Unit,
    onLanguage: () -> Unit,
    onDarkMode: () -> Unit,
    onThemeColor: () -> Unit,
    onLauncherIcon: () -> Unit,
    viewerRotationGesture: Boolean,
    onViewerRotationGesture: (Boolean) -> Unit,
    viewerRotationSave: Boolean,
    onViewerRotationSave: (Boolean) -> Unit,
    showLocation: Boolean,
    onShowLocation: (Boolean) -> Unit,
    extendedDetails: Boolean,
    onExtendedDetails: (Boolean) -> Unit,
    /** The chosen fields, for the summary of the row that opens the choice. */
    extendedDetailFields: String,
    onExtendedDetailFields: () -> Unit,
    recycleBinEnabled: Boolean,
    onRecycleBinEnabled: (Boolean) -> Unit,
    /** Null hides the row (before Android 11). */
    syncSystemFavorites: Boolean?,
    onSyncSystemFavorites: (Boolean) -> Unit,
    onStoragePermission: () -> Unit,
    onExcludedFolders: () -> Unit,
    onHiddenFolders: () -> Unit,
    onReleaseHistory: () -> Unit,
    onAbout: () -> Unit,
) {
    MapleScreen(title = stringResource(R.string.standalone_settings_title), onBack = onBack) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())) {
            MapleSectionHeader(stringResource(R.string.standalone_settings_section_appearance))
            MapleSettingsRow(
                title = stringResource(R.string.standalone_settings_language),
                summary = summaries.language,
                iconResource = R.drawable.ic_language_24,
                onClick = onLanguage,
                modifier = Modifier.testTag("maple.settings.language"),
            )
            MapleSettingsRow(
                title = stringResource(R.string.standalone_settings_dark_mode),
                summary = summaries.darkMode,
                iconResource = R.drawable.ic_dark_mode_24,
                onClick = onDarkMode,
                modifier = Modifier.testTag("maple.settings.darkMode"),
            )
            MapleSettingsRow(
                title = stringResource(R.string.standalone_settings_theme_color),
                summary = summaries.themeColor,
                iconResource = R.drawable.ic_palette_24,
                onClick = onThemeColor,
                modifier = Modifier.testTag("maple.settings.themeColor"),
            )
            MapleSettingsRow(
                title = stringResource(R.string.launcher_icon_title),
                summary = summaries.launcherIcon,
                iconResource = R.drawable.ic_apps_24,
                onClick = onLauncherIcon,
                modifier = Modifier.testTag("maple.settings.launcherIcon"),
            )
            MapleSectionHeader(stringResource(R.string.standalone_settings_section_viewer))
            MapleSwitchRow(
                title = stringResource(R.string.viewer_rotation_gesture_title),
                summary = stringResource(R.string.viewer_rotation_gesture_summary),
                iconResource = R.drawable.ic_rotate_right_24,
                checked = viewerRotationGesture,
                onToggle = onViewerRotationGesture,
                modifier = Modifier.testTag("maple.settings.viewerRotation"),
            )
            MapleSwitchRow(
                title = stringResource(R.string.viewer_rotation_save_title),
                summary = stringResource(R.string.viewer_rotation_save_summary),
                iconResource = R.drawable.ic_save_24,
                checked = viewerRotationSave,
                onToggle = onViewerRotationSave,
                modifier = Modifier.testTag("maple.settings.viewerRotationSave"),
            )
            MapleSwitchRow(
                title = stringResource(R.string.settings_show_location_title),
                summary = stringResource(R.string.settings_show_location_summary),
                iconResource = R.drawable.ic_location_on_24,
                checked = showLocation,
                onToggle = onShowLocation,
                modifier = Modifier.testTag("maple.settings.showLocation"),
            )
            MapleSwitchRow(
                title = stringResource(R.string.settings_extended_details_title),
                summary = stringResource(R.string.settings_extended_details_summary),
                iconResource = R.drawable.ic_info_outline_24,
                checked = extendedDetails,
                onToggle = onExtendedDetails,
                modifier = Modifier.testTag("maple.settings.extendedDetails"),
            )
            MapleSettingsRow(
                title = stringResource(R.string.settings_extended_detail_fields_title),
                summary = extendedDetailFields,
                iconResource = R.drawable.ic_tune_24,
                onClick = onExtendedDetailFields,
                enabled = extendedDetails,
                modifier = Modifier.testTag("maple.settings.extendedDetailFields"),
            )
            MapleSectionHeader(stringResource(R.string.standalone_settings_section_storage))
            MapleSettingsRow(
                title = stringResource(R.string.storage_permission_title),
                summary = stringResource(R.string.storage_permission_summary),
                iconResource = R.drawable.ic_folder_24,
                onClick = onStoragePermission,
                modifier = Modifier.testTag("maple.settings.storage"),
            )
            MapleSettingsRow(
                title = stringResource(R.string.excluded_folders_title),
                summary = stringResource(R.string.excluded_folders_row_summary),
                iconResource = R.drawable.ic_block_24,
                onClick = onExcludedFolders,
                modifier = Modifier.testTag("maple.settings.excludedFolders"),
            )
            MapleSettingsRow(
                title = stringResource(R.string.hidden_folders_title),
                summary = stringResource(R.string.hidden_folders_row_summary),
                iconResource = R.drawable.ic_visibility_off_24,
                onClick = onHiddenFolders,
                modifier = Modifier.testTag("maple.settings.hiddenFolders"),
            )
            MapleSwitchRow(
                title = stringResource(R.string.settings_recycle_bin_title),
                summary = stringResource(R.string.settings_recycle_bin_summary),
                iconResource = R.drawable.ic_delete_24,
                checked = recycleBinEnabled,
                onToggle = onRecycleBinEnabled,
                modifier = Modifier.testTag("maple.settings.recycleBin"),
            )
            if (syncSystemFavorites != null) {
                MapleSwitchRow(
                    title = stringResource(R.string.settings_sync_favorites_title),
                    summary = stringResource(R.string.settings_sync_favorites_summary),
                    iconResource = R.drawable.ic_favorite_border_24,
                    checked = syncSystemFavorites,
                    onToggle = onSyncSystemFavorites,
                    modifier = Modifier.testTag("maple.settings.syncFavorites"),
                )
            }
            MapleSectionHeader(stringResource(R.string.standalone_settings_section_information))
            MapleSettingsRow(
                title = stringResource(R.string.standalone_release_history_title),
                summary = stringResource(R.string.standalone_update_release_history_summary),
                iconResource = R.drawable.ic_article_24,
                onClick = onReleaseHistory,
                modifier = Modifier.testTag("maple.settings.releaseHistory"),
            )
            MapleSettingsRow(
                title = stringResource(R.string.standalone_about_title),
                summary = stringResource(R.string.standalone_about_app_summary),
                iconResource = R.drawable.ic_info_24,
                onClick = onAbout,
                modifier = Modifier.testTag("maple.settings.about"),
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}

/** About page: the transparent brand artwork in its rounded container, identity text and the sources row. */
@Composable
internal fun AboutScreen(versionName: String, onBack: () -> Unit, onSources: () -> Unit) {
    MapleScreen(title = stringResource(R.string.standalone_about_title), onBack = onBack) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(32.dp))
            Image(
                painter = painterResource(R.mipmap.ic_launcher),
                contentDescription = stringResource(R.string.app_name),
                modifier = Modifier
                    .size(112.dp)
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(24.dp))
                    .padding(8.dp)
                    .testTag("maple.about.icon"),
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
            Text(
                text = versionName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp).testTag("maple.about.version"),
            )
            Text(
                text = stringResource(R.string.plugin_description),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
            )
            Spacer(Modifier.height(8.dp))
            MapleSettingsRow(
                title = stringResource(R.string.standalone_home_sources),
                summary = stringResource(R.string.standalone_home_sources_summary),
                iconResource = R.drawable.ic_open_in_new_24,
                onClick = onSources,
                modifier = Modifier.testTag("maple.about.sources"),
            )
        }
    }
}

/** Release history: one group per version (version + date header), bullets with a tinted category chip. */
@Composable
internal fun ReleaseHistoryScreen(entries: List<ReleaseHistoryEntry>, onBack: () -> Unit) {
    MapleScreen(title = stringResource(R.string.standalone_release_history_title), onBack = onBack) { padding ->
        if (entries.isEmpty()) {
            Column(modifier = Modifier.fillMaxSize().padding(padding), horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(Modifier.height(48.dp))
                MapleEmptyState(
                    title = stringResource(R.string.standalone_release_history_load_failed),
                    icon = painterResource(R.drawable.ic_warning_24),
                )
            }
            return@MapleScreen
        }
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).testTag("maple.releaseHistory.list")) {
            itemsIndexed(entries, key = { _, entry -> entry.version }) { index, entry ->
                if (index > 0) MapleHairline(insetStart = 24.dp)
                ReleaseEntry(entry)
            }
        }
    }
}

@Composable
private fun ReleaseEntry(entry: ReleaseHistoryEntry) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = entry.version,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            if (entry.date != null) {
                Text(
                    text = entry.date,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        entry.items.forEach { item ->
            Row(modifier = Modifier.padding(top = 10.dp), verticalAlignment = Alignment.Top) {
                if (item.tag != null) {
                    Text(
                        text = item.tag,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .padding(top = 1.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(10.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                }
                Text(
                    text = item.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}
