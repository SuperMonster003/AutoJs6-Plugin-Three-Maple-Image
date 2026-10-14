package io.github.supermonster003.autojs6.plugin.three.maple.image

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EditorViewportTest {

    private fun viewport(viewWidth: Float = 1000f, viewHeight: Float = 2000f, imageWidth: Int = 4000, imageHeight: Int = 3000) =
        EditorViewport().apply {
            setView(viewWidth, viewHeight)
            setImage(imageWidth, imageHeight)
        }

    @Test fun atZoomOneTheImageIsFittedAndCentered() {
        val viewport = viewport()
        assertEquals(0.25f, viewport.scale, 1e-6f)
        assertEquals(0f, viewport.left, 1e-3f)
        assertEquals(625f, viewport.top, 1e-3f)
        assertEquals(1000f, viewport.right, 1e-3f)
        assertEquals(1375f, viewport.bottom, 1e-3f)
        assertFalse(viewport.isZoomed)
    }

    @Test fun zoomingKeepsTheFocusedImagePointUnderTheFingers() {
        val viewport = viewport()
        val imageX = viewport.toImageX(300f)
        val imageY = viewport.toImageY(1000f)
        viewport.zoomBy(3f, 300f, 1000f)
        assertEquals(3f, viewport.zoom, 1e-6f)
        assertEquals(300f, viewport.toViewX(imageX), 1e-2f)
        assertEquals(1000f, viewport.toViewY(imageY), 1e-2f)
        assertTrue(viewport.isZoomed)
        // Near the top edge the focus moves instead of uncovering the view: the 2250 px high image keeps y <= 0.
        viewport.zoomBy(1f, 300f, 100f)
        assertEquals(0f, viewport.top.coerceAtLeast(0f), 1e-3f)
    }

    @Test fun zoomStaysBetweenFitAndTheMaximum() {
        val viewport = viewport()
        viewport.zoomBy(0.2f, 500f, 1000f)
        assertEquals(1f, viewport.zoom, 1e-6f)
        viewport.zoomBy(1000f, 500f, 1000f)
        // 8 view pixels per image pixel: 8 / 0.25.
        assertEquals(32f, viewport.zoom, 1e-4f)
        assertEquals(8f, viewport.scale, 1e-4f)
        // A small image may still zoom 4 times.
        assertEquals(EditorViewport.MIN_MAX_ZOOM, viewport(imageWidth = 100, imageHeight = 100).maxZoom, 1e-6f)
    }

    @Test fun panningNeverUncoversAnEdgeTheImageCanCover() {
        val viewport = viewport()
        viewport.zoomBy(2f, 500f, 1000f)
        viewport.panBy(10_000f, 10_000f)
        // 2000 x 1500 in 1000 x 2000: x may move until the left edge touches the view, y stays centered.
        assertEquals(0f, viewport.left, 1e-3f)
        assertEquals(250f, viewport.top, 1e-3f)
        viewport.panBy(-10_000f, 0f)
        assertEquals(1000f, viewport.right, 1e-3f)
    }

    @Test fun mappingRoundTripsAfterZoomAndPan() {
        val viewport = viewport()
        viewport.zoomBy(3f, 200f, 800f)
        viewport.panBy(-120f, 35f)
        listOf(0f to 0f, 1234.5f to 2222f, 4000f to 3000f).forEach { (x, y) ->
            assertEquals(x, viewport.toImageX(viewport.toViewX(x)), 1e-2f)
            assertEquals(y, viewport.toImageY(viewport.toViewY(y)), 1e-2f)
        }
    }

    @Test fun doubleTapZoomsInAndThenBackToFit() {
        val viewport = viewport()
        viewport.toggleZoom(500f, 1000f)
        assertEquals(EditorViewport.DOUBLE_TAP_ZOOM, viewport.zoom, 1e-6f)
        viewport.toggleZoom(500f, 1000f)
        assertEquals(1f, viewport.zoom, 1e-6f)
        assertEquals(0f, viewport.left, 1e-3f)
    }

    @Test fun anImageOfTheSameSizeKeepsTheZoomAndANewSizeResetsIt() {
        val viewport = viewport()
        viewport.zoomBy(2f, 100f, 700f)
        val left = viewport.left
        viewport.setImage(4000, 3000)
        assertEquals(2f, viewport.zoom, 1e-6f)
        assertEquals(left, viewport.left, 1e-3f)
        viewport.setImage(2000, 1500)
        assertEquals(1f, viewport.zoom, 1e-6f)
    }

    @Test fun aSmallerViewClampsThePanAgain() {
        val viewport = viewport()
        viewport.zoomBy(4f, 0f, 1000f)
        viewport.setView(1000f, 1000f)
        assertTrue(viewport.left <= 0f && viewport.right >= 1000f)
        assertTrue(viewport.top <= 0f && viewport.bottom >= 1000f)
    }

    @Test fun anEmptyViewOrImageDoesNotDivideByZero() {
        val viewport = EditorViewport()
        viewport.zoomBy(2f, 10f, 10f)
        viewport.panBy(5f, 5f)
        assertEquals(1f, viewport.zoom, 1e-6f)
        assertEquals(0f, viewport.scale, 1e-6f)
    }
}
