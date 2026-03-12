<script>
	import { userActions, notificationActions } from '$lib/stores.svelte.js';
	import { goto } from '$app/navigation';

	// Svelte 5: Use $state for all reactive variables
	let name = $state('');
	let email = $state('');
	let password = $state('');
	let confirmPassword = $state('');
	let isLoading = $state(false);

	async function handleSignup() {
		if (!name || !email || !password || !confirmPassword) {
			notificationActions.error('Please fill in all fields');
			return;
		}

		if (password !== confirmPassword) {
			notificationActions.error('Passwords do not match');
			return;
		}

		if (password.length < 6) {
			notificationActions.error('Password must be at least 6 characters long');
			return;
		}

		isLoading = true;

		try {
			// TODO: Replace with actual signup API call
			// For now, simulate a successful signup
			await new Promise((resolve) => setTimeout(resolve, 1500));

			// Simulate successful signup and login
			userActions.login({
				id: '1',
				name: name,
				email: email,
				role: 'user'
			});

			// Set a cookie for server-side protection
			document.cookie = 'user_id=1; path=/; max-age=86400';

			notificationActions.success('Account created successfully!');
			goto('/');
		} catch (error) {
			notificationActions.error('Signup failed. Please try again.');
		} finally {
			isLoading = false;
		}
	}
</script>

<div class="signup-container">
	<div class="signup-card">
		<div class="signup-header">
			<h2>Create Your Account</h2>
			<p>Join CleaningScheduler to manage your cleaning schedules</p>
		</div>

		<form class="signup-form">
			<div class="form-group">
				<label for="name">Full Name</label>
				<input
					type="text"
					id="name"
					bind:value={name}
					placeholder="Enter your full name"
					required
					disabled={isLoading}
				/>
			</div>

			<div class="form-group">
				<label for="email">Email Address</label>
				<input
					type="email"
					id="email"
					bind:value={email}
					placeholder="Enter your email"
					required
					disabled={isLoading}
				/>
			</div>

			<div class="form-group">
				<label for="password">Password</label>
				<input
					type="password"
					id="password"
					bind:value={password}
					placeholder="Create a password"
					required
					disabled={isLoading}
				/>
			</div>

			<div class="form-group">
				<label for="confirmPassword">Confirm Password</label>
				<input
					type="password"
					id="confirmPassword"
					bind:value={confirmPassword}
					placeholder="Confirm your password"
					required
					disabled={isLoading}
				/>
			</div>

			<button type="submit" class="btn btn-primary signup-btn" disabled={isLoading}>
				{isLoading ? 'Creating Account...' : 'Create Account'}
			</button>

			<div class="signup-footer">
				<p>Already have an account? <a href="/login">Login here</a></p>
			</div>
		</form>
	</div>
</div>

<style>
	.signup-container {
		display: flex;
		align-items: center;
		justify-content: center;
		min-height: 80vh;
		padding: 2rem;
	}

	.signup-card {
		background: white;
		padding: 3rem;
		border-radius: 12px;
		box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
		width: 100%;
		max-width: 400px;
	}

	.signup-header {
		text-align: center;
		margin-bottom: 2rem;
	}

	.signup-header h2 {
		margin: 0 0 0.5rem 0;
		color: var(--text-color);
		font-size: 1.8rem;
	}

	.signup-header p {
		color: var(--muted-color);
		font-size: 0.9rem;
		margin: 0;
	}

	.signup-form {
		display: flex;
		flex-direction: column;
		gap: 1.5rem;
	}

	.form-group {
		display: flex;
		flex-direction: column;
		gap: 0.5rem;
	}

	.form-group label {
		font-size: 0.9rem;
		font-weight: 600;
		color: var(--text-color);
	}

	.form-group input {
		padding: 0.75rem;
		border: 1px solid var(--border-color);
		border-radius: 8px;
		font-size: 1rem;
		transition: all 0.2s;
		background: var(--input-bg, #f8f9fa);
		color: var(--text-color);
	}

	.form-group input:focus {
		outline: none;
		border-color: var(--primary-color, #3498db);
		box-shadow: 0 0 0 3px rgba(52, 152, 219, 0.1);
	}

	.form-group input:disabled {
		background: var(--row-hover-bg);
		cursor: not-allowed;
		opacity: 0.6;
	}

	.signup-btn {
		padding: 1rem;
		font-size: 1rem;
		font-weight: 600;
		border-radius: 8px;
		cursor: pointer;
		transition: all 0.2s;
		margin-top: 1rem;
	}

	.signup-btn:disabled {
		background: var(--row-hover-bg);
		cursor: not-allowed;
		opacity: 0.6;
	}

	.signup-footer {
		text-align: center;
		margin-top: 1rem;
	}

	.signup-footer p {
		color: var(--muted-color);
		font-size: 0.9rem;
		margin: 0;
	}

	.signup-footer a {
		color: var(--primary-color, #3498db);
		text-decoration: none;
		font-weight: 600;
	}

	.signup-footer a:hover {
		text-decoration: underline;
	}

	/* Responsive design */
	@media (max-width: 768px) {
		.signup-card {
			padding: 2rem 1.5rem;
		}

		.signup-header h2 {
			font-size: 1.5rem;
		}
	}
</style>
