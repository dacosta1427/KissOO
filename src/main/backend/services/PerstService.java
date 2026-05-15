package services;

import koo.config.PerstConfig;
import org.garret.perst.dbmanager.UnifiedDBManager;
import org.garret.perst.dbmanager.MemoryStats;
import org.garret.perst.json.JSONObject;
import org.kissweb.database.Connection;
import org.kissweb.restServer.ProcessServlet;
import org.kissweb.restServer.MainServlet;

/**
 * PerstService - REST endpoints for Perst OODBMS health and stats.
 */
public class PerstService {

    private static UnifiedDBManager getUdbm() {
        return (UnifiedDBManager) MainServlet.getEnvironment("unifiedDBManager");
    }

    private static boolean isPerstAvailable() {
        return getUdbm() != null;
    }

    public void healthCheck(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        if (!isPerstAvailable()) {
            outjson.put("status", "unavailable");
            outjson.put("message", "Perst is not enabled or not initialized");
            return;
        }

        UnifiedDBManager udbm = getUdbm();
        outjson.put("initialized", true);
        outjson.put("perstEnabled", PerstConfig.getInstance().isPerstEnabled());
        outjson.put("available", true);

        try {
            outjson.put("inTransaction", udbm.isInTransaction());
            outjson.put("historyBufferSize", udbm.getHistoryBufferSize());
            outjson.put("databasePath", PerstConfig.getInstance().getDatabasePath());
            outjson.put("status", "ok");
        } catch (Exception e) {
            outjson.put("status", "error");
            outjson.put("error", e.getMessage());
        }
    }

    public void getStats(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        if (!isPerstAvailable()) {
            outjson.put("status", "unavailable");
            outjson.put("message", "Perst is not enabled or not initialized");
            return;
        }

        UnifiedDBManager udbm = getUdbm();
        try {
            MemoryStats memStats = udbm.getMemoryStats();
            outjson.put("usedMemory", memStats.getUsedMemory());
            outjson.put("maxMemory", memStats.getMaxMemory());
            outjson.put("loadedObjects", memStats.getLoadedObjects());
            outjson.put("lazyLoadedCollections", memStats.getLazyLoadedCollections());
            outjson.put("usagePercent", memStats.getUsagePercent());
            outjson.put("status", "ok");
        } catch (Exception e) {
            outjson.put("status", "error");
            outjson.put("error", e.getMessage());
        }
    }
}