package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Matrix
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.ViewConfiguration
import androidx.appcompat.widget.AppCompatImageView
import io.github.supermonster003.autojs6.plugin.three.maple.image.ImageZoomState.MIN_ZOOM
import io.github.supermonster003.autojs6.plugin.three.maple.image.ImageZoomState.ZOOM_EPSILON
import kotlin.math.abs
import kotlin.math.atan2

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

    /** Fires after a rotation the user requested (button, two-finger gesture, undo) settled on a quarter turn. */
    var onRotationChanged: ((quarterTurns: Int) -> Unit)? = null

    /** Two-finger rotation (ROADMAP D8); when off, two fingers only pinch. */
    var rotationGestureEnabled: Boolean = true

    // Optional gestures (ROADMAP 9.2); the defaults are the behavior from before they were settings.

    /** Double tap, then drag up or down to zoom with one finger (the platform's quick scale). */
    var oneFingerZoom: Boolean = true
        set(value) {
            field = value
            scaleDetector.isQuickScaleEnabled = value
        }

    /** A tap near the left or right edge at 1x turns the page instead of toggling the bars. */
    var edgeTapPaging: Boolean = false

    /** A quick drag down at 1x calls [onSwipeDown]. */
    var swipeDownToClose: Boolean = false

    /** A vertical drag on the left third at 1x reports the brightness through [onBrightnessDrag]. */
    var brightnessSwipe: Boolean = false

    internal var doubleTapZoom: ViewerDoubleTapZoom = ViewerDoubleTapZoom.TWO_AND_HALF
    internal var maxZoom: ViewerMaxZoom = ViewerMaxZoom.FIVE

    /** The image's pixel size as shown (EXIF orientation applied), for "actual pixels"; the drawable's when unknown. */
    internal var sourcePixelSize: ImagePixelSize? = null

    var onSwipeDown: (() -> Unit)? = null

    /** The drag since it started, down positive, in pixels of a view [height] tall; [finished] on the last call. */
    var onBrightnessDrag: ((distanceY: Float, height: Int, finished: Boolean) -> Unit)? = null

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
    private var edgeOverflowX = 0f
    private var rotationGestureActive = false
    private var lastGestureAngle = 0f
    private var gestureRotationDegrees = 0f
    private var snapAnimator: ValueAnimator? = null
    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop.toFloat()
    private var downX = 0f
    private var downY = 0f
    private var brightnessCandidate = false
    private var brightnessActive = false

    // A double tap toggles the zoom when its second tap lifts, unless that tap dragged into a quick scale.
    private var doubleTapPending = false
    private var quickScaled = false

    private val scaleDetector = ScaleGestureDetector(
        context,
        object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
                ensureBaseMatrix()
                if (drawable == null) return false
                gestureHadMultiplePointers = true
                if (doubleTapPending) quickScaled = true
                parent?.requestDisallowInterceptTouchEvent(true)
                lastScaleFocusX = detector.focusX
                lastScaleFocusY = detector.focusY
                onZoomInteraction?.invoke(zoom, true)
                return true
            }

            override fun onScale(detector: ScaleGestureDetector): Boolean {
                val reportedFactor = detector.scaleFactor
                if (!reportedFactor.isFinite() || reportedFactor <= 0f) return false

                val targetZoom = (zoom * reportedFactor).coerceIn(MIN_ZOOM, maximumZoom)
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
                val direction = if (edgeTapPaging && !isZoomedIn) ViewerGesturePolicy.edgeTapDirection(event.x, width) else null
                if (direction != null) onPageSwipe?.invoke(direction) else performClick()
                return true
            }

            override fun onDoubleTap(event: MotionEvent): Boolean {
                // Without quick scale nothing can follow the second tap, so it zooms right away.
                if (!oneFingerZoom) return toggleDoubleTapZoom(event.x, event.y)
                doubleTapPending = true
                quickScaled = false
                return true
            }

            override fun onDoubleTapEvent(event: MotionEvent): Boolean {
                if (!doubleTapPending) return false
                when (event.actionMasked) {
                    MotionEvent.ACTION_UP -> {
                        doubleTapPending = false
                        if (!quickScaled) toggleDoubleTapZoom(event.x, event.y)
                    }
                    MotionEvent.ACTION_CANCEL -> doubleTapPending = false
                }
                return true
            }

            override fun onFling(
                down: MotionEvent?,
                up: MotionEvent,
                velocityX: Float,
                velocityY: Float,
            ): Boolean {
                down ?: return false
                if (brightnessActive) return false
                return routePageFling(
                    distanceX = up.x - down.x,
                    distanceY = up.y - down.y,
                    velocityX = velocityX,
                ) || routeSwipeDown(up.x - down.x, up.y - down.y, velocityY)
            }
        },
    )

    init {
        isClickable = true
        scaleType = ScaleType.MATRIX
        scaleDetector.isQuickScaleEnabled = oneFingerZoom
    }

    /** The largest zoom for the current image and settings. */
    internal val maximumZoom: Float
        get() = ImageZoomState.maximumZoom(maxZoom, doubleTapZoom, actualPixelsZoom())

    /** Where one image pixel covers one screen pixel, for the current fit and user rotation. */
    internal fun actualPixelsZoom(): Float? {
        val current = drawable ?: return null
        if (current !== configuredDrawable || configuredWidth == 0) return null
        val size = sourcePixelSize ?: ImagePixelSize(current.intrinsicWidth, current.intrinsicHeight)
        val fitted = RectF(drawableRect).also(baseMatrix::mapRect)
        val shownWidth = if (rotationQuarterTurns % 2 != 0) size.height else size.width
        return ImageZoomState.actualPixelsZoom(shownWidth, fitted.width())
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
            snapAnimator?.end()
            gestureHadMultiplePointers = false
            activePointerId = event.getPointerId(0)
            lastTouchX = event.x
            lastTouchY = event.y
            downX = event.x
            downY = event.y
            brightnessCandidate = brightnessSwipe && !isZoomedIn && ViewerGesturePolicy.inBrightnessZone(event.x, width)
            brightnessActive = false
            edgeOverflowX = 0f
            // At 1x a horizontal drag belongs to the pager around this view; zoomed in, the image pans itself.
            parent?.requestDisallowInterceptTouchEvent(isZoomedIn)
        } else if (
            event.pointerCount > 1 ||
            event.actionMasked == MotionEvent.ACTION_POINTER_DOWN ||
            event.actionMasked == MotionEvent.ACTION_POINTER_UP
        ) {
            gestureHadMultiplePointers = true
        }

        if (trackBrightnessDrag(event)) return true

        scaleDetector.onTouchEvent(event)
        gestureDetector.onTouchEvent(event)
        handleRotationGesture(event)

        when (event.actionMasked) {
            MotionEvent.ACTION_MOVE -> {
                if (!scaleDetector.isInProgress && event.pointerCount == 1 && zoom > MIN_ZOOM) {
                    val pointerIndex = event.findPointerIndex(activePointerId).takeIf { it >= 0 } ?: 0
                    val touchX = event.getX(pointerIndex)
                    val touchY = event.getY(pointerIndex)
                    trackEdgeOverflow(touchX - lastTouchX)
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

            MotionEvent.ACTION_UP -> {
                activePointerId = MotionEvent.INVALID_POINTER_ID
                parent?.requestDisallowInterceptTouchEvent(false)
                finishEdgeOverflow()
            }

            MotionEvent.ACTION_CANCEL -> {
                activePointerId = MotionEvent.INVALID_POINTER_ID
                edgeOverflowX = 0f
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
        if (!rotateByQuarterTurns(1)) return false
        onRotationChanged?.invoke(rotationQuarterTurns)
        return true
    }

    /** Returns the user rotation to the original orientation without touching the magnification. */
    fun undoRotation(): Boolean {
        val turns = -rotationQuarterTurns
        if (turns == 0 || !rotateByQuarterTurns(turns)) return false
        onRotationChanged?.invoke(rotationQuarterTurns)
        return true
    }

    /**
     * Rotates by [turns] quarter turns around the viewport center, keeping the magnification and the source point
     * under the center; the display matrix is rebuilt from the fit for the new orientation, so a free rotation
     * applied by the gesture is absorbed exactly.
     */
    internal fun rotateByQuarterTurns(turns: Int): Boolean {
        ensureBaseMatrix()
        val currentDrawable = drawable?.takeIf { it === configuredDrawable } ?: return false
        val previousZoom = zoom
        val contentCenter = floatArrayOf(contentCenterX, contentCenterY)
        val sourceAnchor = contentCenter.copyOf()
        val inverse = Matrix()
        val hasSourceAnchor = displayMatrix.invert(inverse)
        if (hasSourceAnchor) inverse.mapPoints(sourceAnchor)

        val previousRotationQuarterTurns = rotationQuarterTurns
        rotationQuarterTurns = ImageRotationState.normalize(rotationQuarterTurns + turns)
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

        val targetZoom = ImageZoomState.doubleTapTarget(zoom, doubleTapZoom, actualPixelsZoom(), maximumZoom)
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

    private val isZoomedIn: Boolean
        get() = zoom > MIN_ZOOM + ZOOM_EPSILON

    internal fun routeSwipeDown(distanceX: Float, distanceY: Float, velocityY: Float): Boolean {
        if (!swipeDownToClose || isZoomedIn || gestureHadMultiplePointers) return false
        if (!ViewerGesturePolicy.isSwipeDown(distanceX, distanceY, velocityY, swipeDistanceThreshold, swipeVelocityThreshold)) return false
        onSwipeDown?.invoke()
        return true
    }

    /**
     * The brightness drag (ROADMAP 9.2): once a single finger on the left third moved mostly up or down, the drag
     * is reported and kept from the tap, zoom and page gestures; a second finger ends it. True while it owns the event.
     */
    private fun trackBrightnessDrag(event: MotionEvent): Boolean {
        if (!brightnessCandidate) return false
        if (event.pointerCount > 1) {
            if (brightnessActive) onBrightnessDrag?.invoke(event.y - downY, height, true)
            brightnessCandidate = false
            brightnessActive = false
            return false
        }
        when (event.actionMasked) {
            MotionEvent.ACTION_MOVE -> {
                if (!brightnessActive && ViewerGesturePolicy.isVerticalDrag(event.x - downX, event.y - downY, touchSlop)) {
                    brightnessActive = true
                    parent?.requestDisallowInterceptTouchEvent(true)
                    // The detectors saw the down; a cancel keeps them from taking the drag as a tap or fling.
                    val cancel = MotionEvent.obtain(event).apply { action = MotionEvent.ACTION_CANCEL }
                    scaleDetector.onTouchEvent(cancel)
                    gestureDetector.onTouchEvent(cancel)
                    cancel.recycle()
                }
                if (brightnessActive) onBrightnessDrag?.invoke(event.y - downY, height, false)
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                val wasActive = brightnessActive
                if (wasActive) onBrightnessDrag?.invoke(event.y - downY, height, true)
                brightnessCandidate = false
                brightnessActive = false
                if (wasActive) {
                    activePointerId = MotionEvent.INVALID_POINTER_ID
                    parent?.requestDisallowInterceptTouchEvent(false)
                }
                return wasActive
            }
        }
        return brightnessActive
    }

    // region Two-finger rotation (ROADMAP D8)

    private fun handleRotationGesture(event: MotionEvent) {
        when (event.actionMasked) {
            MotionEvent.ACTION_POINTER_DOWN -> {
                if (event.pointerCount == 2 && rotationGestureEnabled && drawable != null) {
                    ensureBaseMatrix()
                    rotationGestureActive = drawable === configuredDrawable
                    lastGestureAngle = pointerAngle(event)
                    gestureRotationDegrees = 0f
                }
            }

            MotionEvent.ACTION_MOVE -> {
                if (rotationGestureActive && event.pointerCount >= 2) {
                    val angle = pointerAngle(event)
                    val delta = normalizeDegrees(angle - lastGestureAngle)
                    lastGestureAngle = angle
                    if (delta != 0f) {
                        gestureRotationDegrees += delta
                        displayMatrix.postRotate(
                            delta,
                            (event.getX(0) + event.getX(1)) / 2f,
                            (event.getY(0) + event.getY(1)) / 2f,
                        )
                        constrainDisplayMatrix()
                    }
                }
            }

            MotionEvent.ACTION_POINTER_UP -> {
                if (rotationGestureActive && event.pointerCount - 1 < 2) finishRotationGesture()
            }

            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL,
            -> if (rotationGestureActive) finishRotationGesture()
        }
    }

    /** Snaps the free rotation to the nearest quarter turn with a short animation, then merges it into [rotationQuarterTurns]. */
    private fun finishRotationGesture() {
        rotationGestureActive = false
        val total = gestureRotationDegrees
        gestureRotationDegrees = 0f
        val turns = ImageRotationState.snapQuarterTurns(total)
        val remaining = turns * ImageRotationState.QUARTER_TURN_DEGREES - total
        if (abs(remaining) < SNAP_EPSILON_DEGREES) {
            commitGestureRotation(turns)
            return
        }
        val centerX = contentCenterX
        val centerY = contentCenterY
        var applied = 0f
        snapAnimator?.end()
        snapAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = SNAP_DURATION_MILLIS
            addUpdateListener { animator ->
                val target = remaining * (animator.animatedValue as Float)
                val step = target - applied
                applied = target
                if (step != 0f) {
                    displayMatrix.postRotate(step, centerX, centerY)
                    constrainDisplayMatrix()
                }
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    if (snapAnimator === animation) snapAnimator = null
                    commitGestureRotation(turns)
                }
            })
            start()
        }
    }

    private fun commitGestureRotation(turns: Int) {
        if (!rotateByQuarterTurns(turns)) {
            constrainDisplayMatrix()
            return
        }
        if (turns != 0) onRotationChanged?.invoke(rotationQuarterTurns)
    }

    private fun pointerAngle(event: MotionEvent): Float =
        Math.toDegrees(atan2((event.getY(1) - event.getY(0)).toDouble(), (event.getX(1) - event.getX(0)).toDouble())).toFloat()

    private fun normalizeDegrees(degrees: Float): Float {
        var value = degrees
        while (value > 180f) value -= 360f
        while (value < -180f) value += 360f
        return value
    }

    // endregion

    /**
     * While zoomed in, horizontal drag that the pan constraint swallows at an image edge accumulates here; a drag
     * that keeps pushing past the edge flips the page on release (ROADMAP 6.3 nested scroll coordination).
     */
    private fun trackEdgeOverflow(distanceX: Float) {
        if (distanceX == 0f || gestureHadMultiplePointers || drawable !== configuredDrawable) return
        mappedDrawableRect.set(drawableRect)
        displayMatrix.mapRect(mappedDrawableRect)
        val atEdge = if (distanceX > 0f) {
            mappedDrawableRect.left >= paddingLeft - EDGE_EPSILON
        } else {
            mappedDrawableRect.right <= width - paddingRight + EDGE_EPSILON
        }
        val sameDirection = edgeOverflowX == 0f || (edgeOverflowX > 0f) == (distanceX > 0f)
        edgeOverflowX = if (atEdge && sameDirection) edgeOverflowX + distanceX else 0f
    }

    private fun finishEdgeOverflow() {
        val overflow = edgeOverflowX
        edgeOverflowX = 0f
        if (!isZoomedIn || gestureHadMultiplePointers || abs(overflow) < swipeDistanceThreshold) return
        onPageSwipe?.invoke(if (overflow < 0f) DIRECTION_NEXT else DIRECTION_PREVIOUS)
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
        private const val EDGE_EPSILON = 0.5f
        private const val SNAP_DURATION_MILLIS = 160L
        private const val SNAP_EPSILON_DEGREES = 0.5f
    }
}
