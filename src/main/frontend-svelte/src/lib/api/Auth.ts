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
  _ErrorMessage?: string;
  _ErrorCode?: number;
}

/**
 * Login to Kiss backend
 * @param username - Username (will be lowercased)
 * @param password - Password
 * @returns Login result with UUID on success
 */
export async function login(username: string, password: string): Promise<LoginResult> {
  const res = await Server.call('', 'Login', {
    username: username.toLowerCase(),
    password: password
  }) as LoginResult;
  
  if (res._Success && res.uuid) {
    session.setUUID(res.uuid);
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
}

/**
 * Sign up a new user and auto-login
 * @param username - Username
 * @param password - Password
 * @returns Login result
 */
export async function signup(username: string, password: string): Promise<LoginResult> {
  const res = await Server.call('services.Users', 'addRecord', {
    userName: username.toLowerCase(),
    userPassword: password,
    userActive: 'Y'
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
