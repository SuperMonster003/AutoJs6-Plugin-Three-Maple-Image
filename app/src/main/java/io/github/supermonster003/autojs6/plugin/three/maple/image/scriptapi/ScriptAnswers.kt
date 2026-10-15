package io.github.supermonster003.autojs6.plugin.three.maple.image.scriptapi

import android.os.Bundle
import android.os.Parcel
import org.autojs.plugin.gallery.api.GalleryContract
import org.autojs.plugin.gallery.api.GalleryErrorCodes
import java.nio.charset.StandardCharsets

/** A refusal of the gallery contract (ROADMAP 10.2): a code from `GalleryErrorCodes` and a detail for the host. */
internal class ContractViolation(val code: String, val detail: String) : RuntimeException(GalleryErrorCodes.encode(code, detail))

/**
 * Bundle answers and limits of the gallery contract (ROADMAP 10.2): every answer carries the
 * contract version of its request, a failed one the error code and a bounded detail instead of
 * result keys, and calls that cannot answer with a Bundle throw the `CODE: detail` exception form.
 */
internal object ScriptAnswers {

    fun ok(version: Int, build: Bundle.() -> Unit = {}): Bundle = Bundle().apply {
        putInt(GalleryContract.KEY_CONTRACT_VERSION, version)
        build()
    }

    fun error(version: Int, code: String, detail: String?): Bundle = Bundle().apply {
        putInt(GalleryContract.KEY_CONTRACT_VERSION, version)
        putString(GalleryContract.KEY_ERROR_CODE, if (GalleryErrorCodes.isKnown(code)) code else GalleryErrorCodes.INTERNAL)
        putString(GalleryContract.KEY_ERROR_MESSAGE, errorDetail(detail))
    }

    /** The exception form of a failure, for calls that cannot answer with a Bundle. */
    fun failure(violation: ContractViolation): RuntimeException {
        val message = GalleryErrorCodes.encode(violation.code, errorDetail(violation.detail))
        return when (violation.code) {
            GalleryErrorCodes.INVALID_ARGUMENT, GalleryErrorCodes.UNSUPPORTED_FORMAT, GalleryErrorCodes.LIMIT_EXCEEDED -> IllegalArgumentException(message)
            else -> IllegalStateException(message)
        }
    }

    /** The contract version a request asks for; anything this plugin does not speak is refused. */
    fun versionOf(request: Bundle?): Int {
        val version = request?.getInt(GalleryContract.KEY_CONTRACT_VERSION, GalleryContract.CONTRACT_VERSION) ?: GalleryContract.CONTRACT_VERSION
        if (!GalleryContract.supportsContractVersion(version)) {
            throw ContractViolation(GalleryErrorCodes.INVALID_ARGUMENT, "unsupported contract version $version")
        }
        return version
    }

    /** Refuses an options Bundle above `MAX_OPTIONS_BYTES`. */
    fun checkOptionsSize(options: Bundle?) {
        if (options != null && parcelSize(options) > GalleryContract.MAX_OPTIONS_BYTES) {
            throw ContractViolation(GalleryErrorCodes.LIMIT_EXCEEDED, "options exceed ${GalleryContract.MAX_OPTIONS_BYTES} bytes")
        }
    }

    fun parcelSize(bundle: Bundle): Int {
        val parcel = Parcel.obtain()
        return try {
            parcel.writeBundle(bundle)
            parcel.dataSize()
        } finally {
            parcel.recycle()
        }
    }

    /** A detail cut to `MAX_ERROR_MESSAGE_BYTES` of UTF-8 without splitting a character. */
    fun errorDetail(detail: String?): String {
        val text = detail?.takeIf { it.isNotBlank() } ?: "no detail"
        val bytes = text.toByteArray(StandardCharsets.UTF_8)
        if (bytes.size <= GalleryContract.MAX_ERROR_MESSAGE_BYTES) return text
        var end = GalleryContract.MAX_ERROR_MESSAGE_BYTES
        while (end > 0 && (bytes[end].toInt() and 0xC0) == 0x80) end--
        return String(bytes, 0, end, StandardCharsets.UTF_8)
    }
}
