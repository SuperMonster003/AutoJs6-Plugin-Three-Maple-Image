package io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.supermonster003.autojs6.plugin.three.maple.image.R

/** Observable state of the standalone home page (design draft 7.1). */
internal data class HomeUiState(
    val working: Boolean = false,
    val statusText: String = "",
    val pendingOutputName: String? = null,
)

/**
 * Standalone home for 2.1.0: a compact hero, the "get started" actions and the information rows. Rows are
 * disabled while a file is being prepared and the status line is a polite live region.
 */
@Composable
internal fun HomeScreen(
    state: HomeUiState,
    onView: () -> Unit,
    onEdit: () -> Unit,
    onConvert: () -> Unit,
    onSaveOutput: () -> Unit,
    onSettings: () -> Unit,
    onOpenHost: () -> Unit,
) {
    val enabled = !state.working
    MapleScreen(title = stringResource(R.string.app_name)) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Image(
                    painter = painterResource(R.mipmap.ic_launcher),
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.plugin_description),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = stringResource(R.string.standalone_home_scope),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            MapleSectionHeader(stringResource(R.string.standalone_home_actions))
            MapleSettingsRow(
                title = stringResource(R.string.action_view_image),
                summary = stringResource(R.string.standalone_home_view_summary),
                iconResource = R.drawable.ic_image_24,
                enabled = enabled,
                onClick = onView,
                modifier = Modifier.testTag("maple.home.view"),
            )
            MapleSettingsRow(
                title = stringResource(R.string.action_edit_image),
                summary = stringResource(R.string.standalone_home_edit_summary),
                iconResource = R.drawable.ic_edit_24,
                enabled = enabled,
                onClick = onEdit,
                modifier = Modifier.testTag("maple.home.edit"),
            )
            MapleSettingsRow(
                title = stringResource(R.string.action_convert_image),
                summary = stringResource(R.string.standalone_home_convert_summary),
                iconResource = R.drawable.ic_swap_horiz_24,
                enabled = enabled,
                onClick = onConvert,
                modifier = Modifier.testTag("maple.home.convert"),
            )
            if (state.pendingOutputName != null) {
                MapleSettingsRow(
                    title = stringResource(R.string.standalone_home_save_output),
                    summary = stringResource(R.string.standalone_home_pending_summary, state.pendingOutputName),
                    iconResource = R.drawable.ic_download_24,
                    enabled = enabled,
                    onClick = onSaveOutput,
                    modifier = Modifier.testTag("maple.home.saveOutput"),
                )
            }
            MapleSectionHeader(stringResource(R.string.standalone_settings_section_information))
            MapleSettingsRow(
                title = stringResource(R.string.standalone_settings_title),
                summary = stringResource(R.string.standalone_home_settings_summary),
                iconResource = R.drawable.ic_settings_theme,
                enabled = enabled,
                onClick = onSettings,
                modifier = Modifier.testTag("maple.home.settings"),
            )
            MapleSettingsRow(
                title = stringResource(R.string.standalone_home_open_host),
                summary = stringResource(R.string.standalone_home_host_summary),
                iconResource = R.drawable.ic_open_in_new_24,
                enabled = enabled,
                onClick = onOpenHost,
                modifier = Modifier.testTag("maple.home.openHost"),
            )
            Text(
                text = state.statusText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp)
                    .semantics { liveRegion = LiveRegionMode.Polite }
                    .testTag("maple.home.status"),
            )
            Spacer(Modifier.height(12.dp))
        }
    }
}
