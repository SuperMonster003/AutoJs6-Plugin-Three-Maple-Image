package io.github.supermonster003.autojs6.plugin.imagetools

import android.app.Application
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

class SingleUseOutputCommitGuardTest {

    @Test
    fun onlyOneConcurrentCallerCanStart() {
        val guard = SingleUseOutputCommitGuard()
        val ready = CountDownLatch(WORKER_COUNT)
        val start = CountDownLatch(1)
        val successes = AtomicInteger()
        val executor = Executors.newFixedThreadPool(WORKER_COUNT)
        try {
            repeat(WORKER_COUNT) {
                executor.execute {
                    ready.countDown()
                    start.await()
                    if (guard.tryStart()) successes.incrementAndGet()
                }
            }
            assertTrue(ready.await(5, TimeUnit.SECONDS))
            start.countDown()
            executor.shutdown()
            assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS))
            assertEquals(1, successes.get())
            assertEquals(SingleUseOutputCommitGuard.State.RUNNING, guard.currentState)
        } finally {
            executor.shutdownNow()
        }
    }

    @Test
    fun terminalStateCannotBeReopened() {
        val guard = SingleUseOutputCommitGuard()
        assertTrue(guard.tryStart())
        assertTrue(guard.succeed())
        assertFalse(guard.tryStart())
        assertFalse(guard.fail())
        assertEquals(SingleUseOutputCommitGuard.State.SUCCEEDED, guard.currentState)
    }

    private companion object {
        const val WORKER_COUNT = 16
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class OutputTransactionLedgerTest {

    private lateinit var application: Application

    @Before
    fun setUp() {
        application = RuntimeEnvironment.getApplication()
        preferences().edit().clear().commit()
    }

    @After
    fun tearDown() {
        preferences().edit().clear().commit()
    }

    @Test
    fun claimSurvivesCoordinatorRecreation() {
        val transactionId = "b85c76c4-729c-4fd0-9766-ee1e9be20a23"
        assertTrue(OutputTransactionLedger(application).tryClaim(transactionId, claimedAt = 1L))
        assertFalse(OutputTransactionLedger(application).tryClaim(transactionId, claimedAt = 2L))
        assertTrue(OutputTransactionLedger(application).isClaimed(transactionId))
    }

    private fun preferences() = application.getSharedPreferences(
        "image_tools_output_transactions",
        Application.MODE_PRIVATE,
    )
}
