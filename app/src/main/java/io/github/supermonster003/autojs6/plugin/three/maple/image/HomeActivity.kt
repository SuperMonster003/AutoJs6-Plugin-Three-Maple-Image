package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.content.ClipData
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
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

    /** Current photo access as the gallery sees it; refreshed on resume and after the system dialog. */
    internal var photoAccess: MediaPermissionResolution? = null
        private set

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
        )
        setContent {
            MapleTheme(palette = appPalette) {
                val controller = rememberNavController()
                navigate = { route -> controller.navigate(route) }
                val albums by gallery.albums.collectAsState()
                val grid by gallery.grid.collectAsState()
                NavHost(navController = controller, startDestination = ROUTE_ALBUMS) {
                    composable(ROUTE_ALBUMS) {
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

    /** Opens one grid item in the viewer through the external entry, which already handles single content URIs. */
    private fun openImage(item: MediaItem) {
        val uri = Uri.parse(item.contentUri)
        val intent = Intent(this, ExternalViewActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            setDataAndType(uri, item.mimeType.takeIf { it.startsWith("image/") } ?: "image/*")
            clipData = ClipData.newRawUri(item.displayName, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        runCatching { startActivity(intent) }.onFailure { showMessage(R.string.standalone_home_open_failed) }
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
