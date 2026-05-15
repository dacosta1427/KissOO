package services;

import koo.core.actor.AActor;
import koo.core.actor.ActorManager;
import koo.core.user.PerstUser;
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
 */
public class ActorService {

    // ======================= ENDPOINT METHODS =======================

    /** Get all actors */
    public static final EndpointMethod GET_ALL_ACTORS = new EndpointMethod("services.ActorService.getAllActors", AActor.class) {
        @Override
        protected boolean doExecute(JSONObject in, JSONObject out, Connection db, ProcessServlet servlet) {
            AActor caller = getAuthenticatedActor(servlet);
            if (caller == null) {
                out.put("error", "Not authenticated");
                return false;
            }

            Collection<AActor> AActors = ActorManager.getAll();
            if (AActors == null) {
                out.put("error", "Not authorized to list Actors");
                return false;
            }

            List<JSONObject> actorList = new ArrayList<>();
            for (AActor AActor : AActors) {
                actorList.add(AActor.toJSONObject());
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

            AActor AActor = ActorManager.create(name, type);
            if (AActor == null) {
                out.put("error", "Not authorized to create AActor");
                return false;
            }

            out.put("AActor", AActor.toJSONObject());
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

            String name = in.getString("name");

            if (name == null || name.isEmpty()) {
                out.put("error", "name is required to identify actor");
                return false;
            }

            AActor AActor = ActorManager.getByName(name);
            if (AActor == null) {
                out.put("error", "Actor not found");
                return false;
            }

            if (in.has("newName") && !in.getString("newName").isEmpty()) {
                AActor.setName(in.getString("newName"));
            }

            if (!ActorManager.update(AActor)) {
                out.put("error", "Failed to update AActor");
                return false;
            }

            out.put("AActor", AActor.toJSONObject());
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

            String name = in.getString("name");
            if (name == null || name.isEmpty()) {
                out.put("error", "name is required");
                return false;
            }

            AActor AActor = ActorManager.getByName(name);
            if (AActor == null) {
                out.put("error", "Actor not found");
                return false;
            }

            if (!ActorManager.delete(AActor)) {
                out.put("error", "Failed to delete AActor");
                return false;
            }

            out.put("status", "deleted");
            out.put("name", name);
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

    public void getActor(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        GET_ALL_ACTORS.execute(injson, outjson, db, servlet);
    }

    public void getAllActors(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        GET_ALL_ACTORS.execute(injson, outjson, db, servlet);
    }

    public void createActor(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        CREATE_ACTOR.execute(injson, outjson, db, servlet);
    }

    public void updateActor(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        UPDATE_ACTOR.execute(injson, outjson, db, servlet);
    }

    public void deleteActor(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        DELETE_ACTOR.execute(injson, outjson, db, servlet);
    }
}