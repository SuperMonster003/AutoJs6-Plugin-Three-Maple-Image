package io.github.supermonster003.autojs6.plugin.three.maple.image

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class SlideshowPlanTest {

    private fun plan(count: Int, start: Int, settings: SlideshowSettings, eligible: (Int) -> Boolean = { true }, seed: Int = 7) =
        SlideshowPlan(count, start, eligible, settings, Random(seed))

    private fun SlideshowPlan.drain(limit: Int = 50): List<Int> = buildList {
        while (size < limit) add(next() ?: break)
    }

    @Test fun inOrderItStopsAtTheLastPage() {
        assertEquals(listOf(3, 4), plan(5, 2, SlideshowSettings()).drain())
    }

    @Test fun backwardsItStopsAtTheFirstPage() {
        assertEquals(listOf(1, 0), plan(5, 2, SlideshowSettings(reverse = true)).drain())
    }

    @Test fun aLoopWrapsAroundBothWays() {
        assertEquals(listOf(3, 4, 0, 1, 2, 3), plan(5, 2, SlideshowSettings(loop = true)).drain(6))
        assertEquals(listOf(1, 0, 4, 3, 2, 1), plan(5, 2, SlideshowSettings(loop = true, reverse = true)).drain(6))
    }

    @Test fun leftOutPagesAreSkipped() {
        val gifs = setOf(1, 3)
        assertEquals(listOf(2, 4), plan(5, 0, SlideshowSettings(), eligible = { it !in gifs }).drain())
        // Starting on a left-out page is fine: the show moves on to the next one it may show.
        assertEquals(listOf(2, 4), plan(5, 1, SlideshowSettings(), eligible = { it !in gifs }).drain())
    }

    @Test fun nothingElseToShowEndsAtOnce() {
        val alone = plan(3, 1, SlideshowSettings(loop = true), eligible = { it == 1 })
        assertFalse(alone.hasNext)
        assertNull(alone.next())
        assertNull(plan(1, 0, SlideshowSettings(loop = true)).next())
    }

    @Test fun randomShowsEveryPageOnceThenEnds() {
        val order = plan(6, 2, SlideshowSettings(order = SlideshowOrder.RANDOM)).drain()
        assertEquals(setOf(0, 1, 3, 4, 5), order.toSet())
        assertEquals(5, order.size)
    }

    @Test fun aRandomLoopNeverShowsTheSamePageTwiceInARow() {
        val order = plan(4, 0, SlideshowSettings(order = SlideshowOrder.RANDOM, loop = true), seed = 3).drain(40)
        assertEquals(40, order.size)
        (listOf(0) + order).zipWithNext().forEach { (a, b) -> assertNotEquals(a, b) }
        assertTrue(order.toSet() == setOf(0, 1, 2, 3))
    }

    @Test fun movingByHandContinuesFromThere() {
        val inOrder = plan(6, 0, SlideshowSettings())
        inOrder.moveTo(4)
        assertEquals(listOf(5), inOrder.drain())
        val shuffled = plan(5, 0, SlideshowSettings(order = SlideshowOrder.RANDOM))
        shuffled.moveTo(3)
        val rest = shuffled.drain()
        assertFalse(3 in rest)
        assertEquals(setOf(1, 2, 4), rest.toSet())
    }

    @Test(expected = IllegalArgumentException::class) fun theIntervalIsBounded() {
        SlideshowSettings(intervalSeconds = SlideshowSettings.MAX_INTERVAL_SECONDS + 1)
    }
}
