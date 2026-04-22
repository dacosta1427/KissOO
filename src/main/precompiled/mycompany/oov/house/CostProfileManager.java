package mycompany.oov.house;

import koo.oodb.BaseManager;
import mycompany.actor.owner.Owner;
import koo.oodb.core.database.StorageManager;
import org.garret.perst.continuous.TransactionContainer;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * CostProfileManager - Manager for CostProfile CRUD operations.
 * 
 * Follows the Manager at the Gate pattern.
 */
public class CostProfileManager extends BaseManager<CostProfile> {
    
    private CostProfileManager() {
    }
    
    // ========== RETRIEVE ==========
    
    /**
     * Get all active cost profiles
     */
    public static Collection<CostProfile> getAll() {
        return StorageManager.getAll(CostProfile.class)
                .stream()
                .filter(CostProfile::isActive)
                .collect(Collectors.toList());
    }
    
    /**
     * Get cost profile by OID
     */
    public static CostProfile getByOid(long oid) {
        return StorageManager.getByOid(CostProfile.class, oid);
    }
    
    /**
     * Get the standard (default) cost profile
     */
    public static CostProfile getStandard() {
        return StorageManager.getAll(CostProfile.class)
                .stream()
                .filter(cp -> cp.isStandard() && cp.isActive())
                .findFirst()
                .orElseGet(() -> {
                    // Create default standard profile if none exists
                    return createDefaultStandard();
                });
    }
    
    /**
     * Get cost profiles for a specific owner (includes global standard)
     */
    public static Collection<CostProfile> getByOwner(Owner owner) {
        return StorageManager.getAll(CostProfile.class)
                .stream()
                .filter(cp -> cp.isActive() && (cp.getOwner() == null || cp.getOwner().getOid() == owner.getOid()))
                .collect(Collectors.toList());
    }
    
    // ========== CRUD ==========
    
    /**
     * Create a new cost profile
     */
    public static CostProfile create(String name, boolean isStandard, Owner owner) {
        CostProfile profile;
        if (owner != null) {
            profile = new CostProfile(name, owner);
        } else {
            profile = new CostProfile(name, isStandard);
        }
        
        TransactionContainer tc = StorageManager.createContainer();
        tc.addInsert(profile);
        if (!StorageManager.store(tc)) {
            return null;
        }
        return profile;
    }
    
    /**
     * Create a new cost profile from object
     */
    public static CostProfile create(CostProfile profile) {
        if (profile == null) return null;
        
        TransactionContainer tc = StorageManager.createContainer();
        tc.addInsert(profile);
        if (!StorageManager.store(tc)) {
            return null;
        }
        return profile;
    }
    
    /**
     * Update an existing cost profile
     */
    public static boolean update(CostProfile profile) {
        if (profile == null) return false;
        
        TransactionContainer tc = StorageManager.createContainer();
        tc.addUpdate(profile);
        return StorageManager.store(tc);
    }
    
    /**
     * Delete a cost profile
     */
    public static boolean delete(CostProfile profile) {
        if (profile == null) return false;
        
        // Don't delete if it's the standard profile
        if (profile.isStandard()) {
            // Just deactivate instead
            profile.setActive(false);
            return update(profile);
        }
        
        TransactionContainer tc = StorageManager.createContainer();
        tc.addDelete(profile);
        return StorageManager.store(tc);
    }
    
    /**
     * Soft delete (deactivate) a cost profile
     */
    public static boolean deactivate(CostProfile profile) {
        if (profile == null) return false;
        
        profile.setActive(false);
        return update(profile);
    }
    
    // ========== HELPERS ==========
    
    /**
     * Create the default standard cost profile
     */
    private static CostProfile createDefaultStandard() {
        CostProfile profile = new CostProfile("Standard", true);
        profile.setBaseHourlyRate(25.0);
        profile.setMinimumCharge(75.0);
        profile.setRatePerM2(0.15);
        profile.setRatePerFloor(15.0);
        profile.setRatePerBedroom(10.0);
        profile.setRatePerBathroom(15.0);
        profile.setDogSurcharge(20.0);
        profile.setBasicMultiplier(1.0);
        profile.setStandardMultiplier(1.0);
        profile.setPremiumMultiplier(1.25);
        profile.setLuxuryMultiplier(1.5);
        profile.setActive(true);
        
        return create(profile);
    }
    
    /**
     * Copy a cost profile (for creating custom profiles from standard)
     */
    public static CostProfile copyFrom(CostProfile source, String newName, Owner owner) {
        if (source == null) return null;
        
        CostProfile copy = new CostProfile();
        copy.setName(newName);
        copy.setStandard(false);
        copy.setOwner(owner);
        copy.setBaseHourlyRate(source.getBaseHourlyRate());
        copy.setMinimumCharge(source.getMinimumCharge());
        copy.setRatePerM2(source.getRatePerM2());
        copy.setRatePerFloor(source.getRatePerFloor());
        copy.setRatePerBedroom(source.getRatePerBedroom());
        copy.setRatePerBathroom(source.getRatePerBathroom());
        copy.setDogSurcharge(source.getDogSurcharge());
        copy.setBasicMultiplier(source.getBasicMultiplier());
        copy.setStandardMultiplier(source.getStandardMultiplier());
        copy.setPremiumMultiplier(source.getPremiumMultiplier());
        copy.setLuxuryMultiplier(source.getLuxuryMultiplier());
        copy.setActive(true);
        
        return create(copy);
    }
    
    /**
     * Validate a cost profile
     */
    public static boolean validate(CostProfile profile) {
        return profile != null 
            && profile.getName() != null 
            && !profile.getName().isEmpty()
            && profile.getBaseHourlyRate() >= 0
            && profile.getMinimumCharge() >= 0;
    }
}
