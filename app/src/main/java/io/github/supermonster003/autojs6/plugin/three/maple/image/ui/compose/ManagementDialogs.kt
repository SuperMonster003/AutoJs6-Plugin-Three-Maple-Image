package io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import io.github.supermonster003.autojs6.plugin.three.maple.image.AlbumPath
import io.github.supermonster003.autojs6.plugin.three.maple.image.FileNameCheck
import io.github.supermonster003.autojs6.plugin.three.maple.image.FileNameProblem
import io.github.supermonster003.autojs6.plugin.three.maple.image.MediaFileNamePolicy
import io.github.supermonster003.autojs6.plugin.three.maple.image.R

/** A move / copy target: an album of the media library, or a new one under `Pictures/`. */
/** A move / copy target; [location] names a storage card (the system's description) and is null for the internal storage. */
internal data class TargetAlbum(val name: String, val path: AlbumPath, val location: String? = null)

@Composable
private fun problemText(problem: FileNameProblem): String = stringResource(
    when (problem) {
        FileNameProblem.BLANK -> R.string.rename_error_blank
        FileNameProblem.ILLEGAL_CHARACTER -> R.string.rename_error_illegal
        FileNameProblem.RESERVED -> R.string.rename_error_reserved
        FileNameProblem.TOO_LONG -> R.string.rename_error_too_long
        FileNameProblem.LEADING_DOT -> R.string.rename_error_leading_dot
    },
)

/**
 * One-line name input shared by rename and "new album": validates while typing with [check], warns about a
 * changed extension, and shows [externalError] (a taken name, a failed rename) until the text changes.
 */
@Composable
private fun NameDialog(
    title: String,
    label: String,
    initial: String,
    selectionEnd: Int,
    confirmText: String,
    check: (String) -> FileNameCheck,
    externalError: String?,
    busy: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    testTag: String,
) {
    var value by remember { mutableStateOf(TextFieldValue(initial, selection = TextRange(0, selectionEnd))) }
    var edited by rememberSaveable { mutableStateOf(false) }
    val result = check(value.text)
    // An error from the last attempt stays until the name is edited again.
    val failedName = remember(externalError) { if (externalError != null) value.text else null }
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { runCatching { focus.requestFocus() } }
    val error = when {
        result is FileNameCheck.Invalid && (edited || initial.isNotEmpty()) -> problemText(result.problem)
        externalError != null && value.text == failedName -> externalError
        else -> null
    }
    val warning = (result as? FileNameCheck.Valid)?.takeIf { it.extensionChanged }?.let { stringResource(R.string.rename_extension_warning) }
    MapleDialog(
        title = title,
        onDismissRequest = onDismiss,
        confirmText = confirmText,
        onConfirm = { (result as? FileNameCheck.Valid)?.let { onConfirm(it.name) } },
        dismissText = stringResource(R.string.dialog_button_cancel),
        confirmEnabled = !busy && result is FileNameCheck.Valid && !result.unchanged,
        modifier = Modifier.testTag(testTag),
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {
                value = it
                edited = true
            },
            label = { Text(label) },
            singleLine = true,
            isError = error != null,
            supportingText = (error ?: warning)?.let { text -> { Text(text) } },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MapleSettingsDefaults.horizontalPadding)
                .focusRequester(focus)
                .testTag("$testTag.input"),
        )
    }
}

/** Rename one image (ROADMAP 7.3); the base name is preselected so typing keeps the extension. */
@Composable
internal fun RenameDialog(name: String, externalError: String?, busy: Boolean, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    NameDialog(
        title = stringResource(R.string.rename_title),
        label = stringResource(R.string.rename_label),
        initial = name,
        selectionEnd = MediaFileNamePolicy.baseName(name).length,
        confirmText = stringResource(R.string.action_rename),
        check = { MediaFileNamePolicy.check(name, it) },
        externalError = externalError,
        busy = busy,
        onDismiss = onDismiss,
        onConfirm = onConfirm,
        testTag = "maple.rename",
    )
}

