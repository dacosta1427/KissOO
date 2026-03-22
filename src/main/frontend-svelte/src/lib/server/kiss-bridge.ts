/**
 * Kiss Backend Bridge (Server-Only)
 * 
 * This module provides a bridge between SvelteKit remote functions
 * and the Kiss framework's Server.call() mechanism.
 * 
 * IMPORTANT: This file is server-only and should not be imported
 * in client-side code. Remote functions automatically run on the server.
 */

import { session } from '$lib/state/session.svelte';

// Import the existing Server class
// Note: Server.ts is designed for client-side use, but we can use its
// call mechanism on the server by ensuring UUID is properly set

/**
 * Kiss API Response type matching Kiss backend format
 */
export interface KissResponse<T = any> {
	_Success: boolean;
	_ErrorMessage?: string;
	_ErrorCode?: number;
	uuid?: string;
	data?: T;
	rows?: T[];
	success?: boolean;
	id?: number;
	[key: string]: any;
}

/**
 * Make a call to the Kiss backend using Server.call() mechanism
 * 
 * This wraps the existing Server.call() to work with remote functions.
 * The UUID is automatically included from the session state.
 * 
 * @param service - Service class name (e.g., 'services.Users' or '' for built-in)
 * @param method - Method name to call
 * @param params - Parameters to send
 * @returns Promise with the response data
 */
export async function kissCall<T = any>(
	service: string,
	method: string,
	params: Record<string, any> = {}
): Promise<KissResponse<T>> {
	// Dynamically import Server to ensure it's only loaded on server
	const { Server } = await import('$lib/services/Server');
	
	// Ensure UUID is set in Server class (sync with our session state)
	if (session.uuid) {
		Server.setUUID(session.uuid);
	}
	
	// Make the call using existing Server.call() mechanism
	// Server.call automatically adds _uuid, _method, _class to the payload
	const response = await Server.call(service, method, params);
	
	return response as KissResponse<T>;
}

/**
 * Perform login with Kiss backend
 * 
 * @param username - Username (will be lowercased)
 * @param password - Password
 * @returns Login response with UUID on success
 */
export async function kissLogin(username: string, password: string): Promise<KissResponse> {
	const res = await kissCall('', 'Login', {
		username: username.toLowerCase(),
		password: password
	});
	
	if (res._Success && res.uuid) {
		// Store UUID in our session state
		session.setUUID(res.uuid);
		// Also sync with Server class
		const { Server } = await import('$lib/services/Server');
		Server.setUUID(res.uuid);
	}
	
	return res;
}

/**
 * Perform logout from Kiss backend
 */
export async function kissLogout(): Promise<void> {
	try {
		await kissCall('', 'Logout', {});
	} catch (e) {
		// Ignore errors on logout
		console.warn('Logout call failed:', e);
	}
	
	// Clear session state
	session.clear();
	
	// Clear Server class UUID
	const { Server } = await import('$lib/services/Server');
	Server.setUUID('');
}

/**
 * Check if current session is still valid
 */
export async function kissCheckLogin(): Promise<boolean> {
	if (!session.uuid) return false;
	
	try {
		const res = await kissCall('', 'checkLogin');
		return res._Success === true;
	} catch (e) {
		return false;
	}
}

/**
 * Get the current session UUID
 */
export function getSessionUUID(): string {
	return session.uuid;
}

/**
 * Check if user is authenticated
 */
export function isAuthenticated(): boolean {
	return session.isAuthenticated();
}
