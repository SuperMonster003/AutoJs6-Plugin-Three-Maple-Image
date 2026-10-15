package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.content.Context
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory
import com.bumptech.glide.module.AppGlideModule

/**
 * Glide for the whole app (ROADMAP 9.4): the thumbnail disk cache is sized by the free space of the cache volume
 * ([ThumbnailCachePolicy]); the memory cache keeps Glide's size for the device and Glide trims it on `onTrimMemory`.
 */
@GlideModule
class MapleGlideModule : AppGlideModule() {
    override fun applyOptions(context: Context, builder: GlideBuilder) {
        builder.setDiskCache(InternalCacheDiskCacheFactory(context, ThumbnailCachePolicy.diskCacheBytes(context.cacheDir.usableSpace)))
    }

    override fun isManifestParsingEnabled(): Boolean = false
}
