package io.github.supermonster003.autojs6.plugin.three.maple.image

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ImageFormatSupportTest {

    @Test
    fun explorerCatalogContainsElevenCanonicalExtensions() {
        assertEquals(
            listOf("avif", "bmp", "gif", "heic", "heif", "jfif", "jpe", "jpeg", "jpg", "png", "webp"),
            ImageFormatSupport.explorerExtensions,
        )
        assertEquals(ImageFormatSupport.explorerExtensions, ThreeMapleImagePlugin.EXTENSIONS.toList())
    }

    @Test
    fun validatesExtensionAndMimeFamiliesWithoutCrossFormatWidening() {
        assertTrue(ImageFormatSupport.isSupportedExplorerTarget("camera.HEIC", "image/heic"))
        assertTrue(ImageFormatSupport.isSupportedExplorerTarget("camera.heif", "image/heic"))
        assertTrue(ImageFormatSupport.isSupportedExplorerTarget("burst.heic", "image/heif-sequence"))
        assertTrue(ImageFormatSupport.isSupportedExplorerTarget("frame.avif", "image/avif"))
        assertTrue(ImageFormatSupport.isSupportedExplorerTarget("photo.jpg", "image/*"))
        assertTrue(ImageFormatSupport.isSupportedExplorerTarget("frame.avif", "*/*"))
        assertTrue(ImageFormatSupport.isSupportedExplorerTarget("camera.heic", "application/octet-stream"))
        assertFalse(ImageFormatSupport.isSupportedExplorerTarget("frame.avif", "image/heic"))
        assertFalse(ImageFormatSupport.isSupportedExplorerTarget("photo.png", "image/jpeg"))
        assertFalse(ImageFormatSupport.isSupportedExplorerTarget("notes.txt", "image/png"))
    }

    @Test
    fun normalizesSiblingMimeWithinTheExtensionFamilyOrUsesSafeFallback() {
        assertEquals("image/heif", ImageFormatSupport.siblingMimeType("camera.heic", "image/heif"))
        assertEquals("image/heic", ImageFormatSupport.siblingMimeType("camera.heic", ""))
        assertEquals("image/heif", ImageFormatSupport.siblingMimeType("camera.heif", ""))
        assertEquals("image/avif", ImageFormatSupport.siblingMimeType("frame.avif", "image/*"))
        assertEquals("image/jpeg", ImageFormatSupport.siblingMimeType("photo.jfif", "image/png"))
        assertEquals("image/avif", ImageFormatSupport.explorerMimeType("frame.avif", "*/*"))
    }

    @Test
    fun detectsModernFormatFromConcreteMimeOrWildcardExtension() {
        assertEquals(
            ModernImageFormat.HEIF,
            ImageFormatSupport.modernFormat("opaque", "image/heif"),
        )
        assertEquals(
            ModernImageFormat.AVIF,
            ImageFormatSupport.modernFormat("frame.avif", "image/*"),
        )
        assertNull(ImageFormatSupport.modernFormat("frame.avif", "image/png"))
        assertNull(ImageFormatSupport.modernFormat("photo.jpg", "image/jpeg"))
    }

    @Test
    fun gatesHeifAtAndroidNineAndAvifAtAndroidTwelve() {
        val heifOnEight = requireNotNull(
            ImageFormatSupport.unsupportedModernFormat("camera.heic", "image/heic", 27),
        )
        assertEquals(ModernImageFormat.HEIF, heifOnEight.format)
        assertEquals(ModernImageUnavailableReason.ANDROID_VERSION, heifOnEight.reason)
        assertNull(ImageFormatSupport.unsupportedModernFormat("camera.heif", "image/heif", 28))

        val avifOnEleven = requireNotNull(
            ImageFormatSupport.unsupportedModernFormat("frame.avif", "image/avif", 30),
        )
        assertEquals(ModernImageFormat.AVIF, avifOnEleven.format)
        assertEquals(ModernImageUnavailableReason.ANDROID_VERSION, avifOnEleven.reason)
        assertNull(ImageFormatSupport.unsupportedModernFormat("frame.avif", "image/avif", 31))
    }

    @Test
    fun reportsAPlatformDecoderFailureOnlyAfterTheVersionGatePasses() {
        val unavailable = requireNotNull(
            ImageFormatSupport.unsupportedModernFormat(
                displayName = "frame.avif",
                mimeType = "image/avif",
                sdkInt = 35,
                platformDecoderSupported = false,
            ),
        )
        assertEquals(ModernImageUnavailableReason.PLATFORM_DECODER, unavailable.reason)

        val oldPlatform = requireNotNull(
            ImageFormatSupport.unsupportedModernFormat(
                displayName = "frame.avif",
                mimeType = "image/avif",
                sdkInt = 30,
                platformDecoderSupported = false,
            ),
        )
        assertEquals(ModernImageUnavailableReason.ANDROID_VERSION, oldPlatform.reason)
    }
}
