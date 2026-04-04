package mycompany.database;

import mycompany.domain.Actor;
import mycompany.domain.PerstUser;
import org.garret.perst.continuous.TransactionContainer;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * PerstUserManager - Manager for PerstUser CRUD operations and authentication.
 * 
 * All operations use TransactionContainer for atomicity.
 */
public class PerstUserManager extends BaseManager<PerstUser> {
    
    private PerstUserManager() {
    }
    
    // ========== RETRIEVE ==========
    
    public static Collection<PerstUser> getAll(Actor actor) {
        return getAll();
    }
    
    public static PerstUser getByKey(Actor actor, String key) {
        return getByKey(key);
    }
    
    public static Collection<PerstUser> getAll() {
        return oodb.PerstStorageManager.getAll(PerstUser.class);
    }
    
    public static PerstUser getByKey(String key) {
        return oodb.PerstStorageManager.find(PerstUser.class, "username", key);
    }
    
    public static PerstUser getByOid(long oid) {
        return oodb.PerstStorageManager.getByOid(PerstUser.class, oid);
    }

    public static PerstUser getByActor(Actor actor) {
        // Iterate all PerstUsers to find the one linked to this actor
        for (PerstUser user : getAll()) {
            if (user.getActor() == actor) {
                return user;
            }
        }
        return null;
    }

    // ========== AUTHENTICATION ==========
    
    public static PerstUser authenticate(String username, String password) {
        PerstUser user = getByKey(username);
        if (user != null && user.checkPassword(password) && user.canLogin()) {
            user.setLastLoginDate(System.currentTimeMillis());
            TransactionContainer tc = oodb.PerstStorageManager.createContainer();
            tc.addUpdate(user);
            oodb.PerstStorageManager.store(tc);
            return user;
        }
        return null;
    }
    
    // ========== CRUD ==========
    
    public static PerstUser create(Actor actor, String username, String password) {
        PerstUser user = new PerstUser(username, password, actor);
        
        TransactionContainer tc = oodb.PerstStorageManager.createContainer();
        tc.addInsert(user);
        if (!oodb.PerstStorageManager.store(tc)) {
            return null;
        }
        return user;
    }
    
    public static boolean update(PerstUser user) {
        if (user == null) return false;
        
        TransactionContainer tc = oodb.PerstStorageManager.createContainer();
        tc.addUpdate(user);
        return oodb.PerstStorageManager.store(tc);
    }
    
    public static boolean delete(PerstUser user) {
        if (user == null) return false;
        
        TransactionContainer tc = oodb.PerstStorageManager.createContainer();
        tc.addDelete(user);
        return oodb.PerstStorageManager.store(tc);
    }
    
    public static boolean validate(PerstUser user) {
        return user != null && user.getUsername() != null && !user.getUsername().isEmpty();
    }
    
    // ========== PASSWORD OPERATIONS ==========
    
    public static boolean changePassword(String username, String oldPassword, String newPassword) {
        PerstUser user = authenticate(username, oldPassword);
        if (user != null) {
            user.setPassword(newPassword);
            TransactionContainer tc = oodb.PerstStorageManager.createContainer();
            tc.addUpdate(user);
            return oodb.PerstStorageManager.store(tc);
        }
        return false;
    }
    
    public static boolean resetPassword(String username, String newPassword) {
        PerstUser user = getByKey(username);
        if (user != null) {
            user.setPassword(newPassword);
            TransactionContainer tc = oodb.PerstStorageManager.createContainer();
            tc.addUpdate(user);
            return oodb.PerstStorageManager.store(tc);
        }
        return false;
    }
    
    // ========== STATUS OPERATIONS ==========
    
    public static boolean deactivate(String username) {
        PerstUser user = getByKey(username);
        if (user != null) {
            user.setActive(false);
            TransactionContainer tc = oodb.PerstStorageManager.createContainer();
            tc.addUpdate(user);
            return oodb.PerstStorageManager.store(tc);
        }
        return false;
    }
    
    public static boolean activate(String username) {
        PerstUser user = getByKey(username);
        if (user != null) {
            user.setActive(true);
            TransactionContainer tc = oodb.PerstStorageManager.createContainer();
            tc.addUpdate(user);
            return oodb.PerstStorageManager.store(tc);
        }
        return false;
    }
    
    public static boolean exists(String username) {
        return getByKey(username) != null;
    }
    
    public static List<PerstUser> getActiveUsers() {
        return getAll().stream()
                .filter(PerstUser::canLogin)
                .collect(Collectors.toList());
    }
}
