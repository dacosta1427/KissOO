package nl.dcg.gfe;

import org.garret.perst.*;

import java.sql.*;
import java.util.*;

/**
 * ComparisonBenchmark2 - Enhanced benchmark with real-world scenarios
 * 
 * Tests complex objects, relationships, and optimized PostgreSQL operations.
 */
public class ComparisonBenchmark2 {

    // ==================== Complex Object Models ====================
    
    static class Movie extends Persistent {
        String uuid;
        String title;
        int year;
        String genre;
        double rating;
        String actorIds; // JSON array of actor UUIDs
        
        public Movie() {}
        
        public Movie(String title, int year, String genre, double rating) {
            this.uuid = UUID.randomUUID().toString();
            this.title = title;
            this.year = year;
            this.genre = genre;
            this.rating = rating;
            this.actorIds = "[]";
        }
    }
    
    static class Actor extends Persistent {
        String uuid;
        String name;
        int birthYear;
        String country;
        
        public Actor() {}
        
        public Actor(String name, int birthYear, String country) {
            this.uuid = UUID.randomUUID().toString();
            this.name = name;
            this.birthYear = birthYear;
            this.country = country;
        }
    }

    // ==================== Configuration ====================
    
    static final int N_MOVIES = 10000;
    static final int N_ACTORS = 5000;
    static final int N_RELATIONSHIPS = 30000;
    
    static String jdbcUrl = "jdbc:postgresql://localhost:5432/perst_test";
    static String dbUser = "postgres";
    static String dbPassword = "gfe";
    
    static Connection pgConn;
    static Storage perstStorage;
    static Index intIndex;
    static Index actorIndex;
    
    static void printResult(String test, long perstMs, long pgMs) {
        if (pgMs > 0) {
            double speedup = (double) pgMs / perstMs;
            System.out.printf("%-45s Perst: %6d ms  PostgreSQL: %6d ms  Speedup: %7.2fx%n",
                    test, perstMs, pgMs, speedup);
        } else {
            System.out.printf("%-45s Perst: %6d ms  PostgreSQL: %6d ms%n",
                    test, perstMs, pgMs);
        }
    }

    // ==================== PostgreSQL Setup ====================
    
    static void setupPostgres() throws SQLException {
        Statement stmt = pgConn.createStatement();
        
        // Movies table
        stmt.execute("DROP TABLE IF EXISTS movies CASCADE");
        stmt.execute("DROP TABLE IF EXISTS actors CASCADE");
        
        stmt.execute("""
            CREATE TABLE movies (
                id SERIAL PRIMARY KEY,
                uuid VARCHAR(36) UNIQUE NOT NULL,
                title VARCHAR(255) NOT NULL,
                year INTEGER NOT NULL,
                genre VARCHAR(50),
                rating DECIMAL(5,2),
                actor_ids TEXT
            )
        """);
        
        stmt.execute("CREATE INDEX idx_movies_year ON movies(year)");
        stmt.execute("CREATE INDEX idx_movies_genre ON movies(genre)");
        stmt.execute("CREATE INDEX idx_movies_rating ON movies(rating)");
        stmt.execute("CREATE INDEX idx_movies_uuid ON movies(uuid)");
        
        // Actors table
        stmt.execute("""
            CREATE TABLE actors (
                id SERIAL PRIMARY KEY,
                uuid VARCHAR(36) UNIQUE NOT NULL,
                name VARCHAR(255) NOT NULL,
                birth_year INTEGER NOT NULL,
                country VARCHAR(50)
            )
        """);
        
        stmt.execute("CREATE INDEX idx_actors_name ON actors(name)");
        stmt.execute("CREATE INDEX idx_actors_birth_year ON actors(birth_year)");
        stmt.execute("CREATE INDEX idx_actors_uuid ON actors(uuid)");
        
        pgConn.commit();
        stmt.close();
    }
    
    // ==================== Perst Setup ====================
    
