package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.autojs.plugin.explorer.api.IExplorerActionHostSession

/** Signature-protected bridge from AutoJs6 into the private image viewer. */
class ExplorerActionActivity : Activity() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var hostSession: IExplorerActionHostSession? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) {
            finish()
            return
        }
        val explorerRequest = ImageRequestPolicy.resolveExplorer(intent)
        if (explorerRequest == null) {
            finish()
            return
        }
        hostSession = explorerRequest.hostSession
        scope.launch {
            val request = withTimeoutOrNull(readabilityTimeoutMillis(explorerRequest.targets.size)) {
                withContext(Dispatchers.IO) { buildViewerRequest(explorerRequest) }
            }
            if (request == null) {
                Toast.makeText(
                    this@ExplorerActionActivity,
                    R.string.error_cannot_read_image,
                    Toast.LENGTH_LONG,
                ).show()
            } else {
                runCatching {
                    startActivity(ImageViewerContract.viewerIntent(this@ExplorerActionActivity, request))
                }.onSuccess {
                    if (request.hostSession != null) {
                        // ImageViewerActivity now owns the short-lived host capability.
                        hostSession = null
                    }
                }.onFailure {
                    Toast.makeText(
                        this@ExplorerActionActivity,
                        R.string.error_cannot_read_image,
                        Toast.LENGTH_LONG,
                    ).show()
                }
            }
            closeHostSession()
            finish()
        }
    }

    override fun onDestroy() {
        scope.cancel()
        closeHostSession()
        super.onDestroy()
    }

    private fun buildViewerRequest(request: ExplorerImageRequest): ImageViewerLaunchRequest? {
        return when (request.mode) {
            ExplorerImageRequestMode.SINGLE_WITH_SIBLINGS -> buildSiblingViewerRequest(request)
            ExplorerImageRequestMode.EXPLICIT_SELECTION -> buildSelectionViewerRequest(request)
        }
    }

    private fun buildSelectionViewerRequest(
        request: ExplorerImageRequest,
    ): ImageViewerLaunchRequest? {
        val selected = request.targets.map { target ->
            ImageContentValidator.validateExplorer(contentResolver, target.image) ?: return null
        }
        return ImageViewerLaunchRequest.explicitSelection(selected)
    }

    private fun buildSiblingViewerRequest(
        request: ExplorerImageRequest,
    ): ImageViewerLaunchRequest? {
        val target = request.targets.single()
        val selected = ImageContentValidator.validateExplorer(contentResolver, target.image)
            ?: return null
        val gallery = runCatching { ExplorerSiblingImageClient.discover(request) }.getOrNull()
            ?: return ImageViewerLaunchRequest.single(selected)
        if (gallery.pages.size <= 1) return ImageViewerLaunchRequest.single(selected)

        val pages = gallery.pages.mapIndexed { index, page ->
            if (index == gallery.startIndex) {
                ImageViewerPage(image = selected, hostRelativePath = "")
            } else {
                ImageViewerPage(
                    image = ImageViewerRequest(
                        targetUri = ImageViewerContract.hostImageUri(index),
                        displayName = page.displayName,
                        declaredSize = page.declaredSize,
                        mimeType = page.mimeType,
                    ),
                    hostRelativePath = page.relativePath,
                )
            }
        }
        return ImageViewerLaunchRequest(
            pages = pages,
            startIndex = gallery.startIndex,
            hostSession = requireNotNull(request.hostSession),
            hostTargetId = target.id,
        )
    }

    private fun closeHostSession() {
        hostSession?.let { session -> runCatching { session.close() } }
        hostSession = null
    }

    private companion object {
        const val BASE_READABILITY_TIMEOUT_MS = 15_000L
        const val EXTRA_TARGET_TIMEOUT_MS = 250L
        const val MAX_READABILITY_TIMEOUT_MS = 45_000L

        fun readabilityTimeoutMillis(targetCount: Int): Long =
            (BASE_READABILITY_TIMEOUT_MS +
                (targetCount - 1).coerceAtLeast(0) * EXTRA_TARGET_TIMEOUT_MS)
                .coerceAtMost(MAX_READABILITY_TIMEOUT_MS)
    }
}
