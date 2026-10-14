package io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import io.github.supermonster003.autojs6.plugin.three.maple.image.MediaCatalog
import io.github.supermonster003.autojs6.plugin.three.maple.image.MediaGrouping
import io.github.supermonster003.autojs6.plugin.three.maple.image.MediaSort
import io.github.supermonster003.autojs6.plugin.three.maple.image.MediaSortField
import io.github.supermonster003.autojs6.plugin.three.maple.image.R

/** Sort fields of a media grid, with their labels. */
internal val mediaSortFields: List<Pair<MediaSortField, Int>> = listOf(
    MediaSortField.NAME to R.string.gallery_sort_name,
    MediaSortField.DATE_MODIFIED to R.string.gallery_sort_date_modified,
    MediaSortField.DATE_TAKEN to R.string.gallery_sort_date_taken,
    MediaSortField.SIZE to R.string.gallery_sort_size,
    MediaSortField.RANDOM to R.string.gallery_sort_random,
)

/** Sort fields of the album grid: by name, latest change, latest capture or image count. */
internal val albumSortFields: List<Pair<MediaSortField, Int>> = listOf(
    MediaSortField.NAME to R.string.gallery_sort_name,
    MediaSortField.DATE_MODIFIED to R.string.gallery_sort_date_modified,
    MediaSortField.DATE_TAKEN to R.string.gallery_sort_date_taken,
    MediaSortField.SIZE to R.string.gallery_sort_count,
)

/**
 * Sort dialog (ROADMAP 7.5): the field, the direction (not for a random order), the date grouping of a media grid
 * ([grouping] null hides it; only a date order can group) and "this album only" ([thisAlbumOnly] null hides it).
 * Nothing changes until the confirm button.
 */
@Composable
internal fun SortDialog(
    fields: List<Pair<MediaSortField, Int>>,
    sort: MediaSort,
    grouping: MediaGrouping?,
    thisAlbumOnly: Boolean?,
    onDismiss: () -> Unit,
    onConfirm: (MediaSort, MediaGrouping, Boolean) -> Unit,
) {
    var field by rememberSaveable { mutableStateOf(sort.field) }
    var ascending by rememberSaveable { mutableStateOf(sort.ascending) }
    var pendingGrouping by rememberSaveable { mutableStateOf(grouping ?: MediaGrouping.NONE) }
    var onlyHere by rememberSaveable { mutableStateOf(thisAlbumOnly ?: false) }
    val groupable = MediaCatalog.groupable(MediaSort(field))
    MapleDialog(
        title = stringResource(R.string.gallery_sort),
        onDismissRequest = onDismiss,
        confirmText = stringResource(R.string.dialog_button_confirm),
        onConfirm = {
            onConfirm(MediaSort(field, ascending), if (groupable) pendingGrouping else MediaGrouping.NONE, onlyHere)
        },
        dismissText = stringResource(R.string.dialog_button_cancel),
        modifier = Modifier.testTag("maple.sortDialog"),
    ) {
        Column {
            SectionLabel(R.string.sort_section_field)
            for ((option, label) in fields) {
                RadioRow(stringResource(label), selected = field == option, onSelect = { field = option }, tag = "maple.sort.${option.name.lowercase()}")
            }
            SectionLabel(R.string.sort_section_direction)
            val directional = field != MediaSortField.RANDOM
            RadioRow(stringResource(R.string.gallery_sort_ascending), selected = ascending, enabled = directional, onSelect = { ascending = true }, tag = "maple.sort.ascending")
            RadioRow(stringResource(R.string.gallery_sort_descending), selected = !ascending, enabled = directional, onSelect = { ascending = false }, tag = "maple.sort.descending")
            if (grouping != null) {
                SectionLabel(R.string.sort_section_grouping)
                val choices = listOf(
                    MediaGrouping.NONE to R.string.group_none,
                    MediaGrouping.DAY to R.string.group_day,
                    MediaGrouping.MONTH to R.string.group_month,
                )
                for ((option, label) in choices) {
                    RadioRow(
                        stringResource(label),
                        selected = (if (groupable) pendingGrouping else MediaGrouping.NONE) == option,
                        enabled = groupable,
                        onSelect = { pendingGrouping = option },
                        tag = "maple.group.${option.name.lowercase()}",
                    )
                }
                if (!groupable) {
                    Text(
                        text = stringResource(R.string.group_needs_date),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = MapleDialogDefaults.contentPadding, vertical = 4.dp),
                    )
                }
            }
            if (thisAlbumOnly != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .toggleable(value = onlyHere, role = Role.Checkbox, onValueChange = { onlyHere = it })
                        .defaultMinSize(minHeight = 56.dp)
                        .padding(horizontal = MapleDialogDefaults.contentPadding, vertical = 8.dp)
                        .testTag("maple.sort.thisAlbumOnly"),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(checked = onlyHere, onCheckedChange = null)
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(stringResource(R.string.sort_this_album_only), style = MaterialTheme.typography.bodyLarge)
                        Text(
                            stringResource(R.string.sort_this_album_only_summary),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(@StringRes text: Int) {
    Text(
        text = stringResource(text),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = MapleDialogDefaults.contentPadding, end = MapleDialogDefaults.contentPadding, top = 12.dp, bottom = 4.dp),
    )
}

@Composable
private fun RadioRow(label: String, selected: Boolean, onSelect: () -> Unit, tag: String, enabled: Boolean = true) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = selected, enabled = enabled, role = Role.RadioButton, onClick = onSelect)
            .defaultMinSize(minHeight = 48.dp)
            .padding(horizontal = MapleDialogDefaults.contentPadding)
            .testTag(tag),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = null, enabled = enabled)
        Spacer(Modifier.width(16.dp))
        Text(label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface.disabledIf(!enabled))
    }
}
