package example.managers;

import koo.framework.services.FrameworkService;
import org.kissweb.database.Connection;
import org.kissweb.json.JSONObject;
import org.kissweb.restServer.ProcessServlet;

public class HouseManager extends FrameworkService {
    
    public static java.util.Collection<example.domain.House> getAll() {
        // TODO: Implement using PerstStorageManager
        return new java.util.ArrayList<>();
    }
    
    public static example.domain.House getByOid(long oid) {
        // TODO: Implement
        return null;
    }
    
    public static void create(example.domain.House house) {
        // TODO: Implement
    }
    
    public static void update(example.domain.House house) {
        // TODO: Implement
    }
    
    public static void delete(example.domain.House house) {
        // TODO: Implement
    }
    
    public static java.util.Collection<example.domain.House> search(String address, double minPrice, double maxPrice) {
        // TODO: Implement
        return new java.util.ArrayList<>();
    }
}