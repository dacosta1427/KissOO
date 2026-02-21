package nl.dcg.gfe;

import org.garret.perst.*;
import org.garret.perst.continuous.*;
import java.sql.*;
import java.util.*;

public class ComparisonBenchmark12 {

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
    static final int N_OPERATIONS = 500;
    
    static String jdbcUrl = "jdbc:postgresql://localhost:5432/perst_test";
    static String dbUser = "postgres";
    static String dbPassword = "gfe";
    
    static Connection pgConn;
    static Storage perstStorage;
    
    static void setupPostgres() throws SQLException {
        Statement stmt = pgConn.createStatement();
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
    
    static void setupPerst() throws Exception {
        // Clean up any existing database
        new java.io.File("benchmark12.dbs").delete();
        
        perstStorage = StorageFactory.getInstance().createStorage();
        perstStorage.setProperty("perst.gc.threshold", Long.MAX_VALUE);
        perstStorage.open("benchmark12.dbs", 64 * 1024 * 1024);
        
        // Create index
        Index index = perstStorage.createIndex(String.class, true);
        perstStorage.setRoot(index);
        
        // Populate with users
        Random rand = new Random(42);
        for (int i = 0; i < N_USERS; i++) {
            User u = new User("user" + i, 100.0);
            perstStorage.makePersistent(u);
            index.put(new Key(u.username), u);
        }
        perstStorage.commit();
        
        System.out.println("Perst initialized with " + index.size() + " users");
    }

    static void testPerstSequential() throws Exception {
        Random rand = new Random(12345);
        long start = System.currentTimeMillis();
        
        for (int i = 0; i < N_OPERATIONS; i++) {
            int userId = rand.nextInt(N_USERS);
            String username = "user" + userId;
            
            Index index = (Index) perstStorage.getRoot();
            User user = (User) index.get(new Key(username));
            
            if (user != null) {
                user.balance += rand.nextDouble() * 10;
                perstStorage.commit();
            }
        }
        
        long elapsed = System.currentTimeMillis() - start;
        
        System.out.println("\nPerst Sequential (" + N_OPERATIONS + " operations):");
        System.out.println("  Time: " + elapsed + " ms");
        System.out.println("  Avg per operation: " + (elapsed * 1000.0 / N_OPERATIONS) + " microsec");
    }

    static void testPostgresSequential() throws Exception {
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
        
        rand = new Random(12345);
        long start = System.currentTimeMillis();
        
        for (int i = 0; i < N_OPERATIONS; i++) {
            int userId = rand.nextInt(N_USERS) + 1;
            
            PreparedStatement ps2 = pgConn.prepareStatement(
                "UPDATE conc_users SET balance = balance + ? WHERE id = ?"
            );
            ps2.setDouble(1, rand.nextDouble() * 10);
            ps2.setInt(2, userId);
            ps2.executeUpdate();
            pgConn.commit();
            ps2.close();
        }
        
        long elapsed = System.currentTimeMillis() - start;
        
        System.out.println("\nPostgreSQL Sequential (" + N_OPERATIONS + " operations):");
        System.out.println("  Time: " + elapsed + " ms");
        System.out.println("  Avg per operation: " + (elapsed * 1000.0 / N_OPERATIONS) + " microsec");
    }

    public static void main(String[] args) throws Exception {
        System.out.println("=== Sequential User Operations Benchmark ===");
        System.out.println("Users: " + N_USERS + ", Operations: " + N_OPERATIONS);
        System.out.println("\nThis demonstrates the Perst workflow that would be used in a multi-user environment:");
        System.out.println("- Each request uses PerstContext (or thread-local Storage)");
        System.out.println("- beginTransaction() / commitTransaction() pattern");
        
        Class.forName("org.postgresql.Driver");
        pgConn = DriverManager.getConnection(jdbcUrl, dbUser, dbPassword);
        pgConn.setAutoCommit(false);
        
        System.out.println("\nSetting up databases...");
        setupPostgres();
        setupPerst();
        
        System.out.println("\n--- Running Tests ---");
        
        System.out.println("\nTesting Perst (single-threaded simulation)...");
        testPerstSequential();
        
        System.out.println("\nTesting PostgreSQL...");
        testPostgresSequential();
        
        perstStorage.close();
        pgConn.close();
        
        new java.io.File("benchmark12.dbs").delete();
        
        System.out.println("\n=== Benchmark Complete ===");
        System.out.println("\nNote: Perst Storage is NOT thread-safe.");
        System.out.println("In production, use PerstContext which provides:");
        System.out.println("  - Thread-local Storage instances");
        System.out.println("  - Proper transaction management per request");
        System.out.println("  - Automatic conflict detection via CVersion");
    }
}
