package mycompany.database;

import mycompany.domain.Actor;
import mycompany.domain.PerstUser;
import java.util.Collection;
import java.util.List;

/**
 * PerstUserManager - Manager for PerstUser CRUD operations and authentication.
 */
public class PerstUserManager extends BaseManager<PerstUser> {
    
    private PerstUserManager() {
    }
    
    public static Collection<PerstUser> getAll(Actor actor) {
        return getAll();
    }
    
    public static PerstUser getByKey(Actor actor, String key) {
        return getByKey(key);
    }
    
    public static PerstUser create(Actor actor, Object... args) {
        return create(args);
    }
    
    public static boolean update(Actor actor, PerstUser user) {
        return update(user);
    }
    
    public static boolean delete(Actor actor, PerstUser user) {
        return delete(user);
    }
    
    public static Collection<PerstUser> getAll() {
        return PerstUser.getAll();
    }
    
    public static PerstUser getByKey(String key) {
        try {
            int id = Integer.parseInt(key);
            return PerstUser.get(id);
        } catch (NumberFormatException e) {
            return PerstUser.getByUsername(key);
        }
    }
    
    public static PerstUser getByOid(long oid) {
        return PerstUser.getByOid(oid);
    }
    
    public static PerstUser authenticate(String username, String password) {
        PerstUser user = PerstUser.getByUsername(username);
        if (user != null && user.checkPassword(password) && user.canLogin()) {
            user.setLastLoginDate(System.currentTimeMillis());
            return user;
        }
        return null;
    }
    
    public static PerstUser create(Object... args) {
        if (args == null || args.length < 2) {
            return null;
        }
        String username = args[0].toString();
        String password = args[1].toString();
        int userId = args.length > 2 ? Integer.parseInt(args[2].toString()) : 0;
        
        PerstUser user = new PerstUser(username, password, userId);
        user.index();
        oodb.PerstStorageManager.getDatabase().insert(user);
        return user;
    }
    
    public static boolean update(PerstUser user) {
        if (user == null) return false;
        user.index();
        return true;
    }
    
    public static boolean delete(PerstUser user) {
        if (user == null) return false;
        user.removeIndex();
        return true;
    }
    
    public static boolean validate(PerstUser user) {
        return user != null && user.getUsername() != null && !user.getUsername().isEmpty();
    }
    
    public static boolean changePassword(String username, String oldPassword, String newPassword) {
        PerstUser user = authenticate(username, oldPassword);
        if (user != null) {
            user.setPassword(newPassword);
            return true;
        }
        return false;
    }
    
    public static boolean resetPassword(String username, String newPassword) {
        PerstUser user = PerstUser.getByUsername(username);
        if (user != null) {
            user.setPassword(newPassword);
            return true;
        }
        return false;
    }
    
    public static boolean deactivate(String username) {
        PerstUser user = PerstUser.getByUsername(username);
        if (user != null) {
            user.setActive(false);
            return true;
        }
        return false;
    }
    
    public static boolean activate(String username) {
        PerstUser user = PerstUser.getByUsername(username);
        if (user != null) {
            user.setActive(true);
            return true;
        }
        return false;
    }
    
    public static boolean exists(String username) {
        return PerstUser.getByUsername(username) != null;
    }
    
    public static List<PerstUser> getActiveUsers() {
        return PerstUser.getActiveUsers();
    }
}