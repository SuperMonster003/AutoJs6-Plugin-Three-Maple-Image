package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.app.ActivityManager
import android.content.ClipData
import android.content.ComponentName
import android.content.ComponentCallbacks2
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.print.PrintAttributes
import android.print.PrintManager
import android.text.format.Formatter
import android.widget.Toast
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.VisibleForTesting
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.MapleTheme
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.ViewerCallbacks
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.ViewerScreen
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.ViewerUiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

/**
 * Read-only image viewer. The decoding pipeline (direct content URIs, host session siblings, tiled large images,
 * EXIF normalization through Glide) is unchanged; the overlay chrome is a Compose [ViewerScreen] driven by one
 * [ViewerUiState], while the zoomable surface stays the [ZoomableImageView] hosted through AndroidView.
 */
class ImageViewerActivity : ConfiguredActivity() {

    private lateinit var imageView: ZoomableImageView
    private lateinit var launchRequest: ImageViewerLaunchRequest
    private lateinit var animationController: ImageAnimationController
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var pageJob: Job? = null
    private var tiledImageDrawable: TiledImageDrawable? = null
    private var hostImageSessionToken: String? = null
    private var currentIndex = -1
    private var loadGeneration = 0
    private var hostSessionClosed = false
    private var restoredAnimationIndex = -1
    private var restoredAnimationPaused = false
    private var restoredRotationIndex = -1
    private var restoredRotationQuarterTurns = ImageRotationState.ORIGINAL_QUARTER_TURNS
    private var restoredExpandedDetailsIndex = -1
    private var uiState by mutableStateOf(ViewerUiState())
    private var editBusy = false
    private val editHandoff = StandaloneEditHandoff(
        activity = this,
        onBusy = { busy -> editBusy = busy },
        onMessage = { resource -> Toast.makeText(this, resource, Toast.LENGTH_SHORT).show() },
    )
    private val memoryClassMebibytes: Int by lazy {
        getSystemService(ActivityManager::class.java)?.memoryClass ?: 128
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        launchRequest = ImageViewerContract.resolve(intent) ?: run {
            ImageViewerContract.closeAttachedHostSession(intent)
            finish()
            return
        }
        hostImageSessionToken = launchRequest.hostSession?.let {
            runCatching { HostSessionImageRegistry.register(launchRequest) }.getOrNull()
        }
        if (launchRequest.hostSession != null && hostImageSessionToken == null) {
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
        imageView = ZoomableImageView(this).apply {
            id = R.id.image
            contentDescription = getString(R.string.image_content_description)
            setOnClickListener {
                if (uiState.detailsExpanded) setExifDetailsExpanded(false) else setControlsVisible(!uiState.controlsVisible)
            }
            onPageSwipe = { direction -> showPage(currentIndex + direction) }
            onZoomInteraction = ::showZoomIndicator
        }
        animationController = ImageAnimationController { playback ->
            uiState = uiState.copy(animation = playback)
        }
        val callbacks = ViewerCallbacks(
            onBack = ::finish,
            onToggleDetails = { setExifDetailsExpanded(!uiState.detailsExpanded) },
            onDismissDetails = { setExifDetailsExpanded(false) },
            onRotate = { imageView.rotateClockwise90() },
            onEdit = ::editImage,
            onShare = ::shareImage,
            onOpenWith = ::openWithAnotherApp,
            onResetZoom = { hideZoomIndicator(); imageView.resetZoom() },
            onPrint = ::printImage,
            onReleaseHistory = ::showReleaseHistory,
            onToggleAnimation = { animationController.toggle() },
        )
        setContent {
            MapleTheme(palette = appPalette) {
                ViewerScreen(state = uiState, imageView = imageView, callbacks = callbacks)
            }
        }

        val restoredIndex = savedInstanceState?.getInt(STATE_CURRENT_INDEX, -1)
            ?.takeIf { it in launchRequest.pages.indices }
        restoredAnimationIndex = savedInstanceState
            ?.getInt(STATE_ANIMATION_INDEX, -1)
            ?.takeIf { it in launchRequest.pages.indices }
            ?: -1
        restoredAnimationPaused = savedInstanceState
            ?.getBoolean(STATE_ANIMATION_PAUSED, false)
            ?: false
        restoredRotationIndex = savedInstanceState
            ?.getInt(STATE_ROTATION_INDEX, -1)
            ?.takeIf { it in launchRequest.pages.indices }
            ?: -1
        restoredRotationQuarterTurns = savedInstanceState
            ?.getInt(STATE_ROTATION_QUARTER_TURNS, ImageRotationState.ORIGINAL_QUARTER_TURNS)
            ?: ImageRotationState.ORIGINAL_QUARTER_TURNS
        restoredExpandedDetailsIndex = savedInstanceState
            ?.getInt(STATE_EXPANDED_DETAILS_INDEX, -1)
            ?.takeIf { it in launchRequest.pages.indices }
            ?: -1
        showPage(restoredIndex ?: launchRequest.startIndex)
    }

    override fun onStart() {
        super.onStart()
        if (::animationController.isInitialized) {
            imageView.post {
                if (!isFinishing && !isDestroyed) animationController.reapplyRequestedPlayback()
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        if (::launchRequest.isInitialized && currentIndex in launchRequest.pages.indices) {
            outState.putInt(STATE_CURRENT_INDEX, currentIndex)
        }
        if (
            ::animationController.isInitialized &&
            animationController.playback != ImageAnimationPlayback.UNAVAILABLE
        ) {
            outState.putInt(STATE_ANIMATION_INDEX, currentIndex)
            outState.putBoolean(
                STATE_ANIMATION_PAUSED,
                animationController.playback == ImageAnimationPlayback.PAUSED,
            )
        }
        if (
            ::imageView.isInitialized &&
            imageView.currentRotationQuarterTurns != ImageRotationState.ORIGINAL_QUARTER_TURNS
        ) {
            outState.putInt(STATE_ROTATION_INDEX, currentIndex)
            outState.putInt(STATE_ROTATION_QUARTER_TURNS, imageView.currentRotationQuarterTurns)
        }
        if (uiState.detailsExpanded && ::launchRequest.isInitialized && currentIndex in launchRequest.pages.indices) {
            outState.putInt(STATE_EXPANDED_DETAILS_INDEX, currentIndex)
        }
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        pageJob?.cancel()
        scope.cancel()
        if (::imageView.isInitialized) {
            animationController.clear()
            releaseTiledImage()
            Glide.with(applicationContext).clear(imageView)
            imageView.onPageSwipe = null
            imageView.onZoomInteraction = null
            imageView.removeCallbacks(hideZoomIndicatorRunnable)
        }
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
            tiledImageDrawable?.trimTileCache()
        }
    }

    override fun onLowMemory() {
        tiledImageDrawable?.trimTileCache()
        super.onLowMemory()
    }

    // region Test hooks

    @get:VisibleForTesting
    internal val viewerStateForTesting: ViewerUiState
        get() = uiState

    @VisibleForTesting
    internal fun toggleDetailsForTesting() = setExifDetailsExpanded(!uiState.detailsExpanded)

    @VisibleForTesting
    internal fun rotateForTesting(): Boolean = imageView.rotateClockwise90()

    @VisibleForTesting
    internal fun resetZoomForTesting() {
        hideZoomIndicator()
        imageView.resetZoom()
    }

    @VisibleForTesting
    internal fun toggleAnimationForTesting() = animationController.toggle()

    // endregion

    private fun setPrintEnabled(enabled: Boolean) {
        uiState = uiState.copy(printEnabled = enabled)
    }

    private fun showPage(index: Int) {
        if (index !in launchRequest.pages.indices || index == currentIndex) return
        pageJob?.cancel()
        animationController.clear()
        releaseTiledImage()
        Glide.with(this).clear(imageView)
        loadGeneration += 1
        val generation = loadGeneration
        currentIndex = index
        val page = launchRequest.pages[index]

        imageView.resetZoom()
        hideZoomIndicator()
        val canForward = page.hostRelativePath.isNullOrEmpty()
        uiState = uiState.copy(
            title = page.image.displayName,
            subtitle = launchRequest.pages.size.takeIf { it > 1 }?.let { pageCount ->
                getString(R.string.page_counter, index + 1, pageCount)
            },
            metadataText = getString(R.string.metadata_loading),
            detailsSummary = getString(R.string.metadata_loading),
            exifText = getString(R.string.exif_unavailable),
            detailsAvailable = false,
            detailsExpanded = false,
            loading = true,
            errorText = null,
            printEnabled = false,
            canForward = canForward,
            canEdit = canForward && launchRequest.editable,
        )

        ImagePlatformDecodeSupport.unsupported(page.image)?.let { unavailable ->
            uiState = uiState.copy(metadataText = getString(R.string.metadata_unavailable))
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
            showLoadFailure(message)
            return
        }

        val limits = imageView.largeImageLimits(memoryClassMebibytes)
        if (page.hostRelativePath.isNullOrEmpty()) {
            loadDirectPage(index, page, generation, limits)
        } else {
            loadHostPage(index, page, generation, limits)
        }
    }

    private fun loadDirectPage(
        index: Int,
        page: ImageViewerPage,
        generation: Int,
        limits: ImageBitmapLimits,
    ) {
        pageJob = scope.launch(Dispatchers.IO) {
            var tiledCandidate: TiledImageDrawable? = null
            try {
                val metadata = ImageContentValidator.readDisplayMetadata(contentResolver, page.image)
                tiledCandidate = metadata?.let { createTiledImage(page, it, limits) }
                withContext(Dispatchers.Main.immediate) {
                    if (generation != loadGeneration || isFinishing || isDestroyed) {
                        return@withContext
                    }
                    showMetadata(page.image, metadata)
                    val candidate = tiledCandidate
                    if (candidate == null) {
                        startGlideLoad(page.image.targetUri, index, generation)
                    } else {
                        installTiledImage(candidate, index, generation)
                        tiledCandidate = null
                    }
                }
            } finally {
                tiledCandidate?.close()
            }
        }
    }

    private fun loadHostPage(
        index: Int,
        page: ImageViewerPage,
        generation: Int,
        limits: ImageBitmapLimits,
    ) {
        pageJob = scope.launch(Dispatchers.IO) {
            var tiledCandidate: TiledImageDrawable? = null
            try {
                val metadata = openHostDescriptor(page)?.use { descriptor ->
                    ImageContentValidator.readHostMetadata(descriptor, page.image)
                }
                tiledCandidate = metadata?.let { createTiledImage(page, it, limits) }
                val imageUri = if (metadata != null && tiledCandidate == null) {
                    runCatching {
                        HostSessionImageRegistry.imageUri(requireNotNull(hostImageSessionToken), index)
                    }.getOrNull()
                } else {
                    null
                }
                withContext(Dispatchers.Main.immediate) {
                    if (generation != loadGeneration || isFinishing || isDestroyed) {
                        return@withContext
                    }
                    showMetadata(page.image, metadata)
                    when {
                        metadata == null -> showLoadFailure()
                        tiledCandidate != null -> {
                            installTiledImage(requireNotNull(tiledCandidate), index, generation)
                            tiledCandidate = null
                        }
                        imageUri == null -> showLoadFailure()
                        else -> startGlideLoad(imageUri, index, generation)
                    }
                }
            } finally {
                tiledCandidate?.close()
            }
        }
    }

    private fun createTiledImage(
        page: ImageViewerPage,
        metadata: ImageMetadata,
        limits: ImageBitmapLimits,
    ): TiledImageDrawable? {
        val width = metadata.width ?: return null
        val height = metadata.height ?: return null
        if (
            !ImageLargeImagePolicy.shouldUseTiling(
                width = width,
                height = height,
                mimeType = page.image.mimeType,
                sdkInt = Build.VERSION.SDK_INT,
                limits = limits,
            )
        ) {
            return null
        }
        val descriptor = if (page.hostRelativePath.isNullOrEmpty()) {
            runCatching { contentResolver.openFileDescriptor(page.image.targetUri, "r") }
                .getOrNull()
        } else {
            openHostDescriptor(page)
        } ?: return null
        return TiledImageDrawable.create(
            descriptor = descriptor,
            orientation = metadata.exif?.orientation,
            cacheBudgetBytes = limits.tileCacheBudgetBytes,
        )
    }

    private fun installTiledImage(
        drawable: TiledImageDrawable,
        pageIndex: Int,
        generation: Int,
    ) {
        tiledImageDrawable = drawable
        imageView.setImageDrawable(drawable)
        uiState = uiState.copy(loading = false, errorText = null)
        setPrintEnabled(true)
        imageView.post {
            if (generation == loadGeneration && !isFinishing && !isDestroyed) {
                imageView.restoreRotationQuarterTurns(consumeRestoredRotation(pageIndex))
            }
        }
    }

    private fun releaseTiledImage() {
        val drawable = tiledImageDrawable ?: return
        tiledImageDrawable = null
        if (::imageView.isInitialized && imageView.drawable === drawable) {
            imageView.setImageDrawable(null)
        }
        drawable.close()
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

    private fun startGlideLoad(
        model: Any,
        pageIndex: Int,
        generation: Int,
    ) {
        uiState = uiState.copy(loading = true, errorText = null)
        // Glide 5's static-image Downsampler applies all eight EXIF rotation/mirror variants.
        // ZoomableImageView therefore tracks only the user's additional quarter turns.
        Glide.with(this)
            .load(model)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    error: GlideException?,
                    model: Any?,
                    target: Target<Drawable>,
                    isFirstResource: Boolean,
                ): Boolean {
                    if (generation == loadGeneration && !isFinishing && !isDestroyed) {
                        showLoadFailure()
                    }
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable,
                    model: Any,
                    target: Target<Drawable>?,
                    dataSource: DataSource,
                    isFirstResource: Boolean,
                ): Boolean {
                    if (generation == loadGeneration && !isFinishing && !isDestroyed) {
                        uiState = uiState.copy(loading = false, errorText = null)
                        imageView.post {
                            if (generation == loadGeneration && !isFinishing && !isDestroyed) {
                                imageView.restoreRotationQuarterTurns(consumeRestoredRotation(pageIndex))
                                animationController.attach(
                                    resource,
                                    startPaused = consumeRestoredAnimationPause(pageIndex),
                                )
                                setPrintEnabled(true)
                            }
                        }
                    }
                    return false
                }
            })
            .into(imageView)
    }

