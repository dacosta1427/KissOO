package nl.dcg.gfe;

import org.garret.perst.*;
import java.sql.*;
import java.util.*;

public class ComparisonBenchmark7 {

    static class Customer extends Persistent {
        String name;
        String email;
        String city;
        double balance;
        
        public Customer() {}
        
        public Customer(String name, String email, String city, double balance) {
            this.name = name;
            this.email = email;
            this.city = city;
            this.balance = balance;
        }
    }
    
    static class Order extends Persistent {
        int orderId;
        Customer customer;
        Product product;
        int quantity;
        double total;
        long orderDate;
        
        public Order() {}
        
        public Order(int orderId, Customer customer, Product product, int quantity, double total) {
            this.orderId = orderId;
            this.customer = customer;
            this.product = product;
            this.quantity = quantity;
            this.total = total;
            this.orderDate = System.currentTimeMillis();
        }
    }
    
    static class Product extends Persistent {
        String sku;
        String name;
        String category;
        double price;
        int stock;
        
        public Product() {}
        
        public Product(String sku, String name, String category, double price, int stock) {
            this.sku = sku;
            this.name = name;
            this.category = category;
            this.price = price;
            this.stock = stock;
        }
    }

    static final int N_CUSTOMERS = 5000;
    static final int N_PRODUCTS = 2000;
    static final int N_ORDERS = 10000;
    
    static String jdbcUrl = "jdbc:postgresql://localhost:5432/perst_test";
    static String dbUser = "postgres";
    static String dbPassword = "gfe";
    
    static Connection pgConn;
    static Storage perstStorage;
    static Index customerIndex;
    static Index orderIndex;
    static Index productIndex;
    
    static void printResult(String test, long perstMs, long pgMs) {
        double speedup = pgMs > 0 ? (double) pgMs / perstMs : 0;
        System.out.printf("%-50s Perst: %6d ms  PostgreSQL: %6d ms  Speedup: %7.2fx%n",
                test, perstMs, pgMs, speedup);
    }

    static void setupPostgres() throws SQLException {
        Statement stmt = pgConn.createStatement();
        stmt.execute("DROP TABLE IF EXISTS orders CASCADE");
        stmt.execute("DROP TABLE IF EXISTS customers CASCADE");
        stmt.execute("DROP TABLE IF EXISTS products CASCADE");
        
        stmt.execute("""
            CREATE TABLE customers (
                id SERIAL PRIMARY KEY, name VARCHAR(255), email VARCHAR(255),
                city VARCHAR(100), balance DECIMAL(10,2)
            )
        """);
        
        stmt.execute("""
            CREATE TABLE products (
                id SERIAL PRIMARY KEY, sku VARCHAR(50) UNIQUE, name VARCHAR(255),
                category VARCHAR(100), price DECIMAL(10,2), stock INTEGER
            )
        """);
        
        stmt.execute("""
            CREATE TABLE orders (
                id SERIAL PRIMARY KEY, order_id INTEGER, customer_id INTEGER REFERENCES customers(id),
                product_id INTEGER REFERENCES products(id), quantity INTEGER, total DECIMAL(10,2),
                order_date BIGINT
            )
        """);
        
        stmt.execute("CREATE INDEX idx_orders_customer ON orders(customer_id)");
        stmt.execute("CREATE INDEX idx_orders_product ON orders(product_id)");
        stmt.execute("CREATE INDEX idx_customers_city ON customers(city)");
        stmt.execute("CREATE INDEX idx_products_category ON products(category)");
        
        pgConn.commit();
        stmt.close();
    }
    
    static final int pagePoolSize = 128 * 1024 * 1024;
    
