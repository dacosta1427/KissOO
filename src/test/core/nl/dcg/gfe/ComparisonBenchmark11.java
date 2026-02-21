package nl.dcg.gfe;

import org.garret.perst.*;
import org.garret.perst.continuous.*;
import java.sql.*;
import java.util.*;
import java.util.concurrent.*;

public class ComparisonBenchmark11 {

    static class User extends CVersion {
        String username;
        double balance;
        
        public User() {}
        
        public User(String username, double balance) {
            this.username = username;
            this.balance = balance;
        }
    }

    static final int N_USERS = 100;
    static final int N_THREADS = 10;
    static final int OPERATIONS_PER_THREAD = 50;
    
    static String jdbcUrl = "jdbc:postgresql://localhost:5432/perst_test";
    static String dbUser = "postgres";
    static String dbPassword = "gfe";
    
    static Connection pgConn;
    
    static volatile int perstSuccess = 0;
    static volatile int pgSuccess = 0;
    
    static void setupPostgres() throws SQLException {
        Statement stmt = pgConn.createStatement();
        stmt.execute("DROP TABLE IF EXISTS conc_user_history CASCADE");
        stmt.execute("DROP TABLE IF EXISTS conc_users CASCADE");
        
        stmt.execute("""
            CREATE TABLE conc_users (
                id SERIAL PRIMARY KEY, 
                username VARCHAR(100) UNIQUE,
                balance DECIMAL(10,2),
                version INTEGER DEFAULT 1
            )
        """);
        
        pgConn.commit();
        stmt.close();
    }

    static void testPerstConcurrent() throws Exception {
        // Clean up first
        new java.io.File("benchmark11.dbs").delete();
        
        // Use thread-local Storage approach (like PerstContext does)
        ExecutorService executor = Executors.newFixedThreadPool(N_THREADS);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(N_THREADS);
        
        for (int t = 0; t < N_THREADS; t++) {
            final int threadId = t;
            executor.submit(() -> {
                Storage localStorage = null;
                try {
                    // Each thread gets its own Storage connection (thread-local like PerstContext)
                    localStorage = StorageFactory.getInstance().createStorage();
                    localStorage.setProperty("perst.gc.threshold", Long.MAX_VALUE);
                    localStorage.open("benchmark11.dbs", 64 * 1024 * 1024);
                    
                    // Create index if first thread
                    Index localIndex = localStorage.createIndex(String.class, true);
                    
                    startLatch.await();
                    Random rand = new Random(threadId);
                    
                    for (int i = 0; i < OPERATIONS_PER_THREAD; i++) {
                        try {
                            int userId = rand.nextInt(N_USERS);
                            String username = "user" + userId;
                            
                            // First thread populates
                            if (localIndex.size() < N_USERS && localIndex.get(new Key(username)) == null) {
                                User u = new User(username, 100.0);
                                localStorage.makePersistent(u);
                                localIndex.put(new Key(username), u);
                                localStorage.commit();
                            } else {
                                // Get and update
                                User user = (User) localIndex.get(new Key(username));
                                if (user != null) {
                                    user.balance += rand.nextDouble() * 10;
                                    localStorage.commit();
                                    perstSuccess++;
                                }
                            }
                        } catch (Exception e) {
                            try { localStorage.rollback(); } catch (Exception ignored) {}
                        }
                    }
                    
                    localStorage.close();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    doneLatch.countDown();
                }
            });
        }
        
        long start = System.currentTimeMillis();
        startLatch.countDown();
        doneLatch.await();
        long elapsed = System.currentTimeMillis() - start;
        
        executor.shutdown();
        
        System.out.println("\nPerst Concurrent (" + N_THREADS + " threads, thread-local Storage):");
        System.out.println("  Time: " + elapsed + " ms");
        System.out.println("  Successful updates: " + perstSuccess);
    }

    static void testPostgresConcurrent() throws Exception {
        // Reset data
        Statement stmt = pgConn.createStatement();
        stmt.execute("DELETE FROM conc_users");
        PreparedStatement ps = pgConn.prepareStatement(
            "INSERT INTO conc_users (username, balance, version) VALUES (?, ?, 1)"
        );
        Random rand = new Random(42);
        for (int i = 0; i < N_USERS; i++) {
            ps.setString(1, "user" + i);
            ps.setDouble(2, 100.0);
            ps.executeUpdate();
        }
        pgConn.commit();
        ps.close();
        
        ExecutorService executor = Executors.newFixedThreadPool(N_THREADS);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(N_THREADS);
        
        for (int t = 0; t < N_THREADS; t++) {
            final int threadId = t;
            executor.submit(() -> {
                Connection conn = null;
                try {
                    conn = DriverManager.getConnection(jdbcUrl, dbUser, dbPassword);
                    conn.setAutoCommit(false);
                    
                    startLatch.await();
                    Random r = new Random(threadId);
                    
                    for (int i = 0; i < OPERATIONS_PER_THREAD; i++) {
                        try {
                            int userId = r.nextInt(N_USERS) + 1;
                            
                            PreparedStatement ps2 = conn.prepareStatement(
                                "UPDATE conc_users SET balance = balance + ? WHERE id = ?"
                            );
                            ps2.setDouble(1, r.nextDouble() * 10);
                            ps2.setInt(2, userId);
                            ps2.executeUpdate();
                            conn.commit();
                            pgSuccess++;
                        } catch (SQLException e) {
                            try { conn.rollback(); } catch (Exception ignored) {}
                        }
                    }
                    
                    conn.close();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    doneLatch.countDown();
                }
            });
        }
        
        long start = System.currentTimeMillis();
        startLatch.countDown();
        doneLatch.await();
        long elapsed = System.currentTimeMillis() - start;
        
        executor.shutdown();
        
        System.out.println("\nPostgreSQL Concurrent (" + N_THREADS + " threads):");
        System.out.println("  Time: " + elapsed + " ms");
        System.out.println("  Successful updates: " + pgSuccess);
    }

    public static void main(String[] args) throws Exception {
        System.out.println("=== Concurrent Multi-User Benchmark ===");
        System.out.println("Users: " + N_USERS + ", Threads: " + N_THREADS + 
                          ", Ops/Thread: " + OPERATIONS_PER_THREAD);
        
        Class.forName("org.postgresql.Driver");
        pgConn = DriverManager.getConnection(jdbcUrl, dbUser, dbPassword);
        pgConn.setAutoCommit(false);
        
        System.out.println("\nSetting up PostgreSQL...");
        setupPostgres();
        
        System.out.println("\n--- Running Concurrent Tests ---");
        
        System.out.println("\nTesting Perst (thread-local Storage like PerstContext)...");
        testPerstConcurrent();
        
        System.out.println("\nTesting PostgreSQL...");
        testPostgresConcurrent();
        
        pgConn.close();
        
        new java.io.File("benchmark11.dbs").delete();
        
        System.out.println("\n=== Benchmark Complete ===");
    }
}
