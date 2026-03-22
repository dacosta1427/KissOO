import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { session } from './session.svelte';

// Mock localStorage
const localStorageMock = {
  getItem: vi.fn(),
  setItem: vi.fn(),
  removeItem: vi.fn(),
  clear: vi.fn()
};

// Mock global localStorage
Object.defineProperty(global, 'localStorage', {
  value: localStorageMock,
  writable: true
});

describe('Session State', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    // Reset session state by calling clear
    session.clear();
  });

  describe('uuid getter', () => {
    it('should return empty string initially', () => {
      expect(session.uuid).toBe('');
    });

    it('should return the set UUID', () => {
      session.setUUID('test-uuid-123');
      expect(session.uuid).toBe('test-uuid-123');
    });
  });

  describe('isAuthenticated getter', () => {
    it('should return false when UUID is empty', () => {
      expect(session.isAuthenticated).toBe(false);
    });

    it('should return true when UUID is set', () => {
      session.setUUID('test-uuid-123');
      expect(session.isAuthenticated).toBe(true);
    });

    it('should return false after clearing', () => {
      session.setUUID('test-uuid-123');
      session.clear();
      expect(session.isAuthenticated).toBe(false);
    });
  });

  describe('setUUID', () => {
    it('should set the UUID', () => {
      session.setUUID('new-uuid-456');
      expect(session.uuid).toBe('new-uuid-456');
    });

    it('should persist to localStorage by default', () => {
      session.setUUID('test-uuid-123');
      expect(localStorageMock.setItem).toHaveBeenCalledWith('kiss_session_uuid', 'test-uuid-123');
    });

    it('should persist when persist parameter is true', () => {
      session.setUUID('test-uuid-123', true);
      expect(localStorageMock.setItem).toHaveBeenCalledWith('kiss_session_uuid', 'test-uuid-123');
    });

    it('should not persist when persist parameter is false', () => {
      session.setUUID('test-uuid-123', false);
      expect(localStorageMock.setItem).not.toHaveBeenCalled();
    });

    it('should handle undefined localStorage gracefully', () => {
      const originalLocalStorage = global.localStorage;
      // @ts-expect-error - testing SSR scenario
      delete global.localStorage;

      expect(() => session.setUUID('test-uuid-123')).not.toThrow();

      global.localStorage = originalLocalStorage;
    });
  });

  describe('clear', () => {
    it('should clear the UUID', () => {
      session.setUUID('test-uuid-123');
      session.clear();
      expect(session.uuid).toBe('');
    });

    it('should remove from localStorage', () => {
      session.setUUID('test-uuid-123');
      session.clear();
      expect(localStorageMock.removeItem).toHaveBeenCalledWith('kiss_session_uuid');
    });

    it('should handle undefined localStorage gracefully', () => {
      session.setUUID('test-uuid-123');
      const originalLocalStorage = global.localStorage;
      // @ts-expect-error - testing SSR scenario
      delete global.localStorage;

      expect(() => session.clear()).not.toThrow();

      global.localStorage = originalLocalStorage;
    });
  });

  describe('restore', () => {
    it('should restore UUID from localStorage', () => {
      localStorageMock.getItem.mockReturnValue('saved-uuid-789');
      const result = session.restore();
      expect(session.uuid).toBe('saved-uuid-789');
      expect(result).toBe(true);
    });

    it('should return false if no saved UUID', () => {
      localStorageMock.getItem.mockReturnValue(null);
      const result = session.restore();
      expect(result).toBe(false);
    });

    it('should return false if saved UUID is empty', () => {
      localStorageMock.getItem.mockReturnValue('');
      const result = session.restore();
      expect(result).toBe(false);
    });

    it('should return false in SSR environment', () => {
      const originalLocalStorage = global.localStorage;
      // @ts-expect-error - testing SSR scenario
      delete global.localStorage;

      const result = session.restore();
      expect(result).toBe(false);

      global.localStorage = originalLocalStorage;
    });
  });

  describe('setPersistence', () => {
    it('should enable persistence by default', () => {
      session.setUUID('test-uuid-123');
      expect(localStorageMock.setItem).toHaveBeenCalled();
    });

    it('should disable persistence', () => {
      session.setPersistence(false);
      localStorageMock.setItem.mockClear();
      session.setUUID('test-uuid-123');
      expect(localStorageMock.setItem).not.toHaveBeenCalled();
    });

    it('should clear storage when disabling persistence', () => {
      session.setPersistence(false);
      expect(localStorageMock.removeItem).toHaveBeenCalledWith('kiss_session_uuid');
    });

    it('should re-enable persistence', () => {
      session.setPersistence(false);
      session.setPersistence(true);
      localStorageMock.setItem.mockClear();
      session.setUUID('test-uuid-123');
      expect(localStorageMock.setItem).toHaveBeenCalledWith('kiss_session_uuid', 'test-uuid-123');
    });
  });

  describe('isPersistent getter', () => {
    it('should return true by default', () => {
      expect(session.isPersistent).toBe(true);
    });

    it('should return false after disabling persistence', () => {
      session.setPersistence(false);
      expect(session.isPersistent).toBe(false);
    });

    it('should return true after re-enabling persistence', () => {
      session.setPersistence(false);
      session.setPersistence(true);
      expect(session.isPersistent).toBe(true);
    });
  });
});
