package io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
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
        // Small phones (G8441, 360 x 640 dp) with the keyboard up keep the switch below the scrolling viewport.
        compose.onNodeWithTag("maple.editor.text.outline").performScrollTo().performClick()
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
                EditorSaveDialog(state = state, onDismissRequest = {}, onConfirm = {})
            }
        }
        compose.onNodeWithTag("maple.chip.0").assertIsSelected()
        compose.onNodeWithTag("maple.chip.3").performScrollTo().performClick()
        compose.waitForIdle()
        assertEquals(3, state.formatIndex)
        assertEquals(1, changes)
        compose.onNodeWithTag("maple.chip.3").assertIsSelected()
        compose.onNodeWithTag("maple.editor.save.lossless").assertDoesNotExist()
        state.losslessAvailable = true
        compose.waitForIdle()
        compose.onNodeWithTag("maple.editor.save.lossless").performScrollTo().performClick()
        compose.waitForIdle()
        assertTrue(state.webpLossless)
        assertEquals(2, changes)
    }

    @Test
    fun overwriteLocksTheFormatAndTurnsOnTheCaptureDetailsUntilTheyAreTouched() {
        val state = EditorSaveDialogState(
            formatLabels = listOf("Follow source (JPEG)", "JPEG", "PNG", "WebP"),
            formatIndex = 2,
            quality = 92,
            webpLossless = false,
            overwriteAvailable = true,
            keepExifAvailable = true,
        )
        state.fileNote = "New file: photo_edited.png"
        compose.setContent {
            MapleTheme(palette = testPalette()) {
                EditorSaveDialog(state = state, onDismissRequest = {}, onConfirm = {})
            }
        }
        compose.onNodeWithTag("maple.editor.save.fileNote").assertTextEquals("New file: photo_edited.png")
        compose.onNodeWithTag("maple.editor.save.overwrite").performScrollTo().performClick()
        compose.waitForIdle()
        assertTrue(state.overwrite)
        assertTrue("capture details follow an overwrite", state.keepExif)
        // The format shows the original's own and cannot change.
        compose.onNodeWithTag("maple.chip.0").assertIsSelected().assertIsNotEnabled()
        compose.onNodeWithTag("maple.chip.2").assertIsNotEnabled()
        compose.onNodeWithTag("maple.editor.save.overwrite").performClick()
        compose.waitForIdle()
        assertFalse(state.keepExif)
        compose.onNodeWithTag("maple.chip.2").assertIsSelected().assertIsEnabled()
        // Once the user decided about the details, overwriting leaves them alone.
        compose.onNodeWithTag("maple.editor.save.keepExif").performScrollTo().performClick()
        compose.onNodeWithTag("maple.editor.save.keepExif").performClick()
        compose.onNodeWithTag("maple.editor.save.overwrite").performClick()
        compose.waitForIdle()
        assertTrue(state.overwrite)
        assertFalse(state.keepExif)
    }

    @Test
    fun overwriteAndCaptureDetailsAreHiddenWhenTheyDoNotApply() {
        val state = EditorSaveDialogState(formatLabels = listOf("Follow source (PNG)", "JPEG", "PNG", "WebP"), formatIndex = 0, quality = 92, webpLossless = false)
        compose.setContent {
            MapleTheme(palette = testPalette()) {
                EditorSaveDialog(state = state, onDismissRequest = {}, onConfirm = {})
            }
        }
        compose.onNodeWithTag("maple.editor.save.overwrite").assertDoesNotExist()
        compose.onNodeWithTag("maple.editor.save.keepExif").assertDoesNotExist()
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
