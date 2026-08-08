package io.github.supermonster003.autojs6.plugin.imagetools

import android.content.ComponentName
import android.content.Intent
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
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
}
