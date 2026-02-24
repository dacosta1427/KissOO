import java.sql.*;

public class SetupDb {
    public static void main(String[] args) {
        try {
            Connection conn = DriverManager.getConnection(
                "jdbc:postgresql://localhost:5432/postgres", 
                "postgres", "gfe"
            );
            
            Statement stmt = conn.createStatement();
            
            // Create database if not exists
            try {
                stmt.execute("CREATE DATABASE compare_test");
                System.out.println("Created database: compare_test");
            } catch (SQLException e) {
                if (e.getMessage().contains("already exists"))
                    System.out.println("Database already exists");
                else
                    System.out.println("DB creation: " + e.getMessage());
            }
            stmt.close();
            conn.close();
            
            // Connect to compare_test and create tables
            conn = DriverManager.getConnection(
                "jdbc:postgresql://localhost:5432/compare_test", 
                "postgres", "gfe"
            );
            stmt = conn.createStatement();
            
            // Create users table for login
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    user_id SERIAL PRIMARY KEY,
                    user_name VARCHAR(255) UNIQUE NOT NULL,
                    user_password VARCHAR(255),
                    user_active CHAR(1) DEFAULT 'Y'
                )
            """);
            System.out.println("Created/verified users table");
            
            // Insert test user if not exists
            try {
                stmt.execute("INSERT INTO users (user_name, user_password, user_active) VALUES ('root', 'root', 'Y')");
                System.out.println("Created user 'root'");
            } catch (SQLException e) {
                System.out.println("User already exists or error: " + e.getMessage());
            }
            
            // Create benchmark_test table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS benchmark_test (
                    rec_id SERIAL PRIMARY KEY,
                    name VARCHAR(255),
                    value BIGINT,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)
            """);
            System.out.println("Created/verified benchmark_test table");
            
            stmt.close();
            conn.close();
            System.out.println("Setup complete!");
            
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
