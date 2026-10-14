package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.content.ContentUris
import android.graphics.Color
import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.compose.ui.test.ComposeTimeoutException
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import kotlinx.coroutines.runBlocking
import org.autojs.plugin.explorer.api.IExplorerActionHostSession
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.UUID

/** ROADMAP 6.3: one instrumentation per viewer source kind that is not covered by the host-entry tests. */
@RunWith(AndroidJUnit4::class)
class ViewerSourceInstrumentationTest {

    @get:Rule
    val compose = createEmptyComposeRule()

    @get:Rule
    val permission: GrantPermissionRule = GrantPermissionRule.grant(*MediaStoreTestImages.permissions())

    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    @After
    fun resetLoader() {
        ViewerSourceLoader.factoryForTesting = null
    }

    /** A MediaStore album is enumerated inside the viewer: pager starts on the tapped item, the strip jumps pages. */
    @Test
    fun mediaAlbumSourcePagesTheWholeBucketFromTheTappedItem() {
        val folder = "MapleViewerTest-" + UUID.randomUUID().toString().take(8)
        val inserted = listOf(Color.RED, Color.GREEN, Color.BLUE).mapIndexed { index, color ->
            MediaStoreTestImages.insert(context, folder, "page-${index + 1}.png", color)
        }
        try {
            val ids = inserted.map { ContentUris.parseId(it) }.toSet()
            val items = runBlocking { MediaStoreRepository(context).loadAll() }.filter { it.id in ids }
            assertEquals("inserted rows are cataloged", 3, items.size)
            val sort = MediaSort(MediaSortField.NAME, ascending = true)
            val ordered = MediaCatalog.sort(items, sort)
            val start = ordered[1]
            val request = ImageViewerLaunchRequest.gallery(
                ViewerSource.request(start),
                ViewerSourceSpec.MediaAlbum(start.bucketId, sort, start.id),
            )
            ActivityScenario.launchActivityForResult<ImageViewerActivity>(
                ImageViewerContract.viewerIntent(context, request),
            ).use { scenario ->
                awaitSource(scenario) { it.kind == ViewerSourceKind.MEDIA_ALBUM && it.pages.size == 3 }
                scenario.onActivity { activity ->
                    val source = requireNotNull(activity.sourceForTesting)
                    assertEquals(ordered.map { it.displayName }, source.pages.map { it.image.displayName })
                    assertEquals(1, source.startIndex)
                    assertTrue(source.editable)
                }
                awaitPage(scenario, index = 1, title = start.displayName)
                scenario.onActivity { activity ->
                    assertTrue(activity.viewerStateForTesting.canEdit)
                    assertEquals("2 / 3", activity.viewerStateForTesting.subtitle)
                }
                compose.onNodeWithTag("maple.viewer.strip").assertIsDisplayed()
                compose.onNodeWithTag("maple.viewer.strip.1").assertIsSelected()
                compose.onNodeWithTag("maple.viewer.strip.2").performClick()
                awaitPage(scenario, index = 2, title = ordered[2].displayName)
                compose.onNodeWithTag("maple.viewer.strip.2").assertIsSelected()
                scenario.onActivity { activity -> activity.finish() }
                val result = scenario.result
                assertEquals(ordered[2].id, result.resultData.getLongExtra(ImageViewerContract.EXTRA_RESULT_ITEM_ID, -1L))
                assertEquals(2, result.resultData.getIntExtra(ImageViewerContract.EXTRA_RESULT_INDEX, -1))
            }
        } finally {
            MediaStoreTestImages.cleanUp(context, inserted, folder)
        }
    }

