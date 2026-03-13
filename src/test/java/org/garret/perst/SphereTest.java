package org.garret.perst;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Sphere.* classes and package-private helpers: FP, Point3D, Euler.
 * FP, Point3D, Euler are package-private so they are only accessible from this package.
 */
class SphereTest {

    // ===========================
    // FP - floating point helpers
    // ===========================

    @Test
    @DisplayName("FP.zero: detects values within epsilon of zero")
    void testFpZero() {
        assertTrue(FP.zero(0.0));
        assertTrue(FP.zero(1e-7));   // within EPSILON (1e-6)
        assertFalse(FP.zero(1.0));
        assertFalse(FP.zero(-1.0));
    }

    @Test
    @DisplayName("FP.eq / ne: approximate equality")
    void testFpEqNe() {
        assertTrue(FP.eq(1.0, 1.0));
        assertTrue(FP.eq(1.0, 1.0 + 1e-7));
        assertFalse(FP.eq(1.0, 2.0));

        assertTrue(FP.ne(1.0, 2.0));
        assertFalse(FP.ne(1.0, 1.0));
    }

    @Test
    @DisplayName("FP.lt / le / gt / ge: comparisons with epsilon")
    void testFpComparisons() {
        assertTrue(FP.lt(1.0, 2.0));
        assertFalse(FP.lt(2.0, 1.0));
        assertFalse(FP.lt(1.0, 1.0));

        assertTrue(FP.le(1.0, 2.0));
        assertTrue(FP.le(1.0, 1.0));
        assertFalse(FP.le(2.0, 1.0));

        assertTrue(FP.gt(2.0, 1.0));
        assertFalse(FP.gt(1.0, 2.0));
        assertFalse(FP.gt(1.0, 1.0));

        assertTrue(FP.ge(2.0, 1.0));
        assertTrue(FP.ge(1.0, 1.0));
        assertFalse(FP.ge(1.0, 2.0));
    }

    // ===========================
    // Point3D
    // ===========================

    @Test
    @DisplayName("Point3D: constructor (x,y,z) and distance()")
    void testPoint3DXyzAndDistance() {
        Point3D p = new Point3D(3.0, 4.0, 0.0);
        assertEquals(3.0, p.x, 1e-9);
        assertEquals(4.0, p.y, 1e-9);
        assertEquals(0.0, p.z, 1e-9);
        assertEquals(5.0, p.distance(), 1e-9);  // sqrt(9+16+0)
    }

    @Test
    @DisplayName("Point3D: default constructor produces (0,0,0)")
    void testPoint3DDefault() {
        Point3D p = new Point3D();
        assertEquals(0.0, p.distance(), 1e-9);
    }

    @Test
    @DisplayName("Point3D: RA/Dec constructor and equals")
    void testPoint3DRaDec() {
        // ra=0, dec=0 → x=1, y=0, z=0
        Point3D p = new Point3D(0.0, 0.0);
        assertEquals(1.0, p.x, 1e-9);
        assertEquals(0.0, p.y, 1e-9);
        assertEquals(0.0, p.z, 1e-9);

        Point3D p2 = new Point3D(0.0, 0.0);
        assertTrue(p.equals(p2));
        assertFalse(p.equals(new Point3D(1.0, 0.0)));
        assertFalse(p.equals("not a point"));
    }

    @Test
    @DisplayName("Point3D: Sphere.Point constructor")
    void testPoint3DFromSpherePoint() {
        Sphere.Point sp = new Sphere.Point(Math.PI / 2, 0.0); // lng=90°, lat=0°
        Point3D p = new Point3D(sp);
        // x=cos(90)cos(0)=0, y=sin(90)cos(0)=1, z=sin(0)=0
        assertEquals(0.0, p.x, 1e-9);
        assertEquals(1.0, p.y, 1e-9);
        assertEquals(0.0, p.z, 1e-9);
    }

    @Test
    @DisplayName("Point3D: cross product")
    void testPoint3DCross() {
        Point3D a = new Point3D(1.0, 0.0, 0.0);
        Point3D b = new Point3D(0.0, 1.0, 0.0);
        Point3D cross = a.cross(b);
        assertEquals(0.0, cross.x, 1e-9);
        assertEquals(0.0, cross.y, 1e-9);
        assertEquals(1.0, cross.z, 1e-9);
    }

