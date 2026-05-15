package services;

import org.kissweb.json.JSONArray;
import org.kissweb.json.JSONObject;
import org.kissweb.database.Connection;
import org.kissweb.restServer.MainServlet;
import org.kissweb.restServer.ProcessServlet;
import org.garret.perst.dbmanager.UnifiedDBManager;
import org.garret.perst.continuous.TransactionContainer;
import koo.core.actor.Role;
import koo.core.user.PerstUser;
import domain.actor.cleaner.Schedule;
import domain.actor.cleaner.ScheduleManager;
import domain.actor.cleaner.Cleaner;
import domain.actor.cleaner.CleanerManager;
import domain.oov.house.Booking;
import domain.oov.house.BookingManager;
import domain.oov.house.House;
import domain.actor.owner.Owner;

import java.util.Collection;

/**
 * ScheduleService - REST endpoints for Schedule CRUD operations.
 *
 * Authorization:
 * - Admin: can see and do everything
 * - Cleaner: sees only their own schedules
 * - Owner: sees schedules for bookings on their houses
 */
public class ScheduleService {

    private static UnifiedDBManager getUdbm(ProcessServlet servlet) {
        return (UnifiedDBManager) MainServlet.getEnvironment("unifiedDBManager");
    }

    public void getSchedules(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            boolean admin = isAdmin(servlet);
            Owner owner = getCurrentOwner(servlet);
            Cleaner cleaner = getCurrentCleaner(servlet);

            Collection<Schedule> schedules;
            if (admin) {
                schedules = ScheduleManager.getAll();
            } else if (cleaner != null) {
                schedules = cleaner.getSchedules();
            } else if (owner != null) {
                schedules = owner.getSchedulesViaHouses();
            } else {
                schedules = new java.util.ArrayList<>();
            }

            JSONArray rows = new JSONArray();
            for (Schedule schedule : schedules) {
                JSONObject row = scheduleToJson(schedule);
                rows.put(row);
            }

            outjson.put("data", rows);
            outjson.put("_Success", true);
        } catch (Exception e) {
            outjson.put("_Success", false);
            outjson.put("_ErrorMessage", e.getMessage());
        }
    }

    public void getSchedule(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            long oid = injson.getLong("oid");
            Schedule schedule = ScheduleManager.getByOid(oid);
            if (schedule == null) {
                outjson.put("_Success", false);
                outjson.put("_ErrorMessage", "Schedule not found");
                return;
            }

            // Authorization check
            PerstUser pu = (PerstUser) servlet.getUserData("perstUser");
            if (pu != null) {
                koo.core.actor.AActor actor = pu.getActor();
                if (actor != null) {
                    boolean isAdmin = actor.getAgreement() != null &&
                        (actor.getAgreement().getRole() == Role.ADMIN || actor.getAgreement().getRole() == Role.SUPER_ADMIN);
                    if (!isAdmin) {
                        boolean hasAccess = false;
                        if (actor instanceof Cleaner) {
                            hasAccess = ((Cleaner) actor).getSchedules().contains(schedule);
                        } else if (actor instanceof Owner) {
                            hasAccess = ((Owner) actor).getSchedulesViaHouses().contains(schedule);
                        }
                        if (!hasAccess) {
                            outjson.put("_Success", false);
                            outjson.put("_ErrorMessage", "Not authorized");
                            outjson.put("_ErrorCode", 3);
                            return;
                        }
                    }
                }
            }

            JSONObject data = scheduleToJson(schedule);
            outjson.put("data", data);
            outjson.put("_Success", true);
        } catch (Exception e) {
            outjson.put("_Success", false);
            outjson.put("_ErrorMessage", e.getMessage());
        }
    }

    public void createSchedule(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            UnifiedDBManager udbm = getUdbm(servlet);
            JSONObject data = injson.getJSONObject("data");
            long cleanerOid = data.getLong("cleanerOid");
            long bookingOid = data.getLong("bookingOid");
            String date = data.getString("date");
            String startTime = data.getString("start_time");
            String endTime = data.getString("end_time");
            String notes = data.has("notes") ? data.getString("notes") : "";
            String status = data.has("status") ? data.getString("status") : "scheduled";
            if (status == null || status.isEmpty()) status = "scheduled";

            Cleaner cleaner = CleanerManager.getByOid(cleanerOid);
            Booking booking = BookingManager.getByOid(bookingOid);
            if (cleaner == null || booking == null) {
                outjson.put("_Success", false);
                outjson.put("_ErrorMessage", cleaner == null ? "Cleaner not found" : "Booking not found");
                return;
            }

            Schedule schedule = new Schedule();
            schedule.setCleaner(cleaner);
            schedule.setBooking(booking);
            schedule.setScheduleDate(date);
            schedule.setStartTime(startTime);
            schedule.setEndTime(endTime);
            schedule.setNotes(notes);
            schedule.setStatus(status);

            cleaner.addSchedule(schedule);

            TransactionContainer tc = udbm.createContainer();
            tc.addInsert(schedule);
            if (!udbm.store(tc).isSuccess()) {
                outjson.put("_Success", false);
                outjson.put("_ErrorMessage", "Failed to save schedule");
                return;
            }

            JSONObject result = scheduleToJson(schedule);
            outjson.put("data", result);
            outjson.put("_Success", true);
        } catch (Exception e) {
            outjson.put("_Success", false);
            outjson.put("_ErrorMessage", e.getMessage());
        }
    }

    public void updateSchedule(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            UnifiedDBManager udbm = getUdbm(servlet);
            long oid = injson.getLong("oid");
            JSONObject data = injson.getJSONObject("data");
            Schedule schedule = ScheduleManager.getByOid(oid);
            if (schedule == null) {
                outjson.put("_Success", false);
                outjson.put("_ErrorMessage", "Schedule not found");
                return;
            }

            if (data.has("cleanerOid")) {
                long cleanerOid = data.getLong("cleanerOid");
                Cleaner cleaner = CleanerManager.getByOid(cleanerOid);
                schedule.setCleaner(cleaner);
            }
            if (data.has("bookingOid")) {
                long bookingOid = data.getLong("bookingOid");
                Booking booking = BookingManager.getByOid(bookingOid);
                schedule.setBooking(booking);
            }
            if (data.has("date")) schedule.setScheduleDate(data.getString("date"));
            if (data.has("start_time")) schedule.setStartTime(data.getString("start_time"));
            if (data.has("end_time")) schedule.setEndTime(data.getString("end_time"));
            if (data.has("notes")) schedule.setNotes(data.getString("notes"));
            if (data.has("status")) schedule.setStatus(data.getString("status"));

            TransactionContainer tc = udbm.createContainer();
            tc.addUpdate(schedule);
            if (!udbm.store(tc).isSuccess()) {
                outjson.put("_Success", false);
                outjson.put("_ErrorMessage", "Failed to update schedule");
                return;
            }

            JSONObject result = scheduleToJson(schedule);
            outjson.put("data", result);
            outjson.put("_Success", true);
        } catch (Exception e) {
            outjson.put("_Success", false);
            outjson.put("_ErrorMessage", e.getMessage());
        }
    }

    public void deleteSchedule(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            UnifiedDBManager udbm = getUdbm(servlet);
            long oid = injson.getLong("oid");
            Schedule schedule = ScheduleManager.getByOid(oid);
            if (schedule == null) {
                outjson.put("_Success", false);
                outjson.put("_ErrorMessage", "Schedule not found");
                return;
            }
            TransactionContainer tc = udbm.createContainer();
            tc.addDelete(schedule);
            if (!udbm.store(tc).isSuccess()) {
                outjson.put("_Success", false);
                outjson.put("_ErrorMessage", "Failed to delete schedule");
                return;
            }
            outjson.put("_Success", true);
        } catch (Exception e) {
            outjson.put("_Success", false);
            outjson.put("_ErrorMessage", e.getMessage());
        }
    }

    public void getSchedulesByCleaner(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            long cleanerOid = injson.getLong("cleanerOid");
            boolean admin = isAdmin(servlet);
            Cleaner currentCleaner = getCurrentCleaner(servlet);

            Cleaner cleaner = CleanerManager.getByOid(cleanerOid);
            if (cleaner == null) {
                outjson.put("_Success", false);
                outjson.put("_ErrorMessage", "Cleaner not found");
                return;
            }

            if (!admin && currentCleaner != null && currentCleaner.getOid() != cleaner.getOid()) {
                outjson.put("_Success", false);
                outjson.put("_ErrorMessage", "Access denied: not your schedules");
                return;
            }

            Collection<Schedule> schedules = cleaner.getSchedules();
            JSONArray rows = new JSONArray();
            for (Schedule schedule : schedules) {
                rows.put(scheduleToJson(schedule));
            }
            outjson.put("data", rows);
            outjson.put("_Success", true);
        } catch (Exception e) {
            outjson.put("_Success", false);
            outjson.put("_ErrorMessage", e.getMessage());
        }
    }

    public void getSchedulesByBooking(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            long bookingOid = injson.getLong("bookingOid");
            Booking booking = BookingManager.getByOid(bookingOid);
            if (booking == null) {
                outjson.put("_Success", false);
                outjson.put("_ErrorMessage", "Booking not found");
                return;
            }
            Schedule schedule = booking.getSchedule();
            JSONArray rows = new JSONArray();
            if (schedule != null) {
                rows.put(scheduleToJson(schedule));
            }
            outjson.put("data", rows);
            outjson.put("_Success", true);
        } catch (Exception e) {
            outjson.put("_Success", false);
            outjson.put("_ErrorMessage", e.getMessage());
        }
    }

    public void getSchedulesByDateRange(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            String startDate = injson.getString("startDate");
            String endDate = injson.getString("endDate");
            Collection<Schedule> allSchedules = ScheduleManager.getAll();
            JSONArray rows = new JSONArray();
            for (Schedule schedule : allSchedules) {
                if (schedule.getScheduleDate().compareTo(startDate) >= 0 &&
                    schedule.getScheduleDate().compareTo(endDate) <= 0) {
                    rows.put(scheduleToJson(schedule));
                }
            }
            outjson.put("data", rows);
            outjson.put("_Success", true);
        } catch (Exception e) {
            outjson.put("_Success", false);
            outjson.put("_ErrorMessage", e.getMessage());
        }
    }

    private JSONObject scheduleToJson(Schedule schedule) {
        JSONObject json = new JSONObject();
        json.put("oid", schedule.getOid());
        json.put("cleanerOid", schedule.getCleanerOid());
        json.put("cleaner_name", schedule.getCleaner() != null ? schedule.getCleaner().getName() : null);
        json.put("bookingOid", schedule.getBookingOid());
        json.put("date", schedule.getScheduleDate());
        json.put("start_time", schedule.getStartTime());
        json.put("end_time", schedule.getEndTime());
        json.put("notes", schedule.getNotes());
        json.put("status", schedule.getStatus());

        // Add house details from booking
        Booking booking = schedule.getBooking();
        if (booking != null) {
            House house = booking.getHouse();
            if (house != null) {
json.put("houseOid", house.getOid());
                 json.put("houseName", house.getName());
                 json.put("houseAddress", house.getAddress());
                 json.put("bookingCheckInDate", booking.getCheckInDate());
                 json.put("bookingCheckOutDate", booking.getCheckOutDate());
                 json.put("guestName", booking.getGuestName());
            }
        }
        return json;
    }

    private boolean isAdmin(ProcessServlet servlet) {
        try {
            PerstUser pu = (PerstUser) servlet.getUserData("perstUser");
            if (pu == null) return false;
            koo.core.actor.AActor actor = pu.getActor();
            if (actor == null) return false;
            koo.core.actor.Role role = actor.getAgreement() != null ? actor.getAgreement().getRole() : null;
            return role == Role.ADMIN || role == Role.SUPER_ADMIN;
        } catch (Exception e) {
            return false;
        }
    }

    private Owner getCurrentOwner(ProcessServlet servlet) {
        try {
            PerstUser pu = (PerstUser) servlet.getUserData("perstUser");
            if (pu == null) return null;
            koo.core.actor.AActor actor = pu.getActor();
            if (actor instanceof Owner) {
                return (Owner) actor;
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private Cleaner getCurrentCleaner(ProcessServlet servlet) {
        try {
            PerstUser pu = (PerstUser) servlet.getUserData("perstUser");
            if (pu == null) return null;
            koo.core.actor.AActor actor = pu.getActor();
            if (actor instanceof Cleaner) {
                return (Cleaner) actor;
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}