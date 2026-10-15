package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.content.ComponentCallbacks2

/**
 * How large the thumbnail disk cache may grow (ROADMAP 9.4): 2% of the free space of the cache volume, between
 * 32 MiB and Glide's default of 250 MiB, so a nearly full phone does not give a fifth of what is left to thumbnails
 * while a roomy one keeps Glide's usual cache. Applied when Glide starts (`MapleGlideModule`).
 */
internal object ThumbnailCachePolicy {
    const val MIN_BYTES = 32L * 1024 * 1024
    const val MAX_BYTES = 250L * 1024 * 1024
    private const val SHARE_DIVISOR = 50

    fun diskCacheBytes(usableBytes: Long): Long = (usableBytes.coerceAtLeast(0L) / SHARE_DIVISOR).coerceIn(MIN_BYTES, MAX_BYTES)
}

/**
 * Which `onTrimMemory` levels drop the gallery's in-memory library scan (ROADMAP 9.4): leaving for the background
 * list, and running low while in front (delivered before Android 14). Hiding the UI alone keeps it, since a quick
 * return would rescan for nothing. Glide trims its own memory cache, the viewer its image tiles.
 */
internal object MemoryTrimPolicy {
    fun releasesCaches(level: Int): Boolean =
        level >= ComponentCallbacks2.TRIM_MEMORY_BACKGROUND ||
            (level >= ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW && level < ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN)
}
