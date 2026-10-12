package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.HomeScreen
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.MapleTheme
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class HomeActivity : ConfiguredActivity() {
    private lateinit var actions: HomeActions

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        actions = HomeActions(this)
        actions.restore(savedInstanceState)
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
