package io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

internal object MapleFastScrollerDefaults {
    val trackWidth = 28.dp
    val thumbWidth = 6.dp
    val thumbHeight = 48.dp
    const val HIDE_DELAY_MILLIS = 1_200L
}

/**
 * Fast scroller for a lazy grid (design draft 8.2): a thumb on the end edge appears while the grid scrolls or the
 * thumb is dragged, dragging jumps the grid, and a bubble next to the thumb shows [label] of the first visible
 * item (the date in the media grid). Hidden from accessibility: the grid itself remains fully reachable.
 */
@Composable
internal fun MapleFastScroller(
    gridState: LazyGridState,
    itemCount: Int,
    label: (Int) -> String?,
    modifier: Modifier = Modifier,
) {
    if (itemCount <= 0) return
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    var dragging by remember { mutableStateOf(false) }
    var visible by remember { mutableStateOf(false) }
    var trackHeightPx by remember { mutableIntStateOf(0) }
    val scrolling = gridState.isScrollInProgress
    LaunchedEffect(scrolling, dragging) {
        if (scrolling || dragging) {
            visible = true
        } else {
            delay(MapleFastScrollerDefaults.HIDE_DELAY_MILLIS)
            visible = false
        }
    }
    // The scroll position is read in the placement phase (thumb offset) or behind derived state, so scrolling
    // moves the thumb without recomposing the scroller on every frame.
    val canScroll by remember(gridState, itemCount) {
        derivedStateOf {
            val visibleCount = gridState.layoutInfo.visibleItemsInfo.size
            itemCount > visibleCount && visibleCount > 0
        }
    }
    val thumbHeightPx = with(density) { MapleFastScrollerDefaults.thumbHeight.roundToPx() }
    val travelPx = (trackHeightPx - thumbHeightPx).coerceAtLeast(0)
    val thumbOffsetPx: () -> Int = {
        val visibleCount = gridState.layoutInfo.visibleItemsInfo.size
        val scrollableItems = (itemCount - visibleCount).coerceAtLeast(1)
        val fraction = (gridState.firstVisibleItemIndex.toFloat() / scrollableItems).coerceIn(0f, 1f)
        (travelPx * fraction).roundToInt()
    }

    fun jumpTo(y: Float) {
        if (travelPx <= 0) return
        val target = ((y - thumbHeightPx / 2f) / travelPx).coerceIn(0f, 1f)
        val index = (target * (itemCount - 1)).roundToInt()
        scope.launch { gridState.scrollToItem(index) }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { trackHeightPx = it.height }
            .semantics { invisibleToUser() },
    ) {
        AnimatedVisibility(
            visible = canScroll && visible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopEnd),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(MapleFastScrollerDefaults.trackWidth)
                    .pointerInput(itemCount, travelPx) {
                        detectVerticalDragGestures(
                            onDragStart = { offset ->
                                dragging = true
                                jumpTo(offset.y)
                            },
                            onDragEnd = { dragging = false },
                            onDragCancel = { dragging = false },
                            onVerticalDrag = { change, _ ->
                                change.consume()
                                jumpTo(change.position.y)
                            },
                        )
                    }
                    .testTag("maple.fastScroller"),
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset { IntOffset(0, thumbOffsetPx()) }
                        .padding(end = 4.dp)
                        .size(MapleFastScrollerDefaults.thumbWidth, MapleFastScrollerDefaults.thumbHeight)
                        .background(
                            if (dragging) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                            RoundedCornerShape(50),
                        ),
                )
            }
        }
        val bubble = if (dragging) label(gridState.firstVisibleItemIndex) else null
        if (bubble != null) {
            Text(
                text = bubble,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset { IntOffset(0, thumbOffsetPx()) }
                    .padding(end = MapleFastScrollerDefaults.trackWidth + 4.dp)
                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .testTag("maple.fastScroller.bubble"),
            )
        }
    }
}
