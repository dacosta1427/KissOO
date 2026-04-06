import "clsx";
let uuid = "";
let username = "";
let userOid = 0;
let isAdmin = false;
let adminType = "none";
let ownerOid = 0;
let ownerName = "";
let cleanerOid = 0;
let email = "";
let preferredLanguage = "en";
let persistToStorage = true;
const STORAGE_KEY = "kiss_session_uuid";
const USERNAME_KEY = "kiss_session_username";
const USERID_KEY = "kiss_session_useroid";
const OWNERID_KEY = "kiss_session_owneroid";
const OWNERNAME_KEY = "kiss_session_ownername";
const CLEANERID_KEY = "kiss_session_cleaneroid";
const EMAIL_KEY = "kiss_session_email";
const CREDENTIALS_KEY = "kiss_encrypted_credentials";
const KEY_STORAGE_KEY = "kiss_encryption_key";
const LANGUAGE_KEY = "kiss_preferred_language";
const IS_ADMIN_KEY = "kiss_session_isadmin";
const ADMIN_TYPE_KEY = "kiss_session_admintype";
async function generateEncryptionKey() {
  return await crypto.subtle.generateKey({ name: "AES-GCM", length: 256 }, true, ["encrypt", "decrypt"]);
}
async function exportKey(key) {
  const exported = await crypto.subtle.exportKey("raw", key);
  return btoa(String.fromCharCode(...new Uint8Array(exported)));
}
async function importKey(keyStr) {
  const keyData = Uint8Array.from(atob(keyStr), (c) => c.charCodeAt(0));
  return await crypto.subtle.importKey("raw", keyData, { name: "AES-GCM", length: 256 }, true, ["encrypt", "decrypt"]);
}
async function getOrCreateKey() {
  if (typeof localStorage === "undefined") {
    throw new Error("localStorage not available");
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
async function encryptData(data) {
  const key = await getOrCreateKey();
  const iv = crypto.getRandomValues(new Uint8Array(12));
  const encoded = new TextEncoder().encode(data);
  const encrypted = await crypto.subtle.encrypt({ name: "AES-GCM", iv }, key, encoded);
  const combined = new Uint8Array(iv.length + encrypted.byteLength);
  combined.set(iv);
  combined.set(new Uint8Array(encrypted), iv.length);
  return btoa(String.fromCharCode(...combined));
}
async function decryptData(encryptedStr) {
  try {
    const key = await getOrCreateKey();
    const combined = Uint8Array.from(atob(encryptedStr), (c) => c.charCodeAt(0));
    const iv = combined.slice(0, 12);
    const encrypted = combined.slice(12);
    const decrypted = await crypto.subtle.decrypt({ name: "AES-GCM", iv }, key, encrypted);
    return new TextDecoder().decode(decrypted);
  } catch (error) {
    console.error("Decryption failed:", error);
    throw new Error("Failed to decrypt credentials");
  }
}
const session = {
  /**
   * Get the current session UUID
   */
  get uuid() {
    return uuid;
  },
  /**
   * Get the current username
   */
  get username() {
    return username;
  },
  /**
   * Set the username
   */
  setUsername(name) {
    username = name;
    if (typeof localStorage !== "undefined" && name) {
      localStorage.setItem(USERNAME_KEY, name);
    }
  },
  /**
   * Get the user OID
   */
  get userOid() {
    return userOid;
  },
  /**
   * Set the user OID
   */
  setUserOid(id) {
    userOid = id;
    if (typeof localStorage !== "undefined") {
      localStorage.setItem(USERID_KEY, id.toString());
    }
  },
  /**
   * Get isAdmin flag
   */
  get isAdmin() {
    return isAdmin;
  },
  /**
   * Set isAdmin flag
   */
  setIsAdmin(admin) {
    isAdmin = admin;
    if (typeof localStorage !== "undefined") {
      localStorage.setItem(IS_ADMIN_KEY, admin.toString());
    }
  },
  /**
   * Get admin type: 'system' (full), 'content' (no system), or 'none'
   */
  get adminType() {
    return adminType;
  },
  /**
   * Set admin type
   */
  setAdminType(type) {
    adminType = type;
    if (typeof localStorage !== "undefined") {
      localStorage.setItem(ADMIN_TYPE_KEY, type);
    }
  },
  /**
   * Get the owner OID
   */
  get ownerOid() {
    return ownerOid;
  },
  /**
   * Set the owner OID
   */
  setOwnerOid(id) {
    ownerOid = id;
    if (typeof localStorage !== "undefined") {
      localStorage.setItem(OWNERID_KEY, id.toString());
    }
  },
  /**
   * Get the owner name
   */
  get ownerName() {
    return ownerName;
  },
  /**
   * Set the owner name
   */
  setOwnerName(name) {
    ownerName = name;
    if (typeof localStorage !== "undefined") {
      localStorage.setItem(OWNERNAME_KEY, name);
    }
  },
  /**
   * Get the cleaner OID (for cleaner-specific views)
   */
  get cleanerOid() {
    return cleanerOid;
  },
  /**
   * Set the cleaner OID
   */
  setCleanerOid(id) {
    cleanerOid = id;
    if (typeof localStorage !== "undefined") {
      localStorage.setItem(CLEANERID_KEY, id.toString());
    }
  },
  /**
   * Get the email
   */
  get email() {
    return email;
  },
  /**
   * Set the email
   */
  setEmail(emailAddr) {
    email = emailAddr;
    if (typeof localStorage !== "undefined") {
      localStorage.setItem(EMAIL_KEY, emailAddr);
    }
  },
  /**
   * Check if user is authenticated
   */
  get isAuthenticated() {
    return uuid.length > 0;
  },
  /**
   * Set the session UUID
   * @param newUuid - The UUID from backend login
   * @param persist - Override persistence setting (optional)
   */
  setUUID(newUuid, persist) {
    uuid = newUuid;
    if (persist ?? persistToStorage) {
      if (typeof localStorage !== "undefined") {
        localStorage.setItem(STORAGE_KEY, newUuid);
      }
    }
  },
  /**
   * Clear the session (logout)
   */
  clear() {
    uuid = "";
    username = "";
    userOid = 0;
    ownerOid = 0;
    ownerName = "";
    cleanerOid = 0;
    email = "";
    if (typeof localStorage !== "undefined") {
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
  restore() {
    if (typeof localStorage === "undefined") return false;
    const saved = localStorage.getItem(STORAGE_KEY);
    if (saved && saved.length > 0) {
      uuid = saved;
      const savedUsername = localStorage.getItem(USERNAME_KEY);
      if (savedUsername && savedUsername.length > 0) {
        username = savedUsername;
      }
      const savedUserId = localStorage.getItem(USERID_KEY);
      if (savedUserId) {
        userOid = parseInt(savedUserId) || 0;
      }
      const savedOwnerId = localStorage.getItem(OWNERID_KEY);
      if (savedOwnerId) {
        ownerOid = parseInt(savedOwnerId) || 0;
      }
      const savedOwnerName = localStorage.getItem(OWNERNAME_KEY);
      if (savedOwnerName) {
        ownerName = savedOwnerName;
      }
      const savedCleanerId = localStorage.getItem(CLEANERID_KEY);
      if (savedCleanerId) {
        cleanerOid = parseInt(savedCleanerId) || 0;
      }
      const savedEmail = localStorage.getItem(EMAIL_KEY);
      if (savedEmail) {
        email = savedEmail;
      }
      const savedIsAdmin = localStorage.getItem(IS_ADMIN_KEY);
      if (savedIsAdmin) {
        isAdmin = savedIsAdmin === "true";
      }
      const savedAdminType = localStorage.getItem(ADMIN_TYPE_KEY);
      if (savedAdminType) {
        adminType = savedAdminType;
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
  async storeCredentials(username2, password) {
    if (typeof localStorage === "undefined") return;
    try {
      const data = JSON.stringify({ username: username2, password });
      const encrypted = await encryptData(data);
      localStorage.setItem(CREDENTIALS_KEY, encrypted);
    } catch (error) {
      console.error("Failed to store credentials:", error);
    }
  },
  /**
   * Get stored credentials (decrypted)
   * @returns Object with username and password, or null if not stored
   */
  async getStoredCredentials() {
    if (typeof localStorage === "undefined") return null;
    const encrypted = localStorage.getItem(CREDENTIALS_KEY);
    if (!encrypted) return null;
    try {
      const decrypted = await decryptData(encrypted);
      return JSON.parse(decrypted);
    } catch (error) {
      console.error("Failed to retrieve credentials:", error);
      localStorage.removeItem(CREDENTIALS_KEY);
      return null;
    }
  },
  /**
   * Clear stored credentials
   */
  clearCredentials() {
    if (typeof localStorage !== "undefined") {
      localStorage.removeItem(CREDENTIALS_KEY);
    }
  },
  /**
   * Check if credentials are stored
   */
  hasStoredCredentials() {
    if (typeof localStorage === "undefined") return false;
    return !!localStorage.getItem(CREDENTIALS_KEY);
  },
  /**
   * Configure persistence behavior
   * @param enabled - true to persist to localStorage, false for memory only
   */
  setPersistence(enabled) {
    persistToStorage = enabled;
    if (!enabled) {
      if (typeof localStorage !== "undefined") {
        localStorage.removeItem(STORAGE_KEY);
      }
    }
  },
  /**
   * Get current persistence setting
   */
  get isPersistent() {
    return persistToStorage;
  },
  /**
   * Get the preferred language
   */
  getLanguage() {
    if (typeof localStorage !== "undefined") {
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
  setLanguage(lang) {
    preferredLanguage = lang;
    if (typeof localStorage !== "undefined") {
      localStorage.setItem(LANGUAGE_KEY, lang);
    }
  },
  /**
   * Restore language preference from localStorage
   */
  restoreLanguage() {
    if (typeof localStorage === "undefined") return preferredLanguage;
    const saved = localStorage.getItem(LANGUAGE_KEY);
    if (saved && saved.length > 0) {
      preferredLanguage = saved;
      return saved;
    }
    return preferredLanguage;
  }
};
export {
  session as s
};
