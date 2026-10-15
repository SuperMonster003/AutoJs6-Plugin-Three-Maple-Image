package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.Manifest
import android.content.ContentUris
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTouchInput
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckPreset
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckResult.AccessibilityCheckResultType
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityHierarchyCheckResult
import com.google.android.apps.common.testing.accessibility.framework.Parameters
import com.google.android.apps.common.testing.accessibility.framework.checks.TouchTargetSizeCheck
import com.google.android.apps.common.testing.accessibility.framework.uielement.AccessibilityHierarchyAndroid
import com.google.android.apps.common.testing.accessibility.framework.utils.contrast.BitmapImage
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.ViewerUiState
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.text.DateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * ROADMAP 9.4: the Accessibility Test Framework checks (the ones Accessibility Scanner runs: labels, touch target
 * sizes, text and image contrast from a screenshot, duplicate descriptions, traversal order...) find no error on the
 * album grid, an album, selection mode, the viewer and the settings; warnings go to the log for the record. A media
 * cell reads its file name and the day it was taken.
 *
 * Like Accessibility Scanner the checks read the window's accessibility nodes (through UiAutomation) and a
 * screenshot: a hierarchy built from Views stops at the Compose view ("composable content is not evaluated").
 */
@RunWith(AndroidJUnit4::class)
class AccessibilityChecksInstrumentationTest {

