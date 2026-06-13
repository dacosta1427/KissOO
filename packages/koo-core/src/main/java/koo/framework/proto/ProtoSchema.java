package koo.framework.proto;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class ProtoSchema {
    private String messageName;
    private String packageName;
    private List<ProtoField> fields;
    private Map<String, ProtoSchema> nestedTypes;
    
    public ProtoSchema() {
        this.fields = new ArrayList<>();
        this.nestedTypes = new HashMap<>();
    }
    
    public ProtoSchema(String messageName) {
        this();
        this.messageName = messageName;
    }
    
    public void addField(ProtoField field) {
        fields.add(field);
    }
    
    public void addNestedType(String name, ProtoSchema schema) {
        nestedTypes.put(name, schema);
    }
    
    // Getters and setters
    public String getMessageName() { return messageName; }
    public void setMessageName(String messageName) { this.messageName = messageName; }
    
    public String getPackageName() { return packageName; }
    public void setPackageName(String packageName) { this.packageName = packageName; }
    
    public List<ProtoField> getFields() { return fields; }
    public void setFields(List<ProtoField> fields) { this.fields = fields; }
    
    public Map<String, ProtoSchema> getNestedTypes() { return nestedTypes; }
    public void setNestedTypes(Map<String, ProtoSchema> nestedTypes) { this.nestedTypes = nestedTypes; }
}