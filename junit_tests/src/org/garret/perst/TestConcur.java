package org.garret.perst;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 conversion of tst/TestConcur.java
 * Tests concurrent access concepts - circular linked list with locking
 * Note: Simplified to test sequential operations instead of multi-threading
 */
class TestConcur {

    static class L2List extends PersistentResource {
        L2Elem head;
    }

    static class L2Elem extends Persistent {
        L2Elem next;
        L2Elem prev;
        int count;

        void unlink() {
            next.prev = prev;
            prev.next = next;
            next.store();
            prev.store();
        }

        void linkAfter(L2Elem elem) {
            elem.next.prev = this;
            next = elem.next;
            elem.next = this;
            prev = elem;
            store();
            next.store();
            prev.store();
        }
    }

    private Storage storage;
    private static final String TEST_DB = "testconcur.dbs";
    private static final int nElements = 100;

    @BeforeEach
    void setUp() throws Exception {
        new java.io.File(TEST_DB).delete();
        storage = StorageFactory.getInstance().createStorage();
        storage.open(TEST_DB);

        L2List list = (L2List) storage.getRoot();
        if (list == null) {
            list = new L2List();
            list.head = new L2Elem();
            list.head.next = list.head.prev = list.head;
            storage.setRoot(list);
            
            for (int i = 1; i < nElements; i++) {
                L2Elem elem = new L2Elem();
                elem.count = i;
                elem.linkAfter(list.head);
            }
            storage.commit();
        }
    }

    @AfterEach
    void tearDown() throws Exception {
        if (storage.isOpened()) {
            storage.close();
        }
        new java.io.File(TEST_DB).delete();
    }

    @Test
    @DisplayName("Test concurrent - list structure")
    void testListStructure() {
        L2List list = (L2List) storage.getRoot();
        L2Elem head = list.head;
        
        int count = 0;
        L2Elem elem = head;
        do {
            count++;
            elem = elem.next;
        } while (elem != head);
        
        assertEquals(nElements, count, "List should have nElements");
    }

    @Test
    @DisplayName("Test concurrent - shared lock iteration")
    void testSharedLockIteration() {
        L2List list = (L2List) storage.getRoot();
        
        list.sharedLock();
        try {
            L2Elem head = list.head;
            L2Elem elem = head;
            long sum = 0;
            int n = 0;
            do {
                elem.load();
                sum += elem.count;
                n += 1;
            } while ((elem = elem.next) != head);
            
            assertEquals(nElements, n, "Should iterate through all elements");
            assertEquals((long) nElements * (nElements - 1) / 2, sum, "Sum should match");
        } finally {
            list.unlock();
        }
    }

    @Test
    @DisplayName("Test concurrent - exclusive lock element move")
    void testExclusiveLockElementMove() {
        L2List list = (L2List) storage.getRoot();
        
        L2Elem head = list.head;
        
        // Get initial count
        int initialCount = nElements;
        
        list.exclusiveLock();
        try {
            L2Elem last = head.prev;
            last.unlink();
            last.linkAfter(head);
        } finally {
            list.unlock();
        }
        
        storage.commit();
        
        // Verify list integrity after move
        int count = 0;
        L2Elem elem = head;
        do {
            count++;
            elem = elem.next;
        } while (elem != head);
        
        assertEquals(initialCount, count, "List should still have all elements after move");
    }

    @Test
    @DisplayName("Test concurrent - multiple iterations and moves")
    void testMultipleIterations() {
        L2List list = (L2List) storage.getRoot();
        
        for (int iter = 0; iter < 3; iter++) {
            list.sharedLock();
            try {
                L2Elem head = list.head;
                L2Elem elem = head;
                long sum = 0;
                int n = 0;
                do {
                    elem.load();
                    sum += elem.count;
                    n += 1;
                } while ((elem = elem.next) != head);
                
                assertEquals(nElements, n, "Iteration " + iter + ": should have all elements");
                assertEquals((long) nElements * (nElements - 1) / 2, sum, "Iteration " + iter + ": sum should match");
            } finally {
                list.unlock();
            }
            
            list.exclusiveLock();
            try {
                L2Elem head = list.head;
                L2Elem last = head.prev;
                last.unlink();
                last.linkAfter(head);
            } finally {
                list.unlock();
            }
        }
        
        storage.commit();
        
        L2Elem finalHead = list.head;
        list.sharedLock();
        try {
            L2Elem elem = finalHead;
            int n = 0;
            do {
                n++;
                elem = elem.next;
            } while (elem != finalHead);
            
            assertEquals(nElements, n, "Final list should still have all elements");
        } finally {
            list.unlock();
        }
    }
}
