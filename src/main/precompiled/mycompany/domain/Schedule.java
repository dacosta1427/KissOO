package mycompany.domain;

import org.garret.perst.continuous.CVersion;
import org.garret.perst.Indexable;
import org.garret.perst.continuous.FullTextSearchable;

/**
 * Schedule entity for cleaning scheduler.
 * Represents a scheduled cleaning task assigned to a cleaner for a booking.
 */
public class Schedule extends CVersion {
    
    @Indexable
    private int cleanerId;
    
    @Indexable
    private int bookingId;
    
    private String scheduleDate;   // ISO date string
    private String startTime;      // HH:mm
    private String endTime;        // HH:mm
    private String notes;
    
    @Indexable
    private String status;  // scheduled, completed, cancelled
    
    public Schedule() {
        this.status = "scheduled";
    }
    
    public Schedule(int cleanerId, int bookingId, String scheduleDate, 
                    String startTime, String endTime, String notes) {
        this.cleanerId = cleanerId;
        this.bookingId = bookingId;
        this.scheduleDate = scheduleDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.notes = notes;
        this.status = "scheduled";
    }
    
    // Getters and setters
    public int getCleanerId() { return cleanerId; }
    public void setCleanerId(int cleanerId) { this.cleanerId = cleanerId; }
    
    public int getBookingId() { return bookingId; }
    public void setBookingId(int bookingId) { this.bookingId = bookingId; }
    
    public String getScheduleDate() { return scheduleDate; }
    public void setScheduleDate(String scheduleDate) { this.scheduleDate = scheduleDate; }
    
    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    
    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    @Override
    public String toString() {
        return "Schedule{" +
                "cleanerId=" + cleanerId +
                ", bookingId=" + bookingId +
                ", scheduleDate='" + scheduleDate + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}