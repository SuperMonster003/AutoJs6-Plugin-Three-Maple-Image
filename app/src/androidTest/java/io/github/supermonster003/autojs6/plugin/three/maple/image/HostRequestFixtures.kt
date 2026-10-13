package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.ParcelFileDescriptor
import org.autojs.plugin.explorer.api.ExplorerActionHostSessionKeys
import org.autojs.plugin.explorer.api.ExplorerActionIntentExtras
import org.autojs.plugin.explorer.api.ExplorerActionIntentValues
import org.autojs.plugin.explorer.api.ExplorerActionPluginActions
import org.autojs.plugin.explorer.api.ExplorerActionTargetKeys
import org.autojs.plugin.explorer.api.ExplorerActionValues
import org.autojs.plugin.explorer.api.IExplorerActionHostSession
import java.io.File
import java.util.Locale
import java.util.UUID

/**
 * A host session over a real directory tree, listing and opening files the way the host does. An empty relative
 * path lists [root] itself (the target directory, or the siblings of a file target).
 */
internal class TreeHostSession(private val root: File) : UnsupportedHostSessionStub() {
    val listedPaths = mutableListOf<String>()
    val openedPaths = mutableListOf<String>()
    var closeCount = 0

    override fun listChildren(targetId: String?, relativePath: String?, offset: Int, limit: Int): Bundle {
        val path = relativePath.orEmpty()
        listedPaths += path
        val directory = if (path.isEmpty()) root else File(root, path)
        val children = directory.listFiles().orEmpty().sortedBy { it.name.lowercase(Locale.ROOT) }
        val page = children.drop(offset).take(limit)
        val items = page.map { child ->
            Bundle().apply {
                putString(ExplorerActionHostSessionKeys.RELATIVE_PATH, if (path.isEmpty()) child.name else path + "/" + child.name)
                putString(ExplorerActionHostSessionKeys.DISPLAY_NAME, child.name)
                putInt(ExplorerActionHostSessionKeys.KIND, if (child.isDirectory) ExplorerActionValues.TARGET_DIRECTORY else ExplorerActionValues.TARGET_FILE)
                putString(
                    ExplorerActionHostSessionKeys.MIME_TYPE,
                    when {
                        child.isDirectory -> HostRequestFixtures.DIRECTORY_MIME_TYPE
                        child.extension == "png" -> "image/png"
                        else -> "text/plain"
                    },
                )
                putLong(ExplorerActionHostSessionKeys.SIZE, if (child.isFile) child.length() else -1L)
                putLong(ExplorerActionHostSessionKeys.LAST_MODIFIED, child.lastModified())
                putBoolean(ExplorerActionHostSessionKeys.READABLE, true)
                putBoolean(ExplorerActionHostSessionKeys.SYMBOLIC_LINK, false)
            }
        }
        val nextOffset = offset + page.size
        return Bundle().apply {
            putParcelableArrayList(ExplorerActionHostSessionKeys.ITEMS, ArrayList(items))
            putInt(ExplorerActionHostSessionKeys.NEXT_OFFSET, nextOffset)
            putBoolean(ExplorerActionHostSessionKeys.COMPLETE, nextOffset >= children.size)
        }
    }

    override fun openFile(targetId: String?, relativePath: String?): ParcelFileDescriptor {
        val path = requireNotNull(relativePath)
        openedPaths += path
        return ParcelFileDescriptor.open(File(root, path), ParcelFileDescriptor.MODE_READ_ONLY)
    }

    override fun close() {
        closeCount += 1
    }

    override fun attachClient(clientToken: android.os.IBinder?) = Unit
}

/** Explorer action requests shaped like the host's: "View as album" on a directory, "View image" on one file. */
internal object HostRequestFixtures {
    const val DIRECTORY_TARGET_ID = "directory-target"
    const val DIRECTORY_NAME = "Camera"
    const val DIRECTORY_MIME_TYPE = "inode/directory"
    val PARENT_URI: Uri = Uri.parse("content://images.test/root")
    val DIRECTORY_URI: Uri = Uri.parse("content://images.test/root/Camera")

