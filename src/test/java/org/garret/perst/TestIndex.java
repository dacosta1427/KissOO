package org.garret.perst;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;

class TestIndex {

    static class Record extends Persistent {
        String strKey;
        long intKey;
    }

    static class Indices extends Persistent {
        Index<Record> strIndex;
        Index<Record> intIndex;
    }

    private Storage storage;
    private static final int nRecords = 1000;
    private static final String TEST_DB = "testindex.dbs";

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
    @DisplayName("Test index insert and get by long key")
    void testIndexInsertAndGetLongKey() {
        Indices root = new Indices();
        root.intIndex = storage.createIndex(long.class, true);
        storage.setRoot(root);

        Index<Record> intIndex = root.intIndex;

        for (int i = 0; i < nRecords; i++) {
            Record rec = new Record();
            rec.intKey = i;
            rec.strKey = "key" + i;
            intIndex.put(rec.intKey, rec);
        }

        storage.commit();

        for (int i = 0; i < nRecords; i++) {
            Record rec = intIndex.get((long) i);
            assertNotNull(rec, "Record should be found for key " + i);
            assertEquals(i, rec.intKey, "Record intKey should match");
        }
    }

    @Test
    @DisplayName("Test index insert and get by String key")
    void testIndexInsertAndGetStringKey() {
        Indices root = new Indices();
        root.strIndex = storage.createIndex(String.class, true);
        storage.setRoot(root);

        Index<Record> strIndex = root.strIndex;

        for (int i = 0; i < nRecords; i++) {
            Record rec = new Record();
            rec.intKey = i;
            rec.strKey = "key" + i;
            strIndex.put(rec.strKey, rec);
        }

        storage.commit();

        for (int i = 0; i < nRecords; i++) {
            String key = "key" + i;
            Record rec = strIndex.get(key);
            assertNotNull(rec, "Record should be found for key " + key);
            assertEquals("key" + i, rec.strKey, "Record strKey should match");
        }
    }

    @Test
    @DisplayName("Test index contains")
    void testIndexContains() {
        Indices root = new Indices();
        root.intIndex = storage.createIndex(long.class, true);
        storage.setRoot(root);

        Index<Record> intIndex = root.intIndex;

        Record rec = new Record();
        rec.intKey = 42;
        intIndex.put(rec.intKey, rec);
        storage.commit();

        boolean found = false;
        for (Record r : intIndex) {
            if (r.intKey == 42) {
                found = true;
                break;
            }
        }
        assertTrue(found, "Index should contain key 42 via iteration");
    }

    @Test
    @DisplayName("Test index iteration")
    void testIndexIteration() {
        Indices root = new Indices();
        root.intIndex = storage.createIndex(long.class, true);
        storage.setRoot(root);

        Index<Record> intIndex = root.intIndex;

        for (int i = 0; i < 100; i++) {
            Record rec = new Record();
            rec.intKey = i;
            intIndex.put(rec.intKey, rec);
        }

        storage.commit();

        int count = 0;
        long sum = 0;
        for (Record r : intIndex) {
            count++;
            sum += r.intKey;
        }
        assertEquals(100, count, "Should have 100 records");
        assertEquals(4950, sum, "Sum of keys should be 0+1+...+99 = 4950");
    }

    @Test
    @DisplayName("Test index iterator")
    void testIndexIterator() {
        Indices root = new Indices();
        root.intIndex = storage.createIndex(long.class, true);
        storage.setRoot(root);

        Index<Record> intIndex = root.intIndex;

        for (int i = 0; i < nRecords; i++) {
            Record rec = new Record();
            rec.intKey = i;
            intIndex.put(rec.intKey, rec);
        }

        storage.commit();

        Iterator<Record> iterator = intIndex.iterator();
        int count = 0;
        while (iterator.hasNext()) {
            Record rec = iterator.next();
            assertNotNull(rec, "Iterator should return non-null records");
            count++;
        }
        assertEquals(nRecords, count, "Iterator should visit all records");
    }

