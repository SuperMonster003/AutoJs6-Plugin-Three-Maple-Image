package io.github.supermonster003.autojs6.plugin.imagetools

import android.content.ClipData
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Process
import org.autojs.plugin.explorer.api.ExplorerActionIntentExtras
import org.autojs.plugin.explorer.api.ExplorerActionIntentValues
import org.autojs.plugin.explorer.api.ExplorerActionPluginActions
import org.autojs.plugin.explorer.api.ExplorerActionProtocol
import java.util.Locale
import java.util.UUID

internal data class ImageToolsRequest(
    val actionId: String,
    val inputUri: Uri,
    val outputUri: Uri,
    val transactionId: String,
    val displayName: String,
    val declaredSize: Long,
    val declaredMimeType: String,
    val allowedOutputMimeTypes: Set<String>,
    val maxOutputBytes: Long,
    val outputNameSuffix: String,
)

/** Strictly validates the v3 read-only input and host-owned output transaction. */
internal object ImageToolsRequestPolicy {

    fun resolve(context: Context, intent: Intent?, expectedActionId: String): ImageToolsRequest? {
        return try {
            intent ?: return null
            if (expectedActionId !in SUPPORTED_ACTION_IDS) return null
            if (intent.action != ExplorerActionPluginActions.EXECUTE) return null
            if (intent.getStringExtra(ExplorerActionIntentExtras.ACTION_ID) != expectedActionId) return null
            if (
                intent.getIntExtra(ExplorerActionIntentExtras.PROTOCOL_VERSION, Int.MIN_VALUE) !=
                ExplorerActionProtocol.VERSION
            ) return null
            if (
                intent.getStringExtra(ExplorerActionIntentExtras.SOURCE_SURFACE) !=
                ExplorerActionIntentValues.SOURCE_SURFACE_MAIN
            ) return null
            if (!hasExactReadOnlyIntentGrant(intent)) return null
            if (intent.hasExtra(ExplorerActionIntentExtras.PARENT_URI)) return null

            val inputUri = intent.data?.takeIf(::isPlainContentUri) ?: return null
            val outputUri = intent.parcelableUriExtra(ExplorerActionIntentExtras.OUTPUT_URI)
                ?.takeIf(::isPlainContentUri)
                ?: return null
            if (inputUri == outputUri) return null
            if (!hasExactInputClip(intent.clipData, inputUri)) return null
            if (!hasExpectedPermissions(context, inputUri, outputUri)) return null

            val transactionId = intent.getStringExtra(ExplorerActionIntentExtras.OUTPUT_TRANSACTION_ID)
                ?.takeIf(::isUuid)
                ?: return null
            val displayName = validateDisplayName(
                intent.getStringExtra(ExplorerActionIntentExtras.DISPLAY_NAME),
            ) ?: return null
            if (!intent.hasExtra(ExplorerActionIntentExtras.SIZE)) return null
            val declaredSize = intent.getLongExtra(ExplorerActionIntentExtras.SIZE, -1L)
                .takeIf { it in 1L..MAX_INPUT_BYTES }
                ?: return null
            val declaredMimeType = normalizeMimeType(intent.type)
                ?.takeIf {
                    it.startsWith("image/") ||
                        it == GENERIC_BINARY_MIME ||
                        it == GLOBAL_WILDCARD_MIME
                }
                ?: return null
            val allowedOutputMimeTypes = normalizeOutputMimeTypes(
                intent.getStringArrayListExtra(ExplorerActionIntentExtras.OUTPUT_MIME_TYPES),
            )
                ?: return null
            val maxOutputBytes = intent.getLongExtra(ExplorerActionIntentExtras.MAX_OUTPUT_BYTES, -1L)
                .takeIf { it in 1L..ImageToolsPlugin.MAX_OUTPUT_BYTES }
                ?: return null
            val expectedSuffix = if (expectedActionId == ImageToolsPlugin.EDIT_ACTION_ID) "edited" else "converted"
            val outputNameSuffix = intent.getStringExtra(ExplorerActionIntentExtras.OUTPUT_NAME_SUFFIX)
                ?.takeIf { it == expectedSuffix }
                ?: return null

            if (!canOpenInput(context.contentResolver, inputUri)) return null
            ImageToolsRequest(
                actionId = expectedActionId,
                inputUri = inputUri,
                outputUri = outputUri,
                transactionId = transactionId,
                displayName = displayName,
                declaredSize = declaredSize,
                declaredMimeType = declaredMimeType,
                allowedOutputMimeTypes = allowedOutputMimeTypes,
                maxOutputBytes = maxOutputBytes,
                outputNameSuffix = outputNameSuffix,
            )
        } catch (_: RuntimeException) {
            null
        }
    }

    internal fun hasExactReadOnlyIntentGrant(intent: Intent): Boolean {
        if (intent.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION == 0) return false
        return intent.flags and FORBIDDEN_INTENT_GRANTS == 0
    }

