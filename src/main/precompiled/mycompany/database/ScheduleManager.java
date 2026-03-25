package mycompany.database;

import mycompany.domain.Schedule;
import org.garret.perst.continuous.TransactionContainer;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ScheduleManager - Manager for Schedule CRUD operations.
 */
public class ScheduleManager extends BaseManager<Schedule> {
    
    private ScheduleManager() {
    }
    
    // ========== RETRIEVE ==========
    
    public static Collection<Schedule> getAll() {
        return oodb.PerstStorageManager.getAll(Schedule.class);
    }
    
    public static Collection<Schedule> getByCleaner(int cleanerId) {
        return getAll().stream()
                .filter(s -> s.getCleanerId() == cleanerId)
                .collect(Collectors.toList());
    }
    
    public static Collection<Schedule> getByBooking(int bookingId) {
        return getAll().stream()
                .filter(s -> s.getBookingId() == bookingId)
                .collect(Collectors.toList());
    }
    
    public static Collection<Schedule> getByDateRange(String startDate, String endDate) {
        return getAll().stream()
                .filter(s -> {
                    String date = s.getScheduleDate();
                    return date.compareTo(startDate) >= 0 && date.compareTo(endDate) <= 0;
                })
                .collect(Collectors.toList());
    }
    
    public static Collection<Schedule> getByStatus(String status) {
        return getAll().stream()
                .filter(s -> s.getStatus().equals(status))
                .collect(Collectors.toList());
    }
    
    public static Schedule getByOid(long oid) {
        return oodb.PerstStorageManager.getByOid(Schedule.class, oid);
    }
    
    // ========== CRUD ==========
    
    public static Schedule create(Object... args) {
        if (args == null || args.length < 5) {
            return null;
        }
        int cleanerId = Integer.parseInt(args[0].toString());
        int bookingId = Integer.parseInt(args[1].toString());
        String scheduleDate = args[2].toString();
        String startTime = args[3].toString();
        String endTime = args[4].toString();
        String notes = args.length > 5 ? args[5].toString() : null;
        String status = args.length > 6 ? args[6].toString() : "scheduled";
        
        Schedule schedule = new Schedule(cleanerId, bookingId, scheduleDate, startTime, endTime, notes);
        schedule.setStatus(status);
        
        TransactionContainer tc = oodb.PerstStorageManager.createContainer();
        tc.addInsert(schedule);
        if (!oodb.PerstStorageManager.store(tc)) {
            return null;
        }
        return schedule;
    }
    
    public static boolean update(Schedule schedule) {
        if (schedule == null) return false;
        
        TransactionContainer tc = oodb.PerstStorageManager.createContainer();
        tc.addUpdate(schedule);
        return oodb.PerstStorageManager.store(tc);
    }
    
    public static boolean delete(Schedule schedule) {
        if (schedule == null) return false;
        
        TransactionContainer tc = oodb.PerstStorageManager.createContainer();
        tc.addDelete(schedule);
        return oodb.PerstStorageManager.store(tc);
    }
    
    public static boolean validate(Schedule schedule) {
        return schedule != null && schedule.getScheduleDate() != null && !schedule.getScheduleDate().isEmpty();
    }
    
    // ========== STATUS OPERATIONS ==========
    
    public static boolean complete(long oid) {
        Schedule schedule = getByOid(oid);
        if (schedule != null) {
            schedule.setStatus("completed");
            TransactionContainer tc = oodb.PerstStorageManager.createContainer();
            tc.addUpdate(schedule);
            return oodb.PerstStorageManager.store(tc);
        }
        return false;
    }
    
    public static boolean cancel(long oid) {
        Schedule schedule = getByOid(oid);
        if (schedule != null) {
            schedule.setStatus("cancelled");
            TransactionContainer tc = oodb.PerstStorageManager.createContainer();
            tc.addUpdate(schedule);
            return oodb.PerstStorageManager.store(tc);
        }
        return false;
    }
}