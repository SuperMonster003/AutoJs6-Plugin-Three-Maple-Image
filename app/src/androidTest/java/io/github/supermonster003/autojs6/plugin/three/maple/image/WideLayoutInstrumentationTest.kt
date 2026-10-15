package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.Manifest
import android.content.ContentUris
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Build
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.core.content.FileProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.LayoutOverrides
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.UUID

/**
 * ROADMAP 9.3: a wide window shows the albums beside the open album, a fold or resize carries the open album between
 * the two layouts (also through the recreation a fold brings), and a landscape viewer keeps its thumbnail strip at
 * the side. The layouts are chosen through [LayoutOverrides]: resizing the display in the shared test process
 * strands Compose roots of earlier tests and hangs every later wait for idle.
 */
@RunWith(AndroidJUnit4::class)
class WideLayoutInstrumentationTest {

    @get:Rule(order = 0)
    val permissions: GrantPermissionRule = GrantPermissionRule.grant(
        *(MediaStoreTestImages.permissions().toSet() + if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) setOf(Manifest.permission.WRITE_EXTERNAL_STORAGE) else emptySet()).toTypedArray(),
    )

    @get:Rule(order = 1)
    val compose = createEmptyComposeRule()

    private val instrumentation get() = InstrumentationRegistry.getInstrumentation()
    private val context get() = instrumentation.targetContext
    private val folder = "MapleWide-" + UUID.randomUUID().toString().take(8)
    private val inserted = mutableListOf<Uri>()
    private val record get() = MediaPermissionRecordStore(context)
    private var originalRequested = false
    private lateinit var directory: File

    @Before fun prepare() {
        TestScreen.awake()
        originalRequested = record.requestedBefore
        record.markRequested()
        directory = File(context.cacheDir, "standalone-images/wide-${UUID.randomUUID()}").apply { mkdirs() }
    }

    @After fun restore() {
        wide(null)
        sideStrip(null)
        if (!originalRequested) record.reset()
        directory.deleteRecursively()
        MediaStoreTestImages.cleanUp(context, inserted, folder)
    }

    private fun wide(value: Boolean?) = instrumentation.runOnMainSync { LayoutOverrides.twoPane = value }

    private fun sideStrip(value: Boolean?) = instrumentation.runOnMainSync { LayoutOverrides.sideStrip = value }

    private fun exists(tag: String): Boolean =
        runCatching { compose.onAllNodes(hasTestTag(tag)).fetchSemanticsNodes().isNotEmpty() }.getOrDefault(false)

    private fun ActivityScenario<HomeActivity>.openAlbumId(): Long? {
        var id: Long? = null
        onActivity { id = it.galleryForTesting.grid.value.album?.id }
        return id
    }

    @Test fun theOpenAlbumFollowsTheWindowBetweenOneAndTwoPanes() {
        inserted += MediaStoreTestImages.insert(context, folder, "a.png", Color.RED)
        inserted += MediaStoreTestImages.insert(context, folder, "b.png", Color.GREEN)
        val ids = inserted.map { ContentUris.parseId(it) }
        val bucket = runBlocking { MediaStoreRepository(context).also { it.invalidate() }.loadAll() }.first { it.id in ids }.bucketId
        wide(true)
        ActivityScenario.launch(HomeActivity::class.java).use { scenario ->
            compose.waitUntil(TIMEOUT) { exists("maple.home.albumPane") && exists("maple.home.mediaPane") && exists("maple.gallery.grid") }
            assertTrue("nothing open yet", exists("maple.emptyState"))
            val album = "maple.gallery.album.media.$bucket"
            compose.waitUntil(TIMEOUT) {
                runCatching { compose.onNodeWithTag("maple.gallery.grid").performScrollToNode(hasTestTag(album)) }.isSuccess && exists(album)
            }
            compose.onNodeWithTag(album).performClick()
            compose.waitUntil(TIMEOUT) { exists("maple.media.grid") && exists("maple.media.item.${ids[0]}") }
            assertTrue("the albums stay beside it", exists("maple.home.albumPane"))

            // Folded (narrow, then the recreation a fold brings): the open album carries on as its own page.
            wide(false)
            compose.waitUntil(TIMEOUT) { exists("maple.media.grid") && !exists("maple.home.albumPane") }
            scenario.recreate()
            compose.waitUntil(TIMEOUT) { exists("maple.media.grid") && !exists("maple.home.albumPane") }
            assertEquals(bucket, scenario.openAlbumId())

            // Unfolded again: back beside the albums, still open, also after a recreation.
            wide(true)
            compose.waitUntil(TIMEOUT) { exists("maple.home.albumPane") && exists("maple.media.grid") }
            scenario.recreate()
            compose.waitUntil(TIMEOUT) { exists("maple.home.albumPane") && exists("maple.media.grid") }
            assertEquals(bucket, scenario.openAlbumId())

            // Closing it in the pane leaves the albums.
            compose.onNode(hasTestTag("maple.topBar.navigation") and hasAnyAncestor(hasTestTag("maple.home.mediaPane"))).performClick()
            compose.waitUntil(TIMEOUT) { exists("maple.emptyState") && !exists("maple.media.grid") }
        }
    }

    @Test fun aLandscapeViewerKeepsTheStripAtTheSide() {
        val pages = listOf(Color.RED, Color.GREEN, Color.BLUE).mapIndexed { index, color ->
            val file = File(directory, "$index.png")
            val bitmap = Bitmap.createBitmap(40, 30, Bitmap.Config.ARGB_8888).apply { eraseColor(color) }
            file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
            bitmap.recycle()
            ImageViewerRequest(FileProvider.getUriForFile(context, context.packageName + ".standalone-images", file), file.name, file.length(), "image/png")
        }
        sideStrip(true)
        ActivityScenario.launchActivityForResult<ImageViewerActivity>(
            ImageViewerContract.viewerIntent(context, ImageViewerLaunchRequest.explicitSelection(pages)),
        ).use {
            compose.waitUntil(TIMEOUT) { exists("maple.viewer.strip") }
            val strip = compose.onNodeWithTag("maple.viewer.strip").fetchSemanticsNode().boundsInRoot
            val root = compose.onNodeWithTag("maple.viewer.canvas").fetchSemanticsNode().boundsInRoot
            assertTrue("upright strip $strip", strip.height > strip.width)
            assertTrue("at the end side $strip of $root", strip.right > root.width * 0.9f)
        }
        sideStrip(false)
        ActivityScenario.launchActivityForResult<ImageViewerActivity>(
            ImageViewerContract.viewerIntent(context, ImageViewerLaunchRequest.explicitSelection(pages)),
        ).use {
            compose.waitUntil(TIMEOUT) { exists("maple.viewer.strip") }
            val strip = compose.onNodeWithTag("maple.viewer.strip").fetchSemanticsNode().boundsInRoot
            assertTrue("a portrait strip lies across the bottom $strip", strip.width > strip.height)
        }
    }

    private companion object {
        const val TIMEOUT = 30_000L
    }
}
