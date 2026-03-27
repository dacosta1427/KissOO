/**
 * Auth.ts - Authentication API Module
 * 
 * Simple functions that call Server.call() directly.
 * Used by Svelte 5 components for authentication.
 */

import { Server } from '$lib/services/Server';
import { session } from '$lib/state/session';

export interface LoginResult {
  _Success: boolean;
  uuid?: string;
  userId?: number;
  ownerId?: number;
  ownerName?: string;
  cleanerId?: number;
  email?: string;
  preferredLanguage?: string;
  _ErrorMessage?: string;
  _ErrorCode?: number;
}

/**
 * Login to Kiss backend
 * @param username - Username (will be lowercased)
 * @param password - Password
 * @returns Login result with UUID on success
 */
export async function login(usernameInput: string, password: string): Promise<LoginResult> {
  const res = await Server.call('', 'Login', {
    username: usernameInput.toLowerCase(),
    password: password
  }) as LoginResult;
  
  if (res._Success && res.uuid) {
    session.setUUID(res.uuid);
    session.setUsername(usernameInput.toLowerCase());
    Server.setUUID(res.uuid);
    
    // Store user info from response
    if (res.userId) {
      session.setUserId(res.userId);
    }
    if (res.ownerId) {
      session.setOwnerId(res.ownerId);
    }
    if (res.ownerName) {
      session.setOwnerName(res.ownerName);
    }
    if (res.cleanerId) {
      session.setCleanerId(res.cleanerId);
    }
    if (res.email) {
      session.setEmail(res.email);
    }
    if (res.preferredLanguage) {
      session.setLanguage(res.preferredLanguage);
    }
  }
  
  return res;
}

/**
 * Logout from Kiss backend
 */
export async function logout(): Promise<void> {
  try {
    await Server.call('', 'Logout', {});
  } catch (e) {
    // Ignore errors on logout
  }
  session.clear();
  session.clearCredentials(); // Clear encrypted credentials
}

/**
 * Sign up a new user and auto-login
 * @param username - Username
 * @param password - Password
 * @returns Login result
 */
export async function signup(username: string, password: string, email: string = '', name: string = ''): Promise<LoginResult> {
  const res = await Server.call('services.Users', 'addRecord', {
    userName: username.toLowerCase(),
    userPassword: password,
    userActive: 'Y',
    email: email || `${username}@example.com`,
    name: name || username,
    phone: '',
    address: ''
  });
  
  // Check for success (backend returns _Success or success)
  if (res._Success || res.success) {
    // Auto-login after successful signup
    return login(username, password);
  }
  
  return {
    _Success: false,
    _ErrorMessage: res._ErrorMessage || res.error || 'Signup failed'
  };
}

/**
 * Check if user is currently authenticated
 */
export function isAuthenticated(): boolean {
  return session.isAuthenticated;
}

/**
 * Initialize backend URL based on current environment
 */
export function initBackend(): void {
  if (typeof window === 'undefined') return;
  
  if (window.location.protocol === 'file:') {
    Server.setURL('http://localhost:8080');
  } else {
    const port = parseInt(window.location.port || '0');
    if (port === 5173 || port === 5174) {
      Server.setURL('http://localhost:8080');
    } else {
      Server.setURL(window.location.origin);
    }
  }
}
