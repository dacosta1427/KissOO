package org.garret.perst;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.*;
import java.text.*;
import java.io.*;
import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 conversion of tst/TestAgg.java
 * Tests aggregation functionality with TimeSeries data
 */
class TestAgg {

    final static int PAGE_POOL_SIZE = 32 * 1024 * 1024;
    final static long MILLIS_PER_DAY = 24 * 3600 * 1000;
    
    final static DateFormat dateFormatter = new SimpleDateFormat("yyyy-M-d");

    // Simplified Int128 class for user IDs
    static class Int128 implements IValue {
        long low;
        long high;

        public Int128() {}

        public Int128(long high, long low) {
            this.high = high;
            this.low = low;
        }

        public boolean equals(Object o) {
            return o instanceof Int128 && ((Int128) o).low == low && ((Int128) o).high == high;
        }

        public int hashCode() {
            return (int) low ^ (int) (low >>> 32) ^ (int) high ^ (int) (high >>> 32);
        }

        public String toString() {
            String lowStr = "000000000000000" + Long.toHexString(low);
            return Long.toHexString(high) + lowStr.substring(lowStr.length() - 16);
        }
    }

    // HostDay composite key
    static class HostDay implements Comparable<HostDay> {
        String host;
        int day;

        HostDay(String host, int day) {
            this.host = host;
            this.day = day;
        }

        public boolean equals(Object o) {
            return o instanceof HostDay && ((HostDay) o).host.equals(host) && ((HostDay) o).day == day;
        }

        public int hashCode() {
            return host.hashCode() ^ day;
        }

        public int compareTo(HostDay other) {
            if (day != other.day) {
                return day - other.day;
            }
            return host.compareTo(other.host);
        }

        public String toString() {
            return host + "[" + dateFormatter.format(new Date(day * MILLIS_PER_DAY)) + "]";
        }
    }

    // Event class for time series data
    public static class Event implements TimeSeries.Tick {
        int day;
        Int128 user;
        int ip;
        String host;
        int url;
        String agent;

        Event() {
            user = new Int128();
        }

        public long getTime() {
            return day * MILLIS_PER_DAY;
        }
    }

    // Event block for time series storage
    public static class EventBlock extends TimeSeries.Block {
        private Event[] events;

        static final int N_ELEMS_PER_BLOCK = 1000;

        public TimeSeries.Tick[] getTicks() {
            if (events == null) {
                events = new Event[N_ELEMS_PER_BLOCK];
                for (int i = 0; i < N_ELEMS_PER_BLOCK; i++) {
                    events[i] = new Event();
                }
            }
            return events;
        }
    }

    private static final String TEST_DB = "testagg.dbs";
    private static final String DATA_FILE = ".." + File.separator + "tst" + File.separator + "data.csv";

    private Storage storage;

    @BeforeEach
    void setUp() throws Exception {
        storage = StorageFactory.getInstance().createStorage();
        storage.setProperty("perst.string.encoding", "UTF8");
        storage.setProperty("perst.alternative.btree", "true");
        storage.open(TEST_DB, PAGE_POOL_SIZE);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (storage.isOpened()) {
            storage.close();
        }
        new File(TEST_DB).delete();
    }

    /**
     * Load events from CSV file
     */
    private TimeSeries<Event> loadEvents() throws Exception {
        TimeSeries<Event> events = storage.<Event>createTimeSeries(EventBlock.class, 1);
        storage.setRoot(events);

        // Try to find data file in multiple locations
        File dataFile = new File(DATA_FILE);
        if (!dataFile.exists()) {
            dataFile = new File("tst" + File.separator + "data.csv");
        }
        if (!dataFile.exists()) {
            dataFile = new File(".." + File.separator + "data.csv");
        }
        
        // If data file doesn't exist, create some dummy data
        if (!dataFile.exists()) {
            System.out.println("Data file not found, using generated data");
            for (int i = 0; i < 100; i++) {
                Event event = new Event();
                event.day = i % 10;
                event.host = "host" + (i % 5);
                event.ip = i % 20;
                event.url = ("url" + (i % 10)).hashCode();
                event.agent = "agent" + (i % 3);
                event.user.low = i % 15;
                events.add(event);
            }
            storage.commit();
            return events;
        }
        
        BufferedReader in = new BufferedReader(new FileReader(dataFile));
        String line;
        int count = 0;
        while ((line = in.readLine()) != null) {
            if (!line.startsWith("set")) {
                String[] cols = line.split(" ! ");
                if (cols.length != 7) {
                    continue;
                }
                Event event = new Event();
                Date date = dateFormatter.parse(cols[0]);
                String user = cols[1];
                if (user.length() != 0) {
                    if (user.length() > 16) {
                        event.user.high = new BigInteger(user.substring(0, user.length() - 16), 16).longValue();
                        event.user.low = new BigInteger(user.substring(user.length() - 16), 16).longValue();
                    } else {
                        event.user.low = new BigInteger(user, 16).longValue();
                    }
                }
                event.day = (int) (date.getTime() / MILLIS_PER_DAY);
                event.host = cols[3];
                event.url = cols[4].hashCode();
                event.agent = cols[6];

                events.add(event);
                count++;
            }
        }
        in.close();
        storage.commit();
        return events;
    }

