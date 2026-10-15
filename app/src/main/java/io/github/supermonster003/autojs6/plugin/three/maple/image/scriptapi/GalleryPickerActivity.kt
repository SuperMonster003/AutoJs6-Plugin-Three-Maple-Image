package io.github.supermonster003.autojs6.plugin.three.maple.image.scriptapi

import android.content.Intent
import android.content.res.Configuration
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import io.github.supermonster003.autojs6.plugin.three.maple.image.AlbumKind
import io.github.supermonster003.autojs6.plugin.three.maple.image.ConfiguredActivity
import io.github.supermonster003.autojs6.plugin.three.maple.image.GalleryViewModel
import io.github.supermonster003.autojs6.plugin.three.maple.image.MediaItem
import io.github.supermonster003.autojs6.plugin.three.maple.image.MediaPermissionAction
import io.github.supermonster003.autojs6.plugin.three.maple.image.MediaPermissionGate
import io.github.supermonster003.autojs6.plugin.three.maple.image.MediaPermissionResolution
import io.github.supermonster003.autojs6.plugin.three.maple.image.R
import io.github.supermonster003.autojs6.plugin.three.maple.image.StandaloneOverwrite
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.LocalGridSettings
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.MapleTheme
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.MediaGridCallbacks
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.MediaGridPick
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.MediaGridScreen
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.PickerAlbumsScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.autojs.plugin.gallery.api.GalleryContract
import org.autojs.plugin.gallery.api.GalleryErrorCodes
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.Locale

/**
 * The script picker (ROADMAP 10.2, decision 6 of the M6 contract review): the gallery's albums and image grid in a
 * pick mode started by [GallerySessionActivity]. Without photo access it asks first (partial access included);
 * a refusal returns `PERMISSION_REQUIRED`. A single choice returns at the first tap, a multiple one with the
 * confirm bar, capped at the request's `maxCount`. The result is the JSON array of the gallery contract, built
 * from the media library: absolute path, name, type, size in pixels and bytes, capture time.
 */
class GalleryPickerActivity : ConfiguredActivity() {

    private val gallery by viewModels<GalleryViewModel>()
    private lateinit var permissionGate: MediaPermissionGate
    private lateinit var request: PickerRequest
    private var delivering = false

