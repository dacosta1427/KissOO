package nl.dcg.gfe;

import org.garret.perst.*;
import org.garret.perst.continuous.*;
import java.sql.*;
import java.util.*;

public class ComparisonBenchmark10 {

    static class User extends CVersion {
        String username;
        String email;
        String fullName;
        double balance;
        
        public User() {}
        
        public User(String username, String email, String fullName) {
            this.username = username;
            this.email = email;
            this.fullName = fullName;
            this.balance = 0.0;
        }
    }
    
    static class Task extends CVersion {
        String title;
        String description;
        String status;
        int priority;
        
        public Task() {}
        
        public Task(String title, String description, String status, int priority) {
            this.title = title;
            this.description = description;
            this.status = status;
            this.priority = priority;
        }
    }

    static final int N_USERS = 1000;
    static final int N_TASKS = 5000;
    static final int N_SESSIONS = 100;
    
    static String jdbcUrl = "jdbc:postgresql://localhost:5432/perst_test";
    static String dbUser = "postgres";
    static String dbPassword = "gfe";
    
    static Connection pgConn;
    static Storage perstStorage;
    static Index userIndex;
    static Index taskIndex;
    
    static void printResult(String test, long perstMs, long pgMs) {
        if (pgMs <= 0 || perstMs <= 0) {
            System.out.printf("%-55s Perst: %6d ms  PostgreSQL: %6d ms%n", test, perstMs, pgMs);
        } else {
            double speedup = (double) pgMs / perstMs;
            System.out.printf("%-55s Perst: %6d ms  PostgreSQL: %6d ms  Speedup: %7.2fx%n",
                    test, perstMs, pgMs, speedup);
        }
    }

    static void setupPostgres() throws SQLException {
        Statement stmt = pgConn.createStatement();
        stmt.execute("DROP TABLE IF EXISTS cver_user_history CASCADE");
        stmt.execute("DROP TABLE IF EXISTS cver_tasks CASCADE");
        stmt.execute("DROP TABLE IF EXISTS cver_users CASCADE");
        
        stmt.execute("""
            CREATE TABLE cver_users (
                id SERIAL PRIMARY KEY, 
                username VARCHAR(100) UNIQUE,
                email VARCHAR(255),
                full_name VARCHAR(255),
                balance DECIMAL(10,2),
                version INTEGER DEFAULT 1,
                created_at TIMESTAMP DEFAULT NOW()
            )
        """);
        
        stmt.execute("""
            CREATE TABLE cver_tasks (
                id SERIAL PRIMARY KEY,
                title VARCHAR(255),
                description TEXT,
                status VARCHAR(50),
                priority INTEGER,
                version INTEGER DEFAULT 1,
                created_at TIMESTAMP DEFAULT NOW()
            )
        """);
        
        stmt.execute("CREATE INDEX idx_cver_users_username ON cver_users(username)");
        stmt.execute("CREATE INDEX idx_cver_tasks_status ON cver_tasks(status)");
        
        stmt.execute("""
            CREATE TABLE cver_user_history (
                id SERIAL PRIMARY KEY,
                user_id INTEGER,
                balance DECIMAL(10,2),
                changed_at TIMESTAMP DEFAULT NOW()
            )
        """);
        
        pgConn.commit();
        stmt.close();
    }
    
    static void setupPerst() throws Exception {
        java.io.File dbFile = new java.io.File("benchmark10.dbs");
        if (dbFile.exists()) dbFile.delete();
        
        perstStorage = StorageFactory.getInstance().createStorage();
        perstStorage.setProperty("perst.gc.threshold", Long.MAX_VALUE);
        perstStorage.open("benchmark10.dbs", 64 * 1024 * 1024);
        
        userIndex = perstStorage.createIndex(String.class, true);
        taskIndex = perstStorage.createIndex(String.class, true);
    }

