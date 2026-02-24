package services

import org.kissweb.json.JSONObject
import org.kissweb.database.Connection
import org.kissweb.database.Record
import org.kissweb.restServer.ProcessServlet

/**
 * Benchmark service - performs database operations for performance testing
 * Tests Perst vs PostgreSQL via HTTP service layer
 */
class Benchmark {

    /**
     * Setup benchmark tables with richer schema
     */
    void setupTable(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        if (db == null) {
            outjson.put("error", "No database connection")
            return
        }
        
        String table = injson.optString("table", "benchmark_data")
        
        db.execute("""
            create table if not exists """ + table + """ (
                rec_id serial primary key,
                name varchar(255),
                value bigint,
                category varchar(100),
                rating integer,
                amount decimal(10,2),
                created_at timestamp default current_timestamp,
                updated_at timestamp default current_timestamp
            )
        """)
        
        // History table for PostgreSQL (manual versioning)
        db.execute("""
            create table if not exists """ + table + """_history (
                history_id serial primary key,
                rec_id integer,
                name varchar(255),
                value bigint,
                category varchar(100),
                rating integer,
                amount decimal(10,2),
                version_number integer,
                changed_at timestamp default current_timestamp,
                changed_by varchar(255)
            )
        """)
        
        db.execute("delete from " + table)
        db.execute("delete from " + table + "_history")
        
        outjson.put("success", true)
    }

    /**
     * Teardown benchmark table
     */
    void teardownTable(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        if (db == null) {
            outjson.put("error", "No database connection")
            return
        }
        
        String table = injson.optString("table", "benchmark_data")
        db.execute("drop table if exists " + table + "_history")
        db.execute("drop table if exists " + table)
        
        outjson.put("success", true)
    }

    /**
     * Bulk insert - insert multiple records
     */
    void bulkInsert(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        if (db == null) {
            outjson.put("error", "No database connection")
            return
        }
        
        int count = injson.optInt("count", 1000)
        String table = injson.optString("table", "benchmark_data")
        
        long start = System.currentTimeMillis()
        
        for (int i = 0; i < count; i++) {
            Record rec = db.newRecord(table)
            rec.set("name", "item_" + i)
            rec.set("value", i)
            rec.set("category", "cat_" + (i % 10))
            rec.set("rating", (i % 5) + 1)
            rec.set("amount", i * 1.5)
            rec.addRecord()
        }
        
        long elapsed = System.currentTimeMillis() - start
        outjson.put("elapsed", elapsed)
        outjson.put("count", count)
    }

    /**
     * Version History Test - Make multiple updates to records
     * This tests Perst's automatic versioning vs PostgreSQL manual versioning
     */
    void versionHistoryTest(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        if (db == null) {
            outjson.put("error", "No database connection")
            return
        }
        
        int recordCount = injson.optInt("recordCount", 100)
        int updateCount = injson.optInt("updateCount", 10)
        String table = injson.optString("table", "benchmark_data")
        
        long start = System.currentTimeMillis()
        
        // First insert records
        for (int i = 0; i < recordCount; i++) {
            Record rec = db.newRecord(table)
            rec.set("name", "item_" + i)
            rec.set("value", i)
            rec.set("category", "cat_" + (i % 10))
            rec.set("rating", 1)
            rec.set("amount", i * 1.5)
            rec.addRecord()
        }
        
        // Now make multiple updates to each record
        for (int r = 1; r <= recordCount; r++) {
            for (int u = 0; u < updateCount; u++) {
                Record rec = db.fetchOne("select * from " + table + " where rec_id = ?", r)
                if (rec) {
                    rec.set("value", rec.getInt("value") + 1)
                    rec.set("rating", ((u + 1) % 5) + 1)
                    rec.set("updated_at", new java.util.Date())
                    
                    // For PostgreSQL, manually insert into history
                    Record historyRec = db.newRecord(table + "_history")
                    historyRec.set("rec_id", rec.getInt("rec_id"))
                    historyRec.set("name", rec.getString("name"))
                    historyRec.set("value", rec.getInt("value"))
                    historyRec.set("category", rec.getString("category"))
                    historyRec.set("rating", rec.getInt("rating"))
                    historyRec.set("amount", rec.getDouble("amount"))
                    historyRec.set("version_number", u + 1)
                    historyRec.set("changed_at", new java.util.Date())
                    historyRec.addRecord()
                    
                    rec.update()
                }
            }
        }
        
        long elapsed = System.currentTimeMillis() - start
        
        // Count total history records
        List<Record> histCount = db.fetchAll("select count(*) as cnt from " + table + "_history")
        int totalVersions = histCount.get(0).getInt("cnt")
        
        outjson.put("elapsed", elapsed)
        outjson.put("records", recordCount)
        outjson.put("updatesPerRecord", updateCount)
        outjson.put("totalVersions", totalVersions)
    }

