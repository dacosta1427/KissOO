package domain;

import org.garret.perst.Link;
import org.garret.perst.dbmanager.UnifiedDBManager;
import org.kissweb.restServer.MainServlet;

/**
 * DomainCollections - Thin helper that delegates Link creation to UnifiedDBManager.
 *
 * This is the ONLY place in the domain layer that knows about Perst Link.
 * All domain classes that need collection fields call DomainCollections.createLink().
 *
 * If the persistence engine ever changes, only this class needs modification.
 */
public class DomainCollections {

    private DomainCollections() {
        // Utility class - not instantiable
    }

    /**
     * Create a new Perst Link backed by the shared Storage instance.
     * Delegates to UnifiedDBManager which internally manages the Storage lifecycle.
     *
     * @param <T> Element type of the link
     * @return A new Link backed by the managed Storage
     */
    public static <T> Link<T> createLink() {
        UnifiedDBManager udbm = (UnifiedDBManager) MainServlet.getEnvironment("unifiedDBManager");
        if (udbm == null) {
            throw new IllegalStateException("UnifiedDBManager not initialized. Call UnifiedDBManager.create() first.");
        }
        return udbm.createLink();
    }
}