package org.garret.perst;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;

/**
 * Phase 2F: Tests for compound/random-access indexes.
 * Covers: AltBtreeCompoundIndex, RndBtreeCompoundIndex, RndBtreeMultiFieldIndex
 */
class CompoundIndexTest {

    private static final String DB = "testcompound.dbs";
    private Storage storage;

    public static class Record extends Persistent {
        public String  name;
        public int     intVal;
        public double  dblVal;
        public Record() {}
        public Record(Storage s, String name, int intVal, double dblVal) {
            super(s); this.name = name; this.intVal = intVal; this.dblVal = dblVal;
        }
    }

    /** Open with alternative btree enabled */
    private void openAlt() {
        storage = StorageFactory.getInstance().createStorage();
        storage.setProperty("perst.alternative.btree", "true");
        storage.open(DB, 8 * 1024 * 1024);
    }

    /** Open with default (standard) btree */
    private void openStd() {
        storage = StorageFactory.getInstance().createStorage();
        storage.open(DB, 8 * 1024 * 1024);
    }

    @BeforeEach
    void setUp() {
        // Individual tests call openAlt() or openStd() as needed
    }

    @AfterEach
    void tearDown() {
        if (storage != null && storage.isOpened()) storage.close();
        new java.io.File(DB).delete();
    }

    // ---- AltBtreeCompoundIndex (via createIndex(Class[], bool) with alt btree) ----

    @Test @DisplayName("AltBtreeCompoundIndex: basic put/get")
    void testAltCompoundBasic() {
        openAlt();
        Index<Record> idx = storage.createIndex(
            new Class[]{Integer.class, String.class}, true);
        storage.setRoot((IPersistent)idx);

        Record r1 = new Record(storage, "a", 1, 1.0);
        Record r2 = new Record(storage, "b", 2, 2.0);
        Record r3 = new Record(storage, "c", 3, 3.0);
        idx.put(new Key(new Object[]{1, "a"}), r1);
        idx.put(new Key(new Object[]{2, "b"}), r2);
        idx.put(new Key(new Object[]{3, "c"}), r3);
        storage.commit();

        assertEquals(3, idx.size());
        Record found = idx.get(new Key(new Object[]{2, "b"}));
        assertNotNull(found);
        assertEquals("b", found.name);
    }

    @Test @DisplayName("AltBtreeCompoundIndex: range iteration")
    void testAltCompoundRange() {
        openAlt();
        Index<Record> idx = storage.createIndex(
            new Class[]{Integer.class, String.class}, false);
        storage.setRoot((IPersistent)idx);

        for (int i = 1; i <= 10; i++) {
            idx.put(new Key(new Object[]{i, "k"+i}), new Record(storage, "r"+i, i, i*1.0));
        }
        storage.commit();

        Key lo = new Key(new Object[]{3, ""}, true);
        Key hi = new Key(new Object[]{7, "\uFFFF"}, true);
        Iterator<Record> it = idx.iterator(lo, hi, GenericIndex.ASCENT_ORDER);
        int count = 0;
        while (it.hasNext()) { it.next(); count++; }
        assertEquals(5, count);
    }

    @Test @DisplayName("AltBtreeCompoundIndex: remove entry")
    void testAltCompoundRemove() {
        openAlt();
        Index<Record> idx = storage.createIndex(
            new Class[]{Integer.class, String.class}, true);
        storage.setRoot((IPersistent)idx);

        Record r = new Record(storage, "del", 99, 99.0);
        idx.put(new Key(new Object[]{99, "del"}), r);
        assertEquals(1, idx.size());
        idx.remove(new Key(new Object[]{99, "del"}));
        assertEquals(0, idx.size());
    }

