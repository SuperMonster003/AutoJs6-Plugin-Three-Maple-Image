package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.content.Intent
import android.os.Bundle
import org.autojs.plugin.explorer.api.ExplorerActionCatalogKeys
import org.autojs.plugin.explorer.api.ExplorerActionPluginActions
import org.autojs.plugin.explorer.api.ExplorerActionPluginPermissions
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/** The host accepts a catalog Activity only when Android also resolves its execute intent. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExplorerActionManifestTest {
    @Suppress("DEPRECATION")
    @Test
    fun everyCatalogActivityIsDiscoverableWithTheHostExecuteIntent() {
        val context = RuntimeEnvironment.getApplication()
        val intent = Intent(ExplorerActionPluginActions.EXECUTE).apply {
            setPackage(context.packageName)
            addCategory(Intent.CATEGORY_DEFAULT)
        }
        val activities = context.packageManager.queryIntentActivities(intent, 0)
            .map { it.activityInfo }
            .associateBy { it.name }
        val actions = threeMapleImageActionCatalog()
            .getParcelableArrayList<Bundle>(ExplorerActionCatalogKeys.ACTIONS).orEmpty()
        assertTrue("The merged catalog must contain actions", actions.isNotEmpty())

        actions.forEach { action ->
            val id = action.getString(ExplorerActionCatalogKeys.ID)
            val className = action.getString(ExplorerActionCatalogKeys.ACTIVITY_CLASS_NAME)
            val activity = activities[className]
            assertNotNull("$id is hidden by the host: $className does not resolve the execute intent", activity)
            requireNotNull(activity)
            assertEquals(context.packageName, activity.packageName)
            assertEquals(ExplorerActionPluginPermissions.PLUGIN, activity.permission)
            assertTrue("$id must be exported and enabled", activity.exported && activity.enabled && activity.applicationInfo.enabled)
        }
    }
}
