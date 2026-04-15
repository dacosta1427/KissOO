package services;

import koo.oodb.core.StorageManager;
import org.garret.perst.json.JSONObject;
import org.kissweb.database.Connection;
import org.kissweb.restServer.ProcessServlet;

/**
 * PerstService - REST endpoints for Perst OODBMS health and stats.
 * 
 * Exposes:
 * - /rest?service=PerstService&method=healthCheck
 * - /rest?service=PerstService&method=getStats
 */
public class PerstService {

    public void healthCheck(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        if (!StorageManager.isAvailable()) {
            outjson.put("status", "unavailable");
            outjson.put("message", "Perst is not enabled or not initialized");
            return;
        }
        
        java.util.Map<String, Object> health = StorageManager.healthCheck();
        for (java.util.Map.Entry<String, Object> entry : health.entrySet()) {
            outjson.put(entry.getKey(), entry.getValue());
        }
        outjson.put("status", "ok");
    }
    
    public void getStats(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        if (!StorageManager.isAvailable()) {
            outjson.put("status", "unavailable");
            outjson.put("message", "Perst is not enabled or not initialized");
            return;
        }
        
        java.util.Map<String, Object> stats = StorageManager.getStats();
        for (java.util.Map.Entry<String, Object> entry : stats.entrySet()) {
            outjson.put(entry.getKey(), entry.getValue());
        }
        outjson.put("status", "ok");
    }
}
