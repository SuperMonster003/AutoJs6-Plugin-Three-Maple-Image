package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.graphics.drawable.ColorDrawable
import android.view.ContextThemeWrapper
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.google.android.material.button.MaterialButton
import java.io.File
import java.util.concurrent.TimeUnit
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ImageAnimationControllerInstrumentationTest {

    @Test
    fun animatedGifShowsControlAndTogglesPauseAndResume() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val file = File(context.cacheDir, "image-viewer-animation-control.gif")
        AnimatedGifTestFixture.writeTo(file)
        val future = Glide.with(context).asGif().load(file).submit()
        val drawable = future.get(10L, TimeUnit.SECONDS)
        try {
            assertEquals(2, drawable.frameCount)
            instrumentation.runOnMainSync {
                val button = controlButton()
                val controller = ImageAnimationController(button::renderImageAnimationPlayback)

                controller.attach(drawable)
                assertEquals(View.VISIBLE, button.visibility)
                assertEquals(context.getString(R.string.action_pause_animation), button.contentDescription.toString())
                assertEquals(ImageAnimationPlayback.PLAYING, controller.playback)
                assertTrue(drawable.isRunning)

                controller.toggle()
                assertEquals(context.getString(R.string.action_resume_animation), button.contentDescription.toString())
                assertEquals(ImageAnimationPlayback.PAUSED, controller.playback)
                assertFalse(drawable.isRunning)

                controller.toggle()
                assertEquals(context.getString(R.string.action_pause_animation), button.contentDescription.toString())
                assertEquals(ImageAnimationPlayback.PLAYING, controller.playback)
                assertTrue(drawable.isRunning)
                controller.clear()
            }
        } finally {
            Glide.with(context).clear(future)
            file.delete()
        }
    }

    @Test
    fun staticDrawableKeepsAnimationControlHidden() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        instrumentation.runOnMainSync {
            val button = controlButton()
            val controller = ImageAnimationController(button::renderImageAnimationPlayback)

            controller.attach(ColorDrawable(0xFF336699.toInt()))

            assertEquals(View.GONE, button.visibility)
            assertEquals(ImageAnimationPlayback.UNAVAILABLE, controller.playback)
        }
    }

    @Test
    fun singleFrameGifIsTreatedAsAStaticImage() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val file = File(context.cacheDir, "image-viewer-single-frame.gif")
        AnimatedGifTestFixture.writeSingleFrameTo(file)
        val future = Glide.with(context).asGif().load(file).submit()
        val drawable = future.get(10L, TimeUnit.SECONDS)
        try {
            assertEquals(1, drawable.frameCount)
            instrumentation.runOnMainSync {
                val button = controlButton()
                val controller = ImageAnimationController(button::renderImageAnimationPlayback)

                controller.attach(drawable)

                assertEquals(View.GONE, button.visibility)
                assertEquals(ImageAnimationPlayback.UNAVAILABLE, controller.playback)
                assertFalse(drawable.isRunning)
            }
        } finally {
            Glide.with(context).clear(future)
            file.delete()
        }
    }

    private fun controlButton(): MaterialButton {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        return MaterialButton(ContextThemeWrapper(context, R.style.AppTheme)).apply {
            visibility = View.GONE
        }
    }
}
