package io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import io.github.supermonster003.autojs6.plugin.three.maple.image.AppThemePalette
import io.github.supermonster003.autojs6.plugin.three.maple.image.R
import io.github.supermonster003.autojs6.plugin.three.maple.image.ThemeAccentRoles
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/** Three settings specification section 4: 72 dp rows, whole-row targets, one toggle per tap. */
class MapleSettingsRowTest {

    @get:Rule
    val compose = createComposeRule()

    @Test
    fun switchRowTogglesOncePerTapAndExposesSwitchState() {
        var checked by mutableStateOf(false)
        var toggles = 0
        compose.setContent {
            MapleTheme(palette = palette()) {
                MapleSwitchRow(
                    title = "Resume",
                    summary = "Summary",
                    iconResource = R.drawable.ic_settings_tune,
                    checked = checked,
                    onToggle = { checked = it; toggles++ },
                    modifier = androidx.compose.ui.Modifier.testTag("row"),
                )
            }
        }
        compose.onNodeWithTag("row").assertIsOff().assertHeightIsAtLeast(72.dp)
        compose.onNodeWithTag("row").performClick()
        compose.onNodeWithTag("row").assertIsOn()
        assertEquals(1, toggles)
        compose.onNodeWithTag("row").performClick()
        compose.onNodeWithTag("row").assertIsOff()
        assertEquals(2, toggles)
    }

    @Test
    fun settingsRowIsOneClickTargetWithMinimumHeights() {
        var clicks = 0
        compose.setContent {
            MapleTheme(palette = palette()) {
                androidx.compose.foundation.layout.Column {
                    MapleSettingsRow(
                        title = "Language",
                        summary = "Follow AutoJs6",
                        iconResource = R.drawable.ic_settings_language,
                        onClick = { clicks++ },
                        modifier = androidx.compose.ui.Modifier.testTag("double"),
                    )
                    MapleSettingsRow(
                        title = "Single",
                        onClick = { clicks++ },
                        modifier = androidx.compose.ui.Modifier.testTag("single"),
                    )
                }
            }
        }
        compose.onNodeWithTag("double").assertHeightIsAtLeast(72.dp)
        compose.onNodeWithTag("single").assertHeightIsAtLeast(56.dp)
        compose.onNodeWithText("Language").performClick()
        compose.onNodeWithTag("single").performClick()
        assertEquals(2, clicks)
    }

    private fun palette(): AppThemePalette {
        val seed = 0xFFFFDEAD.toInt()
        val roles = ThemeAccentRoles.fromSeed(seed, true)
        return AppThemePalette(
            seed = seed, primary = roles.primary, onPrimary = roles.onPrimary, accent = 0xFF9CCAFF.toInt(),
            windowBackground = 0xFF121212.toInt(), surface = 0xFF1E1E1E.toInt(), surfaceVariant = 0xFF1E1E1E.toInt(),
            outline = 0xFF777A82.toInt(), primaryText = 0xFFE6E1E5.toInt(), secondaryText = 0xFFB9BAC0.toInt(),
            divider = 0xFF34363A.toInt(), isDark = true,
        )
    }
}
