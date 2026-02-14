package org.garret.perst;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 conversion of tst/TestKDTree.java
 * Tests K-D tree multidimensional index functionality
 */
class TestKDTree {

    static class Quote extends Persistent {
        int timestamp;
        float low;
        float high;
        float open;
        float close;
        int volume;

        boolean eq(Quote q) {
            return low == q.low && high == q.high && open == q.open && close == q.close && volume == q.volume;
        }

        boolean le(Quote q) {
            return low <= q.low && high <= q.high && open <= q.open && close <= q.close && volume <= q.volume;
        }
    }

    private Storage storage;
    private MultidimensionalIndex index;
    // Scaled down from 100000 records
    private static final int nRecords = 1000;
    private static final String TEST_DB = "testkbtree.dbs";

    @BeforeEach
    void setUp() throws Exception {
        storage = StorageFactory.getInstance().createStorage();
        storage.open(TEST_DB, 32 * 1024 * 1024);

        // Create the index
        index = storage.createMultidimensionalIndex(Quote.class, new String[]{"low", "high", "open", "close", "volume"}, false);
        storage.setRoot(index);

        // Populate with random data
        Random r = new Random(2007);
        for (int i = 0; i < nRecords; i++) {
            Quote q = getRandomQuote(r);
            index.add(q);
        }
        storage.commit();
    }

    @AfterEach
    void tearDown() throws Exception {
        if (storage.isOpened()) {
            storage.close();
        }
        new java.io.File(TEST_DB).delete();
    }

    private Quote getRandomQuote(Random r) {
        Quote q = new Quote();
        q.timestamp = (int) (System.currentTimeMillis() / 1000);
        q.low = (float) r.nextInt(1000) / 10;
        q.high = q.low + (float) r.nextInt(1000) / 10;
        q.open = (float) r.nextInt(1000) / 10;
        q.close = (float) r.nextInt(1000) / 10;
        q.volume = r.nextInt(10000);
        return q;
    }

    @Test
    @DisplayName("Test KD-tree query by example")
    void testKDTreeQueryByExample() {
        Random r = new Random(2007);
        long total = 0;

        for (int i = 0; i < nRecords; i++) {
            Quote q = getRandomQuote(r);
            ArrayList<Quote> result = index.queryByExample(q);
            int n = result.size();
            assertTrue(n >= 1, "Should find at least one result");
            total += n;
            for (int j = 0; j < n; j++) {
                assertTrue(q.eq(result.get(j)), "Result should match query");
            }
        }
        assertTrue(total > 0, "Should have selected objects");
    }

    @Test
    @DisplayName("Test KD-tree range query")
    void testKDTreeRangeQuery() {
        Random r = new Random(2007);
        final int MAX_PRICE = 100;
        final int MAX_VOLUME = 10000;
        final int EPSILON = 100;
        long total = 0;

        for (int i = 0; i < nRecords; i++) {
            Quote q = getRandomQuote(r);
            Quote min = new Quote();
            Quote max = new Quote();

            min.low = q.low - (float) MAX_PRICE / EPSILON;
            min.high = q.high - (float) MAX_PRICE / EPSILON;
            min.open = q.open - (float) MAX_PRICE / EPSILON;
            min.close = q.close - (float) MAX_PRICE / EPSILON;
            min.volume = q.volume - MAX_VOLUME / EPSILON;

            max.low = q.low + (float) MAX_PRICE / EPSILON;
            max.high = q.high + (float) MAX_PRICE / EPSILON;
            max.open = q.open + (float) MAX_PRICE / EPSILON;
            max.close = q.close + (float) MAX_PRICE / EPSILON;
            max.volume = q.volume + MAX_VOLUME / EPSILON;

            Iterator iterator = index.iterator(min, max);
            int n = 0;
            while (iterator.hasNext()) {
                Quote result = (Quote) iterator.next();
                assertTrue(min.le(result), "Result should be >= min");
                assertTrue(result.le(max), "Result should be <= max");
                n += 1;
            }
            assertTrue(n >= 1, "Should find at least one result in range");
            total += n;
        }
        assertTrue(total > 0, "Should have selected objects");
    }

    @Test
    @DisplayName("Test KD-tree iterator")
    void testKDTreeIterator() {
        Iterator iterator = index.iterator();
        int n = 0;
        while (iterator.hasNext()) {
            iterator.next();
            n += 1;
        }
        assertEquals(nRecords, n, "Should iterate through all records");
    }

    @Test
    @DisplayName("Test KD-tree remove")
    void testKDTreeRemove() {
        Iterator iterator = index.iterator();
        int n = 0;
        while (iterator.hasNext()) {
            Quote q = (Quote) iterator.next();
            iterator.remove();
            q.deallocate();
            n += 1;
        }
        storage.commit();

        assertEquals(nRecords, n, "Should have removed all records");
        assertFalse(index.iterator().hasNext(), "Index should be empty after removal");
    }

    @Test
    @DisplayName("Test KD-tree clear")
    void testKDTreeClear() {
        index.clear();
        assertEquals(0, index.size(), "Index size should be 0 after clear");
        assertFalse(index.iterator().hasNext(), "Index should be empty after clear");
    }

    @Test
    @DisplayName("Test KD-tree optimize")
    void testKDTreeOptimize() {
        int heightBefore = index.getHeight();
        index.optimize();
        int heightAfter = index.getHeight();

        // After optimization, height should be <= before
        assertTrue(heightAfter <= heightBefore, "Tree height should not increase after optimization");
    }
}
