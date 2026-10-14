package io.github.supermonster003.autojs6.plugin.three.maple.image

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class FilterPresetTest {

    private val identity = LookMath.identity()

    @Test
    fun intensityZeroIsTheIdentityAndFullIntensityIsThePreset() {
        for (preset in FilterPreset.entries) {
            assertArrayEquals(preset.name, identity, preset.matrix(0), 1e-6f)
            assertArrayEquals(preset.name, preset.matrix, preset.matrix(100), 1e-6f)
            assertArrayEquals(preset.name, preset.matrix(100), preset.matrix(250), 1e-6f)
        }
        assertArrayEquals(identity, FilterPreset.NONE.matrix, 0f)
    }

    @Test
    fun colorPresetsStayInvertibleWhileGrayscaleOnesCollapseColor() {
        for (preset in FilterPreset.entries) {
            val determinant = LookMath.colorDeterminant(preset.matrix)
            if (preset.monochrome) {
                assertEquals(preset.name, 0f, determinant, 1e-4f)
            } else {
                assertTrue("${preset.name} determinant $determinant", abs(determinant) > 0.05f)
            }
            // Half strength keeps colors apart, even for the grayscale presets; half an inversion is plain mid gray.
            if (preset == FilterPreset.INVERT) {
                assertArrayEquals(intArrayOf(128, 128, 128, 255), LookMath.apply(preset.matrix(50), 10, 200, 90, 255))
            } else {
                assertTrue(preset.name, abs(LookMath.colorDeterminant(preset.matrix(50))) > 0.01f)
            }
        }
    }

    @Test
    fun theTwelveToSixteenPresetsAreDistinct() {
        val presets = FilterPreset.entries - FilterPreset.NONE
        assertTrue(presets.size in 12..16)
        val distinct = presets.map { it.matrix.toList() }.toSet()
        assertEquals(presets.size, distinct.size)
    }

    @Test
    fun knownColorsMoveTheWayEachPresetPromises() {
        val gray = LookMath.apply(FilterPreset.MONO.matrix, 200, 40, 40, 255)
        assertEquals(gray[0], gray[1])
        assertEquals(gray[1], gray[2])
        val inverted = LookMath.apply(FilterPreset.INVERT.matrix, 10, 100, 250, 255)
        assertArrayEquals(intArrayOf(245, 155, 5, 255), inverted)
        val warm = LookMath.apply(FilterPreset.WARM.matrix, 128, 128, 128, 255)
        assertTrue(warm[0] > warm[2])
        val cool = LookMath.apply(FilterPreset.COOL.matrix, 128, 128, 128, 255)
        assertTrue(cool[2] > cool[0])
        // Alpha is never touched.
        for (preset in FilterPreset.entries) assertEquals(preset.name, 77, LookMath.apply(preset.matrix, 90, 120, 30, 77)[3])
    }

    @Test
    fun concatAppliesInOrder() {
        val first = LookMath.offset(10f, 0f, 0f)
        val second = LookMath.scale(2f, 1f, 1f)
        // (r + 10) * 2, not r * 2 + 10.
        assertEquals(60, LookMath.apply(LookMath.concat(first, second), 20, 0, 0, 255)[0])
        assertEquals(50, LookMath.apply(LookMath.concat(second, first), 20, 0, 0, 255)[0])
    }
}

class EditorLookTest {

    @Test
    fun anUntouchedLookIsTheIdentity() {
        val look = EditorLook()
        assertTrue(look.isIdentity)
        assertFalse(look.needsPixelPass)
        assertArrayEquals(LookMath.identity(), look.colorMatrix(), 1e-5f)
        assertTrue(EditorLook(FilterPreset.SEPIA, intensity = 0).isIdentity)
        assertFalse(EditorLook(FilterPreset.SEPIA).isIdentity)
    }

    @Test
    fun adjustmentsAreClampedAndZeroRemovesThem() {
        val look = EditorLook().with(LookAdjustment.EXPOSURE, 250).with(LookAdjustment.SHARPEN, -20)
        assertEquals(100, look.value(LookAdjustment.EXPOSURE))
        assertEquals(0, look.value(LookAdjustment.SHARPEN))
        assertFalse(LookAdjustment.SHARPEN in look.adjustments)
        assertEquals(EditorLook(), look.with(LookAdjustment.EXPOSURE, 0))
    }

    @Test
    fun theFourOriginalAdjustmentsKeepTheirFormerCurves() {
        // Brightness +50 adds half of 255, contrast keeps mid gray, saturation -100 is gray, temperature warms.
        assertEquals(255, LookMath.apply(EditorLook().with(LookAdjustment.BRIGHTNESS, 50).colorMatrix(), 200, 0, 0, 255)[0])
        assertEquals(128, LookMath.apply(EditorLook().with(LookAdjustment.CONTRAST, 60).colorMatrix(), 128, 128, 128, 255)[0])
        val gray = LookMath.apply(EditorLook().with(LookAdjustment.SATURATION, -100).colorMatrix(), 250, 10, 10, 255)
        assertEquals(gray[0], gray[2])
        val warm = LookMath.apply(EditorLook().with(LookAdjustment.TEMPERATURE, 100).colorMatrix(), 100, 100, 100, 255)
        assertTrue(warm[0] > 100 && warm[2] < 100)
    }

