package io.github.supermonster003.autojs6.plugin.three.maple.image.scriptapi

import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.ParcelFileDescriptor
import io.github.supermonster003.autojs6.plugin.three.maple.image.ImageBitmapIO
import io.github.supermonster003.autojs6.plugin.three.maple.image.ImageConversionOptions
import io.github.supermonster003.autojs6.plugin.three.maple.image.ImageConversionSizing
import io.github.supermonster003.autojs6.plugin.three.maple.image.ImageOutputEncodingPolicy
import io.github.supermonster003.autojs6.plugin.three.maple.image.ImageOutputFormat
import io.github.supermonster003.autojs6.plugin.three.maple.image.ImagePixelSize
import io.github.supermonster003.autojs6.plugin.three.maple.image.ImageResizeError
import io.github.supermonster003.autojs6.plugin.three.maple.image.ImageResizeMode
import io.github.supermonster003.autojs6.plugin.three.maple.image.ImageResizeRequest
import io.github.supermonster003.autojs6.plugin.three.maple.image.ImageToolsPlugin
import io.github.supermonster003.autojs6.plugin.three.maple.image.ImageToolsRequestPolicy
import org.autojs.plugin.gallery.api.GalleryContract
import org.autojs.plugin.gallery.api.GalleryErrorCodes
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.util.UUID
import kotlin.math.roundToInt

/**
 * The headless `convert` of the gallery contract (ROADMAP 10.2): the converter's own pipeline
 * ([ImageBitmapIO], [ImageConversionSizing], EXIF selection, target file size search, PNG
 * optimization) between a read-only source descriptor and the host's writable target. The source
 * is first copied into a private cache file because decoding reads it several times; the copy is
 * deleted before the call returns.
 */
internal class ScriptConverter(private val context: Context) {

    /** The written image: what the host reads back before it publishes the output. */
    data class Written(val mimeType: String, val width: Int, val height: Int, val size: Long)

    fun convert(source: ParcelFileDescriptor, target: ParcelFileDescriptor, request: ConvertRequest): Written {
        val directory = File(context.cacheDir, "gallery-convert").apply { mkdirs() }
        val input = File(directory, UUID.randomUUID().toString() + ".bin")
        try {
            copy(source, input)
            val uri = Uri.fromFile(input)
            val resolver = context.contentResolver
            val info = try {
                ImageBitmapIO.inspect(resolver, uri)
            } catch (e: IOException) {
                throw ContractViolation(GalleryErrorCodes.DECODE_FAILED, "the source does not decode as an image: ${e.message.orEmpty()}")
            }
            val format = outputFormat(request.encode.format, info.mimeType)
            val targetSize = targetSize(info.displaySize, request)
            if (!ImageBitmapIO.isWithinConversionMemoryBudget(info, targetSize)) {
                throw ContractViolation(GalleryErrorCodes.LIMIT_EXCEEDED, "the conversion needs more memory than this device allows")
            }
            val lossless = ImageOutputEncodingPolicy.usesWebpLossless(format, request.lossless, Build.VERSION.SDK_INT)
            val targetBytes = request.targetBytes?.also {
                if (!ImageOutputEncodingPolicy.supportsTargetFileSize(format, lossless, Build.VERSION.SDK_INT)) {
                    throw ContractViolation(GalleryErrorCodes.INVALID_ARGUMENT, "targetBytes applies to JPEG and lossy WebP output, not ${format.displayName}")
                }
            }
            val quality = (request.encode.quality ?: ImageConversionOptions.DEFAULT_QUALITY)
                .coerceAtMost(ImageOutputEncodingPolicy.maximumLossyQuality(format, Build.VERSION.SDK_INT))
            val exif = info.preservableExifMetadata
                .select(capture = request.encode.keepExif, location = request.encode.keepExif && request.encode.keepLocation)
                .takeIf { !it.isEmpty }
            val options = ImageConversionOptions(
                format = format,
                quality = quality,
                targetSize = targetSize,
                jpegBackgroundColor = request.background ?: ImageConversionOptions.DEFAULT_JPEG_BACKGROUND_COLOR,
                webpLossless = lossless,
                pngPaletteColorCountHint = info.pngPaletteColorCount.takeIf { format == ImageOutputFormat.PNG },
                targetFileSizeBytes = targetBytes,
                preservedExifMetadata = exif,
            )
            val bitmap = try {
                ImageBitmapIO.decodeForConversion(resolver, uri, targetSize, info)
            } catch (e: OutOfMemoryError) {
                throw ContractViolation(GalleryErrorCodes.LIMIT_EXCEEDED, "the image does not fit in memory")
            } catch (e: IOException) {
                throw ContractViolation(GalleryErrorCodes.DECODE_FAILED, "the source does not decode: ${e.message.orEmpty()}")
            }
            try {
                val result = ImageBitmapIO.writeOutput(bitmap, options, ImageToolsPlugin.MAX_OUTPUT_BYTES, context.cacheDir) { target.dup() }
                return Written(format.mimeType, targetSize.width, targetSize.height, result.encodedBytes)
            } catch (e: IOException) {
                throw ContractViolation(GalleryErrorCodes.IO, "the output could not be written: ${e.message.orEmpty()}")
            } finally {
                bitmap.recycle()
            }
        } finally {
            input.delete()
        }
    }

