package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.bumptech.glide.Glide
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.ViewerPageSurface
import kotlinx.coroutines.Job

/**
 * One composed page of the viewer pager: its zoomable surface, its decode job and the resources behind it. The
 * Activity owns the loading pipeline; this class only holds per-page state so the pager can keep the current page
 * and both neighbours alive while every other page is released (ROADMAP 6.3).
 */
internal class ViewerPageHost(
    val index: Int,
    val page: ImageViewerPage,
    override val view: ZoomableImageView,
    onAnimation: (ImageAnimationPlayback) -> Unit,
) : ViewerPageSurface {

    override var loading: Boolean by mutableStateOf(true)
    override var errorText: String? by mutableStateOf(null)

    val animation = ImageAnimationController(onAnimation)
    var job: Job? = null
    var generation = 0
    var tiledImage: TiledImageDrawable? = null

    /** Null until the metadata read finished; [metadataRead] tells "not yet" and "unavailable" apart. */
    var metadata: ImageMetadata? = null
    var metadataRead = false

    /** True once a drawable is installed, which is when printing becomes possible. */
    var ready = false

    /** A tiled page decodes only while it is current, so one tile cache budget is live at a time. */
    var tiledDeferred = false

    fun releaseTiledImage() {
        val drawable = tiledImage ?: return
        tiledImage = null
        if (view.drawable === drawable) view.setImageDrawable(null)
        drawable.close()
    }

    fun release(context: Context) {
        job?.cancel()
        job = null
        generation += 1
        animation.clear()
        releaseTiledImage()
        Glide.with(context.applicationContext).clear(view)
        view.setImageDrawable(null)
        view.onPageSwipe = null
        view.onZoomInteraction = null
        view.setOnClickListener(null)
    }
}
