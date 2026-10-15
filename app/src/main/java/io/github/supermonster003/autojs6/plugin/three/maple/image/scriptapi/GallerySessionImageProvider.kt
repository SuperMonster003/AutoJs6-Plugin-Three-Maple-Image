package io.github.supermonster003.autojs6.plugin.three.maple.image.scriptapi

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.MatrixCursor
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import android.system.Os
import io.github.supermonster003.autojs6.plugin.three.maple.image.ImageFormatSupport
import io.github.supermonster003.autojs6.plugin.three.maple.image.ImageRequestPolicy
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Non-exported, process-local provider of the images a script handed to the viewer (ROADMAP 10.2):
 * the viewer and Glide read ordinary content URIs while the images stay behind the host's read-only
 * descriptors. A page is copied into the plugin's cache the first time it is opened (the viewer
 * opens a page several times and needs a seekable file); the copies go with the registration.
 */
class GallerySessionImageProvider : ContentProvider() {

    override fun onCreate(): Boolean = true

    override fun getType(uri: Uri): String? = GallerySessionImages.describe(uri)?.mimeType

    override fun query(uri: Uri, projection: Array<out String>?, selection: String?, selectionArgs: Array<out String>?, sortOrder: String?): Cursor? {
        val page = GallerySessionImages.describe(uri) ?: return null
        val columns = projection ?: arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE)
        return MatrixCursor(columns, 1).apply {
            addRow(columns.map { column ->
                when (column) {
                    OpenableColumns.DISPLAY_NAME -> page.displayName
                    OpenableColumns.SIZE -> page.size
                    else -> null
                }
            })
        }
    }

    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor {
        if (mode != "r") throw FileNotFoundException("Read-only provider")
        val context = context ?: throw FileNotFoundException("No context")
        return GallerySessionImages.open(context, uri) ?: throw FileNotFoundException("Expired or invalid gallery session")
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int = 0
}

/** One viewer page as the provider describes it. */
internal data class GallerySessionImage(val displayName: String, val size: Long, val mimeType: String)

internal object GallerySessionImages {

    const val AUTHORITY = "io.github.supermonster003.autojs6.plugin.three.maple.image.gallery-images"

    private const val CACHE_DIRECTORY = "gallery-sessions"

    private class Entry(val directory: File, val sources: List<ParcelFileDescriptor>, val pages: List<GallerySessionImage>) {
        val locks = List(sources.size) { Any() }
    }

    private val entries = ConcurrentHashMap<String, Entry>()

    /** Registers the adopted viewer sources; the registration owns them until [unregister]. */
    fun register(context: Context, viewer: SessionPayload.Viewer): String {
        val pages = viewer.sources.mapIndexed { index, source -> describe(source, viewer.names.getOrNull(index), index) }
        while (true) {
            val token = UUID.randomUUID().toString()
            val directory = File(File(context.cacheDir, CACHE_DIRECTORY), token)
            if (entries.putIfAbsent(token, Entry(directory, viewer.sources, pages)) == null) return token
        }
    }

    fun pages(token: String): List<GallerySessionImage> = entries[token]?.pages.orEmpty()

    fun imageUri(token: String, index: Int): Uri = Uri.Builder()
        .scheme("content")
        .authority(AUTHORITY)
        .appendPath(token)
        .appendPath(index.toString())
        .build()

    /** Forgets [token], closes its descriptors and deletes its cached pages. */
    fun unregister(token: String?) {
        val entry = token?.let(entries::remove) ?: return
        entry.sources.forEach { runCatching { it.close() } }
        entry.directory.listFiles()?.forEach { it.delete() }
        entry.directory.delete()
    }

    fun describe(uri: Uri): GallerySessionImage? {
        val (entry, index) = route(uri) ?: return null
        return entry.pages[index]
    }

    fun open(context: Context, uri: Uri): ParcelFileDescriptor? {
        val (entry, index) = route(uri) ?: return null
        val file = File(entry.directory, "$index.bin")
        synchronized(entry.locks[index]) {
            if (!file.isFile || file.length() == 0L) {
                entry.directory.mkdirs()
                val partial = File(entry.directory, "$index.part")
                partial.outputStream().use { output -> PositionalInputStream(entry.sources[index]).use { it.copyTo(output, 64 * 1024) } }
                if (!partial.renameTo(file)) {
                    partial.delete()
                    return null
                }
            }
        }
        return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
    }

    private fun route(uri: Uri): Pair<Entry, Int>? {
        if (uri.authority != AUTHORITY) return null
        val segments = uri.pathSegments
        if (segments.size != 2) return null
        val entry = entries[segments[0]] ?: return null
        val index = segments[1].toIntOrNull()?.takeIf { it in entry.sources.indices } ?: return null
        return entry to index
    }

    /** Size, type (sniffed from the image header, else from the name) and a safe display name of one source. */
    private fun describe(source: ParcelFileDescriptor, name: String?, index: Int): GallerySessionImage {
        val size = runCatching { Os.fstat(source.fileDescriptor).st_size }.getOrDefault(0L)
        val displayName = ImageRequestPolicy.validateDisplayName(name) ?: "image-${index + 1}"
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        runCatching { PositionalInputStream(source).use { BitmapFactory.decodeStream(it, null, bounds) } }
        val sniffed = ImageRequestPolicy.normalizeImageMimeType(bounds.outMimeType)
        val mimeType = sniffed
            ?: ImageFormatSupport.explorerMimeType(displayName, "image/*")?.let(ImageRequestPolicy::normalizeImageMimeType)
            ?: "image/*"
        return GallerySessionImage(displayName, size, mimeType)
    }

    /** Reads a shared descriptor from offset 0 with positional reads, leaving its file offset alone. */
    private class PositionalInputStream(source: ParcelFileDescriptor) : InputStream() {
        private val channel: FileChannel = FileInputStream(source.fileDescriptor).channel
        private var position = 0L

        override fun read(): Int {
            val one = ByteArray(1)
            return if (read(one, 0, 1) <= 0) -1 else one[0].toInt() and 0xFF
        }

        override fun read(b: ByteArray, off: Int, len: Int): Int {
            if (len == 0) return 0
            val count = channel.read(ByteBuffer.wrap(b, off, len), position)
            if (count > 0) position += count
            return count
        }

        // The channel belongs to the descriptor's owner; closing this stream must not close it.
        override fun close() = Unit
    }
}
