package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.Manifest
import android.content.ContentUris
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.view.KeyEvent
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performSemanticsAction
import androidx.core.content.FileProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.ViewerUiState
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.UUID

/**
 * ROADMAP 9.3: with a keyboard or D-pad the viewer turns pages with left and right and asks to delete with the delete
 * key; in a media grid the delete key asks to delete the focused image.
 */
@RunWith(AndroidJUnit4::class)
class KeyboardInstrumentationTest {

    @get:Rule(order = 0)
    val permissions: GrantPermissionRule = GrantPermissionRule.grant(
        *(MediaStoreTestImages.permissions().toSet() + if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) setOf(Manifest.permission.WRITE_EXTERNAL_STORAGE) else emptySet()).toTypedArray(),
    )

    @get:Rule(order = 1)
    val compose = createEmptyComposeRule()

    private val instrumentation get() = InstrumentationRegistry.getInstrumentation()
    private val context get() = instrumentation.targetContext
    private val store get() = ApplicationSettingsStore(context)
    private lateinit var original: ApplicationSettings
    private val folder = "MapleKeys-" + UUID.randomUUID().toString().take(8)
    private val inserted = mutableListOf<Uri>()
    private lateinit var directory: File
    private val record get() = MediaPermissionRecordStore(context)
    private var originalRequested = false

    @Before fun prepare() {
        TestScreen.awake()
        original = store.load()
        // Without the recycle bin the delete asks in the app's own dialog on every version.
        store.save(original.copy(recycleBinEnabled = false))
        originalRequested = record.requestedBefore
        record.markRequested()
        directory = File(context.cacheDir, "standalone-images/keys-${UUID.randomUUID()}").apply { mkdirs() }
    }

    @After fun restore() {
        instrumentation.setInTouchMode(true)
        store.save(original)
        if (!originalRequested) record.reset()
        directory.deleteRecursively()
        MediaStoreTestImages.cleanUp(context, inserted, folder)
    }

    private fun exists(tag: String): Boolean =
        runCatching { compose.onAllNodes(hasTestTag(tag)).fetchSemanticsNodes().isNotEmpty() }.getOrDefault(false)

    private fun key(code: Int) {
        instrumentation.sendKeyDownUpSync(code)
        compose.waitForIdle()
    }

    /** Keys go to the focused window, and a page turn needs the pager: a key sent before both are there is lost. */
    private fun ActivityScenario<ImageViewerActivity>.hasWindowFocus(): Boolean {
        var ready = false
        onActivity { ready = it.hasWindowFocus() && (it.currentImageViewForTesting()?.width ?: 0) > 0 }
        return ready
    }

    private fun ActivityScenario<ImageViewerActivity>.state(): ViewerUiState {
        var state: ViewerUiState? = null
        onActivity { state = it.viewerStateForTesting }
        return requireNotNull(state)
    }

    @Test fun leftAndRightTurnTheViewersPages() {
        val pages = listOf(Color.RED, Color.GREEN, Color.BLUE).mapIndexed { index, color ->
            val file = File(directory, "$index.png")
            val bitmap = Bitmap.createBitmap(40, 30, Bitmap.Config.ARGB_8888).apply { eraseColor(color) }
            file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
            bitmap.recycle()
            ImageViewerRequest(FileProvider.getUriForFile(context, context.packageName + ".standalone-images", file), file.name, file.length(), "image/png")
        }
        ActivityScenario.launchActivityForResult<ImageViewerActivity>(
            ImageViewerContract.viewerIntent(context, ImageViewerLaunchRequest.explicitSelection(pages)),
        ).use { scenario ->
            compose.waitUntil(TIMEOUT) { scenario.state().let { !it.loading && it.pageCount == 3 } && scenario.hasWindowFocus() }
            // In touch mode the platform spends the first arrow key on leaving it (a control takes focus) before any
            // app sees the key; a keyboard user is past that after one press.
            instrumentation.setInTouchMode(false)
            key(KeyEvent.KEYCODE_DPAD_RIGHT)
            compose.waitUntil(TIMEOUT) { scenario.state().pageIndex == 1 }
            key(KeyEvent.KEYCODE_PAGE_DOWN)
            compose.waitUntil(TIMEOUT) { scenario.state().pageIndex == 2 }
            key(KeyEvent.KEYCODE_DPAD_LEFT)
            compose.waitUntil(TIMEOUT) { scenario.state().pageIndex == 1 }
            // These pages cannot be deleted: the key does nothing.
            key(KeyEvent.KEYCODE_FORWARD_DEL)
            assertEquals(false, exists("maple.dialog"))
        }
    }

    @Test fun theDeleteKeyAsksToDeleteTheViewersImage() {
        inserted += MediaStoreTestImages.insert(context, folder, "a.png", Color.RED)
        val item = runBlocking { MediaStoreRepository(context).also { it.invalidate() }.loadAll() }.first { it.id == ContentUris.parseId(inserted[0]) }
        val request = ImageViewerLaunchRequest.gallery(ViewerSource.request(item), ViewerSourceSpec.MediaAlbum(item.bucketId, MediaSort.DEFAULT, item.id))
        ActivityScenario.launchActivityForResult<ImageViewerActivity>(ImageViewerContract.viewerIntent(context, request)).use { scenario ->
            compose.waitUntil(TIMEOUT) { scenario.state().let { !it.loading && it.management != null } && scenario.hasWindowFocus() }
            key(KeyEvent.KEYCODE_DEL)
            compose.waitUntil(TIMEOUT) { exists("maple.dialog") }
            compose.onNodeWithTag("maple.dialog.cancel").performClick()
            compose.waitUntil(TIMEOUT) { !exists("maple.dialog") }
        }
    }

    @Test fun theDeleteKeyAsksToDeleteTheFocusedImageOfAGrid() {
        inserted += MediaStoreTestImages.insert(context, folder, "a.png", Color.RED)
        inserted += MediaStoreTestImages.insert(context, folder, "b.png", Color.GREEN)
        val ids = inserted.map { ContentUris.parseId(it) }
        val bucket = runBlocking { MediaStoreRepository(context).also { it.invalidate() }.loadAll() }.first { it.id in ids }.bucketId
        ActivityScenario.launch(HomeActivity::class.java).use { scenario ->
            val album = "maple.gallery.album.media.$bucket"
            compose.waitUntil(TIMEOUT) { exists("maple.gallery.grid") && exists(album).also { if (!it) runCatching { compose.onNodeWithTag("maple.gallery.grid").performScrollToNode(hasTestTag(album)) } } }
            compose.onNodeWithTag("maple.gallery.grid").performScrollToNode(hasTestTag(album))
            compose.onNodeWithTag(album).performClick()
            val focused = "maple.media.item.${ids[1]}"
            compose.waitUntil(TIMEOUT) { exists(focused) }
            // A real key press leaves touch mode, where clickable cells cannot take focus.
            instrumentation.setInTouchMode(false)
            compose.onNodeWithTag(focused).performSemanticsAction(SemanticsActions.RequestFocus)
            compose.onNodeWithTag(focused).assertIsFocused()
            key(KeyEvent.KEYCODE_FORWARD_DEL)
            compose.waitUntil(TIMEOUT) { exists("maple.dialog") }
            var selection: Set<Long> = emptySet()
            scenario.onActivity { selection = it.galleryForTesting.grid.value.selection }
            assertEquals("the focused image is the one asked about", setOf(ids[1]), selection)
            compose.onNodeWithTag("maple.dialog.cancel").performClick()
        }
    }

    private companion object {
        const val TIMEOUT = 30_000L
    }
}
