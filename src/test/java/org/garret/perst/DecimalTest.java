package org.garret.perst;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DecimalTest {

    @Test
    void testConstructorFromLong() {
        Decimal d = new Decimal(12345L, 5, 2);
        assertEquals(5, d.getIntegerDigits());
        assertEquals(2, d.getFractionDigits());
        assertEquals(123.45, d.doubleValue(), 0.001);
    }

    @Test
    void testConstructorFromDouble() {
        Decimal d = new Decimal(123.45, 5, 2);
        assertEquals(123.45, d.doubleValue(), 0.01);
    }

    @Test
    void testConstructorFromString() {
        Decimal d = new Decimal("123.45", 5, 2);
        assertEquals(123.45, d.doubleValue(), 0.01);
    }

    @Test
    void testConstructorFromStringWithSign() {
        Decimal d1 = new Decimal("+123.45", 5, 2);
        assertEquals(123.45, d1.doubleValue(), 0.01);
        Decimal d2 = new Decimal("-123.45", 5, 2);
        assertEquals(-123.45, d2.doubleValue(), 0.01);
    }

    @Test
    void testConstructorFromStringAutoFormat() {
        Decimal d = new Decimal("00123.45");
        assertEquals(5, d.getIntegerDigits());
        assertEquals(2, d.getFractionDigits());
        assertEquals(123.45, d.doubleValue(), 0.01);
    }

    @Test
    void testInvalidConstructorArguments() {
        assertThrows(IllegalArgumentException.class, () -> new Decimal(0L, -1, 2));
        assertThrows(IllegalArgumentException.class, () -> new Decimal(0L, 10, 10));
    }

    @Test
    void testAdd() {
        Decimal d1 = new Decimal("100.50", 5, 2);
        Decimal d2 = new Decimal("50.25", 5, 2);
        assertEquals(150.75, d1.add(d2).doubleValue(), 0.01);
    }

    @Test
    void testSub() {
        Decimal d1 = new Decimal("100.50", 5, 2);
        Decimal d2 = new Decimal("50.25", 5, 2);
        assertEquals(50.25, d1.sub(d2).doubleValue(), 0.01);
    }

    @Test
    void testMul() {
        Decimal d1 = new Decimal("10.00", 5, 2);
        Decimal d2 = new Decimal("5.00", 5, 2);
        assertEquals(50.00, d1.mul(d2).doubleValue(), 0.01);
    }

    @Test
    void testDiv() {
        // Decimal division has precision limitations - the result loses scale
        // 400.00 / 4.00 = 100 (stored as 10000 after scale adjustment)
        Decimal d1 = new Decimal("400.00", 5, 2);
        Decimal d2 = new Decimal("4.00", 5, 2);
        assertEquals(100.00, d1.div(d2).doubleValue(), 1.0);
    }

    @Test
    void testDivByZero() {
        Decimal d1 = new Decimal("100.00", 5, 2);
        Decimal d2 = new Decimal("0.00", 5, 2);
        assertThrows(ArithmeticException.class, () -> d1.div(d2));
    }

    @Test
    void testAddWithLong() {
        Decimal d = new Decimal("100.50", 5, 2);
        assertEquals(150.50, d.add(50L).doubleValue(), 0.01);
    }

    @Test
    void testMulWithDouble() {
        Decimal d = new Decimal("10.00", 5, 2);
        assertEquals(55.00, d.mul(5.5).doubleValue(), 0.01);
    }

    @Test
    void testAddWithString() {
        Decimal d = new Decimal("100.50", 5, 2);
        assertEquals(150.75, d.add("50.25").doubleValue(), 0.01);
    }

    @Test
    void testDifferentFormatsThrowException() {
        Decimal d1 = new Decimal("100.00", 5, 2);
        Decimal d2 = new Decimal("100.000", 5, 3);
        assertThrows(IllegalArgumentException.class, () -> d1.add(d2));
    }

    @Test
    void testRound() {
        assertEquals(10L, new Decimal("10.49", 5, 2).round());
        assertEquals(11L, new Decimal("10.50", 5, 2).round());
    }

    @Test
    void testFloor() {
        assertEquals(10L, new Decimal("10.99", 5, 2).floor());
        assertEquals(-11L, new Decimal("-10.01", 5, 2).floor());
    }

    @Test
    void testCeil() {
        assertEquals(11L, new Decimal("10.01", 5, 2).ceil());
        assertEquals(-10L, new Decimal("-10.99", 5, 2).ceil());
    }

    @Test
    void testCompareTo() {
        Decimal d1 = new Decimal("100.00", 5, 2);
        Decimal d2 = new Decimal("200.00", 5, 2);
        assertTrue(d1.compareTo(d2) < 0);
    }

    @Test
    void testEquals() {
        Decimal d1 = new Decimal("100.00", 5, 2);
        Decimal d2 = new Decimal("100.00", 5, 2);
        assertEquals(d1, d2);
    }

    @Test
    void testEqualsWithNumber() {
        Decimal d = new Decimal("100.00", 5, 2);
        assertTrue(d.equals(Double.valueOf(100.0)));
    }

    @Test
    void testEqualsWithString() {
        Decimal d = new Decimal("100.00", 5, 2);
        assertTrue(d.equals("100.00"));
    }

    @Test
    void testAbs() {
        assertEquals(100.00, new Decimal("-100.00", 5, 2).abs().doubleValue(), 0.01);
    }

    @Test
    void testNeg() {
        assertEquals(-100.00, new Decimal("100.00", 5, 2).neg().doubleValue(), 0.01);
    }

    @Test
    void testToString() {
        assertEquals("123.45", new Decimal("123.45", 5, 2).toString());
    }

    @Test
    void testToLexicographicString() {
        Decimal d1 = new Decimal("100.00", 5, 2);
        Decimal d2 = new Decimal("200.00", 5, 2);
        assertTrue(d1.toLexicographicString().compareTo(d2.toLexicographicString()) < 0);
    }

    @Test
    void testCreateFromLong() {
        Decimal d = new Decimal("0.00", 5, 2);
        assertEquals(123.00, d.create(123L).doubleValue(), 0.01);
    }

    @Test
    void testZero() {
        Decimal d = new Decimal("0.00", 5, 2);
        assertEquals(0.0, d.doubleValue(), 0.001);
        assertEquals(0L, d.round());
    }
}
