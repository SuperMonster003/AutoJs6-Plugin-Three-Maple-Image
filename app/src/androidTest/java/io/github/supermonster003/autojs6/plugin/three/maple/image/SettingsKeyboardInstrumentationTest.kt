package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.app.Activity
import android.content.Intent
import android.os.ParcelFileDescriptor
import android.util.Log
import android.view.KeyEvent
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.ComposeTimeoutException
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.isFocused
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * ROADMAP 5.2: the settings, about and release history pages are reachable with a keyboard. Tab visits every settings
 * row (each scrolled into view when it takes focus), Enter acts on the focused row, a dialog's buttons are reachable
 * too, and the about and release history pages offer their controls the same way.
 */
@RunWith(AndroidJUnit4::class)
class SettingsKeyboardInstrumentationTest {

    @get:Rule
    val compose = createEmptyComposeRule()

    private val instrumentation get() = InstrumentationRegistry.getInstrumentation()
    private val context get() = instrumentation.targetContext
    private lateinit var original: ApplicationSettings

    @Before fun prepare() {
        TestScreen.awake()
        original = ApplicationSettingsStore(context).load()
    }

    @After fun restore() {
        instrumentation.setInTouchMode(true)
        ApplicationSettingsStore(context).save(original)
    }

    private fun exists(tag: String): Boolean =
        runCatching { compose.onAllNodes(hasTestTag(tag)).fetchSemanticsNodes().isNotEmpty() }.getOrDefault(false)

    private fun key(code: Int) {
        instrumentation.sendKeyDownUpSync(code)
        compose.waitForIdle()
    }

    /**
     * Leaves touch mode as a keyboard user does. Newer versions take [android.app.Instrumentation.setInTouchMode] at
     * once; older ones (API 24) learn it only from a key, so an up arrow goes first.
     */
    private fun leaveTouchMode() {
        instrumentation.setInTouchMode(false)
        key(KeyEvent.KEYCODE_DPAD_UP)
    }

    /**
     * The test tag of the focused control (or of its nearest tagged ancestor), null when nothing has focus. A page
     * keeps its focused row while a dialog is open over it, so a control inside the dialog wins.
     */
    private fun focusedTag(): String? {
        val chains = compose.onAllNodes(isFocused(), useUnmergedTree = true).fetchSemanticsNodes().map { node ->
            generateSequence(node) { it.parent }.mapNotNull { it.config.getOrNull(SemanticsProperties.TestTag) }.toList()
        }
        val chain = chains.firstOrNull { "maple.dialog" in it } ?: chains.firstOrNull() ?: return null
        return chain.firstOrNull() ?: UNTAGGED
    }

    /** Runs [command] as the shell user (it connects UiAutomation) and returns its output. */
    private fun shell(command: String): String =
        ParcelFileDescriptor.AutoCloseInputStream(instrumentation.uiAutomation.executeShellCommand(command)).bufferedReader().use { it.readText() }

    /**
     * The focused window and app as `dumpsys window` reports them, for diagnostics only. API 33 and 35 leave
     * mCurrentFocus out of `dumpsys window windows`, so the whole dump is read.
     */
    private fun focusedWindow(): String = runCatching {
        shell("dumpsys window").lineSequence().map(String::trim)
            .filter { it.startsWith("mCurrentFocus=") || it.startsWith("mFocusedApp=") }.distinct().joinToString()
            .ifEmpty { "mCurrentFocus not in dumpsys" }
    }.getOrElse { "dumpsys failed: $it" }

    private fun windowFocused(scenario: ActivityScenario<*>): Boolean = try {
        var focused = false
        compose.waitUntil(TIMEOUT) { scenario.onActivity { focused = it.hasWindowFocus() }; focused }
        true
    } catch (timeout: ComposeTimeoutException) {
        false
    }

