package io.github.supermonster003.autojs6.plugin.three.maple.image.scriptapi

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import android.os.ParcelFileDescriptor
import android.system.Os
import android.system.OsConstants
import io.github.supermonster003.autojs6.plugin.three.maple.image.imageViewerPluginInfo
import org.autojs.plugin.common.api.PluginCapabilityKeys
import org.autojs.plugin.common.api.PluginInfo
import org.autojs.plugin.gallery.api.GalleryCapabilityKeys
import org.autojs.plugin.gallery.api.GalleryContract
import org.autojs.plugin.gallery.api.GalleryErrorCodes
import org.autojs.plugin.gallery.api.GalleryIds
import org.autojs.plugin.gallery.api.IGalleryCallback
import org.autojs.plugin.gallery.api.IGalleryPlugin
import org.autojs.plugin.gallery.api.IGallerySession

/**
 * `org.autojs.plugin.GALLERY` service (ROADMAP 10.2, `docs/dev/gallery-plugin-protocol-v1.md` in the
 * host): the script object `gallery` of AutoJs6 reaches the picker, the viewer, the editor and the
 * converter through it. It shares the plugin identity of the Explorer Action service with engine
 * `gallery`. Interactive calls only open a session; the host starts [GallerySessionActivity] with the
 * session's token. `convert` runs here, on the calling Binder thread.
 */
class GalleryPluginService : Service() {

    private val guard by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { ScriptCallerGuard(this) }
    private val converter by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { ScriptConverter(this) }

    private val binder = object : IGalleryPlugin.Stub() {
        override fun getInfo(): PluginInfo {
            guard.check()
            return galleryPluginInfo()
        }

        override fun getCapabilities(): Bundle {
            guard.check()
            return galleryCapabilities()
        }

        override fun openPicker(options: Bundle?, callback: IGalleryCallback?): IGallerySession {
            guard.check()
            return open(options, callback, emptyList()) { _ ->
                SessionPayload.Picker(ScriptRequests.picker(options))
            }
        }

        override fun openViewer(sources: Array<out ParcelFileDescriptor?>?, options: Bundle?, callback: IGalleryCallback?): IGallerySession {
            guard.check()
            val received = sources?.toList().orEmpty()
            return open(options, callback, received.filterNotNull()) { _ ->
                if (received.any { it == null }) throw ContractViolation(GalleryErrorCodes.INVALID_ARGUMENT, "a source descriptor is missing")
                val (names, start) = ScriptRequests.viewer(received.size, options)
                received.forEach { requireRegularFile(it!!) }
                SessionPayload.Viewer(received.map { it!! }, names, start)
            }
        }

        override fun openEditor(source: ParcelFileDescriptor?, target: ParcelFileDescriptor?, options: Bundle?, callback: IGalleryCallback?): IGallerySession {
            guard.check()
            return open(options, callback, listOfNotNull(source, target)) { _ ->
                if (source == null || target == null) throw ContractViolation(GalleryErrorCodes.INVALID_ARGUMENT, "the source and the target descriptors are required")
                requireRegularFile(source)
                requireRegularFile(target)
                SessionPayload.Editor(source, target, ScriptRequests.displayName(options?.getString(GalleryContract.KEY_DISPLAY_NAME)), ScriptRequests.encode(options))
            }
        }

        override fun convert(source: ParcelFileDescriptor?, target: ParcelFileDescriptor?, options: Bundle?): Bundle {
            guard.check()
            var version = GalleryContract.CONTRACT_VERSION
            return try {
                version = ScriptAnswers.versionOf(options)
                ScriptAnswers.checkOptionsSize(options)
                if (source == null || target == null) throw ContractViolation(GalleryErrorCodes.INVALID_ARGUMENT, "the source and the target descriptors are required")
                requireRegularFile(source)
                requireRegularFile(target)
                val request = ScriptRequests.convert(options)
                converter.answer(version, converter.convert(source, target, request))
            } catch (e: ContractViolation) {
                ScriptAnswers.error(version, e.code, e.detail)
            } catch (e: OutOfMemoryError) {
                ScriptAnswers.error(version, GalleryErrorCodes.LIMIT_EXCEEDED, "the image does not fit in memory")
            } catch (e: SecurityException) {
                throw e
            } catch (e: Throwable) {
                ScriptAnswers.error(version, GalleryErrorCodes.INTERNAL, e.toString())
            } finally {
                runCatching { source?.close() }
                runCatching { target?.close() }
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder = binder

    /** The last client left: no session may wait for a host that is gone. */
    override fun onUnbind(intent: Intent?): Boolean {
        ScriptSessionRegistry.currentSession?.takeIf { !it.claimed }?.close(GalleryContract.REASON_HOST, finish = false)
        return false
    }

    /**
     * Opens one interactive session: checks the version, the size of the options and the callback,
     * builds the payload (which owns [descriptors] from then on) and registers the session. Any
     * refusal closes [descriptors] and leaves as the contract's exception form.
     */
    private fun open(
        options: Bundle?,
        callback: IGalleryCallback?,
        descriptors: List<ParcelFileDescriptor>,
        payload: (Int) -> SessionPayload,
    ): IGallerySession {
        try {
            val version = ScriptAnswers.versionOf(options)
            ScriptAnswers.checkOptionsSize(options)
            if (callback == null) throw ContractViolation(GalleryErrorCodes.INVALID_ARGUMENT, "the session callback is missing")
            val built = payload(version)
            val kind = when (built) {
                is SessionPayload.Picker -> GalleryContract.KIND_PICKER
                is SessionPayload.Viewer -> GalleryContract.KIND_VIEWER
                is SessionPayload.Editor -> GalleryContract.KIND_EDITOR
            }
            return ScriptSessionBinder(ScriptSessionRegistry.open(kind, version, built, callback), guard)
        } catch (e: ContractViolation) {
            descriptors.forEach { runCatching { it.close() } }
            throw ScriptAnswers.failure(e)
        } catch (e: SecurityException) {
            descriptors.forEach { runCatching { it.close() } }
            throw e
        } catch (e: Throwable) {
            descriptors.forEach { runCatching { it.close() } }
            throw IllegalStateException(GalleryErrorCodes.encode(GalleryErrorCodes.INTERNAL, e.toString()))
        }
    }

    private fun requireRegularFile(descriptor: ParcelFileDescriptor) {
        val regular = runCatching { OsConstants.S_ISREG(Os.fstat(descriptor.fileDescriptor).st_mode) }.getOrDefault(false)
        if (!regular) throw ContractViolation(GalleryErrorCodes.FILE_UNREADABLE, "a descriptor is not a regular file")
    }
}

/** The gallery engine's identity: the plugin's shared identity with engine `gallery` and the gallery capabilities. */
internal fun Context.galleryPluginInfo(): PluginInfo = imageViewerPluginInfo().apply {
    engine = GalleryIds.ENGINE
    variant = GalleryIds.VARIANT_DEFAULT
    capabilities = galleryCapabilities()
}

internal fun galleryCapabilities(): Bundle = Bundle().apply {
    putLong(PluginCapabilityKeys.REQUIRES_HOST_VERSION, GalleryIds.REQUIRED_HOST_VERSION_CODE)
    putInt(GalleryCapabilityKeys.CONTRACT_VERSION, GalleryContract.CONTRACT_VERSION)
    putInt(GalleryCapabilityKeys.MAX_CONTRACT_VERSION, GalleryContract.MAX_CONTRACT_VERSION)
    putStringArray(GalleryCapabilityKeys.FEATURES, GalleryContract.FEATURES.toTypedArray())
}
