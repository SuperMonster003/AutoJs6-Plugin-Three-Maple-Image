package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.content.FileProvider
import org.json.JSONObject
import java.io.File
import java.util.UUID

/** Private standalone sessions never broaden the exported host's strict URI grant contract. */
internal object StandaloneImageWork {
    const val EXTRA_SESSION = "io.github.supermonster003.autojs6.plugin.three.maple.image.extra.STANDALONE_SESSION"
    private val sessionPattern = Regex("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")
    private fun directory(context: Context, id: String): File {
        require(sessionPattern.matches(id))
        val root = File(context.cacheDir, "standalone-images").canonicalFile
        return File(root, id).canonicalFile.also { require(it.parentFile == root) }
    }
    fun create(context: Context, document: StandaloneDocument, action: String): String {
        require(action == ImageToolsPlugin.EDIT_ACTION_ID || action == ImageToolsPlugin.CONVERT_ACTION_ID)
        require(document.size <= ImageToolsRequestPolicy.MAX_INPUT_BYTES)
        val id = UUID.randomUUID().toString()
        val directory = directory(context, id).apply { check(mkdirs()) }
        try {
            val input = File(directory, "input.bin")
            context.contentResolver.openInputStream(document.uri)?.use { source ->
                input.outputStream().use { output ->
                    val buffer = ByteArray(64 * 1024)
                    var bytes = 0L
                    while (true) {
                        val count = source.read(buffer)
                        if (count < 0) break
                        bytes += count
                        require(bytes <= ImageToolsRequestPolicy.MAX_INPUT_BYTES)
                        output.write(buffer, 0, count)
                    }
                }
            } ?: error("Input is unavailable")
            require(input.length() > 0)
            val metadata = JSONObject().put("action", action).put("name", document.name)
                .put("mime", document.mimeType).put("size", input.length())
            File(directory, "session.json").writeText(metadata.toString())
            return id
        } catch (error: Throwable) {
            discard(context, id)
            throw error
        }
    }
    fun resolve(context: Context, intent: Intent, expectedAction: String): ImageToolsRequest? = runCatching {
        val id = intent.getStringExtra(EXTRA_SESSION) ?: return null
        val folder = directory(context, id)
        val metadata = JSONObject(File(folder, "session.json").readText())
        require(metadata.getString("action") == expectedAction)
        val input = File(folder, "input.bin")
        val size = metadata.getLong("size")
        require(size in 1..ImageToolsRequestPolicy.MAX_INPUT_BYTES && input.length() == size)
        val name = ImageToolsRequestPolicy.validateDisplayName(metadata.getString("name")) ?: return null
        val authority = context.packageName + ".standalone-images"
        ImageToolsRequest(expectedAction,
            FileProvider.getUriForFile(context, authority, input),
            FileProvider.getUriForFile(context, authority, File(folder, "output.bin")),
            id, name, size, metadata.getString("mime"), ImageToolsPlugin.OUTPUT_MIME_TYPES.toSet(),
            ImageToolsPlugin.MAX_OUTPUT_BYTES, if (expectedAction == ImageToolsPlugin.EDIT_ACTION_ID) "edited" else "converted")
    }.getOrNull()
    fun hasOutput(context: Context, id: String): Boolean = runCatching {
        File(directory(context, id), "output.bin").length() > 0
    }.getOrDefault(false)
    fun outputMime(context: Context, id: String): String {
        val file = File(directory(context, id), "output.bin")
        require(file.length() in 1..ImageToolsPlugin.MAX_OUTPUT_BYTES)
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.path, options)
        require(options.outWidth > 0 && options.outHeight > 0)
        return requireNotNull(options.outMimeType).also { require(it in ImageToolsPlugin.OUTPUT_MIME_TYPES) }
    }
    fun export(context: Context, id: String, target: Uri) {
        require(target.scheme == "content" && target.authority != context.packageName + ".standalone-images")
        outputMime(context, id)
        val output = File(directory(context, id), "output.bin")
        context.contentResolver.openOutputStream(target, "wt")?.use { destination ->
            output.inputStream().use { source -> source.copyTo(destination) }
        } ?: error("Destination is unavailable")
        discard(context, id)
    }
    fun discard(context: Context, id: String) {
        val folder = directory(context, id)
        listOf("input.bin", "output.bin", "session.json").forEach { File(folder, it).delete() }
        folder.delete()
    }
}

class StandaloneImageEditorActivity : ImageEditorActivity() {
    internal override fun resolveRequest(): ImageToolsRequest? =
        StandaloneImageWork.resolve(this, intent, ImageToolsPlugin.EDIT_ACTION_ID)
}

class StandaloneImageConverterActivity : ImageConverterActivity() {
    internal override fun resolveRequest(): ImageToolsRequest? =
        StandaloneImageWork.resolve(this, intent, ImageToolsPlugin.CONVERT_ACTION_ID)
}
