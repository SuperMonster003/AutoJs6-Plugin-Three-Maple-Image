package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.util.TypedValue
import android.graphics.Path
import android.view.GestureDetector
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import androidx.core.graphics.withClip
import androidx.core.graphics.withRotation
import androidx.core.graphics.withScale
import androidx.core.graphics.withTranslation
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

internal class ImageEditingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {

    sealed interface CanvasState {
        data object Normal : CanvasState
        data class Rotation(val degrees: Float) : CanvasState
        data class Crop(
            val selection: RectF,
            val aspectRatioPreset: CropAspectRatioPreset,
        ) : CanvasState
        data class Brush(
            val tool: BrushTool,
            val color: Int,
            val widthOnScreen: Float,
            val mosaicBlockSizeOnScreen: Float,
            val strokes: List<ImageEditingEngine.BrushStroke>,
        ) : CanvasState
        data class Text(val spec: ImageEditingEngine.TextSpec) : CanvasState
        data class Shapes(
            val style: ShapeStyle,
            val shapes: List<EditorShape>,
            val selected: Int,
        ) : CanvasState
    }

    /** What the next shape looks like; color, width and fill also apply to the selected shape when they change. */
    data class ShapeStyle(
        val kind: ShapeKind,
        val color: Int,
        val widthOnScreen: Float,
        val filled: Boolean,
    )

    /** [allowsColorPick]: a long press picks a color (ROADMAP 8.1, T16) where a drawing color is in use or no tool is. */
    private enum class Mode(val hasToolGesture: Boolean, val allowsColorPick: Boolean) {
        NORMAL(false, true),
        ROTATION(false, false),
        CROP(true, false),
        BRUSH(true, true),
        TEXT(true, false),
        SHAPES(true, true),
    }

    private enum class ShapeDrag { NONE, CREATE, MOVE, HANDLE }

    /** The original image for "compare" (ROADMAP 8.1): [bitmap] may be smaller than [width] x [height]. */
    class CompareImage(val bitmap: Bitmap, val width: Int, val height: Int)

    private data class MutableStroke(
        val tool: BrushTool,
        val color: Int,
        val width: Float,
        val mosaicBlockSize: Int,
        val points: MutableList<ImageEditingEngine.Point>,
    )

    private val bitmapPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    private val plainBitmapPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    private val overlayPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val imageRect = RectF()
    private val cropRect = RectF()
    private val cropStartRect = RectF()
    private val strokes = mutableListOf<MutableStroke>()
    private var currentStroke: MutableStroke? = null
    private var bitmap: Bitmap? = null
    private var mode = Mode.NORMAL
    private var imageScale = 1f
    private var rotationPreviewDegrees = 0f
    private var cropHandle: CropResizeHandle? = null
    private var cropAspectRatioPreset = CropAspectRatioPreset.FREE
    private var touchStartX = 0f
    private var touchStartY = 0f
    private var brushTool = BrushTool.PEN
    private var brushColor = Color.RED
    private var brushWidthOnScreen = dp(8f)
    private var mosaicBlockSizeOnScreen = dp(12f)
    private var mosaicBitmapCache: MosaicBitmapCache? = null
    private var textSpec: ImageEditingEngine.TextSpec? = null

    /** Annotation shapes (ROADMAP 8.1) in bitmap pixels, drawn in order; [selectedShape] is -1 when none is. */
    private val shapes = mutableListOf<EditorShape>()
    private var selectedShape = -1
    private var shapeStyle = ShapeStyle(ShapeKind.RECTANGLE, Color.RED, dp(4f), filled = false)
    private var shapeDrag = ShapeDrag.NONE
    private var shapeHandle = -1
    private var shapeDragStart = ShapePoint(0f, 0f)
    private var shapeBeforeDrag: EditorShape? = null
    private var pendingShape: EditorShape? = null
    private val shapePaint = ShapeRenderer.newPaint()

    /** Called after shapes are added, removed or (de)selected, so the panel can follow. */
    var onShapesChanged: (() -> Unit)? = null

    /** A downscaled render of the look being previewed (ROADMAP 8.1), drawn instead of [bitmap] while set. */
    private var previewBitmap: Bitmap? = null

    /** Zoom and pan of the canvas (ROADMAP 8.1); [imageRect] and [imageScale] follow it. */
    private val viewport = EditorViewport()
    private var compareImage: CompareImage? = null
    private val compareRect = RectF()

    /**
     * True from the moment a gesture moves the canvas until all fingers are up. Without an active tool every gesture
     * does; with one, a second finger turns the tool gesture into a canvas gesture (the stroke or drag is dropped).
     */
    private var viewportGesture = false
    private var lastFocusX = 0f
    private var lastFocusY = 0f

    /**
     * Mean distance of the fingers from their center at the previous event, 0 with one finger. Measured here rather
     * than with ScaleGestureDetector, which ignores fingers closer than about 27 mm.
     */
    private var lastSpan = 0f
    private val tapDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onDoubleTap(e: MotionEvent): Boolean {
            if (mode.hasToolGesture) return false
            viewport.toggleZoom(e.x, e.y)
            updateImageRect()
            invalidate()
            return true
        }

