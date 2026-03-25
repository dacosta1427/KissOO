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
    private long ownerId;
    
    @Indexable
    private boolean active = true;
    
    private String checkInTime = "16:00";  // default check-in time 24h format
    private String checkOutTime = "10:00"; // default check-out time 24h format
    
    public House() {
        // default constructor
    }
    
    public House(String name, String address, String description, boolean active) {
        this.name = name;
        this.address = address;
        this.description = description;
        this.active = active;
        this.ownerId = 0;
        this.checkInTime = "16:00";
        this.checkOutTime = "10:00";
    }
    
    public House(String name, String address, String description, long ownerId, boolean active) {
        this.name = name;
        this.address = address;
        this.description = description;
        this.ownerId = ownerId;
        this.active = active;
        this.checkInTime = "16:00";
        this.checkOutTime = "10:00";
    }
    
    public House(String name, String address, String description, long ownerId, boolean active, String checkInTime, String checkOutTime) {
        this.name = name;
        this.address = address;
        this.description = description;
        this.ownerId = ownerId;
        this.active = active;
        this.checkInTime = checkInTime != null ? checkInTime : "16:00";
        this.checkOutTime = checkOutTime != null ? checkOutTime : "10:00";
    }
    
    // Getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public long getOwnerId() { return ownerId; }
    public void setOwnerId(long ownerId) { this.ownerId = ownerId; }
    
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    
    public String getCheckInTime() { return checkInTime; }
    public void setCheckInTime(String checkInTime) { this.checkInTime = checkInTime; }
    
    public String getCheckOutTime() { return checkOutTime; }
    public void setCheckOutTime(String checkOutTime) { this.checkOutTime = checkOutTime; }
    
    @Override
    public String toString() {
        return "House{" +
                "name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", ownerId=" + ownerId +
                ", active=" + active +
                ", checkInTime='" + checkInTime + '\'' +
                ", checkOutTime='" + checkOutTime + '\'' +
                '}';
    }
}