package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.app.Application
import android.content.ComponentCallbacks2
import android.content.res.Configuration
import android.os.SystemClock
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.TimeZone
import kotlin.random.Random

internal enum class AlbumKind {
    MEDIA,
    FOLDER,
    /** The flat "all images" view over every MediaStore bucket (ROADMAP 6.2). */
    ALL,
    /** The "Favorites" virtual album, first in the album grid when it has images (ROADMAP 7.4). */
    FAVORITES,
    /** A folder hidden with `.nomedia`, read from the file system; browse and share only (ROADMAP 7.4). */
    HIDDEN,
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
    /** Shown in the top section of the album grid (ROADMAP 7.4). */
    val pinned: Boolean = false,
    /** Latest change and latest capture of the album's images, for the album grid sort (ROADMAP 7.5). */
    val latestModifiedSeconds: Long = 0L,
    val latestTakenMillis: Long = 0L,
) {
    /** The folder of a media library album (volume and relative path). */
    val path: AlbumPath get() = AlbumPath(volume, subtitle)
}

/** The home screen search (ROADMAP 7.5): albums by name and images by file name over the whole visible library. */
internal data class SearchUiState(
    val active: Boolean = false,
    val query: String = "",
    val albums: List<AlbumSummary> = emptyList(),
    val items: List<MediaItem> = emptyList(),
    /** A search for the current query is running. */
    val searching: Boolean = false,
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
    /** Folders hidden with `.nomedia`, listed after the others when Settings > Hidden folders shows them. */
    val hidden: List<AlbumSummary> = emptyList(),
    /** Bucket ids of the media library albums selected on the album grid (ROADMAP 7.4); kept across rotation. */
    val albumSelection: Set<Long> = emptySet(),
) {
    val canReadMediaStore: Boolean get() = access?.canReadMediaStore == true

    val selectingAlbums: Boolean get() = albumSelection.isNotEmpty()

    val selectedAlbums: List<AlbumSummary> get() = if (albumSelection.isEmpty()) emptyList() else albums.filter { it.id in albumSelection }
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
    /** Date sections (ROADMAP 7.5): with a grouping the whole album is loaded and [groups] index its [items]. */
    val grouping: MediaGrouping = MediaGrouping.NONE,
    val groups: List<MediaGroup> = emptyList(),
    /** The open album keeps a sort of its own (the sort dialog's "this album only"). */
    val ownSort: Boolean = false,
    /** Search inside the album (ROADMAP 7.5): with a query the grid shows the whole album filtered by file name. */
    val query: String = "",
    /** The open media album has a cover of its own (ROADMAP 9.2): the menu offers to go back to the default. */
    val customCover: Boolean = false,
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
    private val folderMarks = FolderMarksRepository(application)
    private val hiddenFolders = HiddenFolders(application)
    private var hiddenItems: Map<Long, List<MediaItem>> = emptyMap()

    /** What the hidden section was built for; a change (settings, access) rebuilds it on return. */
    private var hiddenShown: Boolean? = null

    /** Excluded and pinned folders (ROADMAP 7.4), read before each album list is built. */
    private var marks: FolderMarkState = FolderMarkState.NONE
    private var favoriteIds: Set<Long> = emptySet()
    private var favoriteOrder: Pair<MediaSort, List<MediaItem>>? = null
    private val _albums = MutableStateFlow(GalleryUiState(albumSort = preferences.albumSort))
    private val _gridSettings = MutableStateFlow(preferences.grid)

    /** Album and media grid settings (ROADMAP 9.2); the home screen reloads them when it comes back. */
    val gridSettings: StateFlow<GridSettings> = _gridSettings.asStateFlow()

    /** The window is in landscape: the media grid uses its landscape columns. */
    private var landscape = false
    private val _grid = MutableStateFlow(MediaGridUiState(columns = currentColumns()))
    private var access: MediaPermissionResolution? = null
    private var gridJob: Job? = null
    private var reloadJob: Job? = null
    private var revealJob: Job? = null
    private var folderOrder: Triple<List<MediaItem>, MediaSort, List<MediaItem>>? = null
    private var selectionJob: Job? = null
    private var dragBase: Set<Long>? = null
    private var dragAnchor: Long? = null

    val albums: StateFlow<GalleryUiState> = _albums.asStateFlow()
    private val _search = MutableStateFlow(SearchUiState())
    val search: StateFlow<SearchUiState> = _search.asStateFlow()
    private var searchJob: Job? = null
    private var gridQueryJob: Job? = null
    val grid: StateFlow<MediaGridUiState> = _grid.asStateFlow()

    /** Set by the Activity between onStart and onStop; changes seen while hidden reload once on return. */
    private var visible = false
    private var stale = false

    /**
     * Under memory pressure the library scan and folder listings are dropped (ROADMAP 9.4); the shown lists stay,
     * the next page or reload scans again.
     */
    private val memoryCallbacks = object : ComponentCallbacks2 {
        override fun onTrimMemory(level: Int) {
            if (!MemoryTrimPolicy.releasesCaches(level)) return
            media.invalidate()
            folderRepository.invalidateCache()
            favoriteOrder = null
        }

        override fun onConfigurationChanged(newConfig: Configuration) = Unit

        @Deprecated("Deprecated in Java")
        override fun onLowMemory() = onTrimMemory(ComponentCallbacks2.TRIM_MEMORY_COMPLETE)
    }

    init {
        application.registerComponentCallbacks(memoryCallbacks)
        viewModelScope.launch {
            media.changes().collect {
                folderRepository.invalidateCache()
                // A hidden gallery must not rescan the library for every settled change (a bulk import keeps
                // notifying for minutes); it only remembers that the lists are stale.
                if (visible) reloadAfterChange() else stale = true
            }
        }
        viewModelScope.launch {
            folderMarks.observe().collect { state ->
                if (state == marks) return@collect
                marks = state
                favoriteOrder = null
                reloadAfterChange()
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

    /**
     * The folder marks are read after the last suspension and callers publish the result without suspending again, so
     * a summary worked out before the marks first loaded cannot land after the one that used them.
     */
    private suspend fun favoritesAlbum(): AlbumSummary? {
        if (!_albums.value.canReadMediaStore) return null
        val resolved = runCatching { favorites.resolve(media.loadAll()) }.getOrDefault(emptyList())
        val items = marks.visible(resolved)
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
        // Resolved against the whole library (a rebind must see every image), then without the excluded folders (read
        // after the suspension, see favoritesAlbum).
        val all = runCatching { favorites.resolve(media.loadAll()) }.getOrDefault(emptyList())
        val resolved = marks.visible(all)
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

    override fun onCleared() {
        getApplication<Application>().unregisterComponentCallbacks(memoryCallbacks)
        super.onCleared()
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
        preferences.albumSort = sort
        _albums.update { state -> state.copy(albumSort = sort, albums = arrangeAlbums(state.albums, sort)) }
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
            marks = runCatching { folderMarks.current() }.getOrDefault(marks)
            val resolution = access
            val buckets = if (resolution?.canReadMediaStore == true) {
                val order = _gridSettings.value.albumCover
                var loaded = runCatching { media.loadBuckets(order, preferences::customCover) }.getOrNull()
                if (retryOnce && loaded.isNullOrEmpty()) {
                    delay(GRANT_SETTLE_MILLIS)
                    media.invalidate()
                    loaded = runCatching { media.loadBuckets(order, preferences::customCover) }.getOrNull()
                }
                // A reload replaced by a newer one is cancelled; runCatching must not turn that into a failure.
                ensureActive()
                if (loaded == null) _albums.update { s -> s.copy(failed = true) }
                loaded.orEmpty()
            } else {
                media.invalidate()
                emptyList()
            }
            val rootName = getApplication<Application>().getString(R.string.gallery_album_root)
            val albums = buckets.mapNotNull { bucket ->
                val path = AlbumPath(bucket.volume, bucket.relativePath)
                if (marks.isExcluded(path)) return@mapNotNull null
                val name = bucket.name.ifBlank { bucket.relativePath.trimEnd('/').substringAfterLast('/').ifBlank { rootName } }
                AlbumSummary(
                    AlbumKind.MEDIA, bucket.id, name, bucket.count, bucket.cover.contentUri, bucket.relativePath,
                    volume = bucket.volume,
                    pinned = marks.isPinned(path),
                    latestModifiedSeconds = bucket.latestModifiedSeconds,
                    latestTakenMillis = bucket.latestTakenMillis,
                )
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
            val showHidden = preferences.showHiddenFolders && hiddenFolders.available()
            hiddenShown = showHidden
            val hidden = if (showHidden) runCatching { hiddenFolders.list() }.getOrDefault(emptyList()) else emptyList()
            hiddenItems = hidden.associate { it.id to it.items }
            val hiddenAlbums = hidden.map { folder ->
                AlbumSummary(
                    kind = AlbumKind.HIDDEN,
                    id = folder.id,
                    name = folder.name.ifEmpty { rootName },
                    count = folder.items.size,
                    coverUri = GridPolicy.cover(folder.items, _gridSettings.value.albumCover, chosenId = null)?.contentUri,
                    subtitle = folder.path.relativePath,
                )
            }
            favoriteOrder = null
            val favoritesSummary = favoritesAlbum()
            ensureActive()
            _albums.update { state ->
                state.copy(
                    loading = false,
                    refreshing = false,
                    albums = arrangeAlbums(albums, state.albumSort),
                    folders = folders,
                    hidden = hiddenAlbums,
                    favorites = favoritesSummary,
                    // A selected album that went away (deleted, excluded, moved out) leaves the selection.
                    albumSelection = state.albumSelection.filterTo(LinkedHashSet()) { id -> albums.any { it.id == id } },
                )
            }
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
        AlbumKind.HIDDEN -> _albums.value.hidden.firstOrNull { it.id == id }
    }

    /** Back on the home screen: the hidden section follows Settings > Hidden folders and the storage access. */
    fun refreshHiddenIfChanged() {
        val shown = preferences.showHiddenFolders && hiddenFolders.available()
        if (hiddenShown != null && hiddenShown != shown) reload()
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
        val kept = keepSort && _grid.value.album?.id == album.id
        val sort = if (kept) _grid.value.sort else withSeed(preferences.sortFor(album.kind, album.id) ?: preferences.defaultSort)
        val grouping = if (kept) _grid.value.grouping else preferences.groupingFor(album.kind, album.id) ?: preferences.defaultGrouping
        val current = _grid.value
        val sameAlbum = current.album?.id == album.id && current.album.kind == album.kind
        _grid.value = MediaGridUiState(
            album = album,
            // A refresh or a change-triggered reload keeps the current cells until the first page replaces them.
            items = if (sameAlbum && sort == current.sort) current.items else emptyList(),
            loading = true,
            sort = sort,
            grouping = grouping,
            groups = if (sameAlbum && sort == current.sort && grouping == current.grouping) current.groups else emptyList(),
            ownSort = preferences.hasOwnSort(album.kind, album.id),
            query = if (sameAlbum) current.query else "",
            columns = currentColumns(),
            refreshing = current.refreshing && sameAlbum,
            customCover = album.kind == AlbumKind.MEDIA && preferences.customCover(album.path) != null,
            selection = if (sameAlbum) current.selection else emptySet(),
            // The favorites flow only emits on changes, so a fresh grid state must carry the current marks.
            favoriteIds = favoriteIds,
        )
        loadPage(reset = true)
    }

    /** Remembered per album and as the default for albums without a memory (ROADMAP 6.2 sort memory). */
    /**
     * The sort dialog of a media grid (ROADMAP 7.5): [thisAlbumOnly] keeps the choice for the open album, otherwise
     * it becomes the default for every album without a choice of its own. A random order gets a fresh shuffle.
     */
    fun setGridSort(sort: MediaSort, grouping: MediaGrouping = _grid.value.grouping, thisAlbumOnly: Boolean = true) {
        val album = _grid.value.album
        if (album != null) preferences.applySort(album.kind, album.id, sort.copy(seed = 0L), grouping, thisAlbumOnly)
        val current = _grid.value
        _grid.update { it.copy(ownSort = thisAlbumOnly) }
        if (current.sort.copy(seed = 0L) == sort.copy(seed = 0L) && sort.field != MediaSortField.RANDOM && current.grouping == grouping) return
        gridJob?.cancel()
        _grid.update { it.copy(sort = withSeed(sort), grouping = grouping, items = emptyList(), groups = emptyList(), loading = true, endReached = false) }
        loadPage(reset = true)
    }

    /** Whether the open album keeps a sort of its own (the dialog's "this album only"). */
    fun openAlbumHasOwnSort(): Boolean = _grid.value.album?.let { preferences.hasOwnSort(it.kind, it.id) } ?: false

    private fun withSeed(sort: MediaSort): MediaSort = if (sort.field == MediaSortField.RANDOM) sort.copy(seed = Random.nextLong()) else sort

    /** Media grid columns (3..6), from the menu or a pinch; persisted for every album. */
    fun setColumns(columns: Int) {
        val clamped = GalleryPreferences.clampColumns(columns)
        if (landscape) {
            val settings = _gridSettings.value.copy(mediaColumnsLandscape = clamped)
            preferences.grid = settings
            _gridSettings.value = settings
        } else {
            preferences.mediaColumns = clamped
        }
        _grid.update { if (it.columns == clamped) it else it.copy(columns = clamped) }
    }

    private fun currentColumns(): Int = GridPolicy.mediaColumns(preferences.mediaColumns, _gridSettings.value, landscape)

    /** The home screen's orientation (ROADMAP 9.2): portrait and landscape keep their own media columns. */
    fun setLandscape(value: Boolean) {
        if (landscape == value) return
        landscape = value
        val columns = currentColumns()
        _grid.update { if (it.columns == columns) it else it.copy(columns = columns) }
    }

    /** Settings changed elsewhere: the grids follow at once, and a new cover order reloads the albums. */
    fun refreshGridSettings() {
        val settings = preferences.grid
        val previous = _gridSettings.value
        if (settings == previous && _grid.value.columns == currentColumns()) return
        _gridSettings.value = settings
        val columns = currentColumns()
        _grid.update { if (it.columns == columns) it else it.copy(columns = columns) }
        if (settings.albumCover != previous.albumCover) reload()
    }

    /** The one selected image becomes the open media album's cover (ROADMAP 9.2). */
    fun setAlbumCoverFromSelection() {
        val album = _grid.value.album?.takeIf { it.kind == AlbumKind.MEDIA } ?: return
        val id = _grid.value.selection.singleOrNull() ?: return
        preferences.setCustomCover(album.path, id)
        _grid.update { it.copy(customCover = true, selection = emptySet()) }
        reload()
    }

    fun resetAlbumCover() {
        val album = _grid.value.album?.takeIf { it.kind == AlbumKind.MEDIA } ?: return
        preferences.setCustomCover(album.path, null)
        _grid.update { it.copy(customCover = false) }
        reload()
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

    // ---- search (ROADMAP 7.5) ----

    fun startSearch() {
        if (!_search.value.active) _search.value = SearchUiState(active = true)
    }

    fun endSearch() {
        searchJob?.cancel()
        _search.value = SearchUiState()
    }

    /** Runs the home search once typing pauses; albums come from the album list, images from the full scan. */
    fun setSearchQuery(query: String) {
        _search.update { it.copy(query = query) }
        searchJob?.cancel()
        if (MediaSearch.terms(query).isEmpty()) {
            _search.update { it.copy(albums = emptyList(), items = emptyList(), searching = false) }
            return
        }
        searchJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_MILLIS)
            val started = SystemClock.elapsedRealtime()
            _search.update { it.copy(searching = true) }
            val terms = MediaSearch.terms(query)
            val albumList = _albums.value.albums + _albums.value.folders
            val library = runCatching { media.loadAll() }.getOrDefault(emptyList())
            val scanned = SystemClock.elapsedRealtime()
            val (albums, items) = withContext(Dispatchers.Default) {
                albumList.filter { MediaSearch.matches(it.name, terms) } to MediaSearch.items(marks.visible(library), query)
            }
            _search.update { if (it.query == query) it.copy(albums = albums, items = items, searching = false) else it }
            if (Log.isLoggable(SEARCH_LOG_TAG, Log.DEBUG)) {
                val published = SystemClock.elapsedRealtime()
                Log.d(
                    SEARCH_LOG_TAG,
                    "search over ${library.size} images: wait for scan ${scanned - started} ms, " +
                        "filter ${published - scanned} ms, ${albums.size} albums and ${items.size} images, published at $published",
                )
            }
        }
    }

    /** Runs the home search again (after the library changed while the viewer was open). */
    fun refreshSearch() {
        val search = _search.value
        if (search.active && MediaSearch.terms(search.query).isNotEmpty()) setSearchQuery(search.query)
    }

    /** Search inside the open album: a query filters the whole album by file name, an empty one pages normally again. */
    fun setGridQuery(query: String) {
        if (_grid.value.query == query) return
        _grid.update { it.copy(query = query) }
        gridQueryJob?.cancel()
        gridQueryJob = viewModelScope.launch {
            if (query.isNotBlank()) delay(SEARCH_DEBOUNCE_MILLIS)
            gridJob?.cancel()
            _grid.update { it.copy(items = emptyList(), groups = emptyList(), endReached = false, loading = true) }
            loadPage(reset = true)
        }
    }

    // ---- album selection (ROADMAP 7.4) ----

    fun toggleAlbum(id: Long) {
        _albums.update { it.copy(albumSelection = MediaSelection.toggle(it.albumSelection, id)) }
    }

    fun clearAlbumSelection() {
        _albums.update { if (it.albumSelection.isEmpty()) it else it.copy(albumSelection = emptySet()) }
    }

    fun selectAllAlbums() {
        _albums.update { state -> state.copy(albumSelection = state.albums.mapTo(LinkedHashSet()) { it.id }) }
    }

    /** Every image of a media library album, for album-wide actions (delete, rename). */
    suspend fun albumItems(album: AlbumSummary): List<MediaItem> =
        runCatching { media.loadBucketAll(album.id) }.getOrDefault(emptyList())

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
        if (album.kind == AlbumKind.HIDDEN) return folderOrdered(hiddenItems[album.id].orEmpty(), state.sort)
        return if (album.kind != AlbumKind.FOLDER) {
            runCatching { media.loadBucketAll(album.id, state.sort, marks) }.getOrDefault(state.items)
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
            val grouping = _grid.value.grouping
            val query = _grid.value.query
            if (query.isNotBlank()) {
                // A search covers the whole album at once; no sections while searching.
                if (!reset) return@launch
                val (all, folderLimit) = wholeAlbum(album, sort)
                val found = withContext(Dispatchers.Default) { MediaSearch.items(all, query) }
                _grid.update { state ->
                    if (state.album?.id != album.id || state.query != query) return@update state
                    state.copy(items = found, groups = emptyList(), loading = false, refreshing = false, endReached = true, limit = folderLimit)
                }
                return@launch
            }
            if (grouping != MediaGrouping.NONE && MediaCatalog.groupable(sort)) {
                // Sections need the whole album: it is loaded at once (MediaStore albums come from the shared scan).
                if (!reset) return@launch
                val (all, folderLimit) = wholeAlbum(album, sort)
                val groups = withContext(Dispatchers.Default) { MediaCatalog.groups(all, sort, grouping, TimeZone.getDefault()) }
                _grid.update { state ->
                    if (state.album?.id != album.id) return@update state
                    state.copy(items = all, groups = groups, loading = false, refreshing = false, endReached = true, limit = folderLimit)
                }
                return@launch
            }
            val page: List<MediaItem>
            var limit = DocumentTreeLimit.NONE
            if (album.kind == AlbumKind.FAVORITES || album.kind == AlbumKind.HIDDEN) {
                val ordered = if (album.kind == AlbumKind.HIDDEN) folderOrdered(hiddenItems[album.id].orEmpty(), sort) else favoriteItems(sort)
                page = ordered.subList(offset.coerceAtMost(ordered.size), (offset + PAGE_SIZE).coerceAtMost(ordered.size))
            } else if (album.kind != AlbumKind.FOLDER) {
                page = runCatching { media.loadBucket(album.id, sort, PAGE_SIZE, offset, marks) }.getOrDefault(emptyList())
            } else {
                val folder = album.folder
                val cached = folder?.let { runCatching { folderRepository.enumerateCached(it) }.getOrNull() } ?: (emptyList<MediaItem>() to DocumentTreeLimit.NONE)
                limit = cached.second
                val ordered = folderOrdered(cached.first, sort)
                page = ordered.subList(offset.coerceAtMost(ordered.size), (offset + PAGE_SIZE).coerceAtMost(ordered.size))
            }
            // runCatching above also swallows the cancellation of a superseded load (a reopen of the same album); such
            // a load must not publish its empty page as the finished album.
            ensureActive()
            _grid.update { state ->
                if (state.album?.id != album.id) return@update state
                state.copy(
                    items = if (reset) page else state.items + page,
                    groups = emptyList(),
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

    /** Every image of [album] in [sort] order, and the folder walk limit of an authorized folder. */
    private suspend fun wholeAlbum(album: AlbumSummary, sort: MediaSort): Pair<List<MediaItem>, DocumentTreeLimit> = when (album.kind) {
        AlbumKind.FAVORITES -> favoriteItems(sort) to DocumentTreeLimit.NONE
        AlbumKind.HIDDEN -> folderOrdered(hiddenItems[album.id].orEmpty(), sort) to DocumentTreeLimit.NONE
        AlbumKind.FOLDER -> {
            val cached = album.folder?.let { runCatching { folderRepository.enumerateCached(it) }.getOrNull() } ?: (emptyList<MediaItem>() to DocumentTreeLimit.NONE)
            folderOrdered(cached.first, sort) to cached.second
        }
        AlbumKind.MEDIA, AlbumKind.ALL -> runCatching { media.loadBucketAll(album.id, sort, marks) }.getOrDefault(emptyList()) to DocumentTreeLimit.NONE
    }

    /** Folder pages are slices of one sorted copy (an authorized folder may hold up to 20000 images). */
    private suspend fun folderOrdered(items: List<MediaItem>, sort: MediaSort): List<MediaItem> {
        folderOrder?.let { (source, cachedSort, ordered) -> if (source === items && cachedSort == sort) return ordered }
        val ordered = withContext(Dispatchers.Default) { MediaCatalog.sort(items, sort) }
        folderOrder = Triple(items, sort, ordered)
        return ordered
    }

    /** The album grid order: pinned albums first (ROADMAP 7.4), each part in the album sort. */
    private fun arrangeAlbums(albums: List<AlbumSummary>, sort: MediaSort): List<AlbumSummary> =
        FolderMarkPolicy.pinnedFirst(sortAlbums(albums, sort)) { it.pinned }

    private fun sortAlbums(albums: List<AlbumSummary>, sort: MediaSort): List<AlbumSummary> {
        val primary: Comparator<AlbumSummary> = when (sort.field) {
            MediaSortField.NAME, MediaSortField.RANDOM -> Comparator { a, b -> ImageSiblingDiscoveryPolicy.compareNaturally(a.name, b.name) }
            MediaSortField.SIZE -> compareBy { it.count }
            MediaSortField.DATE_MODIFIED -> compareBy { it.latestModifiedSeconds }
            MediaSortField.DATE_TAKEN -> compareBy { it.latestTakenMillis }
        }
        val combined = primary.thenBy { it.id }
        return albums.sortedWith(if (sort.ascending) combined else combined.reversed())
    }

    companion object {
        /** Larger pages mean fewer list appends (each one recomposes the grid) while flinging through a big album. */
        const val PAGE_SIZE = 360
        /** Typing pauses this long before a search runs (ROADMAP 7.5). */
        const val SEARCH_DEBOUNCE_MILLIS = 250L

        /** Search timings are logged only after `adb shell setprop log.tag.MapleSearch DEBUG`. */
        const val SEARCH_LOG_TAG = "MapleSearch"
        private const val GRANT_SETTLE_MILLIS = 800L
        private const val MAX_REVEAL_PAGES = 100
    }
}