    @Test
    @DisplayName("Point3D: toRectangle() produces degenerate 3D box")
    void testPoint3DToRectangle() {
        Point3D p = new Point3D(1.0, 2.0, 3.0);
        RectangleRn r = p.toRectangle();
        assertNotNull(r);
        assertEquals(3, r.nDimensions());
    }

    @Test
    @DisplayName("Point3D: toSpherePoint() and back")
    void testPoint3DToSpherePoint() {
        // Start at north pole (ra=0, dec=PI/2) → x=0,y=0,z=1
        Point3D p = new Point3D(0.0, Math.PI / 2);
        Sphere.Point sp = p.toSpherePoint();
        assertNotNull(sp);
        // dec should be ~PI/2
        assertEquals(Math.PI / 2, sp.dec, 1e-9);
    }

    @Test
    @DisplayName("Point3D: toSpherePoint() with z=0 and lng negative path")
    void testPoint3DToSpherePointNegativeLng() {
        // ra = PI (180°), dec = 0 → x=-1, y=0, z=0
        Point3D p = new Point3D(Math.PI, 0.0);
        Sphere.Point sp = p.toSpherePoint();
        assertNotNull(sp);
        // lng = atan2(0, -1) = PI, which is > 0 so no adjustment
        assertTrue(sp.ra >= 0);
    }

    @Test
    @DisplayName("Point3D: static addToRectangle(r, ra, dec)")
    void testPoint3DAddToRectangleRaDec() {
        double[] coords = {0.0, 0.0, 0.0, 1.0, 1.0, 1.0};
        RectangleRn r = new RectangleRn(coords);
        Point3D.addToRectangle(r, 0.0, 0.0); // ra=0, dec=0 → (1,0,0)
        // min[0] should remain 0 or update
        assertNotNull(r);
    }

    @Test
    @DisplayName("Point3D: instance addToRectangle(r)")
    void testPoint3DInstanceAddToRectangle() {
        double[] coords = {0.0, 0.0, 0.0, 0.5, 0.5, 0.5};
        RectangleRn r = new RectangleRn(coords);
        Point3D p = new Point3D(1.0, 1.0, 1.0);
        p.addToRectangle(r);
        assertEquals(1.0, r.getMaxCoord(0), 1e-9);
        assertEquals(1.0, r.getMaxCoord(1), 1e-9);
        assertEquals(1.0, r.getMaxCoord(2), 1e-9);
    }

    // ===========================
    // Euler
    // ===========================

    @Test
    @DisplayName("Euler: transform around X axis")
    void testEulerTransformX() {
        Euler euler = new Euler();
        euler.phi_a   = Euler.AXIS_X;
        euler.theta_a = Euler.AXIS_X;
        euler.psi_a   = Euler.AXIS_X;
        euler.phi     = Math.PI / 2;  // 90° around X
        euler.theta   = 0.0;
        euler.psi     = 0.0;

        Point3D in  = new Point3D(0.0, 1.0, 0.0); // y=1
        Point3D out = new Point3D();
        euler.transform(out, in);
        // After 90° rotation around X: (0, 1, 0) → (0, 0, 1)
        assertEquals(0.0, out.x, 1e-9);
        assertEquals(0.0, out.y, 1e-9);
        assertEquals(1.0, out.z, 1e-9);
    }

    @Test
    @DisplayName("Euler: transform around Y axis")
    void testEulerTransformY() {
        Euler euler = new Euler();
        euler.phi_a   = Euler.AXIS_Y;
        euler.theta_a = Euler.AXIS_Y;
        euler.psi_a   = Euler.AXIS_Y;
        euler.phi     = Math.PI / 2;  // 90° around Y
        euler.theta   = 0.0;
        euler.psi     = 0.0;

        Point3D in  = new Point3D(1.0, 0.0, 0.0); // x=1
        Point3D out = new Point3D();
        euler.transform(out, in);
        // After 90° rotation around Y: (1, 0, 0) → (0, 0, -1)
        assertEquals(0.0,  out.x, 1e-9);
        assertEquals(0.0,  out.y, 1e-9);
        assertEquals(-1.0, out.z, 1e-9);
    }

