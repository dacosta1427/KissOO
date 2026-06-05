package koo.lombok;

import org.kissweb.json.JSONObject;
import org.garret.perst.continuous.CVersion;

/**
 * Test class that extends CVersion to test JSONUtils
 */
public class TestCVersion extends CVersion {
    
    private String name;
    private String description;
    
    public TestCVersion(String name, String description) {
        this.name = name;
        this.description = description;
    }
    
    // Manually implemented toJSON method
    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("name", name);
        json.put("description", description);
        json.put("oid", getOid());
        return json;
    }
    
    @Override
    public String toString() {
        return "TestCVersion{name='" + name + "', description='" + description + "', oid=" + getOid() + "}";
    }
}