package koo.lombok;

import org.kissweb.json.JSONObject;

/**
 * Simple test to verify JSON functionality
 */
public class BasicJSONTest {
    
    private String name;
    private String email;
    private int age;
    
    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("name", name);
        json.put("email", email);
        json.put("age", age);
        return json;
    }
    
    public static void main(String[] args) {
        BasicJSONTest test = new BasicJSONTest();
        test.name = "John Doe";
        test.email = "john@example.com";
        test.age = 30;
        
        JSONObject json = test.toJSON();
        System.out.println("Generated JSON: " + json.toString());
        
        // Test with null values
        BasicJSONTest test2 = new BasicJSONTest();
        test2.name = "Jane Smith";
        test2.email = null;
        test2.age = 25;
        
        JSONObject json2 = test2.toJSON();
        System.out.println("JSON with null email: " + json2.toString());
    }
}