    @Test
    @DisplayName("Test index size")
    void testIndexSize() {
        Indices root = new Indices();
        root.intIndex = storage.createIndex(long.class, true);
        storage.setRoot(root);

        Index<Record> intIndex = root.intIndex;

        assertEquals(0, intIndex.size(), "Empty index should have size 0");

        for (int i = 0; i < 10; i++) {
            Record rec = new Record();
            rec.intKey = i;
            intIndex.put(rec.intKey, rec);
        }

        assertEquals(10, intIndex.size(), "Index should have 10 elements");
    }

    @Test
    @DisplayName("Test index put multiple keys")
    void testIndexPutMultipleKeys() {
        Indices root = new Indices();
        root.intIndex = storage.createIndex(long.class, true);
        storage.setRoot(root);

        Index<Record> intIndex = root.intIndex;

        for (int i = 0; i < 10; i++) {
            Record rec = new Record();
            rec.intKey = i;
            rec.strKey = "value" + i;
            intIndex.put(rec.intKey, rec);
        }
        storage.commit();

        int count = 0;
        for (Record r : intIndex) {
            count++;
        }
        assertEquals(10, count, "Index should have 10 elements");
    }

    @Test
    @DisplayName("Test index remove by key")
    void testIndexRemoveByKey() {
        Indices root = new Indices();
        root.intIndex = storage.createIndex(long.class, true);
        storage.setRoot(root);

        Index<Record> intIndex = root.intIndex;

        for (int i = 0; i < 10; i++) {
            Record rec = new Record();
            rec.intKey = i;
            intIndex.put(rec.intKey, rec);
        }
        storage.commit();

        // Remove a key using removeKey (for Object key)
        Record removed = intIndex.removeKey((long) 5);
        assertNotNull(removed, "Removed record should not be null");
        assertEquals(5, removed.intKey, "Removed record should have key 5");

        // Verify it's gone
        assertNull(intIndex.get((long) 5), "Key 5 should no longer exist");
        assertEquals(9, intIndex.size(), "Index should have 9 elements after removal");
    }

    @Test
    @DisplayName("Test index range query from key")
    void testIndexRangeFromKey() {
        Indices root = new Indices();
        root.intIndex = storage.createIndex(long.class, true);
        storage.setRoot(root);

        Index<Record> intIndex = root.intIndex;

        for (int i = 0; i < 100; i++) {
            Record rec = new Record();
            rec.intKey = i;
            intIndex.put(rec.intKey, rec);
        }
        storage.commit();

        // Get entries from key 50
        Iterator<Record> iter = intIndex.iterator(new Key((long) 50), null, Index.ASCENT_ORDER);
        int count = 0;
        while (iter.hasNext()) {
            Record r = iter.next();
            assertTrue(r.intKey >= 50, "Key should be >= 50");
            count++;
        }
        assertEquals(50, count, "Should have 50 entries from key 50");
    }

    @Test
    @DisplayName("Test index range query to key")
    void testIndexRangeToKey() {
        Indices root = new Indices();
        root.intIndex = storage.createIndex(long.class, true);
        storage.setRoot(root);

        Index<Record> intIndex = root.intIndex;

        for (int i = 0; i < 100; i++) {
            Record rec = new Record();
            rec.intKey = i;
            intIndex.put(rec.intKey, rec);
        }
        storage.commit();

        // Get entries up to key 50
        Iterator<Record> iter = intIndex.iterator(null, new Key((long) 50), Index.ASCENT_ORDER);
        int count = 0;
        while (iter.hasNext()) {
            Record r = iter.next();
            assertTrue(r.intKey <= 50, "Key should be <= 50");
            count++;
        }
        assertEquals(51, count, "Should have 51 entries up to key 50 (inclusive)");
    }

