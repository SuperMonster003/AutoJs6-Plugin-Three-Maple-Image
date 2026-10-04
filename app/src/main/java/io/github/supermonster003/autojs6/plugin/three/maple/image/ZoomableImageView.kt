package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Matrix
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import androidx.appcompat.widget.AppCompatImageView
import io.github.supermonster003.autojs6.plugin.three.maple.image.ImageZoomState.MAX_ZOOM
import io.github.supermonster003.autojs6.plugin.three.maple.image.ImageZoomState.MIN_ZOOM
import io.github.supermonster003.autojs6.plugin.three.maple.image.ImageZoomState.ZOOM_EPSILON
import kotlin.math.abs

/**
 * An image surface with focal-point pinch zoom, double-tap zoom, panning and page swipes at 1x.
 *
 * Glide normalizes the drawable's EXIF source orientation before it reaches this view. The
 * displayed image is then transformed from the fit-center bounds for its current user-requested
 * 90-degree orientation. This keeps scale, focal points and pan constraints in lockstep with the
 * fingers without applying the source orientation twice.
 */
class ZoomableImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : AppCompatImageView(context, attrs, defStyleAttr) {

    var onPageSwipe: ((direction: Int) -> Unit)? = null
    var onZoomInteraction: ((zoom: Float, gestureInProgress: Boolean) -> Unit)? = null

    private val baseMatrix = Matrix()
    private val displayMatrix = Matrix()
    private val drawableRect = RectF()
    private val mappedDrawableRect = RectF()
    private val tiledViewportRect = RectF()

    private var zoom = MIN_ZOOM
    private var rotationQuarterTurns = ImageRotationState.ORIGINAL_QUARTER_TURNS
    private var configuredDrawable: Drawable? = null
    private var configuredWidth = 0
    private var configuredHeight = 0
    private var observedMaximumBitmapWidth = 0
    private var observedMaximumBitmapHeight = 0

    private var activePointerId = MotionEvent.INVALID_POINTER_ID
    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private var lastScaleFocusX = 0f
    private var lastScaleFocusY = 0f
    private var gestureHadMultiplePointers = false

    private val scaleDetector = ScaleGestureDetector(
        context,
        object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
                ensureBaseMatrix()
                if (drawable == null) return false
                gestureHadMultiplePointers = true
                lastScaleFocusX = detector.focusX
                lastScaleFocusY = detector.focusY
                onZoomInteraction?.invoke(zoom, true)
                return true
            }

            override fun onScale(detector: ScaleGestureDetector): Boolean {
                val reportedFactor = detector.scaleFactor
                if (!reportedFactor.isFinite() || reportedFactor <= 0f) return false

                val targetZoom = (zoom * reportedFactor).coerceIn(MIN_ZOOM, MAX_ZOOM)
                val appliedFactor = targetZoom / zoom

                // A pinch can translate as well as scale. Track the centroid explicitly so the
                // image follows two fingers even when they move together while changing span.
                displayMatrix.postTranslate(
                    detector.focusX - lastScaleFocusX,
                    detector.focusY - lastScaleFocusY,
                )
                displayMatrix.postScale(
                    appliedFactor,
                    appliedFactor,
                    detector.focusX,
                    detector.focusY,
                )
                zoom = targetZoom
                lastScaleFocusX = detector.focusX
                lastScaleFocusY = detector.focusY

                if (zoom <= MIN_ZOOM + ZOOM_EPSILON) {
                    zoom = MIN_ZOOM
                    displayMatrix.set(baseMatrix)
                    imageMatrix = displayMatrix
                } else {
                    constrainDisplayMatrix()
                }
                onZoomInteraction?.invoke(zoom, true)
                return true
            }

