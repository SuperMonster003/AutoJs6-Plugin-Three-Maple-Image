package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Process
import android.provider.DocumentsContract
import android.provider.MediaStore
import java.io.File
import java.io.InputStream

/** Which originals the standalone tools may write back (ROADMAP 8.2, D3 / T11). */
internal object StandaloneOverwrite {

    /** What decides whether an original may be overwritten, gathered from the system ([facts]). */
    data class Facts(
        val mimeType: String,
        val mediaLibrary: Boolean,
        val sdk: Int,
        /** Media library image on the internal storage (Android 7-10 write through the file only there). */
        val onPrimaryStorage: Boolean = false,
        /** Another app granted this app write access to the document. */
        val writeGranted: Boolean = false,
        /** A document provider document (only those report whether they can be written). */
        val providerDocument: Boolean = false,
        val providerSupportsWrite: Boolean = false,
    )

    /**
     * Whether an original can be overwritten: the tools must be able to write its format (an overwrite keeps the
     * format and the name), and the app must be able to write it. Media library images are written through a system
     * confirmation on Android 11+ and through the file on the internal storage on Android 7-10; other documents need
     * a write grant from the app that shared them and, for a document provider, a writable document.
     */
    fun decide(facts: Facts): Boolean = when {
        facts.mimeType !in ImageToolsPlugin.OUTPUT_MIME_TYPES -> false
        facts.mediaLibrary -> facts.sdk >= Build.VERSION_CODES.R || facts.onPrimaryStorage
        !facts.writeGranted -> false
        facts.providerDocument -> facts.providerSupportsWrite
        else -> true
    }

    fun isOverwritable(context: Context, document: StandaloneDocument): Boolean = decide(facts(context, document))

    fun facts(context: Context, document: StandaloneDocument): Facts {
        val uri = document.uri
        val sdk = Build.VERSION.SDK_INT
        if (uri.authority == MediaStore.AUTHORITY) {
            @Suppress("DEPRECATION")
            val root = Environment.getExternalStorageDirectory().absolutePath.trimEnd('/') + "/"
            val primary = sdk < Build.VERSION_CODES.R && mediaFilePath(context.contentResolver, uri)?.startsWith(root) == true
            return Facts(document.mimeType, mediaLibrary = true, sdk = sdk, onPrimaryStorage = primary)
        }
        val granted = context.checkUriPermission(uri, Process.myPid(), Process.myUid(), Intent.FLAG_GRANT_WRITE_URI_PERMISSION) ==
            PackageManager.PERMISSION_GRANTED
        val providerDocument = DocumentsContract.isDocumentUri(context, uri)
        val supportsWrite = granted && providerDocument && runCatching {
            context.contentResolver.query(uri, arrayOf(DocumentsContract.Document.COLUMN_FLAGS), null, null, null)?.use { cursor ->
                cursor.moveToFirst() && cursor.getInt(0) and DocumentsContract.Document.FLAG_SUPPORTS_WRITE != 0
            }
        }.getOrNull() == true
        return Facts(document.mimeType, mediaLibrary = false, sdk = sdk, writeGranted = granted, providerDocument = providerDocument, providerSupportsWrite = supportsWrite)
    }

    @Suppress("DEPRECATION")
    fun mediaFilePath(resolver: ContentResolver, uri: Uri): String? = runCatching {
        resolver.query(uri, arrayOf(MediaStore.MediaColumns.DATA), null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) cursor.getString(0) else null
        }
    }.getOrNull()
}

/**
 * The one undo backup of an overwrite (ROADMAP 8.2, T11): the original's bytes, kept privately until the undo uses
 * them or the next overwrite replaces them. Originals over [MAX_BYTES], or without room for a copy, get no backup;
 * the caller then asks before overwriting for good.
 */
internal class OverwriteBackup(context: Context) {

    private val directory = File(context.filesDir, "overwrite-backup")
    private val data = File(directory, "original.bin")
    private val target = File(directory, "target.txt")
    private val partial = File(directory, "original.part")

    /** Copies the original behind [uri]; false (and nothing kept) when it is too large or cannot be copied. */
    fun store(resolver: ContentResolver, uri: Uri): Boolean = runCatching {
        clear()
        check(directory.isDirectory || directory.mkdirs())
        resolver.openInputStream(uri)?.use { input ->
            partial.outputStream().use { output ->
                val buffer = ByteArray(64 * 1024)
                var total = 0L
                while (true) {
                    val count = input.read(buffer)
                    if (count < 0) break
                    total += count
                    check(total <= MAX_BYTES) { "The original is too large to back up" }
                    output.write(buffer, 0, count)
                }
            }
        } ?: error("The original cannot be read")
        check(partial.length() > 0 && partial.renameTo(data))
        target.writeText(uri.toString())
        true
    }.getOrElse {
        clear()
        false
    }

    /** Whether the backup is the original of [uri]. */
    fun holds(uri: Uri): Boolean = data.isFile && target.isFile && runCatching { target.readText() == uri.toString() }.getOrDefault(false)

    fun open(): InputStream = data.inputStream()

    fun clear() {
        partial.delete()
        data.delete()
        target.delete()
    }

    companion object {
        const val MAX_BYTES = 256L * 1024L * 1024L
    }
}
