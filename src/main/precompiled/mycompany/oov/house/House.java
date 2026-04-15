package mycompany.oov.house;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import mycompany.actor.owner.Owner;
import org.garret.perst.continuous.CVersion;
import org.garret.perst.Indexable;
import org.garret.perst.continuous.FullTextSearchable;

import java.util.HashSet;
import java.util.Set;
import java.util.Collection;

/**
 * House entity for cleaning scheduler.
 * Represents a house that can be booked for cleaning services.
 * 
 * Uses proper OO references (not ID fields) for Owner and CostProfile.
 * Uses stored collections for Pure OO navigation.
 */
@Getter @Setter
public class House extends CVersion {
    
    @FullTextSearchable
    @Indexable
    private String name;
    
    private String address;

    @FullTextSearchable
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
    
    // Stored collection for Pure OO navigation
    private Set<Booking> bookings = new HashSet<>();
    
    public House() {
    }
    
    public House(@NonNull Owner owner, @NonNull String name, @NonNull String address, String description, boolean active) {
        this.owner = owner;
        this.name = name;
        this.address = address;
        this.description = description;
        this.active = active;
        this.checkInTime = "16:00";
        this.checkOutTime = "10:00";
    }
    
    // Convenience methods for API serialization
    public long getOwnerOid() {
        return owner != null ? owner.getOid() : 0;
    }
    
    public long getCostProfileOid() {
        return costProfile != null ? costProfile.getOid() : 0;
    }
    
    public Collection<Booking> getBookings() {
        return bookings;
    }
    
    public void addBooking(Booking booking) {
        if (booking != null) {
            booking.setHouse(this);
            bookings.add(booking);
        }
    }
    
    public void removeBooking(Booking booking) {
        if (booking != null && bookings.contains(booking)) {
            booking.setHouse(null);
            bookings.remove(booking);
        }
    }
    
    @Override
    public String toString() {
        return "House{" +
                "name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", owner=" + (owner != null ? owner.getOid() : "null") +
                ", costProfile=" + (costProfile != null ? costProfile.getOid() : "null") +
                ", active=" + active +
                ", bookings=" + bookings.size() +
                ", surfaceM2=" + surfaceM2 +
                ", floors=" + floors +
                ", bedrooms=" + bedrooms +
                ", bathrooms=" + bathrooms +
                ", luxuryLevel='" + luxuryLevel + '\'' +
                '}';
    }
}
