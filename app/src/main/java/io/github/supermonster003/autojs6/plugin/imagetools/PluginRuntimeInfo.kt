package io.github.supermonster003.autojs6.plugin.imagetools

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

internal object ImageToolsPlugin {
    const val PACKAGE_NAME = "io.github.supermonster003.autojs6.plugin.imagetools"
    const val ID = "image-tools"
    const val VARIANT = "default"
    const val REQUIRED_HOST_VERSION = 5269L
    const val EDIT_ACTION_ID = "edit-image"
    const val CONVERT_ACTION_ID = "convert-image"
    const val MAX_OUTPUT_BYTES = 256L * 1024L * 1024L
    const val INPUT_OPEN_MODE = "r"
    const val OUTPUT_OPEN_MODE = "w"

    val INPUT_MIME_TYPES = arrayOf("image/*")
    val INPUT_EXTENSIONS = arrayOf("bmp", "gif", "heic", "heif", "jpeg", "jpg", "png", "webp")
    val OUTPUT_MIME_TYPES = arrayOf("image/jpeg", "image/png", "image/webp")
}

internal fun Context.imageToolsPluginInfo(): PluginInfo {
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
        id = ImageToolsPlugin.ID
        engine = ExplorerActionPluginIds.ENGINE
        variant = ImageToolsPlugin.VARIANT
        supportedAbis = emptyArray()
        capabilities = Bundle().apply {
            putLong(PluginCapabilityKeys.REQUIRES_HOST_VERSION, ImageToolsPlugin.REQUIRED_HOST_VERSION)
            putInt(ExplorerActionCapabilityKeys.PROTOCOL_VERSION, ExplorerActionProtocol.VERSION)
        }
    }
}

internal fun imageToolsActionCatalog(): Bundle {
    fun action(
        id: String,
        labelResourceName: String,
        labelFallback: String,
        activityClassName: String,
        priority: Int,
        outputSuffix: String,
    ) = Bundle().apply {
        putString(ExplorerActionCatalogKeys.ID, id)
        putString(ExplorerActionCatalogKeys.LABEL_RESOURCE_NAME, labelResourceName)
        putString(ExplorerActionCatalogKeys.LABEL_FALLBACK, labelFallback)
        putString(ExplorerActionCatalogKeys.ACTIVITY_CLASS_NAME, activityClassName)
        putInt(ExplorerActionCatalogKeys.PRIORITY, priority)
        putInt(ExplorerActionCatalogKeys.TARGET_KIND, ExplorerActionValues.TARGET_FILE)
        putInt(ExplorerActionCatalogKeys.ACCESS_MODE, ExplorerActionValues.ACCESS_READ_ONLY)
        putInt(ExplorerActionCatalogKeys.PLACEMENT, ExplorerActionValues.PLACEMENT_OVERFLOW)
        putStringArrayList(
            ExplorerActionCatalogKeys.MIME_TYPES,
            ArrayList(ImageToolsPlugin.INPUT_MIME_TYPES.asList()),
        )
        putStringArrayList(
            ExplorerActionCatalogKeys.EXTENSIONS,
            ArrayList(ImageToolsPlugin.INPUT_EXTENSIONS.asList()),
        )
        putInt(ExplorerActionCatalogKeys.OUTPUT_MODE, ExplorerActionValues.OUTPUT_CREATE_SIBLING)
        putStringArrayList(
            ExplorerActionCatalogKeys.OUTPUT_MIME_TYPES,
            ArrayList(ImageToolsPlugin.OUTPUT_MIME_TYPES.asList()),
        )
        putString(ExplorerActionCatalogKeys.OUTPUT_NAME_SUFFIX, outputSuffix)
        putLong(ExplorerActionCatalogKeys.MAX_OUTPUT_BYTES, ImageToolsPlugin.MAX_OUTPUT_BYTES)
    }

    return Bundle().apply {
        putInt(ExplorerActionCatalogKeys.PROTOCOL_VERSION, ExplorerActionProtocol.VERSION)
        putParcelableArrayList(
            ExplorerActionCatalogKeys.ACTIONS,
            arrayListOf(
                action(
                    id = ImageToolsPlugin.EDIT_ACTION_ID,
                    labelResourceName = "action_edit_image",
                    labelFallback = "Edit image",
                    activityClassName = "${ImageToolsPlugin.PACKAGE_NAME}.ImageEditorActivity",
                    priority = 110,
                    outputSuffix = "edited",
                ),
                action(
                    id = ImageToolsPlugin.CONVERT_ACTION_ID,
                    labelResourceName = "action_convert_image",
                    labelFallback = "Convert image",
                    activityClassName = "${ImageToolsPlugin.PACKAGE_NAME}.ImageConverterActivity",
                    priority = 100,
                    outputSuffix = "converted",
                ),
            ),
        )
    }
}
