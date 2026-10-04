package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.withRotation
import kotlin.math.max

internal data class TextLayoutPlan(
    val baselines: List<Float>,
    val lineHeight: Float,
    val blockHeight: Float,
)

internal object TextLayoutGeometry {

    fun plan(
        lineCount: Int,
        centerY: Float,
        fontAscent: Float,
        fontDescent: Float,
        lineSpacing: Float = StyledTextRenderer.LINE_SPACING,
    ): TextLayoutPlan {
        require(lineCount > 0) { "Text layout needs at least one line" }
        require(centerY.isFinite()) { "Text center must be finite" }
        require(fontAscent.isFinite() && fontDescent.isFinite() && fontDescent > fontAscent) {
            "Invalid font metrics"
        }
        require(lineSpacing >= 1f && lineSpacing.isFinite()) { "Invalid line spacing" }

        val glyphHeight = fontDescent - fontAscent
        val lineHeight = glyphHeight * lineSpacing
        val blockHeight = glyphHeight + (lineCount - 1) * lineHeight
        val firstBaseline = centerY - blockHeight / 2f - fontAscent
        return TextLayoutPlan(
            baselines = List(lineCount) { index -> firstBaseline + index * lineHeight },
            lineHeight = lineHeight,
            blockHeight = blockHeight,
        )
    }
}

/** Draws committed text and its interactive preview with exactly the same layout. */
internal object StyledTextRenderer {

    const val LINE_SPACING = 1.12f

    fun draw(canvas: Canvas, spec: ImageEditingEngine.TextSpec) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
            textAlign = Paint.Align.CENTER
            textSize = spec.size.coerceAtLeast(1f)
        }
        val lines = textLines(spec.text)
        val metrics = paint.fontMetrics
        val layout = TextLayoutGeometry.plan(
            lineCount = lines.size,
            centerY = spec.centerY,
            fontAscent = metrics.ascent,
            fontDescent = metrics.descent,
        )

        canvas.withRotation(spec.rotationDegrees, spec.centerX, spec.centerY) {
            if (spec.shadowEnabled) {
                val shadowOffset = max(MIN_EFFECT_SIZE, spec.size * SHADOW_OFFSET_RATIO)
                paint.style = Paint.Style.FILL
                paint.color = Color.argb(
                    Color.alpha(spec.color) * SHADOW_ALPHA / 255,
                    0,
                    0,
                    0,
                )
                drawLines(this, paint, spec.centerX + shadowOffset, layout, lines, shadowOffset)
            }
            if (spec.outlineEnabled) {
                paint.style = Paint.Style.STROKE
                paint.strokeJoin = Paint.Join.ROUND
                paint.strokeWidth = max(MIN_EFFECT_SIZE, spec.size * OUTLINE_WIDTH_RATIO)
                paint.color = contrastingColor(spec.color)
                drawLines(this, paint, spec.centerX, layout, lines)
            }
            paint.style = Paint.Style.FILL
            paint.color = spec.color
            drawLines(this, paint, spec.centerX, layout, lines)
        }
    }

    internal fun textLines(text: String): List<String> =
        text.split('\n').map { line -> line.removeSuffix("\r") }.ifEmpty { listOf("") }

    private fun drawLines(
        canvas: Canvas,
        paint: Paint,
        centerX: Float,
        layout: TextLayoutPlan,
        lines: List<String>,
        baselineOffset: Float = 0f,
    ) {
        lines.forEachIndexed { index, line ->
            canvas.drawText(line, centerX, layout.baselines[index] + baselineOffset, paint)
        }
    }

    private fun contrastingColor(color: Int): Int {
        val rgb = if (ColorUtils.calculateLuminance(color) > LIGHT_COLOR_THRESHOLD) {
            Color.BLACK
        } else {
            Color.WHITE
        }
        return Color.argb(Color.alpha(color), Color.red(rgb), Color.green(rgb), Color.blue(rgb))
    }

    private const val OUTLINE_WIDTH_RATIO = 0.08f
    private const val SHADOW_OFFSET_RATIO = 0.10f
    private const val MIN_EFFECT_SIZE = 1f
    private const val SHADOW_ALPHA = 150
    private const val LIGHT_COLOR_THRESHOLD = 0.5
}
