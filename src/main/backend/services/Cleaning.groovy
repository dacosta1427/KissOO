package services

import org.kissweb.json.JSONArray
import org.kissweb.json.JSONObject
import org.kissweb.database.Connection
import org.kissweb.restServer.ProcessServlet
import mycompany.database.CleanerManager
import mycompany.database.BookingManager
import mycompany.database.ScheduleManager
import mycompany.database.HouseManager
import mycompany.domain.Cleaner
import mycompany.domain.Booking
import mycompany.domain.Schedule
import mycompany.domain.House

/**
 * Cleaning service for CRUD operations on cleaning scheduler entities.
 * 
 * Uses Perst OODBMS via managers.
 */
class Cleaning {

    // ==================== CLEANERS ====================
    
    void getCleaners(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        println "[Cleaning.groovy] getCleaners called"
        try {
            Collection<Cleaner> cleaners = CleanerManager.getAll()
            println "[Cleaning.groovy] Found ${cleaners?.size() ?: 0} cleaners"
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
            println "[Cleaning.groovy] Returning ${rows.length()} rows"
        } catch (Exception e) {
            println "[Cleaning.groovy] ERROR: ${e.message}"
            outjson.put("_Success", false)
            outjson.put("_ErrorMessage", e.message)
        }
    }
    
