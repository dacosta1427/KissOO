package nl.dcg.gfe;

import org.garret.perst.*;

import java.sql.*;
import java.util.*;

/**
 * ComparisonBenchmark13 - True JOIN Query Benchmark
 * 
 * This benchmark tests actual JOIN operations between related tables:
 * Movies and Actors with a many-to-many relationship through movie_actors.
 * 
 * This addresses the limitation in Benchmark 2's "Complex Query" which
 * was not a true JOIN but just a filtered query.
 */
public class ComparisonBenchmark13 {

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
    
    // Movie-Actor relationship (many-to-many)
    static class MovieActor extends Persistent {
        String movieUuid;
        String actorUuid;
        String role;
        
        public MovieActor() {}
        
        public MovieActor(String movieUuid, String actorUuid, String role) {
            this.movieUuid = movieUuid;
            this.actorUuid = actorUuid;
            this.role = role;
        }
    }

    // ==================== Configuration ====================
    
    static final int N_MOVIES = 5000;
    static final int N_ACTORS = 2000;
    static final int N_RELATIONSHIPS = 15000;  // average 3 actors per movie
    
    static String jdbcUrl = "jdbc:postgresql://localhost:5432/perst_test";
    static String dbUser = "postgres";
    static String dbPassword = "gfe";
    
    static Connection pgConn;
    static Storage perstStorage;
    static Index movieIndex;
    static Index actorIndex;
    static Index movieActorIndex;
    
    // Store relationships for Perst
    static Map<String, List<String>> movieToActors = new HashMap<>();
    static Map<String, List<String>> actorToMovies = new HashMap<>();
    
    static void printResult(String test, long perstMs, long pgMs) {
        if (pgMs > 0 && perstMs > 0) {
            double speedup = (double) pgMs / perstMs;
            System.out.printf("%-50s Perst: %6d ms  PostgreSQL: %6d ms  Speedup: %7.2fx%n",
                    test, perstMs, pgMs, speedup);
        } else if (pgMs > 0) {
            System.out.printf("%-50s Perst: %6d ms  PostgreSQL: %6d ms%n",
                    test, perstMs, pgMs);
        } else {
            System.out.printf("%-50s Perst: %6d ms  PostgreSQL: %6d ms%n",
                    test, perstMs, pgMs);
        }
    }

    // ==================== PostgreSQL Setup ====================
    
    static void setupPostgres() throws SQLException {
        Statement stmt = pgConn.createStatement();
        
        // Movies table
        stmt.execute("DROP TABLE IF EXISTS movie_actors CASCADE");
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
        
        // Movie-Actors junction table (many-to-many)
        stmt.execute("""
            CREATE TABLE movie_actors (
                id SERIAL PRIMARY KEY,
                movie_uuid VARCHAR(36) NOT NULL,
                actor_uuid VARCHAR(36) NOT NULL,
                role VARCHAR(100),
                FOREIGN KEY (movie_uuid) REFERENCES movies(uuid),
                FOREIGN KEY (actor_uuid) REFERENCES actors(uuid)
            )
        """);
        
        stmt.execute("CREATE INDEX idx_movie_actors_movie ON movie_actors(movie_uuid)");
        stmt.execute("CREATE INDEX idx_movie_actors_actor ON movie_actors(actor_uuid)");
        
        pgConn.commit();
        stmt.close();
    }
    
    // ==================== Perst Setup ====================
    
    static void setupPerst() throws Exception {
        perstStorage = StorageFactory.getInstance().createStorage();
        
        java.io.File dbFile = new java.io.File("benchmark13.dbs");
        if (dbFile.exists()) dbFile.delete();
        
        perstStorage.open("benchmark13.dbs", 64 * 1024 * 1024);
        
        // Create indices
        movieIndex = perstStorage.createIndex(String.class, true);
        actorIndex = perstStorage.createIndex(String.class, true);
        
        // Clear relationship maps
        movieToActors.clear();
        actorToMovies.clear();
    }
    
    // ==================== Data Population ====================
    
    static String getGenre(int i) {
        String[] genres = {"Action", "Comedy", "Drama", "Horror", "Sci-Fi", "Romance", "Thriller"};
        return genres[i % genres.length];
    }
    
