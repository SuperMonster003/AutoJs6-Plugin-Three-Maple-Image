package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.app.Application
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.MapleTheme
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.WallpaperCallbacks
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.WallpaperScreen
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.WallpaperUiModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import kotlin.math.max

/**
 * "Set as wallpaper" (ROADMAP 9.1): the image decoded large enough for the screen, the editor's crop frame locked to
 * the screen's shape, then home / lock / both through `WallpaperManager`. When the system does not take it, another
 * app can (`ACTION_ATTACH_DATA`).
 */
class WallpaperActivity : ConfiguredActivity() {

    private val model by viewModels<WallpaperViewModel>()
    private lateinit var editor: ImageEditingView
    private lateinit var uri: Uri
    private lateinit var mimeType: String
    private lateinit var displayName: String
    private lateinit var frame: ImagePixelSize
    private var target by mutableStateOf(WallpaperTarget.HOME)
    private var applyFailed by mutableStateOf(false)

    internal val editingViewForTesting: ImageEditingView get() = editor

    override val usesDarkSystemBars: Boolean get() = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        uri = intent.data ?: run {
            finish()
            return
        }
        mimeType = intent.type ?: "image/*"
        displayName = intent.getStringExtra(EXTRA_DISPLAY_NAME) ?: uri.lastPathSegment.orEmpty()
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        savedInstanceState?.getString(STATE_TARGET)
            ?.let { name -> WallpaperTarget.entries.firstOrNull { it.name == name } }
            ?.let { target = it }
        frame = Wallpapers.frame(this)
        editor = ImageEditingView(this).apply { contentDescription = getString(R.string.action_set_wallpaper) }
        model.load(uri, frame)
        val callbacks = WallpaperCallbacks(
            onBack = ::finish,
            onTarget = { target = it },
            onApply = ::apply,
            onSetWithAnotherApp = ::setWithAnotherApp,
        )
        setContent {
            MapleTheme(palette = appPalette) {
                val bitmap = model.bitmap
                LaunchedEffect(bitmap) { bitmap?.let(::showCrop) }
                LaunchedEffect(model.outcome) {
                    when (model.consumeOutcome()) {
                        true -> {
                            Toast.makeText(this@WallpaperActivity, R.string.wallpaper_done, Toast.LENGTH_SHORT).show()
                            setResult(RESULT_OK)
                            finish()
                        }
                        false -> applyFailed = true
                        null -> Unit
                    }
                }
                WallpaperScreen(
                    model = WallpaperUiModel(
                        title = displayName,
                        loading = model.loading,
                        loadFailed = model.loadFailed,
                        busy = model.busy,
                        applyFailed = applyFailed,
                        target = target,
                    ),
                    editingView = editor,
                    callbacks = callbacks,
                )
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(STATE_TARGET, target.name)
    }

    override fun onDestroy() {
        if (::editor.isInitialized) {
            // The crop survives a rotation; a tablet turned to the other shape starts a new one (see showCrop).
            if (isChangingConfigurations) model.cropState = editor.captureCanvasState()
            editor.setBitmap(null)
        }
        super.onDestroy()
    }

    private fun showCrop(bitmap: Bitmap) {
        editor.setBitmap(bitmap)
        val aspectRatio = frame.width.toFloat() / frame.height
        val saved = model.cropState as? ImageEditingView.CanvasState.Crop
        if (saved != null && saved.fixedAspectRatio == aspectRatio) editor.restoreCanvasState(saved) else editor.beginCrop(aspectRatio)
    }

    private fun apply() {
        val selection = editor.cropSelection() ?: return
        applyFailed = false
        model.apply(selection, frame, target)
    }

    private fun setWithAnotherApp() {
        val chooser = Wallpapers.attachDataIntent(uri, mimeType, displayName, getString(R.string.wallpaper_set_with))
        runCatching { startActivity(chooser) }
            .onSuccess { finish() }
            .onFailure { Toast.makeText(this, R.string.error_no_wallpaper_app, Toast.LENGTH_SHORT).show() }
    }

    companion object {
        private const val EXTRA_DISPLAY_NAME = "io.github.supermonster003.autojs6.plugin.three.maple.image.wallpaper.DISPLAY_NAME"
        private const val STATE_TARGET = "wallpaper.target"

        internal fun intent(context: Context, image: ImageViewerRequest): Intent =
            Intent(context, WallpaperActivity::class.java).apply {
                setDataAndType(image.targetUri, image.mimeType)
                putExtra(EXTRA_DISPLAY_NAME, image.displayName)
                clipData = ClipData.newRawUri(image.displayName, image.targetUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
    }
}

internal class WallpaperViewModel(application: Application) : AndroidViewModel(application) {

    var bitmap by mutableStateOf<Bitmap?>(null)
        private set
    var loading by mutableStateOf(true)
        private set
    var loadFailed by mutableStateOf(false)
        private set
    var busy by mutableStateOf(false)
        private set

    /** Whether the system took the last wallpaper, until the screen reads it with [consumeOutcome]. */
    var outcome by mutableStateOf<Boolean?>(null)
        private set

    /** The crop kept across a configuration change. */
    var cropState: ImageEditingView.CanvasState? = null

    private var started = false

    fun consumeOutcome(): Boolean? = outcome.also { outcome = null }

    fun load(uri: Uri, frame: ImagePixelSize) {
        if (started) return
        started = true
        viewModelScope.launch {
            val decoded = try {
                withContext(Dispatchers.IO) { decode(uri, frame) }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Throwable) {
                Log.w(TAG, "Unable to read the image for the wallpaper", error)
                null
            }
            bitmap = decoded
            loadFailed = decoded == null
            loading = false
        }
    }

    fun apply(selection: Rect, frame: ImagePixelSize, target: WallpaperTarget) {
        val source = bitmap ?: return
        if (busy) return
        busy = true
        viewModelScope.launch {
            val applied = try {
                withContext(Dispatchers.IO) { applyCrop(source, selection, frame, target) }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Throwable) {
                Log.w(TAG, "Unable to prepare the wallpaper", error)
                false
            }
            busy = false
            outcome = applied
        }
    }

    private fun decode(uri: Uri, frame: ImagePixelSize): Bitmap {
        val resolver = getApplication<Application>().contentResolver
        val info = ImageBitmapIO.inspect(resolver, uri)
        var size = WallpaperPolicy.decodeSize(info.displaySize, frame, ImageBitmapIO.editingPixelBudget())
        repeat(MAX_SHRINKS) {
            if (ImageBitmapIO.isWithinConversionMemoryBudget(info, size)) {
                return ImageBitmapIO.decodeForConversion(resolver, uri, size, info)
            }
            size = ImagePixelSize(max(1, size.width / 2), max(1, size.height / 2))
        }
        throw IOException("Not enough memory to read the image for the wallpaper")
    }

    private fun applyCrop(source: Bitmap, selection: Rect, frame: ImagePixelSize, target: WallpaperTarget): Boolean {
        val cropped = Bitmap.createBitmap(source, selection.left, selection.top, selection.width(), selection.height())
        val output = WallpaperPolicy.outputSize(ImagePixelSize(cropped.width, cropped.height), frame)
        val scaled = if (output.width == cropped.width && output.height == cropped.height) {
            cropped
        } else {
            Bitmap.createScaledBitmap(cropped, output.width, output.height, true)
        }
        try {
            return Wallpapers.apply(getApplication(), scaled, target)
        } finally {
            // createBitmap may hand back the source itself (a full-image crop); that one stays with the screen.
            if (scaled !== source) scaled.recycle()
            if (cropped !== source && cropped !== scaled) cropped.recycle()
        }
    }

    override fun onCleared() {
        bitmap?.recycle()
        bitmap = null
    }

    private companion object {
        const val TAG = "MapleWallpaper"
        const val MAX_SHRINKS = 3
    }
}
