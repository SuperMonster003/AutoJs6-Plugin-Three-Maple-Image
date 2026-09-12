package io.github.supermonster003.autojs6.plugin.imageviewer

import android.content.ContentProvider
import android.content.ContentResolver
import android.content.ContentValues
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import java.io.FileNotFoundException
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import org.autojs.plugin.explorer.api.IExplorerActionHostSession

/**
 * Non-exported, process-local bridge that lets Glide use its normal content-URI loaders while the
 * host remains the sole owner of sibling file access. Tokens are never persisted or shared.
 */
class HostSessionImageProvider : ContentProvider() {

    override fun onCreate(): Boolean = true

    override fun getType(uri: Uri): String? = HostSessionImageRegistry.describe(uri)?.mimeType

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?,
    ): Cursor? {
        val page = HostSessionImageRegistry.describe(uri) ?: return null
        val columns = projection ?: arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE)
        return MatrixCursor(columns, 1).apply {
            addRow(
                columns.map { column ->
                    when (column) {
                        OpenableColumns.DISPLAY_NAME -> page.displayName
                        OpenableColumns.SIZE -> page.declaredSize
                        else -> null
                    }
                },
            )
        }
    }

    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor {
        if (mode != READ_ONLY_MODE) throw FileNotFoundException("Read-only provider")
        return HostSessionImageRegistry.open(uri)
            ?: throw FileNotFoundException("Expired or invalid image session")
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?,
    ): Int = 0

    private companion object {
        const val READ_ONLY_MODE = "r"
    }
}

internal data class HostSessionImagePage(
    val displayName: String,
    val declaredSize: Long,
    val mimeType: String,
)

internal object HostSessionImageRegistry {

    const val AUTHORITY = "io.github.supermonster003.autojs6.plugin.imageviewer.host-images"

    private const val SESSION_PATH = "session"

    private data class Route(
        val relativePath: String,
        val page: HostSessionImagePage,
    )

    private data class Session(
        val hostSession: IExplorerActionHostSession,
        val targetId: String,
        val routes: Map<Int, Route>,
    )

    private val sessions = ConcurrentHashMap<String, Session>()

    fun register(request: ImageViewerLaunchRequest): String {
        val hostSession = requireNotNull(request.hostSession)
        val targetId = requireNotNull(request.hostTargetId)
        val routes = request.pages.mapIndexedNotNull { index, viewerPage ->
            val relativePath = viewerPage.hostRelativePath?.takeIf { it.isNotEmpty() }
                ?: return@mapIndexedNotNull null
            require(relativePath == viewerPage.image.displayName)
            require(ImageRequestPolicy.validateDisplayName(relativePath) != null)
            index to Route(
                relativePath = relativePath,
                page = HostSessionImagePage(
                    displayName = viewerPage.image.displayName,
                    declaredSize = viewerPage.image.declaredSize,
                    mimeType = viewerPage.image.mimeType,
                ),
            )
        }.toMap()
        require(routes.isNotEmpty())

        while (true) {
            val token = UUID.randomUUID().toString()
            if (sessions.putIfAbsent(token, Session(hostSession, targetId, routes)) == null) {
                return token
            }
        }
    }

    fun unregister(token: String?) {
        if (token != null) sessions.remove(token)
    }

    fun imageUri(token: String, index: Int): Uri {
        require(sessions[token]?.routes?.containsKey(index) == true)
        return Uri.Builder()
            .scheme(ContentResolver.SCHEME_CONTENT)
            .authority(AUTHORITY)
            .appendPath(SESSION_PATH)
            .appendPath(token)
            .appendPath(index.toString())
            .build()
    }

    fun describe(uri: Uri): HostSessionImagePage? = resolve(uri)?.second?.page

    fun open(uri: Uri): ParcelFileDescriptor? {
        val (session, route) = resolve(uri) ?: return null
        return runCatching {
            session.hostSession.openFile(session.targetId, route.relativePath)?.let { descriptor ->
                descriptor.takeIf { it.fileDescriptor.valid() }.also {
                    if (it == null) runCatching { descriptor.close() }
                }
            }
        }.getOrNull()
    }

    private fun resolve(uri: Uri): Pair<Session, Route>? {
        if (
            uri.scheme != ContentResolver.SCHEME_CONTENT ||
            uri.authority != AUTHORITY ||
            uri.userInfo != null ||
            uri.port != -1 ||
            uri.query != null ||
            uri.fragment != null
        ) {
            return null
        }
        val segments = uri.pathSegments
        if (segments.size != 3 || segments[0] != SESSION_PATH) return null
        val index = segments[2].toIntOrNull()?.takeIf { it >= 0 } ?: return null
        val session = sessions[segments[1]] ?: return null
        val route = session.routes[index] ?: return null
        return session to route
    }
}
