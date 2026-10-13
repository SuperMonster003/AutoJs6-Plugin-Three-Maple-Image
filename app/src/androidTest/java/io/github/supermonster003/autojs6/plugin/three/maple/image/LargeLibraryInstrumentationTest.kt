package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.os.Debug
import android.os.SystemClock
import android.util.Log
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * ROADMAP 6.5 large library check. Skipped unless run with `-e largeLibrary true` on a device whose library was
 * filled with tens of thousands of images; every step logs one `MapleLargeLibrary` line (milliseconds, PSS).
 */
@RunWith(AndroidJUnit4::class)
class LargeLibraryInstrumentationTest {

    @get:Rule
    val compose = createEmptyComposeRule()

    @get:Rule
    val permission: GrantPermissionRule = GrantPermissionRule.grant(*MediaStoreTestImages.permissions())

    private val instrumentation get() = InstrumentationRegistry.getInstrumentation()
    private val context get() = instrumentation.targetContext
    private val record by lazy { MediaPermissionRecordStore(context) }
    private var originalRequested = false

    @Before
    fun onlyWhenAsked() {
        assumeTrue("run with -e largeLibrary true", InstrumentationRegistry.getArguments().getString("largeLibrary") == "true")
        originalRequested = record.requestedBefore
        record.markRequested()
    }

    @After
    fun restore() {
        if (!originalRequested) record.reset()
    }

    @Test
    fun dataLayerTimings() = runBlocking {
        val repository = MediaStoreRepository(context)
        val all = measure("full scan") { repository.loadAll() }
        val buckets = measure("album aggregation over the cached scan") { repository.loadBuckets() }
        report("library images=${all.size} albums=${buckets.size} pssKb=${Debug.getPss()}")
        repository.invalidate()
        measure("album list cold (scan + aggregation)") { repository.loadBuckets() }
        val pageSize = GalleryViewModel.PAGE_SIZE
        measure("all images first page, date") { repository.loadBucket(MediaCatalog.ALL_BUCKETS, MediaSort.DEFAULT, pageSize, 0) }
        measure("all images page at offset 30000, date") { repository.loadBucket(MediaCatalog.ALL_BUCKETS, MediaSort.DEFAULT, pageSize, 30_000) }
        val byName = MediaSort(MediaSortField.NAME, ascending = true)
        measure("all images first page, name (natural order)") { repository.loadBucket(MediaCatalog.ALL_BUCKETS, byName, pageSize, 0) }
        measure("all images second page, name (cached order)") { repository.loadBucket(MediaCatalog.ALL_BUCKETS, byName, pageSize, pageSize) }
        measure("viewer source over all images, own query") { repository.loadBucketAll(MediaCatalog.ALL_BUCKETS, MediaSort.DEFAULT) }
        coroutineScope {
            val observing = launch(Dispatchers.Default) { repository.changes().collect {} }
            delay(300)
            repository.invalidate()
            repository.loadAll()
            measure("viewer source over all images, shared gallery scan") {
                MediaStoreRepository(context).loadBucketAll(MediaCatalog.ALL_BUCKETS, MediaSort.DEFAULT)
            }
            observing.cancel()
        }
        val largest = buckets.maxBy { it.count }
        measure("largest album (${largest.count}) first page") { repository.loadBucket(largest.id, MediaSort.DEFAULT, pageSize, 0) }
        report("after data layer pssKb=${Debug.getPss()} heapKb=${usedHeapKb()}")
    }

    @Test
    fun homeAndAllImagesTimings() {
        val launched = SystemClock.elapsedRealtime()
        ActivityScenario.launch(HomeActivity::class.java).use {
            compose.waitUntil(TIMEOUT_MILLIS) { compose.onAllNodes(tagPrefix("maple.gallery.album.media.")).fetchSemanticsNodes().isNotEmpty() }
            report("home launch to first album cell ms=${SystemClock.elapsedRealtime() - launched} pssKb=${Debug.getPss()}")
            compose.onNodeWithTag("maple.gallery.more").performClick()
            compose.onNodeWithTag("maple.gallery.menu.showAll").performClick()
            val opened = SystemClock.elapsedRealtime()
            compose.waitUntil(TIMEOUT_MILLIS) { compose.onAllNodes(tagPrefix("maple.media.item.")).fetchSemanticsNodes().isNotEmpty() }
            report("all images tap to first media cell ms=${SystemClock.elapsedRealtime() - opened} pssKb=${Debug.getPss()} heapKb=${usedHeapKb()}")
        }
    }

    private inline fun <T> measure(step: String, block: () -> T): T {
        val started = SystemClock.elapsedRealtime()
        val value = block()
        report("$step ms=${SystemClock.elapsedRealtime() - started}")
        return value
    }

    private fun report(line: String) {
        Log.i(TAG, line)
    }

    private fun usedHeapKb(): Long = Runtime.getRuntime().let { (it.totalMemory() - it.freeMemory()) / 1024 }

    private fun tagPrefix(prefix: String) = SemanticsMatcher("test tag starts with $prefix") { node ->
        node.config.getOrNull(SemanticsProperties.TestTag)?.startsWith(prefix) == true
    }

    private companion object {
        const val TAG = "MapleLargeLibrary"
        const val TIMEOUT_MILLIS = 120_000L
    }
}
