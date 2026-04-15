package mycompany;

import org.garret.perst.continuous.CVersion;
import org.garret.perst.Indexable;
import org.garret.perst.continuous.FullTextSearchable;

/**
 * Phone - Represents a phone record.
 * 
 * Indexing:
 * - @Indexable: fields for b-tree indexing (use db.find())
 * - @FullTextSearchable: fields for Lucene full-text search (use db.fullTextSearch())
 * 
 * NOTE: This class extends Persistent (not CVersion) for non-versioned storage.
 * Change to extend CVersion if you need versioning.
 */
public class Phone extends CVersion {
    
    @Indexable
    private String uuid;
    
    @FullTextSearchable
    private String firstName;
    
    @FullTextSearchable
    private String lastName;
    
    @Indexable
    private String phoneNumber;
    
    private long createdDate;
    
    public Phone() {
        this.createdDate = System.currentTimeMillis();
    }
    
    public Phone(String firstName, String lastName, String phoneNumber) {
        this();
        this.uuid = java.util.UUID.randomUUID().toString();
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
    }
    
    public String getUuid() { return uuid; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public long getCreatedDate() { return createdDate; }
}