    /** The answer Bundle of a successful conversion. */
    fun answer(version: Int, written: Written): Bundle = ScriptAnswers.ok(version) {
        putString(GalleryContract.KEY_MIME_TYPE, written.mimeType)
        putInt(GalleryContract.KEY_WIDTH, written.width)
        putInt(GalleryContract.KEY_HEIGHT, written.height)
        putLong(GalleryContract.KEY_SIZE, written.size)
    }

    private fun copy(source: ParcelFileDescriptor, file: File) {
        // Positional reads keep the host's descriptor offset untouched.
        FileInputStream(source.fileDescriptor).channel.let { channel ->
            file.outputStream().use { output ->
                val buffer = ByteBuffer.allocate(64 * 1024)
                var position = 0L
                while (true) {
                    buffer.clear()
                    val count = channel.read(buffer, position)
                    if (count < 0) break
                    position += count
                    if (position > ImageToolsRequestPolicy.MAX_INPUT_BYTES) {
                        throw ContractViolation(GalleryErrorCodes.LIMIT_EXCEEDED, "the source exceeds ${ImageToolsRequestPolicy.MAX_INPUT_BYTES} bytes")
                    }
                    output.write(buffer.array(), 0, count)
                }
            }
        }
        if (file.length() == 0L) throw ContractViolation(GalleryErrorCodes.FILE_UNREADABLE, "the source is empty")
    }

    companion object {

        /** The written format: the requested one, else the source's when the converter writes it, else JPEG. */
        fun outputFormat(requested: String?, sourceMimeType: String): ImageOutputFormat = when (requested) {
            GalleryContract.FORMAT_JPEG -> ImageOutputFormat.JPEG
            GalleryContract.FORMAT_PNG -> ImageOutputFormat.PNG
            GalleryContract.FORMAT_WEBP -> ImageOutputFormat.WEBP
            null -> ImageOutputFormat.entries.firstOrNull { it.mimeType == sourceMimeType } ?: ImageOutputFormat.JPEG
            else -> throw ContractViolation(GalleryErrorCodes.UNSUPPORTED_FORMAT, "cannot write $requested")
        }

        /**
         * The output size: the converter's own modes, where the custom size keeps the aspect ratio by
         * default (one side given: the other follows; both given: the image fits inside them).
         */
        fun targetSize(source: ImagePixelSize, request: ConvertRequest): ImagePixelSize {
            val resize = when (request.sizeMode) {
                GalleryContract.SIZE_PERCENT -> ImageResizeRequest(ImageResizeMode.PERCENTAGE, percentage = request.percent)
                GalleryContract.SIZE_LONG_EDGE -> ImageResizeRequest(ImageResizeMode.LONG_EDGE, longEdge = request.longEdge)
                GalleryContract.SIZE_CUSTOM -> customSize(source, request).let { ImageResizeRequest(ImageResizeMode.CUSTOM, width = it.width, height = it.height) }
                else -> ImageResizeRequest(ImageResizeMode.ORIGINAL)
            }
            val result = ImageConversionSizing.resolve(source, resize)
            return result.size ?: throw ContractViolation(
                when (result.error) {
                    ImageResizeError.DIMENSION_TOO_LARGE, ImageResizeError.PIXEL_COUNT_TOO_LARGE, ImageResizeError.MEMORY_BUDGET_EXCEEDED -> GalleryErrorCodes.LIMIT_EXCEEDED
                    ImageResizeError.INVALID_SOURCE -> GalleryErrorCodes.DECODE_FAILED
                    else -> GalleryErrorCodes.INVALID_ARGUMENT
                },
                "the requested size is not possible (${result.error})",
            )
        }

        private fun customSize(source: ImagePixelSize, request: ConvertRequest): ImagePixelSize {
            val width = request.width
            val height = request.height
            if (!request.keepAspectRatio && width != null && height != null) return ImagePixelSize(width, height)
            val scale = when {
                width != null && height != null -> minOf(width.toDouble() / source.width, height.toDouble() / source.height)
                width != null -> width.toDouble() / source.width
                height != null -> height.toDouble() / source.height
                else -> 1.0
            }
            return ImagePixelSize(
                (source.width * scale).roundToInt().coerceAtLeast(1),
                (source.height * scale).roundToInt().coerceAtLeast(1),
            )
        }
    }
}
