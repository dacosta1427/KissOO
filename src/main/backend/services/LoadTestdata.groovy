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

class LoadTestdata {

    void load(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            if (!PerstStorageManager.isAvailable()) {
                outjson.put("_Success", false)
                outjson.put("error", "Perst is not available")
                return
            }
            
            def results = [:]
            
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
            
            def owners = PerstStorageManager.getAll(Owner.class)
            if (owners.size() < 3) {
                def data = [
                    [name: 'Jan de Vries', email: 'jan@example.com', phone: '+31 6 12345678'],
                    [name: 'Maria Jansen', email: 'maria@example.com', phone: '+31 6 23456789'],
                    [name: 'Peter Bakker', email: 'peter@example.com', phone: '+31 6 34567890']
                ]
                data.each { d ->
                    def owner = new Owner(d.name, d.email, d.phone, "Address TBD")
                    def tc = PerstStorageManager.createContainer()
                    tc.addInsert(owner)
                    PerstStorageManager.store(tc)
                }
                results.owners = "created ${data.size()}"
            } else {
                results.owners = "exists ${owners.size()}"
            }
            
            def houses = PerstStorageManager.getAll(House.class)
            if (houses.size() < 5) {
                owners = PerstStorageManager.getAll(Owner.class)
                def data = [
                    [name: 'Strandhuis', ownerIdx: 0],
                    [name: 'Stadswoning', ownerIdx: 1],
                    [name: 'Boerderij', ownerIdx: 0],
                    [name: 'Appartement', ownerIdx: 1],
                    [name: 'Villa', ownerIdx: 2]
                ]
                data.each { d ->
                    def owner = owners[d.ownerIdx]
                    def house = new House(d.name, "Address TBD", "Description TBD", (long)owner.getOid(), true)
                    def tc = PerstStorageManager.createContainer()
                    tc.addInsert(house)
                    PerstStorageManager.store(tc)
                }
                results.houses = "created ${data.size()}"
            } else {
                results.houses = "exists ${houses.size()}"
            }
            
            def cleaners = PerstStorageManager.getAll(Cleaner.class)
            if (cleaners.size() < 4) {
                def data = [
                    [name: 'Lisa Smit', phone: '+31 6 11112222', email: 'lisa@example.com'],
                    [name: 'Emma de Jong', phone: '+31 6 22223333', email: 'emma@example.com'],
                    [name: 'Sophie Mulder', phone: '+31 6 33334444', email: 'sophie@example.com'],
                    [name: 'Anna Visser', phone: '+31 6 44445555', email: 'anna@example.com']
                ]
                data.each { d ->
                    def cleaner = new Cleaner(d.name, d.phone, d.email, "Address TBD", true)
                    def tc = PerstStorageManager.createContainer()
                    tc.addInsert(cleaner)
                    PerstStorageManager.store(tc)
                }
                results.cleaners = "created ${data.size()}"
            } else {
                results.cleaners = "exists ${cleaners.size()}"
            }
            
            def bookings = PerstStorageManager.getAll(Booking.class)
            if (bookings.size() < 5) {
                houses = PerstStorageManager.getAll(House.class)
                def data = [
                    [houseIdx: 0, guest: 'John Smith'],
                    [houseIdx: 1, guest: 'Anna Mueller'],
                    [houseIdx: 0, guest: 'Pierre Dubois'],
                    [houseIdx: 2, guest: 'Marco Rossi'],
                    [houseIdx: 3, guest: 'Emma Wilson']
                ]
                data.each { d ->
                    def house = houses[d.houseIdx]
                    def booking = new Booking((int)house.getOid(), "20240401", "20240405", d.guest, "", "", "")
                    def tc = PerstStorageManager.createContainer()
                    tc.addInsert(booking)
                    PerstStorageManager.store(tc)
                }
                results.bookings = "created ${data.size()}"
            } else {
                results.bookings = "exists ${bookings.size()}"
            }
            
            def schedules = PerstStorageManager.getAll(Schedule.class)
            if (schedules.size() < 5) {
                cleaners = PerstStorageManager.getAll(Cleaner.class)
                bookings = PerstStorageManager.getAll(Booking.class)
                if (cleaners && bookings) {
                    5.times { i ->
                        if (i < bookings.size()) {
                            def schedule = new Schedule((int)cleaners[0].getOid(), (int)bookings[i].getOid(), "2024040${i+1}".toString(), "09:00", "12:00")
                            def tc = PerstStorageManager.createContainer()
                            tc.addInsert(schedule)
                            PerstStorageManager.store(tc)
                        }
                    }
                    results.schedules = "created 5"
                } else {
                    results.schedules = "skipped (no cleaners or bookings)"
                }
            } else {
                results.schedules = "exists ${schedules.size()}"
            }
            
            outjson.put("_Success", true)
            outjson.put("results", results)
            
        } catch (Exception e) {
            outjson.put("_Success", false)
            outjson.put("error", "${e.class.name}: ${e.message}")
            e.printStackTrace()
        }
    }

    void clear(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            if (!PerstStorageManager.isAvailable()) {
                outjson.put("_Success", false)
                outjson.put("error", "Perst is not available")
                return
            }
            
            PerstStorageManager.getAll(Schedule.class).each { 
                def tc = PerstStorageManager.createContainer()
                tc.addDelete(it)
                PerstStorageManager.store(tc)
            }
            
            PerstStorageManager.getAll(Booking.class).each { 
                def tc = PerstStorageManager.createContainer()
                tc.addDelete(it)
                PerstStorageManager.store(tc)
            }
            
            PerstStorageManager.getAll(House.class).each { 
                def tc = PerstStorageManager.createContainer()
                tc.addDelete(it)
                PerstStorageManager.store(tc)
            }
            
            PerstStorageManager.getAll(Cleaner.class).each { 
                def tc = PerstStorageManager.createContainer()
                tc.addDelete(it)
                PerstStorageManager.store(tc)
            }
            
            PerstStorageManager.getAll(Owner.class).each { 
                def tc = PerstStorageManager.createContainer()
                tc.addDelete(it)
                PerstStorageManager.store(tc)
            }
            
            outjson.put("_Success", true)
            
        } catch (Exception e) {
            outjson.put("_Success", false)
            outjson.put("error", "${e.class.name}: ${e.message}")
            e.printStackTrace()
        }
    }
}
