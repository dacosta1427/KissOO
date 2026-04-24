package mycompany.actor.owner;

import koo.core.actor.ANaturalActor;
import koo.core.database.StorageManager;
import lombok.Getter;
import lombok.Setter;
import mycompany.oov.house.Booking;
import koo.core.actor.Agreement;
import koo.core.user.PerstUser;
import mycompany.oov.house.House;
import mycompany.actor.cleaner.Schedule;
import org.garret.perst.Link;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Owner entity for cleaning scheduler.
 * Uses Perst Link for one-to-many relationships.
 */
@Getter @Setter
public class Owner extends ANaturalActor {
    
    private String email;
    private String phone;
    private String address;
    
    private Link houses;  // Perst Link - initialized in constructor
    
    public Owner(String name, String phone, String email, boolean active) {
        super(name, new Agreement(), email);
        this.email = email;
        this.phone = phone;
        setActive(active);
        this.houses = StorageManager.getStorage().createLink();
    }
    
    public Owner(String name, String phone, String email, String address, boolean active) {
        super(name, new Agreement(), email);
        this.email = email;
        this.phone = phone;
        this.address = address;
        setActive(active);
        this.houses = StorageManager.getStorage().createLink();
    }
    
    public List<House> getHouses() {
        if (houses == null || houses.isEmpty()) return List.of();
        return Arrays.asList((House[])houses.toArray(new House[0]));
    }
    
    public Link getHousesLink() {
        return houses;
    }
    
    public void addHouse(House house) {
        if (house != null && houses != null) {
            house.setOwner(this);
            houses.add(house);
        }
    }
    
    public void removeHouse(House house) {
        if (house != null && houses != null) {
            house.setOwner(null);
            int idx = houses.indexOf(house);
            if (idx >= 0) {
                houses.remove(idx);
            }
        }
    }
    
    public List<Booking> getBookings() {
        return getHouses().stream()
            .flatMap(house -> house.getBookings().stream())
            .collect(Collectors.toList());
    }
    
    public List<Schedule> getSchedulesViaHouses() {
        return getBookings().stream()
            .map(Booking::getSchedule)
            .filter(s -> s != null)
            .collect(Collectors.toList());
    }
    
    public PerstUser getUser() { return getPerstUser(); }
    public void setUser(PerstUser user) { setPerstUser(user); }
    
    @Override
    public String toString() {
        return "Owner{name=" + getName() + ", houses=" + (houses != null ? houses.size() : 0) + "}";
    }
}