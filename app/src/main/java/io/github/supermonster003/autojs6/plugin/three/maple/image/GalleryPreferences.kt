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

    /** Settings > Hidden folders: list the `.nomedia` folders at the end of the album grid (ROADMAP 7.4). */
    var showHiddenFolders: Boolean
        get() = preferences.getBoolean(KEY_SHOW_HIDDEN, false)
        set(value) = preferences.edit().putBoolean(KEY_SHOW_HIDDEN, value).apply()

    /** The media grid's portrait columns (the menu and the pinch); [grid] may give landscape its own. */
    var mediaColumns: Int
        get() = clampColumns(preferences.getInt(KEY_COLUMNS, DEFAULT_COLUMNS))
        set(value) = preferences.edit().putInt(KEY_COLUMNS, clampColumns(value)).apply()

    /** Album and media grid settings (ROADMAP 9.2). */
    var grid: GridSettings
        get() = GridSettings.read(preferences)
        set(value) = GridSettings.write(preferences.edit(), value).apply()

    /** An album's own cover (ROADMAP 9.2), by media id; null uses the cover order. */
    fun customCover(path: AlbumPath): Long? =
        coverKey(path).let { key -> if (preferences.contains(key)) preferences.getLong(key, 0L) else null }

    fun setCustomCover(path: AlbumPath, mediaId: Long?) {
        preferences.edit().apply { if (mediaId == null) remove(coverKey(path)) else putLong(coverKey(path), mediaId) }.apply()
    }

    private fun coverKey(path: AlbumPath): String = "$KEY_COVER_PREFIX${MediaCatalog.normalizedVolume(path.volume)}:${path.relativePath}"

    /** Sort used by media grids that have no memory of their own; updated whenever the user picks a sort. */
    var defaultSort: MediaSort
        get() = decodeSort(preferences.getString(KEY_DEFAULT_SORT, null)) ?: MediaSort.DEFAULT
        set(value) = preferences.edit().putString(KEY_DEFAULT_SORT, encodeSort(value)).apply()

    fun sortFor(kind: AlbumKind, id: Long): MediaSort? = decodeSort(preferences.getString(sortKey(kind, id), null))

    /** Grouping used by media grids without a memory of their own (ROADMAP 7.5). */
    var defaultGrouping: MediaGrouping
        get() = decodeGrouping(preferences.getString(KEY_DEFAULT_GROUPING, null)) ?: MediaGrouping.NONE
        set(value) = preferences.edit().putString(KEY_DEFAULT_GROUPING, value.name).apply()

    fun groupingFor(kind: AlbumKind, id: Long): MediaGrouping? = decodeGrouping(preferences.getString(groupingKey(kind, id), null))

    /**
     * The sort dialog (ROADMAP 7.5): "this album only" keeps [sort] and [grouping] for that album; otherwise they
     * become the default for every album without a choice of its own, and this album drops its own.
     */
    fun applySort(kind: AlbumKind, id: Long, sort: MediaSort, grouping: MediaGrouping, thisAlbumOnly: Boolean) {
        val edit = preferences.edit()
        if (thisAlbumOnly) {
            edit.putString(sortKey(kind, id), encodeSort(sort)).putString(groupingKey(kind, id), grouping.name)
        } else {
            edit.remove(sortKey(kind, id)).remove(groupingKey(kind, id))
                .putString(KEY_DEFAULT_SORT, encodeSort(sort))
                .putString(KEY_DEFAULT_GROUPING, grouping.name)
        }
        edit.apply()
    }

    /** Whether [kind] / [id] keeps a sort of its own ("this album only" was ticked). */
    fun hasOwnSort(kind: AlbumKind, id: Long): Boolean = preferences.contains(sortKey(kind, id))

    /** The album grid order. */
    var albumSort: MediaSort
        get() = decodeSort(preferences.getString(KEY_ALBUM_SORT, null)) ?: MediaSort.DEFAULT
        set(value) = preferences.edit().putString(KEY_ALBUM_SORT, encodeSort(value)).apply()

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
        private const val KEY_COVER_PREFIX = "album_cover:"
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
        private const val KEY_SHOW_HIDDEN = "show_hidden_folders"
        private const val KEY_DEFAULT_GROUPING = "default_grouping"
        private const val KEY_ALBUM_SORT = "album_sort"

        fun decodeGrouping(value: String?): MediaGrouping? = MediaGrouping.entries.firstOrNull { it.name == value }

        private fun groupingKey(kind: AlbumKind, id: Long): String = "grouping." + kind.name.lowercase() + "." + id

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
