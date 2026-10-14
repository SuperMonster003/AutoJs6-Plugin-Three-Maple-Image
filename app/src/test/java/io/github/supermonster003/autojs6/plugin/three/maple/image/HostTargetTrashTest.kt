package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.os.Bundle
import android.os.RemoteException
import org.autojs.plugin.explorer.api.ExplorerActionHostSessionKeys
import org.autojs.plugin.explorer.api.ExplorerActionHostSessionValues
import org.autojs.plugin.explorer.api.IExplorerActionHostSession
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.lang.reflect.Proxy

/** ROADMAP 7.2: one viewer page goes to the host Trash per call, and a lost answer is asked for with the same request. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class HostTargetTrashTest {

    private val batchId = "8a0e6a3c-3c5e-4d2f-9a7b-1f2e3d4c5b6a"

    @Test fun aCommittedMoveOfOneTargetAnswersItsBatch() {
        val session = FakeSession(moves = mutableListOf({ committed(1) }))
        assertEquals(batchId, HostTargetTrash.move(session.proxy, "target-1"))
        assertEquals(listOf(listOf("target-1") to emptyList<String>()), session.moveCalls)
    }

    @Test fun aLostAnswerRepeatsTheSameRequestOnce() {
        val session = FakeSession(moves = mutableListOf({ throw RemoteException("lost") }, { committed(1) }))
        assertEquals(batchId, HostTargetTrash.move(session.proxy, "target-1"))
        assertEquals(List(2) { listOf("target-1") to emptyList<String>() }, session.moveCalls)
    }

    @Test fun twoLostAnswersMoveNothing() {
        val session = FakeSession(moves = mutableListOf({ throw RemoteException("lost") }, { throw IllegalStateException("running") }))
        assertNull(HostTargetTrash.move(session.proxy, "target-1"))
        assertEquals(2, session.moveCalls.size)
    }

    @Test fun aRefusalIsNotRetried() {
        val failed = Bundle().apply {
            putInt(ExplorerActionHostSessionKeys.TARGET_TRASH_STATE, ExplorerActionHostSessionValues.TARGET_TRASH_STATE_FAILED)
        }
        val session = FakeSession(moves = mutableListOf({ failed }, { committed(1) }))
        assertNull(HostTargetTrash.move(session.proxy, "target-1"))
        assertEquals(1, session.moveCalls.size)
    }

    @Test fun onlyAMoveOfExactlyOneTargetWithABatchCounts() {
        assertNull(HostTargetTrash.movedBatch(committed(0)))
        assertNull(HostTargetTrash.movedBatch(committed(2)))
        assertNull(HostTargetTrash.movedBatch(committed(1).apply { remove(ExplorerActionHostSessionKeys.TARGET_TRASH_BATCH_ID) }))
    }

    @Test fun undoAnswersWhetherTheBatchIsBack() {
        fun undo(state: Int?): Boolean {
            val session = FakeSession(undo = {
                Bundle().apply { state?.let { putInt(ExplorerActionHostSessionKeys.TARGET_TRASH_BATCH_STATE, it) } }
            })
            return HostTargetTrash.undo(session.proxy, batchId)
        }
        assertTrue(undo(ExplorerActionHostSessionValues.TARGET_TRASH_BATCH_STATE_RESTORED))
        assertFalse(undo(null))
        assertFalse(HostTargetTrash.undo(FakeSession(undo = { throw RemoteException("gone") }).proxy, batchId))
    }

    private fun committed(movedCount: Int) = Bundle().apply {
        putInt(ExplorerActionHostSessionKeys.TARGET_TRASH_STATE, ExplorerActionHostSessionValues.TARGET_TRASH_STATE_COMMITTED)
        putInt(ExplorerActionHostSessionKeys.TARGET_TRASH_MOVED_COUNT, movedCount)
        putString(ExplorerActionHostSessionKeys.TARGET_TRASH_BATCH_ID, batchId)
    }

    /** Answers `moveTargetsToTrash` from [moves] in turn and `undoTargetTrashBatch` from [undo]; nothing else. */
    private class FakeSession(
        private val moves: MutableList<() -> Bundle> = mutableListOf(),
        private val undo: () -> Bundle = { throw UnsupportedOperationException() },
    ) {
        val moveCalls = mutableListOf<Pair<List<String>, List<String>>>()

        val proxy: IExplorerActionHostSession = Proxy.newProxyInstance(
            IExplorerActionHostSession::class.java.classLoader,
            arrayOf(IExplorerActionHostSession::class.java),
        ) { _, method, args ->
            val answer: () -> Bundle = when (method.name) {
                "moveTargetsToTrash" -> {
                    @Suppress("UNCHECKED_CAST")
                    moveCalls += (args[0] as List<String>).toList() to (args[1] as List<String>).toList()
                    moves.removeAt(0)
                }
                "undoTargetTrashBatch" -> undo
                else -> throw UnsupportedOperationException(method.name)
            }
            answer()
        } as IExplorerActionHostSession
    }
}
