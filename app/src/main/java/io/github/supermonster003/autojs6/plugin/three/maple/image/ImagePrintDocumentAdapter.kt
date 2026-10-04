package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.os.OperationCanceledException
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.pdf.PrintedPdfDocument
import androidx.core.graphics.createBitmap
import java.io.OutputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.sqrt

/** An owned, in-memory copy of the EXIF-normalized drawable at the time printing is requested. */
internal class ImagePrintSnapshot private constructor(
    val bitmap: Bitmap,
    val rotationQuarterTurns: Int,
) : AutoCloseable {

    val outputWidth: Int
        get() = if (ImageRotationState.swapsDimensions(rotationQuarterTurns)) {
            bitmap.height
        } else {
            bitmap.width
        }

    val outputHeight: Int
        get() = if (ImageRotationState.swapsDimensions(rotationQuarterTurns)) {
            bitmap.width
        } else {
            bitmap.height
        }

    private val closed = AtomicBoolean(false)

    override fun close() {
        if (closed.compareAndSet(false, true) && !bitmap.isRecycled) bitmap.recycle()
    }

    companion object {
        private const val MAX_SNAPSHOT_PIXELS = 16_777_216L

        fun capture(drawable: Drawable, rotationQuarterTurns: Int): ImagePrintSnapshot? {
            val bitmap = try {
                if (drawable is BitmapDrawable) {
                    copyBitmap(drawable.bitmap)
                } else {
                    drawDrawable(drawable)
                }
            } catch (_: OutOfMemoryError) {
                null
            } catch (_: RuntimeException) {
                null
            } ?: return null

            return ImagePrintSnapshot(
                bitmap = bitmap,
                rotationQuarterTurns = ImageRotationState.normalize(rotationQuarterTurns),
            )
        }

        private fun copyBitmap(source: Bitmap): Bitmap? {
            if (source.isRecycled || source.width <= 0 || source.height <= 0) return null
            val (targetWidth, targetHeight) = boundedSize(source.width, source.height)
            var temporarySoftwareCopy: Bitmap? = null
            return try {
                val drawableSource = if (
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
                    source.config == Bitmap.Config.HARDWARE
                ) {
                    source.copy(Bitmap.Config.ARGB_8888, false)?.also {
                        temporarySoftwareCopy = it
                    } ?: return null
                } else {
                    source
                }

                if (targetWidth == drawableSource.width && targetHeight == drawableSource.height) {
                    temporarySoftwareCopy?.also { temporarySoftwareCopy = null }
                        ?: drawableSource.copy(Bitmap.Config.ARGB_8888, false)
                } else {
                    createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888).also {
                        Canvas(it).drawBitmap(
                            drawableSource,
                            null,
                            Rect(0, 0, targetWidth, targetHeight),
                            Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG),
                        )
                    }
                }
            } finally {
                temporarySoftwareCopy?.takeUnless(Bitmap::isRecycled)?.recycle()
            }
        }

        private fun drawDrawable(drawable: Drawable): Bitmap? {
            val sourceWidth = drawable.intrinsicWidth
            val sourceHeight = drawable.intrinsicHeight
            if (sourceWidth <= 0 || sourceHeight <= 0) return null
            val (targetWidth, targetHeight) = boundedSize(sourceWidth, sourceHeight)
            val bitmap = createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
            val originalBounds = Rect(drawable.bounds)
            return try {
                val canvas = Canvas(bitmap)
                canvas.scale(
                    targetWidth.toFloat() / sourceWidth,
                    targetHeight.toFloat() / sourceHeight,
                )
                drawable.setBounds(0, 0, sourceWidth, sourceHeight)
                drawable.draw(canvas)
                bitmap
            } catch (error: Throwable) {
                bitmap.recycle()
                throw error
            } finally {
                drawable.bounds = originalBounds
            }
        }

        private fun boundedSize(width: Int, height: Int): Pair<Int, Int> {
            val pixels = width.toLong() * height.toLong()
            if (pixels <= MAX_SNAPSHOT_PIXELS) return width to height
            val factor = sqrt(MAX_SNAPSHOT_PIXELS.toDouble() / pixels.toDouble())
            return max(1, floor(width * factor).toInt()) to
                max(1, floor(height * factor).toInt())
        }
    }
}

/** Synchronous PDF renderer used by the print adapter and instrumentation tests. */
internal object ImagePrintPdfWriter {

