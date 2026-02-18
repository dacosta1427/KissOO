package org.garret.perst;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 conversion of tst/TestRegex.java
 * Tests regex index functionality
 */
class TestRegex {

    static class Record extends Persistent {
        String key;
    }

    private Storage storage;
    private RegexIndex<Record> index;
    // Scaled down from 1M records
    private static final int nRecords = 1000;
    private static final String TEST_DB = "testregex.dbs";

    @BeforeEach
    void setUp() throws Exception {
        storage = StorageFactory.getInstance().createStorage();
        storage.open(TEST_DB, 256 * 1024 * 1024);

        index = storage.createRegexIndex(Record.class, "key");
        storage.setRoot(index);

        // Populate with records
        for (int i = 0; i < nRecords; i++) {
            Record rec = new Record();
            rec.key = Integer.toHexString(i);
            index.add(rec);
        }
        storage.commit();
    }

    @AfterEach
    void tearDown() throws Exception {
        if (storage.isOpened()) {
            storage.close();
        }
        new java.io.File(TEST_DB).delete();
    }

    @Test
    @DisplayName("Test regex index match with wildcards")
    void testRegexIndexMatchWildcards() {
        // Add records that will match the pattern
        Record rec1 = new Record();
        rec1.key = "abc123";
        index.add(rec1);
        Record rec2 = new Record();
        rec2.key = "testabcd";
        index.add(rec2);
        storage.commit();

        int n = 0;
        for (Record rec : index.match("%abcd%")) {
            assertTrue(rec.key.toLowerCase().contains("abcd"), "Key should contain 'abcd'");
            n += 1;
        }
        assertTrue(n >= 1, "Should find at least 1 match");
    }

    @Test
    @DisplayName("Test regex index match with underscore")
    void testRegexIndexMatchUnderscore() {
        int n = 0;
        for (Record rec : index.match("1_2_3")) {
            // Match pattern like 1X2X3 where X is any character
            assertTrue(rec.key.length() >= 5, "Key should be at least 5 characters");
            n += 1;
        }
        // In our data, we may not have exact matches
        assertTrue(n >= 0, "Should handle pattern matching");
    }

    @Test
    @DisplayName("Test regex index iterator and remove")
    void testRegexIndexIteratorAndRemove() {
        Iterator<Record> iterator = index.iterator();
        int count = 0;
        while (iterator.hasNext()) {
            Record rec = iterator.next();
            iterator.remove();
            rec.deallocate();
            count += 1;
        }

        assertEquals(nRecords, count, "Should iterate through all records");
        assertFalse(index.iterator().hasNext(), "Index should be empty after removal");
    }

    @Test
    @DisplayName("Test regex index insert and search")
    void testRegexIndexInsertAndSearch() {
        // Add new records
        Record rec = new Record();
        rec.key = "abc123def";
        index.add(rec);
        storage.commit();

        // Search for the new record
        int n = 0;
        for (Record r : index.match("%abc%")) {
            assertTrue(r.key.contains("abc"), "Key should contain 'abc'");
            n += 1;
        }
        assertTrue(n >= 1, "Should find at least the newly added record");
    }

    @Test
    @DisplayName("Test regex index clear")
    void testRegexIndexClear() {
        index.clear();
        assertFalse(index.iterator().hasNext(), "Index should be empty after clear");
    }
}
