package nl.dcg.gfe;

import org.garret.perst.*;

import java.sql.*;
import java.util.*;

public class ComparisonBenchmark {
    static class Record extends Persistent {
        int intKey;
    };

    static class Indices extends Persistent {
        Index intIndex;
    }

    final static int nRecords = 100000;
    static int pagePoolSize = 64 * 1024 * 1024;

    static String jdbcUrl = "jdbc:postgresql://localhost:5432/perst_test";
    static String dbUser = "postgres";
    static String dbPassword = "gfe";

    static Connection pgConn;

    static void printResult(String test, long perstMs, long pgMs) {
        System.out.printf("%-40s Perst: %6d ms  PostgreSQL: %6d ms  Speedup: %.2fx%n",
                test, perstMs, pgMs, (double) pgMs / perstMs);
    }

    static void testInsert(Storage db, Index intIndex) throws Exception {
        long start = System.currentTimeMillis();
        for (int i = 0; i < nRecords; i++) {
            Record rec = new Record();
            rec.intKey = i;
            intIndex.put(new Key(rec.intKey), rec);
        }
        db.commit();
        long perstTime = System.currentTimeMillis() - start;

        start = System.currentTimeMillis();
        Statement stmt = pgConn.createStatement();
        stmt.execute("CREATE TABLE IF NOT EXISTS records (id SERIAL PRIMARY KEY, int_key INT)");
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_records_int_key ON records(int_key)");
        stmt.execute("DELETE FROM records");
        PreparedStatement ps = pgConn.prepareStatement("INSERT INTO records (int_key) VALUES (?)");
        for (int i = 0; i < nRecords; i++) {
            ps.setInt(1, i);
            ps.executeUpdate();
        }
        pgConn.commit();
        long pgTime = System.currentTimeMillis() - start;

        printResult("Insert " + nRecords + " records", perstTime, pgTime);
    }

    static void testSearch(Index intIndex) throws Exception {
        long start = System.currentTimeMillis();
        for (int i = 0; i < nRecords; i++) {
            Record rec = (Record) intIndex.get(new Key(i));
            Assert.that(rec != null && rec.intKey == i);
        }
        long perstTime = System.currentTimeMillis() - start;

        start = System.currentTimeMillis();
        PreparedStatement ps = pgConn.prepareStatement("SELECT * FROM records WHERE int_key = ?");
        for (int i = 0; i < nRecords; i++) {
            ps.setInt(1, i);
            ResultSet rs = ps.executeQuery();
            Assert.that(rs.next() && rs.getInt("int_key") == i);
            rs.close();
        }
        long pgTime = System.currentTimeMillis() - start;

        printResult("Search " + nRecords + " records", perstTime, pgTime);
    }

    static void testIteration(Index intIndex) throws Exception {
        long start = System.currentTimeMillis();
        Iterator iterator = intIndex.iterator();
        int i = 0;
        while (iterator.hasNext()) {
            Record rec = (Record) iterator.next();
            Assert.that(rec.intKey == i);
            i++;
        }
        Assert.that(i == nRecords);
        long perstTime = System.currentTimeMillis() - start;

        start = System.currentTimeMillis();
        Statement stmt = pgConn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM records ORDER BY int_key");
        i = 0;
        while (rs.next()) {
            Assert.that(rs.getInt("int_key") == i);
            i++;
        }
        Assert.that(i == nRecords);
        rs.close();
        long pgTime = System.currentTimeMillis() - start;

        printResult("Iterate " + nRecords + " records", perstTime, pgTime);
    }

    static void testDelete(Storage db, Index intIndex) throws Exception {
        long start = System.currentTimeMillis();
        for (int i = 0; i < nRecords; i++) {
            Record rec = (Record) intIndex.get(new Key(i));
            Record removed = (Record) intIndex.remove(new Key(i));
            Assert.that(removed == rec);
            rec.deallocate();
        }
        db.commit();
        long perstTime = System.currentTimeMillis() - start;

        start = System.currentTimeMillis();
        Statement stmt = pgConn.createStatement();
        stmt.execute("DELETE FROM records");
        pgConn.commit();
        long pgTime = System.currentTimeMillis() - start;

        printResult("Delete " + nRecords + " records", perstTime, pgTime);
    }

    static Storage db;
    static Index intIndex;

    public static void main(String[] args) throws Exception {
        for (int i = 0; i < args.length; i++) {
            if ("inmemory".equals(args[i])) {
                pagePoolSize = Storage.INFINITE_PAGE_POOL;
            } else if (args[i].startsWith("jdbc:")) {
                jdbcUrl = args[i];
            } else if (args[i].startsWith("--user=")) {
                dbUser = args[i].substring(7);
            } else if (args[i].startsWith("--password=")) {
                dbPassword = args[i].substring(11);
            } else if ("altbtree".equals(args[i])) {
                System.setProperty("perst.alternative.btree", "true");
            }
        }

        System.out.println("=== Perst vs PostgreSQL Comparison Benchmark ===");
        System.out.println("Records: " + nRecords);
        System.out.println();

        db = StorageFactory.getInstance().createStorage();
        if (pagePoolSize == Storage.INFINITE_PAGE_POOL) {
            db.open(new NullFile(), pagePoolSize);
        } else {
            db.open("benchmark.dbs", pagePoolSize);
        }

        Indices root = (Indices) db.getRoot();
        if (root == null) {
            root = new Indices();
            root.intIndex = db.createIndex(int.class, true);
            db.setRoot(root);
        }
        intIndex = root.intIndex;

        try {
            Class.forName("org.postgresql.Driver");
            pgConn = DriverManager.getConnection(jdbcUrl, dbUser, dbPassword);
            pgConn.setAutoCommit(false);
        } catch (ClassNotFoundException e) {
            System.err.println("PostgreSQL JDBC driver not found!");
            System.err.println("Add postgresql dependency to classpath");
            return;
        } catch (SQLException e) {
            System.err.println("Failed to connect to PostgreSQL: " + e.getMessage());
            System.err.println("JDBC URL: " + jdbcUrl);
            return;
        }

        System.out.println("--- Test 1: Insert ---");
        testInsert(db, intIndex);

        System.out.println("--- Test 2: Search ---");
        testSearch(intIndex);

        System.out.println("--- Test 3: Iteration ---");
        testIteration(intIndex);

        System.out.println("--- Test 4: Delete ---");
        testDelete(db, intIndex);

        db.close();
        pgConn.close();
    }
}
