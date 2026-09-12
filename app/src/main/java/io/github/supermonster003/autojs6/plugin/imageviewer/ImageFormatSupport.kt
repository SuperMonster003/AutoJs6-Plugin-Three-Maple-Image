package io.github.supermonster003.autojs6.plugin.imageviewer

import android.graphics.BitmapFactory
import android.os.Build
import android.util.Base64
import java.util.Locale

internal enum class ModernImageFormat(
    val displayLabel: String,
    val minimumSdk: Int,
    val minimumAndroidVersion: Int,
    internal val extensions: Set<String>,
    internal val mimeTypes: Set<String>,
) {
    HEIF(
        displayLabel = "HEIC / HEIF",
        minimumSdk = 28,
        minimumAndroidVersion = 9,
        extensions = setOf("heic", "heif"),
        mimeTypes = setOf(
            "image/heic",
            "image/heif",
            "image/heic-sequence",
            "image/heif-sequence",
        ),
    ),
    AVIF(
        displayLabel = "AVIF",
        minimumSdk = 31,
        minimumAndroidVersion = 12,
        extensions = setOf("avif"),
        mimeTypes = setOf("image/avif"),
    ),
}

internal enum class ModernImageUnavailableReason {
    ANDROID_VERSION,
    PLATFORM_DECODER,
}

internal data class ModernImageUnavailable(
    val format: ModernImageFormat,
    val reason: ModernImageUnavailableReason,
)

/** Canonical extension, MIME-family, and modern platform-decoder policy. */
internal object ImageFormatSupport {

    private data class ExplorerImageFormat(
        val fallbackMimeType: String,
        val compatibleMimeTypes: Set<String>,
    )

    private val bmp = ExplorerImageFormat(
        fallbackMimeType = "image/bmp",
        compatibleMimeTypes = setOf("image/bmp", "image/x-bmp", "image/x-ms-bmp"),
    )
    private val gif = ExplorerImageFormat("image/gif", setOf("image/gif"))
    private val jpeg = ExplorerImageFormat(
        fallbackMimeType = "image/jpeg",
        compatibleMimeTypes = setOf("image/jpeg", "image/jpg", "image/pjpeg"),
    )
    private val png = ExplorerImageFormat("image/png", setOf("image/png"))
    private val webp = ExplorerImageFormat("image/webp", setOf("image/webp"))
    private val heic = ExplorerImageFormat(
        fallbackMimeType = "image/heic",
        compatibleMimeTypes = ModernImageFormat.HEIF.mimeTypes,
    )
    private val heif = ExplorerImageFormat(
        fallbackMimeType = "image/heif",
        compatibleMimeTypes = ModernImageFormat.HEIF.mimeTypes,
    )
    private val avif = ExplorerImageFormat(
        fallbackMimeType = "image/avif",
        compatibleMimeTypes = ModernImageFormat.AVIF.mimeTypes,
    )

    private val explorerFormats = linkedMapOf(
        "avif" to avif,
        "bmp" to bmp,
        "gif" to gif,
        "heic" to heic,
        "heif" to heif,
        "jfif" to jpeg,
        "jpe" to jpeg,
        "jpeg" to jpeg,
        "jpg" to jpeg,
        "png" to png,
        "webp" to webp,
    )

    val explorerExtensions: List<String> = explorerFormats.keys.sorted()

    fun isSupportedExplorerTarget(displayName: String, mimeType: String): Boolean {
        return explorerMimeType(displayName, mimeType) != null
    }

    fun explorerMimeType(displayName: String, declaredMimeType: String): String? {
        val format = explorerFormats[extension(displayName)] ?: return null
        if (declaredMimeType in GENERIC_HOST_MIME_TYPES) return format.fallbackMimeType
        val normalizedMimeType = ImageRequestPolicy.normalizeImageMimeType(declaredMimeType) ?: return null
        return when {
            normalizedMimeType == "image/*" -> format.fallbackMimeType
            normalizedMimeType in format.compatibleMimeTypes -> normalizedMimeType
            else -> null
        }
    }

    fun siblingMimeType(displayName: String, declaredMimeType: String): String {
        val format = requireNotNull(explorerFormats[extension(displayName)])
        val normalizedMimeType = ImageRequestPolicy.normalizeImageMimeType(declaredMimeType)
        return normalizedMimeType
            ?.takeIf { it != "image/*" && it in format.compatibleMimeTypes }
            ?: format.fallbackMimeType
    }

    fun mimeTypesAreEquivalent(first: String, second: String): Boolean {
        if (first == second) return true
        val firstFamily = explorerFormats.values.firstOrNull { first in it.compatibleMimeTypes }
            ?: return false
        return second in firstFamily.compatibleMimeTypes
    }

    fun modernFormat(displayName: String, mimeType: String): ModernImageFormat? {
        val normalizedMimeType = ImageRequestPolicy.normalizeImageMimeType(mimeType) ?: return null
        ModernImageFormat.entries.firstOrNull { normalizedMimeType in it.mimeTypes }?.let {
            return it
        }
        if (normalizedMimeType != "image/*") return null
        val extension = extension(displayName)
        return ModernImageFormat.entries.firstOrNull { extension in it.extensions }
    }

    fun unsupportedModernFormat(
        displayName: String,
        mimeType: String,
        sdkInt: Int,
        platformDecoderSupported: Boolean? = null,
    ): ModernImageUnavailable? {
        require(sdkInt >= 0)
        val format = modernFormat(displayName, mimeType) ?: return null
        if (sdkInt < format.minimumSdk) {
            return ModernImageUnavailable(format, ModernImageUnavailableReason.ANDROID_VERSION)
        }
        if (platformDecoderSupported == false) {
            return ModernImageUnavailable(format, ModernImageUnavailableReason.PLATFORM_DECODER)
        }
        return null
    }

