// User type for session management
export interface User {
	id: number;
	username: string;
	email: string;
	active: boolean;
	emailVerified: boolean;
	firstName?: string;
	lastName?: string;
}

// Notification types
export interface Notification {
	id: number;
	message: string;
	type: 'success' | 'error' | 'warning' | 'info';
}
