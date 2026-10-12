package io.github.supermonster003.autojs6.plugin.three.maple.image.debug

import android.content.ContentUris
import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import android.provider.MediaStore
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

/**
 * ROADMAP 5.1 image-loading spike (T3). Debug builds only. Launch with
 * `am start -n <pkg>/.debug.ComposeSpikeActivity --ei columns 4 --ei limit 2000` after granting the
 * debug-only media permission. The activity logs the time from onCreate to the first frame that shows grid
 * cells and calls reportFullyDrawn, so `am start -W`, logcat, gfxinfo and meminfo give a repeatable grid
 * benchmark. The 2026-10-13 comparison against Coil 3 (recorded in docs/design/ui-redesign.md) chose Glide.
 */
class ComposeSpikeActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val startedAt = SystemClock.uptimeMillis()
        val loader = "glide"
        val columns = intent.getIntExtra("columns", 4).coerceIn(1, 8)
        val limit = intent.getIntExtra("limit", 2000).coerceIn(1, 20_000)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    SpikeGrid(loader = loader, columns = columns, limit = limit) { count ->
                        val elapsed = SystemClock.uptimeMillis() - startedAt
                        Log.i(TAG, "loader=$loader columns=$columns items=$count firstGridFrameMs=$elapsed")
                        reportFullyDrawn()
                    }
                }
            }
        }
    }

    @Composable
    private fun SpikeGrid(loader: String, columns: Int, limit: Int, onFirstFrame: (Int) -> Unit) {
        var uris by remember { mutableStateOf<List<Uri>?>(null) }
        LaunchedEffect(limit) {
            uris = withContext(Dispatchers.IO) { queryImages(limit) }
        }
        val items = uris
        if (items == null) {
            Text(text = "Loading", modifier = Modifier.padding(24.dp))
            return
        }
        val state = rememberLazyGridState()
        LaunchedEffect(items) {
            snapshotFlow { state.layoutInfo.visibleItemsInfo.isNotEmpty() }.first { it }
            onFirstFrame(items.size)
        }
        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            state = state,
            modifier = Modifier.fillMaxSize().testTag("spike.grid"),
        ) {
            items(items, key = { it.toString() }) { uri ->
                Box(modifier = Modifier.fillMaxWidth().aspectRatio(1f).padding(1.dp)) {
                    GlideCell(uri)
                }
            }
        }
    }

    @OptIn(ExperimentalGlideComposeApi::class)
    @Composable
    private fun GlideCell(uri: Uri) {
        GlideImage(
            model = uri,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
    }

    private fun queryImages(limit: Int): List<Uri> {
        val collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(MediaStore.Images.Media._ID)
        val result = ArrayList<Uri>(limit)
        contentResolver.query(
            collection,
            projection,
            null,
            null,
            "${MediaStore.Images.Media.DATE_MODIFIED} DESC",
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            while (cursor.moveToNext() && result.size < limit) {
                result += ContentUris.withAppendedId(collection, cursor.getLong(idColumn))
            }
        }
        return result
    }

    private companion object {
        const val TAG = "MapleComposeSpike"
    }
}
