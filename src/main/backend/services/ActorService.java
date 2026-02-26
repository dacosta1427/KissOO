package services;

import gfe.Actor;
import gfe.PerstHelper;
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
 */
public class ActorService {
    
    /**
     * Get an actor by UUID using Perst
     * 
     * @param injson Input: { uuid: "..." }
     * @param outjson Output: { actor: { ... } }
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
        if (PerstHelper.isAvailable()) {
            Actor actor = PerstHelper.retrieveObject(Actor.class, uuid);
            if (actor != null) {
                outjson.put("actor", actor.toJSON());
                outjson.put("source", "perst");
                return;
            }
        }
        
        // Fallback: Not found
        outjson.put("error", "Actor not found");
        outjson.put("uuid", uuid);
    }
    
    /**
     * Get all actors using Perst
     * 
     * @param injson Input: { }
     * @param outjson Output: { actors: [ ... ] }
     * @param db Connection
     * @param servlet Servlet reference
     */
    public void getAllActors(JSONObject injson, JSONObject outjson, org.kissweb.database.Connection db, ProcessServlet servlet) {
        
        // Try Perst if available
        if (PerstHelper.isAvailable()) {
            Collection<Actor> actors = PerstHelper.retrieveAllObjects(Actor.class);
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
     * @param injson Input: { name: "...", type: "..." }
     * @param outjson Output: { actor: { ... } }
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
                // Create actor
                Actor actor = new Actor(name, type);
                
                // Store in Perst
                PerstHelper.storeNewObject(actor);
                
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
