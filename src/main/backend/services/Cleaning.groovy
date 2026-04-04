package services

import org.kissweb.json.JSONArray
import org.kissweb.json.JSONObject
import org.kissweb.database.Connection
import org.kissweb.restServer.ProcessServlet
import org.kissweb.restServer.MainServlet
import mycompany.database.CleanerManager
import mycompany.database.BookingManager
import mycompany.database.ScheduleManager
import mycompany.database.HouseManager
import mycompany.database.OwnerManager
import mycompany.database.CostProfileManager
import mycompany.domain.Cleaner
import mycompany.domain.Booking
import mycompany.domain.Schedule
import mycompany.domain.House
import mycompany.domain.Owner
import mycompany.domain.CostProfile
import mycompany.database.PerstUserManager
import mycompany.domain.PerstUser
import oodb.PerstStorageManager
import services.CostService

/**
 * Cleaning service for CRUD operations on cleaning scheduler entities.
 * 
 * Uses PerstStorageManager for Perst OODBMS operations.
 * 
 * Authorization:
 * - Admin (userId=1): can see and do everything
 * - Owner: sees their Houses, their Bookings, their Schedules (NO Cleaner info)
 * - Cleaner: sees Houses assigned to them, their own Schedules (nothing else)
 */
class Cleaning {

    // ==================== AUTHORIZATION HELPERS ====================
    
    private boolean isAdmin(JSONObject injson) {
        try {
            if (injson.has("_isAdmin")) {
                return injson.getBoolean("_isAdmin")
            }
            return false
        } catch (Exception e) { 
            println "isAdmin error: ${e.message}"
            return false 
        }
    }
    
    private long getOwnerId(JSONObject injson) {
        try {
            return injson.has("_ownerId") ? injson.getLong("_ownerId") : 0
        } catch (Exception e) { return 0 }
    }
    
    private long getCleanerId(JSONObject injson) {
        try {
            return injson.has("_cleanerId") ? injson.getLong("_cleanerId") : 0
        } catch (Exception e) { return 0 }
    }
    
    private String getAdminType(JSONObject injson) {
        try {
            if (injson.has("_adminType")) {
                return injson.getString("_adminType")
            }
            return "none"
        } catch (Exception e) { 
            println "getAdminType error: ${e.message}"
            return "none" 
        }
    }
    
    private boolean isSystemAdmin(JSONObject injson) {
        return isAdmin(injson) && getAdminType(injson) == "system"
    }
    
    private void checkAdminOnly(JSONObject injson, String operation) {
        if (!isAdmin(injson)) {
            throw new Exception("Admin access required for: " + operation)
        }
    }
    
    private void checkSystemAdminOnly(JSONObject injson, String operation) {
        if (!isSystemAdmin(injson)) {
            throw new Exception("System admin access required for: " + operation)
        }
    }

    // ==================== CLEANERS ====================
    
