package io.github.supermonster003.autojs6.plugin.three.maple.image

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * The editor's built-in filters (ROADMAP 8.1, D11): each is a 4 x 5 color matrix in Android's `ColorMatrix` layout
 * (rows R, G, B, A; columns R, G, B, A, offset in 0..255). [NONE] is the identity.
 */
internal enum class FilterPreset(private val build: () -> FloatArray) {
    NONE({ LookMath.identity() }),
    MONO({ LookMath.monochrome() }),
    NOIR({ LookMath.concat(LookMath.monochrome(), LookMath.contrast(1.45f)) }),
    SEPIA({ LookMath.sepia() }),
    VINTAGE({ LookMath.concat(LookMath.lerp(LookMath.identity(), LookMath.sepia(), 0.55f), LookMath.contrast(0.88f), LookMath.offset(12f, 6f, -8f)) }),
    COOL({ LookMath.concat(LookMath.scale(0.92f, 1.0f, 1.12f), LookMath.offset(-6f, 0f, 10f)) }),
    WARM({ LookMath.concat(LookMath.scale(1.12f, 1.02f, 0.88f), LookMath.offset(10f, 4f, -8f)) }),
    FILM({ LookMath.concat(LookMath.saturation(0.82f), LookMath.contrast(1.12f), LookMath.offset(8f, 6f, 4f)) }),
    FADE({ LookMath.concat(LookMath.contrast(0.78f), LookMath.saturation(0.8f), LookMath.offset(18f, 18f, 18f)) }),
    HIGH_CONTRAST({ LookMath.contrast(1.4f) }),
    SOFT({ LookMath.concat(LookMath.contrast(0.9f), LookMath.saturation(0.9f), LookMath.offset(10f, 10f, 10f)) }),
    VIVID({ LookMath.concat(LookMath.saturation(1.5f), LookMath.contrast(1.06f)) }),
    MATTE({ LookMath.concat(LookMath.contrast(0.84f), LookMath.saturation(0.88f), LookMath.offset(22f, 20f, 18f)) }),
    CHROME({ LookMath.concat(LookMath.contrast(1.18f), LookMath.saturation(1.18f), LookMath.scale(0.98f, 1.0f, 1.04f)) }),
    INVERT({ LookMath.invert() }),
    ;

    /** The full-strength matrix (20 values). */
    val matrix: FloatArray get() = build()

    /** [matrix] blended with the identity: 0 leaves the image as it is, 100 is the full preset. */
    fun matrix(intensity: Int): FloatArray = LookMath.lerp(LookMath.identity(), build(), intensity.coerceIn(0, 100) / 100f)

    /** Grayscale presets (sepia is a tinted grayscale) drop the color information on purpose: not invertible. */
    val monochrome: Boolean get() = this == MONO || this == NOIR || this == SEPIA

    companion object {
        const val DEFAULT_INTENSITY = 100
    }
}

/**
 * Every adjustment of the editor (ROADMAP 8.1). The first group is a color matrix; [HIGHLIGHTS] and [SHADOWS] are a
 * tone curve, [SHARPEN] a convolution and [VIGNETTE] a radial falloff. Values run from -100 to 100 (sharpen 0..100).
 */
internal enum class LookAdjustment(val range: IntRange = -100..100) {
    BRIGHTNESS,
    CONTRAST,
    SATURATION,
    TEMPERATURE,
    EXPOSURE,
    HUE,
    HIGHLIGHTS,
    SHADOWS,
    SHARPEN(0..100),
    VIGNETTE,
}

/**
 * Pending look of the editor's look panel: one filter at an intensity plus any number of adjustments, previewed
 * together and committed as one history step. Values outside an adjustment's range are clamped.
 */
