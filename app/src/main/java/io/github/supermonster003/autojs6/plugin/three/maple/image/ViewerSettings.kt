package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.content.SharedPreferences
import android.content.pm.ActivityInfo

/** What a double tap zooms to (ROADMAP 9.2). */
internal enum class ViewerDoubleTapZoom { TWO_AND_HALF, ACTUAL_PIXELS }

/** How far the viewer zooms in (ROADMAP 9.2); [ACTUAL_PIXELS] stops where one image pixel fills one screen pixel. */
internal enum class ViewerMaxZoom { FIVE, TEN, ACTUAL_PIXELS }

/** The viewer's screen orientation (ROADMAP 9.2): as the device says, kept as it opened, or as the image is shaped. */
internal enum class ViewerOrientation { FOLLOW_SYSTEM, LOCKED, FOLLOW_IMAGE }

/**
 * Viewer gestures and display (ROADMAP D8 / 9.2). The defaults are the behavior before they became settings:
 * two-finger rotation and the save offer on, double tap then drag zooms (the platform's quick scale), double tap to
 * 2.5x, at most 5x, no edge taps, swipe down or brightness swipe; the orientation follows the device, the canvas is
 * black, brightness and screen timeout are the system's, the system bars stay, the strip shows and pages are decoded
 * for the screen.
 */
internal data class ViewerSettings(
    val rotationGesture: Boolean = true,
    val rotationSave: Boolean = true,
    val oneFingerZoom: Boolean = true,
    val edgeTapPaging: Boolean = false,
    val swipeDownToClose: Boolean = false,
    val brightnessSwipe: Boolean = false,
    val doubleTapZoom: ViewerDoubleTapZoom = ViewerDoubleTapZoom.TWO_AND_HALF,
    val maxZoom: ViewerMaxZoom = ViewerMaxZoom.FIVE,
    val orientation: ViewerOrientation = ViewerOrientation.FOLLOW_SYSTEM,
    val blackBackground: Boolean = true,
    val maxBrightness: Boolean = false,
    /** The status and navigation bars hide with the viewer's own bars (a swipe from the edge shows them for a moment). */
    val hideSystemBars: Boolean = false,
    val keepScreenOn: Boolean = false,
    val thumbnailStrip: Boolean = true,
    /** Pages are decoded at their own size (within the memory budget) instead of the screen's, so zooming stays sharp. */
    val highestQuality: Boolean = false,
) {
    internal companion object {
        // The rotation keys predate this class and are kept so the choice survives the update.
        private const val KEY_ROTATION_GESTURE = "viewer-rotation-gesture"
        private const val KEY_ROTATION_SAVE = "viewer-rotation-save"
        private const val KEY_ONE_FINGER_ZOOM = "viewer-one-finger-zoom"
        private const val KEY_EDGE_TAP_PAGING = "viewer-edge-tap-paging"
        private const val KEY_SWIPE_DOWN_TO_CLOSE = "viewer-swipe-down-to-close"
        private const val KEY_BRIGHTNESS_SWIPE = "viewer-brightness-swipe"
        private const val KEY_DOUBLE_TAP_ZOOM = "viewer-double-tap-zoom"
        private const val KEY_MAX_ZOOM = "viewer-max-zoom"
        private const val KEY_ORIENTATION = "viewer-orientation"
        private const val KEY_BLACK_BACKGROUND = "viewer-black-background"
        private const val KEY_MAX_BRIGHTNESS = "viewer-max-brightness"
        private const val KEY_HIDE_SYSTEM_BARS = "viewer-hide-system-bars"
        private const val KEY_KEEP_SCREEN_ON = "viewer-keep-screen-on"
        private const val KEY_THUMBNAIL_STRIP = "viewer-thumbnail-strip"
        private const val KEY_HIGHEST_QUALITY = "viewer-highest-quality"

        fun read(preferences: SharedPreferences): ViewerSettings {
            val defaults = ViewerSettings()
            return ViewerSettings(
                rotationGesture = preferences.getBoolean(KEY_ROTATION_GESTURE, defaults.rotationGesture),
                rotationSave = preferences.getBoolean(KEY_ROTATION_SAVE, defaults.rotationSave),
                oneFingerZoom = preferences.getBoolean(KEY_ONE_FINGER_ZOOM, defaults.oneFingerZoom),
                edgeTapPaging = preferences.getBoolean(KEY_EDGE_TAP_PAGING, defaults.edgeTapPaging),
                swipeDownToClose = preferences.getBoolean(KEY_SWIPE_DOWN_TO_CLOSE, defaults.swipeDownToClose),
                brightnessSwipe = preferences.getBoolean(KEY_BRIGHTNESS_SWIPE, defaults.brightnessSwipe),
                doubleTapZoom = enumValue(preferences.getString(KEY_DOUBLE_TAP_ZOOM, null), defaults.doubleTapZoom),
                maxZoom = enumValue(preferences.getString(KEY_MAX_ZOOM, null), defaults.maxZoom),
                orientation = enumValue(preferences.getString(KEY_ORIENTATION, null), defaults.orientation),
                blackBackground = preferences.getBoolean(KEY_BLACK_BACKGROUND, defaults.blackBackground),
                maxBrightness = preferences.getBoolean(KEY_MAX_BRIGHTNESS, defaults.maxBrightness),
                hideSystemBars = preferences.getBoolean(KEY_HIDE_SYSTEM_BARS, defaults.hideSystemBars),
                keepScreenOn = preferences.getBoolean(KEY_KEEP_SCREEN_ON, defaults.keepScreenOn),
                thumbnailStrip = preferences.getBoolean(KEY_THUMBNAIL_STRIP, defaults.thumbnailStrip),
                highestQuality = preferences.getBoolean(KEY_HIGHEST_QUALITY, defaults.highestQuality),
            )
        }

        fun write(editor: SharedPreferences.Editor, settings: ViewerSettings): SharedPreferences.Editor = editor
            .putBoolean(KEY_ROTATION_GESTURE, settings.rotationGesture)
            .putBoolean(KEY_ROTATION_SAVE, settings.rotationSave)
            .putBoolean(KEY_ONE_FINGER_ZOOM, settings.oneFingerZoom)
            .putBoolean(KEY_EDGE_TAP_PAGING, settings.edgeTapPaging)
            .putBoolean(KEY_SWIPE_DOWN_TO_CLOSE, settings.swipeDownToClose)
            .putBoolean(KEY_BRIGHTNESS_SWIPE, settings.brightnessSwipe)
            .putString(KEY_DOUBLE_TAP_ZOOM, settings.doubleTapZoom.name)
            .putString(KEY_MAX_ZOOM, settings.maxZoom.name)
            .putString(KEY_ORIENTATION, settings.orientation.name)
            .putBoolean(KEY_BLACK_BACKGROUND, settings.blackBackground)
            .putBoolean(KEY_MAX_BRIGHTNESS, settings.maxBrightness)
            .putBoolean(KEY_HIDE_SYSTEM_BARS, settings.hideSystemBars)
            .putBoolean(KEY_KEEP_SCREEN_ON, settings.keepScreenOn)
            .putBoolean(KEY_THUMBNAIL_STRIP, settings.thumbnailStrip)
            .putBoolean(KEY_HIGHEST_QUALITY, settings.highestQuality)

        /** A name this version does not know (written by a later one) falls back to the default. */
        internal inline fun <reified T : Enum<T>> enumValue(stored: String?, default: T): T =
            enumValues<T>().firstOrNull { it.name == stored } ?: default
    }
}