    /**
     * Query historical versions - test retrieval of version history
     */
    void queryHistory(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        if (db == null) {
            outjson.put("error", "No database connection")
            return
        }
        
        int recId = injson.optInt("recId", 1)
        String table = injson.optString("table", "benchmark_data")
        
        long start = System.currentTimeMillis()
        
        // Query history for a specific record
        List<Record> history = db.fetchAll(
            "select * from " + table + "_history where rec_id = ? order by version_number",
            recId
        )
        
        long elapsed = System.currentTimeMillis() - start
        
        outjson.put("elapsed", elapsed)
        outjson.put("versionCount", history.size())
    }

    /**
     * Query historical data with time filter
     */
    void queryHistoryTimeRange(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        if (db == null) {
            outjson.put("error", "No database connection")
            return
        }
        
        String table = injson.optString("table", "benchmark_data")
        int hoursBack = injson.optInt("hoursBack", 24)
        
        long start = System.currentTimeMillis()
        
        // Query recent history
        List<Record> history = db.fetchAll(
            """select * from """ + table + """_history 
               where changed_at > current_timestamp - interval '""" + hoursBack + """ hours'
               order by changed_at"""
        )
        
        long elapsed = System.currentTimeMillis() - start
        
        outjson.put("elapsed", elapsed)
        outjson.put("count", history.size())
    }

    /**
     * Aggregate over historical data
     */
    void aggregateHistory(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        if (db == null) {
            outjson.put("error", "No database connection")
            return
        }
        
        String table = injson.optString("table", "benchmark_data")
        
        long start = System.currentTimeMillis()
        
        // Count total versions ever created
        List<Record> result = db.fetchAll(
            "select count(*) as total_versions, count(distinct rec_id) as unique_records from " + table + "_history"
        )
        
        long elapsed = System.currentTimeMillis() - start
        
        outjson.put("elapsed", elapsed)
        outjson.put("totalVersions", result.get(0).getInt("total_versions"))
        outjson.put("uniqueRecords", result.get(0).getInt("unique_records"))
    }

    /**
     * Search by exact value - indexed lookup
     */
    void searchExact(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        if (db == null) {
            outjson.put("error", "No database connection")
            return
        }
        
        String table = injson.optString("table", "benchmark_data")
        int searchValue = injson.optInt("value", 5000)
        
        long start = System.currentTimeMillis()
        
        List<Record> recs = db.fetchAll("select * from " + table + " where value = ?", searchValue)
        
        long elapsed = System.currentTimeMillis() - start
        outjson.put("elapsed", elapsed)
        outjson.put("count", recs.size())
    }

    /**
     * Search by range - range query
     */
    void searchRange(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        if (db == null) {
            outjson.put("error", "No database connection")
            return
        }
        
        String table = injson.optString("table", "benchmark_data")
        int minValue = injson.optInt("minValue", 1000)
        int maxValue = injson.optInt("maxValue", 2000)
        
        long start = System.currentTimeMillis()
        
        List<Record> recs = db.fetchAll(
            "select * from " + table + " where value >= ? and value < ?", 
            minValue, maxValue
        )
        
        long elapsed = System.currentTimeMillis() - start
        outjson.put("elapsed", elapsed)
        outjson.put("count", recs.size())
    }

    /**
     * Search by category - exact match on string field
     */
    void searchCategory(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        if (db == null) {
            outjson.put("error", "No database connection")
            return
        }
        
        String table = injson.optString("table", "benchmark_data")
        String category = injson.optString("category", "cat_5")
        
        long start = System.currentTimeMillis()
        
        List<Record> recs = db.fetchAll("select * from " + table + " where category = ?", category)
        
        long elapsed = System.currentTimeMillis() - start
        outjson.put("elapsed", elapsed)
        outjson.put("count", recs.size())
    }

    /**
     * Aggregation - COUNT
     */
    void aggregateCount(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        if (db == null) {
            outjson.put("error", "No database connection")
            return
        }
        
        String table = injson.optString("table", "benchmark_data")
        
        long start = System.currentTimeMillis()
        
        List<Record> recs = db.fetchAll("select count(*) as cnt from " + table)
        int count = recs.get(0).getInt("cnt")
        
        long elapsed = System.currentTimeMillis() - start
        outjson.put("elapsed", elapsed)
        outjson.put("count", count)
    }