internal data class EditorLook(
    val filter: FilterPreset = FilterPreset.NONE,
    val intensity: Int = FilterPreset.DEFAULT_INTENSITY,
    val adjustments: Map<LookAdjustment, Int> = emptyMap(),
) {
    fun value(adjustment: LookAdjustment): Int = (adjustments[adjustment] ?: 0).coerceIn(adjustment.range)

    fun with(adjustment: LookAdjustment, value: Int): EditorLook {
        val clamped = value.coerceIn(adjustment.range)
        return copy(adjustments = if (clamped == 0) adjustments - adjustment else adjustments + (adjustment to clamped))
    }

    /** Nothing would change: no filter (or zero intensity) and every adjustment at zero. */
    val isIdentity: Boolean get() = (filter == FilterPreset.NONE || intensity <= 0) && LookAdjustment.entries.all { value(it) == 0 }

    /** The color matrix part: the filter, then brightness, contrast, saturation, temperature, exposure and hue. */
    fun colorMatrix(): FloatArray = LookMath.concat(
        filter.matrix(intensity),
        LookMath.brightness(value(LookAdjustment.BRIGHTNESS)),
        LookMath.contrastValue(value(LookAdjustment.CONTRAST)),
        LookMath.saturation((1f + value(LookAdjustment.SATURATION) / 100f).coerceAtLeast(0f)),
        LookMath.temperature(value(LookAdjustment.TEMPERATURE)),
        LookMath.exposure(value(LookAdjustment.EXPOSURE)),
        LookMath.hue(value(LookAdjustment.HUE) * 1.8f),
    )

    /** Whether pixels need more than the color matrix (tone curve, sharpening or vignette). */
    val needsPixelPass: Boolean
        get() = value(LookAdjustment.HIGHLIGHTS) != 0 || value(LookAdjustment.SHADOWS) != 0 ||
            value(LookAdjustment.SHARPEN) != 0 || value(LookAdjustment.VIGNETTE) != 0
}

/** Android-free color math of [EditorLook]: 4 x 5 matrices in `ColorMatrix` layout, the tone curve, the vignette. */
internal object LookMath {

    private const val LUMA_R = 0.2126f
    private const val LUMA_G = 0.7152f
    private const val LUMA_B = 0.0722f

    fun identity(): FloatArray = floatArrayOf(
        1f, 0f, 0f, 0f, 0f,
        0f, 1f, 0f, 0f, 0f,
        0f, 0f, 1f, 0f, 0f,
        0f, 0f, 0f, 1f, 0f,
    )

    fun scale(r: Float, g: Float, b: Float): FloatArray = floatArrayOf(
        r, 0f, 0f, 0f, 0f,
        0f, g, 0f, 0f, 0f,
        0f, 0f, b, 0f, 0f,
        0f, 0f, 0f, 1f, 0f,
    )

    fun offset(r: Float, g: Float, b: Float): FloatArray = identity().also {
        it[4] = r
        it[9] = g
        it[14] = b
    }

    fun contrast(scale: Float): FloatArray {
        val offset = 128f * (1f - scale)
        return floatArrayOf(
            scale, 0f, 0f, 0f, offset,
            0f, scale, 0f, 0f, offset,
            0f, 0f, scale, 0f, offset,
            0f, 0f, 0f, 1f, 0f,
        )
    }

    /** The same contrast curve as the original editor adjustment (-100..100). */
    fun contrastValue(value: Int): FloatArray = contrast((1f + value.coerceIn(-100, 100) / 100f).coerceAtLeast(0.05f))

    /** The same brightness offset as the original editor adjustment (-100..100 maps to -255..255). */
    fun brightness(value: Int): FloatArray {
        val offset = value.coerceIn(-100, 100) / 100f * 255f
        return offset(offset, offset, offset)
    }

    /** `ColorMatrix.setSaturation`: 0 is gray, 1 unchanged. */
    fun saturation(amount: Float): FloatArray {
        val inv = 1f - amount
        val r = LUMA_R * inv
        val g = LUMA_G * inv
        val b = LUMA_B * inv
        return floatArrayOf(
            r + amount, g, b, 0f, 0f,
            r, g + amount, b, 0f, 0f,
            r, g, b + amount, 0f, 0f,
            0f, 0f, 0f, 1f, 0f,
        )
    }

    /** The same warm / cool channel scales as the original editor adjustment (-100..100). */
    fun temperature(value: Int): FloatArray {
        val n = value.coerceIn(-100, 100) / 100f
        return scale(1f + n * 0.35f, 1f + n * 0.08f, 1f - n * 0.35f)
    }

    /** Exposure in stops: -100..100 maps to -1.5..+1.5 EV, a linear gain. */
    fun exposure(value: Int): FloatArray {
        val gain = 2f.pow(value.coerceIn(-100, 100) / 100f * 1.5f)
        return scale(gain, gain, gain)
    }

