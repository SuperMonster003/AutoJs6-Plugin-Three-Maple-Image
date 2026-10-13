package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.HomeScreen
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.MapleTheme
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class HomeActivity : ConfiguredActivity() {
    private lateinit var actions: HomeActions
    private lateinit var permissionGate: MediaPermissionGate
    private val requestPhotoAccess = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { grants ->
        permissionGate.recordResult(grants)
        photoAccess = permissionGate.resolve()
    }

    /** Current photo access as the gallery sees it; refreshed on resume and after the system dialog. */
    internal var photoAccess: MediaPermissionResolution? = null
        private set

    /** How many times this instance launched the system permission dialog (test hook). */
    internal var permissionRequestsForTesting: Int = 0
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        actions = HomeActions(this)
        actions.restore(savedInstanceState)
        permissionGate = MediaPermissionGate(this)
        if (savedInstanceState == null) requestPhotoAccessOnce()
        setContent {
            MapleTheme(palette = appPalette) {
                HomeScreen(
                    state = actions.uiState,
                    onView = { actions.choose(HomeActions.MODE_VIEW) },
                    onEdit = { actions.choose(HomeActions.MODE_EDIT) },
                    onConvert = { actions.choose(HomeActions.MODE_CONVERT) },
                    onSaveOutput = { runWork { actions.prepareExport() } },
                    onSettings = { startActivity(Intent(this, AppSettingsActivity::class.java)) },
                    onOpenHost = ::openHost,
                )
                actions.Dialogs()
            }
        }
        actions.refresh()
    }

    override fun onResume() {
        super.onResume()
        photoAccess = permissionGate.resolve()
    }

    /**
     * The standalone home asks for photo access exactly once, on its first launch (ROADMAP D1 / T5); afterwards only
     * the explicit buttons of the gallery empty state and the storage permission settings ask again.
     */
    private fun requestPhotoAccessOnce() {
        val resolution = permissionGate.resolve()
        photoAccess = resolution
        if (!resolution.autoRequestOnLaunch) return
        permissionGate.markRequested()
        permissionRequestsForTesting += 1
        requestPhotoAccess.launch(resolution.permissionsToRequest.toTypedArray())
    }

    /** Explicit request from the gallery empty state or the settings page; never automatic. */
    internal fun requestPhotoAccess() {
        val resolution = permissionGate.resolve()
        when (resolution.nextAction) {
            MediaPermissionAction.REQUEST, MediaPermissionAction.MANAGE_SELECTED -> {
                permissionGate.markRequested()
                permissionRequestsForTesting += 1
                requestPhotoAccess.launch(resolution.permissionsToRequest.toTypedArray())
            }
            MediaPermissionAction.OPEN_APP_SETTINGS -> runCatching { startActivity(permissionGate.appSettingsIntent()) }
                .onFailure { showMessage(R.string.standalone_home_open_failed) }
            MediaPermissionAction.NONE -> Unit
        }
    }

    private fun openHost() {
        val intent = packageManager.getLaunchIntentForPackage("org.autojs.autojs6")
        if (intent == null) showMessage(R.string.standalone_home_host_missing)
        else runCatching { startActivity(intent) }.onFailure { showMessage(R.string.standalone_home_open_failed) }
    }

    /** Runs one file task at a time; rows are disabled and the status line announces progress meanwhile. */
    internal fun runWork(block: suspend () -> Unit) {
        if (actions.uiState.working) return
        actions.uiState = actions.uiState.copy(working = true, statusText = getString(R.string.standalone_home_working))
        lifecycleScope.launch {
            try { block() }
            catch (cancelled: CancellationException) { throw cancelled }
            catch (_: Exception) { showMessage(R.string.standalone_home_open_failed) }
            finally {
                actions.uiState = actions.uiState.copy(working = false, statusText = "")
                actions.refresh()
            }
        }
    }

    internal fun showMessage(resource: Int) = Toast.makeText(this, resource, Toast.LENGTH_LONG).show()

    override fun onSaveInstanceState(outState: Bundle) {
        actions.save(outState)
        super.onSaveInstanceState(outState)
    }
}
