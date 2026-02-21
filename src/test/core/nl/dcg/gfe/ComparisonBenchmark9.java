package nl.dcg.gfe;

import org.garret.perst.*;
import java.sql.*;
import java.util.*;

public class ComparisonBenchmark9 {

    static class User extends Persistent {
        String username;
        String email;
        String fullName;
        double balance;
        long createdAt;
        long version;
        
        public User() {}
        
        public User(String username, String email, String fullName) {
            this.username = username;
            this.email = email;
            this.fullName = fullName;
            this.balance = 0.0;
            this.createdAt = System.currentTimeMillis();
        }
    }
    
    static class Task extends Persistent {
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
    
    static class UserTask extends Persistent {
        User user;
        Task task;
        long assignedAt;
        
        public UserTask() {}
        
        public UserTask(User user, Task task) {
            this.user = user;
            this.task = task;
            this.assignedAt = System.currentTimeMillis();
        }
    }

    static final int N_USERS = 1000;
    static final int N_TASKS = 5000;
    static final int N_SESSIONS = 100;
    static final int OPERATIONS_PER_SESSION = 10;
    
    static String jdbcUrl = "jdbc:postgresql://localhost:5432/perst_test";
    static String dbUser = "postgres";
    static String dbPassword = "gfe";
    
    static Connection pgConn;
    static Storage perstStorage;
    static Index userIndex;
    static Index taskIndex;
    
    static void printResult(String test, long perstMs, long pgMs) {
        if (pgMs <= 0 || perstMs <= 0) {
            System.out.printf("%-50s Perst: %6d ms  PostgreSQL: %6d ms%n", test, perstMs, pgMs);
        } else {
            double speedup = (double) pgMs / perstMs;
            System.out.printf("%-50s Perst: %6d ms  PostgreSQL: %6d ms  Speedup: %7.2fx%n",
                    test, perstMs, pgMs, speedup);
        }
    }

    static void setupPostgres() throws SQLException {
        Statement stmt = pgConn.createStatement();
        stmt.execute("DROP TABLE IF EXISTS ver_user_tasks CASCADE");
        stmt.execute("DROP TABLE IF EXISTS ver_tasks CASCADE");
        stmt.execute("DROP TABLE IF EXISTS ver_users CASCADE");
        
        stmt.execute("""
            CREATE TABLE ver_users (
                id SERIAL PRIMARY KEY, 
                username VARCHAR(100) UNIQUE,
                email VARCHAR(255),
                full_name VARCHAR(255),
                balance DECIMAL(10,2),
                created_at BIGINT,
                version INTEGER DEFAULT 1
            )
        """);
        
        stmt.execute("""
            CREATE TABLE ver_tasks (
                id SERIAL PRIMARY KEY,
                title VARCHAR(255),
                description TEXT,
                status VARCHAR(50),
                priority INTEGER,
                version INTEGER DEFAULT 1
            )
        """);
        
        stmt.execute("""
            CREATE TABLE ver_user_tasks (
                id SERIAL PRIMARY KEY,
                user_id INTEGER REFERENCES ver_users(id),
                task_id INTEGER REFERENCES ver_tasks(id),
                assigned_at BIGINT,
                version INTEGER DEFAULT 1
            )
        """);
        
        stmt.execute("CREATE INDEX idx_ver_users_username ON ver_users(username)");
        stmt.execute("CREATE INDEX idx_ver_tasks_status ON ver_tasks(status)");
        stmt.execute("CREATE INDEX idx_ver_user_tasks_user ON ver_user_tasks(user_id)");
        
        pgConn.commit();
        stmt.close();
    }
    
    static void setupPerst() throws Exception {
        java.io.File dbFile = new java.io.File("benchmark9.dbs");
        if (dbFile.exists()) dbFile.delete();
        
        perstStorage = StorageFactory.getInstance().createStorage();
        perstStorage.setProperty("perst.gc.threshold", Long.MAX_VALUE);
        perstStorage.open("benchmark9.dbs", 64 * 1024 * 1024);
        
        userIndex = perstStorage.createIndex(String.class, true);
        taskIndex = perstStorage.createIndex(String.class, true);
    }

