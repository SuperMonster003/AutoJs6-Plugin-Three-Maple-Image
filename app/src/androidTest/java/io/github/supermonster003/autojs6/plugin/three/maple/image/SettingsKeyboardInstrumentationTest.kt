package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.app.Activity
import android.view.KeyEvent
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
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

    /** Presses Tab until focus returns to a control seen before; every focused control must be on screen. */
    private fun tabCycle(): List<String> {
        val visited = mutableListOf<String>()
        repeat(MAX_TABS) {
            key(KeyEvent.KEYCODE_TAB)
            val tag = focusedTag() ?: return@repeat
            if (tag in visited) return visited
            compose.onNode(isFocused(), useUnmergedTree = true).assertIsDisplayed()
            visited += tag
        }
        return visited
    }

    private fun tabTo(tag: String) {
        val seen = mutableListOf<String?>()
        repeat(MAX_TABS) {
            val focused = focusedTag()
            if (focused == tag) return
            seen += focused
            key(KeyEvent.KEYCODE_TAB)
        }
        throw AssertionError("Tab never reached $tag, focus went ${seen.distinct()}; ${TestScreen.summary()}")
    }

    private fun <A : Activity> launch(type: Class<A>, ready: String, block: (ActivityScenario<A>) -> Unit) {
        ActivityScenario.launch(type).use { scenario ->
            compose.waitUntil(TIMEOUT) { exists(ready) }
            leaveTouchMode()
            block(scenario)
        }
    }

    @Test fun tabVisitsEverySettingsRowAndTheBackButton() = launch(AppSettingsActivity::class.java, "maple.settings.language") {
        // A disabled row (extended detail fields while extended details are off) is skipped, as it should be.
        val rows = compose.onAllNodes(SemanticsMatcher("an enabled settings row") { node ->
            node.config.getOrNull(SemanticsProperties.TestTag)?.startsWith("maple.settings.") == true &&
                SemanticsProperties.Disabled !in node.config
        }).fetchSemanticsNodes().map { it.config[SemanticsProperties.TestTag] }
        assertTrue("rows: ${rows.size}", rows.size >= 30)
        val visited = tabCycle()
        assertEquals("rows Tab never reached", emptyList<String>(), rows - visited.toSet())
        assertTrue("the back button is reachable: $visited", "maple.topBar.navigation" in visited)
        assertTrue("nothing focusable without a name: $visited", UNTAGGED !in visited)
        // Tab goes down the page in reading order.
        assertEquals(rows, visited.filter { it in rows })
    }

    @Test fun enterOpensTheFocusedRowAndTheDialogCanBeCancelledFromTheKeyboard() = launch(AppSettingsActivity::class.java, "maple.settings.darkMode") {
        tabTo("maple.settings.darkMode")
        key(KeyEvent.KEYCODE_ENTER)
        compose.waitUntil(TIMEOUT) { exists("maple.dialog.cancel") }
        // The choices and both buttons are reachable; cancelling from the keyboard saves nothing.
        tabTo("maple.choice.3")
        key(KeyEvent.KEYCODE_ENTER)
        tabTo("maple.dialog.cancel")
        key(KeyEvent.KEYCODE_ENTER)
        compose.waitUntil(TIMEOUT) { !exists("maple.dialog.cancel") }
        assertEquals(original, ApplicationSettingsStore(context).load())
    }

    @Test fun theAboutPageOffersItsSourcesAndTheBackButton() = launch(AboutActivity::class.java, "maple.about.sources") {
        val visited = tabCycle()
        assertTrue(visited.toString(), visited.containsAll(listOf("maple.topBar.navigation", "maple.about.sources")))
    }

    /** Every release takes focus in turn, scrolled into view, down to the oldest one. */
    @Test fun theReleaseHistoryCanBeReadToTheEndFromTheKeyboard() = launch(ReleaseHistoryActivity::class.java, "maple.releaseHistory.list") { scenario ->
        var versions = emptyList<String>()
        scenario.onActivity { activity ->
            val asset = ReleaseHistoryAssetPolicy.assetFor(activity.resources.configuration.locales[0])
            versions = ReleaseHistoryParser.parse(activity.assets.open("doc/$asset").use { it.readBytes().toString(Charsets.UTF_8) }).map { it.version }
        }
        assertTrue("releases: ${versions.size}", versions.size > 10)
        val visited = tabCycle()
        assertTrue(visited.toString(), "maple.topBar.navigation" in visited)
        assertEquals(versions.map { "maple.releaseHistory.entry.$it" }, visited.filter { it.startsWith("maple.releaseHistory.entry.") })
    }

    private companion object {
        const val TIMEOUT = 30_000L
        const val MAX_TABS = 120
        const val UNTAGGED = "(untagged)"
    }
}
