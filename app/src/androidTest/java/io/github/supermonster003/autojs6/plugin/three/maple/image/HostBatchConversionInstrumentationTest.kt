package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.os.IBinder
import android.os.ParcelFileDescriptor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.autojs.plugin.explorer.api.ExplorerActionHostSessionKeys
import org.autojs.plugin.explorer.api.ExplorerActionHostSessionValues
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.UUID

/**
 * ROADMAP 7.6 (host entry): "Convert images" from the file manager writes one create-in-parent output per image,
 * reads each back, and publishes them together (one through `commitOutput`, several as a v15 output batch); one
 * unreadable image rolls every output back. The session below plays the host's transaction rules.
 */
@RunWith(AndroidJUnit4::class)
class HostBatchConversionInstrumentationTest {

    @get:Rule
    val compose = createEmptyComposeRule()

    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private val run = UUID.randomUUID().toString().take(8)
    private val sources by lazy { File(context.cacheDir, "standalone-images/batch-$run").apply { mkdirs() } }
    private val outputs by lazy { File(context.cacheDir, "batch-out-$run").apply { mkdirs() } }

    @After
    fun tearDown() {
        sources.deleteRecursively()
        outputs.deleteRecursively()
    }

    @Test
    fun theConvertImagesEnvelopeNeedsItsSession() {
        val files = listOf(image("a.png", 20, 10, Bitmap.CompressFormat.PNG), image("b.png", 20, 10, Bitmap.CompressFormat.PNG))
        val session = OutputHostSession(outputs)
        val request = ImageRequestPolicy.resolveExplorer(HostRequestFixtures.convertIntent(context, files, session))
        assertNotNull(request)
        assertEquals(ExplorerImageRequestMode.BATCH_CONVERT, request?.mode)
        assertEquals(listOf("a.png", "b.png"), request?.targets?.map { it.image.displayName })
        // Even one image needs the session: without it the files could not be created.
        assertNull(ImageRequestPolicy.resolveExplorer(HostRequestFixtures.convertIntent(context, files.take(1), null)))
    }

    @Test
    fun severalImagesArePublishedTogetherAsOneBatch() {
        val files = listOf(image("first.png", 50, 40, Bitmap.CompressFormat.PNG), image("second.jpg", 30, 20, Bitmap.CompressFormat.JPEG))
        val session = OutputHostSession(outputs)
        ActivityScenario.launch<HostBatchConverterActivity>(HostRequestFixtures.convertIntent(context, files, session)).use { scenario ->
            compose.waitUntil(TIMEOUT_MILLIS) { compose.onAllNodes(hasTestTag("maple.batch.start")).fetchSemanticsNodes().isNotEmpty() }
            compose.onNodeWithTag("maple.batch.format.2").performScrollTo().performClick()
            compose.onNodeWithTag("maple.batch.rotation.1").performScrollTo().performClick()
            compose.onNodeWithTag("maple.batch.start").performClick()
            compose.waitUntil(TIMEOUT_MILLIS) { scenario.state == Lifecycle.State.DESTROYED }
        }
        assertEquals(1, session.batches.size)
        assertEquals(0, session.singleCommits.size)
        assertEquals(mapOf("first_converted.webp" to (40 to 50), "second_converted.webp" to (20 to 30)), published())
        assertEquals(emptyList<String>(), outputs.list().orEmpty().filter { it.endsWith(".part") })
        assertTrue(session.attached)
        assertTrue(session.closed)
    }

    @Test
    fun oneImageIsPublishedWithASingleCommit() {
        val files = listOf(image("only.webp", 24, 12, @Suppress("DEPRECATION") Bitmap.CompressFormat.WEBP))
        val session = OutputHostSession(outputs)
        ActivityScenario.launch<HostBatchConverterActivity>(HostRequestFixtures.convertIntent(context, files, session)).use { scenario ->
            compose.waitUntil(TIMEOUT_MILLIS) { compose.onAllNodes(hasTestTag("maple.batch.start")).fetchSemanticsNodes().isNotEmpty() }
            compose.onNodeWithTag("maple.batch.start").performClick()
            compose.waitUntil(TIMEOUT_MILLIS) { scenario.state == Lifecycle.State.DESTROYED }
        }
        assertEquals(0, session.batches.size)
        assertEquals(1, session.singleCommits.size)
        assertEquals(mapOf("only_converted.jpg" to (24 to 12)), published())
    }

