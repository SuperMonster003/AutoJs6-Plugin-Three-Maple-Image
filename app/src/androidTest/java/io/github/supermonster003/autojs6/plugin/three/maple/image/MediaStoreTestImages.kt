package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

/** Inserts small solid PNGs into the device library under a test folder and removes them again. */
internal object MediaStoreTestImages {

    const val PIXELS = 64

    /** The permissions a test needs to insert, read and delete its own rows on this platform. */
    fun permissions(): Array<String> = buildList {
        add(MediaPermissionPolicy.readPermission(Build.VERSION.SDK_INT))
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }.toTypedArray()

    fun insert(context: Context, folder: String, name: String, color: Int): Uri {
        val resolver = context.contentResolver
        val bytes = solidPng(color)
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, name)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/" + folder)
            values.put(MediaStore.Images.Media.IS_PENDING, 1)
            val collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            val uri = requireNotNull(resolver.insert(collection, values))
            requireNotNull(resolver.openOutputStream(uri)).use { stream -> stream.write(bytes) }
            resolver.update(uri, ContentValues().apply { put(MediaStore.Images.Media.IS_PENDING, 0) }, null, null)
            return uri
        }
        val file = File(legacyFolder(folder).apply { mkdirs() }, name)
        FileOutputStream(file).use { stream -> stream.write(bytes) }
        @Suppress("DEPRECATION")
        values.put(MediaStore.Images.Media.DATA, file.absolutePath)
        values.put(MediaStore.Images.Media.SIZE, file.length())
        values.put(MediaStore.Images.Media.WIDTH, PIXELS)
        values.put(MediaStore.Images.Media.HEIGHT, PIXELS)
        @Suppress("DEPRECATION")
        return requireNotNull(resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values))
    }

    fun cleanUp(context: Context, uris: List<Uri>, folder: String) {
        uris.forEach { uri -> runCatching { context.contentResolver.delete(uri, null, null) } }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            legacyFolder(folder).deleteRecursively()
        } else {
            // The provider removed the files with their rows; the empty folder would otherwise stay behind.
            runCatching { legacyFolder(folder).delete() }
        }
    }

    fun solidPng(color: Int): ByteArray {
        val bitmap = Bitmap.createBitmap(PIXELS, PIXELS, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(color)
        return ByteArrayOutputStream().use { stream ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            bitmap.recycle()
            stream.toByteArray()
        }
    }

    @Suppress("DEPRECATION")
    private fun legacyFolder(folder: String): File =
        File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), folder)
}
