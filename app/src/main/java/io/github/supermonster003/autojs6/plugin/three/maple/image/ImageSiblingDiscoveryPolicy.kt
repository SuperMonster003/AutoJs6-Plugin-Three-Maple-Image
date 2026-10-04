package io.github.supermonster003.autojs6.plugin.three.maple.image

import java.util.Locale
import org.autojs.plugin.explorer.api.ExplorerActionValues

internal data class ImageSiblingItem(
    val relativePath: String,
    val displayName: String,
    val kind: Int,
    val mimeType: String,
    val declaredSize: Long,
    val lastModified: Long,
    val readable: Boolean,
    val symbolicLink: Boolean,
)

internal data class DiscoveredImagePage(
    val relativePath: String,
    val displayName: String,
    val mimeType: String,
    val declaredSize: Long,
)

internal data class DiscoveredImageGallery(
    val pages: List<DiscoveredImagePage>,
    val startIndex: Int,
)

/** Pure, bounded direct-sibling discovery with stable natural filename ordering. */
internal object ImageSiblingDiscoveryPolicy {

    const val MAX_PAGES = 128

    private val supportedExtensions = ThreeMapleImagePlugin.EXTENSIONS.toSet()
    fun discover(
        selectedDisplayName: String,
        selectedMimeType: String,
        selectedDeclaredSize: Long,
        siblings: List<ImageSiblingItem>,
    ): DiscoveredImageGallery {
        require(ImageRequestPolicy.validateDisplayName(selectedDisplayName) != null)
        require(extension(selectedDisplayName) in supportedExtensions)
        require(ImageFormatSupport.isSupportedExplorerTarget(selectedDisplayName, selectedMimeType))
        require(ImageRequestPolicy.isDeclaredSizeAccepted(selectedDeclaredSize))

        val pagesByName = LinkedHashMap<String, DiscoveredImagePage>()
        siblings.asSequence()
            .filter { sibling ->
                sibling.kind == ExplorerActionValues.TARGET_FILE &&
                    sibling.readable &&
                    !sibling.symbolicLink &&
                    sibling.relativePath == sibling.displayName &&
                    ImageRequestPolicy.validateDisplayName(sibling.displayName) != null &&
                    extension(sibling.displayName) in supportedExtensions &&
                    ImageRequestPolicy.isDeclaredSizeAccepted(sibling.declaredSize) &&
                    sibling.lastModified >= -1L
            }
            .distinctBy(ImageSiblingItem::relativePath)
            .forEach { sibling ->
                pagesByName.putIfAbsent(
                    sibling.displayName,
                    DiscoveredImagePage(
                        relativePath = sibling.relativePath,
                        displayName = sibling.displayName,
                        mimeType = imageMimeType(sibling.mimeType, sibling.displayName),
                        declaredSize = sibling.declaredSize,
                    ),
                )
            }
        pagesByName[selectedDisplayName] = DiscoveredImagePage(
            relativePath = selectedDisplayName,
            displayName = selectedDisplayName,
            mimeType = selectedMimeType,
            declaredSize = selectedDeclaredSize,
        )

        val sorted = pagesByName.values.sortedWith { first, second ->
            compareNaturally(first.displayName, second.displayName)
        }
        val selectedIndex = sorted.indexOfFirst { it.displayName == selectedDisplayName }
        check(selectedIndex >= 0)
        val windowStart = (selectedIndex - MAX_PAGES / 2)
            .coerceIn(0, (sorted.size - MAX_PAGES).coerceAtLeast(0))
        val bounded = sorted.drop(windowStart).take(MAX_PAGES).map { page ->
            if (page.displayName == selectedDisplayName) page.copy(relativePath = "") else page
        }
        return DiscoveredImageGallery(
            pages = bounded,
            startIndex = bounded.indexOfFirst { it.displayName == selectedDisplayName }.also {
                check(it >= 0)
            },
        )
    }

    fun compareNaturally(first: String, second: String): Int {
        var firstIndex = 0
        var secondIndex = 0
        var stableCaseComparison = 0
        while (firstIndex < first.length && secondIndex < second.length) {
            val firstCharacter = first[firstIndex]
            val secondCharacter = second[secondIndex]
            if (firstCharacter.isDigit() && secondCharacter.isDigit()) {
                val firstEnd = first.consumeDigits(firstIndex)
                val secondEnd = second.consumeDigits(secondIndex)
                val firstDigits = first.substring(firstIndex, firstEnd)
                val secondDigits = second.substring(secondIndex, secondEnd)
                val firstSignificant = firstDigits.trimStart('0').ifEmpty { "0" }
                val secondSignificant = secondDigits.trimStart('0').ifEmpty { "0" }
                val comparison = firstSignificant.length.compareTo(secondSignificant.length)
                    .takeIf { it != 0 }
                    ?: firstSignificant.compareTo(secondSignificant).takeIf { it != 0 }
                    ?: firstDigits.length.compareTo(secondDigits.length)
                if (comparison != 0) return comparison
                firstIndex = firstEnd
                secondIndex = secondEnd
                continue
            }
            val folded = firstCharacter.lowercaseChar().compareTo(secondCharacter.lowercaseChar())
            if (folded != 0) return folded
            if (stableCaseComparison == 0) {
                stableCaseComparison = firstCharacter.compareTo(secondCharacter)
            }
            firstIndex += 1
            secondIndex += 1
        }
        return first.length.compareTo(second.length).takeIf { it != 0 } ?: stableCaseComparison
    }

    private fun imageMimeType(value: String, displayName: String): String =
        ImageFormatSupport.siblingMimeType(displayName, value)

    private fun String.consumeDigits(start: Int): Int {
        var index = start
        while (index < length && this[index].isDigit()) index += 1
        return index
    }

    private fun extension(value: String): String =
        value.substringAfterLast('.', "").lowercase(Locale.ROOT)
}
