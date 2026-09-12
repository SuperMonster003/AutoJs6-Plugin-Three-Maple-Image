package io.github.supermonster003.autojs6.plugin.imagetools

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
import android.view.MotionEvent
import android.view.View
import androidx.core.graphics.withClip
import androidx.core.graphics.withRotation
import androidx.core.graphics.withScale
import androidx.core.graphics.withTranslation
import kotlin.math.abs
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
    }

    private enum class Mode { NORMAL, ROTATION, CROP, BRUSH, TEXT }

    private data class MutableStroke(
        val tool: BrushTool,
        val color: Int,
        val width: Float,
        val mosaicBlockSize: Int,
        val points: MutableList<ImageEditingEngine.Point>,
    )

    private val bitmapPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
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

    fun setBitmap(value: Bitmap?) {
        bitmap = value
        clearToolState()
        updateImageRect()
        invalidate()
    }

    fun setPreviewColorMatrix(matrix: ColorMatrix?) {
        bitmapPaint.colorFilter = matrix?.let(::ColorMatrixColorFilter)
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
        }
        invalidate()
    }

    fun clearToolState() {
        mode = Mode.NORMAL
        bitmapPaint.colorFilter = null
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
        invalidate()
    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)
        updateImageRect()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val source = bitmap ?: return
        if (imageRect.isEmpty) updateImageRect()
        if (mode == Mode.ROTATION) {
            drawRotationPreview(canvas, source)
        } else {
            canvas.drawBitmap(source, null, imageRect, bitmapPaint)
        }
        when (mode) {
            Mode.ROTATION -> Unit
            Mode.CROP -> drawCropOverlay(canvas)
            Mode.BRUSH -> drawBrushStrokes(canvas)
            Mode.TEXT -> drawTextOverlay(canvas)
            Mode.NORMAL -> Unit
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (bitmap == null || mode == Mode.NORMAL) return false
        return when (mode) {
            Mode.ROTATION -> false
            Mode.CROP -> handleCropTouch(event)
            Mode.BRUSH -> handleBrushTouch(event)
            Mode.TEXT -> handleTextTouch(event)
            Mode.NORMAL -> false
        }
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
        imageScale = min(width.toFloat() / source.width, height.toFloat() / source.height)
        val drawnWidth = source.width * imageScale
        val drawnHeight = source.height * imageScale
        val left = (width - drawnWidth) / 2f
        val top = (height - drawnHeight) / 2f
        imageRect.set(left, top, left + drawnWidth, top + drawnHeight)
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
        const val MIN_CROP_SCREEN_SIZE_DP = 48f
        const val MIN_CROP_BITMAP_SIZE = 8f
        const val MIN_BRUSH_POINT_DISTANCE_DP = 1.5f
        const val MOSAIC_PREVIEW_CACHE_SIZE = 4
        const val CROP_SHADE_COLOR = 0x99000000.toInt()
        const val GRID_ALPHA = 140
    }
}
