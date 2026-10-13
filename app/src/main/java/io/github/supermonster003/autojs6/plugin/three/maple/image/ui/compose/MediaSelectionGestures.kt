package io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.round

/** The key of the grid item under [position] (grid coordinates), or null over a gap or outside the items. */
internal fun LazyGridState.keyAt(position: Offset): Any? {
    val point = position.round()
    return layoutInfo.visibleItemsInfo.firstOrNull { IntRect(it.offset, it.size).contains(point) }?.key
}

/**
 * Long press on a grid item, then drag to extend the selection over a range (ROADMAP 7.1). The gesture is watched
 * in the initial pass without consuming anything until the long press fires, so taps still reach the cells and a
 * quick drag still scrolls the grid; from the long press on every change is consumed, which keeps the grid still
 * and stops the cell from treating the release as a tap. [onAutoScroll] receives a speed in pixels per frame
 * (negative = up) while the finger rests near the top or bottom edge, and 0 when it leaves the edge or lifts.
 */
internal fun Modifier.longPressDragSelection(
    gridState: LazyGridState,
    edgePx: Float,
    maxSpeedPx: Float,
    onStart: (key: Any) -> Unit,
    onDrag: (key: Any) -> Unit,
    onPointer: (Offset) -> Unit,
    onAutoScroll: (Float) -> Unit,
    onEnd: () -> Unit,
): Modifier = pointerInput(gridState) {
    val slop = viewConfiguration.touchSlop
    val timeout = viewConfiguration.longPressTimeoutMillis
    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
        val startKey = gridState.keyAt(down.position) ?: return@awaitEachGesture
        // Null when the timeout elapsed with one finger resting on the item: that is the long press.
        val ended = withTimeoutOrNull(timeout) {
            var cancelled = false
            while (!cancelled) {
                val event = awaitPointerEvent(PointerEventPass.Initial)
                val change = event.changes.firstOrNull { it.id == down.id }
                cancelled = change == null || !change.pressed || change.isConsumed ||
                    event.changes.count { it.pressed } > 1 ||
                    (change.position - down.position).getDistance() > slop
            }
            true
        }
        if (ended != null) return@awaitEachGesture
        onStart(startKey)
        var lastKey: Any = startKey
        try {
            while (true) {
                val event = awaitPointerEvent(PointerEventPass.Initial)
                val change = event.changes.firstOrNull { it.id == down.id } ?: break
                event.changes.forEach { it.consume() }
                if (!change.pressed) break
                onPointer(change.position)
                gridState.keyAt(change.position)?.let { key ->
                    if (key != lastKey) {
                        lastKey = key
                        onDrag(key)
                    }
                }
                onAutoScroll(autoScrollSpeed(change.position.y, size.height.toFloat(), edgePx, maxSpeedPx))
            }
        } finally {
            onAutoScroll(0f)
            onEnd()
        }
    }
}

/** Linear ramp inside the top / bottom [edgePx] band, up to [maxSpeedPx] per frame at the very edge. */
internal fun autoScrollSpeed(y: Float, height: Float, edgePx: Float, maxSpeedPx: Float): Float = when {
    edgePx <= 0f || height <= 0f -> 0f
    y < edgePx -> -maxSpeedPx * ((edgePx - y) / edgePx).coerceAtMost(1f)
    y > height - edgePx -> maxSpeedPx * ((y - (height - edgePx)) / edgePx).coerceAtMost(1f)
    else -> 0f
}
