package org.garret.perst;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Phase 2B: Tests for L2List (doubly-linked persistent list) and L2ListElem.
 *
 * L2ListElem only provides a default constructor, so Node elements are created
 * with new Node() and associated with storage via storage.makePersistent() when
 * needed for persistent operations.
 */
class L2ListTest {

    /** Minimal concrete L2ListElem subclass for testing. */
    static class Node extends L2ListElem {
        String value;

        Node() {}

        static Node create(Storage s, String v) {
            Node n = new Node();
            n.value = v;
            s.makePersistent(n);
            return n;
        }
    }

    private static final String TEST_DB = "testl2list.dbs";
    private Storage storage;
    private L2List list;

    @BeforeEach
    void setUp() {
        storage = StorageFactory.getInstance().createStorage();
        storage.open(TEST_DB, 4 * 1024 * 1024);
        list = new L2List();
        storage.setRoot(list);
    }

    @AfterEach
    void tearDown() {
        if (storage.isOpened()) {
            storage.close();
        }
        new java.io.File(TEST_DB).delete();
    }

    // ===== isEmpty / size on fresh list =====

    @Test
    @DisplayName("Empty list: isEmpty() true, size() zero, head/tail null")
    void testEmptyList() {
        assertTrue(list.isEmpty(), "Fresh list should be empty");
        assertEquals(0, list.size(), "Fresh list size should be 0");
        assertNull(list.head(), "head() of empty list should be null");
        assertNull(list.tail(), "tail() of empty list should be null");
    }

    // ===== append =====

    @Test
    @DisplayName("append(): elements are ordered, size grows")
    void testAppend() {
        Node a = Node.create(storage, "A");
        Node b = Node.create(storage, "B");
        Node c = Node.create(storage, "C");

        list.append(a);
        list.append(b);
        list.append(c);

        assertFalse(list.isEmpty());
        assertEquals(3, list.size());
        assertEquals(a, list.head(), "First appended element should be head");
        assertEquals(c, list.tail(), "Last appended element should be tail");
    }

    // ===== prepend =====

    @Test
    @DisplayName("prepend(): each prepend becomes new head")
    void testPrepend() {
        Node a = Node.create(storage, "A");
        Node b = Node.create(storage, "B");

        list.prepend(a);
        assertEquals(a, list.head());
        assertEquals(a, list.tail());

        list.prepend(b);
        assertEquals(b, list.head(), "Last prepended element should be head");
        assertEquals(a, list.tail(), "First prepended element stays tail");
        assertEquals(2, list.size());
    }

    // ===== remove element =====

    @Test
    @DisplayName("remove(elem): removes middle element, adjusts links")
    void testRemoveElement() {
        Node a = Node.create(storage, "A");
        Node b = Node.create(storage, "B");
        Node c = Node.create(storage, "C");
        list.append(a);
        list.append(b);
        list.append(c);

        list.remove(b);
        assertEquals(2, list.size());
        assertFalse(list.contains(b), "Removed element should not be in list");
        assertTrue(list.contains(a));
        assertTrue(list.contains(c));
    }

    @Test
    @DisplayName("remove(Object): removes head element")
    void testRemoveHead() {
        Node a = Node.create(storage, "A");
        Node b = Node.create(storage, "B");
        list.append(a);
        list.append(b);

        list.remove((Object) a);
        assertEquals(1, list.size());
        assertEquals(b, list.head());
        assertEquals(b, list.tail());
    }

    // ===== clear =====

    @Test
    @DisplayName("clear(): resets size to 0 and list becomes empty")
    void testClear() {
        Node a = Node.create(storage, "A");
        Node b = Node.create(storage, "B");
        list.append(a);
        list.append(b);

        list.clear();
        assertTrue(list.isEmpty());
        assertEquals(0, list.size());
        assertNull(list.head());
        assertNull(list.tail());
    }

    // ===== contains =====

    @Test
    @DisplayName("contains(): true for member, false for non-member")
    void testContains() {
        Node a = Node.create(storage, "A");
        Node b = Node.create(storage, "B");
        Node notIn = Node.create(storage, "X");
        list.append(a);
        list.append(b);

        assertTrue(list.contains(a));
        assertTrue(list.contains(b));
        assertFalse(list.contains(notIn));
    }

    // ===== add(Object) convenience method =====

    @Test
    @DisplayName("add(Object): delegates to append")
    void testAddObject() {
        Node a = Node.create(storage, "A");
        assertTrue(list.add(a));
        assertEquals(1, list.size());
        assertEquals(a, list.head());
    }