    static void setupPerst() throws Exception {
        java.io.File dbFile = new java.io.File("benchmark7.dbs");
        if (dbFile.exists()) dbFile.delete();
        
        perstStorage = StorageFactory.getInstance().createStorage();
        perstStorage.open("benchmark7.dbs", pagePoolSize);
        
        customerIndex = perstStorage.createIndex(String.class, true);
        orderIndex = perstStorage.createIndex(int.class, true);
        productIndex = perstStorage.createIndex(String.class, true);
    }

    static void populateData() throws Exception {
        Random rand = new Random(42);
        String[] cities = {"New York", "Los Angeles", "Chicago", "Houston", "Phoenix"};
        String[] categories = {"Electronics", "Clothing", "Food", "Books", "Sports"};
        
        System.out.println("Populating PostgreSQL...");
        
        Map<Integer, Integer> custIdMap = new HashMap<>();
        Map<Integer, Integer> prodIdMap = new HashMap<>();
        
        PreparedStatement custPs = pgConn.prepareStatement(
            "INSERT INTO customers (name, email, city, balance) VALUES (?, ?, ?, ?)",
            Statement.RETURN_GENERATED_KEYS
        );
        
        for (int i = 0; i < N_CUSTOMERS; i++) {
            custPs.setString(1, "Customer-" + i);
            custPs.setString(2, "customer" + i + "@example.com");
            custPs.setString(3, cities[i % cities.length]);
            custPs.setDouble(4, rand.nextDouble() * 1000);
            custPs.executeUpdate();
            ResultSet rs = custPs.getGeneratedKeys();
            if (rs.next()) custIdMap.put(i, rs.getInt(1));
        }
        pgConn.commit();
        custPs.close();
        
        PreparedStatement prodPs = pgConn.prepareStatement(
            "INSERT INTO products (sku, name, category, price, stock) VALUES (?, ?, ?, ?, ?)",
            Statement.RETURN_GENERATED_KEYS
        );
        
        for (int i = 0; i < N_PRODUCTS; i++) {
            prodPs.setString(1, "SKU-" + i);
            prodPs.setString(2, "Product-" + i);
            prodPs.setString(3, categories[i % categories.length]);
            prodPs.setDouble(4, 10.0 + rand.nextDouble() * 100);
            prodPs.setInt(5, rand.nextInt(1000));
            prodPs.executeUpdate();
            ResultSet rs = prodPs.getGeneratedKeys();
            if (rs.next()) prodIdMap.put(i, rs.getInt(1));
        }
        pgConn.commit();
        prodPs.close();
        
        PreparedStatement orderPs = pgConn.prepareStatement(
            "INSERT INTO orders (order_id, customer_id, product_id, quantity, total, order_date) VALUES (?, ?, ?, ?, ?, ?)"
        );
        
        for (int i = 0; i < N_ORDERS; i++) {
            int custId = custIdMap.get(rand.nextInt(N_CUSTOMERS));
            int prodId = prodIdMap.get(rand.nextInt(N_PRODUCTS));
            int qty = 1 + rand.nextInt(5);
            double price = 10.0 + rand.nextDouble() * 100;
            
            orderPs.setInt(1, i);
            orderPs.setInt(2, custId);
            orderPs.setInt(3, prodId);
            orderPs.setInt(4, qty);
            orderPs.setDouble(5, qty * price);
            orderPs.setLong(6, System.currentTimeMillis());
            orderPs.addBatch();
            if (i % 1000 == 0) orderPs.executeBatch();
        }
        orderPs.executeBatch();
        pgConn.commit();
        orderPs.close();
        
        System.out.println("Populating Perst...");
        
        Map<Integer, Customer> custMap = new HashMap<>();
        for (int i = 0; i < N_CUSTOMERS; i++) {
            Customer c = new Customer("Customer-" + i, "customer" + i + "@example.com",
                cities[i % cities.length], rand.nextDouble() * 1000);
            perstStorage.makePersistent(c);
            customerIndex.put(new Key(c.email), c);
            custMap.put(i, c);
        }
        
        Map<Integer, Product> prodMap = new HashMap<>();
        for (int i = 0; i < N_PRODUCTS; i++) {
            Product p = new Product("SKU-" + i, "Product-" + i, categories[i % categories.length],
                10.0 + rand.nextDouble() * 100, rand.nextInt(1000));
            perstStorage.makePersistent(p);
            productIndex.put(new Key(p.sku), p);
            prodMap.put(i, p);
        }
        
        for (int i = 0; i < N_ORDERS; i++) {
            Customer c = custMap.get(rand.nextInt(N_CUSTOMERS));
            Product p = prodMap.get(rand.nextInt(N_PRODUCTS));
            int qty = 1 + rand.nextInt(5);
            double total = qty * p.price;
            
            Order o = new Order(i, c, p, qty, total);
            perstStorage.makePersistent(o);
            orderIndex.put(new Key(i), o);
        }
        perstStorage.commit();
    }

