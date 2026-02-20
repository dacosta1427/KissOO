package gfe;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Actor domain class
 * Tests all fields, constructors, getters/setters, and toJSON()
 */
class ActorTest {

    @Test
    void testDefaultConstructor() {
        Actor actor = new Actor();
        
        assertNotNull(actor, "Actor should be created");
        assertTrue(actor.isActive(), "Actor should be active by default");
        assertTrue(actor.getCreatedDate() > 0, "Created date should be set");
    }

    @Test
    void testParameterizedConstructor() {
        Actor actor = new Actor("John Doe", "EMPLOYEE");
        
        assertEquals("John Doe", actor.getName(), "Name should be set");
        assertEquals("EMPLOYEE", actor.getType(), "Type should be set");
        assertNotNull(actor.getUuid(), "UUID should be generated");
        assertTrue(actor.isActive(), "Should be active by default");
    }

    @Test
    void testUuidGeneration() {
        Actor actor1 = new Actor("Test1", "RETAIL");
        Actor actor2 = new Actor("Test2", "RETAIL");
        
        assertNotNull(actor1.getUuid(), "UUID should not be null");
        assertNotNull(actor2.getUuid(), "UUID should not be null");
        assertNotEquals(actor1.getUuid(), actor2.getUuid(), "UUIDs should be unique");
    }

    @Test
    void testSetName() {
        Actor actor = new Actor();
        
        actor.setName("New Name");
        
        assertEquals("New Name", actor.getName(), "Name should be updated");
    }

    @Test
    void testSetType() {
        Actor actor = new Actor();
        
        actor.setType("CORPORATE");
        
        assertEquals("CORPORATE", actor.getType(), "Type should be updated");
    }

    @Test
    void testSetActiveTrue() {
        Actor actor = new Actor();
        actor.setActive(false);
        
        actor.setActive(true);
        
        assertTrue(actor.isActive(), "Active should be true");
    }

    @Test
    void testSetActiveFalse() {
        Actor actor = new Actor();
        
        actor.setActive(false);
        
        assertFalse(actor.isActive(), "Active should be false");
    }

    @Test
    void testActorTypes() {
        Actor employee = new Actor("Emp", "EMPLOYEE");
        Actor retail = new Actor("Retail", "RETAIL");
        Actor corporate = new Actor("Corp", "CORPORATE");
        
        assertEquals("EMPLOYEE", employee.getType());
        assertEquals("RETAIL", retail.getType());
        assertEquals("CORPORATE", corporate.getType());
    }

    @Test
    void testToJSON() {
        Actor actor = new Actor("Test Actor", "EMPLOYEE");
        Map<String, Object> json = actor.toJSON();
        
        assertNotNull(json, "JSON should not be null");
        assertTrue(json.containsKey("uuid"), "JSON should contain uuid");
        assertTrue(json.containsKey("name"), "JSON should contain name");
        assertTrue(json.containsKey("type"), "JSON should contain type");
        assertTrue(json.containsKey("active"), "JSON should contain active");
        assertTrue(json.containsKey("createdDate"), "JSON should contain createdDate");
        
        assertEquals("Test Actor", json.get("name"));
        assertEquals("EMPLOYEE", json.get("type"));
    }

    @Test
    void testToJSONValues() {
        Actor actor = new Actor("JSON Test", "CORPORATE");
        Map<String, Object> json = actor.toJSON();
        
        assertEquals(actor.getUuid(), json.get("uuid"));
        assertEquals(actor.getName(), json.get("name"));
        assertEquals(actor.getType(), json.get("type"));
        assertEquals(actor.isActive(), json.get("active"));
        assertEquals(actor.getCreatedDate(), json.get("createdDate"));
    }

    @Test
    void testToString() {
        Actor actor = Actor.builder()
                .name("ToString Test")
                .type("RETAIL")
                .active(true)
                .build();
        String str = actor.toString();
        
        assertTrue(str.contains("Actor"), "toString should contain Actor");
        assertTrue(str.contains("ToString Test"), "toString should contain name");
        assertTrue(str.contains("RETAIL"), "toString should contain type");
    }

    @Test
    void testBuilderWithAllFields() {
        long now = System.currentTimeMillis();
        Actor actor = Actor.builder()
                .uuid("test-uuid-123")
                .name("Builder Test")
                .type("CORPORATE")
                .active(false)
                .createdDate(now)
                .build();
        
        assertEquals("test-uuid-123", actor.getUuid());
        assertEquals("Builder Test", actor.getName());
        assertEquals("CORPORATE", actor.getType());
        assertFalse(actor.isActive());
        assertEquals(now, actor.getCreatedDate());
    }

    @Test
    void testMultipleActorCreation() {
        Actor actor1 = new Actor("First", "EMPLOYEE");
        Actor actor2 = new Actor("Second", "RETAIL");
        Actor actor3 = new Actor("Third", "CORPORATE");
        
        assertNotSame(actor1, actor2, "Actors should be different instances");
        assertNotSame(actor2, actor3, "Actors should be different instances");
        assertNotSame(actor1, actor3, "Actors should be different instances");
    }

    @Test
    void testActorWithEmptyName() {
        Actor actor = new Actor("", "RETAIL");
        
        assertEquals("", actor.getName(), "Empty name should be allowed");
    }

    @Test
    void testActorWithNullType() {
        Actor actor = new Actor("Test", null);
        
        assertNull(actor.getType(), "Null type should be allowed");
    }

    @Test
    void testCreatedDateIsTimestamp() {
        long before = System.currentTimeMillis();
        Actor actor = new Actor("TimeTest", "EMPLOYEE");
        long after = System.currentTimeMillis();
        
        assertTrue(actor.getCreatedDate() >= before, "Created date should be >= before");
        assertTrue(actor.getCreatedDate() <= after, "Created date should be <= after");
    }

    @Test
    void testJsonMapIsMutable() {
        Actor actor = new Actor("Mutable", "RETAIL");
        Map<String, Object> json = actor.toJSON();
        
        // Verify we get a new map each time
        Map<String, Object> json2 = actor.toJSON();
        assertNotSame(json, json2, "Should return new map each call");
    }
}