    /**
     * Keys go to the focused window: while a system dialog has focus (an ANR or crash dialog of another app on a slow
     * emulator, say), every Tab would be lost. When focus does not come, the test closes system dialogs (error dialogs
     * take that as "wait" or "close") and brings the page back to the front, once, before it gives up. A dialog of the
     * page holds the window focus itself, so this is only for the page.
     */
    private fun awaitWindowFocus(scenario: ActivityScenario<*>, visited: List<String?> = emptyList()) {
        if (windowFocused(scenario)) return
        val before = focusedWindow()
        Log.w(LOG_TAG, "no window focus after $TIMEOUT ms ($before), closing system dialogs")
        shell("am broadcast -a android.intent.action.CLOSE_SYSTEM_DIALOGS")
        scenario.onActivity { it.startActivity(Intent(it, it.javaClass).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)) }
        if (!windowFocused(scenario)) {
            fail("the page has no window focus after $TIMEOUT ms, nor after closing system dialogs; visited: $visited; " +
                "before: $before; after: ${focusedWindow()}; ${TestScreen.summary()}")
        }
        Log.i(LOG_TAG, "window focus is back: ${focusedWindow()}")
    }

    /** Presses Tab until focus returns to a control seen before; every focused control must be on screen. */
    private fun tabCycle(scenario: ActivityScenario<*>): List<String> {
        val visited = mutableListOf<String>()
        repeat(MAX_TABS) {
            awaitWindowFocus(scenario, visited)
            key(KeyEvent.KEYCODE_TAB)
            val tag = focusedTag() ?: return@repeat
            if (tag in visited) return visited
            compose.onNode(isFocused(), useUnmergedTree = true).assertIsDisplayed()
            visited += tag
        }
        return visited
    }

    private fun tabTo(scenario: ActivityScenario<*>, tag: String) {
        val seen = mutableListOf<String?>()
        repeat(MAX_TABS) {
            val focused = focusedTag()
            if (focused == tag) return
            seen += focused
            if (!exists("maple.dialog")) awaitWindowFocus(scenario, seen)
            key(KeyEvent.KEYCODE_TAB)
        }
        throw AssertionError("Tab never reached $tag, focus went ${seen.distinct()}; ${focusedWindow()}; ${TestScreen.summary()}")
    }

    /** Fails with what Tab visited and which window has focus. */
    private fun expect(ok: Boolean, what: String, visited: List<String>) {
        if (!ok) fail("$what; visited: $visited; ${focusedWindow()}")
    }

    private fun <A : Activity> launch(type: Class<A>, ready: String, block: (ActivityScenario<A>) -> Unit) {
        ActivityScenario.launch(type).use { scenario ->
            compose.waitUntil(TIMEOUT) { exists(ready) }
            awaitWindowFocus(scenario)
            leaveTouchMode()
            block(scenario)
        }
    }

    @Test fun tabVisitsEverySettingsRowAndTheBackButton() = launch(AppSettingsActivity::class.java, "maple.settings.language") { scenario ->
        // A disabled row (extended detail fields while extended details are off) is skipped, as it should be.
        val rows = compose.onAllNodes(SemanticsMatcher("an enabled settings row") { node ->
            node.config.getOrNull(SemanticsProperties.TestTag)?.startsWith("maple.settings.") == true &&
                SemanticsProperties.Disabled !in node.config
        }).fetchSemanticsNodes().map { it.config[SemanticsProperties.TestTag] }
        assertTrue("rows: ${rows.size}", rows.size >= 30)
        val visited = tabCycle(scenario)
        expect((rows - visited.toSet()).isEmpty(), "rows Tab never reached: ${rows - visited.toSet()}", visited)
        expect("maple.topBar.navigation" in visited, "the back button is not reachable", visited)
        expect(UNTAGGED !in visited, "something focusable has no name", visited)
        // Tab goes down the page in reading order.
        val inOrder = visited.filter { it in rows }
        expect(inOrder == rows, "the rows in Tab order: expected $rows, got $inOrder", visited)
    }

    @Test fun enterOpensTheFocusedRowAndTheDialogCanBeCancelledFromTheKeyboard() = launch(AppSettingsActivity::class.java, "maple.settings.darkMode") { scenario ->
        tabTo(scenario, "maple.settings.darkMode")
        key(KeyEvent.KEYCODE_ENTER)
        compose.waitUntil(TIMEOUT) { exists("maple.dialog.cancel") }
        // The choices and both buttons are reachable; cancelling from the keyboard saves nothing.
        tabTo(scenario, "maple.choice.3")
        key(KeyEvent.KEYCODE_ENTER)
        tabTo(scenario, "maple.dialog.cancel")
        key(KeyEvent.KEYCODE_ENTER)
        compose.waitUntil(TIMEOUT) { !exists("maple.dialog.cancel") }
        assertEquals(original, ApplicationSettingsStore(context).load())
    }

    @Test fun theAboutPageOffersItsSourcesAndTheBackButton() = launch(AboutActivity::class.java, "maple.about.sources") { scenario ->
        val visited = tabCycle(scenario)
        expect(visited.containsAll(listOf("maple.topBar.navigation", "maple.about.sources")), "Tab never reached the back button or the sources row", visited)
    }

    /** Every release takes focus in turn, scrolled into view, down to the oldest one. */
    @Test fun theReleaseHistoryCanBeReadToTheEndFromTheKeyboard() = launch(ReleaseHistoryActivity::class.java, "maple.releaseHistory.list") { scenario ->
        var versions = emptyList<String>()
        scenario.onActivity { activity ->
            val asset = ReleaseHistoryAssetPolicy.assetFor(activity.resources.configuration.locales[0])
            versions = ReleaseHistoryParser.parse(activity.assets.open("doc/$asset").use { it.readBytes().toString(Charsets.UTF_8) }).map { it.version }
        }
        assertTrue("releases: ${versions.size}", versions.size > 10)
        val visited = tabCycle(scenario)
        expect("maple.topBar.navigation" in visited, "Tab never reached the back button", visited)
        val expected = versions.map { "maple.releaseHistory.entry.$it" }
        val entries = visited.filter { it.startsWith("maple.releaseHistory.entry.") }
        expect(entries == expected, "the releases in Tab order: expected $expected, got $entries", visited)
    }

    private companion object {
        const val LOG_TAG = "SettingsKeyboardTest"
        const val TIMEOUT = 30_000L
        const val MAX_TABS = 120
        const val UNTAGGED = "(untagged)"
    }
}
