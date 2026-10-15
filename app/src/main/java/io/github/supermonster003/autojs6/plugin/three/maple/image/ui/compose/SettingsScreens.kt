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
import io.github.supermonster003.autojs6.plugin.three.maple.image.AlbumCoverOrder
import io.github.supermonster003.autojs6.plugin.three.maple.image.GridSettings
import io.github.supermonster003.autojs6.plugin.three.maple.image.R
import io.github.supermonster003.autojs6.plugin.three.maple.image.ThumbnailCorners
import io.github.supermonster003.autojs6.plugin.three.maple.image.ThumbnailSpacing
import io.github.supermonster003.autojs6.plugin.three.maple.image.ViewerDoubleTapZoom
import io.github.supermonster003.autojs6.plugin.three.maple.image.ViewerMaxZoom
import io.github.supermonster003.autojs6.plugin.three.maple.image.ViewerOrientation
import io.github.supermonster003.autojs6.plugin.three.maple.image.ViewerSettings
import io.github.supermonster003.autojs6.plugin.three.maple.image.ReleaseHistoryEntry

/** Current-value summaries of the four appearance rows (Three settings specification section 2). */
internal data class SettingsSummaries(
    val language: String,
    val darkMode: String,
    val themeColor: String,
    val launcherIcon: String,
)

/** The grid settings that open a choice (ROADMAP 9.2). */
internal enum class GridChoice {
    ALBUM_COLUMNS_PORTRAIT, ALBUM_COLUMNS_LANDSCAPE, MEDIA_COLUMNS_PORTRAIT, MEDIA_COLUMNS_LANDSCAPE, CORNERS, SPACING, COVER,
}

/**
 * Settings page (design draft 7.2, ROADMAP 9.2): appearance (the four rows first), albums and grid, viewer, editing
 * and output, media management, storage, about.
 */
