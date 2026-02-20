package org.garret.perst;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;

/**
 * Phase 2E: Tests for ScalableList / PersistentList.
 * Exercises PersistentListImpl and its inner pages.
 * Note: ScalableList stores Persistent objects (not plain Strings).
 */
class ScalableListTest {

    private static final String DB = "testscalablelist.dbs";
    private Storage storage;

    /** Simple persistent wrapper for a value */
    public static class Item extends Persistent {
        public String value;
        public int    order;

        public Item() {}
        public Item(Storage s, String value, int order) {
            super(s);
            this.value = value;
            this.order = order;
        }
        @Override public String toString() { return value; }
    }

    @BeforeEach
    void setUp() {
        storage = StorageFactory.getInstance().createStorage();
        storage.open(DB, 4 * 1024 * 1024);
    }

    @AfterEach
    void tearDown() {
        if (storage.isOpened()) storage.close();
        new java.io.File(DB).delete();
    }

    @Test @DisplayName("empty ScalableList")
    void testEmpty() {
        IPersistentList<Item> list = storage.createScalableList();
        storage.setRoot((IPersistent)list);
        assertTrue(list.isEmpty());
        assertEquals(0, list.size());
        assertFalse(list.iterator().hasNext());
    }

    @Test @DisplayName("add and get elements")
    void testAddAndGet() {
        IPersistentList<Item> list = storage.createScalableList();
        storage.setRoot((IPersistent)list);
        Item a = new Item(storage, "alpha", 0);
        Item b = new Item(storage, "beta",  1);
        Item c = new Item(storage, "gamma", 2);
        list.add(a); list.add(b); list.add(c);
        assertEquals(3, list.size());
        assertEquals(a, list.get(0));
        assertEquals(b, list.get(1));
        assertEquals(c, list.get(2));
    }

    @Test @DisplayName("add at index")
    void testAddAtIndex() {
        IPersistentList<Item> list = storage.createScalableList();
        storage.setRoot((IPersistent)list);
        Item a = new Item(storage, "a", 0);
        Item b = new Item(storage, "b", 1);
        Item c = new Item(storage, "c", 2);
        list.add(a); list.add(c);
        list.add(1, b);
        assertEquals(3, list.size());
        assertEquals(a, list.get(0));
        assertEquals(b, list.get(1));
        assertEquals(c, list.get(2));
    }

    @Test @DisplayName("set element")
    void testSet() {
        IPersistentList<Item> list = storage.createScalableList();
        storage.setRoot((IPersistent)list);
        Item old = new Item(storage, "old", 0);
        Item nw  = new Item(storage, "new", 0);
        list.add(old);
        list.set(0, nw);
        assertEquals(nw, list.get(0));
    }

    @Test @DisplayName("remove by index")
    void testRemoveByIndex() {
        IPersistentList<Item> list = storage.createScalableList();
        storage.setRoot((IPersistent)list);
        Item x = new Item(storage, "x", 0);
        Item y = new Item(storage, "y", 1);
        Item z = new Item(storage, "z", 2);
        list.add(x); list.add(y); list.add(z);
        list.remove(1);
        assertEquals(2, list.size());
        assertEquals(x, list.get(0));
        assertEquals(z, list.get(1));
    }

    @Test @DisplayName("remove by object")
    void testRemoveByObject() {
        IPersistentList<Item> list = storage.createScalableList();
        storage.setRoot((IPersistent)list);
        Item h = new Item(storage, "hello", 0);
        Item w = new Item(storage, "world", 1);
        list.add(h); list.add(w);
        boolean removed = list.remove(h);
        assertTrue(removed);
        assertEquals(1, list.size());
        assertFalse(list.remove(new Item(storage, "nothere", 99)));
    }

    @Test @DisplayName("contains")
    void testContains() {
        IPersistentList<Item> list = storage.createScalableList();
        storage.setRoot((IPersistent)list);
        Item found   = new Item(storage, "found", 0);
        Item missing = new Item(storage, "missing", 1);
        list.add(found);
        assertTrue(list.contains(found));
        assertFalse(list.contains(missing));
    }

    @Test @DisplayName("indexOf and lastIndexOf")
    void testIndexOf() {
        IPersistentList<Item> list = storage.createScalableList();
        storage.setRoot((IPersistent)list);
        Item a1 = new Item(storage, "a", 0);
        Item b  = new Item(storage, "b", 1);
        Item a2 = new Item(storage, "a", 2);
        list.add(a1); list.add(b); list.add(a2);
        assertEquals(0, list.indexOf(a1));
        assertEquals(2, list.lastIndexOf(a2));
        assertEquals(-1, list.indexOf(new Item(storage, "z", 99)));
    }

    @Test @DisplayName("subList")
    void testSubList() {
        IPersistentList<Item> list = storage.createScalableList();
        storage.setRoot((IPersistent)list);
        for (int i = 0; i < 5; i++) list.add(new Item(storage, "v"+i, i));
        List<Item> sub = list.subList(1, 4);
        assertEquals(3, sub.size());
        assertEquals("v1", sub.get(0).value);
        assertEquals("v3", sub.get(2).value);
    }

    @Test @DisplayName("clear")
    void testClear() {
        IPersistentList<Item> list = storage.createScalableList();
        storage.setRoot((IPersistent)list);
        list.add(new Item(storage, "a", 0));
        list.add(new Item(storage, "b", 1));
        list.clear();
        assertTrue(list.isEmpty());
    }

    @Test @DisplayName("iterate many elements (forces intermediate pages)")
    void testManyElements() {
        IPersistentList<Item> list = storage.createScalableList(8);
        storage.setRoot((IPersistent)list);
        int N = 200;
        for (int i = 0; i < N; i++) {
            list.add(new Item(storage, "item"+i, i));
        }
        storage.commit();
        assertEquals(N, list.size());

        // Verify all elements in order
        for (int i = 0; i < N; i++) {
            assertEquals(i, list.get(i).order);
        }

        // Iterate via iterator
        int count = 0;
        for (Item v : list) {
            assertEquals(count, v.order);
            count++;
        }
        assertEquals(N, count);
    }

    @Test @DisplayName("toArray")
    void testToArray() {
        IPersistentList<Item> list = storage.createScalableList();
        storage.setRoot((IPersistent)list);
        Item x = new Item(storage, "x", 0);
        Item y = new Item(storage, "y", 1);
        list.add(x); list.add(y);
        Object[] arr = list.toArray();
        assertEquals(2, arr.length);
        assertEquals(x, arr[0]);
        assertEquals(y, arr[1]);
    }

    @Test @DisplayName("addAll")
    void testAddAll() {
        IPersistentList<Item> list = storage.createScalableList();
        storage.setRoot((IPersistent)list);
        Item p = new Item(storage, "p", 0);
        Item q = new Item(storage, "q", 1);
        Item r = new Item(storage, "r", 2);
        List<Item> src = Arrays.asList(p, q, r);
        list.addAll(src);
        assertEquals(3, list.size());
        assertEquals(p, list.get(0));
    }

    @Test @DisplayName("listIterator")
    void testListIterator() {
        IPersistentList<Item> list = storage.createScalableList();
        storage.setRoot((IPersistent)list);
        Item first  = new Item(storage, "first",  0);
        Item second = new Item(storage, "second", 1);
        Item third  = new Item(storage, "third",  2);
        list.add(first); list.add(second); list.add(third);
        ListIterator<Item> it = list.listIterator();
        assertTrue(it.hasNext());
        assertEquals(first,  it.next());
        assertEquals(second, it.next());
        assertTrue(it.hasPrevious());
        assertEquals(second, it.previous());
    }
}
