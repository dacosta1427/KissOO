import java.sql.*;

public class CreateUser {
    public static void main(String[] args) {
        try {
            Connection conn = DriverManager.getConnection(
                "jdbc:postgresql://localhost:5432/compare_test", 
                "postgres", "gfe"
            );
            
            Statement stmt = conn.createStatement();
            
            // Insert test user
            try {
                stmt.execute("INSERT INTO users (user_name, user_password, user_active) VALUES ('root', 'root', 'Y')");
                System.out.println("Created user 'root'");
            } catch (SQLException e) {
                System.out.println("User error: " + e.getMessage());
            }
            
            stmt.close();
            conn.close();
            System.out.println("Done!");
            
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
