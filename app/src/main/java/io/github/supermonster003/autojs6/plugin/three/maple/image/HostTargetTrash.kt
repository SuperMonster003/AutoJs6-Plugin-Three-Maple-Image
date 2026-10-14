package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.os.Bundle
import org.autojs.plugin.explorer.api.ExplorerActionHostSessionKeys
import org.autojs.plugin.explorer.api.ExplorerActionHostSessionValues
import org.autojs.plugin.explorer.api.ExplorerActionProtocol
import org.autojs.plugin.explorer.api.IExplorerActionHostSession

/**
 * Deleting from a viewer opened by the file manager (ROADMAP 7.2, protocol v24): one request target at a time goes to
 * the host Trash, recorded as its own v21 batch that the snackbar can undo. Siblings and directory pages are never
 * request targets and are never passed here.
 */
internal object HostTargetTrash {

    /**
     * Moves [targetId] to the host Trash; the batch ID that undoes it, or null when nothing moved. A lost answer is
     * asked for once more with the same request: the host answers a repeated request with its first result instead of
     * moving twice. (`queryTargetTrash` would not do: it answers with the session's last move, maybe another page's.)
     */
    fun move(session: IExplorerActionHostSession, targetId: String): String? {
        repeat(2) {
            runCatching { session.moveTargetsToTrash(listOf(targetId), emptyList()) }
                .onSuccess { result -> return movedBatch(result) }
        }
        return null
    }

    /** Puts the batch back; true when the image is at its place again. */
    fun undo(session: IExplorerActionHostSession, batchId: String): Boolean {
        val result = runCatching { session.undoTargetTrashBatch(batchId) }.getOrNull() ?: return false
        return result.getInt(ExplorerActionHostSessionKeys.TARGET_TRASH_BATCH_STATE) ==
            ExplorerActionHostSessionValues.TARGET_TRASH_BATCH_STATE_RESTORED
    }

    internal fun movedBatch(result: Bundle): String? {
        if (result.getInt(ExplorerActionHostSessionKeys.TARGET_TRASH_STATE) != ExplorerActionHostSessionValues.TARGET_TRASH_STATE_COMMITTED) return null
        if (result.getInt(ExplorerActionHostSessionKeys.TARGET_TRASH_MOVED_COUNT) != 1) return null
        return result.getString(ExplorerActionHostSessionKeys.TARGET_TRASH_BATCH_ID)
            ?.takeIf { it.length in 1..ExplorerActionProtocol.MAX_TARGET_TRASH_BATCH_ID_LENGTH }
    }
}
