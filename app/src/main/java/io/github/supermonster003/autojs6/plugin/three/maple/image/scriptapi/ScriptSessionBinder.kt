package io.github.supermonster003.autojs6.plugin.three.maple.image.scriptapi

import android.os.Bundle
import org.autojs.plugin.gallery.api.GalleryContract
import org.autojs.plugin.gallery.api.GalleryErrorCodes
import org.autojs.plugin.gallery.api.IGallerySession

/**
 * `IGallerySession` of one [ScriptSession] (ROADMAP 10.2): every call passes the caller guard;
 * `getState` answers a closed session with `SESSION_CLOSED` in the Bundle, and `close` ends the
 * session with reason `host`, finishing its UI unless `finish` is false.
 */
internal class ScriptSessionBinder(
    private val session: ScriptSession,
    private val guard: ScriptCallerGuard,
) : IGallerySession.Stub() {

    override fun getState(): Bundle {
        guard.check()
        if (session.closed) return ScriptAnswers.error(session.contractVersion, GalleryErrorCodes.SESSION_CLOSED, "the gallery session is closed")
        return session.stateBundle()
    }

    override fun close(options: Bundle?) {
        guard.check()
        session.close(GalleryContract.REASON_HOST, finish = options?.getBoolean(GalleryContract.KEY_FINISH, true) ?: true)
    }
}
