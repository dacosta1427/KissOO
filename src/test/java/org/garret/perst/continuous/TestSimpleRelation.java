package org.garret.perst.continuous;

import org.garret.perst.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.util.Iterator;

public class TestSimpleRelation {
    private Storage storage;
    private CDatabase db;
    private static final String DB_FILE = "TestSimpleRelation.dbs";
    private static final String INDEX_DIR = "TestSimpleRelation_index";

    @BeforeEach
    public void setUp() throws Exception {
        storage = StorageFactory.getInstance().createStorage();
        storage.open(DB_FILE);
        db = CDatabase.instance;
        db.open(storage, INDEX_DIR);
    }

    @AfterEach
    public void tearDown() throws Exception {
        if (db != null) {
            db.close();
        }
        if (storage != null && storage.isOpened()) {
            storage.close();
        }
        new File(DB_FILE).delete();
        deleteDir(new File(INDEX_DIR));
    }

    private void deleteDir(File dir) {
        if (dir.exists()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f.isDirectory()) {
                        deleteDir(f);
                    } else {
                        f.delete();
                    }
                }
            }
            dir.delete();
        }
    }

    @Test
    public void testInsertAndFindCompany() {
        db.beginTransaction();
        
        Address address = new Address("USA", "New York", "Broadway 123");
        Company company = new Company("ACME Corp", address);
        db.insert(company);
        
        db.commitTransaction();

        Company found = db.getSingleton(db.find(Company.class, "name", new Key("ACME Corp")));
        assertNotNull(found);
        assertEquals("ACME Corp", found.getName());
        assertEquals("USA", found.getLocation().getCountry());
        assertEquals("New York", found.getLocation().getCity());
    }

    @Test
    public void testInsertAndFindEmployee() {
        db.beginTransaction();
        
        Address address = new Address("USA", "New York", "Broadway 123");
        Company company = new Company("ACME Corp", address);
        db.insert(company);
        
        Employee employee = new Employee("John Doe", 30, company);
        db.insert(employee);
        
        db.commitTransaction();

        Employee found = db.getSingleton(db.find(Employee.class, "name", new Key("John Doe")));
        assertNotNull(found);
        assertEquals("John Doe", found.getName());
        assertEquals(30, found.getAge());
        assertNotNull(found.getCompany());
        assertEquals("ACME Corp", found.getCompany().getName());
    }

    @Test
    public void testJSQLQuery() {
        db.beginTransaction();
        
        Address addr1 = new Address("USA", "New York", "Broadway 123");
        Company comp1 = new Company("ACME Corp", addr1);
        db.insert(comp1);
        
        Address addr2 = new Address("UK", "London", "Oxford St");
        Company comp2 = new Company("British Ltd", addr2);
        db.insert(comp2);
        
        Employee emp1 = new Employee("John", 30, comp1);
        db.insert(emp1);
        
        Employee emp2 = new Employee("Jane", 25, comp2);
        db.insert(emp2);
        
        db.commitTransaction();

        Iterator<Company> results = db.select(Company.class, "name like 'A%'", null);
        int count = 0;
        while (results.hasNext()) {
            results.next();
            count++;
        }
        assertEquals(1, count);
    }

    @Test
    public void testFullTextSearch() {
        db.beginTransaction();
        
        Address address = new Address("USA", "New York", "Times Square");
        Company company = new Company("Big Corp", address);
        db.insert(company);
        
        Employee employee = new Employee("Alice", 28, company);
        db.insert(employee);
        
        db.commitTransaction();

        FullTextSearchResult[] results = db.fullTextSearch("Times Square", 10);
        assertNotNull(results);
    }

    @Test
    public void testVersionHistory() {
        long transId;
        
        db.beginTransaction();
        Address address = new Address("USA", "Boston", "Main St");
        Company company = new Company("Startup Inc", address);
        db.insert(company);
        db.commitTransaction();
        transId = company.transId;

        db.beginTransaction();
        company = company.update();
        company.setName("Startup Inc v2");
        db.commitTransaction();

        Company original = db.getSingleton(db.find(Company.class, "name", new Key("Startup Inc")));
        assertNull(original);

        Company v2 = db.getSingleton(db.find(Company.class, "name", new Key("Startup Inc v2")));
        assertNotNull(v2);
        
        db.beginTransaction(transId);
        Company snapshot = db.getSingleton(db.find(Company.class, "name", new Key("Startup Inc")));
        assertNotNull(snapshot);
        assertEquals("Startup Inc", snapshot.getName());
        db.commitTransaction();
    }

    @Test
    public void testGetRecords() {
        db.beginTransaction();
        
        for (int i = 0; i < 5; i++) {
            Address address = new Address("Country" + i, "City" + i, "Street" + i);
            Company company = new Company("Company" + i, address);
            db.insert(company);
        }
        
        db.commitTransaction();

        int count = 0;
        IterableIterator<Company> iter = db.getRecords(Company.class);
        while (iter.hasNext()) {
            iter.next();
            count++;
        }
        assertEquals(5, count);
    }
}
