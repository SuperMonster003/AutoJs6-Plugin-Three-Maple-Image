package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.view.KeyEvent

/** What a key does in the viewer (ROADMAP 9.3). */
internal enum class ViewerKeyAction { PREVIOUS, NEXT, DELETE }

/**
 * The keyboard and D-pad keys of the viewer and the grids (ROADMAP 9.3). Left and right turn pages the same way
 * as a swipe; the delete keys ask to delete, as the delete button does.
 */
internal object KeyboardPolicy {

    fun viewerAction(keyCode: Int): ViewerKeyAction? = when (keyCode) {
        KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.KEYCODE_PAGE_UP, KeyEvent.KEYCODE_MEDIA_PREVIOUS -> ViewerKeyAction.PREVIOUS
        KeyEvent.KEYCODE_DPAD_RIGHT, KeyEvent.KEYCODE_PAGE_DOWN, KeyEvent.KEYCODE_MEDIA_NEXT -> ViewerKeyAction.NEXT
        KeyEvent.KEYCODE_DEL, KeyEvent.KEYCODE_FORWARD_DEL -> ViewerKeyAction.DELETE
        else -> null
    }

    fun isDelete(keyCode: Int): Boolean = keyCode == KeyEvent.KEYCODE_DEL || keyCode == KeyEvent.KEYCODE_FORWARD_DEL
}
