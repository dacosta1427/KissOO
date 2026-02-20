package org.garret.perst;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;
import java.io.PrintWriter;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Phase 2C: Tests for QueryProfiler and QueryProfiler.QueryInfo.
 *
 * QueryProfiler is a StorageListener that records timing info for each executed
 * JSQL query. It must be registered on the storage before queries are run.
 */
class QueryProfilerTest {

    static class Article extends Persistent {
        String title;
        int    year;

        Article() {}

        Article(Storage s, String title, int year) {
            super(s);
            this.title = title;
            this.year  = year;
        }
    }

    static class Root extends Persistent {
        FieldIndex<Article> byTitle;

        Root() {}

        Root(Storage s) {
            super(s);
            byTitle = s.createFieldIndex(Article.class, "title", true);
        }
    }

    private static final String TEST_DB = "testqueryprofiler.dbs";
    private Storage         storage;
    private QueryProfiler   profiler;
    private Root            root;

    @BeforeEach
    void setUp() {
        storage = StorageFactory.getInstance().createStorage();
        storage.open(TEST_DB, 4 * 1024 * 1024);

        profiler = new QueryProfiler();
        storage.setListener(profiler);

        root = new Root(storage);
        storage.setRoot(root);

        // Populate a few articles
        root.byTitle.put(new Article(storage, "Alpha", 2020));
        root.byTitle.put(new Article(storage, "Beta",  2021));
        root.byTitle.put(new Article(storage, "Gamma", 2022));
        root.byTitle.put(new Article(storage, "Delta", 2023));
        storage.commit();
    }

    @AfterEach
    void tearDown() {
        if (storage.isOpened()) {
            storage.close();
        }
        new java.io.File(TEST_DB).delete();
    }

    // ===== Basic registration and query capture =====

    @Test
    @DisplayName("queryExecution() accumulates stats per query string")
    void testQueryExecutionAccumulatesStats() {
        // Call queryExecution directly (QueryProfiler.queryExecution has 3-param signature
        // which differs from the 4-param StorageListener base; JSQL won't auto-trigger it)
        profiler.queryExecution("year > 2020", 15L, false);
        profiler.queryExecution("year > 2020", 25L, false);

        QueryProfiler.QueryInfo[] profile = profiler.getProfile();
        assertTrue(profile.length > 0, "Profile should record at least one query");

        QueryProfiler.QueryInfo info = Arrays.stream(profile)
            .filter(qi -> qi.query.contains("2020"))
            .findFirst().orElse(null);
        assertNotNull(info, "Should find 'year > 2020' in profile");
        assertEquals(2L, info.count, "Query should have been counted twice");
        assertEquals(40L, info.totalTime, "Total time should be 40");
        assertTrue(info.maxTime >= 0, "Max time should be non-negative");
    }

    @Test
    @DisplayName("getProfile() returns queries sorted by totalTime descending")
    void testGetProfileSortedByTotalTime() {
        // Manually record queries with distinct timings
        profiler.queryExecution("fast query",  10L, false);
        profiler.queryExecution("slow query",  500L, false);
        profiler.queryExecution("medium query", 100L, false);
        profiler.queryExecution("slow query",  300L, false); // totalTime=800

        QueryProfiler.QueryInfo[] profile = profiler.getProfile();
        assertTrue(profile.length >= 3);
        // Sorted descending by totalTime
        for (int i = 1; i < profile.length; i++) {
            assertTrue(profile[i-1].totalTime >= profile[i].totalTime,
                "Profile should be sorted descending by totalTime");
        }
        // Verify slow query is first
        assertEquals("slow query", profile[0].query);
    }

    // ===== QueryInfo.compareTo =====

