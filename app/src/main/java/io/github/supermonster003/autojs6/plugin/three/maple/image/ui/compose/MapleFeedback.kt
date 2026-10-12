package io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/** Button of [MapleEmptyState]; the first one renders filled, the rest as text buttons. */
internal data class MapleEmptyAction(val label: String, val onClick: () -> Unit, val testTag: String? = null)

/**
 * Empty, error and no-permission state (design draft section 6): 48 dp icon, 16 sp title, 14 sp body, one primary
 * and up to two secondary actions. [dark] switches to the overlay colors for the viewer canvas.
 */
@Composable
internal fun MapleEmptyState(
    title: String,
    modifier: Modifier = Modifier,
    body: String? = null,
    icon: Painter? = null,
    primaryAction: MapleEmptyAction? = null,
    secondaryActions: List<MapleEmptyAction> = emptyList(),
    dark: Boolean = false,
) {
    val canvas = MapleTheme.canvas
    val titleColor = if (dark) canvas.onScrim else MaterialTheme.colorScheme.onSurface
    val bodyColor = if (dark) canvas.onScrimMuted else MaterialTheme.colorScheme.onSurfaceVariant
    Column(
        modifier = modifier.padding(horizontal = 24.dp, vertical = 32.dp).testTag("maple.emptyState"),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (icon != null) {
            Icon(painter = icon, contentDescription = null, tint = bodyColor, modifier = Modifier.size(48.dp))
            Spacer(Modifier.height(16.dp))
        }
        Text(title, style = MaterialTheme.typography.bodyLarge, color = titleColor, textAlign = TextAlign.Center)
        if (!body.isNullOrEmpty()) {
            Spacer(Modifier.height(8.dp))
            Text(body, style = MaterialTheme.typography.bodyMedium, color = bodyColor, textAlign = TextAlign.Center)
        }
        if (primaryAction != null) {
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = primaryAction.onClick,
                modifier = Modifier.defaultMinSize(minHeight = 48.dp).then(primaryAction.testTag?.let { Modifier.testTag(it) } ?: Modifier),
            ) { Text(primaryAction.label) }
        }
        if (secondaryActions.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Row {
                secondaryActions.forEachIndexed { index, action ->
                    if (index > 0) Spacer(Modifier.width(8.dp))
                    TextButton(
                        onClick = action.onClick,
                        modifier = Modifier.defaultMinSize(minHeight = 48.dp).then(action.testTag?.let { Modifier.testTag(it) } ?: Modifier),
                    ) { Text(action.label) }
                }
            }
        }
    }
}

/** Centered indeterminate progress for loading states. */
@Composable
internal fun MapleLoading(modifier: Modifier = Modifier, dark: Boolean = false) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            color = if (dark) MapleTheme.canvas.onScrim else MaterialTheme.colorScheme.primary,
            modifier = Modifier.testTag("maple.loading"),
        )
    }
}

/** Zoom factor badge of the viewer: strong scrim, bold white text, hidden from accessibility (design draft 5.4). */
@Composable
internal fun MapleZoomIndicator(text: String, visible: Boolean, modifier: Modifier = Modifier) {
    AnimatedVisibility(visible = visible, enter = fadeIn(), exit = fadeOut(), modifier = modifier) {
        Box(
            modifier = Modifier
                .defaultMinSize(minWidth = 56.dp, minHeight = 32.dp)
                .background(MapleTheme.canvas.scrimStrong, RoundedCornerShape(16.dp))
                .padding(horizontal = 12.dp)
                .semantics { invisibleToUser() }
                .testTag("maple.zoomIndicator"),
            contentAlignment = Alignment.Center,
        ) {
            Text(text, color = Color.White, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
        }
    }
}
