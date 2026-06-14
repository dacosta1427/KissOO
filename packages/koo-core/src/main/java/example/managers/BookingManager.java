package example.managers;

import koo.core.database.StorageManager;
import example.domain.Booking;
import java.util.Collection;

public class BookingManager {
    
    public static Collection<Booking> getAll() {
        return StorageManager.getAll(Booking.class);
    }
    
    public static Booking getByOid(long oid) {
        return StorageManager.getByOid(Booking.class, oid);
    }
    
    public static void create(Booking booking) {
        StorageManager.store(StorageManager.createContainer().addInsert(booking));
    }
    
    public static void update(Booking booking) {
        StorageManager.store(StorageManager.createContainer().addUpdate(booking));
    }
    
    public static void delete(Booking booking) {
        StorageManager.store(StorageManager.createContainer().addDelete(booking));
    }
}