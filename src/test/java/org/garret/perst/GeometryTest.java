package org.garret.perst;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Phase 2A: Tests for pure-Java geometry value classes:
 * Rectangle, RectangleR2, RectangleRn, PointRn
 * These classes have no storage dependency.
 */
class GeometryTest {

    // ===== Rectangle (int coords) =====

    @Test
    @DisplayName("Rectangle: construction and getters")
    void testRectangleConstructionAndGetters() {
        Rectangle r = new Rectangle(10, 20, 30, 40);
        assertEquals(10, r.getTop());
        assertEquals(20, r.getLeft());
        assertEquals(30, r.getBottom());
        assertEquals(40, r.getRight());

        // Copy constructor
        Rectangle copy = new Rectangle(r);
        assertEquals(r.getTop(), copy.getTop());
        assertEquals(r.getLeft(), copy.getLeft());
        assertEquals(r.getBottom(), copy.getBottom());
        assertEquals(r.getRight(), copy.getRight());

        // Default constructor
        Rectangle empty = new Rectangle();
        assertNotNull(empty);
    }

    @Test
    @DisplayName("Rectangle: area()")
    void testRectangleArea() {
        Rectangle r = new Rectangle(0, 0, 10, 20);
        assertEquals(200L, r.area()); // width=20, height=10

        Rectangle unit = new Rectangle(0, 0, 1, 1);
        assertEquals(1L, unit.area());

        // Degenerate (zero-area)
        Rectangle line = new Rectangle(5, 5, 5, 15);
        assertEquals(0L, line.area());
    }

    @Test
    @DisplayName("Rectangle: static joinArea(a, b)")
    void testRectangleJoinArea() {
        Rectangle a = new Rectangle(0, 0, 10, 10);
        Rectangle b = new Rectangle(5, 5, 15, 15);
        long ja = Rectangle.joinArea(a, b);
        // bounding box is (0,0,15,15) → area=225
        assertEquals(225L, ja);

        // Adjacent rectangles
        Rectangle c = new Rectangle(0, 0, 10, 10);
        Rectangle d = new Rectangle(0, 10, 10, 20);
        assertEquals(200L, Rectangle.joinArea(c, d));
    }

    @Test
    @DisplayName("Rectangle: distance(x, y)")
    void testRectangleDistance() {
        Rectangle r = new Rectangle(0, 0, 10, 10); // top=0,left=0,bottom=10,right=10

        // Point inside → distance 0
        assertEquals(0.0, r.distance(5, 5), 1e-9);

        // Point on edge → distance 0
        assertEquals(0.0, r.distance(0, 5), 1e-9);
        assertEquals(0.0, r.distance(10, 5), 1e-9);

        // Point outside (to the right)
        double d = r.distance(5, 15);
        assertTrue(d > 0, "Outside point should have positive distance");
        assertEquals(5.0, d, 1e-9);

        // Point diagonally outside
        double diagonal = r.distance(0, 0); // corner itself
        assertEquals(0.0, diagonal, 1e-9);
    }

    @Test
    @DisplayName("Rectangle: clone()")
    void testRectangleClone() {
        Rectangle r = new Rectangle(1, 2, 3, 4);
        Rectangle cloned = (Rectangle) r.clone();
        assertNotNull(cloned);
        assertEquals(r.getTop(), cloned.getTop());
        assertEquals(r.getLeft(), cloned.getLeft());
        assertEquals(r.getBottom(), cloned.getBottom());
        assertEquals(r.getRight(), cloned.getRight());
        assertNotSame(r, cloned);
    }

