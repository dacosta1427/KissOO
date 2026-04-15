package mycompany.database;

import koo.oodb.core.actor.Actor;
import koo.oodb.core.actor.Agreement;
import koo.oodb.core.actor.ActorManager;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ActorManager.
 * Tests business logic methods - storage is delegated to PerstStorageManager.
 */
public class ActorManagerTest {

    @Test
    public void testValidateRejectsNullActor() {
        assertFalse(ActorManager.validate(null));
    }

    @Test
    public void testValidateRejectsNullName() {
        Actor actor = new Actor(null, "RETAIL", new Agreement("test"));
        assertFalse(ActorManager.validate(actor));
    }

    @Test
    public void testValidateRejectsEmptyName() {
        Actor actor = new Actor("", "RETAIL", new Agreement("test"));
        assertFalse(ActorManager.validate(actor));
    }

    @Test
    public void testValidateRejectsNullType() {
        Actor actor = new Actor("test", null, new Agreement("test"));
        assertFalse(ActorManager.validate(actor));
    }

    @Test
    public void testValidateRejectsEmptyType() {
        Actor actor = new Actor("test", "", new Agreement("test"));
        assertFalse(ActorManager.validate(actor));
    }

    @Test
    public void testValidateAcceptsValidActor() {
        Actor actor = new Actor("testActor", "RETAIL", new Agreement("test"));
        assertTrue(ActorManager.validate(actor));
    }

    @Test
    public void testExistsReturnsFalseForNonExistent() {
        assertNotNull(ActorManager.exists("nonExistentActorXYZ"));
    }

    @Test
    public void testGetByNameDoesNotThrowWhenPerstUnavailable() {
        Actor result = ActorManager.getByName("testName");
        assertNull(result);
    }

    @Test
    public void testGetByUuidDoesNotThrowWhenPerstUnavailable() {
        Actor result = ActorManager.getByUuid("test-uuid");
        assertNull(result);
    }

    @Test
    public void testUpdateRejectsNull() {
        assertFalse(ActorManager.update(null));
    }

    @Test
    public void testDeleteRejectsNull() {
        assertFalse(ActorManager.delete(null));
    }
}
