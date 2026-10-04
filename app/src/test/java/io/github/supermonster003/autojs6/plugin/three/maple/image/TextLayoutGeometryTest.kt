package io.github.supermonster003.autojs6.plugin.three.maple.image

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class TextLayoutGeometryTest {

    @Test
    fun singleLineCentersItsVisualFontBounds() {
        val plan = TextLayoutGeometry.plan(
            lineCount = 1,
            centerY = 50f,
            fontAscent = -8f,
            fontDescent = 2f,
        )

        assertEquals(10f, plan.blockHeight, 0.001f)
        assertEquals(45f, plan.baselines.single() - 8f, 0.001f)
        assertEquals(55f, plan.baselines.single() + 2f, 0.001f)
    }

    @Test
    fun multipleLinesCenterTheWholeBlockAndKeepStableSpacing() {
        val plan = TextLayoutGeometry.plan(
            lineCount = 3,
            centerY = 50f,
            fontAscent = -8f,
            fontDescent = 2f,
        )

        assertEquals(11.2f, plan.lineHeight, 0.001f)
        assertEquals(32.4f, plan.blockHeight, 0.001f)
        assertEquals(plan.lineHeight, plan.baselines[1] - plan.baselines[0], 0.001f)
        assertEquals(plan.lineHeight, plan.baselines[2] - plan.baselines[1], 0.001f)
        val visualTop = plan.baselines.first() - 8f
        val visualBottom = plan.baselines.last() + 2f
        assertEquals(50f, (visualTop + visualBottom) / 2f, 0.001f)
    }

    @Test
    fun lineSplitterPreservesIntentionalBlankLinesAndWindowsEndings() {
        assertEquals(
            listOf("first", "", "third"),
            StyledTextRenderer.textLines("first\r\n\r\nthird"),
        )
    }

    @Test
    fun invalidLayoutInputsFailBeforeDrawing() {
        assertThrows(IllegalArgumentException::class.java) {
            TextLayoutGeometry.plan(0, 0f, -8f, 2f)
        }
        assertThrows(IllegalArgumentException::class.java) {
            TextLayoutGeometry.plan(1, 0f, 2f, -8f)
        }
    }
}
