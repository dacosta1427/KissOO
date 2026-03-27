package oodb.json;

import java.util.LinkedHashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Minimal JSON object for Perst serialization.
 * 
 * Just enough for entity serialization - simple and elegant.
 * ~200 lines vs 2000+ lines in full org.json.JSONObject.
 */
public class JSONObject {
    
    private final Map<String, Object> map = new LinkedHashMap<>();
    
    public JSONObject() {
    }
    
    public JSONObject put(String key, Object value) {
        if (key == null) {
            throw new JSONException("Key cannot be null");
        }
        map.put(key, value);
        return this;
    }
    
    public Object get(String key) {
        if (!map.containsKey(key)) {
            throw new JSONException("Key not found: " + key);
        }
        return map.get(key);
    }
    
    public Object opt(String key) {
        return opt(key, null);
    }
    
    public Object opt(String key, Object defaultValue) {
        Object value = map.get(key);
        return value != null ? value : defaultValue;
    }
    
    public String getString(String key) {
        Object value = get(key);
        return value != null ? value.toString() : null;
    }
    
    public String optString(String key, String defaultValue) {
        Object value = opt(key);
        return value != null ? value.toString() : defaultValue;
    }
    
    public int getInt(String key) {
        Object value = get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return Integer.parseInt(value.toString());
    }
    
    public int optInt(String key, int defaultValue) {
        Object value = opt(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return defaultValue;
    }
    
    public long getLong(String key) {
        Object value = get(key);
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return Long.parseLong(value.toString());
    }
    
    public long optLong(String key, long defaultValue) {
        Object value = opt(key);
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return defaultValue;
    }
    
    public double getDouble(String key) {
        Object value = get(key);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return Double.parseDouble(value.toString());
    }
    
    public double optDouble(String key, double defaultValue) {
        Object value = opt(key);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return defaultValue;
    }
    
    public boolean getBoolean(String key) {
        Object value = get(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return Boolean.parseBoolean(value.toString());
    }
    
    public boolean optBoolean(String key, boolean defaultValue) {
        Object value = opt(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return defaultValue;
    }
    
    public boolean has(String key) {
        return map.containsKey(key);
    }
    
    public JSONObject remove(String key) {
        map.remove(key);
        return this;
    }
    
    public int length() {
        return map.size();
    }
    
    public boolean isEmpty() {
        return map.isEmpty();
    }
    
    @SuppressWarnings("unchecked")
    public <T> Iterable<T> keys() {
        return (Iterable<T>) map.keySet();
    }
    
    /**
     * Returns the set of keys in this object.
     */
    public java.util.Set<String> keySet() {
        return map.keySet();
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('{');
        boolean first = true;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (!first) sb.append(',');
            first = false;
            sb.append(quote(entry.getKey()));
            sb.append(':');
            sb.append(valueToString(entry.getValue()));
        }
        sb.append('}');
        return sb.toString();
    }
    
    static String valueToString(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof String) {
            return quote((String) value);
        }
        if (value instanceof Number || value instanceof Boolean) {
            return value.toString();
        }
        if (value instanceof JSONObject) {
            return value.toString();
        }
        if (value instanceof JSONArray) {
            return value.toString();
        }
        return quote(value.toString());
    }
    
    static String quote(String string) {
        if (string == null) {
            return "null";
        }
        StringBuilder sb = new StringBuilder();
        sb.append('"');
        for (char c : string.toCharArray()) {
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\b': sb.append("\\b"); break;
                case '\f': sb.append("\\f"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < ' ') {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        sb.append('"');
        return sb.toString();
    }
}
