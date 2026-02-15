package org.garret.perst;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 conversion of tst/TestVersion.java
 * Tests versioning functionality with bug tracking system
 */
class TestVersion {

    // Scaled down from 10000 bug reports
    private static final int N_PRODUCTS = 3;
    private static final int N_BUG_REPORTS = 100;
    private static final String TEST_DB = "bugdb.dbs";

    // Inner classes from original test
    static class BugReport extends Version {
        public static final int SEVERITY_LOW = 1;
        public static final int SEVERITY_NORMAL = 2;
        public static final int SEVERITY_CRITICAL = 3;
        public static final int SEVERITY_URGENT = 4;

        public static final int STATUS_SUBMITTED = 0;
        public static final int STATUS_OPEN = 1;
        public static final int STATUS_CLOSED = 2;
        public static final int STATUS_REJECTED = 3;

        String description;
        int severity;
        int status;
        VersionHistory<BugReport> product;

        public BugReport() {
        }

        public String getDescription() {
            return description;
        }

        public int getSeverity() {
            return severity;
        }

        public int getStatus() {
            return status;
        }

        public VersionHistory<BugReport> getProduct() {
            return product;
        }

        public BugReport(Storage storage, String description, int severity, VersionHistory<BugReport> product) {
            super(storage);
            this.description = description;
            this.severity = severity;
            this.product = product;
            status = STATUS_SUBMITTED;
        }
    }

    static class Release extends Version {
        String name;
        String releaseNotes;

        public Release() {
        }

        public String getName() {
            return name;
        }

        public String getReleaseNotes() {
            return releaseNotes;
        }

        public Release(Storage storage, String name) {
            super(storage);
            this.name = name;
            this.releaseNotes = "Initial release";
        }
    }

    static class BugTrackingSystem extends Persistent {
        Index<VersionHistory<BugReport>> bugReports;
        Index<VersionHistory<Release>> products;

        public BugTrackingSystem() {
        }

        public BugTrackingSystem(Storage storage) {
            super(storage);
            bugReports = storage.<VersionHistory<BugReport>>createIndex(String.class, true);
            products = storage.<VersionHistory<Release>>createIndex(String.class, true);
        }

        public boolean addProduct(String name) {
            return products.put(name, new VersionHistory<Release>(new Release(getStorage(), name)));
        }

        public boolean addRelease(String name, String releaseNotes) {
            VersionHistory<Release> vh = products.get(name);
            if (vh != null) {
                Release release = vh.checkout();
                release.releaseNotes = releaseNotes;
                release.checkin();
                return true;
            }
            return false;
        }

        public boolean addBugReport(String id, String description, int severity, String productName) {
            VersionHistory<Release> product = products.get(productName);
            if (product == null) {
                return false;
            }
            return bugReports.put(id, new VersionHistory<BugReport>(new BugReport(getStorage(), description, severity, null)));
        }

        public boolean changeBugReport(String id, int status) {
            VersionHistory<BugReport> vh = bugReports.get(id);
            if (vh == null) {
                return false;
            }
            BugReport cr = vh.checkout();
            cr.status = status;
            cr.checkin();
            return true;
        }

        public Iterator<VersionHistory<Release>> productIterator() {
            return products.iterator();
        }

        public Iterator<VersionHistory<BugReport>> bugReportIterator() {
            return bugReports.iterator();
        }
    }

    private Storage storage;

    @BeforeEach
    void setUp() throws Exception {
        storage = StorageFactory.getInstance().createStorage();
        storage.open(TEST_DB, 32 * 1024 * 1024);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (storage.isOpened()) {
            storage.close();
        }
        new java.io.File(TEST_DB).delete();
    }

    @Test
    @DisplayName("Test versioning create products")
    void testVersioningCreateProducts() {
        BugTrackingSystem bts = new BugTrackingSystem(storage);
        storage.setRoot(bts);

        // Create products
        for (int i = 0; i < N_PRODUCTS; i++) {
            assertTrue(bts.addProduct("Product" + (i + 1)), "Should add product " + (i + 1));
        }

        assertEquals(N_PRODUCTS, bts.products.size(), "Should have all products");
    }