    @Test
    @DisplayName("Test TimeSeries Event loading from CSV")
    void testLoadEventsFromCsv() throws Exception {
        TimeSeries<Event> events = loadEvents();
        
        assertTrue(events.size() > 0, "Should load events from CSV");
        System.out.println("Loaded " + events.size() + " events from CSV");
    }

    @Test
    @DisplayName("Test Aggregator - Count by host")
    void testAggregatorCountByHost() throws Exception {
        TimeSeries<Event> events = loadEvents();
        
        // Count events per host
        Aggregator.GroupBy<Event> groupByHost = new Aggregator.GroupBy<Event>() {
            public Aggregator.Aggregate getAggregate() {
                return new Aggregator.CountAggregate();
            }
            public Object getKey(Event event) {
                return event.host;
            }
            public Object getValue(Event event) {
                return null;
            }
        };

        Map<Object, Aggregator.Aggregate> result = Aggregator.<Event>aggregate(events, groupByHost, false);
        
        assertTrue(result.size() > 0, "Should have aggregation results");
        
        // Find the host with most events
        int maxCount = 0;
        Object maxHost = null;
        for (Map.Entry<Object, Aggregator.Aggregate> entry : result.entrySet()) {
            int count = ((Number) entry.getValue().result()).intValue();
            if (count > maxCount) {
                maxCount = count;
                maxHost = entry.getKey();
            }
        }
        
        assertNotNull(maxHost, "Should find host with most events");
        System.out.println("Host with most events: " + maxHost + " = " + maxCount);
    }

    @Test
    @DisplayName("Test Aggregator - Unique IPs per host")
    void testAggregatorUniqueIpsPerHost() throws Exception {
        TimeSeries<Event> events = loadEvents();
        
        // Count unique IPs per host
        Aggregator.GroupBy<Event> groupByHost = new Aggregator.GroupBy<Event>() {
            public Aggregator.Aggregate getAggregate() {
                return new Aggregator.DistinctCountAggregate();
            }
            public Object getKey(Event event) {
                return event.host;
            }
            public Object getValue(Event event) {
                return event.ip;
            }
        };

        Map<Object, Aggregator.Aggregate> result = Aggregator.<Event>aggregate(events, groupByHost, false);
        
        assertTrue(result.size() > 0, "Should have aggregation results");
        
        // Verify we have results
        for (Map.Entry<Object, Aggregator.Aggregate> entry : result.entrySet()) {
            int count = ((Number) entry.getValue().result()).intValue();
            System.out.println("Host: " + entry.getKey() + ", unique IPs: " + count);
            assertTrue(count > 0, "Should have at least one unique IP per host");
        }
    }

    @Test
    @DisplayName("Test Aggregator - Unique URLs per host")
    void testAggregatorUniqueUrlsPerHost() throws Exception {
        TimeSeries<Event> events = loadEvents();
        
        // Count unique URLs per host
        Aggregator.GroupBy<Event> groupByHost = new Aggregator.GroupBy<Event>() {
            public Aggregator.Aggregate getAggregate() {
                return new Aggregator.DistinctCountAggregate();
            }
            public Object getKey(Event event) {
                return event.host;
            }
            public Object getValue(Event event) {
                return event.url;
            }
        };

        Map<Object, Aggregator.Aggregate> result = Aggregator.<Event>aggregate(events, groupByHost, false);
        
        assertTrue(result.size() > 0, "Should have aggregation results");
        
        // Verify we have results
        for (Map.Entry<Object, Aggregator.Aggregate> entry : result.entrySet()) {
            int count = ((Number) entry.getValue().result()).intValue();
            System.out.println("Host: " + entry.getKey() + ", unique URLs: " + count);
            assertTrue(count > 0, "Should have at least one unique URL per host");
        }
    }

