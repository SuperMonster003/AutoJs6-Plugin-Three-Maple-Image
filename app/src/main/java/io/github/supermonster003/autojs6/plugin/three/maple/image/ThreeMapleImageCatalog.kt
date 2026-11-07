package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.os.Bundle
import org.autojs.plugin.explorer.api.ExplorerActionCatalogKeys

/**
 * One plugin identity, with the viewer and editing action contracts preserved, and the name of its first-level menu
 * in the host's file manager (the plugin's display name).
 */
internal fun threeMapleImageActionCatalog(): Bundle = imageViewerActionCatalog().apply {
    @Suppress("DEPRECATION")
    val viewer = getParcelableArrayList<Bundle>(ExplorerActionCatalogKeys.ACTIONS).orEmpty()
    @Suppress("DEPRECATION")
    val tools = imageToolsActionCatalog().getParcelableArrayList<Bundle>(ExplorerActionCatalogKeys.ACTIONS).orEmpty()
    putParcelableArrayList(ExplorerActionCatalogKeys.ACTIONS, ArrayList(viewer + tools))
    putString(ThreeMapleImagePlugin.MENU_GROUP_LABEL_RESOURCE_NAME_KEY, ThreeMapleImagePlugin.MENU_GROUP_LABEL_RESOURCE_NAME)
    putString(ThreeMapleImagePlugin.MENU_GROUP_LABEL_FALLBACK_KEY, ThreeMapleImagePlugin.MENU_GROUP_LABEL_FALLBACK)
}