    private val requestPhotoAccess = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { grants ->
        permissionGate.recordResult(grants)
        if (!updatePhotoAccess().canReadMediaStore) refuse()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val generation = intent.getLongExtra(GallerySessionActivity.EXTRA_GENERATION, 0L)
        val picker = ScriptSessionRegistry.claimed(generation)?.payload() as? SessionPayload.Picker
        if (picker == null) {
            finish()
            return
        }
        request = picker.request
        setResult(RESULT_CANCELED)
        permissionGate = MediaPermissionGate(this)
        gallery.setPickFilter(mimeFilter(request.mimeTypes))
        gallery.setLandscape(resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)
        if (savedInstanceState == null) requestAccessIfNeeded() else updatePhotoAccess()
        val title = request.title ?: getString(if (request.multiple) R.string.pick_title_multiple else R.string.pick_title_single)
        setContent {
            MapleTheme(palette = appPalette) {
                val gridSettings by gallery.gridSettings.collectAsState()
                CompositionLocalProvider(LocalGridSettings provides gridSettings) {
                    val controller = rememberNavController()
                    val albums by gallery.albums.collectAsState()
                    val grid by gallery.grid.collectAsState()
                    NavHost(navController = controller, startDestination = ROUTE_ALBUMS) {
                        composable(ROUTE_ALBUMS) {
                            PickerAlbumsScreen(
                                title = title,
                                state = albums,
                                allImages = gallery.allImagesAlbum(),
                                onCancel = ::finish,
                                onOpenAlbum = { album -> controller.navigate("album/${album.kind.name}/${album.id}") },
                                onGrant = ::requestPhotoAccess,
                                onManageSelected = ::requestPhotoAccess,
                                onRefresh = gallery::refreshAlbums,
                            )
                        }
                        composable(
                            route = ROUTE_ALBUM,
                            arguments = listOf(navArgument("kind") { type = NavType.StringType }, navArgument("id") { type = NavType.LongType }),
                        ) { entry ->
                            val kind = AlbumKind.valueOf(entry.arguments?.getString("kind") ?: AlbumKind.MEDIA.name)
                            val id = entry.arguments?.getLong("id") ?: 0L
                            LaunchedEffect(kind, id, albums.loading) {
                                val album = gallery.findAlbum(kind, id)
                                if (album != null && grid.album?.id != id) gallery.openAlbum(album)
                            }
                            val back = {
                                gallery.clearSelection()
                                gallery.setGridQuery("")
                                controller.popBackStack()
                                Unit
                            }
                            val count = grid.selection.size
                            MediaGridScreen(
                                state = grid,
                                callbacks = MediaGridCallbacks(
                                    onBack = back,
                                    onOpen = { item -> if (request.multiple) toggle(item.id) else deliver(listOf(item)) },
                                    onQuery = gallery::setGridQuery,
                                    onLoadMore = gallery::loadMore,
                                    onSort = { sort, grouping, thisAlbumOnly -> gallery.setGridSort(sort, grouping, thisAlbumOnly) },
                                    onRevealed = gallery::clearReveal,
                                    onColumns = gallery::setColumns,
                                    onRefresh = gallery::refreshGrid,
                                    onToggle = { id -> if (request.multiple) toggle(id) },
                                    onDragStart = { id -> if (request.multiple) toggle(id) },
                                    onClearSelection = gallery::clearSelection,
                                ),
                                pick = MediaGridPick(
                                    multiple = request.multiple,
                                    confirmLabel = getString(R.string.pick_confirm, count),
                                    confirmEnabled = count > 0 && !delivering,
                                    onConfirm = ::confirmSelection,
                                ),
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        gallery.setVisible(true)
    }

    override fun onStop() {
        gallery.setVisible(false)
        super.onStop()
    }

    override fun onResume() {
        super.onResume()
        updatePhotoAccess()
        gallery.refreshGridSettings()
    }

    /** Asks for photo access once on launch; a state that can no longer ask shows the settings button instead. */
    private fun requestAccessIfNeeded() {
        val resolution = updatePhotoAccess()
        if (resolution.canReadMediaStore) return
        if (resolution.nextAction == MediaPermissionAction.REQUEST || resolution.nextAction == MediaPermissionAction.MANAGE_SELECTED) {
            permissionGate.markRequested()
            requestPhotoAccess.launch(resolution.permissionsToRequest.toTypedArray())
        }
    }

    private fun requestPhotoAccess() {
        val resolution = permissionGate.resolve()
        when (resolution.nextAction) {
            MediaPermissionAction.REQUEST, MediaPermissionAction.MANAGE_SELECTED -> {
                permissionGate.markRequested()
                requestPhotoAccess.launch(resolution.permissionsToRequest.toTypedArray())
            }
            MediaPermissionAction.OPEN_APP_SETTINGS -> runCatching { startActivity(permissionGate.appSettingsIntent()) }
            MediaPermissionAction.NONE -> Unit
        }
    }

    /** The access the gallery lists with; leaving without access answers `PERMISSION_REQUIRED` instead of a cancel. */
    private fun updatePhotoAccess(): MediaPermissionResolution {
        val resolution = permissionGate.resolve()
        gallery.setAccess(resolution)
        if (!delivering) {
            if (resolution.canReadMediaStore) setResult(RESULT_CANCELED) else setResult(RESULT_CANCELED, refusal())
        }
        return resolution
    }

    private fun refuse() {
        setResult(RESULT_CANCELED, refusal())
        finish()
    }

    private fun refusal(): Intent = Intent()
        .putExtra(EXTRA_ERROR_CODE, GalleryErrorCodes.PERMISSION_REQUIRED)
        .putExtra(EXTRA_ERROR_MESSAGE, "the user did not grant photo access to the gallery plugin")

    private fun toggle(id: Long) {
        val selection = gallery.grid.value.selection
        if (id !in selection && selection.size >= request.maxCount) {
            Toast.makeText(this, resources.getQuantityString(R.plurals.pick_limit_reached, request.maxCount, request.maxCount), Toast.LENGTH_SHORT).show()
            return
        }
        gallery.toggleSelection(id)
    }

    private fun confirmSelection() {
        if (delivering) return
        lifecycleScope.launch { deliver(gallery.selectedItems().take(request.maxCount)) }
    }

    private fun deliver(items: List<MediaItem>) {
        if (delivering || items.isEmpty()) return
        delivering = true
        lifecycleScope.launch {
            val picked = withContext(Dispatchers.IO) { items.mapNotNull(::describe) }
            if (picked.isEmpty()) {
                setResult(
                    RESULT_CANCELED,
                    Intent().putExtra(EXTRA_ERROR_CODE, GalleryErrorCodes.FILE_UNREADABLE).putExtra(EXTRA_ERROR_MESSAGE, "the chosen images have no file path"),
                )
            } else {
                setResult(RESULT_OK, Intent().putExtra(EXTRA_ITEMS, JSONArray(picked).toString()))
            }
            finish()
        }
    }

    /** One picked image as the contract describes it; null when it has no file path. */
    private fun describe(item: MediaItem): JSONObject? {
        val uri = Uri.parse(item.contentUri)
        val path = runCatching { StandaloneOverwrite.mediaFilePath(contentResolver, uri) }.getOrNull()
            ?: primaryVolumePath(item)
            ?: return null
        var width = item.width
        var height = item.height
        if (width <= 0 || height <= 0) {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            runCatching { contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) } }
            width = bounds.outWidth.coerceAtLeast(0)
            height = bounds.outHeight.coerceAtLeast(0)
        }
        return JSONObject()
            .put(GalleryContract.FIELD_PATH, path)
            .put(GalleryContract.FIELD_NAME, item.displayName)
            .put(GalleryContract.FIELD_MIME_TYPE, item.mimeType)
            .put(GalleryContract.FIELD_WIDTH, width)
            .put(GalleryContract.FIELD_HEIGHT, height)
            .put(GalleryContract.FIELD_SIZE, item.sizeBytes)
            .put(GalleryContract.FIELD_DATE_TAKEN, item.dateTakenMillis ?: item.dateModifiedSeconds * 1000L)
    }

    @Suppress("DEPRECATION")
    private fun primaryVolumePath(item: MediaItem): String? {
        if (item.volume != io.github.supermonster003.autojs6.plugin.three.maple.image.MediaCatalog.PRIMARY_VOLUME || item.relativePath.isBlank()) return null
        val file = File(File(Environment.getExternalStorageDirectory(), item.relativePath), item.displayName)
        return file.absolutePath.takeIf { file.isFile }
    }

    companion object {
        /** Result extras read by [GallerySessionActivity]. */
        internal const val EXTRA_ITEMS = "io.github.supermonster003.autojs6.plugin.three.maple.image.extra.GALLERY_PICKED"
        internal const val EXTRA_ERROR_CODE = "io.github.supermonster003.autojs6.plugin.three.maple.image.extra.GALLERY_ERROR_CODE"
        internal const val EXTRA_ERROR_MESSAGE = "io.github.supermonster003.autojs6.plugin.three.maple.image.extra.GALLERY_ERROR_MESSAGE"

        private const val ROUTE_ALBUMS = "albums"
        private const val ROUTE_ALBUM = "album/{kind}/{id}"

        /** The MIME filter of a request: exact types and `image/` wildcards; no filter lists every image. */
        internal fun mimeFilter(mimeTypes: List<String>): ((MediaItem) -> Boolean)? {
            if (mimeTypes.isEmpty() || "image/*" in mimeTypes) return null
            val accepted = mimeTypes.toSet()
            return { item -> item.mimeType.lowercase(Locale.ROOT) in accepted }
        }
    }
}
