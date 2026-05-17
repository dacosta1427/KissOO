package domain.oov.house;

import lombok.NonNull;
import domain.actor.owner.Owner;
import domain.oov.house.CostProfile;
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
        this.bookings = StorageManager.createLink();
    }
    
    public House(@NonNull Owner owner, @NonNull String name, @NonNull String address, String description, boolean active) {
        super();
        if (owner == null) {
            throw new IllegalArgumentException("House must have an owner - owner cannot be null");
        }
        this.owner = owner;
        this.name = name;
        this.address = address;
        this.description = description;
        this.active = active;
        this.bookings = StorageManager.createLink();
    }
    
    public Owner getOwner() { return owner; }
    public void setOwner(Owner owner) {
        if (owner == null) {
            throw new IllegalArgumentException("House must have an owner - owner cannot be null");
        }
        this.owner = owner;
    }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public String getCheckInTime() { return checkInTime; }
    public void setCheckInTime(String checkInTime) { this.checkInTime = checkInTime; }
    public String getCheckOutTime() { return checkOutTime; }
    public void setCheckOutTime(String checkOutTime) { this.checkOutTime = checkOutTime; }
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
    public CostProfile getCostProfile() { return costProfile; }
    public void setCostProfile(CostProfile costProfile) { this.costProfile = costProfile; }
    
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
