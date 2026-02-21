package nl.dcg.gfe;

import org.garret.perst.*;

import java.sql.*;
import java.util.*;

/**
 * ComparisonBenchmark3 - Benchmark with proper Perst FieldIndex
 * 
 * Tests the same complex objects but with proper field-level indices
 * to match PostgreSQL's indexing strategy.
 */
public class ComparisonBenchmark3 {

    // ==================== Complex Object Models ====================
    
    static class Movie extends Persistent {
        String uuid;
        String title;
        int year;
        String genre;
        double rating;
        
        public Movie() {}
        
        public Movie(String title, int year, String genre, double rating) {
            this.uuid = UUID.randomUUID().toString();
            this.title = title;
            this.year = year;
            this.genre = genre;
            this.rating = rating;
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
    
    static String jdbcUrl = "jdbc:postgresql://localhost:5432/perst_test";
    static String dbUser = "postgres";
    static String dbPassword = "gfe";
    
    static Connection pgConn;
    static Storage perstStorage;
    
    // Field indices for Perst
    static FieldIndex movieYearIndex;
    static FieldIndex movieGenreIndex;
    static FieldIndex movieRatingIndex;
    static FieldIndex actorNameIndex;
    static FieldIndex actorBirthYearIndex;
    
    static void printResult(String test, long perstMs, long pgMs) {
        if (pgMs > 0 && perstMs > 0) {
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
        
        stmt.execute("DROP TABLE IF EXISTS movies CASCADE");
        stmt.execute("DROP TABLE IF EXISTS actors CASCADE");
        
        stmt.execute("""
            CREATE TABLE movies (
                id SERIAL PRIMARY KEY,
                uuid VARCHAR(36) UNIQUE NOT NULL,
                title VARCHAR(255) NOT NULL,
                year INTEGER NOT NULL,
                genre VARCHAR(50),
                rating DECIMAL(5,2)
            )
        """);
        
        // Field indices
        stmt.execute("CREATE INDEX idx_movies_year ON movies(year)");
        stmt.execute("CREATE INDEX idx_movies_genre ON movies(genre)");
        stmt.execute("CREATE INDEX idx_movies_rating ON movies(rating)");
        stmt.execute("CREATE INDEX idx_movies_uuid ON movies(uuid)");
        stmt.execute("CREATE INDEX idx_movies_title ON movies(title)");
        
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
    
    // ==================== Perst Setup with Field Indices ====================
    
    static void setupPerst() throws Exception {
        perstStorage = StorageFactory.getInstance().createStorage();
        
        java.io.File dbFile = new java.io.File("benchmark3.dbs");
        if (dbFile.exists()) dbFile.delete();
        
        perstStorage.open("benchmark3.dbs", 64 * 1024 * 1024);
        
        // Create FieldIndices - equivalent to PostgreSQL indices
        movieYearIndex = perstStorage.createFieldIndex(Movie.class, "year", true);
        movieGenreIndex = perstStorage.createFieldIndex(Movie.class, "genre", false);
        movieRatingIndex = perstStorage.createFieldIndex(Movie.class, "rating", false);
        
        actorNameIndex = perstStorage.createFieldIndex(Actor.class, "name", false);
        actorBirthYearIndex = perstStorage.createFieldIndex(Actor.class, "birthYear", false);
    }
    
    static String getGenre(int i) {
        String[] genres = {"Action", "Comedy", "Drama", "Horror", "Sci-Fi", "Romance", "Thriller"};
        return genres[i % genres.length];
    }
    
    // ==================== Test 1: Bulk Insert ====================
    
    static void testBulkInsert() throws Exception {
        // PostgreSQL
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
            movieYearIndex.put(movie);
            movieGenreIndex.put(movie);
            movieRatingIndex.put(movie);
        }
        perstStorage.commit();
        long perstTime = System.currentTimeMillis() - start;
        
        printResult("Bulk Insert (" + N_MOVIES + " movies)", perstTime, pgTime);
    }
    
    // ==================== Test 2: Range Query on Year ====================
    
    static void testRangeQueryYear() throws Exception {
        // PostgreSQL - uses idx_movies_year
        long start = System.currentTimeMillis();
        PreparedStatement ps = pgConn.prepareStatement(
            "SELECT * FROM movies WHERE year BETWEEN ? AND ?"
        );
        ps.setInt(1, 2010);
        ps.setInt(2, 2020);
        ResultSet rs = ps.executeQuery();
        int pgCount = 0;
        while (rs.next()) pgCount++;
        rs.close();
        long pgTime = System.currentTimeMillis() - start;
        
        // Perst - uses FieldIndex on year
        start = System.currentTimeMillis();
        Iterator<Movie> iter = movieYearIndex.iterator(new Key(2010), new Key(2020), Index.ASCENT_ORDER);
        int perstCount = 0;
        while (iter.hasNext()) {
            iter.next();
            perstCount++;
        }
        long perstTime = System.currentTimeMillis() - start;
        
        printResult("Range Query (year 2010-2020)", perstTime, pgTime);
    }
    
    // ==================== Test 3: Range Query on Rating ====================
    
    static void testRangeQueryRating() throws Exception {
        // PostgreSQL - uses idx_movies_rating
        long start = System.currentTimeMillis();
        PreparedStatement ps = pgConn.prepareStatement(
            "SELECT * FROM movies WHERE rating >= ?"
        );
        ps.setDouble(1, 7.5);
        ResultSet rs = ps.executeQuery();
        int pgCount = 0;
        while (rs.next()) pgCount++;
        rs.close();
        long pgTime = System.currentTimeMillis() - start;
        
        // Perst - uses FieldIndex on rating
        start = System.currentTimeMillis();
        Iterator<Movie> iter = movieRatingIndex.iterator(new Key(7.5), null, Index.ASCENT_ORDER);
        int perstCount = 0;
        while (iter.hasNext()) {
            iter.next();
            perstCount++;
        }
        long perstTime = System.currentTimeMillis() - start;
        
        printResult("Range Query (rating >= 7.5)", perstTime, pgTime);
    }
    
    // ==================== Test 4: Combined Range Query ====================
    
    static void testCombinedRange() throws Exception {
        // PostgreSQL - uses index on year, then filters rating
        long start = System.currentTimeMillis();
        PreparedStatement ps = pgConn.prepareStatement(
            "SELECT * FROM movies WHERE year BETWEEN ? AND ? AND rating >= ?"
        );
        ps.setInt(1, 2010);
        ps.setInt(2, 2020);
        ps.setDouble(3, 7.0);
        ResultSet rs = ps.executeQuery();
        int pgCount = 0;
        while (rs.next()) pgCount++;
        rs.close();
        long pgTime = System.currentTimeMillis() - start;
        
        // Perst - iterate using year index, filter by rating
        start = System.currentTimeMillis();
        Iterator<Movie> iter = movieYearIndex.iterator(new Key(2010), new Key(2020), Index.ASCENT_ORDER);
        int perstCount = 0;
        while (iter.hasNext()) {
            Movie m = iter.next();
            if (m.rating >= 7.0) {
                perstCount++;
            }
        }
        long perstTime = System.currentTimeMillis() - start;
        
        printResult("Combined Range (year+rating)", perstTime, pgTime);
    }
    
    // ==================== Test 5: Exact Match Query ====================
    
    static void testExactMatch() throws Exception {
        // PostgreSQL - uses index on genre
        long start = System.currentTimeMillis();
        PreparedStatement ps = pgConn.prepareStatement(
            "SELECT * FROM movies WHERE genre = ?"
        );
        ps.setString(1, "Action");
        ResultSet rs = ps.executeQuery();
        int pgCount = 0;
        while (rs.next()) pgCount++;
        rs.close();
        long pgTime = System.currentTimeMillis() - start;
        
        // Perst - uses FieldIndex on genre
        start = System.currentTimeMillis();
        Iterator<Movie> iter = movieGenreIndex.iterator(new Key("Action"), new Key("Action"), Index.ASCENT_ORDER);
        int perstCount = 0;
        while (iter.hasNext()) {
            iter.next();
            perstCount++;
        }
        long perstTime = System.currentTimeMillis() - start;
        
        printResult("Exact Match (genre='Action')", perstTime, pgTime);
    }
    
    // ==================== Test 6: Aggregation with Index ====================
    
    static void testAggregationByGenre() throws Exception {
        // PostgreSQL - uses GROUP BY with index
        long start = System.currentTimeMillis();
        String sql = "SELECT genre, COUNT(*) as cnt FROM movies GROUP BY genre";
        ResultSet rs = pgConn.createStatement().executeQuery(sql);
        Map<String, Integer> pgResults = new HashMap<>();
        while (rs.next()) {
            pgResults.put(rs.getString("genre"), rs.getInt("cnt"));
        }
        rs.close();
        long pgTime = System.currentTimeMillis() - start;
        
        // Perst - use genre index
        start = System.currentTimeMillis();
        Map<String, Integer> perstResults = new HashMap<>();
        String[] genres = {"Action", "Comedy", "Drama", "Horror", "Sci-Fi", "Romance", "Thriller"};
        for (String genre : genres) {
            Iterator<Movie> iter = movieGenreIndex.iterator(new Key(genre), new Key(genre), Index.ASCENT_ORDER);
            int count = 0;
            while (iter.hasNext()) {
                iter.next();
                count++;
            }
            perstResults.put(genre, count);
        }
        long perstTime = System.currentTimeMillis() - start;
        
        printResult("Aggregation by Genre (using index)", perstTime, pgTime);
    }
    
    // ==================== Test 7: Actor Name Search ====================
    
    static void testActorNameSearch() throws Exception {
        // Insert actors
        PreparedStatement ps = pgConn.prepareStatement(
            "INSERT INTO actors (uuid, name, birth_year, country) VALUES (?, ?, ?, ?)"
        );
        String[] countries = {"USA", "UK", "Canada", "France", "Germany"};
        for (int i = 0; i < N_ACTORS; i++) {
            ps.setString(1, UUID.randomUUID().toString());
            ps.setString(2, "Actor-" + i);
            ps.setInt(3, 1950 + (i % 70));
            ps.setString(4, countries[i % countries.length]);
            ps.addBatch();
            if (i % 1000 == 0) ps.executeBatch();
        }
        ps.executeBatch();
        pgConn.commit();
        
        // Insert into Perst
        for (int i = 0; i < N_ACTORS; i++) {
            Actor actor = new Actor("Actor-" + i, 1950 + (i % 70), countries[i % countries.length]);
            actorNameIndex.put(actor);
            actorBirthYearIndex.put(actor);
        }
        perstStorage.commit();
        
        // PostgreSQL - index on name
        long start = System.currentTimeMillis();
        PreparedStatement pss = pgConn.prepareStatement(
            "SELECT * FROM actors WHERE name LIKE ?"
        );
        pss.setString(1, "Actor-1%");
        ResultSet rs = pss.executeQuery();
        int pgCount = 0;
        while (rs.next()) pgCount++;
        rs.close();
        long pgTime = System.currentTimeMillis() - start;
        
        // Perst - linear scan (no string prefix index)
        start = System.currentTimeMillis();
        Iterator iter = actorNameIndex.iterator();
        int perstCount = 0;
        while (iter.hasNext()) {
            Actor a = (Actor) iter.next();
            if (a.name.startsWith("Actor-1")) {
                perstCount++;
            }
        }
        long perstTime = System.currentTimeMillis() - start;
        
        printResult("Actor Name Search (prefix)", perstTime, pgTime);
    }

    // ==================== Main ====================
    
    public static void main(String[] args) throws Exception {
        System.out.println("=== Perst vs PostgreSQL - Field Index Benchmark ===");
        System.out.println("Movies: " + N_MOVIES + ", Actors: " + N_ACTORS);
        System.out.println("Both use field-level indices");
        System.out.println();
        
        // Connect to PostgreSQL
        Class.forName("org.postgresql.Driver");
        pgConn = DriverManager.getConnection(jdbcUrl, dbUser, dbPassword);
        pgConn.setAutoCommit(false);
        
        // Setup databases
        System.out.println("Setting up databases with indices...");
        setupPostgres();
        setupPerst();
        
        // Run tests
        System.out.println("\n--- Test 1: Bulk Insert ---");
        testBulkInsert();
        
        System.out.println("\n--- Test 2: Range Query (year) ---");
        testRangeQueryYear();
        
        System.out.println("\n--- Test 3: Range Query (rating) ---");
        testRangeQueryRating();
        
        System.out.println("\n--- Test 4: Combined Range Query ---");
        testCombinedRange();
        
        System.out.println("\n--- Test 5: Exact Match (genre) ---");
        testExactMatch();
        
        System.out.println("\n--- Test 6: Aggregation by Genre ---");
        testAggregationByGenre();
        
        System.out.println("\n--- Test 7: Actor Name Search ---");
        testActorNameSearch();
        
        // Cleanup
        perstStorage.close();
        pgConn.close();
        
        System.out.println("\n=== Benchmark Complete ===");
    }
}
