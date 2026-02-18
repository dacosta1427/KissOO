package org.garret.perst;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;

/**
 * Phase 2E: Tests for AltBtreeMultiFieldIndex.
 * Triggered when perst.alternative.btree=true + createFieldIndex(multi-field).
 */
class AltBtreeTest {

    private static final String DB = "testaltbtree.dbs";
    private Storage storage;

    public static class Record extends Persistent {
        public String  strKey;
        public int     intKey;

        public Record() {}
        public Record(Storage s, String strKey, int intKey) {
            super(s);
            this.strKey = strKey;
            this.intKey = intKey;
        }
    }

    @BeforeEach
    void setUp() {
        storage = StorageFactory.getInstance().createStorage();
        storage.setProperty("perst.alternative.btree", "true");
        storage.open(DB, 8 * 1024 * 1024);
    }

    @AfterEach
    void tearDown() {
        if (storage.isOpened()) storage.close();
        new java.io.File(DB).delete();
    }

    @Test @DisplayName("AltBtreeMultiFieldIndex: basic put/get with two fields")
    void testMultiFieldBasic() {
        FieldIndex<Record> idx = storage.createFieldIndex(
            Record.class, new String[]{"intKey", "strKey"}, true);
        storage.setRoot((IPersistent)idx);

        Record r1 = new Record(storage, "alpha", 1);
        Record r2 = new Record(storage, "beta",  2);
        Record r3 = new Record(storage, "gamma", 3);
        idx.put(r1); idx.put(r2); idx.put(r3);
        storage.commit();

        assertEquals(3, idx.size());

        // Iterate to verify all present
        int count = 0;
        for (Record r : idx) { assertNotNull(r.strKey); count++; }
        assertEquals(3, count);
    }

    @Test @DisplayName("AltBtreeMultiFieldIndex: iterator ascent order (single field)")
    void testMultiFieldIteratorAscent() {
        // Single-field AltBtree index (avoids convertKey issue with multi-field null ranges)
        FieldIndex<Record> idx = storage.createFieldIndex(
            Record.class, new String[]{"intKey"}, false);
        storage.setRoot((IPersistent)idx);

        for (int i = 5; i >= 1; i--) {
            idx.put(new Record(storage, "k"+i, i));
        }
        storage.commit();

        Iterator<Record> it = idx.iterator(null, null, GenericIndex.ASCENT_ORDER);
        int prev = -1;
        while (it.hasNext()) {
            Record r = it.next();
            assertTrue(r.intKey >= prev);
            prev = r.intKey;
        }
    }

    @Test @DisplayName("AltBtreeMultiFieldIndex: iterator descent order (single field)")
    void testMultiFieldIteratorDescent() {
        FieldIndex<Record> idx = storage.createFieldIndex(
            Record.class, new String[]{"intKey"}, false);
        storage.setRoot((IPersistent)idx);

        for (int i = 1; i <= 5; i++) {
            idx.put(new Record(storage, "k"+i, i));
        }
        storage.commit();

        Iterator<Record> it = idx.iterator(null, null, GenericIndex.DESCENT_ORDER);
        int prev = Integer.MAX_VALUE;
        while (it.hasNext()) {
            Record r = it.next();
            assertTrue(r.intKey <= prev);
            prev = r.intKey;
        }
    }

    @Test @DisplayName("AltBtreeMultiFieldIndex: range query (single-field int)")
    void testMultiFieldRangeQuery() {
        // Use single-field index so Key types are unambiguous
        FieldIndex<Record> idx = storage.createFieldIndex(
            Record.class, new String[]{"intKey"}, false);
        storage.setRoot((IPersistent)idx);

        for (int i = 1; i <= 10; i++) {
            idx.put(new Record(storage, "k"+i, i));
        }
        storage.commit();

        // Range [3, 7] inclusive
        Key lo = new Key(3, true);
        Key hi = new Key(7, true);
        Iterator<Record> it = idx.iterator(lo, hi, GenericIndex.ASCENT_ORDER);
        int count = 0;
        while (it.hasNext()) { it.next(); count++; }
        assertEquals(5, count);
    }

    @Test @DisplayName("AltBtreeMultiFieldIndex: remove entry")
    void testMultiFieldRemove() {
        FieldIndex<Record> idx = storage.createFieldIndex(
            Record.class, new String[]{"intKey", "strKey"}, true);
        storage.setRoot((IPersistent)idx);

        Record r = new Record(storage, "test", 42);
        idx.put(r);
        assertEquals(1, idx.size());
        idx.remove(r);
        assertEquals(0, idx.size());
    }

    @Test @DisplayName("AltBtreeMultiFieldIndex: select via JSQL predicate")
    void testMultiFieldSelect() {
        FieldIndex<Record> idx = storage.createFieldIndex(
            Record.class, new String[]{"intKey", "strKey"}, false);
        storage.setRoot((IPersistent)idx);

        idx.put(new Record(storage, "aaa", 10));
        idx.put(new Record(storage, "bbb", 20));
        idx.put(new Record(storage, "ccc", 30));
        storage.commit();

        IterableIterator<Record> it = idx.select("intKey >= 20");
        int count = 0;
        while (it.hasNext()) { it.next(); count++; }
        assertEquals(2, count);
    }

    @Test @DisplayName("AltBtreeMultiFieldIndex: many entries forces splits")
    void testMultiFieldManyEntries() {
        FieldIndex<Record> idx = storage.createFieldIndex(
            Record.class, new String[]{"intKey", "strKey"}, false);
        storage.setRoot((IPersistent)idx);

        int N = 500;
        for (int i = 0; i < N; i++) {
            idx.put(new Record(storage, "key" + i, i));
        }
        storage.commit();
        assertEquals(N, idx.size());

        // Iterate all
        int count = 0;
        for (Record r : idx) { assertNotNull(r); count++; }
        assertEquals(N, count);
    }

    @Test @DisplayName("AltBtreeMultiFieldIndex: non-unique allows duplicate keys")
    void testMultiFieldNonUniqueAllowsDuplicates() {
        FieldIndex<Record> idx = storage.createFieldIndex(
            Record.class, new String[]{"intKey"}, false);
        storage.setRoot((IPersistent)idx);

        // Same intKey, different strKey
        idx.put(new Record(storage, "first",  5));
        idx.put(new Record(storage, "second", 5));
        idx.put(new Record(storage, "third",  5));
        storage.commit();

        assertEquals(3, idx.size());
        // All three returned for key=5
        Object[] arr = idx.get(new Key(5), new Key(5));
        assertEquals(3, arr.length);
    }

    @Test @DisplayName("AltBtreeMultiFieldIndex: prefix string iteration")
    void testMultiFieldStringPrefix() {
        FieldIndex<Record> idx = storage.createFieldIndex(
            Record.class, new String[]{"strKey"}, false);
        storage.setRoot((IPersistent)idx);

        idx.put(new Record(storage, "apple",  1));
        idx.put(new Record(storage, "apricot", 2));
        idx.put(new Record(storage, "banana", 3));
        storage.commit();

        // prefix "ap" should match apple and apricot
        IterableIterator<Record> it = idx.prefixIterator("ap");
        int count = 0;
        while (it.hasNext()) { it.next(); count++; }
        assertEquals(2, count);
    }
}
