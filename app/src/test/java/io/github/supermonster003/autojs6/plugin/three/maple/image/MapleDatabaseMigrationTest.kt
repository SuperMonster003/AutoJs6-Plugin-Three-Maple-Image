package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.io.File

/**
 * ROADMAP 7.4: version 2 adds `folder_marks`; the 3.1.0 favorites and recycle bin rows survive the upgrade. The
 * version 1 database is built from the exported schema (`app/schemas`), then Room opens it, runs the automatic
 * migration and checks the resulting tables against version 2 itself.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class MapleDatabaseMigrationTest {

    private val context get() = RuntimeEnvironment.getApplication()

    @After
    fun forget() {
        MapleDatabase.replaceForTesting(null)
        context.deleteDatabase(NAME)
    }

    @Test
    fun version1Becomes2WithItsRows() = runBlocking {
        createVersion1()
        val database = Room.databaseBuilder(context, MapleDatabase::class.java, NAME).allowMainThreadQueries().build()
        try {
            assertEquals(listOf(7L), database.favorites().all().map { it.mediaId })
            assertEquals(listOf("a.jpg"), database.recycleBin().all().map { it.displayName })
            database.folderMarks().upsert(listOf(FolderMarkPolicy.entity(AlbumPath(MediaCatalog.PRIMARY_VOLUME, "Pictures/"), FolderMark.PINNED, 1)))
            assertEquals(listOf("pinned"), database.folderMarks().all().map { it.mark })
            assertEquals(2, database.openHelper.readableDatabase.version)
        } finally {
            database.close()
        }
    }

    private fun createVersion1() {
        val schema = JSONObject(File(SCHEMAS, "1.json").readText()).getJSONObject("database")
        val file = context.getDatabasePath(NAME).apply { parentFile?.mkdirs() }
        SQLiteDatabase.openOrCreateDatabase(file, null).use { db ->
            val entities = schema.getJSONArray("entities")
            for (i in 0 until entities.length()) {
                val entity = entities.getJSONObject(i)
                val table = entity.getString("tableName")
                db.execSQL(entity.getString("createSql").replace("\${TABLE_NAME}", table))
                val indices = entity.optJSONArray("indices") ?: continue
                for (j in 0 until indices.length()) db.execSQL(indices.getJSONObject(j).getString("createSql").replace("\${TABLE_NAME}", table))
            }
            val setup = schema.getJSONArray("setupQueries")
            for (i in 0 until setup.length()) db.execSQL(setup.getString(i))
            db.execSQL(
                "INSERT INTO favorites (media_id, fingerprint, relative_path, display_name, added_at_millis) " +
                    "VALUES (7, '10:1:1x1', 'Pictures/', 'f7.jpg', 5)",
            )
            db.execSQL(
                "INSERT INTO recycle_bin (stored_name, original_path, display_name, mime_type, size_bytes, date_modified_seconds, date_taken_millis, deleted_at_millis) " +
                    "VALUES ('1-a.jpg', '/storage/emulated/0/Pictures/a.jpg', 'a.jpg', 'image/jpeg', 10, 1, NULL, 100)",
            )
            db.version = 1
        }
    }

    private companion object {
        const val NAME = "migration-test.db"

        /** Gradle runs unit tests in the module directory. */
        val SCHEMAS = File("schemas/io.github.supermonster003.autojs6.plugin.three.maple.image.MapleDatabase")
    }
}
