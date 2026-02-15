package org.garret.perst;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 conversion of tst/TestDecimal.java
 * Tests decimal type operations
 */
class TestDecimal {

    private static final int INT_DIGITS = 5;
    private static final int FRAC_DIGITS = 2;

    @Test
    @DisplayName("Test decimal basic operations")
    void testDecimalBasicOperations() {
        Decimal d1 = new Decimal(12345, INT_DIGITS, FRAC_DIGITS);
        Decimal d2 = new Decimal(12.34, INT_DIGITS, FRAC_DIGITS);
        Decimal d3 = new Decimal("1.23", INT_DIGITS, FRAC_DIGITS);
        Decimal d4 = new Decimal("00001.00");
        Decimal d5 = new Decimal(-12345, INT_DIGITS, FRAC_DIGITS);
        Decimal d6 = new Decimal(-12.34, INT_DIGITS, FRAC_DIGITS);
        Decimal d7 = new Decimal("    -1.23", INT_DIGITS, FRAC_DIGITS);
        Decimal d8 = new Decimal("-00001.00");

        // Test add
        assertEquals(new Decimal(13579, INT_DIGITS, FRAC_DIGITS), d1.add(d2), "Addition should work");

        // Test sub
        assertEquals(new Decimal(13579, INT_DIGITS, FRAC_DIGITS), d1.sub(d6), "Subtraction should work");

        // Test neg
        assertEquals(d5, d1.neg(), "Negation should work");
        assertEquals(d8, d4.neg(), "Negation of positive should work");
    }

    @Test
    @DisplayName("Test decimal floor operations")
    void testDecimalFloor() {
        Decimal d3 = new Decimal("1.23", INT_DIGITS, FRAC_DIGITS);
        Decimal d4 = new Decimal("00001.00");
        Decimal d7 = new Decimal("    -1.23", INT_DIGITS, FRAC_DIGITS);
        Decimal d8 = new Decimal("-00001.00");

        assertEquals(1, d3.floor(), "Floor of 1.23 should be 1");
        assertEquals(-2, d7.floor(), "Floor of -1.23 should be -2");
        assertEquals(1, d4.floor(), "Floor of 1.00 should be 1");
        assertEquals(-1, d8.floor(), "Floor of -1.00 should be -1");
    }

    @Test
    @DisplayName("Test decimal ceil operations")
    void testDecimalCeil() {
        Decimal d3 = new Decimal("1.23", INT_DIGITS, FRAC_DIGITS);
        Decimal d4 = new Decimal("00001.00");
        Decimal d7 = new Decimal("    -1.23", INT_DIGITS, FRAC_DIGITS);
        Decimal d8 = new Decimal("-00001.00");

        assertEquals(2, d3.ceil(), "Ceil of 1.23 should be 2");
        assertEquals(-1, d7.ceil(), "Ceil of -1.23 should be -1");
        assertEquals(1, d4.ceil(), "Ceil of 1.00 should be 1");
        assertEquals(-1, d8.ceil(), "Ceil of -1.00 should be -1");
    }

    @Test
    @DisplayName("Test decimal round operations")
    void testDecimalRound() {
        Decimal d3 = new Decimal("1.23", INT_DIGITS, FRAC_DIGITS);
        Decimal d4 = new Decimal("00001.00");
        Decimal d7 = new Decimal("    -1.23", INT_DIGITS, FRAC_DIGITS);
        Decimal d8 = new Decimal("-00001.00");

        assertEquals(1, d3.round(), "Round of 1.23 should be 1");
        assertEquals(-1, d7.round(), "Round of -1.23 should be -1");
        assertEquals(1, d4.round(), "Round of 1.00 should be 1");
        assertEquals(-1, d8.round(), "Round of -1.00 should be -1");
    }

    @Test
    @DisplayName("Test decimal comparison")
    void testDecimalComparison() {
        Decimal d1 = new Decimal(12345, INT_DIGITS, FRAC_DIGITS);
        Decimal d2 = new Decimal(12.34, INT_DIGITS, FRAC_DIGITS);
        Decimal d3 = new Decimal("1.23", INT_DIGITS, FRAC_DIGITS);
        Decimal d4 = new Decimal("00001.00");
        Decimal d5 = new Decimal(-12345, INT_DIGITS, FRAC_DIGITS);
        Decimal d6 = new Decimal(-12.34, INT_DIGITS, FRAC_DIGITS);
        Decimal d7 = new Decimal("    -1.23", INT_DIGITS, FRAC_DIGITS);
        Decimal d8 = new Decimal("-00001.00");

        assertTrue(d1.compareTo(d2) > 0, "d1 should be greater than d2");
        assertTrue(d3.compareTo(d2) < 0, "d3 should be less than d2");
        assertTrue(d5.compareTo(d6) < 0, "d5 should be less than d6");
        assertTrue(d7.compareTo(d6) > 0, "d7 should be greater than d6");
        assertEquals(0, d4.compareTo(d8.abs()), "d4 should equal absolute value of d8");
    }

    @Test
    @DisplayName("Test decimal string representation")
    void testDecimalStringRepresentation() {
        Decimal d1 = new Decimal(12345, INT_DIGITS, FRAC_DIGITS);
        Decimal d5 = new Decimal(-12345, INT_DIGITS, FRAC_DIGITS);

        assertEquals("   123.45", d1.toString(' '), "Formatted string with padding");
        assertEquals("  -123.45", d5.toString(' '), "Formatted negative string with padding");
        assertEquals("123.45", d1.toString(), "Default string representation");
        assertEquals("-123.45", d5.toString(), "Default negative string representation");
        assertEquals("100123.45", d1.toLexicographicString(), "Lexicographic string");
        assertEquals("099876.55", d5.toLexicographicString(), "Negative lexicographic string");
    }
}
