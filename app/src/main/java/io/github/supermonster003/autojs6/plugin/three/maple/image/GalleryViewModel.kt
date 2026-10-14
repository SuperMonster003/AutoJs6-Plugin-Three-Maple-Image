package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal enum class AlbumKind {
    MEDIA,
    FOLDER,
    /** The flat "all images" view over every MediaStore bucket (ROADMAP 6.2). */
    ALL,
    /** The "Favorites" virtual album, first in the album grid when it has images (ROADMAP 7.4). */
    FAVORITES,
}

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
    val volume: String = MediaCatalog.PRIMARY_VOLUME,
)

internal data class GalleryUiState(
    val loading: Boolean = true,
    val access: MediaPermissionResolution? = null,
    val albums: List<AlbumSummary> = emptyList(),
    val folders: List<AlbumSummary> = emptyList(),
    /** Shown before the albums while it has images. */
    val favorites: AlbumSummary? = null,
    val albumSort: MediaSort = MediaSort.DEFAULT,
    val failed: Boolean = false,
    /** A user-initiated reload (pull to refresh) is in flight while the current albums stay on screen. */
    val refreshing: Boolean = false,
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
    /** Item the grid should scroll to (set when the viewer returns); cleared through [GalleryViewModel.clearReveal]. */
    val reveal: Long? = null,
    val columns: Int = GalleryPreferences.DEFAULT_COLUMNS,
    val refreshing: Boolean = false,
    /** Ids selected in the grid (ROADMAP 7.1); kept across rotation and refreshes of the same album. */
    val selection: Set<Long> = emptySet(),
    /** Media ids of the favorites; the selection bar shows "remove from favorites" when it covers the selection. */
    val favoriteIds: Set<Long> = emptySet(),
) {
    val selecting: Boolean get() = selection.isNotEmpty()
}

/**
 * State of the standalone gallery (ROADMAP 6.2): the album list built from MediaStore buckets and authorized folders,
 * and the media grid of the opened album with provider paging (MediaStore) or in-memory slices (folders). A
 * MediaStore change reloads both; the permission resolution is pushed in by the Activity that owns the gate.
 */
internal class GalleryViewModel(application: Application) : AndroidViewModel(application) {

    private val media = MediaStoreRepository(application)
    private val folderRepository = AuthorizedFolderRepository(application)
    private val preferences = GalleryPreferences(application)
    private val favorites = FavoritesRepository(application)
    private var favoriteIds: Set<Long> = emptySet()
    private var favoriteOrder: Pair<MediaSort, List<MediaItem>>? = null
    private val _albums = MutableStateFlow(GalleryUiState())
    private val _grid = MutableStateFlow(MediaGridUiState(columns = preferences.mediaColumns))
    private var access: MediaPermissionResolution? = null
    private var gridJob: Job? = null
    private var reloadJob: Job? = null
    private var revealJob: Job? = null
    private var folderOrder: Triple<List<MediaItem>, MediaSort, List<MediaItem>>? = null
    private var selectionJob: Job? = null
    private var dragBase: Set<Long>? = null
    private var dragAnchor: Long? = null

    val albums: StateFlow<GalleryUiState> = _albums.asStateFlow()
    val grid: StateFlow<MediaGridUiState> = _grid.asStateFlow()

    /** Set by the Activity between onStart and onStop; changes seen while hidden reload once on return. */
    private var visible = false
    private var stale = false

    init {
        viewModelScope.launch {
            media.changes().collect {
                folderRepository.invalidateCache()
                // A hidden gallery must not rescan the library for every settled change (a bulk import keeps
                // notifying for minutes); it only remembers that the lists are stale.
                if (visible) reloadAfterChange() else stale = true
            }
        }
        viewModelScope.launch {
            favorites.observeIds().collect { ids ->
                if (ids == favoriteIds) return@collect
                favoriteIds = ids
                favoriteOrder = null
                _grid.update { it.copy(favoriteIds = ids) }
                refreshFavorites()
            }
        }
    }

    /** The favorites album summary, and the open favorites grid, after a favorite was added or removed. */
    private fun refreshFavorites() = viewModelScope.launch {
        val summary = favoritesAlbum()
        _albums.update { it.copy(favorites = summary) }
        val open = _grid.value.album
        if (open?.kind == AlbumKind.FAVORITES) {
            if (summary != null) openAlbum(summary, keepSort = true) else _grid.update { it.copy(items = emptyList(), album = open.copy(count = 0)) }
        }
    }

