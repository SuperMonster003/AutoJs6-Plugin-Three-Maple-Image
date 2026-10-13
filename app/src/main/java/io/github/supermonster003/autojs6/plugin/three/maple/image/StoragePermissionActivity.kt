package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.MapleTheme
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.StoragePermissionCallbacks
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.StoragePermissionScreen
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.StoragePermissionUiModel

/**
 * Settings > Storage permission (ROADMAP 6.1): the only place besides the gallery empty state that may ask for
 * photo access again. The all files and media management switches and the app settings row hand over to the system pages and the model
 * is rebuilt on every resume so it always mirrors the platform state.
 */
class StoragePermissionActivity : ConfiguredActivity() {

    private lateinit var gate: MediaPermissionGate
    private lateinit var folders: AuthorizedFolderRepository
    private var model by mutableStateOf(StoragePermissionUiModel())

    private val requestPhotoAccess = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { grants ->
        gate.recordResult(grants)
        refresh()
    }
    private val pickFolder = registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) {
            folders.add(uri)
            refresh()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gate = MediaPermissionGate(this)
        folders = AuthorizedFolderRepository(this)
        val callbacks = StoragePermissionCallbacks(
            onBack = ::finish,
            onGrant = ::grant,
            onManageSelected = ::grant,
            onAllFiles = {
                val intent = gate.allFilesAccessIntent()
                if (intent != null) runCatching { startActivity(intent) }.onFailure { toast(R.string.standalone_home_open_failed) }
            },
            onManageMedia = {
                val intent = manageMediaIntent()
                if (intent != null) runCatching { startActivity(intent) }.onFailure { toast(R.string.standalone_home_open_failed) }
            },
            onAddFolder = { runCatching { pickFolder.launch(null) }.onFailure { toast(R.string.standalone_home_open_failed) } },
            onRemoveFolder = { folder ->
                folders.remove(folder.treeUri)
                refresh()
            },
        )
        setContent {
            MapleTheme(palette = appPalette) {
                StoragePermissionScreen(model = model, callbacks = callbacks)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun grant() {
        val resolution = gate.resolve()
        when (resolution.nextAction) {
            MediaPermissionAction.REQUEST, MediaPermissionAction.MANAGE_SELECTED -> {
                gate.markRequested()
                requestPhotoAccess.launch(resolution.permissionsToRequest.toTypedArray())
            }
            MediaPermissionAction.OPEN_APP_SETTINGS -> runCatching { startActivity(gate.appSettingsIntent()) }
                .onFailure { toast(R.string.standalone_home_open_failed) }
            MediaPermissionAction.NONE -> Unit
        }
    }

    private fun refresh() {
        val resolution = gate.resolve()
        model = StoragePermissionUiModel(
            state = resolution.state,
            nextAction = resolution.nextAction,
            allFilesAvailable = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R,
            allFilesOn = resolution.state == MediaAccessState.ALL_FILES,
            manageMediaAvailable = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S,
            manageMediaOn = MediaWriteCoordinator.canManageMedia(this),
            folders = folders.list(),
        )
    }

    /** Settings > Special app access > Media management for this app; null before Android 12. */
    private fun manageMediaIntent(): Intent? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        Intent(Settings.ACTION_REQUEST_MANAGE_MEDIA, Uri.fromParts("package", packageName, null))
    } else null

    private fun toast(resource: Int) = Toast.makeText(this, resource, Toast.LENGTH_LONG).show()

    companion object {
        fun intent(context: android.content.Context): Intent = Intent(context, StoragePermissionActivity::class.java)
    }
}
