package io.github.supermonster003.autojs6.plugin.imageviewer

import android.content.ContentResolver
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import android.system.Os
import android.system.OsConstants
import androidx.annotation.RequiresApi

internal data class ImageMetadata(
    val byteSize: Long,
    val width: Int?,
    val height: Int?,
    val exif: ImageExifMetadata? = null,
    val decodedColor: ImageDecodedColorMetadata? = null,
)

internal object ImageContentValidator {

    fun validateExplorer(
        contentResolver: ContentResolver,
        request: ImageViewerRequest,
    ): ImageViewerRequest? {
        val resolverMime = runCatching { contentResolver.getType(request.targetUri) }.getOrNull()
        val normalizedResolverMime = resolverMime?.let {
            ImageFormatSupport.explorerMimeType(request.displayName, it)
        }
        if (
            resolverMime != null &&
            (
                normalizedResolverMime == null ||
                    !ImageRequestPolicy.mimeTypesAreCompatible(request.mimeType, normalizedResolverMime)
            )
        ) {
            return null
        }
        val metadata = readMetadata(contentResolver, request) ?: return null
        if (request.declaredSize > 0L && metadata.byteSize >= 0L && metadata.byteSize != request.declaredSize) {
            return null
        }
        return request.copy(mimeType = normalizedResolverMime ?: request.mimeType)
    }

    fun resolveExternal(
        contentResolver: ContentResolver,
        seed: ExternalImageSeed,
    ): ImageViewerRequest? {
        val resolverMime = runCatching { contentResolver.getType(seed.targetUri) }.getOrNull()
        if (resolverMime != null && !ImageRequestPolicy.mimeTypesAreCompatible(seed.mimeType, resolverMime)) {
            return null
        }
        val displayName = queryDisplayName(contentResolver, seed)
            ?: seed.targetUri.lastPathSegment
            ?.substringAfterLast('/')
            ?.let(ImageRequestPolicy::validateDisplayName)
            ?: return null
        val provisional = ImageViewerRequest(seed.targetUri, displayName, 0L, resolverMime ?: seed.mimeType)
        val metadata = readMetadata(contentResolver, provisional) ?: return null
        return provisional.copy(declaredSize = metadata.byteSize.coerceAtLeast(0L))
    }

    fun readMetadata(
        contentResolver: ContentResolver,
        request: ImageViewerRequest,
    ): ImageMetadata? = readContentMetadata(contentResolver, request, includeExif = false)

    fun readDisplayMetadata(
        contentResolver: ContentResolver,
        request: ImageViewerRequest,
    ): ImageMetadata? = readContentMetadata(contentResolver, request, includeExif = true)

    private fun readContentMetadata(
        contentResolver: ContentResolver,
        request: ImageViewerRequest,
        includeExif: Boolean,
    ): ImageMetadata? = runCatching {
        val assetLength = contentResolver.openAssetFileDescriptor(request.targetUri, "r")?.use { descriptor ->
            val length = descriptor.length
            if (length > ImageRequestPolicy.MAX_DECLARED_SIZE) return null
            length
        } ?: return null

        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        var exifMetadata: ImageExifMetadata? = null
        contentResolver.openFileDescriptor(request.targetUri, "r")?.use { descriptor ->
            BitmapFactory.decodeFileDescriptor(descriptor.fileDescriptor, null, options)
            if (includeExif) {
                runCatching { Os.lseek(descriptor.fileDescriptor, 0L, OsConstants.SEEK_SET) }
                exifMetadata = ImageExifMetadataReader.read(descriptor.fileDescriptor)
            }
        } ?: return null
        val queriedSize = querySize(contentResolver, request).takeIf { it > 0L }
            ?: assetLength.takeIf { it >= 0L }
            ?: request.declaredSize
        if (queriedSize > ImageRequestPolicy.MAX_DECLARED_SIZE) return null
        val finalExifMetadata = exifMetadata ?: if (includeExif) {
            readExifMetadataFromStream(contentResolver, request)
        } else {
            null
        }
        val (displayWidth, displayHeight) = ImageExifOrientationCorrection.displayDimensions(
            width = options.outWidth.takeIf { it > 0 },
            height = options.outHeight.takeIf { it > 0 },
            orientation = finalExifMetadata?.orientation,
        )
        ImageMetadata(
            byteSize = queriedSize,
            width = displayWidth,
            height = displayHeight,
            exif = finalExifMetadata,
            decodedColor = decodedColorMetadata(options),
        )
    }.getOrNull()

