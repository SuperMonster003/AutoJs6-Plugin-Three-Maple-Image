package io.github.supermonster003.autojs6.plugin.three.maple.image

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class GalleryPreferencesTest {

    private val preferences = GalleryPreferences(RuntimeEnvironment.getApplication())

    @Test
    fun columnsDefaultToFourAndStayWithinThreeToSix() {
        assertEquals(4, preferences.mediaColumns)
        preferences.mediaColumns = 6
        assertEquals(6, preferences.mediaColumns)
        preferences.mediaColumns = 1
        assertEquals(3, preferences.mediaColumns)
        preferences.mediaColumns = 99
        assertEquals(6, preferences.mediaColumns)
        assertEquals(5, GalleryPreferences.clampColumns(5))
    }

    @Test
    fun thisAlbumOnlyKeepsTheSortForOneAlbumOtherwiseItIsTheDefault() {
        assertEquals(MediaSort.DEFAULT, preferences.defaultSort)
        assertEquals(MediaGrouping.NONE, preferences.defaultGrouping)
        assertNull(preferences.sortFor(AlbumKind.MEDIA, 42L))

        val byName = MediaSort(MediaSortField.NAME, ascending = true)
        preferences.applySort(AlbumKind.MEDIA, 42L, byName, MediaGrouping.NONE, thisAlbumOnly = true)
        assertEquals(byName, preferences.sortFor(AlbumKind.MEDIA, 42L))
        assertEquals(MediaGrouping.NONE, preferences.groupingFor(AlbumKind.MEDIA, 42L))
        assertTrue(preferences.hasOwnSort(AlbumKind.MEDIA, 42L))
        assertEquals("the default stays", MediaSort.DEFAULT, preferences.defaultSort)
        assertNull("another album keeps no memory", preferences.sortFor(AlbumKind.MEDIA, 43L))
        assertNull("kinds do not share keys", preferences.sortFor(AlbumKind.FOLDER, 42L))

        val byMonth = MediaSort(MediaSortField.DATE_TAKEN, ascending = false)
        preferences.applySort(AlbumKind.ALL, MediaCatalog.ALL_BUCKETS, byMonth, MediaGrouping.MONTH, thisAlbumOnly = false)
        assertEquals(byMonth, preferences.defaultSort)
        assertEquals(MediaGrouping.MONTH, preferences.defaultGrouping)
        assertFalse(preferences.hasOwnSort(AlbumKind.ALL, MediaCatalog.ALL_BUCKETS))
        assertEquals("an album with its own choice keeps it", byName, preferences.sortFor(AlbumKind.MEDIA, 42L))

        // Unticking "this album only" drops the album's own choice; it follows the default again.
        preferences.applySort(AlbumKind.MEDIA, 42L, byMonth, MediaGrouping.DAY, thisAlbumOnly = false)
        assertNull(preferences.sortFor(AlbumKind.MEDIA, 42L))
        assertNull(preferences.groupingFor(AlbumKind.MEDIA, 42L))
        assertEquals(MediaGrouping.DAY, preferences.defaultGrouping)
    }

    @Test
    fun theAlbumGridSortIsRemembered() {
        assertEquals(MediaSort.DEFAULT, preferences.albumSort)
        preferences.albumSort = MediaSort(MediaSortField.SIZE, ascending = true)
        assertEquals(MediaSort(MediaSortField.SIZE, ascending = true), GalleryPreferences(RuntimeEnvironment.getApplication()).albumSort)
        assertNull(GalleryPreferences.decodeGrouping("WEEK"))
    }

    @Test
    fun sortEncodingRoundTripsAndRejectsGarbage() {
        for (field in MediaSortField.entries) {
            for (ascending in listOf(true, false)) {
                val sort = MediaSort(field, ascending)
                assertEquals(sort, GalleryPreferences.decodeSort(GalleryPreferences.encodeSort(sort)))
            }
        }
        assertNull(GalleryPreferences.decodeSort(null))
        assertNull(GalleryPreferences.decodeSort("NAME"))
        assertNull(GalleryPreferences.decodeSort("COLOR:asc"))
        assertNull(GalleryPreferences.decodeSort("NAME:sideways"))
    }

    @Test
    fun recentTargetsKeepTheNewestFiveWithoutDuplicates() {
        assertEquals(emptyList<AlbumPath>(), preferences.recentTargets)
        for (index in 1..6) preferences.rememberTarget(primary("Pictures/A$index/"))
        preferences.rememberTarget(primary("Pictures/A3/"))
        assertEquals(listOf("Pictures/A3/", "Pictures/A6/", "Pictures/A5/", "Pictures/A4/", "Pictures/A2/").map(::primary), preferences.recentTargets)
    }

    @Test
    fun recentTargetsKeepTheirVolume() {
        val card = AlbumPath("1eff-e702", "Pictures/Trips/")
        preferences.rememberTarget(primary("Pictures/Trips/"))
        preferences.rememberTarget(card)
        assertEquals(listOf(card, primary("Pictures/Trips/")), preferences.recentTargets)
        // 3.1.0 stored bare relative paths: they still read as internal storage targets.
        assertEquals(primary("DCIM/Camera/"), GalleryPreferences.decodeTarget("DCIM/Camera/"))
        assertEquals("Pictures/Trips/", GalleryPreferences.encodeTarget(primary("Pictures/Trips/")))
        assertEquals(card, GalleryPreferences.decodeTarget(GalleryPreferences.encodeTarget(AlbumPath("1EFF-E702", "Pictures/Trips/"))))
    }

    private fun primary(relativePath: String) = AlbumPath(MediaCatalog.PRIMARY_VOLUME, relativePath)
}
