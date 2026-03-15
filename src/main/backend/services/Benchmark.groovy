package services

import org.kissweb.json.JSONArray
import org.kissweb.json.JSONObject
import org.kissweb.database.Connection
import org.kissweb.restServer.ProcessServlet
import mycompany.database.BenchmarkDataManager
import mycompany.domain.BenchmarkData

/**
 * Benchmark service for Perst performance testing.
 */
class Benchmark {

    void setupTable(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        if (!BenchmarkDataManager.isAvailable()) {
            outjson.put("error", "Perst not available")
            return
        }
        
        BenchmarkDataManager.clearAll()
        outjson.put("success", true)
        outjson.put("message", "Benchmark data cleared")
    }

    void teardownTable(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        if (!BenchmarkDataManager.isAvailable()) {
            outjson.put("error", "Perst not available")
            return
        }
        
        BenchmarkDataManager.clearAll()
        outjson.put("success", true)
        outjson.put("message", "Benchmark data cleared")
    }

    void bulkInsert(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        if (!BenchmarkDataManager.isAvailable()) {
            outjson.put("error", "Perst not available")
            return
        }
        
        int count = injson.optInt("count", 100)
        long start = System.currentTimeMillis()
        
        int inserted = BenchmarkDataManager.bulkInsert(count)
        
        long elapsed = System.currentTimeMillis() - start
        
        outjson.put("success", true)
        outjson.put("count", inserted)
        outjson.put("elapsed", elapsed)
        outjson.put("rate", inserted > 0 ? (inserted * 1000.0 / elapsed) : 0)
    }

    void bulkUpdate(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        if (!BenchmarkDataManager.isAvailable()) {
            outjson.put("error", "Perst not available")
            return
        }
        
        List<BenchmarkData> all = BenchmarkDataManager.getAll()
        long start = System.currentTimeMillis()
        
        int updated = 0
        for (BenchmarkData bd : all) {
            bd.setValue(bd.getValue() + 1)
            updated++
        }
        
        long elapsed = System.currentTimeMillis() - start
        
        outjson.put("success", true)
        outjson.put("count", updated)
        outjson.put("elapsed", elapsed)
    }

    void bulkDelete(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        if (!BenchmarkDataManager.isAvailable()) {
            outjson.put("error", "Perst not available")
            return
        }
        
        List<BenchmarkData> all = BenchmarkDataManager.getAll()
        int total = all.size()
        
        long start = System.currentTimeMillis()
        
        BenchmarkDataManager.clearAll()
        
        long elapsed = System.currentTimeMillis() - start
        
        outjson.put("success", true)
        outjson.put("count", total)
        outjson.put("elapsed", elapsed)
    }

    void selectAll(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        if (!BenchmarkDataManager.isAvailable()) {
            outjson.put("error", "Perst not available")
            return
        }
        
        long start = System.currentTimeMillis()
        
        List<BenchmarkData> all = BenchmarkDataManager.getAll()
        
        long elapsed = System.currentTimeMillis() - start
        
        outjson.put("success", true)
        outjson.put("count", all.size())
        outjson.put("elapsed", elapsed)
    }

    void selectWhere(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        if (!BenchmarkDataManager.isAvailable()) {
            outjson.put("error", "Perst not available")
            return
        }
        
        long searchValue = injson.optLong("value", 0)
        String category = injson.optString("category", "")
        
        long start = System.currentTimeMillis()
        
        List<BenchmarkData> results
        if (searchValue != 0) {
            results = BenchmarkDataManager.findByValue(searchValue)
        } else if (!category.isEmpty()) {
            results = BenchmarkDataManager.findByCategory(category)
        } else {
            results = BenchmarkDataManager.getAll()
        }
        
        long elapsed = System.currentTimeMillis() - start
        
        outjson.put("success", true)
        outjson.put("count", results.size())
        outjson.put("elapsed", elapsed)
    }

    void aggregateSum(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        if (!BenchmarkDataManager.isAvailable()) {
            outjson.put("error", "Perst not available")
            return
        }
        
        long start = System.currentTimeMillis()
        
        long sum = BenchmarkDataManager.sumValues()
        
        long elapsed = System.currentTimeMillis() - start
        
        outjson.put("success", true)
        outjson.put("sum", sum)
        outjson.put("elapsed", elapsed)
    }

    void aggregateAvg(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        if (!BenchmarkDataManager.isAvailable()) {
            outjson.put("error", "Perst not available")
            return
        }
        
        long start = System.currentTimeMillis()
        
        double avg = BenchmarkDataManager.avgValues()
        
        long elapsed = System.currentTimeMillis() - start
        
        outjson.put("success", true)
        outjson.put("avg", avg)
        outjson.put("elapsed", elapsed)
    }

    void aggregateGroupBy(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        if (!BenchmarkDataManager.isAvailable()) {
            outjson.put("error", "Perst not available")
            return
        }
        
        String groupBy = injson.optString("groupBy", "category")
        
        long start = System.currentTimeMillis()
        
        Map<String, Long> sums = BenchmarkDataManager.sumByCategory()
        Map<String, Integer> counts = BenchmarkDataManager.countByCategory()
        
        long elapsed = System.currentTimeMillis() - start
        
        JSONObject results = new JSONObject()
        for (String key : sums.keySet()) {
            JSONObject row = new JSONObject()
            row.put("category", key)
            row.put("sum", sums.get(key))
            row.put("count", counts.get(key))
            results.put(key, row)
        }
        
        outjson.put("success", true)
        outjson.put("results", results)
        outjson.put("elapsed", elapsed)
    }

    void countRecords(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        if (!BenchmarkDataManager.isAvailable()) {
            outjson.put("error", "Perst not available")
            return
        }
        
        long start = System.currentTimeMillis()
        
        int count = BenchmarkDataManager.count()
        
        long elapsed = System.currentTimeMillis() - start
        
        outjson.put("success", true)
        outjson.put("count", count)
        outjson.put("elapsed", elapsed)
    }
}
