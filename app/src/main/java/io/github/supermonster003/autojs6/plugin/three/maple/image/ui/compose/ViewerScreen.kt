package io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import io.github.supermonster003.autojs6.plugin.three.maple.image.ImageAnimationPlayback
import io.github.supermonster003.autojs6.plugin.three.maple.image.R
import io.github.supermonster003.autojs6.plugin.three.maple.image.ZoomableImageView

/** Everything the viewer overlay renders; the Activity owns loading and mutates this through one state holder. */
internal data class ViewerUiState(
    val title: String = "",
    val subtitle: String? = null,
    val metadataText: String = "",
    val detailsSummary: String = "",
    val exifText: String = "",
    val loading: Boolean = true,
    val errorText: String? = null,
    val controlsVisible: Boolean = true,
    val detailsAvailable: Boolean = false,
    val detailsExpanded: Boolean = false,
    val printEnabled: Boolean = false,
    val canForward: Boolean = false,
    val canEdit: Boolean = false,
    val animation: ImageAnimationPlayback = ImageAnimationPlayback.UNAVAILABLE,
    val zoomIndicatorText: String? = null,
)

internal class ViewerCallbacks(
    val onBack: () -> Unit,
    val onToggleDetails: () -> Unit,
    val onDismissDetails: () -> Unit,
    val onRotate: () -> Unit,
    val onEdit: () -> Unit,
    val onShare: () -> Unit,
    val onOpenWith: () -> Unit,
    val onResetZoom: () -> Unit,
    val onPrint: () -> Unit,
    val onReleaseHistory: () -> Unit,
    val onToggleAnimation: () -> Unit,
)

