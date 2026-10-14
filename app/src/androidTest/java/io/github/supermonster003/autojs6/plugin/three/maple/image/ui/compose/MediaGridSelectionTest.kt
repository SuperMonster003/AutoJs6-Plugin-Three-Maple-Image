package io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import io.github.supermonster003.autojs6.plugin.three.maple.image.AlbumKind
import io.github.supermonster003.autojs6.plugin.three.maple.image.AlbumSummary
import io.github.supermonster003.autojs6.plugin.three.maple.image.MediaGridUiState
import io.github.supermonster003.autojs6.plugin.three.maple.image.MediaItem
import io.github.supermonster003.autojs6.plugin.three.maple.image.MediaSelection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

/** ROADMAP 7.1: long press enters selection, taps toggle, a drag after the long press selects a range. */
class MediaGridSelectionTest {

    @get:Rule
    val compose = createComposeRule()

    private val items = (1L..60L).map { id ->
        MediaItem(
            id = id, displayName = "img$id.png", mimeType = "image/png", sizeBytes = 100, width = 10, height = 10,
            dateModifiedSeconds = 1_000 - id, dateTakenMillis = null, bucketId = 1, bucketName = "Test",
            relativePath = "Pictures/Test/", contentUri = "content://media/external/images/media/$id",
        )
    }

    @Test
    fun longPressTapsAndADragRangeDriveTheSelection() {
        var state by mutableStateOf(
            MediaGridUiState(
                album = AlbumSummary(AlbumKind.MEDIA, 1, "Test", items.size, null, ""),
                items = items,
                loading = false,
                endReached = true,
                columns = 4,
            ),
        )
        var opened: Long? = null
        var base: Set<Long> = emptySet()
        var anchor = 0L
        val callbacks = MediaGridCallbacks(
            onBack = {},
            onOpen = { opened = it.id },
            onLoadMore = {},
            onSort = { _, _, _ -> },
            onRevealed = {},
            onColumns = {},
            onRefresh = {},
            onToggle = { state = state.copy(selection = MediaSelection.toggle(state.selection, it)) },
            onDragStart = {
                base = state.selection
                anchor = it
                state = state.copy(selection = state.selection + it)
            },
            onDragTo = { state = state.copy(selection = MediaSelection.range(base, items.map { item -> item.id }, anchor, it)) },
            onClearSelection = { state = state.copy(selection = emptySet()) },
        )
        compose.setContent {
            MapleTheme(palette = testPalette()) {
                MediaGridScreen(state = state, callbacks = callbacks, selectionActions = MediaSelectionActions(onShare = {}, onDelete = {}, onRename = {}, onDetails = {}))
            }
        }

        compose.onNodeWithTag("maple.media.item.1").performClick()
        assertEquals(1L, opened)
        opened = null

        compose.onNodeWithTag("maple.media.item.2").performTouchInput { longClick() }
        compose.waitForIdle()
        assertEquals("the release after a long press is not a tap", setOf(2L), state.selection)
        assertNull(opened)
        compose.onNodeWithTag("maple.media.item.2").assertIsSelected()
        compose.onNodeWithTag("maple.selection.share").assertExists()

        compose.onNodeWithTag("maple.media.item.3").performClick()
        compose.onNodeWithTag("maple.media.item.2").performClick()
        compose.waitForIdle()
        assertEquals(setOf(3L), state.selection)
        assertNull("taps toggle while selecting", opened)

        compose.onNodeWithTag("maple.selection.barMore").performClick()
        compose.onNodeWithTag("maple.selection.rename").assertExists()
        compose.onNodeWithTag("maple.selection.details").performClick()

        // Long press item 5, then drag to item 10 without lifting: items 5..10 join the selection.
        val grid = compose.onNodeWithTag("maple.media.grid").fetchSemanticsNode().boundsInRoot.topLeft
        val from = center("maple.media.item.5") - grid
        val to = center("maple.media.item.10") - grid
        compose.onNodeWithTag("maple.media.grid").performTouchInput {
            down(from)
            moveTo(from, delayMillis = viewConfiguration.longPressTimeoutMillis + 200)
            moveTo((from + to) / 2f, delayMillis = 50)
            moveTo(to, delayMillis = 50)
            up()
        }
        compose.waitForIdle()
        assertEquals(setOf(3L) + (5L..10L), state.selection)

        compose.onNodeWithTag("maple.topBar.navigation").performClick()
        compose.waitForIdle()
        assertEquals(emptySet<Long>(), state.selection)
        compose.onNodeWithTag("maple.selection.share").assertDoesNotExist()
    }

    @Test
    fun renameNeedsASingleSelection() {
        val state = MediaGridUiState(
            album = AlbumSummary(AlbumKind.MEDIA, 1, "Test", items.size, null, ""),
            items = items,
            loading = false,
            endReached = true,
            selection = setOf(1L, 2L),
        )
        compose.setContent {
            MapleTheme(palette = testPalette()) {
                MediaGridScreen(
                    state = state,
                    callbacks = MediaGridCallbacks(onBack = {}, onOpen = {}, onLoadMore = {}, onSort = { _, _, _ -> }, onRevealed = {}, onColumns = {}, onRefresh = {}),
                    selectionActions = MediaSelectionActions(onRename = {}),
                )
            }
        }
        compose.onNodeWithTag("maple.selection.barMore").performClick()
        compose.onNodeWithTag("maple.selection.rename").assertIsNotEnabled()
    }

    private fun center(tag: String): Offset = compose.onNodeWithTag(tag).fetchSemanticsNode().boundsInRoot.center
}
