package nl.dcg.gfe;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * HTTP Benchmark Client for KISS Server
 * 
 * This client fires HTTP requests to a running KISS server and measures response times.
 * It compares PostgreSQL vs Perst by making the same requests to servers running with
 * different database backends.
 * 
 * IMPORTANT: Core methods (Login, Logout, etc.) require "_class":""
 * to be set in the request, even though it's empty. This is a KISS framework requirement.
 * 
 * Usage:
 * 1. Start KISS server with PostgreSQL (PerstEnabled=false in application.ini)
 * 2. Run: java -cp "libs/*:target/classes" nl.dcg.gfe.HttpBenchmarkClient
 * 3. Stop server, enable Perst (PerstEnabled=true)
 * 4. Rebuild: cd kissweb && java -cp "work/exploded/WEB-INF/classes;libs/*" Tasks -v build
 * 5. Restart server and run benchmark again
 * 6. Compare results
 */
public class HttpBenchmarkClient {
    
    static final String SERVER_URL = "http://localhost:8080/rest";
    static final String USERNAME = "root";
    static final String PASSWORD = "root";
    static final String TABLE_NAME = "benchmark_data";
    
    static String sessionUuid = null;
    static int requestCount = 0;
    static final HttpClient httpClient = HttpClient.newHttpClient();
    
    public static void main(String[] args) throws Exception {
        System.out.println("=== KISS HTTP Benchmark Client ===");
        System.out.println("Server: " + SERVER_URL);
        System.out.println("Table: " + TABLE_NAME);
        System.out.println();
        
        // Check if server is running
        if (!checkServer()) {
            System.out.println("ERROR: Server not responding at " + SERVER_URL);
            System.out.println("Please start the KISS server first:");
            System.out.println("  cd kissweb");
            System.out.println("  ./bld develop");
            return;
        }
        
        // Login to get session
        if (!login()) {
            System.out.println("ERROR: Login failed");
            System.out.println("Please ensure user 'root' with password 'root' exists");
            return;
        }
        
        System.out.println("Logged in successfully");
        
        // Setup benchmark table
        setupTable();
        
        // Run benchmarks
        System.out.println("\n=== Running Benchmarks ===\n");
        
        // Phase 1: Load large dataset (10K records)
        runTest("Load 10K Records", "bulkInsert", 1, 10000);
        
        // Phase 2: Search operations
        runTest("Search Exact", "searchExact", 50, 5000);
        runTest("Search Range", "searchRange", 50, 1000, 2000);
        runTest("Search Category", "searchCategory", 50, "cat_5");
        
        // Phase 3: Aggregation
        runTest("COUNT", "aggregateCount", 50);
        runTest("SUM", "aggregateSum", 50);
        runTest("AVG", "aggregateAvg", 50);
        runTest("GROUP BY", "aggregateGroupBy", 50);
        
        // Phase 4: Complex queries
        runTest("Filter Complex", "filterComplex", 50, 3, 5000);
        runTest("Query Ordered", "queryOrdered", 50, 100);
        runTest("Query All", "queryAll", 20);
        
        // Phase 5: CRUD operations
        runTest("Insert Single", "insertRecord", 50);
        runTest("Update", "updateRecord", 50, 100);
        runTest("Delete", "deleteRecord", 50, 100);
        
        // Phase 6: Version History (key Perst feature!)
        runTest("Version History - Create 50 records x 10 updates", "versionHistoryTest", 1, 50, 10);
        runTest("Query History - Get all versions of record 1", "queryHistory", 20, 1);
        runTest("Query History Time Range", "queryHistoryTimeRange", 10, 24);
        runTest("Aggregate History", "aggregateHistory", 10);
        
        // Phase 7: Object Navigation (Perst OOP features!)
        System.out.println("\n=== Object Navigation Tests ===\n");
        runNavigationTest("Setup Navigation Tables", "setupNavigationTables", 1);
        runNavigationTest("Populate Navigation Data (100 customers x 5 orders x 3 items)", "populateNavigationData", 1, 100, 5, 3);
        runNavigationTest("Nav: Customer -> Orders", "navCustomerOrders", 50, 1);
        runNavigationTest("Nav: Order -> Items", "navOrderItems", 50, 1);
        runNavigationTest("Nav: Product -> Buyers", "navProductBuyers", 50, 1);
        runNavigationTest("Nav: Full Chain (Customer->Orders->Items)", "navFullChain", 50, 1);
        runNavigationTest("Nav: Aggregate Orders per Customer", "navAggregateOrdersPerCustomer", 10);
        runNavigationTest("Nav: Aggregate Product Popularity", "navAggregateProductPopularity", 10);
        
        // Phase 8: Larger Datasets (50K, 100K)
        System.out.println("\n=== Larger Datasets Tests ===\n");
        runTest("Load 50K Records", "bulkInsert", 1, 50000);
        runTest("Search Exact (50K)", "searchExact", 50, 25000);
        runTest("Search Range (50K)", "searchRange", 50, 10000, 20000);
        runTest("COUNT (50K)", "aggregateCount", 50);
        runTest("SUM (50K)", "aggregateSum", 50);
        
        // Phase 9: Concurrent Load Testing
        System.out.println("\n=== Concurrent Load Tests ===\n");
        runConcurrentTest("Concurrent: 10 threads x 10 ops", 10, 10);
        runConcurrentTest("Concurrent: 20 threads x 10 ops", 20, 10);
        
        // Cleanup
        teardownTable();
        
        System.out.println("\n=== Benchmark Complete ===");
        System.out.println("Total requests: " + requestCount);
    }
    
