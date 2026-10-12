package io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose

import android.annotation.SuppressLint
import com.google.android.material.color.utilities.Hct
import io.github.supermonster003.autojs6.plugin.three.maple.image.AppColorPolicy
import io.github.supermonster003.autojs6.plugin.three.maple.image.AppThemePalette
import io.github.supermonster003.autojs6.plugin.three.maple.image.ThemeAccentRoles
import io.github.supermonster003.autojs6.plugin.three.maple.image.ThemeColorValue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Design draft 5.1: every seed, including the extreme ones, must yield readable control colors in both modes. */
@SuppressLint("RestrictedApi")
class MapleColorRolesTest {

    private val extremeSeeds = listOf(
        0xFFFFDEAD.toInt(), 0xFFFFFFFF.toInt(), 0xFF000000.toInt(),
        0xFFFF0000.toInt(), 0xFF00FF00.toInt(), 0xFF0000FF.toInt(), 0xFFFFFF00.toInt(),
    )

    private fun palette(seed: Int, dark: Boolean): AppThemePalette {
        val roles = ThemeAccentRoles.fromSeed(seed, dark)
        val background = if (dark) 0xFF121212.toInt() else 0xFFF3F4F5.toInt()
        return AppThemePalette(
            seed = seed,
            primary = roles.primary,
            onPrimary = roles.onPrimary,
            accent = AppColorPolicy.readableAccent(roles.primary, background),
            windowBackground = background,
            surface = if (dark) 0xFF1E1E1E.toInt() else 0xFFFFFFFF.toInt(),
            surfaceVariant = if (dark) 0xFF1E1E1E.toInt() else 0xFFF3F4F5.toInt(),
            outline = if (dark) 0xFF777A82.toInt() else 0xFFC5C8CE.toInt(),
            primaryText = if (dark) 0xFFE6E1E5.toInt() else 0xFF1D1B20.toInt(),
            secondaryText = if (dark) 0xFFB9BAC0.toInt() else 0xFF5F6368.toInt(),
            divider = if (dark) 0xFF34363A.toInt() else 0xFFE0E3E7.toInt(),
            isDark = dark,
        )
    }

    @Test fun filledControlsStayReadableForEverySeed() {
        for (seed in ThemeColorValue.presets + extremeSeeds) for (dark in listOf(false, true)) {
            val roles = MapleColorRoles.from(palette(seed, dark))
            assertTrue("$seed dark=$dark primary", AppColorPolicy.contrastRatio(roles.onPrimary, roles.primary) >= 4.5)
            assertTrue("$seed dark=$dark accent", AppColorPolicy.contrastRatio(roles.accent, roles.windowBackground) >= 4.5)
            assertTrue("$seed dark=$dark onAccent", AppColorPolicy.contrastRatio(roles.onAccent, roles.accent) >= 3.0)
            assertTrue("$seed dark=$dark error", AppColorPolicy.contrastRatio(roles.onError, roles.error) >= 4.5)
            assertTrue("$seed dark=$dark text", AppColorPolicy.contrastRatio(roles.primaryText, roles.surface) >= 4.5)
            assertTrue("$seed dark=$dark secondary text", AppColorPolicy.contrastRatio(roles.secondaryText, roles.surface) >= 4.5)
        }
    }

    @Test fun selectionSurfaceIsAnOpaqueTintOfTheSurface() {
        for (seed in ThemeColorValue.presets) for (dark in listOf(false, true)) {
            val roles = MapleColorRoles.from(palette(seed, dark))
            assertEquals(0xFF, roles.primaryContainer ushr 24)
            assertNotEquals(roles.surface, roles.primaryContainer)
            assertTrue(AppColorPolicy.contrastRatio(roles.primaryText, roles.primaryContainer) >= 4.5)
        }
    }

    @Test fun overlayAccentAlwaysUsesTheDarkToneAgainstTheBlackCanvas() {
        for (seed in ThemeColorValue.presets + extremeSeeds) for (dark in listOf(false, true)) {
            val roles = MapleColorRoles.from(palette(seed, dark))
            assertEquals(80.0, Hct.fromInt(roles.accentOnDark).tone, 0.6)
            assertTrue(AppColorPolicy.contrastRatio(roles.accentOnDark, 0xFF000000.toInt()) >= 3.0)
        }
    }

    @Test fun compositeBlendsBetweenBackgroundAndForeground() {
        val red = 0xFFFF0000.toInt()
        val white = 0xFFFFFFFF.toInt()
        assertEquals(white, MapleColorRoles.composite(red, 0, white))
        assertEquals(red, MapleColorRoles.composite(red, 255, white))
        val half = MapleColorRoles.composite(red, 128, white)
        assertEquals(0xFF, half shr 16 and 0xFF)
        assertEquals(127, half shr 8 and 0xFF)
    }
}
