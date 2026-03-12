package org.garret.perst.dbmanager;

import org.garret.perst.*;
import org.garret.perst.continuous.*;

/**
 * Example domain classes demonstrating how to make classes persistent with Perst.
 * 
 * STORAGE OPTIONS (Performance Matters!):
 * 
 * 1. IValue (INLINE) - Best for primitives, Strings, Numbers, Dates
 *    - Stored INSIDE parent object, no OID overhead
 *    - Use directly in fields or collections
 * 
 * 2. Persistent (BY REFERENCE) - For complex objects needing their own identity
 *    - Each object gets OID, loaded on demand
 *    - Use for objects you'll query/lookup individually
 * 
 * 3. CVersion - For version-controlled objects (multi-user optimistic locking)
 *    - Extends Persistent with version history
 *    - Best for shared/multi-user objects
 */

public class DomainClasses {

    // ======== Option 1a: Simple Persistent Class (by reference) ========
    // Use when you need to query objects individually
    public static class Person extends Persistent {
        // IValue types - stored inline, NO OID overhead!
        private String name;
        private int age;
        private String email;
        private double salary;
        private java.util.Date createdAt;
        
        // Required: no-arg constructor for Perst
        public Person() {}
        
        public Person(String name, int age, String email) {
            this.name = name;
            this.age = age;
            this.email = email;
            this.createdAt = new java.util.Date();
        }
        
        // Getters and setters - IValue fields stored inline automatically
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public int getAge() { return age; }
        public void setAge(int age) { this.age = age; }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public double getSalary() { return salary; }
        public void setSalary(double salary) { this.salary = salary; }
        
        public java.util.Date getCreatedAt() { return createdAt; }
        
        @Override
        public String toString() {
            return "Person{name='" + name + "', age=" + age + "}";
        }
    }
    
    // ======== Option 1b: IValue for Custom Inline Types ========
    // For custom types that should be stored inline (no OID overhead)
    // Use when you have small, simple objects always embedded in parent
    public static class Address implements IValue {
        private String street;
        private String city;
        private String zipCode;
        
        public Address() {}
        
        public Address(String street, String city, String zipCode) {
            this.street = street;
            this.city = city;
            this.zipCode = zipCode;
        }
        
        // IMPORTANT: Must implement clone() for Perst
        @Override
        public IValue clone() {
            return new Address(street, city, zipCode);
        }
        
        public String getStreet() { return street; }
        public String getCity() { return city; }
        public String getZipCode() { return zipCode; }
    }
    
    // ======== Option 2: With Collections ========
    // Persistent class with collections
    public static class Customer extends Persistent {
        // IValue fields - stored inline, NO OID overhead
        private String name;
        
        // Collections of IValue - stored inline, NO OID overhead per element!
        // Perst stores IValue elements directly in the collection without OIDs
        private IPersistentList<String> tags;  // Strings are IValue - inline!
        private IPersistentList<Address> addresses;  // Address is IValue - inline!
        
        // Collections of Persistent - BY REFERENCE, each element gets OID
        // Use only when you need to query individual elements
        private IPersistentList<Order> orders;  // Order is Persistent - OID per order
        
        public Customer() {}
        
        public Customer(Storage storage, String name) {
            this.name = name;
            this.tags = storage.createList();
            this.addresses = storage.createList();
            this.orders = storage.createList();
        }
        
        // IValue fields - inline storage
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        // IValue collections - inline storage, no OID overhead
        public IPersistentList<String> getTags() { return tags; }
        public void addTag(String tag) { tags.add(tag); }
        
        public IPersistentList<Address> getAddresses() { return addresses; }
        public void addAddress(Address addr) { addresses.add(addr); }
        
        // Persistent collections - by reference, OID per element
        public IPersistentList<Order> getOrders() { return orders; }
        public void addOrder(Order order) { orders.add(order); }
        
        // Order class - Persistent because we likely query individual orders
        public static class Order extends Persistent {
            private String orderNumber;
            private double amount;
            private java.util.Date orderDate;
            
            public Order() {}
            
            public Order(String orderNumber, double amount) {
                this.orderNumber = orderNumber;
                this.amount = amount;
                this.orderDate = new java.util.Date();
            }
            
            public String getOrderNumber() { return orderNumber; }
            public double getAmount() { return amount; }
        }
    }
    
    // ======== Option 3: Version-Controlled Objects (CVersion) ========
    // For optimistic locking - best for multi-user scenarios
    // IMPORTANT: CVersion requires using CDatabase, not plain Storage
    public static class Document extends CVersion {
        // All IValue fields - stored inline
        private String title;
        private String content;
        private String author;
        
        // Required: no-arg constructor
        public Document() {
            super();
        }
        
        // Constructor for root version (first version)
        public Document(Storage storage, String title, String content, String author) {
            super(storage);
            this.title = title;
            this.content = content;
            this.author = author;
        }
        
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        
        public String getAuthor() { return author; }
        public void setAuthor(String author) { this.author = author; }
        
        @Override
        public String toString() {
            return "Document{title='" + title + "', versionId=" + getId() + "}";
        }
    }
    
    // ======== Performance Comparison ========
    /*
     * EXAMPLE: Storing 10,000 phone numbers
     * 
     * WR Using PersistentONG - wrapper class:
     * - Each PhoneNumber gets OID
     * - 10,000 OIDs in database
     * - Each access loads separate object
     * - MEMORY: ~10MB+ overhead
     * 
     * RIGHT - Using String directly (IValue):
     * - Strings stored inline in collection
     * - No OIDs per element
     * - Single storage block
     * - MEMORY: ~200KB (just the string data)
     * 
     * RIGHT - Using Address implements IValue:
     * - Each Address stored inline
     * - No OIDs per Address
     * - MEMORY: ~500KB (just the address data)
     */
}
