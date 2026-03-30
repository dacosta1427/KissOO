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

    void load(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            if (!PerstStorageManager.isAvailable()) {
                outjson.put("_Success", false)
                outjson.put("error", "Perst is not available")
                return
            }
            
            def results = [:]
            
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
                admin = new PerstUser("admin", "admin", 1)
                admin.setEmail("admin@kissoo.local")
                admin.setActive(true)
                admin.setEmailVerified(true)
                def tc = PerstStorageManager.createContainer()
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
                def owner = new Owner(d.name, d.email, d.phone, "123 ${d.name.split(' ')[1]} Street, Amsterdam")
                def tc = PerstStorageManager.createContainer()
                tc.addInsert(owner)
                PerstStorageManager.store(tc)
                owners << owner
            }
            results.owners = "created ${owners.size()}"
            
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
            houseData.each { d ->
                def owner = owners[d.ownerIdx]
                def house = new House(d.name, "123 ${d.name.split(' ')[1]} Street", d.desc, true)
                house.setOwner(owner)  // Proper OO reference
                def tc = PerstStorageManager.createContainer()
                tc.addInsert(house)
                PerstStorageManager.store(tc)
                houses << house
            }
            results.houses = "created ${houses.size()}"
            
            // Create 6 Cleaners
            def cleanerData = [
                [name: 'Lisa Smit', phone: '+31 6 11112222', email: 'lisa@example.com'],
                [name: 'Emma de Jong', phone: '+31 6 22223333', email: 'emma@example.com'],
                [name: 'Sophie Mulder', phone: '+31 6 33334444', email: 'sophie@example.com'],
                [name: 'Anna Visser', phone: '+31 6 44445555', email: 'anna@example.com'],
                [name: 'Nina de Groot', phone: '+31 6 55556666', email: 'nina@example.com'],
                [name: 'Marieke van der Meer', phone: '+31 6 66667777', email: 'marieke@example.com']
            ]
            
            def cleaners = []
            cleanerData.each { d ->
                def cleaner = new Cleaner(d.name, d.phone, d.email, "45 Cleaner Street, Amsterdam", true)
                def tc = PerstStorageManager.createContainer()
                tc.addInsert(cleaner)
                PerstStorageManager.store(tc)
                cleaners << cleaner
            }
            results.cleaners = "created ${cleaners.size()}"
            
            // Create bookings for each house (next 6 months)
            def bookings = []
            def today = LocalDate.now()
            def formatter = DateTimeFormatter.ofPattern("yyyyMMdd")
            
            houses.eachWithIndex { house, houseIdx ->
                // Create 2 bookings per house (spread over 6 months)
                2.times { bookingIdx ->
                    def checkIn = today.plusDays((houseIdx * 7) + (bookingIdx * 30))
                    def checkOut = checkIn.plusDays(3)
                    def guestName = "Guest ${houseIdx * 2 + bookingIdx + 1}"
                    def booking = new Booking((int)house.getOid(), checkIn.format(formatter), checkOut.format(formatter), guestName, "${guestName.toLowerCase().replace(' ', '.')}@email.com", "+31 6 ${String.format('%08d', houseIdx * 2 + bookingIdx)}", "Special requests: None")
                    booking.setHouse(house)  // Proper OO reference
                    def tc = PerstStorageManager.createContainer()
                    tc.addInsert(booking)
                    PerstStorageManager.store(tc)
                    bookings << booking
                }
            }
            results.bookings = "created ${bookings.size()}"
            
            // Create schedules from today to 6 months (for each booking)
            def schedules = []
            def cleanerIdx = 0
            bookings.eachWithIndex { booking, bookingIdx ->
                def cleaner = cleaners[cleanerIdx % cleaners.size()]
                def checkInDate = LocalDate.parse(booking.getCheckInDate(), formatter)
                
                // Create schedule for each day of the booking
                3.times { dayIdx ->
                    def scheduleDate = checkInDate.plusDays(dayIdx)
                    def schedule = new Schedule()
                    schedule.setCleaner(cleaner)  // Proper OO reference
                    schedule.setBooking(booking)  // Proper OO reference
                    schedule.setScheduleDate(scheduleDate.format(formatter))
                    schedule.setStartTime("09:00")
                    schedule.setEndTime("12:00")
                    schedule.setStatus("scheduled")
                    def tc = PerstStorageManager.createContainer()
                    tc.addInsert(schedule)
                    PerstStorageManager.store(tc)
                    schedules << schedule
                }
                cleanerIdx++
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
}
