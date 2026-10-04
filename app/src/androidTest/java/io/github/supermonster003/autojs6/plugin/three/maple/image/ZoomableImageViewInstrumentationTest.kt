@file:Suppress("DEPRECATION")

package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import android.os.SystemClock
import android.view.InputDevice
import android.view.MotionEvent
import android.view.ViewConfiguration
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ZoomableImageViewInstrumentationTest {

    @Test
    fun doubleTapTogglesZoomAroundTouchPoint() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            val context = InstrumentationRegistry.getInstrumentation().targetContext
            val bitmap = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888)
            try {
                val zoomInteractions = mutableListOf<Pair<Float, Boolean>>()
                val view = ZoomableImageView(context).apply {
                    setImageDrawable(BitmapDrawable(resources, bitmap))
                    layout(0, 0, VIEW_SIZE, VIEW_SIZE)
                    resetZoom()
                    onZoomInteraction = { zoom, gestureInProgress ->
                        zoomInteractions += zoom to gestureInProgress
                    }
                }
                val fittedMatrix = Matrix(view.imageMatrix)
                val fittedSourcePoint = sourcePointAt(view.imageMatrix, FOCUS_X, FOCUS_Y)

                assertTrue(view.toggleDoubleTapZoom(FOCUS_X, FOCUS_Y))
                assertEquals(
                    ImageZoomState.DOUBLE_TAP_ZOOM,
                    view.currentZoomForTesting,
                    ZOOM_TOLERANCE,
                )
                assertArrayEquals(
                    fittedSourcePoint,
                    sourcePointAt(view.imageMatrix, FOCUS_X, FOCUS_Y),
                    POINT_TOLERANCE,
                )
                assertEquals(
                    ImageZoomState.DOUBLE_TAP_ZOOM,
                    zoomInteractions.single().first,
                    ZOOM_TOLERANCE,
                )
                assertFalse(zoomInteractions.single().second)

                assertTrue(view.toggleDoubleTapZoom(FOCUS_X, FOCUS_Y))
                assertEquals(
                    ImageZoomState.MIN_ZOOM,
                    view.currentZoomForTesting,
                    ZOOM_TOLERANCE,
                )
                assertArrayEquals(
                    matrixValues(fittedMatrix),
                    matrixValues(view.imageMatrix),
                    MATRIX_TOLERANCE,
                )
                assertEquals(ImageZoomState.MIN_ZOOM, zoomInteractions.last().first, ZOOM_TOLERANCE)
                assertFalse(zoomInteractions.last().second)
            } finally {
                bitmap.recycle()
            }
        }
    }

    @Test
    fun pinchReportsLiveZoomUntilTheGestureEnds() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            val context = InstrumentationRegistry.getInstrumentation().targetContext
            val bitmap = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888)
            try {
                val zoomInteractions = mutableListOf<Pair<Float, Boolean>>()
                val view = ZoomableImageView(context).apply {
                    setImageDrawable(BitmapDrawable(resources, bitmap))
                    layout(0, 0, PINCH_VIEW_SIZE, PINCH_VIEW_SIZE)
                    resetZoom()
                    onZoomInteraction = { zoom, gestureInProgress ->
                        zoomInteractions += zoom to gestureInProgress
                    }
                }

                dispatchPinchOpen(view)

                assertTrue(zoomInteractions.any { (_, inProgress) -> inProgress })
                assertFalse(zoomInteractions.last().second)
                assertTrue(zoomInteractions.last().first > ImageZoomState.MIN_ZOOM)
                assertEquals(
                    view.currentZoomForTesting,
                    zoomInteractions.last().first,
                    ZOOM_TOLERANCE,
                )
            } finally {
                bitmap.recycle()
            }
        }
    }

    @Test
    fun gestureDetectorRoutesDoubleTapWithoutDispatchingSingleClick() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        lateinit var view: ZoomableImageView
        var bitmap: Bitmap? = null
        var clickCount = 0
        try {
            instrumentation.runOnMainSync {
                val context = instrumentation.targetContext
                bitmap = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888)
                view = ZoomableImageView(context).apply {
                    setImageDrawable(BitmapDrawable(resources, bitmap))
                    layout(0, 0, VIEW_SIZE, VIEW_SIZE)
                    resetZoom()
                    setOnClickListener { clickCount++ }
                }
                dispatchDoubleTap(view)
            }
            SystemClock.sleep(ViewConfiguration.getDoubleTapTimeout().toLong() + TAP_SETTLE_MILLIS)
            instrumentation.waitForIdleSync()
            instrumentation.runOnMainSync {
                assertEquals(
                    ImageZoomState.DOUBLE_TAP_ZOOM,
                    view.currentZoomForTesting,
                    ZOOM_TOLERANCE,
                )
                assertEquals(0, clickCount)
                dispatchDoubleTap(view)
            }
            SystemClock.sleep(ViewConfiguration.getDoubleTapTimeout().toLong() + TAP_SETTLE_MILLIS)
            instrumentation.waitForIdleSync()
            instrumentation.runOnMainSync {
                assertEquals(
                    ImageZoomState.MIN_ZOOM,
                    view.currentZoomForTesting,
                    ZOOM_TOLERANCE,
                )
                assertEquals(0, clickCount)
            }
        } finally {
            bitmap?.recycle()
        }
    }

    @Test
    fun horizontalFlingRoutesPagesOnlyAtBaseZoom() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            val context = InstrumentationRegistry.getInstrumentation().targetContext
            val bitmap = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888)
            try {
                val directions = mutableListOf<Int>()
                val view = ZoomableImageView(context).apply {
                    setImageDrawable(BitmapDrawable(resources, bitmap))
                    layout(0, 0, VIEW_SIZE, VIEW_SIZE)
                    resetZoom()
                    onPageSwipe = directions::add
                }

                assertTrue(view.routePageFling(distanceX = -360f, distanceY = 0f, velocityX = -3_000f))
                assertTrue(view.routePageFling(distanceX = 360f, distanceY = 0f, velocityX = 3_000f))
                assertEquals(
                    listOf(ZoomableImageView.DIRECTION_NEXT, ZoomableImageView.DIRECTION_PREVIOUS),
                    directions,
                )

                assertTrue(view.toggleDoubleTapZoom(FOCUS_X, FOCUS_Y))
                assertTrue(
                    !view.routePageFling(distanceX = -360f, distanceY = 0f, velocityX = -3_000f),
                )
                assertEquals(2, directions.size)
            } finally {
                bitmap.recycle()
            }
        }
    }

    @Test
    fun clockwiseRotationUsesFitCenterQuarterTurnsAndResetRestoresOriginal() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            val context = InstrumentationRegistry.getInstrumentation().targetContext
            val bitmap = Bitmap.createBitmap(RECTANGULAR_WIDTH, RECTANGULAR_HEIGHT, Bitmap.Config.ARGB_8888)
            try {
                val view = ZoomableImageView(context).apply {
                    setImageDrawable(BitmapDrawable(resources, bitmap))
                    layout(0, 0, RECTANGULAR_VIEW_WIDTH, RECTANGULAR_VIEW_HEIGHT)
                    resetZoom()
                }
                val originalMatrix = Matrix(view.imageMatrix)
                assertRectEquals(
                    RectF(0f, 37.5f, 400f, 262.5f),
                    mappedDrawableBounds(view, bitmap),
                )

                val expected = listOf(
                    90 to RectF(115.625f, 0f, 284.375f, 300f),
                    180 to RectF(0f, 37.5f, 400f, 262.5f),
                    270 to RectF(115.625f, 0f, 284.375f, 300f),
                    0 to RectF(0f, 37.5f, 400f, 262.5f),
                )
                expected.forEach { (degrees, bounds) ->
                    assertTrue(view.rotateClockwise90())
                    assertEquals(degrees, view.currentRotationDegreesForTesting)
                    assertEquals(ImageZoomState.MIN_ZOOM, view.currentZoomForTesting, ZOOM_TOLERANCE)
                    assertRectEquals(bounds, mappedDrawableBounds(view, bitmap))
                }
                assertArrayEquals(
                    matrixValues(originalMatrix),
                    matrixValues(view.imageMatrix),
                    MATRIX_TOLERANCE,
                )

                assertTrue(view.rotateClockwise90())
                assertEquals(90, view.currentRotationDegreesForTesting)
                view.resetZoom()
                assertEquals(0, view.currentRotationDegreesForTesting)
                assertEquals(ImageZoomState.MIN_ZOOM, view.currentZoomForTesting, ZOOM_TOLERANCE)
                assertArrayEquals(
                    matrixValues(originalMatrix),
                    matrixValues(view.imageMatrix),
                    MATRIX_TOLERANCE,
                )
            } finally {
                bitmap.recycle()
            }
        }
    }

    @Test
    fun rotatedZoomPreservesFocusAndConstrainsEveryPanEdge() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            val context = InstrumentationRegistry.getInstrumentation().targetContext
            val bitmap = Bitmap.createBitmap(RECTANGULAR_WIDTH, RECTANGULAR_HEIGHT, Bitmap.Config.ARGB_8888)
            try {
                val view = ZoomableImageView(context).apply {
                    setImageDrawable(BitmapDrawable(resources, bitmap))
                    layout(0, 0, RECTANGULAR_VIEW_WIDTH, RECTANGULAR_VIEW_HEIGHT)
                    resetZoom()
                }
                assertTrue(view.rotateClockwise90())
                val fittedSourcePoint = sourcePointAt(view.imageMatrix, RECTANGULAR_FOCUS_X, RECTANGULAR_FOCUS_Y)

                assertTrue(view.toggleDoubleTapZoom(RECTANGULAR_FOCUS_X, RECTANGULAR_FOCUS_Y))
                assertEquals(
                    ImageZoomState.DOUBLE_TAP_ZOOM,
                    view.currentZoomForTesting,
                    ZOOM_TOLERANCE,
                )
                assertArrayEquals(
                    fittedSourcePoint,
                    sourcePointAt(view.imageMatrix, RECTANGULAR_FOCUS_X, RECTANGULAR_FOCUS_Y),
                    POINT_TOLERANCE,
                )

                dispatchDrag(view, RECTANGULAR_FOCUS_X, RECTANGULAR_FOCUS_Y, 1_200f, 1_150f)
                val topLeftBounds = mappedDrawableBounds(view, bitmap)
                assertEquals(0f, topLeftBounds.left, BOUNDS_TOLERANCE)
                assertEquals(0f, topLeftBounds.top, BOUNDS_TOLERANCE)
                assertTrue(topLeftBounds.right > RECTANGULAR_VIEW_WIDTH)
                assertTrue(topLeftBounds.bottom > RECTANGULAR_VIEW_HEIGHT)

                dispatchDrag(view, RECTANGULAR_FOCUS_X, RECTANGULAR_FOCUS_Y, -1_800f, -1_850f)
                val bottomRightBounds = mappedDrawableBounds(view, bitmap)
                assertEquals(
                    RECTANGULAR_VIEW_WIDTH.toFloat(),
                    bottomRightBounds.right,
                    BOUNDS_TOLERANCE,
                )
                assertEquals(
                    RECTANGULAR_VIEW_HEIGHT.toFloat(),
                    bottomRightBounds.bottom,
                    BOUNDS_TOLERANCE,
                )
                assertTrue(bottomRightBounds.left < 0f)
                assertTrue(bottomRightBounds.top < 0f)
            } finally {
                bitmap.recycle()
            }
        }
    }

    @Test
    fun rotationKeepsMagnificationAndTheViewportCenterAnchor() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            val context = InstrumentationRegistry.getInstrumentation().targetContext
            val bitmap = Bitmap.createBitmap(RECTANGULAR_WIDTH, RECTANGULAR_HEIGHT, Bitmap.Config.ARGB_8888)
            try {
                val view = ZoomableImageView(context).apply {
                    setImageDrawable(BitmapDrawable(resources, bitmap))
                    layout(0, 0, RECTANGULAR_VIEW_WIDTH, RECTANGULAR_VIEW_HEIGHT)
                    resetZoom()
                }
                assertTrue(view.toggleDoubleTapZoom(RECTANGULAR_FOCUS_X, RECTANGULAR_FOCUS_Y))
                val sourceAnchor = sourcePointAt(
                    view.imageMatrix,
                    RECTANGULAR_FOCUS_X,
                    RECTANGULAR_FOCUS_Y,
                )

                assertTrue(view.rotateClockwise90())

                assertEquals(90, view.currentRotationDegreesForTesting)
                assertEquals(
                    ImageZoomState.DOUBLE_TAP_ZOOM,
                    view.currentZoomForTesting,
                    ZOOM_TOLERANCE,
                )
                assertArrayEquals(
                    sourceAnchor,
                    sourcePointAt(view.imageMatrix, RECTANGULAR_FOCUS_X, RECTANGULAR_FOCUS_Y),
                    POINT_TOLERANCE,
                )
            } finally {
                bitmap.recycle()
            }
        }
    }

    @Test
    fun restoredRotationBeforeLayoutSurvivesTheFirstFitCalculation() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            val context = InstrumentationRegistry.getInstrumentation().targetContext
            val bitmap = Bitmap.createBitmap(RECTANGULAR_WIDTH, RECTANGULAR_HEIGHT, Bitmap.Config.ARGB_8888)
            try {
                val view = ZoomableImageView(context).apply {
                    setImageDrawable(BitmapDrawable(resources, bitmap))
                    restoreRotationQuarterTurns(3)
                    layout(0, 0, RECTANGULAR_VIEW_WIDTH, RECTANGULAR_VIEW_HEIGHT)
                }

                assertEquals(270, view.currentRotationDegreesForTesting)
                assertEquals(ImageZoomState.MIN_ZOOM, view.currentZoomForTesting, ZOOM_TOLERANCE)
                assertRectEquals(
                    RectF(115.625f, 0f, 284.375f, 300f),
                    mappedDrawableBounds(view, bitmap),
                )
            } finally {
                bitmap.recycle()
            }
        }
    }

    private fun dispatchDoubleTap(view: ZoomableImageView) {
        val firstDown = SystemClock.uptimeMillis()
        dispatch(view, firstDown, firstDown, MotionEvent.ACTION_DOWN)
        dispatch(view, firstDown, firstDown + TAP_DURATION_MILLIS, MotionEvent.ACTION_UP)
        val secondDown = firstDown + DOUBLE_TAP_GAP_MILLIS
        dispatch(view, secondDown, secondDown, MotionEvent.ACTION_DOWN)
        dispatch(view, secondDown, secondDown + TAP_DURATION_MILLIS, MotionEvent.ACTION_UP)
    }

    private fun dispatchPinchOpen(view: ZoomableImageView) {
        val downTime = SystemClock.uptimeMillis()
        dispatchPointers(
            view,
            downTime,
            downTime,
            MotionEvent.ACTION_DOWN,
            floatArrayOf(590f),
            floatArrayOf(590f),
        )
        dispatchPointers(
            view,
            downTime,
            downTime + PINCH_STEP_MILLIS,
            MotionEvent.ACTION_POINTER_DOWN or
                (1 shl MotionEvent.ACTION_POINTER_INDEX_SHIFT),
            floatArrayOf(590f, 610f),
            floatArrayOf(590f, 610f),
        )
        dispatchPointers(
            view,
            downTime,
            downTime + PINCH_STEP_MILLIS * 2,
            MotionEvent.ACTION_MOVE,
            floatArrayOf(250f, 950f),
            floatArrayOf(250f, 950f),
        )
        dispatchPointers(
            view,
            downTime,
            downTime + PINCH_STEP_MILLIS * 3,
            MotionEvent.ACTION_MOVE,
            floatArrayOf(50f, 1_150f),
            floatArrayOf(50f, 1_150f),
        )
        dispatchPointers(
            view,
            downTime,
            downTime + PINCH_STEP_MILLIS * 4,
            MotionEvent.ACTION_MOVE,
            floatArrayOf(0f, 1_200f),
            floatArrayOf(0f, 1_200f),
        )
        dispatchPointers(
            view,
            downTime,
            downTime + PINCH_STEP_MILLIS * 5,
            MotionEvent.ACTION_POINTER_UP or
                (1 shl MotionEvent.ACTION_POINTER_INDEX_SHIFT),
            floatArrayOf(0f, 1_200f),
            floatArrayOf(0f, 1_200f),
        )
        dispatchPointers(
            view,
            downTime,
            downTime + PINCH_STEP_MILLIS * 6,
            MotionEvent.ACTION_UP,
            floatArrayOf(0f),
            floatArrayOf(0f),
        )
    }

    private fun dispatchPointers(
        view: ZoomableImageView,
        downTime: Long,
        eventTime: Long,
        action: Int,
        xCoordinates: FloatArray,
        yCoordinates: FloatArray,
    ) {
        val properties = Array(xCoordinates.size) { index ->
            MotionEvent.PointerProperties().apply {
                id = index
                toolType = MotionEvent.TOOL_TYPE_FINGER
            }
        }
        val coordinates = Array(xCoordinates.size) { index ->
            MotionEvent.PointerCoords().apply {
                x = xCoordinates[index]
                y = yCoordinates[index]
                pressure = 1f
                size = 1f
            }
        }
        val event = MotionEvent.obtain(
            downTime,
            eventTime,
            action,
            properties.size,
            properties,
            coordinates,
            0,
            0,
            1f,
            1f,
            0,
            0,
            InputDevice.SOURCE_TOUCHSCREEN,
            0,
        )
        try {
            assertTrue(view.onTouchEvent(event))
        } finally {
            event.recycle()
        }
    }

    private fun dispatchDrag(
        view: ZoomableImageView,
        startX: Float,
        startY: Float,
        endX: Float,
        endY: Float,
    ) {
        val downTime = SystemClock.uptimeMillis()
        dispatch(view, downTime, downTime, MotionEvent.ACTION_DOWN, startX, startY)
        dispatch(
            view,
            downTime,
            downTime + DRAG_DURATION_MILLIS,
            MotionEvent.ACTION_MOVE,
            endX,
            endY,
        )
        dispatch(
            view,
            downTime,
            downTime + DRAG_DURATION_MILLIS * 2,
            MotionEvent.ACTION_UP,
            endX,
            endY,
        )
    }

    private fun dispatch(
        view: ZoomableImageView,
        downTime: Long,
        eventTime: Long,
        action: Int,
        x: Float = FOCUS_X,
        y: Float = FOCUS_Y,
    ) {
        val event = MotionEvent.obtain(downTime, eventTime, action, x, y, 0)
        try {
            assertTrue(view.onTouchEvent(event))
        } finally {
            event.recycle()
        }
    }

    private fun sourcePointAt(matrix: Matrix, viewX: Float, viewY: Float): FloatArray {
        val inverse = Matrix()
        assertTrue(matrix.invert(inverse))
        return floatArrayOf(viewX, viewY).also(inverse::mapPoints)
    }

    private fun matrixValues(matrix: Matrix): FloatArray =
        FloatArray(9).also(matrix::getValues)

    private fun mappedDrawableBounds(view: ZoomableImageView, bitmap: Bitmap): RectF =
        RectF(0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat()).also {
            view.imageMatrix.mapRect(it)
        }

    private fun assertRectEquals(expected: RectF, actual: RectF) {
        assertEquals(expected.left, actual.left, BOUNDS_TOLERANCE)
        assertEquals(expected.top, actual.top, BOUNDS_TOLERANCE)
        assertEquals(expected.right, actual.right, BOUNDS_TOLERANCE)
        assertEquals(expected.bottom, actual.bottom, BOUNDS_TOLERANCE)
    }

    companion object {
        private const val VIEW_SIZE = 400
        private const val PINCH_VIEW_SIZE = 1_200
        private const val RECTANGULAR_WIDTH = 320
        private const val RECTANGULAR_HEIGHT = 180
        private const val RECTANGULAR_VIEW_WIDTH = 400
        private const val RECTANGULAR_VIEW_HEIGHT = 300
        private const val FOCUS_X = 140f
        private const val FOCUS_Y = 180f
        private const val RECTANGULAR_FOCUS_X = 200f
        private const val RECTANGULAR_FOCUS_Y = 150f
        private const val ZOOM_TOLERANCE = 0.0001f
        private const val POINT_TOLERANCE = 0.01f
        private const val MATRIX_TOLERANCE = 0.0001f
        private const val BOUNDS_TOLERANCE = 0.02f
        private const val TAP_DURATION_MILLIS = 20L
        private const val DOUBLE_TAP_GAP_MILLIS = 80L
        private const val DRAG_DURATION_MILLIS = 40L
        private const val PINCH_STEP_MILLIS = 40L
        private const val TAP_SETTLE_MILLIS = 50L
    }
}
