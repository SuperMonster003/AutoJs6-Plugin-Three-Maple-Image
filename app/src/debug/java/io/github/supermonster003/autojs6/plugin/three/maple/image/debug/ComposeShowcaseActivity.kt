package io.github.supermonster003.autojs6.plugin.three.maple.image.debug

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import io.github.supermonster003.autojs6.plugin.three.maple.image.ConfiguredActivity
import io.github.supermonster003.autojs6.plugin.three.maple.image.R
import io.github.supermonster003.autojs6.plugin.three.maple.image.ThemeColorValue
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.MapleAction
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.MapleBottomActionBar
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.MapleChoice
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.MapleChoiceDialog
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.MapleEmptyAction
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.MapleEmptyState
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.MapleHairline
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.MapleSectionHeader
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.MapleSettingsRow
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.MapleSwitchRow
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.MapleTheme
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.MapleThemeColorDialog
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.MapleThemeColorLabels
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.MapleTopBar
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.MapleZoomIndicator

/**
 * Debug-only kitchen sink of the shared Compose components (ROADMAP 5.1). Launch with
 * `am start -n <pkg>/.debug.ComposeShowcaseActivity`; it renders every component in the current app palette so
 * light / dark, font scale and RTL screenshots can be taken without migrating a real screen first.
 */
class ComposeShowcaseActivity : ConfiguredActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MapleTheme(palette = appPalette) {
                Showcase()
            }
        }
    }

    @Composable
    private fun Showcase() {
        var choiceOpen by rememberSaveable { mutableStateOf(false) }
        var themeOpen by rememberSaveable { mutableStateOf(false) }
        var choice by rememberSaveable { mutableIntStateOf(1) }
        var switch by rememberSaveable { mutableStateOf(true) }
        var themeColor by rememberSaveable { mutableStateOf<Int?>(null) }
        var zoomVisible by rememberSaveable { mutableStateOf(true) }
        val choices = listOf(
            MapleChoice(getString(R.string.standalone_follow_autojs6), getString(R.string.standalone_follow_system)),
            MapleChoice(getString(R.string.standalone_follow_system)),
            MapleChoice(getString(R.string.standalone_dark_mode_light)),
            MapleChoice(getString(R.string.standalone_dark_mode_dark), enabled = false),
        )
        Scaffold(
            topBar = {
                MapleTopBar(
                    title = "Compose showcase",
                    subtitle = ThemeColorValue.hex(appPalette.primary),
                    navigationIcon = painterResource(R.drawable.ic_arrow_back_24),
                    navigationContentDescription = getString(R.string.action_back),
                    onNavigationClick = { finish() },
                )
            },
        ) { padding ->
            Column(modifier = Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState())) {
                MapleSectionHeader(getString(R.string.standalone_settings_section_appearance))
                MapleSettingsRow(
                    title = getString(R.string.standalone_settings_dark_mode),
                    summary = choices[choice].label,
                    iconResource = R.drawable.ic_settings_night,
                    onClick = { choiceOpen = true },
                )
                MapleHairline()
                MapleSettingsRow(
                    title = getString(R.string.standalone_settings_theme_color),
                    summary = themeColor?.let(ThemeColorValue::hex) ?: getString(R.string.standalone_follow_autojs6),
                    iconResource = R.drawable.ic_settings_theme,
                    onClick = { themeOpen = true },
                )
                MapleHairline()
                MapleSwitchRow(
                    title = getString(R.string.action_resume_animation),
                    summary = getString(R.string.launcher_icon_auto_note),
                    iconResource = R.drawable.ic_settings_tune,
                    checked = switch,
                    onToggle = { switch = it },
                )
                MapleSettingsRow(
                    title = getString(R.string.standalone_about_title),
                    summary = getString(R.string.standalone_about_app_summary),
                    iconResource = R.drawable.ic_info_24,
                    enabled = false,
                    onClick = {},
                )
                MapleSectionHeader("Overlay")
                Box(modifier = Modifier.fillMaxWidth().height(280.dp).background(MapleTheme.canvas.viewerCanvas)) {
                    MapleTopBar(
                        title = "photo.jpg",
                        subtitle = "3 / 12",
                        navigationIcon = painterResource(R.drawable.ic_arrow_back_24),
                        navigationContentDescription = getString(R.string.action_back),
                        onNavigationClick = { zoomVisible = !zoomVisible },
                        overlay = true,
                        modifier = Modifier.align(Alignment.TopCenter),
                    )
                    MapleZoomIndicator(
                        text = getString(R.string.zoom_indicator, "2.5"),
                        visible = zoomVisible,
                        modifier = Modifier.align(Alignment.TopEnd).padding(top = 80.dp, end = 16.dp),
                    )
                    MapleBottomActionBar(
                        actions = listOf(
                            MapleAction(painterResource(R.drawable.ic_info_outline), getString(R.string.label_details), { toast("details") }),
                            MapleAction(painterResource(R.drawable.ic_rotate_right), getString(R.string.label_rotate), { toast("rotate") }),
                            MapleAction(painterResource(R.drawable.ic_edit_24), getString(R.string.text_edit), { toast("edit") }, enabled = false),
                            MapleAction(painterResource(R.drawable.ic_share), getString(R.string.action_share), { toast("share") }),
                            MapleAction(painterResource(R.drawable.ic_open_in_new), getString(R.string.label_open_with), { toast("open") }),
                        ),
                        overlay = true,
                        modifier = Modifier.align(Alignment.BottomCenter),
                    )
                }
                MapleSectionHeader("Empty state")
                MapleEmptyState(
                    title = getString(R.string.error_cannot_read_image),
                    body = getString(R.string.standalone_home_scope),
                    icon = painterResource(R.drawable.ic_warning_24),
                    primaryAction = MapleEmptyAction(getString(R.string.action_view_image), onClick = { toast("primary") }),
                    secondaryActions = listOf(MapleEmptyAction(getString(R.string.action_open_external), onClick = { toast("secondary") })),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
        if (choiceOpen) {
            MapleChoiceDialog(
                title = getString(R.string.standalone_settings_dark_mode),
                options = choices,
                selectedIndex = choice,
                onDismissRequest = { choiceOpen = false },
                onConfirm = { choice = it; choiceOpen = false },
                confirmText = getString(R.string.dialog_button_confirm),
                dismissText = getString(R.string.dialog_button_cancel),
            )
        }
        if (themeOpen) {
            MapleThemeColorDialog(
                currentColor = themeColor,
                hostColor = 0xFFFFDEAD.toInt(),
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
                onDismissRequest = { themeOpen = false },
                onConfirm = { themeColor = it; themeOpen = false },
            )
        }
    }

    private fun toast(text: String) = Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
}
