<script>
	import Navigation from '$lib/components/Navigation.svelte';
	import { userState, notificationsState, notificationActions, userActions } from '$lib/stores.svelte.js';
	import { goto } from '$app/navigation';
	import { page } from '$app/stores';

	let { children } = $props();

	let email = $state('');
	let password = $state('');
	let isLoading = $state(false);

	/** @type {typeof $derived} */
	let currentPath = $derived($page.url.pathname);
	let showLoginForm = $derived(!userState.value && currentPath === '/');

	async function handleLogin() {
		if (!email || !password) {
			notificationActions.error('Please fill in all fields');
			return;
		}

		isLoading = true;

		try {
			await new Promise((resolve) => setTimeout(resolve, 1000));

			userActions.login({
				id: '1',
				name: 'Demo User',
				email: email,
				role: 'admin'
			});

			document.cookie = 'user_id=1; path=/; max-age=86400';
			notificationActions.success('Login successful!');
			goto('/');
		} catch (error) {
			notificationActions.error('Login failed. Please try again.');
		} finally {
			isLoading = false;
		}
	}

	function removeNotification(id) {
		notificationActions.remove(id);
	}

	$effect(() => {
		if (notificationsState.value.length > 0) {
			const timer = setTimeout(() => {
				notificationActions.clear();
			}, 3000);
			return () => clearTimeout(timer);
		}
	});
</script>

<div class="layout">
	<Navigation />

	<main class="main-content">
		{#if userState.value}
			{@render children()}
		{:else if showLoginForm}
			<div class="auth-container">
				<div class="auth-card">
					<h2>Welcome to CleaningScheduler</h2>
					<p>Please log in to access your dashboard.</p>
					<form class="login-form" onsubmit={(e) => { e.preventDefault(); handleLogin(); }}>
						<div class="form-group">
							<label for="email">Email</label>
							<input type="email" id="email" bind:value={email} placeholder="Enter your email" required disabled={isLoading} />
						</div>
						<div class="form-group">
							<label for="password">Password</label>
							<input type="password" id="password" bind:value={password} placeholder="Enter your password" required disabled={isLoading} />
						</div>
						<button type="submit" class="btn btn-primary" disabled={isLoading}>
							{isLoading ? 'Logging in...' : 'Login'}
						</button>
					</form>
					<p class="signup-link">Don't have an account? <a href="/signup">Sign up here</a></p>
				</div>
			</div>
		{:else}
			{@render children()}
		{/if}
	</main>

	<div class="notifications">
		{#each notificationsState.value as notification}
			<div class="notification {notification.type}" role="alert">
				<span class="notification-message">{notification.message}</span>
				<button
					class="notification-close"
					aria-label="Close notification"
					onclick={() => removeNotification(notification.id)}
				>
					×
				</button>
			</div>
		{/each}
	</div>
</div>

<style>
	.layout {
		min-height: 100vh;
		display: flex;
		flex-direction: column;
	}

	.main-content {
		flex: 1;
		padding: 2rem;
		max-width: 1200px;
		margin: 0 auto;
		width: 100%;
	}

	.notifications {
		position: fixed;
		top: 0;
		right: 0;
		z-index: 1000;
		display: flex;
		flex-direction: column;
		gap: 1rem;
		padding: 1rem;
		pointer-events: none;
	}

	.notification {
		background: white;
		border: 1px solid var(--border-color);
		border-radius: 8px;
		padding: 1rem;
		box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
		display: flex;
		align-items: center;
		justify-content: space-between;
		gap: 1rem;
		min-width: 300px;
		pointer-events: auto;
		animation: slideIn 0.3s ease-out;
	}

	.notification.success {
		border-left: 4px solid var(--success-color, #2ecc71);
	}

	.notification.error {
		border-left: 4px solid var(--error-color, #e74c3c);
	}

	.notification.warning {
		border-left: 4px solid var(--warning-color, #f1c40f);
	}

	.notification.info {
		border-left: 4px solid var(--info-color, #3498db);
	}

	.notification-message {
		flex: 1;
		font-size: 0.9rem;
		font-weight: 500;
	}

	.notification-close {
		background: none;
		border: none;
		font-size: 1.2rem;
		cursor: pointer;
		color: var(--muted-color);
		padding: 0.25rem 0.5rem;
		border-radius: 4px;
		transition: all 0.2s;
	}

	.notification-close:hover {
		background: var(--row-hover-bg);
		color: var(--text-color);
	}

	@keyframes slideIn {
		from {
			transform: translateX(100%);
			opacity: 0;
		}
		to {
			transform: translateX(0);
			opacity: 1;
		}
	}

	/* Authentication styles */
	.auth-container {
		display: flex;
		align-items: center;
		justify-content: center;
		min-height: 60vh;
	}

	.auth-card {
		background: white;
		padding: 3rem;
		border-radius: 12px;
		box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
		text-align: center;
		max-width: 500px;
		width: 100%;
	}

	.auth-card h2 {
		margin: 0 0 1rem 0;
		color: var(--text-color);
		font-size: 2rem;
	}

	.auth-card p {
		color: var(--muted-color);
		font-size: 1.1rem;
		margin-bottom: 2rem;
		line-height: 1.6;
	}

	.auth-actions {
		display: flex;
		gap: 1rem;
		justify-content: center;
		flex-wrap: wrap;
	}

	.login-form {
		display: flex;
		flex-direction: column;
		gap: 1rem;
		margin-bottom: 1.5rem;
	}

	.form-group {
		display: flex;
		flex-direction: column;
		gap: 0.5rem;
		text-align: left;
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
		background: var(--input-bg, #f8f9fa);
		color: var(--text-color);
	}

	.form-group input:focus {
		outline: none;
		border-color: var(--primary-color, #3498db);
		box-shadow: 0 0 0 3px rgba(52, 152, 219, 0.1);
	}

	.signup-link {
		margin-top: 1rem;
		color: var(--muted-color);
	}

	.signup-link a {
		color: var(--primary-color, #3498db);
		text-decoration: none;
		font-weight: 600;
	}

	.signup-link a:hover {
		text-decoration: underline;
	}

	.btn {
		padding: 0.75rem 2rem;
		border: none;
		border-radius: 8px;
		font-size: 1rem;
		font-weight: 600;
		cursor: pointer;
		transition: all 0.2s;
		text-decoration: none;
		display: inline-block;
	}

	.btn-primary {
		background: var(--primary-color, #3498db);
		color: white;
	}

	.btn-primary:hover {
		background: var(--primary-hover, #2980b9);
		transform: translateY(-1px);
	}

	.btn-secondary {
		background: transparent;
		color: var(--text-color);
		border: 2px solid var(--border-color);
	}

	.btn-secondary:hover {
		background: var(--row-hover-bg);
		border-color: var(--text-color);
	}

	/* Responsive design */
	@media (max-width: 768px) {
		.main-content {
			padding: 1rem;
		}

		.auth-card {
			padding: 2rem 1.5rem;
		}

		.auth-card h2 {
			font-size: 1.5rem;
		}

		.auth-card p {
			font-size: 1rem;
		}

		.auth-actions {
			flex-direction: column;
			align-items: center;
		}

		.btn {
			width: 100%;
			max-width: 200px;
		}

		.notification {
			min-width: 250px;
			font-size: 0.8rem;
		}
	}
</style>
