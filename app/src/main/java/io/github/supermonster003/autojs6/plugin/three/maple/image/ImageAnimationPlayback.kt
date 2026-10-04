package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import androidx.core.view.isVisible
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.google.android.material.button.MaterialButton

internal enum class ImageAnimationPlayback {
    UNAVAILABLE,
    PLAYING,
    PAUSED,
}

/** Small platform-independent state machine behind the animated-image control. */
internal class ImageAnimationPlaybackState {

    var playback: ImageAnimationPlayback = ImageAnimationPlayback.UNAVAILABLE
        private set

    fun attach(startPaused: Boolean) {
        playback = if (startPaused) {
            ImageAnimationPlayback.PAUSED
        } else {
            ImageAnimationPlayback.PLAYING
        }
    }

    fun toggle(): Boolean {
        playback = when (playback) {
            ImageAnimationPlayback.PLAYING -> ImageAnimationPlayback.PAUSED
            ImageAnimationPlayback.PAUSED -> ImageAnimationPlayback.PLAYING
            ImageAnimationPlayback.UNAVAILABLE -> return false
        }
        return true
    }

    fun clear() {
        playback = ImageAnimationPlayback.UNAVAILABLE
    }
}

/** Controls only the currently rendered drawable; it never touches the source image. */
internal class ImageAnimationController(
    private val render: (ImageAnimationPlayback) -> Unit,
) {

    private val state = ImageAnimationPlaybackState()
    private var animation: Animatable? = null

    val playback: ImageAnimationPlayback
        get() = state.playback

    fun attach(drawable: Drawable, startPaused: Boolean = false) {
        clear()
        val controllable = drawable.controllableAnimationOrNull() ?: return
        if (drawable is GifDrawable) {
            // Playback behavior is display-only and deliberately ignores a finite source loop count.
            drawable.setLoopCount(GifDrawable.LOOP_FOREVER)
        }
        animation = controllable
        state.attach(startPaused)
        applyRequestedPlayback()
    }

    fun toggle() {
        if (state.toggle()) applyRequestedPlayback()
    }

    /** Reasserts a user pause after Glide has processed an Activity lifecycle transition. */
    fun reapplyRequestedPlayback() {
        if (animation != null) applyRequestedPlayback()
    }

    fun clear() {
        animation?.let { current -> runCatching { current.stop() } }
        animation = null
        state.clear()
        render(state.playback)
    }

    private fun applyRequestedPlayback() {
        val current = animation ?: return
        val applied = runCatching {
            when (state.playback) {
                ImageAnimationPlayback.PLAYING -> current.start()
                ImageAnimationPlayback.PAUSED -> current.stop()
                ImageAnimationPlayback.UNAVAILABLE -> Unit
            }
        }.isSuccess
        if (!applied) {
            animation = null
            state.clear()
        }
        render(state.playback)
    }

    private fun Drawable.controllableAnimationOrNull(): Animatable? = when {
        this is GifDrawable && frameCount <= 1 -> null
        this is Animatable -> this
        else -> null
    }
}

internal fun MaterialButton.renderImageAnimationPlayback(playback: ImageAnimationPlayback) {
    isVisible = playback != ImageAnimationPlayback.UNAVAILABLE
    if (playback == ImageAnimationPlayback.PAUSED) {
        setIconResource(R.drawable.ic_play_arrow)
        contentDescription = context.getString(R.string.action_resume_animation)
    } else {
        setIconResource(R.drawable.ic_pause)
        contentDescription = context.getString(R.string.action_pause_animation)
    }
}