/**
 * Immersive viewer (design draft 7.3 / 7.4): the zoomable View fills the window, the top bar, metadata line,
 * five-button action bar, GIF control and zoom badge are translucent overlays, and the details panel is a modal
 * bottom sheet. A tap on the image toggles every overlay (wired on the View by the Activity).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ViewerScreen(state: ViewerUiState, imageView: ZoomableImageView, callbacks: ViewerCallbacks) {
    val canvas = MapleTheme.canvas
    Box(modifier = Modifier.fillMaxSize().background(canvas.viewerCanvas)) {
        AndroidView(factory = { imageView }, modifier = Modifier.fillMaxSize())
        if (state.loading && state.errorText == null) {
            MapleLoading(dark = true)
        }
        if (state.errorText != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = state.errorText,
                    color = canvas.errorOnScrim,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(horizontal = 32.dp)
                        .background(canvas.scrimStrong, MaterialTheme.shapes.medium)
                        .padding(16.dp)
                        .testTag("maple.viewer.error"),
                )
            }
        }
        AnimatedVisibility(
            visible = state.controlsVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter),
        ) {
            ViewerTopBar(state, callbacks)
        }
        MapleZoomIndicator(
            text = state.zoomIndicatorText.orEmpty(),
            visible = state.zoomIndicatorText != null,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .windowInsetsPadding(WindowInsets.statusBars)
                .windowInsetsPadding(WindowInsets.displayCutout.only(WindowInsetsSides.Horizontal))
                .padding(top = 80.dp, end = 16.dp),
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.displayCutout.only(WindowInsetsSides.Horizontal)),
            horizontalAlignment = Alignment.End,
        ) {
            val animationVisible = state.controlsVisible && state.animation != ImageAnimationPlayback.UNAVAILABLE
            if (animationVisible) {
                val paused = state.animation == ImageAnimationPlayback.PAUSED
                val description = stringResource(if (paused) R.string.action_resume_animation else R.string.action_pause_animation)
                IconButton(
                    onClick = callbacks.onToggleAnimation,
                    modifier = Modifier
                        .padding(end = 16.dp, bottom = 16.dp)
                        .size(48.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                        .semantics { contentDescription = description }
                        .testTag("maple.viewer.animation"),
                ) {
                    Icon(
                        painter = painterResource(if (paused) R.drawable.ic_play_arrow_24 else R.drawable.ic_pause_24),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            }
            AnimatedVisibility(visible = state.controlsVisible, enter = fadeIn(), exit = fadeOut()) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = state.metadataText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = canvas.onScrimMuted,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(canvas.scrim)
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .testTag("maple.viewer.metadata"),
                    )
                    MapleBottomActionBar(
                        overlay = true,
                        actions = listOf(
                            MapleAction(
                                icon = painterResource(R.drawable.ic_info_outline_24),
                                label = stringResource(R.string.label_details),
                                contentDescription = stringResource(
                                    if (state.detailsExpanded) R.string.action_hide_image_details else R.string.action_show_image_details,
                                ),
                                enabled = state.detailsAvailable,
                                onClick = callbacks.onToggleDetails,
                                testTag = "maple.viewer.details",
                            ),
                            MapleAction(
                                icon = painterResource(R.drawable.ic_rotate_right_24),
                                label = stringResource(R.string.label_rotate),
                                contentDescription = stringResource(R.string.action_rotate_clockwise),
                                onClick = callbacks.onRotate,
                                testTag = "maple.viewer.rotate",
                            ),
                            MapleAction(
                                icon = painterResource(R.drawable.ic_edit_24),
                                label = stringResource(R.string.text_edit),
                                contentDescription = stringResource(R.string.action_edit_image),
                                enabled = state.canEdit,
                                onClick = callbacks.onEdit,
                                testTag = "maple.viewer.edit",
                            ),
                            MapleAction(
                                icon = painterResource(R.drawable.ic_share_24),
                                label = stringResource(R.string.action_share),
                                enabled = state.canForward,
                                onClick = callbacks.onShare,
                                testTag = "maple.viewer.share",
                            ),
                            MapleAction(
                                icon = painterResource(R.drawable.ic_open_in_new_24),
                                label = stringResource(R.string.label_open_with),
                                contentDescription = stringResource(R.string.action_open_external),
                                enabled = state.canForward,
                                onClick = callbacks.onOpenWith,
                                testTag = "maple.viewer.openWith",
                            ),
                        ),
                    )
                }
            }
        }
    }
    if (state.detailsExpanded) {
        ModalBottomSheet(
            onDismissRequest = callbacks.onDismissDetails,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.testTag("maple.viewer.detailsSheet"),
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
                Text(state.title, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
                Text(
                    state.detailsSummary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant)
                Text(
                    state.exifText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.testTag("maple.viewer.exif"),
                )
                Spacer(Modifier.height(24.dp))
                Spacer(Modifier.windowInsetsPadding(WindowInsets.navigationBars))
            }
        }
    }
}

@Composable
private fun ViewerTopBar(state: ViewerUiState, callbacks: ViewerCallbacks) {
    var menuOpen by remember { mutableStateOf(false) }
    MapleTopBar(
        title = state.title,
        subtitle = state.subtitle,
        navigationIcon = painterResource(R.drawable.ic_arrow_back_24),
        navigationContentDescription = stringResource(R.string.action_back),
        onNavigationClick = callbacks.onBack,
        overlay = true,
        actions = {
            IconButton(onClick = { menuOpen = true }, modifier = Modifier.testTag("maple.viewer.more")) {
                Icon(painter = painterResource(R.drawable.ic_more_vert_24), contentDescription = stringResource(R.string.action_more))
            }
            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.action_reset_zoom)) },
                    onClick = { menuOpen = false; callbacks.onResetZoom() },
                    modifier = Modifier.testTag("maple.viewer.menu.resetZoom"),
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.action_print)) },
                    enabled = state.printEnabled,
                    onClick = { menuOpen = false; callbacks.onPrint() },
                    modifier = Modifier.testTag("maple.viewer.menu.print"),
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.release_history)) },
                    onClick = { menuOpen = false; callbacks.onReleaseHistory() },
                    modifier = Modifier.testTag("maple.viewer.menu.releaseHistory"),
                )
            }
        },
    )
}
