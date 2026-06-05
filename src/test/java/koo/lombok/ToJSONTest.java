package koo.lombok;

import koo.core.actor.AActor;
import koo.core.actor.Agreement;
import koo.core.actor.Role;
import koo.core.actor.user.PerstUser;
import org.junit.jupiter.api.Test;
import org.kissweb.json.JSONObject;
import static org.junit.jupiter.api.Assertions.*;

public class ToJSONTest {
    
    @Test
    public void testAActorToJSON() {
        // Create test data
        Agreement agreement = new Agreement(Role.ADMIN);
        AActor actor = new AActor("Test Actor", agreement);
        
        // Test toJSON generation
        JSONObject json = actor.toJSON();
        
        // Verify basic fields
        assertNotNull(json);
        assertEquals(actor.getUuid(), json.getString("uuid"));
        assertEquals("Test Actor", json.getString("name"));
        assertEquals(actor.getActorType().name(), json.getString("actorType"));
        assertTrue(json.getBoolean("active"));
        
        // Verify agreement reference (OID)
        assertTrue(json.has("agreementOid"));
        assertNotNull(json.getLong("agreementOid"));
        
        // Verify excluded fields are not present
        assertFalse(json.has("internalState"));
    }
    
    @Test
    public void testPerstUserToJSON() {
        // Create test data
        Agreement agreement = new Agreement(Role.MEMBER);
        AActor actor = new AActor("Test User", agreement);
        PerstUser user = new PerstUser("test@example.com", "password", actor);
        
        // Test toJSON generation
        JSONObject json = user.toJSON();
        
        // Verify basic fields
        assertNotNull(json);
        assertEquals("test@example.com", json.getString("username"));
        assertEquals("Test User", json.getString("name"));
        assertFalse(json.getBoolean("active")); // Default is false
        
        // Verify excluded fields are not present
        assertFalse(json.has("passwordHash"));
        assertFalse(json.has("verificationToken"));
        
        // Verify actor reference (OID)
        assertTrue(json.has("actorOid"));
        assertNotNull(json.getLong("actorOid"));
    }
    
    @Test
    public void testJSONUtils() {
        // Test collection utilities
        Agreement agreement = new Agreement(Role.MEMBER);
        AActor actor1 = new AActor("User 1", agreement);
        AActor actor2 = new AActor("User 2", agreement);
        
        java.util.List<AActor> actors = java.util.Arrays.asList(actor1, actor2);
        
        // Test toJSONArray
        JSONArray jsonArray = JSONUtils.toJSONArray(actors);
        assertEquals(2, jsonArray.length());
        
        // Test toCollectionResponse
        JSONObject response = JSONUtils.toCollectionResponse(actors);
        assertTrue(response.getBoolean("_Success"));
        assertEquals(2, response.getInt("count"));
        assertNotNull(response.getJSONArray("data"));
    }
}