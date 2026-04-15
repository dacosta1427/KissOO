package mycompany.actor;

import koo.oodb.BaseManager;
import mycompany.actor.cleaner.Schedule;
import mycompany.actor.cleaner.Cleaner;
import koo.oodb.core.StorageManager;
import mycompany.oov.house.Booking;
import org.garret.perst.continuous.TransactionContainer;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * ScheduleManager - Manager for Schedule CRUD operations.
 */
public class ScheduleManager extends BaseManager<Schedule> {
    
    private ScheduleManager() {
    }
    
    // ========== RETRIEVE ==========
    
    public static Collection<Schedule> getAll() {
        return StorageManager.getAll(Schedule.class);
    }
    
    public static Collection<Schedule> getByCleaner(Cleaner cleaner) {
        return getAll().stream()
                .filter(s -> s.getCleaner() == cleaner)
                .collect(Collectors.toList());
    }
    
    public static Collection<Schedule> getByBooking(Booking booking) {
        return getAll().stream()
                .filter(s -> s.getBooking() == booking)
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
        return StorageManager.getByOid(Schedule.class, oid);
    }
    
    // ========== CRUD ==========
    
    public static Schedule create(Cleaner cleaner, Booking booking, String scheduleDate, String startTime, String endTime, String notes) {
        Schedule schedule = new Schedule();
        schedule.setCleaner(cleaner);
        schedule.setBooking(booking);
        schedule.setScheduleDate(scheduleDate);
        schedule.setStartTime(startTime);
        schedule.setEndTime(endTime);
        schedule.setNotes(notes);
        schedule.setStatus("scheduled");
        
        TransactionContainer tc = StorageManager.createContainer();
        tc.addInsert(schedule);
        if (!StorageManager.store(tc)) {
            return null;
        }
        return schedule;
    }
    
    public static boolean update(Schedule schedule) {
        if (schedule == null) return false;
        
        TransactionContainer tc = StorageManager.createContainer();
        tc.addUpdate(schedule);
        return StorageManager.store(tc);
    }
    
    public static boolean delete(Schedule schedule) {
        if (schedule == null) return false;
        
        TransactionContainer tc = StorageManager.createContainer();
        tc.addDelete(schedule);
        return StorageManager.store(tc);
    }
    
    public static boolean validate(Schedule schedule) {
        return schedule != null && schedule.getScheduleDate() != null && !schedule.getScheduleDate().isEmpty();
    }
    
    // ========== STATUS OPERATIONS ==========
    
    public static boolean complete(long oid) {
        Schedule schedule = getByOid(oid);
        if (schedule != null) {
            schedule.setStatus("completed");
            TransactionContainer tc = StorageManager.createContainer();
            tc.addUpdate(schedule);
            return StorageManager.store(tc);
        }
        return false;
    }
    
    public static boolean cancel(long oid) {
        Schedule schedule = getByOid(oid);
        if (schedule != null) {
            schedule.setStatus("cancelled");
            TransactionContainer tc = StorageManager.createContainer();
            tc.addUpdate(schedule);
            return StorageManager.store(tc);
        }
        return false;
    }
}
