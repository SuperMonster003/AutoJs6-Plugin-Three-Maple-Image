package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.os.Bundle
import android.os.IBinder
import android.os.ParcelFileDescriptor
import org.autojs.plugin.explorer.api.IExplorerActionHostSession

/**
 * A Binder-backed host session for intent round trips (a plain `Default` has no binder). Every operation the
 * tests do not override is unsupported; `close` and the no-result calls are no-ops.
 */
internal open class UnsupportedHostSessionStub : IExplorerActionHostSession.Stub() {

    override fun listChildren(targetId: String?, relativePath: String?, offset: Int, limit: Int): Bundle =
        throw UnsupportedOperationException()

    override fun openFile(targetId: String?, relativePath: String?): ParcelFileDescriptor =
        throw UnsupportedOperationException()

    override fun prepareOutput(displayName: String?, mimeType: String?, conflictPolicy: Int): Bundle =
        throw UnsupportedOperationException()

    override fun openOutput(transactionId: String?): ParcelFileDescriptor = throw UnsupportedOperationException()

    override fun commitOutput(transactionId: String?): Bundle = throw UnsupportedOperationException()

    override fun abortOutput(transactionId: String?) = Unit

    override fun close() = Unit

    override fun openPendingOutput(transactionId: String?): ParcelFileDescriptor =
        throw UnsupportedOperationException()

    override fun prepareTargetReplacement(targetId: String?): Bundle = throw UnsupportedOperationException()

    override fun prepareOutputTree(displayName: String?, conflictPolicy: Int): Bundle =
        throw UnsupportedOperationException()

    override fun createOutputDirectory(transactionId: String?, relativePath: String?) = Unit

    override fun openOutputFile(transactionId: String?, relativePath: String?): ParcelFileDescriptor =
        throw UnsupportedOperationException()

    override fun queryOutput(transactionId: String?): Bundle = throw UnsupportedOperationException()

    override fun listOutputs(): Bundle = throw UnsupportedOperationException()

    override fun attachClient(clientToken: IBinder?) = Unit

    override fun getPlaybackProgress(targetId: String?, relativePath: String?): Bundle =
        throw UnsupportedOperationException()

    override fun reportPlaybackProgress(
        targetId: String?,
        relativePath: String?,
        positionMillis: Long,
        durationMillis: Long,
        reportState: Int,
    ) = Unit
}