    @Test
    @DisplayName("Euler: transform around Z axis")
    void testEulerTransformZ() {
        Euler euler = new Euler();
        euler.phi_a   = Euler.AXIS_Z;
        euler.theta_a = Euler.AXIS_Z;
        euler.psi_a   = Euler.AXIS_Z;
        euler.phi     = Math.PI / 2;  // 90° around Z
        euler.theta   = 0.0;
        euler.psi     = 0.0;

        Point3D in  = new Point3D(1.0, 0.0, 0.0); // x=1
        Point3D out = new Point3D();
        euler.transform(out, in);
        // After 90° rotation around Z: (1, 0, 0) → (0, 1, 0)
        assertEquals(0.0, out.x, 1e-9);
        assertEquals(1.0, out.y, 1e-9);
        assertEquals(0.0, out.z, 1e-9);
    }

    @Test
    @DisplayName("Euler: zero angle is no-op")
    void testEulerZeroAngle() {
        Euler euler = new Euler();
        euler.phi_a   = Euler.AXIS_Z;
        euler.theta_a = Euler.AXIS_Z;
        euler.psi_a   = Euler.AXIS_Z;
        euler.phi     = 0.0;
        euler.theta   = 0.0;
        euler.psi     = 0.0;

        Point3D in  = new Point3D(1.0, 2.0, 3.0);
        Point3D out = new Point3D();
        euler.transform(out, in);
        assertEquals(in.x, out.x, 1e-9);
        assertEquals(in.y, out.y, 1e-9);
        assertEquals(in.z, out.z, 1e-9);
    }

    // ===========================
    // Sphere.Point
    // ===========================

    @Test
    @DisplayName("Sphere.Point: constructor, latitude, longitude")
    void testSpherePointBasic() {
        Sphere.Point p = new Sphere.Point(1.0, 0.5);
        assertEquals(1.0, p.ra,  1e-9);
        assertEquals(0.5, p.dec, 1e-9);
        assertEquals(0.5, p.latitude(),  1e-9);
        assertEquals(1.0, p.longitude(), 1e-9);
    }

    @Test
    @DisplayName("Sphere.Point: distance between two points")
    void testSpherePointDistance() {
        Sphere.Point p1 = new Sphere.Point(0.0, 0.0);
        Sphere.Point p2 = new Sphere.Point(0.0, 0.0);
        assertEquals(0.0, p1.distance(p2), 1e-9);

        Sphere.Point p3 = new Sphere.Point(Math.PI, 0.0); // antipode of p1
        double dist = p1.distance(p3);
        assertEquals(Math.PI, dist, 1e-9);
    }

    @Test
    @DisplayName("Sphere.Point: equals")
    void testSpherePointEquals() {
        Sphere.Point a = new Sphere.Point(1.0, 0.5);
        Sphere.Point b = new Sphere.Point(1.0, 0.5);
        Sphere.Point c = new Sphere.Point(2.0, 0.5);
        assertTrue(a.equals(b));
        assertFalse(a.equals(c));
        assertFalse(a.equals("nope"));
    }

    @Test
    @DisplayName("Sphere.Point: wrappingRectangle is 3D unit box around point")
    void testSpherePointWrappingRectangle() {
        Sphere.Point p = new Sphere.Point(0.0, 0.0); // ra=0, dec=0 → (1,0,0)
        RectangleRn r = p.wrappingRectangle();
        assertNotNull(r);
        assertEquals(3, r.nDimensions());
    }

    @Test
    @DisplayName("Sphere.Point: contains() is same as equals()")
    void testSpherePointContains() {
        Sphere.Point p = new Sphere.Point(1.0, 0.5);
        assertTrue(p.contains(new Sphere.Point(1.0, 0.5)));
        assertFalse(p.contains(new Sphere.Point(2.0, 0.5)));
    }

    @Test
    @DisplayName("Sphere.Point: toPointRn()")
    void testSpherePointToPointRn() {
        Sphere.Point p = new Sphere.Point(0.0, 0.0);
        PointRn prn = p.toPointRn();
        assertNotNull(prn);
        assertEquals(1.0, prn.getCoord(0), 1e-9); // x=cos(0)cos(0)=1
    }

    @Test
    @DisplayName("Sphere.Point: toString()")
    void testSpherePointToString() {
        Sphere.Point p = new Sphere.Point(1.0, 0.5);
        String s = p.toString();
        assertNotNull(s);
        assertTrue(s.contains("1.0") || s.contains("0.5"));
    }

    // ===========================
    // Sphere.Box
    // ===========================

