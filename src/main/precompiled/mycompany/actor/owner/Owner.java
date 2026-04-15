package mycompany.actor.owner;

import koo.oodb.core.actor.ANaturalActor;
import koo.oodb.core.actor.ActorType;
import lombok.Getter;
import lombok.Setter;
import mycompany.oov.house.Booking;
import koo.oodb.core.actor.Agreement;
import koo.oodb.core.user.PerstUser;
import mycompany.oov.house.House;
import mycompany.actor.cleaner.Schedule;

import java.util.ArrayList;
import java.util.Collection;

import koo.oodb.core.StorageManager;

/**
 * Owner entity for cleaning scheduler.
 * Represents an owner who can own multiple houses.
 * 
 * Extends AActor (NATURAL by default), so automatically has a PerstUser
 * created by the AActor constructor (deactivated by default).
 */
@Getter @Setter
public class Owner extends ANaturalActor {
    
    private String email;
    private String phone;
    private String address;
    
    public Owner(String name, String phone, String email, boolean active) {
        super(name, "Owner", new Agreement(), ActorType.NATURAL);
        this.email = email;
        this.phone = phone;
        this.setActive(active);
        
        // AActor constructor already created a deactivated PerstUser
        // Update the PU username to use email if provided
        if (email != null && !email.isEmpty()) {
            getPerstUser().setUsername(email);
            getPerstUser().setEmail(email);
        }
    }
    
    public Collection<House> getHouses() {
        Collection<House> result = new ArrayList<>();
        Collection<House> all = StorageManager.getAll(House.class);
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
        Collection<Booking> all = StorageManager.getAll(Booking.class);
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
        Collection<Schedule> all = StorageManager.getAll(Schedule.class);
        for (Schedule schedule : all) {
            if (schedule.getBooking() != null && bookings.contains(schedule.getBooking())) {
                result.add(schedule);
            }
        }
        return result;
    }
    
    public void addHouse(House house) {
        if (house != null) {
            house.setOwner(this);
        }
    }
    
    public void removeHouse(House house) {
        if (house != null && house.getOwner() != null && house.getOwner().getOid() == this.getOid()) {
            house.setOwner(null);
        }
    }
    
    public void addBooking(Booking booking) {
        if (booking != null) {
            booking.setHouse(getHouses().isEmpty() ? null : getHouses().iterator().next());
        }
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
