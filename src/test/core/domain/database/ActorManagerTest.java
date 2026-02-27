package domain.database;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Collection;
import java.util.ArrayList;

/**
 * Unit tests for ActorManager.
 * Tests validation and manager behavior.
 */
public class ActorManagerTest {

    @Test
    public void testGetInstanceReturnsSingleton() {
        ActorManager mgr1 = ActorManager.getInstance();
        ActorManager mgr2 = ActorManager.getInstance();
        assertSame(mgr1, mgr2);
    }

    @Test
    public void testGetAllReturnsEmptyWhenPerstUnavailable() {
        ActorManager manager = ActorManager.getInstance();
        Collection<Actor> result = manager.getAll();
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetByKeyReturnsNullWhenPerstUnavailable() {
        ActorManager manager = ActorManager.getInstance();
        Actor result = manager.getByKey("test");
        assertNull(result);
    }

    @Test
    public void testGetByUuidReturnsNullWhenPerstUnavailable() {
        ActorManager manager = ActorManager.getInstance();
        Actor result = manager.getByUuid("test-uuid");
        assertNull(result);
    }

    @Test
    public void testCreateReturnsNullWhenPerstUnavailable() {
        ActorManager manager = ActorManager.getInstance();
        Actor result = manager.create("test", "RETAIL");
        assertNull(result);
    }

    @Test
    public void testCreateThrowsExceptionForMissingParams() {
        ActorManager manager = ActorManager.getInstance();
        assertThrows(IllegalArgumentException.class, () -> {
            manager.create("only-name");
        });
    }

    @Test
    public void testUpdateReturnsFalseWhenPerstUnavailable() {
        ActorManager manager = ActorManager.getInstance();
        Actor actor = new Actor("test", "RETAIL");
        boolean result = manager.update(actor);
        assertFalse(result);
    }

    @Test
    public void testUpdateReturnsFalseForNull() {
        ActorManager manager = ActorManager.getInstance();
        boolean result = manager.update(null);
        assertFalse(result);
    }

    @Test
    public void testDeleteReturnsFalseWhenPerstUnavailable() {
        ActorManager manager = ActorManager.getInstance();
        Actor actor = new Actor("test", "RETAIL");
        boolean result = manager.delete(actor);
        assertFalse(result);
    }

    @Test
    public void testDeleteReturnsFalseForNull() {
        ActorManager manager = ActorManager.getInstance();
        boolean result = manager.delete(null);
        assertFalse(result);
    }

    @Test
    public void testExistsReturnsFalseWhenPerstUnavailable() {
        ActorManager manager = ActorManager.getInstance();
        boolean result = manager.exists("test");
        assertFalse(result);
    }

    @Test
    public void testValidateRejectsNullActor() {
        ActorManager manager = ActorManager.getInstance();
        assertFalse(manager.validate(null));
    }

    @Test
    public void testValidateRejectsNullName() {
        ActorManager manager = ActorManager.getInstance();
        Actor actor = new Actor(null, "RETAIL");
        assertFalse(manager.validate(actor));
    }

    @Test
    public void testValidateRejectsEmptyName() {
        ActorManager manager = ActorManager.getInstance();
        Actor actor = new Actor("", "RETAIL");
        assertFalse(manager.validate(actor));
    }

    @Test
    public void testValidateAcceptsValidActor() {
        ActorManager manager = ActorManager.getInstance();
        Actor actor = new Actor("valid", "RETAIL");
        assertTrue(manager.validate(actor));
    }

    @Test
    public void testGetByTypeReturnsEmptyWhenPerstUnavailable() {
        ActorManager manager = ActorManager.getInstance();
        java.util.List<Actor> result = manager.getByType("RETAIL");
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
