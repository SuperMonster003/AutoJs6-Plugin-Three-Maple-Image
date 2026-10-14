package io.github.supermonster003.autojs6.plugin.three.maple.image

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.atan2
import kotlin.math.hypot

class ShapeGeometryTest {

    private fun shape(kind: ShapeKind, startX: Float, startY: Float, endX: Float, endY: Float, width: Float = 4f) =
        EditorShape(kind, startX, startY, endX, endY, color = 0xFFFF0000.toInt(), strokeWidth = width)

    @Test fun boxesHaveFourCornerHandlesAndLinesTwoEnds() {
        val box = shape(ShapeKind.RECTANGLE, 10f, 20f, 110f, 70f)
        assertEquals(
            listOf(ShapePoint(10f, 20f), ShapePoint(110f, 20f), ShapePoint(110f, 70f), ShapePoint(10f, 70f)),
            ShapeGeometry.handles(box),
        )
        assertEquals(2, ShapeGeometry.handles(shape(ShapeKind.ARROW, 0f, 0f, 50f, 50f)).size)
    }

    @Test fun theNearestHandleWithinTheToleranceIsHit() {
        val box = shape(ShapeKind.ELLIPSE, 10f, 20f, 110f, 70f)
        assertEquals(2, ShapeGeometry.hitHandle(box, 106f, 66f, 8f))
        assertEquals(1, ShapeGeometry.hitHandle(box, 113f, 18f, 8f))
        assertNull(ShapeGeometry.hitHandle(box, 60f, 45f, 8f))
        // Two handles in reach: the nearer one wins.
        val tiny = shape(ShapeKind.LINE, 0f, 0f, 10f, 0f)
        assertEquals(1, ShapeGeometry.hitHandle(tiny, 7f, 0f, 8f))
    }

    @Test fun boxesAreHitInsideAndLinesNearTheirSegment() {
        val box = shape(ShapeKind.RECTANGLE, 10f, 20f, 110f, 70f)
        assertTrue(ShapeGeometry.hitShape(box, 60f, 45f, 6f))
        assertTrue(ShapeGeometry.hitShape(box, 5f, 45f, 6f))
        assertFalse(ShapeGeometry.hitShape(box, 0f, 45f, 6f))
        val line = shape(ShapeKind.LINE, 0f, 0f, 100f, 100f, width = 10f)
        assertTrue(ShapeGeometry.hitShape(line, 50f, 60f, 4f))
        assertFalse(ShapeGeometry.hitShape(line, 50f, 70f, 4f))
        // Beyond the end only within the reach of the end point.
        assertFalse(ShapeGeometry.hitShape(line, 115f, 115f, 4f))
    }

    @Test fun theTopmostShapeWins() {
        val shapes = listOf(shape(ShapeKind.RECTANGLE, 0f, 0f, 100f, 100f), shape(ShapeKind.ELLIPSE, 40f, 40f, 80f, 80f))
        assertEquals(1, ShapeGeometry.topmostHit(shapes, 60f, 60f, 4f))
        assertEquals(0, ShapeGeometry.topmostHit(shapes, 10f, 10f, 4f))
        assertNull(ShapeGeometry.topmostHit(shapes, 200f, 200f, 4f))
    }

    @Test fun draggingAHandleMovesItsCornerOnly() {
        val box = shape(ShapeKind.RECTANGLE, 10f, 20f, 110f, 70f)
        val dragged = ShapeGeometry.withHandle(box, 3, 30f, 90f, minimumSize = 8f)
        assertEquals(30f, dragged.left, 0f)
        assertEquals(90f, dragged.bottom, 0f)
        assertEquals(110f, dragged.right, 0f)
        assertEquals(20f, dragged.top, 0f)
        // Crossing over is fine: the corners swap roles.
        val crossed = ShapeGeometry.withHandle(box, 2, 0f, 0f, minimumSize = 8f)
        assertEquals(0f, crossed.left, 0f)
        assertEquals(10f, crossed.right, 0f)
    }

