package io.github.supermonster003.autojs6.plugin.three.maple.image

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ImageAnimationPlaybackStateTest {

    @Test
    fun startsUnavailable() {
        assertEquals(ImageAnimationPlayback.UNAVAILABLE, ImageAnimationPlaybackState().playback)
    }

    @Test
    fun animatedResourceStartsPlayingAndTogglesBothWays() {
        val state = ImageAnimationPlaybackState()

        state.attach(startPaused = false)
        assertEquals(ImageAnimationPlayback.PLAYING, state.playback)
        assertTrue(state.toggle())
        assertEquals(ImageAnimationPlayback.PAUSED, state.playback)
        assertTrue(state.toggle())
        assertEquals(ImageAnimationPlayback.PLAYING, state.playback)
    }

    @Test
    fun restoredResourceCanStartPaused() {
        val state = ImageAnimationPlaybackState()

        state.attach(startPaused = true)

        assertEquals(ImageAnimationPlayback.PAUSED, state.playback)
    }

    @Test
    fun unavailableResourceCannotToggleAndClearRemovesPlayback() {
        val state = ImageAnimationPlaybackState()

        assertFalse(state.toggle())
        state.attach(startPaused = false)
        state.clear()

        assertEquals(ImageAnimationPlayback.UNAVAILABLE, state.playback)
        assertFalse(state.toggle())
    }
}
