package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.app.ActivityManager
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ComponentCallbacks2
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.print.PrintAttributes
import android.print.PrintManager
import android.provider.Settings
import android.text.format.Formatter
import android.view.KeyEvent
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.VisibleForTesting
import androidx.compose.animation.core.Animatable as ComposeAnimatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.AndroidUiDispatcher
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.MapleTheme
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.ShareCopyDialog
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.ShareCopyDialogState
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.SlideshowDialog
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.SlideshowDialogState
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.ViewerCallbacks
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.ViewerManagement
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.ViewerPagerState
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.ViewerPagerUi
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.ViewerScreen
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.ViewerUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Read-only image viewer. The launching intent resolves to a [ViewerSource] (ROADMAP 6.3): intent-borne page lists
 * for the host and external entries, or an album spec the viewer enumerates itself for the standalone gallery. A
 * HorizontalPager keeps the current page and both neighbours composed; every composed page owns a [ViewerPageHost]
 * with its [ZoomableImageView] and decode job (direct content URIs, host session siblings, tiled large images, EXIF
 * normalization through Glide). The overlay chrome is a Compose [ViewerScreen] driven by one [ViewerUiState].
 */
class ImageViewerActivity : ConfiguredActivity() {

    private lateinit var launchRequest: ImageViewerLaunchRequest
    private var source: ViewerSource? = null
    private var pagerState: ViewerPagerState? = null
    private var pagerUi by mutableStateOf<ViewerPagerUi?>(null)
    /** Page hosts of the installed source; a reload after a write installs a fresh map (see [installSource]). */
    private var hosts = HashMap<Int, ViewerPageHost>()
    private lateinit var operations: GalleryOperations
    private var favoriteIds: Set<Long> = emptySet()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    /** Pager animations need a frame clock, which the plain main dispatcher does not carry. */
    private val frameScope = CoroutineScope(SupervisorJob() + AndroidUiDispatcher.Main)
    private var sourceJob: Job? = null
    private var pageJumpJob: Job? = null
    private var zoomIndicatorJob: Job? = null
    private var jumpTarget = -1
    private var hostImageSessionToken: String? = null
    private var currentIndex = -1
    private var hostSessionClosed = false
    private var restoredIndex = -1
    private var restoredAnimationIndex = -1
    private var restoredAnimationPaused = false
    private var restoredRotationIndex = -1
    private var restoredRotationQuarterTurns = ImageRotationState.ORIGINAL_QUARTER_TURNS
    private var restoredExpandedDetailsIndex = -1
    private var uiState by mutableStateOf(ViewerUiState())
    private var editBusy = false
    /** Gestures (ROADMAP D8 / 8.2 / 9.2), applied to every page view and refreshed on resume. */
    private var viewerSettings = ViewerSettings()

    /** The window brightness when a brightness drag started (ROADMAP 9.2); null between drags. */
    private var brightnessDragStart: Float? = null

    /** Where the last brightness drag left the window; it then wins over "maximum brightness" until the viewer closes. */
    private var draggedBrightness: Float? = null
    private var rotationHintDismissed = false
    private var hostTrashBusy = false
    private val hostTrashedTargetIds = linkedSetOf<String>()

    /** "Show location" (ROADMAP 9.1, T14): the details show the position and offer a map. */
    private var showLocation = false

    /** Extended details (ROADMAP 9.1): the chosen fields replace the metadata line while it is on. */
    private var extendedDetails = false
    private var extendedDetailFields = emptySet<ExtendedDetailField>()

    // Slideshow (ROADMAP 9.1): the dialog draft, the running plan and the timer of the next slide.
    private var slideshowDialog by mutableStateOf<SlideshowDialogState?>(null)
    private var slideshowPlan: SlideshowPlan? = null
    private var slideshowSettings: SlideshowSettings? = null
    private var slideshowJob: Job? = null
    private var slideshowNavigating = false
    private val slideshowVeil = ComposeAnimatable(0f)
    // "Share a copy" (ROADMAP 9.1): the dialog draft and the copy being made.
    private var shareCopyDialog by mutableStateOf<ShareCopyDialogState?>(null)
    private var shareCopyJob: Job? = null

