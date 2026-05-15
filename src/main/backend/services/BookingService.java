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
import domain.oov.house.Booking;
import domain.oov.house.BookingManager;
import domain.oov.house.House;
import domain.oov.house.HouseManager;
import domain.actor.owner.Owner;
import domain.actor.owner.OwnerManager;
import domain.actor.cleaner.Schedule;
import domain.actor.cleaner.Cleaner;
import domain.actor.cleaner.CleanerManager;

import java.util.Collection;

/**
 * BookingService - REST endpoints for Booking CRUD operations.
 *
 * Authorization:
 * - Admin: can see and do everything
 * - Owner: sees only bookings for their houses
 * - Cleaner: sees bookings linked to their schedules
 */
public class BookingService {

    private static UnifiedDBManager getUdbm(ProcessServlet servlet) {
        return (UnifiedDBManager) MainServlet.getEnvironment("unifiedDBManager");
    }

    public void getBookings(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            JSONArray rows = new JSONArray();
            Owner owner = getCurrentOwner(servlet);
            boolean admin = isAdmin(servlet);

            Collection<Booking> bookings;
            if (admin) {
                bookings = BookingManager.getAll();
            } else if (owner != null) {
                bookings = owner.getBookings();
            } else {
                bookings = new java.util.ArrayList<>();
            }

            for (Booking booking : bookings) {
                JSONObject row = new JSONObject();
                row.put("oid", booking.getOid());
                row.put("houseOid", booking.getHouseOid());
                row.put("ownerOid", booking.getOwnerOid());
                row.put("scheduleOid", booking.getScheduleOid());
                row.put("check_in_date", booking.getCheckInDate());
                row.put("check_out_date", booking.getCheckOutDate());
                row.put("guest_name", booking.getGuestName());
                row.put("guest_email", booking.getGuestEmail());
                row.put("guest_phone", booking.getGuestPhone());
                row.put("notes", booking.getNotes());
                row.put("dogs_count", booking.getDogsCount());
                row.put("status", booking.getStatus());
                rows.put(row);
            }

            outjson.put("data", rows);
            outjson.put("_Success", true);
        } catch (Exception e) {
            outjson.put("_Success", false);
            outjson.put("_ErrorMessage", e.getMessage());
        }
    }

    public void getBooking(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            long oid = injson.getLong("oid");
            Booking booking = BookingManager.getByOid(oid);
            if (booking == null) {
                outjson.put("_Success", false);
                outjson.put("_ErrorMessage", "Booking not found");
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
                        if (actor instanceof Owner) {
                            hasAccess = ((Owner) actor).getBookings().contains(booking);
                        } else if (actor instanceof Cleaner) {
                            hasAccess = ((Cleaner) actor).getSchedules().stream()
                                .anyMatch(s -> s.getBookingOid() == booking.getOid());
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

            JSONObject data = new JSONObject();
            data.put("oid", booking.getOid());
            data.put("houseOid", booking.getHouseOid());
            data.put("check_in_date", booking.getCheckInDate());
            data.put("check_out_date", booking.getCheckOutDate());
            data.put("guest_name", booking.getGuestName());
            data.put("guest_email", booking.getGuestEmail());
            data.put("guest_phone", booking.getGuestPhone());
            data.put("notes", booking.getNotes());
            data.put("dogs_count", booking.getDogsCount());
            data.put("status", booking.getStatus());
            outjson.put("data", data);
            outjson.put("_Success", true);
        } catch (Exception e) {
            outjson.put("_Success", false);
            outjson.put("_ErrorMessage", e.getMessage());
        }
    }

    public void createBooking(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            UnifiedDBManager udbm = getUdbm(servlet);
            JSONObject data = injson.getJSONObject("data");
            long houseOid = data.getLong("houseOid");
            long ownerOid = data.has("ownerOid") ? data.getLong("ownerOid") : 0;
            String checkInDate = data.getString("check_in_date");
            String checkOutDate = data.getString("check_out_date");
            String guestName = data.getString("guest_name");
            String guestEmail = data.getString("guest_email");
            String guestPhone = data.has("guest_phone") ? data.getString("guest_phone") : "";
            String notes = data.has("notes") ? data.getString("notes") : "";
            int dogsCount = data.has("dogs_count") ? data.getInt("dogs_count") : 0;

            House house = HouseManager.getByOid(houseOid);
            if (house == null) {
                outjson.put("_Success", false);
                outjson.put("_ErrorMessage", "House not found");
                return;
            }

            Owner owner = ownerOid > 0 ? OwnerManager.getByOid(ownerOid) : house.getOwner();
            if (owner == null) {
                outjson.put("_Success", false);
                outjson.put("_ErrorMessage", "Owner not found");
                return;
            }

            Booking booking = new Booking(house, owner, checkInDate, checkOutDate, guestName, guestEmail, guestPhone, notes);
            booking.setDogsCount(dogsCount);
            house.addBooking(booking);

            TransactionContainer tc = udbm.createContainer();
            tc.addInsert(booking);
            if (!udbm.store(tc).isSuccess()) {
                outjson.put("_Success", false);
                outjson.put("_ErrorMessage", "Failed to create booking");
                return;
            }

            JSONObject result = bookingToJson(booking);
            outjson.put("data", result);
            outjson.put("_Success", true);
        } catch (Exception e) {
            outjson.put("_Success", false);
            outjson.put("_ErrorMessage", e.getMessage());
        }
    }

    public void updateBooking(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            UnifiedDBManager udbm = getUdbm(servlet);
            long oid = injson.getLong("oid");
            JSONObject data = injson.getJSONObject("data");
            Booking booking = BookingManager.getByOid(oid);
            if (booking == null) {
                outjson.put("_Success", false);
                outjson.put("_ErrorMessage", "Booking not found");
                return;
            }

if (data.has("houseOid")) {
                 long houseOid = data.getLong("houseOid");
                 House house = HouseManager.getByOid(houseOid);
                 booking.setHouse(house);
             }
            if (data.has("ownerOid")) {
                long ownerOid = data.getLong("ownerOid");
                Owner owner = OwnerManager.getByOid(ownerOid);
                booking.setOwner(owner);
            }
            if (data.has("check_in_date")) booking.setCheckInDate(data.getString("check_in_date"));
            if (data.has("check_out_date")) booking.setCheckOutDate(data.getString("check_out_date"));
            if (data.has("guest_name")) booking.setGuestName(data.getString("guest_name"));
            if (data.has("guest_email")) booking.setGuestEmail(data.getString("guest_email"));
            if (data.has("guest_phone")) booking.setGuestPhone(data.getString("guest_phone"));
            if (data.has("notes")) booking.setNotes(data.getString("notes"));
            if (data.has("dogs_count")) booking.setDogsCount(data.getInt("dogs_count"));
            if (data.has("status")) booking.setStatus(data.getString("status"));

            TransactionContainer tc = udbm.createContainer();
            tc.addUpdate(booking);
            if (!udbm.store(tc).isSuccess()) {
                outjson.put("_Success", false);
                outjson.put("_ErrorMessage", "Failed to update booking");
                return;
            }

            JSONObject result = bookingToJson(booking);
            outjson.put("data", result);
            outjson.put("_Success", true);
        } catch (Exception e) {
            outjson.put("_Success", false);
            outjson.put("_ErrorMessage", e.getMessage());
        }
    }

    public void deleteBooking(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            UnifiedDBManager udbm = getUdbm(servlet);
            long oid = injson.getLong("oid");
            Booking booking = BookingManager.getByOid(oid);
            if (booking == null) {
                outjson.put("_Success", false);
                outjson.put("_ErrorMessage", "Booking not found");
                return;
            }
            TransactionContainer tc = udbm.createContainer();
            tc.addDelete(booking);
            if (!udbm.store(tc).isSuccess()) {
                outjson.put("_Success", false);
                outjson.put("_ErrorMessage", "Failed to delete booking");
                return;
            }
            outjson.put("_Success", true);
        } catch (Exception e) {
            outjson.put("_Success", false);
            outjson.put("_ErrorMessage", e.getMessage());
        }
    }

    public void getBookingsByHouse(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            long houseOid = injson.has("houseOid") ? injson.getLong("houseOid") : 0;
            boolean admin = isAdmin(servlet);
            Owner owner = getCurrentOwner(servlet);

            House house = HouseManager.getByOid(houseOid);
            if (house == null) {
                outjson.put("_Success", false);
                outjson.put("_ErrorMessage", "House not found");
                return;
            }

            if (!admin && owner != null) {
                if (house.getOwner() == null || house.getOwner().getOid() != owner.getOid()) {
                    outjson.put("_Success", false);
                    outjson.put("_ErrorMessage", "Access denied: not your house");
                    return;
                }
            }

            Collection<Booking> bookings = house.getBookings();
            JSONArray rows = new JSONArray();
            for (Booking booking : bookings) {
                rows.put(bookingToJson(booking));
            }
            outjson.put("data", rows);
            outjson.put("_Success", true);
        } catch (Exception e) {
            outjson.put("_Success", false);
            outjson.put("_ErrorMessage", e.getMessage());
        }
    }

    public void getBookingsByDateRange(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            String startDate = injson.getString("startDate");
            String endDate = injson.getString("endDate");
            Collection<Booking> allBookings = BookingManager.getAll();
            JSONArray rows = new JSONArray();
            for (Booking booking : allBookings) {
                if (booking.getCheckInDate().compareTo(startDate) >= 0 &&
                    booking.getCheckInDate().compareTo(endDate) <= 0) {
                    rows.put(bookingToJson(booking));
                }
            }
            outjson.put("data", rows);
            outjson.put("_Success", true);
        } catch (Exception e) {
            outjson.put("_Success", false);
            outjson.put("_ErrorMessage", e.getMessage());
        }
    }

    private JSONObject bookingToJson(Booking booking) {
        JSONObject json = new JSONObject();
        json.put("oid", booking.getOid());
        json.put("houseOid", booking.getHouseOid());
        json.put("ownerOid", booking.getOwnerOid());
        json.put("scheduleOid", booking.getScheduleOid());
        json.put("check_in_date", booking.getCheckInDate());
        json.put("check_out_date", booking.getCheckOutDate());
        json.put("guest_name", booking.getGuestName());
        json.put("guest_email", booking.getGuestEmail());
        json.put("guest_phone", booking.getGuestPhone());
        json.put("notes", booking.getNotes());
        json.put("dogs_count", booking.getDogsCount());
        json.put("status", booking.getStatus());
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
}