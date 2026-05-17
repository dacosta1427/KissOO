package services;

import org.kissweb.json.JSONObject;
import org.kissweb.json.JSONArray;
import org.kissweb.database.Connection;
import org.kissweb.restServer.ProcessServlet;
import org.kissweb.restServer.MainServlet;
import org.kissweb.security.EXTERNAL_CALL;
import org.garret.perst.IterableIterator;
import org.garret.perst.dbmanager.UnifiedDBManager;
import org.garret.perst.continuous.TransactionContainer;

import java.util.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import koo.core.actor.Role;
import koo.core.user.PerstUser;
import domain.actor.owner.Owner;
import domain.actor.owner.OwnerManager;
import domain.oov.house.House;
import domain.oov.house.HouseManager;
import domain.oov.house.Booking;
import domain.oov.house.BookingManager;
import domain.actor.cleaner.Cleaner;
import domain.actor.cleaner.CleanerManager;
import domain.actor.cleaner.Schedule;
import domain.actor.cleaner.ScheduleManager;

public class LoadTestdata {

    private static UnifiedDBManager getUdbm() {
        return (UnifiedDBManager) MainServlet.getEnvironment("unifiedDBManager");
    }

    private boolean isSystemAdmin(ProcessServlet servlet) {
        try {
            PerstUser pu = (PerstUser) servlet.getUserData("perstUser");
            if (pu == null) return false;
            koo.core.actor.AActor actor = pu.getActor();
            if (actor == null) return false;
            koo.core.actor.Role role = actor.getAgreement() != null ? 
                actor.getAgreement().getRole() : null;
            return role == Role.SUPER_ADMIN;
        } catch (Exception e) {
            return false;
        }
    }

    private void checkSystemAdmin(ProcessServlet servlet, String operation) throws Exception {
        if (!isSystemAdmin(servlet)) {
            throw new Exception("System admin access required for: " + operation);
        }
    }

    @SuppressWarnings("unchecked")
    private List<PerstUser> getAllUsers() {
        UnifiedDBManager udbm = getUdbm();
        if (udbm == null) return new ArrayList<>();
        IterableIterator<PerstUser> results = udbm.getObjects(PerstUser.class);
        List<PerstUser> list = new ArrayList<>();
        if (results != null) {
            while (results.hasNext()) {
                list.add(results.next());
            }
        }
        return list;
    }

