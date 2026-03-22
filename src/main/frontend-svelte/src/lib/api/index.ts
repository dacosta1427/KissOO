/**
 * Kiss API - Clean wrapper around Server.call() for backend communication
 * All functions call the Kiss backend at the configured URL
 */

import { Server } from '$lib/services/Server';

let initialized = false;

/**
 * Initialize the API - call this before making any API calls
 * @param baseUrl - The Kiss backend URL (e.g., 'http://localhost:8080')
 */
export function init(baseUrl: string): void {
  Server.setURL(baseUrl);
  initialized = true;
}

/**
 * Check if API is initialized
 */
export function isInitialized(): boolean {
  return initialized && !!Server.getURL();
}

/**
 * Get the current backend URL
 */
export function getBackendUrl(): string {
  return Server.getURL();
}

// ============ AUTHENTICATION ============

export interface LoginResult {
  _Success: boolean;
  uuid?: string;
  _ErrorMessage?: string;
}

export async function login(username: string, password: string): Promise<LoginResult> {
  const res = await Server.call('', 'Login', {
    username: username.toLowerCase(),
    password: password
  }) as LoginResult;
  
  if (res._Success && res.uuid) {
    Server.setUUID(res.uuid);
  }
  
  return res;
}

export async function logout(): Promise<void> {
  try {
    await Server.call('', 'Logout', {});
  } catch (e) {
    // Ignore errors on logout
  }
  Server.setUUID('');
}

// ============ USER MANAGEMENT ============

export interface User {
  id: number;
  userName: string;
  userPassword: string;
  userActive: 'Y' | 'N';
}

export interface GetUsersResult {
  _Success: boolean;
  rows?: User[];
  _ErrorMessage?: string;
}

export async function getUsers(): Promise<GetUsersResult> {
  return await Server.call('services.Users', 'getRecords', {}) as GetUsersResult;
}

export interface AddUserParams {
  userName: string;
  userPassword: string;
  userActive: 'Y' | 'N';
}

export interface AddUserResult {
  _Success: boolean;
  success?: boolean;
  id?: number;
  error?: string;
  _ErrorMessage?: string;
}

export async function addUser(params: AddUserParams): Promise<AddUserResult> {
  return await Server.call('services.Users', 'addRecord', params) as AddUserResult;
}

export interface UpdateUserParams extends AddUserParams {
  id: number;
}

export async function updateUser(params: UpdateUserParams): Promise<{ _Success: boolean; error?: string }> {
  return await Server.call('services.Users', 'updateRecord', params);
}

export async function deleteUser(id: number): Promise<{ _Success: boolean; error?: string }> {
  return await Server.call('services.Users', 'deleteRecord', { id });
}

// ============ GENERIC CALL ============

/**
 * Make a generic call to the backend
 * @param cls - Service class name (e.g., 'services.Users' or '' for built-in)
 * @param method - Method name to call
 * @param data - Data to send
 */
export async function call<T = any>(cls: string, method: string, data: any = {}): Promise<T> {
  return await Server.call(cls, method, data) as T;
}