    private suspend fun favoritesAlbum(): AlbumSummary? {
        if (!_albums.value.canReadMediaStore) return null
        val items = runCatching { favorites.resolve(media.loadAll()) }.getOrDefault(emptyList())
        if (items.isEmpty()) return null
        return AlbumSummary(
            kind = AlbumKind.FAVORITES,
            id = MediaCatalog.FAVORITES,
            name = getApplication<Application>().getString(R.string.favorites_title),
            count = items.size,
            coverUri = items.first().contentUri,
            subtitle = "",
        )
    }

    /** Favorites in [sort] order; the default sort keeps "most recently added first". */
    private suspend fun favoriteItems(sort: MediaSort): List<MediaItem> {
        favoriteOrder?.let { (cachedSort, items) -> if (cachedSort == sort) return items }
        val resolved = runCatching { favorites.resolve(media.loadAll()) }.getOrDefault(emptyList())
        val ordered = if (sort == MediaSort.DEFAULT) resolved else withContext(Dispatchers.Default) { MediaCatalog.sort(resolved, sort) }
        favoriteOrder = sort to ordered
        return ordered
    }

    fun setVisible(value: Boolean) {
        visible = value
        if (value && stale) {
            stale = false
            reloadAfterChange()
        }
    }

    private fun reloadAfterChange() {
        favoriteOrder = null
        reload()
        _grid.value.album?.let { openAlbum(it, keepSort = true) }
    }

    fun setAccess(resolution: MediaPermissionResolution) {
        val changed = access?.state != resolution.state
        access = resolution
        _albums.update { it.copy(access = resolution) }
        if (changed || _albums.value.loading) reload(retryOnce = changed)
    }

    fun setAlbumSort(sort: MediaSort) {
        _albums.update { state -> state.copy(albumSort = sort, albums = sortAlbums(state.albums, sort)) }
    }

    /** Pull to refresh on the album grid: reload while the current albums stay visible. */
    fun refreshAlbums() {
        _albums.update { it.copy(refreshing = true) }
        reload()
    }

    /** Pull to refresh on the media grid: reopen the album with its current sort. */
    fun refreshGrid() {
        val album = _grid.value.album ?: return
        folderRepository.invalidateCache()
        _grid.update { it.copy(refreshing = true) }
        openAlbum(album, keepSort = true)
    }

