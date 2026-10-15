package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.ParcelFileDescriptor
import java.io.File
import java.io.FileInputStream
import java.io.IOException

/**
 * What every image of a batch conversion gets (ROADMAP 7.6, T12): one output format and quality, a resize rule that
 * works for images of any size (no fixed width and height), a quarter-turn rotation, EXIF and the JPEG background.
 * The numbers stay text while the form is edited; [BatchConversionPolicy.settingsError] validates them.
 */
internal data class BatchConversionSettings(
    val format: ImageOutputFormat = ImageOutputFormat.JPEG,
    val quality: Int = ImageConversionOptions.DEFAULT_QUALITY,
    val webpLossless: Boolean = false,
    val resizeMode: ImageResizeMode = ImageResizeMode.ORIGINAL,
    val percentage: String = DEFAULT_PERCENTAGE,
    val longEdge: String = ImageConversionSizing.DEFAULT_LONG_EDGE.toString(),
    /** Clockwise, one of [BatchConversionPolicy.ROTATIONS]. */
    val rotationDegrees: Int = 0,
    val preserveExif: Boolean = false,
    /** Keep where each original was taken (ROADMAP 9.1, T14); offered only with "Show location" on. */
    val preserveLocation: Boolean = false,
    val jpegBackgroundBlack: Boolean = false,
) {
    companion object {
        const val DEFAULT_PERCENTAGE = "50"
    }
}

internal enum class BatchSettingsError { INVALID_PERCENTAGE, INVALID_LONG_EDGE }

/** How one image of a batch is converted, or why it cannot be. */
internal sealed interface BatchItemPlan {
    /** Decode at [decodeSize] (upright), turn by [rotationDegrees], then encode with [options] (`targetSize` is the turned size). */
    data class Ready(val decodeSize: ImagePixelSize, val rotationDegrees: Int, val options: ImageConversionOptions) : BatchItemPlan

    data class Refused(val error: ImageResizeError) : BatchItemPlan
}

/** Why one image of a batch was not converted; the batch goes on with the next one (gallery) or stops (host entry). */
internal enum class BatchFailure {
    /** The file could not be read or is not an image Android can decode. */
    UNREADABLE,
    /** The result would exceed the size limits (16384 px a side, 40 MP, 256 MiB). */
    TOO_LARGE,
    /** Not enough memory to convert this image at this size. */
    OUT_OF_MEMORY,
    /** The new file could not be created or written. */
    WRITE_FAILED,
}

internal class BatchItemException(val failure: BatchFailure, cause: Throwable? = null) :
    IOException("Batch conversion failed: $failure", cause)

/** Android-free rules of a batch conversion: settings validation, the per-image plan and the output name. */
internal object BatchConversionPolicy {

    val ROTATIONS = listOf(0, 90, 180, 270)

    /** Fixed sizes make no sense for images of different shapes; percentage and long edge scale each one. */
    val RESIZE_MODES = listOf(ImageResizeMode.ORIGINAL, ImageResizeMode.PERCENTAGE, ImageResizeMode.LONG_EDGE)

    /** The suffix the host gives single conversions too: `photo.png` becomes `photo_converted.jpg`. */
    const val OUTPUT_SUFFIX = "converted"

    fun settingsError(settings: BatchConversionSettings): BatchSettingsError? = when (settings.resizeMode) {
        ImageResizeMode.PERCENTAGE -> BatchSettingsError.INVALID_PERCENTAGE.takeIf {
            settings.percentage.trim().toIntOrNull() !in ImageConversionSizing.MIN_PERCENTAGE..ImageConversionSizing.MAX_PERCENTAGE
        }
        ImageResizeMode.LONG_EDGE -> BatchSettingsError.INVALID_LONG_EDGE.takeIf {
            settings.longEdge.trim().toIntOrNull() !in 1..ImageConversionSizing.MAX_DIMENSION
        }
        else -> null
    }

    fun losslessVisible(settings: BatchConversionSettings, sdkInt: Int): Boolean =
        settings.format == ImageOutputFormat.WEBP && ImageOutputEncodingPolicy.isWebpLosslessAvailable(sdkInt)

    fun qualityEnabled(settings: BatchConversionSettings, sdkInt: Int): Boolean =
        ImageOutputEncodingPolicy.qualityEnabled(settings.format, settings.webpLossless, sdkInt)

    /**
     * The plan for one image whose upright size and EXIF are in [source]; [withinMemoryBudget] says whether the
     * device can decode it at a given size. Call it only with valid settings ([settingsError] is null).
     */
    fun plan(
        settings: BatchConversionSettings,
        source: ImageBitmapIO.SourceInfo,
        sdkInt: Int,
        withinMemoryBudget: (ImagePixelSize) -> Boolean,
    ): BatchItemPlan {
        require(settings.rotationDegrees in ROTATIONS) { "Rotation must be a quarter turn" }
        val request = when (settings.resizeMode) {
            ImageResizeMode.PERCENTAGE -> ImageResizeRequest(ImageResizeMode.PERCENTAGE, percentage = settings.percentage.trim().toIntOrNull())
            ImageResizeMode.LONG_EDGE -> ImageResizeRequest(ImageResizeMode.LONG_EDGE, longEdge = settings.longEdge.trim().toIntOrNull())
            else -> ImageResizeRequest(ImageResizeMode.ORIGINAL)
        }
        val resolved = ImageConversionSizing.resolve(source.displaySize, request)
        val size = resolved.size ?: return BatchItemPlan.Refused(resolved.error ?: ImageResizeError.INVALID_SOURCE)
        if (!withinMemoryBudget(size)) return BatchItemPlan.Refused(ImageResizeError.MEMORY_BUDGET_EXCEEDED)
        val turned = if (settings.rotationDegrees % 180 != 0) ImagePixelSize(size.height, size.width) else size
        val options = ImageConversionOptions(
            format = settings.format,
            quality = settings.quality.coerceIn(ImageConversionOptions.MIN_QUALITY, ImageConversionOptions.MAX_QUALITY),
            targetSize = turned,
            jpegBackgroundColor = if (settings.jpegBackgroundBlack) JPEG_BACKGROUND_BLACK else ImageConversionOptions.DEFAULT_JPEG_BACKGROUND_COLOR,
            webpLossless = ImageOutputEncodingPolicy.usesWebpLossless(settings.format, settings.webpLossless, sdkInt),
            preservedExifMetadata = source.preservableExifMetadata
                .select(capture = settings.preserveExif, location = settings.preserveLocation)
                .takeIf { !it.isEmpty },
        )
        return BatchItemPlan.Ready(size, settings.rotationDegrees, options)
    }