    static void populateData() throws Exception {
        Random rand = new Random(42);  // fixed seed for reproducibility
        
        List<String> movieUuids = new ArrayList<>();
        List<String> actorUuids = new ArrayList<>();
        
        // Insert movies in PostgreSQL
        System.out.println("Populating PostgreSQL...");
        PreparedStatement psMovie = pgConn.prepareStatement(
            "INSERT INTO movies (uuid, title, year, genre, rating) VALUES (?, ?, ?, ?, ?)"
        );
        PreparedStatement psActor = pgConn.prepareStatement(
            "INSERT INTO actors (uuid, name, birth_year, country) VALUES (?, ?, ?, ?)"
        );
        PreparedStatement psRelation = pgConn.prepareStatement(
            "INSERT INTO movie_actors (movie_uuid, actor_uuid, role) VALUES (?, ?, ?)"
        );
        
        // Create movies
        for (int i = 0; i < N_MOVIES; i++) {
            String uuid = UUID.randomUUID().toString();
            movieUuids.add(uuid);
            psMovie.setString(1, uuid);
            psMovie.setString(2, "Movie-" + i);
            psMovie.setInt(3, 2000 + (i % 24));
            psMovie.setString(4, getGenre(i));
            psMovie.setDouble(5, 5.0 + (i % 50) / 10.0);
            psMovie.addBatch();
            
            if (i % 1000 == 0) psMovie.executeBatch();
        }
        psMovie.executeBatch();
        
        // Create actors
        String[] countries = {"USA", "UK", "Canada", "France", "Germany", "Japan", "Australia"};
        for (int i = 0; i < N_ACTORS; i++) {
            String uuid = UUID.randomUUID().toString();
            actorUuids.add(uuid);
            psActor.setString(1, uuid);
            psActor.setString(2, "Actor-" + i);
            psActor.setInt(3, 1950 + (i % 70));
            psActor.setString(4, countries[i % countries.length]);
            psActor.addBatch();
            
            if (i % 1000 == 0) psActor.executeBatch();
        }
        psActor.executeBatch();
        
        // Create relationships
        for (int i = 0; i < N_RELATIONSHIPS; i++) {
            String movieUuid = movieUuids.get(rand.nextInt(N_MOVIES));
            String actorUuid = actorUuids.get(rand.nextInt(N_ACTORS));
            psRelation.setString(1, movieUuid);
            psRelation.setString(2, actorUuid);
            psRelation.setString(3, "Role-" + (i % 10));
            psRelation.addBatch();
            
            if (i % 1000 == 0) psRelation.executeBatch();
        }
        psRelation.executeBatch();
        pgConn.commit();
        
        // Now populate Perst - store UUIDs separately for Perst
        System.out.println("Populating Perst...");
        
        List<String> perstMovieUuids = new ArrayList<>();
        List<String> perstActorUuids = new ArrayList<>();
        
        // Create movies in Perst
        for (int i = 0; i < N_MOVIES; i++) {
            Movie movie = new Movie(
                "Movie-" + i,
                2000 + (i % 24),
                getGenre(i),
                5.0 + (i % 50) / 10.0
            );
            movieIndex.put(new Key(movie.uuid), movie);
            movieToActors.put(movie.uuid, new ArrayList<>());
            perstMovieUuids.add(movie.uuid);
        }
        
        // Create actors in Perst
        for (int i = 0; i < N_ACTORS; i++) {
            Actor actor = new Actor(
                "Actor-" + i,
                1950 + (i % 70),
                countries[i % countries.length]
            );
            actorIndex.put(new Key(actor.uuid), actor);
            actorToMovies.put(actor.uuid, new ArrayList<>());
            perstActorUuids.add(actor.uuid);
        }
        
        // Create relationships in Perst (simulating junction table)
        for (int i = 0; i < N_RELATIONSHIPS; i++) {
            String movieUuid = perstMovieUuids.get(rand.nextInt(N_MOVIES));
            String actorUuid = perstActorUuids.get(rand.nextInt(N_ACTORS));
            
            // Add to movie's actor list
            movieToActors.get(movieUuid).add(actorUuid);
            // Add to actor's movie list
            actorToMovies.get(actorUuid).add(movieUuid);
        }
        
        perstStorage.commit();
        System.out.println("Data populated.");
    }
    
