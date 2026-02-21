package nl.dcg.gfe;

import org.garret.perst.*;
import java.sql.*;
import java.util.*;

/**
 * ComparisonBenchmark5 - OODB Query Benchmark (Corrected)
 * 
 * Tests proper OODB patterns:
 * - Perst: Direct object references (no manual lookups)
 * - PostgreSQL: JOINs
 */
public class ComparisonBenchmark5 {

    // ==================== Perst Models with Object References ====================
    
    static class Movie extends Persistent {
        String title;
        int year;
        String genre;
        double rating;
        Actor leadActor;  // Direct object reference!
        
        public Movie() {}
        
        public Movie(String title, int year, String genre, double rating) {
            this.title = title;
            this.year = year;
            this.genre = genre;
            this.rating = rating;
        }
    }
    
    static class Actor extends Persistent {
        String name;
        int birthYear;
        
        public Actor() {}
        
        public Actor(String name, int birthYear) {
            this.name = name;
            this.birthYear = birthYear;
        }
    }

    // ==================== Configuration ====================
    
    static final int N_MOVIES = 5000;
    static final int N_ACTORS = 2000;
    
    static String jdbcUrl = "jdbc:postgresql://localhost:5432/perst_test";
    static String dbUser = "postgres";
    static String dbPassword = "gfe";
    
    static Connection pgConn;
    static Storage perstStorage;
    static Index movieIndex;
    static Index actorIndex;
    
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
        stmt.execute("DROP TABLE IF EXISTS movie_actors CASCADE");
        stmt.execute("DROP TABLE IF EXISTS movies CASCADE");
        stmt.execute("DROP TABLE IF EXISTS actors CASCADE");
        
        stmt.execute("""
            CREATE TABLE actors (
                id SERIAL PRIMARY KEY, name VARCHAR(255), birth_year INTEGER
            )
        """);
        
        stmt.execute("""
            CREATE TABLE movies (
                id SERIAL PRIMARY KEY,
                title VARCHAR(255) UNIQUE, year INTEGER, genre VARCHAR(50), 
                rating DECIMAL(3,2), lead_actor_id INTEGER REFERENCES actors(id)
            )
        """);
        
