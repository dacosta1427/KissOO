package services;

import koo.oodb.core.actor.AActor;
import koo.oodb.core.actor.ActorManager;
import koo.oodb.core.user.PerstUser;
import koo.security.EndpointMethod;
import org.garret.perst.json.JSONObject;
import org.kissweb.restServer.ProcessServlet;
import org.kissweb.restServer.UserData;
import org.kissweb.database.Connection;

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
 *   "_method": "getAllActors",   // or "getAActor", "createActor"
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
 * 
 * ======================= ENDPOINTMETHOD PATTERN =======================
 * 
 * This service also demonstrates the EndpointMethod pattern for type-safe
 * authorization. Each endpoint is defined as a static EndpointMethod that
 * can be granted via Agreement.
 * 
 * Usage in Agreement:
 *   Agreement agreement = new Agreement("ADMIN");
 *   agreement.grant(ActorService.GET_ACTOR);      // Type-safe!
 *   agreement.grant(ActorService.CREATE_ACTOR);
 * 
 * OR via CRUD:
 *   agreement.grant("AActor", "read");
 *   agreement.grant("AActor", "create");
 */
public class ActorService {
    
    // ======================= ENDPOINT METHODS =======================
    // These can be granted via Agreement for type-safe authorization
    
    /** Get a single actor by UUID */
    public static final EndpointMethod GET_ACTOR = new EndpointMethod("services.ActorService.getAActor", AActor.class) {
        @Override
        protected boolean doExecute(JSONObject in, JSONObject out, Connection db, ProcessServlet servlet) {
            // Inline implementation - authorization done by Agreement
            AActor caller = getAuthenticatedActor(servlet);
            if (caller == null) {
                out.put("error", "Not authenticated");
                return false;
            }
            
            String uuid = in.getString("uuid");
            if (uuid == null || uuid.isEmpty()) {
                out.put("error", "uuid is required");
                return false;
            }
            
            AActor AActor = ActorManager.getByUuid(caller, uuid);
            if (AActor == null) {
                out.put("error", "Not authorized or AActor not found");
                return false;
            }
            
            out.put("AActor", AActor.toJSON());
            out.put("source", "perst");
            return true;
        }
    };
    
    /** Get all actors */
    public static final EndpointMethod GET_ALL_ACTORS = new EndpointMethod("services.ActorService.getAllActors", AActor.class) {
        @Override
        protected boolean doExecute(JSONObject in, JSONObject out, Connection db, ProcessServlet servlet) {
            AActor caller = getAuthenticatedActor(servlet);
            if (caller == null) {
                out.put("error", "Not authenticated");
                return false;
            }
            
            Collection<AActor> AActors = ActorManager.getAll(caller);
            if (AActors == null) {
                out.put("error", "Not authorized to list Actors");
                return false;
            }
            
            List<Map<String, Object>> actorList = new ArrayList<>();
            for (AActor AActor : AActors) {
                actorList.add(AActor.toJSON());
            }
            out.put("AActors", actorList);
            out.put("source", "perst");
            out.put("count", actorList.size());
            return true;
        }
    };
    
    /** Create a new actor */
    public static final EndpointMethod CREATE_ACTOR = new EndpointMethod("services.ActorService.createActor", AActor.class) {
        @Override
        protected boolean doExecute(JSONObject in, JSONObject out, Connection db, ProcessServlet servlet) {
            AActor caller = getAuthenticatedActor(servlet);
            if (caller == null) {
                out.put("error", "Not authenticated");
                return false;
            }
            
            String name = in.getString("name");
            String type = in.getString("type");
            if (type == null || type.isEmpty()) type = "RETAIL";
            
            if (name == null || name.isEmpty()) {
                out.put("error", "name is required");
                return false;
            }
            
            AActor AActor = ActorManager.create(caller, name, type);
            if (AActor == null) {
                out.put("error", "Not authorized to create AActor");
                return false;
            }
            
            out.put("AActor", AActor.toJSON());
            out.put("status", "created");
            return true;
        }
    };
    
