package services

import org.kissweb.json.JSONArray
import org.kissweb.json.JSONObject
import org.kissweb.database.Connection
import org.kissweb.restServer.ProcessServlet
import mycompany.database.PhoneManager
import mycompany.domain.Phone

/**
 * Crud service for phone book operations using Perst.
 */
class Crud {

    void getRecords(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        if (!PhoneManager.getAll()) {
            outjson.put("error", "Perst not available")
            return
        }
        
        Collection<Phone> phones = PhoneManager.getAll()
        JSONArray rows = new JSONArray()
        
        int id = 0
        for (Phone phone : phones) {
            JSONObject row = new JSONObject()
            row.put("id", id++)
            row.put("firstName", phone.getFirstName())
            row.put("lastName", phone.getLastName())
            row.put("phoneNumber", phone.getPhoneNumber())
            rows.put(row)
        }
        
        outjson.put("rows", rows)
    }

    void addRecord(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            Phone phone = PhoneManager.create(
                injson.getString("firstName"),
                injson.getString("lastName"),
                injson.getString("phoneNumber")
            )
            outjson.put("success", true)
        } catch (Exception e) {
            outjson.put("error", e.message)
        }
    }

    void updateRecord(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            int id = injson.getInt("id")
            Collection<Phone> phones = PhoneManager.getAll()
            
            Phone phoneToUpdate = null
            int currentId = 0
            for (Phone phone : phones) {
                if (currentId == id) {
                    phoneToUpdate = phone
                    break
                }
                currentId++
            }
            
            if (phoneToUpdate == null) {
                outjson.put("error", "Record not found")
                return
            }
            
            phoneToUpdate.setFirstName(injson.getString("firstName"))
            phoneToUpdate.setLastName(injson.getString("lastName"))
            phoneToUpdate.setPhoneNumber(injson.getString("phoneNumber"))
            
            PhoneManager.update(phoneToUpdate)
            outjson.put("success", true)
        } catch (Exception e) {
            outjson.put("error", e.message)
        }
    }

    void deleteRecord(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            int id = injson.getInt("id")
            Collection<Phone> phones = PhoneManager.getAll()
            
            Phone phoneToDelete = null
            int currentId = 0
            for (Phone phone : phones) {
                if (currentId == id) {
                    phoneToDelete = phone
                    break
                }
                currentId++
            }
            
            if (phoneToDelete == null) {
                outjson.put("error", "Record not found")
                return
            }
            
            PhoneManager.delete(phoneToDelete)
            outjson.put("success", true)
        } catch (Exception e) {
            outjson.put("error", e.message)
        }
    }

    void runReport(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        outjson.put("reportUrl", "Reports not implemented for Perst")
    }

    void runExport(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        outjson.put("exportUrl", "Export not implemented for Perst")
    }
}
