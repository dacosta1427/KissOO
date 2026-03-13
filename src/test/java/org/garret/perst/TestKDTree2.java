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
 * JUnit 5 conversion of tst/TestKDTree2.java
 * Tests K-D tree with custom MultidimensionalComparator
 */
class TestKDTree2 {

    static class Stock extends Persistent {
        String symbol;
        float price;
        int volume;

        boolean eq(Stock s) {
            return symbol.equals(s.symbol) && price == s.price && volume == s.volume;
        }

        boolean le(Stock s) {
            return price <= s.price && volume <= s.volume;
        }
    }

    static class StockComparator extends MultidimensionalComparator<Stock> {
        public int compare(Stock s1, Stock s2, int i) {
            switch (i) {
            case 0:
                if (s1.symbol == null && s2.symbol == null) {
                    return EQ;
                } else if (s1.symbol == null) {
                    return LEFT_UNDEFINED;
                } else if (s2.symbol == null) {
                    return RIGHT_UNDEFINED;
                } else {
                    int diff = s1.symbol.compareTo(s2.symbol);
                    return diff < 0 ? LT : diff == 0 ? EQ : GT;
                }
            case 1:
                return s1.price < s2.price ? LT : s1.price == s2.price ? EQ : GT;
            case 2:
                return s1.volume < s2.volume ? LT : s1.volume == s2.volume ? EQ : GT;
            default:
                throw new IllegalArgumentException();
            }
        }

        public int getNumberOfDimensions() {
            return 3;
        }

        public Stock cloneField(Stock src, int i) {
            Stock clone = new Stock();
            switch (i) {
            case 0:
                clone.symbol = src.symbol;
                break;
            case 1:
                clone.price = src.price;
                break;
            case 2:
                clone.volume = src.volume;
                break;
            default:
                throw new IllegalArgumentException();
            }
            return clone;
        }
    }

    private Storage storage;
    private MultidimensionalIndex index;
    // Scaled down from 100000 records
    private static final int nRecords = 1000;
    private static final int MAX_SYMBOLS = 1000;
    private static final int MAX_PRICE = 100;
    private static final int MAX_VOLUME = 10000;
    private static final int EPSILON = 100;
    private static final String TEST_DB = "testkdtree2.dbs";

    @BeforeEach
    void setUp() throws Exception {
        storage = StorageFactory.getInstance().createStorage();
        storage.open(TEST_DB, 32 * 1024 * 1024);

        // Create the index with custom comparator
        index = storage.createMultidimensionalIndex(new StockComparator());
        storage.setRoot(index);

        // Populate with random data
        Random r = new Random(2007);
        for (int i = 0; i < nRecords; i++) {
            Stock s = getRandomStock(r);
            index.add(s);
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

    private Stock getRandomStock(Random r) {
        Stock s = new Stock();
        s.symbol = Integer.toHexString(r.nextInt(MAX_SYMBOLS));
        s.price = (float) r.nextInt(MAX_PRICE * 10) / 10;
        s.volume = r.nextInt(MAX_VOLUME);
        return s;
    }

    @Test
    @DisplayName("Test KD-tree2 query by example")
    void testKDTree2QueryByExample() {
        Random r = new Random(2007);

        for (int i = 0; i < nRecords; i++) {
            Stock s = getRandomStock(r);
            ArrayList<Stock> result = index.queryByExample(s);
            int n = result.size();
            assertTrue(n >= 1, "Should find at least one result");
            for (int j = 0; j < n; j++) {
                assertTrue(s.eq(result.get(j)), "Result should match query");
            }
        }
    }

    @Test
    @DisplayName("Test KD-tree2 range query")
    void testKDTree2RangeQuery() {
        Random r = new Random(2007);
        long total = 0;

        for (int i = 0; i < nRecords; i++) {
            Stock s = getRandomStock(r);
            Stock min = new Stock();
            Stock max = new Stock();

            min.price = s.price - (float) MAX_PRICE / EPSILON;
            min.volume = s.volume - MAX_VOLUME / EPSILON;

            max.price = s.price + (float) MAX_PRICE / EPSILON;
            max.volume = s.volume + MAX_VOLUME / EPSILON;

            Iterator iterator = index.iterator(min, max);
            int n = 0;
            while (iterator.hasNext()) {
                s = (Stock) iterator.next();
                assertTrue(min.le(s), "Result should be >= min");
                assertTrue(s.le(max), "Result should be <= max");
                n += 1;
            }
            assertTrue(n >= 1, "Should find at least one result in range");
            total += n;
        }
        assertTrue(total > 0, "Should have selected objects");
    }

    @Test
    @DisplayName("Test KD-tree2 iterator")
    void testKDTree2Iterator() {
        Iterator iterator = index.iterator();
        int n = 0;
        while (iterator.hasNext()) {
            iterator.next();
            n += 1;
        }
        assertEquals(nRecords, n, "Should iterate through all records");
    }

    @Test
    @DisplayName("Test KD-tree2 remove")
    void testKDTree2Remove() {
        Iterator iterator = index.iterator();
        int n = 0;
        while (iterator.hasNext()) {
            Stock s = (Stock) iterator.next();
            iterator.remove();
            s.deallocate();
            n += 1;
        }
        storage.commit();

        assertEquals(nRecords, n, "Should have removed all records");
        assertFalse(index.iterator().hasNext(), "Index should be empty after removal");
    }

    @Test
    @DisplayName("Test KD-tree2 clear")
    void testKDTree2Clear() {
        index.clear();
        assertEquals(0, index.size(), "Index size should be 0 after clear");
        assertFalse(index.iterator().hasNext(), "Index should be empty after clear");
    }

    @Test
    @DisplayName("Test KD-tree2 optimize")
    void testKDTree2Optimize() {
        int sizeBefore = index.size();
        index.optimize();

        // Verify index still works after optimization
        int n = 0;
        Iterator iterator = index.iterator();
        while (iterator.hasNext()) {
            iterator.next();
            n += 1;
        }

        assertEquals(sizeBefore, n, "All records should still be accessible after optimization");
    }
}
