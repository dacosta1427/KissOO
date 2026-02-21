package nl.dcg.gfe;

import org.garret.perst.*;
import java.sql.*;
import java.util.*;
import java.io.*;

public class ComparisonBenchmark8 {

    static class Record extends Persistent {
        int key;
        String data;
        
        public Record() {}
        
        public Record(int key) {
            this.key = key;
            this.data = "Data-" + key + "-" + new Random(key).nextLong();
        }
    }

    static final int N_RECORDS = 50000;
    
    static String jdbcUrl = "jdbc:postgresql://localhost:5432/perst_test";
    static String dbUser = "postgres";
    static String dbPassword = "gfe";
    
    static Connection pgConn;
    static Storage perstStorage;
    static Index recordIndex;
    
    static void printResult(String test, long perstMs, long pgMs) {
        if (pgMs <= 0) {
            System.out.printf("%-50s Perst: %6d ms%n", test, perstMs);
        } else if (perstMs <= 0) {
            System.out.printf("%-50s PostgreSQL: %6d ms%n", test, pgMs);
        } else {
            double speedup = (double) pgMs / perstMs;
            System.out.printf("%-50s Perst: %6d ms  PostgreSQL: %6d ms  Speedup: %7.2fx%n",
                    test, perstMs, pgMs, speedup);
        }
    }

    static void setupPostgres(boolean withData) throws SQLException {
        Statement stmt = pgConn.createStatement();
        stmt.execute("DROP TABLE IF EXISTS persist_records");
        stmt.execute("CREATE TABLE persist_records (id SERIAL PRIMARY KEY, int_key INTEGER, data TEXT)");
        stmt.execute("CREATE INDEX idx_persist_int_key ON persist_records(int_key)");
        
        if (withData) {
            PreparedStatement ps = pgConn.prepareStatement(
                "INSERT INTO persist_records (int_key, data) VALUES (?, ?)"
            );
            Random rand = new Random(42);
            for (int i = 0; i < N_RECORDS; i++) {
                ps.setInt(1, i);
                ps.setString(2, "Data-" + i + "-" + rand.nextLong());
                ps.addBatch();
                if (i % 1000 == 0) ps.executeBatch();
            }
            ps.executeBatch();
            ps.close();
        }
        
        pgConn.commit();
        stmt.close();
    }
    
    static final int pagePoolSize = 64 * 1024 * 1024;
    
    static void setupPerst(boolean withData) throws Exception {
        if (perstStorage != null && perstStorage.isOpened()) {
            perstStorage.close();
        }
        
        java.io.File dbFile = new java.io.File("benchmark8.dbs");
        if (dbFile.exists()) dbFile.delete();
        
        perstStorage = StorageFactory.getInstance().createStorage();
        perstStorage.open("benchmark8.dbs", pagePoolSize);
        
        recordIndex = perstStorage.createIndex(int.class, true);
        
        if (withData) {
            Random rand = new Random(42);
            for (int i = 0; i < N_RECORDS; i++) {
                Record r = new Record(i);
                r.data = "Data-" + i + "-" + rand.nextLong();
                recordIndex.put(new Key(i), r);
            }
            perstStorage.commit();
        }
    }

    static long testPerstBackup(String destFile) throws Exception {
        long start = System.currentTimeMillis();
        
        java.io.File src = new java.io.File("benchmark8.dbs");
        java.io.File dest = new java.io.File(destFile);
        
        try (FileInputStream fis = new FileInputStream(src);
             FileOutputStream fos = new FileOutputStream(dest)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
        }
        
        return System.currentTimeMillis() - start;
    }

    static long testPostgresBackup(String destFile) throws Exception {
        long start = System.currentTimeMillis();
        
        try {
            ProcessBuilder pb = new ProcessBuilder(
                "pg_dump", "-h", "localhost", "-U", "postgres", "-d", "perst_test", "-t", "persist_records", "-f", destFile
            );
            pb.environment().put("PGPASSWORD", "gfe");
            pb.redirectErrorStream(true);
            
            Process p = pb.start();
            int result = p.waitFor();
        } catch (IOException e) {
            Statement stmt = pgConn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT pg_current_wal_lsn()");
            rs.close();
            stmt.close();
            
            java.io.File dbFile = new java.io.File(destFile);
            try (FileOutputStream fos = new FileOutputStream(dbFile)) {
                fos.write(new byte[0]);
            }
        }
        
        return System.currentTimeMillis() - start;
    }

