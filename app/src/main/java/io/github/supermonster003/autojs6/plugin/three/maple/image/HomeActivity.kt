package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.ReportDrawnWhen
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.AlbumGridCallbacks
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.AlbumGridScreen
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.MapleTheme
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.MediaGridScreen
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

/**
 * Standalone home (ROADMAP 6.2): the album grid with the media grid behind it in a NavHost, the one-time photo
 * access request, and the file-picker flows of 2.1.0 reachable from the menu and the no-permission state.
 */
class HomeActivity : ConfiguredActivity() {
    private lateinit var actions: HomeActions
    private lateinit var permissionGate: MediaPermissionGate
    private val gallery by viewModels<GalleryViewModel>()
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
        if (savedInstanceState == null) requestPhotoAccessOnce()
        val callbacks = AlbumGridCallbacks(
            onOpenAlbum = { album -> navigate?.invoke("album/${album.kind.name}/${album.id}") },
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
        )
        setContent {
            MapleTheme(palette = appPalette) {
                val controller = rememberNavController()
                navigate = { route -> controller.navigate(route) }
                val albums by gallery.albums.collectAsState()
                val grid by gallery.grid.collectAsState()
                NavHost(navController = controller, startDestination = ROUTE_ALBUMS) {
                    composable(ROUTE_ALBUMS) {
                        // Reports "Fully drawn" once the album list (or the no-permission state) is on screen: the
                        // startup metric of the large library check (ROADMAP 6.5).
                        ReportDrawnWhen { !albums.loading }
                        AlbumGridScreen(state = albums, home = actions.uiState, callbacks = callbacks)
                        actions.Dialogs()
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
                        MediaGridScreen(
                            state = grid,
                            onBack = { controller.popBackStack() },
                            onOpen = ::openImage,
                            onLoadMore = gallery::loadMore,
                            onSort = gallery::setGridSort,
                            onRevealed = gallery::clearReveal,
                            onColumns = gallery::setColumns,
                            onRefresh = gallery::refreshGrid,
                        )
                    }
                }
            }
        }
        actions.refresh()
    }

    private var navigate: ((String) -> Unit)? = null

    override fun onResume() {
        super.onResume()
        updatePhotoAccess()
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

    /** Opens one grid item in the paged viewer with the whole album as its source (ROADMAP 6.3). */
    private fun openImage(item: MediaItem) {
        val grid = gallery.grid.value
        val album = grid.album
        val start = ViewerSource.request(item)
        val spec = when {
            album == null -> null
            album.kind == AlbumKind.MEDIA || album.kind == AlbumKind.ALL -> ViewerSourceSpec.MediaAlbum(album.id, grid.sort, item.id)
            album.folder != null -> ViewerSourceSpec.AuthorizedFolder(album.folder.treeUri, grid.sort, item.id)
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
    }
}
