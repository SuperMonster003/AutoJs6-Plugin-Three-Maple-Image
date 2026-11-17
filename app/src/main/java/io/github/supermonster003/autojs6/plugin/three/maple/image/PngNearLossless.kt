package io.github.supermonster003.autojs6.plugin.three.maple.image

/**
 * How far lossy PNG output may move a pixel. A pixel's limit follows its activity, the largest channel difference
 * to its four neighbors in the source:
 * - a pixel equal to all its neighbors is never changed, so flat backgrounds keep their exact color;
 * - where neighbors differ by no more than [smoothTolerance] (noise, gentle gradients) the limit is the activity
 *   itself, so only variations that small can vanish;
 * - elsewhere it is half the activity, at least [smoothTolerance] and at most [maxTolerance]: edges and texture
 *   hide larger errors, and the limit stays below the local contrast, so no visible detail is erased.
 */
internal data class PngLossyLevel(
    val maxTolerance: Int,
    val smoothTolerance: Int,
) {
    init {
        require(maxTolerance in 1..MAX_TOLERANCE) { "Tolerance must be between 1 and $MAX_TOLERANCE" }
        require(smoothTolerance in 1..maxTolerance) { "Smooth tolerance must be between 1 and the tolerance" }
    }

    fun toleranceForActivity(activity: Int): Int =
        if (activity <= smoothTolerance) activity else minOf(maxTolerance, maxOf(smoothTolerance, activity / 2))

    companion object {
        const val MAX_TOLERANCE = 16
    }
}

/**
 * What the quality of lossy PNG output means: 100 changes nothing, and every ten points below it are one level
 * that lets pixels move a little further (see [PngLossyLevel]). Even the lowest level keeps flat areas exact and
 * stays below the local contrast, so the scale has no setting that visibly damages an image; it trades the last
 * bit of fidelity in noise, texture and edges for file size.
 */
internal object PngLossyPolicy {

    const val LOSSLESS_QUALITY = 100

    private val LEVELS = listOf(
        PngLossyLevel(maxTolerance = 1, smoothTolerance = 1),
        PngLossyLevel(maxTolerance = 2, smoothTolerance = 1),
        PngLossyLevel(maxTolerance = 2, smoothTolerance = 2),
        PngLossyLevel(maxTolerance = 4, smoothTolerance = 2),
        PngLossyLevel(maxTolerance = 4, smoothTolerance = 3),
        PngLossyLevel(maxTolerance = 6, smoothTolerance = 3),
        PngLossyLevel(maxTolerance = 8, smoothTolerance = 4),
        PngLossyLevel(maxTolerance = 10, smoothTolerance = 5),
        PngLossyLevel(maxTolerance = 12, smoothTolerance = 6),
        PngLossyLevel(maxTolerance = 16, smoothTolerance = 8),
    )

    /** Share of the lossless size a photograph keeps at each level (measured on sample images; estimates only). */
    private val ESTIMATED_SIZE_FACTORS = doubleArrayOf(0.62, 0.54, 0.48, 0.45, 0.42, 0.41, 0.40, 0.39, 0.38, 0.37)

    /** Qualities that encode differently: 100 and the highest quality of each level, from the best downwards. */
    val DISTINCT_QUALITIES: List<Int> =
        listOf(LOSSLESS_QUALITY) + LEVELS.indices.map { LOSSLESS_QUALITY - 1 - it * QUALITIES_PER_LEVEL }

    /** The level of [quality] (1 to 100), or null at 100, where the output is lossless. */
    fun level(quality: Int): PngLossyLevel? = levelIndex(quality)?.let(LEVELS::get)

    /** How large lossy output at [quality] is expected to be, as a share of the lossless size. */
    fun estimatedSizeFactor(quality: Int): Double = levelIndex(quality)?.let { ESTIMATED_SIZE_FACTORS[it] } ?: 1.0

    private fun levelIndex(quality: Int): Int? {
        require(quality in ImageConversionOptions.MIN_QUALITY..ImageConversionOptions.MAX_QUALITY) {
            "Quality must be between 1 and 100"
        }
        return if (quality >= LOSSLESS_QUALITY) null else (LOSSLESS_QUALITY - 1 - quality) / QUALITIES_PER_LEVEL
    }

    private const val QUALITIES_PER_LEVEL = 10
}

/**
 * Near-lossless preparation of PNG rows (the idea of JPEG-LS near-lossless coding, applied to PNG's own row
 * filters). PNG stores the difference between a pixel and a prediction from pixels already written; the smaller
 * and the more repetitive the differences, the better they deflate. This filter predicts from the pixels it has
 * already reconstructed, keeps the prediction whenever the source is within the pixel's tolerance, and otherwise
 * moves by a multiple of `2 * tolerance + 1`, so that the result never leaves the tolerance. Each row is tried with
 * the left, upper, average and Paeth predictors and keeps the cheapest; [filterType] tells the encoder which PNG
 * filter reproduces those differences.
 *
 * Rows must be fed from top to bottom. Alpha is never changed. The same input always gives the same rows, so a
 * second pass over an image reconstructs exactly what the first pass saw.
 */
