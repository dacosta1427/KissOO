package mycompany.database;

import mycompany.domain.PerstUser;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PerstUserManager.
 * Tests business logic methods - storage is delegated to PerstStorageManager.
 */
public class PerstUserManagerTest {

    @Test
    public void testAuthenticateReturnsNullForInvalidCredentials() {
        PerstUser result = PerstUserManager.authenticate("nonexistent", "wrongpassword");
        assertNull(result);
    }

    @Test
    public void testGetByKeyReturnsNullWhenPerstUnavailable() {
        PerstUser result = PerstUserManager.getByKey("nonexistent");
        assertNull(result);
    }

    @Test
    public void testValidateRejectsNull() {
        assertFalse(PerstUserManager.validate(null));
    }

    @Test
    public void testValidateRejectsNullUsername() {
        PerstUser user = new PerstUser(null, "password", null);
        assertFalse(PerstUserManager.validate(user));
    }

    @Test
    public void testValidateAcceptsValidUser() {
        PerstUser user = new PerstUser("testuser", "password", null);
        assertTrue(PerstUserManager.validate(user));
    }

    @Test
    public void testExistsReturnsFalseForNonExistent() {
        boolean result = PerstUserManager.exists("nonexistent");
        assertFalse(result);
    }
}