    @Test @DisplayName("AltBtreeCompoundIndex: descent order iterator")
    void testAltCompoundDescent() {
        openAlt();
        Index<Record> idx = storage.createIndex(new Class[]{Integer.class}, false);
        storage.setRoot((IPersistent)idx);

        for (int i = 1; i <= 5; i++) {
            idx.put(new Key(new Object[]{i}), new Record(storage, "r"+i, i, i*1.0));
        }
        Iterator<Record> it = idx.iterator(null, null, GenericIndex.DESCENT_ORDER);
        int prev = Integer.MAX_VALUE;
        while (it.hasNext()) {
            Record r = it.next();
            assertTrue(r.intVal <= prev);
            prev = r.intVal;
        }
    }

    @Test @DisplayName("AltBtreeCompoundIndex: many entries force page splits")
    void testAltCompoundMany() {
        openAlt();
        Index<Record> idx = storage.createIndex(
            new Class[]{int.class, String.class}, false);
        storage.setRoot((IPersistent)idx);

        int N = 300;
        for (int i = 0; i < N; i++) {
            idx.put(new Key(new Object[]{i, "k"+i}), new Record(storage, "r"+i, i, i*1.0));
        }
        storage.commit();
        assertEquals(N, idx.size());

        int count = 0;
        for (Record r : idx) { assertNotNull(r); count++; }
        assertEquals(N, count);
    }

    // ---- RndBtreeCompoundIndex (via createRandomAccessIndex) ----

    @Test @DisplayName("RndBtreeCompoundIndex: basic put/get")
    void testRndCompoundBasic() {
        openStd();
        Index<Record> idx = storage.createRandomAccessIndex(
            new Class[]{int.class, String.class}, true);
        storage.setRoot((IPersistent)idx);

        Record r = new Record(storage, "rnd", 7, 7.0);
        idx.put(new Key(new Object[]{7, "rnd"}), r);
        storage.commit();
        assertEquals(1, idx.size());

        Record found = idx.get(new Key(new Object[]{7, "rnd"}));
        assertNotNull(found);
        assertEquals("rnd", found.name);
    }

    @Test @DisplayName("RndBtreeCompoundIndex: range iterator")
    void testRndCompoundRange() {
        openStd();
        Index<Record> idx = storage.createRandomAccessIndex(
            new Class[]{Integer.class, String.class}, false);
        storage.setRoot((IPersistent)idx);

        for (int i = 1; i <= 10; i++) {
            idx.put(new Key(new Object[]{i, "k"+i}), new Record(storage, "r"+i, i, i*1.0));
        }
        storage.commit();

        Key lo = new Key(new Object[]{2, ""}, true);
        Key hi = new Key(new Object[]{5, "\uFFFF"}, true);
        Iterator<Record> it = idx.iterator(lo, hi, GenericIndex.ASCENT_ORDER);
        int count = 0;
        while (it.hasNext()) { it.next(); count++; }
        assertEquals(4, count);
    }

    @Test @DisplayName("RndBtreeCompoundIndex: random access by position")
    void testRndCompoundRandomAccess() {
        openStd();
        Index<Record> idx = storage.createRandomAccessIndex(
            new Class[]{Integer.class}, false);
        storage.setRoot((IPersistent)idx);

        for (int i = 0; i < 10; i++) {
            idx.put(new Key(new Object[]{i}), new Record(storage, "r"+i, i, i*1.0));
        }
        storage.commit();

        Record first = idx.getAt(0);
        Record last  = idx.getAt(idx.size()-1);
        assertNotNull(first);
        assertNotNull(last);
        assertEquals(0, first.intVal);
        assertEquals(9, last.intVal);
    }

    @Test @DisplayName("RndBtreeCompoundIndex: many entries force page splits")
    void testRndCompoundMany() {
        openStd();
        Index<Record> idx = storage.createRandomAccessIndex(
            new Class[]{int.class, String.class}, false);
        storage.setRoot((IPersistent)idx);

        int N = 300;
        for (int i = 0; i < N; i++) {
            idx.put(new Key(new Object[]{i, "k"+i}), new Record(storage, "r"+i, i, i*1.0));
        }
        storage.commit();
        assertEquals(N, idx.size());

        int count = 0;
        for (Record r : idx) { assertNotNull(r); count++; }
        assertEquals(N, count);
    }

