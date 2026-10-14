package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Debug
import android.os.SystemClock
import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * ROADMAP 8.1 measurements, only with `-e lookBenchmark true` (results in logcat, tag MapleLook): a filter on a
 * 2000 x 2000 image through the color filter path, the pixel pass on the preview size, and the full pixel pass on
 * the largest image the editor accepts on this device (at most 20 MP).
 */
@RunWith(AndroidJUnit4::class)
class EditorLookBenchmarkTest {

    @Test
    fun measure() {
        assumeTrue(InstrumentationRegistry.getArguments().getString("lookBenchmark") == "true")
        val filter = EditorLook(FilterPreset.FILM, 80).with(LookAdjustment.EXPOSURE, 20).with(LookAdjustment.HUE, 10)
        val full = filter.with(LookAdjustment.SHADOWS, 40).with(LookAdjustment.HIGHLIGHTS, -30)
            .with(LookAdjustment.SHARPEN, 60).with(LookAdjustment.VIGNETTE, 50)

        val square = gradient(2000, 2000)
        log("matrix look on 2000 x 2000", square, filter)
        val preview = ImageEditingEngine.downscale(square, 700_000L)
        log("pixel pass on the ${preview.width} x ${preview.height} preview", preview, full)
        log("pixel pass on 2000 x 2000", square, full)
        square.recycle()

        val pixels = minOf(20_000_000L, ImageBitmapIO.editingPixelBudget())
        val width = kotlin.math.sqrt(pixels * 4.0 / 3.0).toInt()
        val height = (pixels / width).toInt()
        val big = gradient(width, height)
        log("matrix look on $width x $height", big, filter)
        log("pixel pass on $width x $height", big, full)
        big.recycle()
    }

    private fun log(label: String, source: Bitmap, look: EditorLook) {
        val runs = 3
        var best = Long.MAX_VALUE
        var peak = 0L
        repeat(runs) {
            System.gc()
            val before = usedMemory()
            val started = SystemClock.elapsedRealtimeNanos()
            val result = ImageEditingEngine.applyLook(source, look)
            val elapsed = (SystemClock.elapsedRealtimeNanos() - started) / 1_000_000
            peak = maxOf(peak, usedMemory() - before)
            result.recycle()
            best = minOf(best, elapsed)
        }
        Log.i(TAG, "$label: best of $runs $best ms, about ${peak / 1_048_576} MiB more while it ran (heap max ${Runtime.getRuntime().maxMemory() / 1_048_576} MiB)")
    }

    private fun usedMemory(): Long = Runtime.getRuntime().let { it.totalMemory() - it.freeMemory() } + Debug.getNativeHeapAllocatedSize()

    private fun gradient(width: Int, height: Int): Bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).also { bitmap ->
        val row = IntArray(width)
        for (y in 0 until height) {
            for (x in 0 until width) row[x] = Color.rgb(x * 255 / width, y * 255 / height, (x + y) % 256)
            bitmap.setPixels(row, 0, width, 0, y, width, 1)
        }
    }

    private companion object {
        const val TAG = "MapleLook"
    }
}
