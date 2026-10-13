package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.app.Application
import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * One image in the recycle bin view (ROADMAP 7.2): a system-trashed MediaStore row on Android 11+ ([id] is the
 * MediaStore id, [model] its content Uri) or a private bin entry on Android 7-10 ([id] is the Room id, [model]
 * the stored file).
 */
internal data class RecycleBinEntry(
    val id: Long,
    val displayName: String,
    val mimeType: String,
    val sizeBytes: Long,
    val model: Any,
    val remainingDays: Int,
)

internal object RecycleBinNames {
    private val trashedPrefix = Regex("""^\.trashed-\d+-""")

    /** The system trash renames files to `.trashed-<expiry>-<name>`; the view shows the original name. */
    fun original(displayName: String): String = displayName.replaceFirst(trashedPrefix, "")
}

/** Lists the recycle bin: the system trash on Android 11+, the private bin before. */
internal class RecycleBinRepository(context: Context, private val now: () -> Long = System::currentTimeMillis) {
    private val appContext = context.applicationContext
    private val resolver: ContentResolver = appContext.contentResolver

    val usesSystemTrash: Boolean get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R

    suspend fun load(): List<RecycleBinEntry> = withContext(Dispatchers.IO) {
        if (usesSystemTrash) systemTrash() else privateBin()
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun systemTrash(): List<RecycleBinEntry> {
        val collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        val args = Bundle().apply {
            putInt(MediaStore.QUERY_ARG_MATCH_TRASHED, MediaStore.MATCH_ONLY)
            putStringArray(ContentResolver.QUERY_ARG_SORT_COLUMNS, arrayOf(MediaStore.MediaColumns.DATE_EXPIRES))
            putInt(ContentResolver.QUERY_ARG_SORT_DIRECTION, ContentResolver.QUERY_SORT_DIRECTION_DESCENDING)
        }
        val projection = arrayOf(
            MediaStore.MediaColumns._ID,
            MediaStore.MediaColumns.DISPLAY_NAME,
            MediaStore.MediaColumns.MIME_TYPE,
            MediaStore.MediaColumns.SIZE,
            MediaStore.MediaColumns.DATE_EXPIRES,
        )
        val entries = ArrayList<RecycleBinEntry>()
        val nowMillis = now()
        runCatching {
            resolver.query(collection, projection, args, null)?.use { cursor ->
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(0)
                    val expiresMillis = cursor.getLong(4) * 1000L
                    entries += RecycleBinEntry(
                        id = id,
                        displayName = RecycleBinNames.original(cursor.getString(1).orEmpty()),
                        mimeType = cursor.getString(2).orEmpty(),
                        sizeBytes = cursor.getLong(3),
                        model = ContentUris.withAppendedId(collection, id),
                        remainingDays = daysUntil(expiresMillis, nowMillis),
                    )
                }
            }
        }
        return entries
    }

    private suspend fun privateBin(): List<RecycleBinEntry> {
        val bin = PrivateRecycleBin(appContext, now)
        bin.purge()
        val nowMillis = now()
        return bin.entries().map { entry ->
            RecycleBinEntry(
                id = entry.id,
                displayName = entry.displayName,
                mimeType = entry.mimeType,
                sizeBytes = entry.sizeBytes,
                model = bin.storedFile(entry),
                remainingDays = RecycleBinPolicy.remainingDays(entry.deletedAtMillis, nowMillis),
            )
        }
    }

    private fun daysUntil(expiresMillis: Long, nowMillis: Long): Int {
        val left = expiresMillis - nowMillis
        return if (left <= 0) 0 else ((left + DAY_MILLIS - 1) / DAY_MILLIS).toInt()
    }

    private companion object {
        const val DAY_MILLIS = 24L * 60 * 60 * 1000
    }
}

internal data class RecycleBinUiState(
    val loading: Boolean = true,
    val entries: List<RecycleBinEntry> = emptyList(),
    val selection: Set<Long> = emptySet(),
)

/** State of the recycle bin page; the restores and deletes themselves run in [GalleryOperations]. */
internal class RecycleBinViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = RecycleBinRepository(application)
    private val _state = MutableStateFlow(RecycleBinUiState())
    private var loadJob: Job? = null

    val state: StateFlow<RecycleBinUiState> = _state.asStateFlow()

    fun load() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _state.update { it.copy(loading = it.entries.isEmpty()) }
            val entries = repository.load()
            val ids = entries.mapTo(HashSet()) { it.id }
            _state.update { state -> state.copy(loading = false, entries = entries, selection = state.selection.filterTo(LinkedHashSet()) { it in ids }) }
        }
    }

    fun toggle(id: Long) {
        _state.update { it.copy(selection = MediaSelection.toggle(it.selection, id)) }
    }

    fun selectAll() {
        _state.update { state -> state.copy(selection = state.entries.mapTo(LinkedHashSet()) { it.id }) }
    }

    fun clearSelection() {
        _state.update { if (it.selection.isEmpty()) it else it.copy(selection = emptySet()) }
    }

    fun selected(): List<RecycleBinEntry> = _state.value.let { state -> state.entries.filter { it.id in state.selection } }
}
