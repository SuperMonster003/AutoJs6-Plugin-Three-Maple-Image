package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException
import java.util.UUID

/** What the shared copy is (ROADMAP 9.1): the full picture without metadata, or a smaller picture (also without). */
internal enum class ShareCopyMode { WITHOUT_METADATA, SMALLER }

internal data class ShareCopyRequest(
    val mode: ShareCopyMode,
    /** The long edge of a [ShareCopyMode.SMALLER] copy; never upscaled. */
    val longEdge: Int = DEFAULT_LONG_EDGE,
) {
    companion object {
        const val DEFAULT_LONG_EDGE = 1920
        val LONG_EDGE_PRESETS = listOf(1080, 1920)
    }
}

/** A copy that cannot be made; [error] says why when it is about the size. */
internal class ShareCopyException(val error: ImageResizeError?, message: String, cause: Throwable? = null) : IOException(message, cause)

/**
 * The Android-free rules of a shared copy: JPEG, PNG and WebP keep their format, every other format (animated GIF
 * included) becomes a still JPEG; the copy carries no EXIF at all (camera, date and location are gone), keeps the
 * name of the original and is removed after a day.
 */
internal object ShareCopyPolicy {

    const val MAX_AGE_MILLIS = 24L * 60L * 60L * 1000L

    fun outputFormat(sourceMimeType: String): ImageOutputFormat = when (sourceMimeType.lowercase()) {
        ImageOutputFormat.PNG.mimeType -> ImageOutputFormat.PNG
        ImageOutputFormat.WEBP.mimeType -> ImageOutputFormat.WEBP
        else -> ImageOutputFormat.JPEG
    }

    fun targetSize(displaySize: ImagePixelSize, request: ShareCopyRequest): ImageResizeResult = ImageConversionSizing.resolve(
        displaySize,
        when (request.mode) {
            ShareCopyMode.WITHOUT_METADATA -> ImageResizeRequest(ImageResizeMode.ORIGINAL)
            ShareCopyMode.SMALLER -> ImageResizeRequest(ImageResizeMode.LONG_EDGE, longEdge = request.longEdge)
        },
    )

    /** "photo.heic" -> "photo.jpg"; a name without a usable stem becomes "image". */
    fun fileName(displayName: String, format: ImageOutputFormat): String {
        val stem = displayName.substringBeforeLast('.').ifBlank { displayName }.trim().ifBlank { "image" }
        val safe = stem.replace(Regex("[\\\\/:*?\"<>|\\u0000-\\u001f]"), "_").take(MAX_STEM_LENGTH)
        return "$safe.${format.extension}"
    }

    fun expired(lastModifiedMillis: Long, nowMillis: Long): Boolean = nowMillis - lastModifiedMillis > MAX_AGE_MILLIS

    private const val MAX_STEM_LENGTH = 120
}

/** Makes shared copies under `cache/share-copies/` (one folder per copy) and removes them a day later. */
internal object ShareCopies {

    fun create(context: Context, source: Uri, displayName: String, request: ShareCopyRequest): Uri {
        cleanup(context)
        val resolver = context.contentResolver
        val info = ImageBitmapIO.inspect(resolver, source)
        val format = ShareCopyPolicy.outputFormat(info.mimeType)
        val resize = ShareCopyPolicy.targetSize(info.displaySize, request)
        val size = resize.size ?: throw ShareCopyException(resize.error, "Copy size is not allowed: ${resize.error}")
        if (!ImageBitmapIO.isWithinConversionMemoryBudget(info, size)) {
            throw ShareCopyException(ImageResizeError.MEMORY_BUDGET_EXCEEDED, "Not enough memory for the copy")
        }
        val bitmap = ImageBitmapIO.decodeForConversion(resolver, source, size, info)
        val folder = File(root(context), UUID.randomUUID().toString()).apply { mkdirs() }
        val file = File(folder, ShareCopyPolicy.fileName(displayName, format))
        try {
            file.outputStream().use { output ->
                ImageBitmapIO.encodeOutput(
                    bitmap = bitmap,
                    options = ImageConversionOptions(format = format, targetSize = size),
                    output = output,
                    maxOutputBytes = ImageToolsPlugin.MAX_OUTPUT_BYTES,
                    temporaryDirectory = folder,
                )
            }
        } catch (error: Throwable) {
            folder.deleteRecursively()
            throw error
        } finally {
            bitmap.recycle()
        }
        return FileProvider.getUriForFile(context, context.packageName + AUTHORITY_SUFFIX, file)
    }

    /** Removes the copies older than a day; the receiving app has long read them by then. */
    fun cleanup(context: Context, nowMillis: Long = System.currentTimeMillis()) {
        root(context).listFiles().orEmpty()
            .filter { ShareCopyPolicy.expired(it.lastModified(), nowMillis) }
            .forEach { it.deleteRecursively() }
    }

    internal fun root(context: Context): File = File(context.cacheDir, DIRECTORY)

    private const val DIRECTORY = "share-copies"
    private const val AUTHORITY_SUFFIX = ".standalone-images"
}
