package org.kissweb.lombok;

import org.kissweb.json.JSONObject;
import org.kissweb.json.JSONArray;
import org.garret.perst.continuous.CVersion;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * Utility methods for JSON serialization of objects and collections.
 * Provides automatic JSON generation based on @toJSON annotations.
 */
public class JSONUtils {
    
    /**
     * Convert an object to JSON using reflection and @toJSON annotation.
     */
    public static JSONObject toJSON(Object obj) {
        if (obj == null) {
            return new JSONObject();
        }
        
        JSONObject json = new JSONObject();
        toJSON annotation = obj.getClass().getAnnotation(toJSON.class);
        
        // If no @toJSON annotation, include basic fields
        if (annotation == null) {
            json.put("className", obj.getClass().getSimpleName());
            json.put("oid", getOid(obj));
            return json;
        }
        
        // Include specified fields
        String[] includeFields = annotation.includeFields();
        String[] excludeFields = annotation.excludeFields();
        boolean includeNulls = annotation.includeNulls();
        
        for (Method method : obj.getClass().getMethods()) {
            String methodName = method.getName();
            
            // Check if it's a getter method
            if (methodName.startsWith("get") && methodName.length() > 3 && 
                method.getParameterCount() == 0 && method.getReturnType() != void.class) {
                
                String fieldName = methodName.substring(3);
                fieldName = Character.toLowerCase(fieldName.charAt(0)) + fieldName.substring(1);
                
                // Check field inclusion/exclusion
                if (shouldIncludeField(fieldName, includeFields, excludeFields)) {
                    try {
                        Object value = method.invoke(obj);
                        if (value != null || includeNulls) {
                            json.put(fieldName, value);
                        }
                    } catch (Exception e) {
                        // Skip problematic fields
                    }
                }
            }
        }
        
        return json;
    }
    
    /**
     * Convert a collection of objects to a JSON array,
     * filtering out deleted objects that extend CVersion.
     */
    public static JSONArray toJSONArray(Collection<?> objects) {
        JSONArray jsonArray = new JSONArray();
        if (objects != null) {
            for (Object obj : objects) {
                if (obj != null && !isDeleted(obj)) {
                    jsonArray.put(toJSON(obj));
                }
            }
        }
        return jsonArray;
    }
    
    /**
     * Convert a collection to a standardized API response format.
     */
    public static JSONObject toCollectionResponse(Collection<?> objects) {
        JSONArray jsonArray = toJSONArray(objects);
        JSONObject response = new JSONObject();
        response.put("_Success", true);
        response.put("data", jsonArray);
        response.put("count", jsonArray.length());
        return response;
    }
    
    /**
     * Convert a collection to a paginated response format.
     */
    public static JSONObject toPaginatedResponse(Collection<?> objects, 
                                              int page, int pageSize) {
        List<?> filtered = objects.stream()
            .filter(obj -> obj != null && !isDeleted(obj))
            .collect(Collectors.toList());
        
        int start = page * pageSize;
        int end = Math.min(start + pageSize, filtered.size());
        List<?> pageData = filtered.subList(start, end);
        
        JSONArray jsonArray = toJSONArray(pageData);
        JSONObject response = new JSONObject();
        response.put("_Success", true);
        response.put("data", jsonArray);
        response.put("page", page);
        response.put("pageSize", pageSize);
        response.put("total", filtered.size());
        return response;
    }
    
    /**
     * Check if an object should be included in JSON serialization.
     */
    private static boolean shouldIncludeField(String fieldName, String[] includeFields, String[] excludeFields) {
        // If includeFields is specified, only include those fields
        if (includeFields.length > 0) {
            for (String field : includeFields) {
                if (field.equals(fieldName)) {
                    return true;
                }
            }
            return false;
        }
        
        // Always exclude certain fields
        for (String field : excludeFields) {
            if (field.equals(fieldName)) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Check if an object is deleted (for CVersion objects).
     */
    private static boolean isDeleted(Object obj) {
        if (obj instanceof CVersion) {
            try {
                Method isDeleted = obj.getClass().getMethod("isDeleted");
                return (Boolean) isDeleted.invoke(obj);
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }
    
    /**
     * Get the OID of an object (for persistent objects).
     */
    private static long getOid(Object obj) {
        if (obj instanceof CVersion) {
            try {
                Method getOid = obj.getClass().getMethod("getOid");
                return (Long) getOid.invoke(obj);
            } catch (Exception e) {
                return 0;
            }
        }
        return 0;
    }
}