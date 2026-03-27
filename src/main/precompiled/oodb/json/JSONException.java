package oodb.json;

/**
 * Minimal JSON exception for Perst JSON implementation.
 * 
 * Simple and elegant - just what's needed.
 */
public class JSONException extends RuntimeException {
    public JSONException(String message) {
        super(message);
    }
    
    public JSONException(String message, Throwable cause) {
        super(message, cause);
    }
}
