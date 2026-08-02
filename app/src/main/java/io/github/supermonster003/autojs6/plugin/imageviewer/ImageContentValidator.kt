package io.github.supermonster003.autojs6.plugin.imageviewer

import android.content.ContentResolver
import android.database.Cursor
import android.graphics.BitmapFactory
import android.provider.OpenableColumns

internal data class ImageMetadata(
    val byteSize: Long,
    val width: Int?,
    val height: Int?,
)

internal object ImageContentValidator {

    fun validateExplorer(
        contentResolver: ContentResolver,
        request: ImageViewerRequest,
    ): ImageViewerRequest? {
        val resolverMime = runCatching { contentResolver.getType(request.targetUri) }.getOrNull()
        if (
            resolverMime != null &&
            !ImageRequestPolicy.mimeTypesAreCompatible(request.mimeType, resolverMime)
        ) {
            return null
        }
        val metadata = readMetadata(contentResolver, request) ?: return null
        if (request.declaredSize > 0L && metadata.byteSize >= 0L && metadata.byteSize != request.declaredSize) {
            return null
        }
        return request.copy(mimeType = resolverMime ?: request.mimeType)
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
    ): ImageMetadata? = runCatching {
        val assetLength = contentResolver.openAssetFileDescriptor(request.targetUri, "r")?.use { descriptor ->
            val length = descriptor.length
            if (length > ImageRequestPolicy.MAX_DECLARED_SIZE) return null
            length
        } ?: return null

        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        contentResolver.openFileDescriptor(request.targetUri, "r")?.use { descriptor ->
            BitmapFactory.decodeFileDescriptor(descriptor.fileDescriptor, null, options)
        } ?: return null
        val queriedSize = querySize(contentResolver, request).takeIf { it > 0L }
            ?: assetLength.takeIf { it >= 0L }
            ?: request.declaredSize
        if (queriedSize > ImageRequestPolicy.MAX_DECLARED_SIZE) return null
        ImageMetadata(
            byteSize = queriedSize,
            width = options.outWidth.takeIf { it > 0 },
            height = options.outHeight.takeIf { it > 0 },
        )
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
