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
import mycompany.domain.Cleaner
import mycompany.domain.Booking
import mycompany.domain.Schedule
import mycompany.domain.House
import mycompany.domain.Owner
import mycompany.database.PerstUserManager
import mycompany.domain.PerstUser
import oodb.PerstConnection

/**
 * Cleaning service for CRUD operations on cleaning scheduler entities.
 * 
 * Uses PerstConnection for Perst OODBMS operations when available.
 */
class Cleaning {

    /**
     * Get PerstConnection from environment if available.
     */
    private PerstConnection getPerst() {
        return (PerstConnection) MainServlet.getEnvironment("PerstConnection")
    }

    // ==================== CLEANERS ====================
    
    void getCleaners(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            PerstConnection perst = getPerst()
            Collection<Cleaner> cleaners = perst.getAll(Cleaner)
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
            PerstConnection perst = getPerst()
            long oid = injson.getLong("id")
            Cleaner cleaner = perst.getByOid(Cleaner, oid)
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
            PerstConnection perst = getPerst()
            JSONObject data = injson.getJSONObject("data")
            String name = data.getString("name")
            String phone = data.getString("phone", "")
            String email = data.getString("email", "")
            String address = data.getString("address", "")
            boolean active = data.getBoolean("active", true)
            
            Cleaner cleaner = new Cleaner(name, phone, email, address, active)
            def tc = perst.perstCreateContainer()
            tc.addInsert(cleaner)
            if (!perst.perstStore(tc)) {
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
        }
    }
    