    @Test
    @DisplayName("Test index range query between keys")
    void testIndexRangeBetweenKeys() {
        Indices root = new Indices();
        root.intIndex = storage.createIndex(long.class, true);
        storage.setRoot(root);

        Index<Record> intIndex = root.intIndex;

        for (int i = 0; i < 100; i++) {
            Record rec = new Record();
            rec.intKey = i;
            intIndex.put(rec.intKey, rec);
        }
        storage.commit();

        // Get entries between key 25 and 75
        Iterator<Record> iter = intIndex.iterator(new Key((long) 25), new Key((long) 75), Index.ASCENT_ORDER);
        int count = 0;
        while (iter.hasNext()) {
            Record r = iter.next();
            assertTrue(r.intKey >= 25 && r.intKey <= 75, "Key should be between 25 and 75");
            count++;
        }
        assertEquals(51, count, "Should have 51 entries between 25 and 75");
    }

    @Test
    @DisplayName("Test index descent order")
    void testIndexDescentOrder() {
        Indices root = new Indices();
        root.intIndex = storage.createIndex(long.class, true);
        storage.setRoot(root);

        Index<Record> intIndex = root.intIndex;

        for (int i = 0; i < 10; i++) {
            Record rec = new Record();
            rec.intKey = i;
            intIndex.put(rec.intKey, rec);
        }
        storage.commit();

        Iterator<Record> iter = intIndex.iterator(null, null, Index.DESCENT_ORDER);
        long prevKey = 10;
        while (iter.hasNext()) {
            Record r = iter.next();
            assertTrue(r.intKey < prevKey, "Keys should be in descending order");
            prevKey = r.intKey;
        }
    }

    @Test
    @DisplayName("Test non-unique index with duplicates")
    void testNonUniqueIndexDuplicates() {
        Indices root = new Indices();
        root.intIndex = storage.createIndex(long.class, false); // non-unique
        storage.setRoot(root);

        Index<Record> intIndex = root.intIndex;

        // Insert multiple records with same key
        for (int i = 0; i < 5; i++) {
            Record rec = new Record();
            rec.intKey = 42; // same key
            rec.strKey = "dup" + i;
            intIndex.put(rec.intKey, rec);
        }
        storage.commit();

        // Get all records with key 42
        Iterator<Record> iter = intIndex.iterator(new Key((long) 42), new Key((long) 42), Index.ASCENT_ORDER);
        int count = 0;
        while (iter.hasNext()) {
            iter.next();
            count++;
        }
        assertEquals(5, count, "Should have 5 records with key 42");
    }

    @Test
    @DisplayName("Test index with int key type")
    void testIndexWithIntKey() {
        Indices root = new Indices();
        root.intIndex = storage.createIndex(int.class, true);
        storage.setRoot(root);

        Index<Record> intIndex = root.intIndex;

        for (int i = 0; i < 10; i++) {
            Record rec = new Record();
            rec.intKey = i;
            assertTrue(intIndex.put(new Key(i), rec), "Put should succeed for key " + i);
        }
        storage.commit();

        for (int i = 0; i < 10; i++) {
            Record rec = intIndex.get(new Key(i));
            assertNotNull(rec, "Record should be found for key " + i);
        }
    }

    @Test
    @DisplayName("Test index with double key type")
    void testIndexWithDoubleKey() {
        Index<Record> dblIndex = storage.createIndex(double.class, true);

        for (int i = 0; i < 10; i++) {
            Record rec = new Record();
            rec.intKey = i;
            double key = i * 1.5;
            dblIndex.put(new Key(key), rec);
        }
        storage.commit();

        for (int i = 0; i < 10; i++) {
            double key = i * 1.5;
            Record rec = dblIndex.get(new Key(key));
            assertNotNull(rec, "Record should be found for key " + key);
        }
    }

    @Test
    @DisplayName("Test index clear")
    void testIndexClear() {
        Indices root = new Indices();
        root.intIndex = storage.createIndex(long.class, true);
        storage.setRoot(root);

        Index<Record> intIndex = root.intIndex;

        for (int i = 0; i < 10; i++) {
            Record rec = new Record();
            rec.intKey = i;
            intIndex.put(rec.intKey, rec);
        }
        storage.commit();

        assertEquals(10, intIndex.size(), "Index should have 10 elements");

        intIndex.clear();
        assertEquals(0, intIndex.size(), "Index should be empty after clear");
    }

