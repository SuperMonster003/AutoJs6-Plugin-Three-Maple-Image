package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.os.ParcelFileDescriptor
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.FilterOutputStream
import java.io.IOException
import java.io.OutputStream

internal object ImageBitmapIO {

    data class SourceInfo(
        val rawSize: ImagePixelSize,
        val displaySize: ImagePixelSize,
        val mimeType: String,
        val orientation: Int,
        val pngPaletteColorCount: Int? = null,
        val preservableExifMetadata: PreservedExifMetadata = PreservedExifMetadata.EMPTY,
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
        val exifSnapshot = ExifMetadataPolicy.read(resolver, uri)
        val orientation = exifSnapshot.orientation
        val rawSize = ImagePixelSize(bounds.outWidth, bounds.outHeight)
        val displaySize = if (orientation.swapsDimensions()) {
            ImagePixelSize(bounds.outHeight, bounds.outWidth)
        } else {
            rawSize
        }
        val pngPaletteColorCount = if (detectedMimeType == ImageOutputFormat.PNG.mimeType) {
            runCatching {
                resolver.openInputStream(uri)?.use(PngSourceMetadata::paletteColorCount)
            }.getOrNull()
        } else {
            null
        }
        return SourceInfo(
            rawSize,
            displaySize,
            detectedMimeType,
            orientation,
            pngPaletteColorCount,
            exifSnapshot.preservableMetadata,
        )
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
        temporaryDirectory: File? = null,
    ): ImageOutputWriteResult {
        validateOutput(bitmap, options, maxOutputBytes)
        val targetFileSizeResult = options.targetFileSizeBytes?.let {
            selectTargetFileSize(bitmap, options, maxOutputBytes, temporaryDirectory)
        }
        val encodingOptions = targetFileSizeResult?.let { result ->
            options.copy(quality = result.quality, targetFileSizeBytes = null)
        } ?: options
        val pngPalettePlan = preparePngPalettePlan(bitmap, encodingOptions)
        val encodedBitmap = prepareBitmapForEncoding(bitmap, encodingOptions)
        var stagedFile: File? = null
        try {
            if (encodingOptions.preservedExifMetadata?.isEmpty == false) {
                stagedFile = stageEncodedOutput(
                    bitmap = encodedBitmap,
                    options = encodingOptions,
                    maxOutputBytes = maxOutputBytes,
                    pngPalettePlan = pngPalettePlan,
                    temporaryDirectory = requireTemporaryDirectory(temporaryDirectory),
                )
            }
            val descriptor = resolver.openFileDescriptor(outputUri, ImageToolsPlugin.OUTPUT_OPEN_MODE)
                ?: throw IOException("Unable to open host output transaction")
            val encodedBytes = ParcelFileDescriptor.AutoCloseOutputStream(descriptor).use { output ->
                stagedFile?.let { file -> copyStagedOutput(file, output, maxOutputBytes) }
                    ?: encodePreparedOutput(
                        encodedBitmap,
                        encodingOptions,
                        output,
                        maxOutputBytes,
                        pngPalettePlan,
                    )
            }
            return ImageOutputWriteResult(
                encodedBytes = encodedBytes,
                quality = encodingOptions.quality,
                targetFileSizeResult = targetFileSizeResult?.copy(encodedBytes = encodedBytes),
            )
        } finally {
            stagedFile?.delete()
            if (encodedBitmap !== bitmap) encodedBitmap.recycle()
        }
    }

    internal fun encodeOutput(
        bitmap: Bitmap,
        options: ImageConversionOptions,
        output: OutputStream,
        maxOutputBytes: Long,
        temporaryDirectory: File? = null,
    ): ImageOutputWriteResult {
        validateOutput(bitmap, options, maxOutputBytes)
        val targetFileSizeResult = options.targetFileSizeBytes?.let {
            selectTargetFileSize(bitmap, options, maxOutputBytes, temporaryDirectory)
        }
        val encodingOptions = targetFileSizeResult?.let { result ->
            options.copy(quality = result.quality, targetFileSizeBytes = null)
        } ?: options
        val pngPalettePlan = preparePngPalettePlan(bitmap, encodingOptions)
        val encodedBitmap = prepareBitmapForEncoding(bitmap, encodingOptions)
        var stagedFile: File? = null
        try {
            val encodedBytes = if (encodingOptions.preservedExifMetadata?.isEmpty == false) {
                stagedFile = stageEncodedOutput(
                    bitmap = encodedBitmap,
                    options = encodingOptions,
                    maxOutputBytes = maxOutputBytes,
                    pngPalettePlan = pngPalettePlan,
                    temporaryDirectory = requireTemporaryDirectory(temporaryDirectory),
                )
                copyStagedOutput(requireNotNull(stagedFile), output, maxOutputBytes)
            } else {
                encodePreparedOutput(
                    encodedBitmap,
                    encodingOptions,
                    output,
                    maxOutputBytes,
                    pngPalettePlan,
                )
            }
            return ImageOutputWriteResult(
                encodedBytes = encodedBytes,
                quality = encodingOptions.quality,
                targetFileSizeResult = targetFileSizeResult?.copy(encodedBytes = encodedBytes),
            )
        } finally {
            stagedFile?.delete()
            if (encodedBitmap !== bitmap) encodedBitmap.recycle()
        }
    }

