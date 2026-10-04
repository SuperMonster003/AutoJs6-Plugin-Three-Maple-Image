package io.github.supermonster003.autojs6.plugin.three.maple.image

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ImageDecodedColorMetadataPolicyTest {

    @Test
    fun keepsKnownDepthAndCollapsesColorSpaceWhitespace() {
        assertEquals(
            ImageDecodedColorMetadata(64, "Display P3"),
            ImageDecodedColorMetadataPolicy.create(64, "  Display   P3  "),
        )
    }

    @Test
    fun unsafeColorSpaceNameIsDroppedWithoutLosingKnownDepth() {
        assertEquals(
            ImageDecodedColorMetadata(32, null),
            ImageDecodedColorMetadataPolicy.create(32, "sRGB\u202Etxt.exe"),
        )
    }

    @Test
    fun overlongColorSpaceNameIsDroppedWithoutLosingKnownDepth() {
        assertEquals(
            ImageDecodedColorMetadata(32, null),
            ImageDecodedColorMetadataPolicy.create(32, "x".repeat(97)),
        )
    }

    @Test
    fun invalidDepthDoesNotHideAnOtherwiseSafeColorSpace() {
        assertEquals(
            ImageDecodedColorMetadata(null, "sRGB IEC61966-2.1"),
            ImageDecodedColorMetadataPolicy.create(0, "sRGB IEC61966-2.1"),
        )
    }

    @Test
    fun returnsNullWhenTheDecoderProvidesNoSafeValues() {
        assertNull(ImageDecodedColorMetadataPolicy.create(null, null))
        assertNull(ImageDecodedColorMetadataPolicy.create(256, "\u0000"))
    }
}
