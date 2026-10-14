package io.github.supermonster003.autojs6.plugin.three.maple.image

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StandaloneOverwriteTest {

    private fun facts(
        mimeType: String = "image/jpeg",
        mediaLibrary: Boolean = false,
        sdk: Int = 33,
        onPrimaryStorage: Boolean = false,
        writeGranted: Boolean = false,
        providerDocument: Boolean = false,
        providerSupportsWrite: Boolean = false,
    ) = StandaloneOverwrite.Facts(mimeType, mediaLibrary, sdk, onPrimaryStorage, writeGranted, providerDocument, providerSupportsWrite)

    @Test fun onlyFormatsTheToolsWriteCanBeOverwritten() {
        listOf("image/jpeg", "image/png", "image/webp").forEach { assertTrue(it, StandaloneOverwrite.decide(facts(it, mediaLibrary = true))) }
        listOf("image/gif", "image/heic", "image/bmp", "application/octet-stream").forEach {
            assertFalse(it, StandaloneOverwrite.decide(facts(it, mediaLibrary = true)))
        }
    }

    @Test fun libraryImagesNeedAndroid11OrTheInternalStorage() {
        assertTrue(StandaloneOverwrite.decide(facts(mediaLibrary = true, sdk = 30)))
        assertTrue(StandaloneOverwrite.decide(facts(mediaLibrary = true, sdk = 28, onPrimaryStorage = true)))
        // Android 7-10 cannot write a storage card.
        assertFalse(StandaloneOverwrite.decide(facts(mediaLibrary = true, sdk = 29, onPrimaryStorage = false)))
    }

    @Test fun otherDocumentsNeedAWriteGrantAndAWritableDocument() {
        assertFalse(StandaloneOverwrite.decide(facts()))
        assertTrue(StandaloneOverwrite.decide(facts(writeGranted = true)))
        assertTrue(StandaloneOverwrite.decide(facts(writeGranted = true, providerDocument = true, providerSupportsWrite = true)))
        assertFalse(StandaloneOverwrite.decide(facts(writeGranted = true, providerDocument = true, providerSupportsWrite = false)))
        assertFalse(StandaloneOverwrite.decide(facts(providerDocument = true, providerSupportsWrite = true)))
    }
}
