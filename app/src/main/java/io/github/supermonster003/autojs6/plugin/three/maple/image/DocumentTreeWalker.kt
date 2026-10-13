package io.github.supermonster003.autojs6.plugin.three.maple.image

/** One child of a document tree as the walker sees it (ROADMAP T7). */
internal data class DocumentEntry(
    val documentId: String,
    val displayName: String,
    val mimeType: String,
    val sizeBytes: Long,
    val lastModifiedMillis: Long,
) {
    val isDirectory: Boolean get() = mimeType == DocumentTreeWalker.DIRECTORY_MIME_TYPE
}

/** Why an enumeration stopped early; callers show it as a hint rather than silently truncating. */
internal enum class DocumentTreeLimit { NONE, DEPTH, ITEMS }

internal data class DocumentTreeResult(
    val images: List<DocumentEntry>,
    /** Relative folder (within the authorized tree) of each image, by document id. */
    val folders: Map<String, String>,
    val limit: DocumentTreeLimit,
)

/**
 * Android-free breadth-first walk of an authorized document tree. The caller supplies the child listing (a
 * `DocumentsContract` query on Android); the walker applies the format filter, the depth and item caps and skips
 * documents it has already visited so provider quirks (aliases, loops) cannot run it forever.
 */
internal object DocumentTreeWalker {

    const val DIRECTORY_MIME_TYPE = "vnd.android.document/directory"
    const val DEFAULT_MAX_DEPTH = 8
    const val DEFAULT_MAX_IMAGES = 20_000

    fun walk(
        rootDocumentId: String,
        children: (String) -> List<DocumentEntry>,
        supports: (String, String) -> Boolean,
        maxDepth: Int = DEFAULT_MAX_DEPTH,
        maxImages: Int = DEFAULT_MAX_IMAGES,
    ): DocumentTreeResult {
        val images = ArrayList<DocumentEntry>()
        val folders = HashMap<String, String>()
        val visited = HashSet<String>()
        var limit = DocumentTreeLimit.NONE
        var level: List<Pair<String, String>> = listOf(rootDocumentId to "")
        var depth = 0
        visited.add(rootDocumentId)
        while (level.isNotEmpty()) {
            val next = ArrayList<Pair<String, String>>()
            for ((documentId, path) in level) {
                for (entry in children(documentId)) {
                    if (entry.isDirectory) {
                        if (visited.add(entry.documentId)) next.add(entry.documentId to path + entry.displayName + "/")
                    } else if (entry.sizeBytes > 0L && supports(entry.displayName, entry.mimeType) && visited.add(entry.documentId)) {
                        if (images.size >= maxImages) return DocumentTreeResult(images, folders, DocumentTreeLimit.ITEMS)
                        images.add(entry)
                        folders[entry.documentId] = path
                    }
                }
            }
            depth += 1
            if (next.isNotEmpty() && depth >= maxDepth) {
                limit = DocumentTreeLimit.DEPTH
                break
            }
            level = next
        }
        return DocumentTreeResult(images, folders, limit)
    }

    /** Stable 64-bit id for a document id string, negative so it never collides with a MediaStore row id. */
    fun syntheticId(documentId: String): Long {
        var hash = 1_125_899_906_842_597L
        for (ch in documentId) hash = 31L * hash + ch.code
        return -(hash and Long.MAX_VALUE) - 1L
    }
}