    internal fun selectTargetFileSize(
        bitmap: Bitmap,
        options: ImageConversionOptions,
        maxOutputBytes: Long,
        temporaryDirectory: File? = null,
    ): ImageTargetFileSizeResult {
        validateOutput(bitmap, options, maxOutputBytes)
        val targetBytes = requireNotNull(options.targetFileSizeBytes) {
            "Target file size mode is not enabled"
        }
        require(targetBytes <= maxOutputBytes) { "Target file size exceeds the host output limit" }
        require(
            ImageOutputEncodingPolicy.supportsTargetFileSize(
                format = options.format,
                webpLosslessRequested = options.webpLossless,
                sdkInt = Build.VERSION.SDK_INT,
            ),
        ) { "Target file size mode requires JPEG or lossy WebP output" }

        val encodedBitmap = prepareBitmapForEncoding(bitmap, options)
        try {
            val minimumQuality = ImageConversionOptions.MIN_QUALITY
            val maximumQuality = ImageOutputEncodingPolicy.maximumLossyQuality(
                format = options.format,
                sdkInt = Build.VERSION.SDK_INT,
            )
            return ImageTargetFileSizeSearch.search(
                targetBytes = targetBytes,
                minQuality = minimumQuality,
                maxQuality = maximumQuality,
            ) { quality ->
                try {
                    measurePreparedOutput(
                        bitmap = encodedBitmap,
                        options = options.copy(quality = quality, targetFileSizeBytes = null),
                        maxOutputBytes = maxOutputBytes,
                        pngPalettePlan = null,
                        temporaryDirectory = temporaryDirectory,
                    )
                } catch (error: OutputLimitExceededException) {
                    if (quality == minimumQuality) throw error
                    maxOutputBytes + 1L
                }
            }
        } finally {
            if (encodedBitmap !== bitmap) encodedBitmap.recycle()
        }
    }

    private fun measurePreparedOutput(
        bitmap: Bitmap,
        options: ImageConversionOptions,
        maxOutputBytes: Long,
        pngPalettePlan: PngOutputOptimizer.PalettePlan?,
        temporaryDirectory: File?,
    ): Long {
        if (options.preservedExifMetadata?.isEmpty != false) {
            return encodePreparedOutput(
                bitmap = bitmap,
                options = options,
                output = DISCARDING_OUTPUT_STREAM,
                maxOutputBytes = maxOutputBytes,
                pngPalettePlan = pngPalettePlan,
            )
        }
        val stagedFile = stageEncodedOutput(
            bitmap = bitmap,
            options = options,
            maxOutputBytes = maxOutputBytes,
            pngPalettePlan = pngPalettePlan,
            temporaryDirectory = requireTemporaryDirectory(temporaryDirectory),
        )
        return try {
            stagedFile.length()
        } finally {
            stagedFile.delete()
        }
    }

    private fun stageEncodedOutput(
        bitmap: Bitmap,
        options: ImageConversionOptions,
        maxOutputBytes: Long,
        pngPalettePlan: PngOutputOptimizer.PalettePlan?,
        temporaryDirectory: File,
    ): File {
        val stagedFile = File.createTempFile(
            "image-tools-output-",
            ".${options.format.extension}",
            temporaryDirectory,
        )
        try {
            FileOutputStream(stagedFile).use { output ->
                encodePreparedOutput(bitmap, options, output, maxOutputBytes, pngPalettePlan)
            }
            ExifMetadataPolicy.applyTo(stagedFile, requireNotNull(options.preservedExifMetadata))
            val finalBytes = stagedFile.length()
            if (finalBytes <= 0L) throw IOException("Unable to stage encoded image")
            if (finalBytes > maxOutputBytes) throw OutputLimitExceededException(maxOutputBytes)
            return stagedFile
        } catch (error: Throwable) {
            stagedFile.delete()
            throw error
        }
    }

    private fun copyStagedOutput(
        stagedFile: File,
        output: OutputStream,
        maxOutputBytes: Long,
    ): Long {
        val bounded = BoundedOutputStream(output, maxOutputBytes)
        FileInputStream(stagedFile).use { input -> input.copyTo(bounded) }
        if (bounded.limitExceeded) throw OutputLimitExceededException(maxOutputBytes)
        bounded.flush()
        return bounded.bytesWritten
    }

    private fun requireTemporaryDirectory(directory: File?): File = requireNotNull(directory) {
        "EXIF preservation requires an app-private temporary directory"
    }.also {
        require(it.isDirectory) { "EXIF temporary directory is unavailable" }
    }