    fun directoryIntent(context: Context, session: IExplorerActionHostSession): Intent {
        val target = Bundle().apply {
            putString(ExplorerActionTargetKeys.ID, DIRECTORY_TARGET_ID)
            putParcelable(ExplorerActionTargetKeys.URI, DIRECTORY_URI)
            putString(ExplorerActionTargetKeys.DISPLAY_NAME, DIRECTORY_NAME)
            putInt(ExplorerActionTargetKeys.KIND, ExplorerActionValues.TARGET_DIRECTORY)
            putString(ExplorerActionTargetKeys.MIME_TYPE, DIRECTORY_MIME_TYPE)
            putLong(ExplorerActionTargetKeys.SIZE, -1L)
            putLong(ExplorerActionTargetKeys.LAST_MODIFIED, 1_700_000_000_000L)
        }
        return envelope(
            activity = ExplorerDirectoryActivity::class.java.name,
            context = context,
            actionId = ThreeMapleImagePlugin.BROWSE_DIRECTORY_ACTION_ID,
            uri = DIRECTORY_URI,
            mimeType = DIRECTORY_MIME_TYPE,
            displayName = DIRECTORY_NAME,
            size = -1L,
            parentUri = PARENT_URI,
            target = target,
            session = session,
        )
    }

    /**
     * "View image" on [file], readable through the plugin's own FileProvider so the viewer can decode it; the
     * session lists the siblings in the file's directory.
     */
    fun imageIntent(context: Context, file: File, session: IExplorerActionHostSession): Intent {
        val uri = androidx.core.content.FileProvider.getUriForFile(context, context.packageName + ".standalone-images", file)
        val parentUri = uri.buildUpon().path(uri.pathSegments.dropLast(1).joinToString("/", prefix = "/")).build()
        val target = Bundle().apply {
            putString(ExplorerActionTargetKeys.ID, "image-target")
            putParcelable(ExplorerActionTargetKeys.URI, uri)
            putString(ExplorerActionTargetKeys.DISPLAY_NAME, file.name)
            putInt(ExplorerActionTargetKeys.KIND, ExplorerActionValues.TARGET_FILE)
            putString(ExplorerActionTargetKeys.MIME_TYPE, "image/png")
            putLong(ExplorerActionTargetKeys.SIZE, file.length())
            putLong(ExplorerActionTargetKeys.LAST_MODIFIED, file.lastModified())
        }
        return envelope(
            activity = ExplorerActionActivity::class.java.name,
            context = context,
            actionId = ThreeMapleImagePlugin.ACTION_ID,
            uri = uri,
            mimeType = "image/png",
            displayName = file.name,
            size = file.length(),
            parentUri = parentUri,
            target = target,
            session = session,
        )
    }

    private fun envelope(
        activity: String,
        context: Context,
        actionId: String,
        uri: Uri,
        mimeType: String,
        displayName: String,
        size: Long,
        parentUri: Uri,
        target: Bundle,
        session: IExplorerActionHostSession,
    ): Intent = Intent(ExplorerActionPluginActions.EXECUTE).apply {
        setClassName(context, activity)
        setDataAndType(uri, mimeType)
        clipData = ClipData.newRawUri(displayName, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PREFIX_URI_PERMISSION)
        putExtra(ExplorerActionIntentExtras.ACTION_ID, actionId)
        putExtra(ExplorerActionIntentExtras.PROTOCOL_VERSION, ThreeMapleImagePlugin.PROTOCOL_VERSION)
        putExtra(ExplorerActionIntentExtras.REQUEST_ID, UUID.randomUUID().toString())
        putExtra(ExplorerActionIntentExtras.DISPLAY_NAME, displayName)
        putExtra(ExplorerActionIntentExtras.SIZE, size)
        putExtra(ExplorerActionIntentExtras.PARENT_URI, parentUri)
        putExtra(ExplorerActionIntentExtras.PARENT_DISPLAY_PATH, "/images")
        putExtra(ExplorerActionIntentExtras.HOST_VERSION_CODE, ThreeMapleImagePlugin.REQUIRED_HOST_VERSION)
        putExtra(ExplorerActionIntentExtras.SOURCE_SURFACE, ExplorerActionIntentValues.SOURCE_SURFACE_MAIN)
        putParcelableArrayListExtra(ExplorerActionIntentExtras.TARGETS, arrayListOf(target))
        putExtra(
            ExplorerActionIntentExtras.HOST_SESSION,
            Bundle().apply { putBinder(ExplorerActionHostSessionKeys.BINDER, session.asBinder()) },
        )
    }
}
