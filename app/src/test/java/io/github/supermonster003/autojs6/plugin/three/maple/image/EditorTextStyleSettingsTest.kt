package io.github.supermonster003.autojs6.plugin.three.maple.image

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class EditorTextStyleSettingsTest {

    @Test
    fun uiStateKeepsLastConfirmedStyleSeparateFromAnOpenDialogDraft() {
        val remembered = EditorTextStyleSettings(
            selectedColor = 0xFF102030.toInt(),
            sizeProgress = 8,
            outlineEnabled = true,
            shadowEnabled = false,
            rotationDegrees = 24,
        )
        val draftStyle = remembered.copy(shadowEnabled = true, rotationDegrees = -81)
        val state = EditorUiState(
            textStyleSettings = remembered,
            textDialogDraft = EditorTextDialogDraft("first\nsecond", draftStyle),
        )

        assertEquals(remembered, state.textStyleSettings)
        assertEquals(draftStyle, state.textDialogDraft?.style)
        assertEquals("first\nsecond", state.textDialogDraft?.text)
    }

    @Test
    fun styleRejectsValuesOutsideTheSupportedControls() {
        assertThrows(IllegalArgumentException::class.java) {
            EditorTextStyleSettings(sizeProgress = -1)
        }
        assertThrows(IllegalArgumentException::class.java) {
            EditorTextStyleSettings(rotationDegrees = 181)
        }
    }
}
