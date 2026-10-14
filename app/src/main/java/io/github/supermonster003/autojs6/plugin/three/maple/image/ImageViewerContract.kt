package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.content.ClipData
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import org.autojs.plugin.explorer.api.ExplorerActionHostSessionKeys
import org.autojs.plugin.explorer.api.ExplorerActionProtocol
import org.autojs.plugin.explorer.api.IExplorerActionHostSession

internal data class ImageViewerPage(
    val image: ImageViewerRequest,
    val hostRelativePath: String? = null,
)

internal data class ImageViewerLaunchRequest(
    val pages: List<ImageViewerPage>,
    val startIndex: Int = 0,
    val hostSession: IExplorerActionHostSession? = null,
    val hostTargetId: String? = null,
    /** True only for the standalone and external entries, where the viewer may hand the image to the editor (R5). */
    val editable: Boolean = false,
    /** Gallery album the viewer enumerates itself; [pages] then holds only the start page (ROADMAP 6.3). */
    val source: ViewerSourceSpec? = null,
    /**
     * False when another Activity (the directory browser, ROADMAP 6.4) keeps the host session alive: the viewer
     * then uses the session but never closes it.
     */
    val ownsHostSession: Boolean = true,
) {
    init {
        require(pages.size in 1..ImageSiblingDiscoveryPolicy.MAX_PAGES)
        require(startIndex in pages.indices)
        require((hostSession == null) == (hostTargetId == null))
        if (source != null) {
            require(hostSession == null)
            require(pages.size == 1)
        }
        if (hostSession == null) {
            require(pages.all { it.hostRelativePath == null })
        } else {
            require(pages.all { it.hostRelativePath != null })
            // The host single-image entry carries the selected image as a direct URI page; the directory browser
            // (nested relative paths) has no such page.
            val selectedPages = pages.count { it.hostRelativePath == "" }
            require(selectedPages <= 1)
            if (selectedPages == 1) {
                require(pages.size > 1)
                require(pages[startIndex].hostRelativePath == "")
            }
        }
    }

    val startPage: ImageViewerPage
        get() = pages[startIndex]

    companion object {
        fun single(image: ImageViewerRequest, editable: Boolean = false): ImageViewerLaunchRequest =
            ImageViewerLaunchRequest(pages = listOf(ImageViewerPage(image)), editable = editable)

        fun explicitSelection(images: List<ImageViewerRequest>): ImageViewerLaunchRequest {
            require(images.isNotEmpty())
            return ImageViewerLaunchRequest(images.map { image -> ImageViewerPage(image) })
        }

        /** The standalone gallery's entry: the tapped item plus the album the viewer enumerates itself. */
        fun gallery(start: ImageViewerRequest, spec: ViewerSourceSpec): ImageViewerLaunchRequest =
            ImageViewerLaunchRequest(pages = listOf(ImageViewerPage(start)), editable = true, source = spec)
    }
}

/** Private, revalidated handoff from ingress activities to [ImageViewerActivity]. */
internal object ImageViewerContract {

    private const val HOST_IMAGE_SCHEME = "autojs6-explorer"
    private const val HOST_IMAGE_AUTHORITY = "image"
    private const val EXTRA_PAGE_MIME_TYPES =
        "io.github.supermonster003.autojs6.plugin.three.maple.image.extra.PAGE_MIME_TYPES"
    private const val EXTRA_PAGE_DISPLAY_NAMES =
        "io.github.supermonster003.autojs6.plugin.three.maple.image.extra.PAGE_DISPLAY_NAMES"
    private const val EXTRA_PAGE_DECLARED_SIZES =
        "io.github.supermonster003.autojs6.plugin.three.maple.image.extra.PAGE_DECLARED_SIZES"
    private const val EXTRA_START_INDEX =
        "io.github.supermonster003.autojs6.plugin.three.maple.image.extra.START_INDEX"
    private const val EXTRA_HOST_REQUEST =
        "io.github.supermonster003.autojs6.plugin.three.maple.image.extra.HOST_REQUEST"
    private const val EXTRA_EDITABLE =
        "io.github.supermonster003.autojs6.plugin.three.maple.image.extra.EDITABLE"
    private const val EXTRA_SOURCE =
        "io.github.supermonster003.autojs6.plugin.three.maple.image.extra.SOURCE"

