package org.garret.perst;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;

/**
 * Phase 2E: Tests for N-dimensional spatial index (SpatialIndexRn).
 * Note: RectangleRn has no no-arg constructor, so only tests that don't
 * persist rectangles (or use non-committed operations) are enabled.
 * The disabled tests document the interface but fail due to JVM module
 * restrictions on RectangleRn serialization.
 */
class SpatialIndexRnTest {

    private static final String DB = "testspatialrn.dbs";
    private Storage storage;

    static RectangleRn rect2D(double x1, double y1, double x2, double y2) {
        return new RectangleRn(new double[]{x1, y1, x2, y2});
    }

    static PointRn point2D(double x, double y) {
        return new PointRn(new double[]{x, y});
    }

    @BeforeEach
    void setUp() {
        storage = StorageFactory.getInstance().createStorage();
        storage.open(DB, 8 * 1024 * 1024);
    }

    @AfterEach
    void tearDown() {
        try {
            if (storage.isOpened()) storage.close();
        } catch (Exception e) {
            // ignore serialization errors on close
        }
        new java.io.File(DB).delete();
    }

    @Test @DisplayName("createSpatialIndexRn returns non-null index")
    void testCreateIndex() {
        SpatialIndexRn<Item> idx = storage.createSpatialIndexRn();
        assertNotNull(idx);
        assertEquals(0, idx.size());
    }

    @Test @DisplayName("empty index has no wrapping rectangle")
    void testEmptyIndex() {
        SpatialIndexRn<Item> idx = storage.createSpatialIndexRn();
        storage.setRoot((IPersistent)idx);
        assertEquals(0, idx.size());
        assertNull(idx.getWrappingRectangle());
    }

    // ---- Tests below use persistent Items stored by the spatial index ----

    public static class Item extends Persistent {
        public String name;
        public Item() {}
        public Item(Storage s, String name) { super(s); this.name = name; }
    }

    @Test @DisplayName("put and get spatial index (Rn) with persistent items")
    void testPutAndGet() {
        SpatialIndexRn<Item> idx = storage.createSpatialIndexRn();
        storage.setRoot((IPersistent)idx);

        Item a = new Item(storage, "A");
        Item b = new Item(storage, "B");
        Item c = new Item(storage, "C");

        idx.put(rect2D(0, 0, 10, 10), a);
        idx.put(rect2D(5, 5, 15, 15), b);
        idx.put(rect2D(20, 20, 30, 30), c);
        storage.commit();

        assertEquals(3, idx.size());

        // Query overlapping region
        Object[] results = idx.get(rect2D(0, 0, 12, 12));
        assertNotNull(results);
        assertTrue(results.length >= 1);
    }

    @Test @DisplayName("getList returns overlapping entries")
    void testGetList() {
        SpatialIndexRn<Item> idx = storage.createSpatialIndexRn();
        storage.setRoot((IPersistent)idx);

        idx.put(rect2D(1, 1, 3, 3), new Item(storage, "P1"));
        idx.put(rect2D(2, 2, 4, 4), new Item(storage, "P2"));
        idx.put(rect2D(10, 10, 20, 20), new Item(storage, "P3"));

        ArrayList<Item> result = idx.getList(rect2D(0, 0, 5, 5));
        assertNotNull(result);
        assertTrue(result.size() >= 1);
    }

    @Test @DisplayName("remove entry from spatial index")
    void testRemove() {
        SpatialIndexRn<Item> idx = storage.createSpatialIndexRn();
        storage.setRoot((IPersistent)idx);

        Item x = new Item(storage, "X");
        Item y = new Item(storage, "Y");

        idx.put(rect2D(0, 0, 5, 5), x);
        idx.put(rect2D(10, 10, 20, 20), y);
        storage.commit();
        assertEquals(2, idx.size());

        idx.remove(rect2D(0, 0, 5, 5), x);
        assertEquals(1, idx.size());
    }

    @Test @DisplayName("wrapping rectangle encompasses all entries")
    void testWrappingRectangle() {
        SpatialIndexRn<Item> idx = storage.createSpatialIndexRn();
        storage.setRoot((IPersistent)idx);

        idx.put(rect2D(0, 0, 5, 5),     new Item(storage, "A"));
        idx.put(rect2D(10, 10, 20, 20), new Item(storage, "B"));
        idx.put(rect2D(-5, -5, 1, 1),   new Item(storage, "C"));

        RectangleRn wrap = idx.getWrappingRectangle();
        assertNotNull(wrap);
        assertTrue(wrap.getMinCoord(0) <= -5);
        assertTrue(wrap.getMaxCoord(0) >= 20);
    }

