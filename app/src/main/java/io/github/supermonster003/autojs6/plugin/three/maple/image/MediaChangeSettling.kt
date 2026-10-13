package io.github.supermonster003.autojs6.plugin.three.maple.image

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Collapses bursts of change signals (ROADMAP 6.5): emits once the source has been quiet for [quietMillis], and at
 * least every [maxWaitMillis] while signals keep arriving. A bulk import or a media rescan notifies for minutes; the
 * gallery then rescans a few times instead of after every notification, and still shows progress meanwhile. A burst
 * that is still pending when the source completes is emitted before completing.
 */
internal fun Flow<Unit>.settle(quietMillis: Long, maxWaitMillis: Long): Flow<Unit> = channelFlow {
    val signals = Channel<Unit>(Channel.CONFLATED)
    launch {
        collect { signals.send(Unit) }
        signals.close()
    }
    while (signals.receiveCatching().isSuccess) {
        val burstStarted = System.nanoTime()
        var open = true
        while (open) {
            val left = maxWaitMillis - (System.nanoTime() - burstStarted) / 1_000_000
            if (left <= 0) break
            val next = withTimeoutOrNull(minOf(quietMillis, left)) { signals.receiveCatching() } ?: break
            open = next.isSuccess
        }
        send(Unit)
        if (!open) break
    }
}