    private fun showMetadata(request: ImageViewerRequest, metadata: ImageMetadata?) {
        val text = if (metadata == null) {
            getString(R.string.metadata_unavailable)
        } else {
            val size = Formatter.formatFileSize(this, metadata.byteSize)
            val resolution = if (metadata.width != null && metadata.height != null) {
                getString(R.string.metadata_resolution, metadata.width, metadata.height)
            } else {
                getString(R.string.metadata_resolution_unknown)
            }
            val decodedColor = formatDecodedColor(metadata.decodedColor)
            if (decodedColor == null) {
                getString(R.string.metadata_summary, request.mimeType, size, resolution)
            } else {
                getString(R.string.metadata_summary_with_color, request.mimeType, size, resolution, decodedColor)
            }
        }
        uiState = uiState.copy(metadataText = text, detailsSummary = text)
        showExifDetails(metadata)
    }

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

    private fun showZoomIndicator(zoom: Float, gestureInProgress: Boolean) {
        val formattedZoom = ImageZoomIndicatorFormatter.format(zoom, resources.configuration.locales[0])
            ?: return hideZoomIndicator()
        imageView.removeCallbacks(hideZoomIndicatorRunnable)
        uiState = uiState.copy(zoomIndicatorText = getString(R.string.zoom_indicator, formattedZoom))
        if (!gestureInProgress) {
            imageView.postDelayed(hideZoomIndicatorRunnable, ZOOM_INDICATOR_DURATION_MILLIS)
        }
    }

