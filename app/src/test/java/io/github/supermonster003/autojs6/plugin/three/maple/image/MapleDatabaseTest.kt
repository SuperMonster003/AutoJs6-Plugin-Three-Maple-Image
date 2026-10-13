package io.github.supermonster003.autojs6.plugin.three.maple.image

import androidx.room.Room
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/** ROADMAP T8: the Room tables behind favorites and the API 24-29 recycle bin. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class MapleDatabaseTest {

    private lateinit var database: MapleDatabase

    @Before
    fun open() {
        database = Room.inMemoryDatabaseBuilder(RuntimeEnvironment.getApplication(), MapleDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun close() {
        database.close()
    }

    private fun favorite(mediaId: Long, addedAt: Long) = FavoriteEntity(
        mediaId = mediaId,
        fingerprint = "100:$mediaId:10x10",
        relativePath = "Pictures/",
        displayName = "f$mediaId.jpg",
        addedAtMillis = addedAt,
    )

    @Test
    fun favoritesAreUniquePerMediaIdAndNewestFirst() = runBlocking {
        val dao = database.favorites()
        dao.upsert(listOf(favorite(1, 10), favorite(2, 20)))
        dao.upsert(listOf(favorite(1, 30)))
        assertEquals(listOf(1L, 2L), dao.all().map { it.mediaId })
        assertEquals(setOf(1L, 2L), dao.observeMediaIds().first().toSet())
        dao.delete(listOf(1))
        assertEquals(listOf(2L), dao.all().map { it.mediaId })
    }

    @Test
    fun aRebindKeepsTheFavoriteUnderTheNewRow() = runBlocking {
        val dao = database.favorites()
        dao.upsert(listOf(favorite(5, 1)))
        dao.rebind(oldMediaId = 5, newMediaId = 9, relativePath = "Pictures/Moved/", displayName = "f5.jpg")
        val moved = dao.all().single()
        assertEquals(9L, moved.mediaId)
        assertEquals("Pictures/Moved/", moved.relativePath)
    }

    @Test
    fun recycleBinEntriesListNewestFirstAndDeleteById() = runBlocking {
        val dao = database.recycleBin()
        val first = dao.insert(entry("a.jpg", deletedAt = 100))
        val second = dao.insert(entry("b.jpg", deletedAt = 200))
        assertEquals(listOf("b.jpg", "a.jpg"), dao.all().map { it.displayName })
        dao.delete(listOf(first))
        assertEquals(listOf(second), dao.observeAll().first().map { it.id })
        assertTrue(dao.all().single().dateTakenMillis == null)
    }

    @Test
    fun theFingerprintIgnoresPathAndName() {
        val item = MediaItem(
            id = 1, displayName = "a.jpg", mimeType = "image/jpeg", sizeBytes = 1234, width = 40, height = 30,
            dateModifiedSeconds = 99, dateTakenMillis = null, bucketId = 7, bucketName = "Camera",
            relativePath = "DCIM/Camera/", contentUri = "content://media/external/images/media/1",
        )
        val moved = item.copy(id = 2, displayName = "b.jpg", relativePath = "Pictures/", bucketId = 8, contentUri = "content://media/external/images/media/2")
        assertEquals(MediaFingerprint.of(item), MediaFingerprint.of(moved))
    }

    private fun entry(name: String, deletedAt: Long) = RecycleBinEntity(
        storedName = "$deletedAt-$name",
        originalPath = "/storage/emulated/0/Pictures/$name",
        displayName = name,
        mimeType = "image/jpeg",
        sizeBytes = 10,
        dateModifiedSeconds = 1,
        dateTakenMillis = null,
        deletedAtMillis = deletedAt,
    )
}
