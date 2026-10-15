package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.content.Context
import kotlin.random.Random

internal enum class SlideshowOrder { SEQUENTIAL, RANDOM }

/** How one slide gives way to the next: at once, through black, or as a swipe. */
internal enum class SlideshowTransition { NONE, FADE, SLIDE }

/** The choices of the slideshow dialog (ROADMAP 9.1); remembered for the next show. */
internal data class SlideshowSettings(
    val intervalSeconds: Int = DEFAULT_INTERVAL_SECONDS,
    val order: SlideshowOrder = SlideshowOrder.SEQUENTIAL,
    /** Backwards through the album; only for [SlideshowOrder.SEQUENTIAL]. */
    val reverse: Boolean = false,
    val loop: Boolean = false,
    val includeGifs: Boolean = true,
    val transition: SlideshowTransition = SlideshowTransition.SLIDE,
) {
    init {
        require(intervalSeconds in MIN_INTERVAL_SECONDS..MAX_INTERVAL_SECONDS) { "Interval out of range: $intervalSeconds" }
    }

    companion object {
        const val MIN_INTERVAL_SECONDS = 1
        const val MAX_INTERVAL_SECONDS = 60
        const val DEFAULT_INTERVAL_SECONDS = 5
    }
}

/**
 * Which page a slideshow shows next. It starts on the page the viewer shows; pages for which [eligible] is false
 * (animated GIFs when they are left out) are skipped. A random show goes through every eligible page once in a
 * shuffled order, and when it loops it reshuffles without showing the last page twice in a row. [next] answers null
 * at the end of a show that does not loop, or when no other page is eligible.
 */
internal class SlideshowPlan(
    private val pageCount: Int,
    start: Int,
    private val eligible: (Int) -> Boolean,
    private val settings: SlideshowSettings,
    private val random: Random = Random.Default,
) {
    var current: Int = start
        private set

    private val queue = ArrayDeque<Int>()

    init {
        require(pageCount > 0 && start in 0 until pageCount) { "Start $start outside $pageCount pages" }
        if (settings.order == SlideshowOrder.RANDOM) refill()
    }

    /** True when the show has anything to move on to. */
    val hasNext: Boolean
        get() = (0 until pageCount).any { it != current && eligible(it) }

    /** The page the user moved to by hand; the show continues from there. */
    fun moveTo(index: Int) {
        if (index !in 0 until pageCount || index == current) return
        current = index
        if (settings.order == SlideshowOrder.RANDOM) queue.remove(index)
    }

    fun next(): Int? {
        val next = when (settings.order) {
            SlideshowOrder.SEQUENTIAL -> nextInOrder()
            SlideshowOrder.RANDOM -> nextRandom()
        } ?: return null
        current = next
        return next
    }

    private fun nextInOrder(): Int? {
        val step = if (settings.reverse) -1 else 1
        for (offset in 1 until pageCount + 1) {
            var index = current + step * offset
            if (index !in 0 until pageCount) {
                if (!settings.loop) return null
                index = Math.floorMod(index, pageCount)
            }
            if (index != current && eligible(index)) return index
        }
        return null
    }

    private fun nextRandom(): Int? {
        if (queue.isEmpty()) {
            if (!settings.loop) return null
            refill()
        }
        return queue.removeFirstOrNull()
    }

    private fun refill() {
        queue.clear()
        queue.addAll((0 until pageCount).filter { it != current && eligible(it) }.shuffled(random))
    }
}

/** The remembered slideshow choices (their own preferences file, apart from the app settings). */
internal class SlideshowSettingsStore(context: Context) {

    private val preferences = context.applicationContext.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)

    fun load(): SlideshowSettings = runCatching {
        SlideshowSettings(
            intervalSeconds = preferences.getInt(KEY_INTERVAL, SlideshowSettings.DEFAULT_INTERVAL_SECONDS)
                .coerceIn(SlideshowSettings.MIN_INTERVAL_SECONDS, SlideshowSettings.MAX_INTERVAL_SECONDS),
            order = preferences.getString(KEY_ORDER, null)?.let { runCatching { SlideshowOrder.valueOf(it) }.getOrNull() } ?: SlideshowOrder.SEQUENTIAL,
            reverse = preferences.getBoolean(KEY_REVERSE, false),
            loop = preferences.getBoolean(KEY_LOOP, false),
            includeGifs = preferences.getBoolean(KEY_INCLUDE_GIFS, true),
            transition = preferences.getString(KEY_TRANSITION, null)?.let { runCatching { SlideshowTransition.valueOf(it) }.getOrNull() }
                ?: SlideshowTransition.SLIDE,
        )
    }.getOrDefault(SlideshowSettings())

    fun save(settings: SlideshowSettings) {
        preferences.edit()
            .putInt(KEY_INTERVAL, settings.intervalSeconds)
            .putString(KEY_ORDER, settings.order.name)
            .putBoolean(KEY_REVERSE, settings.reverse)
            .putBoolean(KEY_LOOP, settings.loop)
            .putBoolean(KEY_INCLUDE_GIFS, settings.includeGifs)
            .putString(KEY_TRANSITION, settings.transition.name)
            .apply()
    }

    private companion object {
        const val PREFERENCES = "slideshow"
        const val KEY_INTERVAL = "interval-seconds"
        const val KEY_ORDER = "order"
        const val KEY_REVERSE = "reverse"
        const val KEY_LOOP = "loop"
        const val KEY_INCLUDE_GIFS = "include-gifs"
        const val KEY_TRANSITION = "transition"
    }
}