    @Test
    fun anUnreadableImageRollsEveryOutputBack() {
        val good = image("good.png", 20, 20, Bitmap.CompressFormat.PNG)
        val bad = File(sources, "bad.png").apply { writeBytes(ByteArray(64) { it.toByte() }) }
        val session = OutputHostSession(outputs)
        ActivityScenario.launch<HostBatchConverterActivity>(HostRequestFixtures.convertIntent(context, listOf(good, bad), session)).use { scenario ->
            compose.waitUntil(TIMEOUT_MILLIS) { compose.onAllNodes(hasTestTag("maple.batch.start")).fetchSemanticsNodes().isNotEmpty() }
            compose.onNodeWithTag("maple.batch.start").performClick()
            compose.waitUntil(TIMEOUT_MILLIS) { compose.onAllNodes(hasTestTag("maple.batch.hostFailed")).fetchSemanticsNodes().isNotEmpty() }
            assertEquals(0, session.batches.size + session.singleCommits.size)
            assertEquals(session.prepared, session.aborted)
            assertEquals(emptyMap<String, Pair<Int, Int>>(), published())
            assertEquals(emptyList<String>(), outputs.list().orEmpty().toList())
            compose.onNodeWithTag("maple.dialog.confirm").performClick()
            compose.waitUntil(TIMEOUT_MILLIS) { scenario.state == Lifecycle.State.DESTROYED }
        }
        assertTrue(session.closed)
    }

