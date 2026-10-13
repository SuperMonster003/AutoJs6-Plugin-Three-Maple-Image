package io.github.supermonster003.autojs6.plugin.three.maple.image

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** ROADMAP T7: recursive enumeration with depth and item caps, format filter and loop safety. */
class DocumentTreeWalkerTest {

    private val supports: (String, String) -> Boolean = { name, mime -> mime.startsWith("image/") && !name.endsWith(".tiff") }

    private fun dir(id: String, name: String) = DocumentEntry(id, name, DocumentTreeWalker.DIRECTORY_MIME_TYPE, 0L, 0L)
    private fun img(id: String, name: String, size: Long = 100L, mime: String = "image/jpeg") = DocumentEntry(id, name, mime, size, 1L)

    private val tree: Map<String, List<DocumentEntry>> = mapOf(
        "root" to listOf(img("a", "a.jpg"), dir("d1", "Scans"), img("t", "t.tiff", mime = "image/tiff"), img("e", "empty.png", size = 0L), DocumentEntry("n", "notes.txt", "text/plain", 5L, 1L)),
        "d1" to listOf(img("b", "b.png", mime = "image/png"), dir("d2", "2024")),
        "d2" to listOf(img("c", "c.webp", mime = "image/webp"), dir("root", "loop")),
    )

    @Test
    fun walksRecursivelyFiltersAndRecordsRelativeFolders() {
        val result = DocumentTreeWalker.walk("root", { tree[it].orEmpty() }, supports)
        assertEquals(listOf("a", "b", "c"), result.images.map { it.documentId })
        assertEquals(mapOf("a" to "", "b" to "Scans/", "c" to "Scans/2024/"), result.folders)
        assertEquals(DocumentTreeLimit.NONE, result.limit)
    }

    @Test
    fun depthCapStopsBeforeDeeperLevelsAndReportsIt() {
        val result = DocumentTreeWalker.walk("root", { tree[it].orEmpty() }, supports, maxDepth = 1)
        assertEquals(listOf("a"), result.images.map { it.documentId })
        assertEquals(DocumentTreeLimit.DEPTH, result.limit)
        val two = DocumentTreeWalker.walk("root", { tree[it].orEmpty() }, supports, maxDepth = 2)
        assertEquals(listOf("a", "b"), two.images.map { it.documentId })
        assertEquals(DocumentTreeLimit.DEPTH, two.limit)
    }

    @Test
    fun itemCapStopsAndReportsIt() {
        val result = DocumentTreeWalker.walk("root", { tree[it].orEmpty() }, supports, maxImages = 2)
        assertEquals(listOf("a", "b"), result.images.map { it.documentId })
        assertEquals(DocumentTreeLimit.ITEMS, result.limit)
    }

    @Test
    fun loopsAndDuplicatesAreVisitedOnce() {
        val looping = mapOf(
            "root" to listOf(dir("x", "x"), img("p", "p.jpg")),
            "x" to listOf(dir("root", "back"), dir("x", "self"), img("p", "p.jpg")),
        )
        val result = DocumentTreeWalker.walk("root", { looping[it].orEmpty() }, supports)
        assertEquals(listOf("p"), result.images.map { it.documentId })
        assertEquals(DocumentTreeLimit.NONE, result.limit)
    }

    @Test
    fun syntheticIdsAreStableNegativeAndDistinct() {
        val first = DocumentTreeWalker.syntheticId("primary:Pictures/a.jpg")
        assertEquals(first, DocumentTreeWalker.syntheticId("primary:Pictures/a.jpg"))
        assertTrue(first < 0L)
        assertNotEquals(first, DocumentTreeWalker.syntheticId("primary:Pictures/b.jpg"))
    }
}
