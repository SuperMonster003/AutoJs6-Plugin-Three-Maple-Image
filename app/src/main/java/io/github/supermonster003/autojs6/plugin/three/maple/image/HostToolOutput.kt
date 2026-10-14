package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.graphics.BitmapFactory
import android.os.Binder
import android.os.ParcelFileDescriptor
import org.autojs.plugin.explorer.api.ExplorerActionHostSessionKeys
import org.autojs.plugin.explorer.api.ExplorerActionHostSessionValues
import org.autojs.plugin.explorer.api.ExplorerActionProtocol
import org.autojs.plugin.explorer.api.IExplorerActionHostSession
import java.io.FileInputStream
import java.io.IOException

/**
 * Output of the editor and the converter through the host session of a "manage target" action (ROADMAP 8.2, T11):
 * a new file next to the target (`prepareOutput`, renamed by the host when the name is taken) or the target itself
 * replaced in place (`prepareTargetReplacement`, protocol v8 / v9). Either way the host stages the file, the plugin
 * reads it back before [publish] commits it, and anything that fails is aborted, so the folder never shows a broken
 * image. A replacement can be undone once through the host's history (v18).
 */
internal class HostToolOutput(val session: IExplorerActionHostSession, val targetId: String) {

    private val clientToken = Binder()

    @Volatile
    private var attached = false

    /** [replacementHistoryId]: set when the target was replaced and the host can undo it. */
    class Published(val replacementHistoryId: String?)

    /**
     * Writes one image: [write] fills the descriptor it opens, and the pending file must read back as a
     * [expectedSize] image of [mimeType] before it is published.
     */
    fun publish(
        displayName: String,
        mimeType: String,
        expectedSize: ImagePixelSize,
        overwrite: Boolean,
        write: (open: () -> ParcelFileDescriptor) -> Unit,
    ): Published {
        if (!attached) {
            // Ties the staged output to this process: the host rolls it back if the plugin dies before publishing.
            session.attachClient(clientToken)
            attached = true
        }
        val prepared = if (overwrite) {
            session.prepareTargetReplacement(targetId)
        } else {
            session.prepareOutput(displayName, mimeType, ExplorerActionHostSessionValues.OUTPUT_CONFLICT_AUTO_RENAME)
        } ?: throw IOException("The host reserved no output")
        val id = prepared.getString(ExplorerActionHostSessionKeys.OUTPUT_TRANSACTION_ID)?.takeIf(::isUuid)
            ?: throw IOException("The host output ID is invalid")
        try {
            write { session.openOutput(id) ?: throw IOException("The host opened no output") }
            verify(session.openPendingOutput(id) ?: throw IOException("The host output cannot be read back"), expectedSize, mimeType)
            val result = session.commitOutput(id) ?: throw IOException("The host returned no commit result")
            val state = result.getInt(ExplorerActionHostSessionKeys.OUTPUT_STATE, ExplorerActionHostSessionValues.OUTPUT_STATE_COMMITTED)
            if (state != ExplorerActionHostSessionValues.OUTPUT_STATE_COMMITTED) throw IOException("The host did not publish the output")
            val history = result.getString(ExplorerActionHostSessionKeys.TARGET_REPLACEMENT_HISTORY_ID)?.takeIf {
                overwrite && it.length in 1..ExplorerActionProtocol.MAX_TARGET_REPLACEMENT_HISTORY_ID_LENGTH &&
                    result.getInt(ExplorerActionHostSessionKeys.TARGET_REPLACEMENT_UNDO_STATE) ==
                    ExplorerActionHostSessionValues.TARGET_REPLACEMENT_UNDO_STATE_AVAILABLE
            }
            return Published(history)
        } catch (error: Throwable) {
            runCatching { session.abortOutput(id) }
            throw error
        }
    }

    /** Puts the original back once (v18); true when the host restored it, false when it is no longer possible. */
    fun undo(replacementHistoryId: String): Boolean {
        val result = session.undoTargetReplacement(targetId, replacementHistoryId) ?: return false
        return result.getInt(ExplorerActionHostSessionKeys.TARGET_REPLACEMENT_UNDO_STATE) ==
            ExplorerActionHostSessionValues.TARGET_REPLACEMENT_UNDO_STATE_RESTORED
    }

    /** Ends the request-scoped session; call it once the tool Activity finishes. */
    fun close() {
        runCatching { session.close() }
    }

    private fun verify(descriptor: ParcelFileDescriptor, expectedSize: ImagePixelSize, mimeType: String) {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        descriptor.use { FileInputStream(it.fileDescriptor).use { input -> BitmapFactory.decodeStream(input, null, bounds) } }
        if (bounds.outWidth != expectedSize.width || bounds.outHeight != expectedSize.height || bounds.outMimeType != mimeType) {
            throw IOException("The written image does not read back as expected")
        }
    }

    private companion object {
        val uuidPattern = Regex("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")

        fun isUuid(value: String): Boolean = uuidPattern.matches(value)
    }
}
