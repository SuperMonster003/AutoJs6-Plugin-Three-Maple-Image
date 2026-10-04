package io.github.supermonster003.autojs6.plugin.three.maple.image

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ImageSiblingDiscoveryPolicyTest {

    @Test
    fun discoversNaturallySortedImagePagesAroundSelectedFile() {
        val gallery = discover(
            selectedName = "photo10.png",
            siblings = listOf(
                file("photo10.png", "image/png"),
                file("photo2.webp", "image/webp"),
                file("photo01.jpg", "image/jpeg"),
            ),
        )

        assertEquals(
            listOf("photo01.jpg", "photo2.webp", "photo10.png"),
            gallery.pages.map { it.displayName },
        )
        assertEquals(2, gallery.startIndex)
        assertEquals("", gallery.pages[gallery.startIndex].relativePath)
    }

    @Test
    fun excludesUnsupportedUnreadableLinkedNestedAndDirectoryEntries() {
        val gallery = discover(
            selectedName = "selected.png",
            siblings = listOf(
                file("selected.png", "image/png"),
                file("notes.txt", "text/plain"),
                file("hidden.jpg", "image/jpeg").copy(readable = false),
                file("linked.webp", "image/webp").copy(symbolicLink = true),
                file("nested/escape.png", "image/png"),
                file("folder.gif", "inode/directory").copy(kind = 2),
                file("oversized.bmp", "image/bmp").copy(
                    declaredSize = ImageRequestPolicy.MAX_DECLARED_SIZE + 1L,
                ),
            ),
        )

        assertEquals(listOf("selected.png"), gallery.pages.map { it.displayName })
    }

    @Test
    fun usesExtensionMimeFallbackWithoutBroadeningTheSupportedSet() {
        val gallery = discover(
            selectedName = "selected.png",
            siblings = listOf(
                file("selected.png", "image/png"),
                file("camera.jfif", "application/octet-stream"),
            ),
        )

        assertEquals("image/jpeg", gallery.pages.first { it.displayName == "camera.jfif" }.mimeType)
    }

    @Test
    fun includesModernFormatsAndCanonicalizesUntrustedSiblingMimeTypes() {
        val gallery = discover(
            selectedName = "selected.png",
            siblings = listOf(
                file("selected.png", "image/png"),
                file("camera.heic", "image/heif"),
                file("archive.heif", "application/octet-stream"),
                file("frame.avif", "image/jpeg"),
            ),
        )

        assertEquals(
            listOf("image/heif", "image/heif", "image/avif"),
            listOf("archive.heif", "camera.heic", "frame.avif").map { name ->
                gallery.pages.first { it.displayName == name }.mimeType
            },
        )
    }

    @Test
    fun boundsLargeDirectoriesWhileKeepingTheSelectedPage() {
        val siblings = (0 until 300).map { index -> file("photo$index.png", "image/png") }

        val gallery = discover("photo150.png", siblings)

        assertEquals(ImageSiblingDiscoveryPolicy.MAX_PAGES, gallery.pages.size)
        assertEquals("photo150.png", gallery.pages[gallery.startIndex].displayName)
        assertEquals("", gallery.pages[gallery.startIndex].relativePath)
    }

    @Test
    fun naturalComparatorDefersCaseTieBreakUntilAfterNumericOrdering() {
        assertTrue(ImageSiblingDiscoveryPolicy.compareNaturally("photo2.png", "Photo10.png") < 0)
        assertTrue(ImageSiblingDiscoveryPolicy.compareNaturally("Photo.png", "photo.png") < 0)
    }

    private fun discover(
        selectedName: String,
        siblings: List<ImageSiblingItem>,
    ): DiscoveredImageGallery = ImageSiblingDiscoveryPolicy.discover(
        selectedDisplayName = selectedName,
        selectedMimeType = "image/png",
        selectedDeclaredSize = 1_024L,
        siblings = siblings,
    )

    private fun file(name: String, mimeType: String) = ImageSiblingItem(
        relativePath = name,
        displayName = name,
        kind = 1,
        mimeType = mimeType,
        declaredSize = 1_024L,
        lastModified = 1_700_000_000_000L,
        readable = true,
        symbolicLink = false,
    )
}