    void getCleaners(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            // Only admins can see cleaners list
            if (!isAdmin(injson)) {
                outjson.put("_Success", false)
                outjson.put("_ErrorMessage", "Access denied: Admin only")
                return
            }
            
            Collection<Cleaner> cleaners = PerstStorageManager.getAll(Cleaner.class)
            JSONArray rows = new JSONArray()
            
            for (Cleaner cleaner : cleaners) {
                JSONObject row = new JSONObject()
                row.put("id", cleaner.getOid())
                row.put("name", cleaner.getName())
                row.put("phone", cleaner.getPhone())
                row.put("email", cleaner.getEmail())
                row.put("address", cleaner.getAddress())
                row.put("active", cleaner.isActive())
                rows.put(row)
            }
            
            outjson.put("data", rows)
        } catch (Exception e) {
            outjson.put("_Success", false)
            outjson.put("_ErrorMessage", e.message)
        }
    }
    
    void getCleaner(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            // Admin only
            if (!isAdmin(injson)) {
                outjson.put("_Success", false)
                outjson.put("_ErrorMessage", "Access denied: Admin only")
                return
            }
            
            long oid = injson.getLong("id")
            Cleaner cleaner = PerstStorageManager.getByOid(Cleaner.class, oid)
            if (cleaner == null) {
                outjson.put("_Success", false)
                outjson.put("_ErrorMessage", "Cleaner not found")
                return
            }
            JSONObject data = new JSONObject()
            data.put("id", cleaner.getOid())
            data.put("name", cleaner.getName())
            data.put("phone", cleaner.getPhone())
            data.put("email", cleaner.getEmail())
            data.put("address", cleaner.getAddress())
            data.put("active", cleaner.isActive())
            outjson.put("data", data)
        } catch (Exception e) {
            outjson.put("_Success", false)
            outjson.put("_ErrorMessage", e.message)
        }
    }
    
    void createCleaner(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            checkAdminOnly(injson, "createCleaner")
            
            JSONObject data = injson.getJSONObject("data")
            String name = data.getString("name")
            String phone = data.getString("phone", "")
            String email = data.getString("email", "")
            String address = data.getString("address", "")
            boolean active = data.getBoolean("active", true)
            
            // Cleaner constructor auto-creates a deactivated PerstUser
            Cleaner cleaner = new Cleaner(name, phone, email, address, active)
            
            def tc = PerstStorageManager.createContainer()
            tc.addInsert(cleaner)
            tc.addInsert(cleaner.getPerstUser())
            if (!PerstStorageManager.store(tc)) {
                outjson.put("_Success", false)
                outjson.put("_ErrorMessage", "Failed to create cleaner")
                return
            }
            JSONObject result = new JSONObject()
            result.put("id", cleaner.getOid())
            result.put("name", cleaner.getName())
            result.put("phone", cleaner.getPhone())
            result.put("email", cleaner.getEmail())
            result.put("address", cleaner.getAddress())
            result.put("active", cleaner.isActive())
            outjson.put("data", result)
        } catch (Exception e) {
            outjson.put("_Success", false)
            outjson.put("_ErrorMessage", e.message)
            e.printStackTrace()
        }
    }
    
    void updateCleaner(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            // Admin only
            checkAdminOnly(injson, "updateCleaner")
            
            long oid = injson.getLong("id")
            JSONObject data = injson.getJSONObject("data")
            Cleaner cleaner = PerstStorageManager.getByOid(Cleaner.class, oid)
            if (cleaner == null) {
                outjson.put("_Success", false)
                outjson.put("_ErrorMessage", "Cleaner not found")
                return
            }
            if (data.has("name")) cleaner.setName(data.getString("name"))
            if (data.has("phone")) cleaner.setPhone(data.getString("phone"))
            if (data.has("email")) cleaner.setEmail(data.getString("email"))
            if (data.has("address")) cleaner.setAddress(data.getString("address"))
            if (data.has("active")) cleaner.setActive(data.getBoolean("active"))
            
            def tc = PerstStorageManager.createContainer()
            tc.addUpdate(cleaner)
            if (!PerstStorageManager.store(tc)) {
                outjson.put("_Success", false)
                outjson.put("_ErrorMessage", "Failed to update cleaner")
                return
            }
            JSONObject result = new JSONObject()
            result.put("id", cleaner.getOid())
            result.put("name", cleaner.getName())
            result.put("phone", cleaner.getPhone())
            result.put("email", cleaner.getEmail())
            result.put("address", cleaner.getAddress())
            result.put("active", cleaner.isActive())
            outjson.put("data", result)
        } catch (Exception e) {
            outjson.put("_Success", false)
            outjson.put("_ErrorMessage", e.message)
        }
    }
    
    void deleteCleaner(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            // Admin only
            checkAdminOnly(injson, "deleteCleaner")
            
            long oid = injson.getLong("id")
            Cleaner cleaner = PerstStorageManager.getByOid(Cleaner.class, oid)
            if (cleaner == null) {
                outjson.put("_Success", false)
                outjson.put("_ErrorMessage", "Cleaner not found")
                return
            }
            def tc = PerstStorageManager.createContainer()
            tc.addDelete(cleaner)
            if (!PerstStorageManager.store(tc)) {
                outjson.put("_Success", false)
                outjson.put("_ErrorMessage", "Failed to delete cleaner")
                return
            }
            outjson.put("_Success", true)
        } catch (Exception e) {
            outjson.put("_Success", false)
            outjson.put("_ErrorMessage", e.message)
        }
    }
    
    // ==================== BOOKINGS ====================
    
    void getBookings(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            Collection<Booking> allBookings = PerstStorageManager.getAll(Booking.class)
            JSONArray rows = new JSONArray()
            long ownerId = getOwnerId(injson)
            boolean admin = isAdmin(injson)
            
            for (Booking booking : allBookings) {
                // Authorization: Admin sees all, Owner sees their bookings only
                if (!admin && ownerId > 0) {
                    // Get house and check ownership via OODB reference
                    House house = booking.getHouse()
                    if (house == null || house.getOwner() == null || house.getOwner().getOid() != ownerId) {
                        continue  // Skip - not owner's booking
                    }
                }
                
                JSONObject row = new JSONObject()
                row.put("id", booking.getOid())
                row.put("house_id", booking.getHouseId())
                row.put("check_in_date", booking.getCheckInDate())
                row.put("check_out_date", booking.getCheckOutDate())
                row.put("guest_name", booking.getGuestName())
                row.put("guest_email", booking.getGuestEmail())
                row.put("guest_phone", booking.getGuestPhone())
                row.put("notes", booking.getNotes())
                row.put("dogs_count", booking.getDogsCount())
                row.put("status", booking.getStatus())
                rows.put(row)
            }
            
            outjson.put("data", rows)
        } catch (Exception e) {
            outjson.put("_Success", false)
            outjson.put("_ErrorMessage", e.message)
        }
    }
    
    void getBooking(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            // Using PerstStorageManager directly
            long oid = injson.getLong("id")
            Booking booking = PerstStorageManager.getByOid(Booking.class, oid)
            if (booking == null) {
                outjson.put("_Success", false)
                outjson.put("_ErrorMessage", "Booking not found")
                return
            }
            JSONObject data = new JSONObject()
            data.put("id", booking.getOid())
            data.put("house_id", booking.getHouseId())
            data.put("check_in_date", booking.getCheckInDate())
            data.put("check_out_date", booking.getCheckOutDate())
            data.put("guest_name", booking.getGuestName())
            data.put("guest_email", booking.getGuestEmail())
            data.put("guest_phone", booking.getGuestPhone())
            data.put("notes", booking.getNotes())
            data.put("dogs_count", booking.getDogsCount())
            data.put("status", booking.getStatus())
            outjson.put("data", data)
        } catch (Exception e) {
            outjson.put("_Success", false)
            outjson.put("_ErrorMessage", e.message)
        }
    }
    
    void createBooking(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            // Using PerstStorageManager directly
            JSONObject data = injson.getJSONObject("data")
            int houseId = data.getInt("house_id")
            String checkInDate = data.getString("check_in_date")
            String checkOutDate = data.getString("check_out_date")
            String guestName = data.getString("guest_name")
            String guestEmail = data.getString("guest_email")
            String guestPhone = data.getString("guest_phone", "")
            String notes = data.getString("notes", "")
            int dogsCount = data.has("dogs_count") ? data.getInt("dogs_count") : 0
            
            Booking booking = new Booking(houseId, checkInDate, checkOutDate,
                    guestName, guestEmail, guestPhone, notes, dogsCount)
            def tc = PerstStorageManager.createContainer()
            tc.addInsert(booking)
            if (!PerstStorageManager.store(tc)) {
                outjson.put("_Success", false)
                outjson.put("_ErrorMessage", "Failed to create booking")
                return
            }
            JSONObject result = new JSONObject()
            result.put("id", booking.getOid())
            result.put("house_id", booking.getHouseId())
            result.put("check_in_date", booking.getCheckInDate())
            result.put("check_out_date", booking.getCheckOutDate())
            result.put("guest_name", booking.getGuestName())
            result.put("guest_email", booking.getGuestEmail())
            result.put("guest_phone", booking.getGuestPhone())
            result.put("notes", booking.getNotes())
            result.put("dogs_count", booking.getDogsCount())
            result.put("status", booking.getStatus())
            outjson.put("data", result)
        } catch (Exception e) {
            outjson.put("_Success", false)
            outjson.put("_ErrorMessage", e.message)
        }
    }
    
    void updateBooking(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            // Using PerstStorageManager directly
            long oid = injson.getLong("id")
            JSONObject data = injson.getJSONObject("data")
            Booking booking = PerstStorageManager.getByOid(Booking.class, oid)
            if (booking == null) {
                outjson.put("_Success", false)
                outjson.put("_ErrorMessage", "Booking not found")
                return
            }
            if (data.has("house_id")) booking.setHouseId(data.getInt("house_id"))
            if (data.has("check_in_date")) booking.setCheckInDate(data.getString("check_in_date"))
            if (data.has("check_out_date")) booking.setCheckOutDate(data.getString("check_out_date"))
            if (data.has("guest_name")) booking.setGuestName(data.getString("guest_name"))
            if (data.has("guest_email")) booking.setGuestEmail(data.getString("guest_email"))
            if (data.has("guest_phone")) booking.setGuestPhone(data.getString("guest_phone"))
            if (data.has("notes")) booking.setNotes(data.getString("notes"))
            if (data.has("dogs_count")) booking.setDogsCount(data.getInt("dogs_count"))
            if (data.has("status")) booking.setStatus(data.getString("status"))
            
            def tc = PerstStorageManager.createContainer()
            tc.addUpdate(booking)
            if (!PerstStorageManager.store(tc)) {
                outjson.put("_Success", false)
                outjson.put("_ErrorMessage", "Failed to update booking")
                return
            }
            JSONObject result = new JSONObject()
            result.put("id", booking.getOid())
            result.put("house_id", booking.getHouseId())
            result.put("check_in_date", booking.getCheckInDate())
            result.put("check_out_date", booking.getCheckOutDate())
            result.put("guest_name", booking.getGuestName())
            result.put("guest_email", booking.getGuestEmail())
            result.put("guest_phone", booking.getGuestPhone())
            result.put("notes", booking.getNotes())
            result.put("dogs_count", booking.getDogsCount())
            result.put("status", booking.getStatus())
            outjson.put("data", result)
        } catch (Exception e) {
            outjson.put("_Success", false)
            outjson.put("_ErrorMessage", e.message)
        }
    }
    
    void deleteBooking(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            // Using PerstStorageManager directly
            long oid = injson.getLong("id")
            Booking booking = PerstStorageManager.getByOid(Booking.class, oid)
            if (booking == null) {
                outjson.put("_Success", false)
                outjson.put("_ErrorMessage", "Booking not found")
                return
            }
            def tc = PerstStorageManager.createContainer()
            tc.addDelete(booking)
            if (!PerstStorageManager.store(tc)) {
                outjson.put("_Success", false)
                outjson.put("_ErrorMessage", "Failed to delete booking")
                return
            }
            outjson.put("_Success", true)
        } catch (Exception e) {
            outjson.put("_Success", false)
            outjson.put("_ErrorMessage", e.message)
        }
    }
    
    void getBookingsByHouse(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            // Using PerstStorageManager directly
            int houseId = injson.getInt("houseId")
            // Filter bookings by house_id
            Collection<Booking> allBookings = PerstStorageManager.getAll(Booking.class)
            Collection<Booking> bookings = allBookings.findAll { it.getHouseId() == houseId }
            JSONArray rows = new JSONArray()
            for (Booking booking : bookings) {
                JSONObject row = new JSONObject()
                row.put("id", booking.getOid())
                row.put("house_id", booking.getHouseId())
                row.put("check_in_date", booking.getCheckInDate())
                row.put("check_out_date", booking.getCheckOutDate())
                row.put("guest_name", booking.getGuestName())
                row.put("guest_email", booking.getGuestEmail())
                row.put("guest_phone", booking.getGuestPhone())
                row.put("notes", booking.getNotes())
                row.put("dogs_count", booking.getDogsCount())
                row.put("status", booking.getStatus())
                rows.put(row)
            }
            outjson.put("data", rows)
        } catch (Exception e) {
            outjson.put("_Success", false)
            outjson.put("_ErrorMessage", e.message)
        }
    }
    
    void getBookingsByDateRange(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            // Using PerstStorageManager directly
            String startDate = injson.getString("startDate")
            String endDate = injson.getString("endDate")
            // Filter bookings by date range
            Collection<Booking> allBookings = PerstStorageManager.getAll(Booking.class)
            Collection<Booking> bookings = allBookings.findAll { 
                it.getCheckInDate() >= startDate && it.getCheckInDate() <= endDate 
            }
            JSONArray rows = new JSONArray()
            for (Booking booking : bookings) {
                JSONObject row = new JSONObject()
                row.put("id", booking.getOid())
                row.put("house_id", booking.getHouseId())
                row.put("check_in_date", booking.getCheckInDate())
                row.put("check_out_date", booking.getCheckOutDate())
                row.put("guest_name", booking.getGuestName())
                row.put("guest_email", booking.getGuestEmail())
                row.put("guest_phone", booking.getGuestPhone())
                row.put("notes", booking.getNotes())
                row.put("dogs_count", booking.getDogsCount())
                row.put("status", booking.getStatus())
                rows.put(row)
            }
            outjson.put("data", rows)
        } catch (Exception e) {
            outjson.put("_Success", false)
            outjson.put("_ErrorMessage", e.message)
        }
    }
    
    // ==================== SCHEDULES ====================
    
    void getSchedules(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            boolean admin = isAdmin(injson)
            long ownerId = getOwnerId(injson)
            long cleanerId = getCleanerId(injson)
            
            Collection<Schedule> allSchedules = PerstStorageManager.getAll(Schedule.class)
            JSONArray rows = new JSONArray()
            
            for (Schedule schedule : allSchedules) {
                // Authorization:
                // - Admin: sees all schedules
                // - Owner: sees schedules for their houses (via booking → house → owner)
                // - Cleaner: sees only their own schedules
                
                boolean include = false
                
                if (admin) {
                    include = true
                } else if (cleanerId > 0) {
                    // Cleaner: only see their own schedules
                    Cleaner cleaner = schedule.getCleaner()
                    include = (cleaner != null && cleaner.getOid() == cleanerId)
                } else if (ownerId > 0) {
                    // Owner: see schedules for their houses
                    Booking booking = schedule.getBooking()
                    if (booking != null) {
                        House house = booking.getHouse()
                        if (house != null) {
                            Owner owner = house.getOwner()
                            include = (owner != null && owner.getOid() == ownerId)
                        }
                    }
                }
                
                if (!include) continue
                
                JSONObject row = new JSONObject()
                row.put("id", schedule.getOid())
                row.put("cleaner_id", schedule.getCleanerId())
                row.put("booking_id", schedule.getBookingId())
                row.put("date", schedule.getScheduleDate())
                row.put("start_time", schedule.getStartTime())
                row.put("end_time", schedule.getEndTime())
                row.put("notes", schedule.getNotes())
                row.put("status", schedule.getStatus())
                rows.put(row)
            }
            
            outjson.put("data", rows)
        } catch (Exception e) {
            outjson.put("_Success", false)
            outjson.put("_ErrorMessage", e.message)
        }
    }
    
    void getSchedule(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            // Using PerstStorageManager directly
            long oid = injson.getLong("id")
            Schedule schedule = PerstStorageManager.getByOid(Schedule.class, oid)
            if (schedule == null) {
                outjson.put("_Success", false)
                outjson.put("_ErrorMessage", "Schedule not found")
                return
            }
            JSONObject data = new JSONObject()
            data.put("id", schedule.getOid())
            data.put("cleaner_id", schedule.getCleanerId())
            data.put("booking_id", schedule.getBookingId())
            data.put("date", schedule.getScheduleDate())
            data.put("start_time", schedule.getStartTime())
            data.put("end_time", schedule.getEndTime())
            data.put("notes", schedule.getNotes())
            data.put("status", schedule.getStatus())
            outjson.put("data", data)
        } catch (Exception e) {
            outjson.put("_Success", false)
            outjson.put("_ErrorMessage", e.message)
        }
    }
    
    void createSchedule(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            // Using PerstStorageManager directly
            JSONObject data = injson.getJSONObject("data")
            int cleanerId = data.getInt("cleaner_id")
            int bookingId = data.getInt("booking_id")
            String date = data.getString("date")
            String startTime = data.getString("start_time")
            String endTime = data.getString("end_time")
            String notes = data.getString("notes", "")
            String status = data.getString("status", "scheduled")
            if (status == null || status.isEmpty()) {
                status = "scheduled"
            }
            
            def schedule = new Schedule(cleanerId, bookingId, date, startTime, endTime, notes)
            schedule.setStatus(status)
            
            def tc = PerstStorageManager.createContainer()
            tc.addInsert(schedule)
            if (!PerstStorageManager.store(tc)) {
                outjson.put("_Success", false)
                outjson.put("_ErrorMessage", "Failed to save schedule")
                return
            }
            
            JSONObject result = new JSONObject()
            result.put("id", schedule.getOid())
            result.put("cleaner_id", cleanerId)
            result.put("booking_id", bookingId)
            result.put("date", date)
            result.put("start_time", startTime)
            result.put("end_time", endTime)
            result.put("notes", notes)
            result.put("status", status)
            outjson.put("data", result)
        } catch (Exception e) {
            outjson.put("_Success", false)
            outjson.put("_ErrorMessage", e.message)
        }
    }
    
    void updateSchedule(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            // Using PerstStorageManager directly
            long oid = injson.getLong("id")
            JSONObject data = injson.getJSONObject("data")
            Schedule schedule = PerstStorageManager.getByOid(Schedule.class, oid)
            if (schedule == null) {
                outjson.put("_Success", false)
                outjson.put("_ErrorMessage", "Schedule not found")
                return
            }
            if (data.has("cleaner_id")) schedule.setCleanerId(data.getInt("cleaner_id"))
            if (data.has("booking_id")) schedule.setBookingId(data.getInt("booking_id"))
            if (data.has("date")) schedule.setScheduleDate(data.getString("date"))
            if (data.has("start_time")) schedule.setStartTime(data.getString("start_time"))
            if (data.has("end_time")) schedule.setEndTime(data.getString("end_time"))
            if (data.has("notes")) schedule.setNotes(data.getString("notes"))
            if (data.has("status")) schedule.setStatus(data.getString("status"))
            
            def tc = PerstStorageManager.createContainer()
            tc.addUpdate(schedule)
            if (!PerstStorageManager.store(tc)) {
                outjson.put("_Success", false)
                outjson.put("_ErrorMessage", "Failed to update schedule")
                return
            }
            JSONObject result = new JSONObject()
            result.put("id", schedule.getOid())
            result.put("cleaner_id", schedule.getCleanerId())
            result.put("booking_id", schedule.getBookingId())
            result.put("date", schedule.getScheduleDate())
            result.put("start_time", schedule.getStartTime())
            result.put("end_time", schedule.getEndTime())
            result.put("notes", schedule.getNotes())
            result.put("status", schedule.getStatus())
            outjson.put("data", result)
        } catch (Exception e) {
            outjson.put("_Success", false)
            outjson.put("_ErrorMessage", e.message)
        }
    }
    
    void deleteSchedule(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            // Using PerstStorageManager directly
            long oid = injson.getLong("id")
            Schedule schedule = PerstStorageManager.getByOid(Schedule.class, oid)
            if (schedule == null) {
                outjson.put("_Success", false)
                outjson.put("_ErrorMessage", "Schedule not found")
                return
            }
            def tc = PerstStorageManager.createContainer()
            tc.addDelete(schedule)
            if (!PerstStorageManager.store(tc)) {
                outjson.put("_Success", false)
                outjson.put("_ErrorMessage", "Failed to delete schedule")
                return
            }
            outjson.put("_Success", true)
        } catch (Exception e) {
            outjson.put("_Success", false)
            outjson.put("_ErrorMessage", e.message)
        }
    }
    
    void getSchedulesByCleaner(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            // Using PerstStorageManager directly
            int cleanerId = injson.getInt("cleanerId")
            Collection<Schedule> allSchedules = PerstStorageManager.getAll(Schedule.class)
            Collection<Schedule> schedules = allSchedules.findAll { it.getCleanerId() == cleanerId }
            JSONArray rows = new JSONArray()
            for (Schedule schedule : schedules) {
                JSONObject row = new JSONObject()
                row.put("id", schedule.getOid())
                row.put("cleaner_id", schedule.getCleanerId())
                row.put("booking_id", schedule.getBookingId())
                row.put("date", schedule.getScheduleDate())
                row.put("start_time", schedule.getStartTime())
                row.put("end_time", schedule.getEndTime())
                row.put("notes", schedule.getNotes())
                row.put("status", schedule.getStatus())
                rows.put(row)
            }
            outjson.put("data", rows)
        } catch (Exception e) {
            outjson.put("_Success", false)
            outjson.put("_ErrorMessage", e.message)
        }
    }
    
    void getSchedulesByBooking(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            // Using PerstStorageManager directly
            int bookingId = injson.getInt("bookingId")
            Collection<Schedule> allSchedules = PerstStorageManager.getAll(Schedule.class)
            Collection<Schedule> schedules = allSchedules.findAll { it.getBookingId() == bookingId }
            JSONArray rows = new JSONArray()
            for (Schedule schedule : schedules) {
                JSONObject row = new JSONObject()
                row.put("id", schedule.getOid())
                row.put("cleaner_id", schedule.getCleanerId())
                row.put("booking_id", schedule.getBookingId())
                row.put("date", schedule.getScheduleDate())
                row.put("start_time", schedule.getStartTime())
                row.put("end_time", schedule.getEndTime())
                row.put("notes", schedule.getNotes())
                row.put("status", schedule.getStatus())
                rows.put(row)
            }
            outjson.put("data", rows)
        } catch (Exception e) {
            outjson.put("_Success", false)
            outjson.put("_ErrorMessage", e.message)
        }
    }
    
    void getSchedulesByDateRange(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            // Using PerstStorageManager directly
            String startDate = injson.getString("startDate")
            String endDate = injson.getString("endDate")
            Collection<Schedule> allSchedules = PerstStorageManager.getAll(Schedule.class)
            Collection<Schedule> schedules = allSchedules.findAll { 
                it.getScheduleDate() >= startDate && it.getScheduleDate() <= endDate 
            }
            JSONArray rows = new JSONArray()
            for (Schedule schedule : schedules) {
                JSONObject row = new JSONObject()
                row.put("id", schedule.getOid())
                row.put("cleaner_id", schedule.getCleanerId())
                row.put("booking_id", schedule.getBookingId())
                row.put("date", schedule.getScheduleDate())
                row.put("start_time", schedule.getStartTime())
                row.put("end_time", schedule.getEndTime())
                row.put("notes", schedule.getNotes())
                row.put("status", schedule.getStatus())
                rows.put(row)
            }
            outjson.put("data", rows)
        } catch (Exception e) {
            outjson.put("_Success", false)
            outjson.put("_ErrorMessage", e.message)
        }
    }
    
    // ==================== HOUSES ====================
    
    void getHouses(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            Collection<House> allHouses = PerstStorageManager.getAll(House.class)
            JSONArray rows = new JSONArray()
            long ownerId = getOwnerId(injson)
            long cleanerId = getCleanerId(injson)
            boolean admin = isAdmin(injson)
            
            for (House house : allHouses) {
                // Authorization:
                // - Admin: sees all houses
                // - Owner: sees their own houses
                // - Cleaner: sees houses assigned to their schedules (via booking)
                boolean include = false
                
                if (admin) {
                    include = true
                } else if (ownerId > 0) {
                    // Owner: check if house belongs to them
                    Owner owner = house.getOwner()
                    include = (owner != null && owner.getOid() == ownerId)
                } else if (cleanerId > 0) {
                    // Cleaner: see houses they need to clean (via their schedules)
                    // This is handled in getSchedules - for now, show all active houses
                    include = house.isActive()
                }
                
                if (!include) continue
                
                JSONObject row = new JSONObject()
                row.put("id", house.getOid())
                row.put("name", house.getName())
                row.put("address", house.getAddress())
                row.put("description", house.getDescription())
                // Hide owner info from cleaners
                if (admin || ownerId > 0) {
                    row.put("owner", house.getOwnerOid())
                }
                row.put("cost_profile", house.getCostProfileOid())  // OO ref → OID
                row.put("active", house.isActive())
                row.put("check_in_time", house.getCheckInTime())
                row.put("check_out_time", house.getCheckOutTime())
                // Cost calculation fields
                row.put("surface_m2", house.getSurfaceM2())
                row.put("floors", house.getFloors())
                row.put("bedrooms", house.getBedrooms())
                row.put("bathrooms", house.getBathrooms())
                row.put("luxury_level", house.getLuxuryLevel())
                rows.put(row)
            }
            
            outjson.put("data", rows)
        } catch (Exception e) {
            outjson.put("_Success", false)
            outjson.put("_ErrorMessage", e.message)
        }
    }
    
    void getHouse(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            // Using PerstStorageManager directly
            long oid = injson.getLong("id")
            House house = PerstStorageManager.getByOid(House.class, oid)
            if (house == null) {
                outjson.put("_Success", false)
                outjson.put("_ErrorMessage", "House not found")
                return
            }
            JSONObject data = new JSONObject()
            data.put("id", house.getOid())
            data.put("name", house.getName())
            data.put("address", house.getAddress())
            data.put("description", house.getDescription())
            data.put("owner", house.getOwnerOid())  // OO ref → OID
            data.put("cost_profile", house.getCostProfileOid())  // OO ref → OID
            data.put("active", house.isActive())
            data.put("check_in_time", house.getCheckInTime())
            data.put("check_out_time", house.getCheckOutTime())
            // Cost calculation fields
            data.put("surface_m2", house.getSurfaceM2())
            data.put("floors", house.getFloors())
            data.put("bedrooms", house.getBedrooms())
            data.put("bathrooms", house.getBathrooms())
            data.put("luxury_level", house.getLuxuryLevel())
            outjson.put("data", data)
        } catch (Exception e) {
            outjson.put("_Success", false)
            outjson.put("_ErrorMessage", e.message)
        }
    }
    
    void getHousesByOwner(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            long ownerId = injson.getLong("owner_id")
            Collection<House> houses = PerstStorageManager.getAll(House.class)
            JSONArray rows = new JSONArray()
            
            for (House house : houses) {
                if (house.getOwnerOid() == ownerId) {
                    JSONObject row = new JSONObject()
                    row.put("id", house.getOid())
                    row.put("name", house.getName())
                    row.put("address", house.getAddress())
                    row.put("description", house.getDescription())
                    row.put("owner", house.getOwnerOid())
                    row.put("active", house.isActive())
                    row.put("check_in_time", house.getCheckInTime())
                    row.put("check_out_time", house.getCheckOutTime())
                    rows.put(row)
                }
            }
            
            outjson.put("data", rows)
        } catch (Exception e) {
            outjson.put("_Success", false)
            outjson.put("_ErrorMessage", e.message)
        }
    }
    
    void createHouse(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            // Using PerstStorageManager directly
            JSONObject data = injson.getJSONObject("data")
            String name = data.getString("name")
            String address = data.getString("address", "")
            String description = data.getString("description", "")
            boolean active = data.getBoolean("active", true)
            String checkInTime = data.getString("check_in_time", "16:00")
            String checkOutTime = data.getString("check_out_time", "10:00")
            
            House house = new House(name, address, description, active)
            house.setCheckInTime(checkInTime)
            house.setCheckOutTime(checkOutTime)
            
            // Set Owner from OID (OO reference)
            long ownerOid = data.getLong("owner", 0)
            if (ownerOid > 0) {
                Owner owner = PerstStorageManager.getByOid(Owner.class, ownerOid)
                house.setOwner(owner)
            }
            
            // Set CostProfile from OID (OO reference)
            long costProfileOid = data.getLong("cost_profile", 0)
            if (costProfileOid > 0) {
                CostProfile costProfile = PerstStorageManager.getByOid(CostProfile.class, costProfileOid)
                house.setCostProfile(costProfile)
            }
            
            // Set cost calculation fields
            if (data.has("surface_m2")) house.setSurfaceM2(data.getDouble("surface_m2"))
            if (data.has("floors")) house.setFloors(data.getInt("floors"))
            if (data.has("bedrooms")) house.setBedrooms(data.getInt("bedrooms"))
            if (data.has("bathrooms")) house.setBathrooms(data.getInt("bathrooms"))
            if (data.has("luxury_level")) house.setLuxuryLevel(data.getString("luxury_level"))
            
            def tc = PerstStorageManager.createContainer()
            tc.addInsert(house)
            if (!PerstStorageManager.store(tc)) {
                outjson.put("_Success", false)
                outjson.put("_ErrorMessage", "Failed to create house")
                return
            }
            JSONObject result = houseToJson(house)
            outjson.put("data", result)
        } catch (Exception e) {
            outjson.put("_Success", false)
            outjson.put("_ErrorMessage", e.message)
        }
    }
    
    void updateHouse(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            // Using PerstStorageManager directly
            long oid = injson.getLong("id")
            JSONObject data = injson.getJSONObject("data")
            House house = PerstStorageManager.getByOid(House.class, oid)
            if (house == null) {
                outjson.put("_Success", false)
                outjson.put("_ErrorMessage", "House not found")
                return
            }
            if (data.has("name")) house.setName(data.getString("name"))
            if (data.has("address")) house.setAddress(data.getString("address"))
            if (data.has("description")) house.setDescription(data.getString("description"))
            if (data.has("active")) house.setActive(data.getBoolean("active"))
            if (data.has("check_in_time")) house.setCheckInTime(data.getString("check_in_time"))
            if (data.has("check_out_time")) house.setCheckOutTime(data.getString("check_out_time"))
            
            // Update Owner from OID (OO reference)
            if (data.has("owner")) {
                long ownerOid = data.getLong("owner")
                if (ownerOid > 0) {
                    Owner owner = PerstStorageManager.getByOid(Owner.class, ownerOid)
                    house.setOwner(owner)
                } else {
                    house.setOwner(null)
                }
            }
            
            // Update CostProfile from OID (OO reference)
            if (data.has("cost_profile")) {
                long costProfileOid = data.getLong("cost_profile")
                if (costProfileOid > 0) {
                    CostProfile costProfile = PerstStorageManager.getByOid(CostProfile.class, costProfileOid)
                    house.setCostProfile(costProfile)
                } else {
                    house.setCostProfile(null)
                }
            }
            
            // Update cost calculation fields
            if (data.has("surface_m2")) house.setSurfaceM2(data.getDouble("surface_m2"))
            if (data.has("floors")) house.setFloors(data.getInt("floors"))
            if (data.has("bedrooms")) house.setBedrooms(data.getInt("bedrooms"))
            if (data.has("bathrooms")) house.setBathrooms(data.getInt("bathrooms"))
            if (data.has("luxury_level")) house.setLuxuryLevel(data.getString("luxury_level"))
            
            def tc = PerstStorageManager.createContainer()
            tc.addUpdate(house)
            if (!PerstStorageManager.store(tc)) {
                outjson.put("_Success", false)
                outjson.put("_ErrorMessage", "Failed to update house")
                return
            }
            JSONObject result = houseToJson(house)
            outjson.put("data", result)
        } catch (Exception e) {
            outjson.put("_Success", false)
            outjson.put("_ErrorMessage", e.message)
        }
    }
    
    void deleteHouse(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            // Using PerstStorageManager directly
            long oid = injson.getLong("id")
            House house = PerstStorageManager.getByOid(House.class, oid)
            if (house == null) {
                outjson.put("_Success", false)
                outjson.put("_ErrorMessage", "House not found")
                return
            }
            def tc = PerstStorageManager.createContainer()
            tc.addDelete(house)
            if (!PerstStorageManager.store(tc)) {
                outjson.put("_Success", false)
                outjson.put("_ErrorMessage", "Failed to delete house")
                return
            }
            outjson.put("_Success", true)
        } catch (Exception e) {
            outjson.put("_Success", false)
            outjson.put("_ErrorMessage", e.message)
        }
    }
    
    // ==================== OWNERS ====================
    
    void getOwners(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            // Using PerstStorageManager directly
            Collection<Owner> owners = PerstStorageManager.getAll(Owner.class)
            JSONArray rows = new JSONArray()
            
            for (Owner owner : owners) {
                JSONObject row = new JSONObject()
                row.put("id", owner.getOid())
                row.put("name", owner.getName())
                row.put("email", owner.getEmail())
                row.put("phone", owner.getPhone())
                row.put("address", owner.getAddress())
                row.put("active", owner.isActive())
                // canLogin = user is active (PerstUser always exists for Owner)
                PerstUser user = owner.getUser()
                row.put("canLogin", user != null && user.isActive())
                rows.put(row)
            }
            
            outjson.put("data", rows)
        } catch (Exception e) {
            outjson.put("_Success", false)
            outjson.put("_ErrorMessage", e.message)
        }
    }
    
    void getOwner(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            // Using PerstStorageManager directly
            long oid = injson.getLong("id")
            Owner owner = PerstStorageManager.getByOid(Owner.class, oid)
            if (owner == null) {
                outjson.put("_Success", false)
                outjson.put("_ErrorMessage", "Owner not found")
                return
            }
            JSONObject data = new JSONObject()
            data.put("id", owner.getOid())
            data.put("name", owner.getName())
            data.put("email", owner.getEmail())
            data.put("phone", owner.getPhone())
            data.put("address", owner.getAddress())
            data.put("active", owner.isActive())
            // canLogin = user is active (PerstUser always exists for Owner)
            PerstUser user = owner.getUser()
            data.put("canLogin", user != null && user.isActive())
            outjson.put("data", data)
        } catch (Exception e) {
            outjson.put("_Success", false)
            outjson.put("_ErrorMessage", e.message)
        }
    }

    void getOwnerByUserId(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            // Using PerstStorageManager directly
            int userId = injson.getInt("userId")
            Collection<Owner> allOwners = PerstStorageManager.getAll(Owner.class)
            Owner found = null
            for (Owner owner : allOwners) {
                PerstUser user = owner.getPerstUser()
                if (user != null && user.getUserId() == userId) {
                    found = owner
                    break
                }
            }
            if (found == null) {
                outjson.put("_Success", false)
                outjson.put("_ErrorMessage", "Owner not found for user")
                return
            }
            JSONObject data = new JSONObject()
            data.put("id", found.getOid())
            data.put("name", found.getName())
            data.put("email", found.getEmail())
            data.put("phone", found.getPhone())
            data.put("address", found.getAddress())
            data.put("active", found.isActive())
            data.put("userId", found.getPerstUser()?.getOid() ?: 0)
            outjson.put("data", data)
        } catch (Exception e) {
            outjson.put("_Success", false)
            outjson.put("_ErrorMessage", e.message)
        }
    }
    
    void getUserByOwnerId(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            // Using PerstStorageManager directly
            long ownerId = injson.getLong("ownerId")
            Owner owner = PerstStorageManager.getByOid(Owner.class, ownerId)
            if (owner == null) {
                outjson.put("_Success", false)
                outjson.put("_ErrorMessage", "Owner not found")
                return
            }
            PerstUser user = owner.getPerstUser()
            if (user == null) {
                outjson.put("_Success", false)
                outjson.put("_ErrorMessage", "User not found for owner")
                return
            }
            JSONObject data = new JSONObject()
            data.put("id", user.getOid())
            data.put("username", user.getUsername())
            data.put("email", user.getEmail())
            data.put("active", user.isActive())
            data.put("emailVerified", user.isEmailVerified())
            data.put("ownerId", owner.getOid())
            data.put("userId", user.getUserId())
            outjson.put("data", data)
        } catch (Exception e) {
            outjson.put("_Success", false)
            outjson.put("_ErrorMessage", e.message)
        }
    }
    
    void createOwner(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            JSONObject data = injson.getJSONObject("data")
            String name = data.getString("name")
            String email = data.getString("email", "")
            String phone = data.getString("phone", "")
            String address = data.getString("address", "")
            boolean active = data.getBoolean("active", true)
            
            // Owner constructor auto-creates a deactivated PerstUser
            Owner owner = new Owner(name, email, phone, address)
            owner.setActive(active)
            
            println "[Cleaning] createOwner: owner OID=${owner.getOid()}, perstUser=${owner.getPerstUser()?.username}"
            
            def tc = PerstStorageManager.createContainer()
            tc.addInsert(owner)
            tc.addInsert(owner.getPerstUser())
            def storeResult = PerstStorageManager.store(tc)
            println "[Cleaning] createOwner: store result=${storeResult}"
            if (!storeResult) {
                outjson.put("_Success", false)
                outjson.put("_ErrorMessage", "Failed to create owner")
                return
            }
            JSONObject result = new JSONObject()
            result.put("id", owner.getOid())
            result.put("name", owner.getName())
            result.put("email", owner.getEmail())
            result.put("phone", owner.getPhone())
            result.put("address", owner.getAddress())
            result.put("active", owner.isActive())
            outjson.put("data", result)
        } catch (Exception e) {
            outjson.put("_Success", false)
            outjson.put("_ErrorMessage", e.message)
            e.printStackTrace()
        }
    }
    
    void updateOwner(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            // Using PerstStorageManager directly
            long oid = injson.getLong("id")
            JSONObject data = injson.getJSONObject("data")
            Owner owner = PerstStorageManager.getByOid(Owner.class, oid)
            if (owner == null) {
                outjson.put("_Success", false)
                outjson.put("_ErrorMessage", "Owner not found")
                return
            }
            if (data.has("name")) owner.setName(data.getString("name"))
            if (data.has("email")) owner.setEmail(data.getString("email"))
            if (data.has("phone")) owner.setPhone(data.getString("phone"))
            if (data.has("address")) owner.setAddress(data.getString("address"))
            if (data.has("active")) owner.setActive(data.getBoolean("active"))
            
            def tc = PerstStorageManager.createContainer()
            tc.addUpdate(owner)
            if (!PerstStorageManager.store(tc)) {
                outjson.put("_Success", false)
                outjson.put("_ErrorMessage", "Failed to update owner")
                return
            }
            JSONObject result = new JSONObject()
            result.put("id", owner.getOid())
            result.put("name", owner.getName())
            result.put("email", owner.getEmail())
            result.put("phone", owner.getPhone())
            result.put("address", owner.getAddress())
            result.put("active", owner.isActive())
            outjson.put("data", result)
        } catch (Exception e) {
            outjson.put("_Success", false)
            outjson.put("_ErrorMessage", e.message)
        }
    }

    /**
     * Toggle login capability for an owner.
     * 
     * When enabling: Sends verification email with link. Owner must verify email AND set password before login works.
     * When disabling: Immediately deactivates login.
     *
     * Input JSON: { "id": <owner_oid>, "canLogin": true|false }
     */
    void toggleOwnerLogin(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            long oid = injson.getLong("id")
            boolean canLogin = injson.getBoolean("canLogin")
            Owner owner = PerstStorageManager.getByOid(Owner.class, oid)
            if (owner == null) {
                outjson.put("_Success", false)
                outjson.put("_ErrorMessage", "Owner not found")
                return
            }

            PerstUser user = owner.getUser()
            if (user == null) {
                outjson.put("_Success", false)
                outjson.put("_ErrorMessage", "Owner has no PerstUser (data error)")
                return
            }

            // Check if owner has email
            String ownerEmail = owner.getEmail()
            if (!ownerEmail || ownerEmail.isEmpty()) {
                outjson.put("_Success", false)
                outjson.put("_ErrorMessage", "Owner has no email address")
                return
            }

            if (canLogin) {
                // Enable login - need email verification first
                // Generate verification token
                user.generateVerificationToken()
                
                // Get base URL from config
                String baseUrl = org.kissweb.restServer.MainServlet.getEnvironment("app.baseUrl") ?: "http://localhost:5173"
                
                // Send verification email via EmailService
                boolean emailSent = false
                try {
                    emailSent = (Boolean) Class.forName("services.EmailService")
                        .getMethod("sendVerificationEmail", String.class, String.class, String.class, String.class)
                        .invoke(null, ownerEmail, owner.getName(), user.getVerificationToken(), baseUrl)
                } catch (Exception e) {
                    println "[Cleaning] Failed to send email via EmailService: " + e.message
                    e.printStackTrace()
                }
                
                def tc = PerstStorageManager.createContainer()
                tc.addUpdate(user)
                PerstStorageManager.store(tc)
                
                if (emailSent) {
                    outjson.put("_Success", true)
                    outjson.put("message", "Verification email sent to " + ownerEmail + ". User must verify email and set password before logging in.")
                } else {
                    outjson.put("_Success", false)
                    outjson.put("_ErrorMessage", "Failed to send verification email. SMTP may not be configured.")
                }
            } else {
                // Disable login - immediately deactivate
                user.setActive(false)
                def tc = PerstStorageManager.createContainer()
                tc.addUpdate(user)
                PerstStorageManager.store(tc)

                outjson.put("_Success", true)
                outjson.put("canLogin", false)
                outjson.put("message", "Owner login deactivated")
            }
        } catch (Exception e) {
            outjson.put("_Success", false)
            outjson.put("_ErrorMessage", e.message)
        }
    }

    void deleteOwner(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            // Using PerstStorageManager directly
            long oid = injson.getLong("id")
            Owner owner = PerstStorageManager.getByOid(Owner.class, oid)
            if (owner == null) {
                outjson.put("_Success", false)
                outjson.put("_ErrorMessage", "Owner not found")
                return
            }
            def tc = PerstStorageManager.createContainer()
            tc.addDelete(owner)
            if (!PerstStorageManager.store(tc)) {
                outjson.put("_Success", false)
                outjson.put("_ErrorMessage", "Failed to delete owner")
                return
            }
            outjson.put("_Success", true)
        } catch (Exception e) {
            outjson.put("_Success", false)
            outjson.put("_ErrorMessage", e.message)
        }
    }
    
    // ==================== COST PROFILES ====================
    
    void getCostProfiles(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            // Using PerstStorageManager directly
            Collection<CostProfile> profiles = CostProfileManager.getAll()
            JSONArray rows = new JSONArray()
            
            for (CostProfile profile : profiles) {
                JSONObject row = new JSONObject()
                row.put("id", profile.getOid())
                row.put("name", profile.getName())
                row.put("is_standard", profile.isStandard())
                row.put("owner", profile.getOwner()?.getOid() ?: 0)
                row.put("base_hourly_rate", profile.getBaseHourlyRate())
                row.put("minimum_charge", profile.getMinimumCharge())
                row.put("rate_per_m2", profile.getRatePerM2())
                row.put("rate_per_floor", profile.getRatePerFloor())
                row.put("rate_per_bedroom", profile.getRatePerBedroom())
                row.put("rate_per_bathroom", profile.getRatePerBathroom())
                row.put("dog_surcharge", profile.getDogSurcharge())
                row.put("basic_multiplier", profile.getBasicMultiplier())
                row.put("standard_multiplier", profile.getStandardMultiplier())
                row.put("premium_multiplier", profile.getPremiumMultiplier())
                row.put("luxury_multiplier", profile.getLuxuryMultiplier())
                row.put("active", profile.isActive())
                rows.put(row)
            }
            
            outjson.put("data", rows)
        } catch (Exception e) {
            outjson.put("_Success", false)
            outjson.put("_ErrorMessage", e.message)
        }
    }
    
    void getCostProfile(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            // Using PerstStorageManager directly
            long oid = injson.getLong("id")
            CostProfile profile = CostProfileManager.getByOid(oid)
            if (profile == null) {
                outjson.put("_Success", false)
                outjson.put("_ErrorMessage", "Cost profile not found")
                return
            }
            JSONObject data = costProfileToJson(profile)
            outjson.put("data", data)
        } catch (Exception e) {
            outjson.put("_Success", false)
            outjson.put("_ErrorMessage", e.message)
        }
    }
    
    void getStandardCostProfile(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            CostProfile profile = CostProfileManager.getStandard()
            if (profile == null) {
                outjson.put("_Success", false)
                outjson.put("_ErrorMessage", "No standard cost profile found")
                return
            }
            JSONObject data = costProfileToJson(profile)
            outjson.put("data", data)
        } catch (Exception e) {
            outjson.put("_Success", false)
            outjson.put("_ErrorMessage", e.message)
        }
    }
    
    void createCostProfile(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            // Using PerstStorageManager directly
            JSONObject data = injson.getJSONObject("data")
            
            CostProfile profile = new CostProfile()
            profile.setName(data.getString("name"))
            profile.setStandard(data.getBoolean("is_standard", false))
            profile.setBaseHourlyRate(data.getDouble("base_hourly_rate", 25.0))
            profile.setMinimumCharge(data.getDouble("minimum_charge", 75.0))
            profile.setRatePerM2(data.getDouble("rate_per_m2", 0.15))
            profile.setRatePerFloor(data.getDouble("rate_per_floor", 15.0))
            profile.setRatePerBedroom(data.getDouble("rate_per_bedroom", 10.0))
            profile.setRatePerBathroom(data.getDouble("rate_per_bathroom", 15.0))
            profile.setDogSurcharge(data.getDouble("dog_surcharge", 20.0))
            profile.setBasicMultiplier(data.getDouble("basic_multiplier", 1.0))
            profile.setStandardMultiplier(data.getDouble("standard_multiplier", 1.0))
            profile.setPremiumMultiplier(data.getDouble("premium_multiplier", 1.25))
            profile.setLuxuryMultiplier(data.getDouble("luxury_multiplier", 1.5))
            profile.setActive(data.getBoolean("active", true))
            
            // Set owner if provided
            long ownerOid = data.getLong("owner", 0)
            if (ownerOid > 0) {
                Owner owner = PerstStorageManager.getByOid(Owner.class, ownerOid)
                profile.setOwner(owner)
            }
            
            if (!CostProfileManager.create(profile)) {
                outjson.put("_Success", false)
                outjson.put("_ErrorMessage", "Failed to create cost profile")
                return
            }
            
            JSONObject result = costProfileToJson(profile)
            outjson.put("data", result)
        } catch (Exception e) {
            outjson.put("_Success", false)
            outjson.put("_ErrorMessage", e.message)
        }
    }
    
    void updateCostProfile(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            // Using PerstStorageManager directly
            long oid = injson.getLong("id")
            JSONObject data = injson.getJSONObject("data")
            
            CostProfile profile = CostProfileManager.getByOid(oid)
            if (profile == null) {
                outjson.put("_Success", false)
                outjson.put("_ErrorMessage", "Cost profile not found")
                return
            }
            
            if (data.has("name")) profile.setName(data.getString("name"))
            if (data.has("is_standard")) profile.setStandard(data.getBoolean("is_standard"))
            if (data.has("base_hourly_rate")) profile.setBaseHourlyRate(data.getDouble("base_hourly_rate"))
            if (data.has("minimum_charge")) profile.setMinimumCharge(data.getDouble("minimum_charge"))
            if (data.has("rate_per_m2")) profile.setRatePerM2(data.getDouble("rate_per_m2"))
            if (data.has("rate_per_floor")) profile.setRatePerFloor(data.getDouble("rate_per_floor"))
            if (data.has("rate_per_bedroom")) profile.setRatePerBedroom(data.getDouble("rate_per_bedroom"))
            if (data.has("rate_per_bathroom")) profile.setRatePerBathroom(data.getDouble("rate_per_bathroom"))
            if (data.has("dog_surcharge")) profile.setDogSurcharge(data.getDouble("dog_surcharge"))
            if (data.has("basic_multiplier")) profile.setBasicMultiplier(data.getDouble("basic_multiplier"))
            if (data.has("standard_multiplier")) profile.setStandardMultiplier(data.getDouble("standard_multiplier"))
            if (data.has("premium_multiplier")) profile.setPremiumMultiplier(data.getDouble("premium_multiplier"))
            if (data.has("luxury_multiplier")) profile.setLuxuryMultiplier(data.getDouble("luxury_multiplier"))
            if (data.has("active")) profile.setActive(data.getBoolean("active"))
            
            // Update owner if provided
            if (data.has("owner")) {
                long ownerOid = data.getLong("owner")
                if (ownerOid > 0) {
                    Owner owner = PerstStorageManager.getByOid(Owner.class, ownerOid)
                    profile.setOwner(owner)
                } else {
                    profile.setOwner(null)
                }
            }
            
            if (!CostProfileManager.update(profile)) {
                outjson.put("_Success", false)
                outjson.put("_ErrorMessage", "Failed to update cost profile")
                return
            }
            
            JSONObject result = costProfileToJson(profile)
            outjson.put("data", result)
        } catch (Exception e) {
            outjson.put("_Success", false)
            outjson.put("_ErrorMessage", e.message)
        }
    }
    
    void deleteCostProfile(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            // Using PerstStorageManager directly
            long oid = injson.getLong("id")
            CostProfile profile = CostProfileManager.getByOid(oid)
            if (profile == null) {
                outjson.put("_Success", false)
                outjson.put("_ErrorMessage", "Cost profile not found")
                return
            }
            if (!CostProfileManager.delete(profile)) {
                outjson.put("_Success", false)
                outjson.put("_ErrorMessage", "Failed to delete cost profile")
                return
            }
            outjson.put("_Success", true)
        } catch (Exception e) {
            outjson.put("_Success", false)
            outjson.put("_ErrorMessage", e.message)
        }
    }
    
    void copyCostProfile(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            // Using PerstStorageManager directly
            long sourceOid = injson.getLong("source_id")
            String newName = injson.getString("name")
            long ownerOid = injson.getLong("owner", 0)
            
            CostProfile source = CostProfileManager.getByOid(sourceOid)
            if (source == null) {
                outjson.put("_Success", false)
                outjson.put("_ErrorMessage", "Source cost profile not found")
                return
            }
            
            Owner owner = ownerOid > 0 ? PerstStorageManager.getByOid(Owner.class, ownerOid) : null
            CostProfile copy = CostProfileManager.copyFrom(source, newName, owner)
            
            if (copy == null) {
                outjson.put("_Success", false)
                outjson.put("_ErrorMessage", "Failed to copy cost profile")
                return
            }
            
            JSONObject result = costProfileToJson(copy)
            outjson.put("data", result)
        } catch (Exception e) {
            outjson.put("_Success", false)
            outjson.put("_ErrorMessage", e.message)
        }
    }
    
    // ==================== COST CALCULATION ====================
    
    void calculateCost(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            // Using PerstStorageManager directly
            long houseOid = injson.getLong("house_id")
            Long bookingOid = injson.has("booking_id") ? injson.getLong("booking_id") : null
            Long profileOid = injson.has("cost_profile_id") ? injson.getLong("cost_profile_id") : null
            
            House house = PerstStorageManager.getByOid(House.class, houseOid)
            if (house == null) {
                outjson.put("_Success", false)
                outjson.put("_ErrorMessage", "House not found")
                return
            }
            
            Booking booking = bookingOid ? PerstStorageManager.getByOid(Booking.class, bookingOid) : null
            CostProfile profile = profileOid ? CostProfileManager.getByOid(profileOid) : house.getCostProfile()
            
            CostService.CostResult result = CostService.calculateCost(house, booking, profile)
            
            JSONObject data = new JSONObject()
            data.put("base_cost", result.getBaseCost())
            data.put("size_cost", result.getSizeCost())
            data.put("room_cost", result.getRoomCost())
            data.put("luxury_multiplier", result.getLuxuryMultiplier())
            data.put("dog_surcharge", result.getDogSurcharge())
            data.put("total", result.getTotal())
            
            JSONArray breakdown = new JSONArray()
            for (String line : result.getBreakdown()) {
                breakdown.put(line)
            }
            data.put("breakdown", breakdown)
            
            outjson.put("_Success", true)
            outjson.put("data", data)
        } catch (Exception e) {
            outjson.put("_Success", false)
            outjson.put("_ErrorMessage", e.message)
        }
    }
    
    void estimateHours(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            // Using PerstStorageManager directly
            long houseOid = injson.getLong("house_id")
            
            House house = PerstStorageManager.getByOid(House.class, houseOid)
            if (house == null) {
                outjson.put("_Success", false)
                outjson.put("_ErrorMessage", "House not found")
                return
            }
            
            double hours = CostService.estimateHours(house)
            outjson.put("_Success", true)
            outjson.put("estimated_hours", hours)
        } catch (Exception e) {
            outjson.put("_Success", false)
            outjson.put("_ErrorMessage", e.message)
        }
    }
    
    // ==================== HELPER METHODS ====================
    
    private JSONObject costProfileToJson(CostProfile profile) {
        JSONObject json = new JSONObject()
        json.put("id", profile.getOid())
        json.put("name", profile.getName())
        json.put("is_standard", profile.isStandard())
        json.put("owner", profile.getOwner()?.getOid() ?: 0)
        json.put("base_hourly_rate", profile.getBaseHourlyRate())
        json.put("minimum_charge", profile.getMinimumCharge())
        json.put("rate_per_m2", profile.getRatePerM2())
        json.put("rate_per_floor", profile.getRatePerFloor())
        json.put("rate_per_bedroom", profile.getRatePerBedroom())
        json.put("rate_per_bathroom", profile.getRatePerBathroom())
        json.put("dog_surcharge", profile.getDogSurcharge())
        json.put("basic_multiplier", profile.getBasicMultiplier())
        json.put("standard_multiplier", profile.getStandardMultiplier())
        json.put("premium_multiplier", profile.getPremiumMultiplier())
        json.put("luxury_multiplier", profile.getLuxuryMultiplier())
        json.put("active", profile.isActive())
        return json
    }
    
    private JSONObject houseToJson(House house) {
        JSONObject json = new JSONObject()
        json.put("id", house.getOid())
        json.put("name", house.getName())
        json.put("address", house.getAddress())
        json.put("description", house.getDescription())
        json.put("owner", house.getOwnerOid())
        json.put("cost_profile", house.getCostProfileOid())
        json.put("active", house.isActive())
        json.put("check_in_time", house.getCheckInTime())
        json.put("check_out_time", house.getCheckOutTime())
        json.put("surface_m2", house.getSurfaceM2())
        json.put("floors", house.getFloors())
        json.put("bedrooms", house.getBedrooms())
        json.put("bathrooms", house.getBathrooms())
        json.put("luxury_level", house.getLuxuryLevel())
        return json
    }
}
