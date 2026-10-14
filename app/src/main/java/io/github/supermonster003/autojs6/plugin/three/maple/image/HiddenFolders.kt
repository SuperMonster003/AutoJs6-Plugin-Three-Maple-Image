package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/** Serves images of hidden folders to the viewer, Glide and the share sheet (internal storage only, not exported). */
class HiddenImageProvider : FileProvider()

/** One folder hidden with `.nomedia` (ROADMAP 7.4) and its images, enumerated from the file system. */
internal data class HiddenFolder(val path: AlbumPath, val items: List<MediaItem>) {
    val name: String get() = path.relativePath.trimEnd('/').substringAfterLast('/')

    /** A stable album id that cannot meet a MediaStore bucket id (those come from the path hash as well). */
    val id: Long get() = HiddenFolders.idOf(path)
}

/**
 * Folders hidden with a `.nomedia` file on the internal storage (ROADMAP 7.4): the ones the gallery hid, plus the
 * ones found under DCIM/ and Pictures/. Reading them needs file system access: all files access on Android 11+,
 * the storage permission before that. The media library does not list their images, so they are read from disk.
 */
internal class HiddenFolders(context: Context) {

    private val context = context.applicationContext
    private val marks = FolderMarksRepository(this.context)

    /** Whether hidden folders can be listed and changed now. */
    fun available(): Boolean = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        Environment.isExternalStorageManager()
    } else {
        ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }

    suspend fun list(): List<HiddenFolder> = withContext(Dispatchers.IO) {
        if (!available()) return@withContext emptyList()
        val tracked = marks.paths(FolderMark.HIDDEN).filter { it.isPrimary }
        (tracked + discover())
            .distinctBy { MediaCatalog.folderKey(it) }
            .filter { File(directory(it), NOMEDIA).exists() }
            .map { HiddenFolder(it, items(it)) }
            .sortedBy { it.name.lowercase() }
    }

    suspend fun folder(relativePath: String): HiddenFolder? = withContext(Dispatchers.IO) {
        val path = AlbumPath(MediaCatalog.PRIMARY_VOLUME, relativePath)
        if (!available() || !File(directory(path), NOMEDIA).exists()) null else HiddenFolder(path, items(path))
    }

    /** Folders with a `.nomedia` file under DCIM/ and Pictures/, a few levels deep. */
    private fun discover(): List<AlbumPath> {
        val root = root()
        return listOf(Environment.DIRECTORY_DCIM, Environment.DIRECTORY_PICTURES).flatMap { top ->
            File(root, top).walkTopDown().maxDepth(DISCOVERY_DEPTH).onEnter { !it.name.startsWith(".") || it.name == top }
                .filter { it.isDirectory && File(it, NOMEDIA).exists() }
                .map { AlbumPath(MediaCatalog.PRIMARY_VOLUME, it.relativeTo(root).path.replace(File.separatorChar, '/') + "/") }
                .toList()
        }
    }

    private fun items(path: AlbumPath): List<MediaItem> {
        val directory = directory(path)
        val authority = "${context.packageName}.$AUTHORITY_SUFFIX"
        return directory.listFiles().orEmpty().filter { it.isFile && !it.name.startsWith(".") }.mapNotNull { file ->
            val mime = ImageFormatSupport.explorerMimeType(file.name, "*/*") ?: return@mapNotNull null
            val uri = runCatching { FileProvider.getUriForFile(context, authority, file) }.getOrNull() ?: return@mapNotNull null
            MediaItem(
                id = idOf(file),
                displayName = file.name,
                mimeType = mime,
                sizeBytes = file.length(),
                width = 0,
                height = 0,
                dateModifiedSeconds = file.lastModified() / 1000,
                dateTakenMillis = null,
                bucketId = idOf(path),
                bucketName = path.relativePath.trimEnd('/').substringAfterLast('/'),
                relativePath = path.relativePath,
                contentUri = uri.toString(),
            )
        }.filter { it.sizeBytes > 0 }
    }

    companion object {
        const val NOMEDIA = ".nomedia"
        const val AUTHORITY_SUFFIX = "hidden-images"
        private const val DISCOVERY_DEPTH = 3

        @Suppress("DEPRECATION")
        fun root(): File = Environment.getExternalStorageDirectory()

        fun directory(path: AlbumPath): File = File(root(), path.relativePath.trimEnd('/'))

        /** Negative ids keep hidden folders and their images apart from MediaStore ids. */
        fun idOf(path: AlbumPath): Long = -1_000_000L - (MediaCatalog.folderKey(path).hashCode().toLong() and 0x7fffffff)

        private fun idOf(file: File): Long = -1_000_000L - (file.absolutePath.hashCode().toLong() and 0x7fffffff)
    }
}
