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
}
