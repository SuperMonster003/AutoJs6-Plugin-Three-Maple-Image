package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.os.Bundle
import org.autojs.plugin.explorer.api.ExplorerActionCatalogKeys

/** One plugin identity, with the viewer and editing action contracts preserved. */
internal fun threeMapleImageActionCatalog(): Bundle = imageViewerActionCatalog().apply {
    @Suppress("DEPRECATION")
    val viewer = getParcelableArrayList<Bundle>(ExplorerActionCatalogKeys.ACTIONS).orEmpty()
    @Suppress("DEPRECATION")
    val tools = imageToolsActionCatalog().getParcelableArrayList<Bundle>(ExplorerActionCatalogKeys.ACTIONS).orEmpty()
    putParcelableArrayList(ExplorerActionCatalogKeys.ACTIONS, ArrayList(viewer + tools))
}
