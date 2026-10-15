package io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose

import android.text.TextUtils
import android.view.View
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.sp
import io.github.supermonster003.autojs6.plugin.three.maple.image.AppThemePalette

/**
 * Compose theme of the standalone app (design draft section 5). The neutral surfaces, the HCT accent roles and
 * the dark-canvas overlay tokens are derived once from the [AppThemePalette] that [ConfiguredActivity] already
 * resolves, so View screens and Compose screens share one source of truth while the migration is in progress.
 */
@Immutable
internal data class MapleCanvasTokens(
    val viewerCanvas: Color,
    val editorCanvas: Color,
    val scrim: Color,
    val scrimStrong: Color,
    val onScrim: Color,
    val onScrimMuted: Color,
    val onScrimDisabled: Color,
    val accentOnScrim: Color,
    val errorOnScrim: Color,
)

internal val LocalMapleCanvasTokens = staticCompositionLocalOf<MapleCanvasTokens> {
    error("MapleCanvasTokens are only available inside MapleTheme")
}

internal val LocalAppThemePalette = staticCompositionLocalOf<AppThemePalette> {
    error("AppThemePalette is only available inside MapleTheme")
}

internal object MapleTheme {
    val canvas: MapleCanvasTokens
        @Composable get() = LocalMapleCanvasTokens.current
    val palette: AppThemePalette
        @Composable get() = LocalAppThemePalette.current
}

@Composable
internal fun MapleTheme(palette: AppThemePalette, content: @Composable () -> Unit) {
    val roles = remember(palette) { MapleColorRoles.from(palette) }
    val colorScheme = remember(roles) { roles.toColorScheme() }
    val canvas = remember(roles) { roles.toCanvasTokens() }
    // Derive the direction from the resolved locale rather than the host View: an Activity recreated after an
    // in-app language change keeps the old View direction while its resources already speak the new language.
    val configuration = LocalConfiguration.current
    val layoutDirection = remember(configuration) {
        val locale = configuration.locales[0]
        if (TextUtils.getLayoutDirectionFromLocale(locale) == View.LAYOUT_DIRECTION_RTL) LayoutDirection.Rtl else LayoutDirection.Ltr
    }
    CompositionLocalProvider(
        LocalAppThemePalette provides palette,
        LocalMapleCanvasTokens provides canvas,
        LocalLayoutDirection provides layoutDirection,
    ) {
        MaterialTheme(colorScheme = colorScheme, typography = MapleTypography, content = content)
    }
}

/** Material 3 type roles from design draft section 5.3; sizes stay in sp so they follow the system font scale. */
internal val MapleTypography: Typography = Typography().let { defaults ->
    defaults.copy(
        titleLarge = defaults.titleLarge.copy(fontSize = 20.sp, lineHeight = 28.sp, fontWeight = FontWeight.Medium),
        titleMedium = defaults.titleMedium.copy(fontSize = 16.sp, lineHeight = 24.sp, fontWeight = FontWeight.Medium),
        bodyLarge = defaults.bodyLarge.copy(fontSize = 16.sp, lineHeight = 24.sp, fontWeight = FontWeight.Normal),
        bodyMedium = defaults.bodyMedium.copy(fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.Normal),
        bodySmall = defaults.bodySmall.copy(fontSize = 13.sp, lineHeight = 18.sp, fontWeight = FontWeight.Normal),
        labelLarge = defaults.labelLarge.copy(fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.Medium),
        labelMedium = defaults.labelMedium.copy(fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.Normal),
    )
}

private fun MapleColorRoles.toColorScheme(): ColorScheme {
    val scheme = if (isDark) darkColorScheme() else lightColorScheme()
    val surface = Color(surface)
    return scheme.copy(
        primary = Color(primary),
        onPrimary = Color(onPrimary),
        primaryContainer = Color(primaryContainer),
        onPrimaryContainer = Color(primaryText),
        secondary = Color(accent),
        onSecondary = Color(onAccent),
        secondaryContainer = Color(primaryContainer),
        onSecondaryContainer = Color(primaryText),
        tertiary = Color(accent),
        onTertiary = Color(onAccent),
        background = Color(windowBackground),
        onBackground = Color(primaryText),
        surface = surface,
        onSurface = Color(primaryText),
        surfaceVariant = Color(surfaceVariant),
        onSurfaceVariant = Color(secondaryText),
        surfaceContainerLowest = surface,
        surfaceContainerLow = surface,
        surfaceContainer = surface,
        surfaceContainerHigh = surface,
        surfaceContainerHighest = Color(surfaceVariant),
        surfaceTint = Color(primary),
        outline = Color(outline),
        outlineVariant = Color(divider),
        error = Color(error),
        onError = Color(onError),
        scrim = Color.Black,
    )
}

private fun MapleColorRoles.toCanvasTokens(): MapleCanvasTokens = MapleCanvasTokens(
    viewerCanvas = Color.Black,
    editorCanvas = Color(0xFF121212),
    scrim = Color.Black.copy(alpha = MapleColorRoles.SCRIM_ALPHA / 255f),
    scrimStrong = Color.Black.copy(alpha = MapleColorRoles.SCRIM_STRONG_ALPHA / 255f),
    onScrim = Color.White,
    onScrimMuted = Color.White.copy(alpha = MapleColorRoles.ON_SCRIM_MUTED_ALPHA / 255f),
    onScrimDisabled = Color.White.copy(alpha = MapleColorRoles.ON_SCRIM_DISABLED_ALPHA / 255f),
    accentOnScrim = Color(accentOnDark),
    errorOnScrim = Color(0xFFFFB4AB),
)
