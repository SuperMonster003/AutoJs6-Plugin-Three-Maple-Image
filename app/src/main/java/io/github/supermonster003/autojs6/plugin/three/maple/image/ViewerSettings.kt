package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.content.SharedPreferences

/** What a double tap zooms to (ROADMAP 9.2). */
internal enum class ViewerDoubleTapZoom { TWO_AND_HALF, ACTUAL_PIXELS }

/** How far the viewer zooms in (ROADMAP 9.2); [ACTUAL_PIXELS] stops where one image pixel fills one screen pixel. */
internal enum class ViewerMaxZoom { FIVE, TEN, ACTUAL_PIXELS }

/**
 * Viewer gestures (ROADMAP D8 / 9.2). The defaults are the behavior before they became settings: two-finger
 * rotation and the save offer on, double tap then drag zooms (the platform's quick scale), double tap to 2.5x,
 * at most 5x, and no edge taps, swipe down or brightness swipe.
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

        /** A name this version does not know (written by a later one) falls back to the default. */
        internal inline fun <reified T : Enum<T>> enumValue(stored: String?, default: T): T =
            enumValues<T>().firstOrNull { it.name == stored } ?: default
    }
}
