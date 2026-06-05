package koo.lombok;

import org.kissweb.json.JSONObject;
import org.kissweb.json.JSONArray;

/**
 * Test class that implements CVersion interface for JSONUtils testing
 */
public class MockCVersion {
    
    private String name;
    private boolean deleted = false;
    private long oid;
    
    public MockCVersion(String name, long oid) {
        this.name = name;
        this.oid = oid;
    }
    
    public boolean isDeleted() {
        return deleted;
    }
    
    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
    
    public long getOid() {
        return oid;
    }
    
    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("name", name);
        json.put("oid", oid);
        json.put("deleted", deleted);
        return json;
    }
    
    public static void main(String[] args) {
        // Test JSONUtils with collection
        java.util.List<MockCVersion> objects = new java.util.ArrayList<>();
        objects.add(new MockCVersion("Object 1", 1));
        objects.add(new MockCVersion("Object 2", 2));
        objects.add(new MockCVersion("Object 3", 3));
        
        // Mark one as deleted
        objects.get(1).setDeleted(true);
        
        // Test toJSONArray
        JSONArray jsonArray = JSONUtils.toJSONArray(objects);
        System.out.println("Array JSON (should exclude deleted): " + jsonArray.toString());
        
        // Test toCollectionResponse
        JSONObject response = JSONUtils.toCollectionResponse(objects);
        System.out.println("Collection Response: " + response.toString());
        
        // Test toPaginatedResponse
        JSONObject paginated = JSONUtils.toPaginatedResponse(objects, 0, 2);
        System.out.println("Paginated Response (page 0, size 2): " + paginated.toString());
    }
}