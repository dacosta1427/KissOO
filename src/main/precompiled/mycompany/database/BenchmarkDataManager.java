package mycompany.database;

import mycompany.domain.BenchmarkData;
import org.garret.perst.dbmanager.TransactionContainer;
import java.util.List;
import java.util.Map;

/**
 * BenchmarkDataManager - Manager for BenchmarkData operations.
 * 
 * All operations use TransactionContainer for atomicity.
 */
public class BenchmarkDataManager {
    
    private BenchmarkDataManager() {
    }
    
    public static boolean isAvailable() {
        return oodb.PerstStorageManager.isAvailable();
    }
    
    public static void clearAll() {
    }
    
    public static int insert(String name, long value, String category, int count, double score) {
        BenchmarkData data = new BenchmarkData(name, value, category, count, score);
        
        TransactionContainer tc = oodb.PerstStorageManager.createContainer();
        tc.addInsert(data);
        if (oodb.PerstStorageManager.store(tc)) {
            return 1;
        }
        return 0;
    }
    
    public static int bulkInsert(int count) {
        int inserted = 0;
        TransactionContainer tc = oodb.PerstStorageManager.createContainer();
        
        for (int i = 0; i < count; i++) {
            String name = "benchmark_" + i;
            long value = System.currentTimeMillis() + i;
            String category = "cat_" + (i % 5);
            int cnt = i;
            double score = Math.random() * 100;
            BenchmarkData data = new BenchmarkData(name, value, category, cnt, score);
            tc.addInsert(data);
            inserted++;
        }
        
        if (oodb.PerstStorageManager.store(tc)) {
            return inserted;
        }
        return 0;
    }
    
    public static List<BenchmarkData> getAll() {
        return oodb.PerstStorageManager.getAll(BenchmarkData.class);
    }
    
    public static int count() {
        List<BenchmarkData> all = getAll();
        return all != null ? all.size() : 0;
    }
    
    public static List<BenchmarkData> findByValue(long value) {
        return getAll();
    }
    
    public static List<BenchmarkData> findByCategory(String category) {
        return getAll();
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
