package koo.security;

import koo.oodb.core.StorageManager;
import org.garret.perst.continuous.CVersion;
import org.garret.perst.Indexable;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * EndpointRegistry - Manages endpoint-to-bit mapping for bitmap authorization.
 * 
 * Persisted in Perst. Each endpoint gets a unique bit position.
 * The registry is loaded on first access and saved when endpoints are added.
 * 
 * Usage:
 *   BigInteger bit = EndpointRegistry.getEndpointBit("services.CleaningService.getCleaners");
 *   boolean hasPermission = (agreement.getEndpointPermissions() & bit) != 0;
 */
public class EndpointRegistry extends CVersion {
    
    private static final String REGISTRY_KEY = "EndpointRegistrySingleton";
    private static EndpointRegistry instance;
    
    @Indexable
    private String name;  // Singleton - always "EndpointRegistry"
    
    // Map of endpoint name -> bit position (1-based, 0 = not found)
    private ConcurrentHashMap<String, BigInteger> endpoints;
    
    // Counter for next bit position (stored as String for Perst)
    private String nextBitPosition;
    
    // Transient: not persisted, used at runtime
    private transient AtomicInteger runtimeCounter;
    
    public EndpointRegistry() {
        this.name = REGISTRY_KEY;
        this.endpoints = new ConcurrentHashMap<>();
        this.nextBitPosition = "1";
        this.runtimeCounter = new AtomicInteger(1);
    }
    
    /**
     * Get singleton instance (creates if not exists)
     */
    public static synchronized EndpointRegistry getInstance() {
        if (instance == null) {
            instance = loadFromStorage();
            if (instance == null) {
                instance = new EndpointRegistry();
                saveToStorage(instance);
            }
        }
        return instance;
    }
    
    /**
     * Register a new endpoint, returning its bit position.
     * If endpoint already exists, returns existing bit.
     */
    public static BigInteger registerEndpoint(String endpointName) {
        EndpointRegistry registry = getInstance();
        
        // Check if already registered
        BigInteger existingBit = registry.endpoints.get(endpointName);
        if (existingBit != null) {
            return existingBit;
        }
        
        // Get next bit position
        int bitPos = registry.runtimeCounter.getAndIncrement();
        BigInteger newBit = BigInteger.valueOf(1).shiftLeft(bitPos - 1);
        
        // Add to map
        registry.endpoints.put(endpointName, newBit);
        registry.nextBitPosition = String.valueOf(bitPos + 1);
        
        // Save to Perst
        saveToStorage(registry);
        
        System.out.println("[EndpointRegistry] Registered: " + endpointName + " -> bit " + bitPos);
        
        return newBit;
    }
    
    /**
     * Get bit position for endpoint name
     */
    public static BigInteger getEndpointBit(String endpointName) {
        EndpointRegistry registry = getInstance();
        BigInteger bit = registry.endpoints.get(endpointName);
        return bit != null ? bit : BigInteger.ZERO;
    }
    
    /**
     * Check if endpoint is registered
     */
    public static boolean isRegistered(String endpointName) {
        return getInstance().endpoints.containsKey(endpointName);
    }
    
    /**
     * Get all registered endpoints (name -> bit)
     */
    public static Map<String, BigInteger> getAllEndpoints() {
        return new HashMap<>(getInstance().endpoints);
    }
    
    /**
     * Get count of registered endpoints
     */
    public static int getEndpointCount() {
        return getInstance().endpoints.size();
    }
    
    /**
     * Create bitmap from set of endpoint names
     */
    public static BigInteger createBitmapFromEndpoints(List<String> endpointNames) {
        BigInteger result = BigInteger.ZERO;
        for (String name : endpointNames) {
            BigInteger bit = getEndpointBit(name);
            if (bit != null && bit.signum() > 0) {
                result = result.or(bit);
            }
        }
        return result;
    }
    
    /**
     * Clear registry (for testing only!)
     */
    public static void clear() {
        EndpointRegistry registry = getInstance();
        registry.endpoints.clear();
        registry.runtimeCounter = new AtomicInteger(1);
        registry.nextBitPosition = "1";
        saveToStorage(registry);
    }
    
    // ========== Persistence ==========
    
    private static EndpointRegistry loadFromStorage() {
        try {
            // Try to find existing registry
            for (EndpointRegistry reg : StorageManager.getAll(EndpointRegistry.class)) {
                if (REGISTRY_KEY.equals(reg.getName())) {
                    reg.runtimeCounter = new AtomicInteger(reg.endpoints.size() + 1);
                    return reg;
                }
            }
        } catch (Exception e) {
            System.out.println("[EndpointRegistry] Load failed: " + e.getMessage());
        }
        return null;
    }
    
    private static void saveToStorage(EndpointRegistry registry) {
        try {
            var tc = StorageManager.createContainer();
            tc.addInsert(registry);
            StorageManager.store(tc);
        } catch (Exception e) {
            System.out.println("[EndpointRegistry] Save failed: " + e.getMessage());
        }
    }
    
    // ========== Getters ==========
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public Map<String, BigInteger> getEndpoints() { 
        // Return serializable map for Perst
        return new HashMap<>(endpoints);
    }
    
    public void setEndpoints(Map<String, BigInteger> endpoints) {
        this.endpoints = new ConcurrentHashMap<>(endpoints);
    }
    
    public String getNextBitPosition() { return nextBitPosition; }
    public void setNextBitPosition(String nextBitPosition) { this.nextBitPosition = nextBitPosition; }
    
    public List<EndpointInfo> getEndpointInfoList() {
        List<EndpointInfo> list = new ArrayList<>();
        for (Map.Entry<String, BigInteger> entry : endpoints.entrySet()) {
            list.add(new EndpointInfo(entry.getKey(), entry.getValue()));
        }
        Collections.sort(list);
        return list;
    }
    
    /**
     * Info class for listing endpoints
     */
    public static class EndpointInfo implements Comparable<EndpointInfo> {
        public String name;
        public BigInteger bit;
        
        public EndpointInfo(String name, BigInteger bit) {
            this.name = name;
            this.bit = bit;
        }
        
        @Override
        public int compareTo(EndpointInfo other) {
            return this.name.compareTo(other.name);
        }
    }
}