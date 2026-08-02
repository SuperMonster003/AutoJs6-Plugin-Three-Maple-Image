package io.github.supermonster003.autojs6.plugin.imagetools

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ImageToolsRequestPolicyTest {

    @Test
    fun normalizesOnlyCanonicalMimeTypes() {
        assertEquals("image/png", ImageToolsRequestPolicy.normalizeMimeType("image/png"))
        assertEquals("image/*", ImageToolsRequestPolicy.normalizeMimeType("image/*"))
        assertEquals("*/*", ImageToolsRequestPolicy.normalizeMimeType("*/*"))
        assertNull(ImageToolsRequestPolicy.normalizeMimeType("IMAGE/PNG"))
        assertNull(ImageToolsRequestPolicy.normalizeMimeType(" image/png"))
        assertNull(ImageToolsRequestPolicy.normalizeMimeType("image/png; charset=binary"))
        assertNull(ImageToolsRequestPolicy.normalizeMimeType("image/"))
    }

    @Test
    fun rejectsMalformedOrDuplicateOutputMimeTypes() {
        assertEquals(
            setOf("image/png", "image/jpeg"),
            ImageToolsRequestPolicy.normalizeOutputMimeTypes(listOf("image/png", "image/jpeg")),
        )
        assertNull(ImageToolsRequestPolicy.normalizeOutputMimeTypes(emptyList()))
        assertNull(ImageToolsRequestPolicy.normalizeOutputMimeTypes(listOf("image/png", "IMAGE/JPEG")))
        assertNull(ImageToolsRequestPolicy.normalizeOutputMimeTypes(listOf("image/png", "image/png")))
        assertNull(ImageToolsRequestPolicy.normalizeOutputMimeTypes(listOf("image/gif")))
    }

    @Test
    fun displayNameNeverActsAsAPath() {
        assertEquals("photo.png", ImageToolsRequestPolicy.validateDisplayName("photo.png"))
        assertNull(ImageToolsRequestPolicy.validateDisplayName("../photo.png"))
        assertNull(ImageToolsRequestPolicy.validateDisplayName("folder\\photo.png"))
        assertNull(ImageToolsRequestPolicy.validateDisplayName("photo\n.png"))
        assertNull(ImageToolsRequestPolicy.validateDisplayName("photo\u202Egnp"))
    }

    @Test
    fun acceptsOnlyReadOnlyInputAndWriteOnlyOutputGrants() {
        assertTrue(ImageToolsRequestPolicy.hasExpectedGrantState(
            inputRead = true,
            inputWrite = false,
            outputRead = false,
            outputWrite = true,
        ))
        assertTrue(!ImageToolsRequestPolicy.hasExpectedGrantState(true, false, true, true))
        assertTrue(!ImageToolsRequestPolicy.hasExpectedGrantState(true, true, false, true))
        assertTrue(!ImageToolsRequestPolicy.hasExpectedGrantState(true, false, false, false))
    }

    @Test
    fun protocolCapsStayWithinHostImageTransactionLimit() {
        assertTrue(ImageToolsPlugin.MAX_OUTPUT_BYTES in 1L..256L * 1024L * 1024L)
        assertEquals(ImageToolsPlugin.MAX_OUTPUT_BYTES, ImageToolsRequestPolicy.MAX_INPUT_BYTES)
        assertEquals(
            setOf("image/jpeg", "image/png", "image/webp"),
            ImageToolsPlugin.OUTPUT_MIME_TYPES.toSet(),
        )
        assertEquals(setOf("edit-image", "convert-image"), setOf(
            ImageToolsPlugin.EDIT_ACTION_ID,
            ImageToolsPlugin.CONVERT_ACTION_ID,
        ))
        assertEquals("r", ImageToolsPlugin.INPUT_OPEN_MODE)
        assertEquals("w", ImageToolsPlugin.OUTPUT_OPEN_MODE)
    }
}