    @Test
    fun exposureDoublesAtTwoThirdsAndHueKeepsGrayAndTurnsRed() {
        val brighter = LookMath.apply(EditorLook().with(LookAdjustment.EXPOSURE, 67).colorMatrix(), 60, 60, 60, 255)
        assertEquals(120f, brighter[0].toFloat(), 2f)
        val gray = LookMath.apply(EditorLook().with(LookAdjustment.HUE, 40).colorMatrix(), 90, 90, 90, 255)
        assertArrayEquals(intArrayOf(90, 90, 90, 255), gray)
        val red = LookMath.apply(EditorLook().with(LookAdjustment.HUE, 67).colorMatrix(), 220, 30, 30, 255)
        assertTrue("about 120 degrees turns red to green: ${red.toList()}", red[1] > red[0] && red[1] > red[2])
    }

    @Test
    fun theToneCurveKeepsBlackWhiteAndOrderForEveryValue() {
        assertArrayEquals(IntArray(256) { it }, LookMath.toneCurve(0, 0))
        for (shadows in -100..100 step 25) {
            for (highlights in -100..100 step 25) {
                val curve = LookMath.toneCurve(shadows, highlights)
                assertEquals(0, curve[0])
                assertEquals(255, curve[255])
                for (i in 1..255) assertTrue("s=$shadows h=$highlights at $i", curve[i] >= curve[i - 1])
            }
        }
        assertTrue(LookMath.toneCurve(100, 0)[64] > 64 + 20)
        assertTrue(LookMath.toneCurve(0, -100)[192] < 192 - 20)
    }

    @Test
    fun theVignetteDarkensOnlyTowardsTheCorners() {
        assertEquals(1f, LookMath.vignette(50f, 50f, 100, 100, 100), 1e-6f)
        assertEquals(0.4f, LookMath.vignette(0f, 0f, 100, 100, 100), 1e-3f)
        assertEquals(1.6f, LookMath.vignette(100f, 100f, 100, 100, -100), 1e-3f)
        assertEquals(1f, LookMath.vignette(0f, 0f, 100, 100, 0), 0f)
    }

    @Test
    fun thePixelPassMatchesTheMatrixAndSharpensEdges() {
        val width = 6
        val height = 4
        val pixels = IntArray(width * height) { i -> if (i % width < 3) 0xFF404040.toInt() else 0xFFC0C0C0.toInt() }
        val plain = EditorLook(FilterPreset.SEPIA)
        val out = IntArray(pixels.size)
        LookPixelPass(plain, width, height).process(pixels, 0, 0, height, out)
        val expected = LookMath.apply(FilterPreset.SEPIA.matrix, 0x40, 0x40, 0x40, 255)
        assertEquals(expected[0], out[0] shr 16 and 0xFF)
        assertEquals(expected[2], out[0] and 0xFF)

        val sharp = EditorLook().with(LookAdjustment.SHARPEN, 100)
        val sharpened = IntArray(pixels.size)
        LookPixelPass(sharp, width, height).process(pixels, 0, 0, height, sharpened)
        // Next to the edge the dark side gets darker and the light side lighter; flat areas stay.
        assertTrue((sharpened[2] and 0xFF) < 0x40)
        assertTrue((sharpened[3] and 0xFF) > 0xC0)
        assertEquals(0x40, sharpened[0] and 0xFF)
    }

    @Test
    fun bandsGiveTheSameResultAsOnePass() {
        val width = 7
        val height = 9
        val pixels = IntArray(width * height) { i -> (0xFF shl 24) or ((i * 37 % 256) shl 16) or ((i * 11 % 256) shl 8) or (i * 53 % 256) }
        val look = EditorLook(FilterPreset.FILM, 70).with(LookAdjustment.SHARPEN, 60).with(LookAdjustment.SHADOWS, 40).with(LookAdjustment.VIGNETTE, 50)
        val whole = IntArray(pixels.size)
        LookPixelPass(look, width, height).process(pixels, 0, 0, height, whole)
        val pass = LookPixelPass(look, width, height)
        val banded = IntArray(pixels.size)
        var top = 0
        while (top < height) {
            val bottom = minOf(height, top + 2)
            val inTop = maxOf(0, top - pass.halo)
            val inBottom = minOf(height, bottom + pass.halo)
            val input = pixels.copyOfRange(inTop * width, inBottom * width)
            val band = IntArray((bottom - top) * width)
            pass.process(input, inTop, top, bottom, band)
            band.copyInto(banded, top * width)
            top = bottom
        }
        assertArrayEquals(whole, banded)
        assertNotEquals(pixels.toList(), whole.toList())
    }
}
