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

    void getPhones(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            Collection<Phone> phones = PhoneManager.getAll()
            JSONArray rows = new JSONArray()

            for (Phone phone : phones) {
                JSONObject row = new JSONObject()
                row.put("id", phone.getOid())
                row.put("firstName", phone.getFirstName())
                row.put("lastName", phone.getLastName())
                row.put("phoneNumber", phone.getPhoneNumber())
                rows.put(row)
            }

            outjson.put("rows", rows)
        } catch (Exception e) {
            outjson.put("error", e.message)
        }
    }

    void createPhone(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            Phone phone = PhoneManager.create(
                injson.getString("firstName"),
                injson.getString("lastName"),
                injson.getString("phoneNumber")
            )
            outjson.put("success", true)
            outjson.put("id", phone.getOid())
        } catch (Exception e) {
            outjson.put("error", e.message)
        }
    }

    void updatePhone(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            long oid = injson.getLong("id")
            Phone phoneToUpdate = PhoneManager.getByOid(oid)

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

    void deletePhone(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            long oid = injson.getLong("id")
            Phone phoneToDelete = PhoneManager.getByOid(oid)

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
