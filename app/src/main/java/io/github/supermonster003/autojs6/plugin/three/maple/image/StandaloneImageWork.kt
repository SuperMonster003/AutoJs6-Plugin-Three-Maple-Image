package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.content.FileProvider
import org.json.JSONObject
import java.io.File
import java.io.InputStream
import java.util.UUID

/** Private standalone sessions never broaden the exported host's strict URI grant contract. */
internal object StandaloneImageWork {
    const val EXTRA_SESSION = "io.github.supermonster003.autojs6.plugin.three.maple.image.extra.STANDALONE_SESSION"

    /** Result extra of a standalone tool: the output replaces the original instead of becoming a new file. */
    const val EXTRA_OVERWRITE = "io.github.supermonster003.autojs6.plugin.three.maple.image.extra.STANDALONE_OVERWRITE"
    private val sessionPattern = Regex("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")
    private fun directory(context: Context, id: String): File {
        require(sessionPattern.matches(id))
        val root = File(context.cacheDir, "standalone-images").canonicalFile
        return File(root, id).canonicalFile.also { require(it.parentFile == root) }
    }
    /** [overwritable]: the caller can write [document] back, so the tool may offer to overwrite it (ROADMAP 8.2). */
    fun create(context: Context, document: StandaloneDocument, action: String, overwritable: Boolean = false): String {
        require(document.size <= ImageToolsRequestPolicy.MAX_INPUT_BYTES)
        return create(context, document.name, document.mimeType, action, overwritable) { context.contentResolver.openInputStream(document.uri) }
    }

    /**
     * A session whose input is read from [open] (a script's source descriptor, ROADMAP 10.2); [mimeType] null
     * detects the type from the copied image. [saveDefaults] seed the editor's save dialog.
     */
    fun create(
        context: Context,
        name: String,
        mimeType: String?,
        action: String,
        overwritable: Boolean = false,
        saveDefaults: EditorSaveOptions? = null,
        open: () -> InputStream?,
    ): String {
        require(action == ImageToolsPlugin.EDIT_ACTION_ID || action == ImageToolsPlugin.CONVERT_ACTION_ID)
        val id = UUID.randomUUID().toString()
        val directory = directory(context, id).apply { check(mkdirs()) }
        try {
            val input = File(directory, "input.bin")
            open()?.use { source ->
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
            val detected = mimeType ?: BitmapFactory.Options().apply { inJustDecodeBounds = true }
                .also { BitmapFactory.decodeFile(input.path, it) }.outMimeType
            requireNotNull(detected) { "Input is not a decodable image" }
            val metadata = JSONObject().put("action", action).put("name", name)
                .put("mime", detected).put("size", input.length()).put("overwrite", overwritable)
            saveDefaults?.let { metadata.put("saveDefaults", saveDefaultsJson(it)) }
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
            ImageToolsPlugin.MAX_OUTPUT_BYTES, if (expectedAction == ImageToolsPlugin.EDIT_ACTION_ID) "edited" else "converted",
            canOverwrite = metadata.optBoolean("overwrite", false),
            initialSaveOptions = metadata.optJSONObject("saveDefaults")?.let(::saveDefaults))
    }.getOrNull()

    private fun saveDefaultsJson(options: EditorSaveOptions): JSONObject = JSONObject()
        .put("format", options.format.name).put("quality", options.quality).put("lossless", options.webpLossless)
        .put("keepExif", options.keepExif).put("keepLocation", options.keepLocation)

    private fun saveDefaults(json: JSONObject): EditorSaveOptions? = runCatching {
        EditorSaveOptions(
            format = EditorSaveFormat.valueOf(json.getString("format")),
            quality = json.getInt("quality"),
            webpLossless = json.optBoolean("lossless", false),
            keepExif = json.optBoolean("keepExif", false),
            keepLocation = json.optBoolean("keepLocation", false),
        )
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
    /** The tool's output of session [id], checked to be an image the tools write. */
    fun outputFile(context: Context, id: String): File {
        outputMime(context, id)
        return File(directory(context, id), "output.bin")
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
