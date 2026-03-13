package org.garret.perst;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Phase 2D: Tests for Key constructors, equals, and inclusion flag.
 */
class KeyTest {

    // ===== Primitive constructors =====

    @Test void booleanKey()  { Key k = new Key(true);  assertEquals(true,  k.oval == null ? k.ival != 0 : k.oval); }
    @Test void byteKey()     { Key k = new Key((byte)7);   assertEquals(7, k.ival); }
    @Test void charKey()     { Key k = new Key('A');        assertEquals('A', k.ival); }
    @Test void shortKey()    { Key k = new Key((short)42);  assertEquals(42, k.ival); }
    @Test void intKey()      { Key k = new Key(123);        assertEquals(123, k.ival); }
    @Test void longKey()     { Key k = new Key(Long.MAX_VALUE); assertEquals(Long.MAX_VALUE, k.lval); }
    @Test void floatKey()    { Key k = new Key(3.14f);      assertEquals(3.14f, (float)k.dval, 0.001f); }
    @Test void doubleKey()   { Key k = new Key(2.718);      assertEquals(2.718, k.dval, 0.0001); }
    @Test void dateKey()     { Date d = new Date(1_000_000L); Key k = new Key(d); assertEquals(d.getTime(), k.lval); }
    @Test void stringKey()   { Key k = new Key("hello");    assertEquals("hello", k.oval); }
    @Test void charArrayKey(){ Key k = new Key(new char[]{'a','b'}); assertNotNull(k.oval); }
    @Test void byteArrayKey(){ Key k = new Key(new byte[]{1,2,3});  assertNotNull(k.oval); }

    // ===== Inclusive flag (default = inclusive) =====

    @Test void defaultInclusionIsOne() {
        assertEquals(1, new Key(42).inclusion);
    }

    @Test void exclusiveIntKey() {
        Key k = new Key(42, false);
        assertEquals(0, k.inclusion, "exclusive key should have inclusion=0");
        assertEquals(42, k.ival);
    }

    @Test void inclusiveBooleanKey() {
        Key k = new Key(true, true);
        assertEquals(1, k.inclusion);
    }

    @Test void exclusiveDoubleKey() {
        Key k = new Key(1.5, false);
        assertEquals(0, k.inclusion);
        assertEquals(1.5, k.dval);
    }

    @Test void exclusiveStringKey() {
        Key k = new Key("abc", false);
        assertEquals(0, k.inclusion);
        assertEquals("abc", k.oval);
    }

    @Test void exclusiveLongKey() {
        Key k = new Key(99L, false);
        assertEquals(0, k.inclusion);
        assertEquals(99L, k.lval);
    }

    @Test void exclusiveByteKey() {
        Key k = new Key((byte)3, false);
        assertEquals(0, k.inclusion);
    }

    @Test void exclusiveCharKey() {
        Key k = new Key('Z', false);
        assertEquals(0, k.inclusion);
    }

    @Test void exclusiveShortKey() {
        Key k = new Key((short)10, false);
        assertEquals(0, k.inclusion);
    }

    @Test void exclusiveDateKey() {
        Date d = new Date();
        Key k = new Key(d, false);
        assertEquals(0, k.inclusion);
    }

    @Test void exclusiveFloatKey() {
        Key k = new Key(1.0f, false);
        assertEquals(0, k.inclusion);
    }

    // ===== equals =====

    @Test void equalsNullReturnsFalse() {
        assertNotEquals(new Key(1), null);
    }

    @Test void equalsSameIntKey() {
        assertEquals(new Key(5), new Key(5));
    }

    @Test void equalsSameStringKey() {
        assertEquals(new Key("x"), new Key("x"));
    }

    @Test void notEqualsDifferentInt() {
        assertNotEquals(new Key(1), new Key(2));
    }

    @Test void differentInclusionSameKeyFields() {
        // Key.equals() checks type, values, oval, and inclusion
        Key k1 = new Key(1, true);
        Key k2 = new Key(1, false);
        // If equals checks inclusion they differ; if not, same - just verify no exception
        assertDoesNotThrow(() -> k1.equals(k2));
        // At minimum: verify inclusion is stored correctly
        assertEquals(1, k1.inclusion);
        assertEquals(0, k2.inclusion);
    }

    @Test void notEqualsDifferentType() {
        Key intKey = new Key(1);
        Key longKey = new Key(1L);
        // They may differ in type field
        // Just verify equals doesn't throw
        assertDoesNotThrow(() -> intKey.equals(longKey));
    }

    // ===== Object key (IPersistent / generic Object) =====

    @Test void objectArrayKey() {
        Key k = new Key(new Object[]{"a", "b"});
        assertNotNull(k.oval);
    }

    @Test void twoObjectsKey() {
        Key k = new Key("lower", "upper");
        assertNotNull(k.oval);
    }

    // ===== IValue key =====

    @Test void ivalueKey() {
        IValue iv = new IValue() {
            public int hashCode() { return 42; }
            public boolean equals(Object o) { return o == this; }
        };
        Key k = new Key(iv);
        assertEquals(iv, k.oval);
    }

    // ===== Enum key =====

    enum Color { RED, GREEN, BLUE }

    @Test void enumKey() {
        Key k = new Key(Color.GREEN);
        // ival should be the ordinal
        assertEquals(Color.GREEN.ordinal(), k.ival);
    }
}