    private fun hideZoomIndicator() {
        if (!::imageView.isInitialized) return
        imageView.removeCallbacks(hideZoomIndicatorRunnable)
        uiState = uiState.copy(zoomIndicatorText = null)
    }

    private val hideZoomIndicatorRunnable = Runnable {
        uiState = uiState.copy(zoomIndicatorText = null)
    }

    private fun showLoadFailure(
        message: CharSequence = getText(R.string.error_cannot_read_image),
    ) {
        animationController.clear()
        hideZoomIndicator()
        uiState = uiState.copy(
            detailsAvailable = false,
            detailsExpanded = false,
            loading = false,
            errorText = message.toString(),
            printEnabled = false,
        )
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

    private fun showExifDetails(metadata: ImageMetadata?) {
        val details = metadata?.exif?.let(::formatExifDetails)
        uiState = uiState.copy(
            exifText = details ?: getString(R.string.exif_unavailable),
            detailsAvailable = metadata != null,
        )
        val restoreExpanded = restoredExpandedDetailsIndex == currentIndex
        if (restoreExpanded) restoredExpandedDetailsIndex = -1
        setExifDetailsExpanded(restoreExpanded && metadata != null)
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
            if (exif.hasGpsMetadata) add(getString(R.string.exif_gps_present))
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

    private fun setExifDetailsExpanded(expanded: Boolean) {
        uiState = uiState.copy(detailsExpanded = expanded && uiState.detailsAvailable)
    }

    private fun setControlsVisible(visible: Boolean) {
        uiState = uiState.copy(controlsVisible = visible)
    }

    private fun editImage() {
        if (editBusy || !launchRequest.editable) return
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
        val page = launchRequest.pages.getOrNull(currentIndex) ?: return
        val drawable = imageView.drawable ?: return showPrintPreparationFailure()
        val snapshot = ImagePrintSnapshot.capture(
            drawable = drawable,
            rotationQuarterTurns = imageView.currentRotationQuarterTurns,
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

    private fun currentShareablePage(): ImageViewerRequest? {
        val page = launchRequest.pages.getOrNull(currentIndex) ?: return null
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
        launchRequest.hostSession?.let { session -> runCatching { session.close() } }
    }

    private companion object {
        const val STATE_CURRENT_INDEX = "currentIndex"
        const val STATE_ANIMATION_INDEX = "animationIndex"
        const val STATE_ANIMATION_PAUSED = "animationPaused"
        const val STATE_ROTATION_INDEX = "rotationIndex"
        const val STATE_ROTATION_QUARTER_TURNS = "rotationQuarterTurns"
        const val STATE_EXPANDED_DETAILS_INDEX = "expandedDetailsIndex"
        const val ZOOM_INDICATOR_DURATION_MILLIS = 900L
        const val LEGACY_NAVIGATION_SCRIM = 0x66000000
    }
}
