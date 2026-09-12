package io.github.supermonster003.autojs6.plugin.imageviewer

import org.junit.Assert.assertEquals
import org.junit.Test

class ImagePrintNamesTest {

    @Test
    fun replacesTheImageExtensionWithPdf() {
        assertEquals("photo.pdf", ImagePrintNames.pdfDocumentName("photo.jpg"))
        assertEquals(
            "holiday.2026.pdf",
            ImagePrintNames.pdfDocumentName("holiday.2026.jpeg"),
        )
        assertEquals("photo.pdf", ImagePrintNames.pdfDocumentName("photo"))
    }

    @Test
    fun sanitizesUnsafeOrEmptyDisplayNames() {
        assertEquals(".._bad_name_.pdf", ImagePrintNames.pdfDocumentName("../bad:name?.png"))
        assertEquals("image.pdf", ImagePrintNames.pdfDocumentName(".jpg"))
        assertEquals("image.pdf", ImagePrintNames.pdfDocumentName("   "))
    }
}
