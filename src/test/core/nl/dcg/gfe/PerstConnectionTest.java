package nl.dcg.gfe;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PerstConnection
 * Tests the wrapper behavior and Perst method delegation
 */
@ExtendWith(MockitoExtension.class)
class PerstConnectionTest {

    @Mock
    private Connection mockDelegate;

    @Test
    void testConstructor() {
        // Test that constructor sets up the delegate and context
        PerstConnection connection = new PerstConnection(mockDelegate);
        
        assertNotNull(connection, "PerstConnection should be created");
    }

    @Test
    void testIsPerstAvailableReturnsFalse() {
        // When Perst is disabled, should return false
        PerstConnection connection = new PerstConnection(mockDelegate);
        
        assertFalse(connection.isPerstAvailable(), "Perst should not be available when disabled");
    }

    @Test
    void testRetrieveObjectThrowsWhenNotAvailable() {
        PerstConnection connection = new PerstConnection(mockDelegate);
        
        assertThrows(IllegalStateException.class, () -> {
            connection.retrieveObject(Actor.class, "test-uuid");
        }, "Should throw when Perst not available");
    }

    @Test
    void testRetrieveObjectWithFieldThrowsWhenNotAvailable() {
        PerstConnection connection = new PerstConnection(mockDelegate);
        
        assertThrows(IllegalStateException.class, () -> {
            connection.retrieveObject(Actor.class, "name", "TestActor");
        }, "Should throw when Perst not available");
    }

    @Test
    void testRetrieveAllObjectsThrowsWhenNotAvailable() {
        PerstConnection connection = new PerstConnection(mockDelegate);
        
        assertThrows(IllegalStateException.class, () -> {
            connection.retrieveAllObjects(Actor.class);
        }, "Should throw when Perst not available");
    }

    @Test
    void testStoreNewObjectThrowsWhenNotAvailable() {
        PerstConnection connection = new PerstConnection(mockDelegate);
        Actor actor = new Actor("Test", "TEST");
        
        assertThrows(IllegalStateException.class, () -> {
            connection.storeNewObject(actor);
        }, "Should throw when Perst not available");
    }

    @Test
    void testStoreModifiedObjectThrowsWhenNotAvailable() {
        PerstConnection connection = new PerstConnection(mockDelegate);
        Actor actor = new Actor("Test", "TEST");
        
        assertThrows(IllegalStateException.class, () -> {
            connection.storeModifiedObject(actor);
        }, "Should throw when Perst not available");
    }

    @Test
    void testRemoveObjectThrowsWhenNotAvailable() {
        PerstConnection connection = new PerstConnection(mockDelegate);
        Actor actor = new Actor("Test", "TEST");
        
        assertThrows(IllegalStateException.class, () -> {
            connection.removeObject(actor);
        }, "Should throw when Perst not available");
    }

    @Test
    void testStartTransactionThrowsWhenNotAvailable() {
        PerstConnection connection = new PerstConnection(mockDelegate);
        
        assertThrows(IllegalStateException.class, () -> {
            connection.startTransaction();
        }, "Should throw when Perst not available");
    }

    @Test
    void testEndTransactionThrowsWhenNotAvailable() {
        PerstConnection connection = new PerstConnection(mockDelegate);
        
        assertThrows(IllegalStateException.class, () -> {
            connection.endTransaction();
        }, "Should throw when Perst not available");
    }

    @Test
    void testRollbackTransactionThrowsWhenNotAvailable() {
        PerstConnection connection = new PerstConnection(mockDelegate);
        
        assertThrows(IllegalStateException.class, () -> {
            connection.rollbackTransaction();
        }, "Should throw when Perst not available");
    }

    // Test delegate methods are properly forwarded

    @Test
    void testCloseDelegatesToConnection() throws SQLException {
        PerstConnection connection = new PerstConnection(mockDelegate);
        
        connection.close();
        
        verify(mockDelegate, times(1)).close();
    }

    @Test
    void testIsClosedDelegatesToConnection() throws SQLException {
        when(mockDelegate.isClosed()).thenReturn(true);
        
        PerstConnection connection = new PerstConnection(mockDelegate);
        boolean result = connection.isClosed();
        
        assertTrue(result);
        verify(mockDelegate, times(1)).isClosed();
    }

    @Test
    void testCommitDelegatesToConnection() throws SQLException {
        PerstConnection connection = new PerstConnection(mockDelegate);
        
        connection.commit();
        
        verify(mockDelegate, times(1)).commit();
    }

    @Test
    void testRollbackDelegatesToConnection() throws SQLException {
        PerstConnection connection = new PerstConnection(mockDelegate);
        
        connection.rollback();
        
        verify(mockDelegate, times(1)).rollback();
    }

    @Test
    void testSetAutoCommitDelegatesToConnection() throws SQLException {
        PerstConnection connection = new PerstConnection(mockDelegate);
        
        connection.setAutoCommit(false);
        
        verify(mockDelegate, times(1)).setAutoCommit(false);
    }

    @Test
    void testGetAutoCommitDelegatesToConnection() throws SQLException {
        when(mockDelegate.getAutoCommit()).thenReturn(true);
        
        PerstConnection connection = new PerstConnection(mockDelegate);
        boolean result = connection.getAutoCommit();
        
        assertTrue(result);
        verify(mockDelegate, times(1)).getAutoCommit();
    }

    @Test
    void testCreateStatementDelegatesToConnection() throws SQLException {
        when(mockDelegate.createStatement()).thenReturn(null);
        
        PerstConnection connection = new PerstConnection(mockDelegate);
        connection.createStatement();
        
        verify(mockDelegate, times(1)).createStatement();
    }

    @Test
    void testPrepareStatementDelegatesToConnection() throws SQLException {
        when(mockDelegate.prepareStatement("SELECT 1")).thenReturn(null);
        
        PerstConnection connection = new PerstConnection(mockDelegate);
        connection.prepareStatement("SELECT 1");
        
        verify(mockDelegate, times(1)).prepareStatement("SELECT 1");
    }

    @Test
    void testGetMetaDataDelegatesToConnection() throws SQLException {
        when(mockDelegate.getMetaData()).thenReturn(null);
        
        PerstConnection connection = new PerstConnection(mockDelegate);
        connection.getMetaData();
        
        verify(mockDelegate, times(1)).getMetaData();
    }

    @Test
    void testIsWrapperForReturnsTrueForConnection() throws SQLException {
        PerstConnection connection = new PerstConnection(mockDelegate);
        
        assertTrue(connection.isWrapperFor(Connection.class), 
            "Should return true for Connection.class");
    }

    @Test
    void testUnwrapReturnsConnection() throws SQLException {
        PerstConnection connection = new PerstConnection(mockDelegate);
        
        Connection unwrapped = connection.unwrap(Connection.class);
        
        assertSame(mockDelegate, unwrapped, "Should return the delegate connection");
    }
}