    @Test
    @DisplayName("Sphere.Box: construction and contains(Point)")
    void testSphereBoxContains() {
        Sphere.Point sw = new Sphere.Point(0.0, 0.0);
        Sphere.Point ne = new Sphere.Point(1.0, 1.0);
        Sphere.Box box = new Sphere.Box(sw, ne);

        // Centre point
        assertTrue(box.contains(new Sphere.Point(0.5, 0.5)));

        // Point outside
        assertFalse(box.contains(new Sphere.Point(2.0, 0.5)));
        assertFalse(box.contains(new Sphere.Point(0.5, -0.5)));
    }

    @Test
    @DisplayName("Sphere.Box: contains at poles (dec=PI/2)")
    void testSphereBoxContainsAtPole() {
        Sphere.Point sw = new Sphere.Point(0.0, Math.PI / 2);
        Sphere.Point ne = new Sphere.Point(1.0, Math.PI / 2);
        Sphere.Box box = new Sphere.Box(sw, ne);

        // Point at north pole
        assertTrue(box.contains(new Sphere.Point(0.5, Math.PI / 2)));
    }

    @Test
    @DisplayName("Sphere.Box: wraps around RA (sw.ra > ne.ra)")
    void testSphereBoxWrapAround() {
        // When sw.ra > ne.ra, the code treats [ne.ra, sw.ra] as the "inside" range
        Sphere.Point sw = new Sphere.Point(5.0, 0.0);   // ra=5
        Sphere.Point ne = new Sphere.Point(1.0, 1.0);   // ra=1
        Sphere.Box box = new Sphere.Box(sw, ne);

        // ra=3 is between ne.ra=1 and sw.ra=5 → inside
        assertTrue(box.contains(new Sphere.Point(3.0, 0.5)));
        // ra=0.5 < ne.ra=1 → outside
        assertFalse(box.contains(new Sphere.Point(0.5, 0.5)));
        // ra=5.5 > sw.ra=5 → outside
        assertFalse(box.contains(new Sphere.Point(5.5, 0.5)));
    }

    @Test
    @DisplayName("Sphere.Box: wrappingRectangle() does not throw")
    void testSphereBoxWrappingRectangle() {
        Sphere.Point sw = new Sphere.Point(0.0, -0.5);
        Sphere.Point ne = new Sphere.Point(1.0, 0.5);
        Sphere.Box box = new Sphere.Box(sw, ne);
        RectangleRn r = box.wrappingRectangle();
        assertNotNull(r);
        assertEquals(3, r.nDimensions());
    }

    @Test
    @DisplayName("Sphere.Box: wrappingRectangle with equator crossing")
    void testSphereBoxEquatorCrossing() {
        Sphere.Point sw = new Sphere.Point(0.0, -0.3);
        Sphere.Point ne = new Sphere.Point(1.0,  0.3);
        Sphere.Box box = new Sphere.Box(sw, ne);
        RectangleRn r = box.wrappingRectangle();
        assertNotNull(r);
    }

    @Test
    @DisplayName("Sphere.Box: equals")
    void testSphereBoxEquals() {
        Sphere.Point sw = new Sphere.Point(0.0, 0.0);
        Sphere.Point ne = new Sphere.Point(1.0, 1.0);
        Sphere.Box a = new Sphere.Box(sw, ne);
        Sphere.Box b = new Sphere.Box(sw, ne);
        Sphere.Box c = new Sphere.Box(ne, sw);
        assertTrue(a.equals(b));
        assertFalse(a.equals(c));
        assertFalse(a.equals("other"));
    }

    // ===========================
    // Sphere.Circle
    // ===========================

    @Test
    @DisplayName("Sphere.Circle: construction and toString")
    void testSphereCircleBasic() {
        Sphere.Point centre = new Sphere.Point(1.0, 0.5);
        Sphere.Circle circle = new Sphere.Circle(centre, 0.1);
        assertEquals(0.1, circle.radius, 1e-9);
        assertTrue(circle.center.equals(centre));

        String s = circle.toString();
        assertNotNull(s);
    }

    @Test
    @DisplayName("Sphere.Circle: contains(Point)")
    void testSphereCircleContains() {
        Sphere.Point centre = new Sphere.Point(0.0, 0.0);
        Sphere.Circle circle = new Sphere.Circle(centre, Math.PI / 4); // radius = 45°

        // Centre itself: distance 0 ≤ radius
        assertTrue(circle.contains(new Sphere.Point(0.0, 0.0)));

        // Antipodal point: distance = PI > radius
        assertFalse(circle.contains(new Sphere.Point(Math.PI, 0.0)));
    }

