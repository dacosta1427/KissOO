/**
 * session.ts - Configurable Session State
 * 
 * Uses Svelte 5 runes ($state) for reactive session management.
 * Supports configurable persistence (memory vs localStorage).
 */

// Session UUID - reactive with Svelte 5 runes
let uuid = $state('');

// Persistence configuration
let persistToStorage = true;
const STORAGE_KEY = 'kiss_session_uuid';

/**
 * Session state object
 * Provides reactive access to authentication state
 */
export const session = {
  /**
   * Get the current session UUID
   */
  get uuid(): string {
    return uuid;
  },
  
  /**
   * Check if user is authenticated
   */
  get isAuthenticated(): boolean {
    return uuid.length > 0;
  },
  
  /**
   * Set the session UUID
   * @param newUuid - The UUID from backend login
   * @param persist - Override persistence setting (optional)
   */
  setUUID(newUuid: string, persist?: boolean): void {
    uuid = newUuid;
    if (persist ?? persistToStorage) {
      if (typeof localStorage !== 'undefined') {
        localStorage.setItem(STORAGE_KEY, newUuid);
      }
    }
  },
  
  /**
   * Clear the session (logout)
   */
  clear(): void {
    uuid = '';
    if (typeof localStorage !== 'undefined') {
      localStorage.removeItem(STORAGE_KEY);
    }
  },
  
  /**
   * Restore session from localStorage (call in onMount)
   * @returns true if session was restored
   */
  restore(): boolean {
    if (typeof localStorage === 'undefined') return false;
    
    const saved = localStorage.getItem(STORAGE_KEY);
    if (saved && saved.length > 0) {
      uuid = saved;
      return true;
    }
    return false;
  },
  
  /**
   * Configure persistence behavior
   * @param enabled - true to persist to localStorage, false for memory only
   */
  setPersistence(enabled: boolean): void {
    persistToStorage = enabled;
    if (!enabled) {
      // Clear storage if disabling persistence
      if (typeof localStorage !== 'undefined') {
        localStorage.removeItem(STORAGE_KEY);
      }
    }
  },
  
  /**
   * Get current persistence setting
   */
  get isPersistent(): boolean {
    return persistToStorage;
  }
};
