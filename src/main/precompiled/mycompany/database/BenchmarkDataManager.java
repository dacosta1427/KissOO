package mycompany.database;

import mycompany.domain.BenchmarkData;
import mycompany.domain.CDatabaseRoot;
import oodb.PerstStorageManager;
import java.util.*;

/**
 * BenchmarkDataManager - Manages BenchmarkData for performance testing.
 */
public class BenchmarkDataManager {
    
    private BenchmarkDataManager() {}
    
    public static boolean isAvailable() {
        return PerstStorageManager.isAvailable();
    }
    
    public static void clearAll() {
        if (!isAvailable()) return;
        
        PerstStorageManager.beginTransaction();
        try {
            CDatabaseRoot root = PerstStorageManager.getRoot();
            List<BenchmarkData> all = new ArrayList<>();
            for (BenchmarkData bd : root.benchmarkIndex) {
                all.add(bd);
            }
            for (BenchmarkData bd : all) {
                root.benchmarkIndex.remove(bd);
            }
            PerstStorageManager.commitTransaction();
        } catch (Exception e) {
            PerstStorageManager.rollbackTransaction();
        }
    }
    
    public static int insert(String name, long value, String category, int rating, double amount) {
        if (!isAvailable()) return 0;
        
        BenchmarkData data = new BenchmarkData(name, value, category, rating, amount);
        
        PerstStorageManager.beginTransaction();
        try {
            CDatabaseRoot root = PerstStorageManager.getRoot();
            root.benchmarkIndex.put(data);
            PerstStorageManager.commitTransaction();
            return 1;
        } catch (Exception e) {
            PerstStorageManager.rollbackTransaction();
            return 0;
        }
    }
    
    public static int bulkInsert(int count) {
        if (!isAvailable()) return 0;
        
        Random rand = new Random();
        String[] categories = {"A", "B", "C", "D", "E"};
        
        PerstStorageManager.beginTransaction();
        try {
            CDatabaseRoot root = PerstStorageManager.getRoot();
            for (int i = 0; i < count; i++) {
                BenchmarkData data = new BenchmarkData(
                    "Item_" + i,
                    rand.nextLong() % 100000,
                    categories[rand.nextInt(categories.length)],
                    rand.nextInt(5) + 1,
                    rand.nextDouble() * 1000
                );
                root.benchmarkIndex.put(data);
            }
            PerstStorageManager.commitTransaction();
            return count;
        } catch (Exception e) {
            PerstStorageManager.rollbackTransaction();
            return 0;
        }
    }
    
    public static List<BenchmarkData> getAll() {
        List<BenchmarkData> result = new ArrayList<>();
        if (!isAvailable()) return result;
        
        CDatabaseRoot root = PerstStorageManager.getRoot();
        for (BenchmarkData bd : root.benchmarkIndex) {
            result.add(bd);
        }
        return result;
    }
    
    public static int count() {
        if (!isAvailable()) return 0;
        
        int count = 0;
        CDatabaseRoot root = PerstStorageManager.getRoot();
        for (BenchmarkData bd : root.benchmarkIndex) {
            count++;
        }
        return count;
    }
    
    public static List<BenchmarkData> findByValue(long searchValue) {
        List<BenchmarkData> result = new ArrayList<>();
        if (!isAvailable()) return result;
        
        CDatabaseRoot root = PerstStorageManager.getRoot();
        for (BenchmarkData bd : root.benchmarkIndex) {
            if (bd.getValue() == searchValue) {
                result.add(bd);
            }
        }
        return result;
    }
    
    public static List<BenchmarkData> findByCategory(String category) {
        List<BenchmarkData> result = new ArrayList<>();
        if (!isAvailable()) return result;
        
        CDatabaseRoot root = PerstStorageManager.getRoot();
        for (BenchmarkData bd : root.benchmarkIndex) {
            if (category.equals(bd.getCategory())) {
                result.add(bd);
            }
        }
        return result;
    }
    
    public static long sumValues() {
        if (!isAvailable()) return 0;
        
        long sum = 0;
        CDatabaseRoot root = PerstStorageManager.getRoot();
        for (BenchmarkData bd : root.benchmarkIndex) {
            sum += bd.getValue();
        }
        return sum;
    }
    
    public static double avgValues() {
        if (!isAvailable()) return 0;
        
        int count = count();
        if (count == 0) return 0;
        return (double) sumValues() / count;
    }
    
    public static Map<String, Long> sumByCategory() {
        Map<String, Long> result = new HashMap<>();
        if (!isAvailable()) return result;
        
        CDatabaseRoot root = PerstStorageManager.getRoot();
        for (BenchmarkData bd : root.benchmarkIndex) {
            String cat = bd.getCategory();
            result.put(cat, result.getOrDefault(cat, 0L) + bd.getValue());
        }
        return result;
    }
    
    public static Map<String, Integer> countByCategory() {
        Map<String, Integer> result = new HashMap<>();
        if (!isAvailable()) return result;
        
        CDatabaseRoot root = PerstStorageManager.getRoot();
        for (BenchmarkData bd : root.benchmarkIndex) {
            String cat = bd.getCategory();
            result.put(cat, result.getOrDefault(cat, 0) + 1);
        }
        return result;
    }
}
