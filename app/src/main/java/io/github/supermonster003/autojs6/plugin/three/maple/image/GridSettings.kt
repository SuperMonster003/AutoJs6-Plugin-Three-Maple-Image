package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.content.SharedPreferences

/** Corners of the media grid's thumbnails (ROADMAP 9.2). */
internal enum class ThumbnailCorners(val dp: Int) { NONE(0), SMALL(6), LARGE(12) }

/** The gap between the media grid's thumbnails (ROADMAP 9.2). */
internal enum class ThumbnailSpacing(val dp: Int) { NONE(0), NORMAL(2), WIDE(6) }

/** Which image covers an album without a cover of its own (ROADMAP 9.2). */
internal enum class AlbumCoverOrder { NEWEST, OLDEST }

/**
 * Album and media grid settings (ROADMAP 9.2). The defaults are the 3.0.0 grids: album columns by window width,
 * the media columns of the menu and pinch in both orientations, square crops 2 dp apart without corners, type
 * badges, image counts, the newest image as the cover, one-line album names and animated GIF thumbnails. The
 * portrait media columns stay in [GalleryPreferences.mediaColumns], where the menu and the pinch keep them.
 */
internal data class GridSettings(
    /** Null: the same as portrait. */
    val mediaColumnsLandscape: Int? = null,
    /** Null: by window width (2 / 3 / 4). */
    val albumColumnsPortrait: Int? = null,
    val albumColumnsLandscape: Int? = null,
    val corners: ThumbnailCorners = ThumbnailCorners.NONE,
    val spacing: ThumbnailSpacing = ThumbnailSpacing.NORMAL,
    /** Off: the whole image fits inside its square cell. */
    val cropSquare: Boolean = true,
    val typeBadges: Boolean = true,
    val albumCounts: Boolean = true,
    val albumCover: AlbumCoverOrder = AlbumCoverOrder.NEWEST,
    val singleLineAlbumNames: Boolean = true,
    val animatedThumbnails: Boolean = true,
) {
    internal companion object {
        private const val KEY_MEDIA_COLUMNS_LANDSCAPE = "grid-media-columns-landscape"
        private const val KEY_ALBUM_COLUMNS_PORTRAIT = "grid-album-columns-portrait"
        private const val KEY_ALBUM_COLUMNS_LANDSCAPE = "grid-album-columns-landscape"
        private const val KEY_CORNERS = "grid-corners"
        private const val KEY_SPACING = "grid-spacing"
        private const val KEY_CROP_SQUARE = "grid-crop-square"
        private const val KEY_TYPE_BADGES = "grid-type-badges"
        private const val KEY_ALBUM_COUNTS = "grid-album-counts"
        private const val KEY_ALBUM_COVER = "grid-album-cover"
        private const val KEY_SINGLE_LINE_NAMES = "grid-single-line-album-names"
        private const val KEY_ANIMATED_THUMBNAILS = "grid-animated-thumbnails"

        fun read(preferences: SharedPreferences): GridSettings {
            val defaults = GridSettings()
            return GridSettings(
                mediaColumnsLandscape = preferences.optionalInt(KEY_MEDIA_COLUMNS_LANDSCAPE)?.let(GridPolicy::clampMediaColumns),
                albumColumnsPortrait = preferences.optionalInt(KEY_ALBUM_COLUMNS_PORTRAIT)?.let(GridPolicy::clampAlbumColumns),
                albumColumnsLandscape = preferences.optionalInt(KEY_ALBUM_COLUMNS_LANDSCAPE)?.let(GridPolicy::clampAlbumColumns),
                corners = ViewerSettings.enumValue(preferences.getString(KEY_CORNERS, null), defaults.corners),
                spacing = ViewerSettings.enumValue(preferences.getString(KEY_SPACING, null), defaults.spacing),
                cropSquare = preferences.getBoolean(KEY_CROP_SQUARE, defaults.cropSquare),
                typeBadges = preferences.getBoolean(KEY_TYPE_BADGES, defaults.typeBadges),
                albumCounts = preferences.getBoolean(KEY_ALBUM_COUNTS, defaults.albumCounts),
                albumCover = ViewerSettings.enumValue(preferences.getString(KEY_ALBUM_COVER, null), defaults.albumCover),
                singleLineAlbumNames = preferences.getBoolean(KEY_SINGLE_LINE_NAMES, defaults.singleLineAlbumNames),
                animatedThumbnails = preferences.getBoolean(KEY_ANIMATED_THUMBNAILS, defaults.animatedThumbnails),
            )
        }

        fun write(editor: SharedPreferences.Editor, settings: GridSettings): SharedPreferences.Editor = editor
            .putOptionalInt(KEY_MEDIA_COLUMNS_LANDSCAPE, settings.mediaColumnsLandscape)
            .putOptionalInt(KEY_ALBUM_COLUMNS_PORTRAIT, settings.albumColumnsPortrait)
            .putOptionalInt(KEY_ALBUM_COLUMNS_LANDSCAPE, settings.albumColumnsLandscape)
            .putString(KEY_CORNERS, settings.corners.name)
            .putString(KEY_SPACING, settings.spacing.name)
            .putBoolean(KEY_CROP_SQUARE, settings.cropSquare)
            .putBoolean(KEY_TYPE_BADGES, settings.typeBadges)
            .putBoolean(KEY_ALBUM_COUNTS, settings.albumCounts)
            .putString(KEY_ALBUM_COVER, settings.albumCover.name)
            .putBoolean(KEY_SINGLE_LINE_NAMES, settings.singleLineAlbumNames)
            .putBoolean(KEY_ANIMATED_THUMBNAILS, settings.animatedThumbnails)

        private fun SharedPreferences.optionalInt(key: String): Int? = if (contains(key)) getInt(key, 0) else null

        private fun SharedPreferences.Editor.putOptionalInt(key: String, value: Int?): SharedPreferences.Editor =
            if (value == null) remove(key) else putInt(key, value)
    }
}

/** The Android-free decisions of the grid settings (ROADMAP 9.2). */
internal object GridPolicy {

    const val MIN_ALBUM_COLUMNS = 2
    const val MAX_ALBUM_COLUMNS = 5

    fun clampMediaColumns(value: Int): Int = GalleryPreferences.clampColumns(value)

    fun clampAlbumColumns(value: Int): Int = value.coerceIn(MIN_ALBUM_COLUMNS, MAX_ALBUM_COLUMNS)

    fun mediaColumns(portrait: Int, settings: GridSettings, landscape: Boolean): Int =
        clampMediaColumns(if (landscape) settings.mediaColumnsLandscape ?: portrait else portrait)

    /** A chosen count, else 2 on phones, 3 on wide phones and small tablets, 4 on large tablets (design draft 8.1). */
    fun albumColumns(settings: GridSettings, landscape: Boolean, widthDp: Int): Int =
        (if (landscape) settings.albumColumnsLandscape else settings.albumColumnsPortrait)?.let(::clampAlbumColumns)
            ?: when {
                widthDp >= 840 -> 4
                widthDp >= 600 -> 3
                else -> 2
            }

    /** The cover: the album's own choice while that image is still in it, else the newest or the oldest. */
    fun cover(items: List<MediaItem>, order: AlbumCoverOrder, chosenId: Long?): MediaItem? {
        if (items.isEmpty()) return null
        chosenId?.let { id -> items.firstOrNull { it.id == id }?.let { return it } }
        return when (order) {
            AlbumCoverOrder.NEWEST -> items.maxWith(MediaCatalog.coverOrder)
            AlbumCoverOrder.OLDEST -> items.minWith(MediaCatalog.coverOrder)
        }
    }
}
