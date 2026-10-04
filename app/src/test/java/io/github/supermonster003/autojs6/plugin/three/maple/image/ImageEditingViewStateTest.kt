package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Rect
import android.view.MotionEvent
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
            assertEquals(CropAspectRatioPreset.FREE, restored.cropAspectRatioPreset())
        } finally {
            bitmap.recycle()
        }
    }

    @Test
    fun cropAspectRatioSurvivesViewRecreation() {
        val bitmap = Bitmap.createBitmap(120, 80, Bitmap.Config.ARGB_8888)
        try {
            val original = createView(bitmap).apply {
                beginCrop(CropAspectRatioPreset.LANDSCAPE_16_9)
            }
            val restored = createView(bitmap).apply {
                restoreCanvasState(original.captureCanvasState())
            }
            val cropState = restored.captureCanvasState() as ImageEditingView.CanvasState.Crop
            assertEquals(CropAspectRatioPreset.LANDSCAPE_16_9, cropState.aspectRatioPreset)
            assertEquals(60f, cropState.selection.centerX(), 0.001f)
            assertEquals(40f, cropState.selection.centerY(), 0.001f)
            assertEquals(16f / 9f, cropState.selection.width() / cropState.selection.height(), 0.001f)
        } finally {
            bitmap.recycle()
        }
    }

    @Test
    fun cropSelectionKeepsAtLeastOnePixelForExtremeAspectRatio() {
        val bitmap = Bitmap.createBitmap(1, 1_000, Bitmap.Config.ARGB_8888)
        try {
            val selection = createView(bitmap).apply {
                beginCrop(CropAspectRatioPreset.LANDSCAPE_16_9)
            }.cropSelection()

            requireNotNull(selection)
            assertEquals(1, selection.width())
            assertEquals(1, selection.height())
            assertTrue(selection.left >= 0)
            assertTrue(selection.top >= 0)
            assertTrue(selection.right <= bitmap.width)
            assertTrue(selection.bottom <= bitmap.height)
        } finally {
            bitmap.recycle()
        }
    }

    @Test
    fun rotationPreviewSurvivesViewRecreation() {
        val bitmap = Bitmap.createBitmap(120, 80, Bitmap.Config.ARGB_8888)
        try {
            val original = createView(bitmap).apply {
                beginRotation(-17f)
            }
            val restored = createView(bitmap).apply {
                restoreCanvasState(original.captureCanvasState())
            }
            val rotationState = restored.captureCanvasState() as ImageEditingView.CanvasState.Rotation
            assertEquals(-17f, rotationState.degrees, 0f)
            assertEquals(-17f, restored.rotationPreviewDegrees(), 0f)
        } finally {
            bitmap.recycle()
        }
    }

    @Test
    fun brushFamilySettingsAndMosaicStrokeSurviveViewRecreation() {
        val bitmap = Bitmap.createBitmap(120, 80, Bitmap.Config.ARGB_8888)
        try {
            val original = createView(bitmap).apply {
                beginBrush(
                    tool = BrushTool.MOSAIC,
                    color = 0xFF123456.toInt(),
                    widthOnScreen = 20f,
                    mosaicBlockSizeOnScreen = 40f,
                )
            }
            original.dispatchTouch(MotionEvent.ACTION_DOWN, 300f, 200f)
            original.dispatchTouch(MotionEvent.ACTION_UP, 300f, 200f)

            val restored = createView(bitmap).apply {
                restoreCanvasState(original.captureCanvasState())
            }
            val state = restored.captureCanvasState() as ImageEditingView.CanvasState.Brush
            assertEquals(BrushTool.MOSAIC, state.tool)
            assertEquals(20f, state.widthOnScreen, 0f)
            assertEquals(40f, state.mosaicBlockSizeOnScreen, 0f)
            assertEquals(1, state.strokes.size)
            assertEquals(BrushTool.MOSAIC, state.strokes.single().tool)
            assertEquals(4f, state.strokes.single().width, 0.001f)
            assertEquals(8, state.strokes.single().mosaicBlockSize)
        } finally {
            bitmap.recycle()
        }
    }

    @Test
    fun textStateSurvivesViewRecreation() {
        val spec = ImageEditingEngine.TextSpec(
            text = "restored\nmultiline",
            color = 0xFF112233.toInt(),
            size = 24f,
            centerX = 30f,
            centerY = 20f,
            outlineEnabled = true,
            shadowEnabled = true,
            rotationDegrees = -37f,
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

    private fun View.dispatchTouch(action: Int, x: Float, y: Float) {
        val event = MotionEvent.obtain(0L, 0L, action, x, y, 0)
        try {
            dispatchTouchEvent(event)
        } finally {
            event.recycle()
        }
    }

    private fun application(): Application = RuntimeEnvironment.getApplication()
}
