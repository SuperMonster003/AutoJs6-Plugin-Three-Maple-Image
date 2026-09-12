package io.github.supermonster003.autojs6.plugin.imageviewer

import android.content.ClipData
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import java.util.Locale
import java.util.UUID
import org.autojs.plugin.explorer.api.ExplorerActionHostSessionKeys
import org.autojs.plugin.explorer.api.ExplorerActionIntentExtras
import org.autojs.plugin.explorer.api.ExplorerActionIntentValues
import org.autojs.plugin.explorer.api.ExplorerActionPluginActions
import org.autojs.plugin.explorer.api.ExplorerActionProtocol
import org.autojs.plugin.explorer.api.ExplorerActionTargetKeys
import org.autojs.plugin.explorer.api.ExplorerActionValues
import org.autojs.plugin.explorer.api.IExplorerActionHostSession

internal data class ImageViewerRequest(
    val targetUri: Uri,
    val displayName: String,
    val declaredSize: Long,
    val mimeType: String,
)

internal data class ExplorerImageTarget(
    val id: String,
    val image: ImageViewerRequest,
    val lastModified: Long,
)

internal data class ExplorerImageRequest(
    val requestId: String,
    val parentUri: Uri,
    val parentDisplayPath: String,
    val targets: List<ExplorerImageTarget>,
    val mode: ExplorerImageRequestMode,
    val hostSession: IExplorerActionHostSession?,
)

internal enum class ExplorerImageRequestMode {
    SINGLE_WITH_SIBLINGS,
    EXPLICIT_SELECTION,
}

internal data class ExternalImageSeed(
    val targetUri: Uri,
    val mimeType: String,
)

/** Validates every value crossing the exported Explorer Action and ACTION_VIEW boundaries. */
internal object ImageRequestPolicy {

    const val MAX_DECLARED_SIZE = 8L * 1024L * 1024L * 1024L * 1024L
    const val MAX_DISPLAY_NAME_LENGTH = 255

    private const val INVALID_LAST_MODIFIED = -1L
    private const val MAX_REQUEST_ID_LENGTH = 36
    private val mimeTokenPattern = Regex("[a-z0-9][a-z0-9!#$&^_.+-]*")

    fun resolveExplorer(intent: Intent?): ExplorerImageRequest? = try {
        resolveExplorerUnchecked(intent)
    } catch (_: RuntimeException) {
        null
    }

    fun resolveExternal(intent: Intent?): ExternalImageSeed? {
        return try {
            intent ?: return null
            if (intent.action != Intent.ACTION_VIEW) return null
            if (!hasReadOnlyExternalGrant(intent)) return null
            val targetUri = intent.data?.takeIf(::isPlainContentUri) ?: return null
            val mimeType = normalizeImageMimeType(intent.type) ?: return null
            ExternalImageSeed(targetUri, mimeType)
        } catch (_: RuntimeException) {
            null
        }
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
        return normalizedDeclared == "image/*" ||
            ImageFormatSupport.mimeTypesAreEquivalent(normalizedDeclared, normalizedResolved)
    }

