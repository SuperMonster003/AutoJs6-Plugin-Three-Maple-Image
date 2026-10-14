package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.app.Application
import android.content.ContentValues
import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.resume

/**
 * New images in the media library for a batch conversion (ROADMAP 7.6): next to the source when its album takes new
 * images, otherwise in `Pictures/<folder>/` on the internal storage. Android 10+ inserts a pending row and publishes it
 * once the image reads back; Android 7-9 writes a hidden temporary file and renames it. A failed image leaves nothing.
 */
internal class MediaOutputWriter(private val context: Context) {

    private val resolver get() = context.contentResolver
    private val media = MediaStoreRepository(context)
    private val scanned = ArrayList<String>()

    /** The source album, or `Pictures/<its folder name>/` when Android does not let the app add images there. */
    fun targetFor(item: MediaItem): AlbumPath {
        val source = item.albumPath
        if (MediaWriteCoordinator.isWritableAlbum(source)) return source
        val folder = source.relativePath.trimEnd('/').substringAfterLast('/').ifEmpty { item.bucketName }.ifEmpty { FALLBACK_FOLDER }
        return AlbumPath(MediaCatalog.PRIMARY_VOLUME, "${Environment.DIRECTORY_PICTURES}/$folder/")
    }

    suspend fun namesIn(path: AlbumPath): List<String> = MediaWriteCoordinator.namesIn(media, path)

    /**
     * Creates [name] in [target] with what [write] puts into the descriptor it opens (the conversion), checks that it
     * reads back as the [ConvertedImage] that [write] returns, and publishes it. Throws [BatchItemException].
     */
    fun create(target: AlbumPath, name: String, dateTakenMillis: Long?, write: (open: () -> ParcelFileDescriptor) -> ConvertedImage) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) createRow(target, name, dateTakenMillis, write) else createFile(target, name, write)
    }

    private fun createRow(target: AlbumPath, name: String, dateTakenMillis: Long?, write: (open: () -> ParcelFileDescriptor) -> ConvertedImage) {
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, mimeTypeOf(name))
            put(MediaStore.MediaColumns.RELATIVE_PATH, target.relativePath)
            dateTakenMillis?.let { put(MediaStore.MediaColumns.DATE_TAKEN, it) }
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        }
        val volume = if (target.isPrimary) MediaStore.VOLUME_EXTERNAL_PRIMARY else target.volume
        val row = runCatching { resolver.insert(MediaStore.Images.Media.getContentUri(volume), values) }.getOrNull()
            ?: throw BatchItemException(BatchFailure.WRITE_FAILED)
        try {
            val converted = write { resolver.openFileDescriptor(row, "w") ?: throw IOException("The new image cannot be opened") }
            BatchImageConverter.verify(resolver.openFileDescriptor(row, "r") ?: throw IOException("The new image cannot be read back"), converted)
            val published = resolver.update(row, ContentValues().apply { put(MediaStore.MediaColumns.IS_PENDING, 0) }, null, null) > 0
            if (!published) throw BatchItemException(BatchFailure.WRITE_FAILED)
        } catch (error: Throwable) {
            runCatching { resolver.delete(row, null, null) }
            throw error.asItemFailure()
        }
    }

    @Suppress("DEPRECATION")
    private fun createFile(target: AlbumPath, name: String, write: (open: () -> ParcelFileDescriptor) -> ConvertedImage) {
        val directory = File(Environment.getExternalStorageDirectory(), target.relativePath)
        if (!directory.isDirectory && !directory.mkdirs()) throw BatchItemException(BatchFailure.WRITE_FAILED)
        val file = File(directory, NameConflictPolicy.uniqueName(name, directory.list()?.asList().orEmpty()))
        val temporary = File(directory, ".${UUID.randomUUID()}.pending")
        try {
            val converted = write {
                ParcelFileDescriptor.open(temporary, ParcelFileDescriptor.MODE_WRITE_ONLY or ParcelFileDescriptor.MODE_CREATE or ParcelFileDescriptor.MODE_TRUNCATE)
            }
            BatchImageConverter.verify(ParcelFileDescriptor.open(temporary, ParcelFileDescriptor.MODE_READ_ONLY), converted)
            if (file.exists() || !temporary.renameTo(file)) throw BatchItemException(BatchFailure.WRITE_FAILED)
        } catch (error: Throwable) {
            temporary.delete()
            throw error.asItemFailure()
        }
        scanned += file.absolutePath
    }

    /** Android 7-9: lets the media scanner add the new files (Android 10+ rows are published as they are written). */
    suspend fun finish() {
        if (scanned.isEmpty()) return
        val paths = scanned.toTypedArray()
        scanned.clear()
        suspendCancellableCoroutine { continuation ->
            val remaining = AtomicInteger(paths.size)
            MediaScannerConnection.scanFile(context, paths, null) { _, _ ->
                if (remaining.decrementAndGet() == 0 && continuation.isActive) continuation.resume(Unit)
            }
        }
    }

    private fun Throwable.asItemFailure(): Throwable = when (this) {
        is BatchItemException, is CancellationException, is Error -> this
        else -> BatchItemException(BatchFailure.WRITE_FAILED, this)
    }

    private fun mimeTypeOf(name: String): String =
        ImageOutputFormat.entries.firstOrNull { it.extension == MediaFileNamePolicy.extension(name) }?.mimeType ?: "image/*"

    private companion object {
        const val FALLBACK_FOLDER = "Converted"
    }
}