    // ===== forward iterator =====

    @Test
    @DisplayName("iterator(): traverses all elements in insertion order")
    void testForwardIterator() {
        Node a = Node.create(storage, "A");
        Node b = Node.create(storage, "B");
        Node c = Node.create(storage, "C");
        list.append(a);
        list.append(b);
        list.append(c);

        List<String> values = new ArrayList<>();
        Iterator<?> it = list.iterator();
        while (it.hasNext()) {
            values.add(((Node) it.next()).value);
        }

        assertEquals(Arrays.asList("A", "B", "C"), values);
    }

    // ===== iterator remove =====

    @Test
    @DisplayName("iterator.remove(): removes current element during traversal")
    void testIteratorRemove() {
        Node a = Node.create(storage, "A");
        Node b = Node.create(storage, "B");
        Node c = Node.create(storage, "C");
        list.append(a);
        list.append(b);
        list.append(c);

        Iterator<?> it = list.iterator();
        it.next(); // A
        it.next(); // B
        it.remove(); // remove B

        assertEquals(2, list.size());
        assertFalse(list.contains(b));
    }

    // ===== iterator ConcurrentModification =====

    @Test
    @DisplayName("iterator: throws IllegalStateException on concurrent modification")
    void testIteratorConcurrentModification() {
        Node a = Node.create(storage, "A");
        Node b = Node.create(storage, "B");
        list.append(a);
        list.append(b);

        Iterator<?> it = list.iterator();
        it.next(); // move forward once
        // Modify the list externally
        list.append(Node.create(storage, "C"));
        assertThrows(IllegalStateException.class, it::hasNext);
    }

    // ===== iterator NoSuchElement =====

    @Test
    @DisplayName("iterator.next() on exhausted iterator throws NoSuchElementException")
    void testIteratorNoSuchElement() {
        Iterator<?> it = list.iterator(); // empty list
        assertThrows(NoSuchElementException.class, it::next);
    }

    // ===== toArray =====

    @Test
    @DisplayName("toArray(): returns all elements as array")
    void testToArray() {
        Node a = Node.create(storage, "A");
        Node b = Node.create(storage, "B");
        list.append(a);
        list.append(b);

        Object[] arr = list.toArray();
        assertEquals(2, arr.length);
        assertEquals(a, arr[0]);
        assertEquals(b, arr[1]);
    }

    @Test
    @DisplayName("toArray(T[]): fills provided array or allocates new one")
    void testToArrayWithType() {
        Node a = Node.create(storage, "A");
        Node b = Node.create(storage, "B");
        list.append(a);
        list.append(b);

        // Provide array that is too small → new array allocated
        Node[] small = new Node[0];
        Object[] result = list.toArray(small);
        assertEquals(2, result.length);

        // Provide large array → fills it
        Node[] large = new Node[5];
        result = list.toArray(large);
        assertEquals(a, result[0]);
        assertEquals(b, result[1]);
        assertNull(result[2], "Element after end should be null");
    }

    // ===== containsAll =====

    @Test
    @DisplayName("containsAll(): true when all elements present")
    void testContainsAll() {
        Node a = Node.create(storage, "A");
        Node b = Node.create(storage, "B");
        Node c = Node.create(storage, "C");
        list.append(a);
        list.append(b);
        list.append(c);

        assertTrue(list.containsAll(Arrays.asList(a, b)));
        assertFalse(list.containsAll(Arrays.asList(a, Node.create(storage, "X"))));
    }

    // ===== addAll =====

    @Test
    @DisplayName("addAll(): appends all elements from collection")
    void testAddAll() {
        Node a = Node.create(storage, "A");
        Node b = Node.create(storage, "B");
        list.addAll(Arrays.asList(a, b));
        assertEquals(2, list.size());
        assertEquals(a, list.head());
        assertEquals(b, list.tail());
    }

    // ===== removeAll =====

    @Test
    @DisplayName("removeAll(): removes elements that appear in given collection")
    void testRemoveAll() {
        Node a = Node.create(storage, "A");
        Node b = Node.create(storage, "B");
        Node c = Node.create(storage, "C");
        list.append(a);
        list.append(b);
        list.append(c);

        boolean changed = list.removeAll(Arrays.asList(a, c));
        assertTrue(changed);
        assertEquals(1, list.size());
        assertTrue(list.contains(b));
    }

    // ===== retainAll =====

