package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.content.ContentResolver
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/** Favorites matched against the library: the images in favorite order and the rows that need a new media id. */
internal data class FavoriteResolution(
    /** Favorite images, most recently added first. */
    val items: List<MediaItem>,
    /** (old media id, image) pairs whose row was re-created under a new id (a move on Android 9 or older). */
    val rebinds: List<Pair<Long, MediaItem>>,
)

/** Matches the favorites table with the library (ROADMAP T9, Q2); Android-free. */
internal object FavoritePolicy {

    /**
     * A favorite whose media id still exists keeps it. Otherwise an image that nobody claims and that has the same
     * [MediaFingerprint] takes over (one with the same name first); with no unique match the favorite stays as it
     * is, so a trashed image comes back as a favorite when it is restored under its id.
     */
    fun resolve(favorites: List<FavoriteEntity>, library: List<MediaItem>): FavoriteResolution {
        val byId = library.associateBy { it.id }
        val claimed = favorites.mapNotNullTo(HashSet()) { favorite -> favorite.mediaId.takeIf { it in byId } }
        val byFingerprint = library.filter { it.id !in claimed }.groupBy { MediaFingerprint.of(it) }
        val items = ArrayList<MediaItem>()
        val rebinds = ArrayList<Pair<Long, MediaItem>>()
        for (favorite in favorites) {
            val direct = byId[favorite.mediaId]
            if (direct != null) {
                items += direct
                continue
            }
            val candidates = byFingerprint[favorite.fingerprint].orEmpty().filter { it.id !in claimed }
            val match = candidates.filter { it.displayName == favorite.displayName }.singleOrNull() ?: candidates.singleOrNull() ?: continue
            claimed += match.id
            items += match
            rebinds += favorite.mediaId to match
        }
        return FavoriteResolution(items, rebinds)
    }

    fun entity(item: MediaItem, addedAtMillis: Long) = FavoriteEntity(
        mediaId = item.id,
        fingerprint = MediaFingerprint.of(item),
        relativePath = item.relativePath,
        displayName = item.displayName,
        addedAtMillis = addedAtMillis,
    )
}

/** The local favorites table (Room) plus the optional Android 11+ system favorite import. */
internal class FavoritesRepository(context: Context, private val now: () -> Long = System::currentTimeMillis) {
    private val appContext = context.applicationContext
    private val dao by lazy { MapleDatabase.get(appContext).favorites() }

    fun observeIds(): Flow<Set<Long>> = dao.observeMediaIds().map { it.toSet() }

    suspend fun add(items: List<MediaItem>) {
        if (items.isEmpty()) return
        // Later items get older timestamps so the batch keeps its order in "most recently added first".
        val base = now()
        dao.upsert(items.mapIndexed { index, item -> FavoritePolicy.entity(item, base - index) })
    }

    suspend fun remove(ids: Collection<Long>) {
        if (ids.isNotEmpty()) dao.delete(ids.toList())
    }

    /** Favorites resolved against [library] (the full scan); rebinds are written back. */
    suspend fun resolve(library: List<MediaItem>): List<MediaItem> {
        val resolution = withContext(Dispatchers.Default) { FavoritePolicy.resolve(dao.all(), library) }
        for ((oldId, item) in resolution.rebinds) dao.rebind(oldId, item.id, item.relativePath, item.displayName)
        return resolution.items
    }

    /** "Sync system favorites" was switched on: the images the system marks as favorite join the local table. */
    suspend fun importSystemFavorites(library: List<MediaItem>): Int {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return 0
        val ids = withContext(Dispatchers.IO) {
            val args = Bundle().apply { putString(ContentResolver.QUERY_ARG_SQL_SELECTION, "${MediaStore.MediaColumns.IS_FAVORITE} = 1") }
            val found = HashSet<Long>()
            runCatching {
                appContext.contentResolver.query(MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL), arrayOf(MediaStore.MediaColumns._ID), args, null)
                    ?.use { cursor -> while (cursor.moveToNext()) found += cursor.getLong(0) }
            }
            found
        }
        val known = dao.all().mapTo(HashSet()) { it.mediaId }
        val added = library.filter { it.id in ids && it.id !in known }
        add(added)
        return added.size
    }
}
