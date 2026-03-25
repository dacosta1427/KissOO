package mycompany.database;

import mycompany.domain.Booking;
import org.garret.perst.continuous.TransactionContainer;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * BookingManager - Manager for Booking CRUD operations.
 */
public class BookingManager extends BaseManager<Booking> {
    
    private BookingManager() {
    }
    
    // ========== RETRIEVE ==========
    
    public static Collection<Booking> getAll() {
        return oodb.PerstStorageManager.getAll(Booking.class);
    }
    
    public static Collection<Booking> getByHouse(int houseId) {
        return getAll().stream()
                .filter(b -> b.getHouseId() == houseId)
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
        return oodb.PerstStorageManager.getByOid(Booking.class, oid);
    }
    
    // ========== CRUD ==========
    
    public static Booking create(Object... args) {
        if (args == null || args.length < 6) {
            return null;
        }
        int houseId = Integer.parseInt(args[0].toString());
        String checkInDate = args[1].toString();
        String checkOutDate = args[2].toString();
        String guestName = args[3].toString();
        String guestEmail = args[4].toString();
        String guestPhone = args.length > 5 ? args[5].toString() : null;
        String notes = args.length > 6 ? args[6].toString() : null;
        int dogsCount = args.length > 7 ? Integer.parseInt(args[7].toString()) : 0;
        
        Booking booking = new Booking(houseId, checkInDate, checkOutDate,
                guestName, guestEmail, guestPhone, notes, dogsCount);
        
        TransactionContainer tc = oodb.PerstStorageManager.createContainer();
        tc.addInsert(booking);
        if (!oodb.PerstStorageManager.store(tc)) {
            return null;
        }
        return booking;
    }
    
    public static boolean update(Booking booking) {
        if (booking == null) return false;
        
        TransactionContainer tc = oodb.PerstStorageManager.createContainer();
        tc.addUpdate(booking);
        return oodb.PerstStorageManager.store(tc);
    }
    
    public static boolean delete(Booking booking) {
        if (booking == null) return false;
        
        TransactionContainer tc = oodb.PerstStorageManager.createContainer();
        tc.addDelete(booking);
        return oodb.PerstStorageManager.store(tc);
    }
    
    public static boolean validate(Booking booking) {
        return booking != null && booking.getGuestName() != null && !booking.getGuestName().isEmpty();
    }
    
    // ========== STATUS OPERATIONS ==========
    
    public static boolean confirm(long oid) {
        Booking booking = getByOid(oid);
        if (booking != null) {
            booking.setStatus("confirmed");
            TransactionContainer tc = oodb.PerstStorageManager.createContainer();
            tc.addUpdate(booking);
            return oodb.PerstStorageManager.store(tc);
        }
        return false;
    }
    
    public static boolean cancel(long oid) {
        Booking booking = getByOid(oid);
        if (booking != null) {
            booking.setStatus("cancelled");
            TransactionContainer tc = oodb.PerstStorageManager.createContainer();
            tc.addUpdate(booking);
            return oodb.PerstStorageManager.store(tc);
        }
        return false;
    }
}