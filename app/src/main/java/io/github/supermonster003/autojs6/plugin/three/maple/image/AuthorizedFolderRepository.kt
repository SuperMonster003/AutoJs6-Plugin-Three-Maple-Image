package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

/** Last enumeration per tree URI in this process: the grid fills it, the viewer reads it (ROADMAP 6.3). */
private val folderEnumerationCache = ConcurrentHashMap<String, Pair<List<MediaItem>, DocumentTreeLimit>>()

/** A folder the user authorized through the system folder picker (ROADMAP T7). */
internal data class AuthorizedFolder(
    val treeUri: String,
    val displayName: String,
    /** Human readable location such as `Pictures` or `1234-5678/DCIM`, derived from the tree document id. */
    val location: String,
    /** False when the persisted read permission is gone or the folder no longer resolves (volume removed). */
    val available: Boolean,
) {
    val bucketId: Long get() = DocumentTreeWalker.syntheticId(treeUri)
}

/**
 * Persists `ACTION_OPEN_DOCUMENT_TREE` authorizations and enumerates them into the gallery's [MediaItem] model, so
 * authorized folders behave like albums whether or not photo access was granted (ROADMAP D4 / T7). The ordered
 * list lives in its own preference file; the permissions themselves are the ones the system keeps.
 */
internal class AuthorizedFolderRepository(
    context: Context,
    private val supports: (String, String) -> Boolean = ImageFormatSupport::isSupportedExplorerTarget,
) {
    private val resolver: ContentResolver = context.applicationContext.contentResolver
    private val preferences = context.applicationContext.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)

    fun list(): List<AuthorizedFolder> {
        val persisted = resolver.persistedUriPermissions.filter { it.isReadPermission }.map { it.uri.toString() }.toSet()
        return storedUris().map { uri ->
            val parsed = Uri.parse(uri)
            val name = displayName(parsed)
            AuthorizedFolder(treeUri = uri, displayName = name ?: fallbackName(parsed), location = location(parsed), available = uri in persisted && name != null)
        }
    }

    /** Takes the persistable read permission and remembers the tree; returns the stored entry. */
    fun add(treeUri: Uri): AuthorizedFolder {
        runCatching { resolver.takePersistableUriPermission(treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
        val uris = storedUris()
        if (treeUri.toString() !in uris) saveUris(uris + treeUri.toString())
        return list().first { it.treeUri == treeUri.toString() }
    }

    fun remove(treeUri: String) {
        runCatching { resolver.releasePersistableUriPermission(Uri.parse(treeUri), Intent.FLAG_GRANT_READ_URI_PERMISSION) }
        saveUris(storedUris().filterNot { it == treeUri })
    }

    /** Enumerates the folder recursively into gallery items; `limit` says whether a cap stopped the walk early. */
    suspend fun enumerate(
        folder: AuthorizedFolder,
        maxDepth: Int = DocumentTreeWalker.DEFAULT_MAX_DEPTH,
        maxImages: Int = DocumentTreeWalker.DEFAULT_MAX_IMAGES,
    ): Pair<List<MediaItem>, DocumentTreeLimit> = withContext(Dispatchers.IO) {
        val tree = Uri.parse(folder.treeUri)
        val root = DocumentsContract.getTreeDocumentId(tree)
        val result = DocumentTreeWalker.walk(root, { children(tree, it) }, supports, maxDepth, maxImages)
        val items = result.images.map { entry ->
            MediaItem(
                id = DocumentTreeWalker.syntheticId(entry.documentId),
                displayName = entry.displayName,
                mimeType = entry.mimeType,
                sizeBytes = entry.sizeBytes,
                width = 0,
                height = 0,
                dateModifiedSeconds = entry.lastModifiedMillis / 1000L,
                dateTakenMillis = null,
                bucketId = folder.bucketId,
                bucketName = folder.displayName,
                relativePath = result.folders[entry.documentId].orEmpty(),
                contentUri = DocumentsContract.buildDocumentUriUsingTree(tree, entry.documentId).toString(),
            )
        }
        items to result.limit
    }

    /** The cached enumeration of [folder], if this process produced one. */
    fun cached(folder: AuthorizedFolder): Pair<List<MediaItem>, DocumentTreeLimit>? = folderEnumerationCache[folder.treeUri]

    /** [enumerate] through the process-wide cache, so opening a folder in the viewer does not walk the tree again. */
    suspend fun enumerateCached(folder: AuthorizedFolder): Pair<List<MediaItem>, DocumentTreeLimit> =
        folderEnumerationCache[folder.treeUri] ?: enumerate(folder).also { folderEnumerationCache[folder.treeUri] = it }

    fun invalidateCache() {
        folderEnumerationCache.clear()
    }

    private fun children(tree: Uri, documentId: String): List<DocumentEntry> {
        val uri = DocumentsContract.buildChildDocumentsUriUsingTree(tree, documentId)
        val cursor = runCatching { resolver.query(uri, CHILD_PROJECTION, null, null, null) }.getOrNull() ?: return emptyList()
        cursor.use {
            val entries = ArrayList<DocumentEntry>(it.count)
            while (it.moveToNext()) {
                entries.add(
                    DocumentEntry(
                        documentId = it.getString(0).orEmpty(),
                        displayName = it.getString(1).orEmpty(),
                        mimeType = it.getString(2).orEmpty(),
                        sizeBytes = if (it.isNull(3)) 0L else it.getLong(3),
                        lastModifiedMillis = if (it.isNull(4)) 0L else it.getLong(4),
                    ),
                )
            }
            return entries
        }
    }

    private fun displayName(tree: Uri): String? = runCatching {
        val document = DocumentsContract.buildDocumentUriUsingTree(tree, DocumentsContract.getTreeDocumentId(tree))
        resolver.query(document, arrayOf(DocumentsContract.Document.COLUMN_DISPLAY_NAME), null, null, null)?.use {
            if (it.moveToFirst()) it.getString(0) else null
        }
    }.getOrNull()

    /** `primary:Pictures` becomes `Pictures`; other volumes keep their id, `1234-5678:DCIM` becomes `1234-5678/DCIM`. */
    private fun location(tree: Uri): String {
        val id = runCatching { DocumentsContract.getTreeDocumentId(tree) }.getOrNull() ?: return tree.lastPathSegment.orEmpty()
        val volume = id.substringBefore(':', missingDelimiterValue = "")
        val path = id.substringAfter(':', missingDelimiterValue = id).trim('/')
        return when {
            volume.isEmpty() || volume == "primary" -> path.ifEmpty { volume }
            path.isEmpty() -> volume
            else -> "$volume/$path"
        }
    }

    private fun fallbackName(tree: Uri): String =
        runCatching { DocumentsContract.getTreeDocumentId(tree) }.getOrNull()?.substringAfterLast(':')?.substringAfterLast('/')?.takeIf { it.isNotEmpty() }
            ?: tree.lastPathSegment.orEmpty()

    private fun storedUris(): List<String> = preferences.getString(KEY_URIS, "").orEmpty().split('\n').filter { it.isNotBlank() }

    private fun saveUris(uris: List<String>) {
        preferences.edit().putString(KEY_URIS, uris.joinToString("\n")).apply()
    }

    private companion object {
        const val FILE_NAME = "authorized_folders"
        const val KEY_URIS = "uris"
        val CHILD_PROJECTION = arrayOf(
            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
            DocumentsContract.Document.COLUMN_MIME_TYPE,
            DocumentsContract.Document.COLUMN_SIZE,
            DocumentsContract.Document.COLUMN_LAST_MODIFIED,
        )
    }
}
