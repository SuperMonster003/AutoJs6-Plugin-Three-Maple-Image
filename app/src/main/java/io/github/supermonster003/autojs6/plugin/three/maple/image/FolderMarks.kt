package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** What a folder mark means (ROADMAP 7.4); [key] is the stored value. */
internal enum class FolderMark(val key: String) {
    /** Left out of the whole gallery: album grid, all images, favorites and search (an app-local choice). */
    EXCLUDED("excluded"),

    /** Shown in the top section of the album grid. */
    PINNED("pinned"),

    /** Hidden by a `.nomedia` file the gallery wrote; remembered so "show hidden folders" can list it. */
    HIDDEN("hidden"),
}

/** The marks in effect, by [MediaCatalog.folderKey]. Android-free so the gallery rules stay JVM-testable. */
internal data class FolderMarkState(
    val excluded: Set<String> = emptySet(),
    val pinned: Set<String> = emptySet(),
    val hidden: Set<String> = emptySet(),
) {
    fun isExcluded(path: AlbumPath): Boolean = excluded.isNotEmpty() && MediaCatalog.folderKey(path) in excluded

    fun isPinned(path: AlbumPath): Boolean = pinned.isNotEmpty() && MediaCatalog.folderKey(path) in pinned

    /** [items] without the ones in excluded folders (only that folder: its subfolders are albums of their own). */
    fun visible(items: List<MediaItem>): List<MediaItem> = if (excluded.isEmpty()) items else items.filter { !isExcluded(it.albumPath) }

    companion object {
        val NONE = FolderMarkState()

        fun of(entities: List<FolderMarkEntity>): FolderMarkState {
            val byMark = entities.groupBy { it.mark }
            fun keys(mark: FolderMark) = byMark[mark.key].orEmpty().mapTo(HashSet()) { MediaCatalog.folderKey(AlbumPath(it.volume, it.relativePath)) }
            return FolderMarkState(keys(FolderMark.EXCLUDED), keys(FolderMark.PINNED), keys(FolderMark.HIDDEN))
        }
    }
}

internal object FolderMarkPolicy {
    /** The album grid order with pins: the pinned albums first, both parts keeping the order they came in. */
    fun <T> pinnedFirst(albums: List<T>, isPinned: (T) -> Boolean): List<T> {
        val (pinned, others) = albums.partition(isPinned)
        return pinned + others
    }

    fun entity(path: AlbumPath, mark: FolderMark, at: Long): FolderMarkEntity =
        FolderMarkEntity(MediaCatalog.normalizedVolume(path.volume), path.relativePath, mark.key, at)
}

/** Excluded, pinned and hidden folders in the Room table `folder_marks` (ROADMAP 7.4). */
internal class FolderMarksRepository(context: Context, private val now: () -> Long = System::currentTimeMillis) {
    private val dao = MapleDatabase.get(context).folderMarks()

    fun observe(): Flow<FolderMarkState> = dao.observeAll().map(FolderMarkState::of)

    suspend fun current(): FolderMarkState = FolderMarkState.of(dao.all())

    /** Every mark of one kind, newest first (the "excluded folders" page). */
    suspend fun paths(mark: FolderMark): List<AlbumPath> = dao.all().filter { it.mark == mark.key }.map { AlbumPath(it.volume, it.relativePath) }

    suspend fun mark(paths: Collection<AlbumPath>, mark: FolderMark) {
        if (paths.isEmpty()) return
        val at = now()
        dao.upsert(paths.map { FolderMarkPolicy.entity(it, mark, at) })
    }

    suspend fun unmark(paths: Collection<AlbumPath>, mark: FolderMark) {
        for (path in paths) dao.delete(MediaCatalog.normalizedVolume(path.volume), path.relativePath, mark.key)
    }
}
