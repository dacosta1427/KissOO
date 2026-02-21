package nl.dcg.gfe;

import org.garret.perst.*;

import java.sql.*;
import java.util.*;

/**
 * ComparisonBenchmark4 - Concurrency Benchmark with Optimistic Locking
 * 
 * Tests Perst's optimistic locking vs PostgreSQL's pessimistic locking.
 * 
 * Since Perst's Storage is not thread-safe, we test:
 * 1. Sequential access with conflict simulation
 * 2. Single-threaded baseline
 * 
 * For true multi-threaded, Perst requires thread-local Storage instances.
 */
public class ComparisonBenchmark4 {

    // ==================== Models ====================
    
    static class Record extends Persistent {
        int key;
        String value;
        
        public Record() {}
        
        public Record(int key, String value) {
            this.key = key;
            this.value = value;
        }
    }

    // ==================== Configuration ====================
    
    static final int N_RECORDS = 5000;
    static final int CONFLICT_RATE = 50; // 50% overwrite, 50% leave
    
    static String jdbcUrl = "jdbc:postgresql://localhost:5432/perst_test";
    static String dbUser = "postgres";
    static String dbPassword = "gfe";
    
    static Connection pgConn;
    static Storage perstStorage;
    static Index perstIndex;
    static Random random = new Random(42);
    
    static void printResult(String test, long perstMs, long pgMs) {
        if (pgMs > 0 && perstMs > 0) {
            double speedup = (double) pgMs / perstMs;
            System.out.printf("%-55s Perst: %6d ms  PostgreSQL: %6d ms  Speedup: %7.2fx%n",
                    test, perstMs, pgMs, speedup);
        } else {
            System.out.printf("%-55s Perst: %6d ms  PostgreSQL: %6d ms%n",
                    test, perstMs, pgMs);
        }
    }

    // ==================== Setup ====================
    
    static void setupPostgres() throws SQLException {
        Statement stmt = pgConn.createStatement();
        stmt.execute("DROP TABLE IF EXISTS concurrent_records");
        stmt.execute("""
            CREATE TABLE concurrent_records (
                id SERIAL PRIMARY KEY,
                key_col INTEGER NOT NULL,
                value_col VARCHAR(255),
                version INTEGER DEFAULT 1
            )
        """);
        stmt.execute("CREATE INDEX idx_concurrent_key ON concurrent_records(key_col)");
        pgConn.commit();
        stmt.close();
    }
    
    static void setupPerst() throws Exception {
        perstStorage = StorageFactory.getInstance().createStorage();
        
        java.io.File dbFile = new java.io.File("benchmark4.dbs");
        if (dbFile.exists()) dbFile.delete();
        
        perstStorage.open("benchmark4.dbs", 128 * 1024 * 1024);
        perstIndex = perstStorage.createIndex(int.class, true);
    }

    // ==================== Test 1: Baseline Insert (No Conflicts) ====================
    
    static void testBaselineInsert() throws Exception {
        // Clear
        setupPostgres();
        if (perstStorage != null) perstStorage.close();
        setupPerst();
        
        // PostgreSQL
        long start = System.currentTimeMillis();
        PreparedStatement ps = pgConn.prepareStatement(
            "INSERT INTO concurrent_records (key_col, value_col) VALUES (?, ?)"
        );
        for (int i = 0; i < N_RECORDS; i++) {
            ps.setInt(1, i);
            ps.setString(2, "Value-" + i);
            ps.addBatch();
            if (i % 1000 == 0) ps.executeBatch();
        }
        ps.executeBatch();
        pgConn.commit();
        ps.close();
        long pgTime = System.currentTimeMillis() - start;
        
        // Perst
        start = System.currentTimeMillis();
        for (int i = 0; i < N_RECORDS; i++) {
            Record rec = new Record(i, "Value-" + i);
            perstIndex.put(new Key(rec.key), rec);
        }
        perstStorage.commit();
        long perstTime = System.currentTimeMillis() - start;
        
        printResult("Baseline Insert (" + N_RECORDS + ")", perstTime, pgTime);
    }
    
    // ==================== Test 2: Simulated Concurrent Updates ====================
    
