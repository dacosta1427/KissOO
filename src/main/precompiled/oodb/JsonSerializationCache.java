package oodb;

import org.garret.perst.continuous.CVersion;
import oodb.json.JSONObject;
import oodb.json.JSONArray;
import oodb.annotations.JsonIgnore;
import oodb.annotations.JsonIncludeObject;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Generic JSON serialization cache for Perst entities.
 * 
 * Uses reflection to scan entity classes at initialization time and caches
 * field metadata for fast serialization/deserialization at runtime.
 * 
 * Features:
 * - Automatic field discovery via reflection
 * - OO references (CVersion) serialized as OIDs by default
 * - @JsonIncludeObject for inline serialization of referenced objects
 * - @JsonIgnore to exclude fields
 * - Cached MethodHandles for fast getter/setter access
 * 
 * Usage:
 * <pre>
 * // At startup (in KissInit.groovy)
 * JsonSerializationCache.initialize(Cleaner.class, House.class, ...);
 * 
 * // In entities (via KissCVersion)
 * Cleaner cleaner = new Cleaner();
 * JSONObject json = cleaner.toJSON();
 * 
 * // Direct usage
 * JSONObject json = JsonSerializationCache.toJSON(someEntity);
 * JSONArray array = JsonSerializationCache.toJSONArray(listOfEntities);
 * </pre>
 */
public class JsonSerializationCache {
    
    // Cache: Class -> EntityMetadata
    private static final Map<Class<?>, EntityMetadata> cache = new ConcurrentHashMap<>();
    
    private static boolean initialized = false;
    
    /**
     * Initialize the cache by scanning all provided entity classes.
     * Should be called once at startup (in KissInit.groovy).
     * 
     * @param entityClasses Classes to scan and cache
     */
    public static synchronized void initialize(Class<?>... entityClasses) {
        if (initialized) {
            return;
        }
        
        for (Class<?> clazz : entityClasses) {
            if (CVersion.class.isAssignableFrom(clazz)) {
                cache.put(clazz, analyzeClass(clazz));
            }
        }
        
        initialized = true;
    }
    
    /**
     * Serialize an entity to JSON.
     * 
     * @param entity The entity to serialize (must extend CVersion)
     * @return JSONObject representation
     */
    public static JSONObject toJSON(Object entity) {
        if (entity == null) {
            return null;
        }
        
        Class<?> clazz = entity.getClass();
        EntityMetadata metadata = cache.get(clazz);
        
        if (metadata == null) {
            // Auto-register class if not in cache
            metadata = analyzeClass(clazz);
            cache.put(clazz, metadata);
        }
        
        JSONObject json = new JSONObject();
        
        // Always include id (OID)
        if (entity instanceof CVersion) {
            json.put("id", ((CVersion) entity).getOid());
        }
        
        // Serialize each field
        for (FieldInfo field : metadata.fields) {
            try {
                Object value = field.getter.invoke(entity);
                
                if (value == null) {
                    // Skip null values
                    continue;
                }
                
                if (field.isEntityReference && !field.includeObject) {
                    // Serialize as OID
                    if (value instanceof CVersion) {
                        json.put(field.jsonName, ((CVersion) value).getOid());
                    } else {
                        json.put(field.jsonName, value);
                    }
                } else if (field.isEntityReference && field.includeObject) {
                    // Serialize inline (recursive)
                    json.put(field.jsonName, toJSON(value));
                } else if (value instanceof Collection) {
                    // Handle collections
                    JSONArray arr = new JSONArray();
                    for (Object item : (Collection<?>) value) {
                        if (item instanceof CVersion) {
                            arr.put(((CVersion) item).getOid());
                        } else {
                            arr.put(item);
                        }
                    }
                    json.put(field.jsonName, arr);
                } else {
                    // Regular value
                    json.put(field.jsonName, value);
                }
            } catch (Exception e) {
                // Skip fields that can't be read
                System.err.println("[JsonSerializationCache] Error serializing field " + field.jsonName + ": " + e.getMessage());
            }
        }
        
        return json;
    }
    
