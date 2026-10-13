package io.github.supermonster003.autojs6.plugin.three.maple.image

/** Photo access as the gallery sees it (ROADMAP T5). */
internal enum class MediaAccessState {
    /** Every image in MediaStore is readable. */
    FULL,
    /** Android 14+ "select photos": only the user-selected images are readable. */
    PARTIAL,
    /** Not granted; the system dialog can still be shown. */
    DENIED,
    /** Not granted and the system will not show the dialog again; only the app settings page can change it. */
    NEVER_ASK,
    /** `MANAGE_EXTERNAL_STORAGE` is granted (Android 11+), which implies full photo access plus direct file operations. */
    ALL_FILES,
}

/** What the gallery should offer next for the current [MediaAccessState]. */
internal enum class MediaPermissionAction {
    /** Show the system permission dialog for [MediaPermissionPolicy.permissionsToRequest]. */
    REQUEST,
    /** Open the system app details page because the dialog is no longer available. */
    OPEN_APP_SETTINGS,
    /** Android 14+: let the user change the selected photos through the system picker. */
    MANAGE_SELECTED,
    /** Nothing to do. */
    NONE,
}

/** Everything the policy needs, gathered by the caller from the platform and the persisted request record. */
internal data class MediaPermissionInputs(
    val sdkInt: Int,
    /** `READ_MEDIA_IMAGES` on API 33+, `READ_EXTERNAL_STORAGE` below. */
    val readImagesGranted: Boolean,
    /** `READ_MEDIA_VISUAL_USER_SELECTED` on API 34+; ignored below. */
    val userSelectedGranted: Boolean = false,
    /** `Environment.isExternalStorageManager()` on API 30+; ignored below. */
    val allFilesAccess: Boolean = false,
    /** `shouldShowRequestPermissionRationale` for the read permission. */
    val shouldShowRationale: Boolean = false,
    /** True once the standalone app has shown the system dialog at least once (persisted record). */
    val requestedBefore: Boolean = false,
    /** How many request results came back without any photo permission granted (persisted record). */
    val deniedCount: Int = 0,
)

internal data class MediaPermissionResolution(
    val state: MediaAccessState,
    val nextAction: MediaPermissionAction,
    /** Permissions the next [MediaPermissionAction.REQUEST] should ask for (empty when no request applies). */
    val permissionsToRequest: List<String>,
    /** True only for the single automatic request on the first standalone launch (D1 / R2). */
    val autoRequestOnLaunch: Boolean,
) {
    /** The media grid can show MediaStore content (fully or partially). */
    val canReadMediaStore: Boolean get() = state != MediaAccessState.DENIED && state != MediaAccessState.NEVER_ASK
}

/**
 * Android-free photo permission state machine (ROADMAP T5, D1, D5). Host entries and `ACTION_VIEW` never call it:
 * only the standalone gallery asks, and it asks automatically exactly once.
 */
internal object MediaPermissionPolicy {

    const val READ_EXTERNAL_STORAGE = "android.permission.READ_EXTERNAL_STORAGE"
    const val WRITE_EXTERNAL_STORAGE = "android.permission.WRITE_EXTERNAL_STORAGE"
    const val READ_MEDIA_IMAGES = "android.permission.READ_MEDIA_IMAGES"
    const val READ_MEDIA_VISUAL_USER_SELECTED = "android.permission.READ_MEDIA_VISUAL_USER_SELECTED"

    const val FIRST_SCOPED_STORAGE_SDK = 30
    const val FIRST_MEDIA_PERMISSION_SDK = 33
    const val FIRST_PARTIAL_ACCESS_SDK = 34
    const val LAST_LEGACY_WRITE_SDK = 29
    const val NEVER_ASK_DENIALS = 2

    /** The runtime permissions the standalone gallery requests on the given platform level. */
    fun permissionsToRequest(sdkInt: Int): List<String> = when {
        sdkInt >= FIRST_PARTIAL_ACCESS_SDK -> listOf(READ_MEDIA_IMAGES, READ_MEDIA_VISUAL_USER_SELECTED)
        sdkInt >= FIRST_MEDIA_PERMISSION_SDK -> listOf(READ_MEDIA_IMAGES)
        sdkInt >= FIRST_SCOPED_STORAGE_SDK -> listOf(READ_EXTERNAL_STORAGE)
        else -> listOf(READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE)
    }

    /** The read permission whose grant decides [MediaAccessState.FULL]. */
    fun readPermission(sdkInt: Int): String =
        if (sdkInt >= FIRST_MEDIA_PERMISSION_SDK) READ_MEDIA_IMAGES else READ_EXTERNAL_STORAGE

    fun state(inputs: MediaPermissionInputs): MediaAccessState = when {
        inputs.sdkInt >= FIRST_SCOPED_STORAGE_SDK && inputs.allFilesAccess -> MediaAccessState.ALL_FILES
        inputs.readImagesGranted -> MediaAccessState.FULL
        inputs.sdkInt >= FIRST_PARTIAL_ACCESS_SDK && inputs.userSelectedGranted -> MediaAccessState.PARTIAL
        // After one explicit denial every platform still offers the rationale; a dismissed dialog leaves it false
        // without closing the door, so only a second denial without rationale means the system stopped asking.
        inputs.deniedCount >= NEVER_ASK_DENIALS && !inputs.shouldShowRationale -> MediaAccessState.NEVER_ASK
        else -> MediaAccessState.DENIED
    }

    fun resolve(inputs: MediaPermissionInputs): MediaPermissionResolution {
        val state = state(inputs)
        val action = when (state) {
            MediaAccessState.FULL, MediaAccessState.ALL_FILES -> MediaPermissionAction.NONE
            MediaAccessState.PARTIAL -> MediaPermissionAction.MANAGE_SELECTED
            MediaAccessState.DENIED -> MediaPermissionAction.REQUEST
            MediaAccessState.NEVER_ASK -> MediaPermissionAction.OPEN_APP_SETTINGS
        }
        val permissions = when (action) {
            MediaPermissionAction.REQUEST, MediaPermissionAction.MANAGE_SELECTED -> permissionsToRequest(inputs.sdkInt)
            else -> emptyList()
        }
        return MediaPermissionResolution(
            state = state,
            nextAction = action,
            permissionsToRequest = permissions,
            autoRequestOnLaunch = state == MediaAccessState.DENIED && !inputs.requestedBefore,
        )
    }
}