    // ==================== Test 1: Inner JOIN ====================
    
    static void testInnerJoin() throws Exception {
        // PostgreSQL - INNER JOIN
        long start = System.currentTimeMillis();
        String sql = """
            SELECT m.title, m.rating, a.name, a.country
            FROM movies m
            INNER JOIN movie_actors ma ON m.uuid = ma.movie_uuid
            INNER JOIN actors a ON ma.actor_uuid = a.uuid
            WHERE m.year >= 2015 AND a.birth_year > 1970
            LIMIT 500
        """;
        ResultSet rs = pgConn.createStatement().executeQuery(sql);
        int count = 0;
        while (rs.next()) count++;
        rs.close();
        long pgTime = System.currentTimeMillis() - start;
        
        // Perst - manual join
        start = System.currentTimeMillis();
        int perstCount = 0;
        
        // Iterate movies, filter by year
        Iterator movieIter = movieIndex.iterator();
        while (movieIter.hasNext() && perstCount < 500) {
            Movie m = (Movie) movieIter.next();
            if (m.year >= 2015) {
                // Get actors for this movie
                List<String> actorUuids = movieToActors.get(m.uuid);
                if (actorUuids != null) {
                    for (String actorUuid : actorUuids) {
                        Actor a = (Actor) actorIndex.get(new Key(actorUuid));
                        if (a != null && a.birthYear > 1970) {
                            perstCount++;
                        }
                    }
                }
            }
        }
        long perstTime = System.currentTimeMillis() - start;
        
        printResult("INNER JOIN (movies + actors with filters)", perstTime, pgTime);
    }
    
    // ==================== Test 2: Left JOIN ====================
    
    static void testLeftJoin() throws Exception {
        // PostgreSQL - LEFT JOIN (all movies with their actors)
        long start = System.currentTimeMillis();
        String sql = """
            SELECT m.title, m.genre, a.name
            FROM movies m
            LEFT JOIN movie_actors ma ON m.uuid = ma.movie_uuid
            LEFT JOIN actors a ON ma.actor_uuid = a.uuid
            WHERE m.genre = 'Action'
            LIMIT 200
        """;
        ResultSet rs = pgConn.createStatement().executeQuery(sql);
        int count = 0;
        while (rs.next()) count++;
        rs.close();
        long pgTime = System.currentTimeMillis() - start;
        
        // Perst - manual left join
        start = System.currentTimeMillis();
        int perstCount = 0;
        
        Iterator movieIter = movieIndex.iterator();
        while (movieIter.hasNext() && perstCount < 200) {
            Movie m = (Movie) movieIter.next();
            if ("Action".equals(m.genre)) {
                List<String> actorUuids = movieToActors.get(m.uuid);
                if (actorUuids != null) {
                    for (String actorUuid : actorUuids) {
                        perstCount++;
                    }
                }
                // Even if no actors, we still "include" the movie (left join behavior)
                if (actorUuids == null || actorUuids.isEmpty()) {
                    perstCount++;
                }
            }
        }
        long perstTime = System.currentTimeMillis() - start;
        
        printResult("LEFT JOIN (all Action movies with actors)", perstTime, pgTime);
    }
    
    // ==================== Test 3: Aggregation with JOIN ====================
    
    static void testAggregationWithJoin() throws Exception {
        // PostgreSQL - COUNT with JOIN and GROUP BY
        long start = System.currentTimeMillis();
        String sql = """
            SELECT a.name, a.country, COUNT(m.id) as movie_count
            FROM actors a
            LEFT JOIN movie_actors ma ON a.uuid = ma.actor_uuid
            LEFT JOIN movies m ON ma.movie_uuid = m.uuid
            GROUP BY a.uuid, a.name, a.country
            ORDER BY movie_count DESC
            LIMIT 100
        """;
        ResultSet rs = pgConn.createStatement().executeQuery(sql);
        Map<String, Integer> pgResults = new HashMap<>();
        while (rs.next()) {
            pgResults.put(rs.getString("name"), rs.getInt("movie_count"));
        }
        rs.close();
        long pgTime = System.currentTimeMillis() - start;
        
        // Perst - manual aggregation with join
        start = System.currentTimeMillis();
        Map<String, Integer> perstResults = new HashMap<>();
        
        Iterator actorIter = actorIndex.iterator();
        while (actorIter.hasNext()) {
            Actor a = (Actor) actorIter.next();
            List<String> movieUuids = actorToMovies.get(a.uuid);
            int count = (movieUuids != null) ? movieUuids.size() : 0;
            perstResults.put(a.name, count);
        }
        
        // Sort and limit
        perstResults.entrySet().stream()
            .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
            .limit(100)
            .count();
        long perstTime = System.currentTimeMillis() - start;
        
        printResult("Aggregation with JOIN (actors with movie count)", perstTime, pgTime);
    }
    
