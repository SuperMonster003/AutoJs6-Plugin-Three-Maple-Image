package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal enum class AlbumKind { MEDIA, FOLDER }

/** One entry of the album grid: a MediaStore bucket or an authorized folder (design draft 8.1). */
internal data class AlbumSummary(
    val kind: AlbumKind,
    val id: Long,
    val name: String,
    val count: Int,
    val coverUri: String?,
    val subtitle: String,
    val available: Boolean = true,
    val folder: AuthorizedFolder? = null,
)

internal data class GalleryUiState(
    val loading: Boolean = true,
    val access: MediaPermissionResolution? = null,
    val albums: List<AlbumSummary> = emptyList(),
    val folders: List<AlbumSummary> = emptyList(),
    val albumSort: MediaSort = MediaSort.DEFAULT,
    val failed: Boolean = false,
) {
    val canReadMediaStore: Boolean get() = access?.canReadMediaStore == true
}

internal data class MediaGridUiState(
    val album: AlbumSummary? = null,
    val items: List<MediaItem> = emptyList(),
    val loading: Boolean = true,
    val endReached: Boolean = false,
    val sort: MediaSort = MediaSort.DEFAULT,
    val limit: DocumentTreeLimit = DocumentTreeLimit.NONE,
)

/**
 * State of the standalone gallery (ROADMAP 6.2): the album list built from MediaStore buckets and authorized folders,
 * and the media grid of the opened album with provider paging (MediaStore) or in-memory slices (folders). A
 * MediaStore change reloads both; the permission resolution is pushed in by the Activity that owns the gate.
 */
internal class GalleryViewModel(application: Application) : AndroidViewModel(application) {

    private val media = MediaStoreRepository(application)
    private val folderRepository = AuthorizedFolderRepository(application)
    private val _albums = MutableStateFlow(GalleryUiState())
    private val _grid = MutableStateFlow(MediaGridUiState())
    private val folderCache = HashMap<Long, Pair<List<MediaItem>, DocumentTreeLimit>>()
    private var access: MediaPermissionResolution? = null
    private var gridJob: Job? = null
    private var reloadJob: Job? = null

    val albums: StateFlow<GalleryUiState> = _albums.asStateFlow()
    val grid: StateFlow<MediaGridUiState> = _grid.asStateFlow()

    init {
        viewModelScope.launch {
            media.changes().collect {
                folderCache.clear()
                reload()
                _grid.value.album?.let { openAlbum(it, keepSort = true) }
            }
        }
    }

    fun setAccess(resolution: MediaPermissionResolution) {
        val changed = access?.state != resolution.state
        access = resolution
        _albums.update { it.copy(access = resolution) }
        if (changed || _albums.value.loading) reload()
    }

    fun setAlbumSort(sort: MediaSort) {
        _albums.update { state -> state.copy(albumSort = sort, albums = sortAlbums(state.albums, sort)) }
    }

    fun reload() {
        reloadJob?.cancel()
        reloadJob = viewModelScope.launch {
            _albums.update { it.copy(loading = it.albums.isEmpty() && it.folders.isEmpty(), failed = false) }
            val resolution = access
            val buckets = if (resolution?.canReadMediaStore == true) {
                runCatching { media.loadBuckets() }.onFailure { _albums.update { s -> s.copy(failed = true) } }.getOrDefault(emptyList())
            } else {
                media.invalidate()
                emptyList()
            }
            val rootName = getApplication<Application>().getString(R.string.gallery_album_root)
            val albums = buckets.map { bucket ->
                val name = bucket.name.ifBlank { bucket.relativePath.trimEnd('/').substringAfterLast('/').ifBlank { rootName } }
                AlbumSummary(AlbumKind.MEDIA, bucket.id, name, bucket.count, bucket.cover.contentUri, bucket.relativePath)
            }
            val folders = folderRepository.list().map { folder ->
                val cached = folderCache[folder.bucketId]
                AlbumSummary(
                    kind = AlbumKind.FOLDER,
                    id = folder.bucketId,
                    name = folder.displayName,
                    count = cached?.first?.size ?: -1,
                    coverUri = cached?.first?.firstOrNull()?.contentUri,
                    subtitle = folder.location,
                    available = folder.available,
                    folder = folder,
                )
            }
            _albums.update { it.copy(loading = false, albums = sortAlbums(albums, it.albumSort), folders = folders) }
            folders.filter { it.available && it.count < 0 }.forEach { summary -> enumerateFolder(summary) }
        }
    }

