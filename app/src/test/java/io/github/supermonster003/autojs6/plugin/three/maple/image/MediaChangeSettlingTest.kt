package io.github.supermonster003.autojs6.plugin.three.maple.image

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** ROADMAP 6.5: MediaStore change bursts settle into few gallery rescans (real time, generous margins). */
class MediaChangeSettlingTest {

    /** Signals after each gap, then stays open for [openAfterMillis] like a live observer before completing. */
    private fun signals(vararg gapsMillis: Long, openAfterMillis: Long = 0): Flow<Unit> = flow {
        for (gap in gapsMillis) {
            delay(gap)
            emit(Unit)
        }
        delay(openAfterMillis)
    }

    @Test
    fun aBurstSettlesIntoOneEmissionAfterTheQuietPeriod() = runBlocking {
        val started = System.nanoTime()
        val emittedAt = signals(0, 10, 10, 10, 10, openAfterMillis = 800)
            .settle(quietMillis = 150, maxWaitMillis = 5_000)
            .map { (System.nanoTime() - started) / 1_000_000 }
            .toList()
        assertEquals(1, emittedAt.size)
        assertTrue("waits for the quiet period after the last signal: ${emittedAt.single()} ms", emittedAt.single() in (40L + 150)..700L)
    }

    @Test
    fun separateBurstsEmitSeparately() = runBlocking {
        val emissions = signals(0, 10, 600, 10).settle(quietMillis = 150, maxWaitMillis = 5_000).toList()
        assertEquals(2, emissions.size)
    }

    @Test
    fun continuousSignalsStillEmitAtTheMaximumWait() = runBlocking {
        // A signal every 30 ms for about 1.5 s never leaves a quiet period of 150 ms.
        val emissions = signals(*LongArray(50) { 30L }).settle(quietMillis = 150, maxWaitMillis = 400).toList()
        assertTrue("emits a few times during a long burst: ${emissions.size}", emissions.size in 2..6)
    }

    @Test
    fun aPendingBurstIsEmittedWhenTheSourceCompletes() = runBlocking {
        val emissions = signals(0).settle(quietMillis = 10_000, maxWaitMillis = 60_000).toList()
        assertEquals(1, emissions.size)
    }

    @Test
    fun noSignalsNoEmissions() = runBlocking {
        assertEquals(0, signals().settle(quietMillis = 50, maxWaitMillis = 100).toList().size)
    }
}
