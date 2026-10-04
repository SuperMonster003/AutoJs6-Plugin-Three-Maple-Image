package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.graphics.Shader
import androidx.core.graphics.scale
import androidx.core.graphics.withClip
import kotlin.math.min

internal enum class BrushTool {
    PEN,
    HIGHLIGHTER,
    MOSAIC,
    ERASER,
}

internal object BrushStrokeRenderer {

    fun draw(
        canvas: Canvas,
        source: Bitmap,
        destination: RectF,
        tool: BrushTool,
        color: Int,
        width: Float,
        mosaicBlockSize: Int,
        points: List<ImageEditingEngine.Point>,
        mosaicBitmap: (Int) -> Bitmap,
    ) {
        if (points.isEmpty()) return
        require(!destination.isEmpty) { "Brush destination must be non-empty" }
        require(width.isFinite() && width > 0f) { "Brush width must be positive" }
        val scaleX = destination.width() / source.width
        val scaleY = destination.height() / source.height
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = if (points.size == 1) Paint.Style.FILL else Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            strokeWidth = width * min(scaleX, scaleY)
            when (tool) {
                BrushTool.PEN -> this.color = color
                BrushTool.HIGHLIGHTER -> this.color = highlighterColor(color)
                BrushTool.MOSAIC -> {
                    shader = bitmapShader(mosaicBitmap(mosaicBlockSize), destination)
                    xfermode = SOURCE_REPLACE_XFERMODE
                    isFilterBitmap = false
                }
                BrushTool.ERASER -> {
                    shader = bitmapShader(source, destination)
                    xfermode = SOURCE_REPLACE_XFERMODE
                    isFilterBitmap = true
                }
            }
        }
        canvas.withClip(destination) {
            if (points.size == 1) {
                val point = mapPoint(points.first(), destination, scaleX, scaleY)
                drawCircle(point.x, point.y, paint.strokeWidth / 2f, paint)
            } else {
                val first = mapPoint(points.first(), destination, scaleX, scaleY)
                val path = Path().apply {
                    moveTo(first.x, first.y)
                    for (index in 1 until points.size) {
                        val point = mapPoint(points[index], destination, scaleX, scaleY)
                        lineTo(point.x, point.y)
                    }
                }
                drawPath(path, paint)
            }
        }
    }

    private fun bitmapShader(bitmap: Bitmap, destination: RectF): BitmapShader =
        BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP).apply {
            val source = RectF(0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat())
            setLocalMatrix(Matrix().apply {
                setRectToRect(source, destination, Matrix.ScaleToFit.FILL)
            })
        }

    private fun mapPoint(
        point: ImageEditingEngine.Point,
        destination: RectF,
        scaleX: Float,
        scaleY: Float,
    ) = ImageEditingEngine.Point(
        x = destination.left + point.x * scaleX,
        y = destination.top + point.y * scaleY,
    )

    private fun highlighterColor(color: Int): Int {
        val alpha = (Color.alpha(color) * HIGHLIGHTER_ALPHA + 127) / 255
        return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color))
    }

    private val SOURCE_REPLACE_XFERMODE = PorterDuffXfermode(PorterDuff.Mode.SRC)
    private const val HIGHLIGHTER_ALPHA = 96
}

internal class MosaicBitmapCache(
    private val source: Bitmap,
    private val maxEntries: Int,
) : AutoCloseable {

    private val entries = LinkedHashMap<Int, Bitmap>(maxEntries, 0.75f, true)

    init {
        require(maxEntries > 0) { "Mosaic cache entry limit must be positive" }
    }

    fun bitmap(blockSize: Int): Bitmap {
        require(blockSize >= MIN_MOSAIC_BLOCK_SIZE) {
            "Mosaic block size must be at least $MIN_MOSAIC_BLOCK_SIZE pixels"
        }
        entries[blockSize]?.let { return it }
        check(!source.isRecycled) { "Mosaic source bitmap has been recycled" }
        val scaledWidth = reducedDimension(source.width, blockSize)
        val scaledHeight = reducedDimension(source.height, blockSize)
        val bitmap = if (scaledWidth == source.width && scaledHeight == source.height) {
            source
        } else {
            source.scale(scaledWidth, scaledHeight, filter = true)
        }
        entries[blockSize] = bitmap
        trimToLimit()
        return bitmap
    }

    override fun close() {
        entries.values.toSet().forEach(::recycleOwned)
        entries.clear()
    }

    private fun trimToLimit() {
        while (entries.size > maxEntries) {
            val eldest = entries.entries.iterator().next()
            entries.remove(eldest.key)
            recycleOwned(eldest.value)
        }
    }

    private fun recycleOwned(bitmap: Bitmap) {
        if (bitmap !== source && !bitmap.isRecycled) bitmap.recycle()
    }

    private fun reducedDimension(dimension: Int, blockSize: Int): Int =
        ((dimension.toLong() + blockSize - 1L) / blockSize).coerceAtLeast(1L).toInt()

    companion object {
        const val MIN_MOSAIC_BLOCK_SIZE = 4
    }
}