    static long testPerstRestore(String srcFile) throws Exception {
        perstStorage.close();
        
        java.io.File src = new java.io.File(srcFile);
        java.io.File dest = new java.io.File("benchmark8.dbs");
        if (dest.exists()) dest.delete();
        
        long start = System.currentTimeMillis();
        
        try (FileInputStream fis = new FileInputStream(src);
             FileOutputStream fos = new FileOutputStream(dest)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
        }
        
        perstStorage = StorageFactory.getInstance().createStorage();
        perstStorage.open("benchmark8.dbs", pagePoolSize);
        
        Object root = perstStorage.getRoot();
        if (root instanceof Index) {
            recordIndex = (Index) root;
        } else {
            recordIndex = perstStorage.createIndex(int.class, true);
        }
        
        return System.currentTimeMillis() - start;
    }

    static long testPostgresRestore(String srcFile) throws Exception {
        Statement stmt = pgConn.createStatement();
        stmt.execute("DROP TABLE IF EXISTS persist_records");
        stmt.execute("CREATE TABLE persist_records (id SERIAL PRIMARY KEY, int_key INTEGER, data TEXT)");
        pgConn.commit();
        
        long start = System.currentTimeMillis();
        
        try {
            ProcessBuilder pb = new ProcessBuilder(
                "psql", "-h", "localhost", "-U", "postgres", "-d", "perst_test", "-f", srcFile
            );
            pb.environment().put("PGPASSWORD", "gfe");
            pb.redirectErrorStream(true);
            
            Process p = pb.start();
            int result = p.waitFor();
        } catch (IOException e) {
            PreparedStatement ps = pgConn.prepareStatement(
                "INSERT INTO persist_records (int_key, data) VALUES (?, ?)"
            );
            Random rand = new Random(42);
            for (int i = 0; i < N_RECORDS; i++) {
                ps.setInt(1, i);
                ps.setString(2, "Data-" + i + "-" + rand.nextLong());
                ps.addBatch();
                if (i % 1000 == 0) ps.executeBatch();
            }
            ps.executeBatch();
            ps.close();
        }
        
        stmt.execute("CREATE INDEX idx_persist_int_key ON persist_records(int_key)");
        pgConn.commit();
        stmt.close();
        
        return System.currentTimeMillis() - start;
    }

    static long testPerstRecovery() throws Exception {
        for (int i = N_RECORDS; i < N_RECORDS + 10000; i++) {
            Record r = new Record(i);
            recordIndex.put(new Key(i), r);
        }
        perstStorage.commit();
        
        long start = System.currentTimeMillis();
        
        perstStorage.close();
        
        perstStorage = StorageFactory.getInstance().createStorage();
        perstStorage.open("benchmark8.dbs", pagePoolSize);
        
        Object root = perstStorage.getRoot();
        if (root instanceof Index) {
            recordIndex = (Index) root;
        } else {
            recordIndex = perstStorage.createIndex(int.class, true);
        }
        
        return System.currentTimeMillis() - start;
    }

    static long testPostgresRecovery() throws Exception {
        PreparedStatement ps = pgConn.prepareStatement(
            "INSERT INTO persist_records (int_key, data) VALUES (?, ?)"
        );
        Random rand = new Random(42);
        for (int i = N_RECORDS; i < N_RECORDS + 10000; i++) {
            ps.setInt(1, i);
            ps.setString(2, "Data-" + i + "-" + rand.nextLong());
            ps.addBatch();
            if (i % 1000 == 0) ps.executeBatch();
        }
        ps.executeBatch();
        pgConn.commit();
        ps.close();
        
        long start = System.currentTimeMillis();
        
        pgConn.close();
        Class.forName("org.postgresql.Driver");
        pgConn = DriverManager.getConnection(jdbcUrl, dbUser, dbPassword);
        pgConn.setAutoCommit(false);
        
        return System.currentTimeMillis() - start;
    }

    static long getPerstDatabaseSize() {
        java.io.File f = new java.io.File("benchmark8.dbs");
        return f.exists() ? f.length() : 0;
    }

    static long getPostgresDatabaseSize() throws SQLException {
        Statement stmt = pgConn.createStatement();
        ResultSet rs = stmt.executeQuery(
            "SELECT pg_total_relation_size('persist_records')"
        );
        rs.next();
        long size = rs.getLong(1);
        rs.close();
        stmt.close();
        return size;
    }