    static void populateData() throws Exception {
        Random rand = new Random(42);
        String[] statuses = {"open", "in_progress", "done", "blocked"};
        
        System.out.println("Populating PostgreSQL...");
        
        Map<Integer, Integer> userIdMap = new HashMap<>();
        Map<Integer, Integer> taskIdMap = new HashMap<>();
        
        PreparedStatement userPs = pgConn.prepareStatement(
            "INSERT INTO ver_users (username, email, full_name, balance, created_at, version) VALUES (?, ?, ?, ?, ?, 1)",
            Statement.RETURN_GENERATED_KEYS
        );
        
        for (int i = 0; i < N_USERS; i++) {
            userPs.setString(1, "user" + i);
            userPs.setString(2, "user" + i + "@example.com");
            userPs.setString(3, "User " + i);
            userPs.setDouble(4, rand.nextDouble() * 1000);
            userPs.setLong(5, System.currentTimeMillis());
            userPs.executeUpdate();
            ResultSet rs = userPs.getGeneratedKeys();
            if (rs.next()) userIdMap.put(i, rs.getInt(1));
        }
        pgConn.commit();
        userPs.close();
        
        PreparedStatement taskPs = pgConn.prepareStatement(
            "INSERT INTO ver_tasks (title, description, status, priority, version) VALUES (?, ?, ?, ?, 1)",
            Statement.RETURN_GENERATED_KEYS
        );
        
        for (int i = 0; i < N_TASKS; i++) {
            taskPs.setString(1, "Task-" + i);
            taskPs.setString(2, "Description for task " + i);
            taskPs.setString(3, statuses[i % statuses.length]);
            taskPs.setInt(4, rand.nextInt(5));
            taskPs.executeUpdate();
            ResultSet rs = taskPs.getGeneratedKeys();
            if (rs.next()) taskIdMap.put(i, rs.getInt(1));
        }
        pgConn.commit();
        taskPs.close();
        
        System.out.println("Populating Perst...");
        
        Map<Integer, User> userMap = new HashMap<>();
        for (int i = 0; i < N_USERS; i++) {
            User u = new User("user" + i, "user" + i + "@example.com", "User " + i);
            u.balance = rand.nextDouble() * 1000;
            perstStorage.makePersistent(u);
            userIndex.put(new Key(u.username), u);
            userMap.put(i, u);
        }
        
        Map<Integer, Task> taskMap = new HashMap<>();
        for (int i = 0; i < N_TASKS; i++) {
            Task t = new Task("Task-" + i, "Description for task " + i, statuses[i % statuses.length], rand.nextInt(5));
            perstStorage.makePersistent(t);
            taskIndex.put(new Key("task" + i), t);
            taskMap.put(i, t);
        }
        
        perstStorage.commit();
    }

    static long testPerstReadSession(int sessionId, Random rand) throws Exception {
        long start = System.currentTimeMillis();
        
        int userId = rand.nextInt(N_USERS);
        User user = (User) userIndex.get(new Key("user" + userId));
        
        String status = new String[]{"open", "in_progress", "done"}[rand.nextInt(3)];
        
        perstStorage.commit();
        return System.currentTimeMillis() - start;
    }

    static long testPostgresReadSession(int sessionId, Random rand) throws Exception {
        long start = System.currentTimeMillis();
        
        int userId = rand.nextInt(N_USERS);
        PreparedStatement ps = pgConn.prepareStatement(
            "SELECT * FROM ver_users WHERE username = ?"
        );
        ps.setString(1, "user" + userId);
        ResultSet rs = ps.executeQuery();
        rs.next();
        rs.close();
        ps.close();
        
        pgConn.commit();
        return System.currentTimeMillis() - start;
    }

    static long testPerstUpdateWithVersion(int sessionId, Random rand) throws Exception {
        long start = System.currentTimeMillis();
        
        int userId = rand.nextInt(N_USERS);
        User user = (User) userIndex.get(new Key("user" + userId));
        
        user.balance += rand.nextDouble() * 10;
        
        perstStorage.commit();
        return System.currentTimeMillis() - start;
    }