    /**
     * Aggregation - SUM
     */
    void aggregateSum(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        if (db == null) {
            outjson.put("error", "No database connection")
            return
        }
        
        String table = injson.optString("table", "benchmark_data")
        
        long start = System.currentTimeMillis()
        
        List<Record> recs = db.fetchAll("select sum(value) as total from " + table)
        long sum = recs.get(0).getLong("total")
        
        long elapsed = System.currentTimeMillis() - start
        outjson.put("elapsed", elapsed)
        outjson.put("sum", sum)
    }

    /**
     * Aggregation - AVG
     */
    void aggregateAvg(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        if (db == null) {
            outjson.put("error", "No database connection")
            return
        }
        
        String table = injson.optString("table", "benchmark_data")
        
        long start = System.currentTimeMillis()
        
        List<Record> recs = db.fetchAll("select avg(value) as avg_val from " + table)
        double avg = recs.get(0).getDouble("avg_val")
        
        long elapsed = System.currentTimeMillis() - start
        outjson.put("elapsed", elapsed)
        outjson.put("avg", avg)
    }

    /**
     * Aggregation - GROUP BY
     */
    void aggregateGroupBy(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        if (db == null) {
            outjson.put("error", "No database connection")
            return
        }
        
        String table = injson.optString("table", "benchmark_data")
        
        long start = System.currentTimeMillis()
        
        List<Record> recs = db.fetchAll(
            "select category, count(*) as cnt, avg(rating) as avg_rating from " + table + " group by category"
        )
        
        long elapsed = System.currentTimeMillis() - start
        outjson.put("elapsed", elapsed)
        outjson.put("count", recs.size())
    }

    /**
     * Complex filter with multiple conditions
     */
    void filterComplex(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        if (db == null) {
            outjson.put("error", "No database connection")
            return
        }
        
        String table = injson.optString("table", "benchmark_data")
        int minRating = injson.optInt("minRating", 3)
        int maxValue = injson.optInt("maxValue", 5000)
        
        long start = System.currentTimeMillis()
        
        List<Record> recs = db.fetchAll(
            "select * from " + table + " where rating >= ? and value < ?",
            minRating, maxValue
        )
        
        long elapsed = System.currentTimeMillis() - start
        outjson.put("elapsed", elapsed)
        outjson.put("count", recs.size())
    }

    /**
     * Query with ORDER BY and LIMIT
     */
    void queryOrdered(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        if (db == null) {
            outjson.put("error", "No database connection")
            return
        }
        
        String table = injson.optString("table", "benchmark_data")
        int limit = injson.optInt("limit", 100)
        
        long start = System.currentTimeMillis()
        
        List<Record> recs = db.fetchAll(
            "select * from " + table + " order by value desc limit ?",
            limit
        )
        
        long elapsed = System.currentTimeMillis() - start
        outjson.put("elapsed", elapsed)
        outjson.put("count", recs.size())
    }

    /**
     * Query all records - full table scan
     */
    void queryAll(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        if (db == null) {
            outjson.put("error", "No database connection")
            return
        }
        
        String table = injson.optString("table", "benchmark_data")
        
        long start = System.currentTimeMillis()
        
        List<Record> recs = db.fetchAll("select * from " + table)
        
        long elapsed = System.currentTimeMillis() - start
        outjson.put("elapsed", elapsed)
        outjson.put("count", recs.size())
    }

    /**
     * Update single record
     */
    void updateRecord(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        if (db == null) {
            outjson.put("error", "No database connection")
            return
        }
        
        String table = injson.optString("table", "benchmark_data")
        int id = injson.optInt("id", 1)
        
        long start = System.currentTimeMillis()
        
        Record rec = db.fetchOne("select * from " + table + " where rec_id = ?", id)
        if (rec) {
            rec.set("value", System.currentTimeMillis())
            rec.update()
        }
        
        long elapsed = System.currentTimeMillis() - start
        outjson.put("elapsed", elapsed)
    }

    /**
     * Delete single record
     */
    void deleteRecord(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        if (db == null) {
            outjson.put("error", "No database connection")
            return
        }
        
        String table = injson.optString("table", "benchmark_data")
        int id = injson.optInt("id", 1)
        
        long start = System.currentTimeMillis()
        
        db.execute("delete from " + table + " where rec_id = ?", id)
        
        long elapsed = System.currentTimeMillis() - start
        outjson.put("elapsed", elapsed)
    }

