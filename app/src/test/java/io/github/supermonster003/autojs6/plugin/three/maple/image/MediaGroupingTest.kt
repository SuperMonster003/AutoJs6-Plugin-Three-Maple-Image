package io.github.supermonster003.autojs6.plugin.three.maple.image

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

/** ROADMAP 7.5: date sections, the random order and the grid positions around section headers. */
class MediaGroupingTest {

    private val utc = TimeZone.getTimeZone("UTC")

    private fun millis(year: Int, month: Int, day: Int, hour: Int = 12): Long = Calendar.getInstance(utc).run {
        clear()
        set(year, month - 1, day, hour, 0)
        timeInMillis
    }

    private fun item(id: Long, taken: Long?, modifiedMillis: Long = taken ?: 0L) = MediaItem(
        id = id,
        displayName = "i$id.jpg",
        mimeType = "image/jpeg",
        sizeBytes = 10,
        width = 1,
        height = 1,
        dateModifiedSeconds = modifiedMillis / 1000,
        dateTakenMillis = taken,
        bucketId = 1,
        bucketName = "Camera",
        relativePath = "DCIM/Camera/",
        contentUri = "content://media/external/images/media/$id",
    )

    private val newestFirst = listOf(
        item(1, millis(2026, 10, 14, 20)),
        item(2, millis(2026, 10, 14, 1)),
        item(3, millis(2026, 10, 13)),
        item(4, millis(2026, 9, 30)),
        item(5, millis(2025, 9, 30)),
    )

    @Test
    fun daysAndMonthsSplitASortedAlbum() {
        val sort = MediaSort(MediaSortField.DATE_TAKEN, ascending = false)
        assertEquals(
            listOf(MediaGroup(2026, 10, 14, 0, 2), MediaGroup(2026, 10, 13, 2, 1), MediaGroup(2026, 9, 30, 3, 1), MediaGroup(2025, 9, 30, 4, 1)),
            MediaCatalog.groups(newestFirst, sort, MediaGrouping.DAY, utc),
        )
        assertEquals(
            listOf(MediaGroup(2026, 10, 0, 0, 3), MediaGroup(2026, 9, 0, 3, 1), MediaGroup(2025, 9, 0, 4, 1)),
            MediaCatalog.groups(newestFirst, sort, MediaGrouping.MONTH, utc),
        )
        // The time zone decides the calendar day: 01:00 UTC on the 14th is still the 13th in New York.
        val newYork = MediaCatalog.groups(newestFirst.take(2), sort, MediaGrouping.DAY, TimeZone.getTimeZone("America/New_York"))
        assertEquals(listOf(14, 13), newYork.map { it.day })
    }

    @Test
    fun groupsFollowTheSortedDateOnly() {
        val byModified = listOf(item(1, taken = null, modifiedMillis = millis(2026, 1, 2)), item(2, taken = millis(2020, 1, 1), modifiedMillis = millis(2026, 1, 2)))
        assertEquals(1, MediaCatalog.groups(byModified, MediaSort(MediaSortField.DATE_MODIFIED), MediaGrouping.DAY, utc).size)
        assertEquals(2, MediaCatalog.groups(byModified, MediaSort(MediaSortField.DATE_TAKEN), MediaGrouping.DAY, utc).size)
        for (field in listOf(MediaSortField.NAME, MediaSortField.SIZE, MediaSortField.RANDOM)) {
            assertTrue(MediaCatalog.groups(newestFirst, MediaSort(field), MediaGrouping.DAY, utc).isEmpty())
        }
        assertTrue(MediaCatalog.groups(newestFirst, MediaSort(MediaSortField.DATE_TAKEN), MediaGrouping.NONE, utc).isEmpty())
        assertTrue(MediaCatalog.groups(emptyList(), MediaSort(MediaSortField.DATE_TAKEN), MediaGrouping.DAY, utc).isEmpty())
    }

    @Test
    fun aRandomOrderDependsOnlyOnItsSeed() {
        val items = (1L..50L).map { item(it, millis(2026, 1, 1)) }
        val first = MediaCatalog.sort(items, MediaSort(MediaSortField.RANDOM, seed = 7))
        assertEquals(first, MediaCatalog.sort(items.reversed(), MediaSort(MediaSortField.RANDOM, seed = 7)))
        assertNotEquals(first, MediaCatalog.sort(items, MediaSort(MediaSortField.RANDOM, seed = 8)))
        assertEquals(items.toSet(), first.toSet())
    }

    @Test
    fun gridPositionsSkipTheHeaders() {
        val groups = listOf(MediaGroup(2026, 10, 14, 0, 2), MediaGroup(2026, 10, 13, 2, 1), MediaGroup(2026, 9, 30, 3, 2))
        val layout = MediaGroupLayout(groups, itemCount = 5)
        // Grid: [H0, i0, i1, H1, i2, H2, i3, i4]
        assertEquals(8, layout.gridCount)
        assertEquals(listOf(1, 2, 4, 6, 7), (0 until 5).map(layout::gridIndex))
        assertEquals(listOf(null, 0, 1, null, 2, null, 3, 4), (0 until 8).map(layout::itemIndex))
        assertEquals(groups[0], layout.groupAt(0))
        assertEquals(groups[0], layout.groupAt(2))
        assertEquals(groups[1], layout.groupAt(3))
        assertEquals(groups[2], layout.groupAt(7))
        val flat = MediaGroupLayout(emptyList(), itemCount = 3)
        assertEquals(3, flat.gridCount)
        assertEquals(2, flat.gridIndex(2))
        assertEquals(1, flat.itemIndex(1))
        assertNull(flat.groupAt(0))
    }
}
