package io.github.supermonster003.autojs6.plugin.imagetools

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Rect
import android.view.View
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ImageEditingViewStateTest {

    @Test
    fun cropStateSurvivesViewRecreation() {
        val bitmap = Bitmap.createBitmap(120, 80, Bitmap.Config.ARGB_8888)
        try {
            val original = createView(bitmap).apply { beginCrop() }
            val restored = createView(bitmap).apply {
                restoreCanvasState(original.captureCanvasState())
            }
            assertEquals(Rect(0, 0, 120, 80), restored.cropSelection())
        } finally {
            bitmap.recycle()
        }
    }

    @Test
    fun textStateSurvivesViewRecreation() {
        val spec = ImageEditingEngine.TextSpec(
            text = "restored",
            color = 0xFF112233.toInt(),
            size = 24f,
            centerX = 30f,
            centerY = 20f,
        )
        val original = ImageEditingView(application()).apply { beginText(spec) }
        val restored = ImageEditingView(application()).apply {
            restoreCanvasState(original.captureCanvasState())
        }
        assertTrue(restored.captureCanvasState() is ImageEditingView.CanvasState.Text)
        assertEquals(spec, restored.textSpec())
    }

    private fun createView(bitmap: Bitmap) = ImageEditingView(application()).apply {
        measure(
            View.MeasureSpec.makeMeasureSpec(600, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(400, View.MeasureSpec.EXACTLY),
        )
        layout(0, 0, measuredWidth, measuredHeight)
        setBitmap(bitmap)
    }

    private fun application(): Application = RuntimeEnvironment.getApplication()
}
