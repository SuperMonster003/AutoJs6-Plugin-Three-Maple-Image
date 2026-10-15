package io.github.supermonster003.autojs6.plugin.three.maple.image

import io.github.supermonster003.autojs6.plugin.three.maple.image.ViewerDoubleTapZoom.ACTUAL_PIXELS
import io.github.supermonster003.autojs6.plugin.three.maple.image.ViewerDoubleTapZoom.TWO_AND_HALF
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

/** ROADMAP 9.2: the zoom levels and the optional gestures of the viewer; the defaults keep the 3.0.0 behavior. */
class ViewerGesturePolicyTest {

    @Test fun theDefaultsAreTheBehaviorBeforeTheSettings() {
        val defaults = ViewerSettings()
        assertTrue(defaults.rotationGesture && defaults.rotationSave && defaults.oneFingerZoom)
        assertFalse(defaults.edgeTapPaging || defaults.swipeDownToClose || defaults.brightnessSwipe)
        assertEquals(TWO_AND_HALF, defaults.doubleTapZoom)
        assertEquals(ViewerMaxZoom.FIVE, defaults.maxZoom)
        assertEquals(ImageZoomState.MAX_ZOOM, ImageZoomState.maximumZoom(defaults.maxZoom, defaults.doubleTapZoom, 8f), 0f)
        assertEquals(ImageZoomState.DOUBLE_TAP_ZOOM, ImageZoomState.doubleTapTarget(1f, defaults.doubleTapZoom, 8f, 5f), 0f)
    }

    @Test fun unknownStoredNamesFallBackToTheDefault() {
        assertEquals(ViewerMaxZoom.TEN, ViewerSettings.enumValue("TEN", ViewerMaxZoom.FIVE))
        assertEquals(ViewerMaxZoom.FIVE, ViewerSettings.enumValue("TWENTY", ViewerMaxZoom.FIVE))
        assertEquals(ACTUAL_PIXELS, ViewerSettings.enumValue(null, ACTUAL_PIXELS))
    }

    @Test fun actualPixelsIsTheImageWidthOverItsFittedWidth() {
        assertEquals(4f, ImageZoomState.actualPixelsZoom(4000, 1000f)!!, 0f)
        assertEquals(0.5f, ImageZoomState.actualPixelsZoom(500, 1000f)!!, 0f)
        assertNull(ImageZoomState.actualPixelsZoom(0, 1000f))
        assertNull(ImageZoomState.actualPixelsZoom(4000, 0f))
        assertNull(ImageZoomState.actualPixelsZoom(4000, Float.NaN))
    }

    @Test fun theMaximumFollowsTheSetting() {
        assertEquals(5f, ImageZoomState.maximumZoom(ViewerMaxZoom.FIVE, TWO_AND_HALF, 8f), 0f)
        assertEquals(10f, ImageZoomState.maximumZoom(ViewerMaxZoom.TEN, TWO_AND_HALF, 8f), 0f)
        assertEquals(8f, ImageZoomState.maximumZoom(ViewerMaxZoom.ACTUAL_PIXELS, TWO_AND_HALF, 8f), 0f)
        // A small image still zooms to the double tap level; an unknown size behaves like 5x.
        assertEquals(2.5f, ImageZoomState.maximumZoom(ViewerMaxZoom.ACTUAL_PIXELS, TWO_AND_HALF, 0.5f), 0f)
        assertEquals(5f, ImageZoomState.maximumZoom(ViewerMaxZoom.ACTUAL_PIXELS, TWO_AND_HALF, null), 0f)
        assertEquals(ImageZoomState.CEILING_ZOOM, ImageZoomState.maximumZoom(ViewerMaxZoom.ACTUAL_PIXELS, TWO_AND_HALF, 500f), 0f)
    }

    @Test fun aOneToOneDoubleTapIsNeverCutShortByTheMaximum() {
        assertEquals(8f, ImageZoomState.maximumZoom(ViewerMaxZoom.FIVE, ACTUAL_PIXELS, 8f), 0f)
        assertEquals(10f, ImageZoomState.maximumZoom(ViewerMaxZoom.TEN, ACTUAL_PIXELS, 3f), 0f)
        assertEquals(8f, ImageZoomState.doubleTapTarget(1f, ACTUAL_PIXELS, 8f, 8f), 0f)
        assertEquals(3f, ImageZoomState.doubleTapTarget(1f, ACTUAL_PIXELS, 3f, 10f), 0f)
    }

