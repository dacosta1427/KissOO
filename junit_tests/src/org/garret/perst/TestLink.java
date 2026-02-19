package org.garret.perst;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for Link implementations
 */
class TestLink {

    static class LinkRecord extends Persistent {
        String name;
        
        LinkRecord() {}
        
        LinkRecord(String name) {
            this.name = name;
        }
    }

    private Storage storage;
    private static final String TEST_DB = "testlink.dbs";

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
    @DisplayName("Test Link create and add")
    void testLinkCreateAndAdd() {
        Link<LinkRecord> link = storage.createLink();
        
        LinkRecord rec1 = new LinkRecord("rec1");
        link.add(rec1);
        
        assertNotNull(link, "Link should be created");
        assertEquals(1, link.size(), "Link should have 1 element");
    }

    @Test
    @DisplayName("Test Link add multiple elements")
    void testLinkAddMultiple() {
        Link<LinkRecord> link = storage.createLink();
        
        for (int i = 0; i < 10; i++) {
            link.add(new LinkRecord("rec" + i));
        }
        
        assertEquals(10, link.size(), "Link should have 10 elements");
    }

    @Test
    @DisplayName("Test Link get by index")
    void testLinkGet() {
        Link<LinkRecord> link = storage.createLink();
        
        LinkRecord rec1 = new LinkRecord("rec1");
        link.add(rec1);
        
        storage.commit();
        
        LinkRecord retrieved = link.get(0);
        assertNotNull(retrieved, "Should retrieve element");
        assertEquals("rec1", retrieved.name);
    }

    @Test
    @DisplayName("Test Link set element")
    void testLinkSet() {
        Link<LinkRecord> link = storage.createLink();
        
        link.add(new LinkRecord("rec1"));
        link.add(new LinkRecord("rec2"));
        
        storage.commit();
        
        LinkRecord newRec = new LinkRecord("newRec");
        link.set(0, newRec);
        
        storage.commit();
        
        assertEquals("newRec", link.get(0).name);
    }

    @Test
    @DisplayName("Test Link remove element")
    void testLinkRemove() {
        Link<LinkRecord> link = storage.createLink();
        
        link.add(new LinkRecord("rec1"));
        link.add(new LinkRecord("rec2"));
        
        storage.commit();
        
        link.remove(0);
        
        assertEquals(1, link.size(), "Link should have 1 element after removal");
    }

    @Test
    @DisplayName("Test Link iterator")
    void testLinkIterator() {
        Link<LinkRecord> link = storage.createLink();
        
        for (int i = 0; i < 5; i++) {
            link.add(new LinkRecord("rec" + i));
        }
        
        storage.commit();
        
        int count = 0;
        for (LinkRecord rec : link) {
            count++;
        }
        assertEquals(5, count, "Should iterate over all 5 elements");
    }

    @Test
    @DisplayName("Test Link clear")
    void testLinkClear() {
        Link<LinkRecord> link = storage.createLink();
        
        for (int i = 0; i < 10; i++) {
            link.add(new LinkRecord("rec" + i));
        }
        
        storage.commit();
        
        link.clear();
        
        assertEquals(0, link.size(), "Link should be empty after clear");
    }

    @Test
    @DisplayName("Test Link contains")
    void testLinkContains() {
        Link<LinkRecord> link = storage.createLink();
        
        LinkRecord rec1 = new LinkRecord("rec1");
        link.add(rec1);
        
        storage.commit();
        
        assertTrue(link.contains(rec1), "Link should contain the record");
    }

    @Test
    @DisplayName("Test Link indexOf")
    void testLinkIndexOf() {
        Link<LinkRecord> link = storage.createLink();
        
        LinkRecord rec1 = new LinkRecord("rec1");
        link.add(rec1);
        link.add(new LinkRecord("rec2"));
        
        storage.commit();
        
        int index = link.indexOf(rec1);
        assertEquals(0, index, "rec1 should be at index 0");
    }

    // ============================================
    // LinkImpl.SubList tests
    // ============================================

    @Test
    @DisplayName("Test Link subList basic operations")
    void testLinkSubListBasic() {
        Link<LinkRecord> link = storage.createLink();
        
        for (int i = 0; i < 10; i++) {
            link.add(new LinkRecord("rec" + i));
        }
        storage.commit();
        
        // Get subList from index 2 to 7 (5 elements)
        java.util.List<LinkRecord> subList = link.subList(2, 7);
        
        assertEquals(5, subList.size(), "SubList should have 5 elements");
        assertEquals("rec2", subList.get(0).name, "First element should be rec2");
        assertEquals("rec6", subList.get(4).name, "Last element should be rec6");
    }

