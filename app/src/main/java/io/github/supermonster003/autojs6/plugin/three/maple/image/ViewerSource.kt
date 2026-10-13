package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.content.Context
import android.net.Uri
import androidx.annotation.VisibleForTesting
import kotlinx.coroutines.CancellationException
import org.autojs.plugin.explorer.api.IExplorerActionHostSession

/** Where the viewer's pages come from (ROADMAP 6.3); the kind decides chrome such as edit availability. */
internal enum class ViewerSourceKind {
    /** One content URI from the external `ACTION_VIEW` entry or a file picker. */
    SINGLE,
    /** The host's explicit multi-selection: every page is a forwardable content URI. */
    HOST_SELECTION,
    /** The host's single-image entry with the directory siblings read through the host session. */
    HOST_SIBLINGS,
    /** A MediaStore bucket of the standalone gallery, enumerated inside the viewer. */
    MEDIA_ALBUM,
    /** An authorized SAF folder of the standalone gallery, enumerated inside the viewer. */
    AUTHORIZED_FOLDER,
}

/**
 * Description of a gallery album the viewer enumerates itself. The launching intent carries only the start page plus
 * this spec, so the album size is not bounded by the intent payload and the page list survives process death.
 */
internal sealed interface ViewerSourceSpec {
    val sort: MediaSort
    val startItemId: Long

    data class MediaAlbum(
        val bucketId: Long,
        override val sort: MediaSort,
        override val startItemId: Long,
    ) : ViewerSourceSpec

    data class AuthorizedFolder(
        val treeUri: String,
        override val sort: MediaSort,
        override val startItemId: Long,
    ) : ViewerSourceSpec
}

/** The resolved page list the viewer pages through; host sources keep their session, gallery sources their items. */
internal class ViewerSource(
    val kind: ViewerSourceKind,
    val pages: List<ImageViewerPage>,
    val startIndex: Int,
    val hostSession: IExplorerActionHostSession? = null,
    val hostTargetId: String? = null,
    val editable: Boolean = false,
    /** Gallery items behind [pages], index-aligned; null for host and single sources. */
    val items: List<MediaItem>? = null,
) {
    init {
        require(pages.isNotEmpty())
        require(startIndex in pages.indices)
        require(items == null || items.size == pages.size)
    }

    fun itemId(index: Int): Long? = items?.getOrNull(index)?.id

    companion object {
        fun fromRequest(request: ImageViewerLaunchRequest): ViewerSource = ViewerSource(
            kind = when {
                request.hostSession != null -> ViewerSourceKind.HOST_SIBLINGS
                request.source is ViewerSourceSpec.MediaAlbum -> ViewerSourceKind.MEDIA_ALBUM
                request.source is ViewerSourceSpec.AuthorizedFolder -> ViewerSourceKind.AUTHORIZED_FOLDER
                request.pages.size > 1 -> ViewerSourceKind.HOST_SELECTION
                else -> ViewerSourceKind.SINGLE
            },
            pages = request.pages,
            startIndex = request.startIndex,
            hostSession = request.hostSession,
            hostTargetId = request.hostTargetId,
            editable = request.editable,
        )

        /** Pages of a gallery album; when the start item is gone the viewer falls back to the start page alone. */
        fun fromItems(
            kind: ViewerSourceKind,
            items: List<MediaItem>,
            startItemId: Long,
            fallback: ImageViewerPage,
            editable: Boolean,
        ): ViewerSource {
            val startIndex = items.indexOfFirst { it.id == startItemId }
            if (startIndex < 0) return ViewerSource(kind, listOf(fallback), 0, editable = editable)
            return ViewerSource(kind, items.map(::page), startIndex, editable = editable, items = items)
        }

        fun request(item: MediaItem): ImageViewerRequest = ImageViewerRequest(
            targetUri = Uri.parse(item.contentUri),
            displayName = item.displayName,
            declaredSize = item.sizeBytes.coerceAtLeast(0L),
            mimeType = ImageRequestPolicy.normalizeImageMimeType(item.mimeType) ?: "image/*",
        )

        fun page(item: MediaItem): ImageViewerPage = ImageViewerPage(request(item))
    }
}

/** Enumerates a [ViewerSourceSpec] into a [ViewerSource]; intent-borne page lists pass through unchanged. */
internal class ViewerSourceLoader(
    private val loadAlbum: suspend (bucketId: Long, sort: MediaSort) -> List<MediaItem>,
    private val loadFolder: suspend (treeUri: String, sort: MediaSort) -> List<MediaItem>,
) {
    constructor(context: Context) : this(
        loadAlbum = { bucketId, sort -> MediaStoreRepository(context).loadBucketAll(bucketId, sort) },
        loadFolder = { treeUri, sort ->
            val repository = AuthorizedFolderRepository(context)
            val folder = repository.list().firstOrNull { it.treeUri == treeUri && it.available }
            if (folder == null) emptyList() else MediaCatalog.sort(repository.enumerateCached(folder).first, sort)
        },
    )

    suspend fun load(request: ImageViewerLaunchRequest): ViewerSource {
        val spec = request.source ?: return ViewerSource.fromRequest(request)
        val items = try {
            when (spec) {
                is ViewerSourceSpec.MediaAlbum -> loadAlbum(spec.bucketId, spec.sort)
                is ViewerSourceSpec.AuthorizedFolder -> loadFolder(spec.treeUri, spec.sort)
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            emptyList()
        }
        val kind = when (spec) {
            is ViewerSourceSpec.MediaAlbum -> ViewerSourceKind.MEDIA_ALBUM
            is ViewerSourceSpec.AuthorizedFolder -> ViewerSourceKind.AUTHORIZED_FOLDER
        }
        return ViewerSource.fromItems(kind, items, spec.startItemId, request.startPage, request.editable)
    }

    companion object {
        /** Lets instrumentation replace the repositories behind gallery sources. */
        @VisibleForTesting
        internal var factoryForTesting: ((Context) -> ViewerSourceLoader)? = null

        fun create(context: Context): ViewerSourceLoader =
            factoryForTesting?.invoke(context) ?: ViewerSourceLoader(context)
    }
}
