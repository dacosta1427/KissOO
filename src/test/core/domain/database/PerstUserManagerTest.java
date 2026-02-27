package domain.database;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Collection;
import java.util.List;

/**
 * Unit tests for PerstUserManager.
 * Tests validation, authentication, and manager behavior.
 */
public class PerstUserManagerTest {

    @Test
    public void testGetInstanceReturnsSingleton() {
        PerstUserManager mgr1 = PerstUserManager.getInstance();
        PerstUserManager mgr2 = PerstUserManager.getInstance();
        assertSame(mgr1, mgr2);
    }

    @Test
    public void testGetAllReturnsEmptyWhenPerstUnavailable() {
        PerstUserManager manager = PerstUserManager.getInstance();
        Collection<PerstUser> result = manager.getAll();
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetByKeyReturnsNullWhenPerstUnavailable() {
        PerstUserManager manager = PerstUserManager.getInstance();
        PerstUser result = manager.getByKey("test");
        assertNull(result);
    }

    @Test
    public void testAuthenticateReturnsNullWhenPerstUnavailable() {
        PerstUserManager manager = PerstUserManager.getInstance();
        PerstUser result = manager.authenticate("user", "pass");
        assertNull(result);
    }

    @Test
    public void testAuthenticateReturnsNullForNullUsername() {
        PerstUserManager manager = PerstUserManager.getInstance();
        PerstUser result = manager.authenticate(null, "pass");
        assertNull(result);
    }

    @Test
    public void testAuthenticateReturnsNullForNullPassword() {
        PerstUserManager manager = PerstUserManager.getInstance();
        PerstUser result = manager.authenticate("user", null);
        assertNull(result);
    }

    @Test
    public void testCreateReturnsNullWhenPerstUnavailable() {
        PerstUserManager manager = PerstUserManager.getInstance();
        PerstUser result = manager.create("user", "pass");
        assertNull(result);
    }

    @Test
    public void testCreateThrowsExceptionForMissingParams() {
        PerstUserManager manager = PerstUserManager.getInstance();
        assertThrows(IllegalArgumentException.class, () -> {
            manager.create("only-username");
        });
    }

    @Test
    public void testUpdateReturnsFalseWhenPerstUnavailable() {
        PerstUserManager manager = PerstUserManager.getInstance();
        PerstUser user = new PerstUser("test", "pass", 1);
        boolean result = manager.update(user);
        assertFalse(result);
    }

    @Test
    public void testUpdateReturnsFalseForNull() {
        PerstUserManager manager = PerstUserManager.getInstance();
        boolean result = manager.update(null);
        assertFalse(result);
    }

    @Test
    public void testDeleteReturnsFalseWhenPerstUnavailable() {
        PerstUserManager manager = PerstUserManager.getInstance();
        PerstUser user = new PerstUser("test", "pass", 1);
        boolean result = manager.delete(user);
        assertFalse(result);
    }

    @Test
    public void testDeleteReturnsFalseForNull() {
        PerstUserManager manager = PerstUserManager.getInstance();
        boolean result = manager.delete(null);
        assertFalse(result);
    }

    @Test
    public void testExistsReturnsFalseWhenPerstUnavailable() {
        PerstUserManager manager = PerstUserManager.getInstance();
        boolean result = manager.exists("test");
        assertFalse(result);
    }

    @Test
    public void testChangePasswordReturnsFalseWhenPerstUnavailable() {
        PerstUserManager manager = PerstUserManager.getInstance();
        boolean result = manager.changePassword("user", "old", "new");
        assertFalse(result);
    }

    @Test
    public void testResetPasswordReturnsFalseWhenPerstUnavailable() {
        PerstUserManager manager = PerstUserManager.getInstance();
        boolean result = manager.resetPassword("user", "new");
        assertFalse(result);
    }

    @Test
    public void testDeactivateReturnsFalseWhenPerstUnavailable() {
        PerstUserManager manager = PerstUserManager.getInstance();
        boolean result = manager.deactivate("user");
        assertFalse(result);
    }

    @Test
    public void testActivateReturnsFalseWhenPerstUnavailable() {
        PerstUserManager manager = PerstUserManager.getInstance();
        boolean result = manager.activate("user");
        assertFalse(result);
    }

    @Test
    public void testValidateRejectsNullUser() {
        PerstUserManager manager = PerstUserManager.getInstance();
        assertFalse(manager.validate(null));
    }

    @Test
    public void testValidateRejectsNullUsername() {
        PerstUserManager manager = PerstUserManager.getInstance();
        PerstUser user = new PerstUser(null, "pass", 1);
        assertFalse(manager.validate(user));
    }

    @Test
    public void testValidateRejectsEmptyUsername() {
        PerstUserManager manager = PerstUserManager.getInstance();
        PerstUser user = new PerstUser("", "pass", 1);
        assertFalse(manager.validate(user));
    }

    @Test
    public void testValidateRejectsNullPassword() {
        PerstUserManager manager = PerstUserManager.getInstance();
        PerstUser user = new PerstUser("user", null, 1);
        assertFalse(manager.validate(user));
    }

    @Test
    public void testValidateRejectsEmptyPassword() {
        PerstUserManager manager = PerstUserManager.getInstance();
        PerstUser user = new PerstUser("user", "", 1);
        assertFalse(manager.validate(user));
    }

    @Test
    public void testValidateAcceptsValidUser() {
        PerstUserManager manager = PerstUserManager.getInstance();
        PerstUser user = new PerstUser("valid", "pass", 1);
        assertTrue(manager.validate(user));
    }

    @Test
    public void testGetActiveUsersReturnsEmptyWhenPerstUnavailable() {
        PerstUserManager manager = PerstUserManager.getInstance();
        List<PerstUser> result = manager.getActiveUsers();
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
