import java.sql.*;

public class CheckData {
    public static void main(String[] args) {
        try {
            Connection conn = DriverManager.getConnection(
                "jdbc:postgresql://localhost:5432/compare_test", 
                "postgres", "gfe"
            );
            
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM benchmark_data");
            rs.next();
            System.out.println("Record count: " + rs.getInt(1));
            
            rs.close();
            stmt.close();
            conn.close();
            
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