    private fun resolveExplorerUnchecked(intent: Intent?): ExplorerImageRequest? {
        intent ?: return null
        if (intent.action != ExplorerActionPluginActions.EXECUTE) return null
        val mode = when (intent.getStringExtra(ExplorerActionIntentExtras.ACTION_ID)) {
            ImageViewerPlugin.ACTION_ID -> ExplorerImageRequestMode.SINGLE_WITH_SIBLINGS
            ImageViewerPlugin.MULTIPLE_ACTION_ID -> ExplorerImageRequestMode.EXPLICIT_SELECTION
            else -> return null
        }
        if (
            intent.getIntExtra(ExplorerActionIntentExtras.PROTOCOL_VERSION, Int.MIN_VALUE) !=
            ImageViewerPlugin.PROTOCOL_VERSION
        ) {
            return null
        }
        if (
            intent.getStringExtra(ExplorerActionIntentExtras.SOURCE_SURFACE) !=
            ExplorerActionIntentValues.SOURCE_SURFACE_MAIN
        ) {
            return null
        }
        if (!hasExactReadOnlyExplorerFlags(intent.flags)) return null

        val requestId = canonicalRequestId(
            intent.getStringExtra(ExplorerActionIntentExtras.REQUEST_ID),
        ) ?: return null
        val parentUri = intent.parcelableUriExtra(ExplorerActionIntentExtras.PARENT_URI)
            ?.takeIf(::isPlainContentUri)
            ?: return null
        val parentDisplayPath = validateParentDisplayPath(
            intent.getStringExtra(ExplorerActionIntentExtras.PARENT_DISPLAY_PATH),
        ) ?: return null
        val targets = intent.parcelableBundleArrayListExtra(ExplorerActionIntentExtras.TARGETS)
            ?.takeIf { it.size in 1..ExplorerActionProtocol.MAX_TARGETS_PER_REQUEST }
            ?: return null
        if (mode == ExplorerImageRequestMode.SINGLE_WITH_SIBLINGS && targets.size != 1) return null
        val clipData = intent.clipData ?: return null
        if (clipData.itemCount != targets.size) return null
        val parsedTargets = targets.mapIndexed { index, targetBundle ->
            parseExplorerTarget(parentUri, targetBundle)?.also { target ->
                if (!clipData.getItemAt(index).isExactUri(target.value.image.targetUri)) return null
            } ?: return null
        }
        if (parsedTargets.map { it.value.id }.toSet().size != parsedTargets.size) return null
        if (parsedTargets.map { it.value.image.targetUri }.toSet().size != parsedTargets.size) return null
        if (parsedTargets.map { it.value.image.displayName }.toSet().size != parsedTargets.size) return null

        val firstTarget = parsedTargets.first()
        if (intent.data != firstTarget.value.image.targetUri) return null
        val expectedIntentType = if (parsedTargets.size == 1) firstTarget.declaredMimeType else WILDCARD_MIME_TYPE
        if (intent.type != expectedIntentType) return null
        if (intent.getStringExtra(ExplorerActionIntentExtras.DISPLAY_NAME) != firstTarget.value.image.displayName) {
            return null
        }
        if (!intent.hasExtra(ExplorerActionIntentExtras.SIZE)) return null
        if (
            intent.getLongExtra(ExplorerActionIntentExtras.SIZE, Long.MIN_VALUE) !=
            firstTarget.value.image.declaredSize
        ) {
            return null
        }
        if (
            intent.getLongExtra(ExplorerActionIntentExtras.HOST_VERSION_CODE, Long.MIN_VALUE) <
            ImageViewerPlugin.REQUIRED_HOST_VERSION
        ) {
            return null
        }
        val sessionBundle = intent.parcelableBundleExtra(ExplorerActionIntentExtras.HOST_SESSION)
        val requiresHostSession =
            mode == ExplorerImageRequestMode.SINGLE_WITH_SIBLINGS || parsedTargets.size > 1
        val hostSession = if (requiresHostSession) {
            val binder = sessionBundle?.getBinder(ExplorerActionHostSessionKeys.BINDER) ?: return null
            if (runCatching { binder.interfaceDescriptor }.getOrNull() != IExplorerActionHostSession.DESCRIPTOR) {
                return null
            }
            IExplorerActionHostSession.Stub.asInterface(binder) ?: return null
        } else {
            if (sessionBundle != null) return null
            null
        }

        return ExplorerImageRequest(
            requestId = requestId,
            parentUri = parentUri,
            parentDisplayPath = parentDisplayPath,
            targets = parsedTargets.map(ParsedExplorerTarget::value),
            mode = mode,
            hostSession = hostSession,
        )
    }