@Composable
internal fun SettingsScreen(
    summaries: SettingsSummaries,
    onBack: () -> Unit,
    onLanguage: () -> Unit,
    onDarkMode: () -> Unit,
    onThemeColor: () -> Unit,
    onLauncherIcon: () -> Unit,
    grid: GridSettings,
    /** The media grid's portrait columns, kept apart from [grid] for the menu and the pinch. */
    mediaColumns: Int,
    onGridChange: (GridSettings) -> Unit,
    onGridChoice: (GridChoice) -> Unit,
    viewer: ViewerSettings,
    onViewerChange: (ViewerSettings) -> Unit,
    onDoubleTapZoom: () -> Unit,
    onMaxZoom: () -> Unit,
    onOrientation: () -> Unit,
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
            GridSettingsSection(grid, mediaColumns, onGridChange, onGridChoice)
            MapleSectionHeader(stringResource(R.string.standalone_settings_section_viewer))
            MapleSwitchRow(
                title = stringResource(R.string.viewer_rotation_gesture_title),
                summary = stringResource(R.string.viewer_rotation_gesture_summary),
                iconResource = R.drawable.ic_rotate_right_24,
                checked = viewer.rotationGesture,
                onToggle = { onViewerChange(viewer.copy(rotationGesture = it)) },
                modifier = Modifier.testTag("maple.settings.viewerRotation"),
            )
            MapleSwitchRow(
                title = stringResource(R.string.viewer_one_finger_zoom_title),
                summary = stringResource(R.string.viewer_one_finger_zoom_summary),
                iconResource = R.drawable.ic_swap_vert_24,
                checked = viewer.oneFingerZoom,
                onToggle = { onViewerChange(viewer.copy(oneFingerZoom = it)) },
                modifier = Modifier.testTag("maple.settings.viewerOneFingerZoom"),
            )
            MapleSettingsRow(
                title = stringResource(R.string.viewer_double_tap_zoom_title),
                summary = stringResource(viewer.doubleTapZoom.labelResource()),
                iconResource = R.drawable.ic_resize_24,
                onClick = onDoubleTapZoom,
                modifier = Modifier.testTag("maple.settings.viewerDoubleTapZoom"),
            )
            MapleSettingsRow(
                title = stringResource(R.string.viewer_max_zoom_title),
                summary = stringResource(viewer.maxZoom.labelResource()),
                iconResource = R.drawable.ic_resize_24,
                onClick = onMaxZoom,
                modifier = Modifier.testTag("maple.settings.viewerMaxZoom"),
            )
            MapleSwitchRow(
                title = stringResource(R.string.viewer_edge_tap_title),
                summary = stringResource(R.string.viewer_edge_tap_summary),
                iconResource = R.drawable.ic_swap_horiz_24,
                checked = viewer.edgeTapPaging,
                onToggle = { onViewerChange(viewer.copy(edgeTapPaging = it)) },
                modifier = Modifier.testTag("maple.settings.viewerEdgeTap"),
            )
            MapleSwitchRow(
                title = stringResource(R.string.viewer_swipe_down_title),
                summary = stringResource(R.string.viewer_swipe_down_summary),
                iconResource = R.drawable.ic_close_24,
                checked = viewer.swipeDownToClose,
                onToggle = { onViewerChange(viewer.copy(swipeDownToClose = it)) },
                modifier = Modifier.testTag("maple.settings.viewerSwipeDown"),
            )
            MapleSwitchRow(
                title = stringResource(R.string.viewer_brightness_swipe_title),
                summary = stringResource(R.string.viewer_brightness_swipe_summary),
                iconResource = R.drawable.ic_brightness_24,
                checked = viewer.brightnessSwipe,
                onToggle = { onViewerChange(viewer.copy(brightnessSwipe = it)) },
                modifier = Modifier.testTag("maple.settings.viewerBrightnessSwipe"),
            )
            MapleSettingsRow(
                title = stringResource(R.string.viewer_orientation_title),
                summary = stringResource(viewer.orientation.labelResource()),
                iconResource = R.drawable.ic_rotate_left_24,
                onClick = onOrientation,
                modifier = Modifier.testTag("maple.settings.viewerOrientation"),
            )
            MapleSwitchRow(
                title = stringResource(R.string.viewer_black_background_title),
                summary = stringResource(R.string.viewer_black_background_summary),
                iconResource = R.drawable.ic_dark_mode_24,
                checked = viewer.blackBackground,
                onToggle = { onViewerChange(viewer.copy(blackBackground = it)) },
                modifier = Modifier.testTag("maple.settings.viewerBlackBackground"),
            )
            MapleSwitchRow(
                title = stringResource(R.string.viewer_max_brightness_title),
                summary = stringResource(R.string.viewer_max_brightness_summary),
                iconResource = R.drawable.ic_brightness_24,
                checked = viewer.maxBrightness,
                onToggle = { onViewerChange(viewer.copy(maxBrightness = it)) },
                modifier = Modifier.testTag("maple.settings.viewerMaxBrightness"),
            )
            MapleSwitchRow(
                title = stringResource(R.string.viewer_hide_system_bars_title),
                summary = stringResource(R.string.viewer_hide_system_bars_summary),
                iconResource = R.drawable.ic_visibility_off_24,
                checked = viewer.hideSystemBars,
                onToggle = { onViewerChange(viewer.copy(hideSystemBars = it)) },
                modifier = Modifier.testTag("maple.settings.viewerHideSystemBars"),
            )
            MapleSwitchRow(
                title = stringResource(R.string.viewer_keep_screen_on_title),
                summary = stringResource(R.string.viewer_keep_screen_on_summary),
                iconResource = R.drawable.ic_highlights_24,
                checked = viewer.keepScreenOn,
                onToggle = { onViewerChange(viewer.copy(keepScreenOn = it)) },
                modifier = Modifier.testTag("maple.settings.viewerKeepScreenOn"),
            )
            MapleSwitchRow(
                title = stringResource(R.string.viewer_thumbnail_strip_title),
                summary = stringResource(R.string.viewer_thumbnail_strip_summary),
                iconResource = R.drawable.ic_perm_media_24,
                checked = viewer.thumbnailStrip,
                onToggle = { onViewerChange(viewer.copy(thumbnailStrip = it)) },
                modifier = Modifier.testTag("maple.settings.viewerThumbnailStrip"),
            )
            MapleSwitchRow(
                title = stringResource(R.string.viewer_highest_quality_title),
                summary = stringResource(R.string.viewer_highest_quality_summary),
                iconResource = R.drawable.ic_image_24,
                checked = viewer.highestQuality,
                onToggle = { onViewerChange(viewer.copy(highestQuality = it)) },
                modifier = Modifier.testTag("maple.settings.viewerHighestQuality"),
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
            MapleSectionHeader(stringResource(R.string.settings_section_edit_output))
            MapleSwitchRow(
                title = stringResource(R.string.viewer_rotation_save_title),
                summary = stringResource(R.string.viewer_rotation_save_summary),
                iconResource = R.drawable.ic_save_24,
                checked = viewer.rotationSave,
                onToggle = { onViewerChange(viewer.copy(rotationSave = it)) },
                modifier = Modifier.testTag("maple.settings.viewerRotationSave"),
            )
            MapleSectionHeader(stringResource(R.string.settings_section_media_management))
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
            MapleSectionHeader(stringResource(R.string.standalone_settings_section_storage))
            MapleSettingsRow(
                title = stringResource(R.string.storage_permission_title),
                summary = stringResource(R.string.storage_permission_summary),
                iconResource = R.drawable.ic_folder_24,
                onClick = onStoragePermission,
                modifier = Modifier.testTag("maple.settings.storage"),
            )
            MapleSectionHeader(stringResource(R.string.settings_section_about))
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

/** Settings > Albums and grid (ROADMAP 9.2). */
@Composable
private fun GridSettingsSection(grid: GridSettings, mediaColumns: Int, onGridChange: (GridSettings) -> Unit, onGridChoice: (GridChoice) -> Unit) {
    MapleSectionHeader(stringResource(R.string.settings_section_grid))
    val automatic = stringResource(R.string.grid_columns_automatic)
    val albumPortrait = grid.albumColumnsPortrait?.let { stringResource(R.string.gallery_columns_value, it) } ?: automatic
    val albumLandscape = grid.albumColumnsLandscape?.let { stringResource(R.string.gallery_columns_value, it) } ?: automatic
    val mediaLandscape = grid.mediaColumnsLandscape?.let { stringResource(R.string.gallery_columns_value, it) }
        ?: stringResource(R.string.grid_columns_same_as_portrait)
    MapleSettingsRow(
        title = stringResource(R.string.grid_album_columns_portrait_title),
        summary = albumPortrait,
        iconResource = R.drawable.ic_apps_24,
        onClick = { onGridChoice(GridChoice.ALBUM_COLUMNS_PORTRAIT) },
        modifier = Modifier.testTag("maple.settings.gridAlbumColumnsPortrait"),
    )
    MapleSettingsRow(
        title = stringResource(R.string.grid_album_columns_landscape_title),
        summary = albumLandscape,
        iconResource = R.drawable.ic_apps_24,
        onClick = { onGridChoice(GridChoice.ALBUM_COLUMNS_LANDSCAPE) },
        modifier = Modifier.testTag("maple.settings.gridAlbumColumnsLandscape"),
    )
    MapleSettingsRow(
        title = stringResource(R.string.grid_media_columns_portrait_title),
        summary = stringResource(R.string.gallery_columns_value, mediaColumns),
        iconResource = R.drawable.ic_perm_media_24,
        onClick = { onGridChoice(GridChoice.MEDIA_COLUMNS_PORTRAIT) },
        modifier = Modifier.testTag("maple.settings.gridMediaColumnsPortrait"),
    )
    MapleSettingsRow(
        title = stringResource(R.string.grid_media_columns_landscape_title),
        summary = mediaLandscape,
        iconResource = R.drawable.ic_perm_media_24,
        onClick = { onGridChoice(GridChoice.MEDIA_COLUMNS_LANDSCAPE) },
        modifier = Modifier.testTag("maple.settings.gridMediaColumnsLandscape"),
    )
    MapleSettingsRow(
        title = stringResource(R.string.grid_corners_title),
        summary = stringResource(grid.corners.labelResource()),
        iconResource = R.drawable.ic_shape_rectangle_24,
        onClick = { onGridChoice(GridChoice.CORNERS) },
        modifier = Modifier.testTag("maple.settings.gridCorners"),
    )
    MapleSettingsRow(
        title = stringResource(R.string.grid_spacing_title),
        summary = stringResource(grid.spacing.labelResource()),
        iconResource = R.drawable.ic_select_all_24,
        onClick = { onGridChoice(GridChoice.SPACING) },
        modifier = Modifier.testTag("maple.settings.gridSpacing"),
    )
    MapleSwitchRow(
        title = stringResource(R.string.grid_crop_square_title),
        summary = stringResource(R.string.grid_crop_square_summary),
        iconResource = R.drawable.ic_crop_24,
        checked = grid.cropSquare,
        onToggle = { onGridChange(grid.copy(cropSquare = it)) },
        modifier = Modifier.testTag("maple.settings.gridCropSquare"),
    )
    MapleSwitchRow(
        title = stringResource(R.string.grid_type_badges_title),
        summary = stringResource(R.string.grid_type_badges_summary),
        iconResource = R.drawable.ic_text_24,
        checked = grid.typeBadges,
        onToggle = { onGridChange(grid.copy(typeBadges = it)) },
        modifier = Modifier.testTag("maple.settings.gridTypeBadges"),
    )
    MapleSwitchRow(
        title = stringResource(R.string.grid_album_counts_title),
        summary = stringResource(R.string.grid_album_counts_summary),
        iconResource = R.drawable.ic_info_outline_24,
        checked = grid.albumCounts,
        onToggle = { onGridChange(grid.copy(albumCounts = it)) },
        modifier = Modifier.testTag("maple.settings.gridAlbumCounts"),
    )
    MapleSettingsRow(
        title = stringResource(R.string.grid_album_cover_title),
        summary = stringResource(grid.albumCover.labelResource()),
        iconResource = R.drawable.ic_image_24,
        onClick = { onGridChoice(GridChoice.COVER) },
        modifier = Modifier.testTag("maple.settings.gridAlbumCover"),
    )
    MapleSwitchRow(
        title = stringResource(R.string.grid_single_line_names_title),
        summary = stringResource(R.string.grid_single_line_names_summary),
        iconResource = R.drawable.ic_text_24,
        checked = grid.singleLineAlbumNames,
        onToggle = { onGridChange(grid.copy(singleLineAlbumNames = it)) },
        modifier = Modifier.testTag("maple.settings.gridSingleLineNames"),
    )
    MapleSwitchRow(
        title = stringResource(R.string.grid_animated_thumbnails_title),
        summary = stringResource(R.string.grid_animated_thumbnails_summary),
        iconResource = R.drawable.ic_play_arrow_24,
        checked = grid.animatedThumbnails,
        onToggle = { onGridChange(grid.copy(animatedThumbnails = it)) },
        modifier = Modifier.testTag("maple.settings.gridAnimatedThumbnails"),
    )
}

internal fun ThumbnailCorners.labelResource(): Int = when (this) {
    ThumbnailCorners.NONE -> R.string.grid_corners_none
    ThumbnailCorners.SMALL -> R.string.grid_corners_small
    ThumbnailCorners.LARGE -> R.string.grid_corners_large
}

internal fun ThumbnailSpacing.labelResource(): Int = when (this) {
    ThumbnailSpacing.NONE -> R.string.grid_spacing_none
    ThumbnailSpacing.NORMAL -> R.string.grid_spacing_normal
    ThumbnailSpacing.WIDE -> R.string.grid_spacing_wide
}

internal fun AlbumCoverOrder.labelResource(): Int = when (this) {
    AlbumCoverOrder.NEWEST -> R.string.grid_album_cover_newest
    AlbumCoverOrder.OLDEST -> R.string.grid_album_cover_oldest
}

internal fun ViewerDoubleTapZoom.labelResource(): Int = when (this) {
    ViewerDoubleTapZoom.TWO_AND_HALF -> R.string.viewer_zoom_two_and_half
    ViewerDoubleTapZoom.ACTUAL_PIXELS -> R.string.viewer_zoom_actual_pixels
}

internal fun ViewerOrientation.labelResource(): Int = when (this) {
    ViewerOrientation.FOLLOW_SYSTEM -> R.string.viewer_orientation_follow_system
    ViewerOrientation.LOCKED -> R.string.viewer_orientation_locked
    ViewerOrientation.FOLLOW_IMAGE -> R.string.viewer_orientation_follow_image
}

internal fun ViewerMaxZoom.labelResource(): Int = when (this) {
    ViewerMaxZoom.FIVE -> R.string.viewer_zoom_five
    ViewerMaxZoom.TEN -> R.string.viewer_zoom_ten
    ViewerMaxZoom.ACTUAL_PIXELS -> R.string.viewer_zoom_actual_pixels
}
