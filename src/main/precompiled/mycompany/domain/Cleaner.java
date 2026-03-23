package mycompany.domain;

import org.garret.perst.continuous.CVersion;
import org.garret.perst.Indexable;
import org.garret.perst.continuous.FullTextSearchable;

/**
 * Cleaner entity for cleaning scheduler.
 * Represents a cleaner who can be assigned to cleaning tasks.
 */
public class Cleaner extends CVersion {
    
    @FullTextSearchable
    @Indexable(unique = true)
    private String name;
    
    private String phone;
    
    private String email;
    
    @Indexable
    private boolean active = true;
    
    public Cleaner() {
        // default constructor
    }
    
    public Cleaner(String name, String phone, String email, boolean active) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.active = active;
    }
    
    // Getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    
    @Override
    public String toString() {
        return "Cleaner{" +
                "name='" + name + '\'' +
                ", active=" + active +
                '}';
    }
}