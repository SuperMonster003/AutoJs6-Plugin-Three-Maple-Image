package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.FrameMetrics
import android.view.Window
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performSemanticsAction
import androidx.core.content.FileProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.UUID

/**
 * ROADMAP 8.1 measurement, only with `-e lookBenchmark true` (results in logcat, tag MapleLook): frame times on a
 * 2000 x 2000 image while the filter intensity and then the vignette (pixel pass preview) are moved.
 */
@RunWith(AndroidJUnit4::class)
class EditorPreviewFramesBenchmarkTest {

    @get:Rule
    val compose = createEmptyComposeRule()

    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Test fun previewFrames() {
        assumeTrue(InstrumentationRegistry.getArguments().getString("lookBenchmark") == "true")
        val fixture = File(context.cacheDir, "standalone-images/frames-${UUID.randomUUID()}.png")
        fixture.parentFile!!.mkdirs()
        val bitmap = Bitmap.createBitmap(2000, 2000, Bitmap.Config.ARGB_8888)
        val row = IntArray(2000)
        for (y in 0 until 2000) {
            for (x in 0 until 2000) row[x] = Color.rgb(x * 255 / 2000, y * 255 / 2000, (x + y) % 256)
            bitmap.setPixels(row, 0, 2000, 0, y, 2000, 1)
        }
        fixture.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()
        val uri = FileProvider.getUriForFile(context, context.packageName + ".standalone-images", fixture)
        val session = StandaloneImageWork.create(context, StandaloneDocuments.inspect(context, uri), ImageToolsPlugin.EDIT_ACTION_ID)
        val frames = HandlerThread("maple-frames").apply { start() }
        try {
            val intent = Intent(context, StandaloneImageEditorActivity::class.java).putExtra(StandaloneImageWork.EXTRA_SESSION, session)
            ActivityScenario.launch<StandaloneImageEditorActivity>(intent).use { scenario ->
                compose.waitUntil(TIMEOUT) { runCatching { compose.onNodeWithTag("maple.tool.crop").assertIsEnabled() }.isSuccess }
                compose.onNodeWithTag("maple.editor.groups").performScrollToNode(hasTestTag("maple.tool.group.filter"))
                compose.onNodeWithTag("maple.tool.group.filter").performClick()
                val film = "maple.editor.filter.${FilterPreset.FILM.ordinal}"
                compose.waitUntil(TIMEOUT) { compose.onAllNodes(hasTestTag(film)).fetchSemanticsNodes().isNotEmpty() }
                compose.onNodeWithTag("maple.editor.filters").performScrollToNode(hasTestTag(film))
                compose.onNodeWithTag(film).performClick()
                measure(scenario, frames, "filter intensity (color filter preview)", "maple.slider." + context.getString(R.string.image_editor_filter_intensity))

                compose.onNodeWithTag("maple.editor.lookTab.1").performClick()
                compose.onNodeWithTag("maple.editor.adjustments").performScrollToNode(hasTestTag("maple.chip.${LookAdjustment.VIGNETTE.ordinal}"))
                compose.onNodeWithTag("maple.chip.${LookAdjustment.VIGNETTE.ordinal}").performClick()
                measure(scenario, frames, "vignette (pixel pass preview)", "maple.slider." + context.getString(R.string.image_editor_vignette))
            }
        } finally {
            frames.quitSafely()
            StandaloneImageWork.discard(context, session)
            fixture.delete()
        }
    }

    /** Moves the slider [tag] through 40 values and logs the frame durations of the window meanwhile. */
    private fun measure(scenario: ActivityScenario<StandaloneImageEditorActivity>, thread: HandlerThread, label: String, tag: String) {
        val durations = mutableListOf<Long>()
        val listener = Window.OnFrameMetricsAvailableListener { _, metrics, _ ->
            synchronized(durations) { durations += metrics.getMetric(FrameMetrics.TOTAL_DURATION) / 1_000_000 }
        }
        scenario.onActivity { it.window.addOnFrameMetricsAvailableListener(listener, Handler(thread.looper)) }
        val started = System.nanoTime()
        repeat(40) { index ->
            compose.onNodeWithTag(tag).performSemanticsAction(SemanticsActions.SetProgress) { it(20f + index * 2f) }
            compose.waitForIdle()
        }
        // The pixel pass preview renders in the background after a short debounce; give the last one time to land.
        Thread.sleep(1_000)
        val elapsed = (System.nanoTime() - started) / 1_000_000
        scenario.onActivity { it.window.removeOnFrameMetricsAvailableListener(listener) }
        val sorted = synchronized(durations) { durations.sorted() }
        if (sorted.isEmpty()) {
            Log.i(TAG, "$label: no frames")
            return
        }
        fun percentile(p: Int) = sorted[((sorted.size - 1) * p / 100)]
        Log.i(TAG, "$label: ${sorted.size} frames in $elapsed ms for 40 changes, median ${percentile(50)} ms, p90 ${percentile(90)} ms, max ${sorted.last()} ms")
    }

    private companion object {
        const val TAG = "MapleLook"
        const val TIMEOUT = 30_000L
    }
}