    @Test
    @DisplayName("Rectangle: join(r) mutates receiver, static join(a,b) creates new")
    void testRectangleJoin() {
        Rectangle r = new Rectangle(0, 0, 10, 10);
        Rectangle other = new Rectangle(5, 5, 20, 20);
        r.join(other);
        // Should be bounding box
        assertEquals(0, r.getTop());
        assertEquals(0, r.getLeft());
        assertEquals(20, r.getBottom());
        assertEquals(20, r.getRight());

        // Static join
        Rectangle a = new Rectangle(0, 0, 5, 5);
        Rectangle b = new Rectangle(3, 3, 8, 8);
        Rectangle joined = Rectangle.join(a, b);
        assertEquals(0, joined.getTop());
        assertEquals(0, joined.getLeft());
        assertEquals(8, joined.getBottom());
        assertEquals(8, joined.getRight());
    }

    @Test
    @DisplayName("Rectangle: intersects(r)")
    void testRectangleIntersects() {
        Rectangle r = new Rectangle(0, 0, 10, 10);

        // Fully overlapping
        assertTrue(r.intersects(new Rectangle(2, 2, 8, 8)));

        // Partially overlapping
        assertTrue(r.intersects(new Rectangle(5, 5, 15, 15)));

        // Touching edge (degenerate)
        assertTrue(r.intersects(new Rectangle(10, 0, 20, 10)));

        // Non-overlapping
        assertFalse(r.intersects(new Rectangle(11, 0, 20, 10)));
        assertFalse(r.intersects(new Rectangle(0, 11, 10, 20)));
    }

    @Test
    @DisplayName("Rectangle: contains(r)")
    void testRectangleContains() {
        Rectangle outer = new Rectangle(0, 0, 20, 20);
        Rectangle inner = new Rectangle(5, 5, 15, 15);
        Rectangle partial = new Rectangle(10, 10, 25, 25);

        assertTrue(outer.contains(inner));
        assertFalse(outer.contains(partial));
        // Self-contains
        assertTrue(outer.contains(new Rectangle(0, 0, 20, 20)));
    }

    @Test
    @DisplayName("Rectangle: equals, hashCode, toString")
    void testRectangleEqualsHashCodeToString() {
        Rectangle a = new Rectangle(1, 2, 3, 4);
        Rectangle b = new Rectangle(1, 2, 3, 4);
        Rectangle c = new Rectangle(9, 9, 9, 9);

        assertEquals(a, b);
        assertNotEquals(a, c);
        assertNotEquals(a, null);
        assertNotEquals(a, "not-a-rectangle");
        assertEquals(a.hashCode(), b.hashCode());

        String s = a.toString();
        assertNotNull(s);
        assertFalse(s.isEmpty());
    }

    // ===== RectangleR2 (double coords) =====

    @Test
    @DisplayName("RectangleR2: construction, getters, area")
    void testRectangleR2Basic() {
        RectangleR2 r = new RectangleR2(0.0, 0.0, 5.0, 10.0);
        assertEquals(0.0, r.getTop(), 1e-9);
        assertEquals(0.0, r.getLeft(), 1e-9);
        assertEquals(5.0, r.getBottom(), 1e-9);
        assertEquals(10.0, r.getRight(), 1e-9);
        assertEquals(50.0, r.area(), 1e-9); // 10 * 5

        // Copy constructor
        RectangleR2 copy = new RectangleR2(r);
        assertEquals(r.getTop(), copy.getTop(), 1e-9);

        // Default constructor
        RectangleR2 empty = new RectangleR2();
        assertNotNull(empty);
    }

    @Test
    @DisplayName("RectangleR2: static joinArea, join, intersects, contains")
    void testRectangleR2Operations() {
        RectangleR2 a = new RectangleR2(0.0, 0.0, 5.0, 5.0);
        RectangleR2 b = new RectangleR2(3.0, 3.0, 8.0, 8.0);

        double ja = RectangleR2.joinArea(a, b);
        assertEquals(64.0, ja, 1e-9); // bounding box 8x8

        // join instances
        RectangleR2 joined = RectangleR2.join(a, b);
        assertEquals(0.0, joined.getTop(), 1e-9);
        assertEquals(0.0, joined.getLeft(), 1e-9);
        assertEquals(8.0, joined.getBottom(), 1e-9);
        assertEquals(8.0, joined.getRight(), 1e-9);

        // intersects
        assertTrue(a.intersects(b));
        assertFalse(a.intersects(new RectangleR2(6.0, 6.0, 9.0, 9.0)));

        // contains
        RectangleR2 outer = new RectangleR2(0.0, 0.0, 20.0, 20.0);
        assertTrue(outer.contains(a));
        assertFalse(a.contains(outer));
    }

