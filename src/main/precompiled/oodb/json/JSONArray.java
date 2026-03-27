package oodb.json;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Minimal JSON array for Perst serialization.
 * 
 * Just enough for entity serialization - simple and elegant.
 * ~80 lines vs 1200+ lines in full org.json.JSONArray.
 */
public class JSONArray {
    
    private final List<Object> list = new ArrayList<>();
    
    public JSONArray() {
    }
    
    public JSONArray(Collection<?> collection) {
        if (collection != null) {
            list.addAll(collection);
        }
    }
    
    public JSONArray put(Object value) {
        list.add(value);
        return this;
    }
    
    public Object get(int index) {
        if (index < 0 || index >= list.size()) {
            throw new JSONException("Index " + index + " out of range [0," + list.size() + ")");
        }
        return list.get(index);
    }
    
    public Object opt(int index) {
        return opt(index, null);
    }
    
    public Object opt(int index, Object defaultValue) {
        if (index < 0 || index >= list.size()) {
            return defaultValue;
        }
        return list.get(index);
    }
    
    public int length() {
        return list.size();
    }
    
    public boolean isEmpty() {
        return list.isEmpty();
    }
    
    public List<?> toList() {
        return new ArrayList<>(list);
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append(',');
            sb.append(valueToString(list.get(i)));
        }
        sb.append(']');
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