    @Test fun aHandleCannotCollapseTheShapeBelowTheMinimum() {
        val box = shape(ShapeKind.RECTANGLE, 10f, 20f, 110f, 70f)
        assertSame(box, ShapeGeometry.withHandle(box, 2, 14f, 60f, minimumSize = 8f))
        val arrow = shape(ShapeKind.ARROW, 0f, 0f, 100f, 0f)
        assertSame(arrow, ShapeGeometry.withHandle(arrow, 1, 3f, 3f, minimumSize = 8f))
    }

    @Test fun aTapMakesNoShape() {
        assertTrue(ShapeGeometry.isTooSmall(shape(ShapeKind.LINE, 10f, 10f, 13f, 14f), 8f))
        assertFalse(ShapeGeometry.isTooSmall(shape(ShapeKind.LINE, 10f, 10f, 10f, 30f), 8f))
        // A box needs both sides: a flat one is a tap gone sideways.
        assertTrue(ShapeGeometry.isTooSmall(shape(ShapeKind.RECTANGLE, 10f, 10f, 100f, 12f), 8f))
        assertFalse(ShapeGeometry.isTooSmall(shape(ShapeKind.ELLIPSE, 10f, 10f, 100f, 20f), 8f))
    }

    @Test fun movingKeepsTheShapeInsideTheImage() {
        val box = shape(ShapeKind.RECTANGLE, 10f, 20f, 110f, 70f)
        val moved = ShapeGeometry.moved(box, 500f, -500f, 200f, 100f)
        assertEquals(200f, moved.right, 0f)
        assertEquals(0f, moved.top, 0f)
        assertEquals(100f, moved.right - moved.left, 0f)
        val inside = ShapeGeometry.moved(box, 5f, 5f, 200f, 100f)
        assertEquals(15f, inside.left, 0f)
        // A shape that already sticks out may move back in but not further out.
        val outside = shape(ShapeKind.LINE, -20f, 10f, 50f, 10f)
        assertEquals(-20f, ShapeGeometry.moved(outside, -10f, 0f, 200f, 100f).startX, 0f)
        assertEquals(-10f, ShapeGeometry.moved(outside, 10f, 0f, 200f, 100f).startX, 0f)
    }

    @Test fun theArrowHeadPointsAtTheEndWithBarbsAtTheConfiguredAngle() {
        val arrow = shape(ShapeKind.ARROW, 0f, 0f, 100f, 0f, width = 5f)
        val (tip, left, right) = ShapeGeometry.arrowHead(arrow)
        assertEquals(ShapePoint(100f, 0f), tip)
        assertEquals(20f, ShapeGeometry.arrowHeadLength(arrow), 1e-4f)
        assertEquals(20f, hypot(left.x - tip.x, left.y - tip.y), 1e-3f)
        assertEquals(20f, hypot(right.x - tip.x, right.y - tip.y), 1e-3f)
        val angle = Math.toDegrees(atan2((tip.y - left.y).toDouble(), (tip.x - left.x).toDouble()))
        assertEquals(ShapeGeometry.ARROW_HEAD_ANGLE_DEGREES, kotlin.math.abs(angle), 1e-3)
        // Symmetric around the shaft.
        assertEquals(left.x, right.x, 1e-3f)
        assertEquals(-left.y, right.y, 1e-3f)
        // The shaft stops inside the head.
        val shaftEnd = ShapeGeometry.arrowShaftEnd(arrow)
        assertTrue(shaftEnd.x < 100f && shaftEnd.x > left.x)
    }

    @Test fun aShortArrowKeepsItsHeadAtHalfTheLength() {
        val arrow = shape(ShapeKind.ARROW, 0f, 0f, 0f, 20f, width = 10f)
        assertEquals(10f, ShapeGeometry.arrowHeadLength(arrow), 1e-4f)
        val thin = shape(ShapeKind.ARROW, 0f, 0f, 0f, 200f, width = 0.5f)
        assertEquals(ShapeGeometry.MIN_ARROW_HEAD, ShapeGeometry.arrowHeadLength(thin), 1e-4f)
    }

    @Test fun onlyBoxesDrawFilled() {
        assertTrue(shape(ShapeKind.RECTANGLE, 0f, 0f, 10f, 10f).copy(filled = true).drawsFilled)
        assertFalse(shape(ShapeKind.ARROW, 0f, 0f, 10f, 10f).copy(filled = true).drawsFilled)
    }
}