    @Test
    @DisplayName("RectangleR2: distance, clone, equals, hashCode, toString")
    void testRectangleR2Misc() {
        RectangleR2 r = new RectangleR2(0.0, 0.0, 10.0, 10.0);

        // Inside point → 0
        assertEquals(0.0, r.distance(5.0, 5.0), 1e-9);

        // Outside point → positive
        assertTrue(r.distance(15.0, 5.0) > 0);

        // Clone
        RectangleR2 cloned = (RectangleR2) r.clone();
        assertNotSame(r, cloned);
        assertEquals(r.getTop(), cloned.getTop(), 1e-9);

        // Mutating join
        RectangleR2 m = new RectangleR2(0.0, 0.0, 5.0, 5.0);
        m.join(new RectangleR2(3.0, 3.0, 8.0, 8.0));
        assertEquals(8.0, m.getBottom(), 1e-9);

        // equals / hashCode / toString
        RectangleR2 x = new RectangleR2(1.0, 2.0, 3.0, 4.0);
        RectangleR2 y = new RectangleR2(1.0, 2.0, 3.0, 4.0);
        assertEquals(x, y);
        assertEquals(x.hashCode(), y.hashCode());
        assertNotNull(x.toString());
        assertNotEquals(x, new RectangleR2(9.0, 9.0, 9.0, 9.0));
        assertNotEquals(x, null);
    }

    // ===== RectangleRn (n-dimensional) =====

    @Test
    @DisplayName("RectangleRn: 2D via double[] coords")
    void testRectangleRn2D() {
        // coords layout: [min0, min1, ..., max0, max1, ...] — all mins first, all maxes second
        double[] coords = {0.0, 0.0, 5.0, 10.0}; // 2D: x in [0,5], y in [0,10]
        RectangleRn r = new RectangleRn(coords);

        assertEquals(2, r.nDimensions());
        assertEquals(0.0, r.getMinCoord(0), 1e-9);
        assertEquals(5.0, r.getMaxCoord(0), 1e-9);
        assertEquals(0.0, r.getMinCoord(1), 1e-9);
        assertEquals(10.0, r.getMaxCoord(1), 1e-9);
        assertEquals(50.0, r.area(), 1e-9); // 5 * 10
    }

    @Test
    @DisplayName("RectangleRn: 3D construction from PointRn min/max")
    void testRectangleRn3D() {
        PointRn min = new PointRn(new double[]{0.0, 0.0, 0.0});
        PointRn max = new PointRn(new double[]{2.0, 3.0, 4.0});
        RectangleRn r = new RectangleRn(min, max);

        assertEquals(3, r.nDimensions());
        assertEquals(0.0, r.getMinCoord(0), 1e-9);
        assertEquals(2.0, r.getMaxCoord(0), 1e-9);
        assertEquals(24.0, r.area(), 1e-9); // 2*3*4
    }

