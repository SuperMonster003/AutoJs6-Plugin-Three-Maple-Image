package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.ExcludedFolder
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.ExcludedFoldersScreen
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.MapleTheme
import kotlinx.coroutines.launch

/** Settings > Excluded folders (ROADMAP 7.4): the albums left out of the gallery, each one can come back. */
class ExcludedFoldersActivity : ConfiguredActivity() {

    private lateinit var marks: FolderMarksRepository
    private var folders by mutableStateOf<List<ExcludedFolder>?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        marks = FolderMarksRepository(this)
        setContent {
            MapleTheme(palette = appPalette) {
                ExcludedFoldersScreen(folders = folders, onBack = ::finish, onRestore = ::restore)
            }
        }
        refresh()
    }

    private fun refresh() {
        lifecycleScope.launch {
            val root = getString(R.string.gallery_album_root)
            folders = marks.paths(FolderMark.EXCLUDED).map { path ->
                ExcludedFolder(
                    path = path,
                    name = path.relativePath.trimEnd('/').substringAfterLast('/').ifEmpty { root },
                    location = StorageVolumes.location(this@ExcludedFoldersActivity, path),
                )
            }
        }
    }

    private fun restore(folder: ExcludedFolder) {
        lifecycleScope.launch {
            marks.unmark(listOf(folder.path), FolderMark.EXCLUDED)
            refresh()
        }
    }

    companion object {
        fun intent(context: Context): Intent = Intent(context, ExcludedFoldersActivity::class.java)
    }
}
