package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.AboutScreen
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.MapleTheme

internal class AboutActivity : ConfiguredActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val version = packageManager.getPackageInfo(packageName, 0).versionName.orEmpty()
        setContent {
            MapleTheme(palette = appPalette) {
                AboutScreen(
                    versionName = version,
                    onBack = ::finish,
                    onSources = {
                        runCatching { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(REPOSITORY_URL))) }
                    },
                )
            }
        }
    }

    private companion object {
        const val REPOSITORY_URL = "https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image"
    }
}