    static void testCRUD() throws Exception {
        System.out.println("\n--- Test 1: Create (New Customer) ---");
        
        long start = System.currentTimeMillis();
        PreparedStatement ps = pgConn.prepareStatement(
            "INSERT INTO customers (name, email, city, balance) VALUES (?, ?, ?, ?)",
            Statement.RETURN_GENERATED_KEYS
        );
        for (int i = 0; i < 1000; i++) {
            ps.setString(1, "NewCust-" + i);
            ps.setString(2, "new" + i + "@test.com");
            ps.setString(3, "Boston");
            ps.setDouble(4, 500.0);
            ps.executeUpdate();
        }
        pgConn.commit();
        ps.close();
        long pgCreate = System.currentTimeMillis() - start;
        
        start = System.currentTimeMillis();
        for (int i = 0; i < 1000; i++) {
            Customer c = new Customer("NewCust-" + i, "new" + i + "@test.com", "Boston", 500.0);
            perstStorage.makePersistent(c);
            customerIndex.put(new Key(c.email), c);
        }
        perstStorage.commit();
        long perstCreate = System.currentTimeMillis() - start;
        
        printResult("Create 1000 customers", perstCreate, pgCreate);
        
        System.out.println("\n--- Test 2: Read (Lookup by Email) ---");
        
        start = System.currentTimeMillis();
        ps = pgConn.prepareStatement("SELECT * FROM customers WHERE email = ?");
        for (int i = 0; i < 1000; i++) {
            ps.setString(1, "customer" + i + "@example.com");
            ResultSet rs = ps.executeQuery();
            rs.next();
            rs.close();
        }
        ps.close();
        long pgRead = System.currentTimeMillis() - start;
        
        start = System.currentTimeMillis();
        for (int i = 0; i < 1000; i++) {
            Customer c = (Customer) customerIndex.get(new Key("customer" + i + "@example.com"));
        }
        long perstRead = System.currentTimeMillis() - start;
        
        printResult("Read 1000 customers by email", perstRead, pgRead);
        
        System.out.println("\n--- Test 3: Update (Change Balance) ---");
        
        start = System.currentTimeMillis();
        ps = pgConn.prepareStatement("UPDATE customers SET balance = balance * 1.1 WHERE city = ?");
        ps.setString(1, "New York");
        ps.executeUpdate();
        pgConn.commit();
        ps.close();
        long pgUpdate = System.currentTimeMillis() - start;
        
        start = System.currentTimeMillis();
        Iterator iter = customerIndex.iterator();
        int updated = 0;
        while (iter.hasNext()) {
            Customer c = (Customer) iter.next();
            if ("New York".equals(c.city)) {
                c.balance *= 1.1;
                updated++;
            }
        }
        perstStorage.commit();
        long perstUpdate = System.currentTimeMillis() - start;
        
        printResult("Update customers in New York", perstUpdate, pgUpdate);
        
        System.out.println("\n--- Test 4: Delete (Remove Orders) ---");
        
        start = System.currentTimeMillis();
        Statement stmt = pgConn.createStatement();
        stmt.execute("DELETE FROM orders WHERE order_id < 1000");
        pgConn.commit();
        stmt.close();
        long pgDelete = System.currentTimeMillis() - start;
        
        start = System.currentTimeMillis();
        for (int i = 0; i < 1000; i++) {
            orderIndex.remove(new Key(i));
        }
        perstStorage.commit();
        long perstDelete = System.currentTimeMillis() - start;
        
        printResult("Delete 1000 orders", perstDelete, pgDelete);
    }