    @Test
    @DisplayName("Test index deallocate")
    void testIndexDeallocate() {
        Indices root = new Indices();
        root.intIndex = storage.createIndex(long.class, true);
        storage.setRoot(root);

        Index<Record> intIndex = root.intIndex;

        for (int i = 0; i < 10; i++) {
            Record rec = new Record();
            rec.intKey = i;
            intIndex.put(rec.intKey, rec);
        }
        storage.commit();

        // Deallocate entries
        Iterator<Record> iter = intIndex.iterator();
        while (iter.hasNext()) {
            Record rec = iter.next();
            iter.remove();
            rec.deallocate();
        }
        storage.commit();

        assertEquals(0, intIndex.size(), "Index should be empty after deallocate");
    }

    @Test
    @DisplayName("Test index with Date key type")
    void testIndexWithDateKey() {
        Index<Record> dateIndex = storage.createIndex(java.util.Date.class, true);

        long baseTime = System.currentTimeMillis();
        for (int i = 0; i < 10; i++) {
            Record rec = new Record();
            rec.intKey = i;
            java.util.Date date = new java.util.Date(baseTime + i * 1000);
            dateIndex.put(new Key(date), rec);
        }
        storage.commit();

        java.util.Date searchDate = new java.util.Date(baseTime + 5 * 1000);
        Record rec = dateIndex.get(new Key(searchDate));
        assertNotNull(rec, "Record should be found for date key");
        assertEquals(5, rec.intKey, "Record should have intKey 5");
    }

    @Test
    @DisplayName("Test index entry set")
    void testIndexEntrySet() {
        Indices root = new Indices();
        root.intIndex = storage.createIndex(long.class, true);
        storage.setRoot(root);

        Index<Record> intIndex = root.intIndex;

        for (int i = 0; i < 10; i++) {
            Record rec = new Record();
            rec.intKey = i;
            intIndex.put(rec.intKey, rec);
        }
        storage.commit();

        int count = 0;
        for (Object entry : intIndex.entryIterator()) {
            assertNotNull(entry, "Entry should not be null");
            count++;
        }
        assertEquals(10, count, "Should have 10 entries");
    }

    @Test
    @DisplayName("Test index prefix search for strings")
    void testIndexPrefixSearch() {
        Index<Record> strIndex = storage.createIndex(String.class, true);

        String[] keys = {"apple", "apricot", "banana", "berry", "cherry"};
        for (int i = 0; i < keys.length; i++) {
            Record rec = new Record();
            rec.intKey = i;
            rec.strKey = keys[i];
            strIndex.put(rec.strKey, rec);
        }
        storage.commit();

        // Get entries starting with "ap"
        Iterator<Record> iter = strIndex.prefixIterator("ap");
        int count = 0;
        while (iter.hasNext()) {
            Record r = iter.next();
            assertTrue(r.strKey.startsWith("ap"), "Key should start with 'ap'");
            count++;
        }
        assertEquals(2, count, "Should have 2 entries starting with 'ap'");
    }

    @Test
    @DisplayName("Test index set operation")
    void testIndexSet() {
        Indices root = new Indices();
        root.intIndex = storage.createIndex(long.class, true);
        storage.setRoot(root);

        Index<Record> intIndex = root.intIndex;

        // Insert initial record
        Record rec1 = new Record();
        rec1.intKey = 1;
        rec1.strKey = "first";
        intIndex.set((long) 42, rec1);
        storage.commit();

        // Replace with new record
        Record rec2 = new Record();
        rec2.intKey = 2;
        rec2.strKey = "second";
        Record old = intIndex.set((long) 42, rec2);
        storage.commit();

        assertNotNull(old, "Old record should be returned");
        assertEquals("first", old.strKey, "Old record should have strKey 'first'");
        
        Record current = intIndex.get((long) 42);
        assertEquals("second", current.strKey, "Current record should have strKey 'second'");
    }
}
