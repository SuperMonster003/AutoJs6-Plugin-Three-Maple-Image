package io.github.supermonster003.autojs6.plugin.three.maple.image.scriptapi

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import io.github.supermonster003.autojs6.plugin.three.maple.image.EditorSaveFormat
import io.github.supermonster003.autojs6.plugin.three.maple.image.EditorSaveOptions
import io.github.supermonster003.autojs6.plugin.three.maple.image.ImageConversionOptions
import io.github.supermonster003.autojs6.plugin.three.maple.image.ImageToolsPlugin
import io.github.supermonster003.autojs6.plugin.three.maple.image.ImageViewerContract
import io.github.supermonster003.autojs6.plugin.three.maple.image.ImageViewerLaunchRequest
import io.github.supermonster003.autojs6.plugin.three.maple.image.ImageViewerPage
import io.github.supermonster003.autojs6.plugin.three.maple.image.ImageViewerRequest
import io.github.supermonster003.autojs6.plugin.three.maple.image.StandaloneImageEditorActivity
import io.github.supermonster003.autojs6.plugin.three.maple.image.StandaloneImageWork
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.autojs.plugin.explorer.api.ExplorerActionIntentExtras
import org.autojs.plugin.gallery.api.GalleryActions
import org.autojs.plugin.gallery.api.GalleryContract
import org.autojs.plugin.gallery.api.GalleryErrorCodes
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.lang.ref.WeakReference
import java.nio.ByteBuffer

/**
 * The Activity the host starts for an interactive gallery session (ROADMAP 10.2): it claims the
 * session with the token the host passed, starts the session's UI (the picker, the viewer or the
 * standalone editor) and stays invisible beneath it to collect the outcome. It reports `open` and
 * the visibility of that UI, the viewer's start page, the picker's choice and the editor's saved
 * output (written into the host's target here), and ends the session with `user` when the UI
 * returns without a result. The host's `close` finishes the UI unless it asked to leave a viewer
 * open.
 */
class GallerySessionActivity : ComponentActivity(), SessionUi {

    private var session: ScriptSession? = null
    private var generation = 0L
    private var imagesToken: String? = null
    private var editId: String? = null
    private var launched = false
    private var startPageReported = false
    private var child: WeakReference<Activity>? = null

