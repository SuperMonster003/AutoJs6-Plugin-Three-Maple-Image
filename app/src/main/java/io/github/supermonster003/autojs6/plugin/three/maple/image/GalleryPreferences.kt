package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.content.Context
import android.content.SharedPreferences

/**
 * Gallery view preferences (ROADMAP 6.2): media grid column count, the default media sort and a per-album sort
 * memory. Separate from [ApplicationSettings] because none of this touches appearance or needs the revision that
 * recreates activities.
 */
internal class GalleryPreferences(private val preferences: SharedPreferences) {

    constructor(context: Context) : this(context.applicationContext.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE))

    var mediaColumns: Int
        get() = clampColumns(preferences.getInt(KEY_COLUMNS, DEFAULT_COLUMNS))
        set(value) = preferences.edit().putInt(KEY_COLUMNS, clampColumns(value)).apply()

    /** Sort used by media grids that have no memory of their own; updated whenever the user picks a sort. */
    var defaultSort: MediaSort
        get() = decodeSort(preferences.getString(KEY_DEFAULT_SORT, null)) ?: MediaSort.DEFAULT
        set(value) = preferences.edit().putString(KEY_DEFAULT_SORT, encodeSort(value)).apply()

    fun sortFor(kind: AlbumKind, id: Long): MediaSort? = decodeSort(preferences.getString(sortKey(kind, id), null))

    fun rememberSort(kind: AlbumKind, id: Long, sort: MediaSort) {
        preferences.edit()
            .putString(sortKey(kind, id), encodeSort(sort))
            .putString(KEY_DEFAULT_SORT, encodeSort(sort))
            .apply()
    }

    /** Move / copy targets (`Pictures/Trips/` on a volume) used lately, newest first (ROADMAP 7.3 album picker). */
    val recentTargets: List<AlbumPath>
        get() = preferences.getString(KEY_RECENT_TARGETS, null).orEmpty().split('\n').filter { it.isNotBlank() }.map(::decodeTarget)

    fun rememberTarget(path: AlbumPath) {
        val stored = recentTargets.map(::encodeTarget)
        preferences.edit().putString(KEY_RECENT_TARGETS, pushRecent(stored, encodeTarget(path)).joinToString("\n")).apply()
    }

    companion object {
        const val MIN_COLUMNS = 3
        const val MAX_COLUMNS = 6
        const val DEFAULT_COLUMNS = 4
        private const val FILE_NAME = "gallery"
        private const val KEY_COLUMNS = "media_columns"
        private const val KEY_DEFAULT_SORT = "default_sort"
        private const val KEY_RECENT_TARGETS = "recent_targets"
        const val MAX_RECENT_TARGETS = 5

        /** [path] first, without duplicates, at most [MAX_RECENT_TARGETS]. */
        fun pushRecent(recent: List<String>, path: String): List<String> = (listOf(path) + recent.filter { it != path }).take(MAX_RECENT_TARGETS)

        /** Internal storage targets stay a bare relative path (as 3.1.0 stored them); others add `volume::`. */
        fun encodeTarget(path: AlbumPath): String = if (path.isPrimary) path.relativePath else path.volume.lowercase() + VOLUME_SEPARATOR + path.relativePath

        fun decodeTarget(value: String): AlbumPath {
            val split = value.indexOf(VOLUME_SEPARATOR)
            return if (split < 0) AlbumPath(MediaCatalog.PRIMARY_VOLUME, value) else AlbumPath(value.substring(0, split), value.substring(split + VOLUME_SEPARATOR.length))
        }

        private const val VOLUME_SEPARATOR = "::"

        fun clampColumns(value: Int): Int = value.coerceIn(MIN_COLUMNS, MAX_COLUMNS)

        fun encodeSort(sort: MediaSort): String = sort.field.name + ":" + if (sort.ascending) "asc" else "desc"

        fun decodeSort(value: String?): MediaSort? {
            val (field, direction) = value?.split(':')?.takeIf { it.size == 2 } ?: return null
            val resolved = MediaSortField.entries.firstOrNull { it.name == field } ?: return null
            return when (direction) {
                "asc" -> MediaSort(resolved, ascending = true)
                "desc" -> MediaSort(resolved, ascending = false)
                else -> null
            }
        }

        private fun sortKey(kind: AlbumKind, id: Long): String = "sort." + kind.name.lowercase() + "." + id
    }
}
