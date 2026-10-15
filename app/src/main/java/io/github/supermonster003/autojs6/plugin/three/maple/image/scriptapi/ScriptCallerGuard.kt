package io.github.supermonster003.autojs6.plugin.three.maple.image.scriptapi

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Binder
import android.os.Process
import org.autojs.plugin.gallery.api.GalleryActions
import java.security.MessageDigest
import java.security.SecureRandom

/**
 * Who may call the gallery service (ROADMAP 10.2, T17). The manifest permission already limits
 * callers to apps holding `org.autojs.permission.PLUGIN`; this guard additionally requires the
 * calling uid to own the AutoJs6 host package and to be signed like this plugin. Calls from the
 * plugin's own uid (the instrumentation runner) pass only in debuggable builds.
 */
internal class ScriptCallerGuard(context: Context, private val allowOwnUid: Boolean = isDebuggable(context)) {

    private val packageManager: PackageManager = context.applicationContext.packageManager

    /** Must run on the Binder thread of the call, before any dispatch, so the calling uid is the caller's. */
    fun check() {
        val uid = Binder.getCallingUid()
        if (uid == Process.myUid()) {
            if (allowOwnUid) return
            throw SecurityException("The gallery service does not serve its own uid")
        }
        val packages = packageManager.getPackagesForUid(uid).orEmpty()
        if (packages.none { it in HOST_PACKAGES }) {
            throw SecurityException("The gallery service only serves the AutoJs6 host")
        }
        if (packageManager.checkSignatures(uid, Process.myUid()) != PackageManager.SIGNATURE_MATCH) {
            throw SecurityException("The caller is not signed like the gallery plugin")
        }
    }

    companion object {
        /** Packages whose uid may use the service; the Plugin Center authorization lives on the host side. */
        val HOST_PACKAGES: Set<String> = setOf("org.autojs.autojs6")

        fun isDebuggable(context: Context): Boolean = (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
    }
}

/**
 * Token rules of the two-step session launch (ROADMAP 10.2): an interactive call mints a random
 * 128-bit token, the host starts the session Activity with it as the only extra, and the Activity
 * claims the session by presenting it back. A token is compared in constant time and never logged.
 */
internal object ScriptSessionTokens {

    const val TOKEN_BYTES = 16

    private val TOKEN_PATTERN = Regex("[0-9a-f]{32}")
    private val random = SecureRandom()

    fun newToken(): String = ByteArray(TOKEN_BYTES).also(random::nextBytes).joinToString("") { "%02x".format(it) }

    fun isWellFormed(token: String?): Boolean = token != null && TOKEN_PATTERN.matches(token)

    /** The token of a session-launch Intent: the session action, an explicit component and a well-formed token; null otherwise. */
    fun tokenOf(action: String?, explicit: Boolean, token: String?): String? =
        token?.takeIf { action == GalleryActions.SESSION_ACTIVITY_ACTION && explicit && isWellFormed(it) }

    fun sameToken(presented: String, expected: String): Boolean =
        MessageDigest.isEqual(presented.toByteArray(Charsets.UTF_8), expected.toByteArray(Charsets.UTF_8))
}
