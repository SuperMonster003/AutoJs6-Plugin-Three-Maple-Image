package io.github.supermonster003.autojs6.plugin.three.maple.image

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Read-only checks of [MediaStoreRepository] against the device's real library: the full scan, the album
 * aggregation and the provider-side paging must agree with each other on every platform level.
 */
@RunWith(AndroidJUnit4::class)
class MediaStoreRepositoryInstrumentationTest {

    @get:Rule
    val permission: GrantPermissionRule = GrantPermissionRule.grant(MediaPermissionPolicy.readPermission(android.os.Build.VERSION.SDK_INT))

    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Test fun fullScanAlbumsAndPagesAgree() = runBlocking {
        val repository = MediaStoreRepository(context)
        val all = repository.loadAll()
        assumeTrue("the device library has at least one supported image", all.isNotEmpty())
        assertTrue(all.all { it.sizeBytes > 0L && it.contentUri.startsWith("content://") && it.displayName.isNotEmpty() })
        assertTrue("newest change first", all.zipWithNext().all { (a, b) -> a.dateModifiedSeconds >= b.dateModifiedSeconds })
        val albums = repository.loadBuckets()
        assertEquals(all.size, albums.sumOf { it.count })
        assertEquals(all.map { it.bucketId }.toSet(), albums.map { it.id }.toSet())

        val album = albums.maxBy { it.count }
        val expected = MediaCatalog.sort(all.filter { it.bucketId == album.id }, MediaSort(MediaSortField.NAME, ascending = true))
        val pageSize = 3
        val firstPage = repository.loadBucket(album.id, MediaSort(MediaSortField.NAME, ascending = true), limit = pageSize, offset = 0)
        val secondPage = repository.loadBucket(album.id, MediaSort(MediaSortField.NAME, ascending = true), limit = pageSize, offset = pageSize)
        assertTrue(firstPage.size <= pageSize && secondPage.size <= pageSize)
        assertTrue("pages do not overlap", firstPage.map { it.id }.intersect(secondPage.map { it.id }.toSet()).isEmpty())
        assertEquals("first page is the head of the album", expected.take(firstPage.size).map { it.id }.toSet(), firstPage.map { it.id }.toSet())
        assertTrue("every paged row belongs to the album", (firstPage + secondPage).all { it.bucketId == album.id })

        val newest = repository.loadBucket(album.id, MediaSort(MediaSortField.DATE_MODIFIED, ascending = false), limit = 2)
        assertTrue(newest.zipWithNext().all { (a, b) -> a.dateModifiedSeconds >= b.dateModifiedSeconds })
        repository.invalidate()
        assertEquals(all.map { it.id }, repository.loadAll().map { it.id })
    }
}