    /**
     * Update an entity from JSON.
     * Only sets fields that exist in the JSON and have matching setters.
     * 
     * @param entity The entity to update
     * @param json The JSON data
     * @return The updated entity (same instance)
     */
    @SuppressWarnings("unchecked")
    public static <T> T fromJSON(T entity, JSONObject json) {
        if (entity == null || json == null) {
            return entity;
        }
        
        Class<?> clazz = entity.getClass();
        EntityMetadata metadata = cache.get(clazz);
        
        if (metadata == null) {
            metadata = analyzeClass(clazz);
            cache.put(clazz, metadata);
        }
        
        for (String key : json.keySet()) {
            if ("id".equals(key)) {
                continue; // Skip id - managed by Perst
            }
            
            FieldInfo field = metadata.fieldByName.get(key);
            if (field == null || field.setter == null) {
                continue; // No matching field or setter
            }
            
            try {
                Object value = json.get(key);
                
                if (value == null) {
                    field.setter.invoke(entity, (Object) null);
                    continue;
                }
                
                if (field.isEntityReference && value instanceof Number) {
                    // Load referenced entity by OID
                    long oid = ((Number) value).longValue();
                    Object referenced = PerstStorageManager.getByOid((Class<CVersion>) field.type, oid);
                    field.setter.invoke(entity, referenced);
                } else if (field.type == Integer.class || field.type == int.class) {
                    field.setter.invoke(entity, ((Number) value).intValue());
                } else if (field.type == Long.class || field.type == long.class) {
                    field.setter.invoke(entity, ((Number) value).longValue());
                } else if (field.type == Double.class || field.type == double.class) {
                    field.setter.invoke(entity, ((Number) value).doubleValue());
                } else if (field.type == Float.class || field.type == float.class) {
                    field.setter.invoke(entity, ((Number) value).floatValue());
                } else if (field.type == Boolean.class || field.type == boolean.class) {
                    field.setter.invoke(entity, value);
                } else if (field.type == String.class) {
                    field.setter.invoke(entity, value.toString());
                } else {
                    field.setter.invoke(entity, value);
                }
            } catch (Exception e) {
                System.err.println("[JsonSerializationCache] Error deserializing field " + key + ": " + e.getMessage());
            }
        }
        
        return entity;
    }
    
    /**
     * Serialize a collection of entities to JSON array.
     * 
     * @param entities Collection of entities
     * @return JSONArray of serialized entities
     */
    public static <T> JSONArray toJSONArray(Collection<T> entities) {
        JSONArray arr = new JSONArray();
        if (entities == null) {
            return arr;
        }
        for (T entity : entities) {
            JSONObject json = toJSON(entity);
            if (json != null) {
                arr.put(json);
            }
        }
        return arr;
    }
    
    /**
     * Check if the cache has been initialized.
     */
    public static boolean isInitialized() {
        return initialized;
    }
    
    /**
     * Get metadata for a specific class.
     * Returns null if class not in cache.
     */
    public static EntityMetadata getMetadata(Class<?> clazz) {
        return cache.get(clazz);
    }
    
    /**
     * Analyze a class and build field metadata.
     */
    private static EntityMetadata analyzeClass(Class<?> clazz) {
        EntityMetadata metadata = new EntityMetadata();
        metadata.entityClass = clazz;
        metadata.fields = new ArrayList<>();
        metadata.fieldByName = new HashMap<>();
        
        // Get all fields (including inherited)
        List<Field> allFields = getAllFields(clazz);
        
        for (Field field : allFields) {
            // Skip static and transient fields
            if (Modifier.isStatic(field.getModifiers()) || Modifier.isTransient(field.getModifiers())) {
                continue;
            }
            
            // Check for @JsonIgnore
            if (field.isAnnotationPresent(JsonIgnore.class)) {
                continue;
            }
            
            FieldInfo info = new FieldInfo();
            info.field = field;
            info.jsonName = field.getName();
            info.type = field.getType();
            info.isEntityReference = CVersion.class.isAssignableFrom(field.getType());
            info.includeObject = field.isAnnotationPresent(JsonIncludeObject.class);
            
            // Find getter
            info.getter = findGetter(clazz, field.getName());
            if (info.getter == null) {
                info.getter = findBooleanGetter(clazz, field.getName());
            }
            
            // Find setter
            info.setter = findSetter(clazz, field.getName(), field.getType());
            
            // Only include if we have a getter
            if (info.getter != null) {
                metadata.fields.add(info);
                metadata.fieldByName.put(info.jsonName, info);
            }
        }
        
        return metadata;
    }
    
