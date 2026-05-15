/**
 * Users.ts - User Management API Module
 * 
 * Simple functions that call Server.call() directly.
 * Used by Svelte 5 components for user CRUD operations.
 */

import { Server } from '$lib/services/Server';

export interface User {
  oid: number;
  userName: string;
  userPassword: string;
  canLogin: boolean;
  emailVerified: boolean;
  email: string;
  actorType?: string;  // 'Owner', 'Cleaner', or null
}

export interface ApiResult {
  _Success?: boolean;
  success?: boolean;
  error?: string;
  _ErrorMessage?: string;
  oid?: number;
}

/**
 * Get all users from backend
 * @returns Array of users
 */
export async function getUsers(): Promise<User[]> {
  console.log('[Users.ts] getUsers called, Server.uuid:', (Server as any).uuid);
  const res = await Server.call('services.Users', 'getUsers', {});
  console.log('[Users.ts] getUsers response:', res);
  return res.rows || [];
}

/**
 * Add a new user
 * @param userName - Username
 * @param userPassword - Password
 * @returns API result
 */
export async function addUser(userName: string, userPassword: string): Promise<ApiResult> {
  console.log('[Users.ts] addUser called:', userName);
  const res = await Server.call('services.Users', 'createUser', {
    userName: userName.toLowerCase(),
    userPassword,
    userActive: 'Y'
  });
  console.log('[Users.ts] addUser response:', res);
  
  return {
    success: res._Success ?? res.success ?? false,
    error: res._ErrorMessage || res.error,
    oid: res.oid
  };
}

/**
 * Delete a user by ID
 * @param id - User ID (oid)
 * @returns API result
 */
export async function deleteUser(oid: number): Promise<ApiResult> {
  const res = await Server.call('services.Users', 'deleteUser', { oid });
  
  return {
    success: res._Success ?? res.success ?? false,
    error: res._ErrorMessage || res.error
  };
}

/**
 * Update a user
 * @param id - User ID (oid)
 * @param userName - New username
 * @param userPassword - New password
 * @param userActive - Active status ('Y' or 'N')
 * @returns API result
 */
export async function updateUser(
  oid: number,
  userName: string,
  userPassword: string,
  userActive: 'Y' | 'N'
): Promise<ApiResult> {
  const res = await Server.call('services.Users', 'updateUser', {
    oid,
    userName: userName.toLowerCase(),
    userPassword,
    userActive
  });
  
  return {
    success: res._Success ?? res.success ?? false,
    error: res._ErrorMessage || res.error
  };
}

/**
 * Update user's preferred language
 * @param id - User ID (oid)
 * @param preferredLanguage - Language code (en, nl, de)
 * @returns API result
 */
export async function updateLanguage(
  oid: number,
  preferredLanguage: string
): Promise<ApiResult> {
  const res = await Server.call('services.Users', 'updateLanguage', {
    oid,
    preferredLanguage
  });
  
  return {
    success: res._Success ?? res.success ?? false,
    error: res._ErrorMessage || res.error
  };
}

/**
 * Toggle user login capability
 * @param id - User ID (oid)
 * @param canLogin - Whether user can login
 * @returns API result
 */
export async function toggleUserLogin(oid: number, canLogin: boolean): Promise<ApiResult> {
  const res = await Server.call('services.Users', 'toggleUserLogin', {
    oid,
    canLogin
  });
  
  return {
    success: res._Success ?? res.success ?? false,
    error: res._ErrorMessage || res.error
  };
}
