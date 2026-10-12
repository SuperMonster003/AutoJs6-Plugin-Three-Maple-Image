package io.github.supermonster003.autojs6.plugin.three.maple.image

import org.junit.Assert.*
import org.junit.Test
import java.io.FileNotFoundException

class ReleaseHistoryTest {
    @Test fun mapsChineseLocalesAndFallsBackToEnglish() {
        assertEquals("doc/CHANGELOG-zh-Hant-HK.md", releaseHistoryCandidates("zh", "HK", "").first())
        assertEquals("doc/CHANGELOG-zh-Hant-TW.md", releaseHistoryCandidates("zh", "TW", "").first())
        assertEquals("doc/CHANGELOG-zh-Hant-TW.md", releaseHistoryCandidates("zh", "", "Hant").first())
        assertEquals("doc/CHANGELOG-zh-Hans.md", releaseHistoryCandidates("zh", "CN", "Hans").first())
        assertEquals(listOf("doc/CHANGELOG-en.md"), releaseHistoryCandidates("de", "DE", ""))
    }
    @Test fun skipsMissingOrBlankLocalizedAssetsAndReportsCompleteFailure() {
        val candidates = releaseHistoryCandidates("fr", "FR", "")
        assertEquals("English history", loadReleaseHistory(candidates) {
            if (it.endsWith("-en.md")) "English history" else throw FileNotFoundException()
        })
        assertNull(loadReleaseHistory(candidates) { " " })
    }
}

class ReleaseHistoryParserTest {
    @Test fun parsesTheGeneratedChangelogDialect() {
        val markdown = """
            ******

            ### Release History

            ******

            ## v2.1.0

            ###### 2026/10/13

            * `Hint` First note
            * `Improved` Second note

            ## v2.0.0

            ###### 2026/10/04

            * `Feature` Third note
            * Untagged note
        """.trimIndent()
        val entries = ReleaseHistoryParser.parse(markdown)
        assertEquals(listOf("v2.1.0", "v2.0.0"), entries.map { it.version })
        assertEquals("2026/10/13", entries[0].date)
        assertEquals(listOf("Hint" to "First note", "Improved" to "Second note"), entries[0].items.map { it.tag to it.text })
        assertEquals(listOf("Feature" to "Third note", null to "Untagged note"), entries[1].items.map { it.tag to it.text })
    }

    @Test fun parsesTheSingleHashHeadingToo() {
        val entries = ReleaseHistoryParser.parse("# v1.0.0\n\n* `Feature` Note\n")
        assertEquals("v1.0.0", entries.single().version)
        assertEquals(1, entries.single().items.size)
    }

    @Test fun ignoresSectionTitlesThatAreNotVersions() {
        assertTrue(ReleaseHistoryParser.parse("### Release History\n\n* `Feature` Orphan\n").isEmpty())
    }
}
