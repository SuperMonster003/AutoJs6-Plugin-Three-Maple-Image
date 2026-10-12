package io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
    actions: @Composable RowScope.() -> Unit = {},
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
                navigationIcon = if (onBack != null) painterResource(R.drawable.ic_arrow_back_24) else null,
                navigationContentDescription = stringResource(R.string.standalone_navigation_back),
                onNavigationClick = onBack,
                actions = actions,
            )
        },
        content = content,
    )
}
