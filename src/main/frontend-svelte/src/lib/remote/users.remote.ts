/**
 * User Management Remote Functions
 * 
 * These remote functions handle CRUD operations for user management.
 * They use the kiss-bridge to communicate with the Kiss backend's Users service.
 * 
 * Usage in components:
 * ```svelte
 * <script>
 *   import { getUsers, addUserForm, deleteUserForm } from '$lib/remote/users.remote';
 *   const users = getUsers();
 * </script>
 * 
 * {#await users}
 *   <p>Loading...</p>
 * {:then userList}
 *   {#each userList as user}
 *     <p>{user.userName}</p>
 *   {/each}
 * {/await}
 * ```
 */

import { query, form } from '$app/server';
import { kissCall } from '$lib/server/kiss-bridge';
import * as v from 'valibot';

// ===== Types =====

/**
 * User interface matching Kiss backend response
 */
export interface User {
	id: number;
	userName: string;
	userPassword: string;
	userActive: 'Y' | 'N';
}

/**
 * Parameters for adding a new user
 */
export interface AddUserParams {
	userName: string;
	userPassword: string;
	userActive?: 'Y' | 'N';
}

/**
 * Parameters for updating a user
 */
export interface UpdateUserParams extends AddUserParams {
	id: number;
}

// ===== Validation Schemas =====

const addUserSchema = v.object({
	userName: v.pipe(
		v.string(),
		v.nonEmpty('Username is required'),
		v.minLength(3, 'Username must be at least 3 characters')
	),
	userPassword: v.pipe(
		v.string(),
		v.nonEmpty('Password is required'),
		v.minLength(3, 'Password must be at least 3 characters')
	)
});

const deleteUserSchema = v.object({
	id: v.pipe(
		v.number(),
		v.minValue(1, 'Invalid user ID')
	)
});

const updateUserSchema = v.object({
	id: v.pipe(
		v.number(),
		v.minValue(1, 'Invalid user ID')
	),
	userName: v.pipe(
		v.string(),
		v.nonEmpty('Username is required')
	),
	userPassword: v.pipe(
		v.string(),
		v.minLength(3, 'Password must be at least 3 characters')
	),
	userActive: v.picklist(['Y', 'N'])
});

// ===== Remote Queries =====

/**
 * Get all users from the backend
 * 
 * Returns a promise that resolves to an array of User objects.
 * The query is cached and can be refreshed with .refresh().
 * 
 * Usage:
 * ```svelte
 * const users = getUsers();
 * {#await users}
 *   Loading...
 * {:then userList}
 *   {#each userList as user}
 *     {user.userName}
 *   {/each}
 * {/await}
 * ```
 */
export const getUsers = query(async (): Promise<User[]> => {
	const res = await kissCall('services.Users', 'getRecords');
	
	if (!res._Success) {
		throw new Error(res._ErrorMessage || 'Failed to fetch users');
	}
	
	return res.rows || [];
});

/**
 * Get a single user by ID
 * 
 * @param id - User ID to fetch
 * @returns User object or null if not found
 */
export const getUser = query(v.number(), async (id: number): Promise<User | null> => {
	const users = await getUsers();
	return users.find(u => u.id === id) || null;
});

// ===== Remote Forms =====

/**
 * Add user form
 * 
 * Creates a new user account in the Kiss backend.
 * On success, automatically refreshes the users list.
 * 
 * Usage:
 * ```svelte
 * <form {...addUserForm}>
 *   <input {...addUserForm.fields.userName.as('text')} />
 *   <input {...addUserForm.fields.userPassword.as('password')} />
 *   <button type="submit">Add User</button>
 * </form>
 * ```
 */
export const addUserForm = form(addUserSchema, async (data, { issue }) => {
	try {
		const res = await kissCall('services.Users', 'addRecord', {
			userName: data.userName.toLowerCase(),
			userPassword: data.userPassword,
			userActive: 'Y'
		});
		
		if (!res._Success && !res.success) {
			issue({
				message: res._ErrorMessage || res.error || 'Failed to add user'
			});
			return;
		}
		
		// Refresh the users list
		getUsers().refresh();
	} catch (e) {
		issue({
			message: 'Failed to add user. Please try again.'
		});
	}
});

/**
 * Delete user form
 * 
 * Deletes a user from the Kiss backend.
 * On success, automatically refreshes the users list.
 * 
 * Usage:
 * ```svelte
 * <form {...deleteUserForm}>
 *   <input type="hidden" name="id" value={user.id} />
 *   <button type="submit">Delete</button>
 * </form>
 * ```
 */
export const deleteUserForm = form(deleteUserSchema, async (data, { issue }) => {
	try {
		const res = await kissCall('services.Users', 'deleteRecord', {
			id: data.id
		});
		
		if (!res._Success && !res.success) {
			issue({
				message: res._ErrorMessage || res.error || 'Failed to delete user'
			});
			return;
		}
		
		// Refresh the users list
		getUsers().refresh();
	} catch (e) {
		issue({
			message: 'Failed to delete user. Please try again.'
		});
	}
});

/**
 * Update user form
 * 
 * Updates an existing user in the Kiss backend.
 * On success, automatically refreshes the users list.
 * 
 * Usage:
 * ```svelte
 * <form {...updateUserForm}>
 *   <input type="hidden" name="id" value={user.id} />
 *   <input {...updateUserForm.fields.userName.as('text')} />
 *   <input {...updateUserForm.fields.userPassword.as('password')} />
 *   <select {...updateUserForm.fields.userActive.as('select')}>
 *     <option value="Y">Active</option>
 *     <option value="N">Inactive</option>
 *   </select>
 *   <button type="submit">Update</button>
 * </form>
 * ```
 */
export const updateUserForm = form(updateUserSchema, async (data, { issue }) => {
	try {
		const res = await kissCall('services.Users', 'updateRecord', {
			id: data.id,
			userName: data.userName.toLowerCase(),
			userPassword: data.userPassword,
			userActive: data.userActive
		});
		
		if (!res._Success && !res.success) {
			issue({
				message: res._ErrorMessage || res.error || 'Failed to update user'
			});
			return;
		}
		
		// Refresh the users list
		getUsers().refresh();
	} catch (e) {
		issue({
			message: 'Failed to update user. Please try again.'
		});
	}
});