    /** Result extras of a gallery-sourced viewer: the item the user left on, so the grid can scroll to it. */
    const val EXTRA_RESULT_ITEM_ID =
        "io.github.supermonster003.autojs6.plugin.three.maple.image.extra.RESULT_ITEM_ID"
    const val EXTRA_RESULT_INDEX =
        "io.github.supermonster003.autojs6.plugin.three.maple.image.extra.RESULT_INDEX"
    private const val SOURCE_KIND = "kind"
    private const val SOURCE_KIND_MEDIA = "media"
    private const val SOURCE_KIND_FOLDER = "folder"
    private const val SOURCE_BUCKET_ID = "bucketId"
    private const val SOURCE_TREE_URI = "treeUri"
    private const val SOURCE_KIND_HIDDEN = "hidden"
    private const val SOURCE_RELATIVE_PATH = "relativePath"
    private const val SOURCE_SORT_FIELD = "sortField"
    private const val SOURCE_SORT_ASCENDING = "sortAscending"
    private const val SOURCE_START_ITEM_ID = "startItemId"
    private const val HOST_TARGET_ID = "targetId"
    private const val HOST_RELATIVE_PATHS = "relativePaths"
    private const val HOST_OWNS_SESSION = "ownsSession"
    private const val MAX_RELATIVE_PATH_DEPTH = 64

    fun viewerIntent(context: Context, request: ImageViewerLaunchRequest): Intent =
        Intent(context, ImageViewerActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            putReadOnlyPayload(request)
        }

    fun resolve(intent: Intent?): ImageViewerLaunchRequest? = runCatching {
        intent ?: return null
        if (intent.action != Intent.ACTION_VIEW) return null
        if (intent.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION == 0) return null
        if (intent.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION.inv() != 0) return null

        val clipData = intent.clipData ?: return null
        val count = clipData.itemCount
        if (count !in 1..ImageSiblingDiscoveryPolicy.MAX_PAGES) return null
        val mimeTypes = intent.getStringArrayListExtra(EXTRA_PAGE_MIME_TYPES)
            ?.takeIf { it.size == count }
            ?: return null
        val displayNames = intent.getStringArrayListExtra(EXTRA_PAGE_DISPLAY_NAMES)
            ?.takeIf { it.size == count }
            ?: return null
        val declaredSizes = intent.getLongArrayExtra(EXTRA_PAGE_DECLARED_SIZES)
            ?.takeIf { it.size == count }
            ?: return null
        val startIndex = intent.getIntExtra(EXTRA_START_INDEX, -1)
            .takeIf { it in 0 until count }
            ?: return null

        val hostBundle = intent.parcelableBundleExtra(EXTRA_HOST_REQUEST)
        val hostSession: IExplorerActionHostSession?
        val hostTargetId: String?
        val hostRelativePaths: List<String?>
        if (hostBundle == null) {
            hostSession = null
            hostTargetId = null
            hostRelativePaths = List(count) { null }
        } else {
            val binder = hostBundle.getBinder(ExplorerActionHostSessionKeys.BINDER) ?: return null
            if (runCatching { binder.interfaceDescriptor }.getOrNull() != IExplorerActionHostSession.DESCRIPTOR) {
                return null
            }
            hostSession = IExplorerActionHostSession.Stub.asInterface(binder) ?: return null
            hostTargetId = validateOpaqueId(hostBundle.getString(HOST_TARGET_ID)) ?: return null
            hostRelativePaths = hostBundle.getStringArrayList(HOST_RELATIVE_PATHS)
                ?.takeIf { it.size == count }
                ?: return null
        }

        val sourceBundle = intent.parcelableBundleExtra(EXTRA_SOURCE)
        val source = if (sourceBundle == null) {
            null
        } else {
            readSourceSpec(sourceBundle, hasHost = hostBundle != null, pageCount = count) ?: return null
        }

        val pages = (0 until count).map { index ->
            val item = clipData.getItemAt(index)
            val uri = item.uri ?: return null
            if (!item.isExactUri(uri)) return null
            val relativePath = hostRelativePaths[index]
            when {
                hostSession == null -> if (!isPlainContentUri(uri) || relativePath != null) return null
                relativePath == "" -> if (!isPlainContentUri(uri) || index != startIndex) return null
                relativePath?.let(::isSafeRelativePath) != true -> return null
                !isHostImageUri(uri, index) -> return null
            }
            val displayName = ImageRequestPolicy.validateDisplayName(displayNames[index]) ?: return null
            if (!relativePath.isNullOrEmpty() && relativePath.substringAfterLast('/') != displayName) return null
            val mimeType = ImageRequestPolicy.normalizeImageMimeType(mimeTypes[index]) ?: return null
            val declaredSize = declaredSizes[index]
                .takeIf(ImageRequestPolicy::isDeclaredSizeAccepted)
                ?: return null
            ImageViewerPage(
                image = ImageViewerRequest(uri, displayName, declaredSize, mimeType),
                hostRelativePath = relativePath,
            )
        }
        if (pages.map { it.image.targetUri }.toSet().size != pages.size) return null
        if (hostSession != null && pages.count { it.hostRelativePath == "" } > 1) return null
        val startPage = pages[startIndex]
        if (intent.data != startPage.image.targetUri) return null
        if (ImageRequestPolicy.normalizeImageMimeType(intent.type) != startPage.image.mimeType) return null

        ImageViewerLaunchRequest(
            pages = pages,
            startIndex = startIndex,
            hostSession = hostSession,
            hostTargetId = hostTargetId,
            editable = hostSession == null && intent.getBooleanExtra(EXTRA_EDITABLE, false),
            source = source,
            ownsHostSession = hostBundle?.getBoolean(HOST_OWNS_SESSION, true) ?: true,
        )
    }.getOrNull()

