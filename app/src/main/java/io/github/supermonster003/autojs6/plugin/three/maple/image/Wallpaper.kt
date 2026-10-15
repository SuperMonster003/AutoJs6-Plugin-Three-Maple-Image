package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.app.Activity
import android.app.WallpaperManager
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.SystemClock
import android.util.DisplayMetrics
import android.util.Log
import java.io.IOException
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sqrt

/** Where a new wallpaper goes (ROADMAP 9.1). */
internal enum class WallpaperTarget { HOME, LOCK, BOTH }

/**
 * The Android-free sizes of "Set as wallpaper": the frame the crop is locked to, how large the image is decoded for
 * cropping and how large the cropped wallpaper is handed to the system.
 */
internal object WallpaperPolicy {

    const val TABLET_SMALLEST_WIDTH_DP = 600

    /** A phone keeps its wallpaper upright however it is held; a tablet frames it the way it is held. */
    fun frame(screenWidth: Int, screenHeight: Int, smallestWidthDp: Int): ImagePixelSize {
        require(screenWidth > 0 && screenHeight > 0) { "Screen size must be positive" }
        if (smallestWidthDp >= TABLET_SMALLEST_WIDTH_DP) return ImagePixelSize(screenWidth, screenHeight)
        return ImagePixelSize(min(screenWidth, screenHeight), max(screenWidth, screenHeight))
    }

    /**
     * The size to decode [image] at: the widest crop of the frame's shape gets twice the frame's pixels where the
     * image has them (room to crop closer without blur), at most [maxPixels] and never more than the image itself.
     */
    fun decodeSize(image: ImagePixelSize, frame: ImagePixelSize, maxPixels: Long): ImagePixelSize {
        require(image.width > 0 && image.height > 0 && maxPixels > 0)
        val widerThanFrame = image.width.toLong() * frame.height > frame.width.toLong() * image.height
        // The widest crop spans the image's full height when the image is wider than the frame, else its full width.
        val fullCropScale = if (widerThanFrame) frame.height.toDouble() / image.height else frame.width.toDouble() / image.width
        val budgetScale = sqrt(maxPixels.toDouble() / image.pixelCount)
        // Rounded down, so the budget holds.
        return image.scaled(minOf(1.0, fullCropScale * DECODE_HEADROOM, budgetScale)) { floor(it).toInt() }
    }

    /** The crop brought down to cover the frame; a crop smaller than the frame is left for the system to scale. */
    fun outputSize(crop: ImagePixelSize, frame: ImagePixelSize): ImagePixelSize {
        require(crop.width > 0 && crop.height > 0)
        val cover = max(frame.width.toDouble() / crop.width, frame.height.toDouble() / crop.height)
        return crop.scaled(min(1.0, cover)) { it.roundToInt() }
    }

    private fun ImagePixelSize.scaled(scale: Double, rounding: (Double) -> Int): ImagePixelSize =
        if (scale >= 1.0) this else ImagePixelSize(max(1, rounding(width * scale)), max(1, rounding(height * scale)))

    private const val DECODE_HEADROOM = 2.0
}

/** The system side of "Set as wallpaper" (ROADMAP 9.1); when it is not possible the image goes to `ACTION_ATTACH_DATA`. */
internal object Wallpapers {

    /** False where this user may not change the wallpaper (device policy) or the device has none. */
    fun available(context: Context): Boolean {
        val manager = WallpaperManager.getInstance(context)
        return manager.isWallpaperSupported && manager.isSetWallpaperAllowed
    }

    fun frame(activity: Activity): ImagePixelSize {
        val (width, height) = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            activity.windowManager.maximumWindowMetrics.bounds.let { it.width() to it.height() }
        } else {
            val metrics = DisplayMetrics()
            @Suppress("DEPRECATION")
            activity.windowManager.defaultDisplay.getRealMetrics(metrics)
            metrics.widthPixels to metrics.heightPixels
        }
        return WallpaperPolicy.frame(width, height, activity.resources.configuration.smallestScreenWidthDp)
    }

    /** True once the system took [bitmap]; false when it refused or failed. */
    fun apply(context: Context, bitmap: Bitmap, target: WallpaperTarget): Boolean {
        val which = when (target) {
            WallpaperTarget.HOME -> WallpaperManager.FLAG_SYSTEM
            WallpaperTarget.LOCK -> WallpaperManager.FLAG_LOCK
            WallpaperTarget.BOTH -> WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK
        }
        return try {
            val started = SystemClock.elapsedRealtime()
            val id = WallpaperManager.getInstance(context).setBitmap(bitmap, null, true, which)
            Log.i(TAG, "Wallpaper $target ${bitmap.width}x${bitmap.height}: id $id in ${SystemClock.elapsedRealtime() - started} ms")
            id != 0
        } catch (error: IOException) {
            Log.w(TAG, "Setting the wallpaper failed", error)
            false
        } catch (error: SecurityException) {
            Log.w(TAG, "Setting the wallpaper is not allowed", error)
            false
        }
    }

    /** Lets another app (the system wallpaper picker, a launcher, contacts) use the image. */
    fun attachDataIntent(uri: Uri, mimeType: String, displayName: String, title: CharSequence): Intent {
        val target = Intent(Intent.ACTION_ATTACH_DATA).apply {
            addCategory(Intent.CATEGORY_DEFAULT)
            setDataAndType(uri, mimeType)
            putExtra("mimeType", mimeType)
            clipData = ClipData.newRawUri(displayName, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        return Intent.createChooser(target, title)
    }

    private const val TAG = "MapleWallpaper"
}
