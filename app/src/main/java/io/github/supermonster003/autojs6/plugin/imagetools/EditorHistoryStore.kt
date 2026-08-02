package io.github.supermonster003.autojs6.plugin.imagetools

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.ArrayDeque
import java.util.concurrent.atomic.AtomicLong

internal class EditorHistoryStore(cacheDirectory: File) {

    data class Restored(val bitmap: Bitmap, val stateId: Long)

    private data class Snapshot(val file: File, val stateId: Long)

    private val snapshots = ArrayDeque<Snapshot>()
    private val directory = File(
        cacheDirectory,
        "image-editor-history-${SESSION_COUNTER.incrementAndGet()}",
    )

    val canUndo: Boolean
        get() = snapshots.isNotEmpty()

    fun push(bitmap: Bitmap, stateId: Long) {
        if (!directory.exists() && !directory.mkdirs()) {
            throw IOException("Unable to create image editor history")
        }
        val file = File(directory, "${System.nanoTime()}.png")
        try {
            FileOutputStream(file).use { output ->
                if (!bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)) {
                    throw IOException("Unable to encode image editor history")
                }
                output.fd.sync()
            }
            snapshots.addLast(Snapshot(file, stateId))
            trimHistory()
        } catch (error: Throwable) {
            file.delete()
            throw error
        }
    }

    fun pop(): Restored {
        val snapshot = snapshots.peekLast() ?: throw IOException("Image editor history is empty")
        val restored = FileInputStream(snapshot.file).use(BitmapFactory::decodeStream)
            ?: throw IOException("Unable to decode image editor history")
        snapshots.removeLast()
        snapshot.file.delete()
        return Restored(restored, snapshot.stateId)
    }

    fun discardLast() {
        snapshots.pollLast()?.file?.delete()
    }

    fun clear() {
        snapshots.forEach { it.file.delete() }
        snapshots.clear()
        directory.delete()
    }

    private fun trimHistory() {
        var totalBytes = snapshots.sumOf { it.file.length() }
        while (snapshots.size > 1 && (snapshots.size > MAX_HISTORY_SIZE || totalBytes > MAX_HISTORY_BYTES)) {
            val removed = snapshots.removeFirst()
            totalBytes -= removed.file.length()
            removed.file.delete()
        }
    }

    private companion object {
        const val MAX_HISTORY_SIZE = 8
        const val MAX_HISTORY_BYTES = 192L * 1024L * 1024L
        val SESSION_COUNTER = AtomicLong()
    }
}