    static void testRelationships() throws Exception {
        System.out.println("\n--- Test 5: Object Navigation (Perst) vs Join (PostgreSQL) ---");
        
        long start = System.currentTimeMillis();
        String sql = """
            SELECT c.name, c.city, o.order_id, o.total
            FROM customers c
            JOIN orders o ON c.id = o.customer_id
            WHERE o.total > 100
            LIMIT 1000
        """;
        ResultSet rs = pgConn.createStatement().executeQuery(sql);
        int count = 0;
        while (rs.next()) count++;
        rs.close();
        long pgJoin = System.currentTimeMillis() - start;
        
        start = System.currentTimeMillis();
        int perstCount = 0;
        Iterator iter = orderIndex.iterator();
        while (iter.hasNext()) {
            Order o = (Order) iter.next();
            if (o.total > 100 && o.customer != null) {
                String name = o.customer.name;
                String city = o.customer.city;
                perstCount++;
                if (perstCount >= 1000) break;
            }
        }
        long perstNav = System.currentTimeMillis() - start;
        
        printResult("Get orders > $100 with customer info", perstNav, pgJoin);
        
        System.out.println("\n--- Test 6: Aggregate by Category ---");
        
        start = System.currentTimeMillis();
        String aggSql = """
            SELECT p.category, COUNT(*) as order_count, SUM(o.total) as total_sales
            FROM orders o
            JOIN products p ON o.product_id = p.id
            GROUP BY p.category
        """;
        rs = pgConn.createStatement().executeQuery(aggSql);
        int pgAggCount = 0;
        while (rs.next()) pgAggCount++;
        rs.close();
        long pgAgg = System.currentTimeMillis() - start;
        
        start = System.currentTimeMillis();
        Map<String, Integer> categoryCount = new HashMap<>();
        Map<String, Double> categorySales = new HashMap<>();
        
        iter = orderIndex.iterator();
        while (iter.hasNext()) {
            Order o = (Order) iter.next();
            if (o.product != null) {
                String cat = o.product.category;
                categoryCount.put(cat, categoryCount.getOrDefault(cat, 0) + 1);
                categorySales.put(cat, categorySales.getOrDefault(cat, 0.0) + o.total);
            }
        }
        long perstAgg = System.currentTimeMillis() - start;
        
        printResult("Aggregate orders by product category", perstAgg, pgAgg);
    }

    public static void main(String[] args) throws Exception {
        System.out.println("=== Perst vs PostgreSQL - Real-World Patterns Benchmark ===");
        System.out.println("Customers: " + N_CUSTOMERS + ", Products: " + N_PRODUCTS + ", Orders: " + N_ORDERS);
        System.out.println("Testing CRUD operations and relationships\n");
        
        Class.forName("org.postgresql.Driver");
        pgConn = DriverManager.getConnection(jdbcUrl, dbUser, dbPassword);
        pgConn.setAutoCommit(false);
        
        System.out.println("Setting up databases...");
        setupPostgres();
        setupPerst();
        
        System.out.println("Populating data...");
        populateData();
        
        testCRUD();
        testRelationships();
        
        perstStorage.close();
        pgConn.close();
        
        java.io.File dbFile = new java.io.File("benchmark7.dbs");
        if (dbFile.exists()) dbFile.delete();
        
        System.out.println("\n=== Benchmark Complete ===");
    }
}
