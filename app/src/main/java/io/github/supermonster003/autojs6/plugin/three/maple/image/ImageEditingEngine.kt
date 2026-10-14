package io.github.supermonster003.autojs6.plugin.three.maple.image

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

internal object ImageEditingEngine {

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

    /**
     * Applies an [EditorLook] (ROADMAP 8.1): a color-matrix-only look draws once through a color filter; a look
     * with a tone curve, sharpening or vignette goes through [LookPixelPass] in bands of rows, so even a 20 MP image
     * needs only a few megabytes of pixel buffers on top of the two bitmaps.
     */
    fun applyLook(source: Bitmap, look: EditorLook): Bitmap {
        val target = blankBitmapLike(source, source.width, source.height)
        return try {
            if (!look.needsPixelPass) {
                Canvas(target).drawBitmap(
                    source,
                    0f,
                    0f,
                    Paint(Paint.FILTER_BITMAP_FLAG).apply { colorFilter = ColorMatrixColorFilter(ColorMatrix(look.colorMatrix())) },
                )
            } else {
                val width = source.width
                val height = source.height
                val pass = LookPixelPass(look, width, height)
                val bandRows = (LOOK_BAND_PIXELS / width).coerceIn(1, height)
                val input = IntArray((bandRows + 2 * pass.halo) * width)
                val output = IntArray(bandRows * width)
                var top = 0
                while (top < height) {
                    val bottom = minOf(height, top + bandRows)
                    val inputTop = maxOf(0, top - pass.halo)
                    val inputBottom = minOf(height, bottom + pass.halo)
                    val inputPixels = (inputBottom - inputTop) * width
                    val band = if (inputPixels == input.size) input else IntArray(inputPixels)
                    source.getPixels(band, 0, width, 0, inputTop, width, inputBottom - inputTop)
                    val out = if ((bottom - top) * width == output.size) output else IntArray((bottom - top) * width)
                    pass.process(band, inputTop, top, bottom, out)
                    target.setPixels(out, 0, width, 0, top, width, bottom - top)
                    top = bottom
                }
            }
            target
        } catch (error: Throwable) {
            target.recycle()
            throw error
        }
    }

    /** [source] scaled down to at most [maxPixels] (never up), for previews; the source itself when it is small. */
    fun downscale(source: Bitmap, maxPixels: Long): Bitmap {
        val pixels = source.width.toLong() * source.height
        if (pixels <= maxPixels) return source
        val factor = kotlin.math.sqrt(maxPixels.toDouble() / pixels)
        val width = (source.width * factor).toInt().coerceAtLeast(1)
        val height = (source.height * factor).toInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(source, width, height, true)
    }

    /** One [size] x [size] center-cropped thumbnail of [source] per [FilterPreset], in enum order. */
    fun filterThumbnails(source: Bitmap, size: Int): List<Bitmap> {
        val side = minOf(source.width, source.height)
        val crop = Rect((source.width - side) / 2, (source.height - side) / 2, (source.width + side) / 2, (source.height + side) / 2)
        val destination = RectF(0f, 0f, size.toFloat(), size.toFloat())
        return FilterPreset.entries.map { preset ->
            Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888).also { thumbnail ->
                Canvas(thumbnail).drawBitmap(
                    source,
                    crop,
                    destination,
                    Paint(Paint.FILTER_BITMAP_FLAG).apply { colorFilter = ColorMatrixColorFilter(ColorMatrix(preset.matrix)) },
                )
            }
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

    /** About 1 MP per band of rows (4 MB of input plus 4 MB of output pixels). */
    private const val LOOK_BAND_PIXELS = 1_048_576
}