    fun write(
        context: Context,
        attributes: PrintAttributes,
        snapshot: ImagePrintSnapshot,
        output: OutputStream,
        cancellationSignal: CancellationSignal,
    ) {
        cancellationSignal.throwIfCanceled()
        check(!snapshot.bitmap.isRecycled) { "The print snapshot has already been released" }

        val document = PrintedPdfDocument(context, attributes)
        try {
            val contentRect = document.pageContentRect
            val layout = requireNotNull(
                ImageFitCenterTransform.calculate(
                    sourceWidth = snapshot.bitmap.width.toFloat(),
                    sourceHeight = snapshot.bitmap.height.toFloat(),
                    contentLeft = contentRect.left.toFloat(),
                    contentTop = contentRect.top.toFloat(),
                    contentWidth = contentRect.width().toFloat(),
                    contentHeight = contentRect.height().toFloat(),
                    quarterTurns = snapshot.rotationQuarterTurns,
                ),
            ) { "The selected print page has no drawable content area" }

            val page = document.startPage(0)
            page.canvas.drawColor(Color.WHITE)
            page.canvas.drawBitmap(
                snapshot.bitmap,
                Matrix().apply { setValues(layout.matrixValues) },
                Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG),
            )
            document.finishPage(page)
            cancellationSignal.throwIfCanceled()
            document.writeTo(output)
            cancellationSignal.throwIfCanceled()
        } finally {
            document.close()
        }
    }
}

/** One-page adapter that writes directly to the descriptor owned by Android's print framework. */
internal class ImagePrintDocumentAdapter(
    context: Context,
    private val documentName: String,
    private val snapshot: ImagePrintSnapshot,
    private val failureMessage: CharSequence,
    private val executor: ExecutorService = Executors.newSingleThreadExecutor { runnable ->
        Thread(runnable, "image-print-pdf").apply { isDaemon = true }
    },
) : PrintDocumentAdapter() {

    private val applicationContext = context.applicationContext
    private val disposed = AtomicBoolean(false)
    private var attributes: PrintAttributes? = null

    override fun onLayout(
        oldAttributes: PrintAttributes?,
        newAttributes: PrintAttributes,
        cancellationSignal: CancellationSignal,
        callback: LayoutResultCallback,
        extras: Bundle,
    ) {
        if (cancellationSignal.isCanceled) {
            callback.onLayoutCancelled()
            return
        }
        if (disposed.get()) {
            callback.onLayoutFailed(failureMessage)
            return
        }

        attributes = newAttributes
        callback.onLayoutFinished(
            PrintDocumentInfo.Builder(documentName)
                .setContentType(PrintDocumentInfo.CONTENT_TYPE_PHOTO)
                .setPageCount(PAGE_COUNT)
                .build(),
            oldAttributes != newAttributes,
        )
    }

    override fun onWrite(
        pages: Array<out PageRange>,
        destination: ParcelFileDescriptor,
        cancellationSignal: CancellationSignal,
        callback: WriteResultCallback,
    ) {
        val printAttributes = attributes
        if (
            disposed.get() ||
            printAttributes == null ||
            pages.none { range -> range.start <= PAGE_INDEX && range.end >= PAGE_INDEX }
        ) {
            runCatching { destination.close() }
            callback.onWriteFailed(failureMessage)
            return
        }

        try {
            executor.execute {
                try {
                    ParcelFileDescriptor.AutoCloseOutputStream(destination).use { output ->
                        ImagePrintPdfWriter.write(
                            context = applicationContext,
                            attributes = printAttributes,
                            snapshot = snapshot,
                            output = output,
                            cancellationSignal = cancellationSignal,
                        )
                    }
                    callback.onWriteFinished(arrayOf(PageRange(PAGE_INDEX, PAGE_INDEX)))
                } catch (_: OperationCanceledException) {
                    callback.onWriteCancelled()
                } catch (_: Throwable) {
                    runCatching { destination.close() }
                    callback.onWriteFailed(failureMessage)
                }
            }
        } catch (_: RejectedExecutionException) {
            runCatching { destination.close() }
            callback.onWriteFailed(failureMessage)
        }
    }

    override fun onFinish() {
        dispose()
    }

    fun dispose() {
        if (!disposed.compareAndSet(false, true)) return
        executor.shutdown()
        snapshot.close()
    }

    private companion object {
        const val PAGE_INDEX = 0
        const val PAGE_COUNT = 1
    }
}

internal object ImagePrintNames {
    private val unsafeFileNameCharacters = Regex("[\\\\/:*?\"<>|\\u0000-\\u001F]")

    fun pdfDocumentName(displayName: String): String {
        val safeName = displayName.replace(unsafeFileNameCharacters, "_").trim()
        val baseName = safeName.substringBeforeLast('.', safeName).trim().ifEmpty { "image" }
        return "$baseName.pdf"
    }
}
