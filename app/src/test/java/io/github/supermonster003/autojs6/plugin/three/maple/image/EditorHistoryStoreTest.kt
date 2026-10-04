package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.graphics.Bitmap
import android.graphics.Color
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class EditorHistoryStoreTest {

    private lateinit var cacheDirectory: File

    @Before
    fun setUp() {
        cacheDirectory = Files.createTempDirectory("image-editor-history-test").toFile()
    }

    @After
    fun tearDown() {
        cacheDirectory.deleteRecursively()
    }

    @Test
    fun undoAndRedoRoundTripRetainsStateIdsAndPixels() {
        val original = bitmap(Color.RED)
        val edited = bitmap(Color.BLUE)
        val store = EditorHistoryStore(cacheDirectory)
        var undone: Bitmap? = null
        var redone: Bitmap? = null
        try {
            assertTrue(store.initializeOriginal(original, stateId = 0L))
            assertTrue(store.commit(store.prepare(original, stateId = 0L)))

            val undoResult = store.undo(edited, stateId = 1L)
            undone = undoResult.bitmap
            assertEquals(0L, undoResult.stateId)
            assertColor(Color.RED, undoResult.bitmap)
            assertFalse(store.canUndo)
            assertTrue(store.canRedo)

            val redoResult = store.redo(undoResult.bitmap, stateId = undoResult.stateId)
            redone = redoResult.bitmap
            assertEquals(1L, redoResult.stateId)
            assertColor(Color.BLUE, redoResult.bitmap)
            assertTrue(store.canUndo)
            assertFalse(store.canRedo)
        } finally {
            redone?.recycle()
            undone?.recycle()
            original.recycle()
            edited.recycle()
            store.clear()
        }
    }

    @Test
    fun restoreOriginalParticipatesInUndoAndRedoHistory() {
        val original = bitmap(Color.RED)
        val edited = bitmap(Color.GREEN)
        val store = EditorHistoryStore(cacheDirectory)
        var restored: Bitmap? = null
        var undoRestore: Bitmap? = null
        var redoRestore: Bitmap? = null
        try {
            assertTrue(store.initializeOriginal(original, stateId = 0L))
            assertTrue(store.commit(store.prepare(original, stateId = 0L)))

            val restoreResult = store.restoreOriginal(edited, stateId = 1L)
            restored = restoreResult.bitmap
            assertEquals(0L, restoreResult.stateId)
            assertColor(Color.RED, restoreResult.bitmap)

            val undoResult = store.undo(restoreResult.bitmap, stateId = restoreResult.stateId)
            undoRestore = undoResult.bitmap
            assertEquals(1L, undoResult.stateId)
            assertColor(Color.GREEN, undoResult.bitmap)

            val redoResult = store.redo(undoResult.bitmap, stateId = undoResult.stateId)
            redoRestore = redoResult.bitmap
            assertEquals(0L, redoResult.stateId)
            assertColor(Color.RED, redoResult.bitmap)
        } finally {
            redoRestore?.recycle()
            undoRestore?.recycle()
            restored?.recycle()
            original.recycle()
            edited.recycle()
            store.clear()
        }
    }

    @Test
    fun sharedEntryLimitEvictsOnlyDeepestNavigationStates() {
        val states = listOf(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW).map(::bitmap)
        val store = EditorHistoryStore(cacheDirectory, maxHistorySize = 2)
        val restored = mutableListOf<Bitmap>()
        try {
            assertTrue(store.initializeOriginal(states[0], stateId = 0L))
            assertTrue(store.commit(store.prepare(states[0], stateId = 0L)))
            assertTrue(store.commit(store.prepare(states[1], stateId = 1L)))
            assertTrue(store.commit(store.prepare(states[2], stateId = 2L)))

            val firstUndo = store.undo(states[3], stateId = 3L)
            restored += firstUndo.bitmap
            assertEquals(2L, firstUndo.stateId)
            val secondUndo = store.undo(firstUndo.bitmap, stateId = firstUndo.stateId)
            restored += secondUndo.bitmap
            assertEquals(1L, secondUndo.stateId)
            assertFalse(store.canUndo)
            assertTrue(store.canRedo)

            val firstRedo = store.redo(secondUndo.bitmap, stateId = secondUndo.stateId)
            restored += firstRedo.bitmap
            assertEquals(2L, firstRedo.stateId)
            val secondRedo = store.redo(firstRedo.bitmap, stateId = firstRedo.stateId)
            restored += secondRedo.bitmap
            assertEquals(3L, secondRedo.stateId)
            assertFalse(store.canRedo)
        } finally {
            restored.forEach(Bitmap::recycle)
            states.forEach(Bitmap::recycle)
            store.clear()
        }
    }

    @Test
    fun byteBudgetKeepsZeroCostOriginalEntryWhileEvictingOlderEncodedState() {
        val states = listOf(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW).map(::bitmap)
        val store = EditorHistoryStore(
            cacheDirectory = cacheDirectory,
            maxHistoryBytes = 250L,
            codec = FixedSizeSnapshotCodec(bytesPerSnapshot = 100),
        )
        val restored = mutableListOf<Bitmap>()
        try {
            assertTrue(store.initializeOriginal(states[0], stateId = 0L))
            assertTrue(store.commit(store.prepare(states[0], stateId = 0L)))
            assertTrue(store.commit(store.prepare(states[1], stateId = 1L)))
            assertTrue(store.commit(store.prepare(states[2], stateId = 2L)))

            val firstUndo = store.undo(states[3], stateId = 3L)
            restored += firstUndo.bitmap
            assertEquals(2L, firstUndo.stateId)
            assertTrue(store.canUndo)

            val secondUndo = store.undo(firstUndo.bitmap, stateId = firstUndo.stateId)
            restored += secondUndo.bitmap
            assertEquals(0L, secondUndo.stateId)
            assertColor(Color.RED, secondUndo.bitmap)
            assertFalse(store.canUndo)
        } finally {
            restored.forEach(Bitmap::recycle)
            states.forEach(Bitmap::recycle)
            store.clear()
        }
    }

    @Test
    fun discardedEditPreservesRedoButCommittedBranchClearsIt() {
        val original = bitmap(Color.RED)
        val edited = bitmap(Color.BLUE)
        val store = EditorHistoryStore(cacheDirectory)
        var undone: Bitmap? = null
        try {
            assertTrue(store.initializeOriginal(original, stateId = 0L))
            assertTrue(store.commit(store.prepare(original, stateId = 0L)))
            val undoResult = store.undo(edited, stateId = 1L)
            undone = undoResult.bitmap
            assertTrue(store.canRedo)

            val cancelled = store.prepare(undoResult.bitmap, stateId = undoResult.stateId)
            store.discard(cancelled)
            assertTrue(store.canRedo)

            assertTrue(store.commit(store.prepare(undoResult.bitmap, stateId = undoResult.stateId)))
            assertFalse(store.canRedo)
        } finally {
            undone?.recycle()
            original.recycle()
            edited.recycle()
            store.clear()
        }
    }

    @Test
    fun snapshotWriteOutOfMemoryDegradesWithoutLeavingHistoryGap() {
        val original = bitmap(Color.RED)
        val edited = bitmap(Color.BLUE)
        val codec = FailingSnapshotCodec()
        val store = EditorHistoryStore(cacheDirectory, codec = codec)
        try {
            assertTrue(store.initializeOriginal(original, stateId = 0L))
            assertTrue(store.commit(store.prepare(original, stateId = 0L)))

            codec.failWrites = true
            val prepared = store.prepare(edited, stateId = 1L)
            assertNull(prepared)
            assertFalse(store.commit(prepared))
            assertFalse(store.canUndo)
            assertFalse(store.canRedo)
            assertTrue(store.canRestoreOriginal)
        } finally {
            original.recycle()
            edited.recycle()
            store.clear()
        }
    }

    @Test
    fun snapshotReadOutOfMemoryLeavesUndoTransitionUntouched() {
        val original = bitmap(Color.RED)
        val edited = bitmap(Color.BLUE)
        val codec = FailingSnapshotCodec()
        val store = EditorHistoryStore(cacheDirectory, codec = codec)
        try {
            assertTrue(store.initializeOriginal(original, stateId = 0L))
            assertTrue(store.commit(store.prepare(original, stateId = 0L)))

            codec.failReads = true
            assertThrows(EditorHistoryStore.SnapshotUnavailableException::class.java) {
                store.undo(edited, stateId = 1L)
            }
            assertTrue(store.canUndo)
            assertFalse(store.canRedo)
        } finally {
            original.recycle()
            edited.recycle()
            store.clear()
        }
    }

    private fun bitmap(color: Int): Bitmap = Bitmap.createBitmap(8, 8, Bitmap.Config.ARGB_8888).apply {
        eraseColor(color)
    }

    private fun assertColor(expected: Int, bitmap: Bitmap) {
        assertEquals(expected, bitmap.getPixel(bitmap.width / 2, bitmap.height / 2))
    }

    private class FailingSnapshotCodec : EditorSnapshotCodec {
        var failWrites = false
        var failReads = false

        override fun write(bitmap: Bitmap, file: File) {
            if (failWrites) throw OutOfMemoryError("simulated snapshot write failure")
            PngEditorSnapshotCodec.write(bitmap, file)
        }

        override fun read(file: File): Bitmap {
            if (failReads) throw OutOfMemoryError("simulated snapshot read failure")
            return PngEditorSnapshotCodec.read(file)
        }
    }

    private class FixedSizeSnapshotCodec(private val bytesPerSnapshot: Int) : EditorSnapshotCodec {
        private val colors = mutableMapOf<String, Int>()

        override fun write(bitmap: Bitmap, file: File) {
            colors[file.absolutePath] = bitmap.getPixel(0, 0)
            FileOutputStream(file).use { it.write(ByteArray(bytesPerSnapshot)) }
        }

        override fun read(file: File): Bitmap {
            val color = requireNotNull(colors[file.absolutePath])
            return Bitmap.createBitmap(8, 8, Bitmap.Config.ARGB_8888).apply { eraseColor(color) }
        }
    }
}