    @Test
    @DisplayName("Test Link subList iteration")
    void testLinkSubListIteration() {
        Link<LinkRecord> link = storage.createLink();
        
        for (int i = 0; i < 10; i++) {
            link.add(new LinkRecord("rec" + i));
        }
        storage.commit();
        
        java.util.List<LinkRecord> subList = link.subList(3, 8);
        
        int count = 0;
        for (LinkRecord rec : subList) {
            assertEquals("rec" + (3 + count), rec.name);
            count++;
        }
        assertEquals(5, count, "Should iterate over 5 elements");
    }

    @Test
    @DisplayName("Test Link subList set")
    void testLinkSubListSet() {
        Link<LinkRecord> link = storage.createLink();
        
        for (int i = 0; i < 5; i++) {
            link.add(new LinkRecord("rec" + i));
        }
        storage.commit();
        
        java.util.List<LinkRecord> subList = link.subList(1, 4);
        
        LinkRecord newRec = new LinkRecord("newRec");
        LinkRecord old = subList.set(1, newRec);
        
        assertEquals("rec2", old.name, "Old value should be rec2");
        assertEquals("newRec", subList.get(1).name, "New value should be at index 1");
        assertEquals("newRec", link.get(2).name, "Original list should reflect change");
    }

    @Test
    @DisplayName("Test Link subList add")
    void testLinkSubListAdd() {
        Link<LinkRecord> link = storage.createLink();
        
        for (int i = 0; i < 5; i++) {
            link.add(new LinkRecord("rec" + i));
        }
        storage.commit();
        
        java.util.List<LinkRecord> subList = link.subList(1, 4);
        
        LinkRecord newRec = new LinkRecord("inserted");
        subList.add(1, newRec);
        
        assertEquals(4, subList.size(), "SubList should have 4 elements after add");
        assertEquals(6, link.size(), "Original list should have 6 elements");
        assertEquals("inserted", subList.get(1).name);
    }

    @Test
    @DisplayName("Test Link subList remove")
    void testLinkSubListRemove() {
        Link<LinkRecord> link = storage.createLink();
        
        for (int i = 0; i < 5; i++) {
            link.add(new LinkRecord("rec" + i));
        }
        storage.commit();
        
        java.util.List<LinkRecord> subList = link.subList(1, 4);
        
        LinkRecord removed = subList.remove(1);
        
        assertEquals("rec2", removed.name, "Removed element should be rec2");
        assertEquals(2, subList.size(), "SubList should have 2 elements after remove");
        assertEquals(4, link.size(), "Original list should have 4 elements");
    }

    @Test
    @DisplayName("Test Link subList addAll")
    void testLinkSubListAddAll() {
        Link<LinkRecord> link = storage.createLink();
        
        for (int i = 0; i < 5; i++) {
            link.add(new LinkRecord("rec" + i));
        }
        storage.commit();
        
        java.util.List<LinkRecord> subList = link.subList(1, 4);
        
        java.util.List<LinkRecord> toAdd = new java.util.ArrayList<>();
        toAdd.add(new LinkRecord("new1"));
        toAdd.add(new LinkRecord("new2"));
        
        boolean modified = subList.addAll(toAdd);
        
        assertTrue(modified, "addAll should return true");
        assertEquals(5, subList.size(), "SubList should have 5 elements");
        assertEquals(7, link.size(), "Original list should have 7 elements");
    }

    @Test
    @DisplayName("Test Link subList addAll at index")
    void testLinkSubListAddAllAtIndex() {
        Link<LinkRecord> link = storage.createLink();
        
        for (int i = 0; i < 5; i++) {
            link.add(new LinkRecord("rec" + i));
        }
        storage.commit();
        
        java.util.List<LinkRecord> subList = link.subList(1, 4);
        
        java.util.List<LinkRecord> toAdd = new java.util.ArrayList<>();
        toAdd.add(new LinkRecord("new1"));
        toAdd.add(new LinkRecord("new2"));
        
        boolean modified = subList.addAll(1, toAdd);
        
        assertTrue(modified, "addAll should return true");
        assertEquals(5, subList.size(), "SubList should have 5 elements");
        assertEquals("new1", subList.get(1).name);
        assertEquals("new2", subList.get(2).name);
    }