    /**
     * Insert single record (simple)
     */
    void insertRecord(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        if (db == null) {
            outjson.put("error", "No database connection")
            return
        }
        
        String table = injson.optString("table", "benchmark_data")
        
        long start = System.currentTimeMillis()
        
        Record rec = db.newRecord(table)
        rec.set("name", "item_" + System.currentTimeMillis())
        rec.set("value", System.currentTimeMillis())
        rec.set("category", "cat_1")
        rec.set("rating", 3)
        rec.set("amount", 10.0)
        rec.addRecord()
        
        long elapsed = System.currentTimeMillis() - start
        outjson.put("elapsed", elapsed)
    }

    /**
     * Setup relational tables for Object Navigation test
     * Creates: Customer -> Order -> OrderItem -> Product
     */
    void setupNavigationTables(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        if (db == null) {
            outjson.put("error", "No database connection")
            return
        }
        
        db.execute("drop table if exists bench_order_item")
        db.execute("drop table if exists bench_order")
        db.execute("drop table if exists bench_customer")
        db.execute("drop table if exists bench_product")
        
        db.execute("""
            create table bench_product (
                product_id serial primary key,
                name varchar(255),
                price decimal(10,2),
                category varchar(100)
            )
        """)
        
        db.execute("""
            create table bench_customer (
                customer_id serial primary key,
                name varchar(255),
                email varchar(255),
                created_at timestamp default current_timestamp
            )
        """)
        
        db.execute("""
            create table bench_order (
                order_id serial primary key,
                customer_id integer references bench_customer(customer_id),
                order_date timestamp default current_timestamp,
                total decimal(10,2)
            )
        """)
        
        db.execute("""
            create table bench_order_item (
                item_id serial primary key,
                order_id integer references bench_order(order_id),
                product_id integer references bench_product(product_id),
                quantity integer,
                price decimal(10,2)
            )
        """)
        
        db.execute("create index idx_order_customer on bench_order(customer_id)")
        db.execute("create index idx_order_item_order on bench_order_item(order_id)")
        db.execute("create index idx_order_item_product on bench_order_item(product_id)")
        
        outjson.put("success", true)
    }

    /**
     * Populate navigation tables with test data
     */
    void populateNavigationData(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        if (db == null) {
            outjson.put("error", "No database connection")
            return
        }
        
        int customers = injson.optInt("customers", 100)
        int ordersPerCustomer = injson.optInt("ordersPerCustomer", 5)
        int itemsPerOrder = injson.optInt("itemsPerOrder", 3)
        
        long start = System.currentTimeMillis()
        
        List<Integer> productIds = []
        for (int i = 0; i < 20; i++) {
            Record prod = db.newRecord("bench_product")
            prod.set("name", "Product_" + i)
            prod.set("price", (i + 1) * 10.0)
            prod.set("category", "cat_" + (i % 5))
            prod.addRecord()
            productIds.add(prod.getInt("product_id"))
        }
        
        int totalOrders = 0
        int totalItems = 0
        
        for (int c = 1; c <= customers; c++) {
            Record cust = db.newRecord("bench_customer")
            cust.set("name", "Customer_" + c)
            cust.set("email", "customer" + c + "@test.com")
            cust.addRecord()
            int custId = cust.getInt("customer_id")
            
            for (int o = 0; o < ordersPerCustomer; o++) {
                Record ord = db.newRecord("bench_order")
                ord.set("customer_id", custId)
                ord.set("total", 0.0)
                ord.addRecord()
                int orderId = ord.getInt("order_id")
                totalOrders++
                
                double orderTotal = 0.0
                for (int i = 0; i < itemsPerOrder; i++) {
                    int prodIdx = (int)(Math.random() * productIds.size())
                    int qty = (int)(Math.random() * 5) + 1
                    double price = (prodIdx + 1) * 10.0
                    
                    Record item = db.newRecord("bench_order_item")
                    item.set("order_id", orderId)
                    item.set("product_id", productIds.get(prodIdx))
                    item.set("quantity", qty)
                    item.set("price", price)
                    item.addRecord()
                    totalItems++
                    
                    orderTotal += qty * price
                }
                
                ord.set("total", orderTotal)
                ord.update()
            }
        }
        
        long elapsed = System.currentTimeMillis() - start
        outjson.put("elapsed", elapsed)
        outjson.put("customers", customers)
        outjson.put("orders", totalOrders)
        outjson.put("items", totalItems)
    }