    @get:Rule(order = 0)
    val permissions: GrantPermissionRule = GrantPermissionRule.grant(
        *(MediaStoreTestImages.permissions().toSet() + if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) setOf(Manifest.permission.WRITE_EXTERNAL_STORAGE) else emptySet()).toTypedArray(),
    )

    @get:Rule(order = 1)
    val compose = createEmptyComposeRule()

    private val instrumentation get() = InstrumentationRegistry.getInstrumentation()
    private val context get() = instrumentation.targetContext
    private val folder = "MapleA11y-" + UUID.randomUUID().toString().take(8)
    private val inserted = mutableListOf<Uri>()
    private val record get() = MediaPermissionRecordStore(context)
    private var originalRequested = false

    private val errors = linkedMapOf<String, List<String>>()
    private val touchTargetCheck = requireNotNull(AccessibilityCheckPreset.getHierarchyCheckForClass(TouchTargetSizeCheck::class.java))

    @Before fun prepare() {
        TestScreen.awake()
        originalRequested = record.requestedBefore
        record.markRequested()
    }

    @After fun restore() {
        if (!originalRequested) record.reset()
        MediaStoreTestImages.cleanUp(context, inserted, folder)
    }

    private fun exists(tag: String): Boolean =
        runCatching { compose.onAllNodes(hasTestTag(tag)).fetchSemanticsNodes().isNotEmpty() }.getOrDefault(false)

    /** Runs the checks over the app's window; errors are kept for the final assertion, warnings are logged. */
    private fun scan(screen: String) {
        compose.waitForIdle()
        val automation = instrumentation.uiAutomation
        var root: AccessibilityNodeInfo? = null
        compose.waitUntil(TIMEOUT) {
            root = automation.rootInActiveWindow
            root?.packageName == context.packageName
        }
        val hierarchy = AccessibilityHierarchyAndroid.newBuilder(requireNotNull(root), context).build()
        val parameters = Parameters().apply { putScreenCapture(BitmapImage(automation.takeScreenshot())) }
        val results = AccessibilityCheckPreset.getAccessibilityHierarchyChecksForPreset(AccessibilityCheckPreset.LATEST)
            .flatMap { check -> check.runCheckOnHierarchy(hierarchy, null, parameters) }
        val found = results.filter { it.type == AccessibilityCheckResultType.ERROR }.map(::describe).distinct()
        results.filter { it.type == AccessibilityCheckResultType.WARNING }.map(::describe).distinct().forEach { Log.i(TAG, "$screen warning: $it") }
        Log.i(TAG, "$screen: ${hierarchy.activeWindow.allViews.size} nodes, ${results.groupingBy { it.type }.eachCount()}, ${found.size} errors")
        found.forEach { Log.w(TAG, "$screen error: $it") }
        // The checks really read this screen: an impossible 200 dp touch target fails on its controls. A hierarchy
        // that stopped at the Compose view would pass everything silently.
        val strict = Parameters().apply { putCustomTouchTargetSize(IMPOSSIBLE_TOUCH_TARGET_DP) }
        val evaluated = touchTargetCheck.runCheckOnHierarchy(hierarchy, null, strict).count { it.type == AccessibilityCheckResultType.ERROR }
        assertTrue("$screen: the touch target check found no control to evaluate", evaluated > 0)
        errors[screen] = found
    }

    private fun describe(result: AccessibilityHierarchyCheckResult): String {
        val what = result.element?.let { element ->
            listOfNotNull(element.className, element.contentDescription?.toString(), element.text?.toString(), element.boundsInScreen).joinToString(" ")
        }
        return "${result.sourceCheckClass.simpleName}: $what: ${result.getMessage(Locale.ENGLISH)}"
    }

    private fun viewerState(scenario: ActivityScenario<ImageViewerActivity>): ViewerUiState {
        var state: ViewerUiState? = null
        scenario.onActivity { state = it.viewerStateForTesting }
        return requireNotNull(state)
    }

    @Test fun theMainScreensPassTheAccessibilityChecks() {
        inserted += MediaStoreTestImages.insert(context, folder, "a11y-one.png", Color.RED)
        inserted += MediaStoreTestImages.insert(context, folder, "a11y-two.png", Color.BLUE)
        val ids = inserted.map { ContentUris.parseId(it) }
        val items = runBlocking { MediaStoreRepository(context).also { it.invalidate() }.loadAll() }.filter { it.id in ids }
        val bucket = items.first().bucketId

        ActivityScenario.launch(HomeActivity::class.java).use {
            val album = "maple.gallery.album.media.$bucket"
            compose.waitUntil(TIMEOUT) { exists("maple.gallery.grid") && exists(album).also { if (!it) runCatching { compose.onNodeWithTag("maple.gallery.grid").performScrollToNode(hasTestTag(album)) } } }
            scan("albums")
            compose.onNodeWithTag(album).performClick()
            compose.waitUntil(TIMEOUT) { ids.all { id -> exists("maple.media.item.$id") } }
            scan("album")

            // A media cell reads its name and day (the device's locale and time zone).
            val format = DateFormat.getDateInstance(DateFormat.MEDIUM, context.resources.configuration.locales[0])
            for (item in items) {
                val node = compose.onNodeWithTag("maple.media.item.${item.id}").fetchSemanticsNode()
                assertEquals(listOf(item.displayName + ", " + format.format(Date(item.effectiveTakenMillis))), node.config[SemanticsProperties.ContentDescription])
            }

            compose.onNodeWithTag("maple.media.item.${ids[0]}").performTouchInput { longClick() }
            compose.waitUntil(TIMEOUT) { exists("maple.selection.selectAll") }
            scan("album, selecting")
        }

        val item = items.first { it.id == ids[0] }
        val request = ImageViewerLaunchRequest.gallery(ViewerSource.request(item), ViewerSourceSpec.MediaAlbum(item.bucketId, MediaSort.DEFAULT, item.id))
        ActivityScenario.launchActivityForResult<ImageViewerActivity>(ImageViewerContract.viewerIntent(context, request)).use { scenario ->
            compose.waitUntil(TIMEOUT) { viewerState(scenario).let { !it.loading && it.management != null && it.pageCount == 2 } }
            scan("viewer")
        }

        ActivityScenario.launch(AppSettingsActivity::class.java).use {
            compose.waitUntil(TIMEOUT) { exists("maple.settings.darkMode") }
            scan("settings")
        }

        val failed = errors.filterValues { it.isNotEmpty() }
        assertTrue(
            "accessibility errors:\n" + failed.entries.joinToString("\n") { (screen, lines) -> "$screen:\n  " + lines.joinToString("\n  ") },
            failed.isEmpty(),
        )
    }

    private companion object {
        const val TAG = "MapleA11yChecks"
        const val IMPOSSIBLE_TOUCH_TARGET_DP = 200
        const val TIMEOUT = 30_000L
    }
}
