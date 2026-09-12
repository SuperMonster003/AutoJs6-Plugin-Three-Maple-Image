package io.github.supermonster003.autojs6.plugin.imagetools

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import androidx.core.graphics.withRotation
import androidx.core.graphics.withScale
import androidx.core.graphics.withTranslation
import kotlin.math.max

internal object ImageEditingEngine {

    enum class Adjustment {
        BRIGHTNESS,
        CONTRAST,
        SATURATION,
        TEMPERATURE,
    }

    data class Point(val x: Float, val y: Float)

    data class BrushStroke(
        val tool: BrushTool,
        val color: Int,
        val width: Float,
        val mosaicBlockSize: Int,
        val points: List<Point>,
    )

    data class TextSpec(
        val text: String,
        val color: Int,
        val size: Float,
        val centerX: Float,
        val centerY: Float,
        val outlineEnabled: Boolean = false,
        val shadowEnabled: Boolean = false,
        val rotationDegrees: Float = 0f,
    ) {
        init {
            require(text.isNotEmpty()) { "Text must not be empty" }
            require(size > 0f && size.isFinite()) { "Text size must be positive and finite" }
            require(centerX.isFinite() && centerY.isFinite()) { "Text center must be finite" }
            require(rotationDegrees.isFinite() && rotationDegrees in -180f..180f) {
                "Text rotation must be between -180 and 180 degrees"
            }
        }
    }

    fun transform(
        source: Bitmap,
        rotation: Float = 0f,
        flipX: Boolean = false,
        flipY: Boolean = false,
    ): Bitmap {
        val matrix = Matrix().apply {
            if (rotation != 0f) postRotate(rotation)
            if (flipX || flipY) {
                postScale(if (flipX) -1f else 1f, if (flipY) -1f else 1f)
            }
        }
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }

    fun rotateAndCrop(
        source: Bitmap,
        rotation: Float,
        maxPixelCount: Long = ImageBitmapIO.editingPixelBudget(),
    ): Bitmap {
        val plan = ImageRotationGeometry.plan(
            sourceWidth = source.width,
            sourceHeight = source.height,
            degrees = rotation,
            maxPixelCount = maxPixelCount,
        )
        val target = blankBitmapLike(source, plan.outputWidth, plan.outputHeight)
        return try {
            Canvas(target).withTranslation(plan.outputWidth / 2f, plan.outputHeight / 2f) {
                withRotation(plan.degrees) {
                    withScale(plan.coverScale, plan.coverScale) {
                        drawBitmap(
                            source,
                            -source.width / 2f,
                            -source.height / 2f,
                            Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG or Paint.DITHER_FLAG),
                        )
                    }
                }
            }
            target
        } catch (error: Throwable) {
            target.recycle()
            throw error
        }
    }

    fun crop(source: Bitmap, selection: Rect): Bitmap {
        val safe = Rect(
            selection.left.coerceIn(0, source.width - 1),
            selection.top.coerceIn(0, source.height - 1),
            selection.right.coerceIn(1, source.width),
            selection.bottom.coerceIn(1, source.height),
        )
        if (safe.width() < MIN_CROP_SIZE || safe.height() < MIN_CROP_SIZE) {
            throw IllegalArgumentException("Crop selection is too small")
        }
        return Bitmap.createBitmap(source, safe.left, safe.top, safe.width(), safe.height())
    }

    fun colorMatrix(adjustment: Adjustment, value: Int): ColorMatrix {
        val normalized = value.coerceIn(-100, 100) / 100f
        return when (adjustment) {
            Adjustment.BRIGHTNESS -> {
                val offset = normalized * 255f
                ColorMatrix(
                    floatArrayOf(
                        1f, 0f, 0f, 0f, offset,
                        0f, 1f, 0f, 0f, offset,
                        0f, 0f, 1f, 0f, offset,
                        0f, 0f, 0f, 1f, 0f,
                    ),
                )
            }
            Adjustment.CONTRAST -> {
                val scale = max(0.05f, 1f + normalized)
                val offset = 128f * (1f - scale)
                ColorMatrix(
                    floatArrayOf(
                        scale, 0f, 0f, 0f, offset,
                        0f, scale, 0f, 0f, offset,
                        0f, 0f, scale, 0f, offset,
                        0f, 0f, 0f, 1f, 0f,
                    ),
                )
            }
            Adjustment.SATURATION -> ColorMatrix().apply {
                setSaturation((1f + normalized).coerceAtLeast(0f))
            }
            Adjustment.TEMPERATURE -> {
                val red = 1f + normalized * 0.35f
                val green = 1f + normalized * 0.08f
                val blue = 1f - normalized * 0.35f
                ColorMatrix().apply { setScale(red, green, blue, 1f) }
            }
        }
    }

    fun applyAdjustment(source: Bitmap, adjustment: Adjustment, value: Int): Bitmap {
        val target = blankBitmapLike(source, source.width, source.height)
        return try {
            Canvas(target).drawBitmap(
                source,
                0f,
                0f,
                Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
                    colorFilter = ColorMatrixColorFilter(colorMatrix(adjustment, value))
                },
            )
            target
        } catch (error: Throwable) {
            target.recycle()
            throw error
        }
    }

    fun applyBrush(source: Bitmap, strokes: List<BrushStroke>): Bitmap {
        val target = mutableCopy(source)
        val mosaicCache = MosaicBitmapCache(source, maxEntries = 1)
        return try {
            val canvas = Canvas(target)
            strokes.forEach { stroke ->
                BrushStrokeRenderer.draw(
                    canvas = canvas,
                    source = source,
                    destination = RectF(0f, 0f, source.width.toFloat(), source.height.toFloat()),
                    tool = stroke.tool,
                    color = stroke.color,
                    width = stroke.width,
                    mosaicBlockSize = stroke.mosaicBlockSize,
                    points = stroke.points,
                    mosaicBitmap = mosaicCache::bitmap,
                )
            }
            target
        } catch (error: Throwable) {
            target.recycle()
            throw error
        } finally {
            mosaicCache.close()
        }
    }

    fun addText(source: Bitmap, spec: TextSpec): Bitmap {
        val target = mutableCopy(source)
        return try {
            StyledTextRenderer.draw(Canvas(target), spec)
            target
        } catch (error: Throwable) {
            target.recycle()
            throw error
        }
    }

    private fun mutableCopy(source: Bitmap): Bitmap =
        source.copy(Bitmap.Config.ARGB_8888, true)
            ?: throw IllegalStateException("Unable to allocate edited bitmap")

    private fun blankBitmapLike(source: Bitmap, width: Int, height: Int): Bitmap =
        Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).also {
            it.setHasAlpha(source.hasAlpha())
        }

    private const val MIN_CROP_SIZE = 1
}
