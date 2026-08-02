package io.github.supermonster003.autojs6.plugin.imageviewer

import android.app.Activity
import android.os.Bundle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ExplorerActionActivity : Activity() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val request = ImageRequestPolicy.resolveExplorer(intent)
        if (request == null) {
            finish()
            return
        }
        scope.launch {
            val validated = withContext(Dispatchers.IO) {
                ImageContentValidator.validateExplorer(contentResolver, request)
            }
            if (validated != null && !isFinishing && !isDestroyed) {
                runCatching { startActivity(ImageRequestPolicy.viewerIntent(validated)) }
            }
            finish()
        }
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }
}