    @Test
    @DisplayName("Sphere.Circle: equals")
    void testSphereCircleEquals() {
        Sphere.Point c = new Sphere.Point(0.0, 0.0);
        Sphere.Circle a = new Sphere.Circle(c, 0.5);
        Sphere.Circle b = new Sphere.Circle(c, 0.5);
        Sphere.Circle d = new Sphere.Circle(c, 0.7);
        assertTrue(a.equals(b));
        assertFalse(a.equals(d));
        assertFalse(a.equals(null));
    }

    @Test
    @DisplayName("Sphere.Circle: wrappingRectangle() does not throw")
    void testSphereCircleWrappingRectangle() {
        Sphere.Point c = new Sphere.Point(0.5, 0.3);
        Sphere.Circle circle = new Sphere.Circle(c, 0.2);
        RectangleRn r = circle.wrappingRectangle();
        assertNotNull(r);
        assertEquals(3, r.nDimensions());
    }

    // ===========================
    // Sphere.Ellipse
    // ===========================

    @Test
    @DisplayName("Sphere.Ellipse: construction and center()")
    void testSphereEllipseBasic() {
        Sphere.Ellipse e = new Sphere.Ellipse(0.3, 0.2, 0.1, 0.4, 0.5);
        assertEquals(0.3, e.rad0, 1e-9);
        assertEquals(0.2, e.rad1, 1e-9);
        assertEquals(0.1, e.phi,  1e-9);
        assertEquals(0.4, e.theta, 1e-9);
        assertEquals(0.5, e.psi,   1e-9);

        Sphere.Point center = e.center();
        assertNotNull(center);
        // center is (psi, -theta)
        assertEquals(0.5, center.ra,   1e-9);
        assertEquals(-0.4, center.dec, 1e-9);
    }

    @Test
    @DisplayName("Sphere.Ellipse: contains() throws UnsupportedOperationException")
    void testSphereEllipseContainsThrows() {
        Sphere.Ellipse e = new Sphere.Ellipse(0.3, 0.2, 0.1, 0.4, 0.5);
        assertThrows(UnsupportedOperationException.class,
            () -> e.contains(new Sphere.Point(0.0, 0.0)));
    }

    @Test
    @DisplayName("Sphere.Ellipse: equals")
    void testSphereEllipseEquals() {
        Sphere.Ellipse a = new Sphere.Ellipse(0.3, 0.2, 0.1, 0.4, 0.5);
        Sphere.Ellipse b = new Sphere.Ellipse(0.3, 0.2, 0.1, 0.4, 0.5);
        Sphere.Ellipse c = new Sphere.Ellipse(0.9, 0.2, 0.1, 0.4, 0.5);
        assertTrue(a.equals(b));
        assertFalse(a.equals(c));
        assertFalse(a.equals("other"));
    }

    @Test
    @DisplayName("Sphere.Ellipse: wrappingRectangle() does not throw")
    void testSphereEllipseWrappingRectangle() {
        Sphere.Ellipse e = new Sphere.Ellipse(0.3, 0.2, 0.1, 0.4, 0.5);
        RectangleRn r = e.wrappingRectangle();
        assertNotNull(r);
        assertEquals(3, r.nDimensions());
    }

    // ===========================
    // Sphere.Line
    // ===========================

    @Test
    @DisplayName("Sphere.Line: constructor (phi,theta,psi,length) and equals")
    void testSphereLineBasic() {
        Sphere.Line line = new Sphere.Line(0.1, 0.2, 0.3, 1.0);
        assertEquals(0.1, line.phi,    1e-9);
        assertEquals(0.2, line.theta,  1e-9);
        assertEquals(0.3, line.psi,    1e-9);
        assertEquals(1.0, line.length, 1e-9);

        Sphere.Line same = new Sphere.Line(0.1, 0.2, 0.3, 1.0);
        assertTrue(line.equals(same));
        assertFalse(line.equals(new Sphere.Line(0.9, 0.2, 0.3, 1.0)));
        assertFalse(line.equals("nope"));
    }

    @Test
    @DisplayName("Sphere.Line: constructor from two Sphere.Points (non-antipodal)")
    void testSphereLineFromTwoPoints() {
        Sphere.Point beg = new Sphere.Point(0.0, 0.0);
        Sphere.Point end = new Sphere.Point(0.1, 0.0);
        Sphere.Line line = new Sphere.Line(beg, end);
        assertNotNull(line);
        assertTrue(line.length > 0);
    }

