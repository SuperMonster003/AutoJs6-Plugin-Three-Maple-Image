package io.github.supermonster003.autojs6.plugin.imagetools

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.os.ParcelFileDescriptor
import androidx.exifinterface.media.ExifInterface
import java.io.FilterOutputStream
import java.io.IOException
import java.io.OutputStream

internal object ImageBitmapIO {

    data class SourceInfo(
        val rawSize: ImagePixelSize,
        val displaySize: ImagePixelSize,
        val mimeType: String,
        val orientation: Int,
    )

    class OutputLimitExceededException(val limitBytes: Long) :
        IOException("Encoded image exceeds the $limitBytes byte output limit")

    fun inspect(resolver: ContentResolver, uri: Uri): SourceInfo {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        decodeStream(resolver, uri, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
            throw IOException("Unable to decode image bounds")
        }
        val detectedMimeType = bounds.outMimeType
            ?.lowercase()
            ?.takeIf { it.startsWith("image/") }
            ?: throw IOException("Input is not a recognized image")
        val orientation = readExifOrientation(resolver, uri)
        val rawSize = ImagePixelSize(bounds.outWidth, bounds.outHeight)
        val displaySize = if (orientation.swapsDimensions()) {
            ImagePixelSize(bounds.outHeight, bounds.outWidth)
        } else {
            rawSize
        }
        return SourceInfo(rawSize, displaySize, detectedMimeType, orientation)
    }

    fun decodeForEditing(resolver: ContentResolver, uri: Uri): Bitmap {
        val info = inspect(resolver, uri)
        val pixelBudget = editingPixelBudget()
        var sampleSize = 1
        while (
            sampledDimension(info.rawSize.width, sampleSize) *
            sampledDimension(info.rawSize.height, sampleSize) > pixelBudget
        ) {
            sampleSize *= 2
        }
        val decoded = decodeBitmap(resolver, uri, sampleSize, "edit")
        return try {
            applyExifOrientation(info.orientation, decoded)
        } catch (error: Throwable) {
            if (!decoded.isRecycled) decoded.recycle()
            throw error
        }
    }

    fun decodeForConversion(resolver: ContentResolver, uri: Uri): Bitmap {
        val info = inspect(resolver, uri)
        if (info.displaySize.pixelCount > ImageConversionSizing.MAX_PIXEL_COUNT) {
            throw IOException("Image is too large to convert without changing its resolution")
        }
        return decodeForConversion(resolver, uri, info.displaySize, info)
    }

    fun decodeForConversion(
        resolver: ContentResolver,
        uri: Uri,
        targetSize: ImagePixelSize,
    ): Bitmap = decodeForConversion(resolver, uri, targetSize, inspect(resolver, uri))

    fun isWithinConversionMemoryBudget(info: SourceInfo, targetSize: ImagePixelSize): Boolean =
        selectSampleSize(info, targetSize) != null

    private fun decodeForConversion(
        resolver: ContentResolver,
        uri: Uri,
        targetSize: ImagePixelSize,
        info: SourceInfo,
    ): Bitmap {
        val validation = ImageConversionSizing.resolve(
            info.displaySize,
            ImageResizeRequest(
                mode = ImageResizeMode.CUSTOM,
                width = targetSize.width,
                height = targetSize.height,
            ),
        )
        if (!validation.isValid) throw IOException("Invalid conversion dimensions: ${validation.error}")
        val sampleSize = selectSampleSize(info, targetSize)
            ?: throw IOException("Not enough memory to convert the image at the requested resolution")
        val decoded = decodeBitmap(resolver, uri, sampleSize, "convert")
        val oriented = try {
            applyExifOrientation(info.orientation, decoded)
        } catch (error: OutOfMemoryError) {
            decoded.recycle()
            throw IOException("Not enough memory to orient the converted image", error)
        } catch (error: RuntimeException) {
            decoded.recycle()
            throw IOException("Unable to orient the converted image", error)
        }
        if (oriented.width == targetSize.width && oriented.height == targetSize.height) return oriented
        return try {
            Bitmap.createScaledBitmap(oriented, targetSize.width, targetSize.height, true).also {
                if (it !== oriented) oriented.recycle()
            }
        } catch (error: OutOfMemoryError) {
            oriented.recycle()
            throw IOException("Not enough memory to resize the converted image", error)
        } catch (error: RuntimeException) {
            oriented.recycle()
            throw IOException("Unable to resize the converted image", error)
        }
    }

