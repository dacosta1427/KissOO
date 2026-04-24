package mycompany.oov.house;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import mycompany.actor.owner.Owner;
import koo.core.database.StorageManager;
import org.garret.perst.continuous.CVersion;
import org.garret.perst.Indexable;
import org.garret.perst.continuous.FullTextSearchable;
import org.garret.perst.Link;

import java.util.Arrays;
import java.util.List;

/**
 * House entity for cleaning scheduler.
 * Uses Perst Link for one-to-many relationships.
 */
@Getter @Setter
public class House extends CVersion {
    
    @FullTextSearchable
    @Indexable
    private String name;
    
    private String address;

    @FullTextSearchable
    private String description;
    
    private Owner owner;
    
    private CostProfile costProfile;
    
    @Indexable
    private boolean active = true;
    
    private String checkInTime = "16:00";
    private String checkOutTime = "10:00";
    
    private Double surfaceM2;
    private Integer floors = 1;
    private Integer bedrooms = 0;
    private Integer bathrooms = 0;
    private String luxuryLevel = "standard";
    
    private Link bookings;  // Perst Link - initialized in constructor
    
    public House() {
        super();
        this.bookings = StorageManager.getStorage().createLink();
    }
    
    public House(@NonNull Owner owner, @NonNull String name, @NonNull String address, String description, boolean active) {
        super();
        this.owner = owner;
        this.name = name;
        this.address = address;
        this.description = description;
        this.active = active;
        this.bookings = StorageManager.getStorage().createLink();
    }
    
    public long getOwnerOid() {
        return owner != null ? owner.getOid() : 0;
    }
    
    public long getCostProfileOid() {
        return costProfile != null ? costProfile.getOid() : 0;
    }
    
    public List<Booking> getBookings() {
        if (bookings == null || bookings.isEmpty()) return List.of();
        return Arrays.asList((Booking[])bookings.toArray(new Booking[0]));
    }
    
    public Link getBookingsLink() {
        return bookings;
    }
    
    public void addBooking(Booking booking) {
        if (booking != null && bookings != null) {
            booking.setHouse(this);
            bookings.add(booking);
        }
    }
    
    public void removeBooking(Booking booking) {
        if (booking != null && bookings != null) {
            booking.setHouse(null);
            int idx = bookings.indexOf(booking);
            if (idx >= 0) {
                bookings.remove(idx);
            }
        }
    }
    
    @Override
    public String toString() {
        return "House{name=" + name + ", owner=" + (owner != null ? owner.getOid() : "null") + ", bookings=" + (bookings != null ? bookings.size() : 0) + "}";
    }
}