    /**
     * A host session relative path as the viewer and the image provider accept it: one or more safe name segments
     * joined by `/`, never `.` or `..`, bounded in depth and length (the host validates the same shape).
     */
    fun isSafeRelativePath(value: String): Boolean {
        if (value.isEmpty() || value.length > ExplorerActionProtocol.MAX_SESSION_RELATIVE_PATH_LENGTH) return false
        val segments = value.split('/')
        if (segments.size > MAX_RELATIVE_PATH_DEPTH) return false
        return segments.all { segment ->
            segment != "." && segment != ".." && ImageRequestPolicy.validateDisplayName(segment) != null
        }
    }

    private fun readSourceSpec(bundle: Bundle, hasHost: Boolean, pageCount: Int): ViewerSourceSpec? {
        if (hasHost || pageCount != 1) return null
        val field = bundle.getString(SOURCE_SORT_FIELD)
            ?.let { name -> MediaSortField.entries.firstOrNull { it.name == name } }
            ?: return null
        val sort = MediaSort(field, bundle.getBoolean(SOURCE_SORT_ASCENDING, false))
        if (!bundle.containsKey(SOURCE_START_ITEM_ID)) return null
        val startItemId = bundle.getLong(SOURCE_START_ITEM_ID)
        return when (bundle.getString(SOURCE_KIND)) {
            SOURCE_KIND_MEDIA -> {
                if (!bundle.containsKey(SOURCE_BUCKET_ID)) return null
                ViewerSourceSpec.MediaAlbum(bundle.getLong(SOURCE_BUCKET_ID), sort, startItemId)
            }
            SOURCE_KIND_FOLDER -> {
                val treeUri = bundle.getString(SOURCE_TREE_URI)
                    ?.takeIf { isPlainContentUri(Uri.parse(it)) }
                    ?: return null
                ViewerSourceSpec.AuthorizedFolder(treeUri, sort, startItemId)
            }
            SOURCE_KIND_HIDDEN -> {
                val relativePath = bundle.getString(SOURCE_RELATIVE_PATH)?.takeIf(::isSafeRelativeFolder) ?: return null
                ViewerSourceSpec.HiddenFolder(relativePath, sort, startItemId)
            }
            else -> null
        }
    }

    fun closeAttachedHostSession(intent: Intent?) {
        val hostBundle = runCatching { intent?.parcelableBundleExtra(EXTRA_HOST_REQUEST) }.getOrNull() ?: return
        // A borrowed session belongs to the Activity that launched the viewer and stays open for it.
        if (!hostBundle.getBoolean(HOST_OWNS_SESSION, true)) return
        val binder = runCatching { hostBundle.getBinder(ExplorerActionHostSessionKeys.BINDER) }.getOrNull() ?: return
        if (runCatching { binder.interfaceDescriptor }.getOrNull() != IExplorerActionHostSession.DESCRIPTOR) {
            return
        }
        runCatching { IExplorerActionHostSession.Stub.asInterface(binder)?.close() }
        intent?.removeExtra(EXTRA_HOST_REQUEST)
    }

    fun hostImageUri(index: Int): Uri = Uri.Builder()
        .scheme(HOST_IMAGE_SCHEME)
        .authority(HOST_IMAGE_AUTHORITY)
        .appendPath(index.toString())
        .build()