    fun readHostMetadata(
        descriptor: ParcelFileDescriptor,
        request: ImageViewerRequest,
    ): ImageMetadata? = runCatching {
        val descriptorSize = descriptor.statSize
        if (descriptorSize > ImageRequestPolicy.MAX_DECLARED_SIZE) return null

        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFileDescriptor(descriptor.fileDescriptor, null, options)
        runCatching { Os.lseek(descriptor.fileDescriptor, 0L, OsConstants.SEEK_SET) }
        val byteSize = descriptorSize.takeIf { it >= 0L } ?: request.declaredSize
        if (!ImageRequestPolicy.isDeclaredSizeAccepted(byteSize)) return null
        if (request.declaredSize > 0L && descriptorSize >= 0L && descriptorSize != request.declaredSize) {
            return null
        }
        val exifMetadata = ImageExifMetadataReader.read(descriptor.fileDescriptor)
        val (displayWidth, displayHeight) = ImageExifOrientationCorrection.displayDimensions(
            width = options.outWidth.takeIf { it > 0 },
            height = options.outHeight.takeIf { it > 0 },
            orientation = exifMetadata?.orientation,
        )
        ImageMetadata(
            byteSize = byteSize,
            width = displayWidth,
            height = displayHeight,
            exif = exifMetadata,
            decodedColor = decodedColorMetadata(options),
        )
    }.getOrNull()

    private fun decodedColorMetadata(options: BitmapFactory.Options): ImageDecodedColorMetadata? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return null
        return decodedColorMetadataApi26(options)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @Suppress("DEPRECATION")
    private fun decodedColorMetadataApi26(
        options: BitmapFactory.Options,
    ): ImageDecodedColorMetadata? {
        val bitsPerPixel = when (options.outConfig) {
            Bitmap.Config.ALPHA_8 -> 8
            Bitmap.Config.RGB_565,
            Bitmap.Config.ARGB_4444,
            -> 16
            Bitmap.Config.ARGB_8888 -> 32
            Bitmap.Config.RGBA_F16 -> 64
            else -> if (
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                options.outConfig == Bitmap.Config.RGBA_1010102
            ) {
                32
            } else {
                null
            }
        }
        return ImageDecodedColorMetadataPolicy.create(
            bitsPerPixel = bitsPerPixel,
            rawColorSpaceName = options.outColorSpace?.name,
        )
    }

    private fun readExifMetadataFromStream(
        contentResolver: ContentResolver,
        request: ImageViewerRequest,
    ): ImageExifMetadata? = runCatching {
        contentResolver.openInputStream(request.targetUri)?.use(ImageExifMetadataReader::read)
    }.getOrNull()

    private fun queryDisplayName(
        contentResolver: ContentResolver,
        seed: ExternalImageSeed,
    ): String? = querySingleValue(contentResolver, seed.targetUri, OpenableColumns.DISPLAY_NAME) { cursor, index ->
        cursor.getString(index)?.let(ImageRequestPolicy::validateDisplayName)
    }

    private fun querySize(
        contentResolver: ContentResolver,
        request: ImageViewerRequest,
    ): Long = querySingleValue(contentResolver, request.targetUri, OpenableColumns.SIZE) { cursor, index ->
        cursor.takeUnless { it.isNull(index) }?.getLong(index)
            ?.takeIf(ImageRequestPolicy::isDeclaredSizeAccepted)
    } ?: request.declaredSize

    private fun <T> querySingleValue(
        contentResolver: ContentResolver,
        uri: android.net.Uri,
        column: String,
        read: (Cursor, Int) -> T?,
    ): T? = runCatching {
        contentResolver.query(uri, arrayOf(column), null, null, null)?.use { cursor ->
            if (!cursor.moveToFirst()) return@use null
            val index = cursor.getColumnIndex(column)
            if (index < 0) null else read(cursor, index)
        }
    }.getOrNull()
}
