package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.HiddenFolderRow
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.HiddenFoldersScreen
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.MapleTheme
import kotlinx.coroutines.launch

/**
 * Settings > Hidden folders (ROADMAP 7.4): whether the album grid lists `.nomedia` folders, and the folders
 * themselves with "show again". Without file system access the page explains it and leads to Storage permission.
 */
class HiddenFoldersActivity : ConfiguredActivity() {

    private lateinit var hidden: HiddenFolders
    private lateinit var marks: FolderMarksRepository
    private lateinit var preferences: GalleryPreferences
    private lateinit var coordinator: MediaWriteCoordinator
    private var available by mutableStateOf(false)
    private var showInGallery by mutableStateOf(false)
    private var folders by mutableStateOf<List<HiddenFolderRow>?>(null)
    private var busy by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hidden = HiddenFolders(this)
        marks = FolderMarksRepository(this)
        preferences = GalleryPreferences(this)
        coordinator = MediaWriteCoordinator(this)
        setContent {
            MapleTheme(palette = appPalette) {
                HiddenFoldersScreen(
                    available = available,
                    showInGallery = showInGallery,
                    folders = folders,
                    busy = busy,
                    onBack = ::finish,
                    onShowInGallery = { value ->
                        preferences.showHiddenFolders = value
                        showInGallery = value
                    },
                    onOpenStorage = { runCatching { startActivity(StoragePermissionActivity.intent(this)) } },
                    onUnhide = ::unhide,
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun refresh() {
        available = hidden.available()
        showInGallery = preferences.showHiddenFolders
        lifecycleScope.launch {
            folders = hidden.list().map { folder ->
                HiddenFolderRow(folder.path, folder.name.ifEmpty { getString(R.string.gallery_album_root) }, folder.path.relativePath, folder.items.size)
            }
        }
    }

    private fun unhide(row: HiddenFolderRow) {
        if (busy) return
        busy = true
        lifecycleScope.launch {
            try {
                val outcome = coordinator.showFolders(listOf(row.path))
                if (outcome.succeeded > 0) marks.unmark(listOf(row.path), FolderMark.HIDDEN)
            } finally {
                busy = false
            }
            refresh()
        }
    }

    companion object {
        fun intent(context: Context): Intent = Intent(context, HiddenFoldersActivity::class.java)
    }
}
