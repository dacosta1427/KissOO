package koo.lombok;

import org.kissweb.json.JSONObject;
import org.kissweb.json.JSONArray;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * Simplified JSONUtils for testing without Perst dependencies
 */
public class TestJSONUtils {
    
    /**
     * Convert a collection of objects to a JSON array,
     * filtering out deleted objects.
     */
    public static JSONArray toJSONArray(Collection<?> objects) {
        JSONArray jsonArray = new JSONArray();
        if (objects != null) {
            for (Object obj : objects) {
                if (obj != null) {
                    // Check if object has isDeleted method (simplified check)
                    try {
                        boolean deleted = (Boolean) obj.getClass().getMethod("isDeleted").invoke(obj);
                        if (!deleted) {
                            // Check if object has toJSON method
                            try {
                                JSONObject json = (JSONObject) obj.getClass().getMethod("toJSON").invoke(obj);
                                jsonArray.put(json);
                            } catch (Exception e) {
                                // If no toJSON method, create a simple representation
                                JSONObject simpleJson = new JSONObject();
                                simpleJson.put("name", obj.getClass().getSimpleName());
                                simpleJson.put("oid", 0);
                                jsonArray.put(simpleJson);
                            }
                        }
                    } catch (Exception e) {
                        // If no isDeleted method, include the object
                        try {
                            JSONObject json = (JSONObject) obj.getClass().getMethod("toJSON").invoke(obj);
                            jsonArray.put(json);
                        } catch (Exception ex) {
                            // If no toJSON method, create a simple representation
                            JSONObject simpleJson = new JSONObject();
                            simpleJson.put("name", obj.getClass().getSimpleName());
                            simpleJson.put("oid", 0);
                            jsonArray.put(simpleJson);
                        }
                    }
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
            .filter(obj -> obj != null)
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
}