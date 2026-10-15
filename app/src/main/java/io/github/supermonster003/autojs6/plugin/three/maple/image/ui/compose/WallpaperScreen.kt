package io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import io.github.supermonster003.autojs6.plugin.three.maple.image.ImageEditingView
import io.github.supermonster003.autojs6.plugin.three.maple.image.R
import io.github.supermonster003.autojs6.plugin.three.maple.image.WallpaperTarget

internal data class WallpaperUiModel(
    val title: String,
    val loading: Boolean = true,
    val loadFailed: Boolean = false,
    val busy: Boolean = false,
    /** The system did not take the last wallpaper; another app can still be offered. */
    val applyFailed: Boolean = false,
    val target: WallpaperTarget = WallpaperTarget.HOME,
) {
    val canApply: Boolean get() = !loading && !loadFailed && !busy
}

internal class WallpaperCallbacks(
    val onBack: () -> Unit,
    val onTarget: (WallpaperTarget) -> Unit,
    val onApply: () -> Unit,
    val onSetWithAnotherApp: () -> Unit,
)

/**
 * "Set as wallpaper" (ROADMAP 9.1): the editor's crop frame locked to the screen's shape on the dark canvas, then
 * home / lock / both and "Set as wallpaper" in a parameter panel.
 */
@Composable
internal fun WallpaperScreen(model: WallpaperUiModel, editingView: ImageEditingView, callbacks: WallpaperCallbacks) {
    val canvas = MapleTheme.canvas
    Column(modifier = Modifier.fillMaxSize().background(canvas.editorCanvas)) {
        MapleTopBar(
            title = stringResource(R.string.action_set_wallpaper),
            subtitle = model.title,
            navigationIcon = painterResource(R.drawable.ic_arrow_back_24),
            navigationContentDescription = stringResource(R.string.action_back),
            onNavigationClick = callbacks.onBack,
            overlay = true,
        )
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            AndroidView(factory = { editingView }, modifier = Modifier.fillMaxSize().testTag("maple.wallpaper.canvas"))
            if (model.loading || model.busy) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter).testTag("maple.wallpaper.progress"),
                    color = canvas.accentOnScrim,
                    trackColor = canvas.accentOnScrim.copy(alpha = 0.3f),
                )
            }
            if (model.loadFailed) {
                Column(
                    modifier = Modifier.align(Alignment.Center).padding(24.dp).testTag("maple.wallpaper.error"),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = stringResource(R.string.text_cannot_read_file),
                        color = canvas.onScrim,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                    )
                    SetWithAnotherAppButton(callbacks)
                }
            }
            if (model.applyFailed) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                        .fillMaxWidth()
                        .background(canvas.scrimStrong, RoundedCornerShape(12.dp))
                        .padding(start = 16.dp, end = 8.dp)
                        .testTag("maple.wallpaper.failed"),
                ) {
                    Text(
                        text = stringResource(R.string.wallpaper_failed),
                        color = canvas.onScrim,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f),
                    )
                    SetWithAnotherAppButton(callbacks)
                }
            }
        }
        MapleParameterPanel(
            onCancel = callbacks.onBack,
            onConfirm = callbacks.onApply,
            cancelText = stringResource(R.string.dialog_button_cancel),
            confirmText = stringResource(R.string.action_set_wallpaper),
            confirmEnabled = model.canApply,
            modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars).testTag("maple.wallpaper.panel"),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.wallpaper_crop_hint),
                    color = canvas.onScrimMuted,
                    style = MaterialTheme.typography.bodyMedium,
                )
                MapleSegmented(
                    options = listOf(
                        stringResource(R.string.wallpaper_target_home),
                        stringResource(R.string.wallpaper_target_lock),
                        stringResource(R.string.wallpaper_target_both),
                    ),
                    selectedIndex = model.target.ordinal,
                    onSelect = { callbacks.onTarget(WallpaperTarget.entries[it]) },
                    enabled = !model.busy,
                    tagPrefix = "maple.wallpaper.target",
                    onDark = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun SetWithAnotherAppButton(callbacks: WallpaperCallbacks) {
    TextButton(
        onClick = callbacks.onSetWithAnotherApp,
        modifier = Modifier.defaultMinSize(minHeight = 48.dp).testTag("maple.wallpaper.setWith"),
    ) { Text(stringResource(R.string.wallpaper_set_with), color = MapleTheme.canvas.accentOnScrim) }
}
