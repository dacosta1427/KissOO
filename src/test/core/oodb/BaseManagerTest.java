package core.oodb;

import koo.core.BaseManager;
import koo.core.actor.ActorManager;
import org.junit.jupiter.api.Test;

import static org.armedbear.lisp.Debug.assertTrue;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for BaseManager.
 * Tests common business logic methods.
 */
public class BaseManagerTest {

    @Test
    public void testActorManagerExtendsBaseManager() {
        // Verify ActorManager is a subclass of BaseManager
        assertTrue(BaseManager.class.isAssignableFrom(ActorManager.class));
    }
}
