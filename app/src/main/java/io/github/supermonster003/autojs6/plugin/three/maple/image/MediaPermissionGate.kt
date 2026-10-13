package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/** Persisted "the standalone app has asked once" flag behind [MediaPermissionInputs.requestedBefore]. */
internal interface MediaPermissionRecord {
    val requestedBefore: Boolean
    val deniedCount: Int
    fun markRequested()
    fun markDenied()
}

/**
 * SharedPreferences backed [MediaPermissionRecord]. ROADMAP T8 lists this record among the Room tables of M3; until
 * those tables exist a single flag in its own preference file is enough and migrates trivially.
 */
internal class MediaPermissionRecordStore(context: Context) : MediaPermissionRecord {
    private val preferences = context.applicationContext.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)

    override val requestedBefore: Boolean get() = preferences.getBoolean(KEY_REQUESTED, false)

    override val deniedCount: Int get() = preferences.getInt(KEY_DENIED, 0)

    override fun markRequested() {
        preferences.edit().putBoolean(KEY_REQUESTED, true).apply()
    }

    override fun markDenied() {
        preferences.edit().putInt(KEY_DENIED, deniedCount + 1).apply()
    }

    fun reset() {
        preferences.edit().clear().apply()
    }

    private companion object {
        const val FILE_NAME = "media_permission"
        const val KEY_REQUESTED = "requested"
        const val KEY_DENIED = "denied"
    }
}

/**
 * Collects the platform facts for [MediaPermissionPolicy] and builds the intents its actions need. Only the
 * standalone gallery uses it; host entries and `ACTION_VIEW` never ask for photo access (ROADMAP D1, R2).
 */
internal class MediaPermissionGate(
    private val activity: Activity,
    private val record: MediaPermissionRecord = MediaPermissionRecordStore(activity),
    private val sdkInt: Int = Build.VERSION.SDK_INT,
) {
    fun inputs(): MediaPermissionInputs {
        val readPermission = MediaPermissionPolicy.readPermission(sdkInt)
        return MediaPermissionInputs(
            sdkInt = sdkInt,
            readImagesGranted = granted(readPermission),
            userSelectedGranted = sdkInt >= MediaPermissionPolicy.FIRST_PARTIAL_ACCESS_SDK &&
                granted(MediaPermissionPolicy.READ_MEDIA_VISUAL_USER_SELECTED),
            allFilesAccess = sdkInt >= MediaPermissionPolicy.FIRST_SCOPED_STORAGE_SDK && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
                Environment.isExternalStorageManager(),
            shouldShowRationale = ActivityCompat.shouldShowRequestPermissionRationale(activity, readPermission),
            requestedBefore = record.requestedBefore,
            deniedCount = record.deniedCount,
        )
    }

    fun resolve(): MediaPermissionResolution = MediaPermissionPolicy.resolve(inputs())

    /** Call right before launching the system dialog so the automatic request happens only once. */
    fun markRequested() = record.markRequested()

    /** Call with the dialog result; a result without any granted photo permission counts as one denial. */
    fun recordResult(grants: Map<String, Boolean>) {
        if (grants.isNotEmpty() && grants.values.none { it }) record.markDenied()
    }

    fun appSettingsIntent(): Intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", activity.packageName, null))

    /** Android 11+ system page for "all files access"; null below. */
    fun allFilesAccessIntent(): Intent? =
        if (sdkInt >= MediaPermissionPolicy.FIRST_SCOPED_STORAGE_SDK && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, Uri.fromParts("package", activity.packageName, null))
        } else {
            null
        }

    private fun granted(permission: String): Boolean =
        ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED
}
