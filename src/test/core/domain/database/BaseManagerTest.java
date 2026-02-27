package domain.database;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Collection;

/**
 * Unit tests for BaseManager.
 * Tests the abstract base class behavior.
 */
public class BaseManagerTest {

    /**
     * Concrete implementation for testing BaseManager.
     */
    private static class TestManager extends BaseManager<String> {
        @Override
        public Collection<String> getAll() {
            return java.util.Collections.emptyList();
        }

        @Override
        public String getByKey(String key) {
            return null;
        }

        @Override
        public String create(Object... params) {
            return null;
        }

        @Override
        public boolean update(String entity) {
            return false;
        }

        @Override
        public boolean delete(String entity) {
            return false;
        }

        @Override
        protected boolean validate(String entity) {
            return entity != null && !entity.isEmpty();
        }
    }

    @Test
    public void testIsPerstAvailableDelegatesToPerstHelper() {
        TestManager manager = new TestManager();
        // The method should delegate to PerstHelper.isAvailable()
        // Without Perst initialized, this returns false
        assertFalse(manager.isPerstAvailable());
    }
}