    // ---- RndBtreeMultiFieldIndex (via createRandomAccessFieldIndex multi-field) ----

    @Test @DisplayName("RndBtreeMultiFieldIndex: basic put/iterate")
    void testRndMultiFieldBasic() {
        openStd();
        FieldIndex<Record> idx = storage.createRandomAccessFieldIndex(
            Record.class, new String[]{"intVal", "name"}, false);
        storage.setRoot((IPersistent)idx);

        idx.put(new Record(storage, "alice", 10, 1.0));
        idx.put(new Record(storage, "bob",   20, 2.0));
        idx.put(new Record(storage, "carol", 30, 3.0));
        storage.commit();
        assertEquals(3, idx.size());

        int count = 0;
        for (Record r : idx) { assertNotNull(r.name); count++; }
        assertEquals(3, count);
    }

    @Test @DisplayName("RndBtreeMultiFieldIndex: random access getAt")
    void testRndMultiFieldGetAt() {
        openStd();
        FieldIndex<Record> idx = storage.createRandomAccessFieldIndex(
            Record.class, new String[]{"intVal"}, false);
        storage.setRoot((IPersistent)idx);

        for (int i = 0; i < 10; i++) {
            idx.put(new Record(storage, "r"+i, i, i*1.0));
        }
        storage.commit();

        Record first = idx.getAt(0);
        Record last  = idx.getAt(9);
        assertNotNull(first);
        assertNotNull(last);
        assertEquals(0, first.intVal);
        assertEquals(9, last.intVal);
    }

    @Test @DisplayName("RndBtreeMultiFieldIndex: select predicate")
    void testRndMultiFieldSelect() {
        openStd();
        FieldIndex<Record> idx = storage.createRandomAccessFieldIndex(
            Record.class, new String[]{"intVal", "name"}, false);
        storage.setRoot((IPersistent)idx);

        idx.put(new Record(storage, "a", 5, 5.0));
        idx.put(new Record(storage, "b", 15, 15.0));
        idx.put(new Record(storage, "c", 25, 25.0));
        storage.commit();

        IterableIterator<Record> it = idx.select("intVal >= 15");
        int count = 0;
        while (it.hasNext()) { it.next(); count++; }
        assertEquals(2, count);
    }

    @Test @DisplayName("RndBtreeMultiFieldIndex: many entries force page splits")
    void testRndMultiFieldMany() {
        openStd();
        FieldIndex<Record> idx = storage.createRandomAccessFieldIndex(
            Record.class, new String[]{"intVal", "name"}, false);
        storage.setRoot((IPersistent)idx);

        int N = 300;
        for (int i = 0; i < N; i++) {
            idx.put(new Record(storage, "r"+i, i, i*1.0));
        }
        storage.commit();
        assertEquals(N, idx.size());

        Iterator<Record> it = idx.iterator(null, null, GenericIndex.DESCENT_ORDER);
        int prev = Integer.MAX_VALUE;
        while (it.hasNext()) {
            Record r = it.next();
            assertTrue(r.intVal <= prev);
            prev = r.intVal;
        }
    }

    @Test @DisplayName("RndBtreeMultiFieldIndex: remove entries")
    void testRndMultiFieldRemove() {
        openStd();
        FieldIndex<Record> idx = storage.createRandomAccessFieldIndex(
            Record.class, new String[]{"intVal"}, false);
        storage.setRoot((IPersistent)idx);

        Record r1 = new Record(storage, "x", 1, 1.0);
        Record r2 = new Record(storage, "y", 2, 2.0);
        idx.put(r1); idx.put(r2);
        assertEquals(2, idx.size());

        idx.remove(r1);
        assertEquals(1, idx.size());
    }
}
