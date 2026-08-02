package io.github.supermonster003.autojs6.plugin.imageviewer

import android.content.ClipData
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import org.autojs.plugin.explorer.api.ExplorerActionIntentExtras
import org.autojs.plugin.explorer.api.ExplorerActionIntentValues
import org.autojs.plugin.explorer.api.ExplorerActionPluginActions
import org.autojs.plugin.explorer.api.ExplorerActionProtocol
import java.util.Locale

internal data class ImageViewerRequest(
    val targetUri: Uri,
    val displayName: String,
    val declaredSize: Long,
    val mimeType: String,
)

internal data class ExternalImageSeed(
    val targetUri: Uri,
    val mimeType: String,
)

/** Validates every value crossing the exported Explorer Action and ACTION_VIEW boundaries. */
internal object ImageRequestPolicy {

    const val MAX_DECLARED_SIZE = 8L * 1024L * 1024L * 1024L * 1024L
    const val MAX_DISPLAY_NAME_LENGTH = 255

    private val mimeTokenPattern = Regex("[a-z0-9][a-z0-9!#$&^_.+-]*")

    fun resolveExplorer(intent: Intent?): ImageViewerRequest? = try {
        resolveExplorerUnchecked(intent)
    } catch (_: RuntimeException) {
        null
    }

    fun resolveExternal(intent: Intent?): ExternalImageSeed? = try {
        intent ?: return null
        if (intent.action != Intent.ACTION_VIEW) return null
        if (!hasReadOnlyExternalGrant(intent)) return null
        val targetUri = intent.data?.takeIf(::isPlainContentUri) ?: return null
        val mimeType = normalizeImageMimeType(intent.type) ?: return null
        ExternalImageSeed(targetUri, mimeType)
    } catch (_: RuntimeException) {
        null
    }

    fun resolveInternal(intent: Intent?): ImageViewerRequest? = try {
        intent ?: return null
        if (intent.action != Intent.ACTION_VIEW) return null
        if (intent.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION == 0) return null
        if (intent.flags and FORBIDDEN_INTERNAL_GRANTS != 0) return null
        val targetUri = intent.data?.takeIf(::isPlainContentUri) ?: return null
        val clipData = intent.clipData ?: return null
        if (clipData.itemCount != 1 || !clipData.getItemAt(0).isExactUri(targetUri)) return null
        val displayName = validateDisplayName(intent.getStringExtra(EXTRA_DISPLAY_NAME)) ?: return null
        if (!intent.hasExtra(EXTRA_DECLARED_SIZE)) return null
        val size = intent.getLongExtra(EXTRA_DECLARED_SIZE, -1L)
            .takeIf(::isDeclaredSizeAccepted) ?: return null
        val mimeType = normalizeImageMimeType(intent.type) ?: return null
        ImageViewerRequest(targetUri, displayName, size, mimeType)
    } catch (_: RuntimeException) {
        null
    }