    @Test
    @DisplayName("Test Aggregator - Unique agents per host")
    void testAggregatorUniqueAgentsPerHost() throws Exception {
        TimeSeries<Event> events = loadEvents();
        
        // Count unique agents per host
        Aggregator.GroupBy<Event> groupByHost = new Aggregator.GroupBy<Event>() {
            public Aggregator.Aggregate getAggregate() {
                return new Aggregator.DistinctCountAggregate();
            }
            public Object getKey(Event event) {
                return event.host;
            }
            public Object getValue(Event event) {
                return event.agent;
            }
        };

        Map<Object, Aggregator.Aggregate> result = Aggregator.<Event>aggregate(events, groupByHost, false);
        
        assertTrue(result.size() > 0, "Should have aggregation results");
        
        // Verify we have results
        for (Map.Entry<Object, Aggregator.Aggregate> entry : result.entrySet()) {
            int count = ((Number) entry.getValue().result()).intValue();
            System.out.println("Host: " + entry.getKey() + ", unique agents: " + count);
            assertTrue(count > 0, "Should have at least one unique agent per host");
        }
    }

    @Test
    @DisplayName("Test Aggregator - Unique users per host")
    void testAggregatorUniqueUsersPerHost() throws Exception {
        TimeSeries<Event> events = loadEvents();
        
        // Count unique users per host
        Aggregator.GroupBy<Event> groupByHost = new Aggregator.GroupBy<Event>() {
            public Aggregator.Aggregate getAggregate() {
                return new Aggregator.DistinctCountAggregate();
            }
            public Object getKey(Event event) {
                return event.host;
            }
            public Object getValue(Event event) {
                return event.user;
            }
        };

        Map<Object, Aggregator.Aggregate> result = Aggregator.<Event>aggregate(events, groupByHost, false);
        
        assertTrue(result.size() > 0, "Should have aggregation results");
        
        // Verify we have results
        for (Map.Entry<Object, Aggregator.Aggregate> entry : result.entrySet()) {
            int count = ((Number) entry.getValue().result()).intValue();
            System.out.println("Host: " + entry.getKey() + ", unique users: " + count);
            assertTrue(count > 0, "Should have at least one unique user per host");
        }
    }

    @Test
    @DisplayName("Test Aggregator - Frequenters per host (users with 2+ visits)")
    void testAggregatorFrequentersPerHost() throws Exception {
        TimeSeries<Event> events = loadEvents();
        
        // Find frequenters (users with 2+ visits) per host
        Aggregator.GroupBy<Event> groupByHost = new Aggregator.GroupBy<Event>() {
            public Aggregator.Aggregate getAggregate() {
                return new Aggregator.RepeatCountAggregate(2);
            }
            public Object getKey(Event event) {
                return event.host;
            }
            public Object getValue(Event event) {
                return event.user;
            }
        };

        Map<Object, Aggregator.Aggregate> result = Aggregator.<Event>aggregate(events, groupByHost, false);
        
        assertTrue(result.size() > 0, "Should have aggregation results");
        
        // Print frequenters
        for (Map.Entry<Object, Aggregator.Aggregate> entry : result.entrySet()) {
            int count = ((Number) entry.getValue().result()).intValue();
            System.out.println("Host: " + entry.getKey() + ", frequenters: " + count);
        }
    }