    static void testWritePerformance() throws Exception {
        System.out.println("\n--- Test: Write Performance ---");
        
        setupPerst(false);
        setupPostgres(false);
        
        long start = System.currentTimeMillis();
        for (int i = 0; i < N_RECORDS; i++) {
            Record r = new Record(i);
            recordIndex.put(new Key(i), r);
        }
        perstStorage.commit();
        long perstWrite = System.currentTimeMillis() - start;
        
        start = System.currentTimeMillis();
        PreparedStatement ps = pgConn.prepareStatement(
            "INSERT INTO persist_records (int_key, data) VALUES (?, ?)"
        );
        for (int i = 0; i < N_RECORDS; i++) {
            ps.setInt(1, i);
            ps.setString(2, "Data-" + i);
            ps.addBatch();
            if (i % 1000 == 0) ps.executeBatch();
        }
        ps.executeBatch();
        pgConn.commit();
        ps.close();
        long pgWrite = System.currentTimeMillis() - start;
        
        printResult("Write " + N_RECORDS + " records", perstWrite, pgWrite);
    }

    public static void main(String[] args) throws Exception {
        System.out.println("=== Perst vs PostgreSQL - Persistence & Recovery Benchmark ===");
        System.out.println("Records: " + N_RECORDS + "\n");
        
        Class.forName("org.postgresql.Driver");
        pgConn = DriverManager.getConnection(jdbcUrl, dbUser, dbPassword);
        pgConn.setAutoCommit(false);
        
        System.out.println("--- Test 1: Database Size ---");
        setupPerst(true);
        setupPostgres(true);
        
        long perstSize = getPerstDatabaseSize();
        long pgSize = getPostgresDatabaseSize();
        
        System.out.printf("Perst:       %,d bytes (%,.2f MB)%n", perstSize, perstSize / 1024.0 / 1024.0);
        System.out.printf("PostgreSQL:  %,d bytes (%,.2f MB)%n", pgSize, pgSize / 1024.0 / 1024.0);
        System.out.printf("Perst/PG Ratio: %.2fx%n", (double) perstSize / pgSize);
        
        System.out.println("\n--- Test 2: Backup Performance ---");
        String perstBackupFile = "benchmark8_perst_backup.dbs";
        String pgBackupFile = "benchmark8_pg_backup.sql";
        
        long perstBackup = testPerstBackup(perstBackupFile);
        long pgBackup = testPostgresBackup(pgBackupFile);
        
        printResult("Backup database", perstBackup, pgBackup);
        
        java.io.File pf = new java.io.File(perstBackupFile);
        java.io.File pgf = new java.io.File(pgBackupFile);
        System.out.printf("Perst backup file:  %,d bytes%n", pf.length());
        System.out.printf("PG backup file:     %,d bytes%n", pgf.length());
        
        System.out.println("\n--- Test 3: Restore Performance ---");
        
        long perstRestore = testPerstRestore(perstBackupFile);
        long pgRestore = testPostgresRestore(pgBackupFile);
        
        printResult("Restore database", perstRestore, pgRestore);
        
        System.out.println("\n--- Test 4: Recovery (Reconnect + Reopen) ---");
        
        long perstRecovery = testPerstRecovery();
        long pgRecovery = testPostgresRecovery();
        
        printResult("Recovery time (reopen/reconnect)", perstRecovery, pgRecovery);
        
        System.out.println("\n--- Test 5: Data Integrity After Recovery ---");
        
        int perstCount = 0;
        Iterator iter = recordIndex.iterator();
        while (iter.hasNext()) {
            iter.next();
            perstCount++;
        }
        
        Statement stmt = pgConn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM persist_records");
        rs.next();
        int pgCount = rs.getInt(1);
        rs.close();
        stmt.close();
        
        System.out.printf("Perst records after recovery:       %,d%n", perstCount);
        System.out.printf("PostgreSQL records after recovery:   %,d%n", pgCount);
        System.out.printf("Expected:                            %,d%n", N_RECORDS + 10000);
        
        testWritePerformance();
        
        perstStorage.close();
        pgConn.close();
        
        new java.io.File("benchmark8.dbs").delete();
        new java.io.File(perstBackupFile).delete();
        new java.io.File(pgBackupFile).delete();
        
        System.out.println("\n=== Benchmark Complete ===");
    }
}
