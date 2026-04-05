package services

import org.kissweb.json.JSONObject
import org.kissweb.json.JSONArray
import org.kissweb.database.Connection
import org.kissweb.restServer.ProcessServlet
import oodb.PerstStorageManager
import mycompany.domain.PerstUser
import mycompany.domain.Owner
import mycompany.domain.House
import mycompany.domain.Booking
import mycompany.domain.Cleaner
import mycompany.domain.Schedule

import java.time.LocalDate
import java.time.format.DateTimeFormatter

class LoadTestdata {

    private boolean isSystemAdmin(JSONObject injson) {
        try {
            if (injson.has("_isAdmin") && injson.getBoolean("_isAdmin")) {
                if (injson.has("_adminType") && injson.getString("_adminType") == "system") {
                    return true
                }
            }
            return false
        } catch (Exception e) { 
            return false 
        }
    }
    
    private void checkSystemAdmin(JSONObject injson, String operation) {
        if (!isSystemAdmin(injson)) {
            throw new Exception("System admin access required for: " + operation)
        }
    }

    private void clearDataInternal() {
        try {
            // Delete all schedules
            def allSchedules = PerstStorageManager.getAll(Schedule.class)
            allSchedules.each { s ->
                def tc = PerstStorageManager.createContainer()
                tc.addDelete(s)
                PerstStorageManager.store(tc)
            }
            // Delete all bookings
            def allBookings = PerstStorageManager.getAll(Booking.class)
            allBookings.each { b ->
                def tc = PerstStorageManager.createContainer()
                tc.addDelete(b)
                PerstStorageManager.store(tc)
            }
            // Delete all houses
            def allHouses = PerstStorageManager.getAll(House.class)
            allHouses.each { h ->
                def tc = PerstStorageManager.createContainer()
                tc.addDelete(h)
                PerstStorageManager.store(tc)
            }
            // Delete all PerstUsers via TC delete collection
            def allUsers = PerstStorageManager.getAll(PerstUser.class)
            def userDeleteTC = PerstStorageManager.createContainer()
            allUsers.each { user ->
                // Skip admin user - keep it
                if (user.getUsername() == 'admin') {
                    println "[ClearData] Skipping admin user"
                    return
                }
                userDeleteTC.addDelete(user)
                println "[ClearData] Added PerstUser to delete: ${user.getUsername()}"
            }
            PerstStorageManager.store(userDeleteTC)
            // Now delete all Actors (Owners, Cleaners) - their PUs are already marked deleted
            // In same transaction to ensure consistency
            def allCleaners = PerstStorageManager.getAll(Cleaner.class)
            allCleaners.each { c ->
                def tc = PerstStorageManager.createContainer()
                tc.addDelete(c)
                PerstStorageManager.store(tc)
                println "[ClearData] Deleted Cleaner: ${c.getName()}"
            }
            def allOwners = PerstStorageManager.getAll(Owner.class)
            allOwners.each { o ->
                def tc = PerstStorageManager.createContainer()
                tc.addDelete(o)
                PerstStorageManager.store(tc)
                println "[ClearData] Deleted Owner: ${o.getName()}"
            }
        } catch (Exception e) {
            println "Error clearing data: ${e.message}"
            e.printStackTrace()
        }
    }
    
