package io.github.supermonster003.autojs6.plugin.three.maple.image

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Settings page after the Compose migration: every row is present and dialogs never save on cancel. */
@RunWith(AndroidJUnit4::class)
class AppSettingsComposeInstrumentationTest {

    @get:Rule
    internal val compose = createAndroidComposeRule<AppSettingsActivity>()

    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private lateinit var original: ApplicationSettings

    @Before fun remember() { original = ApplicationSettingsStore(context).load() }

    @After fun restore() { ApplicationSettingsStore(context).save(original) }

    @Test fun showsEveryAppearanceAndInformationRow() {
        for (resource in listOf(
            R.string.standalone_settings_language, R.string.standalone_settings_dark_mode,
            R.string.standalone_settings_theme_color, R.string.launcher_icon_title,
            R.string.standalone_release_history_title, R.string.standalone_about_title,
        )) {
            compose.onNodeWithText(context.getString(resource)).assertIsDisplayed()
        }
    }

    @Test fun cancellingTheDarkModeDialogKeepsTheStoredSettings() {
        compose.onNodeWithTag("maple.settings.darkMode").performClick()
        compose.onNodeWithTag("maple.choice.3").performClick()
        compose.onNodeWithTag("maple.dialog.cancel").performClick()
        compose.waitForIdle()
        assertEquals(original, ApplicationSettingsStore(context).load())
        compose.onNodeWithTag("maple.settings.darkMode").assertIsDisplayed()
    }

    @Test fun cancellingTheThemeColorDialogKeepsTheStoredSettings() {
        compose.onNodeWithTag("maple.settings.themeColor").performClick()
        compose.onNodeWithTag("maple.theme.preset.#F44336").performClick()
        compose.onNodeWithTag("maple.dialog.cancel").performClick()
        compose.waitForIdle()
        assertEquals(original, ApplicationSettingsStore(context).load())
    }
}
