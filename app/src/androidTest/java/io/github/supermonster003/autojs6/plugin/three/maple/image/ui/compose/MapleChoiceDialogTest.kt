package io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/** Three settings specification section 8: select, then confirm; cancel and dismissal never save. */
class MapleChoiceDialogTest {

    @get:Rule
    val compose = createComposeRule()

    private val options = listOf(
        MapleChoice("Follow AutoJs6", "Follow system"),
        MapleChoice("Light"),
        MapleChoice("Dark"),
        MapleChoice("Disabled", enabled = false),
    )

    private class Host {
        var saved by mutableIntStateOf(0)
        var confirmCount = 0
        var open by mutableStateOf(true)
    }

    private fun setDialog(host: Host, tester: StateRestorationTester? = null) {
        val content: @androidx.compose.runtime.Composable () -> Unit = {
            MapleTheme(palette = testPalette()) {
                if (host.open) {
                    MapleChoiceDialog(
                        title = "Dark mode",
                        options = options,
                        selectedIndex = host.saved,
                        onDismissRequest = { host.open = false },
                        onConfirm = { host.saved = it; host.confirmCount++; host.open = false },
                        confirmText = "OK",
                        dismissText = "Cancel",
                    )
                }
            }
        }
        if (tester != null) tester.setContent(content) else compose.setContent(content)
    }

    @Test
    fun selectingARowDoesNotSave() {
        val host = Host()
        setDialog(host)
        compose.onNodeWithTag("maple.choice.2").performClick()
        compose.onNodeWithTag("maple.choice.2").assertIsSelected()
        assertEquals(0, host.saved)
        assertEquals(0, host.confirmCount)
    }

    @Test
    fun cancelLeavesTheSavedValueUntouched() {
        val host = Host()
        setDialog(host)
        compose.onNodeWithTag("maple.choice.1").performClick()
        compose.onNodeWithTag("maple.dialog.cancel").performClick()
        compose.waitForIdle()
        assertEquals(0, host.saved)
        assertEquals(0, host.confirmCount)
        assertEquals(false, host.open)
    }

    @Test
    fun confirmSavesExactlyOnce() {
        val host = Host()
        setDialog(host)
        compose.onNodeWithTag("maple.choice.2").performClick()
        compose.onNodeWithTag("maple.dialog.confirm").assertIsEnabled().performClick()
        compose.waitForIdle()
        assertEquals(2, host.saved)
        assertEquals(1, host.confirmCount)
    }

    @Test
    fun disabledOptionCannotBeConfirmed() {
        val host = Host()
        setDialog(host)
        compose.onNodeWithTag("maple.choice.3").performClick()
        compose.onNodeWithTag("maple.choice.0").assertIsSelected()
        compose.onNodeWithTag("maple.dialog.confirm").assertIsEnabled()
    }

    @Test
    fun pendingSelectionSurvivesStateRestoration() {
        val host = Host()
        val tester = StateRestorationTester(compose)
        setDialog(host, tester)
        compose.onNodeWithTag("maple.choice.1").performClick()
        tester.emulateSavedInstanceStateRestore()
        compose.onNodeWithTag("maple.choice.1").assertIsSelected()
        assertEquals(0, host.saved)
        compose.onNodeWithTag("maple.dialog.confirm").performClick()
        compose.waitForIdle()
        assertEquals(1, host.saved)
        assertEquals(1, host.confirmCount)
    }
}
