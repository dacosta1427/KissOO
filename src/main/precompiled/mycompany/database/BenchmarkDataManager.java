package mycompany.database;

import mycompany.domain.BenchmarkData;
import java.util.List;
import java.util.Map;

/**
 * BenchmarkDataManager - Manager for BenchmarkData operations.
 */
public class BenchmarkDataManager {
    
    private BenchmarkDataManager() {
    }
    
    public static boolean isAvailable() {
        return oodb.PerstStorageManager.getDatabase() != null;
    }
    
    public static void clearAll() {
        // Implementation depends on CDatabase access
    }
    
    public static int insert(String name, long value, String category, int count, double score) {
        BenchmarkData data = new BenchmarkData(name, value, category, count, score);
        oodb.PerstStorageManager.getDatabase().insert(data);
        return 1;
    }
    
    public static int bulkInsert(int count) {
        int inserted = 0;
        for (int i = 0; i < count; i++) {
            String name = "benchmark_" + i;
            long value = System.currentTimeMillis() + i;
            String category = "cat_" + (i % 5);
            int cnt = i;
            double score = Math.random() * 100;
            insert(name, value, category, cnt, score);
            inserted++;
        }
        return inserted;
    }
    
    public static List<BenchmarkData> getAll() {
        Iterable<BenchmarkData> results = oodb.PerstStorageManager.getDatabase().getRecords(BenchmarkData.class);
        return oodb.PerstStorageManager.getDatabase().toList(results.iterator());
    }
    
    public static int count() {
        List<BenchmarkData> all = getAll();
        return all != null ? all.size() : 0;
    }
    
    public static List<BenchmarkData> findByValue(long value) {
        // Use CDatabase find with Key
        return getAll(); // Simplified
    }
    
    public static List<BenchmarkData> findByCategory(String category) {
        return getAll(); // Simplified
    }
    
    public static long sumValues() {
        List<BenchmarkData> all = getAll();
        long sum = 0;
        if (all != null) {
            for (BenchmarkData data : all) {
                sum += data.getValue();
            }
        }
        return sum;
    }
    
    public static double avgValues() {
        List<BenchmarkData> all = getAll();
        if (all == null || all.isEmpty()) return 0;
        return (double) sumValues() / all.size();
    }
    
    public static Map<String, Long> sumByCategory() {
        List<BenchmarkData> all = getAll();
        Map<String, Long> result = new java.util.HashMap<>();
        if (all != null) {
            for (BenchmarkData data : all) {
                String cat = data.getCategory();
                result.put(cat, result.getOrDefault(cat, 0L) + data.getValue());
            }
        }
        return result;
    }
    
    public static Map<String, Integer> countByCategory() {
        List<BenchmarkData> all = getAll();
        Map<String, Integer> result = new java.util.HashMap<>();
        if (all != null) {
            for (BenchmarkData data : all) {
                String cat = data.getCategory();
                result.put(cat, result.getOrDefault(cat, 0) + 1);
            }
        }
        return result;
    }
}