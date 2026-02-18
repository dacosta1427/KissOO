package org.garret.perst;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 conversion of tst/TestRaw.java
 * Tests raw data access functionality
 */
class TestRaw {

    static class L1List extends Persistent {
        L1List next;
        Object obj;
        Object root;

        L1List() {
        }

        L1List(Object value, Object tree, L1List list) {
            obj = value;
            root = tree;
            next = list;
        }
    }

    static class ListItem extends Persistent {
        int id;

        ListItem() {
        }

        ListItem(int id) {
            this.id = id;
        }
    }

    static class TestRawRoot extends Persistent {
        L1List list;
        ArrayList<ListItem> array;
        Object nil;
    }

    private Storage storage;
    private static final int nListMembers = 100;
    private static final int nArrayElements = 1000;
    private static final String TEST_DB = "testraw.dbs";

    @BeforeEach
    void setUp() throws Exception {
        storage = StorageFactory.getInstance().createStorage();
        storage.open(TEST_DB, 48 * 1024 * 1024);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (storage.isOpened()) {
            storage.close();
        }
        new java.io.File(TEST_DB).delete();
    }

    @Test
    @DisplayName("Test raw linked list access")
    void testRawLinkedListAccess() {
        TestRawRoot root = new TestRawRoot();
        storage.setRoot(root);

        // Create linked list
        L1List list = null;
        for (int i = 0; i < nListMembers; i++) {
            list = new L1List(new Integer(i), root, list);
        }
        root.list = list;

        // Create array
        root.array = new ArrayList<ListItem>(nArrayElements);
        for (int i = 0; i < nArrayElements; i++) {
            root.array.add(new ListItem(i));
        }

        root.store();
        storage.commit();

        // Verify linked list
        L1List current = root.list;
        for (int i = nListMembers - 1; i >= 0; i--) {
            assertEquals(new Integer(i), current.obj, "List object should match expected value");
            assertEquals(root, current.root, "List root should reference root object");
            current = current.next;
        }
    }

    @Test
    @DisplayName("Test raw array access")
    void testRawArrayAccess() {
        TestRawRoot root = new TestRawRoot();
        storage.setRoot(root);

        // Create array
        root.array = new ArrayList<ListItem>(nArrayElements);
        for (int i = 0; i < nArrayElements; i++) {
            root.array.add(new ListItem(i));
        }

        root.store();
        storage.commit();

        // Verify array elements
        for (int i = nArrayElements - 1; i >= 0; i--) {
            assertEquals(i, root.array.get(i).id, "Array element ID should match index");
        }
    }

    @Test
    @DisplayName("Test raw data persistence")
    void testRawDataPersistence() {
        TestRawRoot root = new TestRawRoot();
        storage.setRoot(root);

        // Create linked list
        L1List list = null;
        for (int i = 0; i < nListMembers; i++) {
            list = new L1List(new Integer(i), root, list);
        }
        root.list = list;

        // Create array
        root.array = new ArrayList<ListItem>(nArrayElements);
        for (int i = 0; i < nArrayElements; i++) {
            root.array.add(new ListItem(i));
        }

        root.store();
        storage.commit();
        storage.close();

        // Reopen and verify
        storage = StorageFactory.getInstance().createStorage();
        storage.open(TEST_DB);

        TestRawRoot reloadedRoot = (TestRawRoot) storage.getRoot();

        // Verify linked list after reload
        L1List current = reloadedRoot.list;
        for (int i = nListMembers - 1; i >= 0; i--) {
            assertNotNull(current, "List element should not be null");
            assertEquals(new Integer(i), current.obj, "Reloaded list object should match");
            current = current.next;
        }

        // Verify array after reload
        for (int i = nArrayElements - 1; i >= 0; i--) {
            assertEquals(i, reloadedRoot.array.get(i).id, "Reloaded array element ID should match");
        }

        storage.close();
        new java.io.File(TEST_DB).delete();
    }
}