    private val slideshowBack = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() = stopSlideshow()
    }
    private var rotationSaveBusy = false
    private val rotationWritable = mutableMapOf<String, Boolean>()
    private val rotationChecks = mutableSetOf<String>()
    private val editHandoff = StandaloneEditHandoff(
        activity = this,
        onBusy = { busy -> editBusy = busy },
        onMessage = { resource -> Toast.makeText(this, resource, Toast.LENGTH_SHORT).show() },
        overwrite = { original, output -> operations.overwriteOriginal(original, output) },
    )
    private val memoryClassMebibytes: Int by lazy {
        getSystemService(ActivityManager::class.java)?.memoryClass ?: 128
    }

    override val usesDarkSystemBars: Boolean get() = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        launchRequest = ImageViewerContract.resolve(intent)?.withoutTrashedTargets(savedInstanceState) ?: run {
            ImageViewerContract.closeAttachedHostSession(intent)
            finish()
            return
        }
        if (launchRequest.hostSession != null && !registerHostRoutes(launchRequest.pages)) {
            closeHostSession()
            finish()
            return
        }
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.auto(
                Color.TRANSPARENT,
                // Below O the platform cannot draw dark navigation icons, so keep a light scrim there.
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) LEGACY_NAVIGATION_SCRIM else Color.TRANSPARENT,
            ),
        )
        readRestoredState(savedInstanceState)
        ApplicationSettingsStore(this).load().let { settings ->
            viewerSettings = settings.viewer
            showLocation = settings.showLocation
            extendedDetails = settings.extendedDetails
            extendedDetailFields = settings.extendedDetailFields
        }
        operations = GalleryOperations(this, viewerTarget)
        val callbacks = ViewerCallbacks(
            onBack = ::finish,
            onToggleDetails = { setExifDetailsExpanded(!uiState.detailsExpanded) },
            onDismissDetails = { setExifDetailsExpanded(false) },
            onRotate = { currentHost()?.view?.rotateClockwise90() },
            onEdit = ::editImage,
            onShare = ::shareImage,
            onOpenWith = ::openWithAnotherApp,
            onResetZoom = { hideZoomIndicator(); currentHost()?.view?.resetZoom(); syncRotationHint() },
            onPrint = ::printImage,
            onOpenMap = ::openMap,
            onSlideshow = ::openSlideshowDialog,
            onShareCopy = ::openShareCopyDialog,
            onEditWith = ::editWithAnotherApp,
            onSetWallpaper = ::setAsWallpaper,
            onResumeSlideshow = ::resumeSlideshow,
            onStopSlideshow = ::stopSlideshow,
            onReleaseHistory = ::showReleaseHistory,
            onToggleAnimation = { currentHost()?.animation?.toggle() },
            onPageChanged = ::onPagerPageChanged,
            onSelectPage = ::jumpToPage,
            onUndoRotation = { currentHost()?.view?.undoRotation() },
            onSaveRotation = ::saveRotation,
            onDismissRotationHint = ::dismissRotationHint,
            onToggleFavorite = operations::favorite,
            onDelete = ::deleteCurrent,
            onRename = operations::rename,
            onMove = operations::move,
            onCopy = operations::copy,
        )
        uiState = uiState.copy(
            title = launchRequest.startPage.image.displayName,
            metadataText = getString(R.string.metadata_loading),
            detailsSummary = getString(R.string.metadata_loading),
            exifText = getString(R.string.exif_unavailable),
            controlsVisible = savedInstanceState?.getBoolean(STATE_CONTROLS_VISIBLE, true) ?: true,
        )
        applyDisplaySettings()
        onBackPressedDispatcher.addCallback(this, slideshowBack)
        // Copies shared a day ago or earlier are gone by now (ROADMAP 9.1).
        scope.launch(Dispatchers.IO) { ShareCopies.cleanup(applicationContext) }
        setContent {
            MapleTheme(palette = appPalette) {
                ViewerScreen(
                    state = uiState,
                    pager = pagerUi,
                    callbacks = callbacks,
                    // Every viewer shows it: an overwrite of an opened file offers its undo there too.
                    snackbarHostState = operations.snackbar,
                    slideshowVeil = { slideshowVeil.value },
                )
                operations.Dialogs()
                shareCopyDialog?.let { draft ->
                    ShareCopyDialog(draft, onDismissRequest = { shareCopyDialog = null }, onShare = { draft.request()?.let(::shareCopy) })
                }
                slideshowDialog?.let { draft ->
                    SlideshowDialog(draft, onDismissRequest = { slideshowDialog = null }, onStart = { startSlideshow(draft.settings()) })
                }
            }
        }
        if (launchRequest.source is ViewerSourceSpec.MediaAlbum || launchRequest.source is ViewerSourceSpec.Search) {
            scope.launch {
                FavoritesRepository(this@ImageViewerActivity).observeIds().collect { ids ->
                    favoriteIds = ids
                    uiState = uiState.copy(management = managementState())
                }
            }
        }
        sourceJob = scope.launch {
            val resolved = ViewerSourceLoader.create(this@ImageViewerActivity).load(launchRequest)
            if (!isFinishing && !isDestroyed) installSource(resolved)
        }
    }

    /** Request targets moved to the host Trash before a recreation stay away; null when no page is left (ROADMAP 7.2). */
    private fun ImageViewerLaunchRequest.withoutTrashedTargets(savedInstanceState: Bundle?): ImageViewerLaunchRequest? {
        val trashed = savedInstanceState?.getStringArrayList(STATE_HOST_TRASHED_TARGETS).orEmpty()
        if (hostSession == null || trashed.isEmpty()) return this
        hostTrashedTargetIds += trashed
        val kept = pages.filterNot { it.hostTargetId in hostTrashedTargetIds }
        if (kept.isEmpty()) return null
        val start = pages.subList(0, startIndex).count { it.hostTargetId !in hostTrashedTargetIds }
        return copy(pages = kept, startIndex = start.coerceAtMost(kept.lastIndex))
    }

    /**
     * Gives the pages read through the host session (siblings, directory entries) their provider URIs; true when that
     * worked or no page needs one. A changed page list gets a new token: Glide caches by URI, so a `token/index` must
     * never name another image.
     */
    private fun registerHostRoutes(pages: List<ImageViewerPage>): Boolean {
        HostSessionImageRegistry.unregister(hostImageSessionToken)
        hostImageSessionToken = null
        if (pages.all { it.hostRelativePath.isNullOrEmpty() }) return true
        hostImageSessionToken = runCatching {
            HostSessionImageRegistry.register(
                requireNotNull(launchRequest.hostSession),
                requireNotNull(launchRequest.hostTargetId),
                pages,
            )
        }.getOrNull()
        return hostImageSessionToken != null
    }

    private fun readRestoredState(savedInstanceState: Bundle?) {
        restoredIndex = savedInstanceState?.getInt(STATE_CURRENT_INDEX, -1) ?: -1
        restoredAnimationIndex = savedInstanceState?.getInt(STATE_ANIMATION_INDEX, -1) ?: -1
        restoredAnimationPaused = savedInstanceState?.getBoolean(STATE_ANIMATION_PAUSED, false) ?: false
        restoredRotationIndex = savedInstanceState?.getInt(STATE_ROTATION_INDEX, -1) ?: -1
        restoredRotationQuarterTurns = savedInstanceState
            ?.getInt(STATE_ROTATION_QUARTER_TURNS, ImageRotationState.ORIGINAL_QUARTER_TURNS)
            ?: ImageRotationState.ORIGINAL_QUARTER_TURNS
        restoredExpandedDetailsIndex = savedInstanceState?.getInt(STATE_EXPANDED_DETAILS_INDEX, -1) ?: -1
    }

    private fun installSource(resolved: ViewerSource) {
        source = resolved
        // The pager's acquire / release close over this map, so the pages of a replaced source release only
        // their own hosts.
        val pageHosts = HashMap<Int, ViewerPageHost>()
        hosts = pageHosts
        val count = resolved.pages.size
        if (restoredIndex !in resolved.pages.indices) restoredIndex = -1
        if (restoredAnimationIndex !in resolved.pages.indices) restoredAnimationIndex = -1
        if (restoredRotationIndex !in resolved.pages.indices) restoredRotationIndex = -1
        if (restoredExpandedDetailsIndex !in resolved.pages.indices) restoredExpandedDetailsIndex = -1
        val initial = restoredIndex.takeIf { it >= 0 } ?: resolved.startIndex
        val state = ViewerPagerState(initial) { count }
        pagerState = state
        pagerUi = ViewerPagerUi(
            state = state,
            pageCount = count,
            pageKey = { index -> resolved.pages[index].image.targetUri.toString() },
            acquire = { index -> acquirePage(pageHosts, index) },
            release = { index -> releasePage(pageHosts, index) },
            thumbnail = ::thumbnailModel,
            thumbnailDescription = { index -> getString(R.string.viewer_thumbnail_description, index + 1, count) },
        )
        onPageChanged(initial)
    }

    override fun onResume() {
        super.onResume()
        val settings = ApplicationSettingsStore(this).load()
        if (settings.viewer != viewerSettings) {
            viewerSettings = settings.viewer
            hosts.values.forEach { host -> applyGestures(host.view) }
            applyDisplaySettings()
            syncRotationHint()
        }
        val detailsChanged = settings.showLocation != showLocation ||
            settings.extendedDetails != extendedDetails ||
            settings.extendedDetailFields != extendedDetailFields
        if (detailsChanged) {
            showLocation = settings.showLocation
            extendedDetails = settings.extendedDetails
            extendedDetailFields = settings.extendedDetailFields
            if (source != null) syncCurrentPage(pageChanged = false)
        }
    }

    override fun onStart() {
        super.onStart()
        val host = currentHost() ?: return
        host.view.post {
            if (isFinishing || isDestroyed) return@post
            // Glide restarts animated drawables on lifecycle start; keep the user's pause and the neighbours quiet.
            host.animation.reapplyRequestedPlayback()
            hosts.values.forEach { other -> if (other !== host) (other.view.drawable as? Animatable)?.stop() }
        }
    }

    /**
     * Keyboard and D-pad (ROADMAP 9.3): left / right (and page up / down) turn pages, the delete keys ask to delete
     * where the delete button would.
     */
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        // Paging keys always page and come before Compose, which would take them to move focus between the bar
        // buttons; Tab and up / down still do that.
        val direction = when (KeyboardPolicy.viewerAction(event.keyCode)) {
            ViewerKeyAction.PREVIOUS -> ZoomableImageView.DIRECTION_PREVIOUS
            ViewerKeyAction.NEXT -> ZoomableImageView.DIRECTION_NEXT
            else -> null
        }
        if (direction != null) {
            if (event.action == KeyEvent.ACTION_DOWN) flipPage(direction)
            return true
        }
        return super.dispatchKeyEvent(event)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (KeyboardPolicy.viewerAction(keyCode) != ViewerKeyAction.DELETE || (uiState.management == null && !uiState.hostDelete)) {
            return super.onKeyDown(keyCode, event)
        }
        if (event.repeatCount == 0) deleteCurrent()
        return true
    }

    override fun onSaveInstanceState(outState: Bundle) {
        val host = currentHost()
        if (::launchRequest.isInitialized && currentIndex >= 0) {
            outState.putInt(STATE_CURRENT_INDEX, currentIndex)
        }
        if (host != null && host.animation.playback != ImageAnimationPlayback.UNAVAILABLE) {
            outState.putInt(STATE_ANIMATION_INDEX, currentIndex)
            outState.putBoolean(STATE_ANIMATION_PAUSED, host.animation.playback == ImageAnimationPlayback.PAUSED)
        }
        if (host != null && host.view.currentRotationQuarterTurns != ImageRotationState.ORIGINAL_QUARTER_TURNS) {
            outState.putInt(STATE_ROTATION_INDEX, currentIndex)
            outState.putInt(STATE_ROTATION_QUARTER_TURNS, host.view.currentRotationQuarterTurns)
        }
        if (uiState.detailsExpanded && currentIndex >= 0) {
            outState.putInt(STATE_EXPANDED_DETAILS_INDEX, currentIndex)
        }
        outState.putBoolean(STATE_CONTROLS_VISIBLE, uiState.controlsVisible)
        if (hostTrashedTargetIds.isNotEmpty()) {
            outState.putStringArrayList(STATE_HOST_TRASHED_TARGETS, ArrayList(hostTrashedTargetIds))
        }
        super.onSaveInstanceState(outState)
    }

    override fun onPause() {
        // Leaving the viewer holds a running show; it comes back paused with Resume and Stop.
        pauseSlideshow()
        super.onPause()
    }

    override fun onDestroy() {
        slideshowJob?.cancel()
        sourceJob?.cancel()
        pageJumpJob?.cancel()
        zoomIndicatorJob?.cancel()
        hosts.values.toList().forEach { host -> host.release(this) }
        hosts.clear()
        scope.cancel()
        frameScope.cancel()
        HostSessionImageRegistry.unregister(hostImageSessionToken)
        hostImageSessionToken = null
        if (!isChangingConfigurations) {
            closeHostSession()
            if (isFinishing) editHandoff.discard()
        }
        super.onDestroy()
    }

    @Suppress("DEPRECATION")
    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        if (level >= ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW) {
            hosts.values.forEach { host -> host.tiledImage?.trimTileCache() }
        }
    }

    override fun onLowMemory() {
        hosts.values.forEach { host -> host.tiledImage?.trimTileCache() }
        super.onLowMemory()
    }

    // region Test hooks

    @get:VisibleForTesting
    internal val viewerStateForTesting: ViewerUiState
        get() = uiState

    @get:VisibleForTesting
    internal val sourceForTesting: ViewerSource?
        get() = source

    /** The zoomable surface of the current page, or null while the page list is still being enumerated. */
    @VisibleForTesting
    internal fun currentImageViewForTesting(): ZoomableImageView? = currentHost()?.view

    @VisibleForTesting
    internal fun toggleDetailsForTesting() = setExifDetailsExpanded(!uiState.detailsExpanded)

    @VisibleForTesting
    internal fun rotateForTesting(): Boolean = currentHost()?.view?.rotateClockwise90() == true

    @VisibleForTesting
    internal fun resetZoomForTesting() {
        hideZoomIndicator()
        currentHost()?.view?.resetZoom()
        syncRotationHint()
    }

    @VisibleForTesting
    internal fun currentRotationGestureEnabledForTesting(): Boolean? = currentHost()?.view?.rotationGestureEnabled

    @VisibleForTesting
    internal val windowBrightnessForTesting: Float get() = window.attributes.screenBrightness

    @VisibleForTesting
    internal val keepsScreenOnForTesting: Boolean
        get() = window.attributes.flags and WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON != 0

    @VisibleForTesting
    internal fun toggleAnimationForTesting() {
        currentHost()?.animation?.toggle()
    }

    @VisibleForTesting
    internal fun flipPageForTesting(direction: Int) = flipPage(direction)

    @VisibleForTesting
    internal fun toggleControlsForTesting() = setControlsVisible(!uiState.controlsVisible)

    // endregion

    // region Pages

    private fun currentHost(): ViewerPageHost? = hosts[currentIndex]

    private fun acquirePage(index: Int): ViewerPageHost = acquirePage(hosts, index)

    /**
     * The host is in [map] before its load starts: a metadata read can finish on the IO thread before the main thread
     * suspends (withContext then continues without suspending), and isLive must already see the host then, or the
     * load is dropped and the page keeps its spinner for good.
     */
    private fun acquirePage(map: HashMap<Int, ViewerPageHost>, index: Int): ViewerPageHost =
        map[index] ?: createHost(index).also { host ->
            map[index] = host
            startLoad(host)
        }

    /** The gallery page as the management operations see it (ROADMAP 7.0 viewer actions). */
    private val viewerTarget = object : OperationTarget {
        override suspend fun items(): List<MediaItem> = listOfNotNull(source?.items?.getOrNull(currentIndex))

        override val albumKind: AlbumKind?
            get() = when (val spec = launchRequest.source) {
                is ViewerSourceSpec.AuthorizedFolder -> AlbumKind.FOLDER
                is ViewerSourceSpec.HiddenFolder -> AlbumKind.HIDDEN
                is ViewerSourceSpec.Search -> when (spec.bucketId) {
                    MediaCatalog.ALL_BUCKETS -> AlbumKind.ALL
                    MediaCatalog.FAVORITES -> AlbumKind.FAVORITES
                    else -> AlbumKind.MEDIA
                }
                is ViewerSourceSpec.MediaAlbum -> when (spec.bucketId) {
                    MediaCatalog.ALL_BUCKETS -> AlbumKind.ALL
                    MediaCatalog.FAVORITES -> AlbumKind.FAVORITES
                    else -> AlbumKind.MEDIA
                }
                null -> null
            }

        override val openAlbum: AlbumSummary?
            get() {
                val bucketId = when (val spec = launchRequest.source) {
                    is ViewerSourceSpec.MediaAlbum -> spec.bucketId
                    is ViewerSourceSpec.Search -> spec.bucketId
                    else -> return null
                }
                if (albumKind != AlbumKind.MEDIA) return null
                val path = source?.items?.getOrNull(currentIndex)?.relativePath.orEmpty()
                val item = source?.items?.getOrNull(currentIndex)
                return AlbumSummary(AlbumKind.MEDIA, bucketId, "", -1, null, path, volume = item?.volume ?: MediaCatalog.PRIMARY_VOLUME)
            }

        override val favoriteIds: Set<Long> get() = this@ImageViewerActivity.favoriteIds

        override suspend fun albums(): List<AlbumSummary> {
            val root = getString(R.string.gallery_album_root)
            return runCatching { MediaStoreRepository(this@ImageViewerActivity).loadBuckets() }.getOrDefault(emptyList()).map { bucket ->
                val name = bucket.name.ifBlank { bucket.relativePath.trimEnd('/').substringAfterLast('/').ifBlank { root } }
                AlbumSummary(AlbumKind.MEDIA, bucket.id, name, bucket.count, bucket.cover.contentUri, bucket.relativePath, volume = bucket.volume)
            }
        }

        override fun clearSelection() = Unit

        override fun afterWrite() = reloadSource()
    }

    /**
     * After a delete, move or rename: enumerate the album again and stay on the same image, or on its next (then
     * previous) neighbour once it left the album; an emptied album closes the viewer.
     */
    private fun reloadSource() {
        val spec = launchRequest.source ?: return
        val previous = source?.items ?: return
        val index = currentIndex
        sourceJob?.cancel()
        sourceJob = scope.launch {
            // The write changed the library now; neither the shared scan nor the folder cache may answer.
            if (spec is ViewerSourceSpec.AuthorizedFolder) AuthorizedFolderRepository(this@ImageViewerActivity).invalidateCache()
            else MediaStoreRepository(this@ImageViewerActivity).invalidate()
            val items = ViewerSourceLoader.create(this@ImageViewerActivity).items(spec)
            if (isFinishing || isDestroyed) return@launch
            if (items.isEmpty()) {
                finish()
                return@launch
            }
            val ids = items.mapTo(HashSet()) { it.id }
            val order = listOf(index) + (index + 1 until previous.size) + (index - 1 downTo 0)
            val start = order.mapNotNull { previous.getOrNull(it)?.id }.firstOrNull { it in ids } ?: items.first().id
            val kind = requireNotNull(source).kind
            currentIndex = -1
            restoredIndex = -1
            restoredAnimationIndex = -1
            restoredRotationIndex = -1
            restoredExpandedDetailsIndex = -1
            // The old pages release their hosts from the old map when the new pager replaces them.
            installSource(ViewerSource.fromItems(kind, items, start, ViewerSource.page(items.first { it.id == start }), launchRequest.editable))
        }
    }

    /** A request target of the file manager, which delete moves to the host Trash (ROADMAP 7.2, v24). */
    private fun hostDeletable(host: ViewerPageHost?): Boolean =
        launchRequest.hostSession != null && host?.page?.hostTargetId != null && !hostTrashBusy

    private fun deleteCurrent() {
        if (hostDeletable(currentHost())) deleteHostTarget() else operations.delete()
    }

    /**
     * Moves the current request target to the host Trash and drops its page; the snackbar's undo puts the file and
     * the page back. Deleting the last page closes the viewer (the host Trash page restores it then).
     */
    private fun deleteHostTarget() {
        val session = launchRequest.hostSession ?: return
        val current = source ?: return
        val index = currentIndex.takeIf { it in current.pages.indices } ?: return
        val page = current.pages[index]
        val targetId = page.hostTargetId ?: return
        hostTrashBusy = true
        syncCurrentPage(pageChanged = false)
        scope.launch {
            val batchId = withContext(Dispatchers.IO) { HostTargetTrash.move(session, targetId) }
            hostTrashBusy = false
            if (batchId == null) {
                operations.message(getString(R.string.host_trash_failed))
                syncCurrentPage(pageChanged = false)
                return@launch
            }
            hostTrashedTargetIds += targetId
            val remaining = current.pages.filterIndexed { i, _ -> i != index }
            if (remaining.isEmpty()) {
                Toast.makeText(this@ImageViewerActivity, R.string.host_trash_moved_last, Toast.LENGTH_LONG).show()
                finish()
                return@launch
            }
            installHostPages(current, remaining, index.coerceAtMost(remaining.lastIndex))
            operations.undoable(
                getString(R.string.host_trash_moved),
                undo = { withContext(Dispatchers.IO) { HostTargetTrash.undo(session, batchId) } },
                afterUndo = {
                    hostTrashedTargetIds -= targetId
                    val now = source ?: return@undoable
                    val back = now.pages.toMutableList().apply { add(index.coerceAtMost(size), page) }
                    installHostPages(now, back, index.coerceAtMost(back.lastIndex))
                },
            )
        }
    }

    private fun installHostPages(from: ViewerSource, pages: List<ImageViewerPage>, startIndex: Int) {
        registerHostRoutes(pages)
        currentIndex = -1
        restoredIndex = -1
        installSource(
            ViewerSource(
                kind = from.kind,
                pages = pages,
                startIndex = startIndex,
                hostSession = from.hostSession,
                hostTargetId = from.hostTargetId,
                editable = from.editable,
            ),
        )
    }

    /** Management controls of the current page: gallery sources only (ROADMAP 7.0). */
    private fun managementState(): ViewerManagement? {
        val spec = launchRequest.source ?: return null
        // A hidden folder is browse and share only.
        if (spec is ViewerSourceSpec.HiddenFolder) return null
        val item = source?.items?.getOrNull(currentIndex) ?: return null
        val library = spec is ViewerSourceSpec.MediaAlbum || spec is ViewerSourceSpec.Search
        return ViewerManagement(favorite = if (library) item.id in favoriteIds else null, canMoveCopy = library)
    }

    private fun createHost(index: Int): ViewerPageHost {
        val source = requireNotNull(source)
        val page = source.pages[index]
        val view = ZoomableImageView(this).apply {
            id = R.id.image
            contentDescription = getString(R.string.image_content_description)
            setOnClickListener {
                when {
                    // A tap holds a running slideshow and brings the bars back with Resume and Stop.
                    uiState.slideshowPlaying && !uiState.slideshowPaused -> pauseSlideshow()
                    uiState.detailsExpanded -> setExifDetailsExpanded(false)
                    else -> setControlsVisible(!uiState.controlsVisible)
                }
            }
            onPageSwipe = ::flipPage
            onSwipeDown = { if (index == currentIndex) finish() }
            onBrightnessDrag = { distanceY, height, finished -> if (index == currentIndex) dragBrightness(distanceY, height, finished) }
            applyGestures(this)
        }
        val host = ViewerPageHost(index, page, view) { playback ->
            if (index == currentIndex) uiState = uiState.copy(animation = playback)
        }
        view.onZoomInteraction = { zoom, gestureInProgress ->
            if (index == currentIndex) showZoomIndicator(zoom, gestureInProgress)
        }
        view.onRotationChanged = { _ ->
            if (index == currentIndex) {
                rotationHintDismissed = false
                syncRotationHint()
            }
        }
        return host
    }

    private fun ImageMetadata.pixelSize(): ImagePixelSize? =
        if (width != null && height != null && width > 0 && height > 0) ImagePixelSize(width, height) else null

    /** The display settings that live in the overlay state (ROADMAP 9.2), then the window's. */
    private fun applyDisplaySettings() {
        uiState = uiState.copy(blackBackground = viewerSettings.blackBackground, thumbnailStrip = viewerSettings.thumbnailStrip)
        applyWindowState()
        applyOrientation()
    }

    /** Screen-on, system bars and brightness from the settings, the overlay and a running slideshow. */
    private fun applyWindowState() {
        val running = uiState.slideshowPlaying && !uiState.slideshowPaused
        if (ViewerDisplayPolicy.keepScreenOn(viewerSettings.keepScreenOn, running)) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        if (ViewerDisplayPolicy.systemBarsVisible(viewerSettings.hideSystemBars, uiState.controlsVisible)) {
            controller.show(WindowInsetsCompat.Type.systemBars())
        } else {
            controller.hide(WindowInsetsCompat.Type.systemBars())
        }
        val brightness = ViewerDisplayPolicy.windowBrightness(viewerSettings.maxBrightness, draggedBrightness)
        if (window.attributes.screenBrightness != brightness) window.attributes = window.attributes.apply { screenBrightness = brightness }
    }

    /** "Follow the image" turns the screen to the current page's shape once its size is known. */
    private fun applyOrientation() {
        val metadata = currentHost()?.metadata
        val requested = ViewerDisplayPolicy.requestedOrientation(viewerSettings.orientation, metadata?.width, metadata?.height) ?: return
        if (requestedOrientation != requested) requestedOrientation = requested
    }

    private fun applyGestures(view: ZoomableImageView) {
        view.rotationGestureEnabled = viewerSettings.rotationGesture
        view.oneFingerZoom = viewerSettings.oneFingerZoom
        view.edgeTapPaging = viewerSettings.edgeTapPaging
        view.swipeDownToClose = viewerSettings.swipeDownToClose
        view.brightnessSwipe = viewerSettings.brightnessSwipe
        view.doubleTapZoom = viewerSettings.doubleTapZoom
        view.maxZoom = viewerSettings.maxZoom
    }

    /** Up brightens, down dims, for this window only; the badge shows the level while dragging. */
    private fun dragBrightness(distanceY: Float, height: Int, finished: Boolean) {
        val start = brightnessDragStart ?: currentBrightness().also { brightnessDragStart = it }
        val level = ViewerGesturePolicy.brightness(start, distanceY, height)
        draggedBrightness = level
        applyWindowState()
        if (finished) brightnessDragStart = null
        showTransientIndicator(getString(R.string.viewer_brightness_indicator, (level * 100).roundToInt()), gestureInProgress = !finished)
    }

    /** The window's own level, else the system's (0-255 on most devices) as the start of a drag. */
    private fun currentBrightness(): Float {
        window.attributes.screenBrightness.takeIf { it >= 0f }?.let { return it }
        val system = runCatching { Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS) }.getOrNull()
        return ((system?.toFloat() ?: DEFAULT_SYSTEM_BRIGHTNESS) / MAX_SYSTEM_BRIGHTNESS).coerceIn(ViewerGesturePolicy.MIN_BRIGHTNESS, 1f)
    }

    private fun releasePage(map: HashMap<Int, ViewerPageHost>, index: Int) {
        if (map === hosts && index == currentIndex) return // the current page is never released while shown
        map.remove(index)?.release(this)
    }

    /** The pager crossed into [index]; intermediate pages of a programmatic jump are skipped. */
    private fun onPagerPageChanged(index: Int) {
        if (jumpTarget >= 0 && index != jumpTarget) return
        if (index == jumpTarget) jumpTarget = -1
        onPageChanged(index)
        // Swiped by hand during a show: it goes on from that page after a full interval.
        if (!slideshowNavigating && uiState.slideshowPlaying && !uiState.slideshowPaused && slideshowPlan?.current != index) {
            slideshowPlan?.moveTo(index)
            scheduleSlide()
        }
    }

    private fun onPageChanged(index: Int) {
        val source = source ?: return
        if (index !in source.pages.indices || index == currentIndex) return
        val previous = currentHost()
        currentIndex = index
        previous?.let(::parkPage)
        hideZoomIndicator()
        rotationHintDismissed = false
        val host = acquirePage(index)
        when {
            host.tiledDeferred -> startLoad(host)
            host.ready -> host.view.drawable?.let { drawable ->
                host.animation.attach(drawable, startPaused = consumeRestoredAnimationPause(index))
            }
        }
        publishResult()
        syncCurrentPage(pageChanged = true)
    }

    /** A page that stopped being current: stop its animation, reset its zoom and give a tiled decode back. */
    private fun parkPage(host: ViewerPageHost) {
        host.animation.clear()
        (host.view.drawable as? Animatable)?.stop()
        host.view.resetZoom()
        if (host.tiledImage != null) {
            host.releaseTiledImage()
            host.tiledDeferred = true
            host.loading = true
            host.ready = false
        }
    }

    private fun flipPage(direction: Int) {
        jumpToPage(currentIndex + direction)
    }

    private fun jumpToPage(index: Int, animate: Boolean = true): Job? {
        val source = source ?: return null
        val state = pagerState ?: return null
        val target = index.coerceIn(0, source.pages.size - 1)
        if (target == currentIndex) return null
        pageJumpJob?.cancel()
        jumpTarget = target
        onPageChanged(target)
        return frameScope.launch {
            try {
                if (animate) state.animateScrollToPage(target) else state.scrollToPage(target)
            } finally {
                if (jumpTarget == target) jumpTarget = -1
            }
        }.also { pageJumpJob = it }
    }

    // region Slideshow (ROADMAP 9.1)

    private fun openSlideshowDialog() {
        if ((source?.pages?.size ?: 0) < 2) return
        slideshowDialog = SlideshowDialogState(SlideshowSettingsStore(this).load())
    }

    private fun startSlideshow(settings: SlideshowSettings) {
        slideshowDialog = null
        SlideshowSettingsStore(this).save(settings)
        val current = source ?: return
        val pages = current.pages
        val plan = SlideshowPlan(
            pageCount = pages.size,
            start = currentIndex.coerceIn(0, pages.lastIndex),
            eligible = { index -> settings.includeGifs || pages[index].image.mimeType != "image/gif" },
            settings = settings,
        )
        if (!plan.hasNext) {
            operations.message(getString(R.string.slideshow_nothing))
            return
        }
        slideshowPlan = plan
        slideshowSettings = settings
        resumeSlideshow()
    }

    private fun resumeSlideshow() {
        if (slideshowPlan == null) return
        slideshowBack.isEnabled = true
        uiState = uiState.copy(controlsVisible = false, detailsExpanded = false, slideshowPlaying = true, slideshowPaused = false)
        applyWindowState()
        scheduleSlide()
    }

    private fun scheduleSlide() {
        slideshowJob?.cancel()
        val settings = slideshowSettings ?: return
        slideshowJob = frameScope.launch {
            delay(settings.intervalSeconds * 1_000L)
            val next = slideshowPlan?.next()
            if (next == null) {
                stopSlideshow()
                operations.message(getString(R.string.slideshow_ended))
                return@launch
            }
            showSlide(next, settings.transition)
            scheduleSlide()
        }
    }

    private suspend fun showSlide(target: Int, transition: SlideshowTransition) {
        slideshowNavigating = true
        try {
            when (transition) {
                SlideshowTransition.NONE -> jumpToPage(target, animate = false)?.join()
                SlideshowTransition.SLIDE -> jumpToPage(target)?.join()
                SlideshowTransition.FADE -> {
                    slideshowVeil.animateTo(1f, tween(SLIDESHOW_FADE_MILLIS))
                    jumpToPage(target, animate = false)?.join()
                    slideshowVeil.animateTo(0f, tween(SLIDESHOW_FADE_MILLIS))
                }
            }
        } finally {
            slideshowNavigating = false
        }
    }

    private fun pauseSlideshow() {
        if (!uiState.slideshowPlaying) return
        slideshowJob?.cancel()
        frameScope.launch { slideshowVeil.snapTo(0f) }
        uiState = uiState.copy(controlsVisible = true, slideshowPaused = true)
        applyWindowState()
    }

    private fun stopSlideshow() {
        slideshowJob?.cancel()
        slideshowJob = null
        slideshowPlan = null
        slideshowSettings = null
        slideshowBack.isEnabled = false
        frameScope.launch { slideshowVeil.snapTo(0f) }
        uiState = uiState.copy(controlsVisible = true, slideshowPlaying = false, slideshowPaused = false)
        applyWindowState()
    }

    internal val slideshowRunningForTesting: Boolean
        get() = slideshowJob?.isActive == true

    // endregion

    private fun publishResult() {
        val source = source ?: return
        val itemId = source.itemId(currentIndex) ?: return
        setResult(
            RESULT_OK,
            Intent()
                .putExtra(ImageViewerContract.EXTRA_RESULT_ITEM_ID, itemId)
                .putExtra(ImageViewerContract.EXTRA_RESULT_INDEX, currentIndex),
        )
    }

    private fun thumbnailModel(index: Int): Any? {
        val page = source?.pages?.getOrNull(index) ?: return null
        // A page this platform cannot decode gets no thumbnail request either (host pages would open the file for nothing).
        if (ImagePlatformDecodeSupport.unsupported(page.image) != null) return null
        if (page.hostRelativePath.isNullOrEmpty()) return page.image.targetUri
        val token = hostImageSessionToken ?: return null
        return runCatching { HostSessionImageRegistry.imageUri(token, index) }.getOrNull()
    }

    /** Mirrors the current page host into the overlay state; [pageChanged] resets the details sheet. */
    private fun syncCurrentPage(pageChanged: Boolean) {
        val source = source ?: return
        val index = currentIndex.takeIf { it in source.pages.indices } ?: return
        val page = source.pages[index]
        val host = hosts[index]
        val metadata = host?.metadata
        val metadataText = when {
            host == null || !host.metadataRead -> getString(R.string.metadata_loading)
            metadata == null -> getString(R.string.metadata_unavailable)
            else -> formatMetadata(page.image, metadata)
        }
        val detailsAvailable = metadata != null
        val restoreExpanded = detailsAvailable && restoredExpandedDetailsIndex == index
        if (restoreExpanded) restoredExpandedDetailsIndex = -1
        val expanded = when {
            restoreExpanded -> true
            pageChanged -> false
            else -> uiState.detailsExpanded && detailsAvailable
        }
        val canForward = page.hostRelativePath.isNullOrEmpty()
        uiState = uiState.copy(
            title = page.image.displayName,
            subtitle = source.pages.size.takeIf { it > 1 }?.let { pageCount ->
                getString(R.string.page_counter, index + 1, pageCount)
            },
            pageIndex = index,
            pageCount = source.pages.size,
            slideshowAvailable = source.pages.size > 1,
            metadataText = metadataText,
            extendedDetails = if (extendedDetails && metadata != null) {
                ExtendedDetailsPolicy.lines(extendedDetailFields, extendedDetailValues(index, page, metadata), showLocation)
            } else {
                emptyList()
            },
            detailsSummary = metadataText,
            exifText = metadata?.exif?.let(::formatExifDetails) ?: getString(R.string.exif_unavailable),
            mapUri = metadata?.exif?.location?.takeIf { showLocation }?.geoUri(),
            detailsAvailable = detailsAvailable,
            detailsExpanded = expanded,
            loading = host?.loading ?: true,
            errorText = host?.errorText,
            printEnabled = host?.ready == true,
            canForward = canForward,
            canEdit = canForward && source.editable,
            animation = host?.animation?.playback ?: ImageAnimationPlayback.UNAVAILABLE,
            rotationHint = rotationHintText(host),
            rotationSaveAvailable = rotationHintText(host) != null && rotationSavable(host),
            rotationSaveBusy = rotationSaveBusy,
            management = managementState(),
            hostDelete = hostDeletable(host),
        )
        applyOrientation()
    }

    /** The current page's user rotation, as the hint bar text; null when unrotated or dismissed (ROADMAP D8). */
    private fun rotationHintText(host: ViewerPageHost?): String? {
        val turns = host?.view?.currentRotationQuarterTurns ?: ImageRotationState.ORIGINAL_QUARTER_TURNS
        if (turns == ImageRotationState.ORIGINAL_QUARTER_TURNS || rotationHintDismissed) return null
        return getString(R.string.viewer_rotation_hint, ImageRotationState.degrees(turns))
    }

    /**
     * Whether the current page's rotation can be saved (ROADMAP 8.2, T15): the setting is on, the source lets the
     * image be edited (gallery or a shared file; never the file manager's read-only view), the format can carry it
     * and the app can write the file. The write check runs once per image, in the background.
     */
    private fun rotationSavable(host: ViewerPageHost?): Boolean {
        val page = host?.page?.image ?: return false
        if (!viewerSettings.rotationSave || !launchRequest.editable || RotationSave.kind(page.mimeType) == null) return false
        val key = page.targetUri.toString()
        rotationWritable[key]?.let { return it }
        if (rotationChecks.add(key)) {
            scope.launch {
                val writable = withContext(Dispatchers.IO) {
                    runCatching {
                        StandaloneOverwrite.isOverwritable(this@ImageViewerActivity, StandaloneDocument(page.targetUri, page.displayName, page.mimeType, page.declaredSize))
                    }.getOrDefault(false)
                }
                rotationWritable[key] = writable
                syncRotationHint()
            }
        }
        return false
    }

    /** Writes the current page's rotation into the image through the overwrite path (backup and undo included). */
    private fun saveRotation() {
        val host = currentHost() ?: return
        val page = host.page.image
        val turns = host.view.currentRotationQuarterTurns
        val kind = RotationSave.kind(page.mimeType) ?: return
        if (turns == ImageRotationState.ORIGINAL_QUARTER_TURNS || rotationSaveBusy) return
        rotationSaveBusy = true
        syncRotationHint()
        scope.launch {
            try {
                if (kind == RotationSave.Kind.LOSSY_REENCODE && !operations.confirm(
                        getString(R.string.viewer_rotation_reencode_title),
                        getString(R.string.viewer_rotation_reencode_message),
                        getString(R.string.text_save),
                    )
                ) {
                    return@launch
                }
                val output = java.io.File(cacheDir, "rotation-save/${java.util.UUID.randomUUID()}.bin")
                try {
                    val rendered = withContext(Dispatchers.IO) {
                        output.parentFile?.mkdirs()
                        runCatching { RotationSave.render(contentResolver, page.targetUri, page.mimeType, turns, output, cacheDir) }
                    }
                    rendered.exceptionOrNull()?.let { error ->
                        operations.message(getString(if (error is RotationSave.TooLargeException) R.string.viewer_rotation_too_large else R.string.overwrite_failed))
                        return@launch
                    }
                    operations.overwriteOriginal(page.targetUri, output, getString(R.string.viewer_rotation_saved))
                } finally {
                    withContext(Dispatchers.IO) { output.delete() }
                }
            } finally {
                rotationSaveBusy = false
                syncRotationHint()
            }
        }
    }

    private fun syncRotationHint() {
        val host = currentHost()
        uiState = uiState.copy(
            rotationHint = rotationHintText(host),
            rotationSaveAvailable = rotationHintText(host) != null && rotationSavable(host),
            rotationSaveBusy = rotationSaveBusy,
        )
    }

    private fun dismissRotationHint() {
        rotationHintDismissed = true
        syncRotationHint()
    }

    private fun syncIfCurrent(host: ViewerPageHost) {
        if (host.index == currentIndex) syncCurrentPage(pageChanged = false)
    }

    // endregion

    // region Loading

    private fun isLive(host: ViewerPageHost, generation: Int): Boolean =
        host.generation == generation && hosts[host.index] === host && !isFinishing && !isDestroyed

    private fun startLoad(host: ViewerPageHost) {
        host.job?.cancel()
        host.generation += 1
        val generation = host.generation
        host.loading = true
        host.errorText = null
        host.ready = false
        host.tiledDeferred = false
        val page = host.page

        ImagePlatformDecodeSupport.unsupported(page.image)?.let { unavailable ->
            host.metadata = null
            host.metadataRead = true
            val message = when (unavailable.reason) {
                ModernImageUnavailableReason.ANDROID_VERSION -> getString(
                    R.string.error_image_format_requires_android,
                    unavailable.format.displayLabel,
                    unavailable.format.minimumAndroidVersion,
                )
                ModernImageUnavailableReason.PLATFORM_DECODER -> getString(
                    R.string.error_image_decoder_unavailable,
                    unavailable.format.displayLabel,
                )
            }
            failHost(host, message)
            return
        }

        val limits = host.view.largeImageLimits(memoryClassMebibytes)
        if (page.hostRelativePath.isNullOrEmpty()) {
            loadDirectPage(host, generation, limits)
        } else {
            loadHostPage(host, generation, limits)
        }
    }

    private fun loadDirectPage(host: ViewerPageHost, generation: Int, limits: ImageBitmapLimits) {
        val page = host.page
        host.job = scope.launch {
            val metadata = withContext(Dispatchers.IO) {
                ImageContentValidator.readDisplayMetadata(contentResolver, page.image)
            }
            if (!isLive(host, generation)) return@launch
            host.metadata = metadata
            host.metadataRead = true
            host.view.sourcePixelSize = metadata?.pixelSize()
            syncIfCurrent(host)
            if (metadata != null && shouldTile(page, metadata, limits)) {
                installTiledOrDefer(host, generation, metadata, limits)
            } else {
                startGlideLoad(host, page.image.targetUri, generation)
            }
        }
    }

    private fun loadHostPage(host: ViewerPageHost, generation: Int, limits: ImageBitmapLimits) {
        val page = host.page
        host.job = scope.launch {
            val metadata = withContext(Dispatchers.IO) {
                openHostDescriptor(page)?.use { descriptor ->
                    ImageContentValidator.readHostMetadata(descriptor, page.image)
                }
            }
            if (!isLive(host, generation)) return@launch
            host.metadata = metadata
            host.metadataRead = true
            host.view.sourcePixelSize = metadata?.pixelSize()
            syncIfCurrent(host)
            when {
                metadata == null -> failHost(host)
                shouldTile(page, metadata, limits) -> installTiledOrDefer(host, generation, metadata, limits)
                else -> {
                    val imageUri = hostGlideModel(host)
                    if (imageUri == null) failHost(host) else startGlideLoad(host, imageUri, generation)
                }
            }
        }
    }

    /**
     * Tiled pages decode only while current: a neighbour keeps its spinner until the user lands on it, so at most
     * one tile cache budget is live (ROADMAP 6.3 acceptance: 20 MP PNG paging without a full-image allocation).
     */
    private suspend fun installTiledOrDefer(
        host: ViewerPageHost,
        generation: Int,
        metadata: ImageMetadata,
        limits: ImageBitmapLimits,
    ) {
        if (host.index != currentIndex) {
            host.tiledDeferred = true
            host.loading = true
            return
        }
        val drawable = withContext(Dispatchers.IO) { createTiledImage(host.page, metadata, limits) }
        if (!isLive(host, generation)) {
            drawable?.close()
            return
        }
        if (drawable == null) {
            val model = if (host.page.hostRelativePath.isNullOrEmpty()) host.page.image.targetUri else hostGlideModel(host)
            if (model == null) failHost(host) else startGlideLoad(host, model, generation)
            return
        }
        installTiledImage(host, drawable, generation)
    }

    private fun hostGlideModel(host: ViewerPageHost): Any? = runCatching {
        HostSessionImageRegistry.imageUri(requireNotNull(hostImageSessionToken), host.index)
    }.getOrNull()

    private fun shouldTile(page: ImageViewerPage, metadata: ImageMetadata, limits: ImageBitmapLimits): Boolean {
        val width = metadata.width ?: return false
        val height = metadata.height ?: return false
        return ImageLargeImagePolicy.shouldUseTiling(
            width = width,
            height = height,
            mimeType = page.image.mimeType,
            sdkInt = Build.VERSION.SDK_INT,
            limits = limits,
        )
    }

    private fun createTiledImage(
        page: ImageViewerPage,
        metadata: ImageMetadata,
        limits: ImageBitmapLimits,
    ): TiledImageDrawable? {
        val descriptor = if (page.hostRelativePath.isNullOrEmpty()) {
            runCatching { contentResolver.openFileDescriptor(page.image.targetUri, "r") }.getOrNull()
        } else {
            openHostDescriptor(page)
        } ?: return null
        return TiledImageDrawable.create(
            descriptor = descriptor,
            orientation = metadata.exif?.orientation,
            cacheBudgetBytes = limits.tileCacheBudgetBytes,
        )
    }

    private fun installTiledImage(host: ViewerPageHost, drawable: TiledImageDrawable, generation: Int) {
        host.tiledImage = drawable
        host.view.setImageDrawable(drawable)
        host.loading = false
        host.errorText = null
        host.ready = true
        syncIfCurrent(host)
        host.view.post {
            if (isLive(host, generation)) {
                host.view.restoreRotationQuarterTurns(consumeRestoredRotation(host.index))
                syncIfCurrent(host)
            }
        }
    }

    private fun openHostDescriptor(page: ImageViewerPage): ParcelFileDescriptor? = runCatching {
        val session = requireNotNull(launchRequest.hostSession)
        val targetId = requireNotNull(launchRequest.hostTargetId)
        val relativePath = requireNotNull(page.hostRelativePath).takeIf { it.isNotEmpty() }
            ?: return null
        session.openFile(targetId, relativePath)?.let { descriptor ->
            descriptor.takeIf { it.fileDescriptor.valid() }.also {
                if (it == null) runCatching { descriptor.close() }
            }
        }
    }.getOrNull()

    private fun startGlideLoad(host: ViewerPageHost, model: Any, generation: Int) {
        if (!isLive(host, generation)) return
        host.loading = true
        host.errorText = null
        syncIfCurrent(host)
        // Glide 5's static-image Downsampler applies all eight EXIF rotation/mirror variants.
        // ZoomableImageView therefore tracks only the user's additional quarter turns.
        // The target is the window, stated explicitly: a page the pager has premeasured but not laid out would
        // otherwise make Glide fall back to the screen's longer edge on both axes, and the decode is additionally
        // capped at the full-decode budget for formats that cannot be tiled.
        val metrics = resources.displayMetrics
        val budgetPixels = host.view.largeImageLimits(memoryClassMebibytes).fullDecodeBudgetBytes / BYTES_PER_PIXEL
        Glide.with(this)
            .load(model)
            .let { request ->
                // "Highest quality" decodes the page at its own size; the budget below still caps it.
                if (viewerSettings.highestQuality) {
                    request.override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                } else {
                    request.override(metrics.widthPixels.coerceAtLeast(1), metrics.heightPixels.coerceAtLeast(1))
                }
            }
            .downsample(BudgetedDownsampleStrategy(budgetPixels))
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    error: GlideException?,
                    model: Any?,
                    target: Target<Drawable>,
                    isFirstResource: Boolean,
                ): Boolean {
                    if (isLive(host, generation)) failHost(host)
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable,
                    model: Any,
                    target: Target<Drawable>?,
                    dataSource: DataSource,
                    isFirstResource: Boolean,
                ): Boolean {
                    if (!isLive(host, generation)) return false
                    host.loading = false
                    host.errorText = null
                    syncIfCurrent(host)
                    // The target installs the drawable (and starts an animatable) after this callback returns.
                    host.view.post {
                        if (!isLive(host, generation)) return@post
                        host.ready = true
                        host.view.restoreRotationQuarterTurns(consumeRestoredRotation(host.index))
                        if (host.index == currentIndex) {
                            host.animation.attach(resource, startPaused = consumeRestoredAnimationPause(host.index))
                            syncCurrentPage(pageChanged = false)
                        } else {
                            (resource as? Animatable)?.stop()
                        }
                    }
                    return false
                }
            })
            .into(host.view)
    }

    private fun failHost(
        host: ViewerPageHost,
        message: CharSequence = getText(R.string.error_cannot_read_image),
    ) {
        host.animation.clear()
        host.loading = false
        host.errorText = message.toString()
        host.ready = false
        if (host.index == currentIndex) {
            hideZoomIndicator()
            syncCurrentPage(pageChanged = false)
        }
    }

    private fun consumeRestoredAnimationPause(pageIndex: Int): Boolean {
        if (restoredAnimationIndex != pageIndex) return false
        restoredAnimationIndex = -1
        return restoredAnimationPaused.also { restoredAnimationPaused = false }
    }

    private fun consumeRestoredRotation(pageIndex: Int): Int {
        if (restoredRotationIndex != pageIndex) {
            return ImageRotationState.ORIGINAL_QUARTER_TURNS
        }
        restoredRotationIndex = -1
        return restoredRotationQuarterTurns.also {
            restoredRotationQuarterTurns = ImageRotationState.ORIGINAL_QUARTER_TURNS
        }
    }

    // endregion

    // region Metadata text

    private fun formatMetadata(request: ImageViewerRequest, metadata: ImageMetadata): String {
        val size = Formatter.formatFileSize(this, metadata.byteSize)
        val resolution = if (metadata.width != null && metadata.height != null) {
            getString(R.string.metadata_resolution, metadata.width, metadata.height)
        } else {
            getString(R.string.metadata_resolution_unknown)
        }
        val decodedColor = formatDecodedColor(metadata.decodedColor)
        return if (decodedColor == null) {
            getString(R.string.metadata_summary, request.mimeType, size, resolution)
        } else {
            getString(R.string.metadata_summary_with_color, request.mimeType, size, resolution, decodedColor)
        }
    }

    private fun extendedDetailValues(index: Int, page: ImageViewerPage, metadata: ImageMetadata): ExtendedDetailValues {
        val item = source?.items?.getOrNull(index)
        val exif = metadata.exif
        val locale = Locale.getDefault()
        val taken = ImageExifValueFormatter.capturedAt(exif?.capturedAt) ?: item?.dateTakenMillis?.let(::formatDetailTime)
        return ExtendedDetailValues(
            // Media library pages know their folder, host pages their place in the directory; others only the name.
            path = item?.let { it.relativePath + it.displayName }
                ?: page.hostRelativePath?.takeIf { it.isNotEmpty() }
                ?: page.image.displayName,
            resolution = if (metadata.width != null && metadata.height != null) {
                getString(R.string.metadata_resolution, metadata.width, metadata.height)
            } else {
                null
            },
            size = Formatter.formatFileSize(this, metadata.byteSize),
            taken = taken?.let { getString(R.string.extended_taken, it) },
            modified = item?.dateModifiedSeconds?.takeIf { it > 0 }
                ?.let { formatDetailTime(it * 1000L) }
                ?.let { getString(R.string.extended_modified, it) },
            camera = exif?.let { ImageExifValueFormatter.device(it.make, it.model) },
            exposure = exif?.let { ImageExifValueFormatter.shutterAndAperture(it, locale) },
            focalLength = exif?.let { ImageExifValueFormatter.focalLength(it, locale) },
            iso = exif?.let(ImageExifValueFormatter::iso),
            location = exif?.location?.decimal(),
        )
    }

    /** The format of the EXIF capture time, so both times of a page read alike. */
    private fun formatDetailTime(millis: Long): String =
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ROOT).format(Date(millis))

    private fun formatDecodedColor(metadata: ImageDecodedColorMetadata?): String? = metadata?.let {
        buildList {
            it.bitsPerPixel?.let { bitsPerPixel ->
                add(getString(R.string.metadata_decoded_bit_depth, bitsPerPixel))
            }
            it.colorSpaceName?.let { colorSpaceName ->
                add(getString(R.string.metadata_color_space, colorSpaceName))
            }
        }.takeIf { fields -> fields.isNotEmpty() }?.joinToString(" · ")
    }

    /** Opens the position of the current image in a map app (ROADMAP 9.1, T14). */
    private fun openMap() {
        val uri = uiState.mapUri ?: return
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(uri)))
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(this, R.string.error_no_map_app, Toast.LENGTH_SHORT).show()
        }
    }

    private fun formatExifDetails(exif: ImageExifMetadata): String? {
        val lines = buildList {
            ImageExifValueFormatter.capturedAt(exif.capturedAt)?.let { capturedAt ->
                add(getString(R.string.exif_capture_time, capturedAt))
            }
            ImageExifValueFormatter.device(exif.make, exif.model)?.let { device ->
                add(getString(R.string.exif_device, device))
            }
            ImageExifValueFormatter.exposure(exif, Locale.getDefault())?.let { exposure ->
                add(getString(R.string.exif_exposure, exposure))
            }
            exif.orientation?.let { orientation ->
                add(getString(R.string.exif_orientation, formatExifOrientation(orientation)))
            }
            val location = exif.location?.takeIf { showLocation }
            if (location != null) {
                add(getString(R.string.exif_location, "${location.decimal()} (${location.degreesMinutesSeconds()})"))
            } else if (exif.hasGpsMetadata) {
                add(getString(R.string.exif_gps_present))
            }
        }
        return lines.takeIf { it.isNotEmpty() }?.joinToString("\n")
    }

    private fun formatExifOrientation(orientation: ImageExifOrientation): String = when {
        orientation.mirrored && orientation.rotationDegrees == 0 ->
            getString(R.string.exif_orientation_mirrored)
        orientation.mirrored -> getString(
            R.string.exif_orientation_mirrored_clockwise,
            orientation.rotationDegrees,
        )
        orientation.rotationDegrees == 0 -> getString(R.string.exif_orientation_normal)
        else -> getString(R.string.exif_orientation_clockwise, orientation.rotationDegrees)
    }

    // endregion

    // region Overlay state

    private fun showZoomIndicator(zoom: Float, gestureInProgress: Boolean) {
        val maximum = currentHost()?.view?.maximumZoom ?: ImageZoomState.MAX_ZOOM
        val formattedZoom = ImageZoomIndicatorFormatter.format(zoom, resources.configuration.locales[0], maximum)
            ?: return hideZoomIndicator()
        showTransientIndicator(getString(R.string.zoom_indicator, formattedZoom), gestureInProgress)
    }

    /** The zoom badge, also used for the brightness level; it fades a moment after the gesture ends. */
    private fun showTransientIndicator(text: String, gestureInProgress: Boolean) {
        zoomIndicatorJob?.cancel()
        zoomIndicatorJob = null
        uiState = uiState.copy(zoomIndicatorText = text)
        if (!gestureInProgress) {
            zoomIndicatorJob = scope.launch {
                delay(ZOOM_INDICATOR_DURATION_MILLIS)
                uiState = uiState.copy(zoomIndicatorText = null)
            }
        }
    }

    private fun hideZoomIndicator() {
        zoomIndicatorJob?.cancel()
        zoomIndicatorJob = null
        if (uiState.zoomIndicatorText != null) uiState = uiState.copy(zoomIndicatorText = null)
    }

    private fun setExifDetailsExpanded(expanded: Boolean) {
        uiState = uiState.copy(detailsExpanded = expanded && uiState.detailsAvailable)
    }

    private fun setControlsVisible(visible: Boolean) {
        uiState = uiState.copy(controlsVisible = visible)
        applyWindowState()
    }

    // endregion

    // region Actions

    private fun editImage() {
        val source = source ?: return
        if (editBusy || !source.editable) return
        val page = currentShareablePage() ?: return
        scope.launch { editHandoff.edit(page.targetUri) }
    }

    private fun shareImage() {
        val page = currentShareablePage() ?: return
        val target = Intent(Intent.ACTION_SEND).apply {
            type = page.mimeType
            clipData = ClipData.newRawUri(page.displayName, page.targetUri)
            putExtra(Intent.EXTRA_STREAM, page.targetUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startSafely(Intent.createChooser(target, getString(R.string.action_share)))
    }

    private fun printImage() {
        val source = source ?: return
        val page = source.pages.getOrNull(currentIndex) ?: return
        val view = currentHost()?.view ?: return showPrintPreparationFailure()
        val drawable = view.drawable ?: return showPrintPreparationFailure()
        val snapshot = ImagePrintSnapshot.capture(
            drawable = drawable,
            rotationQuarterTurns = view.currentRotationQuarterTurns,
        ) ?: return showPrintPreparationFailure()
        val printManager = getSystemService(PRINT_SERVICE) as? PrintManager
        if (printManager == null) {
            snapshot.close()
            showPrintFailure()
            return
        }

        val adapter = ImagePrintDocumentAdapter(
            context = applicationContext,
            documentName = ImagePrintNames.pdfDocumentName(page.image.displayName),
            snapshot = snapshot,
            failureMessage = getString(R.string.error_print_failed),
        )
        val initialAttributes = PrintAttributes.Builder()
            .setColorMode(PrintAttributes.COLOR_MODE_COLOR)
            .setMediaSize(
                if (snapshot.outputWidth > snapshot.outputHeight) {
                    PrintAttributes.MediaSize.UNKNOWN_LANDSCAPE
                } else {
                    PrintAttributes.MediaSize.UNKNOWN_PORTRAIT
                },
            )
            .build()
        runCatching {
            printManager.print(page.image.displayName, adapter, initialAttributes)
        }.onFailure {
            adapter.dispose()
            showPrintFailure()
        }
    }

    private fun showPrintPreparationFailure() {
        Toast.makeText(this, R.string.error_cannot_prepare_print, Toast.LENGTH_SHORT).show()
    }

    private fun showPrintFailure() {
        Toast.makeText(this, R.string.error_print_failed, Toast.LENGTH_SHORT).show()
    }

    private fun openWithAnotherApp() {
        val page = currentShareablePage() ?: return
        val target = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(page.targetUri, page.mimeType)
            clipData = ClipData.newRawUri(page.displayName, page.targetUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val ownEntry = ComponentName(this, ExternalViewActivity::class.java)
        if (!hasExternalHandler(target, ownEntry)) {
            Toast.makeText(this, R.string.error_no_external_viewer, Toast.LENGTH_SHORT).show()
            return
        }
        val chooser = Intent.createChooser(target, getString(R.string.action_open_external)).apply {
            putExtra(Intent.EXTRA_EXCLUDE_COMPONENTS, arrayOf(ownEntry))
        }
        startSafely(chooser)
    }

    private fun openShareCopyDialog() {
        val page = currentShareablePage() ?: return
        shareCopyDialog = ShareCopyDialogState(stillCopy = page.mimeType == "image/gif")
    }

    /** Makes the copy off the main thread, then hands it to the share sheet (ROADMAP 9.1). */
    private fun shareCopy(request: ShareCopyRequest) {
        val page = currentShareablePage() ?: return
        shareCopyDialog = null
        if (shareCopyJob?.isActive == true) return
        shareCopyJob = scope.launch {
            val copy = runCatching {
                withContext(Dispatchers.IO) { ShareCopies.create(applicationContext, page.targetUri, page.displayName, request) }
            }.getOrElse { error ->
                val resizeError = (error as? ShareCopyException)?.error
                operations.message(resizeError?.let(::resizeErrorText) ?: getString(R.string.share_copy_failed))
                return@launch
            }
            val mimeType = contentResolver.getType(copy) ?: "image/*"
            val target = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                clipData = ClipData.newRawUri(page.displayName, copy)
                putExtra(Intent.EXTRA_STREAM, copy)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startSafely(Intent.createChooser(target, getString(R.string.action_share_copy)))
        }
    }

    /** Hands the image to another app's editor (ROADMAP 9.1); this app is not offered. */
    private fun editWithAnotherApp() {
        val page = currentShareablePage() ?: return
        val target = Intent(Intent.ACTION_EDIT).apply {
            setDataAndType(page.targetUri, page.mimeType)
            clipData = ClipData.newRawUri(page.displayName, page.targetUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val ownEntry = ComponentName(this, ExternalViewActivity::class.java)
        if (!hasExternalHandler(target, ownEntry)) {
            Toast.makeText(this, R.string.error_no_editor_app, Toast.LENGTH_SHORT).show()
            return
        }
        val chooser = Intent.createChooser(target, getString(R.string.action_edit_with)).apply {
            putExtra(Intent.EXTRA_EXCLUDE_COMPONENTS, arrayOf(ownEntry))
        }
        startSafely(chooser)
    }

    /** Crops to the screen and sets the wallpaper (ROADMAP 9.1); where that is not allowed, another app is offered. */
    private fun setAsWallpaper() {
        val page = currentShareablePage() ?: return
        if (Wallpapers.available(this)) {
            startSafely(WallpaperActivity.intent(this, page))
            return
        }
        val chooser = Wallpapers.attachDataIntent(page.targetUri, page.mimeType, page.displayName, getString(R.string.wallpaper_set_with))
        runCatching { startActivity(chooser) }.onFailure {
            Toast.makeText(this, R.string.error_no_wallpaper_app, Toast.LENGTH_SHORT).show()
        }
    }

    private fun currentShareablePage(): ImageViewerRequest? {
        val page = source?.pages?.getOrNull(currentIndex) ?: return null
        return page.image.takeIf { page.hostRelativePath.isNullOrEmpty() }
    }

    private fun hasExternalHandler(intent: Intent, excluded: ComponentName): Boolean {
        val matches = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            packageManager.queryIntentActivities(intent, 0)
        }
        return matches.any { info ->
            info.activityInfo?.let { ComponentName(it.packageName, it.name) != excluded } == true
        }
    }

    private fun startSafely(intent: Intent) {
        runCatching { startActivity(intent) }.onFailure {
            Toast.makeText(this, R.string.error_no_external_viewer, Toast.LENGTH_SHORT).show()
        }
    }

    private fun closeHostSession() {
        if (hostSessionClosed || !::launchRequest.isInitialized) return
        hostSessionClosed = true
        if (!launchRequest.ownsHostSession) return
        launchRequest.hostSession?.let { session -> runCatching { session.close() } }
    }

    // endregion

    private companion object {
        const val DEFAULT_SYSTEM_BRIGHTNESS = 128f
        const val MAX_SYSTEM_BRIGHTNESS = 255f
        const val STATE_CURRENT_INDEX = "currentIndex"
        const val STATE_ANIMATION_INDEX = "animationIndex"
        const val STATE_ANIMATION_PAUSED = "animationPaused"
        const val STATE_ROTATION_INDEX = "rotationIndex"
        const val STATE_ROTATION_QUARTER_TURNS = "rotationQuarterTurns"
        const val STATE_EXPANDED_DETAILS_INDEX = "expandedDetailsIndex"
        const val STATE_HOST_TRASHED_TARGETS = "hostTrashedTargets"
        const val SLIDESHOW_FADE_MILLIS = 250
        const val STATE_CONTROLS_VISIBLE = "controlsVisible"
        const val ZOOM_INDICATOR_DURATION_MILLIS = 900L
        const val BYTES_PER_PIXEL = 4L
        const val LEGACY_NAVIGATION_SCRIM = 0x66000000
    }
}
