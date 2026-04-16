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

import java.util.HashSet;
import java.util.Set;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Owner entity for cleaning scheduler.
 * Represents an owner who can own multiple houses.
 * 
 * Extends ANaturalActor (NATURAL by default), so automatically has a PerstUser
 * created by the AActor constructor (deactivated by default).
 * 
 * Uses stored collections for Pure OO navigation.
 */
@Getter @Setter
public class Owner extends ANaturalActor {
    
    private String email;
    private String phone;
    private String address;
    
    private Set<House> houses = new HashSet<>();
    
    public Owner(String name, String phone, String email, boolean active) {
        super(name, new Agreement(), email);
        this.email = email;
        this.phone = phone;
        setActive(active);
    }
    
    public Collection<House> getHouses() {
        return houses;
    }
    
    public Collection<Booking> getBookings() {
        return houses.stream()
            .flatMap(house -> house.getBookings().stream())
            .collect(Collectors.toList());
    }
    
    public Collection<Schedule> getSchedulesViaHouses() {
        return getBookings().stream()
            .flatMap(booking -> {
                Set<Schedule> schedules = new HashSet<>();
                // Get schedules via booking relationship
                return schedules.stream();
            })
            .collect(Collectors.toList());
    }
    
    public void addHouse(House house) {
        if (house != null) {
            house.setOwner(this);
            houses.add(house);
        }
    }
    
    public void removeHouse(House house) {
        if (house != null && houses.contains(house)) {
            house.setOwner(null);
            houses.remove(house);
        }
    }
    
    public void addBooking(Booking booking) {
        if (booking != null && !houses.isEmpty()) {
            booking.setHouse(houses.iterator().next());
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