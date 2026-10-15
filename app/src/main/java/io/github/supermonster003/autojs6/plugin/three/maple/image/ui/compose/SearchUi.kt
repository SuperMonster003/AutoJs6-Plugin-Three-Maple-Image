package io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose

import android.net.Uri
import android.os.SystemClock
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import io.github.supermonster003.autojs6.plugin.three.maple.image.AlbumSummary
import io.github.supermonster003.autojs6.plugin.three.maple.image.GalleryViewModel
import io.github.supermonster003.autojs6.plugin.three.maple.image.MediaItem
import io.github.supermonster003.autojs6.plugin.three.maple.image.MediaSearch
import io.github.supermonster003.autojs6.plugin.three.maple.image.R
import io.github.supermonster003.autojs6.plugin.three.maple.image.SearchUiState
import java.text.DateFormat

/** One-line search field for the top bar (ROADMAP 7.5): focused on appear, the keyboard's search key closes it. */
@Composable
internal fun MapleSearchField(query: String, onQueryChange: (String) -> Unit, hint: String, modifier: Modifier = Modifier) {
    val focus = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    LaunchedEffect(Unit) { runCatching { focus.requestFocus() } }
    BasicTextField(
        value = query,
        onValueChange = onQueryChange,
        singleLine = true,
        textStyle = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.onSurface),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { keyboard?.hide() }),
        modifier = modifier.fillMaxWidth().focusRequester(focus).testTag("maple.search.input"),
        decorationBox = { inner ->
            Box(contentAlignment = Alignment.CenterStart) {
                if (query.isEmpty()) {
                    Text(hint, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                }
                inner()
            }
        },
    )
}

/**
 * Results of the home search (ROADMAP 7.5): matching albums as rows, then the matching images as a grid. Before a
 * query it explains what is searched; without a match it says so.
 */
@OptIn(ExperimentalGlideComposeApi::class)
@Composable
internal fun SearchResults(
    search: SearchUiState,
    columns: Int,
    onOpenAlbum: (AlbumSummary) -> Unit,
    onOpenImage: (MediaItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    val empty = search.albums.isEmpty() && search.items.isEmpty()
    val locale = LocalConfiguration.current.locales[0]
    val dateFormat = remember(locale) { DateFormat.getDateInstance(DateFormat.MEDIUM, locale) }
    if (!empty && Log.isLoggable(GalleryViewModel.SEARCH_LOG_TAG, Log.DEBUG)) {
        LaunchedEffect(search.albums, search.items) {
            withFrameNanos { }
            Log.d(GalleryViewModel.SEARCH_LOG_TAG, "results drawn at ${SystemClock.elapsedRealtime()}")
        }
    }
    when {
        MediaSearch.terms(search.query).isEmpty() -> Text(
            text = stringResource(R.string.search_prompt),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = modifier.padding(horizontal = MapleSettingsDefaults.horizontalPadding, vertical = 16.dp).testTag("maple.search.prompt"),
        )
        empty && search.searching -> MapleLoading()
        empty -> MapleEmptyState(
            title = stringResource(R.string.search_no_results),
            icon = painterResource(R.drawable.ic_search_24),
            modifier = modifier.fillMaxWidth().testTag("maple.search.empty"),
        )
        else -> LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            horizontalArrangement = Arrangement.spacedBy(MapleGridDefaults.mediaGap),
            verticalArrangement = Arrangement.spacedBy(MapleGridDefaults.mediaGap),
            contentPadding = PaddingValues(bottom = 24.dp),
            modifier = modifier.fillMaxSize().testTag("maple.search.results"),
        ) {
            if (search.albums.isNotEmpty()) {
                item(key = "header.albums", span = { GridItemSpan(maxLineSpan) }) {
                    MapleSectionHeader(stringResource(R.string.search_section_albums) + " · " + search.albums.size)
                }
                items(search.albums, key = { "album.${it.kind.name}.${it.id}" }, span = { GridItemSpan(maxLineSpan) }) { album ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenAlbum(album) }
                            .padding(horizontal = MapleSettingsDefaults.horizontalPadding, vertical = 8.dp)
                            .testTag("maple.search.album.${album.id}"),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(modifier = Modifier.size(48.dp).clip(RoundedCornerShape(4.dp)).background(MaterialTheme.colorScheme.surfaceVariant)) {
                            album.coverUri?.let { cover ->
                                GlideImage(model = Uri.parse(cover), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                            }
                        }
                        Spacer(Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(album.name, style = MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            if (album.count >= 0) {
                                Text(
                                    pluralStringResource(R.plurals.gallery_image_count, album.count, album.count),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
            if (search.items.isNotEmpty()) {
                item(key = "header.images", span = { GridItemSpan(maxLineSpan) }) {
                    MapleSectionHeader(stringResource(R.string.search_section_images) + " · " + search.items.size)
                }
                items(search.items, key = { it.id }) { item ->
                    val model = remember(item.contentUri) { Uri.parse(item.contentUri) }
                    val description = remember(item.displayName, item.effectiveTakenMillis, dateFormat) { mediaCellDescription(item, dateFormat) }
                    MapleMediaCell(
                        model = model,
                        description = description,
                        badge = mediaTypeBadge(item.mimeType),
                        onClick = { onOpenImage(item) },
                        modifier = Modifier.testTag("maple.search.item.${item.id}"),
                    )
                }
            }
        }
    }
}
