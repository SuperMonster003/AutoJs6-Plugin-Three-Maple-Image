package io.github.supermonster003.autojs6.plugin.imageviewer

import android.util.Base64
import java.io.File

internal object AnimatedGifTestFixture {

    fun writeTo(file: File) {
        file.writeBytes(Base64.decode(BASE64, Base64.DEFAULT))
    }

    fun writeSingleFrameTo(file: File) {
        file.writeBytes(Base64.decode(SINGLE_FRAME_BASE64, Base64.DEFAULT))
    }

    // ImageMagick-generated 3 x 2, two-frame GIF89a fixture with a finite frame delay.
    private const val BASE64 =
        "R0lGODlhAwACAPAAAP8AAAAAACH/C05FVFNDQVBFMi4wAwEAAAAh+QQACgAAACwAAAAAAwACAAACAoRfACH5BAAKAAAALAAAAAADAAIAgAAA/wAAAAIChF8AOw=="

    // ImageMagick-generated 3 x 2, one-frame GIF89a fixture.
    private const val SINGLE_FRAME_BASE64 =
        "R0lGODlhAwACAPAAAACAAAAAACH5BAAAAAAALAAAAAADAAIAAAIChF8AOw=="
}