    /** Hue rotation by [degrees] that keeps luminance (the classic luminance-preserving rotation). */
    fun hue(degrees: Float): FloatArray {
        if (degrees == 0f) return identity()
        val radians = degrees / 180f * PI.toFloat()
        val c = cos(radians)
        val s = sin(radians)
        return floatArrayOf(
            LUMA_R + c * (1 - LUMA_R) + s * -LUMA_R, LUMA_G + c * -LUMA_G + s * -LUMA_G, LUMA_B + c * -LUMA_B + s * (1 - LUMA_B), 0f, 0f,
            LUMA_R + c * -LUMA_R + s * 0.143f, LUMA_G + c * (1 - LUMA_G) + s * 0.140f, LUMA_B + c * -LUMA_B + s * -0.283f, 0f, 0f,
            LUMA_R + c * -LUMA_R + s * -(1 - LUMA_R), LUMA_G + c * -LUMA_G + s * LUMA_G, LUMA_B + c * (1 - LUMA_B) + s * LUMA_B, 0f, 0f,
            0f, 0f, 0f, 1f, 0f,
        )
    }

    fun monochrome(): FloatArray = floatArrayOf(
        LUMA_R, LUMA_G, LUMA_B, 0f, 0f,
        LUMA_R, LUMA_G, LUMA_B, 0f, 0f,
        LUMA_R, LUMA_G, LUMA_B, 0f, 0f,
        0f, 0f, 0f, 1f, 0f,
    )

    fun sepia(): FloatArray = floatArrayOf(
        0.393f, 0.769f, 0.189f, 0f, 0f,
        0.349f, 0.686f, 0.168f, 0f, 0f,
        0.272f, 0.534f, 0.131f, 0f, 0f,
        0f, 0f, 0f, 1f, 0f,
    )

    fun invert(): FloatArray = floatArrayOf(
        -1f, 0f, 0f, 0f, 255f,
        0f, -1f, 0f, 0f, 255f,
        0f, 0f, -1f, 0f, 255f,
        0f, 0f, 0f, 1f, 0f,
    )

    /** Element-wise blend: `a` at 0, `b` at 1. */
    fun lerp(a: FloatArray, b: FloatArray, t: Float): FloatArray = FloatArray(20) { a[it] + (b[it] - a[it]) * t }

    /** Applies [first], then each of [then] in order (what `ColorMatrix.postConcat` chains build). */
    fun concat(first: FloatArray, vararg then: FloatArray): FloatArray = then.fold(first) { acc, next -> multiply(next, acc) }

    /** `a * b` for 4 x 5 matrices with an implied last row (0, 0, 0, 0, 1): applies `b`, then `a`. */
    fun multiply(a: FloatArray, b: FloatArray): FloatArray {
        val out = FloatArray(20)
        for (row in 0 until 4) {
            for (col in 0 until 5) {
                var sum = if (col == 4) a[row * 5 + 4] else 0f
                for (k in 0 until 4) sum += a[row * 5 + k] * b[k * 5 + col]
                out[row * 5 + col] = sum
            }
        }
        return out
    }

    /** Applies [matrix] to one channel triple (alpha kept), clamped to 0..255. */
    fun apply(matrix: FloatArray, r: Int, g: Int, b: Int, a: Int): IntArray = IntArray(4) { row ->
        val v = matrix[row * 5] * r + matrix[row * 5 + 1] * g + matrix[row * 5 + 2] * b + matrix[row * 5 + 3] * a + matrix[row * 5 + 4]
        v.roundToInt().coerceIn(0, 255)
    }

    /** Determinant of the 3 x 3 color part; zero means colors collapse (grayscale presets). */
    fun colorDeterminant(m: FloatArray): Float =
        m[0] * (m[6] * m[12] - m[7] * m[11]) - m[1] * (m[5] * m[12] - m[7] * m[10]) + m[2] * (m[5] * m[11] - m[6] * m[10])

    /**
     * Tone curve of [shadows] and [highlights] (-100..100 each) as a 256-entry lookup table:
     * `y = x + k (s x (1 - x)^2 + h x^2 (1 - x))`. Shadows act most around a third of the range, highlights around
     * two thirds; black and white stay where they are, and with `k` = 0.9 the curve stays monotonic for all values.
     */
    fun toneCurve(shadows: Int, highlights: Int): IntArray {
        val s = shadows.coerceIn(-100, 100) / 100f
        val h = highlights.coerceIn(-100, 100) / 100f
        return IntArray(256) { level ->
            val x = level / 255f
            val y = x + TONE_STRENGTH * (s * x * (1f - x) * (1f - x) + h * x * x * (1f - x))
            (y * 255f).roundToInt().coerceIn(0, 255)
        }
    }

