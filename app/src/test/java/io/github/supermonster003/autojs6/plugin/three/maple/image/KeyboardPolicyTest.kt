package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.view.KeyEvent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** ROADMAP 9.3: the keys of the viewer and the grids. */
class KeyboardPolicyTest {

    @Test fun leftAndRightTurnPagesLikeASwipe() {
        assertEquals(ViewerKeyAction.PREVIOUS, KeyboardPolicy.viewerAction(KeyEvent.KEYCODE_DPAD_LEFT))
        assertEquals(ViewerKeyAction.PREVIOUS, KeyboardPolicy.viewerAction(KeyEvent.KEYCODE_PAGE_UP))
        assertEquals(ViewerKeyAction.NEXT, KeyboardPolicy.viewerAction(KeyEvent.KEYCODE_DPAD_RIGHT))
        assertEquals(ViewerKeyAction.NEXT, KeyboardPolicy.viewerAction(KeyEvent.KEYCODE_PAGE_DOWN))
    }

    @Test fun bothDeleteKeysAskToDelete() {
        assertEquals(ViewerKeyAction.DELETE, KeyboardPolicy.viewerAction(KeyEvent.KEYCODE_DEL))
        assertEquals(ViewerKeyAction.DELETE, KeyboardPolicy.viewerAction(KeyEvent.KEYCODE_FORWARD_DEL))
        assertTrue(KeyboardPolicy.isDelete(KeyEvent.KEYCODE_DEL) && KeyboardPolicy.isDelete(KeyEvent.KEYCODE_FORWARD_DEL))
        assertFalse(KeyboardPolicy.isDelete(KeyEvent.KEYCODE_D))
    }

    @Test fun otherKeysAreLeftToTheControls() {
        listOf(KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_DPAD_DOWN, KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_BACK, KeyEvent.KEYCODE_TAB)
            .forEach { assertNull("key $it", KeyboardPolicy.viewerAction(it)) }
    }
}
