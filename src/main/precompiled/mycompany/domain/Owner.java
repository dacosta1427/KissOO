package mycompany.domain;

import lombok.Getter;
import lombok.Setter;
import org.garret.perst.continuous.CVersion;
import org.garret.perst.Indexable;
import org.garret.perst.continuous.FullTextSearchable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import oodb.PerstStorageManager;

/**
 * Owner entity for cleaning scheduler.
 * Represents an owner who can own multiple houses.
 * 
 * Extends Actor (NATURAL by default), so automatically has a PerstUser
 * created by the Actor constructor (deactivated by default).
 */
@Getter @Setter
public class Owner extends Actor {
    
    private String email;
    private String phone;
    private String address;
    
    public Owner() {
        super("Owner", "Owner", new Agreement());
    }
    
    public Owner(String name, String email, String phone, String address) {
        super(name, "Owner", new Agreement(), ActorType.NATURAL);
        this.email = email;
        this.phone = phone;
        this.address = address;
        
        // Actor constructor already created a deactivated PerstUser
        // Update the PU username to use email if provided
        if (email != null && !email.isEmpty()) {
            getPerstUser().setUsername(email);
            getPerstUser().setEmail(email);
        }
    }
    
    public Collection<House> getHouses() {
        Collection<House> result = new ArrayList<>();
        Collection<House> all = PerstStorageManager.getAll(House.class);
        for (House house : all) {
            if (house.getOwner() != null && house.getOwner().getOid() == this.getOid()) {
                result.add(house);
            }
        }
        return result;
    }

    public Collection<Booking> getBookings() {
        Collection<Booking> result = new ArrayList<>();
        Collection<House> houses = getHouses();
        Collection<Booking> all = PerstStorageManager.getAll(Booking.class);
        for (Booking booking : all) {
            if (booking.getHouse() != null && houses.contains(booking.getHouse())) {
                result.add(booking);
            }
        }
        return result;
    }

    public Collection<Schedule> getSchedulesViaHouses() {
        Collection<Schedule> result = new ArrayList<>();
        Collection<Booking> bookings = getBookings();
        Collection<Schedule> all = PerstStorageManager.getAll(Schedule.class);
        for (Schedule schedule : all) {
            if (schedule.getBooking() != null && bookings.contains(schedule.getBooking())) {
                result.add(schedule);
            }
        }
        return result;
    }
    
    // Convenience delegate
    public PerstUser getUser() { return getPerstUser(); }
    public void setUser(PerstUser user) { setPerstUser(user); }
    
    @Override
    public String toString() {
        return "Owner{" +
                "name='" + getName() + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", address='" + address + '\'' +
                ", active=" + isActive() +
                ", user=" + (getPerstUser() != null ? getPerstUser().getUsername() : "null") +
                '}';
    }
}