internal data class BatchFailureEntry(val item: MediaItem, val failure: BatchFailure)

/** A running or finished gallery batch; [runId] tells one run from the next. */
internal data class BatchProgress(
    val runId: Int,
    val total: Int,
    val done: Int = 0,
    val succeeded: Int = 0,
    val failures: List<BatchFailureEntry> = emptyList(),
    val running: Boolean = true,
    val cancelling: Boolean = false,
    val cancelled: Boolean = false,
)

/**
 * The gallery's batch conversion (ROADMAP 7.6, T12): the selection waiting on the settings page, the settings (kept
 * for the next batch of this session), and the run itself, one image after the other in the view model's scope so a
 * rotation does not stop it. "Cancel" stops before the next image; each image is written whole or not at all.
 */
internal class BatchConversionViewModel(application: Application) : AndroidViewModel(application) {

    private val _settings = MutableStateFlow(BatchConversionSettings())
    val settings: StateFlow<BatchConversionSettings> = _settings.asStateFlow()

    private val _pending = MutableStateFlow<List<MediaItem>>(emptyList())

    /** The images the settings page will convert. */
    val pending: StateFlow<List<MediaItem>> = _pending.asStateFlow()

    private val _progress = MutableStateFlow<BatchProgress?>(null)
    val progress: StateFlow<BatchProgress?> = _progress.asStateFlow()

    @Volatile
    private var cancelRequested = false
    private var job: Job? = null
    private var runs = 0
    private var reportedRun = 0

    val running: Boolean get() = job?.isActive == true

    fun prepare(items: List<MediaItem>) {
        _pending.value = items
    }

    fun update(value: BatchConversionSettings) {
        _settings.value = value
    }

    /** Converts the pending images with the current settings; false when a batch is running or nothing is pending. */
    fun start(): Boolean {
        val items = _pending.value
        if (running || items.isEmpty() || BatchConversionPolicy.settingsError(_settings.value) != null) return false
        _pending.value = emptyList()
        val settings = _settings.value
        val runId = ++runs
        cancelRequested = false
        _progress.value = BatchProgress(runId = runId, total = items.size)
        job = viewModelScope.launch { run(runId, items, settings) }
        return true
    }

    fun cancel() {
        if (!running) return
        cancelRequested = true
        _progress.update { it?.copy(cancelling = true) }
    }

    /** The finished run, once: the caller reports it (snackbar) and refreshes the gallery. */
    fun takeFinished(): BatchProgress? {
        val finished = _progress.value?.takeIf { !it.running && it.runId != reportedRun } ?: return null
        reportedRun = finished.runId
        return finished
    }

    private suspend fun run(runId: Int, items: List<MediaItem>, settings: BatchConversionSettings) {
        val app = getApplication<Application>()
        val writer = MediaOutputWriter(app)
        val names = HashMap<String, MutableList<String>>()
        val failures = ArrayList<BatchFailureEntry>()
        var succeeded = 0
        try {
            withContext(Dispatchers.IO) {
                for ((index, item) in items.withIndex()) {
                    if (cancelRequested) break
                    try {
                        val target = writer.targetFor(item)
                        val taken = names.getOrPut(GalleryPreferences.encodeTarget(target)) { writer.namesIn(target).toMutableList() }
                        val name = BatchConversionPolicy.outputName(item.displayName, settings.format, taken)
                        writer.create(target, name, item.dateTakenMillis) { open ->
                            BatchImageConverter.convert(app.contentResolver, Uri.parse(item.contentUri), settings, app.cacheDir, open)
                        }
                        taken += name
                        succeeded++
                    } catch (error: BatchItemException) {
                        failures += BatchFailureEntry(item, error.failure)
                    } catch (error: OutOfMemoryError) {
                        failures += BatchFailureEntry(item, BatchFailure.OUT_OF_MEMORY)
                    }
                    _progress.value = BatchProgress(runId, items.size, index + 1, succeeded, failures.toList(), cancelling = cancelRequested)
                }
                writer.finish()
            }
        } finally {
            _progress.value = BatchProgress(
                runId = runId,
                total = items.size,
                done = _progress.value?.done ?: 0,
                succeeded = succeeded,
                failures = failures.toList(),
                running = false,
                cancelled = cancelRequested,
            )
        }
    }
}
