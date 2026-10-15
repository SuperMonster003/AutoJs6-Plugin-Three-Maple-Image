package io.github.supermonster003.autojs6.plugin.three.maple.image

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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
            R.string.standalone_settings_theme_color, R.string.launcher_icon_title, R.string.viewer_rotation_gesture_title,
            R.string.standalone_release_history_title, R.string.standalone_about_title,
        )) {
            compose.onNodeWithText(context.getString(resource)).performScrollTo().assertIsDisplayed()
        }
    }

    /** ROADMAP 9.2: the groups follow the agreed order, and a screen reader can jump between them as headings. */
    @Test fun theGroupsComeInTheAgreedOrderAsHeadings() {
        val headings = compose.onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsProperties.Heading)).fetchSemanticsNodes()
            .sortedBy { it.positionInRoot.y }
            .map { it.config[SemanticsProperties.Text].joinToString() }
        val expected = listOf(
            R.string.standalone_settings_section_appearance, R.string.settings_section_grid, R.string.standalone_settings_section_viewer,
            R.string.settings_section_edit_output, R.string.settings_section_media_management, R.string.standalone_settings_section_storage,
            R.string.settings_section_about,
        ).map(context::getString)
        assertEquals(expected, headings)
    }

    /** Every row is one stop for a screen reader: its title read out, one tap (or toggle) to act, nothing nested. */
    @Test fun everyRowIsOneScreenReaderStop() {
        val rows = compose.onAllNodes(SemanticsMatcher("a settings row") { node ->
            node.config.getOrNull(SemanticsProperties.TestTag)?.startsWith("maple.settings.") == true
        }).fetchSemanticsNodes()
        assertTrue("rows: ${rows.size}", rows.size >= 30)
        for (row in rows) {
            val tag = row.config[SemanticsProperties.TestTag]
            val text = row.config.getOrNull(SemanticsProperties.Text).orEmpty().joinToString()
            assertTrue("$tag reads its title", text.isNotBlank())
            assertTrue("$tag acts on a tap", row.config.contains(SemanticsActions.OnClick))
            assertTrue("$tag is one merged stop", row.config.isMergingSemanticsOfDescendants)
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
