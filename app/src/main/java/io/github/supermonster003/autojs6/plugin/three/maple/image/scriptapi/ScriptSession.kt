package io.github.supermonster003.autojs6.plugin.three.maple.image.scriptapi

import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.ParcelFileDescriptor
import android.os.RemoteException
import org.autojs.plugin.gallery.api.GalleryContract
import org.autojs.plugin.gallery.api.GalleryErrorCodes
import org.autojs.plugin.gallery.api.IGalleryCallback
import java.io.Closeable
import java.util.concurrent.atomic.AtomicLong

/** What an interactive session shows; the descriptors belong to the session until the UI adopts them. */
internal sealed class SessionPayload : Closeable {

    data class Picker(val request: PickerRequest) : SessionPayload() {
        override fun close() = Unit
    }

    class Viewer(val sources: List<ParcelFileDescriptor>, val names: List<String>, val startIndex: Int) : SessionPayload() {
        override fun close() {
            sources.forEach { runCatching { it.close() } }
        }
    }

    class Editor(val source: ParcelFileDescriptor, val target: ParcelFileDescriptor, val displayName: String?, val encode: EncodeRequest) : SessionPayload() {
        override fun close() {
            runCatching { source.close() }
            runCatching { target.close() }
        }
    }
}

/** The Activity showing a claimed session; every call arrives on the main thread. */
internal interface SessionUi {
    fun finishSession()
}

/**
 * One interactive gallery session (ROADMAP 10.2): minted by `openPicker`, `openViewer` or
 * `openEditor` with a random token, owning its descriptors until the session Activity claims the
 * token, then the bridge between that Activity and the host's callback.
 *
 * Events go out as `onEvent(generation, seq, bundle)` with one generation per session and a
 * strictly increasing `seq`: `open` once the UI is visible, `page` per viewer page (the start page
 * too), `result` for the picker's choice or the editor's saved output, `error`, and exactly one
 * `close` with its reason. A picker or editor closes with `done` right after its result. A dead
 * host callback ends the session silently; the picker and the editor finish, the viewer stays.
 */
