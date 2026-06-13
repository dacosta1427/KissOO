package koo.framework.proto;

public class ProtoField {
    private String name;
    private String type;
    private int number;
    private boolean isRepeated;
    private String nestedTypeName;
    
    public ProtoField() {}
    
    public ProtoField(String name, String type, int number) {
        this.name = name;
        this.type = type;
        this.number = number;
    }
    
    // Getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public int getNumber() { return number; }
    public void setNumber(int number) { this.number = number; }
    
    public boolean isRepeated() { return isRepeated; }
    public void setRepeated(boolean repeated) { isRepeated = repeated; }
    
    public String getNestedTypeName() { return nestedTypeName; }
    public void setNestedTypeName(String nestedTypeName) { this.nestedTypeName = nestedTypeName; }
}