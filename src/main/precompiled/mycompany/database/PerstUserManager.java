package mycompany.database;

import mycompany.domain.Actor;
import mycompany.domain.PerstUser;
import mycompany.domain.Owner;
import mycompany.database.OwnerManager;
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
        try {
            int id = Integer.parseInt(key);
            return oodb.PerstStorageManager.find(PerstUser.class, "userId", id);
        } catch (NumberFormatException e) {
            return oodb.PerstStorageManager.find(PerstUser.class, "username", key);
        }
    }
    
    public static PerstUser getByOid(long oid) {
        return oodb.PerstStorageManager.getByOid(PerstUser.class, oid);
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
    
    public static PerstUser create(Actor actor, Object... args) {
        return create(args);
    }
    
    public static boolean update(Actor actor, PerstUser user) {
        return update(user);
    }
    
    public static boolean delete(Actor actor, PerstUser user) {
        return delete(user);
    }
    
    public static PerstUser create(Object... args) {
        if (args == null || args.length < 2) {
            return null;
        }
        String username = args[0].toString();
        String password = args[1].toString();
        int userId = args.length > 2 ? Integer.parseInt(args[2].toString()) : 0;
        Owner owner = null;
        if (args.length > 3 && args[3] instanceof Owner) {
            owner = (Owner) args[3];
        } else if (args.length > 3) {
            // Legacy: convert long ownerId to Owner object via lookup
            long ownerId = Long.parseLong(args[3].toString());
            owner = OwnerManager.getByOid(ownerId);
        }
        
        PerstUser user = new PerstUser(username, password, userId);
        user.setOwner(owner);
        
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
