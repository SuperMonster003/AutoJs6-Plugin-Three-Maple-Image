package io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose

import io.github.supermonster003.autojs6.plugin.three.maple.image.AppThemePalette
import io.github.supermonster003.autojs6.plugin.three.maple.image.ThemeAccentRoles

/** Light palette shared by the component tests; mirrors a resolved NavajoWhite seed. */
internal fun testPalette(): AppThemePalette {
    val seed = 0xFFFFDEAD.toInt()
    val roles = ThemeAccentRoles.fromSeed(seed, false)
    return AppThemePalette(
        seed = seed, primary = roles.primary, onPrimary = roles.onPrimary, accent = 0xFF005EA8.toInt(),
        windowBackground = 0xFFF3F4F5.toInt(), surface = 0xFFFFFFFF.toInt(), surfaceVariant = 0xFFF3F4F5.toInt(),
        outline = 0xFFC5C8CE.toInt(), primaryText = 0xFF1D1B20.toInt(), secondaryText = 0xFF5F6368.toInt(),
        divider = 0xFFE0E3E7.toInt(), isDark = false,
    )
}
