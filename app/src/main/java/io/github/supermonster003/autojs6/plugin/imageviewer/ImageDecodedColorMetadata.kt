package io.github.supermonster003.autojs6.plugin.imageviewer

internal data class ImageDecodedColorMetadata(
    val bitsPerPixel: Int?,
    val colorSpaceName: String?,
)

/** Keeps decoder-provided display metadata bounded and safe before it reaches the UI. */
internal object ImageDecodedColorMetadataPolicy {

    fun create(
        bitsPerPixel: Int?,
        rawColorSpaceName: String?,
    ): ImageDecodedColorMetadata? {
        val safeBitsPerPixel = bitsPerPixel?.takeIf { it in MIN_BITS_PER_PIXEL..MAX_BITS_PER_PIXEL }
        val safeColorSpaceName = cleanColorSpaceName(rawColorSpaceName)
        if (safeBitsPerPixel == null && safeColorSpaceName == null) return null
        return ImageDecodedColorMetadata(safeBitsPerPixel, safeColorSpaceName)
    }

    private fun cleanColorSpaceName(value: String?): String? {
        val collapsed = value?.trim()?.replace(WHITESPACE, " ")?.takeIf { it.isNotEmpty() }
            ?: return null
        if (
            collapsed.length > MAX_COLOR_SPACE_NAME_LENGTH ||
            collapsed.any(::isUnsafeCharacter)
        ) {
            return null
        }
        return collapsed
    }

    private fun isUnsafeCharacter(character: Char): Boolean =
        character.isISOControl() || character in BIDI_CONTROL_CHARACTERS

    private val WHITESPACE = Regex("\\s+")
    private val BIDI_CONTROL_CHARACTERS = setOf(
        '\u061C',
        '\u200E',
        '\u200F',
        '\u202A',
        '\u202B',
        '\u202C',
        '\u202D',
        '\u202E',
        '\u2066',
        '\u2067',
        '\u2068',
        '\u2069',
    )

    private const val MIN_BITS_PER_PIXEL = 1
    private const val MAX_BITS_PER_PIXEL = 128
    private const val MAX_COLOR_SPACE_NAME_LENGTH = 96
}
