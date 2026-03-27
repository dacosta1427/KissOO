package mycompany.domain;

import org.garret.perst.continuous.CVersion;
import org.garret.perst.Indexable;
import org.garret.perst.continuous.FullTextSearchable;

/**
 * House entity for cleaning scheduler.
 * Represents a house that can be booked for cleaning services.
 * 
 * Uses proper OO references (not ID fields) for Owner and CostProfile.
 */
public class House extends CVersion {
    
    @FullTextSearchable
    @Indexable
    private String name;
    
    private String address;
    private String description;
    
    // Proper OO reference to Owner (was: long ownerId)
    private Owner owner;
    
    // Cost profile for this house (null = use standard)
    private CostProfile costProfile;
    
    @Indexable
    private boolean active = true;
    
    private String checkInTime = "16:00";  // default check-in time 24h format
    private String checkOutTime = "10:00"; // default check-out time 24h format
    
    // NEW: Cost calculation fields
    private Double surfaceM2;              // Total cleaning surface in m²
    private Integer floors;                // Number of floors (default 1)
    private Integer bedrooms;              // Number of bedrooms
    private Integer bathrooms;             // Number of bathrooms
    private String luxuryLevel;            // basic|standard|premium|luxury
    
    public House() {
        this.floors = 1;
        this.bedrooms = 0;
        this.bathrooms = 0;
        this.luxuryLevel = "standard";
    }
    
    public House(String name, String address, String description, boolean active) {
        this();
        this.name = name;
        this.address = address;
        this.description = description;
        this.active = active;
        this.checkInTime = "16:00";
        this.checkOutTime = "10:00";
    }
    
    // Legacy constructor for backward compatibility during migration
    public House(String name, String address, String description, long ownerId, boolean active) {
        this(name, address, description, active);
    }
    
    public House(String name, String address, String description, long ownerId, boolean active, String checkInTime, String checkOutTime) {
        this(name, address, description, active);
        this.checkInTime = checkInTime != null ? checkInTime : "16:00";
        this.checkOutTime = checkOutTime != null ? checkOutTime : "10:00";
    }
    
    // Getters and setters for basic fields
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    // Owner - proper OO reference
    public Owner getOwner() { return owner; }
    public void setOwner(Owner owner) { this.owner = owner; }
    
    /**
     * Get owner OID (for API serialization)
     * Returns 0 if no owner is set.
     */
    public long getOwnerOid() {
        return owner != null ? owner.getOid() : 0;
    }
    
    // CostProfile - proper OO reference
    public CostProfile getCostProfile() { return costProfile; }
    public void setCostProfile(CostProfile costProfile) { this.costProfile = costProfile; }
    
    /**
     * Get cost profile OID (for API serialization)
     * Returns 0 if no cost profile is set.
     */
    public long getCostProfileOid() {
        return costProfile != null ? costProfile.getOid() : 0;
    }
    
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    
    public String getCheckInTime() { return checkInTime; }
    public void setCheckInTime(String checkInTime) { this.checkInTime = checkInTime; }
    
    public String getCheckOutTime() { return checkOutTime; }
    public void setCheckOutTime(String checkOutTime) { this.checkOutTime = checkOutTime; }
    
    // Cost calculation fields
    public Double getSurfaceM2() { return surfaceM2; }
    public void setSurfaceM2(Double surfaceM2) { this.surfaceM2 = surfaceM2; }
    
    public Integer getFloors() { return floors; }
    public void setFloors(Integer floors) { this.floors = floors; }
    
    public Integer getBedrooms() { return bedrooms; }
    public void setBedrooms(Integer bedrooms) { this.bedrooms = bedrooms; }
    
    public Integer getBathrooms() { return bathrooms; }
    public void setBathrooms(Integer bathrooms) { this.bathrooms = bathrooms; }
    
    public String getLuxuryLevel() { return luxuryLevel; }
    public void setLuxuryLevel(String luxuryLevel) { this.luxuryLevel = luxuryLevel; }
    
    @Override
    public String toString() {
        return "House{" +
                "name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", owner=" + (owner != null ? owner.getOid() : "null") +
                ", costProfile=" + (costProfile != null ? costProfile.getOid() : "null") +
                ", active=" + active +
                ", surfaceM2=" + surfaceM2 +
                ", floors=" + floors +
                ", bedrooms=" + bedrooms +
                ", bathrooms=" + bathrooms +
                ", luxuryLevel='" + luxuryLevel + '\'' +
                '}';
    }
}