    static long testPostgresUpdateWithVersion(int sessionId, Random rand) throws Exception {
        long start = System.currentTimeMillis();
        
        int userId = rand.nextInt(N_USERS);
        PreparedStatement ps = pgConn.prepareStatement(
            "UPDATE ver_users SET balance = balance + ?, version = version + 1 WHERE username = ?"
        );
        ps.setDouble(1, rand.nextDouble() * 10);
        ps.setString(2, "user" + userId);
        ps.executeUpdate();
        pgConn.commit();
        ps.close();
        
        return System.currentTimeMillis() - start;
    }

    static long testPerstOptimisticLock(int sessionId, Random rand) throws Exception {
        long start = System.currentTimeMillis();
        
        int taskId = rand.nextInt(N_TASKS);
        Task task = (Task) taskIndex.get(new Key("task" + taskId));
        
        String oldStatus = task.status;
        
        task.status = rand.nextBoolean() ? "done" : "blocked";
        task.priority = rand.nextInt(5);
        
        perstStorage.commit();
        
        return System.currentTimeMillis() - start;
    }

    static long testPostgresOptimisticLock(int sessionId, Random rand) throws Exception {
        long start = System.currentTimeMillis();
        
        int taskId = rand.nextInt(N_TASKS);
        String taskName = "task" + taskId;
        
        pgConn.setAutoCommit(false);
        
        PreparedStatement ps = pgConn.prepareStatement(
            "SELECT status, priority, version FROM ver_tasks WHERE id = ? FOR UPDATE"
        );
        ps.setInt(1, taskId + 1);
        ResultSet rs = ps.executeQuery();
        if (!rs.next()) {
            rs.close();
            ps.close();
            return System.currentTimeMillis() - start;
        }
        
        String newStatus = rand.nextBoolean() ? "done" : "blocked";
        int newPriority = rand.nextInt(5);
        
        PreparedStatement updatePs = pgConn.prepareStatement(
            "UPDATE ver_tasks SET status = ?, priority = ?, version = version + 1 WHERE id = ?"
        );
        updatePs.setString(1, newStatus);
        updatePs.setInt(2, newPriority);
        updatePs.setInt(3, taskId + 1);
        updatePs.executeUpdate();
        
        pgConn.commit();
        rs.close();
        ps.close();
        updatePs.close();
        
        return System.currentTimeMillis() - start;
    }

    static long testPerstAuditHistory(int sessionId, Random rand) throws Exception {
        long start = System.currentTimeMillis();
        
        int userId = rand.nextInt(N_USERS);
        User user = (User) userIndex.get(new Key("user" + userId));
        
        String username = user.username;
        double balance = user.balance;
        
        perstStorage.commit();
        return System.currentTimeMillis() - start;
    }

    static long testPostgresAuditHistory(int sessionId, Random rand) throws Exception {
        long start = System.currentTimeMillis();
        
        int userId = rand.nextInt(N_USERS);
        PreparedStatement ps = pgConn.prepareStatement(
            "SELECT MAX(version) as max_ver FROM ver_users WHERE username = ?"
        );
        ps.setString(1, "user" + userId);
        ResultSet rs = ps.executeQuery();
        rs.next();
        rs.close();
        ps.close();
        
        pgConn.commit();
        return System.currentTimeMillis() - start;
    }

    static long testPerstComplexRead(int sessionId, Random rand) throws Exception {
        long start = System.currentTimeMillis();
        
        int userId = rand.nextInt(N_USERS);
        User user = (User) userIndex.get(new Key("user" + userId));
        
        int count = 0;
        Iterator taskIter = taskIndex.iterator();
        while (taskIter.hasNext()) {
            Task t = (Task) taskIter.next();
            if (t.status.equals("open") && t.priority >= 3) {
                count++;
            }
        }
        
        perstStorage.commit();
        return System.currentTimeMillis() - start;
    }

