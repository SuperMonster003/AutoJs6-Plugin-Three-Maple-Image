package io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.material3.LocalContentColor
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * Page top bar (design draft section 6). [overlay] renders the viewer / editor variant: translucent scrim with
 * white content drawn over the image; otherwise the bar uses the neutral surface.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MapleTopBar(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    navigationIcon: Painter? = null,
    navigationContentDescription: String? = null,
    onNavigationClick: (() -> Unit)? = null,
    overlay: Boolean = false,
    actions: @Composable RowScope.() -> Unit = {},
    /** Replaces the title and subtitle (a search field, ROADMAP 7.5). */
    titleContent: (@Composable () -> Unit)? = null,
) {
    val canvas = MapleTheme.canvas
    val colors = if (overlay) {
        TopAppBarDefaults.topAppBarColors(
            containerColor = canvas.scrim,
            scrolledContainerColor = canvas.scrim,
            navigationIconContentColor = canvas.onScrim,
            titleContentColor = canvas.onScrim,
            actionIconContentColor = canvas.onScrim,
        )
    } else {
        TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            scrolledContainerColor = MaterialTheme.colorScheme.surface,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
    TopAppBar(
        title = {
            if (titleContent != null) {
                titleContent()
                return@TopAppBar
            }
            Column {
                Text(title, style = MaterialTheme.typography.titleLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (!subtitle.isNullOrEmpty()) {
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (overlay) canvas.onScrimMuted else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        },
        modifier = modifier.testTag("maple.topBar"),
        navigationIcon = {
            if (navigationIcon != null && onNavigationClick != null) {
                IconButton(onClick = onNavigationClick, modifier = Modifier.testTag("maple.topBar.navigation")) {
                    Icon(painter = navigationIcon, contentDescription = navigationContentDescription)
                }
            }
        },
        actions = actions,
        colors = colors,
    )
}

/** One entry of [MapleBottomActionBar]: 24 dp icon plus a 12 sp label; the label doubles as the description. */
internal data class MapleAction(
    val icon: Painter,
    val label: String,
    val onClick: () -> Unit,
    val contentDescription: String = label,
    val enabled: Boolean = true,
    val testTag: String? = null,
)

/**
 * Bottom action bar with one to five equal-width icon buttons (design draft section 6). [overlay] draws the bar on
 * the viewer scrim and adds the navigation bar inset; otherwise the bar sits on the neutral surface.
 */
@Composable
internal fun MapleBottomActionBar(
    actions: List<MapleAction>,
    modifier: Modifier = Modifier,
    overlay: Boolean = false,
    /** Pads for the navigation bar; the overlay bar always does, a page bar does when it sits at the window bottom. */
    navigationBarInset: Boolean = overlay,
) {
    require(actions.size in 1..5) { "MapleBottomActionBar supports 1 to 5 actions" }
    val canvas = MapleTheme.canvas
    val background = if (overlay) canvas.scrim else MaterialTheme.colorScheme.surface
    val contentColor = if (overlay) canvas.onScrim else MaterialTheme.colorScheme.onSurfaceVariant
    val disabledColor = if (overlay) canvas.onScrimDisabled else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
    val insets = if (navigationBarInset) Modifier.windowInsetsPadding(WindowInsets.navigationBars) else Modifier
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(background)
            .then(insets)
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .testTag("maple.bottomBar"),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        actions.forEach { action ->
            val color = if (action.enabled) contentColor else disabledColor
            CompositionLocalProvider(LocalContentColor provides color) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = 56.dp)
                        .clickable(enabled = action.enabled, role = Role.Button, onClick = action.onClick)
                        .semantics(mergeDescendants = true) { contentDescription = action.contentDescription }
                        .then(if (action.testTag != null) Modifier.testTag(action.testTag) else Modifier)
                        .padding(vertical = 8.dp, horizontal = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(painter = action.icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = action.label,
                        style = MaterialTheme.typography.labelMedium,
                        color = color,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

/** Fixed scrim color for callers that compose their own overlay surfaces. */
internal val MapleOverlayDefaults.scrimColor: Color
    @Composable get() = MapleTheme.canvas.scrim

internal object MapleOverlayDefaults
