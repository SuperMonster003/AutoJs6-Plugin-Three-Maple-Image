package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.content.Intent
import android.graphics.Bitmap
import androidx.core.content.FileProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.autojs.plugin.explorer.api.ExplorerActionCatalogKeys
import org.autojs.plugin.explorer.api.ExplorerActionValues
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class StandaloneImageWorkInstrumentationTest {
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Test fun mergedCatalogKeepsAllFourActionsAndOutputContracts() {
        @Suppress("DEPRECATION")
        val actions = requireNotNull(threeMapleImageActionCatalog().getParcelableArrayList<android.os.Bundle>(ExplorerActionCatalogKeys.ACTIONS))
        assertEquals(setOf("view-image", "view-images", "edit-image", "convert-image"), actions.map { it.getString(ExplorerActionCatalogKeys.ID) }.toSet())
        for (action in actions.filter { it.getString(ExplorerActionCatalogKeys.ID) in setOf("edit-image", "convert-image") }) {
            assertEquals(ExplorerActionValues.CARDINALITY_SINGLE, action.getInt(ExplorerActionCatalogKeys.CARDINALITY))
            assertEquals(ExplorerActionValues.OUTPUT_CREATE_SIBLING, action.getInt(ExplorerActionCatalogKeys.OUTPUT_MODE))
            assertEquals(ExplorerActionValues.ACCESS_READ_ONLY, action.getInt(ExplorerActionCatalogKeys.ACCESS_MODE))
        }
    }

    @Test fun privateSessionsOpenTheRealEditorWithoutWeakeningTheHostIngress() {
        val fixture = File(context.cacheDir, "standalone-images/fixture-${UUID.randomUUID()}.png")
        fixture.parentFile!!.mkdirs()
        val bitmap = Bitmap.createBitmap(64, 48, Bitmap.Config.ARGB_8888).apply { eraseColor(0xff336699.toInt()) }
        fixture.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()
        var session: String? = null
        try {
            val uri = FileProvider.getUriForFile(context, context.packageName + ".standalone-images", fixture)
            val document = StandaloneDocuments.inspect(context, uri)
            session = StandaloneImageWork.create(context, document, ImageToolsPlugin.EDIT_ACTION_ID)
            val intent = Intent(context, StandaloneImageEditorActivity::class.java).putExtra(StandaloneImageWork.EXTRA_SESSION, session)
            assertNull(ImageToolsRequestPolicy.resolve(context, intent, ImageToolsPlugin.EDIT_ACTION_ID))
            assertNotNull(StandaloneImageWork.resolve(context, intent, ImageToolsPlugin.EDIT_ACTION_ID))
            assertNull(StandaloneImageWork.resolve(context, intent, ImageToolsPlugin.CONVERT_ACTION_ID))
            assertNull(StandaloneImageWork.resolve(context, Intent().putExtra(StandaloneImageWork.EXTRA_SESSION, "../outside"), ImageToolsPlugin.EDIT_ACTION_ID))
            ActivityScenario.launch<StandaloneImageEditorActivity>(intent).use { scenario ->
                scenario.onActivity { activity -> assertFalse(activity.isFinishing) }
            }
        } finally {
            session?.let { StandaloneImageWork.discard(context, it) }
            fixture.delete()
        }
    }
}
