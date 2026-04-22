package mycompany.oov;

import koo.oodb.BaseManager;
import koo.oodb.core.database.StorageManager;
import mycompany.oov.house.Booking;
import mycompany.oov.house.House;
import org.garret.perst.continuous.TransactionContainer;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * BookingManager - Manager for Booking CRUD operations.
 */
public class BookingManager extends BaseManager<Booking> {
    
    private BookingManager() {
    }
    
    // ========== RETRIEVE ==========
    
    public static Collection<Booking> getAll() {
        return StorageManager.getAll(Booking.class);
    }
    
    public static Collection<Booking> getByHouse(House house) {
        return getAll().stream()
                .filter(b -> b.getHouse() == house)
                .collect(Collectors.toList());
    }
    
    public static Collection<Booking> getByStatus(String status) {
        return getAll().stream()
                .filter(b -> b.getStatus().equals(status))
                .collect(Collectors.toList());
    }
    
    public static Collection<Booking> getByDateRange(String startDate, String endDate) {
        return getAll().stream()
                .filter(b -> {
                    String checkIn = b.getCheckInDate();
                    String checkOut = b.getCheckOutDate();
                    return checkIn.compareTo(endDate) <= 0 && checkOut.compareTo(startDate) >= 0;
                })
                .collect(Collectors.toList());
    }
    
    public static Booking getByOid(long oid) {
        return StorageManager.getByOid(Booking.class, oid);
    }
    
    // ========== CRUD ==========
    
    public static Booking create(House house, String checkInDate, String checkOutDate,
                                  String guestName, String guestEmail, String guestPhone, String notes, int dogsCount) {
        Booking booking = new Booking();
        booking.setHouse(house);
        booking.setCheckInDate(checkInDate);
        booking.setCheckOutDate(checkOutDate);
        booking.setGuestName(guestName);
        booking.setGuestEmail(guestEmail);
        booking.setGuestPhone(guestPhone);
        booking.setNotes(notes);
        booking.setDogsCount(dogsCount);
        booking.setStatus("pending");
        
        TransactionContainer tc = StorageManager.createContainer();
        tc.addInsert(booking);
        if (!StorageManager.store(tc)) {
            return null;
        }
        return booking;
    }
    
    public static boolean update(Booking booking) {
        if (booking == null) return false;
        
        TransactionContainer tc = StorageManager.createContainer();
        tc.addUpdate(booking);
        return StorageManager.store(tc);
    }
    
    public static boolean delete(Booking booking) {
        if (booking == null) return false;
        
        TransactionContainer tc = StorageManager.createContainer();
        tc.addDelete(booking);
        return StorageManager.store(tc);
    }
    
    public static boolean validate(Booking booking) {
        return booking != null && booking.getGuestName() != null && !booking.getGuestName().isEmpty();
    }
    
    // ========== STATUS OPERATIONS ==========
    
    public static boolean confirm(long oid) {
        Booking booking = getByOid(oid);
        if (booking != null) {
            booking.setStatus("confirmed");
            TransactionContainer tc = StorageManager.createContainer();
            tc.addUpdate(booking);
            return StorageManager.store(tc);
        }
        return false;
    }
    
    public static boolean cancel(long oid) {
        Booking booking = getByOid(oid);
        if (booking != null) {
            booking.setStatus("cancelled");
            TransactionContainer tc = StorageManager.createContainer();
            tc.addUpdate(booking);
            return StorageManager.store(tc);
        }
        return false;
    }
}
