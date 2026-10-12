package io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class EditorColorParsingTest {

    @Test
    fun parsesOpaqueAndTranslucentHexWithOptionalHash() {
        assertEquals(0xFFF44336.toInt(), parseEditorColor("#F44336"))
        assertEquals(0xFFF44336.toInt(), parseEditorColor("f44336"))
        assertEquals(0x80FF0000.toInt(), parseEditorColor("#80FF0000"))
        assertEquals(0xFF000000.toInt(), parseEditorColor("  #000000  "))
    }

    @Test
    fun rejectsOtherLengthsAndNonHexInput() {
        assertNull(parseEditorColor(""))
        assertNull(parseEditorColor("#FFF"))
        assertNull(parseEditorColor("#12345"))
        assertNull(parseEditorColor("#GGGGGG"))
        assertNull(parseEditorColor("#123456789"))
    }
}
