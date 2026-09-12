package io.github.supermonster003.autojs6.plugin.imagetools

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.ArrayDeque
import java.util.concurrent.CancellationException
import java.util.concurrent.atomic.AtomicLong

internal interface EditorSnapshotCodec {
    fun write(bitmap: Bitmap, file: File)
    fun read(file: File): Bitmap
}

internal object PngEditorSnapshotCodec : EditorSnapshotCodec {
    override fun write(bitmap: Bitmap, file: File) {
        FileOutputStream(file).use { output ->
            if (!bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)) {
                throw IOException("Unable to encode image editor history")
            }
            output.fd.sync()
        }
    }

    override fun read(file: File): Bitmap = FileInputStream(file).use(BitmapFactory::decodeStream)
        ?: throw IOException("Unable to decode image editor history")
}

internal class EditorHistoryStore(
    cacheDirectory: File,
    private val maxHistorySize: Int = MAX_HISTORY_SIZE,
    private val maxHistoryBytes: Long = MAX_HISTORY_BYTES,
    private val codec: EditorSnapshotCodec = PngEditorSnapshotCodec,
) {

    data class Restored(
        val bitmap: Bitmap,
        val stateId: Long,
        val returnStateRetained: Boolean,
    )

    class SnapshotUnavailableException : IOException("Image editor history is unavailable")

    class Prepared internal constructor(
        private val owner: EditorHistoryStore,
        private val snapshot: Snapshot,
        private val stateId: Long,
    ) {
        private var consumed = false

        internal fun consume(expectedOwner: EditorHistoryStore): Pair<Snapshot, Long> {
            check(owner === expectedOwner && !consumed) { "Image editor history snapshot was already consumed" }
            consumed = true
            return snapshot to stateId
        }

        internal fun discard(expectedOwner: EditorHistoryStore): Snapshot? {
            check(owner === expectedOwner) { "Image editor history snapshot belongs to another store" }
            if (consumed) return null
            consumed = true
            return snapshot
        }
    }

    internal class Snapshot(val file: File)

    private class Entry(
        val snapshot: Snapshot,
        val stateId: Long,
        val order: Long,
    )

    private val undoEntries = ArrayDeque<Entry>()
    private val redoEntries = ArrayDeque<Entry>()
    private val directory = File(
        cacheDirectory,
        "image-editor-history-${SESSION_COUNTER.incrementAndGet()}",
    )
    private var originalSnapshot: Snapshot? = null
    private var originalStateId: Long? = null
    private var entryCounter = 0L

    init {
        require(maxHistorySize >= 1) { "Image editor history size must be positive" }
        require(maxHistoryBytes >= 1L) { "Image editor history byte budget must be positive" }
    }

    val canUndo: Boolean
        get() = undoEntries.isNotEmpty()

    val canRedo: Boolean
        get() = redoEntries.isNotEmpty()

    val canRestoreOriginal: Boolean
        get() = originalSnapshot != null

    fun isOriginalState(stateId: Long): Boolean = originalStateId == stateId

    fun initializeOriginal(bitmap: Bitmap, stateId: Long): Boolean {
        clear()
        val snapshot = createSnapshot(bitmap) ?: return false
        if (snapshot.file.length() > maxHistoryBytes) {
            snapshot.file.delete()
            deleteDirectoryIfEmpty()
            return false
        }
        originalSnapshot = snapshot
        originalStateId = stateId
        return true
    }

    /**
     * Captures the current state without changing either navigation stack. This lets a failed
     * or cancelled edit discard its pending snapshot without destroying the redo branch.
     */
    fun prepare(bitmap: Bitmap, stateId: Long): Prepared? {
        val snapshot = snapshotForState(bitmap, stateId) ?: return null
        return Prepared(this, snapshot, stateId)
    }

    /** Commits a successfully applied edit and invalidates the previous redo branch. */
    fun commit(prepared: Prepared?): Boolean {
        clearStack(redoEntries)
        if (prepared == null) {
            clearStack(undoEntries)
            return false
        }
        val (snapshot, stateId) = prepared.consume(this)
        val entry = Entry(snapshot, stateId, ++entryCounter)
        undoEntries.addLast(entry)
        trimHistory()
        val retained = undoEntries.any { it === entry }
        if (!retained) clearStack(undoEntries)
        return retained
    }

    fun discard(prepared: Prepared?) {
        val snapshot = prepared?.discard(this) ?: return
        deleteIfUnreferenced(snapshot)
    }

    fun undo(bitmap: Bitmap, stateId: Long): Restored = transition(
        source = undoEntries,
        destination = redoEntries,
        bitmap = bitmap,
        stateId = stateId,
    )

    fun redo(bitmap: Bitmap, stateId: Long): Restored = transition(
        source = redoEntries,
        destination = undoEntries,
        bitmap = bitmap,
        stateId = stateId,
    )

    fun restoreOriginal(bitmap: Bitmap, stateId: Long): Restored {
        val original = originalSnapshot ?: throw SnapshotUnavailableException()
        if (isOriginalState(stateId)) throw IllegalStateException("Image is already in its original state")
        val returnSnapshot = snapshotForState(bitmap, stateId) ?: throw SnapshotUnavailableException()
        val restored = try {
            decode(original)
        } catch (error: Throwable) {
            deleteIfUnreferenced(returnSnapshot)
            throw error
        }
        clearStack(redoEntries)
        val returnEntry = Entry(returnSnapshot, stateId, ++entryCounter)
        undoEntries.addLast(returnEntry)
        trimHistory()
        return Restored(
            bitmap = restored,
            stateId = requireNotNull(originalStateId),
            returnStateRetained = undoEntries.any { it === returnEntry },
        )
    }

    fun clear() {
        undoEntries.clear()
        redoEntries.clear()
        originalSnapshot = null
        originalStateId = null
        directory.listFiles()?.forEach(File::delete)
        directory.delete()
    }

    private fun transition(
        source: ArrayDeque<Entry>,
        destination: ArrayDeque<Entry>,
        bitmap: Bitmap,
        stateId: Long,
    ): Restored {
        val target = source.peekLast() ?: throw SnapshotUnavailableException()
        val returnSnapshot = snapshotForState(bitmap, stateId) ?: throw SnapshotUnavailableException()
        val restored = try {
            decode(target.snapshot)
        } catch (error: Throwable) {
            deleteIfUnreferenced(returnSnapshot)
            throw error
        }
        source.removeLast()
        val returnEntry = Entry(returnSnapshot, stateId, ++entryCounter)
        destination.addLast(returnEntry)
        deleteIfUnreferenced(target.snapshot)
        trimHistory()
        return Restored(
            bitmap = restored,
            stateId = target.stateId,
            returnStateRetained = destination.any { it === returnEntry },
        )
    }

    private fun snapshotForState(bitmap: Bitmap, stateId: Long): Snapshot? =
        originalSnapshot?.takeIf { isOriginalState(stateId) } ?: createSnapshot(bitmap)

    private fun createSnapshot(bitmap: Bitmap): Snapshot? {
        if (!directory.exists() && !directory.mkdirs()) return null
        val file = File(directory, "${System.nanoTime()}-${FILE_COUNTER.incrementAndGet()}.png")
        return try {
            codec.write(bitmap, file)
            if (!file.isFile || file.length() <= 0L) throw IOException("Image editor history is empty")
            Snapshot(file)
        } catch (error: Throwable) {
            file.delete()
            deleteDirectoryIfEmpty()
            if (error.isRecoverableSnapshotFailure()) null else throw error
        }
    }

    private fun decode(snapshot: Snapshot): Bitmap = try {
        codec.read(snapshot.file)
    } catch (error: Throwable) {
        if (error.isRecoverableSnapshotFailure()) throw SnapshotUnavailableException()
        throw error
    }

    private fun trimHistory() {
        while (historySize() > maxHistorySize) {
            removeEntry(oldestEntry() ?: break)
        }
        while (storedBytes() > maxHistoryBytes) {
            val oldestReleasingBytes = oldestEntry { entry ->
                entry.snapshot !== originalSnapshot && snapshotReferenceCount(entry.snapshot) == 1
            } ?: break
            removeEntry(oldestReleasingBytes)
        }
    }

    private fun oldestEntry(predicate: (Entry) -> Boolean = { true }): Entry? =
        sequenceOf(undoEntries.asSequence(), redoEntries.asSequence())
            .flatten()
            .filter(predicate)
            .minByOrNull(Entry::order)

    private fun removeEntry(entry: Entry) {
        if (!undoEntries.remove(entry)) redoEntries.remove(entry)
        deleteIfUnreferenced(entry.snapshot)
    }

    private fun snapshotReferenceCount(snapshot: Snapshot): Int =
        undoEntries.count { it.snapshot === snapshot } + redoEntries.count { it.snapshot === snapshot }

    private fun historySize(): Int = undoEntries.size + redoEntries.size

    private fun storedBytes(): Long {
        val paths = mutableSetOf<String>()
        var total = 0L
        fun add(snapshot: Snapshot?) {
            snapshot ?: return
            if (paths.add(snapshot.file.absolutePath)) total += snapshot.file.length()
        }
        add(originalSnapshot)
        undoEntries.forEach { add(it.snapshot) }
        redoEntries.forEach { add(it.snapshot) }
        return total
    }

    private fun clearStack(entries: ArrayDeque<Entry>) {
        while (true) {
            val removed = entries.pollFirst() ?: break
            deleteIfUnreferenced(removed.snapshot)
        }
        deleteDirectoryIfEmpty()
    }

    private fun deleteIfUnreferenced(snapshot: Snapshot) {
        if (snapshot === originalSnapshot) return
        if (undoEntries.any { it.snapshot === snapshot } || redoEntries.any { it.snapshot === snapshot }) return
        snapshot.file.delete()
        deleteDirectoryIfEmpty()
    }

    private fun deleteDirectoryIfEmpty() {
        if (directory.list()?.isEmpty() == true) directory.delete()
    }

    private fun Throwable.isRecoverableSnapshotFailure(): Boolean =
        this is IOException ||
            this is SecurityException ||
            this is OutOfMemoryError ||
            this is RuntimeException && this !is CancellationException

    private companion object {
        const val MAX_HISTORY_SIZE = 8
        const val MAX_HISTORY_BYTES = 192L * 1024L * 1024L
        val SESSION_COUNTER = AtomicLong()
        val FILE_COUNTER = AtomicLong()
    }
}
