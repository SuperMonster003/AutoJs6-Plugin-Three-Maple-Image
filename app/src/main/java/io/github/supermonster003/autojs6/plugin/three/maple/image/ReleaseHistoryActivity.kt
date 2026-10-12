package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.os.Bundle
import androidx.activity.compose.setContent
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.MapleTheme
import io.github.supermonster003.autojs6.plugin.three.maple.image.ui.compose.ReleaseHistoryScreen
import java.util.Locale

/** Maps the UI locale to the bundled changelog asset shipped for it. */
internal object ReleaseHistoryAssetPolicy {
    fun assetFor(locale: Locale): String = when (locale.language.lowercase(Locale.ROOT)) {
        "zh" -> when {
            locale.script.equals("Hans", ignoreCase = true) -> "CHANGELOG-zh-Hans.md"
            locale.country.equals("HK", ignoreCase = true) ||
                locale.country.equals("MO", ignoreCase = true) -> "CHANGELOG-zh-Hant-HK.md"
            locale.script.equals("Hant", ignoreCase = true) ||
                locale.country.equals("TW", ignoreCase = true) -> "CHANGELOG-zh-Hant-TW.md"
            else -> "CHANGELOG-zh-Hans.md"
        }
        "ar" -> "CHANGELOG-ar.md"
        "es" -> "CHANGELOG-es.md"
        "fr" -> "CHANGELOG-fr.md"
        "ja" -> "CHANGELOG-ja.md"
        "ko" -> "CHANGELOG-ko.md"
        "ru" -> "CHANGELOG-ru.md"
        else -> "CHANGELOG-en.md"
    }
}

internal data class ReleaseHistoryItem(val tag: String?, val text: String)

internal data class ReleaseHistoryEntry(
    val version: String,
    val date: String?,
    val items: List<ReleaseHistoryItem>,
)

/**
 * Parses the narrow markdown dialect used by the bundled changelogs: `# vX.Y.Z` or `## vX.Y.Z` version
 * headings, `###### date` lines, and `` * `tag` text `` bullets. Anything else (the section title, the
 * separator lines) is ignored.
 */
internal object ReleaseHistoryParser {
    private val BULLET_PATTERN = Regex("^\\*\\s+(?:`([^`]+)`\\s*)?(.*)$")
    private val VERSION_PATTERN = Regex("^#{1,2}\\s+(v\\S+)\\s*$")

    fun parse(markdown: String): List<ReleaseHistoryEntry> {
        val entries = mutableListOf<ReleaseHistoryEntry>()
        var version: String? = null
        var date: String? = null
        var items = mutableListOf<ReleaseHistoryItem>()

        fun flush() {
            val flushedVersion = version ?: return
            entries.add(ReleaseHistoryEntry(flushedVersion, date, items.toList()))
        }

        for (rawLine in markdown.lineSequence()) {
            val line = rawLine.trim()
            val versionMatch = VERSION_PATTERN.matchEntire(line)
            when {
                versionMatch != null -> {
                    flush()
                    version = versionMatch.groupValues[1]
                    date = null
                    items = mutableListOf()
                }
                line.startsWith("###### ") -> date = line.removePrefix("###### ").trim()
                line.startsWith("* ") && version != null -> {
                    val match = BULLET_PATTERN.matchEntire(line) ?: continue
                    val tag = match.groupValues[1].takeIf { it.isNotEmpty() }
                    val text = match.groupValues[2].trim()
                    if (tag != null || text.isNotEmpty()) {
                        items.add(ReleaseHistoryItem(tag, text))
                    }
                }
            }
        }
        flush()
        return entries
    }
}

/** Renders the bundled changelog for the current locale as one group per released version. */
internal class ReleaseHistoryActivity : ConfiguredActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val entries = loadReleaseHistory()?.let(ReleaseHistoryParser::parse).orEmpty()
        setContent {
            MapleTheme(palette = appPalette) {
                ReleaseHistoryScreen(entries = entries, onBack = ::finish)
            }
        }
    }

    private fun loadReleaseHistory(): String? = runCatching {
        val assetName = ReleaseHistoryAssetPolicy.assetFor(resources.configuration.locales[0])
        assets.open("doc/$assetName").use { stream ->
            stream.readBytes().toString(Charsets.UTF_8)
        }
    }.getOrNull()
}