internal class ScriptSession(
    val token: String,
    val generation: Long,
    val kind: String,
    val contractVersion: Int,
    private val payload: SessionPayload,
    private val callback: IGalleryCallback,
) {

    private val lock = Any()
    private val targetLock = Any()
    private val handler = Handler(Looper.getMainLooper())
    private val seq = AtomicLong(0L)

    @Volatile
    var claimed: Boolean = false
        private set

    @Volatile
    var closed: Boolean = false
        private set

    @Volatile
    var visible: Boolean = false
        private set

    @Volatile
    var index: Int? = null
        private set

    private var opened = false
    private var adopted = false

    @Volatile
    private var ui: SessionUi? = null

    @Volatile
    private var finishRequested = false

    /** Set by the registry so it can forget a session that closed on its own. */
    @Volatile
    var onClosed: ((ScriptSession) -> Unit)? = null

    val lastSeq: Long get() = seq.get()

    private val deathRecipient = IBinder.DeathRecipient {
        close(GalleryContract.REASON_ERROR, finish = kind != GalleryContract.KIND_VIEWER, notify = false)
    }

    init {
        try {
            callback.asBinder().linkToDeath(deathRecipient, 0)
        } catch (e: RemoteException) {
            handler.post { close(GalleryContract.REASON_ERROR, finish = false, notify = false) }
        }
    }

    // ---- Registry ----

    fun markClaimed() {
        claimed = true
    }

    // ---- UI side (main thread) ----

    /** The request the UI shows. A viewer's sources stay owned by the session until [adoptViewer]. */
    fun payload(): SessionPayload = payload

    /** The viewer takes its sources over: they outlive the session (a script that exits leaves the viewer open). */
    fun adoptViewer(): SessionPayload.Viewer? = synchronized(lock) {
        val viewer = payload as? SessionPayload.Viewer ?: return null
        if (closed && !adopted) return null
        adopted = true
        viewer
    }

    fun attachUi(ui: SessionUi) {
        this.ui = ui
        if (finishRequested) ui.finishSession()
    }

    fun detachUi(ui: SessionUi) {
        if (this.ui === ui) this.ui = null
    }

    /** The UI became visible: the first time sends `open`. */
    fun onVisible() {
        visible = true
        val first = synchronized(lock) {
            if (opened || closed) false else {
                opened = true
                true
            }
        }
        if (first) emit(GalleryContract.EVENT_OPEN) { putString(GalleryContract.KEY_KIND, kind) }
    }

    fun onHidden() {
        visible = false
    }

    fun page(index: Int) {
        if (this.index == index) return
        this.index = index
        emit(GalleryContract.EVENT_PAGE) { putInt(GalleryContract.KEY_INDEX, index) }
    }

    /** The picker's choice (a JSON array of picked images), then `close(done)`. */
    fun pickResult(itemsJson: String) {
        emit(GalleryContract.EVENT_RESULT) { putString(GalleryContract.KEY_ITEMS, itemsJson) }
        close(GalleryContract.REASON_DONE, finish = false)
    }

    /**
     * Writes the editor's output into the host's target while the session is open; the host never
     * sees bytes after it closed the session. False when the session closed before the write began.
     */
    fun writeTarget(write: (ParcelFileDescriptor) -> Unit): Boolean {
        val editor = payload as? SessionPayload.Editor ?: return false
        synchronized(targetLock) {
            if (closed) return false
            write(editor.target)
            return true
        }
    }

    /** The editor's saved output as written by [writeTarget], then `close(done)`. */
    fun editResult(mimeType: String, width: Int, height: Int, size: Long) {
        emit(GalleryContract.EVENT_RESULT) {
            putString(GalleryContract.KEY_MIME_TYPE, mimeType)
            putInt(GalleryContract.KEY_WIDTH, width)
            putInt(GalleryContract.KEY_HEIGHT, height)
            putLong(GalleryContract.KEY_SIZE, size)
        }
        close(GalleryContract.REASON_DONE, finish = false)
    }

    fun error(code: String, detail: String) {
        emit(GalleryContract.EVENT_ERROR) {
            putString(GalleryContract.KEY_ERROR_CODE, if (GalleryErrorCodes.isKnown(code)) code else GalleryErrorCodes.INTERNAL)
            putString(GalleryContract.KEY_ERROR_MESSAGE, ScriptAnswers.errorDetail(detail))
        }
    }

    /** A failure that ends the session: `error`, then `close(error)`; the UI finishes itself. */
    fun fail(code: String, detail: String) {
        error(code, detail)
        close(GalleryContract.REASON_ERROR, finish = false)
    }

    /** The user left the UI without a result. */
    fun userLeft() {
        close(GalleryContract.REASON_USER, finish = false)
    }

    // ---- Host side (binder threads) ----

    fun stateBundle(): Bundle = ScriptAnswers.ok(contractVersion) {
        putString(GalleryContract.KEY_SESSION_TOKEN, token)
        putString(GalleryContract.KEY_KIND, kind)
        putBoolean(GalleryContract.KEY_VISIBLE, visible)
        index?.let { putInt(GalleryContract.KEY_INDEX, it) }
    }

    /**
     * Ends the session exactly once: the `close` event (unless the host is gone), the callback link,
     * the descriptors (a viewer's only when nobody adopted them) and with [finish] the session UI.
     */
    fun close(reason: String, finish: Boolean, notify: Boolean = true) {
        val release: Boolean
        synchronized(lock) {
            if (closed) return
            if (notify) send(GalleryContract.EVENT_CLOSE) { putString(GalleryContract.KEY_REASON, reason) }
            closed = true
            release = !adopted
        }
        runCatching { callback.asBinder().unlinkToDeath(deathRecipient, 0) }
        synchronized(targetLock) {
            if (release) payload.close()
        }
        if (finish) {
            handler.post {
                val ui = this.ui
                if (ui != null) ui.finishSession() else finishRequested = true
            }
        }
        onClosed?.invoke(this)
    }

    // ---- Internals ----

    private fun emit(type: String, build: Bundle.() -> Unit) {
        val dead: Boolean
        synchronized(lock) {
            if (closed) return
            dead = !send(type, build)
        }
        if (dead) close(GalleryContract.REASON_ERROR, finish = kind != GalleryContract.KIND_VIEWER, notify = false)
    }

    /** Under [lock]: numbers and sends one event; false when the host callback is gone. */
    private fun send(type: String, build: Bundle.() -> Unit): Boolean {
        val bundle = ScriptAnswers.ok(contractVersion) {
            putString(GalleryContract.KEY_EVENT, type)
            build()
        }
        if (ScriptAnswers.parcelSize(bundle) > GalleryContract.MAX_EVENT_BYTES) {
            // Only a picker result can grow; the picker caps its choice well below this.
            bundle.remove(GalleryContract.KEY_ITEMS)
            bundle.putString(GalleryContract.KEY_EVENT, GalleryContract.EVENT_ERROR)
            bundle.putString(GalleryContract.KEY_ERROR_CODE, GalleryErrorCodes.LIMIT_EXCEEDED)
            bundle.putString(GalleryContract.KEY_ERROR_MESSAGE, "the event exceeds ${GalleryContract.MAX_EVENT_BYTES} bytes")
        }
        return try {
            callback.onEvent(generation, seq.incrementAndGet(), bundle)
            true
        } catch (e: RemoteException) {
            false
        }
    }
}