    @Test
    @DisplayName("QueryInfo.compareTo() orders by totalTime then count (descending)")
    void testQueryInfoCompareTo() {
        QueryProfiler.QueryInfo high = new QueryProfiler.QueryInfo();
        high.query     = "high";
        high.totalTime = 1000L;
        high.count     = 5L;

        QueryProfiler.QueryInfo low = new QueryProfiler.QueryInfo();
        low.query     = "low";
        low.totalTime = 100L;
        low.count     = 1L;

        QueryProfiler.QueryInfo same = new QueryProfiler.QueryInfo();
        same.query     = "same-time";
        same.totalTime = 1000L;
        same.count     = 10L; // higher count → comes first

        // high total time should sort before low
        assertTrue(high.compareTo(low) < 0, "Higher totalTime should be less (sorts first)");
        assertTrue(low.compareTo(high) > 0, "Lower totalTime should be greater (sorts last)");

        // Same totalTime but different count
        assertTrue(same.compareTo(high) < 0, "Higher count with same time should sort first");
        assertTrue(high.compareTo(same) > 0);

        // Equal everything
        QueryProfiler.QueryInfo dup = new QueryProfiler.QueryInfo();
        dup.totalTime = 1000L;
        dup.count     = 5L;
        assertEquals(0, high.compareTo(dup), "Identical totalTime/count should compare to 0");
    }

    // ===== dump() =====

    @Test
    @DisplayName("dump() produces formatted output to Appendable")
    void testDumpToAppendable() {
        // Record a query so there is data to dump
        profiler.queryExecution("year >= 2020", 42L, false);

        StringBuilder sb = new StringBuilder();
        profiler.dump(sb);

        String output = sb.toString();
        assertFalse(output.isEmpty(), "dump() should produce output");
        // Header line
        assertTrue(output.contains("Total"), "Output should contain header 'Total'");
        assertTrue(output.contains("Count"), "Output should contain header 'Count'");
    }

    @Test
    @DisplayName("dump() to System.out does not throw")
    void testDumpToSystemOut() {
        // No queries yet – dump should still work (empty profile)
        assertDoesNotThrow(() -> profiler.dump());
    }

    // ===== Multiple queries, different strings =====

    @Test
    @DisplayName("Multiple different queries each get their own QueryInfo entry")
    void testMultipleQueriesDistinct() {
        profiler.queryExecution("year = 2020", 10L, false);
        profiler.queryExecution("year = 2021", 20L, false);
        profiler.queryExecution("year = 2022", 30L, false);

        QueryProfiler.QueryInfo[] profile = profiler.getProfile();
        Set<String> queries = new HashSet<>();
        for (QueryProfiler.QueryInfo qi : profile) {
            queries.add(qi.query);
        }
        assertEquals(3, queries.size(), "Should have exactly 3 distinct query entries");
        assertTrue(queries.contains("year = 2020"));
        assertTrue(queries.contains("year = 2021"));
        assertTrue(queries.contains("year = 2022"));
    }

    // ===== maxTime tracking =====

    @Test
    @DisplayName("maxTime reflects the maximum of all executions for the same query")
    void testMaxTimeTracking() {
        // Manually invoke queryExecution to control timings
        profiler.queryExecution("test query", 50L, false);
        profiler.queryExecution("test query", 200L, false);
        profiler.queryExecution("test query", 80L, false);

        QueryProfiler.QueryInfo[] profile = profiler.getProfile();
        QueryProfiler.QueryInfo info = Arrays.stream(profile)
            .filter(qi -> qi.query.equals("test query"))
            .findFirst().orElse(null);

        assertNotNull(info);
        assertEquals(200L, info.maxTime, "maxTime should be the maximum elapsed time");
        assertEquals(330L, info.totalTime, "totalTime should be sum of all elapsed times");
        assertEquals(3L,   info.count,     "count should be 3");
    }

    // ===== sequentialSearch flag =====

    @Test
    @DisplayName("sequentialSearch flag is OR'd across executions")
    void testSequentialSearchFlag() {
        profiler.queryExecution("q1", 10L, false);
        profiler.queryExecution("q1", 10L, true);  // second run triggers sequential search

        QueryProfiler.QueryInfo[] profile = profiler.getProfile();
        QueryProfiler.QueryInfo info = Arrays.stream(profile)
            .filter(qi -> qi.query.equals("q1"))
            .findFirst().orElse(null);
        assertNotNull(info);
        assertTrue(info.sequentialSearch, "sequentialSearch should be true if any run was sequential");
    }
}