    @Test
    @DisplayName("retainAll(): keeps only elements in given collection")
    void testRetainAll() {
        Node a = Node.create(storage, "A");
        Node b = Node.create(storage, "B");
        Node c = Node.create(storage, "C");
        list.append(a);
        list.append(b);
        list.append(c);

        boolean changed = list.retainAll(Arrays.asList(b));
        assertTrue(changed);
        assertEquals(1, list.size());
        assertTrue(list.contains(b));
        assertFalse(list.contains(a));
        assertFalse(list.contains(c));
    }

    // ===== deallocateMembers =====

    @Test
    @DisplayName("deallocateMembers(): clears list and deallocates elements")
    void testDeallocateMembers() {
        Node a = Node.create(storage, "A");
        Node b = Node.create(storage, "B");
        list.append(a);
        list.append(b);
        storage.commit(); // ensure elements have OIDs

        list.deallocateMembers();
        assertTrue(list.isEmpty(), "List should be empty after deallocateMembers");
        assertEquals(0, list.size());
    }

    // ===== single element edge case =====

    @Test
    @DisplayName("Single element: head == tail, remove makes list empty")
    void testSingleElement() {
        Node a = Node.create(storage, "A");
        list.append(a);

        assertEquals(a, list.head());
        assertEquals(a, list.tail());

        list.remove(a);
        assertTrue(list.isEmpty());
        assertNull(list.head());
        assertNull(list.tail());
    }

    // ===== getNext / getPrev on L2ListElem =====

    @Test
    @DisplayName("L2ListElem.getNext() / getPrev() navigate linked list")
    void testGetNextPrev() {
        Node a = Node.create(storage, "A");
        Node b = Node.create(storage, "B");
        list.append(a);
        list.append(b);

        // Navigate forward from head
        L2ListElem head = list.head();
        assertEquals(a, head);
        L2ListElem second = head.getNext();
        assertEquals(b, second);

        // Navigate backward from tail
        L2ListElem tail = list.tail();
        assertEquals(b, tail);
        assertEquals(a, tail.getPrev());
    }

    // ===== L2ListElem.linkAfter / linkBefore =====

    @Test
    @DisplayName("L2ListElem.linkAfter() inserts element after target")
    void testLinkAfter() {
        Node a = Node.create(storage, "A");
        Node b = Node.create(storage, "B");
        Node mid = Node.create(storage, "MID");
        list.append(a);
        list.append(b);

        a.linkAfter(mid);

        // list should now be a → mid → b
        Iterator<?> it = list.iterator();
        assertEquals(a, it.next());
        assertEquals(mid, it.next());
        assertEquals(b, it.next());
    }

    @Test
    @DisplayName("L2ListElem.linkBefore() inserts element before target")
    void testLinkBefore() {
        Node a = Node.create(storage, "A");
        Node b = Node.create(storage, "B");
        Node mid = Node.create(storage, "MID");
        list.append(a);
        list.append(b);

        b.linkBefore(mid);

        // list should now be a → mid → b
        Iterator<?> it = list.iterator();
        assertEquals(a, it.next());
        assertEquals(mid, it.next());
        assertEquals(b, it.next());
    }

    // ===== L2ListElem.unlink =====

    @Test
    @DisplayName("L2ListElem.unlink() removes element from list")
    void testUnlink() {
        Node a = Node.create(storage, "A");
        Node b = Node.create(storage, "B");
        Node c = Node.create(storage, "C");
        list.append(a);
        list.append(b);
        list.append(c);

        b.unlink();

        // L2List.size is only updated via remove(), not via unlink() directly
        // but elements a and c should now link to each other
        assertEquals(a, list.head());
        assertEquals(c, a.getNext());
    }

    // ===== L2ListElem.prune =====

    @Test
    @DisplayName("L2ListElem.prune() sets self-referencing (circular) pointers")
    void testPrune() {
        Node a = Node.create(storage, "A");
        list.append(a);
        // Prune a standalone node (resets its circular pointer)
        Node standalone = Node.create(storage, "S");
        standalone.prune();
        assertEquals(standalone, standalone.getNext());
        assertEquals(standalone, standalone.getPrev());
    }

    // ===== select (JSQL on list) =====

    @Test
    @DisplayName("select(): JSQL predicate filters list members")
    void testSelect() {
        Node a = Node.create(storage, "Alpha");
        Node b = Node.create(storage, "Beta");
        Node c = Node.create(storage, "Alpha");
        list.append(a);
        list.append(b);
        list.append(c);
        storage.commit();

        IterableIterator<?> iter = list.select(Node.class, "value = 'Alpha'");
        List<Object> found = new ArrayList<>();
        while (iter.hasNext()) {
            found.add(iter.next());
        }
        assertEquals(2, found.size());
    }
}
