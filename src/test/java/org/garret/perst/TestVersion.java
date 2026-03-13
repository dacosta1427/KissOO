package org.garret.perst;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Date;
import java.util.Iterator;
import java.util.Calendar;

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

    @Test
    @DisplayName("Test version history setCurrent")
    void testVersionHistorySetCurrent() {
        BugTrackingSystem bts = new BugTrackingSystem(storage);
        storage.setRoot(bts);

        bts.addProduct("Product1");
        bts.addBugReport("Bug1", "Test bug", BugReport.SEVERITY_NORMAL, "Product1");

        VersionHistory<BugReport> vh = bts.bugReports.get("Bug1");
        assertNotNull(vh, "Should find bug report");

        // Create multiple versions
        bts.changeBugReport("Bug1", BugReport.STATUS_OPEN);
        bts.changeBugReport("Bug1", BugReport.STATUS_CLOSED);

        // Get all versions
        Version[] allVersions = vh.getAllVersions();
        assertEquals(3, allVersions.length, "Should have 3 versions");

        // Set current to the first version (root)
        vh.setCurrent((BugReport) allVersions[0]);
        assertEquals(allVersions[0], vh.getCurrent(), "Current should be set to root");
    }

    @Test
    @DisplayName("Test version history getAllVersions and iterator")
    void testVersionHistoryGetAllVersionsAndIterator() {
        BugTrackingSystem bts = new BugTrackingSystem(storage);
        storage.setRoot(bts);

        bts.addProduct("Product1");
        bts.addBugReport("Bug1", "Test bug", BugReport.SEVERITY_NORMAL, "Product1");

        VersionHistory<BugReport> vh = bts.bugReports.get("Bug1");

        // Initially one version
        Version[] allVersions = vh.getAllVersions();
        assertEquals(1, allVersions.length, "Should have 1 version initially");

        // Add more versions
        bts.changeBugReport("Bug1", BugReport.STATUS_OPEN);
        bts.changeBugReport("Bug1", BugReport.STATUS_CLOSED);

        allVersions = vh.getAllVersions();
        assertEquals(3, allVersions.length, "Should have 3 versions");

        // Test iterator
        int count = 0;
        Iterator<BugReport> iter = vh.iterator();
        while (iter.hasNext()) {
            BugReport br = iter.next();
            assertNotNull(br, "Version should not be null");
            count++;
        }
        assertEquals(3, count, "Iterator should return 3 versions");
    }

    @Test
    @DisplayName("Test version history getLatestBefore")
    void testVersionHistoryGetLatestBefore() throws InterruptedException {
        BugTrackingSystem bts = new BugTrackingSystem(storage);
        storage.setRoot(bts);

        bts.addProduct("Product1");
        bts.addBugReport("Bug1", "Test bug", BugReport.SEVERITY_NORMAL, "Product1");

        VersionHistory<BugReport> vh = bts.bugReports.get("Bug1");

        // Get root time
        BugReport root = vh.getRoot();
        Date rootDate = root.getDate();

        // Wait and add new version
        Thread.sleep(50);
        bts.changeBugReport("Bug1", BugReport.STATUS_OPEN);

        // Get latest with null - should return latest
        BugReport latest = vh.getLatestBefore(null);
        assertNotNull(latest, "Should find latest version");
        assertEquals(BugReport.STATUS_OPEN, latest.status, "Should be the open status version");

        // Get latest before root date + 1ms - should return root
        BugReport beforeRoot = vh.getLatestBefore(new Date(rootDate.getTime() + 1));
        assertNotNull(beforeRoot, "Should find version before root+1");
        assertEquals(BugReport.STATUS_SUBMITTED, beforeRoot.status, "Should be submitted status");

        // Get latest before epoch - should return null
        BugReport beforeEpoch = vh.getLatestBefore(new Date(0));
        assertNull(beforeEpoch, "Should return null for date before all versions");
    }

    @Test
    @DisplayName("Test version history getEarliestAfter")
    void testVersionHistoryGetEarliestAfter() throws InterruptedException {
        BugTrackingSystem bts = new BugTrackingSystem(storage);
        storage.setRoot(bts);

        bts.addProduct("Product1");
        bts.addBugReport("Bug1", "Test bug", BugReport.SEVERITY_NORMAL, "Product1");

        VersionHistory<BugReport> vh = bts.bugReports.get("Bug1");

        // Get root time
        BugReport root = vh.getRoot();
        Date rootDate = root.getDate();

        // Wait and add new versions
        Thread.sleep(50);
        bts.changeBugReport("Bug1", BugReport.STATUS_OPEN);
        Thread.sleep(50);
        bts.changeBugReport("Bug1", BugReport.STATUS_CLOSED);

        // Get earliest with null - should return root
        BugReport earliest = vh.getEarliestAfter(null);
        assertNotNull(earliest, "Should find earliest version");
        assertEquals(BugReport.STATUS_SUBMITTED, earliest.status, "Should be root");

        // Get earliest after root - should be open version
        BugReport afterRoot = vh.getEarliestAfter(new Date(rootDate.getTime() + 25));
        assertNotNull(afterRoot, "Should find version after root");
        assertEquals(BugReport.STATUS_OPEN, afterRoot.status, "Should be open status");

        // Get earliest after far future - should return null
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, 10);
        BugReport afterFuture = vh.getEarliestAfter(cal.getTime());
        assertNull(afterFuture, "Should return null for far future date");
    }

    @Test
    @DisplayName("Test version labels")
    void testVersionLabels() {
        BugTrackingSystem bts = new BugTrackingSystem(storage);
        storage.setRoot(bts);

        bts.addProduct("Product1");
        bts.addBugReport("Bug1", "Test bug", BugReport.SEVERITY_NORMAL, "Product1");

        VersionHistory<BugReport> vh = bts.bugReports.get("Bug1");

        BugReport root = vh.getRoot();
        assertNotNull(root, "Should have root");

        // Initially no labels
        String[] labels = root.getLabels();
        assertEquals(0, labels.length, "Should have no labels initially");

        // Add labels
        root.addLabel("v1.0");
        root.addLabel("stable");

        labels = root.getLabels();
        assertEquals(2, labels.length, "Should have 2 labels");
        assertTrue(root.hasLabel("v1.0"), "Should have v1.0 label");
        assertTrue(root.hasLabel("stable"), "Should have stable label");
        assertFalse(root.hasLabel("nonexistent"), "Should not have nonexistent label");
    }

    @Test
    @DisplayName("Test version history getVersionByLabel")
    void testVersionHistoryGetVersionByLabel() {
        BugTrackingSystem bts = new BugTrackingSystem(storage);
        storage.setRoot(bts);

        bts.addProduct("Product1");
        bts.addBugReport("Bug1", "Test bug", BugReport.SEVERITY_NORMAL, "Product1");

        VersionHistory<BugReport> vh = bts.bugReports.get("Bug1");

        BugReport root = vh.getRoot();
        root.addLabel("v1.0");

        // Add another version with a different label
        bts.changeBugReport("Bug1", BugReport.STATUS_OPEN);
        BugReport current = vh.getCurrent();
        current.addLabel("v2.0");

        // Find by label
        BugReport v1 = vh.getVersionByLabel("v1.0");
        assertNotNull(v1, "Should find v1.0");
        assertEquals(BugReport.STATUS_SUBMITTED, v1.status, "v1.0 should be submitted status");

        BugReport v2 = vh.getVersionByLabel("v2.0");
        assertNotNull(v2, "Should find v2.0");
        assertEquals(BugReport.STATUS_OPEN, v2.status, "v2.0 should be open status");

        // Non-existent label
        BugReport notFound = vh.getVersionByLabel("v3.0");
        assertNull(notFound, "Should return null for non-existent label");
    }

    @Test
    @DisplayName("Test version history getVersionById")
    void testVersionHistoryGetVersionById() {
        BugTrackingSystem bts = new BugTrackingSystem(storage);
        storage.setRoot(bts);

        bts.addProduct("Product1");
        bts.addBugReport("Bug1", "Test bug", BugReport.SEVERITY_NORMAL, "Product1");

        VersionHistory<BugReport> vh = bts.bugReports.get("Bug1");

        BugReport root = vh.getRoot();
        String rootId = root.getId();
        assertNotNull(rootId, "Root should have an ID");
        assertEquals("1", rootId, "Root ID should be '1'");

        // Find by ID
        BugReport found = vh.getVersionById("1");
        assertNotNull(found, "Should find version by ID");
        assertEquals(root, found, "Found version should be root");

        // Add another version and find it
        bts.changeBugReport("Bug1", BugReport.STATUS_OPEN);
        BugReport current = vh.getCurrent();
        String currentId = current.getId();
        assertNotNull(currentId, "Current should have an ID");

        BugReport foundCurrent = vh.getVersionById(currentId);
        assertNotNull(foundCurrent, "Should find current by ID");
        assertEquals(current, foundCurrent, "Found should be current");

        // Non-existent ID
        BugReport notFound = vh.getVersionById("999");
        assertNull(notFound, "Should return null for non-existent ID");
    }

    @Test
    @DisplayName("Test version predecessors and successors")
    void testVersionPredecessorsAndSuccessors() {
        BugTrackingSystem bts = new BugTrackingSystem(storage);
        storage.setRoot(bts);

        bts.addProduct("Product1");
        bts.addBugReport("Bug1", "Test bug", BugReport.SEVERITY_NORMAL, "Product1");

        VersionHistory<BugReport> vh = bts.bugReports.get("Bug1");
        BugReport root = vh.getRoot();

        // Root has no predecessors (the Link is created with capacity 1 but size 0)
        Version[] predecessors = root.getPredecessors();
        assertEquals(0, predecessors.length, "Root has no predecessors");

        // Root has no successors initially
        Version[] successors = root.getSuccessors();
        assertEquals(0, successors.length, "Root has no successors initially");

        // Create a new version
        bts.changeBugReport("Bug1", BugReport.STATUS_OPEN);
        BugReport current = vh.getCurrent();

        // Current should have root as predecessor
        predecessors = current.getPredecessors();
        assertEquals(1, predecessors.length, "Current has 1 predecessor");
        assertEquals(root, predecessors[0], "Predecessor should be root");

        // Root should now have current as successor
        successors = root.getSuccessors();
        assertEquals(1, successors.length, "Root now has 1 successor");
        assertEquals(current, successors[0], "Successor should be current");

        // Current should have no successors
        successors = current.getSuccessors();
        assertEquals(0, successors.length, "Current has no successors");
    }

    @Test
    @DisplayName("Test version isCheckedIn and isCheckedOut")
    void testVersionCheckedInOut() {
        BugTrackingSystem bts = new BugTrackingSystem(storage);
        storage.setRoot(bts);

        bts.addProduct("Product1");
        bts.addBugReport("Bug1", "Test bug", BugReport.SEVERITY_NORMAL, "Product1");

        VersionHistory<BugReport> vh = bts.bugReports.get("Bug1");
        BugReport root = vh.getRoot();

        // Root is checked in
        assertTrue(root.isCheckedIn(), "Root should be checked in");
        assertFalse(root.isCheckedOut(), "Root should not be checked out");

        // Checkout creates a checked-out version
        BugReport checkedOut = vh.checkout();
        assertFalse(checkedOut.isCheckedIn(), "Checked out version should not be checked in");
        assertTrue(checkedOut.isCheckedOut(), "Checked out version should be checked out");

        // Checkin makes it checked-in
        checkedOut.status = BugReport.STATUS_CLOSED;
        checkedOut.checkin();
        assertTrue(checkedOut.isCheckedIn(), "After checkin, should be checked in");
        assertFalse(checkedOut.isCheckedOut(), "After checkin, should not be checked out");
    }

    @Test
    @DisplayName("Test version newVersion and addPredecessor")
    void testVersionNewVersionAndAddPredecessor() {
        BugTrackingSystem bts = new BugTrackingSystem(storage);
        storage.setRoot(bts);

        bts.addProduct("Product1");
        bts.addBugReport("Bug1", "Test bug", BugReport.SEVERITY_NORMAL, "Product1");
        bts.addBugReport("Bug2", "Another bug", BugReport.SEVERITY_NORMAL, "Product1");

        VersionHistory<BugReport> vh1 = bts.bugReports.get("Bug1");
        VersionHistory<BugReport> vh2 = bts.bugReports.get("Bug2");

        BugReport root1 = vh1.getRoot();
        BugReport root2 = vh2.getRoot();

        // Create a new version from root1
        BugReport newVersion = (BugReport) root1.newVersion();
        assertTrue(newVersion.isCheckedOut(), "New version should be checked out");

        // Add a second predecessor (merge from Bug2)
        newVersion.addPredecessor(root2);

        Version[] predecessors = newVersion.getPredecessors();
        assertEquals(2, predecessors.length, "Should have 2 predecessors after merge");

        // Checkin the merged version
        newVersion.checkin();
        assertTrue(newVersion.isCheckedIn(), "Merged version should be checked in after checkin");

        // Verify both predecessors have this as successor
        Version[] successors1 = root1.getSuccessors();
        Version[] successors2 = root2.getSuccessors();
        assertEquals(1, successors1.length, "Root1 should have 1 successor");
        assertEquals(1, successors2.length, "Root2 should have 1 successor");
    }

    @Test
    @DisplayName("Test version getDate")
    void testVersionGetDate() throws InterruptedException {
        BugTrackingSystem bts = new BugTrackingSystem(storage);
        storage.setRoot(bts);

        bts.addProduct("Product1");
        bts.addBugReport("Bug1", "Test bug", BugReport.SEVERITY_NORMAL, "Product1");

        VersionHistory<BugReport> vh = bts.bugReports.get("Bug1");
        BugReport root = vh.getRoot();

        Date rootDate = root.getDate();
        assertNotNull(rootDate, "Root should have a date");

        // Wait and add a new version
        Thread.sleep(50);
        bts.changeBugReport("Bug1", BugReport.STATUS_OPEN);

        BugReport current = vh.getCurrent();
        Date currentDate = current.getDate();
        assertNotNull(currentDate, "Current should have a date");
        assertTrue(currentDate.after(rootDate), "Current date should be after root date");
    }

    @Test
    @DisplayName("Test version history default constructor")
    void testVersionHistoryDefaultConstructor() {
        VersionHistory<BugReport> vh = new VersionHistory<BugReport>();
        assertNull(vh.getCurrent(), "Empty history should have null current");
        assertNull(vh.versions, "Empty history should have null versions");
    }
}