        stmt.execute("CREATE INDEX idx_movies_rating ON movies(rating)");
        pgConn.commit();
        stmt.close();
    }
    
    static void setupPerst() throws Exception {
        perstStorage = StorageFactory.getInstance().createStorage();
        java.io.File dbFile = new java.io.File("benchmark5.dbs");
        if (dbFile.exists()) dbFile.delete();
        perstStorage.open("benchmark5.dbs", 128 * 1024 * 1024);
        
        // Create indices by title for lookup
        movieIndex = perstStorage.createIndex(String.class, true);
        actorIndex = perstStorage.createIndex(String.class, true);
    }

    // ==================== Test 1: Get Lead Actor for Movies ====================
    
    static void testGetLeadActor() throws Exception {
        // PostgreSQL - needs JOIN
        long start = System.currentTimeMillis();
        
        String sql = """
            SELECT a.name, a.birth_year 
            FROM movies m
            JOIN actors a ON m.lead_actor_id = a.id
            WHERE m.id <= 1000
        """;
        
        ResultSet rs = pgConn.createStatement().executeQuery(sql);
        int count = 0;
        while (rs.next()) count++;
        rs.close();
        long pgTime = System.currentTimeMillis() - start;
        
        // Perst - direct object navigation
        start = System.currentTimeMillis();
        
        int perstCount = 0;
        Iterator iter = movieIndex.iterator();
        int movieIdx = 0;
        
        while (iter.hasNext() && movieIdx < 1000) {
            Movie m = (Movie) iter.next();
            // Direct navigation to lead actor - NO lookup needed!
            if (m.leadActor != null) {
                String name = m.leadActor.name;  // Direct access!
                int by = m.leadActor.birthYear;
                perstCount++;
            }
            movieIdx++;
        }
        
        long perstTime = System.currentTimeMillis() - start;
        
        printResult("Get Lead Actor for Movies (1000)", perstTime, pgTime);
    }
    
    // ==================== Test 2: Filtered Actor Access ====================
    
    static void testFilteredActorAccess() throws Exception {
        // PostgreSQL - JOIN with WHERE on movie
        long start = System.currentTimeMillis();
        
        String sql = """
            SELECT a.name
            FROM movies m
            JOIN actors a ON m.lead_actor_id = a.id
            WHERE m.rating >= 7.0
        """;
        
        ResultSet rs = pgConn.createStatement().executeQuery(sql);
        int count = 0;
        while (rs.next()) count++;
        rs.close();
        long pgTime = System.currentTimeMillis() - start;
        
        // Perst - navigate and filter
        start = System.currentTimeMillis();
        
        int perstCount = 0;
        Iterator iter = movieIndex.iterator();
        
        while (iter.hasNext()) {
            Movie m = (Movie) iter.next();
            if (m.rating >= 7.0 && m.leadActor != null) {
                String name = m.leadActor.name;  // Direct access!
                perstCount++;
            }
        }
        
        long perstTime = System.currentTimeMillis() - start;
        
        printResult("Filtered Actor Access (rating >= 7.0)", perstTime, pgTime);
    }
    
    // ==================== Test 3: Count Movies per Actor ====================
    
    static void testCountMoviesPerActor() throws Exception {
        // PostgreSQL - GROUP BY
        long start = System.currentTimeMillis();
        
        String sql = """
            SELECT a.name, COUNT(*) as cnt
            FROM movies m
            JOIN actors a ON m.lead_actor_id = a.id
            GROUP BY a.name
        """;
        
        ResultSet rs = pgConn.createStatement().executeQuery(sql);
        int count = 0;
        while (rs.next()) count++;
        rs.close();
        long pgTime = System.currentTimeMillis() - start;
        
        // Perst - iterate and group
        start = System.currentTimeMillis();
        
        Map<String, Integer> actorMovieCount = new HashMap<>();
        Iterator iter = movieIndex.iterator();
        
        while (iter.hasNext()) {
            Movie m = (Movie) iter.next();
            if (m.leadActor != null) {
                String name = m.leadActor.name;
                actorMovieCount.put(name, actorMovieCount.getOrDefault(name, 0) + 1);
            }
        }
        
        long perstTime = System.currentTimeMillis() - start;
        
        printResult("Count Movies per Actor", perstTime, pgTime);
    }

    // ==================== Main ====================
    
    public static void main(String[] args) throws Exception {
        System.out.println("=== Perst vs PostgreSQL - OODB Query Benchmark ===");
        System.out.println("Movies: " + N_MOVIES + ", Actors: " + N_ACTORS);
        System.out.println("Perst uses direct object references, PostgreSQL uses JOINs");
        System.out.println();
        
        Class.forName("org.postgresql.Driver");
        pgConn = DriverManager.getConnection(jdbcUrl, dbUser, dbPassword);
        pgConn.setAutoCommit(false);
        
        System.out.println("Setting up databases...");
        setupPostgres();
        setupPerst();
        
        Random rand = new Random(42);
        
        // Pre-populate PostgreSQL actors first
        System.out.println("Populating PostgreSQL...");
        PreparedStatement ps = pgConn.prepareStatement(
            "INSERT INTO actors (name, birth_year) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS
        );
        Map<Integer, Integer> actorPgIdMap = new HashMap<>();  // Perst actor index -> PG ID
        for (int i = 0; i < N_ACTORS; i++) {
            ps.setString(1, "Actor-" + i);
            ps.setInt(2, 1950 + (i % 40));
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                actorPgIdMap.put(i, rs.getInt(1));
            }
        }
        pgConn.commit();
        ps.close();
        
        // Pre-populate Perst actors and store in index
        System.out.println("Populating Perst actors...");
        Map<Integer, Actor> actorMap = new HashMap<>();
        for (int i = 0; i < N_ACTORS; i++) {
            Actor a = new Actor("Actor-" + i, 1950 + (i % 40));
            perstStorage.makePersistent(a);
            actorIndex.put(new Key(a.name), a);
            actorMap.put(i, a);
        }
        
        // Movies
        ps = pgConn.prepareStatement(
            "INSERT INTO movies (title, year, genre, rating, lead_actor_id) VALUES (?, ?, ?, ?, ?)"
        );
        for (int i = 0; i < N_MOVIES; i++) {
            int actorIdx = rand.nextInt(N_ACTORS);
            Integer pgActorId = actorPgIdMap.get(actorIdx);
            
            ps.setString(1, "Movie-" + i);
            ps.setInt(2, 2000 + (i % 20));
            ps.setString(3, "Action");
            ps.setDouble(4, 5.0 + (i % 50) / 10.0);
            ps.setInt(5, pgActorId);
            ps.addBatch();
            if (i % 1000 == 0) ps.executeBatch();
        }
        ps.executeBatch();
        pgConn.commit();
        ps.close();
        
        // Perst movies with object references
        System.out.println("Populating Perst movies with object references...");
        for (int i = 0; i < N_MOVIES; i++) {
            Movie m = new Movie("Movie-" + i, 2000 + (i % 20), "Action", 
                              5.0 + (i % 50) / 10.0);
            
            // Direct object reference - NOT ID lookup!
            int actorIdx = rand.nextInt(N_ACTORS);
            m.leadActor = actorMap.get(actorIdx);  // Direct reference!
            
            movieIndex.put(new Key(m.title), m);
        }
        perstStorage.commit();
        
        System.out.println("Data populated.");
        
        // Run tests
        System.out.println("\n--- Test 1: Get Lead Actor for Movies ---");
        testGetLeadActor();
        
        System.out.println("\n--- Test 2: Filtered Actor Access ---");
        testFilteredActorAccess();
        
        System.out.println("\n--- Test 3: Count Movies per Actor ---");
        testCountMoviesPerActor();
        
        perstStorage.close();
        pgConn.close();
        
        System.out.println("\n=== Benchmark Complete ===");
    }
}