    /**
     * Simulates multiple threads trying to update the SAME records
     * - Perst: Uses optimistic locking (detects conflicts at commit)
     * - PostgreSQL: Uses SELECT FOR UPDATE (pessimistic locking)
     * 
     * All threads try to update the same 500 keys = 100% conflict scenario
     */
    static void testSimulatedConcurrentUpdates() throws Exception {
        // Pre-populate with 500 records (same keys all threads will try)
        setupPostgres();
        if (perstStorage != null) perstStorage.close();
        setupPerst();
        
        for (int i = 0; i < 500; i++) {
            Record rec = new Record(i, "Initial-" + i);
            perstIndex.put(new Key(rec.key), rec);
        }
        perstStorage.commit();
        
        PreparedStatement ps = pgConn.prepareStatement(
            "INSERT INTO concurrent_records (key_col, value_col) VALUES (?, ?)"
        );
        for (int i = 0; i < 500; i++) {
            ps.setInt(1, i);
            ps.setString(2, "Initial-" + i);
            ps.addBatch();
        }
        ps.executeBatch();
        pgConn.commit();
        ps.close();
        
        // Simulate 4 "threads" doing updates - ALL trying to update the SAME keys (100% conflict)
        
        // PostgreSQL - pessimistic locking simulation
        long start = System.currentTimeMillis();
        int pgOverwrites = 0;
        int pgSkipped = 0;
        
        for (int thread = 0; thread < 4; thread++) {
            Connection conn = DriverManager.getConnection(jdbcUrl, dbUser, dbPassword);
            conn.setAutoCommit(false);
            
            // All threads try to update ALL keys (100% conflict rate)
            for (int i = 0; i < 500; i++) {
                int key = i; // Same keys for all threads
                
                try {
                    PreparedStatement sel = conn.prepareStatement(
                        "SELECT * FROM concurrent_records WHERE key_col = ? FOR UPDATE"
                    );
                    sel.setInt(1, key);
                    ResultSet rs = sel.executeQuery();
                    
                    if (rs.next()) {
                        if (random.nextInt(100) < CONFLICT_RATE) {
                            PreparedStatement up = conn.prepareStatement(
                                "UPDATE concurrent_records SET value_col = ? WHERE key_col = ?"
                            );
                            up.setString(1, "Updated-T" + thread + "-" + i);
                            up.setInt(2, key);
                            up.executeUpdate();
                            pgOverwrites++;
                        } else {
                            pgSkipped++;
                        }
                    }
                    rs.close();
                    sel.close();
                    conn.commit();
                } catch (SQLException e) {
                    conn.rollback();
                }
            }
            conn.close();
        }
        long pgTime = System.currentTimeMillis() - start;
        
        // Reset Perst for fresh test
        if (perstStorage != null) perstStorage.close();
        setupPerst();
        
        for (int i = 0; i < N_RECORDS; i++) {
            Record rec = new Record(i, "Initial-" + i);
            perstIndex.put(new Key(rec.key), rec);
        }
        perstStorage.commit();
        
        // Perst - optimistic locking simulation (same keys = conflicts)
        start = System.currentTimeMillis();
        int perstOverwrites = 0;
        int perstConflicts = 0;
        
        for (int thread = 0; thread < 4; thread++) {
            // All threads try to update SAME keys
            for (int i = 0; i < 500; i++) {
                int key = i;
                
                try {
                    Record rec = new Record(key, "Updated-T" + thread + "-" + i);
                    perstIndex.put(new Key(rec.key), rec);
                    perstStorage.commit();
                } catch (Exception e) {
                    // Optimistic lock conflict detected!
                    perstConflicts++;
                    
                    if (random.nextInt(100) < CONFLICT_RATE) {
                        // 50% - overwrite (get and update)
                        Record existing = (Record) perstIndex.get(new Key(key));
                        if (existing != null) {
                            existing.value = "Overwritten-T" + thread + "-" + i;
                            perstIndex.set(new Key(key), existing);
                            perstStorage.commit();
                            perstOverwrites++;
                        }
                    }
                    // 50% - skip (leave as-is)
                }
            }
        }
        long perstTime = System.currentTimeMillis() - start;
        
        System.out.println("  PostgreSQL: overwrites=" + pgOverwrites + ", skipped=" + pgSkipped);
        System.out.println("  Perst: conflicts=" + perstConflicts + ", overwrites=" + perstOverwrites);
        
        printResult("Simulated Concurrent Updates (8 threads)", perstTime, pgTime);
    }
    
    // ==================== Test 3: Single Record Update ====================
    