    @Test fun aOneToOneDoubleTapOnAnImageThatFitsAtOneToOneZoomsTo2_5() {
        assertEquals(2.5f, ImageZoomState.doubleTapTarget(1f, ACTUAL_PIXELS, 0.8f, 5f), 0f)
        assertEquals(2.5f, ImageZoomState.doubleTapTarget(1f, ACTUAL_PIXELS, null, 5f), 0f)
        // Zoomed in, a double tap always goes back to the fit.
        assertEquals(1f, ImageZoomState.doubleTapTarget(4f, ACTUAL_PIXELS, 8f, 8f), 0f)
    }

    @Test fun edgeTapsTurnPagesOnlyNearTheSides() {
        assertEquals(ZoomableImageView.DIRECTION_PREVIOUS, ViewerGesturePolicy.edgeTapDirection(10f, 1000))
        assertEquals(ZoomableImageView.DIRECTION_PREVIOUS, ViewerGesturePolicy.edgeTapDirection(199f, 1000))
        assertNull(ViewerGesturePolicy.edgeTapDirection(200f, 1000))
        assertNull(ViewerGesturePolicy.edgeTapDirection(500f, 1000))
        assertNull(ViewerGesturePolicy.edgeTapDirection(800f, 1000))
        assertEquals(ZoomableImageView.DIRECTION_NEXT, ViewerGesturePolicy.edgeTapDirection(801f, 1000))
        assertNull(ViewerGesturePolicy.edgeTapDirection(10f, 0))
    }

    @Test fun theBrightnessDragStartsOnTheLeftThirdAndMostlyVertically() {
        assertTrue(ViewerGesturePolicy.inBrightnessZone(100f, 900))
        assertFalse(ViewerGesturePolicy.inBrightnessZone(300f, 900))
        assertTrue(ViewerGesturePolicy.isVerticalDrag(distanceX = 5f, distanceY = -40f, touchSlop = 16f))
        assertFalse(ViewerGesturePolicy.isVerticalDrag(distanceX = 5f, distanceY = 10f, touchSlop = 16f))
        assertFalse(ViewerGesturePolicy.isVerticalDrag(distanceX = 40f, distanceY = 40f, touchSlop = 16f))
    }

    @Test fun draggingUpBrightensAndDownDimsWithinTheRange() {
        assertEquals(0.5f, ViewerGesturePolicy.brightness(0.5f, 0f, 1000), 0.0001f)
        assertEquals(1f, ViewerGesturePolicy.brightness(0.5f, -300f, 1000), 0.0001f)
        assertEquals(0.25f, ViewerGesturePolicy.brightness(0.5f, 150f, 1000), 0.0001f)
        assertEquals(ViewerGesturePolicy.MIN_BRIGHTNESS, ViewerGesturePolicy.brightness(0.5f, 900f, 1000), 0f)
        assertEquals(1f, ViewerGesturePolicy.brightness(2f, 0f, 0), 0f)
    }

    @Test fun onlyAQuickMostlyDownwardSwipeCloses() {
        assertTrue(ViewerGesturePolicy.isSwipeDown(10f, 200f, 1500f, minDistance = 150f, minVelocity = 400f))
        assertFalse("too short", ViewerGesturePolicy.isSwipeDown(10f, 100f, 1500f, 150f, 400f))
        assertFalse("too slow", ViewerGesturePolicy.isSwipeDown(10f, 200f, 300f, 150f, 400f))
        assertFalse("upward", ViewerGesturePolicy.isSwipeDown(10f, -200f, -1500f, 150f, 400f))
        assertFalse("diagonal", ViewerGesturePolicy.isSwipeDown(180f, 200f, 1500f, 150f, 400f))
    }

    @Test fun theZoomBadgeGoesAsFarAsTheMaximum() {
        assertNull(ImageZoomIndicatorFormatter.format(5.01f, Locale.US))
        assertEquals("8.5", ImageZoomIndicatorFormatter.format(8.5f, Locale.US, maxZoom = 10f))
        assertNull(ImageZoomIndicatorFormatter.format(10.5f, Locale.US, maxZoom = 10f))
    }
}
