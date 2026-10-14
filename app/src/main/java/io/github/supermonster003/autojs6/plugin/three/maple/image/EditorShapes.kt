package io.github.supermonster003.autojs6.plugin.three.maple.image

import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/** Annotation shapes of the editor (ROADMAP 8.1). */
internal enum class ShapeKind {
    RECTANGLE,
    ELLIPSE,
    ARROW,
    LINE,
    ;

    /** Rectangles and ellipses may be filled; lines and arrows are always strokes. */
    val fillable: Boolean get() = this == RECTANGLE || this == ELLIPSE

    /** Rectangles and ellipses are spanned by two opposite corners, lines and arrows run from start to end. */
    val boxed: Boolean get() = fillable
}

internal data class ShapePoint(val x: Float, val y: Float)

/**
 * One annotation shape in bitmap pixels. For [ShapeKind.boxed] kinds the start and end are opposite corners in
 * any order; for lines and arrows the arrow points at the end.
 */
internal data class EditorShape(
    val kind: ShapeKind,
    val startX: Float,
    val startY: Float,
    val endX: Float,
    val endY: Float,
    val color: Int,
    val strokeWidth: Float,
    val filled: Boolean = false,
) {
    val left: Float get() = min(startX, endX)
    val top: Float get() = min(startY, endY)
    val right: Float get() = max(startX, endX)
    val bottom: Float get() = max(startY, endY)
    val length: Float get() = hypot(endX - startX, endY - startY)

    /** Filling only applies to the kinds that can be filled. */
    val drawsFilled: Boolean get() = filled && kind.fillable
}

/** Android-free geometry of the annotation shapes: handles, hit tests, moving, minimum size and the arrow head. */
internal object ShapeGeometry {

    const val ARROW_HEAD_ANGLE_DEGREES = 28.0

    /** The arrow head is this many stroke widths long, but at least [MIN_ARROW_HEAD] and at most half the arrow. */
    const val ARROW_HEAD_WIDTHS = 4f
    const val MIN_ARROW_HEAD = 6f

    /** Corners in the order start, (end x, start y), end, (start x, end y) for boxed kinds; start and end otherwise. */
    fun handles(shape: EditorShape): List<ShapePoint> = if (shape.kind.boxed) {
        listOf(
            ShapePoint(shape.startX, shape.startY),
            ShapePoint(shape.endX, shape.startY),
            ShapePoint(shape.endX, shape.endY),
            ShapePoint(shape.startX, shape.endY),
        )
    } else {
        listOf(ShapePoint(shape.startX, shape.startY), ShapePoint(shape.endX, shape.endY))
    }

    /** The handle nearest to ([x], [y]) within [tolerance], or null. */
    fun hitHandle(shape: EditorShape, x: Float, y: Float, tolerance: Float): Int? =
        handles(shape)
            .mapIndexed { index, point -> index to hypot(point.x - x, point.y - y) }
            .filter { it.second <= tolerance }
            .minByOrNull { it.second }
            ?.first

    /**
     * Whether ([x], [y]) touches [shape]: anywhere inside a box (hollow ones too, so that they are easy to pick up),
     * near the segment of a line or arrow.
     */
    fun hitShape(shape: EditorShape, x: Float, y: Float, tolerance: Float): Boolean {
        val reach = tolerance + shape.strokeWidth / 2f
        return if (shape.kind.boxed) {
            x >= shape.left - reach && x <= shape.right + reach && y >= shape.top - reach && y <= shape.bottom + reach
        } else {
            distanceToSegment(x, y, shape.startX, shape.startY, shape.endX, shape.endY) <= reach
        }
    }

    /** The topmost shape (the last drawn) touched at ([x], [y]), or null. */
    fun topmostHit(shapes: List<EditorShape>, x: Float, y: Float, tolerance: Float): Int? =
        shapes.indices.lastOrNull { hitShape(shapes[it], x, y, tolerance) }

