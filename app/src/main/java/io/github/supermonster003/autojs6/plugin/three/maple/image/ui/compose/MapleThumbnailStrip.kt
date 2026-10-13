package io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage

internal object MapleThumbnailStripDefaults {
    val size = 48.dp
    val gap = 4.dp
    val horizontalPadding = 8.dp
    val verticalPadding = 6.dp
}

/**
 * Horizontal strip of page thumbnails for the viewer (design draft 7.3, 3.0.0 reservation): 48 dp squares on the
 * scrim, the current page outlined in the accent color and kept centered, a tap jumps to that page. Thumbnails
 * load through Glide at strip size, so MediaStore pages use the provider thumbnails.
 */
@OptIn(ExperimentalGlideComposeApi::class)
@Composable
internal fun MapleThumbnailStrip(
    count: Int,
    current: Int,
    key: (Int) -> Any,
    model: (Int) -> Any?,
    description: (Int) -> String,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val canvas = MapleTheme.canvas
    val listState = rememberLazyListState()
    val density = LocalDensity.current
    var widthPx by remember { mutableIntStateOf(0) }
    var centeredOnce by remember { mutableStateOf(false) }
    LaunchedEffect(widthPx, current, count) {
        if (widthPx <= 0 || current !in 0 until count) return@LaunchedEffect
        val itemPx = with(density) { MapleThumbnailStripDefaults.size.roundToPx() }
        val offset = -((widthPx - itemPx) / 2).coerceAtLeast(0)
        if (centeredOnce) {
            listState.animateScrollToItem(current, offset)
        } else {
            listState.scrollToItem(current, offset)
        }
        centeredOnce = true
    }
    val shape = RoundedCornerShape(4.dp)
    LazyRow(
        state = listState,
        modifier = modifier
            .fillMaxWidth()
            .background(canvas.scrim)
            .onSizeChanged { widthPx = it.width }
            .padding(vertical = MapleThumbnailStripDefaults.verticalPadding)
            .testTag("maple.viewer.strip"),
        contentPadding = PaddingValues(horizontal = MapleThumbnailStripDefaults.horizontalPadding),
        horizontalArrangement = Arrangement.spacedBy(MapleThumbnailStripDefaults.gap),
    ) {
        items(count = count, key = key) { index ->
            val isCurrent = index == current
            val label = description(index)
            Box(
                modifier = Modifier
                    .size(MapleThumbnailStripDefaults.size)
                    .clip(shape)
                    .background(canvas.scrimStrong)
                    .then(if (isCurrent) Modifier.border(2.dp, canvas.accentOnScrim, shape) else Modifier)
                    .alpha(if (isCurrent) 1f else 0.72f)
                    .clickable(role = Role.Button) { onSelect(index) }
                    .semantics {
                        contentDescription = label
                        selected = isCurrent
                    }
                    .testTag("maple.viewer.strip.$index"),
            ) {
                GlideImage(
                    model = model(index),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}
