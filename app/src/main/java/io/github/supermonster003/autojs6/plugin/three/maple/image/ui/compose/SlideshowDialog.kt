package io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.supermonster003.autojs6.plugin.three.maple.image.R
import io.github.supermonster003.autojs6.plugin.three.maple.image.SlideshowOrder
import io.github.supermonster003.autojs6.plugin.three.maple.image.SlideshowSettings
import io.github.supermonster003.autojs6.plugin.three.maple.image.SlideshowTransition

/** Draft of the slideshow dialog (ROADMAP 9.1), starting from the remembered choices. */
internal class SlideshowDialogState(settings: SlideshowSettings) {
    var intervalSeconds by mutableIntStateOf(settings.intervalSeconds)
    var order by mutableStateOf(settings.order)
    var reverse by mutableStateOf(settings.reverse)
    var loop by mutableStateOf(settings.loop)
    var includeGifs by mutableStateOf(settings.includeGifs)
    var transition by mutableStateOf(settings.transition)

    fun settings() = SlideshowSettings(
        intervalSeconds = intervalSeconds,
        order = order,
        reverse = reverse && order == SlideshowOrder.SEQUENTIAL,
        loop = loop,
        includeGifs = includeGifs,
        transition = transition,
    )
}

@Composable
internal fun SlideshowDialog(state: SlideshowDialogState, onDismissRequest: () -> Unit, onStart: () -> Unit) {
    MapleDialog(
        title = stringResource(R.string.action_slideshow),
        onDismissRequest = onDismissRequest,
        confirmText = stringResource(R.string.slideshow_start),
        onConfirm = onStart,
        dismissText = stringResource(R.string.dialog_button_cancel),
        modifier = Modifier.testTag("maple.slideshow.dialog"),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            MapleSliderRow(
                title = stringResource(R.string.slideshow_interval),
                value = state.intervalSeconds,
                range = SlideshowSettings.MIN_INTERVAL_SECONDS..SlideshowSettings.MAX_INTERVAL_SECONDS,
                onValueChange = { state.intervalSeconds = it },
                valueText = stringResource(R.string.slideshow_interval_value, state.intervalSeconds),
                onDark = false,
                modifier = Modifier.padding(horizontal = MapleDialogDefaults.contentPadding).testTag("maple.slideshow.interval"),
            )
            MapleSegmented(
                options = listOf(stringResource(R.string.slideshow_order_sequential), stringResource(R.string.slideshow_order_random)),
                selectedIndex = state.order.ordinal,
                onSelect = { state.order = SlideshowOrder.entries[it] },
                tagPrefix = "maple.slideshow.order",
                modifier = Modifier.padding(horizontal = MapleDialogDefaults.contentPadding, vertical = 8.dp),
            )
            MapleSwitchRow(
                title = stringResource(R.string.slideshow_reverse),
                checked = state.reverse && state.order == SlideshowOrder.SEQUENTIAL,
                onToggle = { state.reverse = it },
                enabled = state.order == SlideshowOrder.SEQUENTIAL,
                modifier = Modifier.testTag("maple.slideshow.reverse"),
            )
            MapleSwitchRow(
                title = stringResource(R.string.slideshow_loop),
                checked = state.loop,
                onToggle = { state.loop = it },
                modifier = Modifier.testTag("maple.slideshow.loop"),
            )
            MapleSwitchRow(
                title = stringResource(R.string.slideshow_include_gifs),
                checked = state.includeGifs,
                onToggle = { state.includeGifs = it },
                modifier = Modifier.testTag("maple.slideshow.includeGifs"),
            )
            Text(
                text = stringResource(R.string.slideshow_transition),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = MapleDialogDefaults.contentPadding, end = MapleDialogDefaults.contentPadding, top = 8.dp),
            )
            MapleSegmented(
                options = listOf(
                    stringResource(R.string.slideshow_transition_none),
                    stringResource(R.string.slideshow_transition_fade),
                    stringResource(R.string.slideshow_transition_slide),
                ),
                selectedIndex = state.transition.ordinal,
                onSelect = { state.transition = SlideshowTransition.entries[it] },
                tagPrefix = "maple.slideshow.transition",
                modifier = Modifier.padding(horizontal = MapleDialogDefaults.contentPadding, vertical = 8.dp),
            )
        }
    }
}