    fun viewerIntent(request: ImageViewerRequest): Intent =
        Intent(Intent.ACTION_VIEW).apply {
            setClassName(
                "io.github.supermonster003.autojs6.plugin.imageviewer",
                "io.github.supermonster003.autojs6.plugin.imageviewer.ImageViewerActivity",
            )
            setDataAndType(request.targetUri, request.mimeType)
            clipData = ClipData.newRawUri(request.displayName, request.targetUri)
            putExtra(EXTRA_DISPLAY_NAME, request.displayName)
            putExtra(EXTRA_DECLARED_SIZE, request.declaredSize)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

    fun normalizeImageMimeType(value: String?): String? {
        val raw = value ?: return null
        if (raw.isEmpty() || raw != raw.trim()) return null
        val mimeValue = raw.substringBefore(';')
        val normalized = mimeValue.lowercase(Locale.ROOT)
        if (mimeValue != normalized) return null
        val parts = normalized.split('/')
        if (parts.size != 2 || parts[0] != "image") return null
        val subtype = parts[1]
        if (subtype != "*" && !mimeTokenPattern.matches(subtype)) return null
        return "image/$subtype"
    }

    fun validateDisplayName(value: String?): String? {
        val name = value ?: return null
        if (name.length !in 1..MAX_DISPLAY_NAME_LENGTH || name.isBlank()) return null
        if (name == "." || name == "..") return null
        if (name.any(::isUnsafeNameCharacter)) return null
        return name
    }

    fun isDeclaredSizeAccepted(size: Long): Boolean = size in 0L..MAX_DECLARED_SIZE

    fun mimeTypesAreCompatible(declared: String, resolved: String): Boolean {
        val normalizedDeclared = normalizeImageMimeType(declared) ?: return false
        val normalizedResolved = normalizeImageMimeType(resolved) ?: return false
        return normalizedDeclared == "image/*" || normalizedDeclared == normalizedResolved
    }

    private fun resolveExplorerUnchecked(intent: Intent?): ImageViewerRequest? {
        intent ?: return null
        if (intent.action != ExplorerActionPluginActions.EXECUTE) return null
        if (intent.getStringExtra(ExplorerActionIntentExtras.ACTION_ID) != ImageViewerPlugin.ACTION_ID) {
            return null
        }
        if (
            intent.getIntExtra(ExplorerActionIntentExtras.PROTOCOL_VERSION, Int.MIN_VALUE) !=
            ExplorerActionProtocol.VERSION
        ) {
            return null
        }
        if (
            intent.getStringExtra(ExplorerActionIntentExtras.SOURCE_SURFACE) !=
            ExplorerActionIntentValues.SOURCE_SURFACE_MAIN
        ) {
            return null
        }
        if (intent.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION == 0) return null
        if (intent.flags and FORBIDDEN_EXPLORER_GRANTS != 0) return null

        val targetUri = intent.data?.takeIf(::isPlainContentUri) ?: return null
        val parentUri = intent.parcelableUriExtra(ExplorerActionIntentExtras.PARENT_URI)
            ?.takeIf(::isPlainContentUri)
            ?: return null
        if (!isStrictDescendant(parentUri, targetUri)) return null

        val clipData = intent.clipData ?: return null
        if (clipData.itemCount != REQUIRED_EXPLORER_CLIP_ITEM_COUNT) return null
        if (!clipData.getItemAt(ExplorerActionIntentValues.CLIP_ITEM_TARGET_INDEX).isExactUri(targetUri)) {
            return null
        }
        if (!clipData.getItemAt(ExplorerActionIntentValues.CLIP_ITEM_PARENT_INDEX).isExactUri(parentUri)) {
            return null
        }

        val displayName = validateDisplayName(
            intent.getStringExtra(ExplorerActionIntentExtras.DISPLAY_NAME),
        ) ?: return null
        if (targetUri.pathSegments.lastOrNull() != displayName) return null
        if (!intent.hasExtra(ExplorerActionIntentExtras.SIZE)) return null
        val declaredSize = intent.getLongExtra(ExplorerActionIntentExtras.SIZE, -1L)
            .takeIf(::isDeclaredSizeAccepted) ?: return null
        val mimeType = normalizeImageMimeType(intent.type) ?: return null

        return ImageViewerRequest(targetUri, displayName, declaredSize, mimeType)
    }

    private fun hasReadOnlyExternalGrant(intent: Intent): Boolean {
        if (intent.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION == 0) return false
        return intent.flags and FORBIDDEN_EXTERNAL_GRANTS == 0
    }

    private fun isPlainContentUri(uri: Uri): Boolean {
        if (!uri.isHierarchical || uri.scheme != ContentResolver.SCHEME_CONTENT) return false
        if (uri.authority.isNullOrBlank() || uri.host.isNullOrBlank()) return false
        if (uri.userInfo != null || uri.port != -1 || uri.query != null || uri.fragment != null) return false
        val encodedPath = uri.encodedPath ?: return false
        if (!encodedPath.startsWith('/') || encodedPath.length <= 1) return false
        if (encodedPath.split('/').drop(1).any(String::isEmpty)) return false
        return uri.pathSegments.isNotEmpty() && uri.pathSegments.none { segment ->
            segment.isEmpty() || segment == "." || segment == ".." || segment.any(::isUnsafeUriCharacter)
        }
    }

    private fun isStrictDescendant(parentUri: Uri, targetUri: Uri): Boolean {
        if (parentUri.scheme != targetUri.scheme || parentUri.authority != targetUri.authority) return false
        val parentSegments = parentUri.pathSegments
        val targetSegments = targetUri.pathSegments
        return targetSegments.size > parentSegments.size &&
            targetSegments.take(parentSegments.size) == parentSegments
    }

    private fun ClipData.Item.isExactUri(expected: Uri): Boolean =
        uri == expected && text == null && htmlText == null && intent == null

    private fun isUnsafeNameCharacter(character: Char): Boolean =
        character == '/' || character == '\\' || isUnsafeUnicodeCharacter(character)

    private fun isUnsafeUriCharacter(character: Char): Boolean =
        character == '/' || character == '\\' || isUnsafeUnicodeCharacter(character)

    private fun isUnsafeUnicodeCharacter(character: Char): Boolean =
        character.isISOControl() || Character.getType(character) == Character.FORMAT.toInt()

    @Suppress("DEPRECATION")
    private fun Intent.parcelableUriExtra(name: String): Uri? = getParcelableExtra(name)

    const val EXTRA_DISPLAY_NAME =
        "io.github.supermonster003.autojs6.plugin.imageviewer.extra.DISPLAY_NAME"
    const val EXTRA_DECLARED_SIZE =
        "io.github.supermonster003.autojs6.plugin.imageviewer.extra.DECLARED_SIZE"

    private const val REQUIRED_EXPLORER_CLIP_ITEM_COUNT = 2
    private const val FORBIDDEN_EXPLORER_GRANTS =
        Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
    private const val FORBIDDEN_EXTERNAL_GRANTS =
        Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
            Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION or
            Intent.FLAG_GRANT_PREFIX_URI_PERMISSION
    private const val FORBIDDEN_INTERNAL_GRANTS = FORBIDDEN_EXTERNAL_GRANTS
}