    @EXTERNAL_CALL
    public void load(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            UnifiedDBManager udbm = getUdbm();
            if (udbm == null) {
                outjson.put("_Success", false);
                outjson.put("error", "Perst is not available");
                return;
            }

            checkSystemAdmin(servlet, "load");

            System.out.println("[LoadTestdata] Starting test data load...");

            // Check if test data already exists
            List<House> existingHouses = new ArrayList<>();
            IterableIterator<House> houseIter = udbm.getObjects(House.class);
            if (houseIter != null) {
                while (houseIter.hasNext()) {
                    existingHouses.add(houseIter.next());
                }
            }

            Map<String, Object> results = new HashMap<>();

            // If data exists, clear it first to avoid OID mismatches
            if (!existingHouses.isEmpty()) {
                System.out.println("[LoadTestdata] Existing data found (" + existingHouses.size() + " houses), clearing first...");
                clearDataInternal(udbm);
                results.put("cleared", "existing data cleared");
            } else {
                results.put("cleared", "skipped (no existing data)");
            }

            List<PerstUser> users = getAllUsers();
            PerstUser admin = null;
            for (PerstUser u : users) {
                if ("admin".equals(u.getUsername())) {
                    admin = u;
                    break;
                }
            }
            if (admin == null) {
                results.put("admin", "ERROR: admin not found!");
            } else {
                results.put("admin", "exists");
            }

            List<Map<String, String>> ownerData = Arrays.asList(
                mapOf("name", "Jan de Vries", "email", "jan.devries@test.com", "phone", "+31 6 12345678"),
                mapOf("name", "Maria Jansen", "email", "maria.jansen@test.com", "phone", "+31 6 23456789"),
                mapOf("name", "Peter Bakker", "email", "peter.bakker@test.com", "phone", "+31 6 34567890"),
                mapOf("name", "Sophie de Wit", "email", "sophie.dewit@test.com", "phone", "+31 6 45678901"),
                mapOf("name", "Anna Smit", "email", "anna.smit@test.com", "phone", "+31 6 56789012"),
                mapOf("name", "Marco van Dam", "email", "marco.vandam@test.com", "phone", "+31 6 67890123"),
                mapOf("name", "Lisa Visser", "email", "lisa.visser@test.com", "phone", "+31 6 78901234"),
                mapOf("name", "John de Haas", "email", "john.dehaas@test.com", "phone", "+31 6 89012345"),
                mapOf("name", "Emma Brouwer", "email", "emma.brouwer@test.com", "phone", "+31 6 90123456"),
                mapOf("name", "Pieter Vos", "email", "pieter.vos@test.com", "phone", "+31 6 01234567")
            );

            List<Owner> owners = new ArrayList<>();
            Set<String> existingUsernames = new HashSet<>();
            for (PerstUser u : users) {
                existingUsernames.add(u.getUsername());
            }

            for (Map<String, String> d : ownerData) {
                String email = d.get("email");
                if (existingUsernames.contains(email)) {
                    System.out.println("[LoadTestdata] Skipping existing owner: " + email);
                    continue;
                }
                try {
                    Owner owner = new Owner(d.get("name"), d.get("phone"), email, "123 " + d.get("name").split(" ")[1] + " Street, Amsterdam", true);
                    owner.getPerstUser().setActive(true);
                    owner.getPerstUser().setEmailVerified(false);
                    owners.add(owner);
                } catch (Exception e) {
                    System.out.println("[LoadTestdata] Error creating owner " + d.get("name") + ": " + e.getMessage());
                    throw e;
                }
            }

            // PHASE 1: Persist owners FIRST so they have stable OIDs
            if (!owners.isEmpty()) {
                TransactionContainer ownerTc = udbm.createContainer();
                for (Owner o : owners) {
                    ownerTc.addInsert(o);
                    ownerTc.addInsert(o.getPerstUser());
                }
                udbm.store(ownerTc);
                for (Owner o : owners) {
                    System.out.println("[LoadTestdata] Owner " + o.getName() + " persisted with OID=" + o.getOid());
                }
            }
            results.put("owners", "created " + owners.size());
            System.out.println("[LoadTestdata] Created and persisted " + owners.size() + " owners");

            List<Map<String, String>> houseData = Arrays.asList(
                mapOf("name", "Strandhuis Zandvoort", "desc", "Beach house with sea view"),
                mapOf("name", "Stadswoning Amsterdam", "desc", "Modern city apartment"),
                mapOf("name", "Boerderij Limburg", "desc", "Rural farmhouse"),
                mapOf("name", "Appartement Rotterdam", "desc", "Downtown apartment"),
                mapOf("name", "Villa Den Haag", "desc", "Luxury villa with pool"),
                mapOf("name", "Chalet Ski", "desc", "Mountain chalet"),
                mapOf("name", "Kamphuisje Texel", "desc", "Cozy island house"),
                mapOf("name", "Grachtenpand Utrecht", "desc", "Historic canal house"),
                mapOf("name", "Bungalow Eelde", "desc", "Forest bungalow"),
                mapOf("name", "Herenhuis Groningen", "desc", "Mansion in city center"),
                mapOf("name", "Duinwoning Bloemendaal", "desc", "Dune cottage"),
                mapOf("name", "Loft Eindhoven", "desc", "Industrial loft"),
                mapOf("name", "Serre Delft", "desc", "Glass house"),
                mapOf("name", "Molenhuis Friesland", "desc", "Traditional mill house"),
                mapOf("name", "Watervilla Almere", "desc", "Floating house")
            );

            List<House> houses = new ArrayList<>();
            if (owners.isEmpty()) {
                results.put("houses", "0 (no owners)");
                System.out.println("[LoadTestdata] Cannot create houses - no owners exist");
            } else {
                Random random = new Random();
                for (Map<String, String> d : houseData) {
                    try {
                        Owner randomOwner = owners.get(random.nextInt(owners.size()));
                        House house = new House(randomOwner, d.get("name"), "123 Street", d.get("desc"), true);
                        randomOwner.addHouse(house);
                        houses.add(house);
                    } catch (Exception e) {
                        System.out.println("[LoadTestdata] Error creating house " + d.get("name") + ": " + e.getMessage());
                        throw e;
                    }
                }

                // PHASE 2: Persist houses (owners already persisted)
                TransactionContainer houseTc = udbm.createContainer();
                for (House house : houses) {
                    houseTc.addInsert(house);
                }
                udbm.store(houseTc);

                results.put("houses", "created " + houses.size());
                System.out.println("[LoadTestdata] Created and persisted " + houses.size() + " houses");

                for (Owner owner : owners) {
                    int count = 0;
                    for (House h : houses) {
                        if (h.getOwner() != null && h.getOwner().getOid() == owner.getOid()) {
                            count++;
                        }
                    }
                    System.out.println("[LoadTestdata] Owner " + owner.getName() + " (OID=" + owner.getOid() + ") has " + count + " house(s)");
                }
            }

            List<Map<String, String>> cleanerData = Arrays.asList(
                mapOf("name", "Lisa Smit", "phone", "+31 6 11112222", "email", "lisa.smit@cleaner.test"),
                mapOf("name", "Emma de Jong", "phone", "+31 6 22223333", "email", "emma.dejong@cleaner.test"),
                mapOf("name", "Sophie Mulder", "phone", "+31 6 33334444", "email", "sophie.mulder@cleaner.test"),
                mapOf("name", "Anna Visser", "phone", "+31 6 44445555", "email", "anna.visser@cleaner.test"),
                mapOf("name", "Nina de Groot", "phone", "+31 6 55556666", "email", "nina.degroot@cleaner.test"),
                mapOf("name", "Marieke van der Meer", "phone", "+31 6 66667777", "email", "marieke.vandermeer@cleaner.test")
            );

            List<Cleaner> cleaners = new ArrayList<>();
            for (Map<String, String> d : cleanerData) {
                String email = d.get("email");
                if (existingUsernames.contains(email)) {
                    System.out.println("[LoadTestdata] Skipping existing cleaner: " + email);
                    continue;
                }
                try {
                    Cleaner cleaner = new Cleaner(d.get("name"), d.get("phone"), email, true);
                    cleaner.getPerstUser().setActive(true);
                    cleaner.getPerstUser().setEmailVerified(true);
                    cleaners.add(cleaner);
                } catch (Exception e) {
                    System.out.println("[LoadTestdata] Error creating cleaner " + d.get("name") + ": " + e.getMessage());
                }
            }

            // PHASE 3: Persist cleaners FIRST so they have stable OIDs
            if (!cleaners.isEmpty()) {
                TransactionContainer cleanerTc = udbm.createContainer();
                for (Cleaner c : cleaners) {
                    cleanerTc.addInsert(c);
                    cleanerTc.addInsert(c.getPerstUser());
                }
                udbm.store(cleanerTc);
                for (Cleaner c : cleaners) {
                    System.out.println("[LoadTestdata] Cleaner " + c.getName() + " persisted with OID=" + c.getOid());
                }
            }
            results.put("cleaners", "created " + cleaners.size());
            System.out.println("[LoadTestdata] Created and persisted " + cleaners.size() + " cleaners");

            List<Booking> bookings = new ArrayList<>();
            LocalDate today = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

            Random random = new Random();
            for (int houseIdx = 0; houseIdx < houses.size(); houseIdx++) {
                House house = houses.get(houseIdx);
                for (int bookingIdx = 0; bookingIdx < 2; bookingIdx++) {
                    try {
                        LocalDate checkIn = today.plusDays((houseIdx * 7) + (bookingIdx * 30));
                        LocalDate checkOut = checkIn.plusDays(3);
                        String guestName = "Guest " + (houseIdx * 2 + bookingIdx + 1);
                        Booking booking = new Booking(house, house.getOwner(),
                            checkIn.format(formatter), checkOut.format(formatter),
                            guestName, guestName.toLowerCase().replace(' ', '.') + "@email.com",
                            "+31 6 " + String.format("%08d", houseIdx * 2 + bookingIdx),
                            "Special requests: None");
                        house.addBooking(booking);
                        bookings.add(booking);
                    } catch (Exception e) {
                        System.out.println("[LoadTestdata] Error creating booking: " + e.getMessage());
                        throw e;
                    }
                }
            }

            // PHASE 4: Persist bookings (houses and owners already persisted)
            if (!bookings.isEmpty()) {
                TransactionContainer bookingTc = udbm.createContainer();
                for (Booking b : bookings) {
                    bookingTc.addInsert(b);
                }
                udbm.store(bookingTc);
            }
            results.put("bookings", "created " + bookings.size());
            System.out.println("[LoadTestdata] Created and persisted " + bookings.size() + " bookings");

            List<Schedule> schedules = new ArrayList<>();
            for (int bookingIdx = 0; bookingIdx < bookings.size(); bookingIdx++) {
                Booking booking = bookings.get(bookingIdx);
                Cleaner cleaner = cleaners.get(bookingIdx % cleaners.size());
                LocalDate checkInDate = LocalDate.parse(booking.getCheckInDate(), formatter);

                LocalDate scheduleDate = checkInDate.plusDays(1);
                Schedule schedule = new Schedule();
                schedule.setCleaner(cleaner);
                schedule.setBooking(booking);
                schedule.setScheduleDate(scheduleDate.format(formatter));
                schedule.setStartTime("09:00");
                schedule.setEndTime("12:00");
                schedule.setStatus("scheduled");
                cleaner.addSchedule(schedule);
                booking.setSchedule(schedule);
                schedules.add(schedule);
            }

            // PHASE 5: Persist schedules (bookings and cleaners already persisted)
            if (!schedules.isEmpty()) {
                TransactionContainer scheduleTc = udbm.createContainer();
                for (Schedule s : schedules) {
                    scheduleTc.addInsert(s);
                }
                udbm.store(scheduleTc);
            }
            results.put("schedules", "created " + schedules.size());
            System.out.println("[LoadTestdata] Created and persisted " + schedules.size() + " schedules");

            outjson.put("_Success", true);
            outjson.put("results", results);

        } catch (Exception e) {
            outjson.put("_Success", false);
            outjson.put("error", e.getMessage());
            e.printStackTrace();
        }
    }

    @EXTERNAL_CALL
    public void clear(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            checkSystemAdmin(servlet, "clear");

            UnifiedDBManager udbm = getUdbm();
            if (udbm == null) {
                outjson.put("_Success", false);
                outjson.put("error", "Perst is not available");
                return;
            }

            int scheduleCount = ScheduleManager.getAll().size();
            int bookingCount = BookingManager.getAll().size();
            int houseCount = HouseManager.getAll().size();
            int cleanerCount = CleanerManager.getAll().size();
            int ownerCount = OwnerManager.getAll().size();

            clearDataInternal(udbm);

            outjson.put("_Success", true);
            outjson.put("results", "Cleared: " + scheduleCount + " schedules, " + 
                bookingCount + " bookings, " + houseCount + " houses, " + 
                cleanerCount + " cleaners, " + ownerCount + " owners");

        } catch (Exception e) {
            outjson.put("_Success", false);
            outjson.put("error", e.getMessage());
            e.printStackTrace();
        }
    }

    private void clearDataInternal(UnifiedDBManager udbm) {
        try {
            IterableIterator<Schedule> scheduleIter = udbm.getObjects(Schedule.class);
            if (scheduleIter != null) {
                while (scheduleIter.hasNext()) {
                    Schedule s = scheduleIter.next();
                    TransactionContainer tc = udbm.createContainer();
                    tc.addDelete(s);
                    udbm.store(tc);
                    System.out.println("[ClearData] Deleted Schedule: " + s.getOid());
                }
            }

            IterableIterator<Booking> bookingIter = udbm.getObjects(Booking.class);
            if (bookingIter != null) {
                while (bookingIter.hasNext()) {
                    Booking b = bookingIter.next();
                    TransactionContainer tc = udbm.createContainer();
                    tc.addDelete(b);
                    udbm.store(tc);
                    System.out.println("[ClearData] Deleted Booking: " + b.getOid());
                }
            }

            IterableIterator<House> houseIter2 = udbm.getObjects(House.class);
            if (houseIter2 != null) {
                while (houseIter2.hasNext()) {
                    House h = houseIter2.next();
                    TransactionContainer tc = udbm.createContainer();
                    tc.addDelete(h);
                    udbm.store(tc);
                    System.out.println("[ClearData] Deleted House: " + h.getOid());
                }
            }

            IterableIterator<PerstUser> userIter = udbm.getObjects(PerstUser.class);
            TransactionContainer userTc = udbm.createContainer();
            if (userIter != null) {
                while (userIter.hasNext()) {
                    PerstUser user = userIter.next();
                    if ("admin".equals(user.getUsername())) {
                        System.out.println("[ClearData] Skipping admin user");
                        continue;
                    }
                    userTc.addDelete(user);
                    System.out.println("[ClearData] Added PerstUser to delete: " + user.getUsername());
                }
            }
            udbm.store(userTc);

            IterableIterator<Cleaner> cleanerIter = udbm.getObjects(Cleaner.class);
            if (cleanerIter != null) {
                while (cleanerIter.hasNext()) {
                    Cleaner c = cleanerIter.next();
                    TransactionContainer tc = udbm.createContainer();
                    tc.addDelete(c);
                    udbm.store(tc);
                    System.out.println("[ClearData] Deleted Cleaner: " + c.getName());
                }
            }

            IterableIterator<Owner> ownerIter = udbm.getObjects(Owner.class);
            if (ownerIter != null) {
                while (ownerIter.hasNext()) {
                    Owner o = ownerIter.next();
                    TransactionContainer tc = udbm.createContainer();
                    tc.addDelete(o);
                    udbm.store(tc);
                    System.out.println("[ClearData] Deleted Owner: " + o.getName());
                }
            }
        } catch (Exception e) {
            System.out.println("Error clearing data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static Map<String, String> mapOf(String... pairs) {
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < pairs.length; i += 2) {
            map.put(pairs[i], pairs[i + 1]);
        }
        return map;
    }
}