    @Test
    @DisplayName("Test versioning add and change bug reports")
    void testVersioningBugReports() {
        BugTrackingSystem bts = new BugTrackingSystem(storage);
        storage.setRoot(bts);

        // Create products
        for (int i = 0; i < N_PRODUCTS; i++) {
            bts.addProduct("Product" + (i + 1));
        }

        // Add bug reports
        for (int i = 0; i < N_BUG_REPORTS; i++) {
            assertTrue(bts.addBugReport("Bug" + i, "Something is wrong", BugReport.SEVERITY_URGENT,
                    "Product" + (i % N_PRODUCTS + 1)), "Should add bug report " + i);
        }

        assertEquals(N_BUG_REPORTS, bts.bugReports.size(), "Should have all bug reports");

        // Change bug report status
        for (int i = 0; i < N_BUG_REPORTS; i++) {
            assertTrue(bts.changeBugReport("Bug" + i, BugReport.STATUS_OPEN), "Should change bug status");
        }

        // Add releases
        for (int i = 0; i < N_PRODUCTS; i++) {
            assertTrue(bts.addRelease("Product" + (i + 1), "All bugs are fixed"));
        }

        // Close bug reports
        for (int i = 0; i < N_BUG_REPORTS; i++) {
            assertTrue(bts.changeBugReport("Bug" + i, BugReport.STATUS_CLOSED));
        }
    }

    @Test
    @DisplayName("Test versioning iterate and verify")
    void testVersioningIterateAndVerify() {
        BugTrackingSystem bts = new BugTrackingSystem(storage);
        storage.setRoot(bts);

        // Create products
        for (int i = 0; i < N_PRODUCTS; i++) {
            bts.addProduct("Product" + (i + 1));
        }

        // Add bug reports
        for (int i = 0; i < N_BUG_REPORTS; i++) {
            bts.addBugReport("Bug" + i, "Something is wrong", BugReport.SEVERITY_URGENT,
                    "Product" + (i % N_PRODUCTS + 1));
        }

        // Change status
        for (int i = 0; i < N_BUG_REPORTS; i++) {
            bts.changeBugReport("Bug" + i, BugReport.STATUS_OPEN);
        }

        // Add releases
        for (int i = 0; i < N_PRODUCTS; i++) {
            bts.addRelease("Product" + (i + 1), "All bugs are fixed");
        }

        // Close bug reports
        for (int i = 0; i < N_BUG_REPORTS; i++) {
            bts.changeBugReport("Bug" + i, BugReport.STATUS_CLOSED);
        }

        // Iterate and verify
        int count = 0;
        Iterator<VersionHistory<BugReport>> iterator = bts.bugReportIterator();
        while (iterator.hasNext()) {
            VersionHistory<BugReport> vh1 = iterator.next();
            BugReport cr1 = vh1.getRoot();
            BugReport cr2 = vh1.getCurrent();

            assertEquals(BugReport.STATUS_SUBMITTED, cr1.status, "Root version should have submitted status");
            assertEquals(BugReport.STATUS_CLOSED, cr2.status, "Current version should have closed status");

            count++;
        }
        assertEquals(N_BUG_REPORTS, count, "Should iterate through all bug reports");
    }

    @Test
    @DisplayName("Test version history getCurrent")
    void testVersionHistoryGetCurrent() {
        BugTrackingSystem bts = new BugTrackingSystem(storage);
        storage.setRoot(bts);

        // Create product
        bts.addProduct("Product1");

        // Add initial bug report
        bts.addBugReport("Bug1", "Initial bug", BugReport.SEVERITY_NORMAL, "Product1");

        // Get the version history
        VersionHistory<BugReport> vh = bts.bugReports.get("Bug1");
        assertNotNull(vh, "Should find bug report");

        BugReport current = vh.getCurrent();
        assertEquals(BugReport.STATUS_SUBMITTED, current.status, "Initial status should be submitted");

        // Change status
        bts.changeBugReport("Bug1", BugReport.STATUS_OPEN);

        // Check updated version
        BugReport updated = vh.getCurrent();
        assertEquals(BugReport.STATUS_OPEN, updated.status, "Status should be updated to open");
    }

    @Test
    @DisplayName("Test version history getRoot")
    void testVersionHistoryGetRoot() {
        BugTrackingSystem bts = new BugTrackingSystem(storage);
        storage.setRoot(bts);

        // Create product
        bts.addProduct("Product1");

        // Add initial bug report
        bts.addBugReport("Bug1", "Initial bug", BugReport.SEVERITY_NORMAL, "Product1");

        // Get the version history
        VersionHistory<BugReport> vh = bts.bugReports.get("Bug1");
        assertNotNull(vh, "Should find bug report");

        BugReport root = vh.getRoot();
        assertNotNull(root, "Root version should exist");
        assertEquals("Initial bug", root.getDescription(), "Description should match");
    }
}