    static boolean checkServer() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(SERVER_URL))
                .timeout(java.time.Duration.ofSeconds(2))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
            httpClient.send(request, HttpResponse.BodyHandlers.discarding());
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    static boolean login() throws Exception {
        String requestBody = String.format(
            "{\"_class\":\"\",\"_method\":\"Login\",\"username\":\"%s\",\"password\":\"%s\"}",
            USERNAME, PASSWORD
        );
        
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(SERVER_URL))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBody))
            .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        String body = response.body();
        
        // Extract uuid from response
        Pattern pattern = Pattern.compile("\"uuid\":\"([^\"]+)\"");
        Matcher matcher = pattern.matcher(body);
        if (matcher.find()) {
            sessionUuid = matcher.group(1);
            return true;
        }
        return false;
    }
    
    static void setupTable() throws Exception {
        String requestBody = createRequest("Benchmark", "setupTable", 
            "\"table\":\"" + TABLE_NAME + "\"");
        
        sendRequest(requestBody);
        System.out.println("Setup: OK (created table " + TABLE_NAME + ")");
    }
    
    static void teardownTable() throws Exception {
        String requestBody = createRequest("Benchmark", "teardownTable",
            "\"table\":\"" + TABLE_NAME + "\"");
        
        sendRequest(requestBody);
    }
    
    static void runNavigationTest(String name, String method, int iterations, Object... params) throws Exception {
        System.out.println("--- " + name + " (" + iterations + " iterations) ---");
        
        StringBuilder paramStr = new StringBuilder();
        
        for (Object p : params) {
            if (paramStr.length() > 0) {
                paramStr.append(",");
            }
            if (p instanceof Integer) {
                paramStr.append("\"value\":").append(p);
            } else if (p instanceof String) {
                paramStr.append("\"").append(p).append("\"");
            }
        }
        
        if (method.equals("populateNavigationData")) {
            paramStr = new StringBuilder("\"customers\":" + params[0] + ",\"ordersPerCustomer\":" + params[1] + ",\"itemsPerOrder\":" + params[2]);
        } else if (method.equals("navCustomerOrders")) {
            paramStr = new StringBuilder("\"customerId\":" + params[0]);
        } else if (method.equals("navOrderItems")) {
            paramStr = new StringBuilder("\"orderId\":" + params[0]);
        } else if (method.equals("navProductBuyers")) {
            paramStr = new StringBuilder("\"productId\":" + params[0]);
        } else if (method.equals("navFullChain")) {
            paramStr = new StringBuilder("\"customerId\":" + params[0]);
        }
        
        List<Long> times = new ArrayList<>();
        
        for (int i = 0; i < iterations; i++) {
            String requestBody = createRequest("Benchmark", method, paramStr.toString());
            
            long start = System.currentTimeMillis();
            String response = sendRequest(requestBody);
            long elapsed = System.currentTimeMillis() - start;
            
            long actualElapsed = extractElapsed(response, elapsed);
            times.add(actualElapsed);
            
            requestCount++;
        }
        
        long sum = 0;
        long min = Long.MAX_VALUE;
        long max = 0;
        for (Long t : times) {
            sum += t;
            min = Math.min(min, t);
            max = Math.max(max, t);
        }
        long avg = sum / times.size();
        
        System.out.printf("  Avg: %d ms  Min: %d ms  Max: %d ms%n", avg, min, max);
        System.out.println();
    }
    
    static void runTest(String name, String method, int iterations, Object... params) throws Exception {
        System.out.println("--- " + name + " (" + iterations + " iterations) ---");
        
        // Build parameter string
        StringBuilder paramStr = new StringBuilder();
        paramStr.append("\"table\":\"" + TABLE_NAME + "\"");
        
        for (Object p : params) {
            if (p instanceof Integer) {
                paramStr.append(",\"value\":").append(p);
            } else if (p instanceof String) {
                paramStr.append(",\"category\":\"").append(p).append("\"");
            } else if (p instanceof Double) {
                paramStr.append(",\"amount\":").append(p);
            }
        }
        
        // Add method-specific params
        if (method.equals("searchRange")) {
            paramStr = new StringBuilder("\"table\":\"" + TABLE_NAME + "\",\"minValue\":" + params[0] + ",\"maxValue\":" + params[1]);
        } else if (method.equals("filterComplex")) {
            paramStr = new StringBuilder("\"table\":\"" + TABLE_NAME + "\",\"minRating\":" + params[0] + ",\"maxValue\":" + params[1]);
        } else if (method.equals("queryOrdered")) {
            paramStr = new StringBuilder("\"table\":\"" + TABLE_NAME + "\",\"limit\":" + params[0]);
        } else if (method.equals("bulkInsert")) {
            paramStr = new StringBuilder("\"table\":\"" + TABLE_NAME + "\",\"count\":" + params[0]);
        } else if (method.equals("versionHistoryTest")) {
            paramStr = new StringBuilder("\"table\":\"" + TABLE_NAME + "\",\"recordCount\":" + params[0] + ",\"updateCount\":" + params[1]);
        } else if (method.equals("queryHistory")) {
            paramStr = new StringBuilder("\"table\":\"" + TABLE_NAME + "\",\"recId\":" + params[0]);
        } else if (method.equals("queryHistoryTimeRange")) {
            paramStr = new StringBuilder("\"table\":\"" + TABLE_NAME + "\",\"hoursBack\":" + params[0]);
        }
        
        List<Long> times = new ArrayList<>();
        
        for (int i = 0; i < iterations; i++) {
            String requestBody = createRequest("Benchmark", method, paramStr.toString());
            
            long start = System.currentTimeMillis();
            String response = sendRequest(requestBody);
            long elapsed = System.currentTimeMillis() - start;
            
            // Try to extract elapsed from response
            long actualElapsed = extractElapsed(response, elapsed);
            times.add(actualElapsed);
            
            requestCount++;
        }
        
        // Calculate statistics
        long sum = 0;
        long min = Long.MAX_VALUE;
        long max = 0;
        for (Long t : times) {
            sum += t;
            min = Math.min(min, t);
            max = Math.max(max, t);
        }
        long avg = sum / times.size();
        
        System.out.printf("  Avg: %d ms  Min: %d ms  Max: %d ms%n", avg, min, max);
        System.out.println();
    }
    
    static void runConcurrentTest(String name, int numThreads, int opsPerThread) throws Exception {
        System.out.println("--- " + name + " ---");
        
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        AtomicInteger completed = new AtomicInteger(0);
        List<Long> times = Collections.synchronizedList(new ArrayList<>());
        
        long totalStart = System.currentTimeMillis();
        
        for (int i = 0; i < numThreads * opsPerThread; i++) {
            executor.submit(() -> {
                try {
                    String requestBody = createRequest("Benchmark", "searchExact", 
                        "\"table\":\"" + TABLE_NAME + "\",\"value\":" + (int)(Math.random() * 10000));
                    
                    long start = System.currentTimeMillis();
                    sendRequest(requestBody);
                    long elapsed = System.currentTimeMillis() - start;
                    
                    times.add(elapsed);
                    completed.incrementAndGet();
                    requestCount++;
                } catch (Exception e) {
                    System.err.println("Error: " + e.getMessage());
                }
            });
        }
        
        executor.shutdown();
        executor.awaitTermination(60, TimeUnit.SECONDS);
        
        long totalElapsed = System.currentTimeMillis() - totalStart;
        
        Collections.sort(times);
        long sum = 0;
        for (Long t : times) sum += t;
        long avg = sum / times.size();
        long p50 = times.get(times.size() / 2);
        long p95 = times.get((int)(times.size() * 0.95));
        long throughput = (long)(times.size() * 1000.0 / totalElapsed);
        
        System.out.printf("  Total: %d ms  Avg: %d ms  P50: %d ms  P95: %d ms  Throughput: %d ops/s%n", 
            totalElapsed, avg, p50, p95, throughput);
        System.out.println();
    }
    
    static long extractElapsed(String response, long fallback) {
        Pattern pattern = Pattern.compile("\"elapsed\":(\\d+)");
        Matcher matcher = pattern.matcher(response);
        if (matcher.find()) {
            return Long.parseLong(matcher.group(1));
        }
        return fallback;
    }
    
    static String createRequest(String className, String methodName, String params) {
        // For core methods (Login, etc.), use empty _class
        String actualClass = className.isEmpty() ? "" : className;
        return String.format(
            "{\"_class\":\"%s\",\"_method\":\"%s\",\"_uuid\":\"%s\",%s}",
            actualClass, methodName, sessionUuid, params
        );
    }
    
    static String sendRequest(String requestBody) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(SERVER_URL))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBody))
            .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }
}
