/**
 * Authentication Remote Functions
 * 
 * These remote functions handle login, logout, and signup operations.
 * They use the kiss-bridge to communicate with the Kiss backend.
 * 
 * Usage in components:
 * ```svelte
 * <script>
 *   import { loginForm } from '$lib/remote/auth.remote';
 * </script>
 * 
 * <form {...loginForm}>
 *   <input {...loginForm.fields.username.as('text')} />
 *   <input {...loginForm.fields.password.as('password')} />
 *   <button type="submit">Login</button>
 * </form>
 * ```
 */

import { query, form } from '$app/server';
import { kissCall, kissLogin, kissLogout, kissCheckLogin } from '$lib/server/kiss-bridge';
import { redirect } from '@sveltejs/kit';
import * as v from 'valibot';

// ===== Validation Schemas =====

const loginSchema = v.object({
	username: v.pipe(
		v.string(),
		v.nonEmpty('Username is required')
	),
	password: v.pipe(
		v.string(),
		v.nonEmpty('Password is required')
	)
});

const signupSchema = v.object({
	username: v.pipe(
		v.string(),
		v.nonEmpty('Username is required'),
		v.minLength(3, 'Username must be at least 3 characters')
	),
	password: v.pipe(
		v.string(),
		v.nonEmpty('Password is required'),
		v.minLength(3, 'Password must be at least 3 characters')
	),
	confirmPassword: v.string()
});

// ===== Remote Queries =====

/**
 * Check if user is currently authenticated
 * Returns the auth status or null if not authenticated
 */
export const checkAuth = query(async () => {
	const isAuthenticated = await kissCheckLogin();
	return isAuthenticated ? { authenticated: true } : null;
});

// ===== Remote Forms =====

/**
 * Login form
 * 
 * Handles user authentication with the Kiss backend.
 * On success, stores UUID and redirects to home page.
 * 
 * Usage:
 * ```svelte
 * <form {...loginForm}>
 *   <input {...loginForm.fields.username.as('text')} />
 *   <input {...loginForm.fields.password.as('password')} />
 *   <button type="submit">Login</button>
 * </form>
 * ```
 */
export const loginForm = form(loginSchema, async (data, { issue }) => {
	try {
		const res = await kissLogin(data.username, data.password);
		
		if (!res._Success) {
			issue({
				message: res._ErrorMessage || 'Invalid username or password'
			});
			return;
		}
		
		// Redirect to home page on successful login
		redirect(303, '/');
	} catch (e) {
		// Check if it's a redirect (which is expected)
		if (e && typeof e === 'object' && 'status' in e && 'location' in e) {
			throw e; // Re-throw redirect
		}
		
		issue({
			message: 'Login failed. Please try again.'
		});
	}
});

/**
 * Signup form
 * 
 * Creates a new user account and automatically logs in.
 * 
 * Usage:
 * ```svelte
 * <form {...signupForm}>
 *   <input {...signupForm.fields.username.as('text')} />
 *   <input {...signupForm.fields.password.as('password')} />
 *   <input {...signupForm.fields.confirmPassword.as('password')} />
 *   <button type="submit">Sign Up</button>
 * </form>
 * ```
 */
export const signupForm = form(signupSchema, async (data, { issue }) => {
	// Validate password confirmation
	if (data.password !== data.confirmPassword) {
		issue({
			path: ['confirmPassword'],
			message: 'Passwords do not match'
		});
		return;
	}
	
	try {
		// Create the user account
		const res = await kissCall('services.Users', 'addRecord', {
			userName: data.username.toLowerCase(),
			userPassword: data.password,
			userActive: 'Y'
		});
		
		if (!res._Success && !res.success) {
			issue({
				message: res._ErrorMessage || res.error || 'Signup failed'
			});
			return;
		}
		
		// Auto-login after successful signup
		const loginRes = await kissLogin(data.username, data.password);
		
		if (loginRes._Success) {
			redirect(303, '/');
		} else {
			// Account created but login failed, redirect to login page
			redirect(303, '/login');
		}
	} catch (e) {
		// Check if it's a redirect
		if (e && typeof e === 'object' && 'status' in e && 'location' in e) {
			throw e; // Re-throw redirect
		}
		
		issue({
			message: 'Signup failed. Please try again.'
		});
	}
});

/**
 * Logout action
 * 
 * Logs out the current user and redirects to login page.
 * 
 * Usage:
 * ```svelte
 * <form {...logoutAction}>
 *   <button type="submit">Logout</button>
 * </form>
 * ```
 */
export const logoutAction = form(async (_, { issue }) => {
	try {
		await kissLogout();
		redirect(303, '/login');
	} catch (e) {
		// Check if it's a redirect
		if (e && typeof e === 'object' && 'status' in e && 'location' in e) {
			throw e; // Re-throw redirect
		}
		
		// Even if logout fails on backend, clear local session
		redirect(303, '/login');
	}
});