    static void setupPerst() throws Exception {
        perstStorage = StorageFactory.getInstance().createStorage();
        
        java.io.File dbFile = new java.io.File("benchmark2.dbs");
        if (dbFile.exists()) dbFile.delete();
        
        perstStorage.open("benchmark2.dbs", 64 * 1024 * 1024);
        
        // Create indices - use String as key type
        intIndex = perstStorage.createIndex(String.class, true);
        actorIndex = perstStorage.createIndex(String.class, true);
    }
    
    // ==================== Test 1: Bulk Insert ====================
    
    static String getGenre(int i) {
        String[] genres = {"Action", "Comedy", "Drama", "Horror", "Sci-Fi", "Romance", "Thriller"};
        return genres[i % genres.length];
    }
    
    static void testBulkInsert() throws Exception {
        // PostgreSQL - batch insert with transaction
        long start = System.currentTimeMillis();
        
        PreparedStatement ps = pgConn.prepareStatement(
            "INSERT INTO movies (uuid, title, year, genre, rating) VALUES (?, ?, ?, ?, ?)"
        );
        
        for (int i = 0; i < N_MOVIES; i++) {
            ps.setString(1, UUID.randomUUID().toString());
            ps.setString(2, "Movie-" + i);
            ps.setInt(3, 2000 + (i % 24));
            ps.setString(4, getGenre(i));
            ps.setDouble(5, 5.0 + (i % 50) / 10.0);
            ps.addBatch();
            
            if (i % 1000 == 0) {
                ps.executeBatch();
            }
        }
        ps.executeBatch();
        pgConn.commit();
        long pgTime = System.currentTimeMillis() - start;
        
        // Perst
        start = System.currentTimeMillis();
        for (int i = 0; i < N_MOVIES; i++) {
            Movie movie = new Movie(
                "Movie-" + i,
                2000 + (i % 24),
                getGenre(i),
                5.0 + (i % 50) / 10.0
            );
            intIndex.put(new Key(movie.uuid), movie);
        }
        perstStorage.commit();
        long perstTime = System.currentTimeMillis() - start;
        
        printResult("Bulk Insert (" + N_MOVIES + " movies)", perstTime, pgTime);
    }
    
    // ==================== Test 2: Bulk Insert Actors ====================
    
    static void testBulkInsertActors() throws Exception {
        // PostgreSQL
        long start = System.currentTimeMillis();
        
        PreparedStatement ps = pgConn.prepareStatement(
            "INSERT INTO actors (uuid, name, birth_year, country) VALUES (?, ?, ?, ?)"
        );
        
        String[] countries = {"USA", "UK", "Canada", "France", "Germany", "Japan", "Australia"};
        
        for (int i = 0; i < N_ACTORS; i++) {
            ps.setString(1, UUID.randomUUID().toString());
            ps.setString(2, "Actor-" + i);
            ps.setInt(3, 1950 + (i % 70));
            ps.setString(4, countries[i % countries.length]);
            ps.addBatch();
            
            if (i % 1000 == 0) {
                ps.executeBatch();
            }
        }
        ps.executeBatch();
        pgConn.commit();
        long pgTime = System.currentTimeMillis() - start;
        
        // Perst
        start = System.currentTimeMillis();
        for (int i = 0; i < N_ACTORS; i++) {
            Actor actor = new Actor(
                "Actor-" + i,
                1950 + (i % 70),
                countries[i % countries.length]
            );
            actorIndex.put(new Key(actor.uuid), actor);
        }
        perstStorage.commit();
        long perstTime = System.currentTimeMillis() - start;
        
        printResult("Bulk Insert (" + N_ACTORS + " actors)", perstTime, pgTime);
    }
    
    // ==================== Test 3: Range Query ====================
    
