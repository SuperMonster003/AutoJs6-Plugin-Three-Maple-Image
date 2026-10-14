package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.MapleChoice
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.MapleChoiceDialog
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.MapleTheme
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.MapleThemeColorDialog
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.MapleThemeColorLabels
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.SettingsScreen
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.SettingsSummaries
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * Application settings: appearance (language, dark mode, theme color, each able to follow the AutoJs6 host,
 * plus the launcher icon) and entry points to the release history and about screens. Every dialog follows
 * the "select, then confirm" rule; saving persists once and recreates the screen through [ConfiguredActivity].
 */
internal class AppSettingsActivity : ConfiguredActivity() {

    private sealed interface SettingsDialog {
        data object Language : SettingsDialog
        data object DarkMode : SettingsDialog
        data object ThemeColor : SettingsDialog
        data object LauncherIcon : SettingsDialog
    }

    private lateinit var settingsStore: ApplicationSettingsStore
    private lateinit var settings: ApplicationSettings
    private lateinit var hostResult: AutoJs6HostSettingsResult
    private var dialog by mutableStateOf<SettingsDialog?>(null)
    private var launcherIconSummary by mutableStateOf("")
    private var viewerRotationGesture by mutableStateOf(true)
    private var viewerRotationSave by mutableStateOf(true)
    private var recycleBinEnabled by mutableStateOf(true)
    private var syncSystemFavorites by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settingsStore = ApplicationSettingsStore(applicationContext)
        settings = settingsStore.load()
        viewerRotationGesture = settings.viewerRotationGesture
        viewerRotationSave = settings.viewerRotationSave
        recycleBinEnabled = settings.recycleBinEnabled
        syncSystemFavorites = settings.syncSystemFavorites
        hostResult = AutoJs6HostSettingsClient.query(this)
        launcherIconSummary = getString(launcherIconLabels[LauncherIcons.current(this).ordinal])
        setContent {
            MapleTheme(palette = appPalette) {
                SettingsScreen(
                    summaries = SettingsSummaries(
                        language = languageSummary(),
                        darkMode = darkModeSummary(),
                        themeColor = themeSummary(),
                        launcherIcon = launcherIconSummary,
                    ),
                    onBack = ::finish,
                    onLanguage = { open(SettingsDialog.Language) },
                    onDarkMode = { open(SettingsDialog.DarkMode) },
                    onThemeColor = { open(SettingsDialog.ThemeColor) },
                    onLauncherIcon = { open(SettingsDialog.LauncherIcon) },
                    viewerRotationGesture = viewerRotationGesture,
                    onViewerRotationGesture = ::updateViewerRotationGesture,
                    viewerRotationSave = viewerRotationSave,
                    onViewerRotationSave = ::updateViewerRotationSave,
                    recycleBinEnabled = recycleBinEnabled,
                    onRecycleBinEnabled = ::updateRecycleBinEnabled,
                    syncSystemFavorites = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) syncSystemFavorites else null,
                    onSyncSystemFavorites = ::updateSyncSystemFavorites,
                    onStoragePermission = { startActivity(StoragePermissionActivity.intent(this)) },
                    onExcludedFolders = { startActivity(ExcludedFoldersActivity.intent(this)) },
                    onHiddenFolders = { startActivity(HiddenFoldersActivity.intent(this)) },
                    onReleaseHistory = { startActivity(Intent(this, ReleaseHistoryActivity::class.java)) },
                    onAbout = { startActivity(Intent(this, AboutActivity::class.java)) },
                )
                Dialogs()
            }
        }
    }

    private fun updateViewerRotationGesture(enabled: Boolean) {
        viewerRotationGesture = enabled
        settings = settings.copy(viewerRotationGesture = enabled)
        settingsStore.saveViewerRotationGesture(enabled)
    }

    private fun updateViewerRotationSave(enabled: Boolean) {
        viewerRotationSave = enabled
        settings = settings.copy(viewerRotationSave = enabled)
        settingsStore.saveViewerRotationSave(enabled)
    }

    private fun updateRecycleBinEnabled(enabled: Boolean) {
        recycleBinEnabled = enabled
        settings = settings.copy(recycleBinEnabled = enabled)
        settingsStore.saveRecycleBinEnabled(enabled)
    }

    /** Switching the sync on brings the system's favorites into the local table once. */
    private fun updateSyncSystemFavorites(enabled: Boolean) {
        syncSystemFavorites = enabled
        settings = settings.copy(syncSystemFavorites = enabled)
        settingsStore.saveSyncSystemFavorites(enabled)
        if (enabled) {
            val context = applicationContext
            lifecycleScope.launch { runCatching { FavoritesRepository(context).importSystemFavorites(MediaStoreRepository(context).loadAll()) } }
        }
    }

    private fun open(target: SettingsDialog) {
        // Re-read the host appearance so "follow AutoJs6" descriptions reflect the current host state.
        if (target != SettingsDialog.LauncherIcon) hostResult = AutoJs6HostSettingsClient.query(this)
        dialog = target
        composeDialogOpen = true
    }

    private fun close() {
        dialog = null
        composeDialogOpen = false
    }

    @Composable
    private fun Dialogs() {
        when (dialog) {
            null -> Unit
            SettingsDialog.Language -> {
                val values = AppLanguage.entries
                MapleChoiceDialog(
                    title = getString(R.string.standalone_settings_language),
                    options = values.mapIndexed { index, language ->
                        followAutoJs6Choice(getString(language.labelResource()), index == 0, quotedFollowSystemLabel())
                    },
                    selectedIndex = values.indexOf(settings.language),
                    onDismissRequest = ::close,
                    onConfirm = { index -> close(); saveSettings(settings.copy(language = values[index])) },
                    confirmText = getString(R.string.dialog_button_confirm),
                    dismissText = getString(R.string.dialog_button_cancel),
                )
            }
            SettingsDialog.DarkMode -> {
                val values = AppDarkMode.entries
                MapleChoiceDialog(
                    title = getString(R.string.standalone_settings_dark_mode),
                    options = values.mapIndexed { index, mode ->
                        followAutoJs6Choice(getString(mode.labelResource()), index == 0, quotedFollowSystemLabel())
                    },
                    selectedIndex = values.indexOf(settings.darkMode),
                    onDismissRequest = ::close,
                    onConfirm = { index -> close(); saveSettings(settings.copy(darkMode = values[index])) },
                    confirmText = getString(R.string.dialog_button_confirm),
                    dismissText = getString(R.string.dialog_button_cancel),
                )
            }
            SettingsDialog.ThemeColor -> MapleThemeColorDialog(
                currentColor = if (settings.themeSelection == AppThemeSelection.FOLLOW_AUTOJS6) null else settings.customThemeColor,
                hostColor = followedThemeColor(),
                labels = MapleThemeColorLabels(
                    title = getString(R.string.standalone_settings_theme_color),
                    followHost = getString(R.string.standalone_follow_autojs6),
                    presets = getString(R.string.theme_picker_presets),
                    custom = getString(R.string.theme_picker_custom),
                    input = getString(R.string.theme_picker_input),
                    invalid = getString(R.string.theme_picker_invalid),
                    preview = getString(R.string.theme_picker_preview),
                    confirm = getString(R.string.dialog_button_confirm),
                    dismiss = getString(R.string.dialog_button_cancel),
                ),
                onDismissRequest = ::close,
                onConfirm = { color ->
                    close()
                    saveSettings(
                        if (color == null) settings.copy(themeSelection = AppThemeSelection.FOLLOW_AUTOJS6)
                        else settings.copy(themeSelection = AppThemeSelection.CUSTOM, customThemeColor = color),
                    )
                },
            )
            SettingsDialog.LauncherIcon -> {
                val modes = LauncherIconMode.entries
                MapleChoiceDialog(
                    title = getString(R.string.launcher_icon_title),
                    options = modes.map { mode ->
                        MapleChoice(
                            label = getString(launcherIconLabels[mode.ordinal]),
                            description = when (mode) {
                                LauncherIconMode.AUTO -> getString(R.string.launcher_icon_auto_note)
                                LauncherIconMode.TRANSPARENT -> getString(R.string.launcher_icon_transparent_note)
                                else -> null
                            },
                        )
                    },
                    selectedIndex = modes.indexOf(LauncherIcons.current(this)),
                    onDismissRequest = ::close,
                    onConfirm = { index ->
                        close()
                        val succeeded = runCatching { LauncherIcons.select(this, modes[index]) }.isSuccess
                        launcherIconSummary = getString(launcherIconLabels[LauncherIcons.current(this).ordinal])
                        Toast.makeText(this, if (succeeded) R.string.launcher_icon_applied_note else R.string.launcher_icon_failed, Toast.LENGTH_LONG).show()
                    },
                    confirmText = getString(R.string.dialog_button_confirm),
                    dismissText = getString(R.string.dialog_button_cancel),
                )
            }
        }
    }

    internal companion object {
        val launcherIconLabels = listOf(R.string.launcher_icon_light, R.string.launcher_icon_dark, R.string.launcher_icon_auto, R.string.launcher_icon_transparent)
    }

    // region Saving

    private fun saveSettings(updated: ApplicationSettings) {
        if (updated == settings) return
        settingsStore.save(updated)
        settings = updated
        Toast.makeText(this, R.string.standalone_settings_saved, Toast.LENGTH_SHORT).show()
        recreate()
    }

    // endregion

    // region Summaries and labels

    private fun languageSummary(): String = when (settings.language) {
        AppLanguage.FOLLOW_AUTOJS6 -> followAutoJs6Summary(resolvedHostLanguageLabel())
        else -> getString(settings.language.labelResource())
    }

    private fun darkModeSummary(): String = when (settings.darkMode) {
        AppDarkMode.FOLLOW_AUTOJS6 -> followAutoJs6Summary(
            getString(hostResult.snapshot?.darkModePolicy?.labelResource() ?: R.string.standalone_follow_system),
        )
        else -> getString(settings.darkMode.labelResource())
    }

    private fun themeSummary(): String = when (settings.themeSelection) {
        AppThemeSelection.FOLLOW_AUTOJS6 -> followAutoJs6Summary(AppSettingsPolicy.colorHex(followedThemeColor()))
        AppThemeSelection.CUSTOM -> AppSettingsPolicy.colorHex(settings.customThemeColor)
    }

    private fun followAutoJs6Summary(value: String): String =
        getString(R.string.standalone_follow_autojs6_summary, value)

    private fun followedThemeColor(): Int =
        AppSettingsPolicy.resolveAutoJs6ThemeColor(hostResult.snapshot?.themeColorPrimary)

    private fun resolvedHostLanguageLabel(): String {
        val tag = hostResult.snapshot?.resolvedLanguageTag?.trim().orEmpty()
        if (tag.isEmpty()) return getString(R.string.standalone_follow_system)
        val language = AppSettingsPolicy.languageForResolvedTag(tag)
        if (language != null) return getString(language.labelResource())
        return Locale.forLanguageTag(tag).displayName.ifBlank { tag }
    }

    private fun quotedFollowSystemLabel(): String =
        "\"${getString(R.string.standalone_follow_system)}\""

    /**
     * The "follow AutoJs6" choice stays selectable even when the host is not usable; a smaller secondary line
     * then explains which fallback value applies instead of hiding or disabling the choice.
     */
    private fun followAutoJs6Choice(title: String, isFollowChoice: Boolean, fallbackValue: String): MapleChoice {
        if (!isFollowChoice || hostResult.selectable) return MapleChoice(title)
        return MapleChoice(title, getString(R.string.standalone_follow_autojs6_fallback_subtitle, fallbackValue))
    }

    @StringRes
    private fun AppDarkMode.labelResource(): Int = when (this) {
        AppDarkMode.FOLLOW_AUTOJS6 -> R.string.standalone_follow_autojs6
        AppDarkMode.FOLLOW_SYSTEM -> R.string.standalone_follow_system
        AppDarkMode.LIGHT -> R.string.standalone_dark_mode_light
        AppDarkMode.DARK -> R.string.standalone_dark_mode_dark
    }

    @StringRes
    private fun AutoJs6DarkModePolicy.labelResource(): Int = when (this) {
        AutoJs6DarkModePolicy.FOLLOW_SYSTEM -> R.string.standalone_follow_system
        AutoJs6DarkModePolicy.LIGHT -> R.string.standalone_dark_mode_light
        AutoJs6DarkModePolicy.DARK -> R.string.standalone_dark_mode_dark
    }

    @StringRes
    private fun AppLanguage.labelResource(): Int = when (this) {
        AppLanguage.FOLLOW_AUTOJS6 -> R.string.standalone_follow_autojs6
        AppLanguage.FOLLOW_SYSTEM -> R.string.standalone_follow_system
        AppLanguage.CHINESE_SIMPLIFIED -> R.string.standalone_language_zh_hans
        AppLanguage.CHINESE_TRADITIONAL_HONG_KONG -> R.string.standalone_language_zh_hant_hk
        AppLanguage.CHINESE_TRADITIONAL_TAIWAN -> R.string.standalone_language_zh_hant_tw
        AppLanguage.ENGLISH -> R.string.standalone_language_en
        AppLanguage.FRENCH -> R.string.standalone_language_fr
        AppLanguage.SPANISH -> R.string.standalone_language_es
        AppLanguage.JAPANESE -> R.string.standalone_language_ja
        AppLanguage.KOREAN -> R.string.standalone_language_ko
        AppLanguage.RUSSIAN -> R.string.standalone_language_ru
        AppLanguage.ARABIC -> R.string.standalone_language_ar
    }

    // endregion
}
