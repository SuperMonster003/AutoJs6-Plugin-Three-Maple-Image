@file:Suppress("DEPRECATION")

package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.content.ClipData
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.os.ParcelFileDescriptor
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.bumptech.glide.Glide
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import java.util.concurrent.TimeUnit
import org.autojs.plugin.explorer.api.ExplorerActionHostSessionKeys
import org.autojs.plugin.explorer.api.ExplorerActionIntentExtras
import org.autojs.plugin.explorer.api.ExplorerActionIntentValues
import org.autojs.plugin.explorer.api.ExplorerActionPluginActions
import org.autojs.plugin.explorer.api.ExplorerActionProtocol
import org.autojs.plugin.explorer.api.ExplorerActionTargetKeys
import org.autojs.plugin.explorer.api.ExplorerActionValues
import org.autojs.plugin.explorer.api.IExplorerActionHostSession
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ExplorerActionV12InstrumentationTest {

    @Test
    fun acceptsCompleteSingleTargetV12EnvelopeAndSession() {
        val session = FakeHostSession()

        val request = ImageRequestPolicy.resolveExplorer(validExplorerIntent(session))

        assertNotNull(request)
        requireNotNull(request)
        assertEquals(ExplorerImageRequestMode.SINGLE_WITH_SIBLINGS, request.mode)
        assertEquals("photo10.png", request.targets.single().image.displayName)
        assertEquals("target-id", request.targets.single().id)
        assertSame(session.asBinder(), request.hostSession?.asBinder())
    }

    @Test
    fun rejectsMissingSessionOldProtocolWeakFlagsAndOldTwoItemClip() {
        val noSession = validExplorerIntent(FakeHostSession()).apply {
            removeExtra(ExplorerActionIntentExtras.HOST_SESSION)
        }
        val oldProtocol = validExplorerIntent(FakeHostSession()).apply {
            putExtra(ExplorerActionIntentExtras.PROTOCOL_VERSION, ThreeMapleImagePlugin.PROTOCOL_VERSION - 1)
        }
        val noPrefix = validExplorerIntent(FakeHostSession()).apply {
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        val writable = validExplorerIntent(FakeHostSession()).apply {
            addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        }
        val oldClip = validExplorerIntent(FakeHostSession()).apply {
            clipData?.addItem(ClipData.Item(PARENT_URI))
        }

        listOf(noSession, oldProtocol, noPrefix, writable, oldClip).forEach { intent ->
            assertNull(ImageRequestPolicy.resolveExplorer(intent))
        }
    }

    @Test
    fun rejectsNestedTargetAndHostBelowTheReadSiblingsBaseline() {
        val nestedUri = Uri.withAppendedPath(Uri.withAppendedPath(PARENT_URI, "nested"), "photo10.png")
        val nested = validExplorerIntent(FakeHostSession(), nestedUri)
        val oldHost = validExplorerIntent(FakeHostSession()).apply {
            putExtra(ExplorerActionIntentExtras.HOST_VERSION_CODE, ThreeMapleImagePlugin.REQUIRED_HOST_VERSION - 1L)
        }

        assertNull(ImageRequestPolicy.resolveExplorer(nested))
        assertNull(ImageRequestPolicy.resolveExplorer(oldHost))
    }

    @Test
    fun acceptsModernExtensionMimeFamiliesAndRejectsMismatches() {
        val session = FakeHostSession()
        val heicUri = Uri.withAppendedPath(PARENT_URI, "camera.heic")
        val avifUri = Uri.withAppendedPath(PARENT_URI, "frame.avif")
        val textUri = Uri.withAppendedPath(PARENT_URI, "notes.txt")

        assertNotNull(
            ImageRequestPolicy.resolveExplorer(
                validExplorerIntent(session, heicUri, "camera.heic", "image/heif"),
            ),
        )
        assertNotNull(
            ImageRequestPolicy.resolveExplorer(
                validExplorerIntent(session, avifUri, "frame.avif", "image/avif"),
            ),
        )
        val wildcardAvif = ImageRequestPolicy.resolveExplorer(
            validExplorerIntent(session, avifUri, "frame.avif", "*/*"),
        )
        assertEquals("image/avif", wildcardAvif?.targets?.single()?.image?.mimeType)
        assertNull(
            ImageRequestPolicy.resolveExplorer(
                validExplorerIntent(session, avifUri, "frame.avif", "image/heic"),
            ),
        )
        assertNull(
            ImageRequestPolicy.resolveExplorer(
                validExplorerIntent(session, textUri, "notes.txt", "image/png"),
            ),
        )
    }

    @Test
    fun acceptsOrderedExplicitSelectionAndSingleSelectionWithoutSession() {
        val session = FakeHostSession()

        val request = ImageRequestPolicy.resolveExplorer(validSelectionIntent(session))

        assertNotNull(request)
        requireNotNull(request)
        assertEquals(ExplorerImageRequestMode.EXPLICIT_SELECTION, request.mode)
        assertEquals(
            listOf("photo10.png", "photo2.webp"),
            request.targets.map { target -> target.image.displayName },
        )
        assertEquals(listOf("target-10", "target-2"), request.targets.map(ExplorerImageTarget::id))
        assertSame(session.asBinder(), request.hostSession?.asBinder())

        val singleSelection = ImageRequestPolicy.resolveExplorer(
            validSelectionIntent(
                session = null,
                targets = listOf(
                    ExplorerTargetSpec("target-10", TARGET_URI, "photo10.png", "image/png"),
                ),
            ),
        )
        assertNotNull(singleSelection)
        assertNull(singleSelection?.hostSession)
        assertEquals(listOf("photo10.png"), singleSelection?.targets?.map { it.image.displayName })
    }

    @Test
    fun rejectsSelectionEnvelopeConfusionDuplicatesAndUnboundedTargets() {
        val noSession = validSelectionIntent(session = null)
        val multipleTargetsOnSingleAction = validSelectionIntent().apply {
            putExtra(ExplorerActionIntentExtras.ACTION_ID, ThreeMapleImagePlugin.ACTION_ID)
        }
        val wrongAggregateType = validSelectionIntent().apply {
            setDataAndType(data, "image/png")
        }
        val reorderedClip = validSelectionIntent().apply {
            clipData = ClipData.newRawUri("photo2.webp", SECOND_TARGET_URI).apply {
                addItem(ClipData.Item(TARGET_URI))
            }
        }
        val duplicateId = validSelectionIntent().apply {
            val bundles = requireNotNull(
                getParcelableArrayListExtra<Bundle>(ExplorerActionIntentExtras.TARGETS),
            )
            bundles[1].putString(ExplorerActionTargetKeys.ID, "target-10")
            putParcelableArrayListExtra(ExplorerActionIntentExtras.TARGETS, bundles)
        }
        val wrongFirstSize = validSelectionIntent().apply {
            putExtra(ExplorerActionIntentExtras.SIZE, 9_999L)
        }
        val unexpectedSingleSession = validSelectionIntent(
            session = FakeHostSession(),
            targets = listOf(
                ExplorerTargetSpec("target-10", TARGET_URI, "photo10.png", "image/png"),
            ),
        )
        val nestedTarget = validSelectionIntent(
            targets = listOf(
                ExplorerTargetSpec("target-10", TARGET_URI, "photo10.png", "image/png"),
                ExplorerTargetSpec(
                    "target-nested",
                    Uri.parse("content://org.autojs.autojs6.fileprovider/root/images/nested/photo.png"),
                    "photo.png",
                    "image/png",
                ),
            ),
        )
        val unsupportedTarget = validSelectionIntent(
            targets = listOf(
                ExplorerTargetSpec("target-10", TARGET_URI, "photo10.png", "image/png"),
                ExplorerTargetSpec(
                    "target-text",
                    Uri.withAppendedPath(PARENT_URI, "notes.txt"),
                    "notes.txt",
                    "text/plain",
                ),
            ),
        )
        val tooManyTargets = validSelectionIntent(
            targets = (0..ExplorerActionProtocol.MAX_TARGETS_PER_REQUEST).map { index ->
                val name = "photo$index.png"
                ExplorerTargetSpec(
                    id = "target-$index",
                    uri = Uri.withAppendedPath(PARENT_URI, name),
                    displayName = name,
                    mimeType = "image/png",
                )
            },
        )

        listOf(
            noSession,
            multipleTargetsOnSingleAction,
            wrongAggregateType,
            reorderedClip,
            duplicateId,
            wrongFirstSize,
            unexpectedSingleSession,
            nestedTarget,
            unsupportedTarget,
            tooManyTargets,
        ).forEach { intent -> assertNull(ImageRequestPolicy.resolveExplorer(intent)) }
    }

    @Test
    fun roundTripsOrderedExplicitSelectionAndRejectsInternalClipReordering() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val launch = ImageViewerLaunchRequest.explicitSelection(
            listOf(
                ImageViewerRequest(TARGET_URI, "photo10.png", 1_024L, "image/png"),
                ImageViewerRequest(SECOND_TARGET_URI, "photo2.webp", 2_048L, "image/webp"),
            ),
        )
        val intent = ImageViewerContract.viewerIntent(context, launch)

        val resolved = ImageViewerContract.resolve(intent)

        assertNotNull(resolved)
        assertEquals(
            listOf("photo10.png", "photo2.webp"),
            resolved?.pages?.map { page -> page.image.displayName },
        )
        assertTrue(resolved?.pages?.all { page -> page.hostRelativePath == null } == true)
        assertNull(resolved?.hostSession)
        assertEquals(2, intent.clipData?.itemCount)

        intent.clipData = ClipData.newRawUri("photo2.webp", SECOND_TARGET_URI).apply {
            addItem(ClipData.Item(TARGET_URI))
        }
        assertNull(ImageViewerContract.resolve(intent))
    }

    @Test
    fun enumeratesPagedSessionThenFiltersAndNaturallySortsDirectImages() {
        val session = FakeHostSession(
            children = listOf(
                sibling("photo10.png", "image/png"),
                sibling("notes.txt", "text/plain"),
                sibling("photo2.webp", "image/webp"),
                sibling("hidden.jpg", "image/jpeg", readable = false),
                sibling("linked.gif", "image/gif", symbolicLink = true),
            ),
            pageSize = 2,
        )

        val gallery = ExplorerSiblingImageClient.discover(explorerRequest(session))

        assertEquals(listOf("photo2.webp", "photo10.png"), gallery.pages.map { it.displayName })
        assertEquals(1, gallery.startIndex)
        assertEquals("", gallery.pages[gallery.startIndex].relativePath)
        assertEquals(listOf(0, 2, 4), session.listOffsets)
        assertTrue(session.listCalls.all { call ->
            call.targetId == "target-id" && call.relativePath == "" &&
                call.limit == ExplorerActionProtocol.MAX_SESSION_PAGE_SIZE
        })
    }

    @Test
    fun rejectsMalformedSessionPathAndOutOfSessionInternalRoute() {
        val malformedSession = FakeHostSession(
            children = listOf(
                sibling("escape.png", "image/png").apply {
                    putString(ExplorerActionHostSessionKeys.RELATIVE_PATH, "../escape.png")
                },
            ),
        )
        assertTrue(runCatching { ExplorerSiblingImageClient.discover(explorerRequest(malformedSession)) }.isFailure)

        val session = FakeHostSession()
        val selected = ImageViewerPage(
            image = ImageViewerRequest(TARGET_URI, "photo10.png", 1_024L, "image/png"),
            hostRelativePath = "",
        )
        val escaped = ImageViewerPage(
            image = ImageViewerRequest(
                ImageViewerContract.hostImageUri(1),
                "escape.png",
                1_024L,
                "image/png",
            ),
            hostRelativePath = "../escape.png",
        )
        val request = ImageViewerLaunchRequest(
            pages = listOf(selected, escaped),
            hostSession = session,
            hostTargetId = "target-id",
        )
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        assertNull(ImageViewerContract.resolve(ImageViewerContract.viewerIntent(context, request)))
    }

    @Test
    fun roundTripsBoundedInternalGalleryWithoutGrantingSiblingContentUris() {
        val session = FakeHostSession()
        val selected = ImageViewerPage(
            image = ImageViewerRequest(TARGET_URI, "photo10.png", 1_024L, "image/png"),
            hostRelativePath = "",
        )
        val sibling = ImageViewerPage(
            image = ImageViewerRequest(
                ImageViewerContract.hostImageUri(1),
                "photo2.webp",
                2_048L,
                "image/webp",
            ),
            hostRelativePath = "photo2.webp",
        )
        val launch = ImageViewerLaunchRequest(
            pages = listOf(selected, sibling),
            hostSession = session,
            hostTargetId = "target-id",
        )
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        val resolved = ImageViewerContract.resolve(ImageViewerContract.viewerIntent(context, launch))

        assertNotNull(resolved)
        requireNotNull(resolved)
        assertEquals(listOf("photo10.png", "photo2.webp"), resolved.pages.map { it.image.displayName })
        assertEquals("photo2.webp", resolved.pages[1].hostRelativePath)
        assertEquals("autojs6-explorer", resolved.pages[1].image.targetUri.scheme)
        assertSame(session.asBinder(), resolved.hostSession?.asBinder())
    }

    @Test
    fun glideRegistersAContentUriLoaderForThePrivateSessionBridge() {
        val session = FakeHostSession()
        val launch = galleryLaunchRequest(
            session = session,
            siblingName = "photo2.webp",
            siblingSize = 2_048L,
            siblingMimeType = "image/webp",
        )
        val token = HostSessionImageRegistry.register(launch)
        try {
            val context = InstrumentationRegistry.getInstrumentation().targetContext
            val uri = HostSessionImageRegistry.imageUri(token, 1)
            assertTrue(Glide.get(context).registry.getModelLoaders(uri).isNotEmpty())
            assertEquals("image/webp", context.contentResolver.getType(uri))
        } finally {
            HostSessionImageRegistry.unregister(token)
        }
    }

    @Test
    fun readsSessionImageBoundsAndSizeFromReadOnlyDescriptor() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val file = File(context.cacheDir, "image-viewer-session-test.png")
        val bitmap = Bitmap.createBitmap(7, 5, Bitmap.Config.ARGB_8888)
        try {
            FileOutputStream(file).use { output ->
                assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG, 100, output))
            }
            val session = FakeHostSession(openedFile = file)
            val launch = galleryLaunchRequest(
                session = session,
                siblingName = file.name,
                siblingSize = file.length(),
                siblingMimeType = "image/png",
            )
            val token = HostSessionImageRegistry.register(launch)
            val metadata = try {
                val uri = HostSessionImageRegistry.imageUri(token, 1)
                val value = context.contentResolver.openFileDescriptor(uri, "r")?.use { descriptor ->
                    ImageContentValidator.readHostMetadata(descriptor, launch.pages[1].image)
                }
                val future = Glide.with(context).asDrawable().load(uri).submit()
                try {
                    assertNotNull(future.get(10L, TimeUnit.SECONDS))
                } finally {
                    Glide.with(context).clear(future)
                }
                value
            } finally {
                HostSessionImageRegistry.unregister(token)
            }

            assertNotNull(metadata)
            requireNotNull(metadata)
            assertEquals(file.length(), metadata.byteSize)
            assertEquals(7, metadata.width)
            assertEquals(5, metadata.height)
            assertTrue(session.openCalls.size >= 2)
            assertTrue(session.openCalls.all { it == "target-id" to file.name })
        } finally {
            bitmap.recycle()
            file.delete()
        }
    }

    private fun validExplorerIntent(
        session: IExplorerActionHostSession,
        targetUri: Uri = TARGET_URI,
        displayName: String = "photo10.png",
        mimeType: String = "image/png",
    ): Intent = explorerIntent(
        actionId = ThreeMapleImagePlugin.ACTION_ID,
        targets = listOf(ExplorerTargetSpec("target-id", targetUri, displayName, mimeType)),
        session = session,
    )

    private fun validSelectionIntent(
        session: IExplorerActionHostSession? = FakeHostSession(),
        targets: List<ExplorerTargetSpec> = listOf(
            ExplorerTargetSpec("target-10", TARGET_URI, "photo10.png", "image/png"),
            ExplorerTargetSpec("target-2", SECOND_TARGET_URI, "photo2.webp", "image/webp", 2_048L),
        ),
    ): Intent = explorerIntent(
        actionId = ThreeMapleImagePlugin.MULTIPLE_ACTION_ID,
        targets = targets,
        session = session,
    )

    private fun explorerIntent(
        actionId: String,
        targets: List<ExplorerTargetSpec>,
        session: IExplorerActionHostSession?,
    ): Intent {
        require(targets.isNotEmpty())
        val bundles = targets.map { target ->
            Bundle().apply {
                putString(ExplorerActionTargetKeys.ID, target.id)
                putParcelable(ExplorerActionTargetKeys.URI, target.uri)
                putString(ExplorerActionTargetKeys.DISPLAY_NAME, target.displayName)
                putInt(ExplorerActionTargetKeys.KIND, ExplorerActionValues.TARGET_FILE)
                putString(ExplorerActionTargetKeys.MIME_TYPE, target.mimeType)
                putLong(ExplorerActionTargetKeys.SIZE, target.size)
                putLong(ExplorerActionTargetKeys.LAST_MODIFIED, 1_700_000_000_000L)
            }
        }
        val first = targets.first()
        return Intent(ExplorerActionPluginActions.EXECUTE).apply {
            setDataAndType(first.uri, if (targets.size == 1) first.mimeType else "*/*")
            clipData = ClipData.newRawUri(first.displayName, first.uri).apply {
                targets.drop(1).forEach { target -> addItem(ClipData.Item(target.uri)) }
            }
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PREFIX_URI_PERMISSION)
            putExtra(ExplorerActionIntentExtras.ACTION_ID, actionId)
            putExtra(ExplorerActionIntentExtras.PROTOCOL_VERSION, ThreeMapleImagePlugin.PROTOCOL_VERSION)
            putExtra(ExplorerActionIntentExtras.REQUEST_ID, UUID.randomUUID().toString())
            putExtra(ExplorerActionIntentExtras.DISPLAY_NAME, first.displayName)
            putExtra(ExplorerActionIntentExtras.SIZE, first.size)
            putExtra(ExplorerActionIntentExtras.PARENT_URI, PARENT_URI)
            putExtra(ExplorerActionIntentExtras.PARENT_DISPLAY_PATH, "/images")
            putExtra(ExplorerActionIntentExtras.HOST_VERSION_CODE, ThreeMapleImagePlugin.REQUIRED_HOST_VERSION)
            putExtra(
                ExplorerActionIntentExtras.SOURCE_SURFACE,
                ExplorerActionIntentValues.SOURCE_SURFACE_MAIN,
            )
            putParcelableArrayListExtra(ExplorerActionIntentExtras.TARGETS, ArrayList(bundles))
            session?.let {
                putExtra(
                    ExplorerActionIntentExtras.HOST_SESSION,
                    Bundle().apply {
                        putBinder(ExplorerActionHostSessionKeys.BINDER, session.asBinder())
                    },
                )
            }
        }
    }

    private data class ExplorerTargetSpec(
        val id: String,
        val uri: Uri,
        val displayName: String,
        val mimeType: String,
        val size: Long = 1_024L,
    )

    private fun explorerRequest(session: IExplorerActionHostSession) = ExplorerImageRequest(
        requestId = UUID.randomUUID().toString(),
        parentUri = PARENT_URI,
        parentDisplayPath = "/images",
        targets = listOf(
            ExplorerImageTarget(
                id = "target-id",
                image = ImageViewerRequest(TARGET_URI, "photo10.png", 1_024L, "image/png"),
                lastModified = 1_700_000_000_000L,
            ),
        ),
        mode = ExplorerImageRequestMode.SINGLE_WITH_SIBLINGS,
        hostSession = session,
    )

    private fun galleryLaunchRequest(
        session: IExplorerActionHostSession,
        siblingName: String,
        siblingSize: Long,
        siblingMimeType: String,
    ) = ImageViewerLaunchRequest(
        pages = listOf(
            ImageViewerPage(
                image = ImageViewerRequest(TARGET_URI, "photo10.png", 1_024L, "image/png"),
                hostRelativePath = "",
            ),
            ImageViewerPage(
                image = ImageViewerRequest(
                    ImageViewerContract.hostImageUri(1),
                    siblingName,
                    siblingSize,
                    siblingMimeType,
                ),
                hostRelativePath = siblingName,
            ),
        ),
        hostSession = session,
        hostTargetId = "target-id",
    )

    private fun sibling(
        name: String,
        mimeType: String,
        readable: Boolean = true,
        symbolicLink: Boolean = false,
    ) = Bundle().apply {
        putString(ExplorerActionHostSessionKeys.RELATIVE_PATH, name)
        putString(ExplorerActionHostSessionKeys.DISPLAY_NAME, name)
        putInt(ExplorerActionHostSessionKeys.KIND, ExplorerActionValues.TARGET_FILE)
        putString(ExplorerActionHostSessionKeys.MIME_TYPE, mimeType)
        putLong(ExplorerActionHostSessionKeys.SIZE, 1_024L)
        putLong(ExplorerActionHostSessionKeys.LAST_MODIFIED, 1_700_000_000_000L)
        putBoolean(ExplorerActionHostSessionKeys.READABLE, readable)
        putBoolean(ExplorerActionHostSessionKeys.SYMBOLIC_LINK, symbolicLink)
    }

    private data class ListCall(
        val targetId: String?,
        val relativePath: String?,
        val offset: Int,
        val limit: Int,
    )

    private class FakeHostSession(
        private val children: List<Bundle> = emptyList(),
        private val pageSize: Int = Int.MAX_VALUE,
        private val openedFile: File? = null,
    ) : IExplorerActionHostSession.Stub() {

        val listCalls = mutableListOf<ListCall>()
        val openCalls = mutableListOf<Pair<String?, String?>>()
        val listOffsets: List<Int>
            get() = listCalls.map(ListCall::offset)

        override fun listChildren(
            targetId: String?,
            relativePath: String?,
            offset: Int,
            limit: Int,
        ): Bundle {
            listCalls += ListCall(targetId, relativePath, offset, limit)
            val items = children.drop(offset).take(minOf(pageSize, limit))
            val nextOffset = offset + items.size
            return Bundle().apply {
                putParcelableArrayList(ExplorerActionHostSessionKeys.ITEMS, ArrayList(items))
                putInt(ExplorerActionHostSessionKeys.NEXT_OFFSET, nextOffset)
                putBoolean(ExplorerActionHostSessionKeys.COMPLETE, nextOffset >= children.size)
            }
        }

        override fun openFile(targetId: String?, relativePath: String?): ParcelFileDescriptor {
            openCalls += targetId to relativePath
            return ParcelFileDescriptor.open(
                requireNotNull(openedFile),
                ParcelFileDescriptor.MODE_READ_ONLY,
            )
        }

        override fun prepareOutput(displayName: String?, mimeType: String?, conflictPolicy: Int): Bundle =
            throw UnsupportedOperationException()

        override fun openOutput(transactionId: String?): ParcelFileDescriptor =
            throw UnsupportedOperationException()

        override fun commitOutput(transactionId: String?): Bundle = throw UnsupportedOperationException()

        override fun abortOutput(transactionId: String?) = Unit

        override fun close() = Unit

        override fun openPendingOutput(transactionId: String?): ParcelFileDescriptor =
            throw UnsupportedOperationException()

        override fun prepareTargetReplacement(targetId: String?): Bundle =
            throw UnsupportedOperationException()

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

    private companion object {
        val PARENT_URI: Uri = Uri.parse("content://org.autojs.autojs6.fileprovider/root/images")
        val TARGET_URI: Uri = Uri.withAppendedPath(PARENT_URI, "photo10.png")
        val SECOND_TARGET_URI: Uri = Uri.withAppendedPath(PARENT_URI, "photo2.webp")
    }
}
