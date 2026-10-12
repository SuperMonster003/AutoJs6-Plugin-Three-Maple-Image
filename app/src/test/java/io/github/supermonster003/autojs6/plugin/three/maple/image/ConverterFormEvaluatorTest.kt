package io.github.supermonster003.autojs6.plugin.three.maple.image

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ConverterFormEvaluatorTest {

    private val source = ImageBitmapIO.SourceInfo(
        rawSize = ImagePixelSize(4000, 3000),
        displaySize = ImagePixelSize(4000, 3000),
        mimeType = "image/jpeg",
        orientation = 1,
    )
    private val allFormats = ImageOutputFormat.entries.toList()

    private fun evaluator(
        sdkInt: Int = 33,
        maxOutputBytes: Long = 50L * 1024 * 1024,
        formats: List<ImageOutputFormat> = allFormats,
        withinMemoryBudget: (ImagePixelSize) -> Boolean = { true },
    ) = ConverterFormEvaluator(source, formats, maxOutputBytes, sdkInt, withinMemoryBudget)

    @Test
    fun defaultDraftPrefersPngKeepsSourceSizeAndCapsLongEdgeAndTarget() {
        val draft = evaluator(maxOutputBytes = 200L * 1024).defaultDraft("100")
        assertEquals(ImageOutputFormat.PNG.mimeType, draft.formatMimeType)
        assertEquals(ImageResizeMode.ORIGINAL, draft.resizeMode)
        assertEquals("100", draft.percentage)
        assertEquals("4000", draft.width)
        assertEquals("3000", draft.height)
        assertEquals("1920", draft.longEdge)
        assertEquals("200", draft.targetFileSizeKibibytes)
        assertFalse(draft.lockAspectRatio)
        assertFalse(draft.targetFileSizeEnabled)
    }

    @Test
    fun defaultDraftFallsBackToTheFirstPermittedFormat() {
        val draft = evaluator(formats = listOf(ImageOutputFormat.WEBP, ImageOutputFormat.JPEG)).defaultDraft()
        assertEquals(ImageOutputFormat.WEBP.mimeType, draft.formatMimeType)
    }

    @Test
    fun pngHidesQualityLosslessAndTargetSizeAndEstimatesTheOriginalSize() {
        val form = evaluator()
        val result = form.evaluate(form.defaultDraft())
        assertEquals(ImageOutputFormat.PNG, result.format)
        assertFalse(result.qualityEnabled)
        assertFalse(result.losslessVisible)
        assertFalse(result.targetFileSizeVisible)
        assertFalse(result.jpegBackgroundVisible)
        assertEquals(ImagePixelSize(4000, 3000), result.outputSize)
        assertNotNull(result.estimatedBytes)
        assertTrue(result.convertEnabled)
        assertEquals(ImagePixelSize(4000, 3000), result.options?.targetSize)
    }

    @Test
    fun invalidPercentageReportsTheFieldErrorAndDisablesConvert() {
        val form = evaluator()
        val result = form.evaluate(form.defaultDraft().copy(resizeMode = ImageResizeMode.PERCENTAGE, percentage = "0"))
        assertEquals(ImageResizeError.INVALID_PERCENTAGE, result.resizeError)
        assertNull(result.outputSize)
        assertNull(result.estimatedBytes)
        assertFalse(result.convertEnabled)
        val valid = form.evaluate(form.defaultDraft().copy(resizeMode = ImageResizeMode.PERCENTAGE, percentage = "50"))
        assertEquals(ImagePixelSize(2000, 1500), valid.outputSize)
        assertTrue(valid.convertEnabled)
    }

    @Test
    fun memoryBudgetFailureBecomesAGeneralError() {
        val form = evaluator(withinMemoryBudget = { false })
        val result = form.evaluate(form.defaultDraft())
        assertEquals(ImageResizeError.MEMORY_BUDGET_EXCEEDED, result.resizeError)
        assertFalse(result.convertEnabled)
    }

    @Test
    fun jpegTargetFileSizeValidatesTheKibibytesAndReplacesTheEstimate() {
        val form = evaluator(maxOutputBytes = 1024L * 1024)
        val jpeg = form.defaultDraft().copy(formatMimeType = ImageOutputFormat.JPEG.mimeType)
        val plain = form.evaluate(jpeg)
        assertTrue(plain.qualityEnabled)
        assertTrue(plain.targetFileSizeVisible)
        assertTrue(plain.jpegBackgroundVisible)
        assertNotNull(plain.estimatedBytes)
        val invalid = form.evaluate(jpeg.copy(targetFileSizeEnabled = true, targetFileSizeKibibytes = "0"))
        assertTrue(invalid.targetFileSizeActive)
        assertTrue(invalid.targetFileSizeInvalid)
        assertFalse(invalid.qualityEnabled)
        assertFalse(invalid.convertEnabled)
        val tooLarge = form.evaluate(jpeg.copy(targetFileSizeEnabled = true, targetFileSizeKibibytes = "1025"))
        assertTrue(tooLarge.targetFileSizeInvalid)
        val valid = form.evaluate(jpeg.copy(targetFileSizeEnabled = true, targetFileSizeKibibytes = " 300 "))
        assertEquals(300L * 1024, valid.targetFileSizeBytes)
        assertNull(valid.estimatedBytes)
        assertEquals(300L * 1024, valid.options?.targetFileSizeBytes)
    }

    @Test
    fun webpLosslessDependsOnThePlatformAndExcludesQualityAndTargetSize() {
        val webp = evaluator(sdkInt = 30).defaultDraft().copy(formatMimeType = ImageOutputFormat.WEBP.mimeType, webpLossless = true)
        val modern = evaluator(sdkInt = 30).evaluate(webp)
        assertTrue(modern.losslessVisible)
        assertTrue(modern.losslessActive)
        assertFalse(modern.qualityEnabled)
        assertFalse(modern.targetFileSizeVisible)
        assertTrue(modern.options?.webpLossless == true)
        val legacy = evaluator(sdkInt = 29).evaluate(webp)
        assertFalse(legacy.losslessVisible)
        assertFalse(legacy.losslessActive)
        assertTrue(legacy.qualityEnabled)
        assertTrue(legacy.targetFileSizeVisible)
        assertFalse(legacy.options?.webpLossless == true)
    }

    @Test
    fun customSizeHelpersFollowTheSourceAspectRatio() {
        val form = evaluator()
        assertEquals(1500, form.heightForWidth(2000))
        assertEquals(2000, form.widthForHeight(1500))
        assertNull(form.heightForWidth(null))
        val custom = form.evaluate(form.defaultDraft().copy(resizeMode = ImageResizeMode.CUSTOM, width = "800", height = "abc"))
        assertEquals(ImageResizeError.INVALID_HEIGHT, custom.resizeError)
        val black = form.evaluate(form.defaultDraft().copy(formatMimeType = ImageOutputFormat.JPEG.mimeType, jpegBackgroundBlack = true))
        assertEquals(-0x1000000, black.options?.jpegBackgroundColor)
    }

    @Test
    fun unknownDraftFormatFallsBackToTheFirstPermittedFormat() {
        val form = evaluator(formats = listOf(ImageOutputFormat.JPEG, ImageOutputFormat.WEBP))
        assertEquals(ImageOutputFormat.JPEG, form.format(form.defaultDraft().copy(formatMimeType = "image/png")))
    }
}
