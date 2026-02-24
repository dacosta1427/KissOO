import java.sql.*;

public class TestPg {
    public static void main(String[] args) {
        try {
            DriverManager.setLoginTimeout(5);
            Connection conn = DriverManager.getConnection(
                "jdbc:postgresql://localhost:5432/postgres", 
                "postgres", "gfe"
            );
            System.out.println("Connected!");
            ResultSet rs = conn.createStatement().executeQuery("SELECT 1");
            rs.next();
            System.out.println("Result: " + rs.getInt(1));
            conn.close();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
