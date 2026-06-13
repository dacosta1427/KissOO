package koo.framework.proto;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class ProtoRegistry {
    private static final Map<String, ProtoSchema> registry = new ConcurrentHashMap<>();
    
    public static void registerSchema(String className, ProtoSchema schema) {
        registry.put(className, schema);
    }
    
    public static ProtoSchema getSchema(String className) {
        return registry.get(className);
    }
    
    public static ProtoSchema getSchema(Class<?> clazz) {
        return registry.get(clazz.getName());
    }
    
    public static boolean hasSchema(String className) {
        return registry.containsKey(className);
    }
    
    public static void clearRegistry() {
        registry.clear();
    }
}