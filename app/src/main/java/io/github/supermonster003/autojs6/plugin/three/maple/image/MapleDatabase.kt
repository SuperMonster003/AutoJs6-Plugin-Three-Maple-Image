package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.content.Context
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

/**
 * A favorite image of the media library (ROADMAP T8 / T9, Q2: the local table is the source of truth). The
 * MediaStore id identifies it; [fingerprint] finds it again when a move on Android 9 or older re-creates the row
 * under a new id (see [MediaFingerprint]).
 */
@Entity(tableName = "favorites", indices = [Index(value = ["media_id"], unique = true), Index(value = ["fingerprint"])])
internal data class FavoriteEntity(
    @PrimaryKey(autoGenerate = true) val rowId: Long = 0,
    @ColumnInfo(name = "media_id") val mediaId: Long,
    @ColumnInfo(name = "fingerprint") val fingerprint: String,
    @ColumnInfo(name = "relative_path") val relativePath: String,
    @ColumnInfo(name = "display_name") val displayName: String,
    @ColumnInfo(name = "added_at_millis") val addedAtMillis: Long,
)

/**
 * One image in the private recycle bin of Android 7-10 (ROADMAP T10, Q8); Android 11+ uses the system trash. The
 * file lives under `files/recycle-bin/` as [storedName]; [originalPath] is where a restore puts it back.
 */
@Entity(tableName = "recycle_bin", indices = [Index(value = ["deleted_at_millis"])])
internal data class RecycleBinEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "stored_name") val storedName: String,
    @ColumnInfo(name = "original_path") val originalPath: String,
    @ColumnInfo(name = "display_name") val displayName: String,
    @ColumnInfo(name = "mime_type") val mimeType: String,
    @ColumnInfo(name = "size_bytes") val sizeBytes: Long,
    @ColumnInfo(name = "date_modified_seconds") val dateModifiedSeconds: Long,
    @ColumnInfo(name = "date_taken_millis") val dateTakenMillis: Long?,
    @ColumnInfo(name = "deleted_at_millis") val deletedAtMillis: Long,
)

@Dao
internal interface FavoriteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(favorites: List<FavoriteEntity>)

    @Query("DELETE FROM favorites WHERE media_id IN (:mediaIds)")
    suspend fun delete(mediaIds: List<Long>)

    @Query("SELECT * FROM favorites ORDER BY added_at_millis DESC")
    suspend fun all(): List<FavoriteEntity>

    @Query("SELECT media_id FROM favorites")
    fun observeMediaIds(): Flow<List<Long>>

    @Query("UPDATE favorites SET media_id = :newMediaId, relative_path = :relativePath, display_name = :displayName WHERE media_id = :oldMediaId")
    suspend fun rebind(oldMediaId: Long, newMediaId: Long, relativePath: String, displayName: String)
}

@Dao
internal interface RecycleBinDao {
    @Insert
    suspend fun insert(entry: RecycleBinEntity): Long

    @Query("SELECT * FROM recycle_bin ORDER BY deleted_at_millis DESC")
    suspend fun all(): List<RecycleBinEntity>

    @Query("SELECT * FROM recycle_bin ORDER BY deleted_at_millis DESC")
    fun observeAll(): Flow<List<RecycleBinEntity>>

    @Query("DELETE FROM recycle_bin WHERE id IN (:ids)")
    suspend fun delete(ids: List<Long>)
}

@Database(entities = [FavoriteEntity::class, RecycleBinEntity::class], version = 1, exportSchema = true)
internal abstract class MapleDatabase : RoomDatabase() {
    abstract fun favorites(): FavoriteDao
    abstract fun recycleBin(): RecycleBinDao

    companion object {
        private const val NAME = "maple.db"

        @Volatile
        private var instance: MapleDatabase? = null

        fun get(context: Context): MapleDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(context.applicationContext, MapleDatabase::class.java, NAME).build().also { instance = it }
        }

        /** Lets tests use an in-memory database. */
        internal fun replaceForTesting(database: MapleDatabase?) {
            instance = database
        }
    }
}

/**
 * Identifies an image across a re-created MediaStore row (a move on Android 9 or older copies and deletes): size,
 * modification time and pixel size survive a byte-for-byte move, the path and name do not have to.
 */
internal object MediaFingerprint {
    fun of(item: MediaItem): String = "${item.sizeBytes}:${item.dateModifiedSeconds}:${item.width}x${item.height}"
}
