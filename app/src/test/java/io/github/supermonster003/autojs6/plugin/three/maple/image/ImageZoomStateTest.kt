package io.github.supermonster003.autojs6.plugin.three.maple.image

import org.junit.Assert.assertEquals
import org.junit.Test

class ImageZoomStateTest {

    @Test
    fun doubleTapZoomsInFromFittedState() {
        assertEquals(
            ImageZoomState.DOUBLE_TAP_ZOOM,
            ImageZoomState.doubleTapTarget(ImageZoomState.MIN_ZOOM),
            0f,
        )
        assertEquals(
            ImageZoomState.DOUBLE_TAP_ZOOM,
            ImageZoomState.doubleTapTarget(
                ImageZoomState.MIN_ZOOM + ImageZoomState.ZOOM_EPSILON,
            ),
            0f,
        )
    }

    @Test
    fun doubleTapReturnsEveryMagnifiedStateToFit() {
        assertEquals(
            ImageZoomState.MIN_ZOOM,
            ImageZoomState.doubleTapTarget(
                ImageZoomState.MIN_ZOOM + ImageZoomState.ZOOM_EPSILON * 2f,
            ),
            0f,
        )
        assertEquals(
            ImageZoomState.MIN_ZOOM,
            ImageZoomState.doubleTapTarget(ImageZoomState.DOUBLE_TAP_ZOOM),
            0f,
        )
        assertEquals(
            ImageZoomState.MIN_ZOOM,
            ImageZoomState.doubleTapTarget(ImageZoomState.MAX_ZOOM),
            0f,
        )
    }
}