    private fun enumerateFolder(summary: AlbumSummary) = viewModelScope.launch {
        val folder = summary.folder ?: return@launch
        val result = runCatching { folderRepository.enumerate(folder) }.getOrNull() ?: (emptyList<MediaItem>() to DocumentTreeLimit.NONE)
        folderCache[summary.id] = result
        _albums.update { state ->
            state.copy(folders = state.folders.map { if (it.id == summary.id) it.copy(count = result.first.size, coverUri = result.first.firstOrNull()?.contentUri) else it })
        }
    }

    fun findAlbum(kind: AlbumKind, id: Long): AlbumSummary? =
        (if (kind == AlbumKind.MEDIA) _albums.value.albums else _albums.value.folders).firstOrNull { it.id == id }

    fun openAlbum(album: AlbumSummary, keepSort: Boolean = false) {
        gridJob?.cancel()
        val sort = if (keepSort && _grid.value.album?.id == album.id) _grid.value.sort else MediaSort.DEFAULT
        _grid.value = MediaGridUiState(album = album, loading = true, sort = sort)
        loadPage(reset = true)
    }

    fun setGridSort(sort: MediaSort) {
        if (_grid.value.sort == sort) return
        gridJob?.cancel()
        _grid.update { it.copy(sort = sort, items = emptyList(), loading = true, endReached = false) }
        loadPage(reset = true)
    }

    fun loadMore() {
        val state = _grid.value
        if (state.loading || state.endReached || state.album == null) return
        loadPage(reset = false)
    }

    private fun loadPage(reset: Boolean) {
        val album = _grid.value.album ?: return
        gridJob = viewModelScope.launch {
            _grid.update { it.copy(loading = true) }
            val offset = if (reset) 0 else _grid.value.items.size
            val sort = _grid.value.sort
            val page: List<MediaItem>
            var limit = DocumentTreeLimit.NONE
            if (album.kind == AlbumKind.MEDIA) {
                page = runCatching { media.loadBucket(album.id, sort, PAGE_SIZE, offset) }.getOrDefault(emptyList())
            } else {
                val folder = album.folder
                val cached = folderCache[album.id] ?: (folder?.let { runCatching { folderRepository.enumerate(it) }.getOrNull() } ?: (emptyList<MediaItem>() to DocumentTreeLimit.NONE)).also { folderCache[album.id] = it }
                limit = cached.second
                page = MediaCatalog.sort(cached.first, sort).drop(offset).take(PAGE_SIZE)
            }
            _grid.update { state ->
                if (state.album?.id != album.id) return@update state
                state.copy(
                    items = if (reset) page else state.items + page,
                    loading = false,
                    endReached = page.size < PAGE_SIZE,
                    limit = limit,
                )
            }
        }
    }

    private fun sortAlbums(albums: List<AlbumSummary>, sort: MediaSort): List<AlbumSummary> {
        val primary: Comparator<AlbumSummary> = when (sort.field) {
            MediaSortField.NAME -> Comparator { a, b -> ImageSiblingDiscoveryPolicy.compareNaturally(a.name, b.name) }
            MediaSortField.SIZE -> compareBy { it.count }
            MediaSortField.DATE_MODIFIED, MediaSortField.DATE_TAKEN -> return albums // bucket order from the repository is newest first
        }
        val combined = primary.thenBy { it.id }
        return albums.sortedWith(if (sort.ascending) combined else combined.reversed())
    }

    companion object {
        const val PAGE_SIZE = 120
    }
}