    private fun Intent.putReadOnlyPayload(request: ImageViewerLaunchRequest) {
        val startPage = request.startPage.image
        setDataAndType(startPage.targetUri, startPage.mimeType)
        clipData = ClipData.newRawUri(request.pages.first().image.displayName, request.pages.first().image.targetUri)
            .apply {
                request.pages.drop(1).forEach { page -> addItem(ClipData.Item(page.image.targetUri)) }
            }
        putStringArrayListExtra(
            EXTRA_PAGE_MIME_TYPES,
            ArrayList(request.pages.map { it.image.mimeType }),
        )
        putStringArrayListExtra(
            EXTRA_PAGE_DISPLAY_NAMES,
            ArrayList(request.pages.map { it.image.displayName }),
        )
        putExtra(EXTRA_PAGE_DECLARED_SIZES, request.pages.map { it.image.declaredSize }.toLongArray())
        putExtra(EXTRA_START_INDEX, request.startIndex)
        putExtra(EXTRA_EDITABLE, request.editable)
        request.source?.let { spec ->
            putExtra(
                EXTRA_SOURCE,
                Bundle().apply {
                    putString(SOURCE_SORT_FIELD, spec.sort.field.name)
                    putBoolean(SOURCE_SORT_ASCENDING, spec.sort.ascending)
                    putLong(SOURCE_START_ITEM_ID, spec.startItemId)
                    when (spec) {
                        is ViewerSourceSpec.MediaAlbum -> {
                            putString(SOURCE_KIND, SOURCE_KIND_MEDIA)
                            putLong(SOURCE_BUCKET_ID, spec.bucketId)
                        }
                        is ViewerSourceSpec.AuthorizedFolder -> {
                            putString(SOURCE_KIND, SOURCE_KIND_FOLDER)
                            putString(SOURCE_TREE_URI, spec.treeUri)
                        }
                        is ViewerSourceSpec.HiddenFolder -> {
                            putString(SOURCE_KIND, SOURCE_KIND_HIDDEN)
                            putString(SOURCE_RELATIVE_PATH, spec.relativePath)
                        }
                    }
                },
            )
        }
        request.hostSession?.let { session ->
            putExtra(
                EXTRA_HOST_REQUEST,
                Bundle().apply {
                    putBinder(ExplorerActionHostSessionKeys.BINDER, session.asBinder())
                    putString(HOST_TARGET_ID, requireNotNull(request.hostTargetId))
                    putBoolean(HOST_OWNS_SESSION, request.ownsHostSession)
                    putStringArrayList(
                        HOST_RELATIVE_PATHS,
                        ArrayList(request.pages.map { requireNotNull(it.hostRelativePath) }),
                    )
                },
            )
        }
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    private fun validateOpaqueId(value: String?): String? {
        val id = value ?: return null
        if (id.length !in 1..ExplorerActionProtocol.MAX_TARGET_ID_LENGTH) return null
        return id.takeIf { candidate ->
            candidate.none { it.isWhitespace() || isUnsafeUnicodeCharacter(it) }
        }
    }

    private fun isSafeDirectName(value: String): Boolean =
        ImageRequestPolicy.validateDisplayName(value) != null

    private fun isHostImageUri(uri: Uri, expectedIndex: Int): Boolean =
        uri.scheme == HOST_IMAGE_SCHEME && uri.authority == HOST_IMAGE_AUTHORITY &&
            uri.pathSegments == listOf(expectedIndex.toString()) &&
            uri.userInfo == null && uri.port == -1 && uri.query == null && uri.fragment == null

    /** `Pictures/Private/`: relative, slash-terminated, no empty, `.` or `..` segment. */
    private fun isSafeRelativeFolder(value: String): Boolean {
        if (!value.endsWith('/') || value.startsWith('/')) return false
        val segments = value.dropLast(1).split('/')
        return segments.none { it.isEmpty() || it == "." || it == ".." || it.any { ch -> ch.isISOControl() } }
    }

    private fun isPlainContentUri(uri: Uri): Boolean {
        if (!uri.isHierarchical || uri.scheme != ContentResolver.SCHEME_CONTENT) return false
        if (uri.authority.isNullOrBlank() || uri.host.isNullOrBlank()) return false
        if (uri.userInfo != null || uri.port != -1 || uri.query != null || uri.fragment != null) return false
        val encodedPath = uri.encodedPath ?: return false
        return encodedPath.startsWith('/') && encodedPath.length > 1
    }

    private fun ClipData.Item.isExactUri(expected: Uri): Boolean =
        uri == expected && text == null && htmlText == null && intent == null

    private fun isUnsafeUnicodeCharacter(character: Char): Boolean =
        character.isISOControl() || Character.getType(character) == Character.FORMAT.toInt()

    @Suppress("DEPRECATION")
    private fun Intent.parcelableBundleExtra(name: String): Bundle? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getParcelableExtra(name, Bundle::class.java)
        } else {
            getParcelableExtra(name)
        }
}
