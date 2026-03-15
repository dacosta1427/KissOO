package mycompany.domain;

import org.garret.perst.Persistent;

/**
 * Phone - Represents a phone record.
 */
public class Phone extends Persistent {
    
    private String uuid;
    private String firstName;
    private String lastName;
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
