package io.github.supermonster003.autojs6.plugin.three.maple.image

import java.io.File

internal object ModernImageTestFixture {

    fun writeHeifTo(file: File) {
        file.writeBytes(ModernImageDecodeProbe.bytes(ModernImageFormat.HEIF))
    }

    fun writeAvifTo(file: File) {
        file.writeBytes(ModernImageDecodeProbe.bytes(ModernImageFormat.AVIF))
    }
}
