package io.github.supermonster003.autojs6.plugin.three.maple.image

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EditorResizePolicyTest {

    private val source = ImagePixelSize(4000, 3000)
    private val budget = 20_000_000L

    @Test fun theDefaultIsHalfTheSizeWithTheLongEdgeCappedAtTheConverterDefault() {
        val draft = EditorResizePolicy.defaultDraft(source)
        assertEquals(ImageResizeMode.PERCENTAGE, draft.mode)
        assertEquals(ImagePixelSize(2000, 1500), EditorResizePolicy.resolve(source, draft, budget).size)
        assertEquals("1920", draft.longEdge)
        assertEquals("4000", draft.width)
        assertEquals("3000", draft.height)
        assertTrue(draft.lockAspectRatio)
    }

    @Test fun aSmallImageStartsWithItsOwnLongEdge() {
        assertEquals("640", EditorResizePolicy.defaultDraft(ImagePixelSize(640, 480)).longEdge)
    }

    @Test fun eachModeResolvesFromItsOwnField() {
        val draft = EditorResizePolicy.defaultDraft(source)
        assertEquals(
            ImagePixelSize(1000, 750),
            EditorResizePolicy.resolve(source, draft.copy(mode = ImageResizeMode.LONG_EDGE, longEdge = " 1000 "), budget).size,
        )
        assertEquals(
            ImagePixelSize(800, 300),
            EditorResizePolicy.resolve(source, draft.copy(mode = ImageResizeMode.CUSTOM, width = "800", height = "300"), budget).size,
        )
        assertEquals(
            ImagePixelSize(1000, 750),
            EditorResizePolicy.resolve(source, draft.copy(percentage = "25"), budget).size,
        )
    }

    @Test fun invalidInputKeepsTheConverterErrors() {
        val draft = EditorResizePolicy.defaultDraft(source)
        assertEquals(ImageResizeError.INVALID_PERCENTAGE, EditorResizePolicy.resolve(source, draft.copy(percentage = ""), budget).error)
        assertEquals(ImageResizeError.INVALID_PERCENTAGE, EditorResizePolicy.resolve(source, draft.copy(percentage = "0"), budget).error)
        assertEquals(
            ImageResizeError.INVALID_LONG_EDGE,
            EditorResizePolicy.resolve(source, draft.copy(mode = ImageResizeMode.LONG_EDGE, longEdge = "abc"), budget).error,
        )
        assertEquals(
            ImageResizeError.INVALID_WIDTH,
            EditorResizePolicy.resolve(source, draft.copy(mode = ImageResizeMode.CUSTOM, width = "-1"), budget).error,
        )
    }

    @Test fun moreThanTheEditingBudgetIsRefusedWithTheMemoryMessage() {
        val draft = EditorResizePolicy.defaultDraft(source).copy(percentage = "150")
        val result = EditorResizePolicy.resolve(source, draft, budget)
        assertFalse(result.isValid)
        assertNull(result.size)
        assertEquals(ImageResizeError.MEMORY_BUDGET_EXCEEDED, result.error)
        assertTrue(EditorResizePolicy.resolve(source, draft, 30_000_000L).isValid)
    }

    @Test fun aLockedRatioMovesTheOtherSide() {
        val draft = EditorResizePolicy.defaultDraft(source).copy(mode = ImageResizeMode.CUSTOM)
        val wider = EditorResizePolicy.withWidth(source, draft, "2000")
        assertEquals("2000", wider.width)
        assertEquals("1500", wider.height)
        val taller = EditorResizePolicy.withHeight(source, draft, "600")
        assertEquals("800", taller.width)
        assertEquals("600", taller.height)
        // Text that is not a number yet leaves the other side alone.
        assertEquals("3000", EditorResizePolicy.withWidth(source, draft, "").height)
    }

    @Test fun anUnlockedRatioLeavesTheOtherSideAndLockingAgainFollowsTheWidth() {
        val unlocked = EditorResizePolicy.withLock(source, EditorResizePolicy.defaultDraft(source).copy(mode = ImageResizeMode.CUSTOM), false)
        val stretched = EditorResizePolicy.withWidth(source, unlocked, "1000")
        assertEquals("3000", stretched.height)
        val locked = EditorResizePolicy.withLock(source, stretched, true)
        assertTrue(locked.lockAspectRatio)
        assertEquals("1000", locked.width)
        assertEquals("750", locked.height)
    }
}