    /** An authorized folder resolves through the injected enumeration; overlay visibility survives recreation. */
    @Test
    fun authorizedFolderSourceUsesTheEnumerationAndKeepsTheStripStateAcrossRecreate() {
        val fixture = HostFixture(context, listOf("alpha.png" to Color.RED, "beta.png" to Color.GREEN, "gamma.png" to Color.BLUE))
        try {
            val treeUri = "content://com.android.externalstorage.documents/tree/primary%3APictures"
            val items = fixture.items(bucketId = -7L)
            val sort = MediaSort(MediaSortField.NAME, ascending = true)
            var requestedTree: String? = null
            ViewerSourceLoader.factoryForTesting = {
                ViewerSourceLoader(
                    loadAlbum = { _, _ -> error("album enumeration must not run for a folder source") },
                    loadFolder = { tree, requestedSort -> requestedTree = tree; MediaCatalog.sort(items, requestedSort) },
                )
            }
            val start = items[2]
            val request = ImageViewerLaunchRequest.gallery(
                ViewerSource.request(start),
                ViewerSourceSpec.AuthorizedFolder(treeUri, sort, start.id),
            )
            ActivityScenario.launchActivityForResult<ImageViewerActivity>(
                ImageViewerContract.viewerIntent(context, request),
            ).use { scenario ->
                awaitSource(scenario) { it.kind == ViewerSourceKind.AUTHORIZED_FOLDER && it.pages.size == 3 }
                assertEquals(treeUri, requestedTree)
                awaitPage(scenario, index = 2, title = "gamma.png")
                scenario.onActivity { activity -> activity.flipPageForTesting(ZoomableImageView.DIRECTION_PREVIOUS) }
                awaitPage(scenario, index = 1, title = "beta.png")
                compose.onNodeWithTag("maple.viewer.strip.1").assertIsSelected()
                scenario.onActivity { activity -> activity.toggleControlsForTesting() }
                compose.onNodeWithTag("maple.viewer.strip").assertDoesNotExist()

                scenario.recreate()

                awaitSource(scenario) { it.pages.size == 3 }
                awaitPage(scenario, index = 1, title = "beta.png")
                scenario.onActivity { activity ->
                    assertFalse(activity.viewerStateForTesting.controlsVisible)
                    activity.toggleControlsForTesting()
                }
                compose.onNodeWithTag("maple.viewer.strip").assertIsDisplayed()
                compose.onNodeWithTag("maple.viewer.strip.1").assertIsSelected()
            }
        } finally {
            fixture.close()
        }
    }

    /** A single URI stays a one-page viewer without a strip. */
    /** A hidden folder (ROADMAP 7.4) survives the intent round trip and pages without management actions. */
    @Test
    fun hiddenFolderSourceUsesTheFileEnumerationWithoutManagement() {
        val fixture = HostFixture(context, listOf("alpha.png" to Color.RED, "beta.png" to Color.GREEN, "gamma.png" to Color.BLUE))
        try {
            val items = fixture.items(bucketId = -9L)
            val sort = MediaSort(MediaSortField.NAME, ascending = true)
            var requestedPath: String? = null
            ViewerSourceLoader.factoryForTesting = {
                ViewerSourceLoader(
                    loadAlbum = { _, _ -> error("album enumeration must not run for a hidden folder") },
                    loadFolder = { _, _ -> error("folder enumeration must not run for a hidden folder") },
                    loadHidden = { path, requestedSort -> requestedPath = path; MediaCatalog.sort(items, requestedSort) },
                )
            }
            val start = items[1]
            val request = ImageViewerLaunchRequest.gallery(ViewerSource.request(start), ViewerSourceSpec.HiddenFolder("Pictures/Private/", sort, start.id))
            ActivityScenario.launchActivityForResult<ImageViewerActivity>(ImageViewerContract.viewerIntent(context, request)).use { scenario ->
                awaitSource(scenario) { it.kind == ViewerSourceKind.HIDDEN_FOLDER && it.pages.size == 3 }
                assertEquals("Pictures/Private/", requestedPath)
                awaitPage(scenario, index = 1, title = "beta.png")
                scenario.onActivity { activity -> assertEquals(null, activity.viewerStateForTesting.management) }
            }
        } finally {
            fixture.close()
        }
    }

    @Test
    fun singleSourceHasOnePageAndNoStrip() {
        val fixture = HostFixture(context, listOf("solo.png" to Color.MAGENTA))
        try {
            val request = ImageViewerLaunchRequest.single(fixture.items(bucketId = -1L).single().let(ViewerSource::request), editable = true)
            ActivityScenario.launchActivityForResult<ImageViewerActivity>(
                ImageViewerContract.viewerIntent(context, request),
            ).use { scenario ->
                awaitSource(scenario) { it.kind == ViewerSourceKind.SINGLE && it.pages.size == 1 }
                awaitPage(scenario, index = 0, title = "solo.png")
                scenario.onActivity { activity ->
                    assertEquals(1, activity.viewerStateForTesting.pageCount)
                    assertEquals(null, activity.viewerStateForTesting.subtitle)
                    assertEquals(null, activity.viewerStateForTesting.rotationHint)
                }
                compose.onNodeWithTag("maple.viewer.strip").assertDoesNotExist()

                // ROADMAP D8: a rotation shows the "viewing only" hint; Undo restores the orientation and hides it.
                scenario.onActivity { activity -> assertTrue(activity.rotateForTesting()) }
                compose.onNodeWithTag("maple.viewer.rotationHint").assertIsDisplayed()
                scenario.onActivity { activity ->
                    assertEquals(
                        activity.getString(R.string.viewer_rotation_hint, 90),
                        activity.viewerStateForTesting.rotationHint,
                    )
                }
                compose.onNodeWithTag("maple.viewer.rotationHint.undo").performClick()
                compose.waitUntil(TIMEOUT_MILLIS) {
                    var done = false
                    scenario.onActivity { activity ->
                        done = activity.viewerStateForTesting.rotationHint == null &&
                            activity.currentImageViewForTesting()?.currentRotationQuarterTurns == 0
                    }
                    done
                }
                scenario.onActivity { activity -> assertTrue(activity.rotateForTesting()) }
                compose.onNodeWithTag("maple.viewer.rotationHint.dismiss").performClick()
                compose.onNodeWithTag("maple.viewer.rotationHint").assertDoesNotExist()
                scenario.onActivity { activity ->
                    assertEquals(1, activity.currentImageViewForTesting()?.currentRotationQuarterTurns)
                }
            }
        } finally {
            fixture.close()
        }
    }

