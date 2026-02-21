package org.garret.perst;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;

class TestList {

    static class Record extends Persistent {
        int i;
    }

    private Storage storage;
    private static final int nRecords = 1000;
    private static final String TEST_DB = "testlist.dbs";

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
    @DisplayName("Test list insert and get")
    void testListInsertAndGet() {
        IPersistentList root = storage.createList();
        storage.setRoot(root);

        for (int i = 0; i < nRecords; i++) {
            Record rec = new Record();
            rec.i = i;
            root.add(rec);
        }

        storage.commit();

        for (int i = 0; i < nRecords; i++) {
            Record rec = (Record) root.get(i);
            assertEquals(i, rec.i, "Record value should match index");
        }
    }

    @Test
    @DisplayName("Test list iterator")
    void testListIterator() {
        IPersistentList root = storage.createList();
        storage.setRoot(root);

        for (int i = 0; i < nRecords; i++) {
            Record rec = new Record();
            rec.i = i;
            root.add(rec);
        }

        storage.commit();

        Iterator iterator = root.iterator();
        int count = 0;
        while (iterator.hasNext()) {
            Record rec = (Record) iterator.next();
            assertEquals(count, rec.i, "Iterator value should match");
            count++;
        }
        assertEquals(nRecords, count, "Iterator should visit all records");
    }

    @Test
    @DisplayName("Test list remove")
    void testListRemove() {
        IPersistentList root = storage.createList();
        storage.setRoot(root);

        for (int i = 0; i < nRecords; i++) {
            Record rec = new Record();
            rec.i = i;
            root.add(rec);
        }

        storage.commit();

        for (int i = 0; i < nRecords; i++) {
            Record rec = (Record) root.remove(0);
            assertEquals(i, rec.i, "Removed record should match");
            rec.deallocate();
        }

        assertFalse(root.iterator().hasNext(), "List should be empty after removal");
    }

    @Test
    @DisplayName("Test list insert at index")
    void testListInsertAtIndex() {
        IPersistentList root = storage.createList();
        storage.setRoot(root);

        root.add(new Record());

        Record rec = new Record();
        rec.i = 42;
        root.add(0, rec);

        assertEquals(42, ((Record) root.get(0)).i, "Insert at index should work");
        assertEquals(2, root.size(), "List should have 2 elements");
    }

    @Test
    @DisplayName("Test list size")
    void testListSize() {
        IPersistentList root = storage.createList();
        storage.setRoot(root);

        assertEquals(0, root.size(), "Empty list should have size 0");

        for (int i = 0; i < 10; i++) {
            root.add(new Record());
        }

        assertEquals(10, root.size(), "List should have 10 elements");
    }

    @Test
    @DisplayName("Test list contains")
    void testListContains() {
        IPersistentList root = storage.createList();
        storage.setRoot(root);

        Record rec1 = new Record();
        rec1.i = 1;
        root.add(rec1);

        Record rec2 = new Record();
        rec2.i = 2;
        root.add(rec2);

        assertTrue(root.contains(rec1), "List should contain rec1");
        assertTrue(root.contains(rec2), "List should contain rec2");
    }

    @Test
    @DisplayName("Test list clear")
    void testListClear() {
        IPersistentList root = storage.createList();
        storage.setRoot(root);

        for (int i = 0; i < 10; i++) {
            root.add(new Record());
        }
        storage.commit();

        assertEquals(10, root.size(), "List should have 10 elements");

        root.clear();
        storage.commit();

        assertEquals(0, root.size(), "List should be empty after clear");
    }

    @Test
    @DisplayName("Test list indexOf")
    void testListIndexOf() {
        IPersistentList root = storage.createList();
        storage.setRoot(root);

        Record rec1 = new Record();
        rec1.i = 1;
        Record rec2 = new Record();
        rec2.i = 2;
        Record rec3 = new Record();
        rec3.i = 3;

        root.add(rec1);
        root.add(rec2);
        root.add(rec3);
        storage.commit();

        assertEquals(0, root.indexOf(rec1), "rec1 should be at index 0");
        assertEquals(1, root.indexOf(rec2), "rec2 should be at index 1");
        assertEquals(2, root.indexOf(rec3), "rec3 should be at index 2");
    }

    @Test
    @DisplayName("Test list set")
    void testListSet() {
        IPersistentList root = storage.createList();
        storage.setRoot(root);

        for (int i = 0; i < 5; i++) {
            Record rec = new Record();
            rec.i = i;
            root.add(rec);
        }
        storage.commit();

        Record newRec = new Record();
        newRec.i = 100;
        Record old = (Record) root.set(2, newRec);
        
        assertEquals(2, old.i, "Old record should have value 2");
        assertEquals(100, ((Record) root.get(2)).i, "New record should be at index 2");
    }

    @Test
    @DisplayName("Test list lastIndexOf")
    void testListLastIndexOf() {
        IPersistentList root = storage.createList();
        storage.setRoot(root);

        Record rec1 = new Record();
        rec1.i = 1;
        Record rec2 = new Record();
        rec2.i = 2;

        root.add(rec1);
        root.add(rec2);
        root.add(rec1); // Add rec1 again
        storage.commit();

        assertEquals(2, root.lastIndexOf(rec1), "rec1 last index should be 2");
        assertEquals(1, root.lastIndexOf(rec2), "rec2 last index should be 1");
    }

    @Test
    @DisplayName("Test list toArray")
    void testListToArray() {
        IPersistentList root = storage.createList();
        storage.setRoot(root);

        for (int i = 0; i < 5; i++) {
            Record rec = new Record();
            rec.i = i;
            root.add(rec);
        }
        storage.commit();

        Object[] arr = root.toArray();
        assertEquals(5, arr.length, "Array should have 5 elements");
    }

    @Test
    @DisplayName("Test list addAll")
    void testListAddAll() {
        IPersistentList root = storage.createList();
        storage.setRoot(root);

        java.util.List<Record> toAdd = new java.util.ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Record rec = new Record();
            rec.i = i;
            toAdd.add(rec);
        }

        root.addAll(toAdd);
        storage.commit();

        assertEquals(5, root.size(), "List should have 5 elements");
    }

    @Test
    @DisplayName("Test list isEmpty")
    void testListIsEmpty() {
        IPersistentList root = storage.createList();
        storage.setRoot(root);

        assertTrue(root.isEmpty(), "New list should be empty");

        root.add(new Record());
        assertFalse(root.isEmpty(), "List with elements should not be empty");

        root.clear();
        assertTrue(root.isEmpty(), "Cleared list should be empty");
    }
}