/** Name of a new album; it is created as `Pictures/<name>/` by the first move or copy into it. */
@Composable
internal fun NewAlbumDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    NameDialog(
        title = stringResource(R.string.target_new_album),
        label = stringResource(R.string.target_new_album_label),
        initial = "",
        selectionEnd = 0,
        confirmText = stringResource(R.string.dialog_button_confirm),
        check = MediaFileNamePolicy::checkFolder,
        externalError = null,
        busy = false,
        onDismiss = onDismiss,
        onConfirm = onConfirm,
        testTag = "maple.newAlbum",
    )
}

/** Album picker of move / copy: "new album", the recently used targets, then every writable album. */
@Composable
internal fun TargetAlbumDialog(
    copy: Boolean,
    recent: List<TargetAlbum>,
    albums: List<TargetAlbum>,
    onNewAlbum: () -> Unit,
    onPick: (TargetAlbum) -> Unit,
    onDismiss: () -> Unit,
) {
    MapleDialog(
        title = stringResource(if (copy) R.string.target_copy_title else R.string.target_move_title),
        onDismissRequest = onDismiss,
        confirmText = stringResource(R.string.dialog_button_cancel),
        onConfirm = onDismiss,
        dismissText = null,
        modifier = Modifier.testTag("maple.target"),
    ) {
        MapleSettingsRow(
            title = stringResource(R.string.target_new_album),
            summary = stringResource(R.string.target_new_album_summary),
            iconResource = R.drawable.ic_add_24,
            showChevron = false,
            onClick = onNewAlbum,
            modifier = Modifier.testTag("maple.target.new"),
        )
        if (recent.isNotEmpty()) {
            MapleSectionHeader(stringResource(R.string.target_recent))
            recent.forEachIndexed { index, album -> TargetRow(album, onPick, "maple.target.recent.$index") }
        }
        if (albums.isNotEmpty()) {
            MapleSectionHeader(stringResource(R.string.target_all))
            albums.forEachIndexed { index, album -> TargetRow(album, onPick, "maple.target.album.$index") }
        }
    }
}

@Composable
private fun TargetRow(album: TargetAlbum, onPick: (TargetAlbum) -> Unit, tag: String) {
    MapleSettingsRow(
        title = album.name,
        summary = album.location?.let { "$it \u00b7 ${album.path.relativePath}" } ?: album.path.relativePath,
        iconResource = R.drawable.ic_folder_24,
        showChevron = false,
        onClick = { onPick(album) },
        modifier = Modifier.testTag(tag),
    )
}

/** Same names in the target (ROADMAP 7.3): asked once per batch, the choice applies to all of them. */
@Composable
internal fun ConflictDialog(count: Int, onDismiss: () -> Unit, onConfirm: (Int) -> Unit) {
    MapleChoiceDialog(
        title = pluralStringResource(R.plurals.conflict_title, count, count),
        options = listOf(
            MapleChoice(stringResource(R.string.conflict_auto_rename), stringResource(R.string.conflict_auto_rename_summary)),
            MapleChoice(stringResource(R.string.conflict_skip), stringResource(R.string.conflict_skip_summary)),
            MapleChoice(stringResource(R.string.conflict_replace), stringResource(R.string.conflict_replace_summary)),
        ),
        selectedIndex = 0,
        onDismissRequest = onDismiss,
        onConfirm = onConfirm,
        confirmText = stringResource(R.string.dialog_button_confirm),
        dismissText = stringResource(R.string.dialog_button_cancel),
        modifier = Modifier.testTag("maple.conflict"),
    )
}

/** Progress of a move or copy batch; cancel stops before the next image. */
@Composable
internal fun TransferProgressDialog(copy: Boolean, done: Int, total: Int, onCancel: () -> Unit) {
    MapleDialog(
        title = stringResource(if (copy) R.string.progress_copying else R.string.progress_moving),
        onDismissRequest = {},
        confirmText = stringResource(R.string.dialog_button_cancel),
        onConfirm = onCancel,
        dismissText = null,
        scrollable = false,
        modifier = Modifier.testTag("maple.progress"),
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = MapleSettingsDefaults.horizontalPadding)) {
            LinearProgressIndicator(
                progress = { if (total > 0) done.toFloat() / total else 0f },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            )
            Text(stringResource(R.string.progress_count, done, total), style = MaterialTheme.typography.bodyMedium)
        }
    }
}
