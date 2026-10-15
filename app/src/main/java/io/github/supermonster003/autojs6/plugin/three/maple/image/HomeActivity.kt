package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.ReportDrawnWhen
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.AlbumGridCallbacks
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.AlbumGridScreen
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.BatchConvertScreen
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.LayoutOverrides
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.LocalGridSettings
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.MapleEmptyState
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.MapleTheme
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.MediaGridCallbacks
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.MediaGridScreen
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.RecycleBinCallbacks
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.RecycleBinScreen
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

/**
 * Standalone home (ROADMAP 6.2): the album grid with the media grid behind it in a NavHost, the one-time photo
 * access request, and the file-picker flows of 2.1.0 reachable from the menu and the no-permission state.
 */
class HomeActivity : ConfiguredActivity() {
    private lateinit var actions: HomeActions
    private lateinit var permissionGate: MediaPermissionGate
    internal lateinit var operations: GalleryOperations
        private set
    private val gallery by viewModels<GalleryViewModel>()
    private val recycleBin by viewModels<RecycleBinViewModel>()
    private val batch by viewModels<BatchConversionViewModel>()
    private val requestPhotoAccess = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { grants ->
        permissionGate.recordResult(grants)
        updatePhotoAccess()
    }
    private val pickFolder = registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) {
            AuthorizedFolderRepository(this).add(uri)
            gallery.reload()
        }
    }

    /** The viewer reports the item it was left on; the grid scrolls there (ROADMAP 6.3 position sync). */
    private val openViewer = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val data = result.data ?: return@registerForActivityResult
        if (data.hasExtra(ImageViewerContract.EXTRA_RESULT_ITEM_ID)) {
            gallery.reveal(data.getLongExtra(ImageViewerContract.EXTRA_RESULT_ITEM_ID, 0L))
        }
    }

    /** Current photo access as the gallery sees it; refreshed on resume and after the system dialog. */
    internal var photoAccess: MediaPermissionResolution? = null
        private set

    internal val galleryForTesting: GalleryViewModel get() = gallery

    /** How many times this instance launched the system permission dialog (test hook). */
    internal var permissionRequestsForTesting: Int = 0
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        actions = HomeActions(this)
        actions.restore(savedInstanceState)
        permissionGate = MediaPermissionGate(this)
        operations = GalleryOperations(this, galleryTarget, recycleBin)
        if (savedInstanceState == null) requestPhotoAccessOnce()
        val callbacks = AlbumGridCallbacks(
            onOpenAlbum = { album -> openAlbum?.invoke(album) },
            onAlbumSort = gallery::setAlbumSort,
            onGrant = ::requestPhotoAccess,
            onManageSelected = ::requestPhotoAccess,
            onChooseFolder = { runCatching { pickFolder.launch(null) }.onFailure { showMessage(R.string.standalone_home_open_failed) } },
            onChooseImage = { actions.choose(HomeActions.MODE_VIEW) },
            onEditImage = { actions.choose(HomeActions.MODE_EDIT) },
            onConvertImage = { actions.choose(HomeActions.MODE_CONVERT) },
            onSaveOutput = { runWork { actions.prepareExport() } },
            onOpenHost = ::openHost,
            onSettings = { startActivity(Intent(this, AppSettingsActivity::class.java)) },
            onRetry = gallery::reload,
            onRefresh = gallery::refreshAlbums,
            onShowAll = { navigate?.invoke("album/${AlbumKind.ALL.name}/${MediaCatalog.ALL_BUCKETS}") },
            onRecycleBin = { navigate?.invoke(ROUTE_RECYCLE_BIN) },
            onAlbumLongPress = { album -> gallery.toggleAlbum(album.id) },
            onClearAlbumSelection = gallery::clearAlbumSelection,
            onSelectAllAlbums = gallery::selectAllAlbums,
            onSearch = gallery::startSearch,
            onSearchQuery = gallery::setSearchQuery,
            onEndSearch = gallery::endSearch,
            onOpenSearchResult = ::openSearchResult,
        )
        gallery.setLandscape(resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)
        setContent {
            MapleTheme(palette = appPalette) {
                val gridSettings by gallery.gridSettings.collectAsState()
                CompositionLocalProvider(LocalGridSettings provides gridSettings) {
                val controller = rememberNavController()
                navigate = { route -> controller.navigate(route) }
                val albums by gallery.albums.collectAsState()
                val grid by gallery.grid.collectAsState()
                val search by gallery.search.collectAsState()
                // Wide windows (ROADMAP 9.3) show albums and the open album side by side; the album open in the pane
                // survives a fold or a resize into the single-pane album page and back.
                val twoPane = LayoutOverrides.twoPane ?: (LocalConfiguration.current.screenWidthDp >= TWO_PANE_MIN_WIDTH_DP)
                var paneOpen by rememberSaveable { mutableStateOf(false) }
                SideEffect {
                    openAlbum = { album ->
                        if (twoPane) {
                            gallery.clearSelection()
                            gallery.setGridQuery("")
                            gallery.openAlbum(album)
                            paneOpen = true
                        } else {
                            controller.navigate("album/${album.kind.name}/${album.id}")
                        }
                    }
                }
                @Composable
                fun MediaPage(onBack: () -> Unit) {
                    MediaGridScreen(
                        state = grid,
                        callbacks = MediaGridCallbacks(
                            onBack = {
                                gallery.clearSelection()
                                gallery.setGridQuery("")
                                onBack()
                            },
                            onOpen = ::openImage,
                            onQuery = gallery::setGridQuery,
                            onLoadMore = gallery::loadMore,
                            onSort = { sort, grouping, thisAlbumOnly -> gallery.setGridSort(sort, grouping, thisAlbumOnly) },
                            onRevealed = gallery::clearReveal,
                            onColumns = gallery::setColumns,
                            onRefresh = gallery::refreshGrid,
                            onToggle = gallery::toggleSelection,
                            onDragStart = gallery::beginDragSelection,
                            onDragTo = gallery::dragSelectionTo,
                            onDragEnd = gallery::endDragSelection,
                            onClearSelection = gallery::clearSelection,
                            onSelectAll = gallery::selectAll,
                            onInvertSelection = gallery::invertSelection,
                            onResetCover = if (grid.customCover) gallery::resetAlbumCover else null,
                            onUnhide = grid.album?.takeIf { it.kind == AlbumKind.HIDDEN }?.let { hidden ->
                                { operations.unhideAlbum(hidden) { onBack() } }
                            },
                        ),
                        selectionActions = operations.actionsFor(grid, onSetCover = gallery::setAlbumCoverFromSelection),
                        snackbarHostState = operations.snackbar,
                    )
                }
                NavHost(navController = controller, startDestination = ROUTE_ALBUMS) {
                    composable(ROUTE_ALBUMS) {
                        // Reports "Fully drawn" once the album list (or the no-permission state) is on screen: the
                        // startup metric of the large library check (ROADMAP 6.5).
                        ReportDrawnWhen { !albums.loading }
                        val albumGrid: @Composable () -> Unit = {
                            AlbumGridScreen(
                                state = albums,
                                home = actions.uiState,
                                callbacks = callbacks,
                                albumActions = operations.albumActionsFor(albums),
                                snackbarHostState = operations.snackbar,
                                search = search,
                                mediaColumns = grid.columns,
                            )
                        }
                        if (twoPane) {
                            Row(modifier = Modifier.fillMaxSize()) {
                                Box(modifier = Modifier.weight(ALBUM_PANE_WEIGHT).fillMaxHeight().testTag("maple.home.albumPane")) { albumGrid() }
                                VerticalDivider()
                                Box(modifier = Modifier.weight(1f - ALBUM_PANE_WEIGHT).fillMaxHeight().testTag("maple.home.mediaPane")) {
                                    if (paneOpen && grid.album != null) {
                                        MediaPage(onBack = { paneOpen = false })
                                    } else {
                                        Box(
                                            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            MapleEmptyState(
                                                title = stringResource(R.string.home_pick_album),
                                                icon = painterResource(R.drawable.ic_perm_media_24),
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            albumGrid()
                            // Folded or narrowed with an album open beside the albums: it carries on as its own page.
                            LaunchedEffect(Unit) {
                                val open = grid.album
                                if (paneOpen && open != null) {
                                    paneOpen = false
                                    controller.navigate("album/${open.kind.name}/${open.id}")
                                }
                            }
                        }
                        actions.Dialogs()
                        operations.Dialogs()
                    }
                    composable(
                        route = ROUTE_ALBUM,
                        arguments = listOf(navArgument("kind") { type = NavType.StringType }, navArgument("id") { type = NavType.LongType }),
                    ) { entry ->
                        val kind = AlbumKind.valueOf(entry.arguments?.getString("kind") ?: AlbumKind.MEDIA.name)
                        val id = entry.arguments?.getLong("id") ?: 0L
                        LaunchedEffect(kind, id, albums.loading) {
                            val album = gallery.findAlbum(kind, id)
                            if (album != null && grid.album?.id != id) gallery.openAlbum(album)
                        }
                        // Unfolded or widened on an album page: the albums come back beside it.
                        LaunchedEffect(twoPane) {
                            if (twoPane) {
                                paneOpen = true
                                controller.popBackStack(ROUTE_ALBUMS, inclusive = false)
                            }
                        }
                        MediaPage(onBack = { controller.popBackStack() })
                        operations.Dialogs()
                    }
                    composable(ROUTE_BATCH_CONVERT) {
                        val pending by batch.pending.collectAsState()
                        val settings by batch.settings.collectAsState()
                        // Restored after the process was gone: nothing is waiting any more.
                        LaunchedEffect(Unit) { if (batch.pending.value.isEmpty()) controller.popBackStack() }
                        BatchConvertScreen(
                            count = pending.size,
                            settings = settings,
                            note = stringResource(R.string.batch_convert_note_gallery),
                            onChange = batch::update,
                            onStart = {
                                controller.popBackStack()
                                operations.startBatch()
                            },
                            onBack = { controller.popBackStack() },
                            // Shared preferences stay in memory once loaded, so reading them here costs nothing.
                            showLocation = ApplicationSettingsStore(this@HomeActivity).load().showLocation,
                        )
                    }
                    composable(ROUTE_RECYCLE_BIN) {
                        val bin by recycleBin.state.collectAsState()
                        LaunchedEffect(Unit) { recycleBin.load() }
                        RecycleBinScreen(
                            state = bin,
                            callbacks = RecycleBinCallbacks(
                                onBack = {
                                    recycleBin.clearSelection()
                                    controller.popBackStack()
                                },
                                onToggle = recycleBin::toggle,
                                onSelectAll = recycleBin::selectAll,
                                onClearSelection = recycleBin::clearSelection,
                                onRestore = operations::restoreFromBin,
                                onDeleteForever = operations::deleteFromBin,
                                onEmpty = operations::emptyBin,
                            ),
                            columns = grid.columns,
                            snackbarHostState = operations.snackbar,
                        )
                        operations.Dialogs()
                    }
                }
                }
            }
        }
        actions.refresh()
    }

    private var navigate: ((String) -> Unit)? = null

    /** Opens an album as its own page, or beside the albums in a wide window (ROADMAP 9.3). */
    private var openAlbum: ((AlbumSummary) -> Unit)? = null

    /** The media grid selection as the operations see it. */
    private val galleryTarget = object : OperationTarget {
        override fun openBatchConversion() {
            navigate?.invoke(ROUTE_BATCH_CONVERT)
        }

        override suspend fun items(): List<MediaItem> = gallery.selectedItems()
        override val albumKind: AlbumKind? get() = gallery.grid.value.album?.kind
        override val openAlbum: AlbumSummary? get() = gallery.grid.value.album
        override val favoriteIds: Set<Long> get() = gallery.grid.value.favoriteIds
        override suspend fun albums(): List<AlbumSummary> = gallery.albums.value.albums
        override fun clearSelection() = gallery.clearSelection()
        override fun afterWrite() = gallery.refreshAfterWrite()
        override fun selectedAlbums(): List<AlbumSummary> = gallery.albums.value.selectedAlbums
        override fun clearAlbumSelection() = gallery.clearAlbumSelection()
        override suspend fun albumItems(album: AlbumSummary): List<MediaItem> = gallery.albumItems(album)
    }

    override fun onStart() {
        super.onStart()
        gallery.setVisible(true)
        gallery.refreshHiddenIfChanged()
        // Back from the viewer: results may have been deleted, moved or renamed there.
        gallery.refreshSearch()
    }

    override fun onStop() {
        gallery.setVisible(false)
        super.onStop()
    }

    override fun onResume() {
        super.onResume()
        updatePhotoAccess()
        // Grid settings changed in the settings apply as soon as the grids are back (ROADMAP 9.2).
        gallery.refreshGridSettings()
    }

    private fun updatePhotoAccess() {
        val resolution = permissionGate.resolve()
        photoAccess = resolution
        gallery.setAccess(resolution)
    }

    /**
     * The standalone home asks for photo access exactly once, on its first launch (ROADMAP D1 / T5); afterwards only
     * the explicit buttons of the gallery empty state and the storage permission settings ask again.
     */
    private fun requestPhotoAccessOnce() {
        val resolution = permissionGate.resolve()
        photoAccess = resolution
        gallery.setAccess(resolution)
        if (!resolution.autoRequestOnLaunch) return
        permissionGate.markRequested()
        permissionRequestsForTesting += 1
        requestPhotoAccess.launch(resolution.permissionsToRequest.toTypedArray())
    }

    /** Explicit request from the gallery empty state or the settings page; never automatic. */
    internal fun requestPhotoAccess() {
        val resolution = permissionGate.resolve()
        when (resolution.nextAction) {
            MediaPermissionAction.REQUEST, MediaPermissionAction.MANAGE_SELECTED -> {
                permissionGate.markRequested()
                permissionRequestsForTesting += 1
                requestPhotoAccess.launch(resolution.permissionsToRequest.toTypedArray())
            }
            MediaPermissionAction.OPEN_APP_SETTINGS -> runCatching { startActivity(permissionGate.appSettingsIntent()) }
                .onFailure { showMessage(R.string.standalone_home_open_failed) }
            MediaPermissionAction.NONE -> Unit
        }
    }

    /** A home search result opens in the viewer with every result as its pages (ROADMAP 7.5). */
    private fun openSearchResult(item: MediaItem) {
        val query = gallery.search.value.query
        val request = ImageViewerLaunchRequest.gallery(ViewerSource.request(item), ViewerSourceSpec.Search(query, MediaCatalog.ALL_BUCKETS, MediaSort.DEFAULT, item.id))
        runCatching { openViewer.launch(ImageViewerContract.viewerIntent(this, request)) }.onFailure { showMessage(R.string.standalone_home_open_failed) }
    }

    /** Opens one grid item in the paged viewer with the whole album as its source (ROADMAP 6.3). */
    private fun openImage(item: MediaItem) {
        val grid = gallery.grid.value
        val album = grid.album
        val start = ViewerSource.request(item)
        val library = album != null && (album.kind == AlbumKind.MEDIA || album.kind == AlbumKind.ALL || album.kind == AlbumKind.FAVORITES)
        val spec = when {
            album == null -> null
            // The viewer pages through the search results of the album (ROADMAP 7.5).
            library && grid.query.isNotBlank() -> ViewerSourceSpec.Search(grid.query, album.id, grid.sort, item.id)
            library -> ViewerSourceSpec.MediaAlbum(album.id, grid.sort, item.id)
            album.folder != null -> ViewerSourceSpec.AuthorizedFolder(album.folder.treeUri, grid.sort, item.id)
            album.kind == AlbumKind.HIDDEN -> ViewerSourceSpec.HiddenFolder(album.subtitle, grid.sort, item.id)
            else -> null
        }
        val request = if (spec == null) {
            ImageViewerLaunchRequest.single(start, editable = true)
        } else {
            ImageViewerLaunchRequest.gallery(start, spec)
        }
        runCatching { openViewer.launch(ImageViewerContract.viewerIntent(this, request)) }
            .onFailure { showMessage(R.string.standalone_home_open_failed) }
    }

    private fun openHost() {
        val intent = packageManager.getLaunchIntentForPackage("org.autojs.autojs6")
        if (intent == null) showMessage(R.string.standalone_home_host_missing)
        else runCatching { startActivity(intent) }.onFailure { showMessage(R.string.standalone_home_open_failed) }
    }

    /** Runs one file task at a time; rows are disabled and the status line announces progress meanwhile. */
    internal fun runWork(block: suspend () -> Unit) {
        if (actions.uiState.working) return
        actions.uiState = actions.uiState.copy(working = true, statusText = getString(R.string.standalone_home_working))
        lifecycleScope.launch {
            try { block() }
            catch (cancelled: CancellationException) { throw cancelled }
            catch (_: Exception) { showMessage(R.string.standalone_home_open_failed) }
            finally {
                actions.uiState = actions.uiState.copy(working = false, statusText = "")
                actions.refresh()
            }
        }
    }

    internal fun showMessage(resource: Int) = Toast.makeText(this, resource, Toast.LENGTH_LONG).show()

    override fun onSaveInstanceState(outState: Bundle) {
        actions.save(outState)
        super.onSaveInstanceState(outState)
    }

    private companion object {
        const val ROUTE_ALBUMS = "albums"
        const val ROUTE_ALBUM = "album/{kind}/{id}"

        /** From this window width (expanded, ROADMAP 9.3) the albums and the open album share the screen. */
        const val TWO_PANE_MIN_WIDTH_DP = 840
        const val ALBUM_PANE_WEIGHT = 0.4f
        const val ROUTE_RECYCLE_BIN = "recycle-bin"
        const val ROUTE_BATCH_CONVERT = "batch-convert"
    }
}
