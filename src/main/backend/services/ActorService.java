package services;

import domain.Actor;
import domain.database.ActorManager;
import domain.database.PerstHelper;
import org.kissweb.json.JSONObject;
import org.kissweb.restServer.ProcessServlet;

import java.util.Collection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * ActorService - Demonstrates Perst integration with kissweb
 * 
 * This service shows how to use both PostgreSQL (via Connection) and
 * Perst (via PerstHelper) in the same service.
 * 
 * HTTP API Call Format:
 * {
 *   "_class": "services.ActorService",
 *   "_method": "getAllActors",   // or "getActor", "createActor"
 *   "_uuid": "session-uuid",     // from login
 *   ...params...
 * }
 */
public class ActorService {
    
    /**
     * Get an actor by UUID using Perst
     * 
     * HTTP Request:
     * {
     *   "_class": "services.ActorService",
     *   "_method": "getActor",
     *   "_uuid": "session-uuid",
     *   "uuid": "actor-uuid"
     * }
     * 
     * @param injson Input JSON with uuid
     * @param outjson Output JSON with actor data
     * @param db Connection (PostgreSQL)
     * @param servlet Servlet reference
     */
    public void getActor(JSONObject injson, JSONObject outjson, org.kissweb.database.Connection db, ProcessServlet servlet) {
        String uuid = injson.getString("uuid");
        if (uuid == null || uuid.isEmpty()) {
            outjson.put("error", "uuid is required");
            return;
        }
        
        // Try Perst first if available
        Actor actor = ActorManager.getInstance().getByUuid(uuid);
        if (actor != null) {
            outjson.put("actor", actor.toJSON());
            outjson.put("source", "perst");
            return;
        }
        
        // Fallback: Not found
        outjson.put("error", "Actor not found");
        outjson.put("uuid", uuid);
    }
    
    /**
     * Get all actors using Perst
     * 
     * HTTP Request:
     * {
     *   "_class": "services.ActorService",
     *   "_method": "getAllActors",
     *   "_uuid": "session-uuid"
     * }
     * 
     * @param injson Input JSON
     * @param outjson Output JSON with actors array
     * @param db Connection
     * @param servlet Servlet reference
     */
    public void getAllActors(JSONObject injson, JSONObject outjson, org.kissweb.database.Connection db, ProcessServlet servlet) {
        
        // Try Perst if available
        Collection<Actor> actors = ActorManager.getInstance().getAll();
        if (actors != null) {
            List<Map<String, Object>> actorList = new ArrayList<>();
            for (Actor actor : actors) {
                actorList.add(actor.toJSON());
            }
            outjson.put("actors", actorList);
            outjson.put("source", "perst");
            outjson.put("count", actorList.size());
            return;
        }
        
        // Fallback
        outjson.put("error", "Perst not available");
    }
    
    /**
     * Create a new actor using Perst
     * 
     * HTTP Request:
     * {
     *   "_class": "services.ActorService",
     *   "_method": "createActor",
     *   "_uuid": "session-uuid",
     *   "name": "John",
     *   "type": "RETAIL"   // optional, defaults to RETAIL
     * }
     * 
     * @param injson Input JSON with name and optional type
     * @param outjson Output JSON with created actor
     * @param db Connection
     * @param servlet Servlet reference
     */
    public void createActor(JSONObject injson, JSONObject outjson, org.kissweb.database.Connection db, ProcessServlet servlet) {
        String name = injson.getString("name");
        String type = injson.getString("type");
        if (type == null || type.isEmpty())
            type = "RETAIL";
        
        if (name == null || name.isEmpty()) {
            outjson.put("error", "name is required");
            return;
        }
        
        if (PerstHelper.isAvailable()) {
            // Start transaction
            PerstHelper.startTransaction();
            
            try {
                // Create actor via Manager
                Actor actor = ActorManager.getInstance().create(name, type);
                
                // Commit
                PerstHelper.commitTransaction();
                
                outjson.put("actor", actor.toJSON());
                outjson.put("status", "created");
                return;
                
            } catch (Exception e) {
                PerstHelper.rollbackTransaction();
                outjson.put("error", "Failed to create actor: " + e.getMessage());
                return;
            }
        }
        
        outjson.put("error", "Perst not available");
    }
}
