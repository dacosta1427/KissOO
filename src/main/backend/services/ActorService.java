package services;

import gfe.Actor;
import nl.dcg.gfe.PerstConnection;
import org.json.JSONObject;

import java.util.Collection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * ActorService - Demonstrates Perst integration with kissweb
 * 
 * This service shows how to use both PostgreSQL (via Connection) and
 * Perst (via PerstConnection) in the same service.
 */
public class ActorService {
    
    /**
     * Get an actor by UUID using Perst
     * 
     * @param injson Input: { uuid: "..." }
     * @param outjson Output: { actor: { ... } }
     * @param db Connection (can be PerstConnection if Perst enabled)
     * @param servlet Servlet reference
     */
    public void getActor(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        String uuid = injson.optString("uuid");
        
        // Try Perst first if available
        if (db instanceof PerstConnection) {
            PerstConnection perstDb = (PerstConnection) db;
            if (perstDb.isPerstAvailable()) {
                Actor actor = perstDb.retrieveObject(Actor.class, uuid);
                if (actor != null) {
                    outjson.put("actor", actor.toJSON());
                    outjson.put("source", "perst");
                    return;
                }
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
    public void getAllActors(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        
        // Try Perst if available
        if (db instanceof PerstConnection) {
            PerstConnection perstDb = (PerstConnection) db;
            if (perstDb.isPerstAvailable()) {
                Collection<Actor> actors = perstDb.retrieveAllObjects(Actor.class);
                List<Map<String, Object>> actorList = new ArrayList<>();
                for (Actor actor : actors) {
                    actorList.add(actor.toJSON());
                }
                outjson.put("actors", actorList);
                outjson.put("source", "perst");
                outjson.put("count", actorList.size());
                return;
            }
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
    public void createActor(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        String name = injson.optString("name");
        String type = injson.optString("type", "RETAIL");
        
        if (db instanceof PerstConnection) {
            PerstConnection perstDb = (PerstConnection) db;
            if (perstDb.isPerstAvailable()) {
                
                // Start transaction
                perstDb.startTransaction();
                
                try {
                    // Create actor
                    Actor actor = new Actor(name, type);
                    
                    // Store in Perst
                    perstDb.storeNewObject(actor);
                    
                    // Commit
                    perstDb.endTransaction();
                    
                    outjson.put("actor", actor.toJSON());
                    outjson.put("status", "created");
                    return;
                    
                } catch (Exception e) {
                    perstDb.rollbackTransaction();
                    outjson.put("error", "Failed to create actor: " + e.getMessage());
                    return;
                }
            }
        }
        
        outjson.put("error", "Perst not available");
    }
}
