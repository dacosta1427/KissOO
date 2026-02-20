package org.garret.perst;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;

/**
 * Phase 2F: Tests for PersistentListImpl with ListIntermediatePage.
 * nLeafPageItems = (8192-8-8)/4 = 2044, so >2044 items forces ListIntermediatePage.
 * Also exercises ListItr (listIterator) and ListPage.
 */
class PersistentListPageTest {

    private static final String DB = "testplistpage.dbs";
    private Storage storage;

    public static class Item extends Persistent {
        public int seq;
        public Item() {}
        public Item(Storage s, int seq) { super(s); this.seq = seq; }
    }

    @BeforeEach
    void setUp() {
        storage = StorageFactory.getInstance().createStorage();
        storage.open(DB, 32 * 1024 * 1024);
    }

    @AfterEach
    void tearDown() {
        if (storage.isOpened()) storage.close();
        new java.io.File(DB).delete();
    }

    @Test @DisplayName("createList: empty list")
    void testEmpty() {
        IPersistentList<Item> list = storage.createList();
        storage.setRoot((IPersistent)list);
        assertTrue(list.isEmpty());
        assertEquals(0, list.size());
    }

    @Test @DisplayName("createList: add and get")
    void testAddGet() {
        IPersistentList<Item> list = storage.createList();
        storage.setRoot((IPersistent)list);
        Item a = new Item(storage, 1);
        Item b = new Item(storage, 2);
        list.add(a); list.add(b);
        assertEquals(2, list.size());
        assertEquals(a, list.get(0));
        assertEquals(b, list.get(1));
    }

    @Test @DisplayName("createList: listIterator exercises ListItr")
    void testListIterator() {
        IPersistentList<Item> list = storage.createList();
        storage.setRoot((IPersistent)list);
        for (int i = 0; i < 10; i++) list.add(new Item(storage, i));

        // listIterator() calls PersistentListImpl.listIterator which creates ListItr
        ListIterator<Item> it = list.listIterator();
        int count = 0;
        while (it.hasNext()) {
            Item item = it.next();
            assertEquals(count, item.seq);
            count++;
        }
        assertEquals(10, count);
    }

    @Test @DisplayName("createList: listIterator(index) starts at given position")
    void testListIteratorFromIndex() {
        IPersistentList<Item> list = storage.createList();
        storage.setRoot((IPersistent)list);
        for (int i = 0; i < 5; i++) list.add(new Item(storage, i));

        ListIterator<Item> it = list.listIterator(2);
        assertEquals(2, it.next().seq);
    }

    @Test @DisplayName("createList: listIterator hasPrevious and previous")
    void testListIteratorPrevious() {
        IPersistentList<Item> list = storage.createList();
        storage.setRoot((IPersistent)list);
        list.add(new Item(storage, 10));
        list.add(new Item(storage, 20));
        list.add(new Item(storage, 30));

        ListIterator<Item> it = list.listIterator(3); // at end
        assertTrue(it.hasPrevious());
        assertEquals(30, it.previous().seq);
        assertEquals(20, it.previous().seq);
        assertTrue(it.hasPrevious()); // still item at index 0
    }

    @Test @DisplayName("createList: set via listIterator")
    void testListIteratorSet() {
        IPersistentList<Item> list = storage.createList();
        storage.setRoot((IPersistent)list);
        list.add(new Item(storage, 1));
        list.add(new Item(storage, 2));

        ListIterator<Item> it = list.listIterator();
        it.next(); // at position 1
        it.set(new Item(storage, 99));
        assertEquals(99, list.get(0).seq);
    }

    @Test @DisplayName("createList: nextIndex and previousIndex")
    void testListIteratorIndex() {
        IPersistentList<Item> list = storage.createList();
        storage.setRoot((IPersistent)list);
        for (int i = 0; i < 4; i++) list.add(new Item(storage, i));

        ListIterator<Item> it = list.listIterator(2);
        assertEquals(2, it.nextIndex());
        assertEquals(1, it.previousIndex());
    }

    @Test @DisplayName("createList: LARGE list forces ListIntermediatePage (>2044 items)")
    void testLargeListIntermediatePage() {
        IPersistentList<Item> list = storage.createList();
        storage.setRoot((IPersistent)list);

        // 2200 items forces at least one intermediate page
        int N = 2200;
        for (int i = 0; i < N; i++) {
            list.add(new Item(storage, i));
        }
        storage.commit();

        assertEquals(N, list.size());

        // Verify random access via get() (exercises ListIntermediatePage.getPosition)
        assertEquals(0,      list.get(0).seq);
        assertEquals(1000,   list.get(1000).seq);
        assertEquals(N - 1,  list.get(N-1).seq);

        // Iterate all (exercises ListIntermediatePage iteration)
        int count = 0;
        for (Item item : list) {
            assertEquals(count, item.seq);
            count++;
        }
        assertEquals(N, count);

        // listIterator over large list
        ListIterator<Item> it = list.listIterator(N/2);
        assertEquals(N/2, it.next().seq);
    }

    @Test @DisplayName("createList: remove from large list (exercises ListIntermediatePage.remove)")
    void testRemoveFromLargeList() {
        IPersistentList<Item> list = storage.createList();
        storage.setRoot((IPersistent)list);

        int N = 2200;
        for (int i = 0; i < N; i++) {
            list.add(new Item(storage, i));
        }
        storage.commit();

        // Remove from middle
        list.remove(1100);
        assertEquals(N - 1, list.size());
        assertEquals(1101, list.get(1100).seq);

        // Remove from start
        list.remove(0);
        assertEquals(N - 2, list.size());
        assertEquals(1, list.get(0).seq);
    }

    @Test @DisplayName("createList: add at index in large list")
    void testAddAtIndexLargeList() {
        IPersistentList<Item> list = storage.createList();
        storage.setRoot((IPersistent)list);

        int N = 2200;
        for (int i = 0; i < N; i++) {
            list.add(new Item(storage, i));
        }
        storage.commit();

        // Insert in middle
        Item newItem = new Item(storage, 9999);
        list.add(1000, newItem);
        assertEquals(N + 1, list.size());
        assertEquals(9999, list.get(1000).seq);
        assertEquals(1000, list.get(1001).seq);
    }

    @Test @DisplayName("createList: subList view")
    void testSubList() {
        IPersistentList<Item> list = storage.createList();
        storage.setRoot((IPersistent)list);
        for (int i = 0; i < 10; i++) list.add(new Item(storage, i));

        List<Item> sub = list.subList(2, 7);
        assertEquals(5, sub.size());
        assertEquals(2, sub.get(0).seq);
        assertEquals(6, sub.get(4).seq);
    }

    @Test @DisplayName("createList: clear large list")
    void testClearLargeList() {
        IPersistentList<Item> list = storage.createList();
        storage.setRoot((IPersistent)list);
        int N = 2200;
        for (int i = 0; i < N; i++) list.add(new Item(storage, i));
        storage.commit();
        list.clear();
        assertTrue(list.isEmpty());
        assertEquals(0, list.size());
    }
}