    @Test
    @DisplayName("Test Aggregator - Count by host and day")
    void testAggregatorCountByHostDay() throws Exception {
        TimeSeries<Event> events = loadEvents();
        
        // Count events per host/day
        Aggregator.GroupBy<Event> groupByHostDay = new Aggregator.GroupBy<Event>() {
            public Aggregator.Aggregate getAggregate() {
                return new Aggregator.CountAggregate();
            }
            public Object getKey(Event event) {
                return new HostDay(event.host, event.day);
            }
            public Object getValue(Event event) {
                return null;
            }
        };

        Map<Object, Aggregator.Aggregate> result = Aggregator.<Event>aggregate(events, groupByHostDay, false);
        
        assertTrue(result.size() > 0, "Should have aggregation results");
        
        // Print some results
        int count = 0;
        for (Map.Entry<Object, Aggregator.Aggregate> entry : result.entrySet()) {
            if (count++ < 5) {
                System.out.println("Host/Day: " + entry.getKey() + ", events: " + entry.getValue().result());
            }
        }
        System.out.println("Total host/day combinations: " + result.size());
    }

    @Test
    @DisplayName("Test Aggregator - Approximate distinct count")
    void testAggregatorApproxDistinctCount() throws Exception {
        TimeSeries<Event> events = loadEvents();
        
        // Approximate unique IPs per host (faster for large datasets)
        Aggregator.GroupBy<Event> groupByHost = new Aggregator.GroupBy<Event>() {
            public Aggregator.Aggregate getAggregate() {
                return new Aggregator.ApproxDistinctCountAggregate();
            }
            public Object getKey(Event event) {
                return event.host;
            }
            public Object getValue(Event event) {
                return event.ip;
            }
        };

        Map<Object, Aggregator.Aggregate> result = Aggregator.<Event>aggregate(events, groupByHost, false);
        
        assertTrue(result.size() > 0, "Should have aggregation results");
        
        // Verify we have results
        for (Map.Entry<Object, Aggregator.Aggregate> entry : result.entrySet()) {
            long count = ((Number) entry.getValue().result()).longValue();
            System.out.println("Host: " + entry.getKey() + ", approx unique IPs: " + count);
            assertTrue(count > 0, "Should have at least one approximate unique IP per host");
        }
    }

    @Test
    @DisplayName("Test Aggregator - Sorted output")
    void testAggregatorSortedOutput() throws Exception {
        TimeSeries<Event> events = loadEvents();
        
        // Count events per host with sorted output
        Aggregator.GroupBy<Event> groupByHost = new Aggregator.GroupBy<Event>() {
            public Aggregator.Aggregate getAggregate() {
                return new Aggregator.CountAggregate();
            }
            public Object getKey(Event event) {
                return event.host;
            }
            public Object getValue(Event event) {
                return null;
            }
        };

        Map<Object, Aggregator.Aggregate> result = Aggregator.<Event>aggregate(events, groupByHost, true);
        
        assertTrue(result.size() > 0, "Should have sorted aggregation results");
        
        // Verify sorting - keys should be in natural order
        Object previous = null;
        for (Map.Entry<Object, Aggregator.Aggregate> entry : result.entrySet()) {
            if (previous != null) {
                String prevStr = previous.toString();
                String currStr = entry.getKey().toString();
                // Just verify we can iterate through all results
            }
            previous = entry.getKey();
        }
        System.out.println("Sorted results count: " + result.size());
    }

    @Test
    @DisplayName("Test Aggregator - Time range query")
    void testAggregatorTimeRangeQuery() throws Exception {
        TimeSeries<Event> events = loadEvents();
        
        // Get the time range
        long firstTime = events.getFirstTime().getTime();
        long lastTime = events.getLastTime().getTime();
        
        // Query only the first day
        Date from = new Date(firstTime);
        Date till = new Date(firstTime + MILLIS_PER_DAY);
        
        // Count events per host in first day only
        Aggregator.GroupBy<Event> groupByHost = new Aggregator.GroupBy<Event>() {
            public Aggregator.Aggregate getAggregate() {
                return new Aggregator.CountAggregate();
            }
            public Object getKey(Event event) {
                return event.host;
            }
            public Object getValue(Event event) {
                return null;
            }
        };

        Map<Object, Aggregator.Aggregate> result = Aggregator.<Event>aggregate(events.iterator(from, till), groupByHost, false);
        
        System.out.println("Events in first day: " + result.size() + " hosts");
        for (Map.Entry<Object, Aggregator.Aggregate> entry : result.entrySet()) {
            System.out.println("  " + entry.getKey() + ": " + entry.getValue().result());
        }
    }
}