internal class PngNearLosslessFilter(
    private val width: Int,
    private val level: PngLossyLevel,
) {
    init {
        require(width > 0) { "Width must be positive" }
    }

    private val tolerance = IntArray(width)
    private var previous = IntArray(width * CHANNELS)
    private var best = IntArray(width * CHANNELS)
    private var trial = IntArray(width * CHANNELS)

    /** The PNG filter type (1 Sub, 2 Up, 3 Average, 4 Paeth) of the row [process] handled last. */
    var filterType: Int = FILTER_SUB
        private set

    /**
     * Reconstructs [row]; [above] and [below] are its neighbors in the source (null at the image border). The
     * result is read with [red], [green] and [blue] or [argb] until the next call.
     */
    fun process(above: IntArray?, row: IntArray, below: IntArray?) {
        // The previous call's result becomes the upper row; its old upper row is reused as scratch space.
        val finished = best
        best = previous
        previous = finished
        measureTolerance(above, row, below)
        var bestCost = Long.MAX_VALUE
        for (type in FILTER_SUB..FILTER_PAETH) {
            val cost = reconstruct(type, row, trial, bestCost)
            if (cost < bestCost) {
                bestCost = cost
                filterType = type
                val swapped = best
                best = trial
                trial = swapped
            }
        }
    }

    /** Forgets the rows seen so far; the next row is treated as the first row of an image. */
    fun reset() {
        previous.fill(0)
        best.fill(0)
    }

    fun red(x: Int): Int = best[x * CHANNELS]

    fun green(x: Int): Int = best[x * CHANNELS + 1]

    fun blue(x: Int): Int = best[x * CHANNELS + 2]

    /** The reconstructed pixel at [x] with the alpha of [sourcePixel]. */
    fun argb(x: Int, sourcePixel: Int): Int {
        val i = x * CHANNELS
        return (sourcePixel and ALPHA_MASK) or (best[i] shl 16) or (best[i + 1] shl 8) or best[i + 2]
    }

    /** The reconstructed channels of the row above the current one (zero before the first row), three per pixel. */
    val upperRow: IntArray get() = previous

    /** The reconstructed channels of the current row, three per pixel. */
    val currentRow: IntArray get() = best

    private fun measureTolerance(above: IntArray?, row: IntArray, below: IntArray?) {
        val last = width - 1
        for (x in 0..last) {
            val pixel = row[x]
            var activity = 0
            if (x > 0) activity = maxOf(activity, difference(pixel, row[x - 1]))
            if (x < last) activity = maxOf(activity, difference(pixel, row[x + 1]))
            if (above != null) activity = maxOf(activity, difference(pixel, above[x]))
            if (below != null) activity = maxOf(activity, difference(pixel, below[x]))
            tolerance[x] = level.toleranceForActivity(activity)
        }
    }

    /** Writes the reconstruction of [row] with predictor [type] into [out]; gives up once the cost passes [limit]. */
    private fun reconstruct(type: Int, row: IntArray, out: IntArray, limit: Long): Long {
        val upper = previous
        var cost = 0L
        var i = 0
        for (x in 0 until width) {
            val pixel = row[x]
            val allowed = tolerance[x]
            val step = 2 * allowed + 1
            var shift = 16
            while (shift >= 0) {
                val source = (pixel ushr shift) and 0xFF
                val left = if (x > 0) out[i - CHANNELS] else 0
                val up = upper[i]
                val prediction = when (type) {
                    FILTER_SUB -> left
                    FILTER_UP -> up
                    FILTER_AVERAGE -> (left + up) ushr 1
                    else -> paeth(left, up, if (x > 0) upper[i - CHANNELS] else 0)
                }
                val residual = source - prediction
                var value = prediction
                if (residual > allowed) {
                    value = prediction + (residual + allowed) / step * step
                    if (value > 255) value = 255
                } else if (residual < -allowed) {
                    value = prediction - (allowed - residual) / step * step
                    if (value < 0) value = 0
                }
                out[i] = value
                // What the row costs once filtered: the same "smallest sum of absolute differences" rule libpng uses.
                val stored = value - prediction
                val magnitude = if (stored < 0) -stored else stored
                cost += if (magnitude > 128) 256 - magnitude else magnitude
                i++
                shift -= 8
            }
            if (cost > limit) return cost
        }
        return cost
    }

    companion object {
        const val FILTER_SUB = 1
        const val FILTER_UP = 2
        const val FILTER_AVERAGE = 3
        const val FILTER_PAETH = 4

        /** Red, green and blue; alpha passes through untouched. */
        const val CHANNELS = 3

        private const val ALPHA_MASK = 0xFF shl 24

        fun paeth(left: Int, up: Int, upLeft: Int): Int {
            val estimate = left + up - upLeft
            val toLeft = if (estimate > left) estimate - left else left - estimate
            val toUp = if (estimate > up) estimate - up else up - estimate
            val toUpLeft = if (estimate > upLeft) estimate - upLeft else upLeft - estimate
            return if (toLeft <= toUp && toLeft <= toUpLeft) left else if (toUp <= toUpLeft) up else upLeft
        }

        /** The largest difference of any channel, alpha included, between two ARGB pixels. */
        fun difference(first: Int, second: Int): Int {
            var result = 0
            var shift = 24
            while (shift >= 0) {
                val delta = ((first ushr shift) and 0xFF) - ((second ushr shift) and 0xFF)
                val magnitude = if (delta < 0) -delta else delta
                if (magnitude > result) result = magnitude
                shift -= 8
            }
            return result
        }
    }
}