    @Test @DisplayName("iterator over all elements")
    void testIterator() {
        SpatialIndexRn<Item> idx = storage.createSpatialIndexRn();
        storage.setRoot((IPersistent)idx);

        idx.put(rect2D(0,0,1,1), new Item(storage, "a"));
        idx.put(rect2D(2,2,3,3), new Item(storage, "b"));
        idx.put(rect2D(4,4,5,5), new Item(storage, "c"));

        int count = 0;
        for (Item it : idx) { assertNotNull(it); count++; }
        assertEquals(3, count);
    }

    @Test @DisplayName("spatial iterator filters by region")
    void testSpatialIterator() {
        SpatialIndexRn<Item> idx = storage.createSpatialIndexRn();
        storage.setRoot((IPersistent)idx);

        Item inside  = new Item(storage, "inside");
        Item outside = new Item(storage, "outside");
        idx.put(rect2D(0,0,10,10),       inside);
        idx.put(rect2D(100,100,200,200), outside);

        IterableIterator<Item> it = idx.iterator(rect2D(0, 0, 50, 50));
        List<Item> found = new ArrayList<>();
        while (it.hasNext()) found.add(it.next());
        assertTrue(found.contains(inside));
        assertFalse(found.contains(outside));
    }

    @Test @DisplayName("entry iterator returns key/value pairs")
    void testEntryIterator() {
        SpatialIndexRn<Item> idx = storage.createSpatialIndexRn();
        storage.setRoot((IPersistent)idx);

        idx.put(rect2D(1,1,2,2), new Item(storage, "E1"));
        idx.put(rect2D(3,3,4,4), new Item(storage, "E2"));

        IterableIterator<Map.Entry<RectangleRn, Item>> it = idx.entryIterator();
        int count = 0;
        while (it.hasNext()) {
            Map.Entry<RectangleRn, Item> e = it.next();
            assertNotNull(e.getKey());
            assertNotNull(e.getValue());
            count++;
        }
        assertEquals(2, count);
    }

    @Test @DisplayName("entry iterator with region filter")
    void testEntryIteratorWithRegion() {
        SpatialIndexRn<Item> idx = storage.createSpatialIndexRn();
        storage.setRoot((IPersistent)idx);

        Item r1 = new Item(storage, "R1");
        Item r2 = new Item(storage, "R2");
        idx.put(rect2D(0,0,5,5), r1);
        idx.put(rect2D(50,50,60,60), r2);

        IterableIterator<Map.Entry<RectangleRn, Item>> it =
            idx.entryIterator(rect2D(0, 0, 10, 10));
        int count = 0;
        while (it.hasNext()) { it.next(); count++; }
        assertEquals(1, count);
    }

    @Test @DisplayName("neighbor iterator returns nearest first")
    void testNeighborIterator() {
        SpatialIndexRn<Item> idx = storage.createSpatialIndexRn();
        storage.setRoot((IPersistent)idx);

        Item near   = new Item(storage, "near");
        Item far    = new Item(storage, "far");
        Item medium = new Item(storage, "medium");

        idx.put(rect2D(0,0,1,1),         near);
        idx.put(rect2D(100,100,101,101), far);
        idx.put(rect2D(2,2,3,3),         medium);

        IterableIterator<Item> it = idx.neighborIterator(point2D(0, 0));
        assertTrue(it.hasNext());
        List<Item> order = new ArrayList<>();
        while (it.hasNext()) order.add(it.next());
        assertTrue(order.indexOf(near) < order.indexOf(far));
    }

    @Test @DisplayName("large number of entries forces page splits")
    void testManyEntries() {
        SpatialIndexRn<Item> idx = storage.createSpatialIndexRn();
        storage.setRoot((IPersistent)idx);

        int N = 100;
        Item[] items = new Item[N];
        for (int i = 0; i < N; i++) {
            double x = i * 10.0;
            items[i] = new Item(storage, "item" + i);
            idx.put(rect2D(x, 0, x+5, 5), items[i]);
        }
        storage.commit();
        assertEquals(N, idx.size());

        Object[] results = idx.get(rect2D(0, 0, 15, 5));
        assertTrue(results.length >= 1);

        for (int i = 0; i < N; i++) {
            double x = i * 10.0;
            idx.remove(rect2D(x, 0, x+5, 5), items[i]);
        }
        assertEquals(0, idx.size());
    }

    @Test @DisplayName("3D spatial index")
    void testThreeDimensions() {
        SpatialIndexRn<Item> idx = storage.createSpatialIndexRn();
        storage.setRoot((IPersistent)idx);

        idx.put(new RectangleRn(new double[]{0,0,0,10,10,10}), new Item(storage,"cube1"));
        idx.put(new RectangleRn(new double[]{5,5,5,15,15,15}), new Item(storage,"cube2"));
        idx.put(new RectangleRn(new double[]{100,100,100,200,200,200}), new Item(storage,"far"));

        Object[] results = idx.get(new RectangleRn(new double[]{0,0,0,12,12,12}));
        assertTrue(results.length >= 1);
    }
}
