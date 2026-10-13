package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.annotation.VisibleForTesting
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.HostDirectoryCallbacks
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.HostDirectoryScreen
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.HostDirectoryUiState
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.MapleTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * "View as album" for one directory chosen in the AutoJs6 file manager (ROADMAP 6.4): the host session lists the
 * directory and its subfolders (`listChildren` with nested relative paths), thumbnails and full images come through
 * the private [HostSessionImageProvider], and the viewer is opened with the images of the current folder as host
 * pages that borrow, but never close, this Activity's session. No storage permission is involved and no absolute
 * path is shown; management actions are unavailable in this mode.
 */
class ExplorerDirectoryActivity : ConfiguredActivity() {

    private var request: ExplorerDirectoryRequest? = null
    private lateinit var browser: HostDirectoryBrowser
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var listJob: Job? = null
    private var state by mutableStateOf(HostDirectoryUiState())
    private val ancestors = ArrayDeque<String>()
    private var currentPath = ""
    private var thumbnailToken: String? = null
    private var sessionClosed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val resolved = ImageRequestPolicy.resolveExplorerDirectory(intent)
        if (resolved == null) {
            ImageRequestPolicy.closeExplorerSession(intent)
            finish()
            return
        }
        request = resolved
        browser = HostDirectoryBrowser(resolved.hostSession, resolved.targetId)
        savedInstanceState?.getStringArrayList(STATE_ANCESTORS)?.let(ancestors::addAll)
        currentPath = savedInstanceState?.getString(STATE_PATH).orEmpty()
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() = goUp()
        })
        val callbacks = HostDirectoryCallbacks(
            onBack = ::goUp,
            onOpenFolder = { entry ->
                if (!state.depthLimited) {
                    ancestors.addLast(currentPath)
                    open(entry.relativePath)
                }
            },
            onOpenImage = ::openImage,
            onRetry = { open(currentPath) },
            thumbnail = ::thumbnailModel,
        )
        state = state.copy(title = resolved.displayName, columns = GalleryPreferences(this).mediaColumns)
        setContent {
            MapleTheme(palette = appPalette) {
                HostDirectoryScreen(state = state, callbacks = callbacks)
            }
        }
        open(currentPath)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putStringArrayList(STATE_ANCESTORS, ArrayList(ancestors))
        outState.putString(STATE_PATH, currentPath)
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        listJob?.cancel()
        scope.cancel()
        HostSessionImageRegistry.unregister(thumbnailToken)
        thumbnailToken = null
        if (!isChangingConfigurations) closeSession()
        super.onDestroy()
    }

    @get:VisibleForTesting
    internal val stateForTesting: HostDirectoryUiState
        get() = state

    private fun open(path: String) {
        val resolved = request ?: return
        listJob?.cancel()
        currentPath = path
        state = state.copy(
            title = if (path.isEmpty()) resolved.displayName else path.substringAfterLast('/'),
            path = path,
            loading = true,
            failed = false,
        )
        listJob = scope.launch {
            val listing = runCatching { withContext(Dispatchers.IO) { browser.list(path) } }.getOrNull()
            if (isFinishing || isDestroyed || currentPath != path) return@launch
            if (listing == null) {
                state = state.copy(loading = false, failed = true, folders = emptyList(), images = emptyList())
                return@launch
            }
            registerThumbnails(listing)
            state = state.copy(
                folders = listing.folders,
                images = listing.images,
                truncated = listing.truncated,
                depthLimited = listing.depth >= HostDirectoryPolicy.MAX_DEPTH,
                loading = false,
                failed = false,
            )
        }
    }

    private fun registerThumbnails(listing: HostDirectoryListing) {
        val resolved = request ?: return
        HostSessionImageRegistry.unregister(thumbnailToken)
        thumbnailToken = if (listing.images.isEmpty()) {
            null
        } else {
            HostSessionImageRegistry.registerRoutes(
                hostSession = resolved.hostSession,
                targetId = resolved.targetId,
                routes = listing.images.map { entry ->
                    entry.relativePath to HostSessionImagePage(entry.displayName, entry.size, entry.mimeType)
                },
            )
        }
    }

    private fun thumbnailModel(index: Int): Any? {
        val token = thumbnailToken ?: return null
        if (index !in state.images.indices) return null
        return runCatching { HostSessionImageRegistry.imageUri(token, index) }.getOrNull()
    }

    /** Opens the viewer on the tapped image with a bounded window of this folder's images as host pages. */
    private fun openImage(index: Int) {
        val resolved = request ?: return
        val images = state.images
        if (index !in images.indices) return
        val maxPages = ImageSiblingDiscoveryPolicy.MAX_PAGES
        val windowStart = (index - maxPages / 2).coerceIn(0, (images.size - maxPages).coerceAtLeast(0))
        val window = images.drop(windowStart).take(maxPages)
        val pages = window.mapIndexed { pageIndex, entry ->
            ImageViewerPage(
                image = ImageViewerRequest(
                    targetUri = ImageViewerContract.hostImageUri(pageIndex),
                    displayName = entry.displayName,
                    declaredSize = entry.size,
                    mimeType = entry.mimeType,
                ),
                hostRelativePath = entry.relativePath,
            )
        }
        val launch = ImageViewerLaunchRequest(
            pages = pages,
            startIndex = index - windowStart,
            hostSession = resolved.hostSession,
            hostTargetId = resolved.targetId,
            editable = false,
            ownsHostSession = false,
        )
        runCatching { startActivity(ImageViewerContract.viewerIntent(this, launch)) }
    }

    private fun goUp() {
        val parent = ancestors.removeLastOrNull()
        if (parent == null) {
            finish()
        } else {
            open(parent)
        }
    }

    private fun closeSession() {
        if (sessionClosed) return
        sessionClosed = true
        request?.hostSession?.let { session -> runCatching { session.close() } }
    }

    private companion object {
        const val STATE_ANCESTORS = "ancestors"
        const val STATE_PATH = "path"
    }
}
