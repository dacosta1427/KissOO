package koo.lombok;

import org.kissweb.json.JSONObject;

/**
 * Simple test class to verify @toJSON functionality without complex build system
 */
public class SimpleJSONTest {
    
    private String name;
    private String email;
    private int age;
    
    // Manually implemented toJSON method (what we want Lombok to generate)
    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("name", name);
        json.put("email", email);
        json.put("age", age);
        return json;
    }
    
    public static void main(String[] args) {
        SimpleJSONTest test = new SimpleJSONTest();
        test.name = "John Doe";
        test.email = "john@example.com";
        test.age = 30;
        
        JSONObject json = test.toJSON();
        System.out.println("Generated JSON: " + json.toString());
        
        // Test JSONUtils
        java.util.List<SimpleJSONTest> list = new java.util.ArrayList<>();
        list.add(test);
        
        JSONArray jsonArray = JSONUtils.toJSONArray(list);
        System.out.println("Array JSON: " + jsonArray.toString());
        
        JSONObject response = JSONUtils.toCollectionResponse(list);
        System.out.println("Response JSON: " + response.toString());
    }
}