    /**
     * [shape] with handle [handle] dragged to ([x], [y]); a result smaller than [minimumSize] keeps [shape] (a
     * shape never collapses under the finger).
     */
    fun withHandle(shape: EditorShape, handle: Int, x: Float, y: Float, minimumSize: Float): EditorShape {
        val moved = if (shape.kind.boxed) {
            when (handle) {
                0 -> shape.copy(startX = x, startY = y)
                1 -> shape.copy(endX = x, startY = y)
                2 -> shape.copy(endX = x, endY = y)
                3 -> shape.copy(startX = x, endY = y)
                else -> shape
            }
        } else {
            when (handle) {
                0 -> shape.copy(startX = x, startY = y)
                1 -> shape.copy(endX = x, endY = y)
                else -> shape
            }
        }
        return if (isTooSmall(moved, minimumSize)) shape else moved
    }

    /** [shape] moved by ([deltaX], [deltaY]) but kept inside a [width] x [height] image as far as it fits. */
    fun moved(shape: EditorShape, deltaX: Float, deltaY: Float, width: Float, height: Float): EditorShape {
        val dx = clampDelta(deltaX, shape.left, shape.right, width)
        val dy = clampDelta(deltaY, shape.top, shape.bottom, height)
        return shape.copy(startX = shape.startX + dx, startY = shape.startY + dy, endX = shape.endX + dx, endY = shape.endY + dy)
    }

    /**
     * A drag shorter than [minimumSize] makes no shape (it was a tap). Boxes need both sides, so that a thin
     * rectangle stays visible; lines and arrows need the length.
     */
    fun isTooSmall(shape: EditorShape, minimumSize: Float): Boolean = if (shape.kind.boxed) {
        abs(shape.endX - shape.startX) < minimumSize || abs(shape.endY - shape.startY) < minimumSize
    } else {
        shape.length < minimumSize
    }

    fun arrowHeadLength(shape: EditorShape): Float =
        min(max(shape.strokeWidth * ARROW_HEAD_WIDTHS, MIN_ARROW_HEAD), shape.length / 2f)

    /** The tip (the end) and the two barbs of the arrow head. */
    fun arrowHead(shape: EditorShape): List<ShapePoint> {
        val angle = atan2((shape.endY - shape.startY).toDouble(), (shape.endX - shape.startX).toDouble())
        val head = arrowHeadLength(shape).toDouble()
        val spread = Math.toRadians(ARROW_HEAD_ANGLE_DEGREES)
        fun barb(sign: Int) = ShapePoint(
            (shape.endX - head * cos(angle - sign * spread)).toFloat(),
            (shape.endY - head * sin(angle - sign * spread)).toFloat(),
        )
        return listOf(ShapePoint(shape.endX, shape.endY), barb(1), barb(-1))
    }

    /** Where the shaft of an arrow stops, inside the head, so that a thick shaft does not poke out at the tip. */
    fun arrowShaftEnd(shape: EditorShape): ShapePoint {
        val length = shape.length
        if (length == 0f) return ShapePoint(shape.endX, shape.endY)
        val back = arrowHeadLength(shape) * cos(Math.toRadians(ARROW_HEAD_ANGLE_DEGREES)).toFloat() * 0.8f
        val t = ((length - back) / length).coerceIn(0f, 1f)
        return ShapePoint(shape.startX + (shape.endX - shape.startX) * t, shape.startY + (shape.endY - shape.startY) * t)
    }

    fun distanceToSegment(x: Float, y: Float, x1: Float, y1: Float, x2: Float, y2: Float): Float {
        val dx = x2 - x1
        val dy = y2 - y1
        val lengthSquared = dx * dx + dy * dy
        if (lengthSquared == 0f) return hypot(x - x1, y - y1)
        val t = (((x - x1) * dx + (y - y1) * dy) / lengthSquared).coerceIn(0f, 1f)
        return hypot(x - (x1 + t * dx), y - (y1 + t * dy))
    }

    private fun clampDelta(delta: Float, low: Float, high: Float, limit: Float): Float {
        // A shape larger than the image may still move, but not further out than it already is.
        val minDelta = min(0f, -low)
        val maxDelta = max(0f, limit - high)
        return delta.coerceIn(minDelta, maxDelta)
    }
}