    static void testRangeQuery() throws Exception {
        // PostgreSQL
        long start = System.currentTimeMillis();
        PreparedStatement ps = pgConn.prepareStatement(
            "SELECT * FROM movies WHERE year BETWEEN ? AND ? AND rating >= ?"
        );
        ps.setInt(1, 2010);
        ps.setInt(2, 2020);
        ps.setDouble(3, 7.0);
        ResultSet rs = ps.executeQuery();
        int count = 0;
        while (rs.next()) count++;
        rs.close();
        long pgTime = System.currentTimeMillis() - start;
        
        // Perst - iterate and filter
        start = System.currentTimeMillis();
        Iterator iter = intIndex.iterator();
        int perstCount = 0;
        while (iter.hasNext()) {
            Movie m = (Movie) iter.next();
            if (m.year >= 2010 && m.year <= 2020 && m.rating >= 7.0) {
                perstCount++;
            }
        }
        long perstTime = System.currentTimeMillis() - start;
        
        printResult("Range Query (year 2010-2020, rating>=7)", perstTime, pgTime);
    }
    
    // ==================== Test 4: Text Search ====================
    
    static void testTextSearch() throws Exception {
        // PostgreSQL - LIKE search
        long start = System.currentTimeMillis();
        PreparedStatement ps = pgConn.prepareStatement(
            "SELECT * FROM movies WHERE title LIKE ? LIMIT 100"
        );
        ps.setString(1, "Movie-1%");
        ResultSet rs = ps.executeQuery();
        int count = 0;
        while (rs.next()) count++;
        rs.close();
        long pgTime = System.currentTimeMillis() - start;
        
        // Perst - linear search with string match
        start = System.currentTimeMillis();
        Iterator iter = intIndex.iterator();
        int perstCount = 0;
        while (iter.hasNext() && perstCount < 100) {
            Movie m = (Movie) iter.next();
            if (m.title.startsWith("Movie-1")) {
                perstCount++;
            }
        }
        long perstTime = System.currentTimeMillis() - start;
        
        printResult("Text Search (title LIKE 'Movie-1%')", perstTime, pgTime);
    }
    
    // ==================== Test 5: Aggregation ====================
    
    static void testAggregation() throws Exception {
        // PostgreSQL - GROUP BY with aggregation
        long start = System.currentTimeMillis();
        String sql = """
            SELECT genre, COUNT(*) as cnt, AVG(rating) as avg_rating 
            FROM movies 
            GROUP BY genre 
            ORDER BY cnt DESC
        """;
        ResultSet rs = pgConn.createStatement().executeQuery(sql);
        Map<String, int[]> pgResults = new HashMap<>();
        while (rs.next()) {
            pgResults.put(rs.getString("genre"), new int[]{rs.getInt("cnt")});
        }
        rs.close();
        long pgTime = System.currentTimeMillis() - start;
        
        // Perst - manual aggregation
        start = System.currentTimeMillis();
        Map<String, int[]> perstResults = new HashMap<>();
        Iterator iter = intIndex.iterator();
        while (iter.hasNext()) {
            Movie m = (Movie) iter.next();
            perstResults.computeIfAbsent(m.genre, k -> new int[1])[0]++;
        }
        long perstTime = System.currentTimeMillis() - start;
        
        printResult("Aggregation (GROUP BY genre)", perstTime, pgTime);
    }
    
    // ==================== Test 6: Join-like Query ====================
    
    static void testJoinQuery() throws Exception {
        // Get sample actor UUIDs
        List<String> actorUuids = new ArrayList<>();
        ResultSet rs = pgConn.createStatement().executeQuery("SELECT uuid FROM actors LIMIT 100");
        while (rs.next()) actorUuids.add(rs.getString("uuid"));
        rs.close();
        
        // PostgreSQL - subquery
        long start = System.currentTimeMillis();
        PreparedStatement ps = pgConn.prepareStatement(
            "SELECT * FROM movies WHERE year >= ? ORDER BY rating DESC LIMIT 50"
        );
        ps.setInt(1, 2015);
        rs = ps.executeQuery();
        int pgCount = 0;
        while (rs.next()) pgCount++;
        rs.close();
        long pgTime = System.currentTimeMillis() - start;
        
        // Perst - iterate and filter
        start = System.currentTimeMillis();
        Iterator iter = intIndex.iterator();
        int perstCount = 0;
        while (iter.hasNext() && perstCount < 50) {
            Movie m = (Movie) iter.next();
            if (m.year >= 2015) {
                perstCount++;
            }
        }
        long perstTime = System.currentTimeMillis() - start;
        
        printResult("Complex Query (top movies since 2015)", perstTime, pgTime);
    }
    