    static void populateData() throws Exception {
        Random rand = new Random(42);
        String[] statuses = {"open", "in_progress", "done", "blocked"};
        
        System.out.println("Populating PostgreSQL...");
        
        PreparedStatement userPs = pgConn.prepareStatement(
            "INSERT INTO cver_users (username, email, full_name, balance, version) VALUES (?, ?, ?, ?, 1)",
            Statement.RETURN_GENERATED_KEYS
        );
        
        for (int i = 0; i < N_USERS; i++) {
            userPs.setString(1, "user" + i);
            userPs.setString(2, "user" + i + "@example.com");
            userPs.setString(3, "User " + i);
            userPs.setDouble(4, rand.nextDouble() * 1000);
            userPs.executeUpdate();
        }
        pgConn.commit();
        userPs.close();
        
        PreparedStatement taskPs = pgConn.prepareStatement(
            "INSERT INTO cver_tasks (title, description, status, priority, version) VALUES (?, ?, ?, ?, 1)",
            Statement.RETURN_GENERATED_KEYS
        );
        
        for (int i = 0; i < N_TASKS; i++) {
            taskPs.setString(1, "Task-" + i);
            taskPs.setString(2, "Description for task " + i);
            taskPs.setString(3, statuses[i % statuses.length]);
            taskPs.setInt(4, rand.nextInt(5));
            taskPs.executeUpdate();
        }
        pgConn.commit();
        taskPs.close();
        
        System.out.println("Populating Perst with CVersion...");
        
        for (int i = 0; i < N_USERS; i++) {
            User u = new User("user" + i, "user" + i + "@example.com", "User " + i);
            u.balance = rand.nextDouble() * 1000;
            perstStorage.makePersistent(u);
            userIndex.put(new Key(u.username), u);
        }
        
        for (int i = 0; i < N_TASKS; i++) {
            Task t = new Task("Task-" + i, "Description for task " + i, statuses[i % statuses.length], rand.nextInt(5));
            perstStorage.makePersistent(t);
            taskIndex.put(new Key(t.title), t);
        }
        
        perstStorage.commit();
    }

    static long testPerstCVersionUpdate(int sessionId, Random rand) throws Exception {
        long start = System.currentTimeMillis();
        
        int userId = rand.nextInt(N_USERS);
        User user = (User) userIndex.get(new Key("user" + userId));
        
        user.balance += rand.nextDouble() * 10;
        
        perstStorage.commit();
        return System.currentTimeMillis() - start;
    }

    static long testPostgresVersionedUpdate(int sessionId, Random rand) throws Exception {
        long start = System.currentTimeMillis();
        
        int userId = rand.nextInt(N_USERS) + 1;
        
        pgConn.setAutoCommit(false);
        
        PreparedStatement ps = pgConn.prepareStatement(
            "SELECT balance, version FROM cver_users WHERE id = ? FOR UPDATE"
        );
        ps.setInt(1, userId);
        ResultSet rs = ps.executeQuery();
        
        if (rs.next()) {
            double newBalance = rs.getDouble("balance") + rand.nextDouble() * 10;
            int currentVersion = rs.getInt("version");
            
            PreparedStatement insertHist = pgConn.prepareStatement(
                "INSERT INTO cver_user_history (user_id, balance) VALUES (?, ?)"
            );
            insertHist.setInt(1, userId);
            insertHist.setDouble(2, newBalance);
            insertHist.executeUpdate();
            insertHist.close();
            
            PreparedStatement updatePs = pgConn.prepareStatement(
                "UPDATE cver_users SET balance = ?, version = version + 1 WHERE id = ? AND version = ?"
            );
            updatePs.setDouble(1, newBalance);
            updatePs.setInt(2, userId);
            updatePs.setInt(3, currentVersion);
            updatePs.executeUpdate();
            
            pgConn.commit();
            updatePs.close();
        }
        
        rs.close();
        ps.close();
        pgConn.setAutoCommit(true);
        
        return System.currentTimeMillis() - start;
    }

    static long testPerstVersionHistory(int sessionId, Random rand) throws Exception {
        long start = System.currentTimeMillis();
        
        int userId = rand.nextInt(N_USERS);
        User user = (User) userIndex.get(new Key("user" + userId));
        
        CVersionHistory history = user.getVersionHistory();
        int numVersions = history != null ? history.getNumberOfVersions() : 0;
        
        perstStorage.commit();
        return System.currentTimeMillis() - start;
    }

