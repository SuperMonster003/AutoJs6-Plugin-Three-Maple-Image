package io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** Editor dialogs: the drafts live outside the composition and every confirm goes through the caller. */
class EditorDialogsTest {

    @get:Rule
    val compose = createComposeRule()

    @Test
    fun textDialogEnablesConfirmOnceTextIsTypedAndKeepsTheDraft() {
        val state = EditorTextDialogState(text = "", color = 0xFFFFFFFF.toInt(), sizeProgress = 4, outline = false, shadow = false, rotationProgress = 180)
        var confirmed = 0
        compose.setContent {
            MapleTheme(palette = testPalette()) {
                EditorTextDialog(
                    state = state,
                    sizeLabel = { "${it + 2}%" },
                    rotationLabel = { "${it - 180}" },
                    onPickColor = {},
                    onDismissRequest = {},
                    onConfirm = { confirmed++ },
                )
            }
        }
        compose.onNodeWithTag("maple.dialog.confirm").assertIsNotEnabled()
        compose.onNodeWithTag("maple.editor.text.input").performTextInput("Maple")
        compose.onNodeWithTag("maple.editor.text.outline").performClick()
        compose.waitForIdle()
        assertEquals("Maple", state.text)
        assertTrue(state.outline)
        compose.onNodeWithTag("maple.editor.text.outline").assertIsOn()
        compose.onNodeWithTag("maple.dialog.confirm").assertIsEnabled().performClick()
        assertEquals(1, confirmed)
        compose.onNodeWithTag("maple.editor.text.input").performTextClearance()
        compose.waitForIdle()
        assertEquals("", state.text)
        compose.onNodeWithTag("maple.dialog.confirm").assertIsNotEnabled()
    }

    @Test
    fun saveDialogSelectsFormatsThroughWrappingChipsAndReportsChanges() {
        val state = EditorSaveDialogState(formatLabels = listOf("Follow source (JPEG)", "JPEG", "PNG", "WebP"), formatIndex = 0, quality = 92, webpLossless = false)
        var changes = 0
        state.onChanged = { changes++ }
        compose.setContent {
            MapleTheme(palette = testPalette()) {
                EditorSaveDialog(state = state, hint = "hint", onDismissRequest = {}, onConfirm = {})
            }
        }
        compose.onNodeWithTag("maple.chip.0").assertIsSelected()
        compose.onNodeWithTag("maple.chip.3").performClick()
        compose.waitForIdle()
        assertEquals(3, state.formatIndex)
        assertEquals(1, changes)
        compose.onNodeWithTag("maple.chip.3").assertIsSelected()
        compose.onNodeWithTag("maple.editor.save.lossless").assertDoesNotExist()
        state.losslessAvailable = true
        compose.waitForIdle()
        compose.onNodeWithTag("maple.editor.save.lossless").performClick()
        compose.waitForIdle()
        assertTrue(state.webpLossless)
        assertEquals(2, changes)
    }

    @Test
    fun colorDialogAcceptsPresetsAndRejectsInvalidInput() {
        val state = EditorColorDialogState("#FFFFFFFF")
        var confirmed = 0
        compose.setContent {
            MapleTheme(palette = testPalette()) {
                EditorColorDialog(state = state, onDismissRequest = {}, onConfirm = { confirmed++ })
            }
        }
        compose.onNodeWithTag("maple.editor.color.#F44336").performClick()
        compose.waitForIdle()
        assertEquals("#FFF44336", state.input)
        assertEquals(0xFFF44336.toInt(), parseEditorColor(state.input))
        compose.onNodeWithTag("maple.dialog.confirm").assertIsEnabled()
        compose.onNodeWithTag("maple.editor.color.input").performTextClearance()
        compose.onNodeWithTag("maple.editor.color.input").performTextInput("#12")
        compose.waitForIdle()
        assertFalse(state.error)
        compose.onNodeWithTag("maple.dialog.confirm").assertIsNotEnabled()
        assertEquals(0, confirmed)
    }
}
