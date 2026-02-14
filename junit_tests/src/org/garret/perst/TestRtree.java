package org.garret.perst;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 conversion of tst/TestRtree.java
 * Tests R-tree spatial indexing functionality
 */
class TestRtree {

    static class SpatialObject extends Persistent {
        Rectangle rect;

        public String toString() {
            return rect.toString();
        }
    }

    static class TestRtreeRoot extends Persistent {
        SpatialIndex<SpatialObject> index;
    }

    private Storage storage;
    // Scaled down from 100000 iterations and 1000 objects
    private static final int nObjectsInTree = 100;
    private static final int nIterations = 1000;
    private static final String TEST_DB = "testrtree.dbs";

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
    @DisplayName("Test R-tree insert and search")
    void testRtreeInsertAndSearch() {
        TestRtreeRoot root = new TestRtreeRoot();
        root.index = storage.<SpatialObject>createSpatialIndex();
        storage.setRoot(root);

        Rectangle[] rectangles = new Rectangle[nObjectsInTree];
        long key = 1999;

        for (int i = 0; i < nIterations; i++) {
            int j = i % nObjectsInTree;
            if (i >= nObjectsInTree) {
                Rectangle r = rectangles[j];
                SpatialObject po = null;
                int n = 0;
                for (SpatialObject so : root.index.iterator(r)) {
                    if (r.equals(so.rect)) {
                        po = so;
                    } else {
                        assertTrue(r.intersects(so.rect), "All found objects should intersect with query rectangle");
                    }
                    n += 1;
                }
                assertNotNull(po, "Should find the exact rectangle in the index");

                // Count expected intersections
                for (int k = 0; k < nObjectsInTree; k++) {
                    if (r.intersects(rectangles[k])) {
                        n -= 1;
                    }
                }
                assertEquals(0, n, "Intersection count should match");
                root.index.remove(r, po);
                po.deallocate();
            }

            key = (3141592621L * key + 2718281829L) % 1000000007L;
            int top = (int) (key % 1000);
            int left = (int) (key / 1000 % 1000);
            key = (3141592621L * key + 2718281829L) % 1000000007L;
            int bottom = top + (int) (key % 100);
            int right = left + (int) (key / 100 % 100);

            SpatialObject so = new SpatialObject();
            Rectangle r = new Rectangle(top, left, bottom, right);
            so.rect = r;
            rectangles[j] = r;
            root.index.put(r, so);

            if (i % 100 == 0) {
                storage.commit();
            }
        }
    }

    @Test
    @DisplayName("Test R-tree neighbor iterator")
    void testRtreeNeighborIterator() {
        TestRtreeRoot root = new TestRtreeRoot();
        root.index = storage.<SpatialObject>createSpatialIndex();
        storage.setRoot(root);

        // Insert some rectangles around the origin
        Rectangle r1 = new Rectangle(10, 10, 20, 20);
        Rectangle r2 = new Rectangle(30, 30, 40, 40);
        Rectangle r3 = new Rectangle(50, 50, 60, 60);
        Rectangle r4 = new Rectangle(5, 5, 8, 8);

        SpatialObject so1 = new SpatialObject();
        so1.rect = r1;
        root.index.put(r1, so1);

        SpatialObject so2 = new SpatialObject();
        so2.rect = r2;
        root.index.put(r2, so2);

        SpatialObject so3 = new SpatialObject();
        so3.rect = r3;
        root.index.put(r3, so3);

        SpatialObject so4 = new SpatialObject();
        so4.rect = r4;
        root.index.put(r4, so4);

        storage.commit();

        // Test neighbor iterator starting from origin
        double minDistance = 0;
        int count = 0;
        for (SpatialObject obj : root.index.neighborIterator(0, 0)) {
            double distance = Math.sqrt(obj.rect.getLeft() * obj.rect.getLeft() 
                    + obj.rect.getTop() * obj.rect.getTop());
            assertTrue(distance >= minDistance, "Neighbors should be in increasing distance order");
            minDistance = distance;
            count += 1;
        }

        assertEquals(4, count, "Should find all 4 objects as neighbors");
    }

    @Test
    @DisplayName("Test R-tree clear")
    void testRtreeClear() {
        TestRtreeRoot root = new TestRtreeRoot();
        root.index = storage.<SpatialObject>createSpatialIndex();
        storage.setRoot(root);

        // Insert some rectangles
        for (int i = 0; i < 10; i++) {
            Rectangle r = new Rectangle(i, i, i + 10, i + 10);
            SpatialObject so = new SpatialObject();
            so.rect = r;
            root.index.put(r, so);
        }

        storage.commit();

        // Clear the index
        root.index.clear();

        // Verify index is empty
        Iterator<SpatialObject> iterator = root.index.iterator(new Rectangle(0, 0, 100, 100));
        assertFalse(iterator.hasNext(), "Index should be empty after clear");
    }

    @Test
    @DisplayName("Test R-tree spatial query")
    void testRtreeSpatialQuery() {
        TestRtreeRoot root = new TestRtreeRoot();
        root.index = storage.<SpatialObject>createSpatialIndex();
        storage.setRoot(root);

        // Create overlapping rectangles
        Rectangle r1 = new Rectangle(0, 0, 50, 50);
        Rectangle r2 = new Rectangle(25, 25, 75, 75);
        Rectangle r3 = new Rectangle(100, 100, 150, 150);

        SpatialObject so1 = new SpatialObject();
        so1.rect = r1;
        root.index.put(r1, so1);

        SpatialObject so2 = new SpatialObject();
        so2.rect = r2;
        root.index.put(r2, so2);

        SpatialObject so3 = new SpatialObject();
        so3.rect = r3;
        root.index.put(r3, so3);

        storage.commit();

        // Query for overlapping rectangles
        Rectangle query = new Rectangle(10, 10, 60, 60);
        int count = 0;
        for (SpatialObject so : root.index.iterator(query)) {
            assertTrue(query.intersects(so.rect), "Returned objects should intersect query");
            count += 1;
        }

        assertEquals(2, count, "Should find 2 rectangles overlapping the query");
    }
}