        override fun onLongPress(e: MotionEvent) {
            startColorPick(e.x, e.y)
        }
    })

    /**
     * Color picking (ROADMAP 8.1, T16): after a long press a magnifier follows the finger and letting go picks the
     * pixel under it. The view position goes back to the bitmap through the same viewport as every tool, so the
     * picked color is the bitmap pixel at any zoom and pan.
     */
    private var picking = false
    private var pickViewX = 0f
    private var pickViewY = 0f
    private val magnifierPaint = Paint().apply { isFilterBitmap = false }
    private val magnifierPath = Path()
    private val magnifierSource = Rect()
    private val magnifierTarget = RectF()
    private val magnifierTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 14f, resources.displayMetrics)
    }

    /** Receives the picked color (opaque) when the finger is lifted. */
    var onColorPicked: ((Int) -> Unit)? = null

    /** The color under the finger while picking, or null (test hook). */
    fun pickingColor(): Int? = if (picking) colorUnderPick() else null

    /** Current zoom, 1 = fitted (test hook). */
    val zoom: Float get() = viewport.zoom

    /** Where a bitmap pixel is on the view (test hook for the zoomed canvas). */
    fun bitmapToView(x: Float, y: Float): Pair<Float, Float> = viewport.toViewX(x) to viewport.toViewY(y)

    fun setBitmap(value: Bitmap?) {
        bitmap = value
        viewport.setImage(value?.width ?: 0, value?.height ?: 0)
        clearToolState()
        updateImageRect()
        invalidate()
    }

    fun setPreviewColorMatrix(matrix: ColorMatrix?) {
        bitmapPaint.colorFilter = matrix?.let(::ColorMatrixColorFilter)
        invalidate()
    }

    /**
     * Shows [value] (a render of the whole image at a smaller size) in place of the image, without the color matrix
     * preview; null shows the image again. The caller owns the bitmap and recycles it after replacing it.
     */
    fun setPreviewBitmap(value: Bitmap?) {
        previewBitmap = value
        invalidate()
    }

    /** Shows [value] instead of the edited image while it is set ("compare"); null shows the edit again. */
    fun setCompareImage(value: CompareImage?) {
        compareImage = value
        invalidate()
    }

    fun resetZoom() {
        viewport.reset()
        updateImageRect()
        invalidate()
    }

    fun beginRotation(degrees: Float = 0f) {
        val source = bitmap ?: return
        ImageRotationGeometry.coverScale(source.width, source.height, degrees)
        clearToolState()
        mode = Mode.ROTATION
        rotationPreviewDegrees = degrees
        invalidate()
    }

    fun updateRotationPreview(degrees: Float) {
        val source = bitmap ?: return
        if (mode != Mode.ROTATION || rotationPreviewDegrees == degrees) return
        ImageRotationGeometry.coverScale(source.width, source.height, degrees)
        rotationPreviewDegrees = degrees
        invalidate()
    }

    fun rotationPreviewDegrees(): Float = rotationPreviewDegrees

    fun beginCrop(aspectRatioPreset: CropAspectRatioPreset = CropAspectRatioPreset.FREE) {
        val source = bitmap ?: return
        clearToolState()
        mode = Mode.CROP
        cropRect.set(0f, 0f, source.width.toFloat(), source.height.toFloat())
        cropAspectRatioPreset = aspectRatioPreset
        applyCropAspectRatio(source)
        invalidate()
    }

    fun updateCropAspectRatio(aspectRatioPreset: CropAspectRatioPreset) {
        val source = bitmap ?: return
        if (mode != Mode.CROP || cropAspectRatioPreset == aspectRatioPreset) return
        cropAspectRatioPreset = aspectRatioPreset
        cropHandle = null
        applyCropAspectRatio(source)
        invalidate()
    }

    fun cropAspectRatioPreset(): CropAspectRatioPreset = cropAspectRatioPreset

    fun cropSelection(): Rect? {
        if (mode != Mode.CROP) return null
        val source = bitmap ?: return null
        val left = cropRect.left.roundToInt().coerceIn(0, source.width - 1)
        val top = cropRect.top.roundToInt().coerceIn(0, source.height - 1)
        val right = cropRect.right.roundToInt().coerceIn(left + 1, source.width)
        val bottom = cropRect.bottom.roundToInt().coerceIn(top + 1, source.height)
        return Rect(left, top, right, bottom)
    }

    fun beginBrush(
        tool: BrushTool,
        color: Int,
        widthOnScreen: Float,
        mosaicBlockSizeOnScreen: Float,
    ) {
        clearToolState()
        mode = Mode.BRUSH
        brushTool = tool
        brushColor = color
        brushWidthOnScreen = widthOnScreen
        this.mosaicBlockSizeOnScreen = mosaicBlockSizeOnScreen
        invalidate()
    }

    fun updateBrush(
        tool: BrushTool = brushTool,
        color: Int = brushColor,
        widthOnScreen: Float = brushWidthOnScreen,
        mosaicBlockSizeOnScreen: Float = this.mosaicBlockSizeOnScreen,
    ) {
        brushTool = tool
        brushColor = color
        brushWidthOnScreen = widthOnScreen
        this.mosaicBlockSizeOnScreen = mosaicBlockSizeOnScreen
    }

    fun brushStrokes(): List<ImageEditingEngine.BrushStroke> = strokes.map { stroke ->
        ImageEditingEngine.BrushStroke(
            tool = stroke.tool,
            color = stroke.color,
            width = stroke.width,
            mosaicBlockSize = stroke.mosaicBlockSize,
            points = stroke.points.toList(),
        )
    }

    fun beginText(spec: ImageEditingEngine.TextSpec) {
        clearToolState()
        mode = Mode.TEXT
        textSpec = spec
        invalidate()
    }

    fun textSpec(): ImageEditingEngine.TextSpec? = textSpec

    fun beginShapes(style: ShapeStyle) {
        clearToolState()
        mode = Mode.SHAPES
        shapeStyle = style
        invalidate()
    }

    /** New settings for the next shapes; color, width and fill also change the selected shape. */
    fun updateShapeStyle(style: ShapeStyle) {
        val previous = shapeStyle
        shapeStyle = style
        val index = selectedShape
        if (mode != Mode.SHAPES || index !in shapes.indices) return
        var shape = shapes[index]
        if (style.color != previous.color) shape = shape.copy(color = style.color)
        if (style.widthOnScreen != previous.widthOnScreen) shape = shape.copy(strokeWidth = style.widthOnScreen / imageScale)
        if (style.filled != previous.filled) shape = shape.copy(filled = style.filled)
        shapes[index] = shape
        invalidate()
    }

    fun shapes(): List<EditorShape> = shapes.toList()

    fun hasSelectedShape(): Boolean = selectedShape in shapes.indices

    fun deleteSelectedShape() {
        if (selectedShape !in shapes.indices) return
        shapes.removeAt(selectedShape)
        selectedShape = -1
        invalidate()
        onShapesChanged?.invoke()
    }

    fun captureCanvasState(): CanvasState = when (mode) {
        Mode.NORMAL -> CanvasState.Normal
        Mode.ROTATION -> CanvasState.Rotation(rotationPreviewDegrees)
        Mode.CROP -> CanvasState.Crop(RectF(cropRect), cropAspectRatioPreset)
        Mode.BRUSH -> CanvasState.Brush(
            brushTool,
            brushColor,
            brushWidthOnScreen,
            mosaicBlockSizeOnScreen,
            brushStrokes(),
        )
        Mode.TEXT -> textSpec?.let(CanvasState::Text) ?: CanvasState.Normal
        Mode.SHAPES -> CanvasState.Shapes(shapeStyle, shapes.toList(), selectedShape)
    }

    fun restoreCanvasState(state: CanvasState) {
        clearToolState()
        when (state) {
            CanvasState.Normal -> Unit
            is CanvasState.Rotation -> {
                mode = Mode.ROTATION
                rotationPreviewDegrees = state.degrees
            }
            is CanvasState.Crop -> {
                mode = Mode.CROP
                cropRect.set(state.selection)
                cropAspectRatioPreset = state.aspectRatioPreset
            }
            is CanvasState.Brush -> {
                mode = Mode.BRUSH
                brushTool = state.tool
                brushColor = state.color
                brushWidthOnScreen = state.widthOnScreen
                mosaicBlockSizeOnScreen = state.mosaicBlockSizeOnScreen
                strokes += state.strokes.map { stroke ->
                    MutableStroke(
                        stroke.tool,
                        stroke.color,
                        stroke.width,
                        stroke.mosaicBlockSize,
                        stroke.points.toMutableList(),
                    )
                }
            }
            is CanvasState.Text -> {
                mode = Mode.TEXT
                textSpec = state.spec
            }
            is CanvasState.Shapes -> {
                mode = Mode.SHAPES
                shapeStyle = state.style
                shapes += state.shapes
                selectedShape = state.selected.takeIf { it in shapes.indices } ?: -1
            }
        }
        invalidate()
    }

    fun clearToolState() {
        mode = Mode.NORMAL
        picking = false
        bitmapPaint.colorFilter = null
        previewBitmap = null
        rotationPreviewDegrees = 0f
        cropHandle = null
        cropAspectRatioPreset = CropAspectRatioPreset.FREE
        brushTool = BrushTool.PEN
        brushColor = Color.RED
        brushWidthOnScreen = dp(8f)
        mosaicBlockSizeOnScreen = dp(12f)
        mosaicBitmapCache?.close()
        mosaicBitmapCache = null
        strokes.clear()
        currentStroke = null
        textSpec = null
        shapes.clear()
        selectedShape = -1
        shapeDrag = ShapeDrag.NONE
        shapeBeforeDrag = null
        pendingShape = null
        invalidate()
    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)
        viewport.setView(width.toFloat(), height.toFloat())
        updateImageRect()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // Compose does not clip interop views to their bounds: a zoomed image would cover the bars around the canvas.
        canvas.withClip(0f, 0f, width.toFloat(), height.toFloat()) { drawContent(this) }
    }

    private fun drawContent(canvas: Canvas) {
        val source = bitmap ?: return
        if (imageRect.isEmpty) updateImageRect()
        compareImage?.takeUnless { it.bitmap.isRecycled }?.let { compare ->
            canvas.drawBitmap(compare.bitmap, null, compareDestination(compare, source), plainBitmapPaint)
            return
        }
        val preview = previewBitmap?.takeUnless(Bitmap::isRecycled)
        when {
            mode == Mode.ROTATION -> drawRotationPreview(canvas, source)
            preview != null -> canvas.drawBitmap(preview, null, imageRect, plainBitmapPaint)
            else -> canvas.drawBitmap(source, null, imageRect, bitmapPaint)
        }
        when (mode) {
            Mode.ROTATION -> Unit
            Mode.CROP -> drawCropOverlay(canvas)
            Mode.BRUSH -> drawBrushStrokes(canvas)
            Mode.TEXT -> drawTextOverlay(canvas)
            Mode.SHAPES -> drawShapes(canvas)
            Mode.NORMAL -> Unit
        }
        if (picking) drawMagnifier(canvas)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (bitmap == null || compareImage != null) return false
        if (!mode.hasToolGesture || mode.allowsColorPick) tapDetector.onTouchEvent(event)
        if (picking) return handlePickTouch(event)
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> viewportGesture = !mode.hasToolGesture
            MotionEvent.ACTION_POINTER_DOWN -> if (!viewportGesture) {
                cancelToolGesture()
                viewportGesture = true
                parent?.requestDisallowInterceptTouchEvent(true)
            }
        }
        if (viewportGesture) return handleViewportTouch(event)
        when (mode) {
            Mode.CROP -> handleCropTouch(event)
            Mode.BRUSH -> handleBrushTouch(event)
            Mode.TEXT -> handleTextTouch(event)
            Mode.SHAPES -> handleShapeTouch(event)
            Mode.NORMAL, Mode.ROTATION -> Unit
        }
        // Keep the gesture even when the tool ignores its first finger, so that a second finger can still zoom.
        return true
    }

    private fun startColorPick(x: Float, y: Float) {
        if (!mode.allowsColorPick || compareImage != null || bitmap == null || !imageRect.contains(x, y)) return
        // The stroke dot or shape the first finger began is not wanted any more.
        cancelToolGesture()
        picking = true
        pickViewX = x
        pickViewY = y
        parent?.requestDisallowInterceptTouchEvent(true)
        performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        invalidate()
    }

    private fun handlePickTouch(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_MOVE -> {
                // Inside the image and on screen, even while the finger strays.
                pickViewX = event.x.coerceIn(max(imageRect.left, 0f), min(imageRect.right, width.toFloat()) - 0.5f)
                pickViewY = event.y.coerceIn(max(imageRect.top, 0f), min(imageRect.bottom, height.toFloat()) - 0.5f)
                invalidate()
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                val color = if (event.actionMasked == MotionEvent.ACTION_UP) colorUnderPick() else null
                picking = false
                viewportGesture = false
                parent?.requestDisallowInterceptTouchEvent(false)
                invalidate()
                if (event.actionMasked == MotionEvent.ACTION_UP) performClick()
                color?.let { onColorPicked?.invoke(it) }
            }
        }
        return true
    }

    private fun pickedPixel(): Pair<Int, Int>? {
        val source = bitmap ?: return null
        val x = floor(viewport.toImageX(pickViewX)).toInt().coerceIn(0, source.width - 1)
        val y = floor(viewport.toImageY(pickViewY)).toInt().coerceIn(0, source.height - 1)
        return x to y
    }

    private fun colorUnderPick(): Int? {
        val source = bitmap?.takeUnless(Bitmap::isRecycled) ?: return null
        val (x, y) = pickedPixel() ?: return null
        return source.getPixel(x, y) or OPAQUE
    }

    /** A round loupe above the finger (below it near the top): the pixels around the picked one, unsmoothed. */
    private fun drawMagnifier(canvas: Canvas) {
        val source = bitmap?.takeUnless(Bitmap::isRecycled) ?: return
        val (pixelX, pixelY) = pickedPixel() ?: return
        val color = source.getPixel(pixelX, pixelY) or OPAQUE
        val radius = dp(MAGNIFIER_RADIUS_DP)
        val gap = dp(MAGNIFIER_GAP_DP)
        val centerX = pickViewX.coerceIn(radius, max(radius, width - radius))
        var centerY = pickViewY - gap - radius
        if (centerY - radius < 0f) centerY = pickViewY + gap + radius
        val cell = radius * 2f / MAGNIFIER_PIXELS
        val half = MAGNIFIER_PIXELS / 2
        // Only the part of the 11 x 11 window that lies inside the bitmap is drawn; the rest stays dark.
        magnifierSource.set(
            max(0, pixelX - half),
            max(0, pixelY - half),
            min(source.width, pixelX + half + 1),
            min(source.height, pixelY + half + 1),
        )
        val left = centerX - radius + (magnifierSource.left - (pixelX - half)) * cell
        val top = centerY - radius + (magnifierSource.top - (pixelY - half)) * cell
        magnifierTarget.set(left, top, left + magnifierSource.width() * cell, top + magnifierSource.height() * cell)
        magnifierPath.reset()
        magnifierPath.addCircle(centerX, centerY, radius, Path.Direction.CW)
        canvas.withClip(magnifierPath) {
            drawColor(MAGNIFIER_BACKGROUND)
            drawBitmap(source, magnifierSource, magnifierTarget, magnifierPaint)
            overlayPaint.style = Paint.Style.STROKE
            overlayPaint.strokeWidth = dp(1.5f)
            overlayPaint.color = if (luminance(color) > 0.5f) Color.BLACK else Color.WHITE
            drawRect(centerX - cell / 2f, centerY - cell / 2f, centerX + cell / 2f, centerY + cell / 2f, overlayPaint)
        }
        overlayPaint.style = Paint.Style.STROKE
        overlayPaint.strokeWidth = dp(6f)
        overlayPaint.color = color
        canvas.drawCircle(centerX, centerY, radius, overlayPaint)
        overlayPaint.strokeWidth = dp(1f)
        overlayPaint.color = Color.WHITE
        canvas.drawCircle(centerX, centerY, radius + dp(3f), overlayPaint)
        val label = String.format(java.util.Locale.ROOT, "#%06X", color and 0xFFFFFF)
        val labelY = centerY + radius + dp(10f)
        val labelWidth = magnifierTextPaint.measureText(label) + dp(16f)
        val labelHeight = magnifierTextPaint.textSize + dp(8f)
        overlayPaint.style = Paint.Style.FILL
        overlayPaint.color = MAGNIFIER_BACKGROUND
        canvas.drawRoundRect(centerX - labelWidth / 2f, labelY, centerX + labelWidth / 2f, labelY + labelHeight, labelHeight / 2f, labelHeight / 2f, overlayPaint)
        canvas.drawText(label, centerX, labelY + labelHeight / 2f - (magnifierTextPaint.ascent() + magnifierTextPaint.descent()) / 2f, magnifierTextPaint)
    }

    private fun luminance(color: Int): Float =
        (0.2126f * Color.red(color) + 0.7152f * Color.green(color) + 0.0722f * Color.blue(color)) / 255f

    /** Pans by the movement of the fingers' center and zooms by the change of their spread around it. */
    private fun handleViewportTouch(event: MotionEvent): Boolean {
        val lifted = if (event.actionMasked == MotionEvent.ACTION_POINTER_UP) event.actionIndex else -1
        var sumX = 0f
        var sumY = 0f
        var count = 0
        for (index in 0 until event.pointerCount) {
            if (index == lifted) continue
            sumX += event.getX(index)
            sumY += event.getY(index)
            count++
        }
        if (count == 0) return true
        val focusX = sumX / count
        val focusY = sumY / count
        var spanSum = 0f
        for (index in 0 until event.pointerCount) {
            if (index == lifted) continue
            spanSum += hypot(event.getX(index) - focusX, event.getY(index) - focusY)
        }
        val span = if (count > 1) spanSum / count else 0f
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> parent?.requestDisallowInterceptTouchEvent(true)
            MotionEvent.ACTION_MOVE -> {
                viewport.panBy(focusX - lastFocusX, focusY - lastFocusY)
                if (span > 0f && lastSpan >= dp(MIN_PINCH_SPAN_DP)) viewport.zoomBy(span / lastSpan, focusX, focusY)
                updateImageRect()
                invalidate()
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                parent?.requestDisallowInterceptTouchEvent(false)
                viewportGesture = false
                if (event.actionMasked == MotionEvent.ACTION_UP) performClick()
            }
        }
        lastFocusX = focusX
        lastFocusY = focusY
        lastSpan = span
        return true
    }

    /** A second finger came down during a tool gesture: the stroke is dropped and a crop drag goes back. */
    private fun cancelToolGesture() {
        when (mode) {
            Mode.BRUSH -> currentStroke?.let(strokes::remove)
            Mode.CROP -> if (cropHandle != null) cropRect.set(cropStartRect)
            Mode.SHAPES -> {
                val before = shapeBeforeDrag
                if (before != null && selectedShape in shapes.indices) shapes[selectedShape] = before
                pendingShape = null
                shapeDrag = ShapeDrag.NONE
                shapeBeforeDrag = null
            }
            else -> Unit
        }
        currentStroke = null
        cropHandle = null
        invalidate()
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    private fun updateImageRect() {
        val source = bitmap ?: run {
            imageRect.setEmpty()
            return
        }
        if (width <= 0 || height <= 0) return
        if (viewport.viewWidth != width.toFloat() || viewport.viewHeight != height.toFloat()) {
            viewport.setView(width.toFloat(), height.toFloat())
        }
        if (viewport.imageWidth != source.width || viewport.imageHeight != source.height) {
            viewport.setImage(source.width, source.height)
        }
        imageScale = viewport.scale
        imageRect.set(viewport.left, viewport.top, viewport.right, viewport.bottom)
    }

    /**
     * The original lies exactly under the edit when the size did not change (a zoomed comparison shows the same
     * spot); after a crop or resize it is fitted and centered on its own.
     */
    private fun compareDestination(compare: CompareImage, source: Bitmap): RectF {
        if (compare.width == source.width && compare.height == source.height) return imageRect
        val scale = min(width.toFloat() / compare.width, height.toFloat() / compare.height)
        val drawnWidth = compare.width * scale
        val drawnHeight = compare.height * scale
        compareRect.set((width - drawnWidth) / 2f, (height - drawnHeight) / 2f, (width + drawnWidth) / 2f, (height + drawnHeight) / 2f)
        return compareRect
    }

    private fun applyCropAspectRatio(source: Bitmap) {
        val aspectRatio = cropAspectRatioPreset.aspectRatio(source.width, source.height) ?: return
        cropRect.set(
            CropSelectionGeometry.fitAspectRatio(
                selection = cropRect.toCropSelection(),
                aspectRatio = aspectRatio,
            ),
        )
    }

    private fun drawRotationPreview(canvas: Canvas, source: Bitmap) {
        val scale = ImageRotationGeometry.coverScale(
            sourceWidth = source.width,
            sourceHeight = source.height,
            degrees = rotationPreviewDegrees,
        )
        canvas.withClip(imageRect) {
            withRotation(rotationPreviewDegrees, imageRect.centerX(), imageRect.centerY()) {
                withScale(scale, scale, imageRect.centerX(), imageRect.centerY()) {
                    drawBitmap(source, null, imageRect, bitmapPaint)
                }
            }
        }
    }

    private fun drawCropOverlay(canvas: Canvas) {
        val displayCrop = bitmapRectToView(cropRect)
        overlayPaint.color = CROP_SHADE_COLOR
        overlayPaint.style = Paint.Style.FILL
        canvas.drawRect(imageRect.left, imageRect.top, imageRect.right, displayCrop.top, overlayPaint)
        canvas.drawRect(imageRect.left, displayCrop.bottom, imageRect.right, imageRect.bottom, overlayPaint)
        canvas.drawRect(imageRect.left, displayCrop.top, displayCrop.left, displayCrop.bottom, overlayPaint)
        canvas.drawRect(displayCrop.right, displayCrop.top, imageRect.right, displayCrop.bottom, overlayPaint)

        overlayPaint.color = Color.WHITE
        overlayPaint.style = Paint.Style.STROKE
        overlayPaint.strokeWidth = dp(2f)
        canvas.drawRect(displayCrop, overlayPaint)
        val thirdWidth = displayCrop.width() / 3f
        val thirdHeight = displayCrop.height() / 3f
        overlayPaint.alpha = GRID_ALPHA
        canvas.drawLine(displayCrop.left + thirdWidth, displayCrop.top, displayCrop.left + thirdWidth, displayCrop.bottom, overlayPaint)
        canvas.drawLine(displayCrop.left + thirdWidth * 2f, displayCrop.top, displayCrop.left + thirdWidth * 2f, displayCrop.bottom, overlayPaint)
        canvas.drawLine(displayCrop.left, displayCrop.top + thirdHeight, displayCrop.right, displayCrop.top + thirdHeight, overlayPaint)
        canvas.drawLine(displayCrop.left, displayCrop.top + thirdHeight * 2f, displayCrop.right, displayCrop.top + thirdHeight * 2f, overlayPaint)
        overlayPaint.alpha = 255
        overlayPaint.style = Paint.Style.FILL
        listOf(
            displayCrop.left to displayCrop.top,
            displayCrop.right to displayCrop.top,
            displayCrop.left to displayCrop.bottom,
            displayCrop.right to displayCrop.bottom,
        ).forEach { (x, y) -> canvas.drawCircle(x, y, dp(5f), overlayPaint) }
    }

    private fun drawBrushStrokes(canvas: Canvas) {
        val source = bitmap ?: return
        strokes.forEach { stroke ->
            BrushStrokeRenderer.draw(
                canvas = canvas,
                source = source,
                destination = imageRect,
                tool = stroke.tool,
                color = stroke.color,
                width = stroke.width,
                mosaicBlockSize = stroke.mosaicBlockSize,
                points = stroke.points,
                mosaicBitmap = { blockSize -> mosaicBitmap(source, blockSize) },
            )
        }
    }

    private fun drawTextOverlay(canvas: Canvas) {
        val spec = textSpec ?: return
        canvas.withClip(imageRect) {
            withTranslation(imageRect.left, imageRect.top) {
                withScale(imageScale, imageScale) {
                    StyledTextRenderer.draw(this, spec)
                }
            }
        }
    }

    private fun drawShapes(canvas: Canvas) {
        canvas.withClip(imageRect) {
            withTranslation(imageRect.left, imageRect.top) {
                withScale(imageScale, imageScale) {
                    shapes.forEach { ShapeRenderer.draw(this, it, shapePaint) }
                    pendingShape?.let { ShapeRenderer.draw(this, it, shapePaint) }
                }
            }
        }
        val selected = shapes.getOrNull(selectedShape) ?: return
        val radius = dp(SHAPE_HANDLE_RADIUS_DP)
        ShapeGeometry.handles(selected).forEach { handle ->
            val x = imageRect.left + handle.x * imageScale
            val y = imageRect.top + handle.y * imageScale
            overlayPaint.style = Paint.Style.FILL
            overlayPaint.color = Color.WHITE
            canvas.drawCircle(x, y, radius, overlayPaint)
            overlayPaint.style = Paint.Style.STROKE
            overlayPaint.strokeWidth = dp(1.5f)
            overlayPaint.color = SHAPE_HANDLE_OUTLINE
            canvas.drawCircle(x, y, radius, overlayPaint)
        }
        overlayPaint.style = Paint.Style.FILL
    }

    /**
     * Shapes: a drag on a handle of the selected shape resizes it, a drag on a shape selects and moves it, a drag
     * elsewhere draws a new one; a tap elsewhere only clears the selection.
     */
    private fun handleShapeTouch(event: MotionEvent): Boolean {
        val point = viewToBitmap(event.x, event.y, clamp = true) ?: return false
        val source = bitmap ?: return false
        val tolerance = dp(SHAPE_HIT_SLOP_DP) / imageScale
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                parent?.requestDisallowInterceptTouchEvent(true)
                shapeDragStart = ShapePoint(point.x, point.y)
                val selected = shapes.getOrNull(selectedShape)
                val handle = selected?.let { ShapeGeometry.hitHandle(it, point.x, point.y, tolerance) }
                val hit = ShapeGeometry.topmostHit(shapes, point.x, point.y, tolerance)
                when {
                    handle != null -> {
                        shapeDrag = ShapeDrag.HANDLE
                        shapeHandle = handle
                        shapeBeforeDrag = selected
                    }
                    hit != null -> {
                        shapeDrag = ShapeDrag.MOVE
                        if (hit != selectedShape) {
                            selectedShape = hit
                            onShapesChanged?.invoke()
                        }
                        shapeBeforeDrag = shapes[hit]
                    }
                    else -> {
                        shapeDrag = ShapeDrag.CREATE
                        shapeBeforeDrag = null
                        pendingShape = EditorShape(
                            kind = shapeStyle.kind,
                            startX = point.x,
                            startY = point.y,
                            endX = point.x,
                            endY = point.y,
                            color = shapeStyle.color,
                            strokeWidth = shapeStyle.widthOnScreen / imageScale,
                            filled = shapeStyle.filled,
                        )
                    }
                }
                invalidate()
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val before = shapeBeforeDrag
                when (shapeDrag) {
                    ShapeDrag.CREATE -> pendingShape = pendingShape?.copy(endX = point.x, endY = point.y)
                    ShapeDrag.MOVE -> if (before != null && selectedShape in shapes.indices) {
                        shapes[selectedShape] = ShapeGeometry.moved(
                            before,
                            point.x - shapeDragStart.x,
                            point.y - shapeDragStart.y,
                            source.width.toFloat(),
                            source.height.toFloat(),
                        )
                    }
                    ShapeDrag.HANDLE -> if (before != null && selectedShape in shapes.indices) {
                        shapes[selectedShape] = ShapeGeometry.withHandle(before, shapeHandle, point.x, point.y, minimumShapeSize())
                    }
                    ShapeDrag.NONE -> Unit
                }
                invalidate()
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                parent?.requestDisallowInterceptTouchEvent(false)
                val pending = pendingShape
                if (shapeDrag == ShapeDrag.CREATE) {
                    if (event.actionMasked == MotionEvent.ACTION_UP && pending != null && !ShapeGeometry.isTooSmall(pending, minimumShapeSize())) {
                        shapes += pending
                        selectedShape = shapes.lastIndex
                    } else {
                        selectedShape = -1
                    }
                    onShapesChanged?.invoke()
                } else if (event.actionMasked == MotionEvent.ACTION_CANCEL) {
                    shapeBeforeDrag?.let { before -> if (selectedShape in shapes.indices) shapes[selectedShape] = before }
                }
                pendingShape = null
                shapeDrag = ShapeDrag.NONE
                shapeBeforeDrag = null
                if (event.actionMasked == MotionEvent.ACTION_UP) performClick()
                invalidate()
                return true
            }
        }
        return false
    }

    private fun minimumShapeSize(): Float = dp(MIN_SHAPE_SCREEN_SIZE_DP) / imageScale

    private fun handleCropTouch(event: MotionEvent): Boolean {
        val point = viewToBitmap(event.x, event.y, clamp = true) ?: return false
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                touchStartX = point.x
                touchStartY = point.y
                cropStartRect.set(cropRect)
                cropHandle = resolveCropHandle(point.x, point.y)
                if (cropHandle == null) return false
                parent?.requestDisallowInterceptTouchEvent(true)
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                updateCrop(point.x - touchStartX, point.y - touchStartY)
                invalidate()
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                parent?.requestDisallowInterceptTouchEvent(false)
                cropHandle = null
                if (event.actionMasked == MotionEvent.ACTION_UP) performClick()
                return true
            }
        }
        return false
    }

    private fun resolveCropHandle(x: Float, y: Float): CropResizeHandle? {
        val tolerance = dp(CROP_HIT_SLOP_DP) / imageScale
        val withinHorizontalSpan = x in (cropRect.left - tolerance)..(cropRect.right + tolerance)
        val withinVerticalSpan = y in (cropRect.top - tolerance)..(cropRect.bottom + tolerance)
        val nearLeft = withinVerticalSpan && abs(x - cropRect.left) <= tolerance
        val nearRight = withinVerticalSpan && abs(x - cropRect.right) <= tolerance
        val nearTop = withinHorizontalSpan && abs(y - cropRect.top) <= tolerance
        val nearBottom = withinHorizontalSpan && abs(y - cropRect.bottom) <= tolerance
        return when {
            nearLeft && nearTop -> CropResizeHandle.TOP_LEFT
            nearRight && nearTop -> CropResizeHandle.TOP_RIGHT
            nearLeft && nearBottom -> CropResizeHandle.BOTTOM_LEFT
            nearRight && nearBottom -> CropResizeHandle.BOTTOM_RIGHT
            nearLeft -> CropResizeHandle.LEFT
            nearRight -> CropResizeHandle.RIGHT
            nearTop -> CropResizeHandle.TOP
            nearBottom -> CropResizeHandle.BOTTOM
            cropRect.contains(x, y) -> CropResizeHandle.MOVE
            else -> null
        }
    }

    private fun updateCrop(deltaX: Float, deltaY: Float) {
        val source = bitmap ?: return
        val desiredMinSize = max(MIN_CROP_BITMAP_SIZE, dp(MIN_CROP_SCREEN_SIZE_DP) / imageScale)
        val handle = cropHandle ?: return
        val aspectRatio = cropAspectRatioPreset.aspectRatio(source.width, source.height)
        if (aspectRatio != null) {
            cropRect.set(
                CropSelectionGeometry.resizeLocked(
                    selection = cropStartRect.toCropSelection(),
                    handle = handle,
                    deltaX = deltaX,
                    deltaY = deltaY,
                    boundsWidth = source.width.toFloat(),
                    boundsHeight = source.height.toFloat(),
                    aspectRatio = aspectRatio,
                    minimumSide = desiredMinSize,
                ),
            )
            return
        }
        val minWidth = min(desiredMinSize, cropStartRect.width())
        val minHeight = min(desiredMinSize, cropStartRect.height())
        when (handle) {
            CropResizeHandle.LEFT, CropResizeHandle.TOP_LEFT, CropResizeHandle.BOTTOM_LEFT ->
                cropRect.left = (cropStartRect.left + deltaX).coerceIn(0f, cropStartRect.right - minWidth)
            CropResizeHandle.RIGHT, CropResizeHandle.TOP_RIGHT, CropResizeHandle.BOTTOM_RIGHT ->
                cropRect.right = (cropStartRect.right + deltaX).coerceIn(cropStartRect.left + minWidth, source.width.toFloat())
            else -> Unit
        }
        when (handle) {
            CropResizeHandle.TOP, CropResizeHandle.TOP_LEFT, CropResizeHandle.TOP_RIGHT ->
                cropRect.top = (cropStartRect.top + deltaY).coerceIn(0f, cropStartRect.bottom - minHeight)
            CropResizeHandle.BOTTOM, CropResizeHandle.BOTTOM_LEFT, CropResizeHandle.BOTTOM_RIGHT ->
                cropRect.bottom = (cropStartRect.bottom + deltaY).coerceIn(cropStartRect.top + minHeight, source.height.toFloat())
            else -> Unit
        }
        if (handle == CropResizeHandle.MOVE) {
            val movedLeft = (cropStartRect.left + deltaX).coerceIn(0f, source.width - cropStartRect.width())
            val movedTop = (cropStartRect.top + deltaY).coerceIn(0f, source.height - cropStartRect.height())
            cropRect.set(movedLeft, movedTop, movedLeft + cropStartRect.width(), movedTop + cropStartRect.height())
        }
    }

    private fun handleBrushTouch(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                val point = viewToBitmap(event.x, event.y) ?: return false
                parent?.requestDisallowInterceptTouchEvent(true)
                val stroke = MutableStroke(
                    tool = brushTool,
                    color = brushColor,
                    width = brushWidthOnScreen / imageScale,
                    mosaicBlockSize = (mosaicBlockSizeOnScreen / imageScale)
                        .roundToInt()
                        .coerceAtLeast(MosaicBitmapCache.MIN_MOSAIC_BLOCK_SIZE),
                    points = mutableListOf(point),
                )
                strokes += stroke
                currentStroke = stroke
                invalidate()
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val stroke = currentStroke ?: return false
                for (index in 0 until event.historySize) {
                    viewToBitmap(event.getHistoricalX(index), event.getHistoricalY(index))
                        ?.let { appendBrushPoint(stroke, it) }
                }
                viewToBitmap(event.x, event.y)?.let { appendBrushPoint(stroke, it) }
                invalidate()
                return true
            }
            MotionEvent.ACTION_UP -> {
                currentStroke?.let { stroke ->
                    viewToBitmap(event.x, event.y)?.let { appendBrushPoint(stroke, it, force = true) }
                }
                parent?.requestDisallowInterceptTouchEvent(false)
                currentStroke = null
                performClick()
                return true
            }
            MotionEvent.ACTION_CANCEL -> {
                currentStroke?.let(strokes::remove)
                currentStroke = null
                parent?.requestDisallowInterceptTouchEvent(false)
                invalidate()
                return true
            }
        }
        return false
    }

    private fun appendBrushPoint(
        stroke: MutableStroke,
        point: ImageEditingEngine.Point,
        force: Boolean = false,
    ) {
        val last = stroke.points.lastOrNull()
        if (last == null) {
            stroke.points += point
            return
        }
        val deltaX = point.x - last.x
        val deltaY = point.y - last.y
        val distanceSquared = deltaX * deltaX + deltaY * deltaY
        if (distanceSquared == 0f) return
        val minimumDistance = dp(MIN_BRUSH_POINT_DISTANCE_DP) / imageScale
        if (force || distanceSquared >= minimumDistance * minimumDistance) stroke.points += point
    }

    private fun handleTextTouch(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                val point = viewToBitmap(event.x, event.y, clamp = true) ?: return false
                if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                    parent?.requestDisallowInterceptTouchEvent(true)
                }
                textSpec = textSpec?.copy(centerX = point.x, centerY = point.y)
                invalidate()
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                parent?.requestDisallowInterceptTouchEvent(false)
                if (event.actionMasked == MotionEvent.ACTION_UP) performClick()
                return true
            }
        }
        return false
    }

    private fun viewToBitmap(x: Float, y: Float, clamp: Boolean = false): ImageEditingEngine.Point? {
        if (!clamp && !imageRect.contains(x, y)) return null
        if (imageRect.isEmpty) return null
        return ImageEditingEngine.Point(
            ((x.coerceIn(imageRect.left, imageRect.right) - imageRect.left) / imageScale),
            ((y.coerceIn(imageRect.top, imageRect.bottom) - imageRect.top) / imageScale),
        )
    }

    private fun mosaicBitmap(source: Bitmap, blockSize: Int): Bitmap {
        val cache = mosaicBitmapCache ?: MosaicBitmapCache(source, MOSAIC_PREVIEW_CACHE_SIZE).also {
            mosaicBitmapCache = it
        }
        return cache.bitmap(blockSize)
    }

    private fun bitmapRectToView(rect: RectF) = RectF(
        imageRect.left + rect.left * imageScale,
        imageRect.top + rect.top * imageScale,
        imageRect.left + rect.right * imageScale,
        imageRect.top + rect.bottom * imageScale,
    )

    private fun RectF.toCropSelection() = CropSelection(left, top, right, bottom)

    private fun RectF.set(selection: CropSelection) {
        set(selection.left, selection.top, selection.right, selection.bottom)
    }

    private fun dp(value: Float): Float = value * resources.displayMetrics.density

    private companion object {
        const val CROP_HIT_SLOP_DP = 24f
        const val SHAPE_HIT_SLOP_DP = 20f
        const val SHAPE_HANDLE_RADIUS_DP = 7f
        const val MIN_SHAPE_SCREEN_SIZE_DP = 12f
        const val SHAPE_HANDLE_OUTLINE = 0xFF202020.toInt()
        const val MAGNIFIER_RADIUS_DP = 56f
        const val MAGNIFIER_GAP_DP = 40f
        const val MAGNIFIER_PIXELS = 11
        const val MAGNIFIER_BACKGROUND = 0xE0202020.toInt()
        const val OPAQUE = 0xFF000000.toInt()

        /** Fingers this close (mean distance from their center) only pan: their ratio would jump. */
        const val MIN_PINCH_SPAN_DP = 8f
        const val MIN_CROP_SCREEN_SIZE_DP = 48f
        const val MIN_CROP_BITMAP_SIZE = 8f
        const val MIN_BRUSH_POINT_DISTANCE_DP = 1.5f
        const val MOSAIC_PREVIEW_CACHE_SIZE = 4
        const val CROP_SHADE_COLOR = 0x99000000.toInt()
        const val GRID_ALPHA = 140
    }
}