    @Test
    @DisplayName("Test Link subList listIterator")
    void testLinkSubListListIterator() {
        Link<LinkRecord> link = storage.createLink();
        
        for (int i = 0; i < 5; i++) {
            link.add(new LinkRecord("rec" + i));
        }
        storage.commit();
        
        java.util.List<LinkRecord> subList = link.subList(1, 4);
        java.util.ListIterator<LinkRecord> iter = subList.listIterator();
        
        assertTrue(iter.hasNext(), "Should have next");
        assertEquals(0, iter.nextIndex(), "Next index should be 0");
        assertEquals("rec1", iter.next().name);
        assertEquals(1, iter.nextIndex(), "Next index should be 1");
        
        assertTrue(iter.hasPrevious(), "Should have previous");
        assertEquals(0, iter.previousIndex(), "Previous index should be 0");
        assertEquals("rec1", iter.previous().name);
    }

    @Test
    @DisplayName("Test Link subList removeRange")
    void testLinkSubListRemoveRange() {
        Link<LinkRecord> link = storage.createLink();
        
        for (int i = 0; i < 10; i++) {
            link.add(new LinkRecord("rec" + i));
        }
        storage.commit();
        
        java.util.List<LinkRecord> subList = link.subList(2, 8);
        assertEquals(6, subList.size(), "SubList should have 6 elements");
        
        // Test clear on sublist which removes elements
        subList.clear();
        
        assertEquals(0, subList.size(), "SubList should have 0 elements after clear");
        assertEquals(4, link.size(), "Original list should have 4 elements (10 - 6 cleared)");
    }

    @Test
    @DisplayName("Test Link subList of subList")
    void testLinkSubListOfSubList() {
        Link<LinkRecord> link = storage.createLink();
        
        for (int i = 0; i < 10; i++) {
            link.add(new LinkRecord("rec" + i));
        }
        storage.commit();
        
        java.util.List<LinkRecord> subList1 = link.subList(2, 8);
        java.util.List<LinkRecord> subList2 = subList1.subList(1, 4);
        
        assertEquals(3, subList2.size(), "Nested subList should have 3 elements");
        assertEquals("rec3", subList2.get(0).name, "First element should be rec3");
        assertEquals("rec5", subList2.get(2).name, "Last element should be rec5");
    }

    @Test
    @DisplayName("Test Link subList boundaries")
    void testLinkSubListBoundaries() {
        Link<LinkRecord> link = storage.createLink();
        
        for (int i = 0; i < 5; i++) {
            link.add(new LinkRecord("rec" + i));
        }
        storage.commit();
        
        // Full list as subList
        java.util.List<LinkRecord> full = link.subList(0, 5);
        assertEquals(5, full.size(), "Full subList should match original");
        
        // Empty subList
        java.util.List<LinkRecord> empty = link.subList(2, 2);
        assertEquals(0, empty.size(), "Empty subList should have 0 elements");
        assertTrue(empty.isEmpty(), "Empty subList should be empty");
    }

    @Test
    @DisplayName("Test Link subList exceptions")
    void testLinkSubListExceptions() {
        Link<LinkRecord> link = storage.createLink();
        
        for (int i = 0; i < 5; i++) {
            link.add(new LinkRecord("rec" + i));
        }
        storage.commit();
        
        // fromIndex > toIndex
        assertThrows(IllegalArgumentException.class, () -> link.subList(3, 1));
        
        // fromIndex < 0
        assertThrows(IndexOutOfBoundsException.class, () -> link.subList(-1, 3));
        
        // toIndex > size
        assertThrows(IndexOutOfBoundsException.class, () -> link.subList(1, 10));
    }

    @Test
    @DisplayName("Test Link subList iterator remove")
    void testLinkSubListIteratorRemove() {
        Link<LinkRecord> link = storage.createLink();
        
        for (int i = 0; i < 5; i++) {
            link.add(new LinkRecord("rec" + i));
        }
        storage.commit();
        
        java.util.List<LinkRecord> subList = link.subList(1, 4);
        java.util.Iterator<LinkRecord> iter = subList.iterator();
        
        iter.next();
        iter.remove();
        
        assertEquals(2, subList.size(), "SubList should have 2 elements after iterator remove");
        assertEquals(4, link.size(), "Original list should have 4 elements");
    }

    @Test
    @DisplayName("Test Link subList listIterator add")
    void testLinkSubListListIteratorAdd() {
        Link<LinkRecord> link = storage.createLink();
        
        for (int i = 0; i < 5; i++) {
            link.add(new LinkRecord("rec" + i));
        }
        storage.commit();
        
        java.util.List<LinkRecord> subList = link.subList(1, 4);
        java.util.ListIterator<LinkRecord> iter = subList.listIterator();
        
        iter.add(new LinkRecord("inserted"));
        
        assertEquals(4, subList.size(), "SubList should have 4 elements after iterator add");
        assertEquals(6, link.size(), "Original list should have 6 elements");
        assertEquals("inserted", subList.get(0).name);
    }
}