    /**
     * [retryOnce] covers the moments right after a runtime grant, when MediaStore may still answer the first query
     * with the old permission state (a SecurityException or an empty cursor): one delayed retry before giving up.
     */
    fun reload(retryOnce: Boolean = false) {
        reloadJob?.cancel()
        reloadJob = viewModelScope.launch {
            _albums.update { it.copy(loading = it.albums.isEmpty() && it.folders.isEmpty(), failed = false) }
            val resolution = access
            val buckets = if (resolution?.canReadMediaStore == true) {
                var loaded = runCatching { media.loadBuckets() }.getOrNull()
                if (retryOnce && loaded.isNullOrEmpty()) {
                    delay(GRANT_SETTLE_MILLIS)
                    media.invalidate()
                    loaded = runCatching { media.loadBuckets() }.getOrNull()
                }
                if (loaded == null) _albums.update { s -> s.copy(failed = true) }
                loaded.orEmpty()
            } else {
                media.invalidate()
                emptyList()
            }
            val rootName = getApplication<Application>().getString(R.string.gallery_album_root)
            val albums = buckets.map { bucket ->
                val name = bucket.name.ifBlank { bucket.relativePath.trimEnd('/').substringAfterLast('/').ifBlank { rootName } }
                AlbumSummary(AlbumKind.MEDIA, bucket.id, name, bucket.count, bucket.cover.contentUri, bucket.relativePath, volume = bucket.volume)
            }
            val folders = folderRepository.list().map { folder ->
                val cached = folderRepository.cached(folder)
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
            favoriteOrder = null
            val favoritesSummary = favoritesAlbum()
            _albums.update { it.copy(loading = false, refreshing = false, albums = sortAlbums(albums, it.albumSort), folders = folders, favorites = favoritesSummary) }
            // The open album's title and count follow the reloaded list (after a delete or move, for example).
            _grid.update { grid ->
                val open = grid.album ?: return@update grid
                val fresh = findAlbum(open.kind, open.id) ?: return@update grid
                if (fresh == open) grid else grid.copy(album = fresh)
            }
            folders.filter { it.available && it.count < 0 }.forEach { summary -> enumerateFolder(summary) }
        }
    }

    private fun enumerateFolder(summary: AlbumSummary) = viewModelScope.launch {
        val folder = summary.folder ?: return@launch
        val result = runCatching { folderRepository.enumerateCached(folder) }.getOrNull() ?: (emptyList<MediaItem>() to DocumentTreeLimit.NONE)
        _albums.update { state ->
            state.copy(folders = state.folders.map { if (it.id == summary.id) it.copy(count = result.first.size, coverUri = result.first.firstOrNull()?.contentUri) else it })
        }
    }

    fun findAlbum(kind: AlbumKind, id: Long): AlbumSummary? = when (kind) {
        AlbumKind.MEDIA -> _albums.value.albums.firstOrNull { it.id == id }
        AlbumKind.FOLDER -> _albums.value.folders.firstOrNull { it.id == id }
        AlbumKind.ALL -> allImagesAlbum()
        AlbumKind.FAVORITES -> _albums.value.favorites
    }

    /** The flat view over every bucket ("show all folder contents"); null without photo access. */
    fun allImagesAlbum(): AlbumSummary? {
        val state = _albums.value
        if (!state.canReadMediaStore) return null
        return AlbumSummary(
            kind = AlbumKind.ALL,
            id = MediaCatalog.ALL_BUCKETS,
            name = getApplication<Application>().getString(R.string.gallery_all_images),
            count = state.albums.sumOf { it.count },
            coverUri = state.albums.firstOrNull()?.coverUri,
            subtitle = "",
        )
    }

    fun openAlbum(album: AlbumSummary, keepSort: Boolean = false) {
        gridJob?.cancel()
        val sort = if (keepSort && _grid.value.album?.id == album.id) {
            _grid.value.sort
        } else {
            preferences.sortFor(album.kind, album.id) ?: preferences.defaultSort
        }
        val current = _grid.value
        val sameAlbum = current.album?.id == album.id && current.album.kind == album.kind
        _grid.value = MediaGridUiState(
            album = album,
            // A refresh or a change-triggered reload keeps the current cells until the first page replaces them.
            items = if (sameAlbum && sort == current.sort) current.items else emptyList(),
            loading = true,
            sort = sort,
            columns = preferences.mediaColumns,
            refreshing = current.refreshing && sameAlbum,
            selection = if (sameAlbum) current.selection else emptySet(),
            // The favorites flow only emits on changes, so a fresh grid state must carry the current marks.
            favoriteIds = favoriteIds,
        )
        loadPage(reset = true)
    }

    /** Remembered per album and as the default for albums without a memory (ROADMAP 6.2 sort memory). */
    fun setGridSort(sort: MediaSort) {
        val album = _grid.value.album
        if (album != null) preferences.rememberSort(album.kind, album.id, sort)
        if (_grid.value.sort == sort) return
        gridJob?.cancel()
        _grid.update { it.copy(sort = sort, items = emptyList(), loading = true, endReached = false) }
        loadPage(reset = true)
    }

    /** Media grid columns (3..6), from the menu or a pinch; persisted for every album. */
    fun setColumns(columns: Int) {
        val clamped = GalleryPreferences.clampColumns(columns)
        preferences.mediaColumns = clamped
        _grid.update { if (it.columns == clamped) it else it.copy(columns = clamped) }
    }

    fun loadMore() {
        val state = _grid.value
        if (state.loading || state.endReached || state.album == null || gridJob?.isActive == true) return
        loadPage(reset = false)
    }

    /**
     * Brings [itemId] into the loaded grid, loading further pages if the viewer went past them, then asks the grid
     * to scroll there (ROADMAP 6.3 position sync); the grid acknowledges with [clearReveal].
     */
    fun reveal(itemId: Long) {
        revealJob?.cancel()
        revealJob = viewModelScope.launch {
            repeat(MAX_REVEAL_PAGES) {
                gridJob?.join()
                val state = _grid.value
                if (state.album == null) return@launch
                if (state.items.any { it.id == itemId }) {
                    _grid.update { it.copy(reveal = itemId) }
                    return@launch
                }
                if (state.endReached) return@launch
                loadPage(reset = false)?.join() ?: return@launch
            }
        }
    }

    fun clearReveal() {
        _grid.update { if (it.reveal == null) it else it.copy(reveal = null) }
    }

    // ---- selection (ROADMAP 7.1) ----

    fun toggleSelection(id: Long) {
        _grid.update { it.copy(selection = MediaSelection.toggle(it.selection, id)) }
    }

    fun clearSelection() {
        selectionJob?.cancel()
        _grid.update { if (it.selection.isEmpty()) it else it.copy(selection = emptySet()) }
    }

    /** A long press: selects [id] and anchors a drag range there. */
    fun beginDragSelection(id: Long) {
        val base = _grid.value.selection
        dragBase = base
        dragAnchor = id
        _grid.update { it.copy(selection = LinkedHashSet(base).apply { add(id) }) }
    }

    fun dragSelectionTo(id: Long) {
        val base = dragBase ?: return
        val anchor = dragAnchor ?: return
        _grid.update { state -> state.copy(selection = MediaSelection.range(base, state.items.map { it.id }, anchor, id)) }
    }

    fun endDragSelection() {
        dragBase = null
        dragAnchor = null
    }

    /** Selects every image of the album, including pages the grid has not loaded yet. */
    fun selectAll() {
        selectionJob?.cancel()
        selectionJob = viewModelScope.launch {
            val all = allAlbumItems()
            _grid.update { it.copy(selection = all.mapTo(LinkedHashSet()) { item -> item.id }) }
        }
    }

    fun invertSelection() {
        selectionJob?.cancel()
        selectionJob = viewModelScope.launch {
            val all = allAlbumItems()
            _grid.update { it.copy(selection = MediaSelection.invert(all.map { item -> item.id }, it.selection)) }
        }
    }

    /** The selected images in album order, resolved from the loaded pages or, if needed, the whole album. */
    suspend fun selectedItems(): List<MediaItem> {
        val state = _grid.value
        val ids = state.selection
        if (ids.isEmpty()) return emptyList()
        val loaded = state.items.filter { it.id in ids }
        if (loaded.size == ids.size) return loaded
        return allAlbumItems().filter { it.id in ids }
    }

    private suspend fun allAlbumItems(): List<MediaItem> {
        val state = _grid.value
        val album = state.album ?: return emptyList()
        if (state.endReached) return state.items
        if (album.kind == AlbumKind.FAVORITES) return favoriteItems(state.sort)
        return if (album.kind != AlbumKind.FOLDER) {
            runCatching { media.loadBucketAll(album.id, state.sort) }.getOrDefault(state.items)
        } else {
            val folder = album.folder ?: return state.items
            val cached = runCatching { folderRepository.enumerateCached(folder) }.getOrNull() ?: return state.items
            folderOrdered(cached.first, state.sort)
        }
    }

    /** After a write batch: the library changed now, so reload without waiting for the settled change event. */
    fun refreshAfterWrite() {
        media.invalidate()
        folderRepository.invalidateCache()
        reloadAfterChange()
    }

    private fun loadPage(reset: Boolean): Job? {
        val album = _grid.value.album ?: return null
        val job = viewModelScope.launch {
            // Appending a page while the grid flings must not emit an extra state (one recomposition per page).
            if (reset) _grid.update { if (it.loading) it else it.copy(loading = true) }
            val offset = if (reset) 0 else _grid.value.items.size
            val sort = _grid.value.sort
            val page: List<MediaItem>
            var limit = DocumentTreeLimit.NONE
            if (album.kind == AlbumKind.FAVORITES) {
                val ordered = favoriteItems(sort)
                page = ordered.subList(offset.coerceAtMost(ordered.size), (offset + PAGE_SIZE).coerceAtMost(ordered.size))
            } else if (album.kind != AlbumKind.FOLDER) {
                page = runCatching { media.loadBucket(album.id, sort, PAGE_SIZE, offset) }.getOrDefault(emptyList())
            } else {
                val folder = album.folder
                val cached = folder?.let { runCatching { folderRepository.enumerateCached(it) }.getOrNull() } ?: (emptyList<MediaItem>() to DocumentTreeLimit.NONE)
                limit = cached.second
                val ordered = folderOrdered(cached.first, sort)
                page = ordered.subList(offset.coerceAtMost(ordered.size), (offset + PAGE_SIZE).coerceAtMost(ordered.size))
            }
            _grid.update { state ->
                if (state.album?.id != album.id) return@update state
                state.copy(
                    items = if (reset) page else state.items + page,
                    loading = false,
                    refreshing = false,
                    endReached = page.size < PAGE_SIZE,
                    limit = limit,
                )
            }
        }
        gridJob = job
        return job
    }

    /** Folder pages are slices of one sorted copy (an authorized folder may hold up to 20000 images). */
    private suspend fun folderOrdered(items: List<MediaItem>, sort: MediaSort): List<MediaItem> {
        folderOrder?.let { (source, cachedSort, ordered) -> if (source === items && cachedSort == sort) return ordered }
        val ordered = withContext(Dispatchers.Default) { MediaCatalog.sort(items, sort) }
        folderOrder = Triple(items, sort, ordered)
        return ordered
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
        /** Larger pages mean fewer list appends (each one recomposes the grid) while flinging through a big album. */
        const val PAGE_SIZE = 360
        private const val GRANT_SETTLE_MILLIS = 800L
        private const val MAX_REVEAL_PAGES = 100
    }
}