/** The Android-free display decisions of the viewer (ROADMAP 9.2). */
internal object ViewerDisplayPolicy {

    /**
     * The orientation to request; for [ViewerOrientation.FOLLOW_IMAGE] a square or not yet measured image leaves
     * the current request alone (null).
     */
    fun requestedOrientation(setting: ViewerOrientation, imageWidth: Int?, imageHeight: Int?): Int? = when (setting) {
        ViewerOrientation.FOLLOW_SYSTEM -> ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        ViewerOrientation.LOCKED -> ActivityInfo.SCREEN_ORIENTATION_LOCKED
        ViewerOrientation.FOLLOW_IMAGE -> when {
            imageWidth == null || imageHeight == null || imageWidth <= 0 || imageHeight <= 0 -> null
            imageWidth > imageHeight -> ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            imageHeight > imageWidth -> ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
            else -> null
        }
    }

    /** The screen stays on by the setting, and while a slideshow runs (not while it is paused). */
    fun keepScreenOn(setting: Boolean, slideshowRunning: Boolean): Boolean = setting || slideshowRunning

    /** With "hide system bars" the system bars follow the viewer's bars; otherwise they always show. */
    fun systemBarsVisible(hideSystemBars: Boolean, controlsVisible: Boolean): Boolean = !hideSystemBars || controlsVisible

    /** A brightness the user dragged to wins; otherwise full with "maximum brightness", else the system's (-1). */
    fun windowBrightness(maxBrightness: Boolean, dragged: Float?): Float = dragged ?: if (maxBrightness) 1f else -1f
}
