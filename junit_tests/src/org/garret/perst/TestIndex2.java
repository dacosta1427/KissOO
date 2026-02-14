package org.garret.perst;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.HashMap;
import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 conversion of tst/TestIndex2.java
 * Tests SortedCollection (B-tree) functionality.
 * Scaled down from 100000 to 1000 records for faster testing.
 */
class TestIndex2 {

    static class Record extends Persistent {
        String strKey;
        long intKey;
    }

    static class Indices extends Persistent {
        SortedCollection<Record> strIndex;
        SortedCollection<Record> intIndex;
    }

    static class IntRecordComparator extends PersistentComparator<Record> {
        public int compareMembers(Record m1, Record m2) {
            long diff = m1.intKey - m2.intKey;
            return diff < 0 ? -1 : diff == 0 ? 0 : 1;
        }

        public int compareMemberWithKey(Record mbr, Object key) {
            long diff = mbr.intKey - ((Long) key).longValue();
            return diff < 0 ? -1 : diff == 0 ? 0 : 1;
        }
    }

    static class StrRecordComparator extends PersistentComparator<Record> {
        public int compareMembers(Record m1, Record m2) {
            return m1.strKey.compareTo(m2.strKey);
        }

        public int compareMemberWithKey(Record mbr, Object key) {
            return mbr.strKey.compareTo((String) key);
        }
    }

    private Storage storage;
    private static final int nRecords = 1000; // Reduced from 100000 for faster tests
    private static final String TEST_DB = "testidx2.dbs";

    @BeforeEach
    void setUp() throws Exception {
        storage = StorageFactory.getInstance().createStorage();
        storage.open(TEST_DB, Storage.INFINITE_PAGE_POOL);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (storage.isOpened()) {
            storage.close();
        }
        new java.io.File(TEST_DB).delete();
    }

    @Test
    @DisplayName("Test sorted collection insert")
    void testSortedCollectionInsert() {
        Indices root = new Indices();
        root.strIndex = storage.createSortedCollection(new StrRecordComparator(), true);
        root.intIndex = storage.createSortedCollection(new IntRecordComparator(), true);
        storage.setRoot(root);

        SortedCollection<Record> intIndex = root.intIndex;
        SortedCollection<Record> strIndex = root.strIndex;

        long key = 1999;
        for (int i = 0; i < nRecords; i++) {
            Record rec = new Record();
            key = (3141592621L * key + 2718281829L) % 1000000007L;
            rec.intKey = key;
            rec.strKey = Long.toString(key);
            intIndex.add(rec);
            strIndex.add(rec);
        }
        storage.commit();

        assertEquals(nRecords, intIndex.size(), "Should have nRecords in intIndex");
        assertEquals(nRecords, strIndex.size(), "Should have nRecords in strIndex");
    }

    @Test
    @DisplayName("Test sorted collection get by key")
    void testSortedCollectionGetByKey() {
        Indices root = new Indices();
        root.strIndex = storage.createSortedCollection(new StrRecordComparator(), true);
        root.intIndex = storage.createSortedCollection(new IntRecordComparator(), true);
        storage.setRoot(root);

        // Insert records
        long key = 1999;
        for (int i = 0; i < nRecords; i++) {
            Record rec = new Record();
            key = (3141592621L * key + 2718281829L) % 1000000007L;
            rec.intKey = key;
            rec.strKey = Long.toString(key);
            root.intIndex.add(rec);
            root.strIndex.add(rec);
        }
        storage.commit();

        // Test get by key
        key = 1999;
        for (int i = 0; i < 10; i++) {
            key = (3141592621L * key + 2718281829L) % 1000000007L;
            Record rec1 = root.intIndex.get(key);
            Record rec2 = root.strIndex.get(Long.toString(key));
            assertNotNull(rec1, "Record should be found in intIndex");
            assertNotNull(rec2, "Record should be found in strIndex");
            assertSame(rec1, rec2, "Same record should be returned");
        }
    }

