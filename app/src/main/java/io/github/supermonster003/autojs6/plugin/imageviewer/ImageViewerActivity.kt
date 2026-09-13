package io.github.supermonster003.autojs6.plugin.imageviewer

import android.app.ActivityManager
import android.content.ClipData
import android.content.ComponentName
import android.content.ComponentCallbacks2
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.print.PrintAttributes
import android.print.PrintManager
import android.text.format.Formatter
import android.util.TypedValue
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.SystemBarStyle
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.material.bottomsheet.BottomSheetBehavior
import io.github.supermonster003.autojs6.plugin.imageviewer.databinding.ActivityImageViewerBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.math.roundToInt

class ImageViewerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityImageViewerBinding
    private lateinit var launchRequest: ImageViewerLaunchRequest
    private lateinit var animationController: ImageAnimationController
    private lateinit var detailsBehavior: BottomSheetBehavior<*>
    private lateinit var closeDetailsOnBack: OnBackPressedCallback
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var pageJob: Job? = null
    private var tiledImageDrawable: TiledImageDrawable? = null
    private var hostImageSessionToken: String? = null
    private var currentIndex = -1
    private var loadGeneration = 0
    private var controlsVisible = true
    private var hostSessionClosed = false
    private var restoredAnimationIndex = -1
    private var restoredAnimationPaused = false
    private var restoredRotationIndex = -1
    private var restoredRotationQuarterTurns = ImageRotationState.ORIGINAL_QUARTER_TURNS
    private var exifDetailsExpanded = false
    private var restoredExpandedDetailsIndex = -1
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
        binding = ActivityImageViewerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        animationController = ImageAnimationController { playback ->
            binding.animationToggle.renderImageAnimationPlayback(playback)
            binding.animationToggle.isVisible = binding.animationToggle.isVisible && controlsVisible
        }
        applyWindowInsets()

        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.toolbar.inflateMenu(R.menu.menu_image_viewer)
        binding.toolbar.setOnMenuItemClickListener(::onToolbarMenuItemClick)
        binding.image.setOnClickListener {
            if (exifDetailsExpanded) {
                setExifDetailsExpanded(false)
            } else {
                setControlsVisible(!controlsVisible)
            }
        }
        binding.image.onPageSwipe = { direction -> showPage(currentIndex + direction) }
        binding.image.onZoomInteraction = ::showZoomIndicator
        binding.imageDetailsToggle.setOnClickListener {
            setExifDetailsExpanded(!exifDetailsExpanded)
        }
        binding.animationToggle.setOnClickListener { animationController.toggle() }
        binding.rotateClockwise.setOnClickListener { binding.image.rotateClockwise90() }
        binding.share.setOnClickListener { shareImage() }
        binding.openExternal.setOnClickListener { openWithAnotherApp() }

        detailsBehavior = BottomSheetBehavior.from(binding.detailsSheet)
        detailsBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN && exifDetailsExpanded) {
                    setExifDetailsExpanded(false)
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) = Unit
        })
        closeDetailsOnBack = onBackPressedDispatcher.addCallback(this, enabled = false) {
            setExifDetailsExpanded(false)
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
            binding.image.post {
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
            ::binding.isInitialized &&
            binding.image.currentRotationQuarterTurns !=
            ImageRotationState.ORIGINAL_QUARTER_TURNS
        ) {
            outState.putInt(STATE_ROTATION_INDEX, currentIndex)
            outState.putInt(
                STATE_ROTATION_QUARTER_TURNS,
                binding.image.currentRotationQuarterTurns,
            )
        }
        if (exifDetailsExpanded && currentIndex in launchRequest.pages.indices) {
            outState.putInt(STATE_EXPANDED_DETAILS_INDEX, currentIndex)
        }
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        pageJob?.cancel()
        scope.cancel()
        if (::binding.isInitialized) {
            animationController.clear()
            releaseTiledImage()
            Glide.with(applicationContext).clear(binding.image)
            binding.image.onPageSwipe = null
            binding.image.onZoomInteraction = null
            binding.zoomIndicator.removeCallbacks(hideZoomIndicatorRunnable)
        }
        HostSessionImageRegistry.unregister(hostImageSessionToken)
        hostImageSessionToken = null
        if (!isChangingConfigurations) closeHostSession()
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

    private fun applyWindowInsets() {
        val actionBarSize = TypedValue().let { value ->
            theme.resolveAttribute(android.R.attr.actionBarSize, value, true)
            TypedValue.complexToDimensionPixelSize(value.data, resources.displayMetrics)
        }
        val gap = (OVERLAY_GAP_DP * resources.displayMetrics.density).roundToInt()
        val bottomBarPadding = binding.bottomBar.basePadding()
        val sheetPadding = binding.detailsSheet.basePadding()
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout(),
            )
            binding.toolbar.updatePadding(left = bars.left, top = bars.top, right = bars.right)
            binding.toolbar.updateLayoutParams { height = actionBarSize + bars.top }
            binding.bottomBar.updatePadding(
                left = bottomBarPadding.left + bars.left,
                right = bottomBarPadding.right + bars.right,
                bottom = bottomBarPadding.bottom + bars.bottom,
            )
            binding.animationToggle.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                leftMargin = gap + bars.left
                rightMargin = gap + bars.right
            }
            binding.detailsSheet.updatePadding(
                left = sheetPadding.left + bars.left,
                right = sheetPadding.right + bars.right,
                bottom = sheetPadding.bottom + bars.bottom,
            )
            binding.zoomIndicator.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = bars.top + actionBarSize + gap
                leftMargin = gap + bars.left
                rightMargin = gap + bars.right
            }
            insets
        }
    }

    private fun View.basePadding() = Rect(paddingLeft, paddingTop, paddingRight, paddingBottom)

    private fun onToolbarMenuItemClick(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_release_history -> { showReleaseHistory(); true }
        R.id.reset_zoom -> {
            hideZoomIndicator()
            binding.image.resetZoom()
            true
        }
        R.id.print_image -> {
            printImage()
            true
        }
        else -> false
    }

    private fun setPrintEnabled(enabled: Boolean) {
        binding.toolbar.menu.findItem(R.id.print_image)?.isEnabled = enabled
    }

    private fun showPage(index: Int) {
        if (index !in launchRequest.pages.indices || index == currentIndex) return
        pageJob?.cancel()
        animationController.clear()
        releaseTiledImage()
        Glide.with(this).clear(binding.image)
        loadGeneration += 1
        val generation = loadGeneration
        currentIndex = index
        val page = launchRequest.pages[index]

        binding.image.resetZoom()
        hideZoomIndicator()
        binding.toolbar.title = page.image.displayName
        binding.toolbar.subtitle = launchRequest.pages.size.takeIf { it > 1 }?.let { pageCount ->
            getString(R.string.page_counter, index + 1, pageCount)
        }
        binding.detailsTitle.text = page.image.displayName
        binding.metadata.setText(R.string.metadata_loading)
        binding.detailsSummary.setText(R.string.metadata_loading)
        binding.imageDetailsToggle.isVisible = false
        setExifDetailsExpanded(false)
        binding.loadingIndicator.isVisible = true
        binding.errorText.setText(R.string.error_cannot_read_image)
        binding.errorText.isVisible = false
        setPrintEnabled(false)
        val canForward = page.hostRelativePath.isNullOrEmpty()
        binding.share.isEnabled = canForward
        binding.openExternal.isEnabled = canForward

        ImagePlatformDecodeSupport.unsupported(page.image)?.let { unavailable ->
            binding.metadata.setText(R.string.metadata_unavailable)
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

        val limits = binding.image.largeImageLimits(memoryClassMebibytes)
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
        binding.image.setImageDrawable(drawable)
        binding.loadingIndicator.isVisible = false
        binding.errorText.isVisible = false
        setPrintEnabled(true)
        binding.image.post {
            if (generation == loadGeneration && !isFinishing && !isDestroyed) {
                binding.image.restoreRotationQuarterTurns(
                    consumeRestoredRotation(pageIndex),
                )
            }
        }
    }

    private fun releaseTiledImage() {
        val drawable = tiledImageDrawable ?: return
        tiledImageDrawable = null
        if (::binding.isInitialized && binding.image.drawable === drawable) {
            binding.image.setImageDrawable(null)
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
        binding.loadingIndicator.isVisible = true
        binding.errorText.isVisible = false
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
                        binding.loadingIndicator.isVisible = false
                        binding.errorText.isVisible = false
                        binding.image.post {
                            if (generation == loadGeneration && !isFinishing && !isDestroyed) {
                                binding.image.restoreRotationQuarterTurns(
                                    consumeRestoredRotation(pageIndex),
                                )
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
            .into(binding.image)
    }

    private fun showMetadata(request: ImageViewerRequest, metadata: ImageMetadata?) {
        binding.metadata.text = if (metadata == null) {
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
                getString(
                    R.string.metadata_summary_with_color,
                    request.mimeType,
                    size,
                    resolution,
                    decodedColor,
                )
            }
        }
        binding.detailsSummary.text = binding.metadata.text
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
        val formattedZoom = ImageZoomIndicatorFormatter.format(
            zoom,
            resources.configuration.locales[0],
        )
            ?: return hideZoomIndicator()
        binding.zoomIndicator.apply {
            removeCallbacks(hideZoomIndicatorRunnable)
            text = getString(R.string.zoom_indicator, formattedZoom)
            isVisible = true
            if (!gestureInProgress) {
                postDelayed(hideZoomIndicatorRunnable, ZOOM_INDICATOR_DURATION_MILLIS)
            }
        }
    }

    private fun hideZoomIndicator() {
        if (!::binding.isInitialized) return
        binding.zoomIndicator.removeCallbacks(hideZoomIndicatorRunnable)
        binding.zoomIndicator.isVisible = false
    }

    private val hideZoomIndicatorRunnable = Runnable {
        if (::binding.isInitialized) binding.zoomIndicator.isVisible = false
    }

    private fun showLoadFailure(
        message: CharSequence = getText(R.string.error_cannot_read_image),
    ) {
        animationController.clear()
        hideZoomIndicator()
        binding.imageDetailsToggle.isVisible = false
        setExifDetailsExpanded(false)
        binding.loadingIndicator.isVisible = false
        binding.errorText.text = message
        binding.errorText.isVisible = true
        setPrintEnabled(false)
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
        binding.exifDetails.text = details ?: getString(R.string.exif_unavailable)
        binding.imageDetailsToggle.isVisible = metadata != null
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
                add(
                    getString(
                        R.string.exif_orientation,
                        formatExifOrientation(orientation),
                    ),
                )
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
        exifDetailsExpanded = expanded && binding.imageDetailsToggle.isVisible
        if (exifDetailsExpanded) {
            binding.detailsSheet.isVisible = true
            detailsBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        } else {
            if (detailsBehavior.state != BottomSheetBehavior.STATE_HIDDEN) {
                detailsBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            }
            binding.detailsSheet.isVisible = false
        }
        closeDetailsOnBack.isEnabled = exifDetailsExpanded
        binding.imageDetailsToggle.contentDescription = getString(
            if (exifDetailsExpanded) {
                R.string.action_hide_image_details
            } else {
                R.string.action_show_image_details
            },
        )
    }

    private fun setControlsVisible(visible: Boolean) {
        controlsVisible = visible
        binding.toolbar.isVisible = visible
        binding.bottomBar.isVisible = visible
        binding.animationToggle.isVisible =
            visible && animationController.playback != ImageAnimationPlayback.UNAVAILABLE
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
        val drawable = binding.image.drawable ?: return showPrintPreparationFailure()
        val snapshot = ImagePrintSnapshot.capture(
            drawable = drawable,
            rotationQuarterTurns = binding.image.currentRotationQuarterTurns,
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
        const val OVERLAY_GAP_DP = 16f
        const val LEGACY_NAVIGATION_SCRIM = 0x66000000
    }
}