    void updateCleaner(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            PerstConnection perst = getPerst()
            long oid = injson.getLong("id")
            JSONObject data = injson.getJSONObject("data")
            Cleaner cleaner = perst.getByOid(Cleaner, oid)
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
            
            def tc = perst.perstCreateContainer()
            tc.addUpdate(cleaner)
            if (!perst.perstStore(tc)) {
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
            PerstConnection perst = getPerst()
            long oid = injson.getLong("id")
            Cleaner cleaner = perst.getByOid(Cleaner, oid)
            if (cleaner == null) {
                outjson.put("_Success", false)
                outjson.put("_ErrorMessage", "Cleaner not found")
                return
            }
            def tc = perst.perstCreateContainer()
            tc.addDelete(cleaner)
            if (!perst.perstStore(tc)) {
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
            PerstConnection perst = getPerst()
            Collection<Booking> bookings = perst.getAll(Booking)
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
    
    void getBooking(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            PerstConnection perst = getPerst()
            long oid = injson.getLong("id")
            Booking booking = perst.getByOid(Booking, oid)
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
            PerstConnection perst = getPerst()
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
            def tc = perst.perstCreateContainer()
            tc.addInsert(booking)
            if (!perst.perstStore(tc)) {
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
            PerstConnection perst = getPerst()
            long oid = injson.getLong("id")
            JSONObject data = injson.getJSONObject("data")
            Booking booking = perst.getByOid(Booking, oid)
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
            
            def tc = perst.perstCreateContainer()
            tc.addUpdate(booking)
            if (!perst.perstStore(tc)) {
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
            PerstConnection perst = getPerst()
            long oid = injson.getLong("id")
            Booking booking = perst.getByOid(Booking, oid)
            if (booking == null) {
                outjson.put("_Success", false)
                outjson.put("_ErrorMessage", "Booking not found")
                return
            }
            def tc = perst.perstCreateContainer()
            tc.addDelete(booking)
            if (!perst.perstStore(tc)) {
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
            PerstConnection perst = getPerst()
            int houseId = injson.getInt("houseId")
            // Filter bookings by house_id
            Collection<Booking> allBookings = perst.getAll(Booking)
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
            PerstConnection perst = getPerst()
            String startDate = injson.getString("startDate")
            String endDate = injson.getString("endDate")
            // Filter bookings by date range
            Collection<Booking> allBookings = perst.getAll(Booking)
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
            PerstConnection perst = getPerst()
            Collection<Schedule> schedules = perst.getAll(Schedule)
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
    
    void getSchedule(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            PerstConnection perst = getPerst()
            long oid = injson.getLong("id")
            Schedule schedule = perst.getByOid(Schedule, oid)
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
            PerstConnection perst = getPerst()
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
            
            def tc = perst.perstCreateContainer()
            tc.addInsert(schedule)
            if (!perst.perstStore(tc)) {
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
            PerstConnection perst = getPerst()
            long oid = injson.getLong("id")
            JSONObject data = injson.getJSONObject("data")
            Schedule schedule = perst.getByOid(Schedule, oid)
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
            
            def tc = perst.perstCreateContainer()
            tc.addUpdate(schedule)
            if (!perst.perstStore(tc)) {
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
            PerstConnection perst = getPerst()
            long oid = injson.getLong("id")
            Schedule schedule = perst.getByOid(Schedule, oid)
            if (schedule == null) {
                outjson.put("_Success", false)
                outjson.put("_ErrorMessage", "Schedule not found")
                return
            }
            def tc = perst.perstCreateContainer()
            tc.addDelete(schedule)
            if (!perst.perstStore(tc)) {
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
            PerstConnection perst = getPerst()
            int cleanerId = injson.getInt("cleanerId")
            Collection<Schedule> allSchedules = perst.getAll(Schedule)
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
            PerstConnection perst = getPerst()
            int bookingId = injson.getInt("bookingId")
            Collection<Schedule> allSchedules = perst.getAll(Schedule)
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
            PerstConnection perst = getPerst()
            String startDate = injson.getString("startDate")
            String endDate = injson.getString("endDate")
            Collection<Schedule> allSchedules = perst.getAll(Schedule)
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
            PerstConnection perst = getPerst()
            Collection<House> houses = perst.getAll(House)
            JSONArray rows = new JSONArray()
            
            for (House house : houses) {
                JSONObject row = new JSONObject()
                row.put("id", house.getOid())
                row.put("name", house.getName())
                row.put("address", house.getAddress())
                row.put("description", house.getDescription())
                row.put("owner_id", house.getOwnerId())
                row.put("active", house.isActive())
                row.put("check_in_time", house.getCheckInTime())
                row.put("check_out_time", house.getCheckOutTime())
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
            PerstConnection perst = getPerst()
            long oid = injson.getLong("id")
            House house = perst.getByOid(House, oid)
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
            data.put("owner_id", house.getOwnerId())
            data.put("active", house.isActive())
            data.put("check_in_time", house.getCheckInTime())
            data.put("check_out_time", house.getCheckOutTime())
            outjson.put("data", data)
        } catch (Exception e) {
            outjson.put("_Success", false)
            outjson.put("_ErrorMessage", e.message)
        }
    }
    
    void createHouse(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            PerstConnection perst = getPerst()
            JSONObject data = injson.getJSONObject("data")
            String name = data.getString("name")
            String address = data.getString("address", "")
            String description = data.getString("description", "")
            long ownerId = data.has("owner_id") ? data.getLong("owner_id") : 0
            boolean active = data.getBoolean("active", true)
            String checkInTime = data.getString("check_in_time", "16:00")
            String checkOutTime = data.getString("check_out_time", "10:00")
            
            House house = new House(name, address, description, ownerId, active, checkInTime, checkOutTime)
            def tc = perst.perstCreateContainer()
            tc.addInsert(house)
            if (!perst.perstStore(tc)) {
                outjson.put("_Success", false)
                outjson.put("_ErrorMessage", "Failed to create house")
                return
            }
            JSONObject result = new JSONObject()
            result.put("id", house.getOid())
            result.put("name", house.getName())
            result.put("address", house.getAddress())
            result.put("description", house.getDescription())
            result.put("owner_id", house.getOwnerId())
            result.put("active", house.isActive())
            result.put("check_in_time", house.getCheckInTime())
            result.put("check_out_time", house.getCheckOutTime())
            outjson.put("data", result)
        } catch (Exception e) {
            outjson.put("_Success", false)
            outjson.put("_ErrorMessage", e.message)
        }
    }
    
    void updateHouse(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            PerstConnection perst = getPerst()
            long oid = injson.getLong("id")
            JSONObject data = injson.getJSONObject("data")
            House house = perst.getByOid(House, oid)
            if (house == null) {
                outjson.put("_Success", false)
                outjson.put("_ErrorMessage", "House not found")
                return
            }
            if (data.has("name")) house.setName(data.getString("name"))
            if (data.has("address")) house.setAddress(data.getString("address"))
            if (data.has("description")) house.setDescription(data.getString("description"))
            if (data.has("owner_id")) house.setOwnerId(data.getLong("owner_id"))
            if (data.has("active")) house.setActive(data.getBoolean("active"))
            if (data.has("check_in_time")) house.setCheckInTime(data.getString("check_in_time"))
            if (data.has("check_out_time")) house.setCheckOutTime(data.getString("check_out_time"))
            
            def tc = perst.perstCreateContainer()
            tc.addUpdate(house)
            if (!perst.perstStore(tc)) {
                outjson.put("_Success", false)
                outjson.put("_ErrorMessage", "Failed to update house")
                return
            }
            JSONObject result = new JSONObject()
            result.put("id", house.getOid())
            result.put("name", house.getName())
            result.put("address", house.getAddress())
            result.put("description", house.getDescription())
            result.put("owner_id", house.getOwnerId())
            result.put("active", house.isActive())
            result.put("check_in_time", house.getCheckInTime())
            result.put("check_out_time", house.getCheckOutTime())
            outjson.put("data", result)
        } catch (Exception e) {
            outjson.put("_Success", false)
            outjson.put("_ErrorMessage", e.message)
        }
    }
    
    void deleteHouse(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            PerstConnection perst = getPerst()
            long oid = injson.getLong("id")
            House house = perst.getByOid(House, oid)
            if (house == null) {
                outjson.put("_Success", false)
                outjson.put("_ErrorMessage", "House not found")
                return
            }
            def tc = perst.perstCreateContainer()
            tc.addDelete(house)
            if (!perst.perstStore(tc)) {
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
            PerstConnection perst = getPerst()
            Collection<Owner> owners = perst.getAll(Owner)
            JSONArray rows = new JSONArray()
            
            for (Owner owner : owners) {
                JSONObject row = new JSONObject()
                row.put("id", owner.getOid())
                row.put("name", owner.getName())
                row.put("email", owner.getEmail())
                row.put("phone", owner.getPhone())
                row.put("address", owner.getAddress())
                row.put("active", owner.isActive())
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
            PerstConnection perst = getPerst()
            long oid = injson.getLong("id")
            Owner owner = perst.getByOid(Owner, oid)
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
            outjson.put("data", data)
        } catch (Exception e) {
            outjson.put("_Success", false)
            outjson.put("_ErrorMessage", e.message)
        }
    }
    
    void getOwnerByUserId(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            PerstConnection perst = getPerst()
            int userId = injson.getInt("userId")
            Collection<Owner> allOwners = perst.getAll(Owner)
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
            PerstConnection perst = getPerst()
            long ownerId = injson.getLong("ownerId")
            Owner owner = perst.getByOid(Owner, ownerId)
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
            PerstConnection perst = getPerst()
            JSONObject data = injson.getJSONObject("data")
            String name = data.getString("name")
            String email = data.getString("email", "")
            String phone = data.getString("phone", "")
            String address = data.getString("address", "")
            boolean active = data.getBoolean("active", true)
            
            Owner owner = new Owner(name, email, phone, address, active)
            def tc = perst.perstCreateContainer()
            tc.addInsert(owner)
            if (!perst.perstStore(tc)) {
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
        }
    }
    
    void updateOwner(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            PerstConnection perst = getPerst()
            long oid = injson.getLong("id")
            JSONObject data = injson.getJSONObject("data")
            Owner owner = perst.getByOid(Owner, oid)
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
            
            def tc = perst.perstCreateContainer()
            tc.addUpdate(owner)
            if (!perst.perstStore(tc)) {
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
    
    void deleteOwner(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            PerstConnection perst = getPerst()
            long oid = injson.getLong("id")
            Owner owner = perst.getByOid(Owner, oid)
            if (owner == null) {
                outjson.put("_Success", false)
                outjson.put("_ErrorMessage", "Owner not found")
                return
            }
            def tc = perst.perstCreateContainer()
            tc.addDelete(owner)
            if (!perst.perstStore(tc)) {
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
}
