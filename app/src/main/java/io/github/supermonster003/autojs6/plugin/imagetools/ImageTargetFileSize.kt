package io.github.supermonster003.autojs6.plugin.imagetools

internal enum class ImageTargetFileSizeStatus {
    WITHIN_QUALITY_RANGE,
    TARGET_BELOW_MINIMUM_QUALITY,
    TARGET_ABOVE_MAXIMUM_QUALITY,
}

internal data class ImageTargetFileSizeResult(
    val targetBytes: Long,
    val encodedBytes: Long,
    val quality: Int,
    val status: ImageTargetFileSizeStatus,
    val attemptCount: Int,
) {
    val requiresConfirmation: Boolean
        get() = status != ImageTargetFileSizeStatus.WITHIN_QUALITY_RANGE
}

internal data class ImageOutputWriteResult(
    val encodedBytes: Long,
    val quality: Int,
    val targetFileSizeResult: ImageTargetFileSizeResult? = null,
)

/** Selects the highest lossy quality whose encoded output does not exceed the requested size. */
internal object ImageTargetFileSizeSearch {

    fun search(
        targetBytes: Long,
        minQuality: Int = ImageConversionOptions.MIN_QUALITY,
        maxQuality: Int = ImageConversionOptions.MAX_QUALITY,
        measureEncodedBytes: (quality: Int) -> Long,
    ): ImageTargetFileSizeResult {
        require(targetBytes > 0L) { "Target file size must be positive" }
        require(minQuality in ImageConversionOptions.MIN_QUALITY..ImageConversionOptions.MAX_QUALITY)
        require(maxQuality in minQuality..ImageConversionOptions.MAX_QUALITY)

        val measurements = mutableMapOf<Int, Long>()
        fun measure(quality: Int): Long = measurements.getOrPut(quality) {
            measureEncodedBytes(quality).also { require(it > 0L) { "Encoded size must be positive" } }
        }

        val minimumBytes = measure(minQuality)
        if (minimumBytes > targetBytes) {
            return ImageTargetFileSizeResult(
                targetBytes = targetBytes,
                encodedBytes = minimumBytes,
                quality = minQuality,
                status = ImageTargetFileSizeStatus.TARGET_BELOW_MINIMUM_QUALITY,
                attemptCount = measurements.size,
            )
        }
        if (minimumBytes == targetBytes) {
            return ImageTargetFileSizeResult(
                targetBytes = targetBytes,
                encodedBytes = minimumBytes,
                quality = minQuality,
                status = ImageTargetFileSizeStatus.WITHIN_QUALITY_RANGE,
                attemptCount = measurements.size,
            )
        }
        if (minQuality == maxQuality) {
            return ImageTargetFileSizeResult(
                targetBytes = targetBytes,
                encodedBytes = minimumBytes,
                quality = minQuality,
                status = ImageTargetFileSizeStatus.TARGET_ABOVE_MAXIMUM_QUALITY,
                attemptCount = measurements.size,
            )
        }

        val maximumBytes = measure(maxQuality)
        if (maximumBytes < targetBytes) {
            return ImageTargetFileSizeResult(
                targetBytes = targetBytes,
                encodedBytes = maximumBytes,
                quality = maxQuality,
                status = ImageTargetFileSizeStatus.TARGET_ABOVE_MAXIMUM_QUALITY,
                attemptCount = measurements.size,
            )
        }
        if (maximumBytes == targetBytes) {
            return ImageTargetFileSizeResult(
                targetBytes = targetBytes,
                encodedBytes = maximumBytes,
                quality = maxQuality,
                status = ImageTargetFileSizeStatus.WITHIN_QUALITY_RANGE,
                attemptCount = measurements.size,
            )
        }

        var fittingQuality = minQuality
        var fittingBytes = minimumBytes
        var oversizedQuality = maxQuality
        while (oversizedQuality - fittingQuality > 1) {
            val candidateQuality = fittingQuality + (oversizedQuality - fittingQuality) / 2
            val candidateBytes = measure(candidateQuality)
            if (candidateBytes <= targetBytes) {
                fittingQuality = candidateQuality
                fittingBytes = candidateBytes
            } else {
                oversizedQuality = candidateQuality
            }
        }
        return ImageTargetFileSizeResult(
            targetBytes = targetBytes,
            encodedBytes = fittingBytes,
            quality = fittingQuality,
            status = ImageTargetFileSizeStatus.WITHIN_QUALITY_RANGE,
            attemptCount = measurements.size,
        )
    }
}

internal object ImageTargetFileSizePolicy {

    const val BYTES_PER_KIBIBYTE = 1024L
    const val DEFAULT_TARGET_KIBIBYTES = 500L

    fun isAvailable(
        format: ImageOutputFormat,
        webpLosslessRequested: Boolean,
        sdkInt: Int,
        maxOutputBytes: Long,
    ): Boolean = maxTargetKibibytes(maxOutputBytes) >= 1L &&
        ImageOutputEncodingPolicy.supportsTargetFileSize(
            format = format,
            webpLosslessRequested = webpLosslessRequested,
            sdkInt = sdkInt,
        )

    fun maxTargetKibibytes(maxOutputBytes: Long): Long =
        maxOutputBytes.coerceAtLeast(0L) / BYTES_PER_KIBIBYTE

    fun resolveTargetBytes(targetKibibytes: Long?, maxOutputBytes: Long): Long? {
        val maxKibibytes = maxTargetKibibytes(maxOutputBytes)
        if (targetKibibytes == null || targetKibibytes !in 1L..maxKibibytes) return null
        return targetKibibytes * BYTES_PER_KIBIBYTE
    }
}
