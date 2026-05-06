package koo.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Configuration class for Perst integration.
 * Reads settings from application.ini
 */
public class PerstConfig {
    private static final String CONFIG_FILE = "WEB-INF/backend/application.ini";
    private static PerstConfig instance;
    
    private boolean perstEnabled = false;
    private boolean useCDatabase = true;  // Use CDatabase for versioning
    private boolean perstNoflush = false;  // Safe default - flush writes to disk
    private int perstOptimizeInterval = 86400;  // Default: optimize every 24 hours
    private String databasePath = "koo/";
    private String historyIndexPath = "koo.lex"; // Default path for history index
    private int pagePoolSize = 512 * 1024 * 1024; // 512MB default
    
    private PerstConfig() {
        loadConfig();
    }
    
    public static synchronized PerstConfig getInstance() {
        if (instance == null) {
            instance = new PerstConfig();
        }
        return instance;
    }
    
    public static synchronized void setInstance(PerstConfig testInstance) {
        instance = testInstance;
    }
    
    private void loadConfig() {
        Properties props = new Properties();
        String appPath = null;
        try {
            appPath = org.kissweb.restServer.MainServlet.getApplicationPath();
        } catch (Exception e) {
            System.out.println("[PerstConfig] Cannot get application path (not in web container), using defaults.");
        }
        
        String configPath = (appPath != null) ? appPath + "application.ini" : "application.ini";
        System.out.println("[PerstConfig] Trying to load config from: " + configPath);
        
        try (FileInputStream fis = new FileInputStream(configPath)) {
            props.load(fis);
            
            this.perstEnabled = "true".equalsIgnoreCase(props.getProperty("PerstEnabled", "false"));
            this.useCDatabase = "true".equalsIgnoreCase(props.getProperty("PerstUseCDatabase", "true"));
            this.perstNoflush = "true".equalsIgnoreCase(props.getProperty("PerstNoflush", "false"));
            this.perstOptimizeInterval = Integer.parseInt(props.getProperty("PerstOptimizeInterval", "86400"));
            String dbPath = props.getProperty("PerstDatabasePath", "koo/koo");
            String histPath = props.getProperty("PerstHistoryIndexPath", "koo.lex");
            this.pagePoolSize = Integer.parseInt(props.getProperty("PerstPagePoolSize", "536870912"));
            
            // Platform-agnostic path handling
            // Normalize path separators - convert all separators to standard Java File.separator
            String normalizedDbPath = dbPath.replace('/', java.io.File.separatorChar).replace('\\', java.io.File.separatorChar);
            String normalizedHistPath = histPath.replace('/', java.io.File.separatorChar).replace('\\', java.io.File.separatorChar);
            
// Convert relative path to absolute using application path
            if (appPath != null && !new java.io.File(normalizedDbPath).isAbsolute()) {
                // Normalize application path
                String normalizedAppPath = appPath.replace('/', java.io.File.separatorChar).replace('\\', java.io.File.separatorChar);
                if (!normalizedAppPath.endsWith(java.io.File.separator)) {
                    normalizedAppPath += java.io.File.separator;
                }
                // Combine and resolve to canonical path
                java.io.File combinedFile = new java.io.File(normalizedAppPath, normalizedDbPath);
                try {
                    this.databasePath = combinedFile.getCanonicalPath();
                } catch (Exception e) {
                    this.databasePath = combinedFile.getAbsolutePath();
                }
            } else {
                try {
                    this.databasePath = new java.io.File(normalizedDbPath).getCanonicalPath();
                } catch (Exception e) {
                    this.databasePath = new java.io.File(normalizedDbPath).getAbsolutePath();
                }
            }
            
            // Process history index path
            if (appPath != null && !new java.io.File(normalizedHistPath).isAbsolute()) {
                String normalizedAppPath = appPath.replace('/', java.io.File.separatorChar).replace('\\', java.io.File.separatorChar);
                if (!normalizedAppPath.endsWith(java.io.File.separator)) {
                    normalizedAppPath += java.io.File.separator;
                }
                java.io.File combinedHistFile = new java.io.File(normalizedAppPath, normalizedHistPath);
                try {
                    this.historyIndexPath = combinedHistFile.getCanonicalPath();
                } catch (Exception e) {
                    this.historyIndexPath = combinedHistFile.getAbsolutePath();
                }
            } else {
                try {
                    this.historyIndexPath = new java.io.File(normalizedHistPath).getCanonicalPath();
                } catch (Exception e) {
                    this.historyIndexPath = new java.io.File(normalizedHistPath).getAbsolutePath();
                }
            }
            
            // Ensure database directory exists
            try {
                java.io.File dbDir = new java.io.File(this.databasePath).getParentFile();
                if (dbDir != null && !dbDir.exists()) {
                    dbDir.mkdirs();
                    System.out.println("[PerstConfig] Created database directory: " + dbDir.getCanonicalPath());
                }
            } catch (Exception e) {
                System.out.println("[PerstConfig] Failed to create database directory: " + e.getMessage());
            }
            
            System.out.println("[PerstConfig] Perst Enabled: " + perstEnabled);
            System.out.println("[PerstConfig] Use CDatabase: " + useCDatabase);
            System.out.println("[PerstConfig] NoFlush: " + perstNoflush + " (true = faster but data loss risk on crash)");
            System.out.println("[PerstConfig] Optimize Interval: " + perstOptimizeInterval + " seconds (" + (perstOptimizeInterval/3600) + " hours)");
            System.out.println("[PerstConfig] Database Path: " + databasePath);
            System.out.println("[PerstConfig] History Index Path: " + historyIndexPath);
            
        } catch (IOException e) {
            System.out.println("[PerstConfig] Config file not found, using defaults. Perst disabled.");
        } catch (NumberFormatException e) {
            System.out.println("[PerstConfig] Invalid page pool size, using default: " + e.getMessage());
        }
    }
    
    public boolean isPerstEnabled() {
        System.out.println("[PerstConfig] isPerstEnabled called, perstEnabled=" + perstEnabled);
        return perstEnabled;
    }
    
    public boolean isUseCDatabase() {
        return useCDatabase;
    }
    
    public boolean isPerstNoflush() {
        return perstNoflush;
    }
    
    public int getPerstOptimizeInterval() {
        return perstOptimizeInterval;
    }
    
    public String getDatabasePath() {
        return databasePath;
    }
    
    public String getHistoryIndexPath() {
        return historyIndexPath;
    }
    
    public int getPagePoolSize() {
        return pagePoolSize;
    }
}