    @Test
    @DisplayName("RectangleRn: copy constructor, clone, equals, hashCode, toString")
    void testRectangleRnMisc() {
        double[] coords = {1.0, 3.0, 2.0, 6.0};
        RectangleRn r = new RectangleRn(coords);

        // Copy constructor
        RectangleRn copy = new RectangleRn(r);
        assertEquals(r.nDimensions(), copy.nDimensions());
        assertEquals(r.getMinCoord(0), copy.getMinCoord(0), 1e-9);

        // clone
        RectangleRn cloned = (RectangleRn) r.clone();
        assertNotSame(r, cloned);
        assertEquals(r.area(), cloned.area(), 1e-9);

        // equals / hashCode / toString
        RectangleRn a = new RectangleRn(new double[]{0.0, 1.0, 0.0, 1.0});
        RectangleRn b = new RectangleRn(new double[]{0.0, 1.0, 0.0, 1.0});
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotNull(a.toString());
        assertNotEquals(a, null);
        assertNotEquals(a, "other");
    }

    @Test
    @DisplayName("RectangleRn: static joinArea, join instance and static")
    void testRectangleRnJoin() {
        // layout: [min0, min1, ..., max0, max1, ...]
        RectangleRn a = new RectangleRn(new double[]{0.0, 0.0, 5.0, 5.0}); // [0,5]x[0,5]
        RectangleRn b = new RectangleRn(new double[]{3.0, 3.0, 8.0, 8.0}); // [3,8]x[3,8]

        double ja = RectangleRn.joinArea(a, b);
        assertEquals(64.0, ja, 1e-9); // bounding box [0,8]x[0,8] = 64

        RectangleRn joined = RectangleRn.join(a, b);
        assertEquals(0.0, joined.getMinCoord(0), 1e-9);
        assertEquals(8.0, joined.getMaxCoord(0), 1e-9);

        // Mutating join
        RectangleRn m = new RectangleRn(new double[]{0.0, 0.0, 3.0, 3.0}); // [0,3]x[0,3]
        m.join(new RectangleRn(new double[]{2.0, 2.0, 6.0, 6.0}));         // [2,6]x[2,6]
        assertEquals(0.0, m.getMinCoord(0), 1e-9);
        assertEquals(6.0, m.getMaxCoord(0), 1e-9);
    }

    @Test
    @DisplayName("RectangleRn: intersects, contains")
    void testRectangleRnIntersectsContains() {
        RectangleRn outer   = new RectangleRn(new double[]{0.0,  0.0,  10.0, 10.0}); // [0,10]x[0,10]
        RectangleRn inner   = new RectangleRn(new double[]{2.0,  2.0,   8.0,  8.0}); // [2,8]x[2,8]
        RectangleRn partial = new RectangleRn(new double[]{5.0,  5.0,  15.0, 15.0}); // [5,15]x[5,15]
        RectangleRn disjoint= new RectangleRn(new double[]{12.0, 12.0, 15.0, 15.0}); // [12,15]x[12,15]

        assertTrue(outer.contains(inner));
        assertFalse(outer.contains(partial));
        assertTrue(outer.intersects(inner));
        assertTrue(outer.intersects(partial));
        assertFalse(outer.intersects(disjoint));
    }

    @Test
    @DisplayName("RectangleRn: distance(PointRn)")
    void testRectangleRnDistance() {
        RectangleRn r = new RectangleRn(new double[]{0.0, 0.0, 5.0, 5.0}); // [0,5]x[0,5]

        // Point inside → 0
        PointRn inside = new PointRn(new double[]{2.0, 3.0});
        assertEquals(0.0, r.distance(inside), 1e-9);

        // Point outside
        PointRn outside = new PointRn(new double[]{7.0, 3.0});
        assertTrue(r.distance(outside) > 0, "Distance to outside point should be > 0");
    }

    // ===== PointRn =====

    @Test
    @DisplayName("PointRn: construction, getCoord, toString")
    void testPointRn() {
        double[] coords = {1.0, 2.0, 3.0};
        PointRn p = new PointRn(coords);

        assertEquals(1.0, p.getCoord(0), 1e-9);
        assertEquals(2.0, p.getCoord(1), 1e-9);
        assertEquals(3.0, p.getCoord(2), 1e-9);

        String s = p.toString();
        assertNotNull(s);
        assertFalse(s.isEmpty());
    }
}
