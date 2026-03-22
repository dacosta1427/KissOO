/**
 * Users.ts - User Management API Module
 * 
 * Simple functions that call Server.call() directly.
 * Used by Svelte 5 components for user CRUD operations.
 */

import { Server } from '$lib/services/Server';

export interface User {
  id: number;
  userName: string;
  userPassword: string;
  userActive: 'Y' | 'N';
}

export interface ApiResult {
  _Success?: boolean;
  success?: boolean;
  error?: string;
  _ErrorMessage?: string;
  id?: number;
}

/**
 * Get all users from backend
 * @returns Array of users
 */
export async function getUsers(): Promise<User[]> {
  const res = await Server.call('services.Users', 'getRecords', {});
  return res.rows || [];
}

/**
 * Add a new user
 * @param userName - Username
 * @param userPassword - Password
 * @returns API result
 */
export async function addUser(userName: string, userPassword: string): Promise<ApiResult> {
  const res = await Server.call('services.Users', 'addRecord', {
    userName: userName.toLowerCase(),
    userPassword,
    userActive: 'Y'
  });
  
  return {
    success: res._Success || res.success,
    error: res._ErrorMessage || res.error,
    id: res.id
  };
}

/**
 * Delete a user by ID
 * @param id - User ID (oid)
 * @returns API result
 */
export async function deleteUser(id: number): Promise<ApiResult> {
  const res = await Server.call('services.Users', 'deleteRecord', { id });
  
  return {
    success: res._Success || res.success,
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
  id: number,
  userName: string,
  userPassword: string,
  userActive: 'Y' | 'N'
): Promise<ApiResult> {
  const res = await Server.call('services.Users', 'updateRecord', {
    id,
    userName: userName.toLowerCase(),
    userPassword,
    userActive
  });
  
  return {
    success: res._Success || res.success,
    error: res._ErrorMessage || res.error
  };
}
