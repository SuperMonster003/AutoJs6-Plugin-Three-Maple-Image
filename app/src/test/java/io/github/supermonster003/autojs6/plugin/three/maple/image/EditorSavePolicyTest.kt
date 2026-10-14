package io.github.supermonster003.autojs6.plugin.three.maple.image

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class EditorSavePolicyTest {

    private val allMimeTypes = ImageOutputFormat.entries.mapTo(mutableSetOf()) { it.mimeType }

    @Test
    fun availableFormatsContainFollowSourceAndOnlyPermittedExplicitChoices() {
        assertEquals(
            listOf(
                EditorSaveFormat.FOLLOW_SOURCE,
                EditorSaveFormat.JPEG,
                EditorSaveFormat.WEBP,
            ),
            EditorSavePolicy.availableFormats(setOf("image/jpeg", "image/webp")),
        )
    }

    @Test
    fun followSourcePreservesExistingMimeMapping() {
        mapOf(
            "image/jpeg" to ImageOutputFormat.JPEG,
            "image/png" to ImageOutputFormat.PNG,
            "image/webp" to ImageOutputFormat.WEBP,
            "image/gif" to ImageOutputFormat.PNG,
        ).forEach { (mimeType, expected) ->
            assertEquals(
                expected,
                EditorSavePolicy.effectiveFormat(
                    EditorSaveFormat.FOLLOW_SOURCE,
                    mimeType,
                    allMimeTypes,
                ),
            )
        }
    }

    @Test
    fun followSourceUsesTheSameFirstPermittedFallbackAsTheOldEditorSavePath() {
        assertEquals(
            ImageOutputFormat.WEBP,
            EditorSavePolicy.effectiveFormat(
                EditorSaveFormat.FOLLOW_SOURCE,
                detectedMimeType = "image/jpeg",
                allowedOutputMimeTypes = setOf("image/webp"),
            ),
        )
    }

    @Test
    fun explicitFormatMustBePermittedBeforeAnOutputTransactionCanStart() {
        assertEquals(
            ImageOutputFormat.JPEG,
            EditorSavePolicy.effectiveFormat(
                EditorSaveFormat.JPEG,
                detectedMimeType = "image/png",
                allowedOutputMimeTypes = allMimeTypes,
            ),
        )
        assertThrows(IllegalArgumentException::class.java) {
            EditorSavePolicy.effectiveFormat(
                EditorSaveFormat.JPEG,
                detectedMimeType = "image/png",
                allowedOutputMimeTypes = setOf("image/png"),
            )
        }
    }

    @Test
    fun editorSelectionBecomesTheSameConversionOptionsUsedByTheConverterEncoder() {
        val targetSize = ImagePixelSize(640, 480)
        EditorSavePolicy.availableFormats(allMimeTypes).forEach { selection ->
            val saveOptions = EditorSaveOptions(selection, quality = 73)
            val conversionOptions = EditorSavePolicy.conversionOptions(
                saveOptions = saveOptions,
                detectedMimeType = "image/webp",
                allowedOutputMimeTypes = allMimeTypes,
                targetSize = targetSize,
                sdkInt = 30,
            )

            assertEquals(
                EditorSavePolicy.effectiveFormat(selection, "image/webp", allMimeTypes),
                conversionOptions.format,
            )
            assertEquals(73, conversionOptions.quality)
            assertEquals(targetSize, conversionOptions.targetSize)
            assertEquals(ImageConversionOptions.DEFAULT_JPEG_BACKGROUND_COLOR, conversionOptions.jpegBackgroundColor)
        }
    }

    @Test
    fun editorLosslessRequestIsKeptOnAndroidElevenAndSanitizedOnOlderVersions() {
        val saveOptions = EditorSaveOptions(
            format = EditorSaveFormat.WEBP,
            quality = 41,
            webpLossless = true,
        )
        fun resolve(sdkInt: Int) = EditorSavePolicy.conversionOptions(
            saveOptions = saveOptions,
            detectedMimeType = "image/png",
            allowedOutputMimeTypes = allMimeTypes,
            targetSize = ImagePixelSize(100, 80),
            sdkInt = sdkInt,
        )

        assertEquals(true, resolve(30).webpLossless)
        assertEquals(false, resolve(29).webpLossless)
        assertEquals(41, resolve(29).quality)
    }

    @Test
    fun confirmedOptionsAndOpenDialogDraftRemainIndependentInEditorState() {
        val confirmed = EditorSaveOptions(EditorSaveFormat.FOLLOW_SOURCE, quality = 92)
        val draft = EditorSaveOptions(EditorSaveFormat.WEBP, quality = 61, webpLossless = true)
        val state = EditorUiState(saveOptions = confirmed, saveDialogDraft = draft)

        assertEquals(confirmed, state.saveOptions)
        assertEquals(draft, state.saveDialogDraft)
    }

    @Test
    fun overwritingKeepsTheOriginalFormatAndIsNotOfferedForOthers() {
        assertEquals(ImageOutputFormat.JPEG, EditorSavePolicy.overwriteFormat("image/jpeg", allMimeTypes))
        assertEquals(ImageOutputFormat.WEBP, EditorSavePolicy.overwriteFormat("image/webp", allMimeTypes))
        // GIF would become PNG under the same name: no overwrite.
        assertEquals(null, EditorSavePolicy.overwriteFormat("image/gif", allMimeTypes))
        assertEquals(null, EditorSavePolicy.overwriteFormat("image/heic", allMimeTypes))
        assertEquals(null, EditorSavePolicy.overwriteFormat("image/png", setOf("image/jpeg")))
        // The overwrite wins over a format picked before it was switched on.
        assertEquals(
            ImageOutputFormat.PNG,
            EditorSavePolicy.effectiveFormat(EditorSaveFormat.JPEG, "image/png", allMimeTypes, overwrite = true),
        )
        assertThrows(IllegalArgumentException::class.java) {
            EditorSavePolicy.effectiveFormat(EditorSaveFormat.FOLLOW_SOURCE, "image/gif", allMimeTypes, overwrite = true)
        }
    }

    @Test
    fun theFileNameShowsTheSuffixOrTheOriginalName() {
        assertEquals("photo_edited.webp", EditorSavePolicy.outputFileName("photo.jpg", ImageOutputFormat.WEBP, "edited", overwrite = false))
        assertEquals("photo.jpg", EditorSavePolicy.outputFileName("photo.jpg", ImageOutputFormat.JPEG, "edited", overwrite = true))
        assertEquals("a.b_edited.png", EditorSavePolicy.outputFileName("a.b.gif", ImageOutputFormat.PNG, "edited", overwrite = false))
        assertEquals("README_edited.jpg", EditorSavePolicy.outputFileName("README", ImageOutputFormat.JPEG, "edited", overwrite = false))
        assertEquals(".hidden_edited.jpg", EditorSavePolicy.outputFileName(".hidden", ImageOutputFormat.JPEG, "edited", overwrite = false))
    }

    @Test
    fun capturedDetailsAreKeptOnlyWhenAsked() {
        val metadata = PreservedExifMetadata.from(mapOf("Make" to "Maple", "Model" to "Test"))
        fun options(keep: Boolean) = EditorSavePolicy.conversionOptions(
            saveOptions = EditorSaveOptions(keepExif = keep),
            detectedMimeType = "image/jpeg",
            allowedOutputMimeTypes = allMimeTypes,
            targetSize = ImagePixelSize(10, 10),
            sdkInt = 33,
            preservableExif = metadata,
        )
        assertEquals(metadata.attributes, options(keep = true).preservedExifMetadata?.attributes)
        assertEquals(null, options(keep = false).preservedExifMetadata)
        // Nothing to keep: no metadata step at all.
        val empty = EditorSavePolicy.conversionOptions(
            EditorSaveOptions(keepExif = true), "image/jpeg", allMimeTypes, ImagePixelSize(10, 10), 33, PreservedExifMetadata.EMPTY,
        )
        assertEquals(null, empty.preservedExifMetadata)
    }
}