    // ==================== Test 7: Update ====================
    
    static void testUpdate() throws Exception {
        // PostgreSQL - update with condition
        long start = System.currentTimeMillis();
        PreparedStatement ps = pgConn.prepareStatement(
            "UPDATE movies SET rating = rating * 1.1 WHERE year < 2010"
        );
        int updated = ps.executeUpdate();
        pgConn.commit();
        long pgTime = System.currentTimeMillis() - start;
        
        // Perst - iterate and update
        start = System.currentTimeMillis();
        Iterator iter = intIndex.iterator();
        while (iter.hasNext()) {
            Movie m = (Movie) iter.next();
            if (m.year < 2010) {
                m.rating = m.rating * 1.1;
                intIndex.set(new Key(m.uuid), m);
            }
        }
        perstStorage.commit();
        long perstTime = System.currentTimeMillis() - start;
        
        printResult("Update (rating * 1.1 where year<2010)", perstTime, pgTime);
    }
    
    // ==================== Test 8: Delete ====================
    
    static void testDelete() throws Exception {
        // PostgreSQL - delete with condition
        long start = System.currentTimeMillis();
        PreparedStatement ps = pgConn.prepareStatement(
            "DELETE FROM movies WHERE year < 2005"
        );
        int deleted = ps.executeUpdate();
        pgConn.commit();
        long pgTime = System.currentTimeMillis() - start;
        
        // Perst - iterate and delete
        start = System.currentTimeMillis();
        List<Key> toDelete = new ArrayList<>();
        Iterator iter = intIndex.iterator();
        while (iter.hasNext()) {
            Movie m = (Movie) iter.next();
            if (m.year < 2005) {
                toDelete.add(new Key(m.uuid));
            }
        }
        for (Key k : toDelete) {
            Movie m = (Movie) intIndex.get(k);
            if (m != null) {
                intIndex.remove(k);
                m.deallocate();
            }
        }
        perstStorage.commit();
        long perstTime = System.currentTimeMillis() - start;
        
        printResult("Delete (year < 2005)", perstTime, pgTime);
    }

    // ==================== Main ====================
    
    public static void main(String[] args) throws Exception {
        System.out.println("=== Perst vs PostgreSQL - Complex Benchmark ===");
        System.out.println("Movies: " + N_MOVIES + ", Actors: " + N_ACTORS);
        System.out.println();
        
        // Connect to PostgreSQL
        Class.forName("org.postgresql.Driver");
        pgConn = DriverManager.getConnection(jdbcUrl, dbUser, dbPassword);
        pgConn.setAutoCommit(false);
        
        // Setup databases
        System.out.println("Setting up databases...");
        setupPostgres();
        setupPerst();
        
        // Run tests
        System.out.println("\n--- Test 1: Bulk Insert Movies ---");
        testBulkInsert();
        
        System.out.println("\n--- Test 2: Bulk Insert Actors ---");
        testBulkInsertActors();
        
        System.out.println("\n--- Test 3: Range Query ---");
        testRangeQuery();
        
        System.out.println("\n--- Test 4: Text Search ---");
        testTextSearch();
        
        System.out.println("\n--- Test 5: Aggregation ---");
        testAggregation();
        
        System.out.println("\n--- Test 6: Complex Query ---");
        testJoinQuery();
        
        System.out.println("\n--- Test 7: Update ---");
        testUpdate();
        
        System.out.println("\n--- Test 8: Delete ---");
        testDelete();
        
        // Cleanup
        perstStorage.close();
        pgConn.close();
        
        System.out.println("\n=== Benchmark Complete ===");
    }
}
