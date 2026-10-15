package io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose

import io.github.supermonster003.autojs6.plugin.three.maple.image.AppColorPolicy
import io.github.supermonster003.autojs6.plugin.three.maple.image.AppThemePalette
import io.github.supermonster003.autojs6.plugin.three.maple.image.ThemeAccentRoles

/**
 * Android-free color roles behind [MapleTheme] (design draft sections 5.1 and 5.2). Every value is an opaque ARGB
 * Int so the derivation stays testable on the JVM without Compose or Android resources.
 */
internal data class MapleColorRoles(
    val isDark: Boolean,
    val primary: Int,
    val onPrimary: Int,
    val primaryContainer: Int,
    val accent: Int,
    val onAccent: Int,
    val accentOnDark: Int,
    val windowBackground: Int,
    val surface: Int,
    val surfaceVariant: Int,
    val primaryText: Int,
    val secondaryText: Int,
    val outline: Int,
    val divider: Int,
    val error: Int,
    val onError: Int,
) {
    companion object {
        /** Tinted selection surface: primary at 11% composited over the surface (design draft 5.1). */
        const val PRIMARY_CONTAINER_ALPHA = 0x1C

        /**
         * Viewer and editor overlays (design draft 5.2), alphas out of 255: black scrims at 55% and 75%, white text
         * at 70% (secondary) and 38% (disabled). Over a pure white image white text on the 55% scrim stays at 4.7:1,
         * the 70% white only reaches 3.2:1, so secondary text sits on the 75% scrim (design draft 11, ROADMAP 9.4).
         */
        const val SCRIM_ALPHA = 140
        const val SCRIM_STRONG_ALPHA = 191
        const val ON_SCRIM_MUTED_ALPHA = 179
        const val ON_SCRIM_DISABLED_ALPHA = 97

        fun from(palette: AppThemePalette): MapleColorRoles = MapleColorRoles(
            isDark = palette.isDark,
            primary = palette.primary,
            onPrimary = palette.onPrimary,
            primaryContainer = composite(palette.primary, PRIMARY_CONTAINER_ALPHA, palette.surface),
            accent = palette.accent,
            onAccent = AppColorPolicy.onFilledColor(palette.accent),
            accentOnDark = ThemeAccentRoles.fromSeed(palette.seed, dark = true).primary,
            windowBackground = palette.windowBackground,
            surface = palette.surface,
            surfaceVariant = palette.surfaceVariant,
            primaryText = palette.primaryText,
            secondaryText = palette.secondaryText,
            outline = palette.outline,
            divider = palette.divider,
            error = if (palette.isDark) DARK_ERROR else LIGHT_ERROR,
            onError = if (palette.isDark) DARK_ON_ERROR else LIGHT_ON_ERROR,
        )

        /** Alpha-composites [foreground] with [alpha] (0..255) over an opaque [background]. */
        fun composite(foreground: Int, alpha: Int, background: Int): Int {
            val ratio = alpha.coerceIn(0, 255) / 255.0
            fun channel(shift: Int): Int {
                val top = foreground shr shift and 0xFF
                val bottom = background shr shift and 0xFF
                return (bottom + (top - bottom) * ratio).toInt().coerceIn(0, 255)
            }
            return -0x1000000 or (channel(16) shl 16) or (channel(8) shl 8) or channel(0)
        }

        private const val LIGHT_ERROR = 0xFFB3261E.toInt()
        private const val LIGHT_ON_ERROR = -0x1
        private const val DARK_ERROR = 0xFFFFB4AB.toInt()
        private const val DARK_ON_ERROR = 0xFF690005.toInt()
    }
}
