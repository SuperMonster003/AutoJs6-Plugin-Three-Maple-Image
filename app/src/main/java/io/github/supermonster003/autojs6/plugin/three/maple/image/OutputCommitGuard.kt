package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.content.Context
import java.util.concurrent.atomic.AtomicReference

internal class SingleUseOutputCommitGuard {

    enum class State {
        IDLE,
        RUNNING,
        SUCCEEDED,
        FAILED,
    }

    private val state = AtomicReference(State.IDLE)

    val currentState: State
        get() = state.get()

    fun tryStart(): Boolean = state.compareAndSet(State.IDLE, State.RUNNING)

    fun succeed(): Boolean = state.compareAndSet(State.RUNNING, State.SUCCEEDED)

    fun fail(): Boolean = state.compareAndSet(State.RUNNING, State.FAILED)
}

internal class OutputTransactionLedger(context: Context) {

    private val preferences = context.applicationContext.getSharedPreferences(
        PREFERENCES_NAME,
        Context.MODE_PRIVATE,
    )

    fun isClaimed(transactionId: String): Boolean = synchronized(LOCK) {
        preferences.contains(key(transactionId))
    }

    fun tryClaim(transactionId: String, claimedAt: Long = System.currentTimeMillis()): Boolean = synchronized(LOCK) {
        val transactionKey = key(transactionId)
        if (preferences.contains(transactionKey)) return false
        preferences.edit().putLong(transactionKey, claimedAt).commit()
    }

    private fun key(transactionId: String) = "$KEY_PREFIX$transactionId"

    private companion object {
        const val PREFERENCES_NAME = "image_tools_output_transactions"
        const val KEY_PREFIX = "claimed:"
        val LOCK = Any()
    }
}
