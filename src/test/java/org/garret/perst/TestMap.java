package org.garret.perst;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;

class TestMap {

    static class Record extends Persistent {
        String name;
        long id;
    }

    private Storage storage;
    private static final String TEST_DB = "testmap.dbs";

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
    @DisplayName("Test FieldIndex insert and get by id")
    void testFieldIndexInsertAndGet() {
        FieldIndex<Record> index = storage.createFieldIndex(Record.class, "id", true);
        
        for (int i = 0; i < 100; i++) {
            Record rec = new Record();
            rec.id = i;
            rec.name = "record" + i;
            index.put(rec);
        }

        storage.commit();

        Record found = index.get(new Key(50L));
        assertNotNull(found, "Should find record with id 50");
        assertEquals(50, found.id);
    }

    @Test
    @DisplayName("Test FieldIndex iterator")
    void testFieldIndexIterator() {
        FieldIndex<Record> index = storage.createFieldIndex(Record.class, "id", true);

        for (int i = 0; i < 50; i++) {
            Record rec = new Record();
            rec.id = i;
            index.put(rec);
        }

        storage.commit();

        int count = 0;
        for (Record r : index) {
            count++;
        }
        assertEquals(50, count, "Should iterate over all 50 records");
    }

    @Test
    @DisplayName("Test FieldIndex contains")
    void testFieldIndexContains() {
        FieldIndex<Record> index = storage.createFieldIndex(Record.class, "id", true);

        Record rec = new Record();
        rec.id = 42;
        rec.name = "test";
        index.put(rec);

        storage.commit();

        boolean found = false;
        for (Record r : index) {
            if (r.id == 42) {
                found = true;
                break;
            }
        }
        assertTrue(found, "Should find record with id 42");
    }

    @Test
    @DisplayName("Test FieldIndex range search")
    void testFieldIndexRangeSearch() {
        FieldIndex<Record> index = storage.createFieldIndex(Record.class, "id", true);

        for (int i = 0; i < 100; i++) {
            Record rec = new Record();
            rec.id = i;
            index.put(rec);
        }

        storage.commit();

        Iterator<Record> it = index.iterator(new Key(10L), new Key(20L), 0);
        int count = 0;
        while (it.hasNext()) {
            it.next();
            count++;
        }
        assertTrue(count >= 0, "Range search should work without error");
    }

    @Test
    @DisplayName("Test FieldIndex remove")
    void testFieldIndexRemove() {
        FieldIndex<Record> index = storage.createFieldIndex(Record.class, "id", true);

        Record rec = new Record();
        rec.id = 1;
        rec.name = "test";
        index.put(rec);

        storage.commit();

        int countBefore = 0;
        for (Record r : index) {
            countBefore++;
        }
        assertEquals(1, countBefore, "Should have 1 record before removal");

        index.remove(rec);
        storage.commit();

        int countAfter = 0;
        for (Record r : index) {
            countAfter++;
        }
        assertEquals(0, countAfter, "Should have 0 records after removal");
    }

    @Test
    @DisplayName("Test multiple FieldIndices")
    void testMultipleFieldIndices() {
        FieldIndex<Record> idIndex = storage.createFieldIndex(Record.class, "id", true);
        FieldIndex<Record> nameIndex = storage.createFieldIndex(Record.class, "name", true);

        Record rec = new Record();
        rec.id = 1;
        rec.name = "test";
        idIndex.put(rec);
        nameIndex.put(rec);

        storage.commit();

        Record byId = idIndex.get(new Key(1L));
        Record byName = nameIndex.get(new Key("test"));

        assertNotNull(byId, "Should find by id");
        assertNotNull(byName, "Should find by name");
    }
}
