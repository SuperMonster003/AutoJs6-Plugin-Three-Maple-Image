@file:Suppress("DEPRECATION")

package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.content.ComponentName
import android.content.Intent
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4
import org.autojs.plugin.common.api.PluginCapabilityKeys
import org.autojs.plugin.explorer.api.ExplorerActionCapabilityKeys
import org.autojs.plugin.explorer.api.ExplorerActionCatalogKeys
import org.autojs.plugin.explorer.api.ExplorerActionValues
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ExplorerActionServiceInstrumentationTest {

    @Test
    fun serviceReturnsBinderForExplicitActionlessBinding() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val intent = Intent().setComponent(ComponentName(context, ExplorerActionService::class.java))

        assertNotNull(ExplorerActionService().onBind(intent))
    }

    @Test
    fun serviceAdvertisesV12SingleAndSelectionActionsWithHostBaseline() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val info = context.imageViewerPluginInfo()
        val capabilities = requireNotNull(info.capabilities)
        val catalog = imageViewerActionCatalog()
        val actions = requireNotNull(
            catalog.getParcelableArrayList<android.os.Bundle>(ExplorerActionCatalogKeys.ACTIONS),
        )
        assertEquals(4, actions.size)
        val convertAction = requireNotNull(
            actions.singleOrNull { action ->
                action.getString(ExplorerActionCatalogKeys.ID) == ThreeMapleImagePlugin.CONVERT_IMAGES_ACTION_ID
            },
        )
        // ROADMAP 7.6: a multi-target selection toolbar action that may create files next to its targets.
        assertEquals(ExplorerActionValues.TARGET_FILE, convertAction.getInt(ExplorerActionCatalogKeys.TARGET_KIND))
        assertEquals(ExplorerActionValues.CARDINALITY_MULTIPLE, convertAction.getInt(ExplorerActionCatalogKeys.CARDINALITY))
        assertEquals(ExplorerActionValues.ACCESS_CREATE_IN_PARENT, convertAction.getInt(ExplorerActionCatalogKeys.ACCESS_MODE))
        assertEquals(ExplorerActionValues.PLACEMENT_SELECTION_TOOLBAR, convertAction.getInt(ExplorerActionCatalogKeys.PLACEMENT))
        assertEquals(
            ThreeMapleImagePlugin.CONVERT_IMAGES_ACTIVITY_CLASS_NAME,
            convertAction.getString(ExplorerActionCatalogKeys.ACTIVITY_CLASS_NAME),
        )
        assertFalse(convertAction.containsKey(ExplorerActionCatalogKeys.OUTPUT_MODE))
        val directoryAction = requireNotNull(
            actions.singleOrNull { action ->
                action.getString(ExplorerActionCatalogKeys.ID) == ThreeMapleImagePlugin.BROWSE_DIRECTORY_ACTION_ID
            },
        )
        // ROADMAP 6.4: a read-only, single-directory overflow action without matchers.
        assertEquals(ExplorerActionValues.TARGET_DIRECTORY, directoryAction.getInt(ExplorerActionCatalogKeys.TARGET_KIND))
        assertEquals(ExplorerActionValues.CARDINALITY_SINGLE, directoryAction.getInt(ExplorerActionCatalogKeys.CARDINALITY))
        assertEquals(ExplorerActionValues.ACCESS_READ_ONLY, directoryAction.getInt(ExplorerActionCatalogKeys.ACCESS_MODE))
        assertEquals(ExplorerActionValues.PLACEMENT_OVERFLOW, directoryAction.getInt(ExplorerActionCatalogKeys.PLACEMENT))
        assertEquals(
            ThreeMapleImagePlugin.BROWSE_DIRECTORY_ACTIVITY_CLASS_NAME,
            directoryAction.getString(ExplorerActionCatalogKeys.ACTIVITY_CLASS_NAME),
        )
        assertEquals(emptyList<String>(), directoryAction.getStringArrayList(ExplorerActionCatalogKeys.MIME_TYPES))
        assertEquals(emptyList<String>(), directoryAction.getStringArrayList(ExplorerActionCatalogKeys.EXTENSIONS))
        val singleAction = requireNotNull(
            actions.singleOrNull { action ->
                action.getString(ExplorerActionCatalogKeys.ID) == ThreeMapleImagePlugin.ACTION_ID
            },
        )
        val multipleAction = requireNotNull(
            actions.singleOrNull { action ->
                action.getString(ExplorerActionCatalogKeys.ID) == ThreeMapleImagePlugin.MULTIPLE_ACTION_ID
            },
        )

        assertEquals(
            ThreeMapleImagePlugin.PROTOCOL_VERSION,
            capabilities.getInt(ExplorerActionCapabilityKeys.PROTOCOL_VERSION),
        )
        assertEquals(
            ThreeMapleImagePlugin.REQUIRED_HOST_VERSION,
            capabilities.getLong(PluginCapabilityKeys.REQUIRES_HOST_VERSION),
        )
        assertEquals(
            ThreeMapleImagePlugin.PROTOCOL_VERSION,
            catalog.getInt(ExplorerActionCatalogKeys.PROTOCOL_VERSION),
        )
        assertEquals(
            ExplorerActionValues.CARDINALITY_SINGLE,
            singleAction.getInt(ExplorerActionCatalogKeys.CARDINALITY),
        )
        assertEquals(
            ExplorerActionValues.PLACEMENT_PRIMARY,
            singleAction.getInt(ExplorerActionCatalogKeys.PLACEMENT),
        )
        assertTrue(singleAction.getBoolean(ExplorerActionCatalogKeys.READ_SIBLINGS))
        assertEquals(
            ExplorerActionValues.CARDINALITY_MULTIPLE,
            multipleAction.getInt(ExplorerActionCatalogKeys.CARDINALITY),
        )
        assertEquals(
            ExplorerActionValues.PLACEMENT_SELECTION_TOOLBAR,
            multipleAction.getInt(ExplorerActionCatalogKeys.PLACEMENT),
        )
        assertFalse(multipleAction.getBoolean(ExplorerActionCatalogKeys.READ_SIBLINGS))
        assertEquals(
            ExplorerActionValues.ACCESS_READ_ONLY,
            multipleAction.getInt(ExplorerActionCatalogKeys.ACCESS_MODE),
        )
        assertEquals(
            listOf("avif", "bmp", "gif", "heic", "heif", "jfif", "jpe", "jpeg", "jpg", "png", "webp"),
            singleAction.getStringArrayList(ExplorerActionCatalogKeys.EXTENSIONS),
        )
        assertEquals(
            singleAction.getStringArrayList(ExplorerActionCatalogKeys.EXTENSIONS),
            multipleAction.getStringArrayList(ExplorerActionCatalogKeys.EXTENSIONS),
        )
        assertEquals(
            singleAction.getStringArrayList(ExplorerActionCatalogKeys.EXTENSIONS),
            convertAction.getStringArrayList(ExplorerActionCatalogKeys.EXTENSIONS),
        )
    }
}