    // ==================== Test 4: Multi-table JOIN ====================
    
    static void testMultiTableJoin() throws Exception {
        // PostgreSQL - JOIN across all three tables
        long start = System.currentTimeMillis();
        String sql = """
            SELECT m.title, m.year, a.name, ma.role
            FROM movies m
            INNER JOIN movie_actors ma ON m.uuid = ma.movie_uuid
            INNER JOIN actors a ON ma.actor_uuid = a.uuid
            WHERE m.rating >= 8.0 AND a.country = 'USA'
            ORDER BY m.rating DESC
            LIMIT 100
        """;
        ResultSet rs = pgConn.createStatement().executeQuery(sql);
        int count = 0;
        while (rs.next()) count++;
        rs.close();
        long pgTime = System.currentTimeMillis() - start;
        
        // Perst - manual multi-table join
        start = System.currentTimeMillis();
        int perstCount = 0;
        
        Iterator movieIter = movieIndex.iterator();
        while (movieIter.hasNext() && perstCount < 100) {
            Movie m = (Movie) movieIter.next();
            if (m.rating >= 8.0) {
                List<String> actorUuids = movieToActors.get(m.uuid);
                if (actorUuids != null) {
                    for (String actorUuid : actorUuids) {
                        Actor a = (Actor) actorIndex.get(new Key(actorUuid));
                        if (a != null && "USA".equals(a.country)) {
                            perstCount++;
                        }
                    }
                }
            }
        }
        long perstTime = System.currentTimeMillis() - start;
        
        printResult("Multi-table JOIN (3 tables with filters)", perstTime, pgTime);
    }
    
    // ==================== Test 5: Nested Subquery ====================
    
    static void testNestedSubquery() throws Exception {
        // PostgreSQL - nested subquery
        long start = System.currentTimeMillis();
        String sql = """
            SELECT DISTINCT a.name, a.birth_year
            FROM actors a
            WHERE a.uuid IN (
                SELECT ma.actor_uuid FROM movie_actors ma
                WHERE ma.movie_uuid IN (
                    SELECT m.uuid FROM movies m WHERE m.rating >= 8.5
                )
            )
            ORDER BY a.birth_year DESC
            LIMIT 50
        """;
        ResultSet rs = pgConn.createStatement().executeQuery(sql);
        int count = 0;
        while (rs.next()) count++;
        rs.close();
        long pgTime = System.currentTimeMillis() - start;
        
        // Perst - nested iteration
        start = System.currentTimeMillis();
        int perstCount = 0;
        
        // Find high-rated movies
        Set<String> highRatedMovieUuids = new HashSet<>();
        Iterator movieIter = movieIndex.iterator();
        while (movieIter.hasNext()) {
            Movie m = (Movie) movieIter.next();
            if (m.rating >= 8.5) {
                highRatedMovieUuids.add(m.uuid);
            }
        }
        
        // Find actors in those movies
        Set<String> actorUuids = new HashSet<>();
        for (String movieUuid : highRatedMovieUuids) {
            List<String> actorList = movieToActors.get(movieUuid);
            if (actorList != null) {
                actorUuids.addAll(actorList);
            }
        }
        
        // Get actor details
        for (String actorUuid : actorUuids) {
            Actor a = (Actor) actorIndex.get(new Key(actorUuid));
            if (a != null && perstCount < 50) {
                perstCount++;
            }
        }
        long perstTime = System.currentTimeMillis() - start;
        
        printResult("Nested Subquery (actors in top-rated movies)", perstTime, pgTime);
    }
    
