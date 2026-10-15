package io.github.supermonster003.autojs6.plugin.three.maple.image.scriptapi

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import io.github.supermonster003.autojs6.plugin.three.maple.image.ImageOutputFormat
import io.github.supermonster003.autojs6.plugin.three.maple.image.ImagePixelSize
import io.github.supermonster003.autojs6.plugin.three.maple.image.MediaItem
import org.autojs.plugin.common.api.PluginCapabilityKeys
import org.autojs.plugin.gallery.api.GalleryActions
import org.autojs.plugin.gallery.api.GalleryCapabilityKeys
import org.autojs.plugin.gallery.api.GalleryContract
import org.autojs.plugin.gallery.api.GalleryErrorCodes
import org.autojs.plugin.gallery.api.GalleryIds
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/** The gallery service of the script object `gallery` (ROADMAP 10.2): manifest, identity, request rules, tokens and sizes. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class GalleryScriptApiTest {

    private fun violation(code: String = GalleryErrorCodes.INVALID_ARGUMENT, block: () -> Unit) {
        try {
            block()
            fail("expected $code")
        } catch (e: ContractViolation) {
            assertEquals(code, e.code)
        }
    }

    @Suppress("DEPRECATION")
    @Test
    fun theServiceAndTheSessionActivityResolveTheHostIntents() {
        val context = RuntimeEnvironment.getApplication()
        val services = context.packageManager.queryIntentServices(
            Intent(GalleryActions.SERVICE_ACTION).addCategory(GalleryActions.SERVICE_CATEGORY).setPackage(context.packageName),
            0,
        )
        val service = services.single().serviceInfo
        assertEquals(GalleryPluginService::class.java.name, service.name)
        assertEquals(GalleryActions.PLUGIN_PERMISSION, service.permission)
        assertTrue(service.exported && service.enabled)

        val activities = context.packageManager.queryIntentActivities(
            Intent(GalleryActions.SESSION_ACTIVITY_ACTION).addCategory(Intent.CATEGORY_DEFAULT).setPackage(context.packageName),
            0,
        )
        val activity = activities.single().activityInfo
        assertEquals(GallerySessionActivity::class.java.name, activity.name)
        assertEquals(GalleryActions.PLUGIN_PERMISSION, activity.permission)
        assertTrue(activity.exported)

        val picker = context.packageManager.getActivityInfo(android.content.ComponentName(context, GalleryPickerActivity::class.java), 0)
        assertFalse("the picker is started by the session Activity only", picker.exported)
        val provider = context.packageManager.resolveContentProvider(GallerySessionImages.AUTHORITY, PackageManager.GET_META_DATA)
        assertNotNull(provider)
        assertFalse(provider!!.exported)
    }

    @Test
    fun theGalleryIdentitySharesThePluginAndDeclaresTheContract() {
        val info = RuntimeEnvironment.getApplication().galleryPluginInfo()
        assertEquals(GalleryIds.PLUGIN_ID, info.id)
        assertEquals(GalleryIds.ENGINE, info.engine)
        assertEquals(GalleryIds.VARIANT_DEFAULT, info.variant)
        val capabilities = requireNotNull(info.capabilities)
        assertEquals(GalleryContract.CONTRACT_VERSION, capabilities.getInt(GalleryCapabilityKeys.CONTRACT_VERSION))
        assertEquals(GalleryIds.REQUIRED_HOST_VERSION_CODE, capabilities.getLong(PluginCapabilityKeys.REQUIRES_HOST_VERSION))
        assertEquals(GalleryContract.FEATURES, capabilities.getStringArray(GalleryCapabilityKeys.FEATURES)!!.toSet())
    }

    @Test
    fun pickerRequestsFollowTheContractRanges() {
        val single = ScriptRequests.picker(null)
        assertFalse(single.multiple)
        assertEquals(1, single.maxCount)
        val multiple = ScriptRequests.picker(Bundle().apply {
            putBoolean(GalleryContract.KEY_MULTIPLE, true)
            putStringArray(GalleryContract.KEY_MIME_TYPES, arrayOf("image/PNG", "image/png", "image/*"))
            putString(GalleryContract.KEY_TITLE, "  Pick  ")
        })
        assertEquals(GalleryContract.MAX_PICK_COUNT, multiple.maxCount)
        assertEquals(listOf("image/png", "image/*"), multiple.mimeTypes)
        assertEquals("Pick", multiple.title)
        violation { ScriptRequests.picker(Bundle().apply { putBoolean(GalleryContract.KEY_MULTIPLE, true); putInt(GalleryContract.KEY_MAX_COUNT, 0) }) }
        violation { ScriptRequests.picker(Bundle().apply { putStringArray(GalleryContract.KEY_MIME_TYPES, arrayOf("video/mp4")) }) }
        violation(GalleryErrorCodes.LIMIT_EXCEEDED) { ScriptRequests.picker(Bundle().apply { putString(GalleryContract.KEY_TITLE, "t".repeat(GalleryContract.MAX_TITLE_LENGTH + 1)) }) }
    }

    @Test
    fun viewerAndConvertRequestsAreCheckedAgain() {
        val (names, start) = ScriptRequests.viewer(2, Bundle().apply {
            putStringArray(GalleryContract.KEY_DISPLAY_NAMES, arrayOf("a.jpg", " b.png "))
            putInt(GalleryContract.KEY_START_INDEX, 1)
        })
        assertEquals(listOf("a.jpg", "b.png"), names)
        assertEquals(1, start)
        violation { ScriptRequests.viewer(2, Bundle().apply { putStringArray(GalleryContract.KEY_DISPLAY_NAMES, arrayOf("a.jpg")) }) }
        violation { ScriptRequests.viewer(1, Bundle().apply { putStringArray(GalleryContract.KEY_DISPLAY_NAMES, arrayOf("a.jpg")); putInt(GalleryContract.KEY_START_INDEX, 3) }) }
        violation(GalleryErrorCodes.LIMIT_EXCEEDED) { ScriptRequests.viewer(GalleryContract.MAX_VIEW_PAGES + 1, Bundle()) }

        val convert = ScriptRequests.convert(Bundle().apply {
            putString(GalleryContract.KEY_FORMAT, "webp")
            putBoolean(GalleryContract.KEY_LOSSLESS, true)
            putString(GalleryContract.KEY_SIZE_MODE, GalleryContract.SIZE_CUSTOM)
            putInt(GalleryContract.KEY_WIDTH, 100)
        })
        assertTrue(convert.lossless)
        assertEquals(100, convert.width)
        assertNull(convert.height)
        violation { ScriptRequests.convert(Bundle().apply { putString(GalleryContract.KEY_FORMAT, "gif") }) }
        violation { ScriptRequests.convert(Bundle().apply { putString(GalleryContract.KEY_FORMAT, "png"); putLong(GalleryContract.KEY_TARGET_BYTES, 1000L) }) }
        violation { ScriptRequests.convert(Bundle().apply { putString(GalleryContract.KEY_SIZE_MODE, GalleryContract.SIZE_PERCENT) }) }
        violation { ScriptRequests.convert(Bundle().apply { putString(GalleryContract.KEY_SIZE_MODE, GalleryContract.SIZE_CUSTOM); putInt(GalleryContract.KEY_WIDTH, 10); putBoolean(GalleryContract.KEY_KEEP_ASPECT_RATIO, false) }) }
    }

    @Test
    fun tokensAreRandomHexAndOnlyTheSessionIntentCarriesThem() {
        val token = ScriptSessionTokens.newToken()
        assertTrue(ScriptSessionTokens.isWellFormed(token))
        assertFalse(token == ScriptSessionTokens.newToken())
        assertEquals(token, ScriptSessionTokens.tokenOf(GalleryActions.SESSION_ACTIVITY_ACTION, explicit = true, token))
        assertNull(ScriptSessionTokens.tokenOf(GalleryActions.SESSION_ACTIVITY_ACTION, explicit = false, token))
        assertNull(ScriptSessionTokens.tokenOf(Intent.ACTION_VIEW, explicit = true, token))
        assertNull(ScriptSessionTokens.tokenOf(GalleryActions.SESSION_ACTIVITY_ACTION, explicit = true, token.uppercase()))
        assertTrue(ScriptSessionTokens.sameToken(token, String(token.toCharArray())))
        assertFalse(ScriptSessionTokens.sameToken(token, ScriptSessionTokens.newToken()))
    }

    @Test
    fun convertedSizesFollowTheConverterAndKeepTheAspectRatio() {
        val source = ImagePixelSize(4000, 3000)
        fun size(build: ConvertRequest.() -> ConvertRequest) = ScriptConverter.targetSize(source, ConvertRequest(null, EncodeRequest()).build())
        assertEquals(source, size { this })
        assertEquals(ImagePixelSize(2000, 1500), size { copy(sizeMode = GalleryContract.SIZE_PERCENT, percent = 50) })
        assertEquals(ImagePixelSize(1920, 1440), size { copy(sizeMode = GalleryContract.SIZE_LONG_EDGE, longEdge = 1920) })
        assertEquals(source, size { copy(sizeMode = GalleryContract.SIZE_LONG_EDGE, longEdge = 8000) })
        assertEquals(ImagePixelSize(800, 600), size { copy(sizeMode = GalleryContract.SIZE_CUSTOM, width = 800) })
        assertEquals(ImagePixelSize(400, 300), size { copy(sizeMode = GalleryContract.SIZE_CUSTOM, width = 800, height = 300) })
        assertEquals(ImagePixelSize(800, 300), size { copy(sizeMode = GalleryContract.SIZE_CUSTOM, width = 800, height = 300, keepAspectRatio = false) })
        violation(GalleryErrorCodes.LIMIT_EXCEEDED) { size { copy(sizeMode = GalleryContract.SIZE_PERCENT, percent = 1000) } }

        assertEquals(ImageOutputFormat.PNG, ScriptConverter.outputFormat(null, "image/png"))
        assertEquals(ImageOutputFormat.JPEG, ScriptConverter.outputFormat(null, "image/heic"))
        assertEquals(ImageOutputFormat.WEBP, ScriptConverter.outputFormat(GalleryContract.FORMAT_WEBP, "image/png"))
    }

    @Test
    fun thePickerFilterAcceptsTheRequestedTypes() {
        fun item(mime: String) = MediaItem(1L, "a", mime, 1L, 1, 1, 0L, null, 0L, "b", "DCIM/", "content://media/external/images/media/1")
        assertNull(GalleryPickerActivity.mimeFilter(emptyList()))
        assertNull(GalleryPickerActivity.mimeFilter(listOf("image/png", "image/*")))
        val filter = requireNotNull(GalleryPickerActivity.mimeFilter(listOf("image/png", "image/webp")))
        assertTrue(filter(item("image/png")))
        assertTrue(filter(item("IMAGE/WEBP")))
        assertFalse(filter(item("image/jpeg")))
    }
}
