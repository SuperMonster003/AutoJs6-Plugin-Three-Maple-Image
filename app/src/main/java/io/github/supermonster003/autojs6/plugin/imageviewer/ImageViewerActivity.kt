package io.github.supermonster003.autojs6.plugin.imageviewer

import android.content.ClipData
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.text.format.Formatter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import io.github.supermonster003.autojs6.plugin.imageviewer.databinding.ActivityImageViewerBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ImageViewerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityImageViewerBinding
    private lateinit var request: ImageViewerRequest
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var controlsVisible = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        request = ImageRequestPolicy.resolveInternal(intent) ?: run {
            finish()
            return
        }
        binding = ActivityImageViewerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.title = request.displayName
        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.image.setOnClickListener { setControlsVisible(!controlsVisible) }
        binding.resetZoom.setOnClickListener { binding.image.resetZoom() }
        binding.share.setOnClickListener { shareImage() }
        binding.openExternal.setOnClickListener { openWithAnotherApp() }

        loadImage()
        loadMetadata()
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    private fun loadImage() {
        binding.loadingIndicator.isVisible = true
        binding.errorText.isVisible = false
        Glide.with(this)
            .load(request.targetUri)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    error: GlideException?,
                    model: Any?,
                    target: Target<Drawable>,
                    isFirstResource: Boolean,
                ): Boolean {
                    showLoadFailure()
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable,
                    model: Any,
                    target: Target<Drawable>?,
                    dataSource: DataSource,
                    isFirstResource: Boolean,
                ): Boolean {
                    binding.loadingIndicator.isVisible = false
                    binding.errorText.isVisible = false
                    return false
                }
            })
            .into(binding.image)
    }

    private fun loadMetadata() {
        scope.launch {
            val metadata = withContext(Dispatchers.IO) {
                ImageContentValidator.readMetadata(contentResolver, request)
            }
            if (isFinishing || isDestroyed) return@launch
            binding.metadata.text = if (metadata == null) {
                getString(R.string.metadata_unavailable)
            } else {
                val size = Formatter.formatFileSize(this@ImageViewerActivity, metadata.byteSize)
                val resolution = if (metadata.width != null && metadata.height != null) {
                    getString(R.string.metadata_resolution, metadata.width, metadata.height)
                } else {
                    getString(R.string.metadata_resolution_unknown)
                }
                getString(R.string.metadata_summary, request.mimeType, size, resolution)
            }
        }
    }

    private fun showLoadFailure() {
        binding.loadingIndicator.isVisible = false
        binding.errorText.isVisible = true
    }

    private fun setControlsVisible(visible: Boolean) {
        controlsVisible = visible
        binding.toolbar.isVisible = visible
        binding.informationPanel.isVisible = visible
    }

    private fun shareImage() {
        val target = Intent(Intent.ACTION_SEND).apply {
            type = request.mimeType
            clipData = ClipData.newRawUri(request.displayName, request.targetUri)
            putExtra(Intent.EXTRA_STREAM, request.targetUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startSafely(Intent.createChooser(target, getString(R.string.action_share)))
    }

    private fun openWithAnotherApp() {
        val target = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(request.targetUri, request.mimeType)
            clipData = ClipData.newRawUri(request.displayName, request.targetUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val ownEntry = ComponentName(this, ExternalViewActivity::class.java)
        if (!hasExternalHandler(target, ownEntry)) {
            Toast.makeText(this, R.string.error_no_external_viewer, Toast.LENGTH_SHORT).show()
            return
        }
        val chooser = Intent.createChooser(target, getString(R.string.action_open_external)).apply {
            putExtra(Intent.EXTRA_EXCLUDE_COMPONENTS, arrayOf(ownEntry))
        }
        startSafely(chooser)
    }

    private fun hasExternalHandler(intent: Intent, excluded: ComponentName): Boolean {
        val matches = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            packageManager.queryIntentActivities(intent, 0)
        }
        return matches.any { info ->
            info.activityInfo?.let { ComponentName(it.packageName, it.name) != excluded } == true
        }
    }

    private fun startSafely(intent: Intent) {
        runCatching { startActivity(intent) }.onFailure {
            Toast.makeText(this, R.string.error_no_external_viewer, Toast.LENGTH_SHORT).show()
        }
    }
}
