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
            // Conversion and tool requests belong to their own Activities, never to the viewer.
            ExplorerImageRequestMode.BATCH_CONVERT,
            ExplorerImageRequestMode.EDIT_TOOL,
            ExplorerImageRequestMode.CONVERT_TOOL,
            -> null
        }
    }

    private fun buildSelectionViewerRequest(
        request: ExplorerImageRequest,
    ): ImageViewerLaunchRequest? {
        val selected = request.targets.map { target ->
            ImageContentValidator.validateExplorer(contentResolver, target.image) ?: return null
        }
        val session = request.hostSession ?: return ImageViewerLaunchRequest.explicitSelection(selected)
        // Every page is a request target, so each may be moved to the host Trash (ROADMAP 7.2, v24).
        return ImageViewerLaunchRequest(
            pages = selected.mapIndexed { index, image -> ImageViewerPage(image, hostTargetId = request.targets[index].id) },
            hostSession = session,
            hostTargetId = request.targets.first().id,
        )
    }

    private fun buildSiblingViewerRequest(
        request: ExplorerImageRequest,
    ): ImageViewerLaunchRequest? {
        val target = request.targets.single()
        val selected = ImageContentValidator.validateExplorer(contentResolver, target.image)
            ?: return null
        val session = requireNotNull(request.hostSession)
        // Without siblings the viewer still keeps the session: the selected image is a request target it may delete.
        val alone = ImageViewerLaunchRequest(
            pages = listOf(ImageViewerPage(selected, hostTargetId = target.id)),
            hostSession = session,
            hostTargetId = target.id,
        )
        val gallery = runCatching { ExplorerSiblingImageClient.discover(request) }.getOrNull() ?: return alone
        if (gallery.pages.size <= 1) return alone

        val pages = gallery.pages.mapIndexed { index, page ->
            if (index == gallery.startIndex) {
                ImageViewerPage(image = selected, hostRelativePath = "", hostTargetId = target.id)
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
            hostSession = session,
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
