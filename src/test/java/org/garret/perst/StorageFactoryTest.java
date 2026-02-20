/*
 * $URL: StorageFactoryTest.java $ 
 * $Rev: 3582 $ 
 * $Date: 2007-11-25 14:29:06 +0300 (Вс., 25 нояб. 2007) $
 *
 * Copyright 2005 Netup, Inc. All rights reserved.
 * URL:    http://www.netup.biz
 * e-mail: info@netup.biz
 */

package org.garret.perst;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * These tests verifies an functionality of the <code>StorageFactory</code> class.
 */
public class StorageFactoryTest {

    /**
     * Verifies that a <CODE>createStorage()</CODE> method invocation returns a
     * not-<CODE>null</CODE> object.
     */
    @Test
    public void testCreateStorage() {
        Storage storage = StorageFactory.getInstance().createStorage();
        assertNotNull(storage);
    }

    /**
     * Verifies that a <CODE>createStorage()</CODE> method invocation returns different values
     * in sequential  calls.
     */
    @Test
    public void testCreateTwice() {
        Storage storage0 = StorageFactory.getInstance().createStorage();
        Storage storage1 = StorageFactory.getInstance().createStorage();
        assertNotNull(storage0);
        assertNotNull(storage1);
        assertNotSame(storage0, storage1);
    }

    // ===== Additional StorageFactory tests for coverage =====

    /**
     * Verifies that getInstance() returns the same singleton instance.
     */
    @Test
    public void testGetInstanceReturnsSameInstance() {
        StorageFactory factory1 = StorageFactory.getInstance();
        StorageFactory factory2 = StorageFactory.getInstance();
        assertSame(factory1, factory2, "getInstance() should return same singleton");
    }

    /**
     * Tests createReplicationMasterStorage() with minimal parameters.
     */
    @Test
    public void testCreateReplicationMasterStorage() {
        StorageFactory factory = StorageFactory.getInstance();
        ReplicationMasterStorage storage = factory.createReplicationMasterStorage(-1, new String[0], 0);
        assertNotNull(storage, "createReplicationMasterStorage() should return non-null storage");
    }

    /**
     * Tests createReplicationMasterStorage() with page timestamp file.
     */
    @Test
    public void testCreateReplicationMasterStorageWithTimestampFile() {
        StorageFactory factory = StorageFactory.getInstance();
        ReplicationMasterStorage storage = factory.createReplicationMasterStorage(-1, new String[0], 0, null);
        assertNotNull(storage, "createReplicationMasterStorage() with timestamp file should return non-null storage");
    }

    /**
     * Tests createReplicationSlaveStorage() with minimal parameters.
     */
    @Test
    public void testCreateReplicationSlaveStorage() {
        StorageFactory factory = StorageFactory.getInstance();
        ReplicationSlaveStorage storage = factory.createReplicationSlaveStorage(0);
        assertNotNull(storage, "createReplicationSlaveStorage() should return non-null storage");
    }

    /**
     * Tests createReplicationSlaveStorage() with page timestamp file.
     */
    @Test
    public void testCreateReplicationSlaveStorageWithTimestampFile() {
        StorageFactory factory = StorageFactory.getInstance();
        ReplicationSlaveStorage storage = factory.createReplicationSlaveStorage(0, null);
        assertNotNull(storage, "createReplicationSlaveStorage() with timestamp file should return non-null storage");
    }

    /**
     * Tests addReplicationSlaveStorage() with minimal parameters.
     */
    @Test
    public void testAddReplicationSlaveStorage() {
        StorageFactory factory = StorageFactory.getInstance();
        ReplicationSlaveStorage storage = factory.addReplicationSlaveStorage("localhost", 0);
        assertNotNull(storage, "addReplicationSlaveStorage() should return non-null storage");
    }

    /**
     * Tests addReplicationSlaveStorage() with page timestamp file.
     */
    @Test
    public void testAddReplicationSlaveStorageWithTimestampFile() {
        StorageFactory factory = StorageFactory.getInstance();
        ReplicationSlaveStorage storage = factory.addReplicationSlaveStorage("localhost", 0, null);
        assertNotNull(storage, "addReplicationSlaveStorage() with timestamp file should return non-null storage");
    }

}
