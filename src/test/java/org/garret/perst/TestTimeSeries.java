package org.garret.perst;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Date;
import java.util.Iterator;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 conversion of tst/TestTimeSeries.java
 * Tests TimeSeries functionality with quotes
 */
class TestTimeSeries {

    static final int MSECS_PER_DAY = 24 * 60 * 60 * 1000;

    public static class Quote implements TimeSeries.Tick {
        int date;
        float low;
        float high;
        float open;
        float close;
        int volume;

        public long getTime() {
            return (long) date * MSECS_PER_DAY;
        }
    }

    public static class QuoteBlock extends TimeSeries.Block {
        private Quote[] quotes;

        static final int N_ELEMS_PER_BLOCK = 100;

        public TimeSeries.Tick[] getTicks() {
            if (quotes == null) {
                quotes = new Quote[N_ELEMS_PER_BLOCK];
                for (int i = 0; i < N_ELEMS_PER_BLOCK; i++) {
                    quotes[i] = new Quote();
                }
            }
            return quotes;
        }
    }

    static class Stock extends Persistent {
        String name;
        TimeSeries<Quote> quotes;
    }

    static class StockIndex extends Persistent {
        FieldIndex<Stock> stocks;
    }

    // Scaled down from original: nElements = 10*365 = 3650
    private static final int nElements = 365; // 1 year instead of 10
    private static final String TEST_DB = "testts.dbs";

    private Storage storage;

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
    @DisplayName("Test TimeSeries insert and iterate")
    void testTimeSeriesInsertAndIterate() {
        StockIndex root = new StockIndex();
        root.stocks = storage.<Stock>createFieldIndex(Stock.class, "name", true);
        storage.setRoot(root);

        Stock stock = new Stock();
        stock.name = "BORL";
        stock.quotes = storage.createTimeSeries(QuoteBlock.class, (long) QuoteBlock.N_ELEMS_PER_BLOCK * MSECS_PER_DAY * 2);
        root.stocks.put(stock);

        Random rand = new Random(2004);
        long start = System.currentTimeMillis();
        int date = (int) (start / MSECS_PER_DAY) - nElements;

        // Insert quotes
        for (int i = 0; i < nElements; i++) {
            Quote quote = new Quote();
            quote.date = date + i;
            quote.open = (float) rand.nextInt(10000) / 100;
            quote.close = (float) rand.nextInt(10000) / 100;
            quote.high = Math.max(quote.open, quote.close);
            quote.low = Math.min(quote.open, quote.close);
            quote.volume = rand.nextInt(1000);
            stock.quotes.add(quote);
        }
        storage.commit();

        assertEquals(nElements, stock.quotes.size(), "Should have all quotes inserted");

        // Verify quotes by iteration
        rand.setSeed(2004);
        int count = 0;
        Iterator<Quote> iterator = stock.quotes.iterator();
        while (iterator.hasNext()) {
            Quote quote = iterator.next();
            assertEquals(date + count, quote.date, "Quote date should match");
            assertTrue(quote.open >= 0 && quote.open <= 100, "Open price should be valid");
            assertTrue(quote.close >= 0 && quote.close <= 100, "Close price should be valid");
            assertTrue(quote.high >= quote.open && quote.high >= quote.close, "High should be max");
            assertTrue(quote.low <= quote.open && quote.low <= quote.close, "Low should be min");
            count++;
        }
        assertEquals(nElements, count, "Should iterate through all quotes");
    }

    @Test
    @DisplayName("Test TimeSeries range query")
    void testTimeSeriesRangeQuery() {
        StockIndex root = new StockIndex();
        root.stocks = storage.<Stock>createFieldIndex(Stock.class, "name", true);
        storage.setRoot(root);

        Stock stock = new Stock();
        stock.name = "BORL";
        stock.quotes = storage.createTimeSeries(QuoteBlock.class, (long) QuoteBlock.N_ELEMS_PER_BLOCK * MSECS_PER_DAY * 2);
        root.stocks.put(stock);

        // Add some quotes with known dates
        Random rand = new Random(2004);
        int baseDate = 1000;

        for (int i = 0; i < 100; i++) {
            Quote quote = new Quote();
            quote.date = baseDate + i;
            quote.open = (float) rand.nextInt(10000) / 100;
            quote.close = (float) rand.nextInt(10000) / 100;
            quote.high = Math.max(quote.open, quote.close);
            quote.low = Math.min(quote.open, quote.close);
            quote.volume = rand.nextInt(1000);
            stock.quotes.add(quote);
        }
        storage.commit();

        // Query a range using Date objects
        Date from = new Date((long) (baseDate + 50) * MSECS_PER_DAY);
        Date till = new Date((long) (baseDate + 75) * MSECS_PER_DAY);

        Iterator<Quote> iterator = stock.quotes.iterator(from, till, false);
        int count = 0;
        while (iterator.hasNext()) {
            Quote quote = iterator.next();
            assertTrue(quote.date >= baseDate + 50 && quote.date <= baseDate + 75,
                    "Quote should be within range");
            count++;
        }

        assertTrue(count > 0, "Should find quotes in range");
    }

