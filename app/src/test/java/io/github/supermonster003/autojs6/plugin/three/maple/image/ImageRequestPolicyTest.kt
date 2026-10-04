package io.github.supermonster003.autojs6.plugin.three.maple.image

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ImageRequestPolicyTest {

    @Test
    fun normalizesOnlySafeImageMimeTypes() {
        assertEquals("image/png", ImageRequestPolicy.normalizeImageMimeType("image/png"))
        assertEquals("image/*", ImageRequestPolicy.normalizeImageMimeType("image/*"))
        assertNull(ImageRequestPolicy.normalizeImageMimeType("IMAGE/PNG"))
        assertNull(ImageRequestPolicy.normalizeImageMimeType("audio/png"))
        assertNull(ImageRequestPolicy.normalizeImageMimeType("image/"))
    }

    @Test
    fun rejectsUnsafeDisplayNames() {
        assertEquals("photo.png", ImageRequestPolicy.validateDisplayName("photo.png"))
        assertNull(ImageRequestPolicy.validateDisplayName("../photo.png"))
        assertNull(ImageRequestPolicy.validateDisplayName("photo\u202Egnp"))
        assertNull(ImageRequestPolicy.validateDisplayName("photo\n.png"))
    }

    @Test
    fun enforcesDeclaredSizeLimit() {
        assertTrue(ImageRequestPolicy.isDeclaredSizeAccepted(0L))
        assertTrue(ImageRequestPolicy.isDeclaredSizeAccepted(ImageRequestPolicy.MAX_DECLARED_SIZE))
        assertFalse(ImageRequestPolicy.isDeclaredSizeAccepted(-1L))
        assertFalse(ImageRequestPolicy.isDeclaredSizeAccepted(ImageRequestPolicy.MAX_DECLARED_SIZE + 1L))
    }

    @Test
    fun comparesResolvedMimeWithoutWideningConcreteTypes() {
        assertTrue(ImageRequestPolicy.mimeTypesAreCompatible("image/*", "image/webp"))
        assertTrue(ImageRequestPolicy.mimeTypesAreCompatible("image/jpeg", "image/jpeg"))
        assertTrue(ImageRequestPolicy.mimeTypesAreCompatible("image/heic", "image/heif"))
        assertTrue(ImageRequestPolicy.mimeTypesAreCompatible("image/heif", "image/heic-sequence"))
        assertFalse(ImageRequestPolicy.mimeTypesAreCompatible("image/jpeg", "image/png"))
        assertFalse(ImageRequestPolicy.mimeTypesAreCompatible("image/heic", "image/avif"))
        assertFalse(ImageRequestPolicy.mimeTypesAreCompatible("image/jpeg", "video/jpeg"))
    }
}