/**
 * The process-wide table of interactive gallery sessions (ROADMAP 10.2): one session at a time, a
 * new interactive call replaces the previous one (its UI finishes, its host hears `replaced`), and
 * a session nobody claims within [claimTimeoutMs] closes with `timeout`.
 */
internal object ScriptSessionRegistry {

    private val handler = Handler(Looper.getMainLooper())
    private val generations = AtomicLong(System.currentTimeMillis())
    private val timeouts = HashMap<ScriptSession, Runnable>()
    private var current: ScriptSession? = null

    /** Instrumentation tests shorten the claim window; production keeps the contract's value. */
    @Volatile
    internal var claimTimeoutMs: Long = GalleryContract.SESSION_CLAIM_TIMEOUT_MS

    fun open(kind: String, contractVersion: Int, payload: SessionPayload, callback: IGalleryCallback): ScriptSession {
        val session = ScriptSession(ScriptSessionTokens.newToken(), generations.incrementAndGet(), kind, contractVersion, payload, callback)
        session.onClosed = ::forget
        val previous: ScriptSession?
        synchronized(this) {
            previous = current
            current = session
            val timeout = Runnable { session.close(GalleryContract.REASON_TIMEOUT, finish = false) }
            timeouts[session] = timeout
            handler.postDelayed(timeout, claimTimeoutMs)
        }
        previous?.close(GalleryContract.REASON_REPLACED, finish = true)
        return session
    }

    /** The session behind [token] when it is still waiting for its UI; null otherwise. */
    fun claim(token: String): ScriptSession? = synchronized(this) {
        val session = current ?: return null
        if (session.closed || session.claimed || !ScriptSessionTokens.sameToken(token, session.token)) return null
        timeouts.remove(session)?.let(handler::removeCallbacks)
        session.markClaimed()
        session
    }

    /** The claimed, open session of [generation], for the UI the session Activity started. */
    fun claimed(generation: Long): ScriptSession? = synchronized(this) {
        current?.takeIf { it.generation == generation && it.claimed && !it.closed }
    }

    val currentSession: ScriptSession? get() = synchronized(this) { current }

    fun closeAll(reason: String) {
        currentSession?.close(reason, finish = false)
    }

    private fun forget(session: ScriptSession) {
        synchronized(this) {
            timeouts.remove(session)?.let(handler::removeCallbacks)
            if (current === session) current = null
        }
    }
}