    // region Helpers

    /** The compose rule drives the frame clock, so polling goes through [ComposeTestRule.waitUntil]. */
    private fun awaitSource(scenario: ActivityScenario<ImageViewerActivity>, predicate: (ViewerSource) -> Boolean) {
        try {
            compose.waitUntil(TIMEOUT_MILLIS) {
                var ready = false
                scenario.onActivity { activity -> ready = activity.sourceForTesting?.let(predicate) == true }
                ready
            }
        } catch (_: ComposeTimeoutException) {
            fail("Timed out waiting for the viewer source")
        }
    }

    private fun awaitPage(scenario: ActivityScenario<ImageViewerActivity>, index: Int, title: String) {
        var observed = ""
        try {
            compose.waitUntil(TIMEOUT_MILLIS) {
                var ready = false
                scenario.onActivity { activity ->
                    val state = activity.viewerStateForTesting
                    observed = "index=${state.pageIndex} title=${state.title} print=${state.printEnabled} error=${state.errorText}"
                    ready = state.pageIndex == index && state.title == title && state.printEnabled
                }
                ready
            }
        } catch (_: ComposeTimeoutException) {
            fail("Timed out waiting for page $index ($title): $observed")
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
    }

    /** Files in the app cache exposed through the host image provider, so folder and single sources need no SAF tree. */
    private class HostFixture(context: android.content.Context, pages: List<Pair<String, Int>>) {
        private val directory = File(context.cacheDir, "viewer-source-" + UUID.randomUUID()).apply { mkdirs() }
        private val files = pages.associate { (name, color) ->
            name to File(directory, name).also { file -> file.writeBytes(MediaStoreTestImages.solidPng(color)) }
        }
        private val token: String
        private val names = pages.map { it.first }

        init {
            val session = object : IExplorerActionHostSession.Default() {
                override fun openFile(targetId: String?, relativePath: String?): ParcelFileDescriptor? =
                    files[relativePath]?.let { ParcelFileDescriptor.open(it, ParcelFileDescriptor.MODE_READ_ONLY) }
            }
            val seed = ImageViewerLaunchRequest(
                pages = listOf(
                    ImageViewerPage(
                        image = ImageViewerRequest(Uri.parse("content://viewer-source.test/selected.png"), "selected.png", 1L, "image/png"),
                        hostRelativePath = "",
                    ),
                ) + names.mapIndexed { index, name ->
                    ImageViewerPage(
                        image = ImageViewerRequest(ImageViewerContract.hostImageUri(index + 1), name, requireNotNull(files[name]).length(), "image/png"),
                        hostRelativePath = name,
                    )
                },
                hostSession = session,
                hostTargetId = "viewer-source-fixture",
            )
            token = HostSessionImageRegistry.register(seed)
        }

        fun items(bucketId: Long): List<MediaItem> = names.mapIndexed { index, name ->
            MediaItem(
                id = 1_000L + index,
                displayName = name,
                mimeType = "image/png",
                sizeBytes = requireNotNull(files[name]).length(),
                width = MediaStoreTestImages.PIXELS,
                height = MediaStoreTestImages.PIXELS,
                dateModifiedSeconds = 1_700_000_000L + index,
                dateTakenMillis = null,
                bucketId = bucketId,
                bucketName = "fixture",
                relativePath = "fixture/",
                contentUri = HostSessionImageRegistry.imageUri(token, index + 1).toString(),
            )
        }

        fun close() {
            HostSessionImageRegistry.unregister(token)
            directory.deleteRecursively()
        }
    }

    // endregion

    private companion object {
        // Upper bound only: the shared CI emulator renders the paged viewer in software and is much slower.
        const val TIMEOUT_MILLIS = 30_000L
    }
}
