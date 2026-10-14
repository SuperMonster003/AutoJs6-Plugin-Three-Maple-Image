package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.content.Context
import android.os.Build
import android.os.storage.StorageManager

/** User-facing names of storage volumes (ROADMAP 7.3 / 7.4). */
internal object StorageVolumes {

    /** The system's name of a storage card ("SanDisk SD card"); null for the internal storage. */
    fun label(context: Context, volume: String): String? {
        if (MediaCatalog.isPrimaryVolume(volume)) return null
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return volume.uppercase()
        val storage = context.getSystemService(StorageManager::class.java) ?: return volume.uppercase()
        val match = storage.storageVolumes.firstOrNull { it.mediaStoreVolumeName.equals(volume, ignoreCase = true) }
        return match?.getDescription(context) ?: volume.uppercase()
    }

    /** "SD card · Pictures/Trips/" for a storage card, the relative path alone for the internal storage. */
    fun location(context: Context, path: AlbumPath): String =
        label(context, path.volume)?.let { "$it · ${path.relativePath}" } ?: path.relativePath
}