    static long testPostgresVersionHistory(int sessionId, Random rand) throws Exception {
        long start = System.currentTimeMillis();
        
        int userId = rand.nextInt(N_USERS) + 1;
        
        Statement stmt = pgConn.createStatement();
        ResultSet rs = stmt.executeQuery(
            "SELECT COUNT(*) as cnt FROM cver_user_history WHERE user_id = " + userId
        );
        rs.next();
        int count = rs.getInt("cnt");
        rs.close();
        stmt.close();
        
        return System.currentTimeMillis() - start;
    }

    static long testPerstGetCurrentVersion(int sessionId, Random rand) throws Exception {
        long start = System.currentTimeMillis();
        
        int userId = rand.nextInt(N_USERS);
        User user = (User) userIndex.get(new Key("user" + userId));
        
        CVersionHistory history = user.getVersionHistory();
        CVersion current = history != null ? history.getCurrent() : user;
        double balance = current != null ? ((User)current).balance : user.balance;
        
        perstStorage.commit();
        return System.currentTimeMillis() - start;
    }

    static long testPostgresGetCurrentVersion(int sessionId, Random rand) throws Exception {
        long start = System.currentTimeMillis();
        
        int userId = rand.nextInt(N_USERS) + 1;
        
        PreparedStatement ps = pgConn.prepareStatement(
            "SELECT balance, version FROM cver_users WHERE id = ?"
        );
        ps.setInt(1, userId);
        ResultSet rs = ps.executeQuery();
        rs.next();
        double balance = rs.getDouble("balance");
        rs.close();
        ps.close();
        
        return System.currentTimeMillis() - start;
    }

    public static void main(String[] args) throws Exception {
        System.out.println("=== Perst CVersion vs PostgreSQL Versioning Benchmark ===");
        System.out.println("Users: " + N_USERS + ", Tasks: " + N_TASKS + ", Sessions: " + N_SESSIONS);
        System.out.println("Testing CVersion (automatic versioning) vs manual PostgreSQL versioning\n");
        
        Class.forName("org.postgresql.Driver");
        pgConn = DriverManager.getConnection(jdbcUrl, dbUser, dbPassword);
        pgConn.setAutoCommit(false);
        
        System.out.println("Setting up databases...");
        setupPostgres();
        setupPerst();
        
        System.out.println("Populating data...");
        populateData();
        
        Random rand = new Random(12345);
        
        System.out.println("\n=== Running " + N_SESSIONS + " test iterations ===\n");
        
        long totalPerstUpdate = 0;
        long totalPgUpdate = 0;
        
        long totalPerstHistory = 0;
        long totalPgHistory = 0;
        
        long totalPerstCurrent = 0;
        long totalPgCurrent = 0;
        
        for (int session = 0; session < N_SESSIONS; session++) {
            totalPerstUpdate += testPerstCVersionUpdate(session, rand);
            totalPgUpdate += testPostgresVersionedUpdate(session, rand);
            
            if (session % 10 == 0) {
                totalPerstHistory += testPerstVersionHistory(session, rand);
                totalPgHistory += testPostgresVersionHistory(session, rand);
            }
            
            totalPerstCurrent += testPerstGetCurrentVersion(session, rand);
            totalPgCurrent += testPostgresGetCurrentVersion(session, rand);
        }
        
        System.out.println("=== Results ===\n");
        
        printResult("Update with Version Tracking", totalPerstUpdate, totalPgUpdate);
        printResult("Get Version History", totalPerstHistory, totalPgHistory);
        printResult("Get Current Version", totalPerstCurrent, totalPgCurrent);
        
        System.out.println("\n--- CVersion Key Features ---");
        System.out.println("Perst CVersion:");
        System.out.println("  - Automatic version history on every commit");
        System.out.println("  - getVersionHistory().getCurrent() - get latest version");
        System.out.println("  - getVersionHistory().getNumberOfVersions() - count versions");
        System.out.println("  - No application code needed for versioning");
        
        System.out.println("\nPostgreSQL Manual Versioning:");
        System.out.println("  - Manual version column in table");
        System.out.println("  - Application-level history tracking (separate table)");
        System.out.println("  - FOR UPDATE locking for conflict detection");
        System.out.println("  - Complex queries to track history");
        
        perstStorage.close();
        pgConn.close();
        
        java.io.File dbFile = new java.io.File("benchmark10.dbs");
        if (dbFile.exists()) dbFile.delete();
        
        System.out.println("\n=== Benchmark Complete ===");
    }
}
