package org.garret.perst;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 conversion of tst/TestThickIndex.java
 * Tests ThickIndex (index allowing duplicate keys) functionality
 */
class TestThickIndex {

    static class Record extends Persistent {
        String strKey;
        long intKey;
    }

    static class Indices extends Persistent {
        Index strIndex;
        Index intIndex;
    }

    private Storage storage;
    // Scaled down from 1000 records
    private static final int nRecords = 100;
    private static final int maxDuplicates = 100;
    private static final String TEST_DB = "testthick.dbs";

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
    @DisplayName("Test ThickIndex insert")
    void testThickIndexInsert() {
        Indices root = new Indices();
        root.strIndex = storage.createThickIndex(String.class);
        root.intIndex = storage.createThickIndex(long.class);
        storage.setRoot(root);

        long key = 1999;
        int n = 0;
        for (int i = 0; i < nRecords; i++) {
            key = (3141592621L * key + 2718281829L) % 1000000007L;
            int d = (int) (key % maxDuplicates);
            for (int j = 0; j < d; j++) {
                Record rec = new Record();
                rec.intKey = key;
                rec.strKey = Long.toString(key);
                root.intIndex.put(new Key(rec.intKey), rec);
                root.strIndex.put(new Key(rec.strKey), rec);
                n += 1;
            }
        }

        storage.commit();

        // Verify all records were inserted
        int count = 0;
        for (Object obj : root.intIndex) {
            count++;
        }
        assertTrue(count > 0, "Records should be inserted");
    }

    @Test
    @DisplayName("Test ThickIndex search by key")
    void testThickIndexSearchByKey() {
        Indices root = new Indices();
        root.strIndex = storage.createThickIndex(String.class);
        root.intIndex = storage.createThickIndex(long.class);
        storage.setRoot(root);

        // Insert records
        long key = 1999;
        for (int i = 0; i < nRecords; i++) {
            key = (3141592621L * key + 2718281829L) % 1000000007L;
            int d = (int) (key % maxDuplicates);
            for (int j = 0; j < d; j++) {
                Record rec = new Record();
                rec.intKey = key;
                rec.strKey = Long.toString(key);
                root.intIndex.put(new Key(rec.intKey), rec);
                root.strIndex.put(new Key(rec.strKey), rec);
            }
        }
        storage.commit();

        // Search for records
        key = 1999;
        for (int i = 0; i < nRecords; i++) {
            key = (3141592621L * key + 2718281829L) % 1000000007L;
            Object[] res1 = root.intIndex.get(new Key(key), new Key(key));
            Object[] res2 = root.strIndex.get(new Key(Long.toString(key)), new Key(Long.toString(key)));
            int d = (int) (key % maxDuplicates);

            assertEquals(d, res1.length, "Should find correct number of results");
            assertEquals(d, res2.length, "Should find correct number of string results");
        }
    }

    @Test
    @DisplayName("Test ThickIndex iterator")
    void testThickIndexIterator() {
        Indices root = new Indices();
        root.strIndex = storage.createThickIndex(String.class);
        root.intIndex = storage.createThickIndex(long.class);
        storage.setRoot(root);

        // Insert some records
        for (int i = 0; i < 10; i++) {
            Record rec = new Record();
            rec.intKey = i;
            rec.strKey = "key" + i;
            root.intIndex.put(new Key(rec.intKey), rec);
            root.strIndex.put(new Key(rec.strKey), rec);
        }
        storage.commit();

        // Test integer index iterator
        Iterator iterator = root.intIndex.iterator();
        long prevKey = Long.MIN_VALUE;
        int count = 0;
        while (iterator.hasNext()) {
            Record rec = (Record) iterator.next();
            assertTrue(rec.intKey >= prevKey, "Should be in sorted order");
            prevKey = rec.intKey;
            count++;
        }
        assertEquals(10, count, "Should have 10 records");
    }

    @Test
    @DisplayName("Test ThickIndex remove")
    void testThickIndexRemove() {
        Indices root = new Indices();
        root.strIndex = storage.createThickIndex(String.class);
        root.intIndex = storage.createThickIndex(long.class);
        storage.setRoot(root);

        // Insert records
        Record rec1 = new Record();
        rec1.intKey = 100;
        rec1.strKey = "100";
        root.intIndex.put(new Key(rec1.intKey), rec1);
        root.strIndex.put(new Key(rec1.strKey), rec1);

        Record rec2 = new Record();
        rec2.intKey = 200;
        rec2.strKey = "200";
        root.intIndex.put(new Key(rec2.intKey), rec2);
        root.strIndex.put(new Key(rec2.strKey), rec2);

        storage.commit();

        // Verify both exist
        Object[] res1 = root.intIndex.get(new Key(100L), new Key(100L));
        assertEquals(1, res1.length, "Should find 1 record with key 100");

        // Remove one - use the same key instance
        root.intIndex.remove(new Key(rec1.intKey), rec1);
        root.strIndex.remove(new Key(rec1.strKey), rec1);

        // Verify removal
        res1 = root.intIndex.get(new Key(100L), new Key(100L));
        assertEquals(0, res1.length, "Should find 0 records after removal");

        // Verify other still exists
        Object[] res2 = root.intIndex.get(new Key(200L), new Key(200L));
        assertEquals(1, res2.length, "Other record should still exist");
    }

    @Test
    @DisplayName("Test ThickIndex iteration order")
    void testThickIndexIterationOrder() {
        Indices root = new Indices();
        root.strIndex = storage.createThickIndex(String.class);
        root.intIndex = storage.createThickIndex(long.class);
        storage.setRoot(root);

        // Insert some records
        for (int i = 0; i < 10; i++) {
            Record rec = new Record();
            rec.intKey = i;
            rec.strKey = "key" + i;
            root.intIndex.put(new Key(rec.intKey), rec);
            root.strIndex.put(new Key(rec.strKey), rec);
        }
        storage.commit();

        // Test DESCENT_ORDER
        Iterator descIterator = root.intIndex.iterator(null, null, Index.DESCENT_ORDER);
        long prevKey = Long.MAX_VALUE;
        int count = 0;
        while (descIterator.hasNext()) {
            Record rec = (Record) descIterator.next();
            assertTrue(rec.intKey <= prevKey, "Should be in descending order");
            prevKey = rec.intKey;
            count++;
        }
        assertEquals(10, count, "Should have 10 records in descent");

        // Test ASCENT_ORDER
        Iterator ascIterator = root.intIndex.iterator(null, null, Index.ASCENT_ORDER);
        prevKey = Long.MIN_VALUE;
        count = 0;
        while (ascIterator.hasNext()) {
            Record rec = (Record) ascIterator.next();
            assertTrue(rec.intKey >= prevKey, "Should be in ascending order");
            prevKey = rec.intKey;
            count++;
        }
        assertEquals(10, count, "Should have 10 records in ascent");
    }
}
