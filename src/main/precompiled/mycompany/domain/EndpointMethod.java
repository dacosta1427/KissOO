package mycompany.domain;

import org.kissweb.json.JSONObject;
import org.kissweb.database.Connection;
import org.kissweb.restServer.ProcessServlet;
import org.kissweb.restServer.UserData;

/**
 * EndpointMethod - Abstract base for all service endpoints.
 * 
 * An EndpointMethod represents a callable method in a Service.
 * It can be either:
 * - External (default): callable via REST API
 * - Internal: callable only internally, not via REST
 * 
 * IMPORTANT: Every endpoint MUST check Agreement permission before executing.
 * This is done automatically in execute() - just call it!
 * 
 * Usage:
 *   public class ActorService {
 *       public static final EndpointMethod GET_ACTOR = new GetActorEndpoint(Actor.class);
 *       public static final EndpointMethod DO_INTERNAL = new InternalCalcEndpoint(Actor.class, false);
 *   }
 */
public abstract class EndpointMethod {
    
    private final String name;
    private final Class<?> resourceClass;  // Type-safe resource class
    private final boolean external;  // true = callable via REST
    
    /**
     * Create an external endpoint
     */
    protected EndpointMethod(String name, Class<?> resourceClass) {
        this(name, resourceClass, true);
    }
    
    /**
     * Create an endpoint (external or internal)
     */
    protected EndpointMethod(String name, Class<?> resourceClass, boolean external) {
        this.name = name;
        this.resourceClass = resourceClass;
        this.external = external;
    }
    
    /**
     * Get the endpoint identifier (e.g., "services.ActorService.getActor")
     */
    public String getName() {
        return name;
    }
    
    /**
     * Get the resource class this endpoint operates on
     */
    public Class<?> getResourceClass() {
        return resourceClass;
    }
    
    /**
     * Is this endpoint callable externally via REST?
     */
    public boolean isExternal() {
        return external;
    }
    
    /**
     * Is this endpoint internal only?
     */
    public boolean isInternal() {
        return !external;
    }
    
    /**
     * Execute the endpoint with automatic Agreement authorization.
     * 
     * This is the gate - it checks:
     * 1. Is this an external call to an internal endpoint? → Deny
     * 2. Is there a caller with an Agreement? → Check permission
     * 3. Otherwise → Allow (for backward compatibility)
     */
    public boolean execute(JSONObject in, JSONObject out, Connection db, ProcessServlet servlet) {
        // 1. Check if external call to internal endpoint
        if (!isExternal()) {
            return false;  // Internal only - deny external calls
        }
        
        // 2. Get caller and check Agreement
        Actor caller = getCaller(servlet);
        if (caller != null) {
            // Agreement MUST exist (enforced by Actor constructor)
            if (!caller.getAgreement().grants(this, resourceClass, CRUD.EXECUTE)) {
                return false;  // Not authorized
            }
        }
        
        // 3. Execute the actual logic
        return doExecute(in, out, db, servlet);
    }
    
    /**
     * Helper to get authenticated Actor from servlet
     */
    private Actor getCaller(ProcessServlet servlet) {
        UserData ud = servlet.getUserData();
        if (ud == null) {
            return null;
        }
        PerstUser pu = (PerstUser) ud.getUserData("perstUser");
        return pu != null ? pu.getActor() : null;
    }
    
    /**
     * Override with actual endpoint logic.
     */
    protected abstract boolean doExecute(JSONObject in, JSONObject out, Connection db, ProcessServlet servlet);
    
    @Override
    public String toString() {
        return "EndpointMethod{" + name + ", resource=" + resourceClass.getSimpleName() + 
               (external ? " [external]" : " [internal]") + "}";
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        EndpointMethod that = (EndpointMethod) obj;
        return name.equals(that.name);
    }
    
    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