    private fun parseExplorerTarget(parentUri: Uri, bundle: Bundle): ParsedExplorerTarget? {
        val targetId = validateOpaqueId(bundle.getString(ExplorerActionTargetKeys.ID)) ?: return null
        val targetUri = bundle.parcelableUri(ExplorerActionTargetKeys.URI)
            ?.takeIf(::isPlainContentUri)
            ?: return null
        if (!isDirectChild(parentUri, targetUri)) return null
        val displayName = validateDisplayName(bundle.getString(ExplorerActionTargetKeys.DISPLAY_NAME))
            ?: return null
        if (targetUri.pathSegments.lastOrNull() != displayName) return null
        if (
            bundle.getInt(ExplorerActionTargetKeys.KIND, Int.MIN_VALUE) !=
            ExplorerActionValues.TARGET_FILE
        ) {
            return null
        }
        val declaredMimeType = bundle.getString(ExplorerActionTargetKeys.MIME_TYPE) ?: return null
        val mimeType = ImageFormatSupport.explorerMimeType(displayName, declaredMimeType) ?: return null
        if (!bundle.containsKey(ExplorerActionTargetKeys.SIZE)) return null
        val declaredSize = bundle.getLong(ExplorerActionTargetKeys.SIZE, -1L)
            .takeIf(::isDeclaredSizeAccepted)
            ?: return null
        if (!bundle.containsKey(ExplorerActionTargetKeys.LAST_MODIFIED)) return null
        val lastModified = bundle.getLong(
            ExplorerActionTargetKeys.LAST_MODIFIED,
            Long.MIN_VALUE,
        ).takeIf { it >= INVALID_LAST_MODIFIED } ?: return null
        return ParsedExplorerTarget(
            value = ExplorerImageTarget(
                id = targetId,
                image = ImageViewerRequest(targetUri, displayName, declaredSize, mimeType),
                lastModified = lastModified,
            ),
            declaredMimeType = declaredMimeType,
        )
    }

    private fun hasReadOnlyExternalGrant(intent: Intent): Boolean {
        if (intent.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION == 0) return false
        return intent.flags and FORBIDDEN_EXTERNAL_GRANTS == 0
    }

    private fun hasExactReadOnlyExplorerFlags(flags: Int): Boolean =
        flags and REQUIRED_EXPLORER_FLAGS == REQUIRED_EXPLORER_FLAGS &&
            flags and ALLOWED_EXPLORER_FLAGS.inv() == 0

    private fun canonicalRequestId(value: String?): String? {
        val requestId = value?.takeIf { it.length <= MAX_REQUEST_ID_LENGTH } ?: return null
        val parsed = runCatching { UUID.fromString(requestId) }.getOrNull() ?: return null
        return requestId.takeIf { parsed.toString().equals(requestId, ignoreCase = true) }
    }

    private fun validateParentDisplayPath(value: String?): String? {
        val path = value ?: return null
        if (path.length !in 1..ExplorerActionProtocol.MAX_PARENT_DISPLAY_PATH_LENGTH) return null
        return path.takeIf { candidate -> candidate.none(::isUnsafeUnicodeCharacter) }
    }

    private fun validateOpaqueId(value: String?): String? {
        val id = value ?: return null
        if (id.length !in 1..ExplorerActionProtocol.MAX_TARGET_ID_LENGTH) return null
        return id.takeIf { candidate ->
            candidate.none { it.isWhitespace() || isUnsafeUnicodeCharacter(it) }
        }
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

    private fun isDirectChild(parentUri: Uri, targetUri: Uri): Boolean {
        if (parentUri.scheme != targetUri.scheme || parentUri.authority != targetUri.authority) return false
        val parentSegments = parentUri.pathSegments
        val targetSegments = targetUri.pathSegments
        return targetSegments.size == parentSegments.size + 1 &&
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

    @Suppress("DEPRECATION")
    private fun Intent.parcelableBundleExtra(name: String): Bundle? = getParcelableExtra(name)

    @Suppress("DEPRECATION")
    private fun Intent.parcelableBundleArrayListExtra(name: String): ArrayList<Bundle>? =
        getParcelableArrayListExtra(name)

    @Suppress("DEPRECATION")
    private fun Bundle.parcelableUri(name: String): Uri? = getParcelable(name)

    private const val REQUIRED_EXPLORER_FLAGS =
        Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PREFIX_URI_PERMISSION
    private const val ALLOWED_EXPLORER_FLAGS =
        REQUIRED_EXPLORER_FLAGS or
            Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS or
            Intent.FLAG_ACTIVITY_NEW_TASK
    private const val FORBIDDEN_EXTERNAL_GRANTS =
        Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
            Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION or
            Intent.FLAG_GRANT_PREFIX_URI_PERMISSION
    private const val WILDCARD_MIME_TYPE = "*/*"

    private data class ParsedExplorerTarget(
        val value: ExplorerImageTarget,
        val declaredMimeType: String,
    )
}