    internal fun hasExactInputClip(clipData: ClipData?, inputUri: Uri): Boolean {
        clipData ?: return false
        if (clipData.itemCount != 1) return false
        val item = clipData.getItemAt(0)
        return item.uri == inputUri && item.text == null && item.htmlText == null && item.intent == null
    }

    internal fun isPlainContentUri(uri: Uri): Boolean {
        if (!uri.isHierarchical || uri.scheme != ContentResolver.SCHEME_CONTENT) return false
        if (uri.authority.isNullOrBlank() || uri.host.isNullOrBlank()) return false
        if (uri.userInfo != null || uri.port != -1 || uri.query != null || uri.fragment != null) return false
        val path = uri.encodedPath ?: return false
        if (!path.startsWith('/') || path.length <= 1) return false
        return uri.pathSegments.isNotEmpty() && uri.pathSegments.none { segment ->
            segment.isEmpty() || segment == "." || segment == ".." ||
                segment.any { it == '/' || it == '\\' || it.isISOControl() || Character.getType(it) == Character.FORMAT.toInt() }
        }
    }

    internal fun normalizeMimeType(value: String?): String? {
        val raw = value ?: return null
        if (raw.isEmpty() || raw != raw.trim() || ';' in raw) return null
        val normalized = raw.lowercase(Locale.ROOT)
        if (normalized != raw) return null
        if (normalized == GLOBAL_WILDCARD_MIME) return normalized
        val parts = normalized.split('/')
        if (parts.size != 2 || parts.any(String::isBlank)) return null
        if (!MIME_TOKEN.matches(parts[0])) return null
        if (parts[1] != "*" && !MIME_TOKEN.matches(parts[1])) return null
        return normalized
    }

    internal fun normalizeOutputMimeTypes(values: List<String>?): Set<String>? {
        val requested = values?.takeIf { it.isNotEmpty() } ?: return null
        val normalized = requested.map { normalizeMimeType(it) ?: return null }
        if (normalized.distinct().size != requested.size) return null
        return normalized.toSet().takeIf { it.all(SUPPORTED_OUTPUT_MIME_TYPES::contains) }
    }

    private fun hasExpectedPermissions(context: Context, inputUri: Uri, outputUri: Uri): Boolean {
        val pid = Process.myPid()
        val uid = Process.myUid()
        fun granted(uri: Uri, mode: Int) =
            context.checkUriPermission(uri, pid, uid, mode) == PackageManager.PERMISSION_GRANTED
        return hasExpectedGrantState(
            inputRead = granted(inputUri, Intent.FLAG_GRANT_READ_URI_PERMISSION),
            inputWrite = granted(inputUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION),
            outputRead = granted(outputUri, Intent.FLAG_GRANT_READ_URI_PERMISSION),
            outputWrite = granted(outputUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION),
        )
    }

    internal fun hasExpectedGrantState(
        inputRead: Boolean,
        inputWrite: Boolean,
        outputRead: Boolean,
        outputWrite: Boolean,
    ): Boolean = inputRead && !inputWrite && !outputRead && outputWrite

    private fun canOpenInput(resolver: ContentResolver, inputUri: Uri): Boolean = runCatching {
        resolver.openFileDescriptor(inputUri, ImageToolsPlugin.INPUT_OPEN_MODE)?.use {
            check(it.fileDescriptor.valid())
        }
            ?: return@runCatching false
        true
    }.getOrDefault(false)

    internal fun validateDisplayName(value: String?): String? = value
        ?.takeIf { it.length in 1..255 && it.isNotBlank() }
        ?.takeIf { name ->
            name != "." && name != ".." && name.none {
                it == '/' || it == '\\' || it.isISOControl() ||
                    Character.getType(it) == Character.FORMAT.toInt()
            }
        }

    private fun isUuid(value: String): Boolean = runCatching {
        UUID.fromString(value).toString() == value.lowercase(Locale.ROOT)
    }.getOrDefault(false)

    @Suppress("DEPRECATION")
    private fun Intent.parcelableUriExtra(name: String): Uri? = getParcelableExtra(name)

    private val SUPPORTED_ACTION_IDS = setOf(ImageToolsPlugin.EDIT_ACTION_ID, ImageToolsPlugin.CONVERT_ACTION_ID)
    private val SUPPORTED_OUTPUT_MIME_TYPES = ImageToolsPlugin.OUTPUT_MIME_TYPES.toSet()
    private val MIME_TOKEN = Regex("[a-z0-9][a-z0-9!#$&^_.+-]*")
    private const val GENERIC_BINARY_MIME = "application/octet-stream"
    private const val GLOBAL_WILDCARD_MIME = "*/*"
    internal const val MAX_INPUT_BYTES = 256L * 1024L * 1024L
    private const val FORBIDDEN_INTENT_GRANTS =
        Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
            Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION or
            Intent.FLAG_GRANT_PREFIX_URI_PERMISSION
}
