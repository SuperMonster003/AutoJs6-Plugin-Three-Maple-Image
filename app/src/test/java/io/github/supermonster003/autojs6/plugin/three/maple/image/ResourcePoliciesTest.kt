package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.content.ComponentCallbacks2
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** ROADMAP 9.4: the thumbnail disk cache follows the free space; memory pressure drops the library scan. */
class ResourcePoliciesTest {

    private val mib = 1024L * 1024

    @Test fun theThumbnailCacheIsTwoPercentOfTheFreeSpaceWithinBounds() {
        assertEquals(ThumbnailCachePolicy.MIN_BYTES, ThumbnailCachePolicy.diskCacheBytes(0L))
        assertEquals(ThumbnailCachePolicy.MIN_BYTES, ThumbnailCachePolicy.diskCacheBytes(-1L))
        // 1 GiB free: 2% would be 20 MiB, the floor keeps 32 MiB.
        assertEquals(32 * mib, ThumbnailCachePolicy.diskCacheBytes(1024 * mib))
        // 5 GiB free: 102.4 MiB.
        assertEquals(5 * 1024 * mib / 50, ThumbnailCachePolicy.diskCacheBytes(5 * 1024 * mib))
        // 64 GiB free: Glide's usual 250 MiB, no more.
        assertEquals(250 * mib, ThumbnailCachePolicy.diskCacheBytes(64 * 1024 * mib))
    }

    @Suppress("DEPRECATION")
    @Test fun theLibraryScanIsDroppedInTheBackgroundOrWhenMemoryRunsLow() {
        for (level in listOf(ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW, ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL,
            ComponentCallbacks2.TRIM_MEMORY_BACKGROUND, ComponentCallbacks2.TRIM_MEMORY_MODERATE, ComponentCallbacks2.TRIM_MEMORY_COMPLETE)) {
            assertTrue("level $level", MemoryTrimPolicy.releasesCaches(level))
        }
        for (level in listOf(ComponentCallbacks2.TRIM_MEMORY_RUNNING_MODERATE, ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN)) {
            assertFalse("level $level", MemoryTrimPolicy.releasesCaches(level))
        }
    }
}