    fun writeOutput(
        resolver: ContentResolver,
        outputUri: Uri,
        bitmap: Bitmap,
        options: ImageConversionOptions,
        maxOutputBytes: Long,
    ) {
        require(bitmap.width == options.targetSize.width && bitmap.height == options.targetSize.height) {
            "Bitmap size does not match conversion options"
        }
        require(maxOutputBytes in 1L..ImageToolsPlugin.MAX_OUTPUT_BYTES) {
            "Invalid output byte limit"
        }
        var encodedBitmap = bitmap
        try {
            if (options.format == ImageOutputFormat.JPEG && bitmap.hasAlpha()) {
                encodedBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
                Canvas(encodedBitmap).apply {
                    drawColor(options.jpegBackgroundColor)
                    drawBitmap(bitmap, 0f, 0f, null)
                }
            }
            val quality = when {
                options.format == ImageOutputFormat.WEBP &&
                    Build.VERSION.SDK_INT < Build.VERSION_CODES.R &&
                    options.quality == ImageConversionOptions.MAX_QUALITY ->
                    ImageConversionOptions.MAX_QUALITY - 1
                else -> options.quality
            }
            val descriptor = resolver.openFileDescriptor(outputUri, ImageToolsPlugin.OUTPUT_OPEN_MODE)
                ?: throw IOException("Unable to open host output transaction")
            val base = ParcelFileDescriptor.AutoCloseOutputStream(descriptor)
            try {
                val bounded = BoundedOutputStream(base, maxOutputBytes)
                if (!encodedBitmap.compress(options.format.compressFormat(), quality, bounded)) {
                    throw IOException("Unable to encode image")
                }
                bounded.flush()
            } finally {
                base.close()
            }
        } finally {
            if (encodedBitmap !== bitmap) encodedBitmap.recycle()
        }
    }

    fun outputFormatForDetectedMime(mimeType: String): ImageOutputFormat = when (mimeType) {
        "image/jpeg" -> ImageOutputFormat.JPEG
        "image/webp" -> ImageOutputFormat.WEBP
        else -> ImageOutputFormat.PNG
    }

    fun transform(
        source: Bitmap,
        rotation: Float = 0f,
        flipX: Boolean = false,
        flipY: Boolean = false,
    ): Bitmap {
        val matrix = Matrix().apply {
            if (rotation != 0f) postRotate(rotation)
            if (flipX || flipY) postScale(if (flipX) -1f else 1f, if (flipY) -1f else 1f)
        }
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }

    private fun decodeBitmap(
        resolver: ContentResolver,
        uri: Uri,
        sampleSize: Int,
        operation: String,
    ): Bitmap = try {
        val options = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        decodeStream(resolver, uri, options) ?: throw IOException("Unable to decode image")
    } catch (error: OutOfMemoryError) {
        throw IOException("Not enough memory to $operation the image", error)
    }

    private fun decodeStream(
        resolver: ContentResolver,
        uri: Uri,
        options: BitmapFactory.Options,
    ): Bitmap? = resolver.openInputStream(uri)?.use { input ->
        BitmapFactory.decodeStream(input, null, options)
    } ?: throw IOException("Unable to open input image")

    private fun selectSampleSize(info: SourceInfo, targetSize: ImagePixelSize): Int? {
        val rawTargetSize = if (info.orientation.swapsDimensions()) {
            ImagePixelSize(targetSize.height, targetSize.width)
        } else {
            targetSize
        }
        val exactRatio = minOf(
            info.rawSize.width.toDouble() / rawTargetSize.width,
            info.rawSize.height.toDouble() / rawTargetSize.height,
        )
        val qualitySample = Integer.highestOneBit(exactRatio.toInt().coerceAtLeast(1))
        return qualitySample.takeIf { fitsMemoryBudget(info, targetSize, it) }
    }

    private fun fitsMemoryBudget(info: SourceInfo, targetSize: ImagePixelSize, sampleSize: Int): Boolean {
        val decodedWidth = sampledDimension(info.rawSize.width, sampleSize)
        val decodedHeight = sampledDimension(info.rawSize.height, sampleSize)
        val decodedPixels = decodedWidth * decodedHeight
        val orientationPeak = if (info.orientation.requiresTransform()) decodedPixels * 2L else decodedPixels
        val scalePeak = decodedPixels + targetSize.pixelCount
        val encodingPeak = targetSize.pixelCount * 2L
        val peakPixels = maxOf(orientationPeak, scalePeak, encodingPeak)
        val bitmapBytes = peakPixels.saturatedMultiply(BYTES_PER_PIXEL)
        val estimatedBytes = if (bitmapBytes > Long.MAX_VALUE - MEMORY_OVERHEAD_BYTES) {
            Long.MAX_VALUE
        } else {
            bitmapBytes + MEMORY_OVERHEAD_BYTES
        }
        val heapBudget = (Runtime.getRuntime().maxMemory() * MAX_HEAP_FRACTION).toLong()
        return estimatedBytes in 1..heapBudget
    }

