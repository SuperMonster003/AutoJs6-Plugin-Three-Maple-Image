package io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onLongClick
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import io.github.supermonster003.autojs6.plugin.three.maple.image.R

internal object MapleGridDefaults {
    val albumCornerRadius = 4.dp
    val albumColumnSpacing = 12.dp
    val albumRowSpacing = 16.dp
    val mediaGap = 2.dp
    const val MEDIA_COLUMNS = 4

    /** Album columns by window width: 2 on phones, 3 on wide phones / small tablets, 4 on large tablets (design draft 8.1). */
    fun albumColumns(widthDp: Int): Int = when {
        widthDp >= 840 -> 4
        widthDp >= 600 -> 3
        else -> 2
    }
}

/** Short type badge for formats worth calling out in a grid (design draft 8.2); null for plain JPEG / PNG / BMP. */
internal fun mediaTypeBadge(mimeType: String): String? = when (mimeType.lowercase()) {
    "image/gif" -> "GIF"
    "image/heic", "image/heif" -> "HEIC"
    "image/avif" -> "AVIF"
    "image/webp" -> "WEBP"
    else -> null
}

/**
 * Album cell: square cover with 4 dp corners, optional corner badge, name (16 sp) and count (14 sp). In selection
 * mode (ROADMAP 7.4) a mark at the top start shows the state (the badge moves to the top end) and a selected cover
 * shrinks behind a tinted frame; [onLongClick] starts selecting.
 */
@OptIn(ExperimentalGlideComposeApi::class)
@Composable
internal fun MapleAlbumCell(
    name: String,
    count: String,
    coverModel: Any?,
    modifier: Modifier = Modifier,
    badge: Painter? = null,
    enabled: Boolean = true,
    selectionMode: Boolean = false,
    selected: Boolean = false,
    onLongClick: (() -> Unit)? = null,
    onLongClickLabel: String? = null,
    onClick: () -> Unit,
) {
    val canvas = MapleTheme.canvas
    val inset = if (selectionMode) animateDpAsState(if (selected) SELECTED_INSET else 0.dp, label = "albumSelectionInset") else null
    Column(
        modifier = modifier
            .combinedClickable(
                enabled = enabled,
                role = if (selectionMode) Role.Checkbox else Role.Button,
                onLongClickLabel = onLongClickLabel,
                onLongClick = onLongClick,
                onClick = onClick,
            )
            .semantics(mergeDescendants = true) {
                contentDescription = "$name, $count"
                if (selectionMode) this.selected = selected
            },
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(MapleGridDefaults.albumCornerRadius))
                .background(if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant),
        ) {
            if (coverModel != null) {
                GlideImage(
                    model = coverModel,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().then(
                        if (inset == null) Modifier else Modifier.graphicsLayer {
                            val scale = if (size.width > 0f) 1f - 2f * inset.value.toPx() / size.width else 1f
                            scaleX = scale
                            scaleY = scale
                            if (selected) {
                                shape = RoundedCornerShape(SELECTED_CORNER)
                                clip = true
                            }
                        },
                    ),
                )
            } else {
                Icon(
                    painter = painterResource(R.drawable.ic_image_24),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(32.dp).align(Alignment.Center),
                )
            }
            if (badge != null) {
                Box(
                    modifier = Modifier
                        .align(if (selectionMode) Alignment.TopEnd else Alignment.TopStart)
                        .padding(6.dp)
                        .size(24.dp)
                        .background(canvas.scrimStrong, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(painter = badge, contentDescription = null, tint = canvas.onScrim, modifier = Modifier.size(16.dp))
                }
            }
            if (selectionMode) {
                Icon(
                    painter = painterResource(if (selected) R.drawable.ic_check_circle_24 else R.drawable.ic_radio_button_unchecked_24),
                    contentDescription = null,
                    tint = if (selected) MaterialTheme.colorScheme.primary else canvas.onScrim,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp)
                        .size(24.dp)
                        .background(if (selected) MaterialTheme.colorScheme.onPrimary else canvas.scrim, CircleShape),
                )
            }
        }
        Text(
            text = name,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.disabledIf(!enabled),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 8.dp),
        )
        Text(
            text = count,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.disabledIf(!enabled),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/**
 * Media cell: square thumbnail with an optional type badge at the top end (design draft 8.2). In selection mode
 * (ROADMAP 7.1) a mark at the top start shows the state and a selected thumbnail shrinks behind a tinted frame;
 * [onLongClick] is the accessibility action that starts selecting (touch long presses come from the grid).
 */
@OptIn(ExperimentalGlideComposeApi::class)
@Composable
internal fun MapleMediaCell(
    model: Any?,
    description: String,
    modifier: Modifier = Modifier,
    badge: String? = null,
    selectionMode: Boolean = false,
    selected: Boolean = false,
    onLongClick: (() -> Unit)? = null,
    onLongClickLabel: String? = null,
    onClick: () -> Unit,
) {
    val canvas = MapleTheme.canvas
    // Outside selection mode the cell stays free of animation state, which keeps flinging a large album cheap. The
    // inset is read only while drawing (a layer scale), so animating it never recomposes or re-lays out the cells:
    // selecting all of a full screen stays one recomposition.
    val inset = if (selectionMode) animateDpAsState(if (selected) SELECTED_INSET else 0.dp, label = "selectionInset") else null
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .background(if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
            .clickable(role = if (selectionMode) Role.Checkbox else Role.Button, onClick = onClick)
            .semantics {
                contentDescription = description
                if (selectionMode) this.selected = selected
                if (onLongClick != null) onLongClick(label = onLongClickLabel) { onLongClick(); true }
            },
    ) {
        GlideImage(
            model = model,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize().then(
                if (inset == null) Modifier else Modifier.graphicsLayer {
                    val scale = if (size.width > 0f) 1f - 2f * inset.value.toPx() / size.width else 1f
                    scaleX = scale
                    scaleY = scale
                    if (selected) {
                        shape = RoundedCornerShape(SELECTED_CORNER)
                        clip = true
                    }
                },
            ),
        )
        if (selectionMode) {
            Icon(
                painter = painterResource(if (selected) R.drawable.ic_check_circle_24 else R.drawable.ic_radio_button_unchecked_24),
                contentDescription = null,
                tint = if (selected) MaterialTheme.colorScheme.primary else canvas.onScrim,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(6.dp)
                    .size(24.dp)
                    .background(if (selected) MaterialTheme.colorScheme.onPrimary else canvas.scrim, CircleShape),
            )
        }
        if (badge != null) {
            Text(
                text = badge,
                style = MaterialTheme.typography.labelMedium,
                color = canvas.onScrim,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .background(canvas.scrimStrong, RoundedCornerShape(4.dp))
                    .padding(horizontal = 4.dp, vertical = 1.dp),
            )
        }
    }
}

private val SELECTED_INSET = 10.dp
private val SELECTED_CORNER = 8.dp

/** One-line permission status with an action at the end (design draft 8.1, `MaplePermissionBanner`). */
@Composable
internal fun MaplePermissionBanner(text: String, actionLabel: String, onAction: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(start = MapleSettingsDefaults.horizontalPadding, end = 12.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_warning_24),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.width(12.dp))
        Text(text = text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
        TextButton(onClick = onAction) { Text(actionLabel) }
    }
}
