package io.github.supermonster003.autojs6.plugin.three.maple.image

import org.junit.Assert.assertEquals
import org.junit.Test

class BrushWidthMemoryTest {

    @Test
    fun updatingOneToolDoesNotChangeOtherRememberedWidths() {
        val initial = BrushWidthMemory()
        val updated = initial
            .withProgress(BrushTool.PEN, 3)
            .withProgress(BrushTool.HIGHLIGHTER, 19)
            .withProgress(BrushTool.MOSAIC, 27)
            .withProgress(BrushTool.ERASER, 11)

        assertEquals(3, updated.progress(BrushTool.PEN))
        assertEquals(19, updated.progress(BrushTool.HIGHLIGHTER))
        assertEquals(27, updated.progress(BrushTool.MOSAIC))
        assertEquals(11, updated.progress(BrushTool.ERASER))
        assertEquals(6, initial.progress(BrushTool.PEN))
        assertEquals(14, initial.progress(BrushTool.HIGHLIGHTER))
        assertEquals(18, initial.progress(BrushTool.MOSAIC))
        assertEquals(14, initial.progress(BrushTool.ERASER))
    }
}
