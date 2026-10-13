package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.os.Build
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** ROADMAP 6.1 storage permission page: the rows follow the resolved state and the platform level. */
@RunWith(AndroidJUnit4::class)
class StoragePermissionInstrumentationTest {

    @get:Rule
    val compose = createEmptyComposeRule()

    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Test fun pageShowsStateActionAllFilesAndFolderRows() {
        ActivityScenario.launch(StoragePermissionActivity::class.java).use { scenario ->
            var state: MediaAccessState? = null
            var action: MediaPermissionAction? = null
            scenario.onActivity { activity ->
                val resolution = MediaPermissionGate(activity).resolve()
                state = resolution.state
                action = resolution.nextAction
            }
            val stateLabel = when (state!!) {
                MediaAccessState.FULL -> R.string.storage_state_full
                MediaAccessState.PARTIAL -> R.string.storage_state_partial
                MediaAccessState.DENIED -> R.string.storage_state_denied
                MediaAccessState.NEVER_ASK -> R.string.storage_state_never_ask
                MediaAccessState.ALL_FILES -> R.string.storage_state_all_files
            }
            compose.onNodeWithTag("maple.storage.state").assertIsDisplayed().assertTextContains(context.getString(stateLabel))
            when (action!!) {
                MediaPermissionAction.REQUEST -> compose.onNodeWithTag("maple.storage.grant").assertIsDisplayed().assertIsEnabled()
                MediaPermissionAction.OPEN_APP_SETTINGS -> compose.onNodeWithTag("maple.storage.appSettings").assertIsDisplayed()
                MediaPermissionAction.MANAGE_SELECTED -> compose.onNodeWithTag("maple.storage.manageSelected").assertIsDisplayed()
                MediaPermissionAction.NONE -> {
                    compose.onNodeWithTag("maple.storage.grant").assertDoesNotExist()
                    compose.onNodeWithTag("maple.storage.appSettings").assertDoesNotExist()
                }
            }
            val allFiles = compose.onNodeWithTag("maple.storage.allFiles").performScrollTo().assertIsDisplayed()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) allFiles.assertIsEnabled() else allFiles.assertIsNotEnabled()
            compose.onNodeWithTag("maple.storage.addFolder").performScrollTo().assertIsDisplayed().assertIsEnabled()
            if (AuthorizedFolderRepository(context).list().isEmpty()) {
                compose.onNodeWithTag("maple.storage.noFolders").performScrollTo().assertIsDisplayed()
            } else {
                compose.onNodeWithTag("maple.storage.folder.0").performScrollTo().assertIsDisplayed()
                compose.onNodeWithTag("maple.storage.removeFolder.0").assertIsDisplayed()
            }
        }
    }
}