    fun failureOf(error: ImageResizeError): BatchFailure = when (error) {
        ImageResizeError.MEMORY_BUDGET_EXCEEDED -> BatchFailure.OUT_OF_MEMORY
        ImageResizeError.INVALID_SOURCE -> BatchFailure.UNREADABLE
        else -> BatchFailure.TOO_LARGE
    }

    /** `<base>_converted.<ext>`; with [taken] names (case-insensitive) it becomes `<base>_converted (n).<ext>`. */
    fun outputName(sourceName: String, format: ImageOutputFormat, taken: Collection<String> = emptyList()): String {
        val base = MediaFileNamePolicy.baseName(sourceName).ifBlank { "image" }
        return NameConflictPolicy.uniqueName("${base}_$OUTPUT_SUFFIX.${format.extension}", taken)
    }

    /** `android.graphics.Color.BLACK` without touching the platform class. */
    private const val JPEG_BACKGROUND_BLACK = -0x1000000
}

/** Converts one image of a batch: read bounds and EXIF once, decode at the planned size, turn, encode. */
internal object BatchImageConverter {

    /**
     * Writes [source] converted with [settings] into the descriptor [openOutput] returns (opened only once the image
     * is ready); returns the encoded size. A refusal or failure throws [BatchItemException] and opens nothing when
     * the image could not be prepared.
     */
    fun convert(
        resolver: ContentResolver,
        source: Uri,
        settings: BatchConversionSettings,
        temporaryDirectory: File,
        openOutput: () -> ParcelFileDescriptor,
    ): ConvertedImage {
        val info = try {
            ImageBitmapIO.inspect(resolver, source)
        } catch (error: Exception) {
            throw BatchItemException(BatchFailure.UNREADABLE, error)
        }
        val plan = when (val planned = BatchConversionPolicy.plan(settings, info, Build.VERSION.SDK_INT) { ImageBitmapIO.isWithinConversionMemoryBudget(info, it) }) {
            is BatchItemPlan.Refused -> throw BatchItemException(BatchConversionPolicy.failureOf(planned.error))
            is BatchItemPlan.Ready -> planned
        }
        var bitmap: Bitmap? = null
        try {
            val upright = try {
                ImageBitmapIO.decodeForConversion(resolver, source, plan.decodeSize, info)
            } catch (error: OutOfMemoryError) {
                throw BatchItemException(BatchFailure.OUT_OF_MEMORY, error)
            } catch (error: Exception) {
                throw BatchItemException(BatchFailure.UNREADABLE, error)
            }
            bitmap = upright
            val output = if (plan.rotationDegrees == 0) {
                upright
            } else {
                try {
                    ImageBitmapIO.transform(upright, rotation = plan.rotationDegrees.toFloat())
                } catch (error: OutOfMemoryError) {
                    throw BatchItemException(BatchFailure.OUT_OF_MEMORY, error)
                }.also { turned ->
                    if (turned !== upright) upright.recycle()
                    bitmap = turned
                }
            }
            val result = try {
                ImageBitmapIO.writeOutput(output, plan.options, ImageToolsPlugin.MAX_OUTPUT_BYTES, temporaryDirectory, openOutput)
            } catch (error: ImageBitmapIO.OutputLimitExceededException) {
                throw BatchItemException(BatchFailure.TOO_LARGE, error)
            } catch (error: OutOfMemoryError) {
                throw BatchItemException(BatchFailure.OUT_OF_MEMORY, error)
            } catch (error: Exception) {
                throw BatchItemException(BatchFailure.WRITE_FAILED, error)
            }
            return ConvertedImage(plan.options.format, plan.options.targetSize, result.encodedBytes)
        } finally {
            bitmap?.takeUnless(Bitmap::isRecycled)?.recycle()
        }
    }

    /** Reads back a written image through [descriptor] (closed here) and checks it decodes to [expected]. */
    fun verify(descriptor: ParcelFileDescriptor, expected: ConvertedImage) {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        descriptor.use { FileInputStream(it.fileDescriptor).use { input -> BitmapFactory.decodeStream(input, null, bounds) } }
        if (bounds.outWidth != expected.size.width || bounds.outHeight != expected.size.height || bounds.outMimeType != expected.format.mimeType) {
            throw BatchItemException(BatchFailure.WRITE_FAILED, IOException("The written image does not read back as expected"))
        }
    }
}

internal data class ConvertedImage(val format: ImageOutputFormat, val size: ImagePixelSize, val encodedBytes: Long)
