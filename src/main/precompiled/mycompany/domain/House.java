package mycompany.domain;

import lombok.Getter;
import lombok.Setter;
import org.garret.perst.continuous.CVersion;
import org.garret.perst.Indexable;
import org.garret.perst.continuous.FullTextSearchable;
import java.util.ArrayList;
import java.util.Collection;
import oodb.PerstStorageManager;

/**
 * House entity for cleaning scheduler.
 * Represents a house that can be booked for cleaning services.
 * 
 * Uses proper OO references (not ID fields) for Owner and CostProfile.
 */
@Getter @Setter
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
    
    private String checkInTime = "16:00";
    private String checkOutTime = "10:00";
    
    // Cost calculation fields
    private Double surfaceM2;
    private Integer floors = 1;
    private Integer bedrooms = 0;
    private Integer bathrooms = 0;
    private String luxuryLevel = "standard";
    
    public House() {
    }
    
    public House(String name, String address, String description, boolean active) {
        this.name = name;
        this.address = address;
        this.description = description;
        this.active = active;
        this.checkInTime = "16:00";
        this.checkOutTime = "10:00";
    }
    
    public House(String name, String address, String description, boolean active, Owner owner) {
        this.name = name;
        this.address = address;
        this.description = description;
        this.active = active;
        this.checkInTime = "16:00";
        this.checkOutTime = "10:00";
        if (owner == null) {
            throw new IllegalArgumentException("House must have an owner");
        }
        this.owner = owner;
    }
    
    // Convenience methods for API serialization
    public long getOwnerOid() {
        return owner != null ? owner.getOid() : 0;
    }
    
    public long getCostProfileOid() {
        return costProfile != null ? costProfile.getOid() : 0;
    }
    
    public Collection<Booking> getBookings() {
        Collection<Booking> result = new ArrayList<>();
        Collection<Booking> all = PerstStorageManager.getAll(Booking.class);
        for (Booking booking : all) {
            if (booking.getHouse() != null && booking.getHouse().getOid() == this.getOid()) {
                result.add(booking);
            }
        }
        return result;
    }
    
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
