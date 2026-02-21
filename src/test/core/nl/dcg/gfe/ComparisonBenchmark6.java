package nl.dcg.gfe;

import org.garret.perst.*;
import java.sql.*;
import java.util.*;

public class ComparisonBenchmark6 {

    static class Record extends Persistent {
        int key;
        String value;
        
        public Record() {}
        
        public Record(int key, String value) {
            this.key = key;
            this.value = value;
        }
    }

    static final int[] RECORD_COUNTS = {100000, 500000, 1000000};
    
    static String jdbcUrl = "jdbc:postgresql://localhost:5432/perst_test";
    static String dbUser = "postgres";
    static String dbPassword = "gfe";
    
    static Connection pgConn;
    static Storage perstStorage;
    static Index recordIndex;
    
    static void printResult(String test, long perstMs, long pgMs) {
        double speedup = pgMs > 0 ? (double) pgMs / perstMs : 0;
        System.out.printf("%-45s Perst: %7d ms  PostgreSQL: %7d ms  Speedup: %7.2fx%n",
                test, perstMs, pgMs, speedup);
    }

    static void setupPostgres(int count) throws SQLException {
        Statement stmt = pgConn.createStatement();
        stmt.execute("DROP TABLE IF EXISTS scale_records");
        stmt.execute("CREATE TABLE scale_records (id SERIAL PRIMARY KEY, int_key INTEGER, value TEXT)");
        stmt.execute("CREATE INDEX idx_scale_int_key ON scale_records(int_key)");
        pgConn.commit();
        stmt.close();
    }
    
    static int pagePoolSize = 256 * 1024 * 1024;
    
    static void setupPerst(int count) throws Exception {
        if (perstStorage != null && perstStorage.isOpened()) {
            perstStorage.close();
        }
        java.io.File dbFile = new java.io.File("benchmark6.dbs");
        if (dbFile.exists()) dbFile.delete();
        
        if (count >= 1000000) {
            pagePoolSize = 256 * 1024 * 1024;
        } else if (count >= 500000) {
            pagePoolSize = 128 * 1024 * 1024;
        } else {
            pagePoolSize = 64 * 1024 * 1024;
        }
        
        perstStorage = StorageFactory.getInstance().createStorage();
        perstStorage.open("benchmark6.dbs", pagePoolSize);
        recordIndex = perstStorage.createIndex(int.class, true);
    }

    static long testPerstInsert(int count) {
        long start = System.currentTimeMillis();
        
        for (int i = 0; i < count; i++) {
            Record r = new Record(i, "Value-" + i);
            recordIndex.put(new Key(i), r);
        }
        perstStorage.commit();
        
        return System.currentTimeMillis() - start;
    }

    static long testPostgresInsert(int count) throws SQLException {
        long start = System.currentTimeMillis();
        
        PreparedStatement ps = pgConn.prepareStatement(
            "INSERT INTO scale_records (int_key, value) VALUES (?, ?)"
        );
        
        for (int i = 0; i < count; i++) {
            ps.setInt(1, i);
            ps.setString(2, "Value-" + i);
            ps.addBatch();
            if (i % 1000 == 0) ps.executeBatch();
        }
        ps.executeBatch();
        pgConn.commit();
        ps.close();
        
        return System.currentTimeMillis() - start;
    }

    static long testPerstSearch(int count) {
        Random rand = new Random(42);
        long start = System.currentTimeMillis();
        
        for (int i = 0; i < count; i++) {
            int key = rand.nextInt(count);
            Record r = (Record) recordIndex.get(new Key(key));
        }
        
        return System.currentTimeMillis() - start;
    }

    static long testPostgresSearch(int count) throws SQLException {
        Random rand = new Random(42);
        long start = System.currentTimeMillis();
        
        PreparedStatement ps = pgConn.prepareStatement(
            "SELECT * FROM scale_records WHERE int_key = ?"
        );
        
        for (int i = 0; i < count; i++) {
            int key = rand.nextInt(count);
            ps.setInt(1, key);
            ResultSet rs = ps.executeQuery();
            rs.next();
            rs.close();
        }
        ps.close();
        
        return System.currentTimeMillis() - start;
    }

    static long testPerstIteration() {
        long start = System.currentTimeMillis();
        
        int count = 0;
        Iterator iter = recordIndex.iterator();
        while (iter.hasNext()) {
            Record r = (Record) iter.next();
            count++;
        }
        
        return System.currentTimeMillis() - start;
    }