    private val childResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult(), ::onChildResult)

    private val lifecycleCallbacks = object : Application.ActivityLifecycleCallbacks {
        override fun onActivityResumed(activity: Activity) {
            if (!isOwnChild(activity)) return
            child = WeakReference(activity)
            val current = session ?: return
            current.onVisible()
            val viewer = current.payload() as? SessionPayload.Viewer
            if (viewer != null && !startPageReported) {
                startPageReported = true
                current.page(viewer.startIndex)
            }
        }

        override fun onActivityPaused(activity: Activity) {
            if (isOwnChild(activity)) session?.onHidden()
        }

        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit
        override fun onActivityStarted(activity: Activity) = Unit
        override fun onActivityStopped(activity: Activity) = Unit
        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
        override fun onActivityDestroyed(activity: Activity) {
            if (child?.get() === activity) child = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) {
            generation = savedInstanceState.getLong(STATE_GENERATION)
            imagesToken = savedInstanceState.getString(STATE_IMAGES)
            editId = savedInstanceState.getString(STATE_EDIT)
            launched = savedInstanceState.getBoolean(STATE_LAUNCHED)
            startPageReported = savedInstanceState.getBoolean(STATE_START_PAGE)
            session = ScriptSessionRegistry.claimed(generation)
        } else {
            val token = ScriptSessionTokens.tokenOf(
                intent.action,
                intent.component?.className == GallerySessionActivity::class.java.name,
                intent.getStringExtra(GalleryActions.EXTRA_SESSION_TOKEN),
            )
            session = token?.let(ScriptSessionRegistry::claim)
            generation = session?.generation ?: 0L
        }
        val current = session
        if (current == null && imagesToken == null) {
            cleanUp()
            finish()
            return
        }
        current?.attachUi(this)
        application.registerActivityLifecycleCallbacks(lifecycleCallbacks)
        if (!launched && current != null) launchUi(current)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putLong(STATE_GENERATION, generation)
        outState.putString(STATE_IMAGES, imagesToken)
        outState.putString(STATE_EDIT, editId)
        outState.putBoolean(STATE_LAUNCHED, launched)
        outState.putBoolean(STATE_START_PAGE, startPageReported)
    }

    override fun onDestroy() {
        application.unregisterActivityLifecycleCallbacks(lifecycleCallbacks)
        session?.detachUi(this)
        if (isFinishing) cleanUp()
        super.onDestroy()
    }

    /** The host closed the session with `finish`: the UI goes, and so does this Activity. */
    override fun finishSession() {
        child?.get()?.finish()
        finish()
    }

    private fun launchUi(current: ScriptSession) {
        launched = true
        when (val payload = current.payload()) {
            is SessionPayload.Picker -> childResult.launch(
                Intent(this, GalleryPickerActivity::class.java).putExtra(EXTRA_GENERATION, generation),
            )
            is SessionPayload.Viewer -> launchViewer(current)
            is SessionPayload.Editor -> launchEditor(current, payload)
        }
    }

    private fun launchViewer(current: ScriptSession) {
        val viewer = current.adoptViewer() ?: return finishWith { current.fail(GalleryErrorCodes.SESSION_CLOSED, "the viewer session closed before it opened") }
        val token = runCatching { GallerySessionImages.register(this, viewer) }.getOrElse { error ->
            viewer.close()
            return finishWith { current.fail(GalleryErrorCodes.FILE_UNREADABLE, error.toString()) }
        }
        imagesToken = token
        val pages = GallerySessionImages.pages(token).mapIndexed { index, page ->
            ImageViewerPage(ImageViewerRequest(GallerySessionImages.imageUri(token, index), page.displayName, page.size, page.mimeType))
        }
        val intent = ImageViewerContract.viewerIntent(this, ImageViewerLaunchRequest(pages = pages, startIndex = viewer.startIndex))
            .putExtra(EXTRA_GENERATION, generation)
        childResult.launch(intent)
    }

    private fun launchEditor(current: ScriptSession, editor: SessionPayload.Editor) {
        lifecycleScope.launch {
            val id = runCatching {
                withContext(Dispatchers.IO) {
                    StandaloneImageWork.create(
                        context = this@GallerySessionActivity,
                        name = editor.displayName ?: DEFAULT_EDIT_NAME,
                        mimeType = null,
                        action = ImageToolsPlugin.EDIT_ACTION_ID,
                        saveDefaults = saveDefaults(editor.encode),
                    ) { positionalStream(editor.source) }
                }
            }.getOrElse { error ->
                return@launch finishWith { current.fail(GalleryErrorCodes.UNSUPPORTED_FORMAT, "the editor cannot open the image: $error") }
            }
            editId = id
            childResult.launch(
                Intent(this@GallerySessionActivity, StandaloneImageEditorActivity::class.java)
                    .putExtra(StandaloneImageWork.EXTRA_SESSION, id)
                    .putExtra(EXTRA_GENERATION, generation),
            )
        }
    }

    private fun onChildResult(result: ActivityResult) {
        val current = session
        if (current == null || current.closed) {
            finish()
            return
        }
        when (current.payload()) {
            is SessionPayload.Picker -> {
                val data = result.data
                val items = data?.getStringExtra(GalleryPickerActivity.EXTRA_ITEMS)
                val errorCode = data?.getStringExtra(GalleryPickerActivity.EXTRA_ERROR_CODE)
                when {
                    result.resultCode == RESULT_OK && items != null -> current.pickResult(items)
                    errorCode != null -> current.fail(errorCode, data.getStringExtra(GalleryPickerActivity.EXTRA_ERROR_MESSAGE).orEmpty())
                    else -> current.userLeft()
                }
                finish()
            }
            is SessionPayload.Viewer -> {
                current.userLeft()
                finish()
            }
            is SessionPayload.Editor -> {
                val id = editId
                if (id != null && result.resultCode == RESULT_OK && result.data?.getStringExtra(ExplorerActionIntentExtras.OUTPUT_TRANSACTION_ID) == id) {
                    lifecycleScope.launch {
                        val outcome = runCatching { withContext(Dispatchers.IO) { deliverEdit(current, id) } }
                        outcome.exceptionOrNull()?.let { error -> current.fail(GalleryErrorCodes.IO, "the edited image could not be written: $error") }
                        finish()
                    }
                } else {
                    current.userLeft()
                    finish()
                }
            }
        }
    }

    /** Copies the editor's output into the host's target (from offset 0, truncated) and reports it. */
    private fun deliverEdit(current: ScriptSession, id: String) {
        val output = StandaloneImageWork.outputFile(this, id)
        val mimeType = StandaloneImageWork.outputMime(this, id)
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }.also { BitmapFactory.decodeFile(output.path, it) }
        val size = output.length()
        val written = current.writeTarget { target ->
            val channel = FileOutputStream(target.fileDescriptor).channel
            channel.truncate(0)
            var position = 0L
            FileInputStream(output).use { input ->
                val buffer = ByteArray(64 * 1024)
                while (true) {
                    val count = input.read(buffer)
                    if (count < 0) break
                    val chunk = ByteBuffer.wrap(buffer, 0, count)
                    while (chunk.hasRemaining()) position += channel.write(chunk, position)
                }
            }
            channel.force(true)
        }
        if (written) current.editResult(mimeType, bounds.outWidth, bounds.outHeight, size)
    }

    private fun saveDefaults(encode: EncodeRequest): EditorSaveOptions = EditorSaveOptions(
        format = when (encode.format) {
            GalleryContract.FORMAT_JPEG -> EditorSaveFormat.JPEG
            GalleryContract.FORMAT_PNG -> EditorSaveFormat.PNG
            GalleryContract.FORMAT_WEBP -> EditorSaveFormat.WEBP
            else -> EditorSaveFormat.FOLLOW_SOURCE
        },
        quality = encode.quality ?: ImageConversionOptions.DEFAULT_QUALITY,
        keepExif = encode.keepExif,
        keepLocation = encode.keepExif && encode.keepLocation,
    )

    private inline fun finishWith(block: () -> Unit) {
        block()
        finish()
    }

    private fun isOwnChild(activity: Activity): Boolean =
        activity !== this && activity.intent?.getLongExtra(EXTRA_GENERATION, 0L) == generation && generation != 0L

    private fun cleanUp() {
        GallerySessionImages.unregister(imagesToken)
        imagesToken = null
        editId?.let { id -> runCatching { StandaloneImageWork.discard(this, id) } }
        editId = null
    }

    companion object {
        /** The session generation every UI of a session carries, so this Activity recognizes it. */
        internal const val EXTRA_GENERATION = "io.github.supermonster003.autojs6.plugin.three.maple.image.extra.GALLERY_GENERATION"

        private const val STATE_GENERATION = "gallery.generation"
        private const val STATE_IMAGES = "gallery.images"
        private const val STATE_EDIT = "gallery.edit"
        private const val STATE_LAUNCHED = "gallery.launched"
        private const val STATE_START_PAGE = "gallery.startPage"
        private const val DEFAULT_EDIT_NAME = "image"

        /** The viewer's page hook: a viewer started for a script session reports its page to the host. */
        internal fun onViewerPage(intent: Intent?, index: Int) {
            val generation = intent?.getLongExtra(EXTRA_GENERATION, 0L)?.takeIf { it != 0L } ?: return
            ScriptSessionRegistry.claimed(generation)?.page(index)
        }

        /** Reads a shared descriptor from offset 0 without moving its file offset. */
        internal fun positionalStream(source: android.os.ParcelFileDescriptor): InputStream = object : InputStream() {
            private val channel = FileInputStream(source.fileDescriptor).channel
            private var position = 0L

            override fun read(): Int {
                val one = ByteArray(1)
                return if (read(one, 0, 1) <= 0) -1 else one[0].toInt() and 0xFF
            }

            override fun read(b: ByteArray, off: Int, len: Int): Int {
                if (len == 0) return 0
                val count = channel.read(ByteBuffer.wrap(b, off, len), position)
                if (count > 0) position += count
                return count
            }

            override fun close() = Unit
        }
    }
}