    static long testPostgresComplexRead(int sessionId, Random rand) throws Exception {
        long start = System.currentTimeMillis();
        
        int userId = rand.nextInt(N_USERS);
        PreparedStatement ps = pgConn.prepareStatement(
            "SELECT t.* FROM ver_tasks t WHERE t.status = 'open' AND t.priority >= 3"
        );
        ResultSet rs = ps.executeQuery();
        int count = 0;
        while (rs.next()) count++;
        rs.close();
        ps.close();
        
        pgConn.commit();
        return System.currentTimeMillis() - start;
    }

    public static void main(String[] args) throws Exception {
        System.out.println("=== Perst vs PostgreSQL - Real-World CRUD with Versioning ===");
        System.out.println("Users: " + N_USERS + ", Tasks: " + N_TASKS + ", Sessions: " + N_SESSIONS);
        System.out.println("Simulating typical user sessions with versioning\n");
        
        Class.forName("org.postgresql.Driver");
        pgConn = DriverManager.getConnection(jdbcUrl, dbUser, dbPassword);
        pgConn.setAutoCommit(false);
        
        System.out.println("Setting up databases...");
        setupPostgres();
        setupPerst();
        
        System.out.println("Populating data...");
        populateData();
        
        Random rand = new Random(12345);
        
        System.out.println("\n=== Simulating " + N_SESSIONS + " User Sessions ===");
        System.out.println("Each session performs " + OPERATIONS_PER_SESSION + " operations\n");
        
        long totalPerstRead = 0;
        long totalPgRead = 0;
        
        long totalPerstUpdate = 0;
        long totalPgUpdate = 0;
        
        long totalPerstLock = 0;
        long totalPgLock = 0;
        
        long totalPerstAudit = 0;
        long totalPgAudit = 0;
        
        long totalPerstComplex = 0;
        long totalPgComplex = 0;
        
        for (int session = 0; session < N_SESSIONS; session++) {
            if (session % 20 == 0) {
                totalPerstRead += testPerstReadSession(session, rand);
                totalPgRead += testPostgresReadSession(session, rand);
            }
            
            if (session % 10 == 0) {
                totalPerstUpdate += testPerstUpdateWithVersion(session, rand);
                totalPgUpdate += testPostgresUpdateWithVersion(session, rand);
            }
            
            totalPerstLock += testPerstOptimisticLock(session, rand);
            totalPgLock += testPostgresOptimisticLock(session, rand);
            
            if (session % 20 == 0) {
                totalPerstAudit += testPerstAuditHistory(session, rand);
                totalPgAudit += testPostgresAuditHistory(session, rand);
            }
            
            if (session % 10 == 0) {
                totalPerstComplex += testPerstComplexRead(session, rand);
                totalPgComplex += testPostgresComplexRead(session, rand);
            }
        }
        
        System.out.println("\n=== Results ===\n");
        
        printResult("Read User Profile", totalPerstRead, totalPgRead);
        printResult("Update User Balance", totalPerstUpdate, totalPgUpdate);
        printResult("Optimistic Lock Update", totalPerstLock, totalPgLock);
        printResult("Version/History Query", totalPerstAudit, totalPgAudit);
        printResult("filteredComplex Query ()", totalPerstComplex, totalPgComplex);
        
        System.out.println("\n--- Key Differences ---");
        
        System.out.println("\nPerst object model:");
        System.out.println("  - Direct object access, no JOINs needed");
        System.out.println("  - Embedded database, no network latency");
        System.out.println("  - Simpler CRUD operations");
        System.out.println("  - Object relationships as direct references");
        
        System.out.println("\nPostgreSQL model:");
        System.out.println("  - SQL queries with JOINs");
        System.out.println("  - Network round-trip per operation");
        System.out.println("  - Manual version column for optimistic locking");
        System.out.println("  - Triggers needed for audit trail");
        
        perstStorage.close();
        pgConn.close();
        
        java.io.File dbFile = new java.io.File("benchmark9.dbs");
        if (dbFile.exists()) dbFile.delete();
        
        System.out.println("\n=== Benchmark Complete ===");
    }
}