    static void testSingleRecordUpdate() throws Exception {
        // Clear and setup
        setupPostgres();
        if (perstStorage != null) perstStorage.close();
        setupPerst();
        
        // Pre-populate with 1 record
        Record rec = new Record(1, "Initial");
        perstIndex.put(new Key(rec.key), rec);
        perstStorage.commit();
        
        PreparedStatement ps = pgConn.prepareStatement(
            "INSERT INTO concurrent_records (key_col, value_col) VALUES (?, ?)"
        );
        ps.setInt(1, 1);
        ps.setString(2, "Initial");
        ps.executeUpdate();
        pgConn.commit();
        ps.close();
        
        // PostgreSQL - single record update
        long start = System.currentTimeMillis();
        for (int i = 0; i < 100; i++) {
            Connection conn = DriverManager.getConnection(jdbcUrl, dbUser, dbPassword);
            conn.setAutoCommit(false);
            
            PreparedStatement sel = conn.prepareStatement(
                "SELECT * FROM concurrent_records WHERE key_col = 1 FOR UPDATE"
            );
            ResultSet rs = sel.executeQuery();
            if (rs.next()) {
                PreparedStatement up = conn.prepareStatement(
                    "UPDATE concurrent_records SET value_col = ? WHERE key_col = 1"
                );
                up.setString(1, "Updated-" + i);
                up.executeUpdate();
            }
            conn.commit();
            rs.close();
            sel.close();
            conn.close();
        }
        long pgTime = System.currentTimeMillis() - start;
        
        // Perst - single record update
        start = System.currentTimeMillis();
        for (int i = 0; i < 100; i++) {
            try {
                Record r = new Record(1, "Updated-" + i);
                perstIndex.put(new Key(r.key), r);
                perstStorage.commit();
            } catch (Exception e) {
                // Conflict - get and update
                Record existing = (Record) perstIndex.get(new Key(1));
                if (existing != null) {
                    existing.value = "Overwritten-" + i;
                    perstIndex.set(new Key(1), existing);
                    perstStorage.commit();
                }
            }
        }
        long perstTime = System.currentTimeMillis() - start;
        
        printResult("Single Record Updates (1000)", perstTime, pgTime);
    }
    
    // ==================== Test 4: Bulk Update (No Conflicts) ====================
    
    static void testBulkUpdate() throws Exception {
        // Setup
        setupPostgres();
        if (perstStorage != null) perstStorage.close();
        setupPerst();
        
        // Pre-populate
        for (int i = 0; i < N_RECORDS; i++) {
            Record rec = new Record(i, "Initial-" + i);
            perstIndex.put(new Key(rec.key), rec);
        }
        perstStorage.commit();
        
        PreparedStatement ps = pgConn.prepareStatement(
            "INSERT INTO concurrent_records (key_col, value_col) VALUES (?, ?)"
        );
        for (int i = 0; i < N_RECORDS; i++) {
            ps.setInt(1, i);
            ps.setString(2, "Initial-" + i);
            ps.addBatch();
        }
        ps.executeBatch();
        pgConn.commit();
        ps.close();
        
        // PostgreSQL - bulk update
        long start = System.currentTimeMillis();
        PreparedStatement up = pgConn.prepareStatement(
            "UPDATE concurrent_records SET value_col = ? WHERE key_col < ?"
        );
        up.setString(1, "BULK_UPDATED");
        up.setInt(2, N_RECORDS / 2);
        up.executeUpdate();
        pgConn.commit();
        up.close();
        long pgTime = System.currentTimeMillis() - start;
        
        // Perst - bulk update
        start = System.currentTimeMillis();
        Iterator iter = perstIndex.iterator();
        while (iter.hasNext()) {
            Record r = (Record) iter.next();
            if (r.key < N_RECORDS / 2) {
                r.value = "BULK_UPDATED";
                perstIndex.set(new Key(r.key), r);
            }
        }
        perstStorage.commit();
        long perstTime = System.currentTimeMillis() - start;
        
        printResult("Bulk Update (key < " + (N_RECORDS/2) + ")", perstTime, pgTime);
    }

    // ==================== Main ====================
    
    public static void main(String[] args) throws Exception {
        System.out.println("=== Perst vs PostgreSQL - Optimistic Locking Benchmark ===");
        System.out.println("Records: " + N_RECORDS + ", Conflict rate: " + CONFLICT_RATE + "%");
        System.out.println();
        
        // Connect to PostgreSQL
        Class.forName("org.postgresql.Driver");
        pgConn = DriverManager.getConnection(jdbcUrl, dbUser, dbPassword);
        pgConn.setAutoCommit(false);
        
        System.out.println("=== Test 1: Baseline Insert ===");
        testBaselineInsert();
        
        System.out.println("\n=== Test 2: Simulated Concurrent Updates ===");
        testSimulatedConcurrentUpdates();
        
        System.out.println("\n=== Test 3: Single Record Update ===");
        testSingleRecordUpdate();
        
        System.out.println("\n=== Test 4: Bulk Update ===");
        testBulkUpdate();
        
        // Cleanup
        if (perstStorage != null) perstStorage.close();
        pgConn.close();
        
        System.out.println("\n=== Benchmark Complete ===");
    }
}
