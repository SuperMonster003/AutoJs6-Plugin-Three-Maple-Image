package io.github.supermonster003.autojs6.plugin.three.maple.image

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** ROADMAP 6.1: the Android-free permission state machine across the platform matrix and every user outcome. */
class MediaPermissionPolicyTest {

    private val levels = listOf(24, 28, 29, 30, 33, 34, 36)

    private fun inputs(
        sdkInt: Int,
        read: Boolean = false,
        selected: Boolean = false,
        allFiles: Boolean = false,
        rationale: Boolean = false,
        requested: Boolean = false,
        denied: Int = 0,
    ) = MediaPermissionInputs(sdkInt, read, selected, allFiles, rationale, requested, denied)

    @Test
    fun permissionsFollowThePlatformLevel() {
        assertEquals(listOf(MediaPermissionPolicy.READ_EXTERNAL_STORAGE, MediaPermissionPolicy.WRITE_EXTERNAL_STORAGE), MediaPermissionPolicy.permissionsToRequest(24))
        assertEquals(listOf(MediaPermissionPolicy.READ_EXTERNAL_STORAGE, MediaPermissionPolicy.WRITE_EXTERNAL_STORAGE), MediaPermissionPolicy.permissionsToRequest(29))
        assertEquals(listOf(MediaPermissionPolicy.READ_EXTERNAL_STORAGE), MediaPermissionPolicy.permissionsToRequest(30))
        assertEquals(listOf(MediaPermissionPolicy.READ_EXTERNAL_STORAGE), MediaPermissionPolicy.permissionsToRequest(32))
        assertEquals(listOf(MediaPermissionPolicy.READ_MEDIA_IMAGES), MediaPermissionPolicy.permissionsToRequest(33))
        assertEquals(listOf(MediaPermissionPolicy.READ_MEDIA_IMAGES, MediaPermissionPolicy.READ_MEDIA_VISUAL_USER_SELECTED), MediaPermissionPolicy.permissionsToRequest(34))
        assertEquals(listOf(MediaPermissionPolicy.READ_MEDIA_IMAGES, MediaPermissionPolicy.READ_MEDIA_VISUAL_USER_SELECTED), MediaPermissionPolicy.permissionsToRequest(36))
        assertEquals(MediaPermissionPolicy.READ_EXTERNAL_STORAGE, MediaPermissionPolicy.readPermission(32))
        assertEquals(MediaPermissionPolicy.READ_MEDIA_IMAGES, MediaPermissionPolicy.readPermission(33))
    }

    @Test
    fun firstLaunchIsDeniedAndRequestsExactlyOnceAutomatically() {
        for (sdk in levels) {
            val first = MediaPermissionPolicy.resolve(inputs(sdk))
            assertEquals("api $sdk", MediaAccessState.DENIED, first.state)
            assertEquals("api $sdk", MediaPermissionAction.REQUEST, first.nextAction)
            assertEquals("api $sdk", MediaPermissionPolicy.permissionsToRequest(sdk), first.permissionsToRequest)
            assertTrue("api $sdk", first.autoRequestOnLaunch)
            assertFalse("api $sdk", first.canReadMediaStore)
        }
    }

    @Test
    fun deniedOnceKeepsTheDialogAvailableButStopsAskingAutomatically() {
        for (sdk in levels) {
            val denied = MediaPermissionPolicy.resolve(inputs(sdk, rationale = true, requested = true, denied = 1))
            assertEquals("api $sdk", MediaAccessState.DENIED, denied.state)
            assertEquals("api $sdk", MediaPermissionAction.REQUEST, denied.nextAction)
            assertFalse("api $sdk", denied.autoRequestOnLaunch)
        }
    }

    @Test
    fun dismissedDialogOrPendingDialogStaysDeniedNotNeverAsk() {
        for (sdk in levels) {
            val pending = MediaPermissionPolicy.resolve(inputs(sdk, rationale = false, requested = true, denied = 0))
            assertEquals("api $sdk pending", MediaAccessState.DENIED, pending.state)
            assertFalse("api $sdk pending", pending.autoRequestOnLaunch)
            val dismissed = MediaPermissionPolicy.resolve(inputs(sdk, rationale = false, requested = true, denied = 1))
            assertEquals("api $sdk dismissed", MediaAccessState.DENIED, dismissed.state)
            assertEquals("api $sdk dismissed", MediaPermissionAction.REQUEST, dismissed.nextAction)
        }
    }

    @Test
    fun permanentDenialRoutesToTheAppSettingsPage() {
        for (sdk in levels) {
            val never = MediaPermissionPolicy.resolve(inputs(sdk, rationale = false, requested = true, denied = 2))
            assertEquals("api $sdk", MediaAccessState.NEVER_ASK, never.state)
            assertEquals("api $sdk", MediaPermissionAction.OPEN_APP_SETTINGS, never.nextAction)
            assertTrue("api $sdk", never.permissionsToRequest.isEmpty())
            assertFalse("api $sdk", never.autoRequestOnLaunch)
        }
    }

    @Test
    fun fullGrantNeedsNothingMore() {
        for (sdk in levels) {
            val full = MediaPermissionPolicy.resolve(inputs(sdk, read = true, requested = true))
            assertEquals("api $sdk", MediaAccessState.FULL, full.state)
            assertEquals("api $sdk", MediaPermissionAction.NONE, full.nextAction)
            assertTrue("api $sdk", full.canReadMediaStore)
            assertFalse("api $sdk", full.autoRequestOnLaunch)
        }
    }

    @Test
    fun partialAccessOnlyExistsFromAndroid14() {
        for (sdk in levels) {
            val partial = MediaPermissionPolicy.resolve(inputs(sdk, selected = true, requested = true, rationale = false, denied = 2))
            if (sdk >= 34) {
                assertEquals("api $sdk", MediaAccessState.PARTIAL, partial.state)
                assertEquals("api $sdk", MediaPermissionAction.MANAGE_SELECTED, partial.nextAction)
                assertEquals("api $sdk", MediaPermissionPolicy.permissionsToRequest(sdk), partial.permissionsToRequest)
                assertTrue("api $sdk", partial.canReadMediaStore)
            } else {
                assertEquals("api $sdk", MediaAccessState.NEVER_ASK, partial.state)
            }
        }
    }

    @Test
    fun allFilesAccessWinsFromAndroid11AndIsIgnoredBefore() {
        for (sdk in levels) {
            val all = MediaPermissionPolicy.resolve(inputs(sdk, allFiles = true, requested = true, rationale = true))
            if (sdk >= 30) {
                assertEquals("api $sdk", MediaAccessState.ALL_FILES, all.state)
                assertEquals("api $sdk", MediaPermissionAction.NONE, all.nextAction)
                assertTrue("api $sdk", all.canReadMediaStore)
            } else {
                assertEquals("api $sdk", MediaAccessState.DENIED, all.state)
            }
        }
    }
}
