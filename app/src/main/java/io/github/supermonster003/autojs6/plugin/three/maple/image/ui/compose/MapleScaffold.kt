package io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import io.github.supermonster003.autojs6.plugin.three.maple.image.R

/**
 * Standard page frame of the standalone screens: neutral background, a surface top bar with an optional back
 * arrow and the system bar insets handled by [Scaffold]. Overlay screens (viewer, editor) compose their own frame.
 */
@Composable
internal fun MapleScreen(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    /** Replaces the back arrow (selection mode shows a close icon); defaults to the back arrow. */
    navigationIcon: Painter? = null,
    navigationContentDescription: String? = null,
    actions: @Composable RowScope.() -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    snackbarHostState: SnackbarHostState? = null,
    /** Replaces the title text in the top bar (a search field). */
    titleContent: (@Composable () -> Unit)? = null,
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        topBar = {
            MapleTopBar(
                title = title,
                subtitle = subtitle,
                navigationIcon = if (onBack != null) navigationIcon ?: painterResource(R.drawable.ic_arrow_back_24) else null,
                navigationContentDescription = navigationContentDescription ?: stringResource(R.string.standalone_navigation_back),
                onNavigationClick = onBack,
                actions = actions,
                titleContent = titleContent,
            )
        },
        bottomBar = bottomBar,
        snackbarHost = { if (snackbarHostState != null) SnackbarHost(snackbarHostState, modifier = Modifier.testTag("maple.snackbar")) },
        content = content,
    )
}
