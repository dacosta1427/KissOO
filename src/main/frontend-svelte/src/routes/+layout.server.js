/**
 * Server-side layout for protecting routes
 * Redirects unauthenticated users to login
 */

import { redirect } from '@sveltejs/kit';

export async function load({ url, cookies }) {
	// Get the current path
	const currentPath = url.pathname;

	// Public routes that don't require authentication
	const publicRoutes = ['/', '/login', '/signup'];

	// Check if this is a protected route
	const isProtectedRoute =
		!publicRoutes.includes(currentPath) &&
		!currentPath.startsWith('/login') &&
		!currentPath.startsWith('/signup');

	if (isProtectedRoute) {
		// For now, we'll check if user is "logged in" via a simple cookie
		// In a real app, this would check session/auth tokens
		const isLoggedIn = cookies.get('user_id');

		if (!isLoggedIn) {
			throw redirect(302, '/login');
		}
	}

	return {};
}
