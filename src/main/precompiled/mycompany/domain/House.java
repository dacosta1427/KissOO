package mycompany.domain;

import org.garret.perst.continuous.CVersion;
import org.garret.perst.Indexable;
import org.garret.perst.continuous.FullTextSearchable;

/**
 * House entity for cleaning scheduler.
 * Represents a house that can be booked for cleaning services.
 */
public class House extends CVersion {
    
    @FullTextSearchable
    @Indexable
    private String name;
    
    private String address;
    private String description;
    
    @Indexable
    private boolean active = true;
    
    public House() {
        // default constructor
    }
    
    public House(String name, String address, String description, boolean active) {
        this.name = name;
        this.address = address;
        this.description = description;
        this.active = active;
    }
    
    // Getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    
    @Override
    public String toString() {
        return "House{" +
                "name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", active=" + active +
                '}';
    }
}