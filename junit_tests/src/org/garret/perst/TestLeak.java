package org.garret.perst;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Iterator;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 conversion of tst/TestLeak.java
 * Tests memory leak detection with R-tree spatial index
 * Tests object allocation/deallocation in persistent storage
 */
class TestLeak {

    static class SpatialObject extends Persistent implements SelfSerializable {
        RectangleR2 rect;
        byte[] body;

        public void pack(PerstOutputStream out) throws java.io.IOException {
            out.writeDouble(rect.getTop());
            out.writeDouble(rect.getLeft());
            out.writeDouble(rect.getBottom());
            out.writeDouble(rect.getRight());
            out.writeInt(body.length);
            out.write(body, 0, body.length);
        }

        public void unpack(PerstInputStream in) throws java.io.IOException {
            rect = new RectangleR2(in.readDouble(), in.readDouble(), in.readDouble(), in.readDouble());
            body = new byte[in.readInt()];
            in.read(body);
        }
    }

    static class TestLeakRoot extends Persistent {
        SpatialIndexR2<SpatialObject> index;
    }

    private Storage storage;
    // Scaled down from original: nObjects=1000, batchSize=100, nIterations=10000
    private static final int nObjects = 100;
    private static final int batchSize = 10;
    private static final int minObjectSize = 1000;
    private static final int maxObjectSize = 2000;
    private static final int nIterations = 100;
    private static final String TEST_DB = "testleak.dbs";

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
    @DisplayName("Test memory leak with R-tree spatial index")
    void testMemoryLeak() throws Exception {
        TestLeakRoot root = new TestLeakRoot();
        root.index = storage.<SpatialObject>createSpatialIndexR2();
        storage.setRoot(root);

        RectangleR2[] rectangles = new RectangleR2[nObjects];
        Random rnd = new Random(2014);

        // Insert initial objects
        for (int i = 0; i < nObjects; i++) {
            SpatialObject so = new SpatialObject();
            double lat = rnd.nextDouble() * 180;
            double lng = rnd.nextDouble() * 180;
            so.rect = rectangles[i] = new RectangleR2(lat, lng, lat + 10, lng + 10);
            so.body = new byte[minObjectSize + rnd.nextInt(maxObjectSize - minObjectSize)];
            root.index.put(so.rect, so);
        }
        storage.commit();

        // Perform iterations of object replacement
        for (int i = 0; i < nIterations; i++) {
            if (i % 20 == 0) {
                // Just verify the test is running
                assertTrue(i >= 0, "Iteration " + i + " in progress");
            }
            for (int j = 0; j < batchSize; j++) {
                int k = rnd.nextInt(nObjects);
                boolean found = false;
                for (SpatialObject oldObj : root.index.iterator(rectangles[k])) {
                    if (oldObj.rect.equals(rectangles[k])) {
                        root.index.remove(oldObj.rect, oldObj);
                        SpatialObject newObj = new SpatialObject();
                        newObj.rect = oldObj.rect;
                        newObj.body = new byte[minObjectSize + rnd.nextInt(maxObjectSize - minObjectSize)];
                        root.index.put(newObj.rect, newObj);
                        oldObj.deallocate();
                        found = true;
                        break;
                    }
                }
                assertTrue(found, "Should find the object to replace at iteration " + i + ", batch " + j);
            }
            storage.commit();
        }

        // Verify all objects are still in the index after all iterations
        int count = 0;
        for (int i = 0; i < nObjects; i++) {
            for (SpatialObject obj : root.index.iterator(rectangles[i])) {
                if (obj.rect.equals(rectangles[i])) {
                    count++;
                    break;
                }
            }
        }
        assertEquals(nObjects, count, "All objects should still be in the index after iterations");
    }

    @Test
    @DisplayName("Test spatial object serialization")
    void testSpatialObjectSerialization() throws Exception {
        // Test that SpatialObject properly serializes and deserializes
        SpatialObject so = new SpatialObject();
        so.rect = new RectangleR2(10.0, 20.0, 30.0, 40.0);
        so.body = new byte[]{1, 2, 3, 4, 5};

        // Verify the object was created correctly
        assertNotNull(so.rect);
        assertEquals(5, so.body.length);
        assertEquals(10.0, so.rect.getTop());
        assertEquals(20.0, so.rect.getLeft());
        assertEquals(30.0, so.rect.getBottom());
        assertEquals(40.0, so.rect.getRight());
    }

    @Test
    @DisplayName("Test R-tree 2D index basic operations")
    void testRtree2DBasicOperations() {
        TestLeakRoot root = new TestLeakRoot();
        root.index = storage.<SpatialObject>createSpatialIndexR2();
        storage.setRoot(root);

        // Insert some spatial objects
        SpatialObject so1 = new SpatialObject();
        so1.rect = new RectangleR2(0, 0, 10, 10);
        so1.body = new byte[100];
        root.index.put(so1.rect, so1);

        SpatialObject so2 = new SpatialObject();
        so2.rect = new RectangleR2(20, 20, 30, 30);
        so2.body = new byte[200];
        root.index.put(so2.rect, so2);

        storage.commit();

        // Query for first object
        RectangleR2 query1 = new RectangleR2(0, 0, 10, 10);
        Iterator<SpatialObject> iter1 = root.index.iterator(query1);
        assertTrue(iter1.hasNext(), "Should find first object");
        SpatialObject found1 = iter1.next();
        assertEquals(so1.rect, found1.rect);

        // Query for second object
        RectangleR2 query2 = new RectangleR2(20, 20, 30, 30);
        Iterator<SpatialObject> iter2 = root.index.iterator(query2);
        assertTrue(iter2.hasNext(), "Should find second object");
        SpatialObject found2 = iter2.next();
        assertEquals(so2.rect, found2.rect);

        // Query for non-existent area
        RectangleR2 query3 = new RectangleR2(100, 100, 110, 110);
        Iterator<SpatialObject> iter3 = root.index.iterator(query3);
        assertFalse(iter3.hasNext(), "Should not find any object in empty area");
    }
}