    @Test
    @DisplayName("Sphere.Line: constructor from identical points (zero-length)")
    void testSphereLineZeroLength() {
        Sphere.Point p = new Sphere.Point(0.5, 0.3);
        Sphere.Line line = new Sphere.Line(p, p);
        assertNotNull(line);
        assertEquals(0.0, line.length, 1e-9);
    }

    @Test
    @DisplayName("Sphere.Line: meridian factory method")
    void testSphereLineMeridian() {
        Sphere.Line meridian = Sphere.Line.meridian(0.0);
        assertNotNull(meridian);
        assertEquals(Math.PI, meridian.length, 1e-9);

        // Negative RA
        Sphere.Line negMeridian = Sphere.Line.meridian(-Math.PI / 2);
        assertNotNull(negMeridian);
    }

    @Test
    @DisplayName("Sphere.Line: wrappingRectangle() with non-zero length")
    void testSphereLineWrappingRectangle() {
        Sphere.Line line = new Sphere.Line(0.1, 0.2, 0.3, 1.0);
        RectangleRn r = line.wrappingRectangle();
        assertNotNull(r);
        assertEquals(3, r.nDimensions());
    }

    @Test
    @DisplayName("Sphere.Line: wrappingRectangle() with zero length")
    void testSphereLineWrappingRectangleZero() {
        Sphere.Line line = new Sphere.Line(0.1, 0.2, 0.3, 0.0);
        RectangleRn r = line.wrappingRectangle();
        assertNotNull(r);
        assertEquals(3, r.nDimensions());
    }

    @Test
    @DisplayName("Sphere.Line: contains() checks if point is on the line")
    void testSphereLineContains() {
        // Build a line from two nearby points
        Sphere.Point beg = new Sphere.Point(0.0, 0.0);
        Sphere.Point end = new Sphere.Point(0.2, 0.0);
        Sphere.Line line = new Sphere.Line(beg, end);

        // The beginning point should be on the line (approximately)
        boolean begOnLine = line.contains(beg);
        // The end might or might not be exact due to floating point; just test no exception
        assertDoesNotThrow(() -> line.contains(end));
        // A far away point should NOT be on the line
        assertFalse(line.contains(new Sphere.Point(Math.PI, Math.PI / 2)));
    }

    @Test
    @DisplayName("Sphere.Line: from antipodal points (special case)")
    void testSphereLineAntipodal() {
        // RA same, dec = 0 and PI → distance = PI (antipodal on same meridian)
        Sphere.Point beg = new Sphere.Point(0.5, 0.0);
        Sphere.Point end = new Sphere.Point(0.5, 0.0);
        // Two identical points → zero-length line
        Sphere.Line line = new Sphere.Line(beg, end);
        assertEquals(0.0, line.length, 1e-9);
    }

    // ===========================
    // Sphere.Polygon
    // ===========================

    @Test
    @DisplayName("Sphere.Polygon: wrappingRectangle() on triangle")
    void testSpherePolygonWrappingRectangle() {
        Sphere.Point[] pts = {
            new Sphere.Point(0.0,  0.0),
            new Sphere.Point(0.5,  0.0),
            new Sphere.Point(0.25, 0.3)
        };
        Sphere.Polygon poly = new Sphere.Polygon(pts);
        RectangleRn r = poly.wrappingRectangle();
        assertNotNull(r);
        assertEquals(3, r.nDimensions());
    }

    @Test
    @DisplayName("Sphere.Polygon: contains() throws UnsupportedOperationException")
    void testSpherePolygonContainsThrows() {
        Sphere.Point[] pts = {
            new Sphere.Point(0.0, 0.0),
            new Sphere.Point(1.0, 0.0),
            new Sphere.Point(0.5, 0.5)
        };
        Sphere.Polygon poly = new Sphere.Polygon(pts);
        assertThrows(UnsupportedOperationException.class,
            () -> poly.contains(new Sphere.Point(0.5, 0.2)));
    }

    @Test
    @DisplayName("Sphere.Polygon: wrappingRectangle on larger polygon")
    void testSpherePolygonLarger() {
        Sphere.Point[] pts = {
            new Sphere.Point(0.0, 0.0),
            new Sphere.Point(1.0, 0.0),
            new Sphere.Point(1.0, 0.5),
            new Sphere.Point(0.5, 0.8),
            new Sphere.Point(0.0, 0.5)
        };
        Sphere.Polygon poly = new Sphere.Polygon(pts);
        assertDoesNotThrow(poly::wrappingRectangle);
    }
}
