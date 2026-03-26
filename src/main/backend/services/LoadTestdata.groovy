package services

import org.kissweb.json.JSONObject
import org.kissweb.json.JSONArray
import org.kissweb.database.Connection
import org.kissweb.restServer.ProcessServlet
import org.kissweb.restServer.MainServlet
import mycompany.domain.PerstUser
import mycompany.domain.Owner
import mycompany.domain.House
import mycompany.domain.Booking
import mycompany.domain.Cleaner
import mycompany.domain.Schedule
import oodb.PerstConnection

/**
 * LoadTestdata - Create sample data for testing.
 * 
 * Creates: 3 owners, 5 houses, 10 bookings, 4 cleaners, 15 schedules
 * Also creates admin user if not exists.
 * 
 * HTTP Request:
 * {
 *   "_class": "services.LoadTestdata",
 *   "_method": "load",
 *   "_uuid": "session-uuid"
 * }
 */
class LoadTestdata {

    private PerstConnection getPerst() {
        return (PerstConnection) MainServlet.getEnvironment("PerstConnection")
    }

    /**
     * Load all test data
     */
    void load(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            PerstConnection perst = getPerst()
            
            def results = [:]
            
            // Create admin user if needed
            results.admin = createAdminUser(perst)
            
            // Create owners
            results.owners = createOwners(perst)
            
            // Create houses
            results.houses = createHouses(perst)
            
            // Create cleaners
            results.cleaners = createCleaners(perst)
            
            // Create bookings
            results.bookings = createBookings(perst)
            
            // Create schedules
            results.schedules = createSchedules(perst)
            
            outjson.put("_Success", true)
            outjson.put("results", results)
            
        } catch (Exception e) {
            outjson.put("_Success", false)
            outjson.put("error", e.message)
            e.printStackTrace()
        }
    }
    
    /**
     * Create admin user if none exists, or update existing admin
     */
    private def createAdminUser(PerstConnection perst) {
        def existingUsers = perst.getAll(PerstUser)
        def existingAdmin = existingUsers.find { it.getUsername() == 'admin' }
        
        if (existingAdmin) {
            // Update password to 'admin' and ensure verified
            existingAdmin.setPassword("admin")
            existingAdmin.setActive(true)
            existingAdmin.setEmailVerified(true)
            
            def tc = perst.perstCreateContainer()
            tc.addUpdate(existingAdmin)
            perst.perstStore(tc)
            
            return [status: 'updated', username: 'admin', password: 'admin']
        }
        
        // Create new admin user
        int maxUserId = existingUsers.collect { it.getUserId() }.max() ?: 0
        PerstUser admin = new PerstUser("admin", "admin", maxUserId + 1)
        admin.setEmail("admin@kissoo.local")
        admin.setActive(true)
        admin.setEmailVerified(true)
        
        def tc = perst.perstCreateContainer()
        tc.addInsert(admin)
        perst.perstStore(tc)
        
        return [status: 'created', username: 'admin', password: 'admin']
    }
    
    /**
     * Create sample owners
     */
    private def createOwners(PerstConnection perst) {
        def existingOwners = perst.getAll(Owner)
        if (existingOwners.size() >= 3) {
            return [status: 'exists', count: existingOwners.size()]
        }
        
        def owners = [
            [name: 'Jan de Vries', email: 'jan@example.com', phone: '+31 6 12345678', address: 'Hoofdstraat 1, Amsterdam'],
            [name: 'Maria Jansen', email: 'maria@example.com', phone: '+31 6 23456789', address: 'Kerkstraat 15, Utrecht'],
            [name: 'Peter Bakker', email: 'peter@example.com', phone: '+31 6 34567890', address: 'Dorpsweg 42, Haarlem']
        ]
        
        def created = 0
        def ownerOids = []
        
        owners.each { data ->
            // Owner(name, email, phone, address)
            def owner = new Owner(data.name, data.email, data.phone, data.address)
            def tc = perst.perstCreateContainer()
            tc.addInsert(owner)
            perst.perstStore(tc)
            ownerOids.add(owner.getOid())
            created++
        }
        
        return [status: 'created', count: created, oids: ownerOids]
    }
    
    /**
     * Create sample houses
     */
    private def createHouses(PerstConnection perst) {
        def existingHouses = perst.getAll(House)
        if (existingHouses.size() >= 5) {
            return [status: 'exists', count: existingHouses.size()]
        }
        
        def owners = perst.getAll(Owner)
        if (owners.isEmpty()) {
            return [status: 'skipped', message: 'No owners found']
        }
        
        def houses = [
            [name: 'Strandhuis Castricum', address: 'Strandweg 10, Castricum', desc: 'Beach house with sea view, 4 bedrooms', ownerIdx: 0],
            [name: 'Stadswoning Amsterdam', address: 'Herengracht 200, Amsterdam', desc: 'Canal house in city center, 3 bedrooms', ownerIdx: 1],
            [name: 'Boerderij Laren', address: 'Meentweg 5, Laren', desc: 'Rural farmhouse with garden, 5 bedrooms', ownerIdx: 0],
            [name: 'Appartement Utrecht', address: 'Oudegracht 75, Utrecht', desc: 'City apartment near canals, 2 bedrooms', ownerIdx: 1],
            [name: 'Villa Bloemendaal', address: 'Duinweg 12, Bloemendaal', desc: 'Luxury villa near dunes, 6 bedrooms', ownerIdx: 2]
        ]
        
        def created = 0
        houses.each { data ->
            def owner = owners[Math.min(data.ownerIdx, owners.size() - 1)]
            // House(name, address, description, ownerId, active)
            def house = new House(data.name, data.address, data.desc, owner.getOid(), true)
            house.setCheckInTime("15:00")
            house.setCheckOutTime("10:00")
            
            def tc = perst.perstCreateContainer()
            tc.addInsert(house)
            perst.perstStore(tc)
            created++
        }
        
        return [status: 'created', count: created]
    }
    
    /**
     * Create sample cleaners
     */
    private def createCleaners(PerstConnection perst) {
        def existingCleaners = perst.getAll(Cleaner)
        if (existingCleaners.size() >= 4) {
            return [status: 'exists', count: existingCleaners.size()]
        }
        
        // Cleaner(name, phone, email, address, active)
        def cleaners = [
            [name: 'Lisa Smit', phone: '+31 6 11112222', email: 'lisa@cleanco.nl', address: 'Cleanstraat 1, Haarlem'],
            [name: 'Emma de Jong', phone: '+31 6 22223333', email: 'emma@cleanco.nl', address: 'Wasserijlaan 5, Amsterdam'],
            [name: 'Sophie Mulder', phone: '+31 6 33334444', email: 'sophie@cleanco.nl', address: 'Schoonweg 10, Utrecht'],
            [name: 'Anna Visser', phone: '+31 6 44445555', email: 'anna@cleanco.nl', address: 'Glazenpad 3, Leiden']
        ]
        
        def created = 0
        cleaners.each { data ->
            def cleaner = new Cleaner(data.name, data.phone, data.email, data.address, true)
            def tc = perst.perstCreateContainer()
            tc.addInsert(cleaner)
            perst.perstStore(tc)
            created++
        }
        
        return [status: 'created', count: created]
    }
    
    /**
     * Create sample bookings
     */
    private def createBookings(PerstConnection perst) {
        def existingBookings = perst.getAll(Booking)
        if (existingBookings.size() >= 10) {
            return [status: 'exists', count: existingBookings.size()]
        }
        
        def houses = perst.getAll(House)
        if (houses.isEmpty()) {
            return [status: 'skipped', message: 'No houses found']
        }
        
        // Booking(houseId, checkInDate, checkOutDate, guestName, guestEmail, guestPhone, notes, dogsCount)
        def bookings = [
            [houseIdx: 0, guest: 'John Smith', email: 'john@email.com', phone: '+1 555 1234', checkin: '20240401', checkout: '20240405', dogs: 1],
            [houseIdx: 1, guest: 'Anna Mueller', email: 'anna@email.de', phone: '+49 170 1234567', checkin: '20240410', checkout: '20240415', dogs: 0],
            [houseIdx: 0, guest: 'Pierre Dubois', email: 'pierre@email.fr', phone: '+33 6 12345678', checkin: '20240420', checkout: '20240425', dogs: 2],
            [houseIdx: 2, guest: 'Marco Rossi', email: 'marco@email.it', phone: '+39 333 1234567', checkin: '20240501', checkout: '20240507', dogs: 0],
            [houseIdx: 3, guest: 'Emma Wilson', email: 'emma@email.co.uk', phone: '+44 7700 900123', checkin: '20240510', checkout: '20240514', dogs: 1],
            [houseIdx: 1, guest: 'Carlos Garcia', email: 'carlos@email.es', phone: '+34 600 123456', checkin: '20240515', checkout: '20240520', dogs: 0],
            [houseIdx: 4, guest: 'Sophie Leclerc', email: 'sophie@email.fr', phone: '+33 6 98765432', checkin: '20240525', checkout: '20240530', dogs: 0],
            [houseIdx: 2, guest: 'Hans Schmidt', email: 'hans@email.de', phone: '+49 170 7654321', checkin: '20240601', checkout: '20240608', dogs: 1],
            [houseIdx: 0, guest: 'Lisa Anderson', email: 'lisa@email.se', phone: '+46 70 1234567', checkin: '20240610', checkout: '20240615', dogs: 0],
            [houseIdx: 3, guest: 'Thomas Brown', email: 'thomas@email.au', phone: '+61 400 123 456', checkin: '20240620', checkout: '20240627', dogs: 2]
        ]
        
        def created = 0
        bookings.each { data ->
            def house = houses[Math.min(data.houseIdx, houses.size() - 1)]
            def booking = new Booking(house.getOid() as int, data.checkin, data.checkout, 
                                      data.guest, data.email, data.phone, '', data.dogs)
            booking.setStatus('confirmed')
            
            def tc = perst.perstCreateContainer()
            tc.addInsert(booking)
            perst.perstStore(tc)
            created++
        }
        
        return [status: 'created', count: created]
    }
    
    /**
     * Create sample schedules
     */
    private def createSchedules(PerstConnection perst) {
        def existingSchedules = perst.getAll(Schedule)
        if (existingSchedules.size() >= 15) {
            return [status: 'exists', count: existingSchedules.size()]
        }
        
        def cleaners = perst.getAll(Cleaner)
        def bookings = perst.getAll(Booking)
        
        if (cleaners.isEmpty() || bookings.isEmpty()) {
            return [status: 'skipped', message: 'Need cleaners and bookings first']
        }
        
        def statuses = ['scheduled', 'completed', 'pending']
        def created = 0
        
        // Create schedules for first 5 bookings
        5.times { i ->
            if (i >= bookings.size()) return
            
            def booking = bookings[i]
            def cleaner = cleaners[i % cleaners.size()]
            
            // Schedule(cleanerId, bookingId, scheduleDate, startTime, endTime, notes)
            def schedule = new Schedule(
                cleaner.getOid() as int,
                booking.getOid() as int,
                "2024040${i+1}",
                "09:00",
                "12:00",
                "Checkout cleaning"
            )
            schedule.setStatus(statuses[i % 3])
            
            def tc = perst.perstCreateContainer()
            tc.addInsert(schedule)
            perst.perstStore(tc)
            created++
            
            // Mid-stay cleaning for longer bookings
            if (i < 3) {
                def midSchedule = new Schedule(
                    cleaner.getOid() as int,
                    booking.getOid() as int,
                    "2024040${i+3}",
                    "10:00",
                    "13:00",
                    "Mid-stay cleaning"
                )
                midSchedule.setStatus('pending')
                
                def tc2 = perst.perstCreateContainer()
                tc2.addInsert(midSchedule)
                perst.perstStore(tc2)
                created++
            }
        }
        
        return [status: 'created', count: created]
    }
    
    /**
     * Clear all test data
     */
    void clear(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            PerstConnection perst = getPerst()
            
            def counts = [:]
            
            // Delete schedules
            def schedules = perst.getAll(Schedule)
            schedules.each { 
                def tc = perst.perstCreateContainer()
                tc.addDelete(it)
                perst.perstStore(tc)
            }
            counts.schedules = schedules.size()
            
            // Delete bookings
            def bookings = perst.getAll(Booking)
            bookings.each { 
                def tc = perst.perstCreateContainer()
                tc.addDelete(it)
                perst.perstStore(tc)
            }
            counts.bookings = bookings.size()
            
            // Delete houses
            def houses = perst.getAll(House)
            houses.each { 
                def tc = perst.perstCreateContainer()
                tc.addDelete(it)
                perst.perstStore(tc)
            }
            counts.houses = houses.size()
            
            // Delete cleaners
            def cleaners = perst.getAll(Cleaner)
            cleaners.each { 
                def tc = perst.perstCreateContainer()
                tc.addDelete(it)
                perst.perstStore(tc)
            }
            counts.cleaners = cleaners.size()
            
            // Delete owners
            def owners = perst.getAll(Owner)
            owners.each { 
                def tc = perst.perstCreateContainer()
                tc.addDelete(it)
                perst.perstStore(tc)
            }
            counts.owners = owners.size()
            
            outjson.put("_Success", true)
            outjson.put("deleted", counts)
            
        } catch (Exception e) {
            outjson.put("_Success", false)
            outjson.put("error", e.message)
        }
    }
}
