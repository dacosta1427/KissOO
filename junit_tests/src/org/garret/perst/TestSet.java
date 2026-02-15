package org.garret.perst;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 conversion of tst/TestSet.java
 * Tests IPersistentSet functionality.
 * Scaled down for faster testing.
 */
class TestSet {

    static class Indices extends Persistent {
        IPersistentSet set;
        Index index;
    }

    static class Record extends Persistent {
        int id;

        Record() {
        }

        Record(int id) {
            this.id = id;
        }
    }

    private Storage storage;
    private static final int nRecords = 100;
    private static final int maxInitSize = 50;
    private static final String TEST_DB = "testset.dbs";

    @BeforeEach
    void setUp() throws Exception {
        storage = StorageFactory.getInstance().createStorage();
        storage.open(TEST_DB, 32 * 1024 * 1024);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (storage.isOpened()) {
            storage.close();
        }
        new java.io.File(TEST_DB).delete();
    }

    @Test
    @DisplayName("Test persistent set insert")
    void testPersistentSetInsert() {
        Indices root = new Indices();
        root.set = storage.createSet();
        root.index = storage.createIndex(long.class, true);
        storage.setRoot(root);

        long key = 1999;
        int totalRecords = 0;
        for (int i = 0; i < nRecords; i++) {
            key = (3141592621L * key + 2718281829L) % 1000000007L;
            int r = (int) (key % maxInitSize);
            IPersistentSet ps = storage.createScalableSet(r);
            for (int j = 0; j < r; j++) {
                ps.add(new Record(j));
                totalRecords += 1;
            }
            root.set.add(ps);
            root.index.put(new Key(key), ps);
        }
        storage.commit();

        assertTrue(totalRecords > 0, "Should have inserted records");
        assertEquals(nRecords, root.set.size(), "Should have nRecords sets");
    }

    @Test
    @DisplayName("Test persistent set contains and size")
    void testPersistentSetContainsAndSize() {
        Indices root = new Indices();
        root.set = storage.createSet();
        root.index = storage.createIndex(long.class, true);
        storage.setRoot(root);

        // Insert records
        long key = 1999;
        for (int i = 0; i < nRecords; i++) {
            key = (3141592621L * key + 2718281829L) % 1000000007L;
            int r = (int) (key % maxInitSize);
            IPersistentSet ps = storage.createScalableSet(r);
            for (int j = 0; j < r; j++) {
                ps.add(new Record(j));
            }
            root.set.add(ps);
            root.index.put(new Key(key), ps);
        }
        storage.commit();

        // Test contains and size
        key = 1999;
        for (int i = 0; i < nRecords; i++) {
            key = (3141592621L * key + 2718281829L) % 1000000007L;
            int r = (int) (key % maxInitSize);
            IPersistentSet ps = (IPersistentSet) root.index.get(new Key(key));
            assertNotNull(ps, "Set should be found");
            assertTrue(root.set.contains(ps), "Set should be contained");
            assertEquals(r, ps.size(), "Set size should match");
        }
    }

    @Test
    @DisplayName("Test persistent set iterator")
    void testPersistentSetIterator() {
        Indices root = new Indices();
        root.set = storage.createSet();
        root.index = storage.createIndex(long.class, true);
        storage.setRoot(root);

        // Insert records
        long key = 1999;
        int totalRecords = 0;
        for (int i = 0; i < nRecords; i++) {
            key = (3141592621L * key + 2718281829L) % 1000000007L;
            int r = (int) (key % maxInitSize);
            IPersistentSet ps = storage.createScalableSet(r);
            for (int j = 0; j < r; j++) {
                ps.add(new Record(j));
                totalRecords += 1;
            }
            root.set.add(ps);
            root.index.put(new Key(key), ps);
        }
        storage.commit();

        // Test iterator
        Iterator iterator = root.set.iterator();
        int count = 0;
        while (iterator.hasNext()) {
            IPersistentSet ps = (IPersistentSet) iterator.next();
            Iterator si = ps.iterator();
            int sum = 0;
            while (si.hasNext()) {
                sum += ((Record) si.next()).id;
                count += 1;
            }
            // Verify sum formula: size*(size-1)/2
            assertEquals(ps.size() * (ps.size() - 1) / 2, sum, "Sum should match formula");
        }
        assertEquals(totalRecords, count, "Should iterate all records");
    }

    @Test
    @DisplayName("Test persistent set add records")
    void testPersistentSetAddRecords() {
        Indices root = new Indices();
        root.set = storage.createSet();
        root.index = storage.createIndex(long.class, true);
        storage.setRoot(root);

        // Insert records
        long key = 1999;
        for (int i = 0; i < nRecords; i++) {
            key = (3141592621L * key + 2718281829L) % 1000000007L;
            int r = (int) (key % maxInitSize);
            IPersistentSet ps = storage.createScalableSet(r);
            for (int j = 0; j < r; j++) {
                ps.add(new Record(j));
            }
            root.set.add(ps);
            root.index.put(new Key(key), ps);
        }
        storage.commit();

        // Add more records
        key = 1999;
        int initialTotal = 0;
        for (int i = 0; i < nRecords; i++) {
            key = (3141592621L * key + 2718281829L) % 1000000007L;
            int r = (int) (key % maxInitSize);
            IPersistentSet ps = (IPersistentSet) root.index.get(new Key(key));
            initialTotal += ps.size();
            // Add more records to the set
            for (int j = r; j < r * 2; j++) {
                ps.add(new Record(j));
            }
        }
        storage.commit();

        // Verify that records were added
        key = 1999;
        int finalTotal = 0;
        for (int i = 0; i < nRecords; i++) {
            key = (3141592621L * key + 2718281829L) % 1000000007L;
            IPersistentSet ps = (IPersistentSet) root.index.get(new Key(key));
            finalTotal += ps.size();
        }
        assertTrue(finalTotal > initialTotal, "Total records should increase after adding");
    }

    @Test
    @DisplayName("Test persistent set remove")
    void testPersistentSetRemove() {
        Indices root = new Indices();
        root.set = storage.createSet();
        root.index = storage.createIndex(long.class, true);
        storage.setRoot(root);

        // Insert records
        long key = 1999;
        for (int i = 0; i < nRecords; i++) {
            key = (3141592621L * key + 2718281829L) % 1000000007L;
            int r = (int) (key % maxInitSize);
            IPersistentSet ps = storage.createScalableSet(r);
            for (int j = 0; j < r; j++) {
                ps.add(new Record(j));
            }
            root.set.add(ps);
            root.index.put(new Key(key), ps);
        }
        storage.commit();

        // Remove all sets
        key = 1999;
        for (int i = 0; i < nRecords; i++) {
            key = (3141592621L * key + 2718281829L) % 1000000007L;
            IPersistentSet ps = (IPersistentSet) root.index.remove(new Key(key));
            root.set.remove(ps);
            // Deallocate all records in the set
            Iterator iterator = ps.iterator();
            while (iterator.hasNext()) {
                ((Persistent) iterator.next()).deallocate();
            }
            ps.deallocate();
        }
        storage.commit();

        assertEquals(0, root.set.size(), "Set should be empty");
        assertEquals(0, root.index.size(), "Index should be empty");
        assertFalse(root.set.iterator().hasNext(), "Iterator should be empty");
        assertFalse(root.index.iterator().hasNext(), "Index iterator should be empty");
    }
}
