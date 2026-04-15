package mycompany.domain;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Owner entity for cleaning scheduler.
 * Represents an owner who can own multiple houses.
 * 
 * Extends Actor (NATURAL by default), so automatically has a PerstUser
 * created by the Actor constructor (deactivated by default).
 * 
 * Bidirectional relationships:
 * - Owner HAS houses (Collection)
 * - House HAS owner (single reference)
 */
@Getter @Setter
public class Owner extends Actor {
    
    private String email;
    private String phone;
    private String address;
    
    // Bidirectional: Owner → houses (implicit filtering via house.owner)
    private Set<House> houses = new HashSet<>();
    
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
    
    // Use persisted collection - no more iteration!
    public Collection<House> getHouses() {
        return houses;
    }
    
    // Keep for backward compatibility, now delegates to collection
    public Collection<Booking> getBookings() {
        Collection<Booking> result = new ArrayList<>();
        for (House house : houses) {
            for (Booking booking : house.getBookings()) {
                result.add(booking);
            }
        }
        return result;
    }
    
    // Keep for backward compatibility - iterate through bookings to get schedules
    public Collection<Schedule> getSchedulesViaHouses() {
        Collection<Schedule> result = new ArrayList<>();
        for (House house : houses) {
            for (Booking booking : house.getBookings()) {
                if (booking.getSchedule() != null) {
                    result.add(booking.getSchedule());
                }
            }
        }
        return result;
    }
    
    public void addHouse(House house) {
        if (house != null) {
            houses.add(house);
            house.setOwner(this);
        }
    }
    
    public void removeHouse(House house) {
        if (house != null && houses.contains(house)) {
            houses.remove(house);
            house.setOwner(null);
        }
    }
    
    public void addBooking(Booking booking) {
        // For backward compatibility - add to first house
        if (booking != null && !houses.isEmpty()) {
            houses.iterator().next().addBooking(booking);
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
                ", houses=" + houses.size() +
                ", user=" + (getPerstUser() != null ? getPerstUser().getUsername() : "null") +
                '}';
    }
}