    /**
     * Get all fields including inherited ones.
     */
    private static List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        Class<?> current = clazz;
        
        while (current != null && current != Object.class) {
            fields.addAll(Arrays.asList(current.getDeclaredFields()));
            current = current.getSuperclass();
        }
        
        return fields;
    }
    
    /**
     * Find getter method for a field (getFieldXxx or isXxx for boolean).
     */
    private static Method findGetter(Class<?> clazz, String fieldName) {
        String capitalized = capitalize(fieldName);
        
        // Try getFieldName()
        try {
            return clazz.getMethod("get" + capitalized);
        } catch (NoSuchMethodException e) {
            // Try field() for public fields
            try {
                Field field = clazz.getField(fieldName);
                if (Modifier.isPublic(field.getModifiers())) {
                    return null; // Will use field.get() directly
                }
            } catch (NoSuchFieldException ex) {
                // Ignore
            }
        }
        
        return null;
    }
    
    /**
     * Find boolean getter (isFieldName).
     */
    private static Method findBooleanGetter(Class<?> clazz, String fieldName) {
        String capitalized = capitalize(fieldName);
        try {
            return clazz.getMethod("is" + capitalized);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }
    
    /**
     * Find setter method for a field.
     */
    private static Method findSetter(Class<?> clazz, String fieldName, Class<?> fieldType) {
        String capitalized = capitalize(fieldName);
        
        try {
            return clazz.getMethod("set" + capitalized, fieldType);
        } catch (NoSuchMethodException e) {
            // Try with primitive/wrapper alternatives
            if (fieldType == Integer.class) {
                try { return clazz.getMethod("set" + capitalized, int.class); } catch (NoSuchMethodException ex) { }
            } else if (fieldType == int.class) {
                try { return clazz.getMethod("set" + capitalized, Integer.class); } catch (NoSuchMethodException ex) { }
            } else if (fieldType == Long.class) {
                try { return clazz.getMethod("set" + capitalized, long.class); } catch (NoSuchMethodException ex) { }
            } else if (fieldType == long.class) {
                try { return clazz.getMethod("set" + capitalized, Long.class); } catch (NoSuchMethodException ex) { }
            } else if (fieldType == Double.class) {
                try { return clazz.getMethod("set" + capitalized, double.class); } catch (NoSuchMethodException ex) { }
            } else if (fieldType == double.class) {
                try { return clazz.getMethod("set" + capitalized, Double.class); } catch (NoSuchMethodException ex) { }
            } else if (fieldType == Boolean.class) {
                try { return clazz.getMethod("set" + capitalized, boolean.class); } catch (NoSuchMethodException ex) { }
            } else if (fieldType == boolean.class) {
                try { return clazz.getMethod("set" + capitalized, Boolean.class); } catch (NoSuchMethodException ex) { }
            }
        }
        return null;
    }
    
    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
    
    /**
     * Metadata for a scanned entity class.
     */
    public static class EntityMetadata {
        public Class<?> entityClass;
        public List<FieldInfo> fields;
        public Map<String, FieldInfo> fieldByName;
    }
    
    /**
     * Metadata for a single field.
     */
    public static class FieldInfo {
        public String jsonName;
        public Field field;
        public Method getter;
        public Method setter;
        public Class<?> type;
        public boolean isEntityReference;  // type extends CVersion
        public boolean includeObject;      // @JsonIncludeObject present
    }
}