    /**
     * Navigate: Get customer with all their orders (1:N relationship)
     * Tests Perst's object references vs PostgreSQL JOIN
     */
    void navCustomerOrders(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        if (db == null) {
            outjson.put("error", "No database connection")
            return
        }
        
        int customerId = injson.optInt("customerId", 1)
        
        long start = System.currentTimeMillis()
        
        Record customer = db.fetchOne("select * from bench_customer where customer_id = ?", customerId)
        List<Record> orders = db.fetchAll("select * from bench_order where customer_id = ?", customerId)
        
        long elapsed = System.currentTimeMillis() - start
        
        outjson.put("elapsed", elapsed)
        outjson.put("customerFound", customer != null)
        outjson.put("orderCount", orders.size())
    }

    /**
     * Navigate: Get order with all its items (1:N relationship)
     */
    void navOrderItems(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        if (db == null) {
            outjson.put("error", "No database connection")
            return
        }
        
        int orderId = injson.optInt("orderId", 1)
        
        long start = System.currentTimeMillis()
        
        Record order = db.fetchOne("select * from bench_order where order_id = ?", orderId)
        List<Record> items = db.fetchAll("select * from bench_order_item where order_id = ?", orderId)
        
        long elapsed = System.currentTimeMillis() - start
        
        outjson.put("elapsed", elapsed)
        outjson.put("orderFound", order != null)
        outjson.put("itemCount", items.size())
    }

    /**
     * Navigate: Get all customers who bought a specific product (N:M via items)
     */
    void navProductBuyers(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        if (db == null) {
            outjson.put("error", "No database connection")
            return
        }
        
        int productId = injson.optInt("productId", 1)
        
        long start = System.currentTimeMillis()
        
        List<Record> buyers = db.fetchAll("""
            select distinct c.* from bench_customer c
            join bench_order o on c.customer_id = o.customer_id
            join bench_order_item oi on o.order_id = oi.order_id
            where oi.product_id = ?
        """, productId)
        
        long elapsed = System.currentTimeMillis() - start
        
        outjson.put("elapsed", elapsed)
        outjson.put("buyerCount", buyers.size())
    }

    /**
     * Complex navigation: Customer -> Orders -> Items -> Product (full chain)
     */
    void navFullChain(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        if (db == null) {
            outjson.put("error", "No database connection")
            return
        }
        
        int customerId = injson.optInt("customerId", 1)
        
        long start = System.currentTimeMillis()
        
        Record customer = db.fetchOne("select * from bench_customer where customer_id = ?", customerId)
        List<Record> orders = db.fetchAll("select * from bench_order where customer_id = ?", customerId)
        
        int totalItems = 0
        for (Record order : orders) {
            List<Record> items = db.fetchAll("select * from bench_order_item where order_id = ?", order.getInt("order_id"))
            totalItems += items.size()
        }
        
        long elapsed = System.currentTimeMillis() - start
        
        outjson.put("elapsed", elapsed)
        outjson.put("customerFound", customer != null)
        outjson.put("orderCount", orders.size())
        outjson.put("totalItems", totalItems)
    }

    /**
     * Aggregate over related data: Total orders per customer
     */
    void navAggregateOrdersPerCustomer(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        if (db == null) {
            outjson.put("error", "No database connection")
            return
        }
        
        long start = System.currentTimeMillis()
        
        List<Record> result = db.fetchAll("""
            select c.customer_id, c.name, count(o.order_id) as order_count, sum(o.total) as total_spent
            from bench_customer c
            left join bench_order o on c.customer_id = o.customer_id
            group by c.customer_id, c.name
            order by total_spent desc
            limit 10
        """)
        
        long elapsed = System.currentTimeMillis() - start
        
        outjson.put("elapsed", elapsed)
        outjson.put("count", result.size())
    }

    /**
     * Aggregate over related data: Product popularity
     */
    void navAggregateProductPopularity(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        if (db == null) {
            outjson.put("error", "No database connection")
            return
        }
        
        long start = System.currentTimeMillis()
        
        List<Record> result = db.fetchAll("""
            select p.product_id, p.name, 
                   count(oi.item_id) as times_ordered,
                   sum(oi.quantity) as total_quantity,
                   sum(oi.quantity * oi.price) as total_revenue
            from bench_product p
            left join bench_order_item oi on p.product_id = oi.product_id
            group by p.product_id, p.name
            order by total_revenue desc
        """)
        
        long elapsed = System.currentTimeMillis() - start
        
        outjson.put("elapsed", elapsed)
        outjson.put("count", result.size())
    }
}