    void getCleaner(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            long oid = injson.getLong("id")
            Cleaner cleaner = CleanerManager.getByOid(oid)
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
            JSONObject data = injson.getJSONObject("data")
            String name = data.getString("name")
            String phone = data.getString("phone", "")
            String email = data.getString("email", "")
            String address = data.getString("address", "")
            boolean active = data.getBoolean("active", true)
            
            Cleaner cleaner = CleanerManager.create(name, phone, email, address, active)
            if (cleaner == null) {
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
            long oid = injson.getLong("id")
            JSONObject data = injson.getJSONObject("data")
            Cleaner cleaner = CleanerManager.getByOid(oid)
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
            
            boolean success = CleanerManager.update(cleaner)
            if (!success) {
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
            long oid = injson.getLong("id")
            Cleaner cleaner = CleanerManager.getByOid(oid)
            if (cleaner == null) {
                outjson.put("_Success", false)
                outjson.put("_ErrorMessage", "Cleaner not found")
                return
            }
            boolean success = CleanerManager.delete(cleaner)
            if (!success) {
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
            Collection<Booking> bookings = BookingManager.getAll()
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
            long oid = injson.getLong("id")
            Booking booking = BookingManager.getByOid(oid)
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
            data.put("status", booking.getStatus())
            outjson.put("data", data)
        } catch (Exception e) {
            outjson.put("_Success", false)
            outjson.put("_ErrorMessage", e.message)
        }
    }
    
    void createBooking(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            JSONObject data = injson.getJSONObject("data")
            int houseId = data.getInt("house_id")
            String checkInDate = data.getString("check_in_date")
            String checkOutDate = data.getString("check_out_date")
            String guestName = data.getString("guest_name")
            String guestEmail = data.getString("guest_email")
            String guestPhone = data.getString("guest_phone", "")
            String notes = data.getString("notes", "")
            
            Booking booking = BookingManager.create(houseId, checkInDate, checkOutDate,
                    guestName, guestEmail, guestPhone, notes)
            if (booking == null) {
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
            result.put("status", booking.getStatus())
            outjson.put("data", result)
        } catch (Exception e) {
            outjson.put("_Success", false)
            outjson.put("_ErrorMessage", e.message)
        }
    }
    
    void updateBooking(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            long oid = injson.getLong("id")
            JSONObject data = injson.getJSONObject("data")
            Booking booking = BookingManager.getByOid(oid)
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
            if (data.has("status")) booking.setStatus(data.getString("status"))
            
            boolean success = BookingManager.update(booking)
            if (!success) {
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
            result.put("status", booking.getStatus())
            outjson.put("data", result)
        } catch (Exception e) {
            outjson.put("_Success", false)
            outjson.put("_ErrorMessage", e.message)
        }
    }
    
    void deleteBooking(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            long oid = injson.getLong("id")
            Booking booking = BookingManager.getByOid(oid)
            if (booking == null) {
                outjson.put("_Success", false)
                outjson.put("_ErrorMessage", "Booking not found")
                return
            }
            boolean success = BookingManager.delete(booking)
            if (!success) {
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
            int houseId = injson.getInt("houseId")
            Collection<Booking> bookings = BookingManager.getByHouse(houseId)
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
            String startDate = injson.getString("startDate")
            String endDate = injson.getString("endDate")
            Collection<Booking> bookings = BookingManager.getByDateRange(startDate, endDate)
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
            Collection<Schedule> schedules = ScheduleManager.getAll()
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
            long oid = injson.getLong("id")
            Schedule schedule = ScheduleManager.getByOid(oid)
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
            JSONObject data = injson.getJSONObject("data")
            int cleanerId = data.getInt("cleaner_id")
            int bookingId = data.getInt("booking_id")
            String date = data.getString("date")
            String startTime = data.getString("start_time")
            String endTime = data.getString("end_time")
            String notes = data.getString("notes", "")
            
            Schedule schedule = ScheduleManager.create(cleanerId, bookingId, date, startTime, endTime, notes)
            if (schedule == null) {
                outjson.put("_Success", false)
                outjson.put("_ErrorMessage", "Failed to create schedule")
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
    
    void updateSchedule(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            long oid = injson.getLong("id")
            JSONObject data = injson.getJSONObject("data")
            Schedule schedule = ScheduleManager.getByOid(oid)
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
            
            boolean success = ScheduleManager.update(schedule)
            if (!success) {
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
            long oid = injson.getLong("id")
            Schedule schedule = ScheduleManager.getByOid(oid)
            if (schedule == null) {
                outjson.put("_Success", false)
                outjson.put("_ErrorMessage", "Schedule not found")
                return
            }
            boolean success = ScheduleManager.delete(schedule)
            if (!success) {
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
            int cleanerId = injson.getInt("cleanerId")
            Collection<Schedule> schedules = ScheduleManager.getByCleaner(cleanerId)
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
            int bookingId = injson.getInt("bookingId")
            Collection<Schedule> schedules = ScheduleManager.getByBooking(bookingId)
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
            String startDate = injson.getString("startDate")
            String endDate = injson.getString("endDate")
            Collection<Schedule> schedules = ScheduleManager.getByDateRange(startDate, endDate)
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
            Collection<House> houses = HouseManager.getAll()
            JSONArray rows = new JSONArray()
            
            for (House house : houses) {
                JSONObject row = new JSONObject()
                row.put("id", house.getOid())
                row.put("name", house.getName())
                row.put("address", house.getAddress())
                row.put("description", house.getDescription())
                row.put("active", house.isActive())
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
            long oid = injson.getLong("id")
            House house = HouseManager.getByOid(oid)
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
            data.put("active", house.isActive())
            outjson.put("data", data)
        } catch (Exception e) {
            outjson.put("_Success", false)
            outjson.put("_ErrorMessage", e.message)
        }
    }
    
    void createHouse(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            JSONObject data = injson.getJSONObject("data")
            String name = data.getString("name")
            String address = data.getString("address", "")
            String description = data.getString("description", "")
            boolean active = data.getBoolean("active", true)
            
            House house = HouseManager.create(name, address, description, active)
            if (house == null) {
                outjson.put("_Success", false)
                outjson.put("_ErrorMessage", "Failed to create house")
                return
            }
            JSONObject result = new JSONObject()
            result.put("id", house.getOid())
            result.put("name", house.getName())
            result.put("address", house.getAddress())
            result.put("description", house.getDescription())
            result.put("active", house.isActive())
            outjson.put("data", result)
        } catch (Exception e) {
            outjson.put("_Success", false)
            outjson.put("_ErrorMessage", e.message)
        }
    }
    
    void updateHouse(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            long oid = injson.getLong("id")
            JSONObject data = injson.getJSONObject("data")
            House house = HouseManager.getByOid(oid)
            if (house == null) {
                outjson.put("_Success", false)
                outjson.put("_ErrorMessage", "House not found")
                return
            }
            if (data.has("name")) house.setName(data.getString("name"))
            if (data.has("address")) house.setAddress(data.getString("address"))
            if (data.has("description")) house.setDescription(data.getString("description"))
            if (data.has("active")) house.setActive(data.getBoolean("active"))
            
            boolean success = HouseManager.update(house)
            if (!success) {
                outjson.put("_Success", false)
                outjson.put("_ErrorMessage", "Failed to update house")
                return
            }
            JSONObject result = new JSONObject()
            result.put("id", house.getOid())
            result.put("name", house.getName())
            result.put("address", house.getAddress())
            result.put("description", house.getDescription())
            result.put("active", house.isActive())
            outjson.put("data", result)
        } catch (Exception e) {
            outjson.put("_Success", false)
            outjson.put("_ErrorMessage", e.message)
        }
    }
    
    void deleteHouse(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            long oid = injson.getLong("id")
            House house = HouseManager.getByOid(oid)
            if (house == null) {
                outjson.put("_Success", false)
                outjson.put("_ErrorMessage", "House not found")
                return
            }
            boolean success = HouseManager.delete(house)
            if (!success) {
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
}