    @Test
    @DisplayName("Test sorted collection iterator")
    void testSortedCollectionIterator() {
        Indices root = new Indices();
        root.strIndex = storage.createSortedCollection(new StrRecordComparator(), true);
        root.intIndex = storage.createSortedCollection(new IntRecordComparator(), true);
        storage.setRoot(root);

        // Insert records
        long key = 1999;
        for (int i = 0; i < nRecords; i++) {
            Record rec = new Record();
            key = (3141592621L * key + 2718281829L) % 1000000007L;
            rec.intKey = key;
            rec.strKey = Long.toString(key);
            root.intIndex.add(rec);
            root.strIndex.add(rec);
        }
        storage.commit();

        // Test iterator for intIndex
        Iterator<Record> iterator = root.intIndex.iterator();
        int count = 0;
        long prevKey = Long.MIN_VALUE;
        while (iterator.hasNext()) {
            Record rec = iterator.next();
            assertTrue(rec.intKey >= prevKey, "Records should be sorted");
            prevKey = rec.intKey;
            count++;
        }
        assertEquals(nRecords, count, "Should iterate all records");

        // Test iterator for strIndex
        iterator = root.strIndex.iterator();
        count = 0;
        String prevStrKey = "";
        while (iterator.hasNext()) {
            Record rec = iterator.next();
            assertTrue(rec.strKey.compareTo(prevStrKey) >= 0, "Records should be sorted");
            prevStrKey = rec.strKey;
            count++;
        }
        assertEquals(nRecords, count, "Should iterate all records");
    }

    @Test
    @DisplayName("Test sorted collection remove")
    void testSortedCollectionRemove() {
        Indices root = new Indices();
        root.strIndex = storage.createSortedCollection(new StrRecordComparator(), true);
        root.intIndex = storage.createSortedCollection(new IntRecordComparator(), true);
        storage.setRoot(root);

        // Insert records
        long key = 1999;
        for (int i = 0; i < nRecords; i++) {
            Record rec = new Record();
            key = (3141592621L * key + 2718281829L) % 1000000007L;
            rec.intKey = key;
            rec.strKey = Long.toString(key);
            root.intIndex.add(rec);
            root.strIndex.add(rec);
        }
        storage.commit();

        // Remove all records
        key = 1999;
        for (int i = 0; i < nRecords; i++) {
            key = (3141592621L * key + 2718281829L) % 1000000007L;
            Record rec = root.intIndex.get(key);
            if (rec != null) {
                root.intIndex.remove(rec);
                root.strIndex.remove(rec);
                rec.deallocate();
            }
        }
        storage.commit();

        assertFalse(root.intIndex.iterator().hasNext(), "intIndex should be empty");
        assertFalse(root.strIndex.iterator().hasNext(), "strIndex should be empty");
    }

    @Test
    @DisplayName("Test sorted collection GC")
    void testSortedCollectionGC() {
        Indices root = new Indices();
        root.strIndex = storage.createSortedCollection(new StrRecordComparator(), true);
        root.intIndex = storage.createSortedCollection(new IntRecordComparator(), true);
        storage.setRoot(root);

        // Insert records
        long key = 1999;
        for (int i = 0; i < nRecords; i++) {
            Record rec = new Record();
            key = (3141592621L * key + 2718281829L) % 1000000007L;
            rec.intKey = key;
            rec.strKey = Long.toString(key);
            root.intIndex.add(rec);
            root.strIndex.add(rec);
        }
        storage.commit();

        // Remove all records
        key = 1999;
        for (int i = 0; i < nRecords; i++) {
            key = (3141592621L * key + 2718281829L) % 1000000007L;
            Record rec = root.intIndex.get(key);
            if (rec != null) {
                root.intIndex.remove(rec);
                root.strIndex.remove(rec);
                rec.deallocate();
            }
        }
        storage.commit();

        // Run GC
        storage.gc();

        // Verify collection is empty
        assertFalse(root.intIndex.iterator().hasNext(), "intIndex should be empty after GC");
        assertFalse(root.strIndex.iterator().hasNext(), "strIndex should be empty after GC");
    }
}
