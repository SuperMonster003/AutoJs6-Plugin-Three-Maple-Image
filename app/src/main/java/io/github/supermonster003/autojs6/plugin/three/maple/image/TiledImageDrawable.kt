package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapRegionDecoder
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.os.ParcelFileDescriptor
import java.io.IOException
import java.util.LinkedHashMap
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.hypot

private fun Bitmap.safeAllocationByteCount(): Long =
    if (isRecycled) 0L else runCatching { allocationByteCount.toLong() }.getOrDefault(0L)

private fun Bitmap.recycleIfNeeded() {
    if (!isRecycled) recycle()
}

/**
 * Bounded, asynchronous region-decoding drawable with an always-available sampled preview.
 *
 * The drawable exposes the EXIF-normalized intrinsic dimensions expected by ZoomableImageView.
 * Encoded source coordinates are transformed only while drawing, so every decoded tile remains
 * small even when the normalized image is larger than the device texture limit.
 */
internal class TiledImageDrawable private constructor(
    private val descriptor: ParcelFileDescriptor,
    private val decoder: BitmapRegionDecoder,
    private val preview: Bitmap,
    private val previewSampleSize: Int,
    private val sourceToDisplayMatrix: Matrix,
    private val displayToSourceMatrix: Matrix,
    private val rawWidth: Int,
    private val rawHeight: Int,
    private val displayWidth: Int,
    private val displayHeight: Int,
    private val cacheBudgetBytes: Long,
) : Drawable(), AutoCloseable {

    private data class DecodedTile(
        val sourceRectF: RectF,
        val bitmap: Bitmap,
    )

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG or Paint.DITHER_FLAG)
    private val rawBounds = Rect(0, 0, rawWidth, rawHeight)
    private val rawBoundsF = RectF(rawBounds)
    private val viewToDisplayMatrix = Matrix()
    private val visibleDisplayRect = RectF()
    private val visibleSourceRect = RectF()
    private val scaleMatrixValues = FloatArray(9)
    private val mainHandler = Handler(Looper.getMainLooper())
    private val closed = AtomicBoolean(false)
    private val desiredKeys = ConcurrentHashMap.newKeySet<ImageTileKey>()
    private val pendingKeys = ConcurrentHashMap.newKeySet<ImageTileKey>()
    private val failedKeys = ConcurrentHashMap.newKeySet<ImageTileKey>()
    private val executor = ThreadPoolExecutor(
        1,
        1,
        0L,
        TimeUnit.MILLISECONDS,
        LinkedBlockingQueue(),
    ) { runnable ->
        Thread(runnable, "image-region-decoder").apply { isDaemon = true }
    }
    private val tileCache = LinkedHashMap<ImageTileKey, DecodedTile>(16, 0.75f, true)
    private var tileCacheBytes = 0L
    private var visibleKeys: List<ImageTileKey> = emptyList()

    @Volatile
    internal var lastRequestedSampleSizeForTesting: Int = previewSampleSize
        private set

    internal val previewSampleSizeForTesting: Int
        get() = previewSampleSize

    internal val previewWidthForTesting: Int
        get() = preview.width

    internal val previewHeightForTesting: Int
        get() = preview.height

    internal val loadedTileCountForTesting: Int
        get() = tileCache.size

    internal val isClosedForTesting: Boolean
        get() = closed.get()

    override fun getIntrinsicWidth(): Int = displayWidth

    override fun getIntrinsicHeight(): Int = displayHeight

    @SuppressLint("UseKtx")
    override fun draw(canvas: Canvas) {
        if (closed.get() || preview.isRecycled || bounds.isEmpty) return
        val saveCount = canvas.save()
        canvas.translate(bounds.left.toFloat(), bounds.top.toFloat())
        canvas.scale(
            bounds.width().toFloat() / displayWidth.toFloat(),
            bounds.height().toFloat() / displayHeight.toFloat(),
        )
        canvas.concat(sourceToDisplayMatrix)
        canvas.drawBitmap(preview, null, rawBoundsF, paint)
        visibleKeys.forEach { key ->
            val tile = tileCache[key] ?: return@forEach
            if (!tile.bitmap.isRecycled) {
                canvas.drawBitmap(tile.bitmap, null, tile.sourceRectF, paint)
            }
        }
        canvas.restoreToCount(saveCount)
    }

    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha.coerceIn(0, 255)
        invalidateSelf()
    }

    override fun getAlpha(): Int = paint.alpha

    override fun setColorFilter(colorFilter: ColorFilter?) {
        paint.colorFilter = colorFilter
        invalidateSelf()
    }

    @Deprecated("Deprecated in Android")
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT

    /** Called by ZoomableImageView with its current image-to-view matrix on every draw. */
    fun updateViewport(displayMatrix: Matrix, contentBounds: RectF) {
        if (closed.get() || contentBounds.isEmpty) return
        if (!displayMatrix.invert(viewToDisplayMatrix)) return
        visibleDisplayRect.set(contentBounds)
        viewToDisplayMatrix.mapRect(visibleDisplayRect)
        if (!visibleDisplayRect.intersect(0f, 0f, displayWidth.toFloat(), displayHeight.toFloat())) {
            updateDesiredTiles(emptyList(), previewSampleSize)
            return
        }

        visibleSourceRect.set(visibleDisplayRect)
        displayToSourceMatrix.mapRect(visibleSourceRect)
        val sampleSize = ImageLargeImagePolicy.tileSampleSize(effectiveScale(displayMatrix))
        if (sampleSize >= previewSampleSize) {
            updateDesiredTiles(emptyList(), sampleSize)
            return
        }
        val tiles = ImageLargeImagePolicy.visibleTiles(
            rawWidth = rawWidth,
            rawHeight = rawHeight,
            visibleLeft = visibleSourceRect.left,
            visibleTop = visibleSourceRect.top,
            visibleRight = visibleSourceRect.right,
            visibleBottom = visibleSourceRect.bottom,
            sampleSize = sampleSize,
        )
        updateDesiredTiles(tiles, sampleSize)
    }

    fun trimTileCache() {
        if (tileCache.isEmpty()) return
        tileCache.values.forEach { tile -> tile.bitmap.recycleIfNeeded() }
        tileCache.clear()
        tileCacheBytes = 0L
        invalidateSelf()
    }

    private fun updateDesiredTiles(tiles: List<ImageTileSpec>, sampleSize: Int) {
        lastRequestedSampleSizeForTesting = sampleSize
        visibleKeys = tiles.map(ImageTileSpec::key)
        desiredKeys.clear()
        desiredKeys.addAll(visibleKeys)
        tiles.forEach(::requestTile)
    }

    private fun requestTile(spec: ImageTileSpec) {
        val key = spec.key
        if (
            closed.get() ||
            tileCache.containsKey(key) ||
            key in failedKeys ||
            !pendingKeys.add(key)
        ) {
            return
        }
        try {
            executor.execute {
                if (closed.get() || key !in desiredKeys) {
                    pendingKeys.remove(key)
                    return@execute
                }
                val decoded = decodeTile(spec)
                mainHandler.post {
                    pendingKeys.remove(key)
                    if (closed.get() || key !in desiredKeys) {
                        decoded?.bitmap?.recycleIfNeeded()
                        return@post
                    }
                    if (decoded == null) {
                        failedKeys.add(key)
                    } else {
                        putTile(key, decoded)
                        invalidateSelf()
                    }
                }
            }
        } catch (_: RuntimeException) {
            pendingKeys.remove(key)
            failedKeys.add(key)
        }
    }

    private fun decodeTile(spec: ImageTileSpec): DecodedTile? = try {
        val overlap = spec.key.sampleSize
        val sourceRect = Rect(
            (spec.left - overlap).coerceAtLeast(0),
            (spec.top - overlap).coerceAtLeast(0),
            (spec.right.toLong() + overlap.toLong()).coerceAtMost(rawWidth.toLong()).toInt(),
            (spec.bottom.toLong() + overlap.toLong()).coerceAtMost(rawHeight.toLong()).toInt(),
        )
        val bitmap = decoder.decodeRegion(
            sourceRect,
            BitmapFactory.Options().apply {
                inPreferredConfig = Bitmap.Config.ARGB_8888
                inSampleSize = spec.key.sampleSize
            },
        ) ?: return null
        DecodedTile(RectF(sourceRect), bitmap)
    } catch (_: IllegalArgumentException) {
        null
    } catch (_: IllegalStateException) {
        null
    } catch (_: OutOfMemoryError) {
        null
    }

    private fun putTile(key: ImageTileKey, tile: DecodedTile) {
        tileCache.remove(key)?.let { replaced ->
            tileCacheBytes -= replaced.bitmap.safeAllocationByteCount()
            replaced.bitmap.recycleIfNeeded()
        }
        tileCache[key] = tile
        tileCacheBytes += tile.bitmap.safeAllocationByteCount()
        val iterator = tileCache.entries.iterator()
        while (tileCacheBytes > cacheBudgetBytes && iterator.hasNext()) {
            val entry = iterator.next()
            iterator.remove()
            tileCacheBytes -= entry.value.bitmap.safeAllocationByteCount()
            entry.value.bitmap.recycleIfNeeded()
        }
    }

    override fun close() {
        if (!closed.compareAndSet(false, true)) return
        desiredKeys.clear()
        pendingKeys.clear()
        visibleKeys = emptyList()
        executor.queue.clear()
        trimTileCache()
        preview.recycleIfNeeded()
        executor.execute {
            runCatching { decoder.recycle() }
            runCatching { descriptor.close() }
        }
        executor.shutdown()
    }

    private fun effectiveScale(matrix: Matrix): Float {
        matrix.getValues(scaleMatrixValues)
        val horizontal = hypot(
            scaleMatrixValues[Matrix.MSCALE_X].toDouble(),
            scaleMatrixValues[Matrix.MSKEW_Y].toDouble(),
        ).toFloat()
        val vertical = hypot(
            scaleMatrixValues[Matrix.MSKEW_X].toDouble(),
            scaleMatrixValues[Matrix.MSCALE_Y].toDouble(),
        ).toFloat()
        return maxOf(horizontal, vertical)
    }

    companion object {

        fun create(
            descriptor: ParcelFileDescriptor,
            orientation: ImageExifOrientation?,
            cacheBudgetBytes: Long,
        ): TiledImageDrawable? {
            var decoder: BitmapRegionDecoder? = null
            var preview: Bitmap? = null
            try {
                if (!descriptor.fileDescriptor.valid()) return null.also { descriptor.close() }
                @Suppress("DEPRECATION")
                val createdDecoder = BitmapRegionDecoder.newInstance(
                    descriptor.fileDescriptor,
                    false,
                )
                decoder = createdDecoder
                val rawWidth = createdDecoder.width
                val rawHeight = createdDecoder.height
                if (rawWidth <= 0 || rawHeight <= 0) throw IOException("Invalid image bounds")
                val matrixValues = ImageExifOrientationCorrection.sourceToDisplayMatrixValues(
                    rawWidth,
                    rawHeight,
                    orientation,
                ) ?: throw IOException("Invalid EXIF orientation")
                val sourceToDisplay = Matrix().apply { setValues(matrixValues) }
                val displayToSource = Matrix()
                if (!sourceToDisplay.invert(displayToSource)) {
                    throw IOException("Invalid EXIF transform")
                }
                val (displayWidth, displayHeight) = ImageExifOrientationCorrection.displayDimensions(
                    rawWidth,
                    rawHeight,
                    orientation,
                )
                val normalizedWidth = displayWidth ?: throw IOException("Invalid display width")
                val normalizedHeight = displayHeight ?: throw IOException("Invalid display height")
                val previewSample = ImageLargeImagePolicy.previewSampleSize(rawWidth, rawHeight)
                val createdPreview = createdDecoder.decodeRegion(
                    Rect(0, 0, rawWidth, rawHeight),
                    BitmapFactory.Options().apply {
                        inPreferredConfig = Bitmap.Config.ARGB_8888
                        inSampleSize = previewSample
                    },
                ) ?: throw IOException("Unable to decode a large-image preview")
                preview = createdPreview
                return TiledImageDrawable(
                    descriptor = descriptor,
                    decoder = createdDecoder,
                    preview = createdPreview,
                    previewSampleSize = previewSample,
                    sourceToDisplayMatrix = sourceToDisplay,
                    displayToSourceMatrix = displayToSource,
                    rawWidth = rawWidth,
                    rawHeight = rawHeight,
                    displayWidth = normalizedWidth,
                    displayHeight = normalizedHeight,
                    cacheBudgetBytes = cacheBudgetBytes.coerceAtLeast(1L),
                )
            } catch (_: IOException) {
                // Unsupported/non-seekable sources keep the existing bounded Glide fallback.
            } catch (_: IllegalArgumentException) {
                // Malformed bounds or decoder options are treated as an unsupported source.
            } catch (_: IllegalStateException) {
                // A provider may invalidate a descriptor while the decoder is being created.
            } catch (_: OutOfMemoryError) {
                // Preview creation is bounded, but allocation can still fail under memory pressure.
            }
            preview?.recycleIfNeeded()
            decoder?.let { runCatching { it.recycle() } }
            runCatching { descriptor.close() }
            return null
        }
    }
}