    static long testPostgresIteration() throws SQLException {
        long start = System.currentTimeMillis();
        
        Statement stmt = pgConn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM scale_records");
        int count = 0;
        while (rs.next()) count++;
        rs.close();
        stmt.close();
        
        return System.currentTimeMillis() - start;
    }

    static long testPerstRangeQuery(int count) {
        long start = System.currentTimeMillis();
        
        Key from = new Key(count / 2);
        Key to = new Key(count - 1);
        Iterator iter = recordIndex.iterator(from, to, Index.ASCENT_ORDER);
        
        int found = 0;
        while (iter.hasNext()) {
            Record r = (Record) iter.next();
            found++;
        }
        
        return System.currentTimeMillis() - start;
    }

    static long testPostgresRangeQuery(int count) throws SQLException {
        long start = System.currentTimeMillis();
        
        PreparedStatement ps = pgConn.prepareStatement(
            "SELECT * FROM scale_records WHERE int_key >= ? AND int_key < ?"
        );
        ps.setInt(1, count / 2);
        ps.setInt(2, count);
        
        ResultSet rs = ps.executeQuery();
        int found = 0;
        while (rs.next()) found++;
        rs.close();
        ps.close();
        
        return System.currentTimeMillis() - start;
    }

    static long getPerstDatabaseSize() {
        java.io.File f = new java.io.File("benchmark6.dbs");
        return f.exists() ? f.length() : 0;
    }

    static long getPostgresDatabaseSize() throws SQLException {
        Statement stmt = pgConn.createStatement();
        ResultSet rs = stmt.executeQuery(
            "SELECT pg_database_size(current_database())"
        );
        rs.next();
        long size = rs.getLong(1);
        rs.close();
        stmt.close();
        return size;
    }

    public static void main(String[] args) throws Exception {
        System.out.println("=== Perst vs PostgreSQL - Data Size Scaling Benchmark ===");
        System.out.println("Testing 100K, 500K, and 1M records\n");
        
        Class.forName("org.postgresql.Driver");
        pgConn = DriverManager.getConnection(jdbcUrl, dbUser, dbPassword);
        pgConn.setAutoCommit(false);

        for (int count : RECORD_COUNTS) {
            System.out.println("========================================");
            System.out.println("Testing with " + (count / 1000) + "K records");
            System.out.println("========================================");
            
            System.out.println("\nSetting up databases...");
            setupPostgres(count);
            setupPerst(count);
            
            System.out.println("\n--- Test 1: Insert ---");
            long perstInsert = testPerstInsert(count);
            long pgInsert = testPostgresInsert(count);
            printResult("Insert " + count + " records", perstInsert, pgInsert);
            
            System.out.println("\n--- Test 2: Search (random lookups) ---");
            int searchCount = Math.min(count, 10000);
            long perstSearch = testPerstSearch(searchCount);
            long pgSearch = testPostgresSearch(searchCount);
            printResult("Search " + searchCount + " records", perstSearch, pgSearch);
            
            System.out.println("\n--- Test 3: Iteration ---");
            long perstIter = testPerstIteration();
            long pgIter = testPostgresIteration();
            printResult("Iterate all records", perstIter, pgIter);
            
            System.out.println("\n--- Test 4: Range Query ---");
            long perstRange = testPerstRangeQuery(count);
            long pgRange = testPostgresRangeQuery(count);
            printResult("Range query (50%)", perstRange, pgRange);
            
            System.out.println("\n--- Database Size ---");
            long perstSize = getPerstDatabaseSize();
            long pgSize = getPostgresDatabaseSize();
            System.out.printf("Perst:       %,d bytes (%,.2f MB)%n", perstSize, perstSize / 1024.0 / 1024.0);
            System.out.printf("PostgreSQL:  %,d bytes (%,.2f MB)%n", pgSize, pgSize / 1024.0 / 1024.0);
            System.out.printf("Ratio:       %.2fx%n", (double) perstSize / pgSize);
            
            recordIndex = null;
            if (perstStorage != null && perstStorage.isOpened()) {
                perstStorage.close();
            }
        }
        
        pgConn.close();
        
        java.io.File dbFile = new java.io.File("benchmark6.dbs");
        if (dbFile.exists()) dbFile.delete();
        
        System.out.println("\n=== Benchmark Complete ===");
    }
}
