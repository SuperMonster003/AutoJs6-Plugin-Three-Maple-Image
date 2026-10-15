package io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose

import androidx.annotation.VisibleForTesting
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Lets tests choose the wide or narrow layouts (ROADMAP 9.3) without resizing the display: a resize in the shared
 * test process leaves Compose roots of earlier tests waiting for a layout that never comes, and every later wait for
 * idle hangs. Null follows the window, as in the app.
 */
@VisibleForTesting
internal object LayoutOverrides {

    /** The home screen shows albums and the open album side by side. */
    var twoPane by mutableStateOf<Boolean?>(null)

    /** The viewer stands its thumbnail strip at the side. */
    var sideStrip by mutableStateOf<Boolean?>(null)
}
