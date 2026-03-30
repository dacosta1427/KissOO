package mycompany.domain;

import org.garret.perst.continuous.CVersion;
import org.garret.perst.Indexable;
import org.garret.perst.continuous.FullTextSearchable;

/**
 * Cleaner entity for cleaning scheduler.
 * Represents a cleaner who can be assigned to cleaning tasks.
 * Extends Actor to leverage the "Manager at the Gate" pattern with PerstUser.
 */
public class Cleaner extends Actor {
    
    private String phone;
    
    @FullTextSearchable
    private String email;
    
    private String address;
    
    public Cleaner() {
        super("Cleaner", "Cleaner", new Agreement());
    }
    
    public Cleaner(String name, String phone, String email, boolean active) {
        super(name, "Cleaner", new Agreement());
        this.phone = phone;
        this.email = email;
        setActive(active);
    }
    
    public Cleaner(String name, String phone, String email, String address, boolean active) {
        super(name, "Cleaner", new Agreement());
        this.phone = phone;
        this.email = email;
        this.address = address;
        setActive(active);
    }
    
    // Getters and setters
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    
    // Delegating methods to Actor's perstUser field
    public PerstUser getUser() { return getPerstUser(); }
    public void setUser(PerstUser user) { setPerstUser(user); }
    
    @Override
    public String toString() {
        return "Cleaner{" +
                "name='" + getName() + '\'' +
                ", email='" + email + '\'' +
                ", address='" + address + '\'' +
                ", active=" + isActive() +
                ", user=" + (getPerstUser() != null ? getPerstUser().getUsername() : "null") +
                '}';
    }
}
