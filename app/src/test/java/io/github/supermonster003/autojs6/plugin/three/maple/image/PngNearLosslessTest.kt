package io.github.supermonster003.autojs6.plugin.three.maple.image

import java.util.Random
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class PngNearLosslessTest {

    @Test
    fun qualityOneHundredIsLosslessAndEveryTenPointsBelowAreOneLevel() {
        assertNull(PngLossyPolicy.level(100))
        assertEquals(PngLossyLevel(maxTolerance = 1, smoothTolerance = 1), PngLossyPolicy.level(99))
        assertEquals(PngLossyPolicy.level(99), PngLossyPolicy.level(90))
        assertEquals(PngLossyLevel(maxTolerance = 2, smoothTolerance = 1), PngLossyPolicy.level(89))
        assertEquals(PngLossyLevel(maxTolerance = 16, smoothTolerance = 8), PngLossyPolicy.level(9))
        assertEquals(PngLossyPolicy.level(9), PngLossyPolicy.level(1))
        assertThrows(IllegalArgumentException::class.java) { PngLossyPolicy.level(0) }
        assertThrows(IllegalArgumentException::class.java) { PngLossyPolicy.level(101) }
    }

    @Test
    fun lowerQualityNeverTightensATolerance() {
        var previous = PngLossyLevel(1, 1)
        for (quality in 99 downTo 1) {
            val level = requireNotNull(PngLossyPolicy.level(quality))
            assertTrue("quality $quality", level.maxTolerance >= previous.maxTolerance)
            assertTrue("quality $quality", level.smoothTolerance >= previous.smoothTolerance)
            previous = level
        }
    }

    @Test
    fun distinctQualitiesNameEachLevelOnceFromTheBestDown() {
        val qualities = PngLossyPolicy.DISTINCT_QUALITIES
        assertEquals(listOf(100, 99, 89, 79, 69, 59, 49, 39, 29, 19, 9), qualities)
        assertEquals(qualities.size - 1, qualities.drop(1).map(PngLossyPolicy::level).toSet().size)
        assertEquals(1.0, PngLossyPolicy.estimatedSizeFactor(100), 0.0)
        for (index in 1 until qualities.size - 1) {
            assertTrue(PngLossyPolicy.estimatedSizeFactor(qualities[index]) > PngLossyPolicy.estimatedSizeFactor(qualities[index + 1]))
        }
    }

    @Test
    fun flatPixelsAreNeverChangedAndTheLimitStaysBelowTheLocalContrast() {
        val level = PngLossyLevel(maxTolerance = 8, smoothTolerance = 3)
        assertEquals(0, level.toleranceForActivity(0))
        // Up to the smooth tolerance a pixel may meet its neighbors, but go no further.
        assertEquals(1, level.toleranceForActivity(1))
        assertEquals(3, level.toleranceForActivity(3))
        // Beyond it the limit is half the contrast, never less than the smooth tolerance, never more than the maximum.
        assertEquals(3, level.toleranceForActivity(4))
        assertEquals(3, level.toleranceForActivity(7))
        assertEquals(4, level.toleranceForActivity(8))
        assertEquals(8, level.toleranceForActivity(16))
        assertEquals(8, level.toleranceForActivity(255))
        for (activity in 4..255) assertTrue(level.toleranceForActivity(activity) < activity)
    }

    @Test
    fun levelsRejectTolerancesOutsideTheirRange() {
        assertThrows(IllegalArgumentException::class.java) { PngLossyLevel(maxTolerance = 0, smoothTolerance = 0) }
        assertThrows(IllegalArgumentException::class.java) { PngLossyLevel(maxTolerance = 17, smoothTolerance = 1) }
        assertThrows(IllegalArgumentException::class.java) { PngLossyLevel(maxTolerance = 2, smoothTolerance = 3) }
        assertNotNull(PngLossyLevel(maxTolerance = 16, smoothTolerance = 16))
    }

    @Test
    fun everyChannelStaysWithinThePixelsOwnToleranceAndAlphaIsUntouched() {
        val width = 61
        val height = 47
        val random = Random(20261117L)
        val source = Array(height) { y ->
            IntArray(width) { x ->
                when {
                    // A flat band, a gentle ramp, noise and hard edges, with every kind of alpha.
                    y < 8 -> 0xFF336699.toInt()
                    y < 16 -> argb(255, 40 + x, 40 + x / 2, 200 - x)
                    y < 32 -> argb(if (x % 7 == 0) random.nextInt(256) else 255, random.nextInt(256), random.nextInt(256), random.nextInt(256))
                    else -> if ((x / 5 + y / 3) % 2 == 0) 0xFF000000.toInt() else 0xFFFFFFFF.toInt()
                }
            }
        }
        for (quality in listOf(99, 79, 49, 9, 1)) {
            val level = requireNotNull(PngLossyPolicy.level(quality))
            val filter = PngNearLosslessFilter(width, level)
            for (y in 0 until height) {
                val above = source.getOrNull(y - 1)
                val below = source.getOrNull(y + 1)
                filter.process(above, source[y], below)
                assertTrue(filter.filterType in PngNearLosslessFilter.FILTER_SUB..PngNearLosslessFilter.FILTER_PAETH)
                for (x in 0 until width) {
                    val pixel = source[y][x]
                    var activity = 0
                    if (x > 0) activity = maxOf(activity, PngNearLosslessFilter.difference(pixel, source[y][x - 1]))
                    if (x < width - 1) activity = maxOf(activity, PngNearLosslessFilter.difference(pixel, source[y][x + 1]))
                    if (above != null) activity = maxOf(activity, PngNearLosslessFilter.difference(pixel, above[x]))
                    if (below != null) activity = maxOf(activity, PngNearLosslessFilter.difference(pixel, below[x]))
                    val allowed = level.toleranceForActivity(activity)
                    val result = filter.argb(x, pixel)
                    assertEquals("alpha at $x,$y", pixel ushr 24, result ushr 24)
                    for (shift in intArrayOf(16, 8, 0)) {
                        val before = (pixel ushr shift) and 0xFF
                        val after = (result ushr shift) and 0xFF
                        assertTrue("quality $quality at $x,$y: $before -> $after allows $allowed", kotlin.math.abs(before - after) <= allowed)
                    }
                    assertEquals((result ushr 16) and 0xFF, filter.red(x))
                    assertEquals((result ushr 8) and 0xFF, filter.green(x))
                    assertEquals(result and 0xFF, filter.blue(x))
                }
            }
        }
    }

    @Test
    fun theSameRowsAlwaysReconstructTheSameWayAndResetStartsOver() {
        val width = 33
        val random = Random(7L)
        val rows = Array(12) { IntArray(width) { 0xFF000000.toInt() or random.nextInt(0x1000000) } }
        val level = PngLossyLevel(maxTolerance = 6, smoothTolerance = 3)
        val filter = PngNearLosslessFilter(width, level)

        /** Each row's pixels followed by the filter type the row was given. */
        fun run(with: PngNearLosslessFilter): List<IntArray> = rows.indices.map { y ->
            with.process(rows.getOrNull(y - 1), rows[y], rows.getOrNull(y + 1))
            IntArray(width) { x -> with.argb(x, rows[y][x]) } + with.filterType
        }

        val first = run(filter)
        filter.reset()
        val afterReset = run(filter)
        val fresh = run(PngNearLosslessFilter(width, level))
        first.indices.forEach {
            assertArrayEquals("row $it after reset", first[it], afterReset[it])
            assertArrayEquals("row $it with a new filter", first[it], fresh[it])
        }
    }

    @Test
    fun paethAndDifferenceFollowThePngDefinitions() {
        assertEquals(10, PngNearLosslessFilter.paeth(left = 0, up = 10, upLeft = 0))
        assertEquals(20, PngNearLosslessFilter.paeth(left = 20, up = 0, upLeft = 0))
        // The estimate 30 + 40 - 35 = 35 is nearest to the upper left neighbor.
        assertEquals(35, PngNearLosslessFilter.paeth(left = 30, up = 40, upLeft = 35))
        // Ties go to the left neighbor first, then to the upper one.
        assertEquals(10, PngNearLosslessFilter.paeth(left = 10, up = 10, upLeft = 10))
        assertEquals(0, PngNearLosslessFilter.difference(0x80112233.toInt(), 0x80112233.toInt()))
        assertEquals(0x7F, PngNearLosslessFilter.difference(0xFF112233.toInt(), 0x80112233.toInt()))
        assertEquals(5, PngNearLosslessFilter.difference(0xFF102030.toInt(), 0xFF152030.toInt()))
        assertEquals(9, PngNearLosslessFilter.difference(0xFF102030.toInt(), 0xFF112939.toInt()))
    }

    private fun argb(alpha: Int, red: Int, green: Int, blue: Int): Int =
        (alpha shl 24) or ((red and 0xFF) shl 16) or ((green and 0xFF) shl 8) or (blue and 0xFF)
}
