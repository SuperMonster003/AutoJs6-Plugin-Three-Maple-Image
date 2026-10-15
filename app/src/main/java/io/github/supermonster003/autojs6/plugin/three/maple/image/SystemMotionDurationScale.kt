package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.animation.ValueAnimator
import android.content.Context
import android.os.Build
import android.provider.Settings
import androidx.compose.ui.MotionDurationScale

/**
 * The system animation duration scale ("Remove animations" makes it 0) for Compose animations run outside a
 * composition, in a scope of the app's own: only the window's recomposer adds this to its coroutines, so without it
 * such an animation always takes its full time (ROADMAP 9.4). Read on every frame, like the recomposer's own.
 */
internal class SystemMotionDurationScale(private val context: Context) : MotionDurationScale {
    override val scaleFactor: Float
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ValueAnimator.getDurationScale()
        } else {
            Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f)
        }
}
