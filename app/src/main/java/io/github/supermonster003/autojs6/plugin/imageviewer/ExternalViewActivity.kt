package io.github.supermonster003.autojs6.plugin.imageviewer

import android.app.Activity
import android.os.Bundle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ExternalViewActivity : Activity() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val seed = ImageRequestPolicy.resolveExternal(intent)
        if (seed == null) {
            finish()
            return
        }
        scope.launch {
            val request = withContext(Dispatchers.IO) {
                ImageContentValidator.resolveExternal(contentResolver, seed)
            }
            if (request != null && !isFinishing && !isDestroyed) {
                runCatching {
                    startActivity(
                        ImageViewerContract.viewerIntent(
                            this@ExternalViewActivity,
                            ImageViewerLaunchRequest.single(request),
                        ),
                    )
                }
            }
            finish()
        }
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }
}
