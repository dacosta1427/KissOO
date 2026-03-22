/**
 * Session state management using Svelte 5 runes
 * 
 * This module provides a reactive session state that works with
 * the Kiss framework's UUID-based authentication system.
 * 
 * The UUID is stored in memory only (matching Kiss backend behavior)
 * and is included with every Server.call() request.
 */

interface SessionUser {
	uuid: string;
	[key: string]: any;
}

/**
 * Creates a reactive session state using Svelte 5 runes
 */
function createSession() {
	// Session UUID (matches Kiss backend's UserData UUID)
	let uuid = $state<string>('');
	
	// User data (optional, for future use)
	let user = $state<SessionUser | null>(null);
	
	return {
		/**
		 * Get the current session UUID
		 */
		get uuid(): string {
			return uuid;
		},
		
		/**
		 * Get the current user data
		 */
		get user(): SessionUser | null {
			return user;
		},
		
		/**
		 * Set the session UUID (called after successful login)
		 */
		setUUID(newUuid: string): void {
			uuid = newUuid;
		},
		
		/**
		 * Set user data
		 */
		setUser(newUser: SessionUser | null): void {
			user = newUser;
		},
		
		/**
		 * Clear the session (called on logout)
		 */
		clear(): void {
			uuid = '';
			user = null;
		},
		
		/**
		 * Check if user is authenticated
		 */
		isAuthenticated(): boolean {
			return uuid.length > 0;
		}
	};
}

/**
 * Global session instance
 * 
 * Usage:
 * ```typescript
 * import { session } from '$lib/state/session.svelte';
 * 
 * // After login
 * session.setUUID(res.uuid);
 * 
 * // Check auth
 * if (session.isAuthenticated()) { ... }
 * 
 * // On logout
 * session.clear();
 * ```
 */
export const session = createSession();