    void load(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            checkSystemAdmin(injson, "load")
            
            println "[LoadTestdata] Starting test data load..."
            
            if (!PerstStorageManager.isAvailable()) {
                outjson.put("_Success", false)
                outjson.put("error", "Perst is not available")
                return
            }
            
            def results = [:]
            
            // Check if data already exists - if so, skip
            def existingHouses = PerstStorageManager.getAll(House.class)
            if (existingHouses.size() >= 10) {
                results.put("message", "Test data already exists (${existingHouses.size()} houses). Use Clear first if you want to reload.")
                outjson.put("_Success", true)
                outjson.put("results", results)
                return
            }
            
            // Ensure admin exists
            def users = PerstStorageManager.getAll(PerstUser.class)
            def admin = null
            for (def u : users) {
                if (u.getUsername() == 'admin') {
                    admin = u
                    break
                }
            }
            if (!admin) {
                def agreement = new mycompany.domain.Agreement("superAdmin")
                def adminActor = new mycompany.domain.Actor("System Admin", "superAdmin", agreement, mycompany.domain.ActorType.NATURAL)
                // Actor constructor already created a deactivated PerstUser
                // Get it and configure it
                admin = adminActor.getPerstUser()
                admin.setUsername("admin")
                admin.setPassword("admin")
                admin.setEmail("admin@kissoo.local")
                admin.setActive(true)
                admin.setEmailVerified(true)
                def tc = PerstStorageManager.createContainer()
                tc.addInsert(adminActor)
                tc.addInsert(admin)
                PerstStorageManager.store(tc)
                results.admin = "created"
            } else {
                results.admin = "exists"
            }
            
            // Skip clear - just create data (or check if exists)
            results.cleared = "skipped (not clearing)"
            
            // Create 10 Owners
            def ownerData = [
                [name: 'Jan de Vries', email: 'jan@example.com', phone: '+31 6 12345678'],
                [name: 'Maria Jansen', email: 'maria@example.com', phone: '+31 6 23456789'],
                [name: 'Peter Bakker', email: 'peter@example.com', phone: '+31 6 34567890'],
                [name: 'Sophie de Wit', email: 'sophie@example.com', phone: '+31 6 45678901'],
                [name: 'Anna Smit', email: 'anna@example.com', phone: '+31 6 56789012'],
                [name: 'Marco van Dam', email: 'marco@example.com', phone: '+31 6 67890123'],
                [name: 'Lisa Visser', email: 'lisa@example.com', phone: '+31 6 78901234'],
                [name: 'John de Haas', email: 'john@example.com', phone: '+31 6 89012345'],
                [name: 'Emma Brouwer', email: 'emma@example.com', phone: '+31 6 90123456'],
                [name: 'Pieter Vos', email: 'pieter@example.com', phone: '+31 6 01234567']
            ]
            
            def owners = []
            ownerData.each { d ->
                try {
                    def owner = new Owner(d.name, d.email, d.phone, "123 ${d.name.split(' ')[1]} Street, Amsterdam")
                    def tc = PerstStorageManager.createContainer()
                    tc.addInsert(owner)
                    tc.addInsert(owner.getPerstUser())
                    def storeResult = PerstStorageManager.store(tc)
                    println "[LoadTestdata] Owner ${d.name} OID=${owner.getOid()} stored=${storeResult}"
                    owners << owner
                } catch (Exception e) {
                    println "[LoadTestdata] Error creating owner ${d.name}: ${e.message}"
                    throw e
                }
            }
            results.owners = "created ${owners.size()}"
            println "[LoadTestdata] Created ${owners.size()} owners"
            
            // Create 15 Houses with proper Owner references
            def houseData = [
                [name: 'Strandhuis Zandvoort', desc: 'Beach house with sea view', ownerIdx: 0],
                [name: 'Stadswoning Amsterdam', desc: 'Modern city apartment', ownerIdx: 1],
                [name: 'Boerderij Limburg', desc: 'Rural farmhouse', ownerIdx: 2],
                [name: 'Appartement Rotterdam', desc: 'Downtown apartment', ownerIdx: 3],
                [name: 'Villa Den Haag', desc: 'Luxury villa with pool', ownerIdx: 4],
                [name: 'Chalet Ski', desc: 'Mountain chalet', ownerIdx: 5],
                [name: 'Kamphuisje Texel', desc: 'Cozy island house', ownerIdx: 6],
                [name: 'Grachtenpand Utrecht', desc: 'Historic canal house', ownerIdx: 7],
                [name: 'Bungalow Eelde', desc: 'Forest bungalow', ownerIdx: 8],
                [name: 'Herenhuis Groningen', desc: 'Mansion in city center', ownerIdx: 9],
                [name: 'Duinwoning Bloemendaal', desc: 'Dune cottage', ownerIdx: 0],
                [name: 'Loft Eindhoven', desc: 'Industrial loft', ownerIdx: 1],
                [name: 'Serre Delft', desc: 'Glass house', ownerIdx: 2],
                [name: 'Molenhuis Friesland', desc: 'Traditional mill house', ownerIdx: 3],
                [name: 'Watervilla Almere', desc: 'Floating house', ownerIdx: 4]
            ]
            
            def houses = []
            def houseTc = PerstStorageManager.createContainer()
            houseData.each { d ->
                try {
                    def owner = owners[d.ownerIdx]
                    def house = new House(d.name, "123 ${d.name.split(' ')[1]} Street", d.desc, true)
                    house.setOwner(owner)
                    houseTc.addInsert(house)
                    houses << house
                } catch (Exception e) {
                    println "[LoadTestdata] Error creating house ${d.name}: ${e.message}"
                    throw e
                }
            }
            PerstStorageManager.store(houseTc)
            results.houses = "created ${houses.size()}"
            println "[LoadTestdata] Created ${houses.size()} houses"
            
            // Create 6 Cleaners
            def cleanerData = [
                [name: 'Lisa Smit', phone: '+31 6 11112222', email: 'lisa.cleaner@example.com'],
                [name: 'Emma de Jong', phone: '+31 6 22223333', email: 'emma.cleaner@example.com'],
                [name: 'Sophie Mulder', phone: '+31 6 33334444', email: 'sophie.cleaner@example.com'],
                [name: 'Anna Visser', phone: '+31 6 44445555', email: 'anna.cleaner@example.com'],
                [name: 'Nina de Groot', phone: '+31 6 55556666', email: 'nina.cleaner@example.com'],
                [name: 'Marieke van der Meer', phone: '+31 6 66667777', email: 'marieke.cleaner@example.com']
            ]
            
            def cleaners = []
            cleanerData.each { d ->
                try {
                    def cleaner = new Cleaner(d.name, d.phone, d.email, "45 Cleaner Street, Amsterdam", true)
                    def tc = PerstStorageManager.createContainer()
                    tc.addInsert(cleaner)
                    tc.addInsert(cleaner.getPerstUser())
                    def storeResult = PerstStorageManager.store(tc)
                    println "[LoadTestdata] Cleaner ${d.name} OID=${cleaner.getOid()} userOID=${cleaner.getPerstUser()?.getOid()} username=${cleaner.getPerstUser()?.getUsername()} stored=${storeResult}"
                    cleaners << cleaner
                } catch (Exception e) {
                    println "[LoadTestdata] Error creating cleaner ${d.name}: ${e.message}"
                    throw e
                }
            }
            results.cleaners = "created ${cleaners.size()}"
            println "[LoadTestdata] Created ${cleaners.size()} cleaners"
            
            // Create bookings for each house (next 6 months)
            def bookings = []
            def today = LocalDate.now()
            def formatter = DateTimeFormatter.ofPattern("yyyyMMdd")
            def bookingTc = PerstStorageManager.createContainer()
            houses.eachWithIndex { house, houseIdx ->
                2.times { bookingIdx ->
                    try {
                        def checkIn = today.plusDays((houseIdx * 7) + (bookingIdx * 30))
                        def checkOut = checkIn.plusDays(3)
                        def guestName = "Guest ${houseIdx * 2 + bookingIdx + 1}"
                        def booking = new Booking(house, checkIn.format(formatter), checkOut.format(formatter), guestName, "${guestName.toLowerCase().replace(' ', '.')}@email.com", "+31 6 ${String.format('%08d', houseIdx * 2 + bookingIdx)}", "Special requests: None")
                        bookingTc.addInsert(booking)
                        bookings << booking
                    } catch (Exception e) {
                        println "[LoadTestdata] Error creating booking for house ${houseIdx}: ${e.message}"
                        throw e
                    }
                }
            }
            PerstStorageManager.store(bookingTc)
            results.bookings = "created ${bookings.size()}"
            println "[LoadTestdata] Created ${bookings.size()} bookings"
            
            // Create schedules from today to 6 months (for each booking)
            // Use batched transaction containers (15 per batch) to reduce lock contention
            def schedules = []
            def cleanerIdx = 0
            def BATCH_SIZE = 15
            def scheduleTc = PerstStorageManager.createContainer()
            def scheduleCount = 0
            
            bookings.eachWithIndex { booking, bookingIdx ->
                def cleaner = cleaners[cleanerIdx % cleaners.size()]
                def checkInDate = LocalDate.parse(booking.getCheckInDate(), formatter)
                
                // Create schedule for each day of the booking
                3.times { dayIdx ->
                    def scheduleDate = checkInDate.plusDays(dayIdx)
                    def schedule = new Schedule()
                    schedule.setCleaner(cleaner)
                    schedule.setBooking(booking)
                    schedule.setScheduleDate(scheduleDate.format(formatter))
                    schedule.setStartTime("09:00")
                    schedule.setEndTime("12:00")
                    schedule.setStatus("scheduled")
                    scheduleTc.addInsert(schedule)
                    schedules << schedule
                    scheduleCount++
                    
                    if (scheduleCount % BATCH_SIZE == 0) {
                        PerstStorageManager.store(scheduleTc)
                        scheduleTc = PerstStorageManager.createContainer()
                    }
                }
                cleanerIdx++
            }
            // Store remaining schedules
            if (scheduleCount % BATCH_SIZE != 0) {
                PerstStorageManager.store(scheduleTc)
            }
            results.schedules = "created ${schedules.size()}"
            
            outjson.put("_Success", true)
            outjson.put("results", results)
            
        } catch (Exception e) {
            outjson.put("_Success", false)
            outjson.put("error", e.message)
            e.printStackTrace()
        }
    }
    
    void clear(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            checkSystemAdmin(injson, "clear")
            
            if (!PerstStorageManager.isAvailable()) {
                outjson.put("_Success", false)
                outjson.put("error", "Perst is not available")
                return
            }
            
            def counts = [
                schedules: PerstStorageManager.getAll(Schedule.class).size(),
                bookings: PerstStorageManager.getAll(Booking.class).size(),
                houses: PerstStorageManager.getAll(House.class).size(),
                cleaners: PerstStorageManager.getAll(Cleaner.class).size(),
                owners: PerstStorageManager.getAll(Owner.class).size()
            ]
            
            clearDataInternal()
            
            outjson.put("_Success", true)
            outjson.put("results", "Cleared: ${counts.schedules} schedules, ${counts.bookings} bookings, ${counts.houses} houses, ${counts.cleaners} cleaners, ${counts.owners} owners")
            
        } catch (Exception e) {
            outjson.put("_Success", false)
            outjson.put("error", e.message)
            e.printStackTrace()
        }
    }
}
