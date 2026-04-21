package services

import koo.core.database.StorageManager
import org.kissweb.json.JSONObject
import org.kissweb.database.Connection
import org.kissweb.restServer.ProcessServlet
import koo.security.EndpointRegistry
import koo.security.RolePermissions
import koo.core.actor.Role
import koo.core.actor.ActorManager
import koo.core.user.PerstUser

/**
 * PermissionService - Manages endpoint permissions via REST API.
 * 
 * Endpoints:
 * - GET /permissions - List all registered endpoints
 * - GET /permissions/role/{role} - Get permissions for a role
 * - POST /permissions/role/{role}/grant - Grant endpoint to role (SUPER_ADMIN only)
 * - POST /permissions/role/{role}/revoke - Revoke endpoint from role (SUPER_ADMIN only)
 * - GET /permissions/actor/{actorOid} - Get effective permissions for actor
 * - POST /permissions/actor/{actorOid}/grant - Grant endpoint to specific actor (ADMIN only)
 * - POST /permissions/actor/{actorOid}/revoke - Revoke endpoint from actor (ADMIN only)
 */
class PermissionService {
    
    /**
     * Check if caller is SUPER_ADMIN - required for role permission modifications.
     * @return true if authorized, false otherwise
     */
    private static boolean isSuperAdmin(ProcessServlet servlet) {
        try {
            PerstUser pu = (PerstUser) servlet.getUserData("perstUser")
            if (pu == null) return false
            def actor = pu.getAActor()
            if (actor == null || actor.getAgreement() == null) return false
            return actor.getAgreement().getRole() == Role.SUPER_ADMIN
        } catch (Exception e) {
            return false
        }
    }
    
    /**
     * Check if caller is ADMIN or SUPER_ADMIN - required for actor permission modifications.
     * @return true if authorized, false otherwise
     */
    private static boolean isAdmin(ProcessServlet servlet) {
        try {
            PerstUser pu = (PerstUser) servlet.getUserData("perstUser")
            if (pu == null) return false
            def actor = pu.getAActor()
            if (actor == null || actor.getAgreement() == null) return false
            def role = actor.getAgreement().getRole()
            return role == Role.ADMIN || role == Role.SUPER_ADMIN
        } catch (Exception e) {
            return false
        }
    }
    
