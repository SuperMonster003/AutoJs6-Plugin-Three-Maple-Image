package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.content.Context
import android.os.Build
import android.os.Bundle
import org.autojs.plugin.common.api.PluginCapabilityKeys
import org.autojs.plugin.common.api.PluginInfo
import org.autojs.plugin.explorer.api.ExplorerActionCapabilityKeys
import org.autojs.plugin.explorer.api.ExplorerActionCatalogKeys
import org.autojs.plugin.explorer.api.ExplorerActionPluginIds
import org.autojs.plugin.explorer.api.ExplorerActionValues

internal object ThreeMapleImagePlugin {
    const val ID = "three-maple-image"
    const val ACTION_ID = "view-image"
    const val MULTIPLE_ACTION_ID = "view-images"
    const val VARIANT = "default"
    const val PROTOCOL_VERSION = 12
    const val REQUIRED_HOST_VERSION = 5276L
    const val LABEL_RESOURCE_NAME = "action_view_image"
    const val LABEL_FALLBACK = "View image"
    const val ACTIVITY_CLASS_NAME =
        "io.github.supermonster003.autojs6.plugin.three.maple.image.ExplorerActionActivity"
    const val ACTION_PRIORITY = 100

    val MIME_TYPES = emptyArray<String>()
    val EXTENSIONS = ImageFormatSupport.explorerExtensions.toTypedArray()
}

internal fun Context.imageViewerPluginInfo(): PluginInfo {
    val packageInfo = packageManager.getPackageInfo(packageName, 0)
    return PluginInfo().apply {
        name = getString(R.string.app_name)
        description = getString(R.string.plugin_description)
        instruction = "@raw/plugin_instruction"
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
        id = ThreeMapleImagePlugin.ID
        engine = ExplorerActionPluginIds.ENGINE
        variant = ThreeMapleImagePlugin.VARIANT
        supportedAbis = emptyArray()
        capabilities = Bundle().apply {
            putLong(PluginCapabilityKeys.REQUIRES_HOST_VERSION, ThreeMapleImagePlugin.REQUIRED_HOST_VERSION)
            putInt(ExplorerActionCapabilityKeys.PROTOCOL_VERSION, ThreeMapleImagePlugin.PROTOCOL_VERSION)
        }
    }
}

internal fun imageViewerActionCatalog(): Bundle {
    val singleAction = imageViewerAction(
        id = ThreeMapleImagePlugin.ACTION_ID,
        cardinality = ExplorerActionValues.CARDINALITY_SINGLE,
        placement = ExplorerActionValues.PLACEMENT_PRIMARY,
    ).apply {
        putBoolean(ExplorerActionCatalogKeys.READ_SIBLINGS, true)
    }
    val multipleAction = imageViewerAction(
        id = ThreeMapleImagePlugin.MULTIPLE_ACTION_ID,
        cardinality = ExplorerActionValues.CARDINALITY_MULTIPLE,
        placement = ExplorerActionValues.PLACEMENT_SELECTION_TOOLBAR,
    )
    return Bundle().apply {
        putInt(ExplorerActionCatalogKeys.PROTOCOL_VERSION, ThreeMapleImagePlugin.PROTOCOL_VERSION)
        putParcelableArrayList(
            ExplorerActionCatalogKeys.ACTIONS,
            arrayListOf(singleAction, multipleAction),
        )
    }
}

private fun imageViewerAction(
    id: String,
    cardinality: Int,
    placement: Int,
): Bundle = Bundle().apply {
        putString(ExplorerActionCatalogKeys.ID, id)
        putString(ExplorerActionCatalogKeys.LABEL_RESOURCE_NAME, ThreeMapleImagePlugin.LABEL_RESOURCE_NAME)
        putString(ExplorerActionCatalogKeys.LABEL_FALLBACK, ThreeMapleImagePlugin.LABEL_FALLBACK)
        putString(ExplorerActionCatalogKeys.ACTIVITY_CLASS_NAME, ThreeMapleImagePlugin.ACTIVITY_CLASS_NAME)
        putInt(ExplorerActionCatalogKeys.PRIORITY, ThreeMapleImagePlugin.ACTION_PRIORITY)
        putInt(ExplorerActionCatalogKeys.TARGET_KIND, ExplorerActionValues.TARGET_FILE)
        putInt(ExplorerActionCatalogKeys.CARDINALITY, cardinality)
        putInt(ExplorerActionCatalogKeys.ACCESS_MODE, ExplorerActionValues.ACCESS_READ_ONLY)
        putInt(ExplorerActionCatalogKeys.PLACEMENT, placement)
        putStringArrayList(
            ExplorerActionCatalogKeys.MIME_TYPES,
            ArrayList(ThreeMapleImagePlugin.MIME_TYPES.asList()),
        )
        putStringArrayList(
            ExplorerActionCatalogKeys.EXTENSIONS,
            ArrayList(ThreeMapleImagePlugin.EXTENSIONS.asList()),
        )
    }
