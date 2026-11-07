package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.content.ComponentName
import android.content.Intent
import org.autojs.plugin.explorer.api.ExplorerActionCatalogKeys
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExplorerActionServiceTest {

    @Test
    fun explicitComponentWithoutActionReturnsBinder() {
        val controller = Robolectric.buildService(ExplorerActionService::class.java).create()
        val service = controller.get()
        val intent = Intent().setComponent(ComponentName(service, ExplorerActionService::class.java))

        assertNotNull(service.onBind(intent))

        controller.destroy()
    }

    /** ROADMAP 8.4 (T18): one protocol for every action, and the host build that brought it, everywhere it is declared. */
    @Test
    fun everyCatalogDeclaresProtocol24AndTheHostBuildThatBroughtIt() {
        assertEquals(24, ThreeMapleImagePlugin.PROTOCOL_VERSION)
        assertEquals(5352L, ThreeMapleImagePlugin.REQUIRED_HOST_VERSION)
        assertEquals(ThreeMapleImagePlugin.REQUIRED_HOST_VERSION, ImageToolsPlugin.REQUIRED_HOST_VERSION)
        assertEquals(24, threeMapleImageActionCatalog().getInt(ExplorerActionCatalogKeys.PROTOCOL_VERSION))
        assertEquals(24, imageToolsActionCatalog().getInt(ExplorerActionCatalogKeys.PROTOCOL_VERSION))
        // The manifest meta-data the plugin center reads comes from the same number.
        assertEquals(
            ThreeMapleImagePlugin.REQUIRED_HOST_VERSION.toString(),
            RuntimeEnvironment.getApplication().getString(R.string.plugin_requires_host_version),
        )
    }

    /**
     * Host Explorer Action protocol v27: the catalog names the plugin's first-level menu after its display name, under
     * the two catalog-level keys the host reads from catalogs of every protocol version.
     */
    @Test
    fun theCatalogNamesTheFirstLevelMenuAfterThePlugin() {
        val catalog = threeMapleImageActionCatalog()

        assertEquals("app_name", catalog.getString("menuGroupLabelResourceName"))
        assertEquals("3-Maple Image", catalog.getString("menuGroupLabelFallback"))
        assertEquals(24, catalog.getInt(ExplorerActionCatalogKeys.PROTOCOL_VERSION))
        assertEquals(
            setOf(
                ExplorerActionCatalogKeys.PROTOCOL_VERSION,
                "menuGroupLabelResourceName",
                "menuGroupLabelFallback",
                ExplorerActionCatalogKeys.ACTIONS,
            ),
            catalog.keySet(),
        )
        val application = RuntimeEnvironment.getApplication()
        val resourceId = application.resources.getIdentifier("app_name", "string", application.packageName)
        assertEquals("3-Maple Image", application.getString(resourceId))
    }
}