    /**
     * GET /permissions - List all registered endpoints
     */
    static JSONObject listEndpoints(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            def endpoints = EndpointRegistry.getAllEndpoints()
            def list = []
            
            endpoints.each { name, bit ->
                list.add([
                    name: name,
                    bit: bit.toString(),
                    bitPosition: bit.bitLength()
                ])
            }
            
            // Sort by name
            list = list.sort { it.name }
            
            outjson.put("_Success", true)
            outjson.put("endpoints", list)
            outjson.put("count", list.size())
            
        } catch (Exception e) {
            outjson.put("_Success", false)
            outjson.put("error", "Failed to list endpoints: " + e.message)
        }
    }
    
    /**
     * GET /permissions/role/{role} - Get permissions for a role
     */
    static JSONObject getRolePermissions(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            def roleName = injson.optString("roleName", null)
            if (!roleName) {
                outjson.put("_Success", false)
                outjson.put("error", "Missing roleName parameter")
                return
            }
            
            def role = Role.valueOf(roleName.toUpperCase())
            def permissions = RolePermissions.getDefaultPermissions(role)
            def allEndpoints = EndpointRegistry.getAllEndpoints()
            
            // Build list of endpoints this role has
            def grantedEndpoints = []
            allEndpoints.each { name, bit ->
                if (permissions.and(bit).signum() > 0) {
                    grantedEndpoints.add(name)
                }
            }
            
            outjson.put("_Success", true)
            outjson.put("role", roleName)
            outjson.put("permissions", permissions.toString())
            outjson.put("endpointCount", grantedEndpoints.size())
            outjson.put("endpoints", grantedEndpoints)
            
        } catch (Exception e) {
            outjson.put("_Success", false)
            outjson.put("error", "Failed to get role permissions: " + e.message)
        }
    }
    
    /**
     * POST /permissions/role/{role}/grant - Grant endpoint to role (SUPER_ADMIN only)
     */
    static JSONObject grantRoleEndpoint(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        if (!isSuperAdmin(servlet)) {
            outjson.put("_Success", false)
            outjson.put("error", "Only SUPER_ADMIN can modify role permissions")
            return
        }
        
        try {
            def roleName = injson.optString("roleName", null)
            def endpointName = injson.optString("endpointName", null)
            
            if (!roleName || !endpointName) {
                outjson.put("_Success", false)
                outjson.put("error", "Missing roleName or endpointName parameter")
                return
            }
            
            def role = Role.valueOf(roleName.toUpperCase())
            RolePermissions.grantEndpointToRole(role, endpointName)
            
            outjson.put("_Success", true)
            outjson.put("message", "Granted ${endpointName} to role ${roleName}")
            
        } catch (Exception e) {
            outjson.put("_Success", false)
            outjson.put("error", "Failed to grant endpoint: " + e.message)
        }
    }
    
    /**
     * POST /permissions/role/{role}/revoke - Revoke endpoint from role (SUPER_ADMIN only)
     */
    static JSONObject revokeRoleEndpoint(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        if (!isSuperAdmin(servlet)) {
            outjson.put("_Success", false)
            outjson.put("error", "Only SUPER_ADMIN can modify role permissions")
            return
        }
        
        try {
            def roleName = injson.optString("roleName", null)
            def endpointName = injson.optString("endpointName", null)
            
            if (!roleName || !endpointName) {
                outjson.put("_Success", false)
                outjson.put("error", "Missing roleName or endpointName parameter")
                return
            }
            
            def role = Role.valueOf(roleName.toUpperCase())
            RolePermissions.revokeEndpointFromRole(role, endpointName)
            
            outjson.put("_Success", true)
            outjson.put("message", "Revoked ${endpointName} from role ${roleName}")
            
        } catch (Exception e) {
            outjson.put("_Success", false)
            outjson.put("error", "Failed to revoke endpoint: " + e.message)
        }
    }
    
    /**
     * GET /permissions/actor/{actorOid} - Get effective permissions for actor
     */
    static JSONObject getActorPermissions(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            long actorOid = injson.optLong("actorOid", 0)
            if (actorOid == 0) {
                outjson.put("_Success", false)
                outjson.put("error", "Missing actorOid parameter")
                return
            }
            
            def actor = ActorManager.getByOid(actorOid)
            if (!actor) {
                outjson.put("_Success", false)
                outjson.put("error", "Actor not found: " + actorOid)
                return
            }
            
            def agreement = actor.getAgreement()
            def role = agreement.getRole()
            
            // Get explicit permissions
            def explicitPerms = agreement.getEndpointPermissions()
            
            // Get role default permissions
            def rolePerms = RolePermissions.getDefaultPermissions(role)
            
            // Effective = explicit OR role (if explicit is empty, inherit from role)
            def effectivePerms = explicitPerms
            if (explicitPerms.signum() == 0) {
                effectivePerms = rolePerms
            } else {
                effectivePerms = explicitPerms.or(rolePerms)
            }
            
            def allEndpoints = EndpointRegistry.getAllEndpoints()
            def grantedEndpoints = []
            allEndpoints.each { name, bit ->
                if (effectivePerms.and(bit).signum() > 0) {
                    grantedEndpoints.add(name)
                }
            }
            
            outjson.put("_Success", true)
            outjson.put("actorOid", actorOid)
            outjson.put("actorName", actor.getName())
            outjson.put("role", role.name())
            outjson.put("explicitPermissions", explicitPerms.toString())
            outjson.put("rolePermissions", rolePerms.toString())
            outjson.put("effectivePermissions", effectivePerms.toString())
            outjson.put("endpointCount", grantedEndpoints.size())
            outjson.put("endpoints", grantedEndpoints)
            
        } catch (Exception e) {
            outjson.put("_Success", false)
            outjson.put("error", "Failed to get actor permissions: " + e.message)
        }
    }
    
    /**
     * POST /permissions/actor/{actorOid}/grant - Grant endpoint to specific actor (ADMIN only)
     */
    static JSONObject grantActorEndpoint(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        if (!isAdmin(servlet)) {
            outjson.put("_Success", false)
            outjson.put("error", "Only ADMIN can modify actor permissions")
            return
        }
        
        try {
            long actorOid = injson.optLong("actorOid", 0)
            def endpointName = injson.optString("endpointName", null)
            
            if (actorOid == 0 || !endpointName) {
                outjson.put("_Success", false)
                outjson.put("error", "Missing actorOid or endpointName parameter")
                return
            }
            
            def actor = ActorManager.getByOid(actorOid)
            if (!actor) {
                outjson.put("_Success", false)
                outjson.put("error", "Actor not found: " + actorOid)
                return
            }
            
            def agreement = actor.getAgreement()
            agreement.grantEndpoint(endpointName)
            
            // Save the actor
            def tc = StorageManager.createContainer()
            tc.addUpdate(actor)
            StorageManager.store(tc)
            
            outjson.put("_Success", true)
            outjson.put("message", "Granted ${endpointName} to actor ${actor.getName()}")
            
        } catch (Exception e) {
            outjson.put("_Success", false)
            outjson.put("error", "Failed to grant endpoint: " + e.message)
        }
    }
    
    /**
     * POST /permissions/actor/{actorOid}/revoke - Revoke endpoint from specific actor (ADMIN only)
     */
    static JSONObject revokeActorEndpoint(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        if (!isAdmin(servlet)) {
            outjson.put("_Success", false)
            outjson.put("error", "Only ADMIN can modify actor permissions")
            return
        }
        
        try {
            long actorOid = injson.optLong("actorOid", 0)
            def endpointName = injson.optString("endpointName", null)
            
            if (actorOid == 0 || !endpointName) {
                outjson.put("_Success", false)
                outjson.put("error", "Missing actorOid or endpointName parameter")
                return
            }
            
            def actor = ActorManager.getByOid(actorOid)
            if (!actor) {
                outjson.put("_Success", false)
                outjson.put("error", "Actor not found: " + actorOid)
                return
            }
            
            def agreement = actor.getAgreement()
            agreement.revokeEndpoint(endpointName)
            
            // Save the actor
            def tc = StorageManager.createContainer()
            tc.addUpdate(actor)
            StorageManager.store(tc)
            
            outjson.put("_Success", true)
            outjson.put("message", "Revoked ${endpointName} from actor ${actor.getName()}")
            
        } catch (Exception e) {
            outjson.put("_Success", false)
            outjson.put("error", "Failed to revoke endpoint: " + e.message)
        }
    }
    
    /**
     * GET /permissions/roles - List all role permissions summary
     */
    static JSONObject listRolePermissions(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            def result = []
            
            for (Role role : Role.values()) {
                def permissions = RolePermissions.getDefaultPermissions(role)
                def allEndpoints = EndpointRegistry.getAllEndpoints()
                
                def grantedCount = 0
                allEndpoints.each { name, bit ->
                    if (permissions.and(bit).signum() > 0) {
                        grantedCount++
                    }
                }
                
                result.add([
                    role: role.name(),
                    permissionCount: grantedCount,
                    permissions: permissions.toString()
                ])
            }
            
            outjson.put("_Success", true)
            outjson.put("roles", result)
            
        } catch (Exception e) {
            outjson.put("_Success", false)
            outjson.put("error", "Failed to list role permissions: " + e.message)
        }
    }
    
    /**
     * POST /permissions/register - Manually register an endpoint (fallback)
     */
    static JSONObject registerEndpoint(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        try {
            def endpointName = injson.optString("endpointName", null)
            
            if (!endpointName) {
                outjson.put("_Success", false)
                outjson.put("error", "Missing endpointName parameter")
                return
            }
            
            def bit = EndpointRegistry.registerEndpoint(endpointName)
            
            outjson.put("_Success", true)
            outjson.put("message", "Registered ${endpointName}")
            outjson.put("bit", bit.toString())
            
        } catch (Exception e) {
            outjson.put("_Success", false)
            outjson.put("error", "Failed to register endpoint: " + e.message)
        }
    }
}