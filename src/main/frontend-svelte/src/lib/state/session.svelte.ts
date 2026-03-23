/**
 * session.ts - Configurable Session State
 * 
 * Uses Svelte 5 runes ($state) for reactive session management.
 * Supports configurable persistence (memory vs localStorage).
 * Includes encrypted credential storage for silent re-authentication.
 */

// Session UUID - reactive with Svelte 5 runes
let uuid = $state('');

// Persistence configuration
let persistToStorage = true;
const STORAGE_KEY = 'kiss_session_uuid';
const CREDENTIALS_KEY = 'kiss_encrypted_credentials';
const KEY_STORAGE_KEY = 'kiss_encryption_key';

// Encryption/Decryption helpers (Web Crypto API)
async function generateEncryptionKey(): Promise<CryptoKey> {
  return await crypto.subtle.generateKey(
    { name: 'AES-GCM', length: 256 },
    true,
    ['encrypt', 'decrypt']
  );
}

async function exportKey(key: CryptoKey): Promise<string> {
  const exported = await crypto.subtle.exportKey('raw', key);
  return btoa(String.fromCharCode(...new Uint8Array(exported)));
}

async function importKey(keyStr: string): Promise<CryptoKey> {
  const keyData = Uint8Array.from(atob(keyStr), c => c.charCodeAt(0));
  return await crypto.subtle.importKey(
    'raw',
    keyData,
    { name: 'AES-GCM', length: 256 },
    true,
    ['encrypt', 'decrypt']
  );
}

async function getOrCreateKey(): Promise<CryptoKey> {
  if (typeof localStorage === 'undefined') {
    throw new Error('localStorage not available');
  }
  
  const storedKey = localStorage.getItem(KEY_STORAGE_KEY);
  if (storedKey) {
    return await importKey(storedKey);
  }
  
  const key = await generateEncryptionKey();
  const exported = await exportKey(key);
  localStorage.setItem(KEY_STORAGE_KEY, exported);
  return key;
}

async function encryptData(data: string): Promise<string> {
  const key = await getOrCreateKey();
  const iv = crypto.getRandomValues(new Uint8Array(12));
  const encoded = new TextEncoder().encode(data);
  
  const encrypted = await crypto.subtle.encrypt(
    { name: 'AES-GCM', iv },
    key,
    encoded
  );
  
  const combined = new Uint8Array(iv.length + encrypted.byteLength);
  combined.set(iv);
  combined.set(new Uint8Array(encrypted), iv.length);
  
  return btoa(String.fromCharCode(...combined));
}

async function decryptData(encryptedStr: string): Promise<string> {
  try {
    const key = await getOrCreateKey();
    const combined = Uint8Array.from(atob(encryptedStr), c => c.charCodeAt(0));
    
    const iv = combined.slice(0, 12);
    const encrypted = combined.slice(12);
    
    const decrypted = await crypto.subtle.decrypt(
      { name: 'AES-GCM', iv },
      key,
      encrypted
    );
    
    return new TextDecoder().decode(decrypted);
  } catch (error) {
    console.error('Decryption failed:', error);
    throw new Error('Failed to decrypt credentials');
  }
}

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
   * Store credentials encrypted in localStorage
   * @param username - Username
   * @param password - Password
   */
  async storeCredentials(username: string, password: string): Promise<void> {
    if (typeof localStorage === 'undefined') return;
    
    try {
      const data = JSON.stringify({ username, password });
      const encrypted = await encryptData(data);
      localStorage.setItem(CREDENTIALS_KEY, encrypted);
    } catch (error) {
      console.error('Failed to store credentials:', error);
    }
  },
  
  /**
   * Get stored credentials (decrypted)
   * @returns Object with username and password, or null if not stored
   */
  async getStoredCredentials(): Promise<{ username: string; password: string } | null> {
    if (typeof localStorage === 'undefined') return null;
    
    const encrypted = localStorage.getItem(CREDENTIALS_KEY);
    if (!encrypted) return null;
    
    try {
      const decrypted = await decryptData(encrypted);
      return JSON.parse(decrypted);
    } catch (error) {
      console.error('Failed to retrieve credentials:', error);
      // Clear invalid credentials
      localStorage.removeItem(CREDENTIALS_KEY);
      return null;
    }
  },
  
  /**
   * Clear stored credentials
   */
  clearCredentials(): void {
    if (typeof localStorage !== 'undefined') {
      localStorage.removeItem(CREDENTIALS_KEY);
    }
  },
  
  /**
   * Check if credentials are stored
   */
  hasStoredCredentials(): boolean {
    if (typeof localStorage === 'undefined') return false;
    return !!localStorage.getItem(CREDENTIALS_KEY);
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