    private fun extension(displayName: String): String =
        displayName.substringAfterLast('.', "").lowercase(Locale.ROOT)

    private val GENERIC_HOST_MIME_TYPES = setOf("*/*", "application/octet-stream")
}

/** Adds a cached bounds-decode probe for the exact platform path used by the viewer. */
internal object ImagePlatformDecodeSupport {

    private val decoderSupport = mutableMapOf<ModernImageFormat, Boolean>()

    fun unsupported(request: ImageViewerRequest): ModernImageUnavailable? {
        val format = ImageFormatSupport.modernFormat(request.displayName, request.mimeType)
            ?: return null
        val sdkInt = Build.VERSION.SDK_INT
        val versionFailure = ImageFormatSupport.unsupportedModernFormat(
            displayName = request.displayName,
            mimeType = request.mimeType,
            sdkInt = sdkInt,
        )
        if (versionFailure != null) return versionFailure
        return ImageFormatSupport.unsupportedModernFormat(
            displayName = request.displayName,
            mimeType = request.mimeType,
            sdkInt = sdkInt,
            platformDecoderSupported = platformDecoderSupport(format),
        )
    }

    private fun platformDecoderSupport(format: ModernImageFormat): Boolean =
        synchronized(decoderSupport) {
            decoderSupport.getOrPut(format) {
                ModernImageDecodeProbe.canDecode(format)
            }
        }
}

/** Tiny local 8-bit fixtures avoid false negatives from ImageDecoder's MIME capability query. */
internal object ModernImageDecodeProbe {

    fun canDecode(format: ModernImageFormat): Boolean = runCatching {
        val bytes = bytes(format)
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
        options.outWidth > 0 && options.outHeight > 0
    }.getOrDefault(false)

    fun bytes(format: ModernImageFormat): ByteArray = Base64.decode(
        when (format) {
            ModernImageFormat.HEIF -> HEIF_BASE64
            ModernImageFormat.AVIF -> AVIF_BASE64
        },
        Base64.DEFAULT,
    )

    // pillow-heif-generated 8 x 6, 8-bit HEIC with red/blue halves.
    private const val HEIF_BASE64 =
        "AAAAHGZ0eXBoZWljAAAAAG1pZjFoZWljbWlhZgAAAXxtZXRhAAAAAAAAACFoZGxyAAAAAAAAAABwaWN0AAAAAAAAAAAAAAAAAAAAACJpbG9jAAAAAERAAAEAAQAAAAABoAABAAAAAAAAAJoAAAAjaWluZgAAAAAAAQAAABVpbmZlAgAAAAABAABodmMxAAAAAA5waXRtAAAAAAABAAAA/GlwcnAAAADcaXBjbwAAAHVodmNDAQNwAAAAAAAAAAAAHvAA/P34+AAADwNgAAEAGEABDAH//wNwAAADAJAAAAMAAAMAHroCQGEAAQApQgEBA3AAAAMAkAAAAwAAAwAeoCCBBZbqrprm4CGgwIAAAAyAAAADAIRiAAEABkQBwXPBiQAAABNjb2xybmNseAABAA0ABoAAAAAUaXNwZQAAAAAAAABAAAAAQAAAAChjbGFwAAAACAAAAAEAAAAGAAAAAf///8gAAAAC////xgAAAAIAAAAQcGl4aQAAAAADCAgIAAAAGGlwbWEAAAAAAAAAAQABBYECAwWEAAAAom1kYXQAAACWKAGvBjId+QPw8cs/CG/P+9eJP7bM+WTVt4+s7qlE2ErhkcKzvY3DTi+VuYGbZUpca+iiP+hiMrM57LSb4WZXC7NVD/PNgF0BXWRnJLpG7VJJN51UzYIXJIEoorJgaxpQ+kFB99RyveypXuqh8EYzus0nrgYZfVwvp2rJmDYGbzMljEarlCO1lEJ1huZ4jBstbIMVvfly"

    // ImageMagick-generated 8 x 6, 8-bit AVIF with red/blue halves.
    private const val AVIF_BASE64 =
        "AAAAHGZ0eXBhdmlmAAAAAGF2aWZtaWYxbWlhZgAAAXBtZXRhAAAAAAAAACFoZGxyAAAAAAAAAABwaWN0AAAAAAAAAAAAAAAAAAAAAA5waXRtAAAAAAABAAAANGlsb2MAAAAAREAAAgABAAAAAAGUAAEAAAAAAAAAKQACAAAAAAG9AAEAAAAAAAAAFQAAADhpaW5mAAAAAAACAAAAFWluZmUCAAAAAAEAAGF2MDEAAAAAFWluZmUCAAAAAAIAAGF2MDEAAAAAr2lwcnAAAACKaXBjbwAAAAxhdjFDgQAMAAAAABRpc3BlAAAAAAAAAAgAAAAGAAAAEHBpeGkAAAAAAwgICAAAAAxhdjFDgQAcAAAAAA5waXhpAAAAAAEIAAAAOGF1eEMAAAAAdXJuOm1wZWc6bXBlZ0I6Y2ljcDpzeXN0ZW1zOmF1eGlsaWFyeTphbHBoYQAAAAAdaXBtYQAAAAAAAAACAAEDgQIDAAIEhAIFhgAAABppcmVmAAAAAAAAAA5hdXhsAAIAAQABAAAARm1kYXQSAAoIGAi9YICGg0IyGxgACiiihQBBwb3fBKT/FxbBBcEj4QqPC0oVIBIACgUYCL1hUDIKGAAooQACIRujYA=="
}