    private const val TONE_STRENGTH = 0.9f

    /**
     * Multiplier of the vignette at pixel ([x], [y]) of a [width] x [height] image: 1 in the middle; [strength]
     * -100..100 darkens (positive) or lightens (negative) the corners up to 60 percent.
     */
    fun vignette(x: Float, y: Float, width: Int, height: Int, strength: Int): Float {
        if (strength == 0) return 1f
        val dx = (x - width / 2f) / (width / 2f)
        val dy = (y - height / 2f) / (height / 2f)
        val distance = (sqrt(dx * dx + dy * dy) / sqrt(2f)).coerceIn(0f, 1f)
        val falloff = smoothstep(0.35f, 1f, distance)
        return 1f - strength / 100f * 0.6f * falloff
    }

    private fun smoothstep(edge0: Float, edge1: Float, x: Float): Float {
        val t = ((x - edge0) / (edge1 - edge0)).coerceIn(0f, 1f)
        return t * t * (3f - 2f * t)
    }
}

/**
 * The per-pixel part of an [EditorLook] for an image of [width] x [height] ARGB pixels (not premultiplied, as
 * `Bitmap.getPixels` returns them): color matrix, tone curve, unsharp mask (3 x 3) and vignette. Works on bands of
 * rows so a 20 MP image never needs a full pixel copy; sharpening reads one extra row above and below the band. The
 * matrix is folded into per-channel lookup tables, the 3 x 3 mean is a sum of row sums, and the vignette is a table
 * over the squared distance, so a pixel costs a few dozen integer operations.
 */
internal class LookPixelPass(look: EditorLook, private val width: Int, private val height: Int) {

    private val matrix = look.colorMatrix()
    private val tone: IntArray? = if (look.value(LookAdjustment.SHADOWS) != 0 || look.value(LookAdjustment.HIGHLIGHTS) != 0) {
        LookMath.toneCurve(look.value(LookAdjustment.SHADOWS), look.value(LookAdjustment.HIGHLIGHTS))
    } else {
        null
    }
    private val sharpen = look.value(LookAdjustment.SHARPEN) / 100f * SHARPEN_GAIN
    private val sharpenFixed = (sharpen * FIXED_ONE).toInt()
    private val vignette = look.value(LookAdjustment.VIGNETTE)

    /** Factor of the vignette (fixed point, [FIXED_ONE] = 1) by squared normalized distance, [VIGNETTE_STEPS] steps. */
    private val vignetteTable: IntArray? = if (vignette == 0) {
        null
    } else {
        IntArray(VIGNETTE_STEPS + 1) { step ->
            val squared = step.toFloat() / VIGNETTE_STEPS * 2f
            val x = width / 2f * (1f + kotlin.math.sqrt(squared / 2f) * kotlin.math.sqrt(2f))
            (LookMath.vignette(x, height / 2f, width, height, vignette) * FIXED_ONE).toInt()
        }
    }
    private val dx2: FloatArray? = vignetteTable?.let {
        FloatArray(width) { x -> ((x + 0.5f - width / 2f) / (width / 2f)).let { d -> d * d } }
    }

    /** Rows of context [process] needs above and below a band. */
    val halo: Int get() = if (sharpen > 0f) 1 else 0

    /**
     * Turns rows `top until bottom` into [output] (`(bottom - top) * width` pixels). [input] holds rows
     * `max(0, top - halo) until min(height, bottom + halo)`, starting at [inputTop].
     */
    fun process(input: IntArray, inputTop: Int, top: Int, bottom: Int, output: IntArray) {
        val rows = input.size / width
        val graded = IntArray(input.size)
        for (i in input.indices) graded[i] = grade(input[i])
        val sums = if (sharpenFixed > 0) rowSums(graded, rows) else null
        for (y in top until bottom) {
            val row = y - inputTop
            val above = maxOf(row - 1, 0) * width
            val here = row * width
            val below = minOf(row + 1, rows - 1) * width
            val dy = (y + 0.5f - height / 2f) / (height / 2f)
            val dy2 = dy * dy
            val out = (y - top) * width
            for (x in 0 until width) {
                var pixel = graded[here + x]
                if (sums != null) {
                    val sr = sums[0][above + x] + sums[0][here + x] + sums[0][below + x]
                    val sg = sums[1][above + x] + sums[1][here + x] + sums[1][below + x]
                    val sb = sums[2][above + x] + sums[2][here + x] + sums[2][below + x]
                    pixel = sharpenPixel(pixel, sr, sg, sb)
                }
                if (vignetteTable != null) {
                    val index = ((dx2!![x] + dy2) / 2f * VIGNETTE_STEPS).toInt().coerceIn(0, VIGNETTE_STEPS)
                    pixel = scaleRgb(pixel, vignetteTable[index])
                }
                output[out + x] = pixel
            }
        }
    }

