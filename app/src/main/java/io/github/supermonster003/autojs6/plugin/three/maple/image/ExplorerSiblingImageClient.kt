package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.os.Build
import android.os.Bundle
import org.autojs.plugin.explorer.api.ExplorerActionHostSessionKeys
import org.autojs.plugin.explorer.api.ExplorerActionProtocol
import org.autojs.plugin.explorer.api.IExplorerActionHostSession

/** Reads only the direct-parent page exposed by a v12 readSiblings host session. */
internal object ExplorerSiblingImageClient {

    fun discover(request: ExplorerImageRequest): DiscoveredImageGallery {
        require(request.mode == ExplorerImageRequestMode.SINGLE_WITH_SIBLINGS)
        val target = request.targets.single()
        return ImageSiblingDiscoveryPolicy.discover(
            selectedDisplayName = target.image.displayName,
            selectedMimeType = target.image.mimeType,
            selectedDeclaredSize = target.image.declaredSize,
            siblings = listDirectSiblings(requireNotNull(request.hostSession), target.id),
        )
    }

    private fun listDirectSiblings(
        session: IExplorerActionHostSession,
        targetId: String,
    ): List<ImageSiblingItem> {
        val result = ArrayList<ImageSiblingItem>()
        var offset = 0
        while (true) {
            val page = session.listChildren(
                targetId,
                "",
                offset,
                ExplorerActionProtocol.MAX_SESSION_PAGE_SIZE,
            )
            val items = page.bundleArrayList(ExplorerActionHostSessionKeys.ITEMS)
                ?: error("Explorer sibling page has no items")
            require(items.size <= ExplorerActionProtocol.MAX_SESSION_PAGE_SIZE)
            require(result.size + items.size <= MAX_DISCOVERY_ITEMS)
            items.forEach { bundle -> result += parseItem(bundle) }
            val nextOffset = page.getInt(ExplorerActionHostSessionKeys.NEXT_OFFSET, -1)
            val complete = page.getBoolean(ExplorerActionHostSessionKeys.COMPLETE, false)
            require(nextOffset == offset + items.size && nextOffset >= offset)
            if (complete) return result
            require(items.isNotEmpty() && nextOffset > offset)
            offset = nextOffset
        }
    }

    private fun parseItem(bundle: Bundle): ImageSiblingItem {
        val relativePath = requireNotNull(
            bundle.getString(ExplorerActionHostSessionKeys.RELATIVE_PATH),
        )
        val displayName = requireNotNull(
            bundle.getString(ExplorerActionHostSessionKeys.DISPLAY_NAME),
        )
        require(relativePath == displayName && '/' !in relativePath && '\\' !in relativePath)
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

    private const val MAX_DISCOVERY_ITEMS = 4_096
}