    // ==================== Test 6: Complex Aggregation ====================
    
    static void testComplexAggregation() throws Exception {
        // PostgreSQL - complex aggregation with JOIN
        long start = System.currentTimeMillis();
        String sql = """
            SELECT m.genre, 
                   COUNT(DISTINCT m.id) as movie_count,
                   COUNT(DISTINCT ma.actor_uuid) as actor_count,
                   AVG(m.rating) as avg_rating
            FROM movies m
            LEFT JOIN movie_actors ma ON m.uuid = ma.movie_uuid
            GROUP BY m.genre
            HAVING COUNT(DISTINCT m.id) > 100
            ORDER BY avg_rating DESC
        """;
        ResultSet rs = pgConn.createStatement().executeQuery(sql);
        Map<String, double[]> pgResults = new HashMap<>();
        while (rs.next()) {
            pgResults.put(rs.getString("genre"), 
                new double[]{rs.getInt("movie_count"), rs.getInt("actor_count"), rs.getDouble("avg_rating")});
        }
        rs.close();
        long pgTime = System.currentTimeMillis() - start;
        
        // Perst - manual complex aggregation
        start = System.currentTimeMillis();
        Map<String, double[]> perstResults = new HashMap<>();
        
        // Initialize counters
        Map<String, Integer> genreMovieCount = new HashMap<>();
        Map<String, Set<String>> genreActors = new HashMap<>();
        Map<String, Double> genreRatingSum = new HashMap<>();
        
        // Count movies and ratings per genre
        Iterator movieIter = movieIndex.iterator();
        while (movieIter.hasNext()) {
            Movie m = (Movie) movieIter.next();
            genreMovieCount.merge(m.genre, 1, Integer::sum);
            genreRatingSum.merge(m.genre, m.rating, Double::sum);
            
            List<String> actorList = movieToActors.get(m.uuid);
            Set<String> actorSet = genreActors.computeIfAbsent(m.genre, k -> new HashSet<>());
            if (actorList != null) {
                actorSet.addAll(actorList);
            }
        }
        
        // Calculate averages
        for (String genre : genreMovieCount.keySet()) {
            if (genreMovieCount.get(genre) > 100) {
                double avgRating = genreRatingSum.get(genre) / genreMovieCount.get(genre);
                perstResults.put(genre, new double[]{
                    genreMovieCount.get(genre),
                    genreActors.get(genre).size(),
                    avgRating
                });
            }
        }
        long perstTime = System.currentTimeMillis() - start;
        
        printResult("Complex Aggregation (genre stats with JOIN)", perstTime, pgTime);
    }
    
    // ==================== Main ====================
    
    public static void main(String[] args) throws Exception {
        System.out.println("=== Perst vs PostgreSQL - True JOIN Query Benchmark ===");
        System.out.println("Movies: " + N_MOVIES + ", Actors: " + N_ACTORS + ", Relationships: " + N_RELATIONSHIPS);
        System.out.println();
        
        // Connect to PostgreSQL
        Class.forName("org.postgresql.Driver");
        pgConn = DriverManager.getConnection(jdbcUrl, dbUser, dbPassword);
        pgConn.setAutoCommit(false);
        
        // Setup databases
        System.out.println("Setting up databases...");
        setupPostgres();
        setupPerst();
        
        // Populate data
        System.out.println("Populating data...");
        populateData();
        
        // Run tests
        System.out.println("\n--- Test 1: INNER JOIN ---");
        testInnerJoin();
        
        System.out.println("\n--- Test 2: LEFT JOIN ---");
        testLeftJoin();
        
        System.out.println("\n--- Test 3: Aggregation with JOIN ---");
        testAggregationWithJoin();
        
        System.out.println("\n--- Test 4: Multi-table JOIN (3 tables) ---");
        testMultiTableJoin();
        
        System.out.println("\n--- Test 5: Nested Subquery ---");
        testNestedSubquery();
        
        System.out.println("\n--- Test 6: Complex Aggregation ---");
        testComplexAggregation();
        
        // Cleanup
        perstStorage.close();
        pgConn.close();
        
        System.out.println("\n=== Benchmark Complete ===");
    }
}
