package io.github.supermonster003.autojs6.plugin.imagetools

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
}