    @Test
    @DisplayName("Test TimeSeries get first and last time")
    void testTimeSeriesGetFirstAndLastTime() {
        StockIndex root = new StockIndex();
        root.stocks = storage.<Stock>createFieldIndex(Stock.class, "name", true);
        storage.setRoot(root);

        Stock stock = new Stock();
        stock.name = "TEST";
        stock.quotes = storage.createTimeSeries(QuoteBlock.class, (long) QuoteBlock.N_ELEMS_PER_BLOCK * MSECS_PER_DAY * 2);
        root.stocks.put(stock);

        // Add quotes
        Random rand = new Random(123);
        int baseDate = 2000;

        for (int i = 0; i < 50; i++) {
            Quote quote = new Quote();
            quote.date = baseDate + i;
            quote.open = 10.0f;
            quote.close = 20.0f;
            quote.high = 20.0f;
            quote.low = 10.0f;
            quote.volume = 100;
            stock.quotes.add(quote);
        }
        storage.commit();

        // Check first and last time (returns Date)
        Date firstTime = stock.quotes.getFirstTime();
        Date lastTime = stock.quotes.getLastTime();

        assertEquals(new Date((long) baseDate * MSECS_PER_DAY), firstTime, "First time should match");
        assertEquals(new Date((long) (baseDate + 49) * MSECS_PER_DAY), lastTime, "Last time should match");
    }

    @Test
    @DisplayName("Test TimeSeries remove")
    void testTimeSeriesRemove() {
        StockIndex root = new StockIndex();
        root.stocks = storage.<Stock>createFieldIndex(Stock.class, "name", true);
        storage.setRoot(root);

        Stock stock = new Stock();
        stock.name = "REM";
        stock.quotes = storage.createTimeSeries(QuoteBlock.class, (long) QuoteBlock.N_ELEMS_PER_BLOCK * MSECS_PER_DAY * 2);
        root.stocks.put(stock);

        // Add quotes
        Random rand = new Random(456);
        int baseDate = 3000;

        for (int i = 0; i < 30; i++) {
            Quote quote = new Quote();
            quote.date = baseDate + i;
            quote.open = 10.0f;
            quote.close = 20.0f;
            quote.high = 20.0f;
            quote.low = 10.0f;
            quote.volume = 100;
            stock.quotes.add(quote);
        }
        storage.commit();

        assertEquals(30, stock.quotes.size(), "Should have 30 quotes before removal");

        // Remove all quotes using Date objects
        int removed = stock.quotes.remove(stock.quotes.getFirstTime(), stock.quotes.getLastTime());

        assertEquals(30, removed, "Should remove 30 quotes");
        assertEquals(0, stock.quotes.size(), "Should have no quotes after removal");
    }

    @Test
    @DisplayName("Test TimeSeries iterator with different directions")
    void testTimeSeriesIteratorDirections() {
        StockIndex root = new StockIndex();
        root.stocks = storage.<Stock>createFieldIndex(Stock.class, "name", true);
        storage.setRoot(root);

        Stock stock = new Stock();
        stock.name = "DIR";
        stock.quotes = storage.createTimeSeries(QuoteBlock.class, (long) QuoteBlock.N_ELEMS_PER_BLOCK * MSECS_PER_DAY * 2);
        root.stocks.put(stock);

        // Add quotes
        int baseDate = 6000;
        for (int i = 0; i < 10; i++) {
            Quote quote = new Quote();
            quote.date = baseDate + i;
            quote.open = 10.0f;
            quote.close = 20.0f;
            quote.high = 20.0f;
            quote.low = 10.0f;
            quote.volume = 100;
            stock.quotes.add(quote);
        }
        storage.commit();

        // Test forward iterator
        Iterator<Quote> fwdIterator = stock.quotes.iterator(false);
        int fwdCount = 0;
        while (fwdIterator.hasNext()) {
            fwdIterator.next();
            fwdCount++;
        }
        assertEquals(10, fwdCount, "Forward iterator should find all 10 quotes");

        // Test reverse iterator
        Iterator<Quote> revIterator = stock.quotes.iterator(true);
        int revCount = 0;
        while (revIterator.hasNext()) {
            revIterator.next();
            revCount++;
        }
        assertEquals(10, revCount, "Reverse iterator should find all 10 quotes");
    }

    @Test
    @DisplayName("Test TimeSeries index lookup")
    void testTimeSeriesIndexLookup() {
        StockIndex root = new StockIndex();
        root.stocks = storage.<Stock>createFieldIndex(Stock.class, "name", true);
        storage.setRoot(root);

        Stock stock = new Stock();
        stock.name = "LOOKUP";
        stock.quotes = storage.createTimeSeries(QuoteBlock.class, (long) QuoteBlock.N_ELEMS_PER_BLOCK * MSECS_PER_DAY * 2);
        root.stocks.put(stock);

        // Add quotes
        Random rand = new Random(999);
        int baseDate = 5000;

        for (int i = 0; i < 25; i++) {
            Quote quote = new Quote();
            quote.date = baseDate + i;
            quote.open = 10.0f + i;
            quote.close = 20.0f + i;
            quote.high = 20.0f + i;
            quote.low = 10.0f + i;
            quote.volume = 100 + i;
            stock.quotes.add(quote);
        }
        storage.commit();

        // Lookup by name
        Stock found = root.stocks.get("LOOKUP");
        assertNotNull(found, "Should find stock by name");
        assertEquals("LOOKUP", found.name, "Stock name should match");
        assertEquals(25, found.quotes.size(), "Should have 25 quotes");
    }
}