            override fun onScaleEnd(detector: ScaleGestureDetector) {
                if (zoom <= MIN_ZOOM + ZOOM_EPSILON) resetToCurrentFit()
                onZoomInteraction?.invoke(zoom, false)
            }
        },
    )

    private val gestureDetector = GestureDetector(
        context,
        object : GestureDetector.SimpleOnGestureListener() {
            override fun onDown(event: MotionEvent): Boolean = true

            override fun onSingleTapConfirmed(event: MotionEvent): Boolean {
                performClick()
                return true
            }

            override fun onDoubleTap(event: MotionEvent): Boolean {
                return toggleDoubleTapZoom(event.x, event.y)
            }

            override fun onFling(
                down: MotionEvent?,
                up: MotionEvent,
                velocityX: Float,
                velocityY: Float,
            ): Boolean {
                down ?: return false
                return routePageFling(
                    distanceX = up.x - down.x,
                    distanceY = up.y - down.y,
                    velocityX = velocityX,
                )
            }
        },
    )

    init {
        isClickable = true
        scaleType = ScaleType.MATRIX
    }

    override fun onDraw(canvas: android.graphics.Canvas) {
        observedMaximumBitmapWidth = canvas.maximumBitmapWidth.takeIf { it > 0 }
            ?: observedMaximumBitmapWidth
        observedMaximumBitmapHeight = canvas.maximumBitmapHeight.takeIf { it > 0 }
            ?: observedMaximumBitmapHeight
        ensureBaseMatrix()
        tiledViewportRect.set(
            paddingLeft.toFloat(),
            paddingTop.toFloat(),
            (width - paddingRight).toFloat(),
            (height - paddingBottom).toFloat(),
        )
        (drawable as? TiledImageDrawable)?.updateViewport(
            displayMatrix = displayMatrix,
            contentBounds = tiledViewportRect,
        )
        super.onDraw(canvas)
    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)
        configuredWidth = 0
        configuredHeight = 0
        ensureBaseMatrix()
    }

    // GestureDetector dispatches confirmed single taps through performClick().
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.actionMasked == MotionEvent.ACTION_DOWN) {
            gestureHadMultiplePointers = false
            activePointerId = event.getPointerId(0)
            lastTouchX = event.x
            lastTouchY = event.y
            parent?.requestDisallowInterceptTouchEvent(true)
        } else if (
            event.pointerCount > 1 ||
            event.actionMasked == MotionEvent.ACTION_POINTER_DOWN ||
            event.actionMasked == MotionEvent.ACTION_POINTER_UP
        ) {
            gestureHadMultiplePointers = true
        }

        scaleDetector.onTouchEvent(event)
        gestureDetector.onTouchEvent(event)

        when (event.actionMasked) {
            MotionEvent.ACTION_MOVE -> {
                if (!scaleDetector.isInProgress && event.pointerCount == 1 && zoom > MIN_ZOOM) {
                    val pointerIndex = event.findPointerIndex(activePointerId).takeIf { it >= 0 } ?: 0
                    val touchX = event.getX(pointerIndex)
                    val touchY = event.getY(pointerIndex)
                    displayMatrix.postTranslate(touchX - lastTouchX, touchY - lastTouchY)
                    constrainDisplayMatrix()
                    lastTouchX = touchX
                    lastTouchY = touchY
                }
            }

            MotionEvent.ACTION_POINTER_UP -> {
                val liftedPointerId = event.getPointerId(event.actionIndex)
                if (liftedPointerId == activePointerId) {
                    val replacementIndex = if (event.actionIndex == 0) 1 else 0
                    activePointerId = event.getPointerId(replacementIndex)
                    lastTouchX = event.getX(replacementIndex)
                    lastTouchY = event.getY(replacementIndex)
                } else {
                    val activeIndex = event.findPointerIndex(activePointerId)
                    if (activeIndex >= 0) {
                        lastTouchX = event.getX(activeIndex)
                        lastTouchY = event.getY(activeIndex)
                    }
                }
            }

            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL,
            -> {
                activePointerId = MotionEvent.INVALID_POINTER_ID
                parent?.requestDisallowInterceptTouchEvent(false)
            }
        }
        return true
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    fun resetZoom() {
        // The EXIF-normalized source orientation is baked into the drawable; reset only the
        // user-requested rotation and zoom maintained by this view.
        rotationQuarterTurns = ImageRotationState.ORIGINAL_QUARTER_TURNS
        resetToCurrentFit()
    }

    fun rotateClockwise90(): Boolean {
        ensureBaseMatrix()
        val currentDrawable = drawable?.takeIf { it === configuredDrawable } ?: return false
        val previousZoom = zoom
        val contentCenter = floatArrayOf(contentCenterX, contentCenterY)
        val sourceAnchor = contentCenter.copyOf()
        val inverse = Matrix()
        val hasSourceAnchor = displayMatrix.invert(inverse)
        if (hasSourceAnchor) inverse.mapPoints(sourceAnchor)

        val previousRotationQuarterTurns = rotationQuarterTurns
        rotationQuarterTurns = ImageRotationState.clockwise(rotationQuarterTurns)
        if (!configureBaseMatrix(currentDrawable)) {
            rotationQuarterTurns = previousRotationQuarterTurns
            return false
        }

        if (previousZoom > MIN_ZOOM + ZOOM_EPSILON) {
            displayMatrix.postScale(
                previousZoom,
                previousZoom,
                contentCenter[0],
                contentCenter[1],
            )
            zoom = previousZoom
            if (hasSourceAnchor) {
                val mappedAnchor = sourceAnchor.copyOf().also(displayMatrix::mapPoints)
                displayMatrix.postTranslate(
                    contentCenter[0] - mappedAnchor[0],
                    contentCenter[1] - mappedAnchor[1],
                )
            }
            constrainDisplayMatrix()
        }
        return true
    }

    internal fun toggleDoubleTapZoom(focusX: Float, focusY: Float): Boolean {
        ensureBaseMatrix()
        if (drawable == null) return false

        val targetZoom = ImageZoomState.doubleTapTarget(zoom)
        if (targetZoom == MIN_ZOOM) {
            resetToCurrentFit()
            onZoomInteraction?.invoke(zoom, false)
            return true
        }

        val appliedFactor = targetZoom / zoom
        displayMatrix.postScale(appliedFactor, appliedFactor, focusX, focusY)
        zoom = targetZoom
        constrainDisplayMatrix()
        onZoomInteraction?.invoke(zoom, false)
        return true
    }

    internal val currentZoomForTesting: Float
        get() = zoom

    internal val currentRotationQuarterTurns: Int
        get() = rotationQuarterTurns

    internal val currentRotationDegreesForTesting: Int
        get() = ImageRotationState.degrees(rotationQuarterTurns)

    internal fun largeImageLimits(memoryClassMebibytes: Int): ImageBitmapLimits =
        ImageLargeImagePolicy.runtimeLimits(
            memoryClassMebibytes = memoryClassMebibytes,
            observedMaximumBitmapWidth = observedMaximumBitmapWidth,
            observedMaximumBitmapHeight = observedMaximumBitmapHeight,
        )

    internal fun restoreRotationQuarterTurns(quarterTurns: Int) {
        val normalized = ImageRotationState.normalize(quarterTurns)
        if (normalized == rotationQuarterTurns) return
        rotationQuarterTurns = normalized
        resetToCurrentFit()
    }

    internal fun routePageFling(
        distanceX: Float,
        distanceY: Float,
        velocityX: Float,
    ): Boolean {
        if (zoom > MIN_ZOOM + ZOOM_EPSILON || gestureHadMultiplePointers) return false
        if (
            abs(distanceX) < swipeDistanceThreshold ||
            abs(velocityX) < swipeVelocityThreshold ||
            abs(distanceX) <= abs(distanceY)
        ) {
            return false
        }
        onPageSwipe?.invoke(if (distanceX < 0f) DIRECTION_NEXT else DIRECTION_PREVIOUS)
        return true
    }

    private fun ensureBaseMatrix() {
        val currentDrawable = drawable ?: return
        val drawableChanged = currentDrawable !== configuredDrawable
        if (
            !drawableChanged &&
            width == configuredWidth &&
            height == configuredHeight
        ) {
            return
        }
        if (drawableChanged) {
            rotationQuarterTurns = ImageRotationState.ORIGINAL_QUARTER_TURNS
        }
        configureBaseMatrix(currentDrawable)
    }

    private fun configureBaseMatrix(currentDrawable: Drawable): Boolean {
        val drawableWidth = currentDrawable.intrinsicWidth
        val drawableHeight = currentDrawable.intrinsicHeight
        val contentWidth = width - paddingLeft - paddingRight
        val contentHeight = height - paddingTop - paddingBottom
        if (drawableWidth <= 0 || drawableHeight <= 0 || contentWidth <= 0 || contentHeight <= 0) {
            configuredDrawable = currentDrawable
            configuredWidth = 0
            configuredHeight = 0
            return false
        }

        val layout = ImageFitCenterTransform.calculate(
            sourceWidth = drawableWidth.toFloat(),
            sourceHeight = drawableHeight.toFloat(),
            contentLeft = paddingLeft.toFloat(),
            contentTop = paddingTop.toFloat(),
            contentWidth = contentWidth.toFloat(),
            contentHeight = contentHeight.toFloat(),
            quarterTurns = rotationQuarterTurns,
        ) ?: return false

        drawableRect.set(0f, 0f, drawableWidth.toFloat(), drawableHeight.toFloat())
        baseMatrix.setValues(layout.matrixValues)
        displayMatrix.set(baseMatrix)
        imageMatrix = displayMatrix
        zoom = MIN_ZOOM
        configuredDrawable = currentDrawable
        configuredWidth = width
        configuredHeight = height
        return true
    }

    private fun resetToCurrentFit() {
        zoom = MIN_ZOOM
        val currentDrawable = drawable
        if (currentDrawable == null) {
            configuredDrawable = null
            configuredWidth = 0
            configuredHeight = 0
            baseMatrix.reset()
            displayMatrix.reset()
            imageMatrix = displayMatrix
            return
        }
        if (!configureBaseMatrix(currentDrawable)) {
            configuredWidth = 0
            configuredHeight = 0
        }
    }

    private fun constrainDisplayMatrix() {
        val currentDrawable = drawable ?: return
        if (currentDrawable !== configuredDrawable) {
            ensureBaseMatrix()
            return
        }
        mappedDrawableRect.set(drawableRect)
        displayMatrix.mapRect(mappedDrawableRect)

        val contentLeft = paddingLeft.toFloat()
        val contentTop = paddingTop.toFloat()
        val contentRight = (width - paddingRight).toFloat()
        val contentBottom = (height - paddingBottom).toFloat()
        val contentWidth = contentRight - contentLeft
        val contentHeight = contentBottom - contentTop

        val correctionX = when {
            mappedDrawableRect.width() <= contentWidth ->
                contentLeft + (contentWidth - mappedDrawableRect.width()) / 2f - mappedDrawableRect.left
            mappedDrawableRect.left > contentLeft -> contentLeft - mappedDrawableRect.left
            mappedDrawableRect.right < contentRight -> contentRight - mappedDrawableRect.right
            else -> 0f
        }
        val correctionY = when {
            mappedDrawableRect.height() <= contentHeight ->
                contentTop + (contentHeight - mappedDrawableRect.height()) / 2f - mappedDrawableRect.top
            mappedDrawableRect.top > contentTop -> contentTop - mappedDrawableRect.top
            mappedDrawableRect.bottom < contentBottom -> contentBottom - mappedDrawableRect.bottom
            else -> 0f
        }
        displayMatrix.postTranslate(correctionX, correctionY)
        imageMatrix = displayMatrix
    }

    private val swipeDistanceThreshold: Float
        get() = SWIPE_DISTANCE_DP * resources.displayMetrics.density

    private val swipeVelocityThreshold: Float
        get() = SWIPE_VELOCITY_DP * resources.displayMetrics.density

    private val contentCenterX: Float
        get() = (paddingLeft + width - paddingRight) / 2f

    private val contentCenterY: Float
        get() = (paddingTop + height - paddingBottom) / 2f

    companion object {
        const val DIRECTION_PREVIOUS = -1
        const val DIRECTION_NEXT = 1

        private const val SWIPE_DISTANCE_DP = 72f
        private const val SWIPE_VELOCITY_DP = 160f
    }
}
