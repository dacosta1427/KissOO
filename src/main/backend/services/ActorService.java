package services;

import domain.Actor;
import domain.database.ActorManager;
import domain.database.BaseManager;
import org.kissweb.json.JSONObject;
import org.kissweb.restServer.ProcessServlet;
import org.kissweb.restServer.UserData;

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
 * 
 * IMPORTANT: This service demonstrates the "early rejection" pattern:
 * 1. First authenticate via UserData
 * 2. Then authorize via Manager methods (action is auto-identified)
 * 3. Only then execute the operation
 * 
 * The Manager methods automatically identify the action (READ, CREATE, UPDATE, DELETE)
 * and check permissions. Service just calls the method.
 */
public class ActorService {
    
    /**
     * Helper method to get authenticated Actor from servlet.
     * Returns null if not authenticated.
     */
    private Actor getAuthenticatedActor(ProcessServlet servlet) {
        UserData ud = servlet.getUserData();
        if (ud == null) {
            return null;
        }
        return ActorManager.getByUserId((int) ud.getUserId());
    }
    
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
        // EARLY REJECTION: Authenticate first
        Actor caller = getAuthenticatedActor(servlet);
        if (caller == null) {
            outjson.put("error", "Not authenticated");
            return;
        }
        
        String uuid = injson.getString("uuid");
        if (uuid == null || uuid.isEmpty()) {
            outjson.put("error", "uuid is required");
            return;
        }
        
        // Manager handles authorization (ACTION_READ) and lookup in one call
        Actor actor = ActorManager.getByUuid(caller, uuid);
        if (actor == null) {
            outjson.put("error", "Not authorized or Actor not found");
            return;
        }
        
        outjson.put("actor", actor.toJSON());
        outjson.put("source", "perst");
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
        // EARLY REJECTION: Authenticate first
        Actor caller = getAuthenticatedActor(servlet);
        if (caller == null) {
            outjson.put("error", "Not authenticated");
            return;
        }
        
        // Manager handles authorization (ACTION_READ) - returns null if not authorized
        Collection<Actor> actors = ActorManager.getAll(caller);
        if (actors == null) {
            outjson.put("error", "Not authorized to list Actors");
            return;
        }
        
        List<Map<String, Object>> actorList = new ArrayList<>();
        for (Actor actor : actors) {
            actorList.add(actor.toJSON());
        }
        outjson.put("actors", actorList);
        outjson.put("source", "perst");
        outjson.put("count", actorList.size());
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
        // EARLY REJECTION: Authenticate first
        Actor caller = getAuthenticatedActor(servlet);
        if (caller == null) {
            outjson.put("error", "Not authenticated");
            return;
        }
        
        String name = injson.getString("name");
        String type = injson.getString("type");
        if (type == null || type.isEmpty())
            type = "RETAIL";
        
        if (name == null || name.isEmpty()) {
            outjson.put("error", "name is required");
            return;
        }
        
        // Manager handles authorization (ACTION_CREATE) - returns null if not authorized
        Actor actor = ActorManager.create(caller, name, type);
        if (actor == null) {
            outjson.put("error", "Not authorized to create Actor");
            return;
        }
        
        outjson.put("actor", actor.toJSON());
        outjson.put("status", "created");
    }
    
    /**
     * Update an actor
     * 
     * HTTP Request:
     * {
     *   "_class": "services.ActorService",
     *   "_method": "updateActor",
     *   "_uuid": "session-uuid",
     *   "uuid": "actor-uuid",
     *   "name": "new-name"
     * }
     */
    public void updateActor(JSONObject injson, JSONObject outjson, org.kissweb.database.Connection db, ProcessServlet servlet) {
        // EARLY REJECTION: Authenticate first
        Actor caller = getAuthenticatedActor(servlet);
        if (caller == null) {
            outjson.put("error", "Not authenticated");
            return;
        }
        
        String uuid = injson.getString("uuid");
        String name = injson.getString("name");
        
        if (uuid == null || uuid.isEmpty()) {
            outjson.put("error", "uuid is required");
            return;
        }
        
        // Get actor - needs READ permission
        Actor actor = ActorManager.getByUuid(caller, uuid);
        if (actor == null) {
            outjson.put("error", "Not authorized or Actor not found");
            return;
        }
        
        // Update the actor locally
        if (name != null && !name.isEmpty()) {
            actor.setName(name);
        }
        
        // Manager handles authorization (ACTION_UPDATE)
        if (!ActorManager.update(caller, actor)) {
            outjson.put("error", "Not authorized to update Actor");
            return;
        }
        
        outjson.put("actor", actor.toJSON());
        outjson.put("status", "updated");
    }
    
    /**
     * Delete an actor
     * 
     * HTTP Request:
     * {
     *   "_class": "services.ActorService",
     *   "_method": "deleteActor",
     *   "_uuid": "session-uuid",
     *   "uuid": "actor-uuid"
     * }
     */
    public void deleteActor(JSONObject injson, JSONObject outjson, org.kissweb.database.Connection db, ProcessServlet servlet) {
        // EARLY REJECTION: Authenticate first
        Actor caller = getAuthenticatedActor(servlet);
        if (caller == null) {
            outjson.put("error", "Not authenticated");
            return;
        }
        
        String uuid = injson.getString("uuid");
        if (uuid == null || uuid.isEmpty()) {
            outjson.put("error", "uuid is required");
            return;
        }
        
        // Get actor - needs READ permission
        Actor actor = ActorManager.getByUuid(caller, uuid);
        if (actor == null) {
            outjson.put("error", "Not authorized or Actor not found");
            return;
        }
        
        // Manager handles authorization (ACTION_DELETE)
        if (!ActorManager.delete(caller, actor)) {
            outjson.put("error", "Not authorized to delete Actor");
            return;
        }
        
        outjson.put("status", "deleted");
        outjson.put("uuid", uuid);
    }
}
