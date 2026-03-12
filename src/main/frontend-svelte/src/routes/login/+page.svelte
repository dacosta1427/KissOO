<script>
	import { userActions, notificationActions } from '$lib/stores.svelte.js';
	import { goto } from '$app/navigation';

	// Svelte 5: Use $state for all reactive variables
	let email = $state('');
	let password = $state('');
	let isLoading = $state(false);

	async function handleLogin() {
		if (!email || !password) {
			notificationActions.error('Please fill in all fields');
			return;
		}

		isLoading = true;

		try {
			// TODO: Replace with actual login API call
			// For now, simulate a successful login
			await new Promise((resolve) => setTimeout(resolve, 1000));

			// Simulate successful login
			userActions.login({
				id: '1',
				name: 'Demo User',
				email: email,
				role: 'admin'
			});

			// Set a cookie for server-side protection
			document.cookie = 'user_id=1; path=/; max-age=86400';

			notificationActions.success('Login successful!');

			// Use goto for navigation
			goto('/');
		} catch (error) {
			notificationActions.error('Login failed. Please try again.');
		} finally {
			isLoading = false;
		}
	}
</script>

<div class="login-container">
	<div class="login-card">
		<div class="login-header">
			<h2>Login to CleaningScheduler</h2>
			<p>Access your dashboard and manage your cleaning schedules</p>
		</div>

		<form class="login-form">
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
					placeholder="Enter your password"
					required
					disabled={isLoading}
				/>
			</div>

			<button type="submit" class="btn btn-primary login-btn" disabled={isLoading}>
				{isLoading ? 'Logging in...' : 'Login'}
			</button>

			<div class="login-footer">
				<p>Don't have an account? <a href="/signup">Sign up here</a></p>
			</div>
		</form>
	</div>
</div>

<style>
	.login-container {
		display: flex;
		align-items: center;
		justify-content: center;
		min-height: 80vh;
		padding: 2rem;
	}

	.login-card {
		background: white;
		padding: 3rem;
		border-radius: 12px;
		box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
		width: 100%;
		max-width: 400px;
	}

	.login-header {
		text-align: center;
		margin-bottom: 2rem;
	}

	.login-header h2 {
		margin: 0 0 0.5rem 0;
		color: var(--text-color);
		font-size: 1.8rem;
	}

	.login-header p {
		color: var(--muted-color);
		font-size: 0.9rem;
		margin: 0;
	}

	.login-form {
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

	.login-btn {
		padding: 1rem;
		font-size: 1rem;
		font-weight: 600;
		border-radius: 8px;
		cursor: pointer;
		transition: all 0.2s;
		margin-top: 1rem;
	}

	.login-btn:disabled {
		background: var(--row-hover-bg);
		cursor: not-allowed;
		opacity: 0.6;
	}

	.login-footer {
		text-align: center;
		margin-top: 1rem;
	}

	.login-footer p {
		color: var(--muted-color);
		font-size: 0.9rem;
		margin: 0;
	}

	.login-footer a {
		color: var(--primary-color, #3498db);
		text-decoration: none;
		font-weight: 600;
	}

	.login-footer a:hover {
		text-decoration: underline;
	}

	/* Responsive design */
	@media (max-width: 768px) {
		.login-card {
			padding: 2rem 1.5rem;
		}

		.login-header h2 {
			font-size: 1.5rem;
		}
	}
</style>
