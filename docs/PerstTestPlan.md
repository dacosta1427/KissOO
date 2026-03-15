# Perst DB Integration Test Plan

## Overview
This document outlines the test strategy for the Perst OODB integration code added to the KissOO project.

## Test Strategy: Integration Tests

Since PerstContext is a database-centric class, integration tests are more appropriate than unit tests with mocks because:
1. Mockito has compatibility issues with Java 25 (Byte Buddy)
2. CDatabase versioning can only be tested with real database operations
3. The core value is testing actual database interactions

## Test Files

### 1. PerstConfigTest.java (`src/test/core/oodb/`)
**Type:** Unit tests (no mocks needed)

**Test Cases:**
- `testSingletonPattern()` - Verify singleton returns same instance
- `testSetInstance()` - Verify dependency injection works
- `testDefaultValues()` - Verify default config values
- `testGetInstanceCreatesNewInstanceWhenNull()` - Verify lazy initialization
- `testSetInstanceAllowsNull()` - Verify null handling

### 2. PerstContextIntegrationTest.java (`src/test/core/oodb/`)
**Type:** Integration tests with in-memory Perst database

**Test Cases:**

#### Initialization Tests
- `testInitializeWithInMemoryDatabase()` - Startup with temp storage
- `testIsAvailableReturnsCorrectState()` - Availability state
- `testIsVersioningEnabled()` - CDatabase enable check

#### CRUD Operations
- `testStoreAndRetrieveUser()` - Create and read PerstUser
- `testStoreAndRetrieveActor()` - Create and read Actor
- `testStoreAndRetrieveAgreement()` - Create and read Agreement
- `testStoreAndRetrieveGroup()` - Create and read Group
- `testUpdateUser()` - Update existing record
- `testRemoveUser()` - Delete record

#### Transaction Tests
- `testTransactionCommit()` - Verify commit persists changes
- `testTransactionRollback()` - Verify rollback discards changes
- `testBeginTransactionThrowsWhenNotAvailable()` - Error handling

#### Version History Tests (CDatabase)
- `testVersionHistoryWhenEnabled()` - Get version history
- `testCurrentVersionRetrieval()` - Get current version

## Test Execution

### Running Tests
```bash
# Run all Perst tests
java -cp "..." org.junit.platform.console.ConsoleLauncher \
  --select-class=oodb.PerstConfigTest \
  --select-class=oodb.PerstContextIntegrationTest
```

### Prerequisites
- Perst jar (perst-dcg-4.0.0.jar) in classpath
- JUnit 5 in classpath
- No Mockito required (uses real Perst database)

## Notes

- Tests use temporary/in-memory storage to avoid file system pollution
- Each test should clean up after itself
- Tests are designed to run independently
- CDatabase versioning tests require `PerstConfig.setInstance()` with test config