    private fun grade(pixel: Int): Int {
        val a = pixel ushr 24
        val r = pixel shr 16 and 0xFF
        val g = pixel shr 8 and 0xFF
        val b = pixel and 0xFF
        val m = matrix
        var nr = clamp(m[0] * r + m[1] * g + m[2] * b + m[3] * a + m[4])
        var ng = clamp(m[5] * r + m[6] * g + m[7] * b + m[8] * a + m[9])
        var nb = clamp(m[10] * r + m[11] * g + m[12] * b + m[13] * a + m[14])
        val na = clamp(m[15] * r + m[16] * g + m[17] * b + m[18] * a + m[19])
        tone?.let {
            nr = it[nr]
            ng = it[ng]
            nb = it[nb]
        }
        return na shl 24 or (nr shl 16) or (ng shl 8) or nb
    }

    /** Per channel, each pixel's sum with its left and right neighbours (edges repeat). */
    private fun rowSums(graded: IntArray, rows: Int): Array<IntArray> {
        val sums = Array(3) { IntArray(graded.size) }
        for (row in 0 until rows) {
            val base = row * width
            for (x in 0 until width) {
                val left = graded[base + if (x > 0) x - 1 else 0]
                val mid = graded[base + x]
                val right = graded[base + if (x < width - 1) x + 1 else x]
                sums[0][base + x] = (left shr 16 and 0xFF) + (mid shr 16 and 0xFF) + (right shr 16 and 0xFF)
                sums[1][base + x] = (left shr 8 and 0xFF) + (mid shr 8 and 0xFF) + (right shr 8 and 0xFF)
                sums[2][base + x] = (left and 0xFF) + (mid and 0xFF) + (right and 0xFF)
            }
        }
        return sums
    }

    /** `c + k (c - mean)` with the 3 x 3 sums, in fixed point. */
    private fun sharpenPixel(center: Int, sr: Int, sg: Int, sb: Int): Int {
        val r = center shr 16 and 0xFF
        val g = center shr 8 and 0xFF
        val b = center and 0xFF
        val nr = clampFixed(r * FIXED_ONE + sharpenFixed * (r * 9 - sr) / 9)
        val ng = clampFixed(g * FIXED_ONE + sharpenFixed * (g * 9 - sg) / 9)
        val nb = clampFixed(b * FIXED_ONE + sharpenFixed * (b * 9 - sb) / 9)
        return (center and -0x1000000) or (nr shl 16) or (ng shl 8) or nb
    }

    private fun scaleRgb(pixel: Int, factor: Int): Int {
        val nr = clampFixed((pixel shr 16 and 0xFF) * factor)
        val ng = clampFixed((pixel shr 8 and 0xFF) * factor)
        val nb = clampFixed((pixel and 0xFF) * factor)
        return (pixel and -0x1000000) or (nr shl 16) or (ng shl 8) or nb
    }

    private fun clamp(value: Float): Int {
        val v = (value + 0.5f).toInt()
        return if (v < 0) 0 else if (v > 255) 255 else v
    }

    private fun clampFixed(value: Int): Int {
        val v = (value + FIXED_HALF) shr FIXED_SHIFT
        return if (v < 0) 0 else if (v > 255) 255 else v
    }

    private companion object {
        /** Sharpen 100 adds 1.5 times the difference to the 3 x 3 mean. */
        const val SHARPEN_GAIN = 1.5f
        const val FIXED_SHIFT = 12
        const val FIXED_ONE = 1 shl FIXED_SHIFT
        const val FIXED_HALF = FIXED_ONE / 2
        const val VIGNETTE_STEPS = 1024
    }
}