    /** Update an actor */
    public static final EndpointMethod UPDATE_ACTOR = new EndpointMethod("services.ActorService.updateActor", AActor.class) {
        @Override
        protected boolean doExecute(JSONObject in, JSONObject out, Connection db, ProcessServlet servlet) {
            AActor caller = getAuthenticatedActor(servlet);
            if (caller == null) {
                out.put("error", "Not authenticated");
                return false;
            }
            
            String uuid = in.getString("uuid");
            String name = in.getString("name");
            
            if (uuid == null || uuid.isEmpty()) {
                out.put("error", "uuid is required");
                return false;
            }
            
            AActor AActor = ActorManager.getByUuid(caller, uuid);
            if (AActor == null) {
                out.put("error", "Not authorized or AActor not found");
                return false;
            }
            
            if (name != null && !name.isEmpty()) {
                AActor.setName(name);
            }
            
            if (!ActorManager.update(caller, AActor)) {
                out.put("error", "Not authorized to update AActor");
                return false;
            }
            
            out.put("AActor", AActor.toJSON());
            out.put("status", "updated");
            return true;
        }
    };
    
    /** Delete an actor */
    public static final EndpointMethod DELETE_ACTOR = new EndpointMethod("services.ActorService.deleteActor", AActor.class) {
        @Override
        protected boolean doExecute(JSONObject in, JSONObject out, Connection db, ProcessServlet servlet) {
            AActor caller = getAuthenticatedActor(servlet);
            if (caller == null) {
                out.put("error", "Not authenticated");
                return false;
            }
            
            String uuid = in.getString("uuid");
            if (uuid == null || uuid.isEmpty()) {
                out.put("error", "uuid is required");
                return false;
            }
            
            AActor AActor = ActorManager.getByUuid(caller, uuid);
            if (AActor == null) {
                out.put("error", "Not authorized or AActor not found");
                return false;
            }
            
            if (!ActorManager.delete(caller, AActor)) {
                out.put("error", "Not authorized to delete AActor");
                return false;
            }
            
            out.put("status", "deleted");
            out.put("uuid", uuid);
            return true;
        }
    };
    
    // ======================= INTERNAL ENDPOINTS =======================
    // These are NOT callable via REST (internal only)
    
    /** Internal calculation - not accessible via REST */
    public static final EndpointMethod DO_INTERNAL_CALC = new EndpointMethod("services.ActorService.doInternalCalc", AActor.class, false) {
        @Override
        protected boolean doExecute(JSONObject in, JSONObject out, Connection db, ProcessServlet servlet) {
            // Internal logic only - can only be called from within the app
            return true;
        }
    };
    
    // ======================= HELPER METHODS =======================
    
    /**
     * Helper method to get authenticated AActor from servlet.
     * Returns null if not authenticated.
     */
    private static AActor getAuthenticatedActor(ProcessServlet servlet) {
        UserData ud = servlet.getUserData();
        if (ud == null) {
            return null;
        }
        PerstUser pu = (PerstUser) ud.getUserData("perstUser");
        return pu != null ? pu.getAActor() : null;
    }
    
    // ======================= LEGACY METHODS (still work) =======================
    // These are kept for backward compatibility but delegate to EndpointMethods
    
    /**
     * Get an actor by UUID using Perst
     */
    public void getActor(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        // Delegate to endpoint
        GET_ACTOR.execute(injson, outjson, db, servlet);
    }
    
    /**
     * Get all actors using Perst
     */
    public void getAllActors(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        GET_ALL_ACTORS.execute(injson, outjson, db, servlet);
    }
    
    /**
     * Create a new actor using Perst
     */
    public void createActor(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        CREATE_ACTOR.execute(injson, outjson, db, servlet);
    }
    
    /**
     * Update an actor
     */
    public void updateActor(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        UPDATE_ACTOR.execute(injson, outjson, db, servlet);
    }
    
    /**
     * Delete an actor
     */
    public void deleteActor(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        DELETE_ACTOR.execute(injson, outjson, db, servlet);
    }
}
