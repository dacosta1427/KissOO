package org.garret.perst;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for R-tree spatial index implementations
 */
class TestRtree {

    static class RectangleRecord extends Persistent {
        int x1, y1, x2, y2;
        
        RectangleRecord() {}
        
        RectangleRecord(int x1, int y1, int x2, int y2) {
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
        }
        
        Rectangle getRect() {
            return new Rectangle(x1, y1, x2, y2);
        }
    }

    private Storage storage;
    private static final String TEST_DB = "testrtree.dbs";

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
    @DisplayName("Test R-tree create and insert")
    void testRtreeCreateAndInsert() {
        SpatialIndex<RectangleRecord> rtree = storage.createSpatialIndex();
        storage.setRoot(rtree);
        
        RectangleRecord rect = new RectangleRecord(0, 0, 10, 10);
        rtree.put(rect.getRect(), rect);
        
        assertNotNull(rtree, "R-tree should be created");
    }

    @Test
    @DisplayName("Test R-tree multiple inserts")
    void testRtreeMultipleInserts() {
        SpatialIndex<RectangleRecord> rtree = storage.createSpatialIndex();
        storage.setRoot(rtree);
        
        for (int i = 0; i < 100; i++) {
            RectangleRecord rect = new RectangleRecord(i, i, i + 10, i + 10);
            rtree.put(rect.getRect(), rect);
        }
        
        assertEquals(100, rtree.size(), "R-tree should have 100 elements");
    }

    @Test
    @DisplayName("Test R-tree rectangle intersection query")
    void testRtreeIntersection() {
        SpatialIndex<RectangleRecord> rtree = storage.createSpatialIndex();
        storage.setRoot(rtree);
        
        // Insert some rectangles
        rtree.put(new Rectangle(0, 0, 10, 10), new RectangleRecord(0, 0, 10, 10));
        rtree.put(new Rectangle(20, 20, 30, 30), new RectangleRecord(20, 20, 30, 30));
        rtree.put(new Rectangle(5, 5, 15, 15), new RectangleRecord(5, 5, 15, 15));
        
        storage.commit();
        
        // Query intersection with (5, 5) to (25, 25)
        Rectangle queryRect = new Rectangle(5, 5, 25, 25);
        Object[] results = rtree.get(queryRect);
        
        assertTrue(results.length >= 2, "Should find at least 2 intersecting rectangles");
    }

    @Test
    @DisplayName("Test R-tree iterator")
    void testRtreeIterator() {
        SpatialIndex<RectangleRecord> rtree = storage.createSpatialIndex();
        storage.setRoot(rtree);
        
        for (int i = 0; i < 10; i++) {
            rtree.put(new Rectangle(i, i, i + 5, i + 5), new RectangleRecord(i, i, i + 5, i + 5));
        }
        
        storage.commit();
        
        int count = 0;
        for (RectangleRecord r : rtree) {
            count++;
        }
        assertEquals(10, count, "Should iterate over all 10 elements");
    }

    @Test
    @DisplayName("Test R-tree remove")
    void testRtreeRemove() {
        SpatialIndex<RectangleRecord> rtree = storage.createSpatialIndex();
        storage.setRoot(rtree);
        
        RectangleRecord rect = new RectangleRecord(0, 0, 10, 10);
        rtree.put(rect.getRect(), rect);
        
        storage.commit();
        
        rtree.remove(rect.getRect(), rect);
        
        assertEquals(0, rtree.size(), "R-tree should be empty after removal");
    }

    @Test
    @DisplayName("Test R-tree clear")
    void testRtreeClear() {
        SpatialIndex<RectangleRecord> rtree = storage.createSpatialIndex();
        storage.setRoot(rtree);
        
        for (int i = 0; i < 10; i++) {
            rtree.put(new Rectangle(i, i, i + 5, i + 5), new RectangleRecord(i, i, i + 5, i + 5));
        }
        
        storage.commit();
        
        rtree.clear();
        
        assertEquals(0, rtree.size(), "R-tree should be empty after clear");
    }

    @Test
    @DisplayName("Test R-tree neighbor search")
    void testRtreeNeighborSearch() {
        SpatialIndex<RectangleRecord> rtree = storage.createSpatialIndex();
        storage.setRoot(rtree);
        
        rtree.put(new Rectangle(0, 0, 10, 10), new RectangleRecord(0, 0, 10, 10));
        rtree.put(new Rectangle(100, 100, 110, 110), new RectangleRecord(100, 100, 110, 110));
        
        storage.commit();
        
        // Neighbor iterator
        var results = rtree.neighborIterator(5, 5);
        assertNotNull(results, "Should get neighbor iterator");
    }

    @Test
    @DisplayName("Test R-tree wrapping rectangle")
    void testRtreeWrappingRectangle() {
        SpatialIndex<RectangleRecord> rtree = storage.createSpatialIndex();
        storage.setRoot(rtree);
        
        rtree.put(new Rectangle(0, 0, 10, 10), new RectangleRecord(0, 0, 10, 10));
        rtree.put(new Rectangle(100, 100, 110, 110), new RectangleRecord(100, 100, 110, 110));
        
        storage.commit();
        
        Rectangle wrapping = rtree.getWrappingRectangle();
        assertNotNull(wrapping, "Should get wrapping rectangle");
    }
}