    private fun encodePreparedOutput(
        bitmap: Bitmap,
        options: ImageConversionOptions,
        output: OutputStream,
        maxOutputBytes: Long,
        pngPalettePlan: PngOutputOptimizer.PalettePlan?,
    ): Long {
        val useLosslessWebp = ImageOutputEncodingPolicy.usesWebpLossless(
            format = options.format,
            requested = options.webpLossless,
            sdkInt = Build.VERSION.SDK_INT,
        )
        val quality = when {
            useLosslessWebp -> ImageConversionOptions.MAX_QUALITY
            options.format == ImageOutputFormat.WEBP &&
                Build.VERSION.SDK_INT < Build.VERSION_CODES.R &&
                options.quality == ImageConversionOptions.MAX_QUALITY ->
                ImageConversionOptions.MAX_QUALITY - 1
            else -> options.quality
        }
        val bounded = BoundedOutputStream(output, maxOutputBytes)
        val compressed = if (options.format == ImageOutputFormat.PNG && pngPalettePlan != null) {
            PngOutputOptimizer.encode(bitmap, pngPalettePlan, bounded)
            true
        } else {
            bitmap.compress(
                options.format.compressFormat(useLosslessWebp),
                quality,
                bounded,
            )
        }
        if (bounded.limitExceeded) throw OutputLimitExceededException(maxOutputBytes)
        if (!compressed) {
            throw IOException("Unable to encode image")
        }
        bounded.flush()
        return bounded.bytesWritten
    }

    private fun prepareBitmapForEncoding(
        bitmap: Bitmap,
        options: ImageConversionOptions,
    ): Bitmap {
        if (options.format != ImageOutputFormat.JPEG || !bitmap.hasAlpha()) return bitmap
        return Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888).also { encoded ->
            Canvas(encoded).apply {
                drawColor(options.jpegBackgroundColor)
                drawBitmap(bitmap, 0f, 0f, null)
            }
        }
    }

    private fun preparePngPalettePlan(
        bitmap: Bitmap,
        options: ImageConversionOptions,
    ): PngOutputOptimizer.PalettePlan? = if (options.format == ImageOutputFormat.PNG) {
        PngOutputOptimizer.plan(bitmap)
    } else {
        null
    }

    private fun validateOutput(
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
    ): Bitmap? {
        val input = resolver.openInputStream(uri)
            ?: throw IOException("Unable to open input image")
        return input.use { BitmapFactory.decodeStream(it, null, options) }
    }

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

    internal fun editingPixelBudget(): Long {
        val heapBudget = (Runtime.getRuntime().maxMemory() * MAX_EDIT_HEAP_FRACTION).toLong()
        val bitmapBudget = (heapBudget - EDIT_MEMORY_OVERHEAD_BYTES).coerceAtLeast(
            MIN_EDIT_PIXEL_COUNT * BYTES_PER_PIXEL * EDIT_PEAK_BITMAP_NUMERATOR /
                EDIT_PEAK_BITMAP_DENOMINATOR,
        )
        return (bitmapBudget / BYTES_PER_PIXEL * EDIT_PEAK_BITMAP_DENOMINATOR /
            EDIT_PEAK_BITMAP_NUMERATOR)
            .coerceIn(MIN_EDIT_PIXEL_COUNT, MAX_EDIT_PIXELS)
    }

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
    private fun ImageOutputFormat.compressFormat(useLosslessWebp: Boolean): Bitmap.CompressFormat = when (this) {
        ImageOutputFormat.JPEG -> Bitmap.CompressFormat.JPEG
        ImageOutputFormat.PNG -> Bitmap.CompressFormat.PNG
        ImageOutputFormat.WEBP -> when {
            useLosslessWebp && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R ->
                Bitmap.CompressFormat.WEBP_LOSSLESS
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> Bitmap.CompressFormat.WEBP_LOSSY
            else -> Bitmap.CompressFormat.WEBP
        }
    }

    private class BoundedOutputStream(
        output: OutputStream,
        private val maxBytes: Long,
    ) : FilterOutputStream(output) {
        private var count = 0L
        val bytesWritten: Long
            get() = count
        var limitExceeded = false
            private set

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
                limitExceeded = true
                throw OutputLimitExceededException(maxBytes)
            }
        }
    }

    private val DISCARDING_OUTPUT_STREAM = object : OutputStream() {
        override fun write(value: Int) = Unit

        override fun write(buffer: ByteArray, offset: Int, length: Int) {
            require(offset >= 0 && length >= 0 && offset <= buffer.size - length)
        }
    }

    private const val MAX_EDIT_PIXELS = 16_000_000L
    private const val MIN_EDIT_PIXEL_COUNT = 256L * 256L
    // Source + target + one mosaic buffer no larger than 1/16 of the source.
    private const val EDIT_PEAK_BITMAP_NUMERATOR = 33L
    private const val EDIT_PEAK_BITMAP_DENOMINATOR = 16L
    private const val BYTES_PER_PIXEL = 4L
    private const val EDIT_MEMORY_OVERHEAD_BYTES = 8L * 1024L * 1024L
    private const val MEMORY_OVERHEAD_BYTES = 16L * 1024L * 1024L
    private const val MAX_EDIT_HEAP_FRACTION = 0.55
    private const val MAX_HEAP_FRACTION = 0.65
}
