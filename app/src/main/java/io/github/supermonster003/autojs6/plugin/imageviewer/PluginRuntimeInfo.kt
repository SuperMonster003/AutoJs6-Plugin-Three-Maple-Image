package io.github.supermonster003.autojs6.plugin.imageviewer

import android.content.Context
import android.os.Build
import android.os.Bundle
import org.autojs.plugin.common.api.PluginCapabilityKeys
import org.autojs.plugin.common.api.PluginInfo
import org.autojs.plugin.explorer.api.ExplorerActionCapabilityKeys
import org.autojs.plugin.explorer.api.ExplorerActionCatalogKeys
import org.autojs.plugin.explorer.api.ExplorerActionPluginIds
import org.autojs.plugin.explorer.api.ExplorerActionProtocol
import org.autojs.plugin.explorer.api.ExplorerActionValues

internal object ImageViewerPlugin {
    const val ID = "image-viewer"
    const val ACTION_ID = "view-image"
    const val VARIANT = "default"
    const val REQUIRED_HOST_VERSION = 5269L
    const val LABEL_RESOURCE_NAME = "action_view_image"
    const val LABEL_FALLBACK = "View image"
    const val ACTIVITY_CLASS_NAME =
        "io.github.supermonster003.autojs6.plugin.imageviewer.ExplorerActionActivity"
    const val ACTION_PRIORITY = 100

    val MIME_TYPES = arrayOf("image/*")
    val EXTENSIONS = arrayOf(
        "avif", "bmp", "gif", "heic", "heif", "ico", "jpeg", "jpg", "jxl", "png",
        "svg", "tif", "tiff", "webp",
    )
}

internal fun Context.imageViewerPluginInfo(): PluginInfo {
    val packageInfo = packageManager.getPackageInfo(packageName, 0)
    return PluginInfo().apply {
        name = getString(R.string.app_name)
        description = getString(R.string.plugin_description)
        instruction = null
        author = getString(R.string.plugin_author)
        collaborators = null
        versionName = packageInfo.versionName.orEmpty()
        versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packageInfo.longVersionCode
        } else {
            @Suppress("DEPRECATION")
            packageInfo.versionCode.toLong()
        }
        versionDate = getString(R.string.plugin_version_date)
        id = ImageViewerPlugin.ID
        engine = ExplorerActionPluginIds.ENGINE
        variant = ImageViewerPlugin.VARIANT
        supportedAbis = emptyArray()
        capabilities = Bundle().apply {
            putLong(PluginCapabilityKeys.REQUIRES_HOST_VERSION, ImageViewerPlugin.REQUIRED_HOST_VERSION)
            putInt(ExplorerActionCapabilityKeys.PROTOCOL_VERSION, ExplorerActionProtocol.VERSION)
        }
    }
}

internal fun imageViewerActionCatalog(): Bundle {
    val action = Bundle().apply {
        putString(ExplorerActionCatalogKeys.ID, ImageViewerPlugin.ACTION_ID)
        putString(ExplorerActionCatalogKeys.LABEL_RESOURCE_NAME, ImageViewerPlugin.LABEL_RESOURCE_NAME)
        putString(ExplorerActionCatalogKeys.LABEL_FALLBACK, ImageViewerPlugin.LABEL_FALLBACK)
        putString(ExplorerActionCatalogKeys.ACTIVITY_CLASS_NAME, ImageViewerPlugin.ACTIVITY_CLASS_NAME)
        putInt(ExplorerActionCatalogKeys.PRIORITY, ImageViewerPlugin.ACTION_PRIORITY)
        putInt(ExplorerActionCatalogKeys.TARGET_KIND, ExplorerActionValues.TARGET_FILE)
        putInt(ExplorerActionCatalogKeys.ACCESS_MODE, ExplorerActionValues.ACCESS_READ_ONLY)
        putInt(ExplorerActionCatalogKeys.PLACEMENT, ExplorerActionValues.PLACEMENT_PRIMARY)
        putStringArrayList(
            ExplorerActionCatalogKeys.MIME_TYPES,
            ArrayList(ImageViewerPlugin.MIME_TYPES.asList()),
        )
        putStringArrayList(
            ExplorerActionCatalogKeys.EXTENSIONS,
            ArrayList(ImageViewerPlugin.EXTENSIONS.asList()),
        )
    }
    return Bundle().apply {
        putInt(ExplorerActionCatalogKeys.PROTOCOL_VERSION, ExplorerActionProtocol.VERSION)
        putParcelableArrayList(ExplorerActionCatalogKeys.ACTIONS, arrayListOf(action))
    }
}
