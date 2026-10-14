package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path

/** Draws [EditorShape]s in bitmap coordinates, the same on the canvas preview and on the saved image. */
internal object ShapeRenderer {

    fun draw(canvas: Canvas, shape: EditorShape, paint: Paint = newPaint()) {
        paint.color = shape.color
        paint.strokeWidth = shape.strokeWidth
        when (shape.kind) {
            ShapeKind.RECTANGLE -> {
                paint.style = if (shape.drawsFilled) Paint.Style.FILL else Paint.Style.STROKE
                paint.strokeJoin = Paint.Join.MITER
                canvas.drawRect(shape.left, shape.top, shape.right, shape.bottom, paint)
            }
            ShapeKind.ELLIPSE -> {
                paint.style = if (shape.drawsFilled) Paint.Style.FILL else Paint.Style.STROKE
                canvas.drawOval(shape.left, shape.top, shape.right, shape.bottom, paint)
            }
            ShapeKind.LINE -> {
                paint.style = Paint.Style.STROKE
                paint.strokeCap = Paint.Cap.ROUND
                canvas.drawLine(shape.startX, shape.startY, shape.endX, shape.endY, paint)
            }
            ShapeKind.ARROW -> {
                paint.style = Paint.Style.STROKE
                paint.strokeCap = Paint.Cap.ROUND
                val shaftEnd = ShapeGeometry.arrowShaftEnd(shape)
                canvas.drawLine(shape.startX, shape.startY, shaftEnd.x, shaftEnd.y, paint)
                val (tip, left, right) = ShapeGeometry.arrowHead(shape)
                paint.style = Paint.Style.FILL_AND_STROKE
                paint.strokeJoin = Paint.Join.ROUND
                // A thin outline of the head rounds its corners the way the shaft is rounded.
                paint.strokeWidth = shape.strokeWidth / 2f
                canvas.drawPath(
                    Path().apply {
                        moveTo(tip.x, tip.y)
                        lineTo(left.x, left.y)
                        lineTo(right.x, right.y)
                        close()
                    },
                    paint,
                )
            }
        }
    }

    fun newPaint(): Paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG)
}