    private fun image(name: String, width: Int, height: Int, format: Bitmap.CompressFormat): File {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).apply { eraseColor(Color.rgb(200, 120, 40)) }
        return File(sources, name).apply {
            outputStream().use { bitmap.compress(format, 90, it) }
            bitmap.recycle()
        }
    }

    /** Published output names with their decoded width and height. */
    private fun published(): Map<String, Pair<Int, Int>> = outputs.listFiles().orEmpty().filter { !it.name.endsWith(".part") }.associate { file ->
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.path, options)
        file.name to (options.outWidth to options.outHeight)
    }

    /**
     * The host's create-in-parent rules in small: staged `.part` files, one writer and one read-back per output, a
     * batch only of read-back outputs, publication by rename, and aborts that delete what was staged.
     */
    private class OutputHostSession(private val directory: File) : UnsupportedHostSessionStub() {

        private class Output(val id: String, val name: String, val staged: File, var state: Int)

        private val outputs = LinkedHashMap<String, Output>()
        private val batchMembers = HashMap<String, List<String>>()
        val prepared = ArrayList<String>()
        val aborted = ArrayList<String>()
        val batches = ArrayList<String>()
        val singleCommits = ArrayList<String>()

        @Volatile
        var attached = false
            private set

        @Volatile
        var closed = false
            private set

        @Synchronized
        override fun prepareOutput(displayName: String?, mimeType: String?, conflictPolicy: Int): Bundle {
            requireNotNull(displayName)
            assertEquals(ExplorerActionHostSessionValues.OUTPUT_CONFLICT_AUTO_RENAME, conflictPolicy)
            val taken = outputs.values.map { it.name } + directory.list().orEmpty()
            val name = NameConflictPolicy.uniqueName(displayName, taken)
            val output = Output(UUID.randomUUID().toString(), name, File(directory, ".${UUID.randomUUID()}.part"), ExplorerActionHostSessionValues.OUTPUT_STATE_PREPARED)
            check(output.staged.createNewFile())
            outputs[output.id] = output
            prepared += output.id
            return Bundle().apply {
                putString(ExplorerActionHostSessionKeys.OUTPUT_TRANSACTION_ID, output.id)
                putString(ExplorerActionHostSessionKeys.OUTPUT_DISPLAY_NAME, name)
                putString(ExplorerActionHostSessionKeys.OUTPUT_DISPLAY_PATH, "/images/$name")
                putInt(ExplorerActionHostSessionKeys.OUTPUT_STATE, output.state)
            }
        }

        @Synchronized
        override fun openOutput(transactionId: String?): ParcelFileDescriptor {
            val output = outputs.getValue(requireNotNull(transactionId))
            check(output.state == ExplorerActionHostSessionValues.OUTPUT_STATE_PREPARED) { "opened twice" }
            output.state = OPENED
            return ParcelFileDescriptor.open(output.staged, ParcelFileDescriptor.MODE_WRITE_ONLY or ParcelFileDescriptor.MODE_TRUNCATE)
        }

        @Synchronized
        override fun openPendingOutput(transactionId: String?): ParcelFileDescriptor {
            val output = outputs.getValue(requireNotNull(transactionId))
            check(output.state == OPENED) { "not written" }
            output.state = VERIFYING
            return ParcelFileDescriptor.open(output.staged, ParcelFileDescriptor.MODE_READ_ONLY)
        }

        @Synchronized
        override fun commitOutput(transactionId: String?): Bundle {
            val output = outputs.getValue(requireNotNull(transactionId))
            check(output.state == OPENED || output.state == VERIFYING)
            publish(output)
            singleCommits += output.id
            return Bundle().apply { putInt(ExplorerActionHostSessionKeys.OUTPUT_STATE, ExplorerActionHostSessionValues.OUTPUT_STATE_COMMITTED) }
        }

        @Synchronized
        override fun prepareOutputBatch(transactionIds: MutableList<String>?): Bundle {
            requireNotNull(transactionIds)
            check(transactionIds.size >= 2 && transactionIds.all { outputs.getValue(it).state == VERIFYING }) { "batch members must be read back" }
            val id = UUID.randomUUID().toString()
            batchMembers[id] = transactionIds.toList()
            return Bundle().apply {
                putString(ExplorerActionHostSessionKeys.OUTPUT_BATCH_ID, id)
                putInt(ExplorerActionHostSessionKeys.OUTPUT_BATCH_STATE, ExplorerActionHostSessionValues.OUTPUT_BATCH_STATE_PREPARED)
            }
        }

        @Synchronized
        override fun commitOutputBatch(batchId: String?): Bundle {
            val members = batchMembers.getValue(requireNotNull(batchId))
            members.forEach { publish(outputs.getValue(it)) }
            batches += requireNotNull(batchId)
            return Bundle().apply {
                putString(ExplorerActionHostSessionKeys.OUTPUT_BATCH_ID, batchId)
                putInt(ExplorerActionHostSessionKeys.OUTPUT_BATCH_STATE, ExplorerActionHostSessionValues.OUTPUT_BATCH_STATE_COMMITTED)
                putInt(ExplorerActionHostSessionKeys.OUTPUT_BATCH_PUBLISHED_COUNT, members.size)
            }
        }

        @Synchronized
        override fun abortOutput(transactionId: String?) {
            val output = outputs[transactionId] ?: return
            if (output.state == ExplorerActionHostSessionValues.OUTPUT_STATE_COMMITTED) return
            output.staged.delete()
            output.state = ABORTED
            if (output.id !in aborted) aborted += output.id
        }

        override fun attachClient(clientToken: IBinder?) {
            attached = clientToken != null
        }

        override fun close() {
            closed = true
        }

        private fun publish(output: Output) {
            check(output.staged.renameTo(File(directory, output.name)))
            output.state = ExplorerActionHostSessionValues.OUTPUT_STATE_COMMITTED
        }

        private companion object {
            const val OPENED = 100
            const val VERIFYING = 101
            const val ABORTED = 102
        }
    }

    private companion object {
        const val TIMEOUT_MILLIS = 30_000L
    }
}
