/**
 * session.ts - Configurable Session State
 * 
 * Uses Svelte 5 runes ($state) for reactive session management.
 * Supports configurable persistence (memory vs localStorage).
 * Includes encrypted credential storage for silent re-authentication.
 */

// Session UUID - reactive with Svelte 5 runes
let uuid = $state('');

// Username - stored for admin detection
let username = $state('');

// User ID - stored for user operations
let userId = $state(0);

// Is Admin - for admin-only operations
let isAdmin = $state(false);

// Owner ID - stored for owner-specific operations (user is also an owner)
let ownerId = $state(0);

// Owner name - for display purposes
let ownerName = $state('');

// Cleaner ID - for cleaner-specific views (if user is a cleaner)
let cleanerId = $state(0);

// Email - for display and verification
let email = $state('');

// Language preference - stored locally for quick access
let preferredLanguage = $state('en');

// Persistence configuration
let persistToStorage = true;
const STORAGE_KEY = 'kiss_session_uuid';
const USERNAME_KEY = 'kiss_session_username';
const USERID_KEY = 'kiss_session_userid';
const OWNERID_KEY = 'kiss_session_ownerid';
const OWNERNAME_KEY = 'kiss_session_ownername';
const CLEANERID_KEY = 'kiss_session_cleanerid';
const EMAIL_KEY = 'kiss_session_email';
const CREDENTIALS_KEY = 'kiss_encrypted_credentials';
const KEY_STORAGE_KEY = 'kiss_encryption_key';
const LANGUAGE_KEY = 'kiss_preferred_language';

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
   * Get the current username
   */
  get username(): string {
    return username;
  },
  
  /**
   * Set the username
   */
  setUsername(name: string): void {
    username = name;
    // Persist username to localStorage
    if (typeof localStorage !== 'undefined' && name) {
      localStorage.setItem(USERNAME_KEY, name);
    }
  },
  
  /**
   * Get the user ID
   */
  get userId(): number {
    return userId;
  },
  
  /**
   * Set the user ID
   */
  setUserId(id: number): void {
    userId = id;
    if (typeof localStorage !== 'undefined') {
      localStorage.setItem(USERID_KEY, id.toString());
    }
  },
  
  /**
   * Get isAdmin flag
   */
  get isAdmin(): boolean {
    return isAdmin;
  },
  
  /**
   * Set isAdmin flag
   */
  setIsAdmin(admin: boolean): void {
    isAdmin = admin;
  },
  
  /**
   * Get the owner ID
   */
  get ownerId(): number {
    return ownerId;
  },
  
  /**
   * Set the owner ID
   */
  setOwnerId(id: number): void {
    ownerId = id;
    if (typeof localStorage !== 'undefined') {
      localStorage.setItem(OWNERID_KEY, id.toString());
    }
  },
  
  /**
   * Get the owner name
   */
  get ownerName(): string {
    return ownerName;
  },
  
  /**
   * Set the owner name
   */
  setOwnerName(name: string): void {
    ownerName = name;
    if (typeof localStorage !== 'undefined') {
      localStorage.setItem(OWNERNAME_KEY, name);
    }
  },
  
  /**
   * Get the cleaner ID (for cleaner-specific views)
   */
  get cleanerId(): number {
    return cleanerId;
  },
  
  /**
   * Set the cleaner ID
   */
  setCleanerId(id: number): void {
    cleanerId = id;
    if (typeof localStorage !== 'undefined') {
      localStorage.setItem(CLEANERID_KEY, id.toString());
    }
  },
  
  /**
   * Get the email
   */
  get email(): string {
    return email;
  },
  
  /**
   * Set the email
   */
  setEmail(emailAddr: string): void {
    email = emailAddr;
    if (typeof localStorage !== 'undefined') {
      localStorage.setItem(EMAIL_KEY, emailAddr);
    }
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
    username = '';
    userId = 0;
    ownerId = 0;
    ownerName = '';
    cleanerId = 0;
    email = '';
    if (typeof localStorage !== 'undefined') {
      localStorage.removeItem(STORAGE_KEY);
      localStorage.removeItem(USERNAME_KEY);
      localStorage.removeItem(USERID_KEY);
      localStorage.removeItem(OWNERID_KEY);
      localStorage.removeItem(OWNERNAME_KEY);
      localStorage.removeItem(CLEANERID_KEY);
      localStorage.removeItem(EMAIL_KEY);
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
      
      // Restore username if available
      const savedUsername = localStorage.getItem(USERNAME_KEY);
      if (savedUsername && savedUsername.length > 0) {
        username = savedUsername;
      }
      
      // Restore userId if available
      const savedUserId = localStorage.getItem(USERID_KEY);
      if (savedUserId) {
        userId = parseInt(savedUserId) || 0;
      }
      
      // Restore ownerId if available
      const savedOwnerId = localStorage.getItem(OWNERID_KEY);
      if (savedOwnerId) {
        ownerId = parseInt(savedOwnerId) || 0;
      }
      
      // Restore ownerName if available
      const savedOwnerName = localStorage.getItem(OWNERNAME_KEY);
      if (savedOwnerName) {
        ownerName = savedOwnerName;
      }
      
      // Restore cleanerId if available
      const savedCleanerId = localStorage.getItem(CLEANERID_KEY);
      if (savedCleanerId) {
        cleanerId = parseInt(savedCleanerId) || 0;
      }
      
      // Restore email if available
      const savedEmail = localStorage.getItem(EMAIL_KEY);
      if (savedEmail) {
        email = savedEmail;
      }
      
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
  },
  
  /**
   * Get the preferred language
   */
  getLanguage(): string {
    if (typeof localStorage !== 'undefined') {
      const stored = localStorage.getItem(LANGUAGE_KEY);
      if (stored) {
        preferredLanguage = stored;
        return stored;
      }
    }
    return preferredLanguage;
  },
  
  /**
   * Set the preferred language
   * @param lang - Language code (en, nl, de)
   */
  setLanguage(lang: string): void {
    preferredLanguage = lang;
    if (typeof localStorage !== 'undefined') {
      localStorage.setItem(LANGUAGE_KEY, lang);
    }
  },
  
  /**
   * Restore language preference from localStorage
   */
  restoreLanguage(): string {
    if (typeof localStorage === 'undefined') return preferredLanguage;
    
    const saved = localStorage.getItem(LANGUAGE_KEY);
    if (saved && saved.length > 0) {
      preferredLanguage = saved;
      return saved;
    }
    return preferredLanguage;
  }
};