    private fun editingPixelBudget(): Long {
        val heapBudget = (Runtime.getRuntime().maxMemory() * MAX_EDIT_HEAP_FRACTION).toLong()
        val bitmapBudget = (heapBudget - EDIT_MEMORY_OVERHEAD_BYTES).coerceAtLeast(
            MIN_EDIT_PIXEL_COUNT * BYTES_PER_PIXEL * EDIT_PEAK_BITMAP_COUNT,
        )
        return (bitmapBudget / BYTES_PER_PIXEL / EDIT_PEAK_BITMAP_COUNT)
            .coerceIn(MIN_EDIT_PIXEL_COUNT, MAX_EDIT_PIXELS)
    }

    private fun readExifOrientation(resolver: ContentResolver, uri: Uri): Int = runCatching {
        resolver.openFileDescriptor(uri, ImageToolsPlugin.INPUT_OPEN_MODE)?.use { descriptor ->
            ExifInterface(descriptor.fileDescriptor).getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL,
            )
        }
    }.getOrNull() ?: ExifInterface.ORIENTATION_NORMAL

    private fun applyExifOrientation(orientation: Int, source: Bitmap): Bitmap {
        val transformed = when (orientation) {
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> transform(source, flipX = true)
            ExifInterface.ORIENTATION_ROTATE_180 -> transform(source, rotation = 180f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> transform(source, flipY = true)
            ExifInterface.ORIENTATION_TRANSPOSE -> transform(source, rotation = 90f, flipX = true)
            ExifInterface.ORIENTATION_ROTATE_90 -> transform(source, rotation = 90f)
            ExifInterface.ORIENTATION_TRANSVERSE -> transform(source, rotation = 270f, flipX = true)
            ExifInterface.ORIENTATION_ROTATE_270 -> transform(source, rotation = 270f)
            else -> source
        }
        if (transformed !== source) source.recycle()
        return transformed
    }

    private fun Int.swapsDimensions(): Boolean = this == ExifInterface.ORIENTATION_TRANSPOSE ||
        this == ExifInterface.ORIENTATION_ROTATE_90 ||
        this == ExifInterface.ORIENTATION_TRANSVERSE ||
        this == ExifInterface.ORIENTATION_ROTATE_270

    private fun Int.requiresTransform(): Boolean = this != ExifInterface.ORIENTATION_UNDEFINED &&
        this != ExifInterface.ORIENTATION_NORMAL

    private fun sampledDimension(dimension: Int, sampleSize: Int): Long =
        (dimension.toLong() + sampleSize - 1L) / sampleSize

    private fun Long.saturatedMultiply(factor: Long): Long =
        if (this > Long.MAX_VALUE / factor) Long.MAX_VALUE else this * factor

    @Suppress("DEPRECATION")
    private fun ImageOutputFormat.compressFormat(): Bitmap.CompressFormat = when (this) {
        ImageOutputFormat.JPEG -> Bitmap.CompressFormat.JPEG
        ImageOutputFormat.PNG -> Bitmap.CompressFormat.PNG
        ImageOutputFormat.WEBP -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Bitmap.CompressFormat.WEBP_LOSSY
        } else {
            Bitmap.CompressFormat.WEBP
        }
    }

    private class BoundedOutputStream(
        output: OutputStream,
        private val maxBytes: Long,
    ) : FilterOutputStream(output) {
        private var count = 0L

        override fun write(value: Int) {
            requireCapacity(1)
            out.write(value)
            count += 1L
        }

        override fun write(buffer: ByteArray, offset: Int, length: Int) {
            requireCapacity(length)
            out.write(buffer, offset, length)
            count += length.toLong()
        }

        private fun requireCapacity(additional: Int) {
            if (additional < 0 || count > maxBytes - additional.toLong()) {
                throw OutputLimitExceededException(maxBytes)
            }
        }
    }

    private const val MAX_EDIT_PIXELS = 16_000_000L
    private const val MIN_EDIT_PIXEL_COUNT = 256L * 256L
    private const val EDIT_PEAK_BITMAP_COUNT = 2L
    private const val BYTES_PER_PIXEL = 4L
    private const val EDIT_MEMORY_OVERHEAD_BYTES = 8L * 1024L * 1024L
    private const val MEMORY_OVERHEAD_BYTES = 16L * 1024L * 1024L
    private const val MAX_EDIT_HEAP_FRACTION = 0.55
    private const val MAX_HEAP_FRACTION = 0.65
}
