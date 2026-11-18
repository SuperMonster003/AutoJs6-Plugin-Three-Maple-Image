package io.github.supermonster003.autojs6.plugin.three.maple.image

import android.content.Context
import android.content.Intent
import java.io.IOException

/**
 * Opens the release history page. The settings row and the menus of the viewer and the editor all lead here; the
 * page replaced the old dialog that showed the raw markdown.
 */
internal fun Context.openReleaseHistory() {
    startActivity(Intent(this, ReleaseHistoryActivity::class.java))
}

/**
 * Returns the releases of the first candidate asset that holds at least one, so a missing, blank or unparsable
 * localized changelog falls back to the English one; empty when none of them does (the page shows its empty state).
 */
internal fun loadReleaseHistory(candidates: List<String>, loader: (String) -> String): List<ReleaseHistoryEntry> {
    for (path in candidates) {
        val markdown = try { loader(path) } catch (_: IOException) { continue }
        val entries = ReleaseHistoryParser.parse(markdown)
        if (entries.isNotEmpty()) return entries
    }
    return emptyList()
}
