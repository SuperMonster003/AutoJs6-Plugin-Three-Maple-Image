package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.os.Build
import android.os.Bundle
import org.autojs.plugin.explorer.api.ExplorerActionHostSessionKeys
import org.autojs.plugin.explorer.api.ExplorerActionProtocol
import org.autojs.plugin.explorer.api.ExplorerActionValues
import org.autojs.plugin.explorer.api.IExplorerActionHostSession

/** One readable entry of a host directory listing (ROADMAP 6.4 "browse directory as album"). */
internal data class HostDirectoryEntry(
    val relativePath: String,
    val displayName: String,
    val directory: Boolean,
    val mimeType: String,
    val size: Long,
    val lastModified: Long,
)

internal data class HostDirectoryListing(
    val relativePath: String,
    val folders: List<HostDirectoryEntry>,
    val images: List<HostDirectoryEntry>,
    /** True when the host reported more than [HostDirectoryPolicy.MAX_ENTRIES] children. */
    val truncated: Boolean,
) {
    val depth: Int get() = if (relativePath.isEmpty()) 0 else relativePath.count { it == '/' } + 1
    val name: String get() = relativePath.substringAfterLast('/')
}

/** Pure classification and bounds of host child listings, shared with the unit tests. */
internal object HostDirectoryPolicy {

    /** Entries read per folder before the listing is cut off with a notice. */
    const val MAX_ENTRIES = 4_096

    /** Folders below this depth are shown but not opened, like the authorized-folder walker. */
    const val MAX_DEPTH = 8

    fun classify(
        parentPath: String,
        items: List<ImageSiblingItem>,
        supports: (String, String) -> Boolean = ImageFormatSupport::isSupportedExplorerTarget,
    ): Pair<List<HostDirectoryEntry>, List<HostDirectoryEntry>> {
        val folders = ArrayList<HostDirectoryEntry>()
        val images = ArrayList<HostDirectoryEntry>()
        for (item in items) {
            if (!item.readable || item.symbolicLink) continue
            val name = item.displayName
            if (ImageRequestPolicy.validateDisplayName(name) == null) continue
            val expectedPath = if (parentPath.isEmpty()) name else "$parentPath/$name"
            if (item.relativePath != expectedPath) continue
            when (item.kind) {
                ExplorerActionValues.TARGET_DIRECTORY -> folders += HostDirectoryEntry(
                    relativePath = item.relativePath,
                    displayName = name,
                    directory = true,
                    mimeType = item.mimeType,
                    size = -1L,
                    lastModified = item.lastModified,
                )
                ExplorerActionValues.TARGET_FILE -> {
                    if (!supports(name, item.mimeType)) continue
                    if (!ImageRequestPolicy.isDeclaredSizeAccepted(item.declaredSize)) continue
                    val mimeType = ImageFormatSupport.explorerMimeType(name, item.mimeType) ?: continue
                    images += HostDirectoryEntry(
                        relativePath = item.relativePath,
                        displayName = name,
                        directory = false,
                        mimeType = mimeType,
                        size = item.declaredSize,
                        lastModified = item.lastModified,
                    )
                }
            }
        }
        val natural = Comparator<HostDirectoryEntry> { a, b -> ImageSiblingDiscoveryPolicy.compareNaturally(a.displayName, b.displayName) }
        return folders.sortedWith(natural) to images.sortedWith(natural)
    }
}

/**
 * Pages one directory target of a host session (`listChildren` with nested relative paths) and turns the result
 * into folder and image entries. The host sorts case-insensitively and never discloses absolute paths; this side
 * re-sorts naturally and drops anything unreadable, linked or mislabeled.
 */
internal class HostDirectoryBrowser(
    private val session: IExplorerActionHostSession,
    private val targetId: String,
) {

    fun list(relativePath: String): HostDirectoryListing {
        val items = ArrayList<ImageSiblingItem>()
        var offset = 0
        var truncated = false
        while (true) {
            val page = session.listChildren(targetId, relativePath, offset, ExplorerActionProtocol.MAX_SESSION_PAGE_SIZE)
            val bundles = page.bundleArrayList(ExplorerActionHostSessionKeys.ITEMS)
                ?: error("Explorer directory page has no items")
            require(bundles.size <= ExplorerActionProtocol.MAX_SESSION_PAGE_SIZE)
            for (bundle in bundles) {
                if (items.size >= HostDirectoryPolicy.MAX_ENTRIES) {
                    truncated = true
                    break
                }
                items += parseItem(bundle)
            }
            val nextOffset = page.getInt(ExplorerActionHostSessionKeys.NEXT_OFFSET, -1)
            val complete = page.getBoolean(ExplorerActionHostSessionKeys.COMPLETE, false)
            require(nextOffset == offset + bundles.size && nextOffset >= offset)
            if (truncated || complete) break
            require(bundles.isNotEmpty())
            offset = nextOffset
        }
        val (folders, images) = HostDirectoryPolicy.classify(relativePath, items)
        return HostDirectoryListing(relativePath, folders, images, truncated)
    }

    private fun parseItem(bundle: Bundle): ImageSiblingItem {
        val relativePath = requireNotNull(bundle.getString(ExplorerActionHostSessionKeys.RELATIVE_PATH))
        val displayName = requireNotNull(bundle.getString(ExplorerActionHostSessionKeys.DISPLAY_NAME))
        require(relativePath.length <= ExplorerActionProtocol.MAX_SESSION_RELATIVE_PATH_LENGTH)
        require(bundle.containsKey(ExplorerActionHostSessionKeys.SIZE))
        require(bundle.containsKey(ExplorerActionHostSessionKeys.LAST_MODIFIED))
        return ImageSiblingItem(
            relativePath = relativePath,
            displayName = displayName,
            kind = bundle.getInt(ExplorerActionHostSessionKeys.KIND, Int.MIN_VALUE),
            mimeType = bundle.getString(ExplorerActionHostSessionKeys.MIME_TYPE).orEmpty(),
            declaredSize = bundle.getLong(ExplorerActionHostSessionKeys.SIZE, -1L),
            lastModified = bundle.getLong(ExplorerActionHostSessionKeys.LAST_MODIFIED, Long.MIN_VALUE),
            readable = bundle.getBoolean(ExplorerActionHostSessionKeys.READABLE, false),
            symbolicLink = bundle.getBoolean(ExplorerActionHostSessionKeys.SYMBOLIC_LINK, true),
        )
    }

    @Suppress("DEPRECATION")
    private fun Bundle.bundleArrayList(key: String): ArrayList<Bundle>? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getParcelableArrayList(key, Bundle::class.java)
        } else {
            getParcelableArrayList(key)
        }
}
